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

        val userViewModel: UserViewModel by viewModels { UserViewModel.Factory }

        setContent {
            GestionUsuariosTheme {
                AppNavigation(viewModel = userViewModel)
            }
        }
    }
}