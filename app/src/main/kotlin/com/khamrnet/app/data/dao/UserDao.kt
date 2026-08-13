package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Insert
    suspend fun insert(user: User): Long

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?
}
