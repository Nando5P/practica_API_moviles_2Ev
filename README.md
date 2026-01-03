# Gestión de Usuarios Híbrida (Offline-First) 📱🔄

Hola, soy **Fernando**. Este es mi proyecto para el módulo de Programación Multimedia. Se trata de una aplicación Android que resuelve un problema muy común: **¿cómo hacer que una app siga funcionando cuando no hay internet?**

En esta práctica se desarrolla una solución completa que permite gestionar usuarios en local y sincronizarlos con la nube automáticamente cuando recuperamos la conexión o mediante gestos.

---

## 🚀 ¿Qué hace la aplicación?

Lo más importante no es solo que muestra una lista de usuarios, sino cómo gestiona los datos por debajo:

* **Funcionamiento 100% Offline:** Puedes crear, editar y borrar usuarios en "Modo Avión". La app guarda todo en una base de datos local (Room) y no se queja.
* **Sincronización Inteligente:** La app sabe qué cambios has hecho mientras estabas desconectado y los sube al servidor (MockAPI) cuando sincronizas.
* **Sensor de Sacudida (Shake):** 📳 Si agitas el móvil con fuerza, la app detecta el movimiento con el acelerómetro y lanza la sincronización.
* **Usuarios de Prueba:** He añadido un botón para generar usuarios aleatorios rápidamente y facilitar las pruebas.

---

## 🛠️ Tecnologías que he utilizado

Para este proyecto he apostado por una arquitectura moderna basada en **Jetpack Compose** y **MVVM**:

* **Lenguaje:** Kotlin.
* **Interfaz (UI):** Jetpack Compose y Material Design 3.
* **Base de Datos Local:** Room (SQLite) para la persistencia.
* **Red:** Retrofit + Kotlin Serialization para conectar con la API.
* **Imágenes:** Coil 3 para cargar los avatares.
* **Sensores:** Uso del `SensorManager` para el acelerómetro.

---

## 🧠 El Reto: ¿Cómo funciona la Sincronización?

Esta ha sido la parte más compleja del desarrollo. Para lograr la arquitectura "Offline-First", he implementado la siguiente lógica en el `UserRepository`:

1.  **Banderas de Estado:** En la base de datos local, cada usuario tiene dos campos extra:
   * `pendingSync`: Si es `true`, sé que tengo que subir este usuario a la nube.
   * `pendingDelete`: Si borro un usuario sin internet, no lo elimino de la BD, solo lo marco con esta "flag" (borrado lógico) para acordarme de borrarlo del servidor luego.

2.  **Gestión de IDs:**
   * Cuando creo un usuario offline, le asigno un ID temporal que empieza por `local_`.
   * Al subirlo, el servidor le asigna un ID real y yo actualizo mi base de datos local reemplazando el ID temporal.

3.  **Prioridad:**
   * Primero subo mis cambios locales (para no perder nada).
   * Después descargo lo que haya nuevo en el servidor (estrategia Upsert).

---

## 📁 Estructura del Proyecto

He organizado el código siguiendo Clean Architecture para separar responsabilidades:

```text
app/src/main/java/com/example/gestionusuarioshibrido/
├── data/                    # Todo lo relacionado con datos
│   ├── local/               # Base de datos Room (DAO, Entidades)
│   ├── remote/              # API y Modelos remotos
│   └── UserRepository.kt    # El cerebro que decide si usar local o remoto
│
├── viewmodel/               # Lógica de presentación
│   └── UserViewModel.kt     # Comunica la UI con el Repositorio
│
├── ui/                      # Pantallas y Componentes visuales
│   ├── views/               # UserListScreen, UserFormScreen...
│   └── components/          # UserCard (la tarjeta de cada usuario)
│
└── sensors/                 # Lógica del acelerómetro
    ├── SensorShakeDetector.kt
    └── ShakeUserCoordinator.kt
```

---

## 🔍 Detalles de Implementación

### UserRepository
Aquí he centralizado toda la lógica híbrida. El resto de la app (el ViewModel y la UI) no saben si los datos vienen de internet o del móvil, solo piden datos y el repositorio se encarga de dárselos.

### Detección de Sacudida (`Sensors`)
Escucha el acelerómetro y calcula la fuerza G. Si supera un umbral de 1.3G (lo ajusté tras varias pruebas para que no fuera ni muy sensible ni muy duro), dispara el evento de sincronización en el ViewModel.

### Interfaz de Usuario
He usado `Scaffold` para la estructura básica y `LazyColumn` para la lista, lo que hace que la app sea muy fluida incluso con muchos usuarios. Los formularios validan que los campos no estén vacíos antes de permitir guardar.

---

## 🚀 Cómo probar la App

1.  **Instalación:** Clona el repo y abre el proyecto en Android Studio. Sincroniza Gradle.
2.  **API:** Estoy usando MockAPI.io. La URL ya está configurada en `AppDataContainer.kt`.
3.  **Prueba Offline:**
    * Pon el móvil/emulador en **Modo Avión**.
    * Crea un usuario nuevo. Verás que aparece en la lista con un icono de una nube tachada (🔴).
    * Conecta internet de nuevo.
    * Dale al botón de sincronizar en la barra superior (o agita el móvil).
    * La nube desaparecerá y el usuario se habrá subido al servidor correctamente.

---

### 👨‍💻 Autor

**Fernando Parga Fernandez** - [Nando5P](https://github.com/Nando5P)

*Práctica desarrollada para el módulo de Desarrollo de Aplicaciones Móviles (2ª Evaluación).*