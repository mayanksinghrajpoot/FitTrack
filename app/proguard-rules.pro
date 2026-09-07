# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to the flags specified
# in $ANDROID_HOME/tools/proguard/proguard-android.txt
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
