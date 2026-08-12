package net.khamer.link.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String?,
    val passwordHash: String?,
    val salt: String?,
    val isAdmin: Boolean,
    val assignedWarehouseId: String?,
    val assignedCashboxId: String?,
    val mustChangePassword: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
