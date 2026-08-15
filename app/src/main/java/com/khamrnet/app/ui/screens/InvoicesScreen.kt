package com.khamrnet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.components.EmptyState
import com.khamrnet.app.ui.components.formatDate

@Composable
fun InvoicesScreen(state: AppUiState, onNewInvoice: () -> Unit) {
    val userNames = state.users.associateBy { it.id }
    
    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A)) // التدرج الداكن الفخم المتناسق مع بقية الواجهات
                )
            )
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("فواتير المبيعات", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(
                    text = if (state.user?.role == "ADMIN") "عرض جميع فواتير المبيعات المسجلة" else "فواتير المبيعات الخاصة بك",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            // زر فاتورة جديدة باللون الذهبي الفخم
            Button(
                onClick = onNewInvoice,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD99A2B),
                    contentColor = Color(0xFF0F172A)
                )
            ) { 
                Text("فاتورة جديدة", fontWeight = FontWeight.Bold) 
            }
        }
        
        Spacer(Modifier.height(14.dp))
        
        if (state.invoices.isEmpty()) {
            EmptyState("لا توجد فواتير", "ستظهر الفواتير هنا بعد تسجيل أول عملية بيع")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.invoices, key = { it.id }) { invoice ->
                    val customer = state.customers.firstOrNull { it.id == invoice.customerId }
                    
                    // بطاقة الفاتورة بالتصميم الزجاجي الشفاف الأنيق (Glassmorphism)
                    Card(
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.04f)
                        )
                    ) {
                        Column(Modifier.padding(15.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = invoice.id, 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color(0xFFD99A2B) // كود الفاتورة باللون الذهبي الفخم
                                )
                                Text(
                                    text = "%.2f".format(invoice.total), 
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("العميل: ${customer?.name ?: "عميل نقدي"}", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                            Text("المستخدم: ${userNames[invoice.userId]?.displayName ?: invoice.userId}", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("${invoice.paymentType} • ${formatDate(invoice.createdAt)}", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
