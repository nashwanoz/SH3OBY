package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khamrnet.app.vm.POSViewModel
import kotlinx.coroutines.launch

@Composable
fun BondScreen(vm: POSViewModel) {
    var type by remember { mutableStateOf("قبض") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var userIdText by remember { mutableStateOf("") }
    var customerIdText by remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("سند قبض / صرف") }) }, scaffoldState = scaffoldState) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text("نوع السند")
            Row {
                RadioButton(selected = type == "قبض", onClick = { type = "قبض" })
                Spacer(Modifier.width(4.dp))
                Text("قبض")
                Spacer(Modifier.width(12.dp))
                RadioButton(selected = type == "صرف", onClick = { type = "صرف" })
                Spacer(Modifier.width(4.dp))
                Text("صرف")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = userIdText, onValueChange = { userIdText = it }, label = { Text("معرف المستخدم (اختياري)") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = customerIdText, onValueChange = { customerIdText = it }, label = { Text("معرف العميل (اختياري)") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") })
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt == null || amt <= 0.0) {
                    coroutineScope.launch { scaffoldState.snackbarHostState.showSnackbar("أدخل مبلغ صالح") }
                    return@Button
                }
                val uid = userIdText.toLongOrNull()
                val cid = customerIdText.toLongOrNull()
                vm.createBond(type = type, amount = amt, userId = uid, customerId = cid, notes = if (notes.isBlank()) null else notes) { ok, id, err ->
                    coroutineScope.launch {
                        if (ok) scaffoldState.snackbarHostState.showSnackbar("تم تسجيل السند") else scaffoldState.snackbarHostState.showSnackbar("خطأ: $err")
                    }
                }
            }) { Text("حفظ السند") }
        }
    }
}
