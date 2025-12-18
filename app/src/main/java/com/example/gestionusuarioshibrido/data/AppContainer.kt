package com.example.gestionusuarioshibrido.data

import android.content.Context
import androidx.room.Room
import com.example.gestionusuarioshibrido.data.local.UserDatabase
import com.example.gestionusuarioshibrido.network.MockApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {
    val userRepository: UserRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    // Configuración de la URL
    private val BASE_URL = "https://692602c626e7e41498f90a61.mockapi.io/api/wirtz/"

    //Configuración de Retrofit
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    // Servicio API
    private val retrofitService: MockApiService by lazy {
        retrofit.create(MockApiService::class.java)
    }

    // Base de Datos Local
    private val database: UserDatabase by lazy {
        Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "offline_users_db"
        ).build()
    }

    // Repositorio de Usuarios
    override val userRepository: UserRepository by lazy {
        DefaultUserRepository(
            local = database.userDao(),
            remote = retrofitService
        )
    }
}