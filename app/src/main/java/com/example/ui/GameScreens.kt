package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SupportTicket
import com.example.data.Transaction
import com.example.data.User
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MainAppContent(viewModel: AviatorViewModel) {
    val currentScreen = viewModel.currentScreen

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            "LOGIN" -> LoginScreen(viewModel)
            "ADMIN" -> AdminScreen(viewModel)
            else -> UserMainScaffold(viewModel, currentScreen)
        }
    }
}

// --- USER LAYOUT SCAFFOLD (Bottom Navigation for App Tabs) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScaffold(viewModel: AviatorViewModel, activeScreen: String) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val settings by viewModel.gameSettings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightSurface,
                    titleContentColor = TextPrimary
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.FlightTakeoff,
                                contentDescription = "Logo",
                                tint = AviationRed,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tower Aviator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        // Wallet Balance Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MidnightSurfaceLight)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = GoldAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$${String.format("%.2f", currentUser?.balance ?: 0.0)}",
                                color = GoldAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MidnightSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeScreen == "GAME",
                    onClick = { viewModel.navigateTo("GAME") },
                    icon = { Icon(Icons.Filled.Flight, contentDescription = "Game") },
                    label = { Text("Game", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AviationRed,
                        selectedTextColor = AviationRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = MidnightSurfaceLight
                    )
                )
                NavigationBarItem(
                    selected = activeScreen == "REWARDS",
                    onClick = { viewModel.navigateTo("REWARDS") },
                    icon = { Icon(Icons.Filled.CardGiftcard, contentDescription = "Rewards") },
                    label = { Text("Rewards", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AviationRed,
                        selectedTextColor = AviationRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = MidnightSurfaceLight
                    )
                )
                NavigationBarItem(
                    selected = activeScreen == "PROFILE",
                    onClick = { viewModel.navigateTo("PROFILE") },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Wallet", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AviationRed,
                        selectedTextColor = AviationRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = MidnightSurfaceLight
                    )
                )
                NavigationBarItem(
                    selected = activeScreen == "SUPPORT",
                    onClick = { viewModel.navigateTo("SUPPORT") },
                    icon = { Icon(Icons.Filled.HeadsetMic, contentDescription = "Support") },
                    label = { Text("Support", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AviationRed,
                        selectedTextColor = AviationRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = MidnightSurfaceLight
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Admin Global Broadcast Banner (ticker)
            if (settings.adminBroadcast.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AviationRed.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = "Alert",
                        tint = AviationRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = settings.adminBroadcast,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Route content
            when (activeScreen) {
                "GAME" -> GameScreen(viewModel)
                "REWARDS" -> RewardsScreen(viewModel)
                "PROFILE" -> ProfileScreen(viewModel)
                "SUPPORT" -> SupportScreen(viewModel)
            }
        }
    }
}

// ==========================================
// 1. LOGIN & REGISTRATION SCREEN (With Admin Entrance)
// ==========================================
@Composable
fun LoginScreen(viewModel: AviatorViewModel) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showAdminDialog by remember { mutableStateOf(false) }
    var adminPassword by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg)
            .padding(24.dp)
    ) {
        // Admin entrance in the top corner (Requirement)
        IconButton(
            onClick = { showAdminDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .testTag("admin_panel_trigger")
        ) {
            Icon(
                imageVector = Icons.Outlined.AdminPanelSettings,
                contentDescription = "Admin Gate",
                tint = TextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AviationRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FlightTakeoff,
                    contentDescription = "Plane Icon",
                    tint = AviationRed,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "TOWER AVIATOR",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 2.sp
            )

            Text(
                text = if (isRegisterMode) "Create an account to start earning" else "Sign in with phone and password",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AviationRed,
                    unfocusedBorderColor = MidnightSurfaceLight,
                    focusedContainerColor = MidnightSurface,
                    unfocusedContainerColor = MidnightSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_phone_input")
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AviationRed,
                    unfocusedBorderColor = MidnightSurfaceLight,
                    focusedContainerColor = MidnightSurface,
                    unfocusedContainerColor = MidnightSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            // Error Display
            if (viewModel.loginError != null) {
                Text(
                    text = viewModel.loginError ?: "",
                    color = AviationRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Submit Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = viewModel.loginOrRegister(phone, password)
                        if (success) {
                            Toast.makeText(context, "Welcome to Tower Aviator!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AviationRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isRegisterMode) "REGISTER & PLAY" else "LOGIN TO PLAY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Toggle Mode
            TextButton(
                onClick = { isRegisterMode = !isRegisterMode }
            ) {
                Text(
                    text = if (isRegisterMode) "Already have an account? Login" else "Don't have an account? Register Now",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        // Footer Brand
        Text(
            text = "Secure Demo Wallet Mode Active",
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
        )
    }

    // --- Admin panel login gate dialog ---
    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Admin Gate",
                        tint = AviationRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Authorization", color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Please enter the master admin password to access control settings.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Admin Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AviationRed,
                            unfocusedBorderColor = MidnightSurfaceLight,
                            focusedContainerColor = MidnightSurface,
                            unfocusedContainerColor = MidnightSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val auth = viewModel.loginAsAdmin(adminPassword)
                        if (auth) {
                            showAdminDialog = false
                            Toast.makeText(context, "Admin Access Granted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Access Denied!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AviationRed)
                ) {
                    Text("AUTHORIZE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = MidnightSurface
        )
    }
}

// ==========================================
// 2. GAME PLAY SCREEN (Aviator Core Mechanics)
// ==========================================
@Composable
fun GameScreen(viewModel: AviatorViewModel) {
    val state = viewModel.gameState
    val history by viewModel.pastCrashMultipliers

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Multiplier history tape ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History:",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
            history.forEach { mult ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (mult >= 10.0) GoldAmber.copy(alpha = 0.2f)
                            else if (mult >= 2.0) AviatorGreen.copy(alpha = 0.2f)
                            else MidnightSurfaceLight
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${String.format("%.2f", mult)}x",
                        color = if (mult >= 10.0) GoldAmber else if (mult >= 2.0) AviatorGreen else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Core Graph Canvas Panel ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MidnightSurface)
                .border(1.dp, MidnightSurfaceLight, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Canvas for lines and plane animation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw standard coordinate grid lines
                val gridStroke = Stroke(width = 1f)
                val gridColor = MidnightSurfaceLight
                
                // Horizontal lines
                for (i in 1..4) {
                    val y = canvasHeight * i / 5
                    drawLine(gridColor, Offset(0f, y), Offset(canvasWidth, y), strokeWidth = 1f)
                }
                // Vertical lines
                for (i in 1..4) {
                    val x = canvasWidth * i / 5
                    drawLine(gridColor, Offset(x, 0f), Offset(x, canvasHeight), strokeWidth = 1f)
                }

                when (state) {
                    is GameState.Climbing -> {
                        // Progress calculation (limit curve to make it fit nicely)
                        val progress = ((state.currentMultiplier - 1.0) / 10.0).coerceIn(0.0, 1.0)
                        
                        // Bezier path for climbing curve
                        val path = Path()
                        path.moveTo(0f, canvasHeight)
                        path.quadraticTo(
                            canvasWidth * 0.5f, canvasHeight,
                            canvasWidth * progress.toFloat(), canvasHeight - (canvasHeight * 0.8f * progress.toFloat())
                        )
                        
                        // Draw red line glow undercurve
                        drawPath(
                            path = path,
                            color = AviationRed,
                            style = Stroke(width = 6f)
                        )
                    }
                    else -> {}
                }
            }

            // Live Content Overlay
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (state) {
                    is GameState.Idle -> {
                        CircularProgressIndicator(color = AviationRed)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Connecting Game Server...", color = TextSecondary, fontSize = 14.sp)
                    }
                    is GameState.Waiting -> {
                        Text(
                            text = "NEXT ROUND IN",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.1fs", state.secondsRemaining),
                            color = GoldAmber,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Place your bet below!",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    is GameState.Climbing -> {
                        Text(
                            text = "${String.format("%.2f", state.currentMultiplier)}x",
                            color = TextPrimary,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Flight,
                                contentDescription = "Climbing",
                                tint = AviationRed,
                                modifier = Modifier
                                    .size(24.dp)
                                    .animateContentSize()
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "TOWER CLIMBING",
                                color = AviationRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                    is GameState.Crashed -> {
                        Text(
                            text = "FLEW AWAY",
                            color = AviationRed,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Crashed @ ${String.format("%.2f", state.crashedMultiplier)}x",
                            color = TextSecondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- Interactive Betting Control Panel ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header of Betting Board
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Bet Board",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    
                    // Auto-cashout setting
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Auto Out: ",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MidnightBg)
                                .clickable {
                                    // simple rotation list: null -> 1.5 -> 2.0 -> 5.0 -> null
                                    viewModel.autoCashoutMultiplier = when (viewModel.autoCashoutMultiplier) {
                                        null -> 1.50
                                        1.50 -> 2.00
                                        2.00 -> 5.00
                                        else -> null
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (viewModel.autoCashoutMultiplier == null) "MANUAL" else "${viewModel.autoCashoutMultiplier}x",
                                color = if (viewModel.autoCashoutMultiplier == null) TextSecondary else GoldAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bet Selector with Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Bet Amount (min $16):", fontSize = 11.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MidnightBg)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    val newBet = (viewModel.activeBetAmount - 10).coerceAtLeast(16.0)
                                    viewModel.activeBetAmount = newBet
                                }
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Reduce", tint = TextPrimary)
                            }

                            Text(
                                text = "$${viewModel.activeBetAmount.toInt()}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )

                            IconButton(
                                onClick = {
                                    viewModel.activeBetAmount += 10
                                }
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Increase", tint = TextPrimary)
                            }
                        }
                    }

                    // Quick select increments
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            QuickBetChip(label = "50") { viewModel.activeBetAmount = 50.0 }
                            QuickBetChip(label = "100") { viewModel.activeBetAmount = 100.0 }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            QuickBetChip(label = "200") { viewModel.activeBetAmount = 200.0 }
                            QuickBetChip(label = "500") { viewModel.activeBetAmount = 500.0 }
                        }
                    }
                }

                // Main Action Button
                if (viewModel.isBetPlacedInCurrentRound) {
                    if (viewModel.hasCashedOutInCurrentRound) {
                        // Cashed Out Successfully
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AviatorGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CASHED OUT!",
                                    color = AviatorGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "+$${String.format("%.2f", viewModel.currentRoundWinAmount)}",
                                    color = AviatorGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else if (state is GameState.Climbing) {
                        // Active climbing, can CASH OUT NOW
                        Button(
                            onClick = {
                                viewModel.cashOut(state.currentMultiplier)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AviatorGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "CASH OUT",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Take $${String.format("%.2f", viewModel.activeBetAmount * state.currentMultiplier)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Placed but waiting or crashed
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(disabledContainerColor = MidnightSurfaceLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "BET LOCKED (WAITING ROUND)",
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Place bet button
                    val isWaiting = state is GameState.Waiting
                    val alreadyPlaced = viewModel.hasPlacedBetForNextRound

                    Button(
                        onClick = {
                            if (alreadyPlaced) {
                                viewModel.cancelBet()
                            } else {
                                viewModel.placeBet()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (alreadyPlaced) AviationRed.copy(alpha = 0.6f) else AviationRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (alreadyPlaced) "CANCEL BET ($${viewModel.activeBetAmount.toInt()})"
                            else "BET FOR NEXT ROUND ($${viewModel.activeBetAmount.toInt()})",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickBetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MidnightSurfaceLight)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$$label",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==========================================
// 3. REWARDS SCREEN (VIP, Daily Claims, Share Link, Promo Codes)
// ==========================================
@Composable
fun RewardsScreen(viewModel: AviatorViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var promoInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- VIP Level Status Module ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("VIP Club Status", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextMuted)
                        Text(
                            "Level ${currentUser?.vipLevel ?: 1} / 50",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldAmber
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.MilitaryTech,
                        contentDescription = "VIP Badge",
                        tint = GoldAmber,
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Progress Bar
                val progressFraction = ((currentUser?.vipLevel ?: 1).toFloat() / 50f).coerceIn(0f, 1f)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = GoldAmber,
                        trackColor = MidnightBg
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("VIP 1 (Daily $20)", fontSize = 11.sp, color = TextSecondary)
                        Text("VIP 50 (Daily $1000)", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Divider(color = MidnightSurfaceLight)

                // Actions: Claim Daily Reward or Upgrade Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily claim button
                    val available = viewModel.isDailyRewardAvailable()
                    Button(
                        onClick = {
                            viewModel.claimDailyVipReward()
                            Toast.makeText(context, "Daily VIP Reward Claimed!", Toast.LENGTH_SHORT).show()
                        },
                        enabled = available,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AviatorGreen,
                            disabledContainerColor = MidnightSurfaceLight
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (available) "CLAIM DAILY" else "CLAIMED TODAY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                "Rewards: +$${(currentUser?.vipLevel ?: 1) * 20}",
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Upgrade VIP
                    val cost = viewModel.nextVipUpgradeCost
                    Button(
                        onClick = {
                            if ((currentUser?.balance ?: 0.0) >= cost) {
                                viewModel.upgradeVipLevel()
                                Toast.makeText(context, "VIP Status Upgraded!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Insufficient balance!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = (currentUser?.vipLevel ?: 1) < 50,
                        colors = ButtonDefaults.buttonColors(containerColor = AviationRed),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UPGRADE VIP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Cost: $${cost.toInt()}", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // --- Promo Code Redemption Panel ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Redeem Promo Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promoInput,
                        onValueChange = { promoInput = it },
                        placeholder = { Text("Enter Promo Code", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AviationRed,
                            unfocusedBorderColor = MidnightSurfaceLight,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            viewModel.claimPromoCode(promoInput)
                            promoInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AviationRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Text("CLAIM")
                    }
                }

                if (viewModel.promoMessage != null) {
                    Text(
                        text = viewModel.promoMessage ?: "",
                        color = if (viewModel.promoSuccess) AviatorGreen else AviationRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- Share & Earn (Copy and invitation rewards) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share Logo",
                    tint = AviationRed,
                    modifier = Modifier.size(36.dp)
                )

                Text(
                    "Invite Friends & Earn",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )

                Text(
                    "Get a continuous bonus of $250 immediately for every friend you share your referral code with. Let's grow the community!",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        viewModel.shareWithFriends()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AviationRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GENERATE 100% WORKING LINK", fontWeight = FontWeight.Bold)
                }

                // Loading generator spinner
                if (viewModel.isGeneratingShareLink) {
                    CircularProgressIndicator(color = AviationRed, modifier = Modifier.size(24.dp))
                }

                // Generated link outcome
                viewModel.generatedShareLink?.let { link ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MidnightBg)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = link,
                            color = GoldAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(link))
                                    Toast.makeText(context, "Link Copied!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MidnightSurfaceLight)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("COPY", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.claimReferralReward()
                                    Toast.makeText(context, "+$250 Referral Balance Added!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AviatorGreen)
                            ) {
                                Icon(Icons.Filled.Done, contentDescription = "Simulate Share", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SIMULATE JOIN (+250)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. WALLET & DEPOSIT/WITHDRAW (Profile Screen)
// ==========================================
@Composable
fun ProfileScreen(viewModel: AviatorViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var amountInput by remember { mutableStateOf("") }
    var accountInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("UPI / EasyPaisa") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- User Core Stats ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(AviationRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Avatar", tint = AviationRed, modifier = Modifier.size(36.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Phone: ${currentUser?.phoneNumber ?: ""}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "VIP Level: ${currentUser?.vipLevel ?: 1}",
                            color = GoldAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Friends Invited: ${currentUser?.referCount ?: 0}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = { viewModel.navigateTo("LOGIN") }) {
                    Icon(Icons.Filled.Logout, contentDescription = "Log Out", tint = AviationRed)
                }
            }
        }

        // --- Deposit / Withdraw Form Panel ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Deposit & Withdraw (Real Ledger)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )

                // Select Method Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("UPI / EasyPaisa", "JazzCash / Bank").forEach { method ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedMethod == method) AviationRed else MidnightBg)
                                .clickable { selectedMethod = method }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method,
                                color = if (selectedMethod == method) Color.White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Amount Text Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AviationRed,
                        unfocusedBorderColor = MidnightSurfaceLight,
                        focusedContainerColor = MidnightBg,
                        unfocusedContainerColor = MidnightBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Mobile Wallet / Card details field
                OutlinedTextField(
                    value = accountInput,
                    onValueChange = { accountInput = it },
                    label = { Text("Receiver UPI ID / Number / Details") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AviationRed,
                        unfocusedBorderColor = MidnightSurfaceLight,
                        focusedContainerColor = MidnightBg,
                        unfocusedContainerColor = MidnightBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Transaction buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // DEPOSIT Action
                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull()
                            if (amt == null || amt <= 0 || accountInput.isBlank()) {
                                Toast.makeText(context, "Please enter valid deposit parameters", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.submitDeposit(amt, "$selectedMethod: $accountInput")
                                amountInput = ""
                                accountInput = ""
                                Toast.makeText(context, "Deposit Requested! Admin will verify.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AviatorGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("DEPOSIT", fontWeight = FontWeight.Bold)
                    }

                    // WITHDRAW Action
                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull()
                            if (amt == null || amt <= 0 || accountInput.isBlank()) {
                                Toast.makeText(context, "Please enter valid withdrawal parameters", Toast.LENGTH_SHORT).show()
                            } else {
                                val err = viewModel.submitWithdraw(amt, "$selectedMethod: $accountInput")
                                if (err != null) {
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                } else {
                                    amountInput = ""
                                    accountInput = ""
                                    Toast.makeText(context, "Withdrawal Requested! Funds locked.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AviationRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("WITHDRAW", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- User Transaction History Log ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Your Wallet History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )

                if (transactions.isEmpty()) {
                    Text(
                        text = "No pending or historic transactions found.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        transactions.take(5).forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MidnightBg)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${tx.type} - $${tx.amount.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (tx.type == "DEPOSIT") AviatorGreen else AviationRed,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = tx.paymentDetails,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (tx.status) {
                                                "PENDING" -> GoldAmber.copy(alpha = 0.15f)
                                                "APPROVED" -> AviatorGreen.copy(alpha = 0.15f)
                                                else -> AviationRed.copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tx.status,
                                        color = when (tx.status) {
                                            "PENDING" -> GoldAmber
                                            "APPROVED" -> AviatorGreen
                                            else -> AviationRed
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. CUSTOMER SERVICE TICKET CHAT (Support Screen)
// ==========================================
@Composable
fun SupportScreen(viewModel: AviatorViewModel) {
    val tickets by viewModel.userTickets.collectAsStateWithLifecycle()
    var ticketMsg by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.SupportAgent, contentDescription = "Agent", tint = AviationRed, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Interactive Customer Support", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("We answer questions about withdraws, deposits & codes.", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // List of chat tickets
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            reverseLayout = true
        ) {
            if (tickets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No support tickets created yet.\nType your issue below to get assistance!",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(tickets) { ticket ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MidnightSurface)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Your Query:", fontWeight = FontWeight.Bold, color = AviationRed, fontSize = 12.sp)
                            Text(
                                "ID: #${ticket.id}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Text(ticket.message, color = TextPrimary, fontSize = 13.sp)

                        if (ticket.reply != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MidnightBg)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("Official Answer:", fontWeight = FontWeight.Bold, color = AviatorGreen, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(ticket.reply, color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        } else {
                            Text(
                                "Waiting for administrator reply...",
                                color = GoldAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = ticketMsg,
                onValueChange = { ticketMsg = it },
                placeholder = { Text("Ask about deposits, withdraws...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AviationRed,
                    unfocusedBorderColor = MidnightSurfaceLight,
                    focusedContainerColor = MidnightSurface,
                    unfocusedContainerColor = MidnightSurface
                ),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (ticketMsg.isNotBlank()) {
                        viewModel.sendSupportTicket(ticketMsg)
                        ticketMsg = ""
                        Toast.makeText(context, "Feedback sent to Admin!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AviationRed)
                    .size(48.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// ==========================================
// 6. POWERFUL ADMIN CONTROL PANEL SCREEN
// ==========================================
@Composable
fun AdminScreen(viewModel: AviatorViewModel) {
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val tickets by viewModel.allTickets.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val promoCodes by viewModel.allPromoCodes.collectAsStateWithLifecycle()
    val settings by viewModel.gameSettings.collectAsStateWithLifecycle()

    var activeAdminTab by remember { mutableStateOf("GAME_CONTROL") }
    var promoCodeInput by remember { mutableStateOf("") }
    var promoRewardInput by remember { mutableStateOf("") }
    var broadcastInput by remember { mutableStateOf("") }
    var forcedMultInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AviationRed),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Security, contentDescription = "Admin")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Master Control Panel", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Button(
                            onClick = { viewModel.navigateTo("LOGIN") },
                            colors = ButtonDefaults.buttonColors(containerColor = MidnightBg),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("EXIT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MidnightSurface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeAdminTab == "GAME_CONTROL",
                    onClick = { activeAdminTab = "GAME_CONTROL" },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Game Control") },
                    label = { Text("Game", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AviationRed,
                        selectedTextColor = AviationRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = MidnightSurfaceLight
                    )
                )
                NavigationBarItem(
                    selected = activeAdminTab == "PAYMENTS",
                    onClick = { activeAdminTab = "PAYMENTS" },
                    icon = { Icon(Icons.Filled.Payments, contentDescription = "Payments") },
                    label = { Text("Payments", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AviationRed,
                        selectedTextColor = AviationRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = MidnightSurfaceLight
                    )
                )
                NavigationBarItem(
                    selected = activeAdminTab == "USERS",
                    onClick = { activeAdminTab = "USERS" },
                    icon = { Icon(Icons.Filled.People, contentDescription = "Users") },
                    label = { Text("Users", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AviationRed,
                        selectedTextColor = AviationRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = MidnightSurfaceLight
                    )
                )
                NavigationBarItem(
                    selected = activeAdminTab == "TICKETS",
                    onClick = { activeAdminTab = "TICKETS" },
                    icon = { Icon(Icons.Filled.QuestionAnswer, contentDescription = "Tickets") },
                    label = { Text("Feedback", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AviationRed,
                        selectedTextColor = AviationRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = MidnightSurfaceLight
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MidnightBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (activeAdminTab) {
                "GAME_CONTROL" -> {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // TOWER FLIGHT rigging overrides (CRITICAL REQUIREMENT)
                        Card(colors = CardDefaults.cardColors(containerColor = MidnightSurface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Aviation Crash Multiplier Override", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(
                                    "Force the climbing plane to crash at a specific target. Empty out the setting to resume authentic random flight logic.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = forcedMultInput,
                                        onValueChange = { forcedMultInput = it },
                                        placeholder = { Text("e.g. 1.00 or 10.00", color = TextMuted) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = AviationRed,
                                            unfocusedBorderColor = MidnightSurfaceLight,
                                            focusedContainerColor = MidnightBg,
                                            unfocusedContainerColor = MidnightBg
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Button(
                                        onClick = {
                                            val mult = forcedMultInput.toDoubleOrNull()
                                            viewModel.adminSetForcedCrashMultiplier(mult)
                                            Toast.makeText(context, "Game crash rate updated!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AviationRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("SET")
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.adminSetForcedCrashMultiplier(null)
                                            forcedMultInput = ""
                                            Toast.makeText(context, "Natural random mode active", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MidnightSurfaceLight),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("RESET")
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Active Override Status: ", fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        text = if (settings.forcedCrashMultiplier != null) "${settings.forcedCrashMultiplier}x (FORCED)" else "RANDOM FLIGHT (NATURAL)",
                                        color = if (settings.forcedCrashMultiplier != null) AviationRed else AviatorGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Broadcast Announcement Text Banner
                        Card(colors = CardDefaults.cardColors(containerColor = MidnightSurface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Global Notification Banner", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = broadcastInput,
                                        onValueChange = { broadcastInput = it },
                                        placeholder = { Text("Announce VIP levels, payouts...", color = TextMuted) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = AviationRed,
                                            unfocusedBorderColor = MidnightSurfaceLight,
                                            focusedContainerColor = MidnightBg,
                                            unfocusedContainerColor = MidnightBg
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.adminSetBroadcastMessage(broadcastInput)
                                            Toast.makeText(context, "Broadcast message updated!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AviationRed)
                                    ) {
                                        Text("SEND")
                                    }
                                }
                            }
                        }

                        // Promo Codes Creation (CRITICAL REQUIREMENT)
                        Card(colors = CardDefaults.cardColors(containerColor = MidnightSurface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Issue Reward Promo Codes", fontWeight = FontWeight.Bold, color = TextPrimary)
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = promoCodeInput,
                                        onValueChange = { promoCodeInput = it },
                                        placeholder = { Text("Code e.g. TOWER500", color = TextMuted) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = AviationRed,
                                            unfocusedBorderColor = MidnightSurfaceLight,
                                            focusedContainerColor = MidnightBg,
                                            unfocusedContainerColor = MidnightBg
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = promoRewardInput,
                                        onValueChange = { promoRewardInput = it },
                                        placeholder = { Text("Reward $", color = TextMuted) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = AviationRed,
                                            unfocusedBorderColor = MidnightSurfaceLight,
                                            focusedContainerColor = MidnightBg,
                                            unfocusedContainerColor = MidnightBg
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        val reward = promoRewardInput.toDoubleOrNull()
                                        if (promoCodeInput.isNotBlank() && reward != null && reward > 0) {
                                            viewModel.adminCreatePromoCode(promoCodeInput, reward)
                                            promoCodeInput = ""
                                            promoRewardInput = ""
                                            Toast.makeText(context, "Promo code issued!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Please enter valid fields", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AviatorGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("ISSUE CODE TO COMMUNITY", fontWeight = FontWeight.Bold)
                                }

                                Divider(color = MidnightSurfaceLight)

                                Text("Active Promo Codes:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (promoCodes.isEmpty()) {
                                    Text("No active promo codes present.", color = TextMuted, fontSize = 12.sp)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        promoCodes.forEach { promo ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MidnightBg)
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "${promo.code} (Value: $${promo.rewardAmount.toInt()})",
                                                    color = GoldAmber,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                                IconButton(
                                                    onClick = { viewModel.adminDeletePromoCode(promo.code) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AviationRed, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "PAYMENTS" -> {
                    // Manage Pending Deposits & Withdrawals (CRITICAL REQUIREMENT)
                    Text("Moderate Wallet Requests", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    
                    val pendingList = transactions.filter { it.status == "PENDING" }
                    if (pendingList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pending deposits or withdrawal claims present.", color = TextMuted)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                            items(pendingList) { tx ->
                                Card(colors = CardDefaults.cardColors(containerColor = MidnightSurface)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${tx.type} Request - $${tx.amount.toInt()}",
                                                fontWeight = FontWeight.Black,
                                                color = if (tx.type == "DEPOSIT") AviatorGreen else AviationRed,
                                                fontSize = 14.sp
                                            )
                                            Text("User: ${tx.userPhone}", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Text("Details: ${tx.paymentDetails}", color = TextSecondary, fontSize = 12.sp)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.adminApproveTransaction(tx.id)
                                                    Toast.makeText(context, "Request Approved!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = AviatorGreen),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("APPROVE", fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.adminRejectTransaction(tx.id)
                                                    Toast.makeText(context, "Request Rejected!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = AviationRed),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("REJECT", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "USERS" -> {
                    // Manage Users list (BAN / UNBAN - REQUIREMENT)
                    Text("User Accounts Moderation", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                        items(users) { u ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MidnightSurface)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(u.phoneNumber, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        "Bal: $${String.format("%.2f", u.balance)} | VIP: ${u.vipLevel}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                if (u.isBanned) {
                                    Button(
                                        onClick = { viewModel.adminUnbanUser(u.phoneNumber) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AviatorGreen),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("UNBAN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.adminBanUser(u.phoneNumber) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AviationRed),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("BAN USER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                "TICKETS" -> {
                    // Feedback & Support Tickets replies (CRITICAL REQUIREMENT)
                    Text("Customer Service Inbox", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    
                    if (tickets.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No support tickets or feedbacks from users.", color = TextMuted)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                            items(tickets) { t ->
                                var responseText by remember { mutableStateOf("") }
                                Card(colors = CardDefaults.cardColors(containerColor = MidnightSurface)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("From: ${t.userPhone}", fontWeight = FontWeight.Bold, color = AviationRed, fontSize = 12.sp)
                                            IconButton(onClick = { viewModel.adminDeleteTicket(t.id) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Text(t.message, color = TextPrimary, fontSize = 13.sp)

                                        if (t.reply != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MidnightBg)
                                                    .padding(8.dp)
                                            ) {
                                                Text("Your Reply: ${t.reply}", color = TextSecondary, fontSize = 12.sp)
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = responseText,
                                                    onValueChange = { responseText = it },
                                                    placeholder = { Text("Type support answer...", color = TextMuted) },
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = TextPrimary,
                                                        unfocusedTextColor = TextPrimary,
                                                        focusedBorderColor = AviationRed,
                                                        unfocusedBorderColor = MidnightSurfaceLight,
                                                        focusedContainerColor = MidnightBg,
                                                        unfocusedContainerColor = MidnightBg
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                Button(
                                                    onClick = {
                                                        if (responseText.isNotBlank()) {
                                                            viewModel.adminReplyTicket(t.id, responseText)
                                                            responseText = ""
                                                            Toast.makeText(context, "Reply sent!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = AviatorGreen)
                                                ) {
                                                    Text("REPLY", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
