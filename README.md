# Gestión de Usuarios Híbrido 📱

Aplicación Android desarrollada en Kotlin con Jetpack Compose para la gestión de usuarios con funcionalidad híbrida (online/offline), que permite sincronizar datos entre una base de datos local (Room) y un servidor remoto (MockAPI).

## 📋 Características Principales

- ✅ **CRUD completo de usuarios**: Crear, leer, actualizar y eliminar usuarios
- 🔄 **Sincronización híbrida**: Funciona offline y sincroniza cambios con el servidor
- 📶 **Detección de sacudida**: Sincroniza datos al agitar el dispositivo
- 🎨 **Interfaz moderna**: Diseñada con Jetpack Compose y Material Design 3
- 🖼️ **Carga de imágenes**: Integración con Coil para cargar avatares de usuarios
- 🗄️ **Persistencia local**: Base de datos Room para almacenamiento offline

## 🛠️ Tecnologías Utilizadas

- **Kotlin**: Lenguaje de programación principal
- **Jetpack Compose**: Framework para UI declarativa
- **Material Design 3**: Sistema de diseño de Google
- **Room**: Base de datos local SQLite
- **Retrofit**: Cliente HTTP para consumir APIs REST
- **Kotlin Serialization**: Serialización/deserialización de JSON
- **Coil 3**: Carga y caché de imágenes
- **Navigation Compose**: Navegación entre pantallas
- **Coroutines & Flow**: Programación asíncrona y reactiva
- **ViewModel**: Arquitectura MVVM para gestión de estado
- **Sensor Framework**: Acceso al acelerómetro del dispositivo

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/gestionusuarioshibrido/
│
├── MainActivity.kt                          # Actividad principal
├── GestionUsuariosApplication.kt            # Clase Application para DI
│
├── data/                                    # Capa de datos
│   ├── AppContainer.kt                      # Contenedor de dependencias
│   ├── UserRepository.kt                    # Repositorio de usuarios
│   ├── TestUsers.kt                         # Datos de prueba
│   │
│   ├── local/                               # Base de datos local
│   │   ├── User.kt                          # Entidad usuario (Room)
│   │   ├── UserDao.kt                       # Data Access Object
│   │   └── UserDatabase.kt                  # Configuración de Room
│   │
│   └── remote/                              # Datos remotos
│       └── RemoteUser.kt                    # Modelo de usuario remoto
│
├── network/                                 # Capa de red
│   └── MockApiService.kt                    # Interfaz Retrofit
│
├── viewmodel/                               # Capa de presentación
│   └── UserViewModel.kt                     # ViewModel principal
│
├── ui/                                      # Interfaz de usuario
│   ├── components/                          # Componentes reutilizables
│   │   └── UserCard.kt                      # Tarjeta de usuario
│   │
│   ├── views/                               # Pantallas
│   │   ├── AppNavigation.kt                 # Controlador de navegación
│   │   ├── UserListScreen.kt                # Pantalla lista de usuarios
│   │   └── UserFormScreen.kt                # Pantalla formulario
│   │
│   └── theme/                               # Tema de la app
│       ├── Color.kt                         # Colores
│       ├── Theme.kt                         # Tema principal
│       └── Type.kt                          # Tipografía
│
└── sensors/                                 # Sensores del dispositivo
    ├── SensorShakeDetector.kt               # Detector de sacudidas
    └── ShakeUserCoordinator.kt              # Coordinador de sincronización
```

## 🔍 Descripción de Clases Principales

### 📱 Actividad Principal

#### `MainActivity.kt`
Punto de entrada de la aplicación. Inicializa el `UserViewModel` y configura el tema de Jetpack Compose. Delega la navegación a `AppNavigation`.

**Responsabilidades:**
- Crear el ViewModel con su Factory
- Establecer el contenido de la UI con Compose
- Aplicar el tema de la aplicación

---

### 🏗️ Capa de Aplicación

#### `GestionUsuariosApplication.kt`
Clase que extiende `Application` y actúa como punto de inicialización de la aplicación.

**Responsabilidades:**
- Crear y mantener el contenedor de dependencias (`AppContainer`)
- Inicializar componentes globales al arrancar la app
- Proporcionar acceso al contexto de aplicación

---

### 💾 Capa de Datos

#### `AppContainer.kt`
Implementa el patrón **Dependency Injection** manual. Contiene dos clases:

**`AppContainer` (Interfaz):**
- Define el contrato para acceder a los repositorios

**`AppDataContainer` (Implementación):**
- Configura Retrofit con la URL base del API
- Crea la instancia de Room Database
- Proporciona el `UserRepository` configurado

**Componentes que gestiona:**
- Retrofit con serialización JSON
- Room Database local
- MockApiService
- UserRepository

#### `UserRepository.kt`
Patrón **Repository** que abstrae el acceso a datos locales y remotos.

**Clases:**

**`RepositoryResult` (Sealed Class):**
- `Success`: Operación exitosa con mensaje
- `Error`: Operación fallida con mensaje y excepción

**`UserRepository` (Interfaz):**
Define las operaciones disponibles sobre usuarios.

**`DefaultUserRepository` (Implementación):**
Implementa la lógica híbrida de sincronización.

**Responsabilidades:**
- Gestionar operaciones CRUD en la base de datos local
- Marcar usuarios con flags de sincronización (`pendingSync`, `pendingDelete`)
- Subir cambios pendientes al servidor (`uploadPendingChanges`)
- Descargar usuarios desde el servidor (`syncFromServer`)
- Resolver conflictos entre datos locales y remotos
- Manejar IDs temporales para usuarios creados offline

**Flujo de sincronización:**
1. Los cambios locales se marcan con `pendingSync = true`
2. Al sincronizar, se suben primero los cambios locales al servidor
3. Se descargan los datos del servidor y se actualizan en local
4. Los usuarios con ID `local_*` se reemplazan por IDs del servidor

#### `data/local/User.kt`
Modelo de datos principal de la aplicación.

**Anotaciones:**
- `@Entity`: Define la tabla "users" en Room
- `@PrimaryKey`: Marca el campo `id` como clave primaria
- `@Serializable`: Permite serialización con Kotlin Serialization

**Propiedades:**
- `id`: Identificador único (puede ser del servidor o local temporal)
- `firstName`: Nombre del usuario
- `lastName`: Apellidos
- `email`: Correo electrónico
- `age`: Edad
- `userName`: Nombre de usuario único
- `positionTitle`: Cargo o puesto de trabajo
- `imagen`: URL de la imagen de perfil
- `pendingSync`: Indica si hay cambios pendientes de sincronizar
- `pendingDelete`: Indica si el usuario está marcado para eliminar

**Función de extensión:**
- `toRemote()`: Convierte un `User` local a `RemoteUser` para enviar al API

#### `data/local/UserDao.kt`
**Data Access Object** (DAO) de Room que define las operaciones de base de datos.

**Métodos principales:**
- `getAllActiveUsersStream()`: Flow reactivo de usuarios activos (no marcados para borrar)
- `getUserById()`: Obtiene un usuario por su ID
- `insertUser()` / `insertUsers()`: Inserta uno o varios usuarios
- `updateUser()` / `updateUsers()`: Actualiza usuarios existentes
- `deleteUser()` / `deleteAllUsers()`: Elimina usuarios
- `getUsersToSync()`: Obtiene usuarios pendientes de sincronizar
- `getUsersToDelete()`: Obtiene usuarios marcados para eliminar
- `getAllIds()`: Lista todos los IDs para comparar con el servidor

**Características:**
- Usa `Flow` para observar cambios en tiempo real
- Operaciones suspendidas para ejecutar en coroutines
- Estrategia de reemplazo en conflictos de inserción

#### `data/local/UserDatabase.kt`
Configuración de la base de datos Room.

**Características:**
- Base de datos versión 1
- Contiene la entidad `User`
- Implementa patrón Singleton
- Usa `@Volatile` para seguridad en multi-threading
- `fallbackToDestructiveMigration()`: Recrea la BD en cambios de versión

**Métodos:**
- `userDao()`: Proporciona acceso al DAO
- `getDatabase()`: Obtiene/crea la instancia única de la BD

#### `data/remote/RemoteUser.kt`
Modelo de datos para comunicación con el API REST.

**Diferencias con `User` local:**
- El `id` es nullable (puede ser null al crear usuarios)
- No contiene campos de sincronización (`pendingSync`, `pendingDelete`)

**Función de extensión:**
- `toLocal()`: Convierte un `RemoteUser` a `User` local, generando ID temporal si es necesario

#### `TestUsers.kt`
Contiene una lista de 20 usuarios de prueba con datos realistas para facilitar el desarrollo y testing.

---

### 🌐 Capa de Red

#### `network/MockApiService.kt`
Interfaz de Retrofit que define los endpoints del API REST.

**Endpoints:**
- `GET /users`: Obtiene todos los usuarios del servidor
- `POST /users`: Crea un nuevo usuario
- `PUT /users/{id}`: Actualiza un usuario existente
- `DELETE /users/{id}`: Elimina un usuario

**Características:**
- Funciones suspendidas para uso con coroutines
- Serialización automática con Kotlin Serialization
- Integrado con MockAPI.io para simular backend

---

### 🎨 Capa de ViewModel

#### `viewmodel/UserViewModel.kt`
ViewModel que gestiona el estado de la UI y la lógica de negocio.

**Propiedades:**
- `users`: StateFlow con la lista de usuarios
- `message`: SharedFlow para mensajes al usuario

**Métodos principales:**
- `insertUser()`: Crea un nuevo usuario (genera ID local si es necesario)
- `updateUser()`: Actualiza un usuario existente
- `deleteUser()`: Marca un usuario para eliminar
- `addTestUser()`: Genera y añade un usuario de prueba aleatorio
- `sync()`: Ejecuta sincronización completa (subir y descargar)

**Características:**
- Usa `viewModelScope` para lanzar coroutines
- `StateFlow` para estado reactivo observado por la UI
- `SharedFlow` para eventos únicos (mensajes)
- Factory personalizado para inyección de dependencias

**Flujo de sincronización:**
1. Emite mensaje "Iniciando sincronización..."
2. Sube cambios pendientes al servidor
3. Descarga datos del servidor
4. Emite resultado final

---

### 🎭 Capa de UI

#### `ui/views/AppNavigation.kt`
Controlador central de navegación de la aplicación usando Navigation Compose.

**Rutas definidas:**
- `userList`: Pantalla principal con lista de usuarios
- `userForm_create`: Formulario para crear usuario nuevo
- `userForm_edit/{userId}`: Formulario para editar usuario existente

**Responsabilidades:**
- Gestionar el NavController
- Observar mensajes del ViewModel y mostrar Toasts
- Inicializar el detector de sacudidas en la pantalla principal
- Coordinar la navegación entre pantallas

#### `ui/views/UserListScreen.kt`
Pantalla principal que muestra la lista de usuarios.

**Componentes:**
- `TopAppBar`: Barra superior con botones de sincronización y añadir usuario de prueba
- `LazyColumn`: Lista scrollable de usuarios
- `FloatingActionButton`: Botón para crear nuevo usuario

**Funcionalidades:**
- Mostrar todos los usuarios activos
- Navegar a formulario de edición al tocar un usuario
- Eliminar usuarios con confirmación
- Sincronizar datos manualmente
- Añadir usuarios de prueba rápidamente

#### `ui/views/UserFormScreen.kt`
Pantallas para crear y editar usuarios. Contiene dos composables:

**`UserFormScreen`:**
Scaffold con barra superior y navegación hacia atrás.

**`UserEditScreen`:**
Formulario editable con los siguientes campos:
- Nombre
- Apellidos
- Email (teclado de email)
- Edad (teclado numérico)
- Nombre de usuario
- Cargo/Puesto
- URL de imagen

**Características:**
- Modo creación: campos vacíos
- Modo edición: campos pre-cargados con datos existentes
- Validación: requiere nombre y apellidos
- Actualización reactiva con `LaunchedEffect`

#### `ui/components/UserCard.kt`
Componente reutilizable que representa un usuario en la lista.

**Elementos visuales:**
- Imagen circular del usuario (con Coil)
- Nombre completo en negrita
- Puesto de trabajo
- Nombre de usuario y edad
- Email
- Botones de editar y eliminar

**Características:**
- Diseño con Material 3 Card
- Carga asíncrona de imágenes
- Imágenes de placeholder y error
- Layout responsive

#### `ui/theme/`
Contiene la configuración del tema visual de la aplicación:

- **`Color.kt`**: Paleta de colores de la app
- **`Theme.kt`**: Configuración del tema claro/oscuro
- **`Type.kt`**: Sistema de tipografía Material 3

---

### 📡 Capa de Sensores

#### `sensors/SensorShakeDetector.kt`
Detector de sacudidas del dispositivo usando el acelerómetro.

**Funcionamiento:**
1. Registra listener del sensor de aceleración
2. Calcula la fuerza G en los tres ejes (x, y, z)
3. Si la fuerza supera el umbral (1.3G), detecta sacudida
4. Implementa debouncing de 500ms para evitar múltiples eventos

**Parámetros configurables:**
- `SHAKE_THRESHOLD_GRAVITY`: Umbral de fuerza (1.3G)
- `SHAKE_SLOP_TIME_MS`: Tiempo mínimo entre sacudidas (500ms)

**Métodos:**
- `start()`: Inicia la escucha del acelerómetro
- `stop()`: Detiene la escucha
- `onSensorChanged()`: Procesa eventos del sensor

#### `sensors/ShakeUserCoordinator.kt`
Coordinador que conecta el detector de sacudidas con el ViewModel.

**Responsabilidades:**
- Crear instancia de `SensorShakeDetector`
- Definir callback de sacudida
- Mostrar Toast de feedback al usuario
- Ejecutar sincronización cuando se detecta sacudida

**Flujo:**
1. Usuario agita el dispositivo
2. `SensorShakeDetector` detecta la sacudida
3. `ShakeUserCoordinator` muestra Toast "¡Sacudida! Sincronizando..."
4. Se llama a `userViewModel.sync()`
5. Los datos se sincronizan con el servidor

---

## 🚀 Instalación y Configuración

### Requisitos previos:
- Android Studio Hedgehog o superior
- Kotlin 1.9+
- SDK mínimo: Android 7.0 (API 24)
- SDK objetivo: Android 14 (API 36)

### Pasos de instalación:

1. **Clonar el repositorio:**
```bash
git clone https://github.com/Nando5P/practica_API_moviles_2Ev.git
cd practica_API_moviles_2Ev
```

2. **Abrir en Android Studio:**
   - File → Open
   - Seleccionar la carpeta del proyecto
   - Esperar a que Gradle sincronice las dependencias

3. **Configurar el API (Opcional):**
   - El proyecto usa MockAPI.io con URL ya configurada
   - Si deseas usar tu propia API, modifica `BASE_URL` en `AppContainer.kt`

4. **Ejecutar la aplicación:**
   - Conectar un dispositivo Android o iniciar un emulador
   - Hacer clic en Run (▶️) o presionar Shift+F10

## 📖 Uso de la Aplicación

### Gestión de Usuarios:
1. **Ver lista**: La pantalla principal muestra todos los usuarios
2. **Crear usuario**: Pulsar el botón flotante "+" 
3. **Editar usuario**: Tocar el icono de edición en una tarjeta de usuario
4. **Eliminar usuario**: Tocar el icono de papelera

### Sincronización:
- **Manual**: Pulsar el botón de sincronización en la barra superior
- **Automática**: Agitar el dispositivo para sincronizar

### Usuario de prueba:
- Pulsar el icono de persona con "+" para añadir un usuario aleatorio de prueba

## 🏗️ Arquitectura

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)** con arquitectura limpia:

```
┌─────────────────────────────────────────┐
│            UI Layer (Compose)           │
│  - UserListScreen                       │
│  - UserFormScreen                       │
│  - UserCard                             │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│      ViewModel Layer                    │
│  - UserViewModel                        │
│  - State Management                     │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│      Data Layer                         │
│  - UserRepository (híbrido)             │
│  ├─ Local: Room Database                │
│  └─ Remote: Retrofit + MockAPI          │
└─────────────────────────────────────────┘
```

### Flujo de datos:
1. **UI → ViewModel**: Eventos del usuario
2. **ViewModel → Repository**: Operaciones de datos
3. **Repository → Local/Remote**: Acceso a datos
4. **Repository → ViewModel**: Resultados
5. **ViewModel → UI**: Estado actualizado (Flow/StateFlow)

## 🔧 Dependencias Principales

```kotlin
// Compose & Material Design
implementation("androidx.activity:activity-compose")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")

// Navigation
implementation("androidx.navigation:navigation-compose")

// Room Database
implementation("androidx.room:room-runtime")
implementation("androidx.room:room-ktx")
ksp("androidx.room:room-compiler")

// Retrofit & Networking
implementation("com.squareup.retrofit2:retrofit")
implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter")
implementation("com.squareup.okhttp3:okhttp")

// Kotlin Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

// Coil 3 - Image Loading
implementation("io.coil-kt.coil3:coil-compose")
implementation("io.coil-kt.coil3:coil-network-okhttp")
```

## 📝 Notas Técnicas

### Gestión de IDs:
- **IDs del servidor**: Números o strings del API
- **IDs locales temporales**: Prefijo `"local_"` + timestamp
- Al sincronizar, los IDs locales se reemplazan por IDs del servidor

### Estrategia de sincronización:
1. **Optimista**: Los cambios se aplican localmente primero
2. **Eventual**: Se sincronizan con el servidor cuando hay conexión
3. **Flags de estado**: `pendingSync` y `pendingDelete` marcan cambios pendientes

### Manejo de conflictos:
- Los datos del servidor tienen prioridad en descargas
- Los cambios locales se suben antes de descargar
- La estrategia `REPLACE` en Room sobrescribe registros en conflicto

## 👨‍💻 Autor

**Fernando** - [Nando5P](https://github.com/Nando5P)

## 📄 Licencia

Este proyecto es una práctica educativa para el módulo de desarrollo móvil.

---

**Desarrollado con ❤️ usando Kotlin y Jetpack Compose**
