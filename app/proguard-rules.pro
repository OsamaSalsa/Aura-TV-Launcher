# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep WebView @JavascriptInterface bridges across R8 release minification
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.example.ui.home.ScreensaverVideoBridge {
    *;
}

# Preserve source file and line numbers for readable release crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
