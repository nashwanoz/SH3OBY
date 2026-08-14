package com.khamrnet.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.ProductEntity
import com.khamrnet.app.data.SaleLineInput
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.AppViewModel
import com.khamrnet.app.ui.components.SearchChoiceField
import com.khamrnet.app.ui.components.accountBalance
import com.khamrnet.app.ui.components.balanceColor
import com.khamrnet.app.ui.components.formatDate

@Composable
fun PosScreen(
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
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp)
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
            Button(onClick = { credit = false; customer = null }, enabled = credit) { Text("نقداً", fontSize = 12.sp) }
            OutlinedButton(onClick = { credit = true }, enabled = !credit) { Text("آجل", fontSize = 12.sp) }
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
                    Modifier.fillMaxWidth().padding(bottom = 6.dp).clickable {
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
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(line.product.name, fontWeight = FontWeight.Bold)
                            Text("${line.quantity} ${line.unitName} × ${"%.2f".format(line.unitPrice)}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text("%.2f".format(line.lineTotal), fontWeight = FontWeight.Bold)
                        IconButton(onClick = { cart = cart.toMutableList().also { it.removeAt(index) } }) {
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
            ) { Text("حفظ واعتماد الفاتورة") }
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
        colors = CardDefaults.cardColors(containerColor = if (stock > 0) Color.White else Color(0xFFF1E7E7))
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.SpaceBetween) {
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
private fun AddLineDialog(product: ProductEntity, onDismiss: () -> Unit, onAdd: (String, Int) -> Unit) {
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