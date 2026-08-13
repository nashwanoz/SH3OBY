package com.khamrnet.app.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.CustomerStatementRow
import com.khamrnet.app.data.FinancialBondEntity
import com.khamrnet.app.data.InvoiceEntity
import java.io.File
import java.nio.charset.Charset
import java.util.UUID

object PrintAndShare {
    fun whatsapp(context: Context, customer: CustomerEntity?, invoice: InvoiceEntity) {
        val text = """
            عميلنا العزيز ${customer?.name ?: "عميلنا العزيز"}
            عليكم فاتورة بمبلغ: ${format(invoice.total)}
            رصيدكم الإجمالي: ${format(invoice.newBalance)}
            رقم الفاتورة: ${invoice.id}
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

    fun whatsappBond(context: Context, customer: CustomerEntity, bond: FinancialBondEntity) {
        val text = if (bond.type == "قبض") {
            """
                عميلنا العزيز ${customer.name}
                تم سداد مبلغ: ${format(bond.amount)}
                برقم السند: ${bond.id}
                المبلغ المتبقي عليكم: ${format(customer.balance)}
            """.trimIndent()
        } else {
            """
                عميلنا العزيز ${customer.name}
                تم تسجيل سند صرف بمبلغ: ${format(bond.amount)}
                برقم السند: ${bond.id}
                المبلغ المتبقي عليكم: ${format(customer.balance)}
            """.trimIndent()
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${customer.mobile}")
            putExtra("sms_body", text)
            setPackage("com.whatsapp")
        }
        runCatching { context.startActivity(intent) }.getOrElse {
            context.startActivity(Intent.createChooser(intent, "إرسال عبر واتساب"))
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

    fun pairedPrinters(): List<BluetoothDevice> =
        runCatching {
            BluetoothAdapter.getDefaultAdapter()?.bondedDevices
                ?.sortedBy { it.name ?: it.address }
                .orEmpty()
        }.getOrDefault(emptyList())

    fun printBluetooth(
        device: BluetoothDevice,
        invoice: InvoiceEntity,
        customerName: String
    ): Result<Unit> = runCatching {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: error("لا يدعم الجهاز Bluetooth")
        check(adapter.isEnabled) { "فعّل Bluetooth أولًا" }
        val socket = device.createRfcommSocketToServiceRecord(
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        )
        try {
            adapter.cancelDiscovery()
            socket.connect()
            socket.outputStream.use { output ->
                output.write(byteArrayOf(0x1B, 0x40))
                output.write(receiptBytes(invoice, customerName))
                output.write(byteArrayOf(0x1D, 0x56, 0x00))
                output.flush()
            }
        } finally {
            socket.close()
        }
    }

    fun shareStatement(
        context: Context,
        customer: CustomerEntity,
        rows: List<CustomerStatementRow>
    ) {
        val body = buildString {
            appendLine("كشف حساب العميل")
            appendLine("العميل: ${customer.name}")
            appendLine("الرصيد الحالي: ${format(customer.balance)}")
            appendLine("------------------------------")
            rows.forEach { row ->
                appendLine("${formatDate(row.createdAt)} - ${row.type}")
                appendLine("المرجع: ${row.reference}")
                appendLine("المبلغ: ${format(row.amount)}")
                appendLine("الرصيد بعد الحركة: ${format(row.balanceAfter)}")
                appendLine("------------------------------")
            }
        }
        val file = File(context.cacheDir, "statement-${customer.id}.txt")
        file.writeText(body, Charset.forName("UTF-8"))
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة كشف الحساب"))
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

    private fun formatDate(timestamp: Long): String =
        java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale("ar")).format(java.util.Date(timestamp))
}
