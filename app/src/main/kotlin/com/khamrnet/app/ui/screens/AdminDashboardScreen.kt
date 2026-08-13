package com.khamrnet.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khamrnet.app.vm.AuthViewModel

@Composable
fun AdminDashboardScreen(onNavigatePOS: () -> Unit, onUsers: () -> Unit, onStockTransfer: () -> Unit, onSettlement: () -> Unit, vm: AuthViewModel) {
    val user = vm.currentUser.collectAsState().value
    Scaffold(topBar = { TopAppBar(title = { Text("لوحة تحكم المدير") }) }) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth().clickable { onNavigatePOS() }, elevation = 4.dp, shape = RoundedCornerShape(8.dp)) {
                ListItem(text = { Text("نقطة بيع سريعة") }, secondaryText = { Text("الوصول إلى شاشة POS") })
            }
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth().clickable { onUsers() }, elevation = 4.dp, shape = RoundedCornerShape(8.dp)) {
                ListItem(text = { Text("إدارة المستخدمين") }, secondaryText = { Text("إنشاء وتوزيع صلاحيات") })
            }
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth().clickable { onStockTransfer() }, elevation = 4.dp, shape = RoundedCornerShape(8.dp)) {
                ListItem(text = { Text("توزيع المخزون") }, secondaryText = { Text("نقل الكميات إلى صناديق الكاشير") })
            }
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth().clickable { onSettlement() }, elevation = 4.dp, shape = RoundedCornerShape(8.dp)) {
                ListItem(text = { Text("تصفية الكاشير") }, secondaryText = { Text("تسجيل المدفوعات والفرق") })
            }
            Spacer(Modifier.height(12.dp))
            user?.let { Text("مرحبا ${it.displayName}", style = MaterialTheme.typography.subtitle1) }
        }
    }
}
