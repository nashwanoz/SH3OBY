package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.Settlement

@Dao
interface SettlementDao {
    @Insert
    suspend fun insert(s: Settlement): Long

    @Query("SELECT * FROM settlements WHERE posted = 0")
    suspend fun findUnposted(): List<Settlement>

    @Query("UPDATE settlements SET posted = 1 WHERE id = :id")
    suspend fun markPosted(id: Long)
}
