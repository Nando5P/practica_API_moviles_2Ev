package com.example.gestionusuarioshibrido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.gestionusuarioshibrido.ui.theme.GestionUsuariosTheme
import com.example.gestionusuarioshibrido.ui.views.AppNavigation
import com.example.gestionusuarioshibrido.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Instanciamos el ViewModel usando la Factory
        // Esto conecta la UI con la base de datos y la API
        val userViewModel: UserViewModel by viewModels { UserViewModel.Factory }

        setContent {
            GestionUsuariosTheme {
                // 2. Pasamos el ViewModel a la navegación
                AppNavigation(viewModel = userViewModel)
            }
        }
    }
}