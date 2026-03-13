import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.carwash"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = rootDir.resolve("local.properties")
    if (localPropertiesFile.exists()) {    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
    }

    signingConfigs {
        create("release") {
            storeFile = rootDir.resolve(localProperties.getProperty("KEYSTORE_PATH", ""))
            storePassword = localProperties.getProperty("KEYSTORE_PASSWORD")
            keyAlias = localProperties.getProperty("KEY_ALIAS")
            keyPassword = localProperties.getProperty("KEY_PASSWORD")
        }
    }

    defaultConfig {
        applicationId = "com.carwash.manager"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProperties.getProperty("SUPABASE_KEY")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.exifinterface)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.okhttp)

    // Coil
    implementation(libs.coil.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.camerax.compose)

    // Accompanist
    implementation(libs.accompanist.permissions)

}