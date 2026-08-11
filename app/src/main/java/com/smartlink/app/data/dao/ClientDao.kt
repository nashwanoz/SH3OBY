package com.smartlink.app.data.dao

import androidx.room.*
import com.smartlink.app.data.entities.ClientEntity

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name COLLATE NOCASE")
    suspend fun getAllClients(): List<ClientEntity>

    @Query("SELECT * FROM clients WHERE phone = :phone LIMIT 1")
    suspend fun getClientByPhone(phone: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)
}