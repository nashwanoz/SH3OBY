package net.khamer.link.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import net.khamer.link.data.entities.Stock
import net.khamer.link.data.entities.StockTransaction

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks WHERE productId = :productId LIMIT 1")
    suspend fun getByProductId(productId: String): Stock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: Stock)

    @Update
    suspend fun updateStock(stock: Stock)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: StockTransaction)
}
