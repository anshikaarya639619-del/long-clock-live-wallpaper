plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
}
dependencies { implementation("androidx.activity:activity-ktx:1.9.3") }
android {
 namespace = "com.suraj.depthpixpro"
 compileSdk = 35
 defaultConfig { applicationId = "com.suraj.depthpixpro"; minSdk = 23; targetSdk = 35; versionCode = 1; versionName = "1.0.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
