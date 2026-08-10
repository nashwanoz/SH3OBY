package com.smartlink.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartlink.app.ui.sampleStock

@Composable
fun InventoryScreen() {
    Column {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AddBox, contentDescription = null)
                Text(" توريد")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Text(" تحويل")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleStock) { stock ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stock.name, style = MaterialTheme.typography.titleMedium)
                        Text(stock.unit, style = MaterialTheme.typography.bodySmall)
                        Text(
                            stock.quantity,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(top = 22.dp)
                        )
                        Text("متوفر", style = MaterialTheme.typography.bodySmall)
                        Text(stock.value, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}