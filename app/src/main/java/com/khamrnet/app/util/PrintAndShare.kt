package com.khamrnet.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.InvoiceEntity
import java.io.File
import java.nio.charset.Charset

object PrintAndShare {
    fun whatsapp(context: Context, customer: CustomerEntity?, invoice: InvoiceEntity) {
        val text = """
            عزيزي العميل: ${customer?.name ?: "عميل نقدي"}
            قيمة الفاتورة الحالية: ${format(invoice.total)}
            رصيدك السابق: ${format(invoice.previousBalance)}
            الإجمالي المستحق: ${format(invoice.newBalance)}
        """.trimIndent()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${customer?.mobile ?: ""}")
            putExtra("sms_body", text)
            setPackage("com.whatsapp")
        }
        runCatching { context.startActivity(intent) }.getOrElse {
            context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة"))
        }
    }

    fun receiptBytes(invoice: InvoiceEntity, customerName: String = "عميل نقدي"): ByteArray {
        val receipt = """
            خمر نت
            ------------------------------
            رقم الفاتورة: ${invoice.id}
            العميل: $customerName
            طريقة الدفع: ${invoice.paymentType}
            الإجمالي: ${format(invoice.total)}
            الرصيد السابق: ${format(invoice.previousBalance)}
            الإجمالي المستحق: ${format(invoice.newBalance)}
            ------------------------------
            شكرًا لتعاملكم معنا
            
        """.trimIndent()
        return runCatching { receipt.toByteArray(Charset.forName("CP864")) }
            .getOrElse { receipt.toByteArray(Charset.forName("windows-1256")) }
    }

    fun shareReceipt(context: Context, invoice: InvoiceEntity, customerName: String) {
        val file = File(context.cacheDir, "receipt-${invoice.id}.txt")
        file.writeBytes(receiptBytes(invoice, customerName))
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "إرسال إلى الطابعة الحرارية"))
    }

    private fun format(value: Double): String = "%.2f".format(value)
}