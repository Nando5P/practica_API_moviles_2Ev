package com.example.gestionusuarioshibrido.network

import com.example.gestionusuarioshibrido.data.local.User
import com.example.gestionusuarioshibrido.data.remote.RemoteUser
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MockApiService {

    // Obtener todos los usuarios del servidor
    @GET("users")
    suspend fun getAllUsers(): List<User>

    // Crear un nuevo usuario en el servidor
    @POST("users")
    suspend fun createUser(@Body user: User): User

    // Actualizar un usuario existente en el servidor
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: User): User

    // Borrar un usuario en el servidor
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): User

}
