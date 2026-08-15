package com.khamrnet.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khamrnet.app.data.AppRepository
import com.khamrnet.app.data.CustomerStatementRow
import com.khamrnet.app.data.CustomerEntity
import com.khamrnet.app.data.FinancialBondEntity
import com.khamrnet.app.data.InvoiceLineEntity
import com.khamrnet.app.data.ProductEntity
import com.khamrnet.app.data.UserEntity
import com.khamrnet.app.data.InvoiceEntity
import com.khamrnet.app.data.SaleLineInput
import com.khamrnet.app.data.UserPermissions
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
    val customer: CustomerEntity?,
    val lines: List<InvoiceLineEntity>
)

data class BondReceipt(
    val bond: FinancialBondEntity,
    val customer: CustomerEntity
)

data class AppUiState(
    val ready: Boolean = false,
    val user: UserEntity? = null,
    val userCodeInput: String = "",
    val passwordInput: String = "",
    val products: List<ProductEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val users: List<UserEntity> = emptyList(),
    val customerLastMovement: Map<Long, Long> = emptyMap(),
    val stock: Map<Long, Int> = emptyMap(),
    val invoices: List<InvoiceEntity> = emptyList(),
    val bonds: List<FinancialBondEntity> = emptyList(),
    val cashBalances: Map<Long, Double> = emptyMap(),
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
            try {
                repository.initialize()
                SyncWorker.schedule(getApplication())
            } catch (error: Throwable) {
                _state.value = _state.value.copy(
                    error = error.message ?: "تعذر تجهيز قاعدة البيانات المحلية"
                )
            } finally {
                _state.value = _state.value.copy(ready = true)
            }
        }
        viewModelScope.launch {
            repository.observeProducts().collectLatest { products ->
                _state.value = _state.value.copy(products = products)
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
        viewModelScope.launch {
            repository.observeAllBonds().collectLatest { bonds ->
                _state.value = _state.value.copy(bonds = bonds)
            }
        }
        viewModelScope.launch {
            repository.observeCashBoxes().collectLatest { boxes ->
                _state.value = _state.value.copy(
                    cashBalances = boxes.filter { it.ownerUserId != null }
                        .associate { it.ownerUserId!! to it.balance }
                )
            }
        }
    }

    fun login(userCode: String, password: String) {
        viewModelScope.launch {
            val user = repository.login(userCode, password)
            if (user == null) {
                _state.value = _state.value.copy(error = "رقم المستخدم أو كلمة المرور غير صحيحة")
            } else {
                _state.value = _state.value.copy(user = user, error = null, message = "مرحبًا ${user.displayName}")
                observeUserData(user.id)
            }
        }
    }

    private fun observeUserData(userId: Long) {
        viewModelScope.launch {
            val stockFlow = if (_state.value.user?.role == "ADMIN") {
                repository.stockForMain()
            } else {
                repository.stockForUser(userId)
            }
            stockFlow.collectLatest { balances ->
                _state.value = _state.value.copy(stock = balances.associate { it.productId to it.quantity })
            }
        }
        viewModelScope.launch {
            val current = _state.value.user
            val invoiceFlow = if (current?.role == "ADMIN") {
                repository.observeAllInvoices()
            } else {
                repository.observeUserInvoices(userId)
            }
            invoiceFlow.collectLatest { invoices ->
                _state.value = _state.value.copy(invoices = invoices)
            }
        }
        refreshStats(userId)
    }

    fun logout() {
        _state.value = _state.value.copy(user = null, stock = emptyMap(), invoices = emptyList(), stats = DashboardStats())
    }

    fun testFirebaseConnection() {
        viewModelScope.launch {
            repository.testFirebaseConnection()
                .onSuccess {
                    _state.value = _state.value.copy(
                        message = "اتصال Firebase يعمل وتم حفظ اختبار الاتصال بنجاح"
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        error = "فشل اختبار Firebase: ${it.message ?: "تحقق من إعداد Firebase وقواعد قاعدة البيانات"}"
                    )
                }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, error = null)
    }

    fun createUser(name: String, userCode: String, password: String, permissions: UserPermissions) {
        viewModelScope.launch {
            suspendRunCatching { repository.createUser(name, userCode, password, permissions) }
                .onSuccess { _state.value = _state.value.copy(message = "تم إنشاء حساب الكاشير والمخزون والصندوق") }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "تعذر إنشاء الحساب") }
        }
    }

    fun updateUser(
        user: UserEntity,
        name: String,
        userCode: String,
        password: String,
        permissions: UserPermissions
    ) {
        viewModelScope.launch {
            suspendRunCatching {
                repository.updateUser(user.id, name, userCode, password.ifBlank { null }, permissions)
            }.onSuccess {
                _state.value = _state.value.copy(message = "تم تعديل بيانات المستخدم")
            }.onFailure {
                _state.value = _state.value.copy(error = it.message ?: "تعذر تعديل المستخدم")
            }
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

    fun addCustomer(name: String, customerCode: String, mobile: String) {
        viewModelScope.launch {
            suspendRunCatching { repository.addCustomer(name, customerCode, mobile) }
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

    fun sell(lines: List<SaleLineInput>, customerId: Long?, credit: Boolean) {
        val user = _state.value.user ?: return
        viewModelScope.launch {
            repository.recordSale(user, lines, customerId, credit)
                .onSuccess {
                    val customer = _state.value.customers.firstOrNull { item -> item.id == it.invoice.customerId }
                    _state.value = _state.value.copy(
                        message = null,
                        saleReceipt = SaleReceipt(it.invoice, customer, it.lines)
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
