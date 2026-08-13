package com.khamrnet.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cashboxes")
data class CashBox(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val ownerUserId: Long? = null,
    val balance: Double = 0.0
)
