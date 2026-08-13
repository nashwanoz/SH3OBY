package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.Customer

@Dao
interface CustomerDao {
    @Insert
    suspend fun insert(c: Customer): Long

    @Query("SELECT * FROM customers")
    suspend fun getAll(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Customer?

    @Query("UPDATE customers SET balance = :balance WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Double)
}
