# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class kotlin.** { *; }

# Keep your main classes
-keep class dev.bonygod.listacompra.** { *; }

# Keep serialization classes if you use them
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt