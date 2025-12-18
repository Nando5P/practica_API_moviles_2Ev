package com.example.gestionusuarioshibrido.ui.views

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestionusuarioshibrido.sensors.ShakeUserCoordinator
import com.example.gestionusuarioshibrido.viewmodel.UserViewModel


/**
 * Controlador principal de navegación de la aplicación.
 *
 * Este composable centraliza todas las rutas y pantallas, gestionando:
 * - La creación del `NavController`.
 * - El acceso al `UserViewModel`.
 * - La configuración del shake listener.
 * - La configuración del `SnackbarHostState` para mostrar mensajes al usuario.
 * - La definición del `NavHost` y sus destinos.
 *
 * Pantallas incluidas:
 *  - `"user_list"`: Lista de usuarios.
 *  - `"user_form"`: Formulario para crear un usuario.
 *  - `"user_form/{id}"`: Formulario para editar un usuario existente.
 *
 * La navegación se realiza mediante rutas simples con argumentos.
 *
 * @param modifier Modificador opcional para ajustar el contenedor raíz.
 */

@Composable
fun AppNavigation(viewModel: UserViewModel) {

    val navController = rememberNavController()
    val users by viewModel.users.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.message.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(navController = navController, startDestination = "userList") {

        // RUTA: Lista de Usuarios
        composable("userList") {
            val sensorCoordinator = remember {
                ShakeUserCoordinator(context, viewModel)
            }

            // Iniciar y detener el listener de shake
            DisposableEffect(Unit) {
                sensorCoordinator.startListening()
                onDispose { sensorCoordinator.stopListening() }
            }

            UserListScreen(
                users = users,
                onAddUser = { navController.navigate("userForm_create") }, // Ir a crear
                onEditUser = { userId -> navController.navigate("userForm_edit/$userId") }, // Ir a editar
                onDeleteUser = { user -> viewModel.deleteUser(user) },
                onSync = { viewModel.sync() },
                onAddTestUser = { viewModel.addTestUser() }
            )
        }

        // RUTA: Pantalla de Creación
        composable("userForm_create") {
            UserFormScreen(
                users = users,
                userId = null,
                onDone = { newUser ->
                    viewModel.insertUser(newUser)
                    navController.popBackStack() // Volver
                },
                onBack = { navController.popBackStack() }
            )
        }

        // RUTA 3: Pantalla de Edición
        composable(
            route = "userForm_edit/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")

            UserFormScreen(
                users = users,
                userId = userId,
                onDone = { updatedUser ->
                    viewModel.updateUser(updatedUser)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
