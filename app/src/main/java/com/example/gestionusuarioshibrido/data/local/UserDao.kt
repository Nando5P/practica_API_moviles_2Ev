package com.example.gestionusuarioshibrido.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad [User].
 *
 * Esta interfaz define las operaciones de acceso y manipulación de datos
 * que Room implementará automáticamente en tiempo de compilación.
 *
 * Incluye consultas reactivas mediante [Flow], lo cual permite escuchar
 * actualizaciones en tiempo real de la base de datos.
 */
@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE pendingDelete = 0")
    fun getAllActiveUsersStream(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUsers(users: List<User>)

    @Query("SELECT * FROM users WHERE pendingSync = 1 AND pendingDelete = 0")
    suspend fun getUsersToSync(): List<User>

    @Query("SELECT * FROM users WHERE pendingDelete = 1")
    suspend fun getUsersToDelete(): List<User>

    @Query("SELECT id FROM users")
    suspend fun getAllIds(): List<String>
}