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
import androidx.compose.foundation.lazy.items
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

    val chunkedActions = actions.chunked(2)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. كارت الترحيب الرئيسي
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
        
        // 2. كروت الإحصائيات (مبيعات اليوم والعهدة)
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("مبيعات اليوم", "%.2f".format(stats.todaySales), Color(0xFF0F766E), Modifier.weight(1f))
                StatCard("العهدة / العجز", "%.2f".format(stats.carriedDifference), Color(0xFFD99A2B), Modifier.weight(1f))
            }
        }
        
        // 3. عنوان الوصول السريع
        item {
            Text("الوصول السريع", fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        
        // 4. عرض الأزرار كعناصر مستقلة ومجمدة داخل الـ LazyColumn لتحسين الأداء والخفة
        items(chunkedActions) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    DashboardActionCard(item, Modifier.weight(1f)) { onNavigate(item) }
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
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
