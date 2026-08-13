package com.khamrnet.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settlements")
data class Settlement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cashierId: Long,
    val physicalCash: Double,
    val expectedCash: Double,
    val difference: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val posted: Int = 0
)
