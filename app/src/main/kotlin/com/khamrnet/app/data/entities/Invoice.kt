package com.khamrnet.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cashierId: Long,
    val customerId: Long?,
    val total: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val posted: Int = 0
)
