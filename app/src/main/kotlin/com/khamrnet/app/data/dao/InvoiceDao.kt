package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.Invoice

@Dao
interface InvoiceDao {
    @Insert
    suspend fun insert(inv: Invoice): Long

    @Query("SELECT * FROM invoices WHERE posted = 0")
    suspend fun findUnposted(): List<Invoice>

    @Query("UPDATE invoices SET posted = 1 WHERE id = :id")
    suspend fun markPosted(id: Long)
}
