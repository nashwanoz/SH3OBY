package com.khamrnet.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        WarehouseEntity::class,
        CashBoxEntity::class,
        ProductEntity::class,
        StockBalanceEntity::class,
        CustomerEntity::class,
        InvoiceEntity::class,
        InvoiceLineEntity::class,
        StockTransferEntity::class,
        FinancialBondEntity::class,
        SettlementEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun users(): UserDao
    abstract fun warehouses(): WarehouseDao
    abstract fun cashBoxes(): CashBoxDao
    abstract fun products(): ProductDao
    abstract fun stock(): StockDao
    abstract fun customers(): CustomerDao
    abstract fun invoices(): InvoiceDao
    abstract fun transfers(): TransferDao
    abstract fun bonds(): BondDao
    abstract fun settlements(): SettlementDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "khamr-net.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN userCode TEXT NOT NULL DEFAULT ''")
                database.execSQL("UPDATE users SET userCode = CAST(id AS TEXT) WHERE userCode = ''")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_userCode ON users(userCode)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN canHome INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE users ADD COLUMN canPos INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE users ADD COLUMN canInvoices INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE users ADD COLUMN canReports INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE users ADD COLUMN canProducts INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN canUsers INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN canTransfers INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN canCustomers INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE users ADD COLUMN canBonds INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE users ADD COLUMN canSettlements INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN canWhatsapp INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE customers ADD COLUMN customerCode TEXT NOT NULL DEFAULT ''")
                database.execSQL("UPDATE customers SET customerCode = 'C' || id WHERE customerCode = ''")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_customers_customerCode ON customers(customerCode)")
                database.execSQL("ALTER TABLE financial_bonds ADD COLUMN previousBalance REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE financial_bonds ADD COLUMN newBalance REAL NOT NULL DEFAULT 0")
                database.execSQL(
                    "UPDATE financial_bonds SET newBalance = CASE WHEN type = 'قبض' THEN -amount ELSE amount END"
                )
            }
        }
    }
}