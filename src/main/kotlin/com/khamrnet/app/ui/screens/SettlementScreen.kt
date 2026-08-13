package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khamrnet.app.vm.POSViewModel
import kotlinx.coroutines.launch

@Composable
fun SettlementScreen(vm: POSViewModel, isAdmin: Boolean) {
    val customers by vm.customers.collectAsState()
    var cashierIdText by remember { mutableStateOf("") }
    var physicalCashText by remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("تصفية الكاشير") }) }, scaffoldState = scaffoldState) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            if (isAdmin) {
                OutlinedTextField(value = cashierIdText, onValueChange = { cashierIdText = it }, label = { Text("معرّف الكاشير") })
            } else {
                OutlinedTextField(value = cashierIdText, onValueChange = { cashierIdText = it }, label = { Text("معرّف الكاشير (يمكن تركه فارغاً)") })
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = physicalCashText, onValueChange = { physicalCashText = it }, label = { Text("المبلغ الفعلي المستلم") })
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                val cashierId = cashierIdText.toLongOrNull() ?: 1L
                val physical = physicalCashText.toDoubleOrNull()
                if (physical == null) {
                    coroutineScope.launch { scaffoldState.snackbarHostState.showSnackbar("أدخل مبلغًا صالحًا") }
                    return@Button
                }
                vm.performSettlement(cashierId = cashierId, physicalCash = physical) { ok, id, err ->
                    coroutineScope.launch {
                        if (ok) scaffoldState.snackbarHostState.showSnackbar("تمت التصفية (id=$id)") else scaffoldState.snackbarHostState.showSnackbar("خطأ: $err")
                    }
                }
            }) {
                Text("تنفيذ التصفية")
            }

            Spacer(Modifier.height(16.dp))
            Text("قائمة العملاء (للاطلاع السريع):", style = MaterialTheme.typography.subtitle1)
            LazyColumn {
                items(customers.size) { idx ->
                    val c = customers[idx]
                    ListItem(text = { Text(c.name) }, secondaryText = { Text("الهاتف: ${c.mobile}  الرصيد: ${String.format("%.2f", c.balance)}") })
                }
            }
        }
    }
}
