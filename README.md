# 🛒 Lista de Compra App

Una aplicación multiplataforma de lista de la compra desarrollada con **Kotlin Multiplatform** y **Jetpack Compose Multiplatform**, integrada con **Firebase Firestore** para sincronización en tiempo real.

## 🎙️ Ecosistema Completo con Alexa

Esta aplicación forma parte de un **ecosistema completo** que incluye:

- 🗣️ **Skill de Alexa personalizada** para agregar productos por voz
- 🔌 **API REST en Node.js** que procesa los comandos de Alexa  
- 🔥 **Firebase Firestore** como base de datos central
- 📱 **Esta app multiplataforma** para visualizar y gestionar la lista

**Flujo de trabajo:**
1. El usuario dice: *"Alexa, añade leche a mi lista de la compra"*
2. La skill de Alexa procesa el comando y envía los datos a la API de Node.js
3. La API almacena el producto en Firebase Firestore
4. Esta aplicación sincroniza automáticamente y muestra el nuevo producto en tiempo real

## 🌟 Características

- ✅ **Multiplataforma**: Android, iOS y Desktop
- ✅ **Tiempo real**: Sincronización instantánea con Firebase Firestore
- ✅ **Arquitectura limpia**: Implementación de MVI con separación de capas
- ✅ **Inyección de dependencias**: Configuración completa con Koin
- ✅ **UI moderna**: Jetpack Compose con Material Design 3
- ✅ **Gestión de estado**: StateFlow y Flow para reactividad

## 🏗️ Arquitectura

El proyecto sigue los principios de **Arquitectura Limpia** con patrón **MVI**:

```
📁 commonMain/
├── 📁 data/           # Capa de datos
│   ├── 📁 network/    # Servicios de red (Firestore)
│   ├── 📁 repository/ # Repositorios
│   └── 📁 mapper/     # Mappers entre capas
├── 📁 domain/         # Capa de dominio
│   ├── 📁 model/      # Entidades de negocio
│   └── 📁 usecase/    # Casos de uso
├── 📁 ui/             # Capa de presentación
│   ├── 📁 model/      # Modelos de UI
│   ├── 📁 screens/    # Pantallas
│   └── 📁 composables/ # Componentes reutilizables
├── 📁 core/           # Configuración y utilidades
└── 📁 util/           # Extensiones y helpers
```

## 🛠️ Stack Tecnológico

### Core
- **Kotlin Multiplatform** 2.2.0
- **Jetpack Compose Multiplatform** 1.8.2
- **Material Design 3**

### Base de datos y sincronización
- **Firebase Firestore** con **GitLive** 2.3.0
- **Kotlinx Serialization** para JSON

### Arquitectura y patrones
- **Koin** 4.0.2 - Inyección de dependencias
- **MVI Pattern** - Gestión de estado unidireccional
- **Repository Pattern** - Abstracción de datos
- **Use Cases** - Lógica de negocio

### Utilidades
- **Kotlinx DateTime** 0.6.0 - Manejo de fechas
- **Kotlinx Coroutines** 1.10.2 - Programación asíncrona

## 🚀 Configuración del proyecto

### Prerrequisitos

- **JDK 17** o superior
- **Android Studio** Iguana o más reciente
- **Xcode** 15+ (para desarrollo iOS)
- **Firebase Project** configurado

### 1. Clonación del repositorio

```bash
git clone https://github.com/BonyGoD/lista-compra-app.git
cd lista-compra-app
```

### 2. Configuración de Firebase

#### 2.1 Crear archivo local.properties

Crea un archivo `local.properties` en la raíz del proyecto:

```properties
# Firebase Configuration
FIREBASE_PROJECT_ID=tu-proyecto-id
FIREBASE_APP_ID=tu-app-id
FIREBASE_API_KEY=tu-api-key
```

#### 2.2 Configuración Android
1. Descarga `google-services.json` desde Firebase Console
2. Colócalo en `composeApp/google-services.json`

#### 2.3 Configuración iOS
1. Descarga `GoogleService-Info.plist` desde Firebase Console  
2. Añádelo al proyecto Xcode en `iosApp/iosApp/`

### 3. Instalación de dependencias

```bash
./gradlew build
```

## 🏃 Ejecución

### Android
```bash
./gradlew :composeApp:installDebug
```

### Desktop
```bash
./gradlew :composeApp:run
```

### iOS
1. Abre `iosApp/iosApp.xcodeproj` en Xcode
2. Selecciona un simulador o dispositivo
3. Ejecuta el proyecto

## 📱 Funcionalidades

### Gestión de Productos
- ✅ **Listar productos** desde Firestore
- ✅ **Agregar productos** a la lista desde Alexa
- ✅ **Sincronización automática** entre dispositivos

### Interfaz de Usuario
- ✅ **Material Design 3** con tema dinámico
- ✅ **Responsive UI** adaptable a diferentes pantallas
- ✅ **Animaciones suaves** y transiciones
- ✅ **Estados de carga** y manejo de errores

## 🔒 Seguridad

- 🔐 **Variables sensibles** en `local.properties` (excluido del repositorio)
- 🔐 **Reglas de seguridad** de Firestore configuradas
- 🔐 **Validación de datos** en cliente y servidor

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit de cambios (`git commit -m 'feat: agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

### Convenciones de código
- **Kotlin Coding Conventions**
- **Commits semánticos** (feat, fix, docs, style, refactor, test, chore)
- **Arquitectura limpia** y separación de responsabilidades

## 📄 Licencia

Este proyecto es **software propietario y confidencial**. Todos los derechos reservados.

Ver el archivo [LICENSE.md](./LICENSE.md) para más detalles sobre los términos de uso.

## 👨‍💻 Autor

**BonyGoD**
- GitHub: [@BonyGoD](https://github.com/BonyGoD)

## 🔗 Enlaces útiles

- [Kotlin Multiplatform](https://kotlinlang.org/multiplatform/)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Firebase Firestore](https://firebase.google.com/docs/firestore)
- [GitLive Firebase](https://github.com/GitLiveApp/firebase-kotlin-sdk)
- [Koin](https://insert-koin.io/)

---

⭐ Si te gusta este proyecto, ¡dale una estrella en GitHub!
