package com.smartlink.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartlink.app.data.dao.ClientDao
import com.smartlink.app.data.entities.ClientEntity

@Database(entities = [ClientEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
}