package com.smartlink.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "clients", indices = [Index(value = ["phone"], unique = true)])
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val clientCode: String,
    val name: String,
    val phone: String,
    val address: String? = null,
    val whatsappOptIn: Boolean = true,
    val smsOptIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)