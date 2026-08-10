package com.smartlink.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.smartlink.app.ui.SmartLinkStore
import com.smartlink.app.ui.formatRiyal

private data class DashboardCard(
    val label: String,
    val value: String,
    val detail: String,
    val icon: ImageVector
)

@Composable
fun DashboardScreen(store: SmartLinkStore) {
    val cards = listOf(
        DashboardCard("المخزون", formatRiyal(store.stockValue()), "قيمة الأصناف", Icons.Default.Inventory2),
        DashboardCard("العملاء", store.customers.size.toString(), "عميل مسجل", Icons.Default.People),
        DashboardCard("الصندوق", formatRiyal(store.totalSales()), "إجمالي المبيعات", Icons.Default.Payments),
        DashboardCard("الفواتير", store.invoices.size.toString(), "فاتورة صادرة", Icons.Default.ReceiptLong)
    )

    Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
        Text(
            "ملخص العمل",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        Text(
            if (store.invoices.isEmpty() && store.customers.isEmpty() && store.stock.isEmpty()) {
                "ابدأ بإضافة أول عميل أو صنف أو فاتورة"
            } else {
                "هذه أرقامك الفعلية في هذا الجهاز"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cards) { card ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(card.label, style = MaterialTheme.typography.titleMedium)
                            Icon(card.icon, contentDescription = null)
                        }
                        Text(
                            card.value,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(top = 22.dp)
                        )
                        Text(
                            card.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}