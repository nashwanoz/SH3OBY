package com.khamrnet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khamrnet.app.vm.AuthViewModel

@Composable
fun LoginScreen(vm: AuthViewModel, onLoginSuccess: (String) -> Unit) {
    val loginError by vm.loginError.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }

    Scaffold {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "خمر نت", style = MaterialTheme.typography.h4)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("اسم المستخدم") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("كلمة المرور") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                isLoading.value = true
                vm.login(username.trim(), password.trim()) { success, role ->
                    isLoading.value = false
                    if (success && role != null) {
                        onLoginSuccess(role)
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                if (isLoading.value) CircularProgressIndicator(color = MaterialTheme.colors.onPrimary, modifier = Modifier.size(20.dp)) else Text("دخول")
            }
            Spacer(Modifier.height(8.dp))
            if (!loginError.isNullOrEmpty()) {
                Text(text = loginError ?: "", color = MaterialTheme.colors.error)
            }
            Spacer(Modifier.height(12.dp))
            Text(text = "المستخدم الافتراضي: اسم المستخدم 1  |  كلمة المرور 1", style = MaterialTheme.typography.caption)
        }
    }
}
