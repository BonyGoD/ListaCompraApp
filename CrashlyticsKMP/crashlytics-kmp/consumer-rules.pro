# Crashlytics necesita fichero y línea para simbolizar los stack traces.
# `-renamesourcefileattribute` NO puede ir aquí: es una opción GLOBAL de R8 y AGP prohíbe
# declararla en un consumerProguardFile (afectaría al build completo de la app consumidora).
# Solo puede declararla la app consumidora en su propio fichero de reglas (ver README.md).
-keepattributes SourceFile,LineNumberTable
