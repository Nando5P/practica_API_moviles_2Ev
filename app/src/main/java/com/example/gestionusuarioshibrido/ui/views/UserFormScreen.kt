package com.example.gestionusuarioshibrido.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gestionusuarioshibrido.data.local.User

/**
 * Pantalla de formulario de usuario, que envuelve el contenido en un [Scaffold]
 * con barra superior y navegación hacia atrás.
 *
 * Esta pantalla decide si se muestra el formulario en modo:
 * - Creación: cuando [userId] es `null`
 * - Edición: cuando [userId] contiene el ID de un usuario existente
 *
 * Delegará el contenido editable al composable [UserEditScreen].
 *
 * @param users Lista completa de usuarios, utilizada para obtener el usuario a editar.
 * @param userId ID del usuario a modificar o `null` si se está creando uno nuevo.
 * @param onDone Callback ejecutado cuando el usuario confirma la creación o edición.
 * @param onBack Callback ejecutado cuando se pulsa el botón de retroceso.
 * @param modifier Modificador opcional para ajustar la apariencia del formulario.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    users: List<User>,
    userId: String?,
    onDone: (User) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text(
                        if (userId == null) "Crear Usuario" else "Modificar Usuario",
                    )
                }
            },
                navigationIcon = {
                    IconButton(onClick = {onBack()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                })
        }
    ) { contentPadding ->
        UserEditScreen(users, userId, onDone, Modifier.padding(contentPadding))
    }
}


/**
 * Pantalla que muestra el formulario editable para crear o modificar un usuario.
 *
 * Si [userId] coincide con un usuario en [users], los campos del formulario se cargan
 * con sus datos actuales; de lo contrario, se muestra un formulario en blanco.
 *
 * Cuando el usuario confirma, se crea una nueva instancia de [User] con los valores
 * actualizados y se envía mediante [onDone].
 *
 * @param users Lista de usuarios existente, usada para obtener datos al editar.
 * @param userId Identificador del usuario a editar o `null` para crear uno nuevo.
 * @param onDone Callback ejecutado al confirmar los cambios del formulario.
 * @param modifier Modificador opcional para ajustar la disposición del formulario.
 */

@Composable
fun UserEditScreen(
    users: List<User>,
    userId: String?,
    onDone: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    // Identificar si es edición y buscar el usuario actual
    val existingUser = remember(userId, users) {
        users.find { it.id == userId }
    }

    var firstName by remember { mutableStateOf(existingUser?.firstName ?: "") }
    var lastName by remember { mutableStateOf(existingUser?.lastName ?: "") }
    var email by remember { mutableStateOf(existingUser?.email ?: "") }
    var age by remember { mutableStateOf(existingUser?.age?.toString() ?: "") }
    var userName by remember { mutableStateOf(existingUser?.userName ?: "") }
    var positionTitle by remember { mutableStateOf(existingUser?.positionTitle ?: "") }
    // Imagen por defecto si es nuevo, o la que tenga el usuario
    var imagenUrl by remember { mutableStateOf(existingUser?.imagen ?: "https://randomuser.me/api/portraits/lego/1.jpg") }

    LaunchedEffect(existingUser) {
        if (existingUser != null) {
            firstName = existingUser.firstName
            lastName = existingUser.lastName
            email = existingUser.email
            age = existingUser.age.toString()
            userName = existingUser.userName
            positionTitle = existingUser.positionTitle
            imagenUrl = existingUser.imagen
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Apellidos") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = age,
            onValueChange = { input ->
                // Solo permitimos números
                if (input.all { it.isDigit() }) age = input
            },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = positionTitle,
            onValueChange = { positionTitle = it },
            label = { Text("Cargo / Puesto") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = imagenUrl,
            onValueChange = { imagenUrl = it },
            label = { Text("URL Imagen") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = firstName.isNotBlank() && lastName.isNotBlank(), // Validación básica
            onClick = {
                val userToSave = User(
                    id = existingUser?.id ?: "",
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    age = age.toIntOrNull() ?: 0,
                    userName = userName,
                    positionTitle = positionTitle,
                    imagen = imagenUrl,
                    pendingSync = true,
                    pendingDelete = false
                )
                onDone(userToSave)
            }
        ) {
            Text(if (userId == null) "Crear Usuario" else "Modificar Usuario")
        }
    }
}