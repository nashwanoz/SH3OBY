package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khamrnet.app.vm.POSViewModel
import kotlinx.coroutines.launch

@Composable
fun StockTransferScreen(vm: POSViewModel) {
    val products by vm.products.collectAsState()
    val users = remember { mutableStateListOf<com.khamrnet.app.data.entities.User>() }
    var selectedProductIndex by remember { mutableStateOf(0) }
    var selectedUserIndex by remember { mutableStateOf(0) }
    var qtyText by remember { mutableStateOf("0") }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val u = vm.repo.db.userDao().getAll()
        users.clear()
        users.addAll(u)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("تحويل مخزون") }) }, scaffoldState = scaffoldState) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text("اختر المنتج")
            if (products.isEmpty()) {
                Text("لا توجد منتجات")
            } else {
                products.forEachIndexed { idx, p ->
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        RadioButton(selected = selectedProductIndex == idx, onClick = { selectedProductIndex = idx })
                        Spacer(Modifier.width(8.dp))
                        Text("${p.name} السعر: ${String.format("%.2f", p.price)}")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("اختر المستخدم المستقبل")
            if (users.isEmpty()) Text("لا يوجد مستخدمون")
            else users.forEachIndexed { idx, u ->
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    RadioButton(selected = selectedUserIndex == idx, onClick = { selectedUserIndex = idx })
                    Spacer(Modifier.width(8.dp))
                    Text("${u.displayName} (id=${u.id})")
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = qtyText, onValueChange = { qtyText = it }, label = { Text("الكمية") })
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                val prod = products.getOrNull(selectedProductIndex)
                val user = users.getOrNull(selectedUserIndex)
                val qty = qtyText.toDoubleOrNull()
                if (prod == null || user == null || qty == null || qty <= 0.0) {
                    coroutineScope.launch { scaffoldState.snackbarHostState.showSnackbar("تأكد من اختيار المنتج والمستخدم وكمية صحيحة") }
                    return@Button
                }
                vm.transferStock(productId = prod.id, qty = qty, toUserId = user.id) { ok, err ->
                    coroutineScope.launch {
                        if (ok) scaffoldState.snackbarHostState.showSnackbar("تم التحويل") else scaffoldState.snackbarHostState.showSnackbar("خطأ: $err")
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("تحويل") }
        }
    }
}
