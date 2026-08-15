package com.khamrnet.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A)) // نفس التدرج الداكن الفخم لشاشة الدخول
                )
            ),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // كرت الترحيب الشفاف العصري
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("أهلًا بك في خمر نت", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    Text("كل عملياتك محفوظة محليًا وتستمر حتى دون اتصال.", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        }
        item {
            // كروت الإحصائيات بألوان متناسقة مع الهوية الجديدة (الذهبي الملكي والأزرق النيلي)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("مبيعات اليوم", "%.2f".format(stats.todaySales), Color(0xFF1E3A8A).copy(alpha = 0.4f), Color(0xFF38BDF8), Modifier.weight(1f))
                StatCard("العهدة / العجز", "%.2f".format(stats.carriedDifference), Color(0xFFD99A2B).copy(alpha = 0.15f), Color(0xFFD99A2B), Modifier.weight(1f))
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("الوصول السريع", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.chunked(2).forEach { rowItems ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    // مربعات الوصول السريع بتصميم زجاجي شفاف (Glassmorphism) مقتبس تماماً من حقول شاشة الدخول
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = section.icon, 
                contentDescription = section.title, 
                tint = Color(0xFFD99A2B), // تغيير لون الأيقونات للذهبي الفخم المتناسق مع أزرار الدخول
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = section.title, 
                fontWeight = FontWeight.SemiBold, 
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f) // تغيير لون الخط للأبيض الناعم
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, backgroundColor: Color, contentColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = backgroundColor), 
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = contentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
