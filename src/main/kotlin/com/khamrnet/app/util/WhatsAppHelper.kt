package com.khamrnet.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

object WhatsAppHelper {

    /**
     * Compose the required Arabic invoice message and open WhatsApp share UI.
     * If phone number is provided, uses the send API for that number (in international format without +).
     *
     * Template:
     * "عزيزي العميل: [Customer_Name]
     * قيمة الفاتورة الحالية: [Amount]
     * رصيدك السابق: [Prev_Balance]
     * الإجمالي المستحق: [New_Total]"
     */
    fun shareInvoice(context: Context, customerName: String, amount: Double, prevBalance: Double, newTotal: Double, phone: String? = null) {
        val template = StringBuilder()
        template.append("عزيزي العميل: ").append(customerName).append("\n")
        template.append("قيمة الفاتورة الحالية: ").append(formatAmount(amount)).append("\n")
        template.append("رصيدك السابق: ").append(formatAmount(prevBalance)).append("\n")
        template.append("الإجمالي المستحق: ").append(formatAmount(newTotal))

        shareText(context, template.toString(), phone)
    }

    private fun formatAmount(value: Double): String {
        return String.format("%.2f", value)
    }

    private fun shareText(context: Context, text: String, phone: String? = null) {
        val encoded = URLEncoder.encode(text, UTF_8.name())
        val url = if (!phone.isNullOrEmpty()) {
            // Ensure phone is digits only and international format without +
            val digits = phone.filter { it.isDigit() }
            "https://api.whatsapp.com/send?phone=$digits&text=$encoded"
        } else {
            "https://api.whatsapp.com/send?text=$encoded"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
