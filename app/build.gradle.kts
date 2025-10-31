import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

// Load keystore properties from keystore.properties file
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.tiarkaerell.ibstracker"
    compileSdk = 35

    signingConfigs {
        create("release") {
            // Secure password loading: prioritizes environment variables over file
            // 1st priority: Environment variable
            // 2nd priority: keystore.properties (if not blank)
            // 3rd priority: Default/empty

            val propKeyAlias = keystoreProperties.getProperty("keyAlias")?.takeIf { it.isNotBlank() }
            val propKeyPassword = keystoreProperties.getProperty("keyPassword")?.takeIf { it.isNotBlank() }
            val propStoreFile = keystoreProperties.getProperty("storeFile")?.takeIf { it.isNotBlank() }
            val propStorePassword = keystoreProperties.getProperty("storePassword")?.takeIf { it.isNotBlank() }

            keyAlias = System.getenv("IBS_KEYSTORE_ALIAS")
                ?: propKeyAlias
                ?: "ibs-tracker-release"

            keyPassword = System.getenv("IBS_KEYSTORE_PASSWORD")
                ?: propKeyPassword
                ?: ""

            storeFile = rootProject.file(System.getenv("IBS_KEYSTORE_FILE")
                ?: propStoreFile
                ?: "app/ibs-tracker-production.jks")

            storePassword = System.getenv("IBS_KEYSTORE_PASSWORD")
                ?: propStorePassword
                ?: ""
        }
    }

    defaultConfig {
        applicationId = "com.tiarkaerell.ibstracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 23
        versionName = "1.13.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Room schema export for migration testing
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/INDEX.LIST"
        }
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/schemas")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager for scheduled backups
    implementation(libs.androidx.work.runtime.ktx)

    // Room testing - for migration tests
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    // WorkManager testing
    androidTestImplementation("androidx.work:work-testing:2.9.0")

    // Security - Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(libs.androidx.datastore.preferences)

    // Google Services
    implementation(libs.play.services.auth)
    implementation(libs.play.services.basement)
    implementation(libs.google.drive.api)
    implementation(libs.google.api.client.android)
    implementation(libs.google.http.client.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.play.services)

    // Google Auth Library for OAuth2 (for Drive API with access tokens)
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")

    // Credential Manager (for migration from GoogleSignIn)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}