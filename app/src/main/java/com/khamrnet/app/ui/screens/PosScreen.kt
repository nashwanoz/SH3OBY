package com.khamrnet.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("فاتورة مبيعات", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("الرقم: سيصدر عند الحفظ", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    Text("التاريخ: ${formatDate(System.currentTimeMillis())}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }
            AssistChip(
                onClick = {}, 
                label = { Text("${state.products.size} صنف", color = Color.White) }, 
                leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFFD99A2B)) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { credit = false; customer = null }, 
                enabled = credit,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD99A2B), contentColor = Color(0xFF0F172A))
            ) { Text("نقداً", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            
            OutlinedButton(
                onClick = { credit = true }, 
                enabled = !credit,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) { Text("آجل", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            if (credit) {
                Box(modifier = Modifier.weight(1f)) {
                    SearchChoiceField(
                        label = "ابحث عن العميل",
                        selected = customer,
                        options = state.customers,
                        display = { it.name },
                        secondary = { "الرصيد: ${"%.2f".format(it.balance)}" },
                        onSelect = { customer = it }
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (credit) {
                    Text(
                        text = "العميل: ${customer?.name ?: "لم يتم اختيار عميل"}", 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Bold,
                        color = if (customer != null) Color(0xFFD99A2B) else Color.Red
                    )
                    customer?.let {
                        Text(accountBalance(it.balance), fontSize = 13.sp, color = balanceColor(it.balance), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("العميل: عميل نقدي", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                    Text("مبيعات نقدية", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("الأصناف السريعة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
        Spacer(Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(quickProducts, key = { it.id }) { product ->
                AssistChip(
                    onClick = { selectedProduct = product },
                    label = { Text(text = product.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White) },
                    shape = RoundedCornerShape(8.dp),
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.05f))
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = productQuery,
            onValueChange = { productQuery = it },
            label = { Text("بحث سريع عن صنف", color = Color.White.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD99A2B),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
            )
        )

        if (searchResults.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            searchResults.forEach { product ->
                Card(
                    Modifier.fillMaxWidth().padding(bottom = 6.dp).clickable {
                        selectedProduct = product
                        productQuery = ""
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(product.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${"%.2f".format(product.price)} / ${product.unitName}", color = Color(0xFFD99A2B))
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("أصناف الفاتورة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
        
        if (cart.isEmpty()) {
            Text("اضغط على أي صنف لإضافته إلى الفاتورة", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
        } else {
            Spacer(Modifier.height(8.dp))
            cart.forEachIndexed { index, line ->
                Card(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp), 
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(line.product.name, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${line.quantity} ${line.unitName} × ${"%.2f".format(line.unitPrice)}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                        Text("%.2f".format(line.lineTotal), fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = { cart = cart.toMutableList().also { it.removeAt(index) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف الصنف", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("الإجمالي", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("%.2f".format(invoiceTotal), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD99A2B))
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
            enabled = cart.isNotEmpty() && (!credit || customer != null),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD99A2B),
                contentColor = Color(0xFF0F172A),
                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                disabledContentColor = Color.White.copy(alpha = 0.3f)
            )
        ) { 
            Text("حفظ واعتماد الفاتورة", fontSize = 16.sp, fontWeight = FontWeight.Bold) 
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
                    if (product.caseUnitName.isNotBlank()) {
                        AssistChip(
                            onClick = { unit = product.caseUnitName },
                            label = { Text(if (unit == product.caseUnitName) "✓ ${product.caseUnitName}" else product.caseUnitName) }
                        )
                    }
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
