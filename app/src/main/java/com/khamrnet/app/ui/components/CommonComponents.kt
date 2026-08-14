package com.khamrnet.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun <T> SearchChoiceField(
    label: String,
    selected: T?,
    options: List<T>,
    display: (T) -> String = { it.toString() },
    secondary: (T) -> String = { "" },
    onSelect: (T?) -> Unit
) {
    val selectedLabel = selected?.let(display).orEmpty()
    var query by remember(selectedLabel) { mutableStateOf(selectedLabel) }
    val matches = options.filter { display(it).contains(query.trim(), ignoreCase = true) }.take(8)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { value ->
                query = value
                if (selected != null && value != selectedLabel) onSelect(null)
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (selected != null) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تم اختيار: $selectedLabel", color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = { query = ""; onSelect(null) }) { Text("تغيير") }
            }
        } else if (query.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                matches.forEach { option ->
                    Card(
                        Modifier.fillMaxWidth().clickable {
                            query = display(option)
                            onSelect(option)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(display(option), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            secondary(option).takeIf { it.isNotBlank() }?.let {
                                Text(it, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                if (matches.isEmpty()) {
                    Text("لا توجد نتائج مطابقة", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    numeric: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { nextValue ->
            onChange(if (numeric) sanitizeNumber(nextValue) else nextValue)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text
        )
    )
}

private fun sanitizeNumber(value: String): String {
    val normalized = value
        .replace(',', '.')
        .filter { it.isDigit() || it == '.' }
    val separatorIndex = normalized.indexOf('.')
    return if (separatorIndex == -1) {
        normalized
    } else {
        normalized.take(separatorIndex + 1) +
            normalized.drop(separatorIndex + 1).replace(".", "")
    }
}

@Composable
fun EmptyState(title: String, description: String) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
            Spacer(Modifier.size(10.dp))
            Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 17.sp)
            Text(description, color = Color.Gray)
        }
    }
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale("ar")).format(Date(timestamp))

fun accountBalance(value: Double): String = when {
    value > 0 -> "عليه: %.2f".format(value)
    value < 0 -> "له: %.2f".format(kotlin.math.abs(value))
    else -> "متزن: 0.00"
}

fun balanceColor(value: Double): Color = when {
    value > 0 -> Color(0xFFB3261E)
    value < 0 -> Color(0xFF0F766E)
    else -> Color.Gray
}
