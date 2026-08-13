package com.khamrnet.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.khamrnet.app.data.entities.*
import com.khamrnet.app.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

data class InvoiceLineUi(
    val productId: Long,
    val productName: String,
    val unitName: String,
    val unitMultiplier: Double,
    var quantity: Double,
    var lineTotal: Double
)

class POSViewModel(private val repo: AppRepository) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers

    private val _currentLines = MutableStateFlow<List<InvoiceLineUi>>(emptyList())
    val currentLines: StateFlow<List<InvoiceLineUi>> = _currentLines

    private val _lastInvoiceId = MutableStateFlow<Long?>(null)
    val lastInvoiceId: StateFlow<Long?> = _lastInvoiceId

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repo.seedDefaultProductsIfEmpty()
            _products.value = repo.getAllProducts()
            _customers.value = repo.getAllCustomers()
        }
    }

    fun reloadCustomers() {
        viewModelScope.launch {
            _customers.value = repo.getAllCustomers()
        }
    }

    fun addLine(product: Product, unit: ProductUnit, qty: Double) {
        val list = _currentLines.value.toMutableList()
        val existing = list.indexOfFirst { it.productId == product.id && it.unitName == unit.name }
        val lineTotal = product.price * qty
        if (existing >= 0) {
            val ex = list[existing]
            ex.quantity += qty
            ex.lineTotal = ex.quantity * product.price
            list[existing] = ex
        } else {
            val ui = InvoiceLineUi(
                productId = product.id,
                productName = product.name,
                unitName = unit.name,
                unitMultiplier = unit.multiplier,
                quantity = qty,
                lineTotal = lineTotal
            )
            list.add(ui)
        }
        _currentLines.value = list
    }

    fun removeLine(index: Int) {
        val list = _currentLines.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _currentLines.value = list
        }
    }

    fun clearCurrentLines() {
        _currentLines.value = emptyList()
    }

    fun getTotal(): Double = _currentLines.value.sumOf { it.lineTotal }

    // Create invoice in DB: requires cashierId and optional customerId
    fun finalizeInvoice(cashierId: Long, customerId: Long?, onResult: (Boolean, Long?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                if (_currentLines.value.isEmpty()) {
                    onResult(false, null, "لا توجد عناصر في الفاتورة")
                    return@launch
                }
                val lines = _currentLines.value.map {
                    InvoiceLine(
                        id = 0,
                        invoiceId = 0,
                        productId = it.productId,
                        unitId = null,
                        quantity = it.quantity,
                        lineTotal = it.lineTotal,
                        posted = 0
                    )
                }
                val invId = repo.createInvoice(cashierId = cashierId, customerId = customerId, lines = lines)
                _lastInvoiceId.value = invId
                _currentLines.value = emptyList()
                onResult(true, invId, null)
            } catch (e: Exception) {
                onResult(false, null, e.message ?: "خطأ أثناء إنشاء الفاتورة")
            }
        }
    }

    // Customers
    fun addCustomer(name: String, mobile: String, onDone: (Boolean, Long?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val id = repo.addCustomer(Customer(name = name, mobile = mobile, balance = 0.0))
                _customers.value = repo.getAllCustomers()
                onDone(true, id, null)
            } catch (e: Exception) {
                onDone(false, null, e.message)
            }
        }
    }

    // Bonds
    fun createBond(type: String, amount: Double, userId: Long?, customerId: Long?, notes: String?, onDone: (Boolean, Long?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val id = repo.createBond(Bond(type = type, amount = amount, userId = userId, customerId = customerId, notes = notes))
                onDone(true, id, null)
            } catch (e: Exception) {
                onDone(false, null, e.message)
            }
        }
    }

    // Settlement
    fun performSettlement(cashierId: Long, physicalCash: Double, onDone: (Boolean, Long?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // expected cash is the current cashbox balance
                val cb = repo.db.cashBoxDao().findByOwner(cashierId)
                val expected = cb?.balance ?: 0.0
                val diff = physicalCash - expected
                // create settlement record
                val id = repo.createSettlement(Settlement(cashierId = cashierId, physicalCash = physicalCash, expectedCash = expected, difference = diff))
                // set cashbox balance to physicalCash (collected cash)
                if (cb != null) {
                    repo.db.cashBoxDao().updateBalance(cb.id, physicalCash)
                } else {
                    repo.db.cashBoxDao().insert(CashBox(name = "صندوق مبيعات $cashierId", ownerUserId = cashierId, balance = physicalCash))
                }
                onDone(true, id, null)
            } catch (e: Exception) {
                onDone(false, null, e.message ?: "خطأ أثناء التصفية")
            }
        }
    }

    // Stock transfer (admin)
    fun transferStock(productId: Long, qty: Double, toUserId: Long, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repo.transferStockToUser(productId = productId, qty = qty, toUserId = toUserId)
                onDone(true, null)
            } catch (e: Exception) {
                onDone(false, e.message)
            }
        }
    }

    companion object {
        fun provideFactory(repo: AppRepository): ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return POSViewModel(repo) as T
                }
            }
        }
    }
}
