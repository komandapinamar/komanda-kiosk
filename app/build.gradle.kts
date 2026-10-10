plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.komanda.kiosk"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.komanda.kiosk"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("release-key.jks")
            val storePass = providers.gradleProperty("KEYSTORE_PASSWORD")
                .orElse(providers.environmentVariable("KEYSTORE_PASSWORD")).orNull
            val alias = providers.gradleProperty("KEY_ALIAS")
                .orElse(providers.environmentVariable("KEY_ALIAS")).orNull
            val keyPass = providers.gradleProperty("KEY_PASSWORD")
                .orElse(providers.environmentVariable("KEY_PASSWORD")).orNull
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.configureEach {
    if (name == "assembleRelease" || name == "bundleRelease") {
        doFirst {
            val keystoreFile = file("release-key.jks")
            val hasStorePassword = providers.gradleProperty("KEYSTORE_PASSWORD").isPresent ||
                providers.environmentVariable("KEYSTORE_PASSWORD").isPresent
            val hasAlias = providers.gradleProperty("KEY_ALIAS").isPresent ||
                providers.environmentVariable("KEY_ALIAS").isPresent
            val hasKeyPassword = providers.gradleProperty("KEY_PASSWORD").isPresent ||
                providers.environmentVariable("KEY_PASSWORD").isPresent
            if (!keystoreFile.exists() || !hasStorePassword || !hasAlias || !hasKeyPassword) {
                throw GradleException("Release signing requires app/release-key.jks and KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD.")
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.security.crypto)
    implementation(libs.coil.compose)
    implementation(libs.converter.moshi)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.logging.interceptor)
    implementation(libs.material)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.escpos.thermalprinter)
    implementation(libs.zxing.core)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
    "ksp"(libs.moshi.kotlin.codegen)
}
