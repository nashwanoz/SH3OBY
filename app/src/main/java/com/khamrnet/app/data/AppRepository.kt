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
    val lines: List<InvoiceLineEntity>,
    val message: String
)

data class BondResult(
    val bond: FinancialBondEntity,
    val customer: CustomerEntity
)

data class SaleLineInput(
    val productId: Long,
    val unitName: String,
    val quantity: Int
)

data class UserPermissions(
    val canHome: Boolean = true,
    val canPos: Boolean = true,
    val canInvoices: Boolean = true,
    val canReports: Boolean = true,
    val canProducts: Boolean = false,
    val canUsers: Boolean = false,
    val canTransfers: Boolean = false,
    val canCustomers: Boolean = true,
    val canBonds: Boolean = true,
    val canSettlements: Boolean = false,
    val canWhatsapp: Boolean = true
)

private data class PreparedSaleLine(
    val input: SaleLineInput,
    val product: ProductEntity,
    val unitPrice: Double,
    val lineTotal: Double,
    val totalUnits: Int
)

class AppRepository(private val context: Context) {
    private val db = AppDatabase.get(context)
    private val preferences = context.getSharedPreferences("khamr_net_device", Context.MODE_PRIVATE)
    private val deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )?.takeIf { it.isNotBlank() } ?: "local-device"

    suspend fun initialize() {
        if (db.users().count() > 0) {
            seedMissingProducts()
            return
        }
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
            seedProduct("كرت ابو 100", "629000000001", 90, "كرت", 60, 5400)
            seedProduct("كرت ابو 200", "629000000002", 180, "كرت", 60, 10800)
            seedProduct("كرت ابو 250", "629000000003", 225, "كرت", 60, 13500)
            seedProduct("كرت ابو 300", "629000000004", 270, "كرت", 60, 16200)
            seedProduct("كرت ابو 500", "629000000005", 450, "كرت", 60, 27000)
            seedProduct("كرت قريبا", "629000000006", 4.0, "كرت", 12, 42.0)
            check(adminId > 0)
        }
    }

    private suspend fun seedMissingProducts() {
        db.withTransaction {
            if (db.products().findByBarcode("629000000005") == null) {
                seedProduct("كرت ابو 500", "629000000005", 1.5, "كرت", 24, 30.0)
            }
            if (db.products().findByBarcode("629000000006") == null) {
                seedProduct("كرت قريبا", "629000000006", 4.0, "كرت", 12, 42.0)
            }
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

    suspend fun login(userCode: String, password: String): UserEntity? =
        db.users().login(userCode.trim(), PasswordHasher.hash(password))

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

    suspend fun createUser(displayName: String, userCode: String, password: String, permissions: UserPermissions): Long {
        require(displayName.trim().isNotEmpty()) { "أدخل اسم المستخدم الظاهر" }
        require(userCode.trim().matches(Regex("\\d+"))) { "كود المستخدم يجب أن يكون رقميًا" }
        require(password.matches(Regex("\\d+"))) { "كلمة المرور يجب أن تكون رقمية" }
        return db.withTransaction {
            check(db.users().findByUserCode(userCode.trim()) == null) {
                "كود المستخدم مستخدم مسبقًا، اختر كودًا آخر"
            }
            val userId = db.users().insert(
                UserEntity(
                    username = displayName.trim(),
                    userCode = userCode.trim(),
                    passwordHash = PasswordHasher.hash(password),
                    displayName = displayName.trim(),
                    role = "CASHIER",
                    canHome = permissions.canHome,
                    canPos = permissions.canPos,
                    canInvoices = permissions.canInvoices,
                    canReports = permissions.canReports,
                    canProducts = permissions.canProducts,
                    canUsers = permissions.canUsers,
                    canTransfers = permissions.canTransfers,
                    canCustomers = permissions.canCustomers,
                    canBonds = permissions.canBonds,
                    canSettlements = permissions.canSettlements,
                    canWhatsapp = permissions.canWhatsapp
                )
            )
            db.warehouses().insert(WarehouseEntity(userId + 1_000_000, "مخزون ${displayName.trim()}", userId))
            db.cashBoxes().insert(CashBoxEntity(userId + 1_000_000, "صندوق ${displayName.trim()}", userId))
            userId
        }
    }

    suspend fun updateUser(
        id: Long,
        displayName: String,
        userCode: String,
        password: String?,
        permissions: UserPermissions
    ) {
        require(displayName.trim().isNotEmpty()) { "أدخل اسم المستخدم الظاهر" }
        require(userCode.trim().matches(Regex("\\d+"))) { "كود المستخدم يجب أن يكون رقميًا" }
        require(password.isNullOrEmpty() || password.matches(Regex("\\d+"))) {
            "كلمة المرور يجب أن تكون رقمية"
        }
        db.withTransaction {
            val other = db.users().findByUserCode(userCode.trim())
            check(other == null || other.id == id) { "كود المستخدم مستخدم مسبقًا، اختر كودًا آخر" }
            db.users().updateProfile(
                id = id,
                username = displayName.trim(),
                userCode = userCode.trim(),
                displayName = displayName.trim(),
                canHome = permissions.canHome,
                canPos = permissions.canPos,
                canInvoices = permissions.canInvoices,
                canReports = permissions.canReports,
                canProducts = permissions.canProducts,
                canUsers = permissions.canUsers,
                canTransfers = permissions.canTransfers,
                canCustomers = permissions.canCustomers,
                canBonds = permissions.canBonds,
                canSettlements = permissions.canSettlements,
                canWhatsapp = permissions.canWhatsapp
            )
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

    suspend fun addCustomer(name: String, customerCode: String, mobile: String): Long {
        require(name.trim().isNotEmpty()) { "أدخل اسم العميل" }
        require(customerCode.trim().isNotEmpty()) { "أدخل كود العميل" }
        return db.withTransaction {
            check(db.customers().findByCode(customerCode.trim()) == null) {
                "كود العميل مستخدم مسبقًا، اختر كودًا آخر"
            }
            db.customers().insert(
                CustomerEntity(
                    name = name.trim(),
                    customerCode = customerCode.trim(),
                    mobile = mobile.trim()
                )
            )
        }
    }

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
        lines: List<SaleLineInput>,
        customerId: Long?,
        credit: Boolean
    ): Result<SaleResult> {
        if (lines.isEmpty()) return Result.failure(IllegalArgumentException("أضف صنفًا واحدًا على الأقل"))
        return suspendRunCatching {
            db.withTransaction {
                val warehouse = if (user.role == "ADMIN") {
                    db.warehouses().main()
                } else {
                    db.warehouses().forUser(user.id)
                } ?: error("لا يوجد مخزون مرتبط بهذا المستخدم")
                val preparedLines = lines.map { line ->
                    check(line.quantity > 0) { "الكمية يجب أن تكون أكبر من صفر" }
                    val product = db.products().find(line.productId) ?: error("الصنف غير موجود")
                    val multiplier = if (line.unitName == product.caseUnitName) product.caseQuantity else 1
                    val unitPrice = if (line.unitName == product.caseUnitName && product.casePrice > 0) {
                        product.casePrice
                    } else {
                        product.price
                    }
                    PreparedSaleLine(
                        input = line,
                        product = product,
                        unitPrice = unitPrice,
                        lineTotal = unitPrice * line.quantity,
                        totalUnits = line.quantity * multiplier
                    )
                }
                preparedLines.forEach { line ->
                    check(db.stock().subtract(line.product.id, warehouse.id, line.totalUnits) == 1) {
                        "الكمية المطلوبة غير متوفرة للصنف: ${line.product.name}"
                    }
                }
                if (credit && customerId == null) error("اختر عميلًا للبيع الآجل")
                val total = preparedLines.sumOf { it.lineTotal }
                val effectiveCustomerId = if (credit) customerId else null
                val customer = effectiveCustomerId?.let { db.customers().find(it) }
                if (credit) check(customer != null) { "العميل غير موجود" }
                val previous = customer?.balance ?: 0.0
                val newBalance = if (credit) previous + total else previous
                if (credit) db.customers().adjustBalance(customerId!!, total)
                val box = if (user.role == "ADMIN") db.cashBoxes().main() else db.cashBoxes().forUser(user.id)
                check(box != null) { "صندوق المستخدم غير موجود" }
                db.cashBoxes().adjust(box.id, total)
                val invoice = InvoiceEntity(
                    id = nextInvoiceId(user.id),
                    userId = user.id,
                    customerId = effectiveCustomerId,
                    paymentType = if (credit) "آجل" else "نقدي",
                    total = total,
                    previousBalance = previous,
                    newBalance = newBalance
                )
                db.invoices().insert(invoice)
                val invoiceLines = preparedLines.map { line ->
                    InvoiceLineEntity(
                        invoiceId = invoice.id,
                        productId = line.product.id,
                        productName = line.product.name,
                        unitName = line.input.unitName,
                        quantity = line.input.quantity,
                        unitPrice = line.unitPrice,
                        lineTotal = line.lineTotal
                    ).also { db.invoices().insertLine(it) }
                }
                SaleResult(invoice, invoiceLines, "تم حفظ الفاتورة ${invoice.id}")
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
            val customer = db.customers().find(customerId) ?: error("العميل غير موجود")
            val previousBalance = customer.balance
            val signedAmount = if (type == "قبض") -amount else amount
            val newBalance = previousBalance + signedAmount
            db.customers().adjustBalance(customerId, signedAmount)
            val box = db.cashBoxes().forUser(userId) ?: db.cashBoxes().main()
            check(box != null) { "صندوق المستخدم غير موجود" }
            db.cashBoxes().adjust(box.id, if (type == "قبض") amount else -amount)
            val bond = FinancialBondEntity(
                id = nextDocumentId(userId, if (type == "قبض") 3 else 1),
                userId = userId,
                customerId = customerId,
                type = type,
                amount = amount,
                previousBalance = previousBalance,
                newBalance = newBalance,
                note = note
            )
            db.bonds().insert(bond)
            BondResult(
                bond = bond,
                customer = customer.copy(balance = newBalance)
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
        return "$prefix${sequence.toString().padStart(5, '0')}"
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
    "previousBalance" to previousBalance,
    "newBalance" to newBalance,
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
