# Integración de AdMob Banner en iOS - Pendiente

## Estado Actual

✅ **Interstitial Ad**: Funciona correctamente
⚠️ **Banner Ad**: Implementación temporal con placeholder

## Problema

El `BannerAd` en iOS actualmente muestra un placeholder gris porque la integración con el paquete Swift `AdMobKMPSwift` requiere configuración adicional.

## Solución Requerida

Para que el banner funcione correctamente en iOS, necesitas hacer UNA de estas dos opciones:

### Opción 1: Usar CocoaPods (Recomendado)

1. Agregar el plugin de CocoaPods al `build.gradle.kts`:

```kotlin
plugins {
    // ... otros plugins
    kotlin("native.cocoapods")
}

kotlin {
    // ... configuración existente
    
    cocoapods {
        summary = "Lista Compra App"
        homepage = "https://example.com"
        ios.deploymentTarget = "14.0"
        
        pod("Google-Mobile-Ads-SDK")
        
        // Vincular el paquete Swift local
        pod("AdMobKMPSwift") {
            source = path(project.file("../AdMobKMPSwift"))
        }
    }
}
```

2. Luego ejecutar:
```bash
./gradlew podInstall
```

### Opción 2: Usar Swift Package Manager

1. En Xcode, agregar el paquete Swift local `AdMobKMPSwift` al proyecto iOS
2. Asegurarse de que el framework esté disponible para Kotlin/Native
3. Actualizar `AdComponents.ios.kt` para importar correctamente:

```kotlin
import platform.AdMobKMPSwift.AdMobBannerView

@Composable
actual fun BannerAd(
    modifier: Modifier,
    adUnitId: String,
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: (String) -> Unit
) {
    UIKitView(
        modifier = modifier,
        factory = {
            AdMobBannerView(
                adUnitId = adUnitId,
                onAdLoaded = { onAdLoaded() },
                onAdFailed = { error -> onAdFailedToLoad(error) }
            )
        }
    )
}
```

## Mientras tanto

El intersticial funciona correctamente, así que puedes seguir probando esa funcionalidad mientras decides qué enfoque usar para el banner.
