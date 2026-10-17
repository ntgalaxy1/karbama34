# Karbama — قواعد ProGuard
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.karbama.app.data.model.** { *; }
-dontwarn org.jetbrains.annotations.**
-dontwarn com.google.firebase.**
