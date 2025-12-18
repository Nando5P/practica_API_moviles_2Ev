package com.example.gestionusuarioshibrido.data

import android.util.Log
import com.example.gestionusuarioshibrido.data.local.User
import com.example.gestionusuarioshibrido.data.local.UserDao
import com.example.gestionusuarioshibrido.network.MockApiService
import kotlinx.coroutines.flow.Flow


sealed class RepositoryResult {
    class Success(val message: String) : RepositoryResult()
    data class Error(val message: String, val exception: Throwable? = null) : RepositoryResult()
}

interface UserRepository {

    fun getAllUsersStream(): Flow<List<User>>

    suspend fun insertUser(user: User): RepositoryResult

    suspend fun updateUser(user: User): RepositoryResult

    suspend fun deleteUser(user: User): RepositoryResult

    // Sincronización
    suspend fun uploadPendingChanges(): RepositoryResult
    suspend fun syncFromServer(): RepositoryResult
}

class DefaultUserRepository(
    private val local: UserDao,
    private val remote: MockApiService
) : UserRepository {

    override fun getAllUsersStream(): Flow<List<User>> {
        // Obtenemos solo los usuarios activos (no borrados en local)
        return local.getAllActiveUsersStream()
    }

    override suspend fun insertUser(user: User): RepositoryResult {
        return try {
            val userToInsert = user.copy(pendingSync = true, pendingDelete = false)
            local.insertUser(userToInsert)
            RepositoryResult.Success("Usuario guardado en local")
        } catch (e: Exception) {
            RepositoryResult.Error("Error al insertar usuario", e)
        }
    }

    override suspend fun updateUser(user: User): RepositoryResult {
        return try {
            val userToUpdate = user.copy(pendingSync = true)
            local.updateUser(userToUpdate)
            RepositoryResult.Success("Usuario actualizado en local")
        } catch (e: Exception) {
            RepositoryResult.Error("Error al actualizar usuario", e)
        }
    }

    override suspend fun deleteUser(user: User): RepositoryResult {
        return try {
            val userToDelete = user.copy(pendingDelete = true, pendingSync = true)
            local.updateUser(userToDelete)
            RepositoryResult.Success("Usuario marcado para eliminar")
        } catch (e: Exception) {
            RepositoryResult.Error("Error al eliminar usuario", e)
        }
    }

    /**
     * Sincroniza con el servidor remoto todos los cambios pendientes almacenados en la base de datos local.
     */
    override suspend fun uploadPendingChanges(): RepositoryResult {
        return try {
            val usersToSync = local.getUsersToSync()
            var usersUpdated = 0

            for (user in usersToSync) {
                if (user.id.startsWith("local_")) {
                    val remoteUser = remote.createUser(user)

                    local.deleteUser(user)
                    local.insertUser(remoteUser.copy(pendingSync = false))
                } else {
                    remote.updateUser(user.id, user)
                    local.updateUser(user.copy(pendingSync = false))
                }
                usersUpdated++
            }

            val usersToDelete = local.getUsersToDelete()
            var usersDeleted = 0

            for (user in usersToDelete) {
                if (!user.id.startsWith("local_")) {
                    try {
                        remote.deleteUser(user.id)
                    } catch (e: Exception) {
                        Log.e("Sync", "Error borrando remoto: ${e.message}")
                    }
                }

                local.deleteUser(user)
                usersDeleted++
            }

            RepositoryResult.Success("Subida: $usersUpdated actualizados, $usersDeleted borrados")

        } catch (e: Exception) {
            RepositoryResult.Error("Error subiendo datos: ${e.message}", e)
        }
    }

    override suspend fun syncFromServer(): RepositoryResult {
        return try {
            val remoteUsers = remote.getAllUsers()
            val localIds = local.getAllIds()
            val usersToInsert = mutableListOf<User>()
            val usersToUpdate = mutableListOf<User>()

            for (remoteUser in remoteUsers) {
                if (localIds.contains(remoteUser.id)) {
                    usersToUpdate.add(remoteUser)
                } else {
                    usersToInsert.add(remoteUser)
                }
            }

            if (usersToUpdate.isNotEmpty()) {
                local.updateUsers(usersToUpdate)
            }
            if (usersToInsert.isNotEmpty()) {
                local.insertUsers(usersToInsert)
            }

            RepositoryResult.Success("Descarga: ${usersToInsert.size} nuevos, ${usersToUpdate.size} actualizados")

        } catch (e: Exception) {
            RepositoryResult.Error("Error descargando datos: ${e.message}", e)
        }
    }
}
