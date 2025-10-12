// 1. ADD THIS BLOCK AT THE VERY TOP to read local.properties
// import androidx.glance.appwidget.compose
// import androidx.navigation.compose.navigation
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.bloghub"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bloghub"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 2. ADD buildFeatures and MODIFY buildTypes
    buildFeatures {
        compose = true
        buildConfig = true // This enables the BuildConfig file generation
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // It's good practice to add for release builds too,
            // using environment variables in a CI/CD pipeline.
            // For now, you can leave them empty or copy the debug ones.
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"\"")
        }
        debug {
            // This makes the properties from local.properties available in your debug build
            buildConfigField(
                "String",
                "CLOUDINARY_CLOUD_NAME",
                "\"${localProperties.getProperty("CLOUDINARY_CLOUD_NAME")}\""
            )
            buildConfigField(
                "String",
                "CLOUDINARY_API_KEY",
                "\"${localProperties.getProperty("CLOUDINARY_API_KEY")}\""
            )
            buildConfigField(
                "String",
                "CLOUDINARY_API_SECRET",
                "\"${localProperties.getProperty("CLOUDINARY_API_SECRET")}\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // The compose buildFeature is moved up into the other buildFeatures block
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(libs.accompanist.systemuicontroller)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    // Use explicit versions to avoid BoM resolution issues
    // Remove or re-add BoM later if desired
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.6.0")

    // For sharing ViewModels between composables in a navigation graph
    implementation("androidx.navigation:navigation-compose:2.8.0-beta01") // Or the latest version

    implementation("com.cloudinary:cloudinary-android:2.4.0")

    //ADD THIS LINE FOR THE PHOTO PICKER
    implementation("androidx.activity:activity-ktx:1.9.0")

}

// Apply the Google services plugin at the end of the file
apply(plugin = "com.google.gms.google-services")
