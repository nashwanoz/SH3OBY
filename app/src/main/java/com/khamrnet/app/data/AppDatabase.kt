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
    version = 2,
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
                .addMigrations(MIGRATION_1_2)
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
    }
}