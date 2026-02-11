# Script de verificación de ProGuard
# Verifica que las clases críticas de Google Sign-In estén presentes en el APK/AAB

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Verificación ProGuard - Google Sign-In" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "1️⃣ Limpiando proyecto..." -ForegroundColor Yellow
.\gradlew clean | Out-Null

Write-Host "2️⃣ Generando APK de release..." -ForegroundColor Yellow
.\gradlew androidApp:assembleRelease

Write-Host ""
Write-Host "3️⃣ Verificando clases en el APK..." -ForegroundColor Yellow
Write-Host ""

$apkPath = "androidApp\build\outputs\apk\release\androidApp-release.apk"

if (Test-Path $apkPath) {
    Write-Host "✅ APK encontrado: $apkPath" -ForegroundColor Green
    Write-Host ""

    # Extraer mapping.txt para ver qué se ofuscó
    $mappingPath = "androidApp\build\outputs\mapping\release\mapping.txt"
    if (Test-Path $mappingPath) {
        Write-Host "📄 Archivo de mapping encontrado" -ForegroundColor Green
        Write-Host ""

        # Verificar clases críticas en el mapping
        Write-Host "🔍 Verificando clases críticas..." -ForegroundColor Cyan

        $criticalClasses = @(
            "androidx.credentials.GetCredentialRequest",
            "com.google.android.libraries.identity.googleid.GoogleIdTokenCredential",
            "com.google.firebase.auth.GoogleAuthProvider",
            "dev.bonygod.googlesignin.kmp"
        )

        foreach ($class in $criticalClasses) {
            $found = Select-String -Path $mappingPath -Pattern $class -Quiet
            if ($found) {
                Write-Host "  ✅ $class - PRESENTE" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️  $class - NO ENCONTRADO (puede estar ofuscado)" -ForegroundColor Yellow
            }
        }

        Write-Host ""
        Write-Host "📝 Revisa el archivo completo:" -ForegroundColor Cyan
        Write-Host "   $mappingPath" -ForegroundColor Gray
    } else {
        Write-Host "⚠️  Archivo de mapping NO encontrado" -ForegroundColor Yellow
        Write-Host "   Esto puede significar que ProGuard no se ejecutó correctamente" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Próximos Pasos:" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Si todas las clases están PRESENTES:" -ForegroundColor White
    Write-Host "   .\gradlew androidApp:bundleRelease" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. Instala el APK en un dispositivo de prueba:" -ForegroundColor White
    Write-Host "   adb install -r $apkPath" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. Prueba el login con Google" -ForegroundColor White
    Write-Host ""

} else {
    Write-Host "❌ APK NO encontrado en: $apkPath" -ForegroundColor Red
    Write-Host "   El build falló. Revisa los errores arriba." -ForegroundColor Gray
}

Write-Host ""

