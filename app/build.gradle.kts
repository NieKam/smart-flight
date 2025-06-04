plugins {
    id("com.android.application")
    id("kotlin-android")
}

import java.util.Properties
import java.io.FileInputStream

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    load(FileInputStream(keystorePropertiesFile))
}

val kotlin_version: String by rootProject.extra

android {
    signingConfigs {
        create("config") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    compileSdk = 35

    defaultConfig {
        applicationId = "kniezrec.com.flightinfo"
        minSdk = 21
        targetSdk = 35
        versionCode = 49
        versionName = "2.4.9"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("config")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

configurations.all {
    resolutionStrategy.force("com.google.code.findbugs:jsr305:1.3.9")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.0.3")
    implementation("org.osmdroid:osmdroid-android:5.6.5")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("org.slf4j:slf4j-simple:1.7.25")
    implementation("com.readystatesoftware.sqliteasset:sqliteassethelper:+")
    implementation("com.afollestad.material-dialogs:core:3.2.1")
    implementation("jp.wasabeef:blurry:4.0.0")
    implementation("com.airbnb.android:lottie:3.3.1")
    implementation(files("libs/trove4j-2.0.2.jar"))
    implementation(files("libs/jsi-1.0.0.jar"))

    // debug
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.8.1")

    // test
    testImplementation("junit:junit:4.13.2")
}
