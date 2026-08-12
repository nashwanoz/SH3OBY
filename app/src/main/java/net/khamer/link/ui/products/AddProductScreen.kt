package net.khamer.link.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddProductScreen(viewModel: AddProductViewModel) {
    val name by viewModel.productName.collectAsState()
    val sku by viewModel.productSku.collectAsState()
    val desc by viewModel.productDesc.collectAsState()
    val units by viewModel.units.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val message by viewModel.message.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Text("إضافة صنف جديد", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = name, onValueChange = { viewModel.productName.value = it }, label = { Text("اسم الصنف") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = sku, onValueChange = { viewModel.productSku.value = it }, label = { Text("الباركود / SKU (اختياري)") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = desc, onValueChange = { viewModel.productDesc.value = it }, label = { Text("الوصف (اختياري)") }, maxLines = 3)
        Spacer(Modifier.height(12.dp))

        Text("الوحدات", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        units.forEach { u ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    OutlinedTextField(value = u.name, onValueChange = { u.name = it; viewModel.units.value = viewModel.units.value }, label = { Text("اسم الوحدة") }, singleLine = true)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = u.multiplier, onValueChange = { u.multiplier = it; viewModel.units.value = viewModel.units.value }, label = { Text("معامل للوحدة الأساسية") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = u.price, onValueChange = { u.price = it; viewModel.units.value = viewModel.units.value }, label = { Text("سعر الوحدة") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Button(onClick = { viewModel.removeUnit(u.id) }) {
                            Text("حذف الوحدة")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { viewModel.addUnit() }) {
            Text("إضافة وحدة جديدة")
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = { viewModel.saveProduct() }, enabled = !saving) {
            Text(if (saving) "جاري الحفظ..." else "حفظ الصنف")
        }

        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}
