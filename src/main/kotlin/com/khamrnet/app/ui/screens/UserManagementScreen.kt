package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khamrnet.app.data.entities.Role
import com.khamrnet.app.vm.POSViewModel
import kotlinx.coroutines.launch

@Composable
fun UserManagementScreen(vm: POSViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.CASHIER) }
    val users = remember { mutableStateListOf<com.khamrnet.app.data.entities.User>() }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val list = vm.repo.db.userDao().getAll()
        users.clear()
        users.addAll(list)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("إدارة المستخدمين") }) }, scaffoldState = scaffoldState) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("اسم المستخدم") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("كلمة المرور") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("الاسم الظاهر") })
            Spacer(Modifier.height(8.dp))
            Row {
                RadioButton(selected = role == Role.ADMIN, onClick = { role = Role.ADMIN })
                Spacer(Modifier.width(4.dp))
                Text("مدير")
                Spacer(Modifier.width(12.dp))
                RadioButton(selected = role == Role.CASHIER, onClick = { role = Role.CASHIER })
                Spacer(Modifier.width(4.dp))
                Text("كاشير")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (username.isBlank() || password.isBlank()) {
                    coroutineScope.launch { scaffoldState.snackbarHostState.showSnackbar("يرجى تعبئة اسم المستخدم وكلمة المرور") }
                    return@Button
                }
                coroutineScope.launch {
                    try {
                        val id = vm.repo.insertUser(com.khamrnet.app.data.entities.User(username = username.trim(), password = password.trim(), displayName = if (displayName.isBlank()) username.trim() else displayName.trim(), role = role))
                        val list = vm.repo.db.userDao().getAll()
                        users.clear()
                        users.addAll(list)
                        scaffoldState.snackbarHostState.showSnackbar("تم إنشاء المستخدم برقم $id")
                        username = ""; password = ""; displayName = ""
                    } catch (e: Exception) {
                        scaffoldState.snackbarHostState.showSnackbar("خطأ: ${e.message}")
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("إنشاء مستخدم") }

            Spacer(Modifier.height(12.dp))
            Text("المستخدمون الحاليون:", style = MaterialTheme.typography.subtitle1)
            LazyColumn {
                items(users.size) { idx ->
                    val u = users[idx]
                    ListItem(text = { Text(u.displayName) }, secondaryText = { Text("اسم المستخدم: ${u.username}  الدور: ${u.role}") })
                }
            }
        }
    }
}
