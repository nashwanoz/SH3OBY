package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khamrnet.app.data.ProductEntity
import com.khamrnet.app.data.UserEntity
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.AppViewModel
import com.khamrnet.app.ui.components.FormField
import com.khamrnet.app.ui.components.SearchChoiceField

@Composable
fun TransfersScreen(state: AppUiState, viewModel: AppViewModel) {
    var cashier by remember { mutableStateOf<UserEntity?>(null) }
    var product by remember { mutableStateOf<ProductEntity?>(null) }
    var quantity by remember { mutableStateOf("1") }
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
            onClick = {
                if (cashier != null && product != null) {
                    viewModel.transferStock(cashier!!.id, product!!.id, quantity.toIntOrNull() ?: 0)
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = cashier != null && product != null && (quantity.toIntOrNull() ?: 0) > 0
        ) {
            Icon(Icons.Default.SwapHoriz, null)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text("تنفيذ التحويل")
        }
        Divider()
        Text("ملاحظة: لا يستطيع الكاشير بيع كمية تتجاوز مخزونه الفرعي.", color = MaterialTheme.colorScheme.primary)
    }
}
