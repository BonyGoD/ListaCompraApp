Write-Host "========================================"
Write-Host "  Instalar APK Release Localmente"
Write-Host "========================================"
Write-Host ""

$apkPath = "androidApp\build\outputs\apk\release\androidApp-release.apk"
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

if (-not (Test-Path $adb)) {
    Write-Host "ERROR: adb no encontrado en: $adb"
    Write-Host "Asegurate de tener Android SDK instalado"
    exit 1
}

if (-not (Test-Path $apkPath)) {
    Write-Host "APK no encontrado, generando..."
    .\gradlew androidApp:assembleRelease

    if (-not (Test-Path $apkPath)) {
        Write-Host "ERROR: No se pudo generar el APK"
        exit 1
    }
}

Write-Host "APK encontrado: $apkPath"
$fileSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
Write-Host "Tamanio: $fileSize MB"
Write-Host ""

Write-Host "Verificando dispositivos conectados..."
$devicesOutput = & $adb devices
Write-Host $devicesOutput

$devices = $devicesOutput | Select-String -Pattern "device$" | Measure-Object
if ($devices.Count -eq 0) {
    Write-Host ""
    Write-Host "No hay dispositivos conectados"
    Write-Host ""
    Write-Host "Conecta un dispositivo o inicia un emulador"
    exit 0
}

Write-Host "Dispositivo encontrado"
Write-Host ""

Write-Host "Desinstalando version anterior..."
& $adb uninstall dev.bonygod.listacompra 2>$null | Out-Null
Write-Host "Listo"
Write-Host ""

Write-Host "Instalando APK..."
$installOutput = & $adb install -r $apkPath 2>&1

if ($installOutput -match "Success") {
    Write-Host "========================================"
    Write-Host "  APP INSTALADA EXITOSAMENTE"
    Write-Host "========================================"
    Write-Host ""
    Write-Host "Ahora prueba:"
    Write-Host "1. Abre la app en tu dispositivo"
    Write-Host "2. Haz login con Google"
    Write-Host "3. Verifica que obtienes el email correctamente"
    Write-Host ""
} else {
    Write-Host "ERROR instalando la app"
    Write-Host $installOutput
    exit 1
}

