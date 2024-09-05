
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
}


android {
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
    //android.buildFeatures.buildConfig=true

    compileSdk = 35

    defaultConfig {
        applicationId = "com.peecock.ymusic"
        minSdk = 21
        targetSdk = 35
        versionCode = 4
        versionName = "4.8"
    }

    splits {
        abi {
            reset()
            isUniversalApk = true
        }
    }

    namespace = "com.peecock.ymusic"

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appName"] = "YMusic-Debug"
        }

        release {
            vcsInfo.include = false
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["appName"] = "YMusic"
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("foss") {
            dimension = "version"
        }
    }
    productFlavors {
        create("accrescent") {
            dimension = "version"
            manifestPlaceholders["appName"] = "YMusic-Acc"
        }
    }

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }



    compileOptions {
        isCoreLibraryDesugaringEnabled = true
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

    androidResources {
        generateLocaleConfig = true
    }



}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

/*
android {
    lint {
        baseline = file("lint-baseline.xml")
        //checkReleaseBuilds = false
    }
}
*/

dependencies {
    implementation(projects.composePersist)
    implementation(projects.composeRouting)
    implementation(projects.composeReordering)
    implementation(libs.compose.activity)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ripple)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.coil)
    implementation(libs.palette)
    implementation(libs.media3.exoplayer)
    //implementation(libs.media3.exoplayer.ui)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.appcompat)
    implementation(libs.appcompat.resources)
    implementation(libs.core.splashscreen)
    implementation(libs.media)
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.compose.ui.graphics.android)
    implementation(libs.constraintlayout)
    implementation(libs.runtime.livedata)
    implementation(libs.core.ktx)
    implementation(libs.compose.animation)
    implementation(libs.translator)
    implementation(libs.kotlin.csv)
    implementation(libs.monetcompat)
    implementation(libs.androidmaterial)
    implementation(libs.navigation)
    implementation(libs.timber)
    implementation(libs.crypto)
    implementation(libs.logging.interceptor)
    implementation(libs.math3)
    implementation(libs.toasty)
    implementation(libs.haze)

    implementation(libs.room)
    ksp(libs.room.compiler)

    implementation(projects.innertube)
    implementation(projects.innertubes)
    implementation(projects.kugou)
    implementation(projects.lrclib)
    implementation(projects.piped)


    coreLibraryDesugaring(libs.desugaring)
}
