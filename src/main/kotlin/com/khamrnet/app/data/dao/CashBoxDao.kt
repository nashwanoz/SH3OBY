package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.CashBox

@Dao
interface CashBoxDao {
    @Insert
    suspend fun insert(c: CashBox): Long

    @Query("SELECT * FROM cashboxes WHERE ownerUserId = :userId LIMIT 1")
    suspend fun findByOwner(userId: Long): CashBox?

    @Query("SELECT * FROM cashboxes WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): CashBox?

    @Query("UPDATE cashboxes SET balance = :balance WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Double)
}
