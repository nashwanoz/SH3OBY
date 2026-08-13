package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.Bond

@Dao
interface BondDao {
    @Insert
    suspend fun insert(b: Bond): Long

    @Query("SELECT * FROM bonds WHERE posted = 0")
    suspend fun findUnposted(): List<Bond>

    @Query("UPDATE bonds SET posted = 1 WHERE id = :id")
    suspend fun markPosted(id: Long)
}
