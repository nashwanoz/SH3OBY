package com.khamrnet.app

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.InvoiceEntity
import com.khamrnet.app.data.ProductEntity
import com.khamrnet.app.data.UserEntity
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
    POS("نقطة البيع", Icons.Default.PointOfSale),
    PRODUCTS("المنتجات", Icons.Default.Inventory),
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
        SaleReceiptDialog(receipt, viewModel::clearSaleReceipt)
    }
    state.bondReceipt?.let { receipt ->
        BondReceiptDialog(receipt, viewModel::clearBondReceipt)
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF102A43), Color(0xFF0F766E)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF102A43), Color(0xFF0F766E)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(.9f),
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
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("اسم المستخدم") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = { viewModel.login(username, password) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) { Text("دخول") }
                Spacer(Modifier.height(18.dp))
                Text("الحساب الافتراضي: 1 / 1", color = Color.Gray, fontSize = 12.sp)
                (state.error)?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(state: AppUiState, viewModel: AppViewModel, snackbar: SnackbarHostState) {
    val isAdmin = state.user?.role == "ADMIN"
    var selected by rememberSaveable { mutableStateOf(if (isAdmin) AppSection.HOME.name else AppSection.POS.name) }
    val section = runCatching { AppSection.valueOf(selected) }.getOrDefault(AppSection.HOME)
    val available = if (isAdmin) {
        listOf(AppSection.HOME, AppSection.PRODUCTS, AppSection.USERS, AppSection.TRANSFERS, AppSection.CUSTOMERS, AppSection.BONDS, AppSection.SETTLEMENTS)
    } else {
        listOf(AppSection.POS, AppSection.CUSTOMERS, AppSection.BONDS)
    }
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
                    IconButton(onClick = viewModel::logout) {
                        Icon(Icons.Default.Logout, contentDescription = "خروج")
                    }
                }
            )
        },
        bottomBar = {
            if (section != AppSection.HOME) {
                NavigationBar {
                    available.forEach { item ->
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
                AppSection.POS -> PosScreen(state, viewModel)
                AppSection.PRODUCTS -> ProductsScreen(state, viewModel)
                AppSection.USERS -> UsersScreen(state, viewModel)
                AppSection.TRANSFERS -> TransfersScreen(state, viewModel)
                AppSection.CUSTOMERS -> CustomersScreen(state, viewModel)
                AppSection.BONDS -> BondsScreen(state, viewModel)
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
            AppSection.PRODUCTS,
            AppSection.USERS,
            AppSection.TRANSFERS,
            AppSection.CUSTOMERS,
            AppSection.BONDS,
            AppSection.SETTLEMENTS
        )
    } else {
        listOf(AppSection.POS, AppSection.CUSTOMERS, AppSection.BONDS)
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
private fun PosScreen(state: AppUiState, viewModel: AppViewModel) {
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("بيع سريع", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("اختر المنتج ثم الوحدة والكمية", color = Color.Gray, fontSize = 12.sp)
            }
            AssistChip(onClick = {}, label = { Text("${state.products.size} منتج") }, leadingIcon = {
                Icon(Icons.Default.Inventory, contentDescription = null)
            })
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.products) { product ->
                val stock = state.stock[product.id] ?: 0
                Card(
                    modifier = Modifier.fillMaxWidth().height(150.dp).clickable { selectedProduct = product },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (stock > 0) Color.White else Color(0xFFF1E7E7)
                    )
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(15.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(product.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("${"%.2f".format(product.price)} / ${product.unitName}", color = MaterialTheme.colorScheme.primary)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المتاح", color = Color.Gray, fontSize = 12.sp)
                            Text("$stock", fontWeight = FontWeight.Bold, color = if (stock > 0) Color(0xFF0F766E) else Color.Red)
                        }
                    }
                }
            }
        }
    }
    selectedProduct?.let { product ->
        SaleDialog(
            product = product,
            customers = state.customers,
            onDismiss = { selectedProduct = null },
            onSell = { unit, quantity, customerId, credit ->
                viewModel.sell(product, unit, quantity, customerId, credit)
                selectedProduct = null
            }
        )
    }
}

@Composable
private fun SaleDialog(
    product: ProductEntity,
    customers: List<CustomerEntity>,
    onDismiss: () -> Unit,
    onSell: (String, Int, Long?, Boolean) -> Unit
) {
    var unit by remember { mutableStateOf(product.unitName) }
    var quantity by remember { mutableStateOf("1") }
    var credit by remember { mutableStateOf(false) }
    var customer by remember { mutableStateOf<CustomerEntity?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة ${product.name} للفاتورة") },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("الوحدة", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { unit = product.unitName }, label = { Text(product.unitName) })
                    AssistChip(onClick = { unit = product.caseUnitName }, label = { Text(product.caseUnitName) })
                }
                Text("سعر الوحدة: ${"%.2f".format(if (unit == product.caseUnitName) product.casePrice else product.price)}")
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    label = { Text("الكمية") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                AssistChip(
                    onClick = { credit = !credit },
                    label = { Text(if (credit) "بيع آجل — اختر العميل" else "بيع نقدي") },
                    leadingIcon = { Icon(if (credit) Icons.Default.People else Icons.Default.AccountBalance, null) }
                )
                if (credit) {
                    if (customers.isEmpty()) {
                        Text("أضف عميلًا من شاشة العملاء أولًا", color = Color.Red)
                    } else {
                        SearchChoiceField(
                            label = "ابحث عن العميل",
                            selected = customer,
                            options = customers,
                            display = { it.name },
                            secondary = { "الرصيد الحالي: ${"%.2f".format(it.balance)}" },
                            onSelect = { customer = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSell(unit, quantity.toIntOrNull() ?: 0, customer?.id, credit) },
                enabled = (quantity.toIntOrNull() ?: 0) > 0 && (!credit || customer != null)
            ) { Text("تأكيد البيع") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun ProductsScreen(state: AppUiState, viewModel: AppViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("كتالوج المنتجات", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("الأسعار تُقفل على الكاشير حسب إعدادات المدير", color = Color.Gray, fontSize = 12.sp)
            }
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "إضافة") }
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.products) { product ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        Modifier.padding(15.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("باركود: ${product.barcode.ifBlank { "غير محدد" }}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${"%.2f".format(product.price)} / ${product.unitName}", color = MaterialTheme.colorScheme.primary)
                            Text("${"%.2f".format(product.casePrice)} / ${product.caseUnitName}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            TextButton(onClick = { editingProduct = product }) { Text("تعديل") }
                            TextButton(onClick = { viewModel.deleteProduct(product.id) }) { Text("حذف", color = Color(0xFFB3261E)) }
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
    var caseName by remember(product?.id) { mutableStateOf(product?.caseUnitName ?: "كرتون") }
    var caseQty by remember(product?.id) { mutableStateOf(product?.caseQuantity?.toString() ?: "12") }
    var casePrice by remember(product?.id) { mutableStateOf(product?.casePrice?.toString() ?: "") }
    var stock by remember(product?.id) { mutableStateOf("0") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "منتج جديد" else "تعديل الصنف") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("اسم المنتج", name) { name = it }
                FormField("الباركود", barcode) { barcode = it }
                FormField("الوحدة المفردة", unit) { unit = it }
                FormField("سعر المفردة", price) { price = it }
                FormField("وحدة الجملة", caseName) { caseName = it }
                FormField("عدد المفردات", caseQty) { caseQty = it }
                FormField("سعر الجملة", casePrice) { casePrice = it }
                if (product == null) {
                    FormField("رصيد المستودع الرئيسي", stock) { stock = it }
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
                            Text("اسم الدخول: ${user.username}", color = Color.Gray)
                        }
                        AssistChip(onClick = {}, label = { Text(if (user.role == "ADMIN") "مدير" else "كاشير") })
                    }
                }
            }
        }
    }
    if (showAdd) AddUserDialog(viewModel) { showAdd = false }
}

@Composable
private fun AddUserDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل كاشير") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("الاسم الظاهر", name) { name = it }
                FormField("اسم المستخدم", username) { username = it }
                OutlinedTextField(password, { password = it }, label = { Text("كلمة المرور") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.createUser(username, password, name); onDismiss() }, enabled = name.isNotBlank() && username.isNotBlank() && password.isNotBlank()) { Text("إنشاء") }
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
        FormField("الكمية", quantity) { quantity = it.filter(Char::isDigit) }
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
private fun CustomersScreen(state: AppUiState, viewModel: AppViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    val matches = state.customers.filter {
        query.isNotBlank() && (
            it.name.contains(query.trim(), ignoreCase = true) ||
                it.mobile.contains(query.trim(), ignoreCase = true)
            )
    }
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("سجل العملاء", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("الأرصدة والبيانات جاهزة للبيع الآجل والسندات", color = Color.Gray, fontSize = 12.sp)
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
                        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text(customer.mobile.ifBlank { "لا يوجد رقم هاتف" }, color = Color.Gray)
                                Text(
                                    "آخر حركة: ${lastMovement?.let(::formatDate) ?: "لا توجد حركة"}",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("الرصيد", color = Color.Gray, fontSize = 11.sp)
                                Text(
                                    "%.2f".format(customer.balance),
                                    color = if (customer.balance > 0) Color(0xFFB3261E) else Color(0xFF0F766E),
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = {
                                    viewModel.loadCustomerStatement(customer.id) { rows ->
                                        PrintAndShare.shareStatement(context, customer, rows)
                                    }
                                }) {
                                    Text("كشف حساب")
                                }
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
                viewModel.loadCustomerStatement(customer.id) { rows ->
                    PrintAndShare.shareStatement(context, customer, rows)
                }
                selectedCustomer = null
            },
            onDismiss = { selectedCustomer = null }
        )
    }
    if (showAdd) AddCustomerDialog(viewModel) { showAdd = false }
}

@Composable
private fun AddCustomerDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("عميل جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("اسم العميل", name) { name = it }
                FormField("رقم الجوال", mobile) { mobile = it }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.addCustomer(name, mobile); onDismiss() }, enabled = name.isNotBlank()) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun BondsScreen(state: AppUiState, viewModel: AppViewModel) {
    var customer by remember { mutableStateOf<CustomerEntity?>(null) }
    var type by remember { mutableStateOf("قبض") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("السندات المالية", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("سند القبض يقلل الرصيد، وسند الصرف يضيف حركة مدينة على حساب العميل.", color = Color.Gray)
        SearchChoiceField(
            label = "ابحث عن العميل",
            selected = customer,
            options = state.customers,
            display = { it.name },
            secondary = { "الرصيد الحالي: ${"%.2f".format(it.balance)}" },
            onSelect = { customer = it }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { type = "قبض" }, label = { Text(if (type == "قبض") "✓ سند قبض" else "سند قبض") })
            AssistChip(onClick = { type = "صرف" }, label = { Text(if (type == "صرف") "✓ سند صرف" else "سند صرف") })
        }
        FormField("المبلغ", amount) { amount = it }
        FormField("البيان", note) { note = it }
        Button(
            onClick = { if (customer != null) viewModel.bond(customer!!.id, type, amount.toDoubleOrNull() ?: 0.0, note) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = customer != null && (amount.toDoubleOrNull() ?: 0.0) > 0
        ) { Icon(Icons.Default.ReceiptLong, null); Spacer(Modifier.width(8.dp)); Text("حفظ السند") }
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
        FormField("النقد الفعلي المعدود", actual) { actual = it }
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
                Text("الرصيد الحالي: ${"%.2f".format(customer.balance)}", fontWeight = FontWeight.Bold)
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
private fun SaleReceiptDialog(receipt: SaleReceipt, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تم حفظ الفاتورة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("عميلنا العزيز ${receipt.customer?.name ?: "عميل نقدي"}")
                Text("عليكم فاتورة بمبلغ: ${"%.2f".format(receipt.invoice.total)}")
                Text("رصيدكم الإجمالي: ${"%.2f".format(receipt.invoice.newBalance)}")
                Text("رقم الفاتورة: ${receipt.invoice.id}", color = Color.Gray, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = { PrintAndShare.whatsapp(context, receipt.customer, receipt.invoice) },
                    enabled = receipt.customer != null
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("واتساب")
                }
                BluetoothPrintButton(receipt.invoice, receipt.customer?.name ?: "عميل نقدي")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
private fun BondReceiptDialog(receipt: BondReceipt, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تم حفظ السند") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("عميلنا العزيز ${receipt.customer.name}")
                if (receipt.bond.type == "قبض") {
                    Text("تم سداد مبلغ: ${"%.2f".format(receipt.bond.amount)}")
                } else {
                    Text("تم تسجيل سند صرف بمبلغ: ${"%.2f".format(receipt.bond.amount)}")
                }
                Text("برقم السند: ${receipt.bond.id}")
                Text("المبلغ المتبقي عليكم: ${"%.2f".format(receipt.customer.balance)}")
            }
        },
        confirmButton = {
            TextButton(onClick = { PrintAndShare.whatsappBond(context, receipt.customer, receipt.bond) }) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("إرسال عبر واتساب")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
private fun BluetoothPrintButton(invoice: InvoiceEntity, customerName: String) {
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
                                        val result = PrintAndShare.printBluetooth(device, invoice, customerName)
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
private fun FormField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
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
