package com.khamrnet.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Role { ADMIN, CASHIER }

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val password: String,
    val displayName: String,
    val role: Role
)
