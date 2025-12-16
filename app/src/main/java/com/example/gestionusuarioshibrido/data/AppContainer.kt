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

// CORRECCIÓN 1: Cambiado nombre de DefaultAppContainer a AppDataContainer
class AppDataContainer(private val context: Context) : AppContainer {

    // 1. Configuración de la Base URL
    private val BASE_URL = "https://692602c626e7e41498f90a61.mockapi.io/api/wirtz/"

    // 2. Configuración de Retrofit
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    // 3. Servicio API
    private val retrofitService: MockApiService by lazy {
        retrofit.create(MockApiService::class.java)
    }

    // 4. Base de Datos Local
    private val database: UserDatabase by lazy {
        Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "offline_users_db"
        ).build()
    }

    // 5. Inyección de Dependencias
    override val userRepository: UserRepository by lazy {
        // CORRECCIÓN 2: Usamos los nombres de parámetros correctos (local y remote)
        DefaultUserRepository(
            local = database.userDao(),
            remote = retrofitService
        )
    }
}