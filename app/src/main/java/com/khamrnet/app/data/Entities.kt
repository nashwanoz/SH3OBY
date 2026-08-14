package com.khamrnet.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["userCode"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val userCode: String,
    val passwordHash: String,
    val displayName: String,
    val role: String,
    val active: Boolean = true,
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
    val canWhatsapp: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun canAccess(section: String): Boolean =
        role == "ADMIN" || when (section) {
            "HOME" -> canHome
            "POS" -> canPos
            "INVOICES" -> canInvoices
            "REPORTS" -> canReports
            "PRODUCTS" -> canProducts
            "USERS" -> canUsers
            "TRANSFERS" -> canTransfers
            "CUSTOMERS" -> canCustomers
            "BONDS" -> canBonds
            "SETTLEMENTS" -> canSettlements
            else -> false
        }
}

@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val ownerUserId: Long? = null,
    val isMain: Boolean = false
)

@Entity(tableName = "cash_boxes")
data class CashBoxEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val ownerUserId: Long? = null,
    val balance: Double = 0.0
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val barcode: String = "",
    val unitName: String = "حبة",
    val price: Double,
    val caseUnitName: String = "كرت",
    val caseQuantity: Int = 60,
    val casePrice: Double = 0.0,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_balances", primaryKeys = ["productId", "warehouseId"])
data class StockBalanceEntity(
    val productId: Long,
    val warehouseId: Long,
    val quantity: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val customerCode: String,
    val mobile: String = "",
    val balance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

data class CustomerMovementTime(
    val customerId: Long,
    val lastMovementAt: Long?
)

data class CustomerStatementRow(
    val createdAt: Long,
    val reference: String,
    val type: String,
    val amount: Double,
    val balanceAfter: Double
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val userId: Long,
    val customerId: Long? = null,
    val paymentType: String,
    val total: Double,
    val previousBalance: Double = 0.0,
    val newBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val posted: Boolean = false
)

@Entity(tableName = "invoice_lines")
data class InvoiceLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: String,
    val productId: Long,
    val productName: String,
    val unitName: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

@Entity(tableName = "stock_transfers")
data class StockTransferEntity(
    @PrimaryKey val id: String,
    val userId: Long,
    val productId: Long,
    val fromWarehouseId: Long,
    val toWarehouseId: Long,
    val quantity: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val posted: Boolean = false
)

@Entity(tableName = "financial_bonds")
data class FinancialBondEntity(
    @PrimaryKey val id: String,
    val userId: Long,
    val customerId: Long,
    val type: String,
    val amount: Double,
    val previousBalance: Double = 0.0,
    val newBalance: Double = 0.0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val posted: Boolean = false
)

@Entity(tableName = "settlements")
data class SettlementEntity(
    @PrimaryKey val id: String,
    val adminUserId: Long,
    val cashierUserId: Long,
    val systemAmount: Double,
    val actualAmount: Double,
    val difference: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val posted: Boolean = false
)
