package com.khamrnet.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash AND active = 1 LIMIT 1")
    suspend fun login(username: String, passwordHash: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun find(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY id")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert
    suspend fun insert(user: UserEntity): Long
}

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses WHERE ownerUserId = :userId LIMIT 1")
    suspend fun forUser(userId: Long): WarehouseEntity?

    @Query("SELECT * FROM warehouses WHERE isMain = 1 LIMIT 1")
    suspend fun main(): WarehouseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warehouse: WarehouseEntity)
}

@Dao
interface CashBoxDao {
    @Query("SELECT * FROM cash_boxes WHERE ownerUserId = :userId LIMIT 1")
    suspend fun forUser(userId: Long): CashBoxEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(box: CashBoxEntity)

    @Query("UPDATE cash_boxes SET balance = balance + :amount WHERE id = :boxId")
    suspend fun adjust(boxId: Long, amount: Double)

    @Query("UPDATE cash_boxes SET balance = :amount WHERE id = :boxId")
    suspend fun setBalance(boxId: Long, amount: Double)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE active = 1 ORDER BY name")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun find(id: Long): ProductEntity?

    @Insert
    suspend fun insert(product: ProductEntity): Long
}

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_balances WHERE warehouseId = :warehouseId ORDER BY productId")
    fun observeWarehouse(warehouseId: Long): Flow<List<StockBalanceEntity>>

    @Query("SELECT * FROM stock_balances WHERE productId = :productId AND warehouseId = :warehouseId LIMIT 1")
    suspend fun find(productId: Long, warehouseId: Long): StockBalanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stock: StockBalanceEntity)

    @Query("UPDATE stock_balances SET quantity = quantity - :amount, updatedAt = :now WHERE productId = :productId AND warehouseId = :warehouseId AND quantity >= :amount")
    suspend fun subtract(productId: Long, warehouseId: Long, amount: Int, now: Long = System.currentTimeMillis()): Int

    @Query("UPDATE stock_balances SET quantity = quantity + :amount, updatedAt = :now WHERE productId = :productId AND warehouseId = :warehouseId")
    suspend fun add(productId: Long, warehouseId: Long, amount: Int, now: Long = System.currentTimeMillis()): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun find(id: Long): CustomerEntity?

    @Insert
    suspend fun insert(customer: CustomerEntity): Long

    @Query("UPDATE customers SET balance = balance + :amount WHERE id = :id")
    suspend fun adjustBalance(id: Long, amount: Double)
}

@Dao
interface InvoiceDao {
    @Insert
    suspend fun insert(invoice: InvoiceEntity)

    @Insert
    suspend fun insertLine(line: InvoiceLineEntity)

    @Query("SELECT * FROM invoices WHERE userId = :userId ORDER BY createdAt DESC LIMIT 100")
    fun observeForUser(userId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT COALESCE(SUM(total), 0) FROM invoices WHERE userId = :userId AND createdAt >= :start")
    suspend fun totalSince(userId: Long, start: Long): Double

    @Query("SELECT * FROM invoices WHERE posted = 0 ORDER BY createdAt LIMIT 100")
    suspend fun pending(): List<InvoiceEntity>

    @Query("UPDATE invoices SET posted = 1 WHERE id = :id")
    suspend fun markPosted(id: String)

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId")
    suspend fun lines(invoiceId: String): List<InvoiceLineEntity>
}

@Dao
interface TransferDao {
    @Insert
    suspend fun insert(transfer: StockTransferEntity)

    @Query("SELECT * FROM stock_transfers WHERE posted = 0 ORDER BY createdAt LIMIT 100")
    suspend fun pending(): List<StockTransferEntity>

    @Query("UPDATE stock_transfers SET posted = 1 WHERE id = :id")
    suspend fun markPosted(id: String)
}

@Dao
interface BondDao {
    @Insert
    suspend fun insert(bond: FinancialBondEntity)

    @Query("SELECT * FROM financial_bonds WHERE posted = 0 ORDER BY createdAt LIMIT 100")
    suspend fun pending(): List<FinancialBondEntity>

    @Query("UPDATE financial_bonds SET posted = 1 WHERE id = :id")
    suspend fun markPosted(id: String)
}

@Dao
interface SettlementDao {
    @Insert
    suspend fun insert(settlement: SettlementEntity)

    @Query("SELECT * FROM settlements WHERE posted = 0 ORDER BY createdAt LIMIT 100")
    suspend fun pending(): List<SettlementEntity>

    @Query("UPDATE settlements SET posted = 1 WHERE id = :id")
    suspend fun markPosted(id: String)

    @Query("SELECT COALESCE(SUM(difference), 0) FROM settlements WHERE cashierUserId = :cashierUserId")
    suspend fun carriedDifference(cashierUserId: Long): Double
}