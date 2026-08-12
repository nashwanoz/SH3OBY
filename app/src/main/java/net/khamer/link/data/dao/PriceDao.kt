package net.khamer.link.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.khamer.link.data.entities.ProductPrice

@Dao
interface PriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: ProductPrice)

    @Query("SELECT * FROM product_prices WHERE productUnitId = :unitId ORDER BY createdAt DESC LIMIT 1")
    suspend fun latestPriceForUnit(unitId: String): ProductPrice?

    @Query("SELECT * FROM product_prices WHERE productUnitId IN (SELECT id FROM product_units WHERE productId = :productId)")
    suspend fun pricesForProduct(productId: String): List<ProductPrice>
}
