package com.smartlink.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FinanceScreen() {
    Column(modifier = Modifier.padding(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {}, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null)
                Text(" قبض")
            }
            Button(onClick = {}, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null)
                Text(" صرف")
            }
        }
        Text(
            "آخر الحركات",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 28.dp, bottom = 10.dp)
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("سند صرف MO-018")
                Text("نثريات المكتب · ١٥,٠٠٠ ر.ي")
                Text("معلق بانتظار الاعتماد", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}