name=app/src/main/kotlin/com/khamrnet/app/data/entities/Customer.kt
package com.khamrnet.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobile: String,
    val balance: Double = 0.0
)
