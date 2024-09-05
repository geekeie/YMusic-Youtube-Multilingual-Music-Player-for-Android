
plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.kotlin.compose)
}

/*
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    //alias(libs.plugins.kotlin.compose)
}
 */

android {
    namespace = "com.peecock.compose.routing"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        //targetSdk = 34
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.compose.activity)
    implementation(libs.compose.foundation)
}
