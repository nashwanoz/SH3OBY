
package com.khamrnet.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bonds")
data class Bond(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "قبض" أو "صرف"
    val amount: Double,
    val userId: Long?,
    val customerId: Long?,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val posted: Int = 0
)
