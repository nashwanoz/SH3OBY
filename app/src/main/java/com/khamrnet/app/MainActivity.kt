package com.khamrnet.app

import android.app.Activity
import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.CustomerStatementRow
import com.khamrnet.app.data.InvoiceEntity
import com.khamrnet.app.data.InvoiceLineEntity
import com.khamrnet.app.data.ProductEntity
import com.khamrnet.app.data.SaleLineInput
import com.khamrnet.app.data.UserEntity
import com.khamrnet.app.data.UserPermissions
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.AppViewModel
import com.khamrnet.app.ui.BondReceipt
import com.khamrnet.app.ui.KhamrTheme
import com.khamrnet.app.ui.SaleReceipt
import com.khamrnet.app.util.PrintAndShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                KhamrTheme { KhamrApp(viewModel) }
            }
        }
    }
}

enum class AppSection(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HOME("الرئيسية", Icons.Default.Assessment),
    POS("فاتورة جديدة", Icons.Default.PointOfSale),
    INVOICES("فواتير المبيعات", Icons.Default.ReceiptLong),
    REPORTS("التقارير", Icons.Default.Assessment),
    PRODUCTS("بيانات الأصناف", Icons.Default.Inventory),
    USERS("المستخدمون", Icons.Default.People),
    TRANSFERS("تحويل مخزون", Icons.Default.SwapHoriz),
    CUSTOMERS("العملاء", Icons.Default.People),
    BONDS("السندات", Icons.Default.ReceiptLong),
    SETTLEMENTS("التصفية", Icons.Default.AccountBalance)
}

@Composable
private fun KhamrApp(viewModel: AppViewModel) {
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
                TextButton(onClick = { (context as? Activity)?.finish() }) {
                    Text("خروج")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("إلغاء")
                }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
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
                Column(
                    Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("خمر نت", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF102A43))
                    Text("نظام المبيعات والمخزون", color = Color(0xFF0F766E))
                    Spacer(Modifier.height(28.dp))
                    OutlinedTextField(
                        value = userCode,
                        onValueChange = { userCode = it.filter(Char::isDigit) },
                        label = { Text("رقم المستخدم") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (userCode.isNotBlank() && password.isNotBlank()) login()
                            }
                        )
                    )
                    Spacer(Modifier.height(22.dp))
                    Button(
                        onClick = login,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = userCode.isNotBlank() && password.isNotBlank()
                    ) { Text("دخول") }
                    Spacer(Modifier.height(18.dp))
                    Text("الحساب الافتراضي: رقم المستخدم 1 / كلمة المرور 1", color = Color.Gray, fontSize = 12.sp)
                    state.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "جميع الحقوق محفوظة Smart Link 2026",
                color = Color.White.copy(alpha = .86f),
                fontSize = 12.sp
            )
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

@Composable
private fun DashboardScreen(state: AppUiState, onNavigate: (AppSection) -> Unit) {
    val stats = state.stats
    val isAdmin = state.user?.role == "ADMIN"
    val actions = if (isAdmin) {
        listOf(
            AppSection.POS,
            AppSection.INVOICES,
            AppSection.REPORTS,
            AppSection.PRODUCTS,
            AppSection.USERS,
            AppSection.TRANSFERS,
            AppSection.CUSTOMERS,
            AppSection.BONDS,
            AppSection.SETTLEMENTS
        )
    } else {
        listOf(AppSection.POS, AppSection.INVOICES, AppSection.REPORTS, AppSection.CUSTOMERS, AppSection.BONDS)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("أهلًا بك في خمر نت", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("كل عملياتك محفوظة محليًا وتستمر حتى دون اتصال.", color = Color.White.copy(alpha = .8f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("مبيعات اليوم", "%.2f".format(stats.todaySales), Color(0xFF0F766E), Modifier.weight(1f))
                StatCard("العهدة / العجز", "%.2f".format(stats.carriedDifference), Color(0xFFD99A2B), Modifier.weight(1f))
            }
        }
        item {
            Text("الوصول السريع", fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                actions.chunked(2).forEach { rowItems ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            DashboardActionCard(item, Modifier.weight(1f)) { onNavigate(item) }
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardActionCard(
    section: AppSection,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(section.icon, contentDescription = section.title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(8.dp))
            Text(section.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.White.copy(alpha = .85f), fontSize = 12.sp)
            Spacer(Modifier.height(7.dp))
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InvoiceRow(invoice: InvoiceEntity, state: AppUiState, viewModel: AppViewModel) {
    val context = LocalContext.current
    val customer = state.customers.firstOrNull { it.id == invoice.customerId }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(invoice.paymentType, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("%.2f".format(invoice.total), fontWeight = FontWeight.Bold)
            }
            Text(customer?.name ?: "عميل نقدي", color = Color.DarkGray)
            Text(invoice.id, fontSize = 11.sp, color = Color.Gray)
            Text(formatDate(invoice.createdAt), fontSize = 11.sp, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { PrintAndShare.whatsapp(context, customer, invoice) }, enabled = customer != null) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("واتساب")
                }
                BluetoothPrintButton(invoice, customer?.name ?: "عميل نقدي")
            }
        }
    }
}

@Composable
private fun InvoicesScreen(state: AppUiState, onNewInvoice: () -> Unit) {
    val userNames = state.users.associateBy { it.id }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("فواتير المبيعات", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (state.user?.role == "ADMIN") "عرض جميع فواتير المبيعات المسجلة" else "فواتير المبيعات الخاصة بك",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Button(onClick = onNewInvoice) { Text("فاتورة جديدة") }
        }
        Spacer(Modifier.height(14.dp))
        if (state.invoices.isEmpty()) {
            EmptyState("لا توجد فواتير", "ستظهر الفواتير هنا بعد تسجيل أول عملية بيع")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.invoices, key = { it.id }) { invoice ->
                    val customer = state.customers.firstOrNull { it.id == invoice.customerId }
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(15.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(invoice.id, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${"%.2f".format(invoice.total)}", fontWeight = FontWeight.Bold)
                            }
                            Text("العميل: ${customer?.name ?: "عميل نقدي"}")
                            Text("المستخدم: ${userNames[invoice.userId]?.displayName ?: invoice.userId}", color = Color.Gray)
                            Text("${invoice.paymentType} • ${formatDate(invoice.createdAt)}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsScreen(state: AppUiState) {
    val visibleUsers = if (state.user?.role == "ADMIN") state.users else state.users.filter { it.id == state.user?.id }
    val salesByUser = state.invoices.groupingBy { it.userId }.fold(0.0) { total, invoice -> total + invoice.total }
    val invoiceCountByUser = state.invoices.groupingBy { it.userId }.eachCount()
    val userNames = state.users.associateBy { it.id }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("التقارير", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("ملخص حركة المبيعات والعهدة حسب المستخدم", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F3EF))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("إجمالي المبيعات المعروضة", color = Color.Gray)
                        Text("%.2f".format(state.invoices.sumOf { it.total }), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("${state.invoices.size} فاتورة", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
            item {
                Text("تقرير المستخدمين", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            items(visibleUsers, key = { it.id }) { user ->
                val sales = salesByUser[user.id] ?: 0.0
                val count = invoiceCountByUser[user.id] ?: 0
                val cash = state.cashBalances[user.id] ?: 0.0
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(user.displayName, fontWeight = FontWeight.Bold)
                            Text("كود ${user.userCode}", color = MaterialTheme.colorScheme.primary)
                        }
                        Text("المبيعات: %.2f • عدد الفواتير: %d".format(sales, count))
                        Text("المتبقي في الصندوق: %.2f".format(cash), color = Color.Gray)
                    }
                }
            }
            item {
                Text("ملخص العملاء", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            items(state.customers.sortedByDescending { kotlin.math.abs(it.balance) }.take(30), key = { it.id }) { customer ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(customer.name, fontWeight = FontWeight.Bold)
                            Text(customer.mobile.ifBlank { "بدون جوال" }, color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(accountBalance(customer.balance), color = balanceColor(customer.balance), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PosScreen(
    state: AppUiState,
    viewModel: AppViewModel,
    initialCustomerId: Long? = null,
    onInitialCustomerConsumed: () -> Unit = {}
) {
    var credit by rememberSaveable(initialCustomerId) { mutableStateOf(initialCustomerId != null) }
    var customer by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var productQuery by rememberSaveable { mutableStateOf("") }
    var cart by remember { mutableStateOf<List<DraftSaleLine>>(emptyList()) }
    val quickProducts = state.products.take(6)
    val searchResults = state.products.filter {
        productQuery.isNotBlank() && (
            it.name.contains(productQuery.trim(), ignoreCase = true) ||
                it.barcode.contains(productQuery.trim(), ignoreCase = true)
            )
    }.take(8)
    val invoiceTotal = cart.sumOf { it.lineTotal }

    LaunchedEffect(initialCustomerId, state.customers) {
        initialCustomerId?.let { id ->
            state.customers.firstOrNull { it.id == id }?.let {
                customer = it
                credit = true
                onInitialCustomerConsumed()
            }
        }
    }

    LaunchedEffect(state.saleReceipt?.invoice?.id) {
        if (state.saleReceipt != null) {
            cart = emptyList()
            customer = null
            credit = false
            onInitialCustomerConsumed()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("فاتورة مبيعات", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("الرقم: سيصدر عند الحفظ", color = Color.Gray, fontSize = 11.sp)
                    Text("التاريخ: ${formatDate(System.currentTimeMillis())}", color = Color.Gray, fontSize = 11.sp)
                }
            }
            AssistChip(onClick = {}, label = { Text("${state.products.size} صنف") }, leadingIcon = {
                Icon(Icons.Default.Inventory, contentDescription = null)
            })
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { credit = false; customer = null },
                enabled = credit
            ) {
                Text("نقداً", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { credit = true },
                enabled = !credit
            ) {
                Text("آجل", fontSize = 12.sp)
            }
            Text(
                if (credit) "اسم العميل" else "العميل: مبيعات نقدية",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
        }

        if (credit) {
            Spacer(Modifier.height(6.dp))
            SearchChoiceField(
                label = "ابحث عن العميل",
                selected = customer,
                options = state.customers,
                display = { it.name },
                secondary = { "الرصيد الحالي: ${"%.2f".format(it.balance)}" },
                onSelect = { customer = it }
            )
            customer?.let {
                Row(
                    Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("العميل: ${it.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(accountBalance(it.balance), fontSize = 12.sp, color = balanceColor(it.balance))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("الأصناف السريعة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        quickProducts.chunked(2).forEach { rowProducts ->
            Row(
                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowProducts.forEach { product ->
                    InvoiceProductCard(
                        product = product,
                        stock = state.stock[product.id] ?: 0,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedProduct = product }
                    )
                }
                if (rowProducts.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        OutlinedTextField(
            value = productQuery,
            onValueChange = { productQuery = it },
            label = { Text("بحث سريع عن صنف") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall
        )
        if (searchResults.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            searchResults.forEach { product ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clickable {
                            selectedProduct = product
                            productQuery = ""
                        },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text("${"%.2f".format(product.price)} / ${product.unitName}", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("أصناف الفاتورة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        if (cart.isEmpty()) {
            Text("اضغط على أي صنف لإضافته إلى الفاتورة", color = Color.Gray, fontSize = 12.sp)
        } else {
            Spacer(Modifier.height(8.dp))
            cart.forEachIndexed { index, line ->
                Card(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(line.product.name, fontWeight = FontWeight.Bold)
                            Text("${line.quantity} ${line.unitName} × ${"%.2f".format(line.unitPrice)}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text("%.2f".format(line.lineTotal), fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                cart = cart.toMutableList().also { it.removeAt(index) }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف الصنف")
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الإجمالي", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("%.2f".format(invoiceTotal), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = {
                    viewModel.sell(
                        lines = cart.map { SaleLineInput(it.product.id, it.unitName, it.quantity) },
                        customerId = customer?.id,
                        credit = credit
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = cart.isNotEmpty() && (!credit || customer != null)
            ) {
                Text("حفظ واعتماد الفاتورة")
            }
        }
        Spacer(Modifier.height(18.dp))
    }
    selectedProduct?.let { product ->
        AddLineDialog(
            product = product,
            onDismiss = { selectedProduct = null },
            onAdd = { unit, quantity ->
                cart = cart + DraftSaleLine(product, unit, quantity)
                selectedProduct = null
            }
        )
    }
}

private data class DraftSaleLine(
    val product: ProductEntity,
    val unitName: String,
    val quantity: Int
) {
    val unitPrice: Double
        get() = if (unitName == product.caseUnitName && product.casePrice > 0) product.casePrice else product.price
    val lineTotal: Double
        get() = unitPrice * quantity
}

@Composable
private fun InvoiceProductCard(
    product: ProductEntity,
    stock: Int,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(98.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (stock > 0) Color.White else Color(0xFFF1E7E7)
        )
    ) {
        Column(
            Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${"%.2f".format(product.price)} / ${product.unitName}", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("المتاح", color = Color.Gray, fontSize = 10.sp)
                Text("$stock", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (stock > 0) Color(0xFF0F766E) else Color.Red)
            }
        }
    }
}

@Composable
private fun AddLineDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onAdd: (String, Int) -> Unit
) {
    var unit by remember { mutableStateOf(product.unitName) }
    var quantity by remember { mutableStateOf("1") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة الصنف") },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("اختر الوحدة ثم اكتب العدد", color = Color.Gray, fontSize = 12.sp)
                Text("الوحدة", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { unit = product.unitName },
                        label = { Text(if (unit == product.unitName) "✓ ${product.unitName}" else product.unitName) }
                    )
                    AssistChip(
                        onClick = { unit = product.caseUnitName },
                        label = { Text(if (unit == product.caseUnitName) "✓ ${product.caseUnitName}" else product.caseUnitName) }
                    )
                }
                Text("سعر الوحدة: ${"%.2f".format(if (unit == product.caseUnitName) product.casePrice else product.price)}")
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    label = { Text("الكمية") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(unit, quantity.toIntOrNull() ?: 0) },
                enabled = (quantity.toIntOrNull() ?: 0) > 0
            ) { Text("إضافة إلى الفاتورة") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun ProductsScreen(state: AppUiState, viewModel: AppViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("بيانات الأصناف", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("الأسعار تُقفل على الكاشير حسب إعدادات المدير", color = Color.Gray, fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = viewModel::testFirebaseConnection,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text("اختبار Firebase", fontSize = 11.sp)
            }
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "إضافة") }
        }
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.products) { product ->
                Card(
                    Modifier.fillMaxWidth().height(118.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.padding(10.dp).fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("باركود: ${product.barcode.ifBlank { "غير محدد" }}", color = Color.Gray, fontSize = 12.sp)
                            Text("${"%.2f".format(product.price)} / ${product.unitName}", color = MaterialTheme.colorScheme.primary)
                            Text("${"%.2f".format(product.casePrice)} / ${product.caseUnitName}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(onClick = { editingProduct = product }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل الصنف", modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { viewModel.deleteProduct(product.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف الصنف",
                                    tint = Color(0xFFB3261E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAdd) ProductEditorDialog(null, viewModel) { showAdd = false }
    editingProduct?.let { product ->
        ProductEditorDialog(product, viewModel) { editingProduct = null }
    }
}

@Composable
private fun ProductEditorDialog(
    product: ProductEntity?,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var name by remember(product?.id) { mutableStateOf(product?.name ?: "") }
    var barcode by remember(product?.id) { mutableStateOf(product?.barcode ?: "") }
    var unit by remember(product?.id) { mutableStateOf(product?.unitName ?: "حبة") }
    var price by remember(product?.id) { mutableStateOf(product?.price?.toString() ?: "") }
    var caseName by remember(product?.id) { mutableStateOf(product?.caseUnitName ?: "كرت") }
    var caseQty by remember(product?.id) { mutableStateOf(product?.caseQuantity?.toString() ?: "60") }
    var casePrice by remember(product?.id) { mutableStateOf(product?.casePrice?.toString() ?: "") }
    var stock by remember(product?.id) { mutableStateOf("0") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "صنف جديد" else "تعديل الصنف") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("اسم الصنف", name) { name = it }
                FormField("الباركود", barcode, numeric = true) { barcode = it }
                FormField("الوحدة المفردة", unit) { unit = it }
                FormField("سعر المفردة", price, numeric = true) { price = it }
                FormField("الوحدة الكبيرة", caseName) { caseName = it }
                FormField("سعة الوحدة", caseQty, numeric = true) { caseQty = it }
                FormField("سعر الوحدة الكبيرة", casePrice, numeric = true) { casePrice = it }
                if (product == null) {
                    FormField("رصيد المستودع الرئيسي", stock, numeric = true) { stock = it }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedPrice = price.toDoubleOrNull() ?: 0.0
                val parsedCaseQty = caseQty.toIntOrNull() ?: 1
                val parsedCasePrice = casePrice.toDoubleOrNull() ?: 0.0
                if (product == null) {
                    viewModel.createProduct(
                        name, barcode, unit, parsedPrice, caseName,
                        parsedCaseQty, parsedCasePrice, stock.toIntOrNull() ?: 0
                    )
                } else {
                    viewModel.updateProduct(
                        product.copy(
                            name = name,
                            barcode = barcode,
                            unitName = unit,
                            price = parsedPrice,
                            caseUnitName = caseName,
                            caseQuantity = parsedCaseQty,
                            casePrice = parsedCasePrice
                        )
                    )
                }
                onDismiss()
            }, enabled = name.isNotBlank() && (price.toDoubleOrNull() ?: 0.0) > 0) {
                Text(if (product == null) "حفظ" else "تحديث")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun UsersScreen(state: AppUiState, viewModel: AppViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserEntity?>(null) }
    val nextUserCode = (state.users.mapNotNull { it.userCode.toIntOrNull() }.maxOrNull() ?: 1) + 1
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("المستخدمون", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("لكل كاشير مخزون فرعي وصندوق مستقل تلقائيًا", color = Color.Gray, fontSize = 12.sp)
            }
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "إضافة") }
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.users) { user ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("كود المستخدم: ${user.userCode}", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                if (user.role == "ADMIN") "مدير — جميع الصلاحيات"
                                else "الصلاحيات مخصصة حسب إعداد المدير",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(onClick = {}, label = { Text(if (user.role == "ADMIN") "مدير" else "كاشير") })
                            IconButton(onClick = { editingUser = user }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل المستخدم")
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAdd) UserEditorDialog(null, viewModel, nextUserCode.toString()) { showAdd = false }
    editingUser?.let { user ->
        UserEditorDialog(user, viewModel, user.userCode) { editingUser = null }
    }
}

@Composable
private fun UserEditorDialog(
    user: UserEntity?,
    viewModel: AppViewModel,
    defaultUserCode: String,
    onDismiss: () -> Unit
) {
    var name by remember(user?.id) { mutableStateOf(user?.displayName ?: "") }
    var userCode by remember(user?.id, defaultUserCode) { mutableStateOf(user?.userCode ?: defaultUserCode) }
    var password by remember(user?.id) { mutableStateOf("") }
    var enabledSections by remember(user?.id) {
        mutableStateOf(
            AppSection.values().filter { user?.let { account -> account.canAccess(it.name) } ?: it in setOf(
                AppSection.POS,
                AppSection.INVOICES,
                AppSection.REPORTS,
                AppSection.CUSTOMERS,
                AppSection.BONDS
            ) }.map { it.name }.toSet()
        )
    }
    var canWhatsapp by remember(user?.id) { mutableStateOf(user?.canWhatsapp ?: true) }
    val permissions = UserPermissions(
        canHome = "HOME" in enabledSections,
        canPos = "POS" in enabledSections,
        canInvoices = "INVOICES" in enabledSections,
        canReports = "REPORTS" in enabledSections,
        canProducts = "PRODUCTS" in enabledSections,
        canUsers = "USERS" in enabledSections,
        canTransfers = "TRANSFERS" in enabledSections,
        canCustomers = "CUSTOMERS" in enabledSections,
        canBonds = "BONDS" in enabledSections,
        canSettlements = "SETTLEMENTS" in enabledSections,
        canWhatsapp = canWhatsapp
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "تسجيل كاشير" else "تعديل بيانات المستخدم") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormField("اسم المستخدم الظاهر", name) { name = it }
                FormField("كود المستخدم الرقمي", userCode, numeric = true) { userCode = it }
                OutlinedTextField(
                    password,
                    { password = it.filter(Char::isDigit) },
                    label = { Text(if (user == null) "كلمة المرور الرقمية" else "كلمة مرور جديدة (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                Divider()
                Text("صلاحيات الشاشات", fontWeight = FontWeight.Bold)
                Text("اضغط على اسم الشاشة أو علامة الصح لتفعيلها للمستخدم.", color = Color.Gray, fontSize = 12.sp)
                AppSection.values().forEach { screen ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            enabledSections = enabledSections.toMutableSet().also {
                                if (!it.add(screen.name)) it.remove(screen.name)
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = screen.name in enabledSections,
                            onCheckedChange = { checked ->
                                enabledSections = enabledSections.toMutableSet().also {
                                    if (checked) it.add(screen.name) else it.remove(screen.name)
                                }
                            }
                        )
                        Text(screen.title)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().clickable { canWhatsapp = !canWhatsapp },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = canWhatsapp, onCheckedChange = { canWhatsapp = it })
                    Text("إرسال WhatsApp في كل الشاشات")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (user == null) {
                        viewModel.createUser(name, userCode, password, permissions)
                    } else {
                        viewModel.updateUser(user, name, userCode, password, permissions)
                    }
                    onDismiss()
                },
                enabled = name.isNotBlank() && userCode.isNotBlank() &&
                    (user != null || password.isNotBlank())
            ) { Text(if (user == null) "إنشاء" else "حفظ التعديلات") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun TransfersScreen(state: AppUiState, viewModel: AppViewModel) {
    var cashier by remember { mutableStateOf<UserEntity?>(null) }
    var product by remember { mutableStateOf<ProductEntity?>(null) }
    var quantity by remember { mutableStateOf("1") }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("تحويل من المستودع الرئيسي", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("وزّع الكميات على مخزن الكاشير المستقل قبل البيع.", color = Color.Gray)
        SearchChoiceField(
            label = "ابحث عن الكاشير",
            selected = cashier,
            options = state.users.filter { it.role == "CASHIER" },
            display = { it.displayName },
            onSelect = { cashier = it }
        )
        SearchChoiceField(
            label = "ابحث عن الصنف",
            selected = product,
            options = state.products,
            display = { it.name },
            secondary = { "${it.price} / ${it.unitName}" },
            onSelect = { product = it }
        )
        FormField("الكمية", quantity, numeric = true) { quantity = it }
        Button(
            onClick = { if (cashier != null && product != null) viewModel.transferStock(cashier!!.id, product!!.id, quantity.toIntOrNull() ?: 0) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = cashier != null && product != null && (quantity.toIntOrNull() ?: 0) > 0
        ) { Icon(Icons.Default.SwapHoriz, null); Spacer(Modifier.width(8.dp)); Text("تنفيذ التحويل") }
        Divider()
        Text("ملاحظة: لا يستطيع الكاشير بيع كمية تتجاوز مخزونه الفرعي.", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun CustomersScreen(
    state: AppUiState,
    viewModel: AppViewModel,
    onIssueInvoice: (CustomerEntity) -> Unit,
    onIssueBond: (CustomerEntity) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var statementCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var statementRows by remember { mutableStateOf<List<CustomerStatementRow>?>(null) }
    val nextCustomerCode = (state.customers.mapNotNull { it.customerCode.toIntOrNull() }.maxOrNull() ?: 0) + 1
    val matches = state.customers.filter {
        query.isNotBlank() && (
            it.name.contains(query.trim(), ignoreCase = true) ||
                it.mobile.contains(query.trim(), ignoreCase = true)
            )
    }
    val context = LocalContext.current
    val canWhatsapp = state.user?.role == "ADMIN" || state.user?.canWhatsapp == true
    val canIssueInvoice = state.user?.canAccess(AppSection.POS.name) == true
    val canIssueBond = state.user?.canAccess(AppSection.BONDS.name) == true
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("العملاء", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("ابحث بالاسم ثم اختر العملية المطلوبة", color = Color.Gray, fontSize = 12.sp)
            }
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "إضافة") }
        }
        Spacer(Modifier.height(14.dp))
        FormField("ابحث باسم العميل أو رقم الجوال", query) { query = it }
        Spacer(Modifier.height(10.dp))
        if (state.customers.isEmpty()) {
            EmptyState("لا يوجد عملاء", "أضف أول عميل لاستخدام البيع الآجل")
        } else if (query.isBlank()) {
            Text("اكتب جزءًا من اسم العميل أو رقم الجوال لعرضه", color = Color.Gray)
        } else if (matches.isEmpty()) {
            EmptyState("لا توجد نتائج", "جرّب كتابة جزء آخر من اسم العميل")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(matches) { customer ->
                    val lastMovement = state.customerLastMovement[customer.id]
                    Card(
                        Modifier.fillMaxWidth().clickable { selectedCustomer = customer },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    Text(
                                        customer.mobile.ifBlank { "لا يوجد رقم هاتف" },
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    accountBalance(customer.balance),
                                    color = balanceColor(customer.balance),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (canIssueInvoice) {
                                    OutlinedButton(
                                        onClick = { onIssueInvoice(customer) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("إصدار فاتورة", fontSize = 11.sp) }
                                }
                                if (canIssueBond) {
                                    OutlinedButton(
                                        onClick = { onIssueBond(customer) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("سند قبض", fontSize = 11.sp) }
                                }
                                OutlinedButton(
                                    onClick = {
                                        statementCustomer = customer
                                        statementRows = null
                                        viewModel.loadCustomerStatement(customer.id) { rows ->
                                            statementRows = rows
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.weight(1f)
                                ) { Text("كشف حساب", fontSize = 11.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
    selectedCustomer?.let { customer ->
        CustomerSummaryDialog(
            customer = customer,
            lastMovement = state.customerLastMovement[customer.id],
            onStatement = {
                selectedCustomer = null
                statementCustomer = customer
                statementRows = null
                viewModel.loadCustomerStatement(customer.id) { rows ->
                    statementRows = rows
                }
            },
            onDismiss = { selectedCustomer = null }
        )
    }
    if (statementCustomer != null && statementRows != null) {
        CustomerStatementDialog(
            customer = statementCustomer!!,
            rows = statementRows!!,
            canWhatsapp = canWhatsapp,
            onSharePdf = { PrintAndShare.shareStatement(context, statementCustomer!!, statementRows!!) },
            onShareWhatsapp = {
                PrintAndShare.shareStatementToWhatsapp(context, statementCustomer!!, statementRows!!)
            },
            onDismiss = {
                statementCustomer = null
                statementRows = null
            }
        )
    }
    if (showAdd) AddCustomerDialog(viewModel, nextCustomerCode.toString()) { showAdd = false }
}

@Composable
private fun AddCustomerDialog(viewModel: AppViewModel, defaultCode: String, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var customerCode by remember { mutableStateOf(defaultCode) }
    var mobile by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("عميل جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("اسم العميل", name) { name = it }
                FormField("كود العميل الفريد", customerCode, numeric = true) { customerCode = it }
                FormField("رقم الجوال", mobile, numeric = true) { mobile = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.addCustomer(name, customerCode, mobile); onDismiss() },
                enabled = name.isNotBlank() && customerCode.isNotBlank()
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun BondsScreen(
    state: AppUiState,
    viewModel: AppViewModel,
    initialCustomerId: Long? = null,
    onInitialCustomerConsumed: () -> Unit = {}
) {
    var customer by remember { mutableStateOf<CustomerEntity?>(null) }
    var type by remember { mutableStateOf("قبض") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    LaunchedEffect(initialCustomerId, state.customers) {
        initialCustomerId?.let { id ->
            state.customers.firstOrNull { it.id == id }?.let {
                customer = it
                onInitialCustomerConsumed()
            }
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("سندات القبض", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("رقم السند: يصدر عند الاعتماد", color = Color.Gray, fontSize = 12.sp)
            Text("التاريخ: ${formatDate(System.currentTimeMillis())}", color = Color.Gray, fontSize = 12.sp)
        }
        SearchChoiceField(
            label = "ابحث عن العميل",
            selected = customer,
            options = state.customers,
            display = { it.name },
            secondary = { "الرصيد الحالي: ${"%.2f".format(it.balance)}" },
            onSelect = { customer = it }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { type = "قبض" }, label = { Text(if (type == "قبض") "✓ قبض" else "قبض") })
            AssistChip(onClick = { type = "صرف" }, label = { Text(if (type == "صرف") "✓ صرف" else "صرف") })
        }
        FormField("المبلغ", amount, numeric = true) { amount = it }
        FormField("البيان", note) { note = it }
        Button(
            onClick = { if (customer != null) viewModel.bond(customer!!.id, type, amount.toDoubleOrNull() ?: 0.0, note) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = customer != null && (amount.toDoubleOrNull() ?: 0.0) > 0
        ) { Icon(Icons.Default.ReceiptLong, null); Spacer(Modifier.width(8.dp)); Text("حفظ واعتماد السند") }
    }
}

@Composable
private fun SettlementsScreen(state: AppUiState, viewModel: AppViewModel) {
    val cashiers = state.users.filter { it.role == "CASHIER" }
    var cashier by remember { mutableStateOf<UserEntity?>(null) }
    var actual by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("التصفية المستمرة", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("أدخل المبلغ النقدي الذي تم عده فعليًا. يسجل النظام العجز أو الزيادة دون إغلاق وردية.", color = Color.Gray)
        SearchChoiceField(
            label = "ابحث عن الكاشير",
            selected = cashier,
            options = cashiers,
            display = { it.displayName },
            onSelect = { cashier = it }
        )
        FormField("النقد الفعلي المعدود", actual, numeric = true) { actual = it }
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3D6)), shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF9A6700))
                Spacer(Modifier.width(10.dp))
                Text("بعد الحفظ سيستمر الكاشير بالعمل، ويظهر الفرق المرحّل في لوحته.")
            }
        }
        Button(
            onClick = { if (cashier != null) viewModel.settle(cashier!!.id, actual.toDoubleOrNull() ?: 0.0) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = cashier != null && (actual.toDoubleOrNull() ?: -1.0) >= 0
        ) { Text("اعتماد التصفية") }
    }
}

@Composable
private fun <T> SearchChoiceField(
    label: String,
    selected: T?,
    options: List<T>,
    display: (T) -> String = { it.toString() },
    secondary: (T) -> String = { "" },
    onSelect: (T?) -> Unit
) {
    val selectedLabel = selected?.let(display).orEmpty()
    var query by remember(selectedLabel) { mutableStateOf(selectedLabel) }
    val matches = options.filter { display(it).contains(query.trim(), ignoreCase = true) }.take(8)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { value ->
                query = value
                if (selected != null && value != selectedLabel) onSelect(null)
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (selected != null) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تم اختيار: $selectedLabel", color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = { query = ""; onSelect(null) }) { Text("تغيير") }
            }
        } else if (query.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                matches.forEach { option ->
                    Card(
                        Modifier.fillMaxWidth().clickable {
                            query = display(option)
                            onSelect(option)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(display(option), fontWeight = FontWeight.Bold)
                            secondary(option).takeIf { it.isNotBlank() }?.let {
                                Text(it, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                if (matches.isEmpty()) {
                    Text("لا توجد نتائج مطابقة", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CustomerStatementDialog(
    customer: CustomerEntity,
    rows: List<CustomerStatementRow>,
    canWhatsapp: Boolean,
    onSharePdf: () -> Unit,
    onShareWhatsapp: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("كشف حساب ${customer.name}") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(accountBalance(customer.balance), color = balanceColor(customer.balance), fontWeight = FontWeight.Bold)
                if (rows.isEmpty()) {
                    Text("لا توجد حركات مالية لهذا العميل", color = Color.Gray)
                } else {
                    rows.forEach { row ->
                        Card(shape = RoundedCornerShape(10.dp)) {
                            Column(Modifier.fillMaxWidth().padding(9.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(row.type, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(formatDate(row.createdAt), fontSize = 10.sp, color = Color.Gray)
                                }
                                Text("المرجع: ${row.reference}", fontSize = 11.sp, color = Color.Gray)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("المبلغ: ${"%.2f".format(row.amount)}", fontSize = 11.sp)
                                    Text(accountBalance(row.balanceAfter), fontSize = 11.sp, color = balanceColor(row.balanceAfter))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onSharePdf) {
                    Text("PDF", fontSize = 11.sp)
                }
                if (canWhatsapp) {
                    TextButton(onClick = onShareWhatsapp) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(3.dp))
                        Text("WhatsApp", fontSize = 11.sp)
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
private fun CustomerSummaryDialog(
    customer: CustomerEntity,
    lastMovement: Long?,
    onStatement: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(customer.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(accountBalance(customer.balance), fontWeight = FontWeight.Bold, color = balanceColor(customer.balance))
                Text("رقم الجوال: ${customer.mobile.ifBlank { "غير متوفر" }}")
                Text("آخر حركة: ${lastMovement?.let(::formatDate) ?: "لا توجد حركة"}", color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = onStatement) {
                Icon(Icons.Default.Assessment, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("توليد كشف الحساب")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
private fun SaleReceiptDialog(
    receipt: SaleReceipt,
    canWhatsapp: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("فاتورة مبيعات") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("رقم: ${receipt.invoice.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(formatDate(receipt.invoice.createdAt), fontSize = 10.sp, color = Color.Gray)
                }
                Text("العميل: ${receipt.customer?.name ?: "مبيعات نقدية"}", fontSize = 12.sp)
                Divider()
                receipt.lines.forEach { line ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${line.productName} • ${line.quantity} ${line.unitName}", fontSize = 11.sp)
                        Text("%.2f".format(line.lineTotal), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Divider()
                Text("مبلغ الفاتورة: ${"%.2f".format(receipt.invoice.total)}", fontWeight = FontWeight.Bold)
                Text("رصيدكم السابق: ${"%.2f".format(receipt.invoice.previousBalance)}", fontSize = 11.sp)
                Text(
                    accountBalance(receipt.invoice.newBalance),
                    color = balanceColor(receipt.invoice.newBalance),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (canWhatsapp && receipt.customer != null) {
                    TextButton(onClick = { PrintAndShare.whatsapp(context, receipt.customer, receipt.invoice) }) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("واتساب")
                    }
                }
                BluetoothPrintButton(
                    invoice = receipt.invoice,
                    customerName = receipt.customer?.name ?: "مبيعات نقدية",
                    lines = receipt.lines
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
private fun BondReceiptDialog(
    receipt: BondReceipt,
    canWhatsapp: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("سند ${receipt.bond.type} معتمد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("رقم: ${receipt.bond.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(formatDate(receipt.bond.createdAt), fontSize = 10.sp, color = Color.Gray)
                }
                Text("العميل: ${receipt.customer.name}")
                Text("رصيدكم السابق: ${"%.2f".format(receipt.bond.previousBalance)}", fontSize = 12.sp)
                Text("مبلغ السند: ${"%.2f".format(receipt.bond.amount)}", fontWeight = FontWeight.Bold)
                Text(
                    accountBalance(receipt.bond.newBalance),
                    color = balanceColor(receipt.bond.newBalance),
                    fontWeight = FontWeight.Bold
                )
                Text("البيان: ${receipt.bond.note.ifBlank { "بدون بيان" }}", fontSize = 11.sp)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (canWhatsapp) {
                    TextButton(onClick = { PrintAndShare.whatsappBond(context, receipt.customer, receipt.bond) }) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("واتساب")
                    }
                }
                BluetoothBondPrintButton(receipt.customer, receipt.bond)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
private fun BluetoothPrintButton(
    invoice: InvoiceEntity,
    customerName: String,
    lines: List<InvoiceLineEntity> = emptyList()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPrinters by remember { mutableStateOf(false) }
    var printers by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            printers = PrintAndShare.pairedPrinters()
            showPrinters = true
        } else {
            Toast.makeText(context, "يلزم السماح بالوصول إلى Bluetooth للطباعة", Toast.LENGTH_LONG).show()
        }
    }

    TextButton(onClick = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            printers = PrintAndShare.pairedPrinters()
            showPrinters = true
        }
    }) {
        Icon(Icons.Default.ReceiptLong, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text("طباعة")
    }

    if (showPrinters) {
        AlertDialog(
            onDismissRequest = { showPrinters = false },
            title = { Text("اختر الطابعة الحرارية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (printers.isEmpty()) {
                        Text("لا توجد طابعة مقترنة. اقترن بالطابعة من إعدادات Bluetooth أولًا.")
                    } else {
                        printers.forEach { device ->
                            OutlinedButton(
                                onClick = {
                                    showPrinters = false
                                    scope.launch(Dispatchers.IO) {
                                        val result = PrintAndShare.printBluetooth(device, invoice, customerName, lines)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                result.exceptionOrNull()?.message ?: "تم إرسال الفاتورة إلى الطابعة",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(device.name ?: device.address)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPrinters = false }) { Text("إغلاق") } }
        )
    }
}

@Composable
private fun BluetoothBondPrintButton(customer: CustomerEntity, bond: com.khamrnet.app.data.FinancialBondEntity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPrinters by remember { mutableStateOf(false) }
    var printers by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            printers = PrintAndShare.pairedPrinters()
            showPrinters = true
        } else {
            Toast.makeText(context, "يلزم السماح بالوصول إلى Bluetooth للطباعة", Toast.LENGTH_LONG).show()
        }
    }

    TextButton(onClick = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            printers = PrintAndShare.pairedPrinters()
            showPrinters = true
        }
    }) {
        Icon(Icons.Default.ReceiptLong, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text("طباعة")
    }

    if (showPrinters) {
        AlertDialog(
            onDismissRequest = { showPrinters = false },
            title = { Text("اختر الطابعة الحرارية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (printers.isEmpty()) {
                        Text("لا توجد طابعة مقترنة. اقترن بالطابعة من إعدادات Bluetooth أولًا.")
                    } else {
                        printers.forEach { device ->
                            OutlinedButton(
                                onClick = {
                                    showPrinters = false
                                    scope.launch(Dispatchers.IO) {
                                        val result = PrintAndShare.printBondBluetooth(device, customer, bond)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                result.exceptionOrNull()?.message ?: "تم إرسال السند إلى الطابعة",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(device.name ?: device.address)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPrinters = false }) { Text("إغلاق") } }
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    numeric: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { value -> onChange(if (numeric) value.filter(Char::isDigit) else value) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text
        )
    )
}

@Composable
private fun EmptyState(title: String, description: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(description, color = Color.Gray)
        }
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale("ar")).format(Date(timestamp))

private fun accountBalance(value: Double): String = when {
    value > 0 -> "عليه: %.2f".format(value)
    value < 0 -> "له: %.2f".format(kotlin.math.abs(value))
    else -> "متزن: 0.00"
}

private fun balanceColor(value: Double): Color = when {
    value > 0 -> Color(0xFFB3261E)
    value < 0 -> Color(0xFF0F766E)
    else -> Color.Gray
}
