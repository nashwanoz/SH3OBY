// app/src/main/kotlin/com/khamrnet/app/ui/screens/CustomerScreen.kt
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
fun CustomerScreen(vm: POSViewModel) {
    val customers by vm.customers.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("العملاء") }) }, scaffoldState = scaffoldState) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) { Text("إضافة عميل") }
            Spacer(Modifier.height(12.dp))
            LazyColumn {
                items(customers.size) { idx ->
                    val c = customers[idx]
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 4.dp) {
                        ListItem(text = { Text(c.name) }, secondaryText = { Text("الموبايل: ${c.mobile}  الرصيد: ${String.format("%.2f", c.balance)}") })
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("إضافة عميل جديد") }, text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("الموبايل") })
            }
        }, confirmButton = {
            Button(onClick = {
                if (name.isBlank() || mobile.isBlank()) {
                    coroutineScope.launch { scaffoldState.snackbarHostState.showSnackbar("يرجى تعبئة الحقول") }
                    return@Button
                }
                vm.addCustomer(name = name.trim(), mobile = mobile.trim()) { ok, id, err ->
                    coroutineScope.launch {
                        if (ok) {
                            scaffoldState.snackbarHostState.showSnackbar("تم إضافة العميل")
                            showAdd = false
                            name = ""
                            mobile = ""
                        } else {
                            scaffoldState.snackbarHostState.showSnackbar("خطأ: $err")
                        }
                    }
                }
            }) { Text("حفظ") }
        }, dismissButton = {
            TextButton(onClick = { showAdd = false }) { Text("إلغاء") }
        })
    }
}
