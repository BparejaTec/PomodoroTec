plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.bpareja.pomodorotec"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bpareja.pomodorotec"
        minSdk = 33
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Jetpack Compose*

    implementation ("androidx.compose.ui:ui:1.7.5")

    implementation ("androidx.compose.material3:material3:1.3.1")

    implementation ("androidx.activity:activity-compose:1.9.3")

    // WorkManager para tareas en segundo plano*

    implementation ("androidx.work:work-runtime-ktx:2.10.0")

    // Core KTX*

    implementation ("androidx.core:core-ktx:1.15.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Jetpack Glance
    implementation("androidx.glance:glance:1.1.1")
    // For AppWidgets support
    implementation ("androidx.glance:glance-appwidget:1.1.1")

    // For interop APIs with Material 3
    implementation ("androidx.glance:glance-material3:1.1.1")

    // For interop APIs with Material 2
    implementation ("androidx.glance:glance-material:1.1.1")
}