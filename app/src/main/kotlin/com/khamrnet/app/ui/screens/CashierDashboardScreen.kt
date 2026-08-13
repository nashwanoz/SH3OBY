package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khamrnet.app.vm.AuthViewModel

@Composable
fun CashierDashboardScreen(onNavigatePOS: () -> Unit, onCustomers: () -> Unit, onBonds: () -> Unit, onSettlement: () -> Unit, vm: AuthViewModel) {
    val user = vm.currentUser.collectAsState().value
    Scaffold(topBar = { TopAppBar(title = { Text("لوحة تحكم الكاشير") }) }) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Button(onClick = onNavigatePOS, modifier = Modifier.fillMaxWidth()) { Text("نقطة بيع سريعة") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCustomers, modifier = Modifier.fillMaxWidth()) { Text("العملاء") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBonds, modifier = Modifier.fillMaxWidth()) { Text("سندات قبض وصرف") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onSettlement, modifier = Modifier.fillMaxWidth()) { Text("تصفية الكاشير") }
            Spacer(Modifier.height(12.dp))
            user?.let { Text("مرحبا ${it.displayName}", style = MaterialTheme.typography.subtitle1) }
        }
    }
}
