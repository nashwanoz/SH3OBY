package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.AppViewModel
import com.khamrnet.app.ui.components.FormField
import com.khamrnet.app.ui.components.SearchChoiceField
import com.khamrnet.app.ui.components.formatDate

@Composable
fun BondsScreen(
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
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
        ) {
            Icon(Icons.Default.ReceiptLong, null)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text("حفظ واعتماد السند")
        }
    }
}