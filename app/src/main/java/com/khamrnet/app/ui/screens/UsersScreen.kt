package com.khamrnet.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.khamrnet.app.AppSection
import com.khamrnet.app.data.UserEntity
import com.khamrnet.app.data.UserPermissions
import com.khamrnet.app.ui.AppUiState
import com.khamrnet.app.ui.AppViewModel
import com.khamrnet.app.ui.components.FormField

@Composable
fun UsersScreen(state: AppUiState, viewModel: AppViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserEntity?>(null) }
    val nextUserCode = (state.users.mapNotNull { it.userCode.toIntOrNull() }.maxOrNull() ?: 1) + 1
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("المستخدمون", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("لكل كاشير مخزون فرعي وصندوق مستقل تلقائيًا", color = Color.Gray, fontSize = 12.sp)
            }
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "إضافة") }
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.users) { user ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("كود المستخدم: ${user.userCode}", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                if (user.role == "ADMIN") "مدير — جميع الصلاحيات" else "الصلاحيات مخصصة حسب إعداد المدير",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(onClick = {}, label = { Text(if (user.role == "ADMIN") "مدير" else "كاشير") })
                            IconButton(onClick = { editingUser = user }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل المستخدم")
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAdd) UserEditorDialog(null, viewModel, nextUserCode.toString()) { showAdd = false }
    editingUser?.let { user ->
        UserEditorDialog(user, viewModel, user.userCode) { editingUser = null }
    }
}

@Composable
private fun UserEditorDialog(user: UserEntity?, viewModel: AppViewModel, defaultUserCode: String, onDismiss: () -> Unit) {
    var name by remember(user?.id) { mutableStateOf(user?.displayName ?: "") }
    var userCode by remember(user?.id, defaultUserCode) { mutableStateOf(user?.userCode ?: defaultUserCode) }
    var password by remember(user?.id) { mutableStateOf("") }
    var enabledSections by remember(user?.id) {
        mutableStateOf(
            AppSection.values().filter {
                user?.let { account -> account.canAccess(it.name) } ?: it in setOf(
                    AppSection.POS, AppSection.INVOICES, AppSection.REPORTS, AppSection.CUSTOMERS, AppSection.BONDS
                )
            }.map { it.name }.toSet()
        )
    }
    var canWhatsapp by remember(user?.id) { mutableStateOf(user?.canWhatsapp ?: true) }
    val permissions = UserPermissions(
        canHome = "HOME" in enabledSections,
        canPos = "POS" in enabledSections,
        canInvoices = "INVOICES" in enabledSections,
        canReports = "REPORTS" in enabledSections,
        canProducts = "PRODUCTS" in enabledSections,
        canUsers = "USERS" in enabledSections,
        canTransfers = "TRANSFERS" in enabledSections,
        canCustomers = "CUSTOMERS" in enabledSections,
        canBonds = "BONDS" in enabledSections,
        canSettlements = "SETTLEMENTS" in enabledSections,
        canWhatsapp = canWhatsapp
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "تسجيل كاشير" else "تعديل بيانات المستخدم") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("اسم المستخدم الظاهر", name) { name = it }
                FormField("كود المستخدم الرقمي", userCode, numeric = true) { userCode = it }
                OutlinedTextField(
                    password,
                    { password = it.filter(Char::isDigit) },
                    label = { Text(if (user == null) "كلمة المرور الرقمية" else "كلمة مرور جديدة (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                Divider()
                Text("صلاحيات الشاشات", fontWeight = FontWeight.Bold)
                Text("اضغط على اسم الشاشة أو علامة الصح لتفعيلها للمستخدم.", color = Color.Gray, fontSize = 12.sp)
                AppSection.values().forEach { screen ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            enabledSections = enabledSections.toMutableSet().also {
                                if (!it.add(screen.name)) it.remove(screen.name)
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = screen.name in enabledSections,
                            onCheckedChange = { checked ->
                                enabledSections = enabledSections.toMutableSet().also {
                                    if (checked) it.add(screen.name) else it.remove(screen.name)
                                }
                            }
                        )
                        Text(screen.title)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().clickable { canWhatsapp = !canWhatsapp },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = canWhatsapp, onCheckedChange = { canWhatsapp = it })
                    Text("إرسال WhatsApp في كل الشاشات")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val existingUser = user
                    if (existingUser == null) {
                        viewModel.createUser(name, userCode, password, permissions)
                    } else {
                        viewModel.updateUser(existingUser, name, userCode, password, permissions)
                    }
                    onDismiss()
                },
                enabled = name.isNotBlank() && userCode.isNotBlank() && (user != null || password.isNotBlank())
            ) { Text(if (user == null) "إنشاء" else "حفظ التعديلات") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
