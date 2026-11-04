import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

// ---------------------------------------------------------------------------
// Versionado (unica fuente de verdad: version.properties en la raiz del repo)
// ---------------------------------------------------------------------------
val appVersion = Properties().apply {
    val f = rootProject.file("version.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// ---------------------------------------------------------------------------
// Firma de release
// Prioridad: keystore.properties (local, gitignored) -> variables de entorno (CI).
// Si no hay credenciales el AAB se genera SIN firmar y Play lo rechaza.
// ---------------------------------------------------------------------------
val keystoreProperties = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun signingValue(key: String, env: String): String? =
    keystoreProperties.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: System.getenv(env)?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingValue("storeFile", "KOMANDA_KEYSTORE_FILE")
val releaseStorePassword = signingValue("storePassword", "KOMANDA_KEYSTORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "KOMANDA_KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "KOMANDA_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { it != null }

// URL base por defecto del backend. Override: -Pkomanda.baseUrl=... o KOMANDA_BASE_URL.
val defaultBaseUrl: String = providers.gradleProperty("komanda.baseUrl").orNull
    ?: System.getenv("KOMANDA_BASE_URL")
    ?: "https://kda.ar"

android {
    namespace = "com.komanda.kiosk"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.komanda.kiosk"
        minSdk = 24
        targetSdk = 37
        versionCode = appVersion.getProperty("versionCode").trim().toInt()
        versionName = appVersion.getProperty("versionName").trim()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DEFAULT_BASE_URL", "\"$defaultBaseUrl\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

if (!hasReleaseSigning) {
    logger.warn(
        "Komanda Kiosk: no hay credenciales de firma. El bundle de release se generara SIN FIRMAR. " +
            "Crea keystore.properties (ver README) o define KOMANDA_KEYSTORE_FILE/KOMANDA_KEYSTORE_PASSWORD/KOMANDA_KEY_ALIAS/KOMANDA_KEY_PASSWORD.",
    )
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
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
