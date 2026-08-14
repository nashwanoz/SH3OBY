package com.khamrnet.app.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.focusable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khamrnet.app.AppSection
import com.khamrnet.app.R
import com.khamrnet.app.ui.screens.BondsScreen
import com.khamrnet.app.ui.screens.CustomersScreen
import com.khamrnet.app.ui.screens.DashboardScreen
import com.khamrnet.app.ui.screens.InvoicesScreen
import com.khamrnet.app.ui.screens.PosScreen
import com.khamrnet.app.ui.screens.ProductsScreen
import com.khamrnet.app.ui.screens.ReportsScreen
import com.khamrnet.app.ui.screens.SettlementsScreen
import com.khamrnet.app.ui.screens.TransfersScreen
import com.khamrnet.app.ui.screens.UsersScreen
import com.khamrnet.app.ui.components.BondReceiptDialog
import com.khamrnet.app.ui.components.SaleReceiptDialog

@Composable
fun KhamrApp(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
    var lastBackPressAt by rememberSaveable { mutableStateOf(0L) }

    BackHandler(enabled = !showExitConfirmation) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressAt < 2_000L) {
            lastBackPressAt = 0L
            showExitConfirmation = true
        } else {
            lastBackPressAt = now
            Toast.makeText(context, "اضغط رجوع مرة أخرى للخروج", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.message, state.error) {
        (state.message ?: state.error)?.let { snackbarHostState.showSnackbar(it) }
        viewModel.clearMessage()
    }
    when {
        !state.ready -> LoadingScreen()
        state.user == null -> LoginScreen(state, viewModel)
        else -> MainShell(state, viewModel, snackbarHostState)
    }
    state.saleReceipt?.let { receipt ->
        SaleReceiptDialog(
            receipt = receipt,
            canWhatsapp = state.user?.role == "ADMIN" || state.user?.canWhatsapp == true,
            onDismiss = viewModel::clearSaleReceipt
        )
    }
    state.bondReceipt?.let { receipt ->
        BondReceiptDialog(
            receipt = receipt,
            canWhatsapp = state.user?.role == "ADMIN" || state.user?.canWhatsapp == true,
            onDismiss = viewModel::clearBondReceipt
        )
    }
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("الخروج من التطبيق") },
            text = { Text("هل تريد الخروج من التطبيق؟") },
            confirmButton = {
                TextButton(onClick = { (context as? Activity)?.finish() }) { Text("خروج") }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        Modifier.fillMaxSize().imePadding().background(
            Brush.verticalGradient(listOf(Color(0xFF102A43), Color(0xFF0F766E)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.khamernet_logo),
                contentDescription = "شعار خمر نت",
                modifier = Modifier.size(104.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(16.dp))
            Text("خمر نت", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("جاري تجهيز النظام محليًا…", color = Color.White.copy(alpha = .8f))
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(color = Color(0xFFD99A2B))
        }
    }
}

@Composable
private fun LoginScreen(state: AppUiState, viewModel: AppViewModel) {
    var userCode by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    val login = {
        focusManager.clearFocus()
        viewModel.login(userCode, password)
    }
    Box(
        Modifier.fillMaxSize().imePadding().background(
            Brush.verticalGradient(listOf(Color(0xFF102A43), Color(0xFF0F766E)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.khamernet_logo),
                contentDescription = "شعار خمر نت",
                modifier = Modifier.size(112.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(.94f),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("خمر نت", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF102A43))
                    Text("نظام المبيعات والمخزون", color = Color(0xFF0F766E))
                    Spacer(Modifier.height(28.dp))
                    OutlinedTextField(
                        value = userCode,
                        onValueChange = { userCode = it.filter(Char::isDigit) },
                        label = { Text("رقم المستخدم") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocusRequester.requestFocus() },
                            onDone = { passwordFocusRequester.requestFocus() }
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it.filter(Char::isDigit) },
                        label = { Text("كلمة المرور") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { if (userCode.isNotBlank() && password.isNotBlank()) login() }
                        )
                    )
                    Spacer(Modifier.height(22.dp))
                    Button(onClick = login, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = userCode.isNotBlank() && password.isNotBlank()) {
                        Text("دخول")
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("الحساب الافتراضي: رقم المستخدم 1 / كلمة المرور 1", color = Color.Gray, fontSize = 12.sp)
                    state.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("جميع الحقوق محفوظة Smart Link 2026", color = Color.White.copy(alpha = .86f), fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(state: AppUiState, viewModel: AppViewModel, snackbar: SnackbarHostState) {
    val isAdmin = state.user?.role == "ADMIN"
    val available = AppSection.values().filter { state.user?.canAccess(it.name) == true }
    val defaultSection = available.firstOrNull { it == AppSection.HOME }
        ?: available.firstOrNull { it == AppSection.POS }
        ?: available.firstOrNull()
        ?: AppSection.POS
    var selected by rememberSaveable { mutableStateOf(defaultSection.name) }
    var showSectionMenu by remember { mutableStateOf(false) }
    var invoiceCustomerId by rememberSaveable { mutableStateOf<Long?>(null) }
    var bondCustomerId by rememberSaveable { mutableStateOf<Long?>(null) }
    val section = available.firstOrNull { it.name == selected } ?: defaultSection
    val bottomItems = available.take(4)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(section.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Icon(
                        if (isAdmin) Icons.Default.Settings else Icons.Default.PointOfSale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    Text(state.user?.displayName ?: "", color = MaterialTheme.colorScheme.primary)
                    Box {
                        IconButton(onClick = { showSectionMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "الشاشات")
                        }
                        DropdownMenu(
                            expanded = showSectionMenu,
                            onDismissRequest = { showSectionMenu = false }
                        ) {
                            available.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    onClick = {
                                        selected = item.name
                                        showSectionMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = viewModel::logout) {
                        Icon(Icons.Default.Logout, contentDescription = "خروج")
                    }
                }
            )
        },
        bottomBar = {
            if (section != AppSection.HOME) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = section == item,
                            onClick = { selected = item.name },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Surface(Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
            when (section) {
                AppSection.HOME -> DashboardScreen(state) { selected = it.name }
                AppSection.POS -> PosScreen(state, viewModel, invoiceCustomerId) { invoiceCustomerId = null }
                AppSection.INVOICES -> InvoicesScreen(state) { selected = AppSection.POS.name }
                AppSection.REPORTS -> ReportsScreen(state)
                AppSection.PRODUCTS -> ProductsScreen(state, viewModel)
                AppSection.USERS -> UsersScreen(state, viewModel)
                AppSection.TRANSFERS -> TransfersScreen(state, viewModel)
                AppSection.CUSTOMERS -> CustomersScreen(
                    state = state,
                    viewModel = viewModel,
                    onIssueInvoice = { customer ->
                        invoiceCustomerId = customer.id
                        selected = AppSection.POS.name
                    },
                    onIssueBond = { customer ->
                        bondCustomerId = customer.id
                        selected = AppSection.BONDS.name
                    }
                )
                AppSection.BONDS -> BondsScreen(state, viewModel, bondCustomerId) { bondCustomerId = null }
                AppSection.SETTLEMENTS -> SettlementsScreen(state, viewModel)
            }
        }
    }
}