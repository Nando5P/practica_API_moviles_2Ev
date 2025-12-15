package com.example.gestionusuarioshibrido.data

// import com.example.gestionusuarioshibrido.data.local.toRemote // Descomentar si usas DTOs
// import com.example.gestionusuarioshibrido.data.remote.toLocal // Descomentar si usas DTOs
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
        // Obtenemos solo los usuarios activos (no marcados para borrar)
        return local.getAllActiveUsersStream()
    }

    override suspend fun insertUser(user: User): RepositoryResult {
        return try {
            // Marcamos como pendiente de sincronizar
            val userToInsert = user.copy(pendingSync = true, pendingDelete = false)
            local.insertUser(userToInsert)
            RepositoryResult.Success("Usuario guardado en local")
        } catch (e: Exception) {
            RepositoryResult.Error("Error al insertar usuario", e)
        }
    }

    override suspend fun updateUser(user: User): RepositoryResult {
        return try {
            // Actualizamos marcando pendingSync = true
            val userToUpdate = user.copy(pendingSync = true)
            local.updateUser(userToUpdate)
            RepositoryResult.Success("Usuario actualizado en local")
        } catch (e: Exception) {
            RepositoryResult.Error("Error al actualizar usuario", e)
        }
    }

    override suspend fun deleteUser(user: User): RepositoryResult {
        return try {
            // Borrado LÓGICO: No borramos de Room, solo marcamos flags
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
            // 1. Altas y actualizaciones (pendingUpdates)
            val usersToSync = local.getUsersToSync()
            var usersUpdated = 0

            for (user in usersToSync) {
                if (user.id.startsWith("local_")) {
                    val remoteUser = remote.createUser(user)

                    // Borramos el temporal y guardamos el definitivo (ID real)
                    local.deleteUser(user)
                    local.insertUser(remoteUser.copy(pendingSync = false))
                } else {
                    // MODIFICACIÓN -> PUT
                    remote.updateUser(user.id, user)
                    // Quitamos la marca de pendiente
                    local.updateUser(user.copy(pendingSync = false))
                }
                usersUpdated++
            }

            // 2. PendingDeletes
            val usersToDelete = local.getUsersToDelete()
            var usersDeleted = 0

            for (user in usersToDelete) {
                // Solo intentamos borrar en remoto si tiene un ID real
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
            // Descarga completa
            val remoteUsers = remote.getAllUsers()

            // Obtener IDs locales para comparar
            val localIds = local.getAllIds()

            val usersToInsert = mutableListOf<User>()
            val usersToUpdate = mutableListOf<User>()

            // Separar en listados
            for (remoteUser in remoteUsers) {
                if (localIds.contains(remoteUser.id)) {
                    usersToUpdate.add(remoteUser)
                } else {
                    usersToInsert.add(remoteUser)
                }
            }

            // 4. Aplicar cambios en Room
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
