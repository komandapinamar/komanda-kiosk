# Komanda Kiosk

Aplicación Android nativa de **autoservicio**: el cliente escanea productos,
arma el pedido, paga y retira su ticket. Pensada para terminales fijas en modo
kiosco.

- Stack: Kotlin + Jetpack Compose + Coroutines + Retrofit/Moshi
- `applicationId`: `com.komanda.kiosk`
- Bundle: `com.komanda.kiosk`

## Requisitos

- JDK 17 (`org.gradle.java.home` ya apunta a `/usr/lib/jvm/java-17-openjdk`)
- Android SDK con la plataforma **API 37**
- Gradle wrapper incluido (9.5.0)

## Build de desarrollo

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

## Backend

La URL base se inyecta como `BuildConfig.DEFAULT_BASE_URL` y por defecto es
`https://kda.ar`. Override sin tocar código:

```bash
./gradlew :app:assembleDebug -Pkomanda.baseUrl=http://10.0.2.2:3000
# o
KOMANDA_BASE_URL=http://10.0.2.2:3000 ./gradlew :app:assembleDebug
```

También se puede editar desde la pantalla de login; el valor queda guardado en
el dispositivo.

## Firma y release

Credenciales en `keystore.properties` (gitignored) o variables de entorno. Sin
ellas el AAB sale **sin firmar**.

```bash
./gradlew :app:bundleRelease
jarsigner -verify app/build/outputs/bundle/release/app-release.aab
```

Variables de entorno equivalentes (CI):

```
KOMANDA_KEYSTORE_FILE=/ruta/al/upload.jks
KOMANDA_KEYSTORE_PASSWORD=…
KOMANDA_KEY_ALIAS=komanda-kiosk
KOMANDA_KEY_PASSWORD=…
```

> Upload key fuera del repo: `~/.komanda/keys/`. Hacé backup del `.jks` y de las
> contraseñas.

## Versionado

`version.properties` es la única fuente de verdad:

```properties
versionCode=1
versionName=1.0.0
```

## Tests

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintRelease
```

## Publicación en Play

Ver [`PLAY.md`](PLAY.md) y [`PRIVACY.md`](PRIVACY.md).
