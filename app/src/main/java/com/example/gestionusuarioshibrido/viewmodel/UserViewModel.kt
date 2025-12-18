package com.example.gestionusuarioshibrido.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gestionusuarioshibrido.data.RepositoryResult
import com.example.gestionusuarioshibrido.data.UserRepository
import com.example.gestionusuarioshibrido.data.local.User
// Asegúrate de que este import coincida con TU clase Application del AndroidManifest
import com.example.gestionusuarioshibrido.GestionUsuariosApplication
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel responsable de gestionar la lógica de presentación relacionada con usuarios.
 *
 * Orquesta las operaciones CRUD, la sincronización con el repositorio híbrido
 * (local + remoto), la comunicación de mensajes hacia la UI y la escucha del
 * sensor “shake” para sincronizar local y remoto con una sacudida.
 *
 * @property userRepository Repositorio híbrido que gestiona acceso local y remoto.
 */
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    // Estado de la lista de usuarios
    val users: StateFlow<List<User>> = repository.getAllUsersStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    fun insertUser(user: User) {
        viewModelScope.launch {
            val userToSave = if (user.id.isBlank()) {
                user.copy(id = "local_${System.nanoTime()}")
            } else {
                user
            }

            val result = repository.insertUser(userToSave)
            handleResult(result)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            val result = repository.updateUser(user)
            handleResult(result)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            val result = repository.deleteUser(user)
            handleResult(result)
        }
    }

    // Generamos un usuario aleatorio
    fun addTestUser() {
        val randomId = System.nanoTime()
        val testUser = User(
            id = "local_$randomId",
            firstName = "Test",
            lastName = "User ${Random.nextInt(1, 100)}",
            email = "test$randomId@example.com",
            age = Random.nextInt(18, 90),
            userName = "user_test",
            positionTitle = "Tester",
            imagen = "https://randomuser.me/api/portraits/lego/${Random.nextInt(1, 9)}.jpg",
            pendingSync = true,
            pendingDelete = false
        )
        insertUser(testUser)
    }

    fun sync() {
        viewModelScope.launch {
            _message.emit("Iniciando sincronización...")

            // 1. Subir cambios
            val uploadResult = repository.uploadPendingChanges()
            if (uploadResult is RepositoryResult.Error) {
                _message.emit("Error subiendo: ${uploadResult.message}")
                return@launch
            }

            val downloadResult = repository.syncFromServer()

            when (downloadResult) {
                is RepositoryResult.Success -> _message.emit(downloadResult.message)
                is RepositoryResult.Error -> _message.emit("Error descargando: ${downloadResult.message}")
            }
        }
    }

    private suspend fun handleResult(result: RepositoryResult) {
        if (result is RepositoryResult.Error) {
            _message.emit(result.message)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as com.example.gestionusuarioshibrido.GestionUsuariosApplication)
                val repository = application.container.userRepository
                UserViewModel(repository)
            }
        }
    }
}