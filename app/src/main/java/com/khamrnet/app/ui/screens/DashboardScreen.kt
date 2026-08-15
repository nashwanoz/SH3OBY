package com.khamrnet.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.khamrnet.app.AppSection
import com.khamrnet.app.ui.AppUiState

@Composable
fun DashboardScreen(state: AppUiState, onNavigate: (AppSection) -> Unit) {
    val stats = state.stats
    val isAdmin = state.user?.role == "ADMIN"
    val actions = (if (isAdmin) {
        listOf(
            AppSection.POS, AppSection.INVOICES, AppSection.REPORTS,
            AppSection.PRODUCTS, AppSection.USERS, AppSection.TRANSFERS,
            AppSection.CUSTOMERS, AppSection.BONDS, AppSection.SETTLEMENTS
        )
    } else {
        listOf(AppSection.POS, AppSection.INVOICES, AppSection.REPORTS, AppSection.CUSTOMERS, AppSection.BONDS)
    }).filter { state.user?.canAccess(it.name) == true }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp), // تقليص الهامش الخارجي قليلاً لتوفير مساحة
        verticalArrangement = Arrangement.spacedBy(10.dp) // تقليل المسافة بين الأجزاء الرئيسية
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) { // تصغير الحواف الداخلية لكرت الترحيب
                    Text("أهلًا بك في خمر نت", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("كل عملياتك محفوظة محليًا وتستمر حتى دون اتصال.", color = Color.White.copy(alpha = .8f), fontSize = 13.sp)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("مبيعات اليوم", "%.2f".format(stats.todaySales), Color(0xFF0F766E), Modifier.weight(1f))
                StatCard("العهدة / العجز", "%.2f".format(stats.carriedDifference), Color(0xFFD99A2B), Modifier.weight(1f))
            }
        }
        item {
            Text("الوصول السريع", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // تقليل الفراغ العمودي بين صفوف الأزرار من 12 إلى 8
                actions.chunked(2).forEach { rowItems ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { // تقليل الفراغ الأفقي بين الأزرار من 12 إلى 8
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
        shape = RoundedCornerShape(14.dp), // حواف أنعم تتناسق مع الحجم المصغر الجديد
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp), // تم ضغط الارتفاع الداخلي من 20 إلى 10 لتقليص المربع تماماً
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = section.icon, 
                contentDescription = section.title, 
                tint = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(24.dp) // تصغير حجم الأيقونة من 34 إلى 24 لتوفير مساحة عمودية ضخمة
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = section.title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 12.sp // ضبط حجم الخط ليتناسق بشكل جذاب مع الحجم الجديد
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) { // تصغير المسافة الداخلية للعدادات لتوفير مساحة طولية
            Text(title, color = Color.White.copy(alpha = .85f), fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
