# GameZone

Aplicación Android (Kotlin/Jetpack Compose) para la comunidad gamer GameZone. Permite registrar y autenticar usuarios, personalizar perfiles con géneros favoritos y avatar, consultar la ubicación actual y desplegar ofertas externas de videojuegos en tiempo real.

**Integrantes:** Camila Soto · Bastian Sandoval  
**Contexto:** Caso GAMEZONE

## Contenido
- [Funcionalidades principales](#funcionalidades-principales)
- [Arquitectura y stack](#arquitectura-y-stack)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Requisitos previos](#requisitos-previos)
- [Configuración del backend](#configuración-del-backend)
- [Configuración de la app Android](#configuración-de-la-app-android)
- [Solución de problemas comunes](#solución-de-problemas-comunes)
- [APIs y servicios externos](#apis-y-servicios-externos)
- [Buenas prácticas y calidad](#buenas-prácticas-y-calidad)
- [Referencias y enlaces](#referencias-y-enlaces)

## Funcionalidades principales
- Registro con validaciones en tiempo real (nombre completo, correo @duoc.cl único, contraseña + confirmación, teléfono opcional, selección de al menos un género gamer).
- Inicio de sesión con mensajes de error claros y opción "Recordar sesión" mediante DataStore.
- Perfil editable con cambio de avatar (cámara/galería) y sincronización de preferencias con backend REST.
- Uso de GPS para obtener la ubicación actual y presentarla en el home.
- Feed dinámico según gustos declarados y sección de **Ofertas gamer** consumida desde la API pública CheapShark.
- Persistencia local con Room para cachear usuarios y funcionar offline.
- Animaciones y transiciones que mejoran la experiencia de usuario.

## Arquitectura y stack
- **Patrón:** MVVM con separación de capas `data` / `domain` / `ui` por feature.
- **UI:** Jetpack Compose + Material 3 + Navigation Compose.
- **Data:** Retrofit + Gson, Room, DataStore Preferences, KSP para Room.
- **Servicios nativos:** Cámara/Galería para avatar, GPS para ubicación.
- **Backend companion:** Spring Boot (Kotlin).

## Estructura del repositorio
```
AppsMoviles/          → Proyecto Android (este repo)
backend/              → Proyecto Spring Boot 
```

## Requisitos previos
- Android Studio Hedgehog (o superior) / IntelliJ IDEA Ultimate con Android plugin.
- Dispositivo/emulador Android API 24+.
- JDK 17 (recomendado) para el backend y Android Studio.
- Maven 3.9+ (puedes usar el wrapper incluido `mvnw`).

## Configuración del backend
1. Copia la carpeta adjunta `backend/` junto al proyecto Android o clónala desde su repositorio dedicado.
2. Abre una terminal en `backend/` y ejecuta:
   - En Windows: `.\mvnw.cmd spring-boot:run`
   - En macOS/Linux: `./mvnw spring-boot:run`
3. El backend expone la API REST en `http://localhost:8080`. Usa 10.0.2.2 para acceder desde un emulador Android.
4. Configura la base de datos en `src/main/resources/application.properties`:
   - **H2 (por defecto):** el backend puede ejecutarse sin configuración extra utilizando la base en memoria incluida.
   - **MySQL local:**
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/gamezone
     spring.datasource.username=tu_usuario
     spring.datasource.password=tu_password
     spring.jpa.hibernate.ddl-auto=update
     spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
     ```
     Crea la base `gamezone` en tu servidor MySQL (`CREATE DATABASE gamezone;`) y ajusta usuario/contraseña según tu entorno. 
5. Credenciales por defecto o datos seed pueden configurarse en `application.properties` (ver backend).

> **Tip:** Si usas un dispositivo físico, reemplaza `localhost` por la IP de tu PC en la red y asegúrate de que ambos estén conectados a la misma red Wi‑Fi.

## Configuración de la app Android
1. Clona este repositorio: `git clone https://github.com/Bastiannsp/AppsMoviles.git`
2. Abre la carpeta `AppsMoviles/Gamezone` en Android Studio.
3. Sincroniza dependencias Gradle (se usa Kotlin 2.0.x, Compose BOM 2024.09+, Retrofit, Room, DataStore, Google Play Services Location, KSP, etc.).
4. Ajusta el endpoint del backend si fuese necesario en `app/src/main/java/com/example/gamezone/network/RetrofitClient.kt`:
   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:8080/" // emulador Android
   ```
   - Emulador: `10.0.2.2`
   - Dispositivo físico: `http://<IP_PC>:8080/`
5. Ejecuta la app en un emulador/dispositivo (Build Variants: debug). 

## Solución de problemas comunes
- **Android Studio no encuentra un JDK compatible:**
   1. Ve a `File > Settings > Build, Execution, Deployment > Build Tools > Gradle` (en macOS `Android Studio > Settings`).
   2. En "Gradle JDK" selecciona **Embedded JDK** o una instalación local de JDK 17.
   3. Si usas un JDK externo, verifica que `JAVA_HOME` apunte a la misma versión (por ejemplo, `C:\Program Files\Microsoft\jdk-17`).
   4. Sincroniza nuevamente el proyecto (`File > Sync Project with Gradle Files`).
- **Error "Unsupported class file major version" durante la compilación:** indica que Gradle está usando un JDK antiguo. Repite los pasos anteriores y asegúrate de reiniciar Android Studio si persiste.

## APIs y servicios externos
- **Backend propio GameZone:** registro, login, actualización de perfil (`/api/auth/*`, `/api/users/*`).
- **CheapShark Deals API:** consulta de ofertas gamer (`https://www.cheapshark.com/api/1.0/deals`). Consumida desde `GameDealsRepositoryImpl` y mostrada en la tarjeta “Ofertas gamer” del home.

## Buenas prácticas y calidad
- Validaciones compartidas en `util/` para correo, contraseña y géneros.
- Manejo de estado con `StateFlow`/`collectAsStateWithLifecycle`.
- Inyección manual via `AppViewModelProvider` y `AppContainer`.
- Cobertura de errores de red con mensajes visibles para el usuario.
- Build verificado con `./gradlew assembleDebug`.

## Referencias y enlaces
- Trello: https://trello.com/invite/b/68dc02ee7d300d2351012822/ATTI9f7256f14657d8dd6f0806e8ba66a33f3D2E5AC1/trabajo-kotlin
- GitHub: https://github.com/Bastiannsp/AppsMoviles
- Backend (adjunto): `backend/`
