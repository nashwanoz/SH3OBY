package com.smartlink.app.ui

data class InvoicePreview(
    val number: String,
    val customer: String,
    val total: String,
    val type: String,
    val date: String
)

data class CustomerRow(
    val name: String,
    val phone: String,
    val balance: String
)

data class StockRow(
    val name: String,
    val unit: String,
    val quantity: String,
    val value: String
)

val sampleInvoices = listOf(
    InvoicePreview("AH-124", "مبيعات نقدية", "١٢٥,٠٠٠ ر.ي", "نقدي", "اليوم، ١١:٤٠ ص"),
    InvoicePreview("AH-123", "شركة الأفق", "٨٥,٥٠٠ ر.ي", "آجل", "اليوم، ١٠:١٥ ص"),
    InvoicePreview("AH-122", "مبيعات نقدية", "٤٢,٠٠٠ ر.ي", "نقدي", "أمس، ٠٤:٣٠ م"),
    InvoicePreview("AH-121", "متجر الندى", "١٨٠,٠٠٠ ر.ي", "آجل", "أمس، ٠١:٠٥ م"),
    InvoicePreview("AH-120", "مبيعات نقدية", "٣٦,٥٠٠ ر.ي", "نقدي", "أمس، ١٠:٢٢ ص")
)

val sampleCustomers = listOf(
    CustomerRow("شركة الأفق", "777 123 456", "٢٨٥,٠٠٠ ر.ي"),
    CustomerRow("متجر الندى", "777 654 321", "١٨٠,٠٠٠ ر.ي"),
    CustomerRow("مؤسسة الربيع", "733 445 678", "٩٥,٥٠٠ ر.ي"),
    CustomerRow("محمود علي", "711 222 444", "٠ ر.ي")
)

val sampleStock = listOf(
    StockRow("مياه معدنية", "كرتون", "٤٨", "١٤٤,٠٠٠ ر.ي"),
    StockRow("عصير برتقال", "كرتون", "٢٦", "١٠٤,٠٠٠ ر.ي"),
    StockRow("أرز بسمتي", "كيس", "٣٥", "٢٨٠,٠٠٠ ر.ي"),
    StockRow("زيت نباتي", "كرتون", "١٩", "١٧١,٠٠٠ ر.ي")
)