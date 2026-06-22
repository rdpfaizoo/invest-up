package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.DepositEntity
import com.example.data.database.PlanEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentApp(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Observe side-channel messages
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    if (currentUser == null) {
        AuthScreen(viewModel = viewModel)
    } else {
        val user = currentUser!!
        var selectedTab by remember { mutableStateOf("home") } // home, wallet, admin

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "InvestUp Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "InvestUp",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.logout() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Log Out",
                                tint = SupportGrayText
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = SlateWhiteBg
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = GrayInputBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(width = 0.5.dp, color = GrayBorder)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == "home",
                        onClick = { selectedTab = "home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            unselectedIconColor = SupportGrayText,
                            selectedTextColor = PrimaryBlue,
                            unselectedTextColor = SupportGrayText,
                            indicatorColor = ProCardBg
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == "wallet",
                        onClick = { selectedTab = "wallet" },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Deposit") },
                        label = { Text("Wallet", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            unselectedIconColor = SupportGrayText,
                            selectedTextColor = PrimaryBlue,
                            unselectedTextColor = SupportGrayText,
                            indicatorColor = ProCardBg
                        )
                    )
                    if (isAdminMode) {
                        NavigationBarItem(
                            selected = selectedTab == "admin",
                            onClick = { selectedTab = "admin" },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Admin Area") },
                            label = { Text("Admin", fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                unselectedIconColor = SupportGrayText,
                                selectedTextColor = PrimaryBlue,
                                unselectedTextColor = SupportGrayText,
                                indicatorColor = ProCardBg
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SlateWhiteBg)
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    "home" -> HomeScreen(viewModel = viewModel, user = user)
                    "wallet" -> WalletScreen(viewModel = viewModel, user = user)
                    "admin" -> AdminScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    var isLoginTab by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateWhiteBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(48.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "InvestUp Symbol",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "InvestUp",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                color = DarkText,
                fontSize = 32.sp,
                letterSpacing = (-1).sp
            )

            Text(
                text = "Secure daily profits simplified",
                fontSize = 14.sp,
                color = SupportGrayText,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GrayInputBg)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLoginTab) Color.White else Color.Transparent)
                        .clickable { isLoginTab = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLoginTab) PrimaryBlue else SupportGrayText,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!isLoginTab) Color.White else Color.Transparent)
                        .clickable { isLoginTab = false }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register",
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isLoginTab) PrimaryBlue else SupportGrayText,
                        fontSize = 15.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SlateWhiteBg),
                border = ButtonDefaults.outlinedButtonBorder,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isLoginTab) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                focusedLabelColor = PrimaryBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            loading = true
                            if (isLoginTab) {
                                viewModel.login(email, password) { success, msg ->
                                    loading = false
                                    viewModel.showMessage(msg)
                                }
                            } else {
                                viewModel.signup(email, name, password) { success, msg ->
                                    loading = false
                                    viewModel.showMessage(msg)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 8.dp),
                        enabled = !loading,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isLoginTab) "Sign In" else "Create Account",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, ProCardBg, RoundedCornerShape(20.dp))
                    .background(ProCardBg.copy(alpha = 0.2f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Promo",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Promo: Sign up today and get +100 PKR welcome deposit loaded as a starter gift!",
                        color = ProCardText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HomeScreen(viewModel: MainViewModel, user: com.example.data.database.UserEntity) {
    val plans by viewModel.userPlans.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome, ${user.name}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = DarkText,
                    lineHeight = 30.sp
                )
                Text(
                    text = "Select your Growth Plan",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    lineHeight = 32.sp
                )
                Text(
                    text = "Fixed daily returns for 90 days slot.",
                    fontSize = 13.sp,
                    color = SupportGrayText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = GrayInputBg),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ACCOUNT BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SupportGrayText,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${user.balance.toInt()} PKR",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(GrayBorder)
                    )

                    Column {
                        Text(
                            text = "TOTAL EARNINGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SupportGrayText,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${user.earnings.toInt()} PKR",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Available Growth Packages",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Plan 1: Basic
        item {
            PlanRow(
                tag = "BASIC",
                tagColor = SupportGrayText,
                priceString = "500 PKR",
                dailyRateString = "+100/Day",
                totalEst = "9,000 PKR",
                bgColor = BasicCardBg,
                textColor = DarkText,
                accentColor = PrimaryBlue,
                onBuyClick = {
                    viewModel.buyPlan(planName = "Basic", price = 500.0, dailyReturn = 100.0) { success, msg ->
                        viewModel.showMessage(msg)
                    }
                }
            )
        }

        // Plan 2: Pro
        item {
            PlanRow(
                tag = "PRO",
                tagColor = ProCardText,
                priceString = "1,000 PKR",
                dailyRateString = "+200/Day",
                totalEst = "18,000 PKR",
                bgColor = ProCardBg,
                textColor = ProCardText,
                accentColor = PrimaryBlue,
                isRecommended = true,
                onBuyClick = {
                    viewModel.buyPlan(planName = "Pro", price = 1000.0, dailyReturn = 200.0) { success, msg ->
                        viewModel.showMessage(msg)
                    }
                }
            )
        }

        // Plan 3: Premium
        item {
            PlanRow(
                tag = "PREMIUM",
                tagColor = PremiumCardText,
                priceString = "1,500 PKR",
                dailyRateString = "+300/Day",
                totalEst = "27,000 PKR",
                bgColor = PremiumCardBg,
                textColor = PremiumCardText,
                accentColor = Color(0xFFB3261E),
                onBuyClick = {
                    viewModel.buyPlan(planName = "Premium", price = 1500.0, dailyReturn = 300.0) { success, msg ->
                        viewModel.showMessage(msg)
                    }
                }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Portfolio (${plans.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                if (plans.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.fastForwardAllPlans() },
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fast-Forward 24H", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (plans.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, GrayBorder, RoundedCornerShape(24.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = SupportGrayText.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active investments yet",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = SupportGrayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Purchase a growth plan to start gathering automated returns daily.",
                            fontSize = 12.sp,
                            color = SupportGrayText.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(plans) { plan ->
                ActivePlanCard(plan = plan, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PlanRow(
    tag: String,
    tagColor: Color,
    priceString: String,
    dailyRateString: String,
    totalEst: String,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    isRecommended: Boolean = false,
    onBuyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tag,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagColor,
                            letterSpacing = 1.sp
                        )
                        if (isRecommended) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryBlue)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "POPULAR",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = priceString,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accentColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = dailyRateString,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total: $totalEst",
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBuyClick,
                colors = ButtonDefaults.buttonColors(containerColor = if (bgColor == ProCardBg) PrimaryBlue else Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Activate Plan",
                    fontWeight = FontWeight.Bold,
                    color = if (bgColor == ProCardBg) Color.White else PrimaryBlue,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ActivePlanCard(plan: PlanEntity, viewModel: MainViewModel) {
    val now = System.currentTimeMillis()
    val nextCollectTime = plan.lastCollectedTimestamp + 24 * 60 * 60 * 1000
    val isCooldowned = now < nextCollectTime
    val percentProgress = plan.daysCollected.toFloat() / plan.totalDays.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateWhiteBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when (plan.planName) {
                                    "Basic" -> BasicCardBg
                                    "Pro" -> ProCardBg
                                    else -> PremiumCardBg
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            modifier = Modifier.size(16.dp),
                            contentDescription = null,
                            tint = when (plan.planName) {
                                "Basic" -> SupportGrayText
                                "Pro" -> PrimaryBlue
                                else -> Color(0xFFB3261E)
                            }
                        )
                    }
                    Column {
                        Text(
                            text = "${plan.planName} Plan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DarkText
                        )
                        Text(
                            text = "+${plan.dailyReturn.toInt()} PKR/Day Returns",
                            fontSize = 11.sp,
                            color = SupportGrayText
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GrayInputBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${plan.daysCollected}/${plan.totalDays} Days",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SupportGrayText
                    )
                }
            }

            LinearProgressIndicator(
                progress = { percentProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = PrimaryBlue,
                trackColor = GrayBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.collectDailyReturn(plan) },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCooldowned) GrayInputBg else PrimaryBlue,
                        contentColor = if (isCooldowned) SupportGrayText else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isCooldowned) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCooldowned) "Collected Today" else "Collect Return",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { viewModel.testCollectImmediate(plan) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProCardBg,
                        contentColor = ProCardText
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Instant Collect",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WalletScreen(viewModel: MainViewModel, user: com.example.data.database.UserEntity) {
    val deposits by viewModel.userDeposits.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var amount by remember { mutableStateOf("") }
    var senderNo by remember { mutableStateOf("") }
    var txId by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "YOUR BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${user.balance.toInt()} PKR",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Submit a deposit code below to load your EasyPaisa cash instantly.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SlateWhiteBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF00AA5B)), // EasyPaisa Brand Green
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ep",
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "Deposit Account",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DarkText
                                )
                                Text(
                                    text = "Send PKR here",
                                    fontSize = 11.sp,
                                    color = SupportGrayText
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("03349000082"))
                                Toast.makeText(context, "Copied EasyPaisa Number!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Copy number", tint = PrimaryBlue)
                        }
                    }

                    Text(
                        text = "0334 9000082",
                        fontSize = 26.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Send balance from your EasyPaisa to our number above. Then record receipt information below.",
                        fontSize = 11.sp,
                        color = SupportGrayText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, GrayBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateWhiteBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Submit Cash Receipt",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Deposit Amount (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = senderNo,
                        onValueChange = { senderNo = it },
                        label = { Text("Your Sender Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        placeholder = { Text("e.g. 03341234567") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = txId,
                        onValueChange = { txId = it },
                        label = { Text("TxID / Receipt Code") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        placeholder = { Text("e.g. 987654321") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt <= 0.0) {
                                viewModel.showMessage("Please supply a valid payment amount")
                                return@Button
                            }
                            loading = true
                            viewModel.makeDeposit(amt, senderNo, txId) { success, msg ->
                                loading = false
                                viewModel.showMessage(msg)
                                if (success) {
                                    amount = ""
                                    senderNo = ""
                                    txId = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = !loading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Deposit Receipt", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        }

        item {
            val isAdmin by viewModel.isAdminMode.collectAsStateWithLifecycle()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ProCardBg.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sandbox Administration Tool",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ProCardText
                        )
                        Text(
                            text = "Toggle this module to authorize your pending EasyPaisa deposits instantly.",
                            fontSize = 10.sp,
                            color = ProCardText.copy(alpha = 0.8f),
                            lineHeight = 14.sp
                        )
                    }
                    Switch(
                        checked = isAdmin,
                        onCheckedChange = { viewModel.toggleAdminMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryBlue,
                            checkedTrackColor = ProCardBg
                        )
                    )
                }
            }
        }

        item {
            Text(
                text = "Deposit Statements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (deposits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GrayBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No prior statements. Deposit PKR above.",
                        fontSize = 13.sp,
                        color = SupportGrayText
                    )
                }
            }
        } else {
            items(deposits) { dep ->
                DepositRow(deposit = dep)
            }
        }
    }
}

@Composable
fun DepositRow(deposit: DepositEntity) {
    val formatter = remember { SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault()) }
    val dateStr = formatter.format(Date(deposit.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateWhiteBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${deposit.amount.toInt()} PKR",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "TxID: ${deposit.transactionId} • ${deposit.senderNumber}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SupportGrayText
                )
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = SupportGrayText.copy(alpha = 0.6f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (deposit.status) {
                            "Approved" -> Color(0xFFE8F5E9)
                            "Rejected" -> Color(0xFFFFEBEE)
                            else -> Color(0xFFFFF3E0)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = deposit.status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (deposit.status) {
                        "Approved" -> Color(0xFF2E7D32)
                        "Rejected" -> Color(0xFFC62828)
                        else -> Color(0xFFEF6C00)
                    }
                )
            }
        }
    }
}

@Composable
fun AdminScreen(viewModel: MainViewModel) {
    val allRecs by viewModel.allDeposits.collectAsStateWithLifecycle()
    val formatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Admin Sandbox Tools",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Observe and manually approve/decline EasyPaisa deposit receipts here.",
                    fontSize = 12.sp,
                    color = SupportGrayText
                )
            }
        }

        if (allRecs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Database registry is currently empty.",
                        color = SupportGrayText
                    )
                }
            }
        } else {
            items(allRecs) { rec ->
                val dateStr = formatter.format(Date(rec.timestamp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GrayBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateWhiteBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${rec.amount.toInt()} PKR",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Text(
                                    text = "Beneficiary: ${rec.userEmail}",
                                    fontSize = 11.sp,
                                    color = SupportGrayText
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (rec.status) {
                                            "Approved" -> Color(0xFFE8F5E9)
                                            "Rejected" -> Color(0xFFFFEBEE)
                                            else -> Color(0xFFFFF3E0)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = rec.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (rec.status) {
                                        "Approved" -> Color(0xFF2E7D32)
                                        "Rejected" -> Color(0xFFC62828)
                                        else -> Color(0xFFEF6C00)
                                    }
                                )
                            }
                        }

                        HorizontalDivider(color = GrayBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "EasyPaisa Sender: ${rec.senderNumber}",
                                    fontSize = 11.sp,
                                    color = SupportGrayText
                                )
                                Text(
                                    text = "TxID: ${rec.transactionId} • $dateStr",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = SupportGrayText
                                )
                            }
                        }

                        if (rec.status == "Pending") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.adminApproveDeposit(rec) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Text("Approve Receipt", style = MaterialTheme.typography.labelLarge, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.adminRejectDeposit(rec) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                                ) {
                                    Text("Decline", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
