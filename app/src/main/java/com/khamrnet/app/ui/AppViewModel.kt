package com.khamrnet.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khamrnet.app.data.AppRepository
import com.khamrnet.app.data.CustomerStatementRow
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.FinancialBondEntity
import com.khamrnet.app.data.ProductEntity
import com.khamrnet.app.data.UserEntity
import com.khamrnet.app.data.InvoiceEntity
import com.khamrnet.app.sync.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DashboardStats(
    val todaySales: Double = 0.0,
    val carriedDifference: Double = 0.0
)

data class SaleReceipt(
    val invoice: InvoiceEntity,
    val customer: CustomerEntity?
)

data class BondReceipt(
    val bond: FinancialBondEntity,
    val customer: CustomerEntity
)

data class AppUiState(
    val ready: Boolean = false,
    val user: UserEntity? = null,
    val products: List<ProductEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val users: List<UserEntity> = emptyList(),
    val customerLastMovement: Map<Long, Long> = emptyMap(),
    val stock: Map<Long, Int> = emptyMap(),
    val invoices: List<InvoiceEntity> = emptyList(),
    val stats: DashboardStats = DashboardStats(),
    val saleReceipt: SaleReceipt? = null,
    val bondReceipt: BondReceipt? = null,
    val message: String? = null,
    val error: String? = null
)


class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()
            SyncWorker.schedule(getApplication())
            repository.observeProducts().collectLatest { products ->
                _state.value = _state.value.copy(products = products, ready = true)
            }
        }
        viewModelScope.launch {
            repository.observeCustomers().collectLatest { customers ->
                _state.value = _state.value.copy(
                    customers = customers,
                    customerLastMovement = repository.customerLastMovements()
                )
            }
        }
        viewModelScope.launch {
            repository.observeUsers().collectLatest { users ->
                _state.value = _state.value.copy(users = users)
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = repository.login(username, password)
            if (user == null) {
                _state.value = _state.value.copy(error = "اسم المستخدم أو كلمة المرور غير صحيحة")
            } else {
                _state.value = _state.value.copy(user = user, error = null, message = "مرحبًا ${user.displayName}")
                observeUserData(user.id)
            }
        }
    }

    private fun observeUserData(userId: Long) {
        viewModelScope.launch {
            repository.stockForUser(userId).collectLatest { balances ->
                _state.value = _state.value.copy(stock = balances.associate { it.productId to it.quantity })
            }
        }
        viewModelScope.launch {
            repository.observeUserInvoices(userId).collectLatest { invoices ->
                _state.value = _state.value.copy(invoices = invoices)
            }
        }
        refreshStats(userId)
    }

    fun logout() {
        _state.value = _state.value.copy(user = null, stock = emptyMap(), invoices = emptyList(), stats = DashboardStats())
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, error = null)
    }

    fun createUser(username: String, password: String, name: String) {
        viewModelScope.launch {
            suspendRunCatching { repository.createUser(username, password, name) }
                .onSuccess { _state.value = _state.value.copy(message = "تم إنشاء حساب الكاشير والمخزون والصندوق") }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "تعذر إنشاء الحساب") }
        }
    }

    fun createProduct(
        name: String,
        barcode: String,
        unit: String,
        price: Double,
        caseName: String,
        caseQuantity: Int,
        casePrice: Double,
        stock: Int
    ) {
        viewModelScope.launch {
            suspendRunCatching {
                repository.createProduct(name, barcode, unit, price, caseName, caseQuantity, casePrice, stock)
            }.onSuccess {
                _state.value = _state.value.copy(message = "تمت إضافة المنتج")
            }.onFailure {
                _state.value = _state.value.copy(error = it.message ?: "تعذر إضافة المنتج")
            }
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            suspendRunCatching { repository.updateProduct(product) }
                .onSuccess { _state.value = _state.value.copy(message = "تم تعديل بيانات الصنف") }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "تعذر تعديل الصنف") }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
                .onSuccess { _state.value = _state.value.copy(message = "تم حذف الصنف") }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "لا يمكن حذف الصنف") }
        }
    }

    fun addCustomer(name: String, mobile: String) {
        viewModelScope.launch {
            suspendRunCatching { repository.addCustomer(name, mobile) }
                .onSuccess { _state.value = _state.value.copy(message = "تمت إضافة العميل") }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "تعذر إضافة العميل") }
        }
    }

    fun transferStock(cashierId: Long, productId: Long, quantity: Int) {
        val admin = _state.value.user ?: return
        viewModelScope.launch {
            repository.transferStock(admin.id, cashierId, productId, quantity)
                .onSuccess { _state.value = _state.value.copy(message = "تم تحويل المخزون إلى الكاشير") }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "تعذر تنفيذ التحويل") }
        }
    }

    fun sell(product: ProductEntity, unit: String, quantity: Int, customerId: Long?, credit: Boolean) {
        val user = _state.value.user ?: return
        viewModelScope.launch {
            repository.recordSale(user, product, unit, quantity, customerId, credit)
                .onSuccess {
                    val customer = _state.value.customers.firstOrNull { item -> item.id == it.invoice.customerId }
                    _state.value = _state.value.copy(
                        message = null,
                        saleReceipt = SaleReceipt(it.invoice, customer)
                    )
                    refreshStats(user.id)
                }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "تعذر حفظ الفاتورة") }
        }
    }

    fun bond(customerId: Long, type: String, amount: Double, note: String) {
        val user = _state.value.user ?: return
        viewModelScope.launch {
            repository.recordBond(user.id, customerId, type, amount, note)
                .onSuccess {
                    _state.value = _state.value.copy(
                        message = null,
                        bondReceipt = BondReceipt(it.bond, it.customer)
                    )
                }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "تعذر حفظ السند") }
        }
    }

    fun loadCustomerStatement(customerId: Long, onLoaded: (List<CustomerStatementRow>) -> Unit) {
        viewModelScope.launch {
            suspendRunCatching { repository.customerStatement(customerId) }
                .onSuccess(onLoaded)
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "تعذر إنشاء كشف الحساب") }
        }
    }

    fun clearSaleReceipt() {
        _state.value = _state.value.copy(saleReceipt = null)
    }

    fun clearBondReceipt() {
        _state.value = _state.value.copy(bondReceipt = null)
    }

    fun settle(cashierId: Long, actual: Double) {
        val admin = _state.value.user ?: return
        viewModelScope.launch {
            repository.settle(admin.id, cashierId, actual)
                .onSuccess { _state.value = _state.value.copy(message = "تمت التصفية وتسجيل الفرق") }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "تعذر تنفيذ التصفية") }
        }
    }

    private fun refreshStats(userId: Long) {
        viewModelScope.launch {
            val (sales, difference) = repository.dashboard(userId)
            _state.value = _state.value.copy(stats = DashboardStats(sales, difference))
        }
    }
}

private suspend inline fun <T> suspendRunCatching(
    crossinline block: suspend () -> T
): Result<T> = try {
    Result.success(block())
} catch (error: Throwable) {
    Result.failure(error)
}
