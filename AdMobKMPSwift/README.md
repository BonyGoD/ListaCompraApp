# AdMobKMPSwift

Swift Package para integrar Google Mobile Ads en Kotlin Multiplatform.

## Instalación

### En tu proyecto Xcode:

1. File → Add Package Dependencies
2. Agrega este paquete local o desde repositorio
3. Selecciona `AdMobKMPSwift`

## Uso

### En AppDelegate:

```swift
import AdMobKMPSwift
import GoogleMobileAds

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        
        // Inicializar Google Mobile Ads
        GADMobileAds.sharedInstance().start(completionHandler: nil)
        
        // Inicializar el helper para comunicación con Kotlin
        _ = AdMobCallbackHelper.shared
        
        return true
    }
}
```

## Arquitectura

Este paquete actúa como un **bridge** entre Kotlin/Native y Google Mobile Ads SDK:

- **Kotlin** envía notificaciones solicitando acciones (cargar/mostrar anuncios)
- **Swift** ejecuta las llamadas nativas de AdMob
- **Swift** envía notificaciones con los resultados
- **Kotlin** recibe y procesa los resultados

### Notificaciones soportadas:

#### Kotlin → Swift (Requests):
- `AdMobLoadBannerRequested`
- `AdMobLoadInterstitialRequested`
- `AdMobShowInterstitialRequested`

#### Swift → Kotlin (Responses):
- `AdMobBannerReadyToLoad`
- `AdMobInterstitialLoaded`
- `AdMobInterstitialLoadFailed`
- `AdMobInterstitialShown`
- `AdMobInterstitialDismissed`
- `AdMobInterstitialShowFailed`

## Componentes

### AdMobCallbackHelper
Singleton que escucha notificaciones desde Kotlin y coordina las llamadas a AdMob.

### AdMobInterstitialBridge
Maneja la carga y presentación de anuncios intersticiales.

### AdMobBannerView
UIView wrapper para GADBannerView, listo para usar desde Kotlin con UIKitView.

## Licencia

MIT
