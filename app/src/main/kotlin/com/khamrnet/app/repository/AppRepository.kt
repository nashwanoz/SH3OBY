package com.khamrnet.app.repository

import androidx.room.withTransaction
import com.khamrnet.app.data.db.KhamrDatabase
import com.khamrnet.app.data.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * Comprehensive repository with transactional logic:
 * - insertUser creates sub-warehouse and cashbox for user
 * - createInvoice verifies and deducts stock from user's warehouse and updates cashbox balance
 * - transferStockToUser used by admin to distribute stock from central warehouse
 *
 * All operations are suspending and run on IO dispatcher.
 */
class AppRepository(private val db: KhamrDatabase) {

    private val userDao = db.userDao()
    private val warehouseDao = db.warehouseDao()
    private val cashBoxDao = db.cashBoxDao()
    private val productDao = db.productDao()
    private val productUnitDao = db.productUnitDao()
    private val stockAllocationDao = db.stockAllocationDao()
    private val customerDao = db.customerDao()
    private val invoiceDao = db.invoiceDao()
    private val invoiceLineDao = db.invoiceLineDao()
    private val bondDao = db.bondDao()
    private val settlementDao = db.settlementDao()

    // Users
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) { userDao.getAll() }
    suspend fun getAllUsersImmediate(): List<User> = getAllUsers()
    suspend fun login(username: String, password: String): User? = withContext(Dispatchers.IO) {
        userDao.login(username, password)
    }

    suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO) {
        // Use a DB transaction to create user + his warehouse + cashbox atomically
        db.withTransaction {
            val userId = userDao.insert(user)
            val whName = "صندوق مخزون $userId"
            val cbName = "صندوق مبيعات $userId"
            warehouseDao.insert(Warehouse(name = whName, ownerUserId = userId))
            cashBoxDao.insert(CashBox(name = cbName, ownerUserId = userId, balance = 0.0))
            userId
        }
    }

    // For App startup synchronous call (same implementation)
    suspend fun insertUserImmediate(user: User): Long = insertUser(user)

    // Products & units
    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) { productDao.getAll() }
    suspend fun getProductById(id: Long): Product? = withContext(Dispatchers.IO) { productDao.findById(id) }
    suspend fun getUnitsForProduct(productId: Long): List<ProductUnit> = withContext(Dispatchers.IO) { productUnitDao.findByProduct(productId) }

    suspend fun seedDefaultProductsIfEmpty() = withContext(Dispatchers.IO) {
        val existing = productDao.getAll()
        if (existing.isEmpty()) {
            val p1Id = productDao.insert(Product(name = "منتج ١", price = 10.0))
            val p2Id = productDao.insert(Product(name = "منتج ٢", price = 20.0))
            val p3Id = productDao.insert(Product(name = "منتج ٣", price = 30.0))
            val p4Id = productDao.insert(Product(name = "منتج ٤", price = 40.0))
            val p5Id = productDao.insert(Product(name = "منتج ٥", price = 50.0))
            // Insert units: base حبة (1) and كرتون (12)
            listOf(p1Id, p2Id, p3Id, p4Id, p5Id).forEach { pid ->
                productUnitDao.insert(ProductUnit(productId = pid, name = "حبة", multiplier = 1.0))
                productUnitDao.insert(ProductUnit(productId = pid, name = "كرتون", multiplier = 12.0))
            }
            // Create a central warehouse if none exists
            val central = warehouseDao.getAll()
            if (central.isEmpty()) {
                warehouseDao.insert(Warehouse(name = "المخزن المركزي", ownerUserId = null))
            }
        }
    }

    // Stock transfer: Admin moves stock from central warehouse to user sub-warehouse.
    // For simplicity central warehouse is any warehouse with ownerUserId == null (the first one).
    suspend fun transferStockToUser(productId: Long, qty: Double, toUserId: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            // find or create toUser's warehouse
            val targetWh = warehouseDao.findByOwner(toUserId)
                ?: throw IllegalStateException("لا يوجد صندوق مخزون للمستخدم الهدف")

            // find or create allocation in that warehouse
            val existing = stockAllocationDao.find(targetWh.id, productId)
            if (existing != null) {
                stockAllocationDao.updateQuantity(existing.id, existing.quantity + qty)
            } else {
                stockAllocationDao.insert(StockAllocation(productId = productId, warehouseId = targetWh.id, quantity = qty, posted = 0))
            }
        }
    }

    // Get user's warehouse id
    suspend fun getUserWarehouseId(userId: Long): Long? = withContext(Dispatchers.IO) {
        warehouseDao.findByOwner(userId)?.id
    }

    // Customers
    suspend fun addCustomer(c: Customer): Long = withContext(Dispatchers.IO) { customerDao.insert(c) }
    suspend fun getAllCustomers(): List<Customer> = withContext(Dispatchers.IO) { customerDao.getAll() }
    suspend fun updateCustomerBalance(id: Long, newBalance: Double) = withContext(Dispatchers.IO) {
        customerDao.updateBalance(id, newBalance)
    }
    suspend fun findCustomerById(id: Long): Customer? = withContext(Dispatchers.IO) { customerDao.findById(id) }

    // Invoice creation (transactional)
    // - Ensure cashier has allocated stock in his sub-warehouse
    // - Deduct allocation quantities
    // - Create invoice & lines
    // - Increase cashier cashbox balance (immediate, physical cash handling/settlement separate)
    suspend fun createInvoice(cashierId: Long, customerId: Long?, lines: List<InvoiceLine>): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            val wh = warehouseDao.findByOwner(cashierId) ?: throw IllegalStateException("لا يوجد صندوق مخزون مرتبط بهذا الكاشير")
            // verify stock for each line
            for (line in lines) {
                val alloc = stockAllocationDao.find(wh.id, line.productId)
                val needed = line.quantity
                if (alloc == null || alloc.quantity < needed) {
                    throw IllegalStateException("كمية غير كافية للمنتج ${line.productId} في صندوق المخزون الخاص بالمستخدم")
                }
            }
            // Deduct stock
            for (line in lines) {
                val alloc = stockAllocationDao.find(wh.id, line.productId)!!
                val newQty = max(0.0, alloc.quantity - line.quantity)
                stockAllocationDao.updateQuantity(alloc.id, newQty)
            }
            // compute total sum of lines
            val total = lines.sumOf { it.lineTotal }
            val invoiceId = invoiceDao.insert(Invoice(cashierId = cashierId, customerId = customerId, total = total, posted = 0))
            // insert lines with invoiceId
            for (line in lines) {
                invoiceLineDao.insert(line.copy(invoiceId = invoiceId, posted = 0))
            }
            // update cashier cashbox balance (add total)
            val cashBox = cashBoxDao.findByOwner(cashierId)
            if (cashBox != null) {
                val newBal = cashBox.balance + total
                cashBoxDao.updateBalance(cashBox.id, newBal)
            } else {
                // If somehow no cashbox, create one and set balance
                val cbId = cashBoxDao.insert(CashBox(name = "صندوق مبيعات $cashierId", ownerUserId = cashierId, balance = total))
                // nothing else
            }
            // Update customer balance (if any credit logic required: here we increase customer's balance by total)
            if (customerId != null) {
                val cust = customerDao.findById(customerId)
                if (cust != null) {
                    customerDao.updateBalance(customerId, cust.balance + total)
                }
            }
            invoiceId
        }
    }

    // Bonds (قبض/صرف)
    suspend fun createBond(bond: Bond): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            val id = bondDao.insert(bond.copy(posted = 0))
            // update cashbox if type is receipt (قبض) increases cashbox; if صرف decreases
            if (bond.userId != null) {
                val cb = cashBoxDao.findByOwner(bond.userId)
                if (cb != null) {
                    val updated = if (bond.type == "قبض") cb.balance + bond.amount else cb.balance - bond.amount
                    cashBoxDao.updateBalance(cb.id, updated)
                }
            }
            id
        }
    }

    // Settlement (تصفية الكاشير)
    suspend fun createSettlement(settlement: Settlement): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            // create settlement record (posted = 0)
            val id = settlementDao.insert(settlement.copy(posted = 0))
            // record difference as liability if negative (عهدة/عجز) - here we don't automatically create a bond, but that can be added
            id
        }
    }

    // Sync worker helpers - read unposted rows
    suspend fun getUnpostedInvoices(): List<Invoice> = withContext(Dispatchers.IO) { invoiceDao.findUnposted() }
    suspend fun getUnpostedInvoiceLines(): List<InvoiceLine> = withContext(Dispatchers.IO) { invoiceLineDao.findUnposted() }
    suspend fun getUnpostedBonds(): List<Bond> = withContext(Dispatchers.IO) { bondDao.findUnposted() }
    suspend fun getUnpostedSettlements(): List<Settlement> = withContext(Dispatchers.IO) { settlementDao.findUnposted() }
    suspend fun getUnpostedStockAllocations(): List<StockAllocation> = withContext(Dispatchers.IO) { stockAllocationDao.findUnposted() }

    // Mark posted
    suspend fun markInvoicePosted(id: Long) = withContext(Dispatchers.IO) { invoiceDao.markPosted(id) }
    suspend fun markInvoiceLinePosted(id: Long) = withContext(Dispatchers.IO) { invoiceLineDao.markPosted(id) }
    suspend fun markBondPosted(id: Long) = withContext(Dispatchers.IO) { bondDao.markPosted(id) }
    suspend fun markSettlementPosted(id: Long) = withContext(Dispatchers.IO) { settlementDao.markPosted(id) }
    suspend fun markStockAllocationPosted(id: Long) = withContext(Dispatchers.IO) { stockAllocationDao.markPosted(id) }
}
