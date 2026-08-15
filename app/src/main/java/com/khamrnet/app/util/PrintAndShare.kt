package com.khamrnet.app.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.CustomerStatementRow
import com.khamrnet.app.data.FinancialBondEntity
import com.khamrnet.app.data.InvoiceEntity
import com.khamrnet.app.data.InvoiceLineEntity
import java.io.File
import java.nio.charset.Charset
import java.util.UUID

object PrintAndShare {
    
    // 1. دالة مشاركة الفاتورة المطورة والمستقرة للواتساب
    fun whatsapp(context: Context, customer: CustomerEntity?, invoice: InvoiceEntity) {
        val text = """
        اشعار فاتورة مبيعات ( شبكه خمر نت اللاسلكيه )
        
        عميلنا العزيز: ${customer?.name ?: "مبيعات نقدية"}
        عليكم فاتورة مبيعات بمبلغ: ${invoice.total.toInt()}
        رصيدكم السابق: ${invoice.previousBalance.toInt()}
        الإجمالي بعد الفاتورة: ${invoice.newBalance.toInt()}
    """.trimIndent()
    val cleanMobile = customer?.mobile?.filter { it.isDigit() }.orEmpty()
    
    // إصلاح صياغة الرابط وتشفير النص وإضافة الرقم إن وجد
    val encodedText = Uri.encode(text)
    val url = if (cleanMobile.isNotEmpty()) {
        "https://api.whatsapp.com/send?phone=$cleanMobile&text=$encodedText"
    } else {
        "https://api.whatsapp.com/send?text=$encodedText"
    }

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
        setPackage("com.whatsapp")
    }

    runCatching { 
        context.startActivity(intent) 
    }.getOrElse {
        val backupIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(Intent.createChooser(backupIntent, "مشاركة الفاتورة عبر"))
    }
}

    // 2. دالة مشاركة السندات المطورة والمستقرة للواتساب
   fun whatsappBond(context: Context, customer: CustomerEntity, bond: FinancialBondEntity) {
        val text = """
            إشعار حركة مالية - شبكة خمر نت
            
            عميلنا العزيز: ${customer.name}
            ${if (bond.type == "قبض") "تم استلام سند قبض" else "تم تسجيل سند صرف"} رقم: ${bond.id}
            مبلغ السند: ${format(bond.amount)} ريال
            رصيدكم السابق: ${format(bond.previousBalance)} ريال
            الإجمالي بعد السند: ${format(bond.newBalance)} ريال
    """.trimIndent()

    val cleanMobile = customer.mobile.filter { it.isDigit() }
    val encodedText = Uri.encode(text)

    // صياغة الرابط الصحيحة وتأمين حالة وجود رقم الهاتف أو عدمه
    val url = if (cleanMobile.isNotEmpty()) {
        "https://api.whatsapp.com/send?phone=$cleanMobile&text=$encodedText"
    } else {
        "https://api.whatsapp.com/send?text=$encodedText"
    }

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
        setPackage("com.whatsapp")
    }

    runCatching { 
        context.startActivity(intent) 
    }.getOrElse {
        val backupIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(Intent.createChooser(backupIntent, "إرسال السند عبر واتساب"))
    }
}
    fun receiptBytes(
        invoice: InvoiceEntity,
        customerName: String = "مبيعات نقدية",
        lines: List<InvoiceLineEntity> = emptyList()
    ): ByteArray {
        val receipt = """
            خمر نت
            ------------------------------
            رقم الفاتورة: ${invoice.id}
            العميل: $customerName
            طريقة الدفع: ${invoice.paymentType}
            التاريخ: ${formatDate(invoice.createdAt)}
            ${lines.joinToString("\n") { "${it.productName} ${it.quantity} ${it.unitName} = ${format(it.lineTotal)}" }}
            الإجمالي: ${format(invoice.total)}
            الرصيد السابق: ${format(invoice.previousBalance)}
            ${balanceLabel(invoice.newBalance)}
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
        customerName: String,
        lines: List<InvoiceLineEntity> = emptyList()
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
                output.write(receiptBytes(invoice, customerName, lines))
                output.write(byteArrayOf(0x1D, 0x56, 0x00))
                output.flush()
            }
        } finally {
            socket.close()
        }
    }
    fun bondReceiptBytes(customer: CustomerEntity, bond: FinancialBondEntity): ByteArray {
        val receipt = """
            خمر نت
            ------------------------------
            سند ${bond.type}
            رقم السند: ${bond.id}
            التاريخ: ${formatDate(bond.createdAt)}
            العميل: ${customer.name}
            رصيدكم السابق: ${format(bond.previousBalance)}
            مبلغ السند: ${format(bond.amount)}
            ${balanceLabel(bond.newBalance)}
            البيان: ${bond.note.ifBlank { "بدون بيان" }}
            ------------------------------
            شكرًا لتعاملكم معنا
            
        """.trimIndent()
        return runCatching { receipt.toByteArray(Charset.forName("CP864")) }
            .getOrElse { receipt.toByteArray(Charset.forName("windows-1256")) }
    }

    fun printBondBluetooth(
        device: BluetoothDevice,
        customer: CustomerEntity,
        bond: FinancialBondEntity
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
                output.write(bondReceiptBytes(customer, bond))
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
        val file = File(context.cacheDir, "statement-${customer.id}.pdf")
        createStatementPdf(file, customer, rows)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة كشف الحساب"))
    }

    // 3. دالة مشاركة كشف الحساب المطورة والمستقرة للواتساب
    fun shareStatementToWhatsapp(
        context: Context,
        customer: CustomerEntity,
        rows: List<CustomerStatementRow>
    ) {
        val summary = rows.joinToString("\n") {
            "${formatDate(it.createdAt)} - ${it.type} - ${signedAmountLabel(it.amount)} - ${balanceLabel(it.balanceAfter)}"
        }
        val text = """
            عميلنا العزيز: ${customer.name}
            كشف حسابكم التفصيلي:
            ${summary.ifBlank { "لا توجد حركات مالية مسجلة" }}
            الإجمالي الحالي المتبقي: ${balanceLabel(customer.balance)}
        """.trimIndent()
        
        val cleanMobile = customer.mobile.filter { it.isDigit() }
        val url = "https://whatsapp.com{Uri.encode(text)}"
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            setPackage("com.whatsapp")
        }
        
        runCatching { 
            context.startActivity(intent) 
        }.getOrElse {
            val backupIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(Intent.createChooser(backupIntent, "إرسال كشف الحساب عبر"))
        }
    }

    private fun createStatementPdf(
        file: File,
        customer: CustomerEntity,
        rows: List<CustomerStatementRow>
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 42
        val contentWidth = pageWidth - (margin * 2)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 13f
        }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(15, 118, 110)
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val lines = buildList {
            add("كشف حساب العميل")
            add("العميل: ${customer.name}")
            add("رقم الجوال: ${customer.mobile.ifBlank { "غير متوفر" }}")
            add(balanceLabel(customer.balance))
            add("------------------------------")
            rows.forEach { row ->
                add("${formatDate(row.createdAt)}  •  ${row.type}")
                add("المرجع: ${row.reference}")
                add("المبلغ: ${signedAmountLabel(row.amount)}")
                add("الرصيد بعد الحركة: ${balanceLabel(row.balanceAfter)}")
                add("------------------------------")
            }
            if (rows.isEmpty()) add("لا توجد حركات مالية مسجلة لهذا العميل")
        }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = margin.toFloat()
        
        lines.forEachIndexed { index, line ->
            val currentPaint = if (index == 0) titlePaint else paint
            val layout = StaticLayout.Builder.obtain(line, 0, line.length, currentPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setIncludePad(true)
                .build()
                if (y + layout.height > pageHeight - margin) {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = margin.toFloat()
            }
            canvas.save()
            canvas.translate(margin.toFloat(), y)
            layout.draw(canvas)
            canvas.restore()
            y += layout.height + 12f
        }
        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
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

    private fun signedAmountLabel(value: Double): String =
        if (value < 0) "دائن / له ${format(kotlin.math.abs(value))}"
        else "مدين / عليه ${format(value)}"

    private fun balanceLabel(value: Double): String = when {
        value < 0 -> "الرصيد: له ${format(kotlin.math.abs(value))}"
        value > 0 -> "الرصيد: عليه ${format(value)}"
        else -> "الرصيد: متزن 0.00"
    }

    private fun formatDate(timestamp: Long): String =
        java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale("ar")).format(java.util.Date(timestamp))
}
