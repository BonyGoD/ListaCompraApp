# Sistema de Anuncios KMP (AdMob)

Este módulo proporciona una implementación multiplataforma (Android e iOS) para mostrar anuncios de AdMob en tu aplicación.

## 📦 Estructura

```
ads/
├── AdManager.kt           # Interfaz expect para gestión de anuncios
├── AdConstants.kt         # IDs de anuncios de prueba y producción
├── ui/
│   └── AdComponents.kt    # Componentes Composable (BannerAd, InterstitialAdTrigger)
```

## 🚀 Configuración

### Android

1. **Agregar Application ID en AndroidManifest.xml** (ya configurado):
```xml
<manifest>
    <application>
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713"/>
    </application>
</manifest>
```

2. **Inicializar AdMob** (ya configurado en `ListaCompraApp.kt`):
```kotlin
// Ya está inicializado automáticamente en Application
MobileAds.initialize(this) {}
```

### iOS

Esta implementación sigue el **mismo patrón que GoogleSignInKMP**, usando un Swift Package como bridge.

1. **Agregar Application ID en Info.plist** (ya configurado):
```xml
<key>GADApplicationIdentifier</key>
<string>ca-app-pub-3940256099942544~1458002511</string>
```

2. **Agregar permisos de ATT** (ya configurado):
```xml
<key>NSUserTrackingUsageDescription</key>
<string>Esta app usa anuncios personalizados para ofrecerte una mejor experiencia</string>
```

3. **Agregar el paquete Swift en Xcode**:
   - Abre el proyecto `.xcodeproj` en Xcode
   - File → Add Package Dependencies
   - Selecciona `AdMobKMPSwift` (local package en la raíz del proyecto)
   - Este paquete maneja la comunicación entre Kotlin y AdMob usando NotificationCenter

4. **Inicialización** (ya configurado en `iOSApp.swift`):
```swift
import AdMobKMPSwift
import GoogleMobileAds

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(...) -> Bool {
        // Inicializar AdMob
        GADMobileAds.sharedInstance().start(completionHandler: nil)
        
        // Inicializar helper de comunicación
        _ = AdMobCallbackHelper.shared
        
        return true
    }
}
```

### Arquitectura iOS (igual que GoogleSignInKMP)

```
┌─────────────┐                    ┌──────────────┐
│   Kotlin    │◄──NotificationCenter──►│    Swift     │
│  (Common)   │                    │   Package    │
└─────────────┘                    └──────────────┘
      │                                    │
      │                                    │
      ▼                                    ▼
  expect/actual                    Google Mobile Ads
  @Composable                          SDK (iOS)
```

**No necesitas CocoaPods ni CInterop** - La comunicación se hace mediante NotificationCenter, igual que en GoogleSignIn.

## 💡 Uso de Componentes

### 1. Banner Ad

Muestra un banner publicitario en cualquier parte de tu UI:

```kotlin
import dev.bonygod.listacompra.ads.ui.BannerAd
import dev.bonygod.listacompra.ads.AdConstants

@Composable
fun MyScreen() {
    Column {
        Text("Mi contenido")
        
        // Banner al final de la pantalla
        BannerAd(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            adUnitId = AdConstants.getBannerAdUnitId(), // o _IOS
            onAdLoaded = {
                println("✅ Banner cargado")
            },
            onAdFailedToLoad = { error ->
                println("❌ Error al cargar banner: $error")
            }
        )
    }
}
```

### 2. Interstitial Ad

Muestra un anuncio a pantalla completa cuando el usuario realiza una acción:

```kotlin
import dev.bonygod.listacompra.ads.ui.InterstitialAdTrigger
import dev.bonygod.listacompra.ads.AdConstants

@Composable
fun MyScreen() {
    InterstitialAdTrigger(
        adUnitId = AdConstants.getInterstitialAdUnitId(), // o _IOS
        onAdShown = {
            println("📺 Anuncio mostrado")
        },
        onAdDismissed = {
            println("👋 Usuario cerró el anuncio")
            // Continuar con la lógica de tu app
        },
        onAdFailedToShow = { error ->
            println("❌ Error: $error")
        }
    ) { showAd ->
        // Tu UI que disparará el anuncio
        Button(
            onClick = { showAd() }
        ) {
            Text("Ver anuncio y continuar")
        }
    }
}
```

### Ejemplo Completo

```kotlin
@Composable
fun ProductDetailScreen(
    onNavigateNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Contenido principal
        Text("Detalles del producto", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.weight(1f))
        
        // Banner en la parte inferior
        BannerAd(
            modifier = Modifier.fillMaxWidth(),
            adUnitId = AdConstants.TEST_BANNER_AD_UNIT_ID_ANDROID,
            onAdLoaded = { println("Banner cargado") },
            onAdFailedToLoad = { error -> println("Error: $error") }
        )
        
        // Botón que muestra intersticial
        InterstitialAdTrigger(
            adUnitId = AdConstants.TEST_INTERSTITIAL_AD_UNIT_ID_ANDROID,
            onAdDismissed = { onNavigateNext() }
        ) { showAd ->
            Button(
                onClick = { showAd() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Continuar")
            }
        }
    }
}
```

## 🧪 IDs de Prueba y Producción

### IDs de Prueba (Google)
Los IDs de prueba de AdMob están disponibles en `AdConstants.kt` para desarrollo:

- **Android Banner**: `ca-app-pub-3940256099942544/6300978111`
- **Android Interstitial**: `ca-app-pub-3940256099942544/1033173712`
- **iOS Banner**: `ca-app-pub-3940256099942544/2934735716`
- **iOS Interstitial**: `ca-app-pub-3940256099942544/4411468910`

### IDs de Producción (ListaCompra App)
✅ **Ya configurados** en `local.properties` (protegidos, NO se suben a Git):

Los IDs de producción se leen desde `local.properties` y se inyectan via BuildConfig.
Para ver cómo configurarlos, revisa el archivo `NUEVOS_IDS_ADMOB.md` en la raíz del proyecto.

⚠️ **NUNCA pongas tus IDs reales aquí en el código fuente.**

### Cambiar entre Prueba y Producción

En `AdConstants.kt` hay un switch:
```kotlin
private const val USE_TEST_ADS = false  // false = producción, true = prueba
```

- **Para desarrollo/testing**: Cambia a `true`
- **Para publicar**: Déjalo en `false` (por defecto)

⚠️ **IMPORTANTE**: Nunca publiques una app en producción con IDs de prueba. Google puede suspender tu cuenta de AdMob.

## 📝 Obtener tus IDs de Producción

1. Ve a [AdMob Console](https://apps.admob.com/)
2. Crea tu app (Android/iOS)
3. Crea Ad Units (Banner, Interstitial, etc.)
4. Copia los IDs y agrégalos a `AdConstants.kt`:

```kotlin
object AdConstants {
    const val PROD_BANNER_AD_UNIT_ID_ANDROID = "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
    const val PROD_INTERSTITIAL_AD_UNIT_ID_ANDROID = "ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ"
    // ... etc
}
```

## 🎯 Buenas Prácticas

1. **No satures de anuncios**: Los usuarios abandonan apps con demasiados anuncios
2. **Timing adecuado**: Muestra intersticiales en transiciones naturales
3. **Maneja errores**: Los anuncios pueden fallar, ten un plan B
4. **Precarga**: `InterstitialAdTrigger` precarga automáticamente el siguiente anuncio
5. **Respeta privacidad**: Implementa consentimiento GDPR/CCPA si es necesario

## 🔄 Migración a Librería

Cuando quieras extraer esto a una librería independiente:

1. Copia toda la carpeta `ads/` a tu nuevo proyecto de librería
2. Mantén la misma estructura de paquetes
3. Publica en JitPack o Maven Central
4. Usa como dependencia: `implementation("com.github.TuUsuario:AdLibraryKMP:x.x.x")`

## 📚 Referencias

- [AdMob Android Documentation](https://developers.google.com/admob/android)
- [AdMob iOS Documentation](https://developers.google.com/admob/ios)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
