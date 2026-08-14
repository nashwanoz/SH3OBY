package com.khamrnet.app.data

import android.content.Context
import android.provider.Settings
import androidx.room.withTransaction
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.khamrnet.app.util.PasswordHasher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

data class SaleResult(
    val invoice: InvoiceEntity,
    val message: String
)

data class BondResult(
    val bond: FinancialBondEntity,
    val customer: CustomerEntity
)

class AppRepository(private val context: Context) {
    private val db = AppDatabase.get(context)
    private val preferences = context.getSharedPreferences("khamr_net_device", Context.MODE_PRIVATE)
    private val deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )?.takeIf { it.isNotBlank() } ?: "local-device"

    suspend fun initialize() {
        if (db.users().count() > 0) return
        db.withTransaction {
            val adminId = db.users().insert(
                UserEntity(
                    username = "المدير العام",
                    userCode = "1",
                    passwordHash = PasswordHasher.hash("1"),
                    displayName = "المدير العام",
                    role = "ADMIN"
                )
            )
            db.warehouses().insert(WarehouseEntity(1, "المستودع الرئيسي", isMain = true))
            db.cashBoxes().insert(CashBoxEntity(1, "الصندوق الرئيسي"))
            seedProduct("ماء معدني", "629000000001", 1.0, "كرت", 60, 10.0)
            seedProduct("عصير برتقال", "629000000002", 2.5, "كرت", 60, 50.0)
            seedProduct("بسكويت", "629000000003", 1.5, "كرت", 60, 25.0)
            seedProduct("مناديل ورقية", "629000000004", 3.0, "كرت", 60, 27.0)
            check(adminId > 0)
        }
    }

    private suspend fun seedProduct(
        name: String,
        barcode: String,
        price: Double,
        caseUnit: String,
        caseQuantity: Int,
        casePrice: Double
    ) {
        val id = db.products().insert(
            ProductEntity(
                name = name,
                barcode = barcode,
                price = price,
                caseUnitName = caseUnit,
                caseQuantity = caseQuantity,
                casePrice = casePrice
            )
        )
        db.stock().upsert(StockBalanceEntity(id, 1, 100))
    }

    suspend fun login(username: String, userCode: String, password: String): UserEntity? =
        db.users().login(username.trim(), userCode.trim(), PasswordHasher.hash(password))

    fun observeProducts(): Flow<List<ProductEntity>> = db.products().observeAll()
    fun observeCustomers(): Flow<List<CustomerEntity>> = db.customers().observeAll()
    fun observeUsers(): Flow<List<UserEntity>> = db.users().observeAll()
    fun observeUserInvoices(userId: Long): Flow<List<InvoiceEntity>> = db.invoices().observeForUser(userId)
    fun observeAllInvoices(): Flow<List<InvoiceEntity>> = db.invoices().observeAll()
    fun observeAllBonds(): Flow<List<FinancialBondEntity>> = db.bonds().observeAll()
    fun observeCashBoxes(): Flow<List<CashBoxEntity>> = db.cashBoxes().observeAll()

    suspend fun stockForUser(userId: Long): Flow<List<StockBalanceEntity>> {
        val warehouse = db.warehouses().forUser(userId) ?: return flowOf(emptyList())
        return db.stock().observeWarehouse(warehouse.id)
    }

    suspend fun stockForMain(): Flow<List<StockBalanceEntity>> {
        val warehouse = db.warehouses().main() ?: return flowOf(emptyList())
        return db.stock().observeWarehouse(warehouse.id)
    }

    suspend fun createUser(username: String, userCode: String, password: String, displayName: String): Long {
        require(username.trim().isNotEmpty()) { "أدخل اسم المستخدم" }
        require(userCode.trim().matches(Regex("\\d+"))) { "كود المستخدم يجب أن يكون رقميًا" }
        require(password.matches(Regex("\\d+"))) { "كلمة المرور يجب أن تكون رقمية" }
        return db.withTransaction {
            check(db.users().findByUserCode(userCode.trim()) == null) {
                "كود المستخدم مستخدم مسبقًا، اختر كودًا آخر"
            }
            val userId = db.users().insert(
                UserEntity(
                    username = username.trim(),
                    userCode = userCode.trim(),
                    passwordHash = PasswordHasher.hash(password),
                    displayName = displayName.trim(),
                    role = "CASHIER"
                )
            )
            db.warehouses().insert(WarehouseEntity(userId + 1_000_000, "مخزون $displayName", userId))
            db.cashBoxes().insert(CashBoxEntity(userId + 1_000_000, "صندوق $displayName", userId))
            userId
        }
    }

    suspend fun updateUser(
        id: Long,
        username: String,
        userCode: String,
        displayName: String,
        password: String?
    ) {
        require(username.trim().isNotEmpty()) { "أدخل اسم المستخدم" }
        require(userCode.trim().matches(Regex("\\d+"))) { "كود المستخدم يجب أن يكون رقميًا" }
        require(password.isNullOrEmpty() || password.matches(Regex("\\d+"))) {
            "كلمة المرور يجب أن تكون رقمية"
        }
        db.withTransaction {
            val other = db.users().findByUserCode(userCode.trim())
            check(other == null || other.id == id) { "كود المستخدم مستخدم مسبقًا، اختر كودًا آخر" }
            db.users().updateProfile(id, username.trim(), userCode.trim(), displayName.trim())
            if (!password.isNullOrEmpty()) {
                db.users().updatePassword(id, PasswordHasher.hash(password))
            }
        }
    }

    suspend fun createProduct(
        name: String,
        barcode: String,
        unitName: String,
        price: Double,
        caseName: String,
        caseQuantity: Int,
        casePrice: Double,
        initialStock: Int
    ) {
        db.withTransaction {
            val id = db.products().insert(
                ProductEntity(
                    name = name.trim(),
                    barcode = barcode.trim(),
                    unitName = unitName.trim(),
                    price = price,
                    caseUnitName = caseName.trim().ifBlank { "كرت" },
                    caseQuantity = caseQuantity.coerceAtLeast(1),
                    casePrice = casePrice
                )
            )
            db.stock().upsert(StockBalanceEntity(id, 1, initialStock))
        }
    }

    suspend fun updateProduct(product: ProductEntity) {
        db.products().update(
            id = product.id,
            name = product.name.trim(),
            barcode = product.barcode.trim(),
            unitName = product.unitName.trim(),
            price = product.price,
            caseUnitName = product.caseUnitName.trim().ifBlank { "كرت" },
            caseQuantity = product.caseQuantity.coerceAtLeast(1),
            casePrice = product.casePrice
        )
    }

    suspend fun deleteProduct(productId: Long): Result<Unit> = suspendRunCatching {
        db.withTransaction {
            check(!db.invoices().hasProductMovement(productId) && !db.transfers().hasProductMovement(productId)) {
                "لا يمكن حذف الصنف بعد تسجيل حركة عليه"
            }
            db.stock().deleteForProduct(productId)
            db.products().delete(productId)
        }
    }

    suspend fun addCustomer(name: String, mobile: String): Long =
        db.customers().insert(CustomerEntity(name = name.trim(), mobile = mobile.trim()))

    suspend fun customerLastMovements(): Map<Long, Long> {
        val movements = db.invoices().customerMovementTimes() + db.bonds().customerMovementTimes()
        return movements
            .filter { it.lastMovementAt != null }
            .groupBy { it.customerId }
            .mapValues { (_, values) -> values.maxOf { it.lastMovementAt!! } }
    }

    suspend fun customerStatement(customerId: Long): List<CustomerStatementRow> {
        val invoices = db.invoices().forCustomer(customerId)
        val bonds = db.bonds().forCustomer(customerId)
        val movements = (
            invoices.map {
                CustomerStatementRow(
                    createdAt = it.createdAt,
                    reference = it.id,
                    type = "فاتورة ${it.paymentType}",
                    amount = it.total,
                    balanceAfter = 0.0
                )
            } + bonds.map {
                CustomerStatementRow(
                    createdAt = it.createdAt,
                    reference = it.id,
                    type = if (it.type == "قبض") "سند قبض" else "سند صرف",
                    amount = if (it.type == "قبض") -it.amount else it.amount,
                    balanceAfter = 0.0
                )
            }
        ).sortedBy { it.createdAt }

        var balance = 0.0
        return movements.map { movement ->
            balance += movement.amount
            movement.copy(balanceAfter = balance)
        }
    }

    suspend fun transferStock(adminId: Long, cashierId: Long, productId: Long, quantity: Int): Result<Unit> {
        if (quantity <= 0) return Result.failure(IllegalArgumentException("أدخل كمية صحيحة"))
        return suspendRunCatching {
            db.withTransaction {
                check(db.users().find(adminId)?.role == "ADMIN") { "هذه العملية متاحة للمدير فقط" }
                val main = db.warehouses().main() ?: error("المستودع الرئيسي غير موجود")
                val target = db.warehouses().forUser(cashierId) ?: error("لا يوجد مخزون فرعي لهذا المستخدم")
                check(db.stock().subtract(productId, main.id, quantity) == 1) { "الكمية غير متوفرة في المستودع الرئيسي" }
                val current = db.stock().find(productId, target.id)
                if (current == null) db.stock().upsert(StockBalanceEntity(productId, target.id, quantity))
                else db.stock().add(productId, target.id, quantity)
                db.transfers().insert(
                    StockTransferEntity(
                        id = newId("transfer", adminId),
                        userId = cashierId,
                        productId = productId,
                        fromWarehouseId = main.id,
                        toWarehouseId = target.id,
                        quantity = quantity
                    )
                )
            }
        }
    }

    suspend fun recordSale(
        user: UserEntity,
        product: ProductEntity,
        unitName: String,
        quantity: Int,
        customerId: Long?,
        credit: Boolean
    ): Result<SaleResult> {
        if (quantity <= 0) return Result.failure(IllegalArgumentException("الكمية يجب أن تكون أكبر من صفر"))
        return suspendRunCatching {
            db.withTransaction {
                val warehouse = db.warehouses().forUser(user.id)
                    ?: error("لا يوجد مخزون فرعي مرتبط بهذا المستخدم")
                val multiplier = if (unitName == product.caseUnitName) product.caseQuantity else 1
                val totalUnits = quantity * multiplier
                check(db.stock().subtract(product.id, warehouse.id, totalUnits) == 1) {
                    "الكمية المطلوبة غير متوفرة في مخزونك"
                }
                if (credit && customerId == null) error("اختر عميلًا للبيع الآجل")
                val price = if (unitName == product.caseUnitName && product.casePrice > 0) product.casePrice else product.price
                val total = price * quantity
                val customer = customerId?.let { db.customers().find(it) }
                val previous = customer?.balance ?: 0.0
                val newBalance = if (credit) previous + total else previous
                if (credit) db.customers().adjustBalance(customerId!!, total)
                if (!credit) {
                    val box = db.cashBoxes().forUser(user.id) ?: error("صندوق المستخدم غير موجود")
                    db.cashBoxes().adjust(box.id, total)
                }
                val invoice = InvoiceEntity(
                    id = nextInvoiceId(user.id),
                    userId = user.id,
                    customerId = customerId,
                    paymentType = if (credit) "آجل" else "نقدي",
                    total = total,
                    previousBalance = previous,
                    newBalance = newBalance
                )
                db.invoices().insert(invoice)
                db.invoices().insertLine(
                    InvoiceLineEntity(
                        invoiceId = invoice.id,
                        productId = product.id,
                        productName = product.name,
                        unitName = unitName,
                        quantity = quantity,
                        unitPrice = price,
                        lineTotal = total
                    )
                )
                SaleResult(invoice, "تم حفظ الفاتورة ${invoice.id}")
            }
        }
    }

    suspend fun recordBond(
        userId: Long,
        customerId: Long,
        type: String,
        amount: Double,
        note: String
    ): Result<BondResult> = suspendRunCatching {
        require(amount > 0) { "أدخل مبلغًا صحيحًا" }
        db.withTransaction {
            val signedAmount = if (type == "قبض") -amount else amount
            db.customers().adjustBalance(customerId, signedAmount)
            val bond = FinancialBondEntity(
                nextDocumentId(userId, if (type == "قبض") 3 else 1),
                userId,
                customerId,
                type,
                amount,
                note
            )
            db.bonds().insert(bond)
            BondResult(
                bond = bond,
                customer = db.customers().find(customerId) ?: error("العميل غير موجود")
            )
        }
    }

    suspend fun settle(adminId: Long, cashierId: Long, actual: Double): Result<Unit> = suspendRunCatching {
        require(actual >= 0) { "المبلغ غير صحيح" }
        db.withTransaction {
            check(db.users().find(adminId)?.role == "ADMIN") { "هذه العملية متاحة للمدير فقط" }
            val box = db.cashBoxes().forUser(cashierId) ?: error("صندوق الكاشير غير موجود")
            val systemAmount = db.cashBoxes().forUser(cashierId)?.balance ?: 0.0
            val difference = systemAmount - actual
            db.cashBoxes().setBalance(box.id, actual)
            db.settlements().insert(
                SettlementEntity(
                    newId("settlement", adminId),
                    adminId,
                    cashierId,
                    systemAmount,
                    actual,
                    difference
                )
            )
        }
    }

    suspend fun dashboard(userId: Long): Pair<Double, Double> {
        val start = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return db.invoices().totalSince(userId, start) to db.settlements().carriedDifference(userId)
    }

    suspend fun testFirebaseConnection(): Result<Unit> = withContext(Dispatchers.IO) {
        suspendRunCatching {
            val app = FirebaseApp.getApps(context).firstOrNull()
                ?: FirebaseApp.initializeApp(context)
                ?: error("ملف google-services.json غير موجود أو لم تتم تهيئة Firebase")
            val testRef = FirebaseDatabase.getInstance(app)
                .reference
                .child("connection_tests")
                .child(deviceId)
            Tasks.await(
                testRef.setValue(
                    mapOf(
                        "ok" to true,
                        "testedAt" to System.currentTimeMillis()
                    )
                )
            )
            Unit
        }
    }

    suspend fun syncPending(): Boolean = withContext(Dispatchers.IO) {
        val app = try {
            if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context)
            FirebaseApp.getInstance()
        } catch (_: Exception) {
            return@withContext false
        }
        suspendRunCatching {
            val root = FirebaseDatabase.getInstance(app).reference
            db.invoices().pending().forEach { invoice ->
                val payload = mapOf(
                    "id" to invoice.id,
                    "userId" to invoice.userId,
                    "customerId" to invoice.customerId,
                    "paymentType" to invoice.paymentType,
                    "total" to invoice.total,
                    "previousBalance" to invoice.previousBalance,
                    "newBalance" to invoice.newBalance,
                    "createdAt" to invoice.createdAt,
                    "lines" to db.invoices().lines(invoice.id).map { it.toMap() }
                )
                Tasks.await(root.child("invoices").child(invoice.id).setValue(payload))
                db.invoices().markPosted(invoice.id)
            }
            db.transfers().pending().forEach {
                Tasks.await(root.child("stock_transfers").child(it.id).setValue(it.toMap()))
                db.transfers().markPosted(it.id)
            }
            db.bonds().pending().forEach {
                Tasks.await(root.child("financial_bonds").child(it.id).setValue(it.toMap()))
                db.bonds().markPosted(it.id)
            }
            db.settlements().pending().forEach {
                Tasks.await(root.child("settlements").child(it.id).setValue(it.toMap()))
                db.settlements().markPosted(it.id)
            }
            true
        }.getOrDefault(false)
    }

    private suspend fun nextInvoiceId(userId: Long): String = nextDocumentId(userId, 4)

    private suspend fun nextDocumentId(userId: Long, documentType: Int): String {
        val user = db.users().find(userId) ?: error("المستخدم غير موجود")
        val year = SimpleDateFormat("yy", Locale.US).format(Date())
        val prefix = "$year${user.userCode}$documentType"
        val key = "document_sequence_${year}_${user.userCode}_$documentType"
        val sequence = preferences.getLong(key, 0L) + 1
        preferences.edit().putLong(key, sequence).apply()
        return "$prefix${sequence.toString().padStart(4, '0')}"
    }

    private fun newId(prefix: String, userId: Long): String =
        "$prefix-$deviceId-$userId-${UUID.randomUUID()}"
}

private suspend inline fun <T> suspendRunCatching(
    crossinline block: suspend () -> T
): Result<T> = try {
    Result.success(block())
} catch (error: Throwable) {
    Result.failure(error)
}

private fun InvoiceLineEntity.toMap() = mapOf(
    "id" to id,
    "invoiceId" to invoiceId,
    "productId" to productId,
    "productName" to productName,
    "unitName" to unitName,
    "quantity" to quantity,
    "unitPrice" to unitPrice,
    "lineTotal" to lineTotal
)

private fun StockTransferEntity.toMap() = mapOf(
    "id" to id,
    "userId" to userId,
    "productId" to productId,
    "fromWarehouseId" to fromWarehouseId,
    "toWarehouseId" to toWarehouseId,
    "quantity" to quantity,
    "createdAt" to createdAt
)

private fun FinancialBondEntity.toMap() = mapOf(
    "id" to id,
    "userId" to userId,
    "customerId" to customerId,
    "type" to type,
    "amount" to amount,
    "note" to note,
    "createdAt" to createdAt
)

private fun SettlementEntity.toMap() = mapOf(
    "id" to id,
    "adminUserId" to adminUserId,
    "cashierUserId" to cashierUserId,
    "systemAmount" to systemAmount,
    "actualAmount" to actualAmount,
    "difference" to difference,
    "createdAt" to createdAt
)