package net.khamer.link.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.khamer.link.auth.PasswordUtils
import net.khamer.link.data.dao.*
import net.khamer.link.data.entities.*
import java.util.*

@Database(entities = [User::class, Product::class, ProductUnit::class, ProductPrice::class, Stock::class, StockTransaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun priceDao(): PriceDao
    abstract fun stockDao(): StockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            scope.launch {
                                val dao = database.userDao()
                                val salt = PasswordUtils.generateSalt()
                                val hash = PasswordUtils.hashPassword("1", salt)
                                val now = System.currentTimeMillis()
                                val admin = User(
                                    id = UUID.randomUUID().toString(),
                                    username = "1",
                                    displayName = "المدير",
                                    passwordHash = hash,
                                    salt = salt,
                                    isAdmin = true,
                                    assignedWarehouseId = null,
                                    assignedCashboxId = null,
                                    mustChangePassword = true,
                                    createdAt = now,
                                    updatedAt = now
                                )
                                dao.insert(admin)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
