package com.smartlink.app.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.runtime.mutableStateListOf

data class InvoicePreview(
    val number: String,
    val customer: String,
    val total: Long,
    val type: String,
    val date: String
)

data class CustomerRow(
    val name: String,
    val phone: String,
    val balance: Long
)

data class StockRow(
    val name: String,
    val unit: String,
    val quantity: Int,
    val value: Long
)

data class FinanceEntry(
    val title: String,
    val amount: Long,
    val type: String,
    val date: String
)

class SmartLinkStore(context: Context) {
    private val preferences = context.getSharedPreferences("smart_link_data", Context.MODE_PRIVATE)
    val invoices = mutableStateListOf<InvoicePreview>()
    val customers = mutableStateListOf<CustomerRow>()
    val stock = mutableStateListOf<StockRow>()
    val financeEntries = mutableStateListOf<FinanceEntry>()

    init {
        load()
    }

    fun addInvoice(customer: String, total: Long, type: String) {
        val number = "INV-%03d".format(invoices.size + 1)
        invoices.add(
            0,
            InvoicePreview(
                number = number,
                customer = customer.ifBlank { "مبيعات نقدية" },
                total = total,
                type = type,
                date = "الآن"
            )
        )
        save()
    }

    fun addCustomer(name: String, phone: String) {
        customers.add(0, CustomerRow(name = name, phone = phone, balance = 0))
        save()
    }

    fun addStock(name: String, unit: String, quantity: Int, unitCost: Long) {
        stock.add(
            0,
            StockRow(
                name = name,
                unit = unit,
                quantity = quantity,
                value = quantity * unitCost
            )
        )
        save()
    }

    fun addFinance(title: String, amount: Long, type: String) {
        financeEntries.add(
            0,
            FinanceEntry(title = title, amount = amount, type = type, date = "الآن")
        )
        save()
    }

    fun totalSales(): Long = invoices.sumOf { it.total }

    fun stockValue(): Long = stock.sumOf { it.value }

    private fun save() {
        fun <T> arrayOfItems(items: List<T>, mapper: (T) -> JSONObject): JSONArray {
            val array = JSONArray()
            items.forEach { array.put(mapper(it)) }
            return array
        }

        val invoiceJson = arrayOfItems(invoices) {
            val item = it
            JSONObject().apply {
                put("number", item.number)
                put("customer", item.customer)
                put("total", item.total)
                put("type", item.type)
                put("date", item.date)
            }
        }
        val customerJson = arrayOfItems(customers) {
            val item = it
            JSONObject().apply {
                put("name", item.name)
                put("phone", item.phone)
                put("balance", item.balance)
            }
        }
        val stockJson = arrayOfItems(stock) {
            val item = it
            JSONObject().apply {
                put("name", item.name)
                put("unit", item.unit)
                put("quantity", item.quantity)
                put("value", item.value)
            }
        }
        val financeJson = arrayOfItems(financeEntries) {
            val item = it
            JSONObject().apply {
                put("title", item.title)
                put("amount", item.amount)
                put("type", item.type)
                put("date", item.date)
            }
        }
        preferences.edit()
            .putString("invoices", invoiceJson.toString())
            .putString("customers", customerJson.toString())
            .putString("stock", stockJson.toString())
            .putString("finance", financeJson.toString())
            .apply()
    }

    private fun load() {
        runCatching {
            val invoiceArray = JSONArray(preferences.getString("invoices", "[]"))
            for (index in 0 until invoiceArray.length()) {
                val item = invoiceArray.getJSONObject(index)
                invoices.add(
                    InvoicePreview(
                        item.getString("number"),
                        item.getString("customer"),
                        item.getLong("total"),
                        item.getString("type"),
                        item.getString("date")
                    )
                )
            }
            val customerArray = JSONArray(preferences.getString("customers", "[]"))
            for (index in 0 until customerArray.length()) {
                val item = customerArray.getJSONObject(index)
                customers.add(
                    CustomerRow(
                        item.getString("name"),
                        item.getString("phone"),
                        item.getLong("balance")
                    )
                )
            }
            val stockArray = JSONArray(preferences.getString("stock", "[]"))
            for (index in 0 until stockArray.length()) {
                val item = stockArray.getJSONObject(index)
                stock.add(
                    StockRow(
                        item.getString("name"),
                        item.getString("unit"),
                        item.getInt("quantity"),
                        item.getLong("value")
                    )
                )
            }
            val financeArray = JSONArray(preferences.getString("finance", "[]"))
            for (index in 0 until financeArray.length()) {
                val item = financeArray.getJSONObject(index)
                financeEntries.add(
                    FinanceEntry(
                        item.getString("title"),
                        item.getLong("amount"),
                        item.getString("type"),
                        item.getString("date")
                    )
                )
            }
        }
    }
}

fun formatRiyal(value: Long): String = "%,d ر.ي".format(value)