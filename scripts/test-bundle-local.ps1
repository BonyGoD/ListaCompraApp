Write-Host "========================================"
Write-Host "  Probar Bundle AAB Localmente"
Write-Host "========================================"
Write-Host ""

$bundlePath = "androidApp\build\outputs\bundle\release\androidApp-release.aab"
$outputApks = "androidApp\build\outputs\bundle\release\app.apks"
$keystorePath = "app-keystore.jks"
$keystorePassword = "Naiamatt16"
$keyAlias = "listacompra"
$keyPassword = "Naiamatt16"
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

# Verificar que el bundle existe
if (-not (Test-Path $bundlePath)) {
    Write-Host "Bundle no encontrado, generando..."
    .\gradlew androidApp:bundleRelease

    if (-not (Test-Path $bundlePath)) {
        Write-Host "ERROR: No se pudo generar el bundle"
        exit 1
    }
}

Write-Host "Bundle encontrado: $bundlePath"
$fileSize = [math]::Round((Get-Item $bundlePath).Length / 1MB, 2)
Write-Host "Tamanio: $fileSize MB"
Write-Host ""

# Verificar bundletool
$bundletoolPath = "bundletool.jar"
if (-not (Test-Path $bundletoolPath)) {
    Write-Host "Descargando bundletool..."
    $bundletoolUrl = "https://github.com/google/bundletool/releases/latest/download/bundletool-all.jar"
    try {
        Invoke-WebRequest -Uri $bundletoolUrl -OutFile $bundletoolPath -UseBasicParsing
        Write-Host "Bundletool descargado"
    } catch {
        Write-Host "ERROR descargando bundletool"
        Write-Host "Descarga manual desde: https://github.com/google/bundletool/releases"
        exit 1
    }
} else {
    Write-Host "Bundletool encontrado"
}

Write-Host ""
Write-Host "Generando APKs desde el bundle..."

# Eliminar APKs antiguos
if (Test-Path $outputApks) {
    Remove-Item $outputApks -Force
}

# Generar APKs desde el bundle
try {
    java -jar $bundletoolPath build-apks `
        --bundle=$bundlePath `
        --output=$outputApks `
        --ks=$keystorePath `
        --ks-pass=pass:$keystorePassword `
        --ks-key-alias=$keyAlias `
        --key-pass=pass:$keyPassword `
        --mode=universal

    Write-Host "APKs generados exitosamente"
} catch {
    Write-Host "ERROR generando APKs"
    Write-Host $_.Exception.Message
    exit 1
}

Write-Host ""
Write-Host "Verificando dispositivos conectados..."

$devices = & $adb devices | Select-String -Pattern "device$" | Measure-Object
if ($devices.Count -eq 0) {
    Write-Host ""
    Write-Host "No hay dispositivos conectados"
    Write-Host ""
    Write-Host "APKs generados en: $outputApks"
    Write-Host "Puedes instalarlos manualmente mas tarde con:"
    Write-Host "  java -jar bundletool.jar install-apks --apks=$outputApks"
    exit 0
}

Write-Host "Dispositivo encontrado"
Write-Host ""
Write-Host "Instalando APKs en el dispositivo..."

try {
    java -jar $bundletoolPath install-apks --apks=$outputApks
    Write-Host "========================================"
    Write-Host "  APP INSTALADA EXITOSAMENTE"
    Write-Host "========================================"
    Write-Host ""
    Write-Host "Esta es la version EXACTA que se instalara desde Play Store"
    Write-Host ""
    Write-Host "Ahora prueba:"
    Write-Host "1. Abre la app en tu dispositivo"
    Write-Host "2. Haz login con Google"
    Write-Host "3. Verifica que obtienes el email correctamente"
    Write-Host ""
} catch {
    Write-Host "ERROR instalando la app"
    Write-Host $_.Exception.Message
    exit 1
}

