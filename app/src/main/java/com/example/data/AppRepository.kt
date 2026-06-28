package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- Users ---
    suspend fun getUserByPhone(phone: String): User? = appDao.getUserByPhone(phone)

    fun observeUserByPhone(phone: String): Flow<User?> = appDao.observeUserByPhone(phone)

    fun getAllUsers(): Flow<List<User>> = appDao.getAllUsers()

    suspend fun insertUser(user: User) = appDao.insertUser(user)

    suspend fun updateUser(user: User) = appDao.updateUser(user)

    suspend fun updateUserBanStatus(phone: String, isBanned: Boolean) =
        appDao.updateUserBanStatus(phone, isBanned)

    suspend fun updateUserBalance(phone: String, newBalance: Double) =
        appDao.updateUserBalance(phone, newBalance)

    // --- Support Tickets ---
    fun observeTicketsForUser(phone: String): Flow<List<SupportTicket>> =
        appDao.observeTicketsForUser(phone)

    fun observeAllTickets(): Flow<List<SupportTicket>> = appDao.observeAllTickets()

    suspend fun insertTicket(ticket: SupportTicket) = appDao.insertTicket(ticket)

    suspend fun updateTicket(ticket: SupportTicket) = appDao.updateTicket(ticket)

    suspend fun deleteTicketById(id: Int) = appDao.deleteTicketById(id)

    // --- Promo Codes ---
    suspend fun getPromoCode(code: String): PromoCode? = appDao.getPromoCode(code)

    fun observeAllPromoCodes(): Flow<List<PromoCode>> = appDao.observeAllPromoCodes()

    suspend fun insertPromoCode(promo: PromoCode) = appDao.insertPromoCode(promo)

    suspend fun deletePromoCode(promo: PromoCode) = appDao.deletePromoCode(promo)

    suspend fun deletePromoCodeByString(code: String) = appDao.deletePromoCodeByString(code)

    // --- Transactions ---
    fun observeTransactionsForUser(phone: String): Flow<List<Transaction>> =
        appDao.observeTransactionsForUser(phone)

    fun observeAllTransactions(): Flow<List<Transaction>> = appDao.observeAllTransactions()

    suspend fun insertTransaction(transaction: Transaction) =
        appDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) =
        appDao.updateTransaction(transaction)

    // --- Game Settings ---
    fun observeGameSettings(): Flow<GameSettings?> = appDao.observeGameSettings()

    suspend fun getGameSettings(): GameSettings? = appDao.getGameSettings()

    suspend fun insertGameSettings(settings: GameSettings) = appDao.insertGameSettings(settings)
}
