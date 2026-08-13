name=app/src/main/kotlin/com/khamrnet/app/data/entities/Warehouse.kt
package com.khamrnet.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warehouses")
data class Warehouse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val ownerUserId: Long? = null // null = central warehouse
)
