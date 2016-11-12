##--------------- Default Settings  ----------
-optimizationpasses 10
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-dontnote
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class com.android.vending.licensing.ILicensingService
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class * extends android.app.Activity{
    public void *(android.view.View);
}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keep class * extends android.webkit.WebViewClient
-keepclassmembers class * extends android.webkit.WebViewClient {
    <methods>;
}

-keepattributes Signature

-keep public class **.R

-dontwarn java.**
-dontnote java.**
-keep class java.** { *; }

-dontwarn javax.**
-dontnote javax.**
-keep class javax.** { *; }

-dontwarn org.**
-dontnote org.**
-keep class org.** { *; }

-dontwarn android.**
-dontnote android.**
-keep class android.** { *; }

-dontwarn com.android.**
-dontnote com.android.**
-keep class com.android.** { *; }

-dontwarn com.google.**
-dontnote com.google.**
-keep class com.google.** { *; }

##---------------End: Default Settings  ----------

#------------- RxJava ------------
-dontwarn rx.**
-dontnote rx.**
-keep class rx.**  { *; }

-dontwarn sun.misc.Unsafe
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
   long producerNode;
   long consumerNode;
}

#------------- Twitter4j -----------
-dontwarn twitter4j.**
-dontnote twitter4j.**
-keep class twitter4j.**  { *; }