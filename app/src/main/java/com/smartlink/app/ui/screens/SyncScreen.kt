package com.smartlink.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SyncScreen() {
    val pending = listOf(
        "فاتورة AH-124 · بانتظار المزامنة",
        "سند قبض AH-044 · بانتظار المزامنة",
        "رسالة واتساب · محفوظة في الطابور"
    )
    Column(modifier = Modifier.padding(20.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.CloudOff, contentDescription = null)
                Text("٣ عمليات بانتظار المعالجة", modifier = Modifier.padding(top = 10.dp))
                Text("سيتم تحديثها عند توفر الاتصال")
            }
        }
        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text(" فحص الآن")
        }
        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(pending) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(item, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}