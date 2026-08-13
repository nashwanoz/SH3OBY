package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.InvoiceLine

@Dao
interface InvoiceLineDao {
    @Insert
    suspend fun insert(line: InvoiceLine): Long

    @Query("SELECT * FROM invoice_lines WHERE posted = 0")
    suspend fun findUnposted(): List<InvoiceLine>

    @Query("UPDATE invoice_lines SET posted = 1 WHERE id = :id")
    suspend fun markPosted(id: Long)

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId")
    suspend fun findByInvoice(invoiceId: Long): List<InvoiceLine>
}
