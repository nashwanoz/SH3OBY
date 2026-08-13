package com.khamrnet.app.data.db
import android.content.Context
import androidx.room.*
import com.khamrnet.app.data.dao.*
import com.khamrnet.app.data.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Room DB and converters
 */
class Converters {
    @TypeConverter
    fun fromRole(role: Role?): String? = role?.name

    @TypeConverter
    fun toRole(name: String?): Role? = name?.let { Role.valueOf(it) }
}

@Database(
    entities = [
        User::class,
        Warehouse::class,
        CashBox::class,
        Product::class,
        ProductUnit::class,
        StockAllocation::class,
        Customer::class,
        Invoice::class,
        InvoiceLine::class,
        Bond::class,
        Settlement::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KhamrDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun cashBoxDao(): CashBoxDao
    abstract fun productDao(): ProductDao
    abstract fun productUnitDao(): ProductUnitDao
    abstract fun stockAllocationDao(): StockAllocationDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceLineDao(): InvoiceLineDao
    abstract fun bondDao(): BondDao
    abstract fun settlementDao(): SettlementDao

    companion object {
        @Volatile
        private var INSTANCE: KhamrDatabase? = null

        fun getDatabase(context: Context): KhamrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KhamrDatabase::class.java,
                    "khamr_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
