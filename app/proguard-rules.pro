# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve classes e métodos do modelo utilizado
-keep class com.mobile.berp.BerpPOSMobile.model.** { *; }

# Preserve classes e métodos do Proxy
-keep class com.mobile.berp.BerpPOSMobile.Controller.Proxy { *; }


# Preserve atributos que podem ser removidos
-keepattributes InnerClasses, EnclosingMethod
# Mantém todas as classes do PagSeguro
-keep class br.com.pagseguro.** { *; }
-keep class com.pagseguro.** { *; }

# Preserve a estrutura de classes usadas em View
-keep class * extends android.view.View { *; }

# Se sua aplicação usar JavaScript em WebView
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Evita avisos relacionados ao Parcelize
-dontwarn kotlinx.android.parcel.Parcelize

