package com.smartlink.app.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SendWhatsAppDialog(
    phone: String,
    invoiceNumber: String,
    amount: String,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    var message by remember { mutableStateOf("عميلنا: \nفاتورة مشتريات رقم $invoiceNumber بقيمة $amount\nالرصيد الإجمالي = ") }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("إرسال فاتورة عبر واتساب") }, text = {
        Column {
            Text("إلى: $phone")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                maxLines = 8
            )
        }
    }, confirmButton = {
        TextButton(onClick = {
            sendWhatsApp(ctx, phone, message)
            onDismiss()
        }) { Text("فتح واتساب") }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("إلغاء") }
    })
}

fun sendWhatsApp(context: Context, phoneRaw: String, message: String) {
    val phone = phoneRaw.filter { it.isDigit() }
    val encoded = URLEncoder.encode(message, "UTF-8")
    val uriApp = Uri.parse("whatsapp://send?phone=$phone&text=$encoded")
    val intentApp = Intent(Intent.ACTION_VIEW, uriApp)

    try {
        intentApp.setPackage("com.whatsapp")
        context.startActivity(intentApp)
        return
    } catch (e: ActivityNotFoundException) {
    }
    try {
        intentApp.setPackage("com.whatsapp.w4b")
        context.startActivity(intentApp)
        return
    } catch (e: ActivityNotFoundException) {
    }
    val webUri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encoded")
    val webIntent = Intent(Intent.ACTION_VIEW, webUri)
    context.startActivity(webIntent)
}