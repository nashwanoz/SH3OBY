package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khamrnet.app.data.UserEntity
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.AppViewModel
import com.khamrnet.app.ui.components.FormField
import com.khamrnet.app.ui.components.SearchChoiceField

@Composable
fun SettlementsScreen(state: AppUiState, viewModel: AppViewModel) {
    val cashiers = state.users.filter { it.role == "CASHIER" }
    var cashier by remember { mutableStateOf<UserEntity?>(null) }
    var actual by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                // تم التصحيح هنا لاستخدام width بدلاً من padding وتوليد مسافة حقيقية
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
