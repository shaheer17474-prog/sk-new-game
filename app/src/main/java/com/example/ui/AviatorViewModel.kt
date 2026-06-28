package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface GameState {
    object Idle : GameState
    data class Waiting(val secondsRemaining: Double) : GameState
    data class Climbing(val currentMultiplier: Double, val targetCrash: Double) : GameState
    data class Crashed(val crashedMultiplier: Double) : GameState
}

class AviatorViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // --- Authentication & Session ---
    var loggedInUserPhone by mutableStateOf<String?>(null)
        private set

    val currentUser: StateFlow<User?> = snapshotFlow { loggedInUserPhone }
        .flatMapLatest { phone ->
            if (phone != null) repository.observeUserByPhone(phone) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var isAdminLoggedIn by mutableStateOf(false)
        private set

    // --- Active UI Screens ---
    // Screens: "LOGIN", "GAME", "REWARDS", "PROFILE", "SUPPORT", "ADMIN"
    var currentScreen by mutableStateOf("LOGIN")
        private set

    fun navigateTo(screen: String) {
        if (screen == "LOGIN") {
            // Logout
            loggedInUserPhone = null
            isAdminLoggedIn = false
        }
        currentScreen = screen
    }

    // --- Aviator Game State ---
    private var gameJob: Job? = null
    var gameState: GameState by mutableStateOf(GameState.Idle)
        private set

    var pastCrashMultipliers = mutableStateOf<List<Double>>(
        listOf(1.20, 2.45, 1.05, 5.80, 1.12, 12.40, 1.88, 3.10)
    )
        private set

    // Betting states
    var activeBetAmount by mutableStateOf(100.0)
    var autoCashoutMultiplier by mutableStateOf<Double?>(null) // null means manual
    var hasPlacedBetForNextRound by mutableStateOf(false)
    var isBetPlacedInCurrentRound by mutableStateOf(false)
    var hasCashedOutInCurrentRound by mutableStateOf(false)
    var currentRoundWinAmount by mutableStateOf(0.0)

    // Admin game control settings observed from Room
    val gameSettings: StateFlow<GameSettings> = repository.observeGameSettings()
        .map { it ?: GameSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameSettings())

    // --- Admin Data Lists ---
    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTickets: StateFlow<List<SupportTicket>> = repository.observeAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<Transaction>> = repository.observeAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPromoCodes: StateFlow<List<PromoCode>> = repository.observeAllPromoCodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- User Support Chat ---
    val userTickets: StateFlow<List<SupportTicket>> = snapshotFlow { loggedInUserPhone }
        .flatMapLatest { phone ->
            if (phone != null) repository.observeTicketsForUser(phone) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- User Transactions (Deposits / Withdrawals) ---
    val userTransactions: StateFlow<List<Transaction>> = snapshotFlow { loggedInUserPhone }
        .flatMapLatest { phone ->
            if (phone != null) repository.observeTransactionsForUser(phone) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Referral / Share State ---
    var isGeneratingShareLink by mutableStateOf(false)
    var generatedShareLink by mutableStateOf<String?>(null)

    // --- Initialization ---
    init {
        // Start infinite Aviator game loop
        startGameLoop()
        
        // Seed default settings if database is empty
        viewModelScope.launch {
            if (repository.getGameSettings() == null) {
                repository.insertGameSettings(GameSettings())
            }
        }
    }

    // --- Login & Registration ---
    var loginError by mutableStateOf<String?>(null)

    suspend fun loginOrRegister(phone: String, pass: String): Boolean {
        loginError = null
        if (phone.isBlank() || pass.isBlank()) {
            loginError = "Please enter phone and password"
            return false
        }
        
        val user = repository.getUserByPhone(phone)
        if (user != null) {
            if (user.isBanned) {
                loginError = "This account is banned by the Administrator."
                return false
            }
            if (user.password != pass) {
                loginError = "Incorrect password."
                return false
            }
            loggedInUserPhone = phone
            navigateTo("GAME")
            return true
        } else {
            // Register as new user
            val newUser = User(
                phoneNumber = phone,
                password = pass,
                balance = 500.0, // starts with some free coins
                vipLevel = 1
            )
            repository.insertUser(newUser)
            loggedInUserPhone = phone
            navigateTo("GAME")
            return true
        }
    }

    fun loginAsAdmin(password: String): Boolean {
        loginError = null
        if (password == "Chotakhan00001") {
            isAdminLoggedIn = true
            navigateTo("ADMIN")
            return true
        } else {
            loginError = "Invalid Admin Password"
            return false
        }
    }

    // --- Aviator Game Loop Core ---
    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (true) {
                // 1. WAITING PHASE (Countdown for placing bets)
                var countdown = 6.0
                while (countdown > 0) {
                    gameState = GameState.Waiting(countdown)
                    delay(100)
                    countdown -= 0.1
                }

                // Move placed bet into current round
                if (hasPlacedBetForNextRound) {
                    isBetPlacedInCurrentRound = true
                    hasPlacedBetForNextRound = false
                    hasCashedOutInCurrentRound = false
                    currentRoundWinAmount = 0.0
                    
                    // Deduct bet amount from user's balance
                    loggedInUserPhone?.let { phone ->
                        val u = repository.getUserByPhone(phone)
                        if (u != null) {
                            val newBal = (u.balance - activeBetAmount).coerceAtLeast(0.0)
                            repository.updateUserBalance(phone, newBal)
                        }
                    }
                } else {
                    isBetPlacedInCurrentRound = false
                    hasCashedOutInCurrentRound = false
                }

                // 2. CLIMBING PHASE
                // Determine crash point
                var crashPoint = generateRandomCrashPoint()
                
                // Admin control override check
                val settings = repository.getGameSettings()
                if (settings?.forcedCrashMultiplier != null) {
                    crashPoint = settings.forcedCrashMultiplier
                }

                var currentMult = 1.0
                gameState = GameState.Climbing(currentMult, crashPoint)

                val tickMs = 50L
                while (currentMult < crashPoint) {
                    delay(tickMs)
                    // Multiplier growth formula: exponential speedup
                    val speed = if (currentMult < 2.0) 0.012 else if (currentMult < 5.0) 0.025 else 0.06
                    currentMult += speed * (1.0 + currentMult * 0.1)
                    
                    // Cap float formatting to 2 decimal places
                    currentMult = Math.round(currentMult * 100.0) / 100.0

                    gameState = GameState.Climbing(currentMult, crashPoint)

                    // Auto-cashout check
                    if (isBetPlacedInCurrentRound && !hasCashedOutInCurrentRound) {
                        val autoC = autoCashoutMultiplier
                        if (autoC != null && currentMult >= autoC && autoC <= crashPoint) {
                            cashOut(autoC)
                        }
                    }
                }

                // 3. CRASH PHASE
                gameState = GameState.Crashed(crashPoint)
                
                // Add to history tape
                val history = pastCrashMultipliers.value.toMutableList()
                history.add(0, crashPoint)
                if (history.size > 12) history.removeLast()
                pastCrashMultipliers.value = history

                // Clean up round
                isBetPlacedInCurrentRound = false
                delay(3000) // Stay in crashed state for 3 seconds before next round
            }
        }
    }

    private fun generateRandomCrashPoint(): Double {
        val rand = Random.nextDouble()
        return when {
            rand < 0.10 -> 1.00 // 10% instant crash
            rand < 0.85 -> {
                // 75% standard multiplier between 1.01 and 3.50
                val raw = 1.01 + Random.nextDouble() * 2.49
                Math.round(raw * 100.0) / 100.0
            }
            rand < 0.97 -> {
                // 12% high multiplier between 3.50 and 15.00
                val raw = 3.50 + Random.nextDouble() * 11.50
                Math.round(raw * 100.0) / 100.0
            }
            else -> {
                // 3% crazy multiplier up to 100x+
                val raw = 15.00 + Random.nextDouble() * 120.00
                Math.round(raw * 100.0) / 100.0
            }
        }
    }

    fun placeBet() {
        val phone = loggedInUserPhone ?: return
        viewModelScope.launch {
            val user = repository.getUserByPhone(phone) ?: return@launch
            if (user.balance >= activeBetAmount) {
                hasPlacedBetForNextRound = true
            }
        }
    }

    fun cancelBet() {
        hasPlacedBetForNextRound = false
    }

    fun cashOut(multiplier: Double) {
        if (!isBetPlacedInCurrentRound || hasCashedOutInCurrentRound) return
        val phone = loggedInUserPhone ?: return
        
        hasCashedOutInCurrentRound = true
        currentRoundWinAmount = activeBetAmount * multiplier
        currentRoundWinAmount = Math.round(currentRoundWinAmount * 100.0) / 100.0

        viewModelScope.launch {
            val user = repository.getUserByPhone(phone) ?: return@launch
            val newBal = user.balance + currentRoundWinAmount
            repository.updateUserBalance(phone, newBal)
        }
    }

    // --- VIP Levels (50 Levels) ---
    // Costs to upgrade to next VIP: Level * 250 coins
    val nextVipUpgradeCost: Double
        get() {
            val currentVip = currentUser.value?.vipLevel ?: 1
            return if (currentVip >= 50) 0.0 else currentVip * 250.0
        }

    fun upgradeVipLevel() {
        val phone = loggedInUserPhone ?: return
        viewModelScope.launch {
            val user = repository.getUserByPhone(phone) ?: return@launch
            if (user.vipLevel < 50) {
                val cost = nextVipUpgradeCost
                if (user.balance >= cost) {
                    val updatedUser = user.copy(
                        vipLevel = user.vipLevel + 1,
                        balance = user.balance - cost
                    )
                    repository.insertUser(updatedUser)
                }
            }
        }
    }

    // Daily Reward: VIP 1 gets 20. Every VIP level adds 20. E.g. VIP level * 20.
    fun claimDailyVipReward() {
        val phone = loggedInUserPhone ?: return
        viewModelScope.launch {
            val user = repository.getUserByPhone(phone) ?: return@launch
            val now = System.currentTimeMillis()
            // 24 hour check
            val oneDayMs = 24 * 60 * 60 * 1000L
            if (now - user.lastDailyRewardClaimed >= oneDayMs) {
                val reward = user.vipLevel * 20.0
                val updatedUser = user.copy(
                    balance = user.balance + reward,
                    lastDailyRewardClaimed = now
                )
                repository.insertUser(updatedUser)
            }
        }
    }

    fun isDailyRewardAvailable(): Boolean {
        val user = currentUser.value ?: return false
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        return (now - user.lastDailyRewardClaimed >= oneDayMs)
    }

    // --- Referral / Share Feature ---
    // Loads for 1.5s, then shows a copyable invite link. Clicking copy/share awards 250 coins!
    fun shareWithFriends() {
        isGeneratingShareLink = true
        generatedShareLink = null
        viewModelScope.launch {
            delay(1500) // Aesthetic visual loading spinner delay
            val phone = loggedInUserPhone ?: "guest"
            generatedShareLink = "https://toweraviator.com/join?ref=$phone"
            isGeneratingShareLink = false
        }
    }

    fun claimReferralReward() {
        val phone = loggedInUserPhone ?: return
        viewModelScope.launch {
            val user = repository.getUserByPhone(phone) ?: return@launch
            // Add 1 to referCount and credit 250.0 coins per friend shared
            val updatedUser = user.copy(
                referCount = user.referCount + 1,
                balance = user.balance + 250.0
            )
            repository.insertUser(updatedUser)
        }
    }

    // --- Customer Support Chat (Support tickets) ---
    fun sendSupportTicket(messageText: String) {
        val phone = loggedInUserPhone ?: return
        if (messageText.isBlank()) return
        viewModelScope.launch {
            val ticket = SupportTicket(
                userPhone = phone,
                message = messageText
            )
            repository.insertTicket(ticket)
        }
    }

    // --- Deposit & Withdrawals ---
    fun submitDeposit(amount: Double, paymentDetails: String) {
        val phone = loggedInUserPhone ?: return
        if (amount <= 0 || paymentDetails.isBlank()) return
        viewModelScope.launch {
            val tx = Transaction(
                userPhone = phone,
                type = "DEPOSIT",
                amount = amount,
                status = "PENDING",
                paymentDetails = paymentDetails
            )
            repository.insertTransaction(tx)
        }
    }

    fun submitWithdraw(amount: Double, paymentDetails: String): String? {
        val phone = loggedInUserPhone ?: return "Please login first"
        if (amount <= 0 || paymentDetails.isBlank()) return "Please enter valid fields"
        
        val user = currentUser.value ?: return "User profile not found"
        if (user.balance < amount) {
            return "Insufficient balance!"
        }
        
        viewModelScope.launch {
            // Instantly deduct balance for safety (lock funds)
            val newBal = user.balance - amount
            repository.updateUserBalance(phone, newBal)

            val tx = Transaction(
                userPhone = phone,
                type = "WITHDRAW",
                amount = amount,
                status = "PENDING",
                paymentDetails = paymentDetails
            )
            repository.insertTransaction(tx)
        }
        return null // success
    }

    // --- Promo Code Claims ---
    var promoMessage by mutableStateOf<String?>(null)
    var promoSuccess by mutableStateOf(false)

    fun claimPromoCode(code: String) {
        promoMessage = null
        promoSuccess = false
        val phone = loggedInUserPhone ?: return
        if (code.isBlank()) {
            promoMessage = "Please enter a code"
            return
        }

        viewModelScope.launch {
            val promo = repository.getPromoCode(code.trim().uppercase())
            if (promo == null) {
                promoMessage = "Invalid or expired promo code"
            } else if (promo.isClaimed) {
                promoMessage = "This promo code was already used"
            } else {
                val user = repository.getUserByPhone(phone) ?: return@launch
                // Credit user, mark promo as used (or delete it)
                val newBal = user.balance + promo.rewardAmount
                repository.updateUserBalance(phone, newBal)
                
                // In a single-user local state db, we can delete it or mark as claimed
                repository.insertPromoCode(promo.copy(isClaimed = true))
                
                promoSuccess = true
                promoMessage = "Congratulations! You claimed +$${promo.rewardAmount}!"
            }
        }
    }

    // --- Admin panel operations (Requires Admin password "Chotakhan00001") ---
    fun adminBanUser(phone: String) {
        viewModelScope.launch {
            repository.updateUserBanStatus(phone, isBanned = true)
            // If currently logged in user is banned, logout
            if (loggedInUserPhone == phone) {
                loggedInUserPhone = null
                currentScreen = "LOGIN"
            }
        }
    }

    fun adminUnbanUser(phone: String) {
        viewModelScope.launch {
            repository.updateUserBanStatus(phone, isBanned = false)
        }
    }

    fun adminDeleteTicket(id: Int) {
        viewModelScope.launch {
            repository.deleteTicketById(id)
        }
    }

    fun adminReplyTicket(ticketId: Int, replyText: String) {
        if (replyText.isBlank()) return
        viewModelScope.launch {
            val all = allTickets.value
            val ticket = all.find { it.id == ticketId } ?: return@launch
            val updated = ticket.copy(reply = replyText)
            repository.insertTicket(updated)
        }
    }

    fun adminApproveTransaction(txId: Int) {
        viewModelScope.launch {
            val txList = allTransactions.value
            val tx = txList.find { it.id == txId } ?: return@launch
            if (tx.status != "PENDING") return@launch

            val updatedTx = tx.copy(status = "APPROVED")
            repository.insertTransaction(updatedTx)

            // If DEPOSIT, credit user balance
            if (tx.type == "DEPOSIT") {
                val user = repository.getUserByPhone(tx.userPhone)
                if (user != null) {
                    val newBal = user.balance + tx.amount
                    repository.updateUserBalance(tx.userPhone, newBal)
                }
            }
            // For WITHDRAW, money was already deducted when they placed the withdraw request,
            // so we just confirm the status as APPROVED.
        }
    }

    fun adminRejectTransaction(txId: Int) {
        viewModelScope.launch {
            val txList = allTransactions.value
            val tx = txList.find { it.id == txId } ?: return@launch
            if (tx.status != "PENDING") return@launch

            val updatedTx = tx.copy(status = "REJECTED")
            repository.insertTransaction(updatedTx)

            // If WITHDRAW, return/refund the balance since it failed
            if (tx.type == "WITHDRAW") {
                val user = repository.getUserByPhone(tx.userPhone)
                if (user != null) {
                    val newBal = user.balance + tx.amount
                    repository.updateUserBalance(tx.userPhone, newBal)
                }
            }
        }
    }

    fun adminCreatePromoCode(code: String, reward: Double) {
        if (code.isBlank() || reward <= 0) return
        viewModelScope.launch {
            val promo = PromoCode(
                code = code.trim().uppercase(),
                rewardAmount = reward,
                isClaimed = false
            )
            repository.insertPromoCode(promo)
        }
    }

    fun adminDeletePromoCode(code: String) {
        viewModelScope.launch {
            repository.deletePromoCodeByString(code)
        }
    }

    fun adminSetForcedCrashMultiplier(mult: Double?) {
        viewModelScope.launch {
            val current = repository.getGameSettings() ?: GameSettings()
            repository.insertGameSettings(current.copy(forcedCrashMultiplier = mult))
        }
    }

    fun adminSetBroadcastMessage(msg: String) {
        viewModelScope.launch {
            val current = repository.getGameSettings() ?: GameSettings()
            repository.insertGameSettings(current.copy(adminBroadcast = msg))
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
    }
}

class AviatorViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AviatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AviatorViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
