# Publicación en Google Play — Komanda Kiosk

Checklist operativo para subir la app. Lo que ya está resuelto en el repo está
marcado ✅; lo que depende de una persona o de Play Console está marcado ⬜.

---

## 1. Estado técnico del repo

| Ítem | Estado |
| :--- | :--- |
| `applicationId` | ✅ `com.komanda.kiosk` |
| Firma de release | ✅ `signingConfigs.release` cableado vía `keystore.properties` / env vars |
| AAB firmado | ✅ `./gradlew :app:bundleRelease` firma con la upload key |
| Versión | ✅ `version.properties` (`versionCode=1`, `versionName=1.0.0`) |
| Lint de release | ✅ `./gradlew :app:lintRelease` pasa sin errores |
| `targetSdk` | ✅ 37 (Android 17) — cumple el requisito de Play de 2026 |
| Errores de lint corregidos | ✅ desugaring de `java.time`, opt-in de CameraX, `dispatchKeyEvent` |
| Copias de seguridad | ✅ `allowBackup="false"` |
| URL de backend | ✅ `BuildConfig.DEFAULT_BASE_URL` = `https://kda.ar`, override con `-Pkomanda.baseUrl=` |
| Atajos de desarrollo en login | ✅ ocultos en release (`BuildConfig.DEBUG`) |
| Icono de marca | ✅ reemplazado el template de Android Studio |
| Metadatos de tienda | ✅ `fastlane/metadata/android/es-AR/` |

> ⚠️ El backend **tiene que estar desplegado y sirviendo `/api/v1/...` en
> `https://kda.ar`** antes de publicar. Hoy ese host responde 307 en `/` y 404 en
> `/api/health/live`.

---

## 2. Bloqueantes que dependen de vos

| # | Qué falta | Detalle |
| :--- | :--- | :--- |
| 1 | **Cuenta de Google Play Console** | 25 USD único. Si la cuenta es personal nueva, Play exige **closed testing con 12 testers durante 14 días** antes de producción. Con CUIT conviene cuenta de organización. |
| 2 | **Política de privacidad publicada** | Texto en `PRIVACY.md`. Falta razón social/CUIT y URL pública (ej. `https://kda.ar/privacidad/kiosk`). |
| 3 | **Email de soporte** | Usé `soporte@komanda.ar` como placeholder. Confirmalo. |
| 4 | **Capturas de pantalla** | Mínimo 2. Requieren terminal con sesión contra el backend real. Ver §5. |
| 5 | **Backup de la upload key** | `~/.komanda/keys/komanda-kiosk-upload.jks` + contraseñas en `~/.komanda/keys/CREDENTIALS.txt`. |
| 6 | **Revisión de "app launcher"** | El manifest declara una actividad con categoría `HOME` (`KioskHomeLauncherAlias`, enabled). Es legítimo para modo kiosco, pero el revisor puede pedir contexto: describí en la ficha que es una terminal de autoservicio que puede fijarse como pantalla de inicio. |
| 7 | **Alta de la app en Play Console** | No se puede automatizar sin credenciales de servicio. |

---

## 3. Cómo se firma

Orden de lectura de credenciales:

1. `keystore.properties` (gitignored, ya creado).
2. Variables de entorno (CI): `KOMANDA_KEYSTORE_FILE`,
   `KOMANDA_KEYSTORE_PASSWORD`, `KOMANDA_KEY_ALIAS`, `KOMANDA_KEY_PASSWORD`.

Sin credenciales el build avisa y genera un AAB **sin firmar**.

```bash
./gradlew :app:bundleRelease
jarsigner -verify app/build/outputs/bundle/release/app-release.aab   # "jar verified"
```

Artefacto: `app/build/outputs/bundle/release/app-release.aab`

---

## 4. Declaraciones en Play Console

### 4.1 Seguridad de los datos

| Pregunta | Respuesta |
| :--- | :--- |
| ¿La app recopila o comparte datos? | **Sí, recopila** |
| ¿Se transmiten cifrados? | Sí (HTTPS) |
| ¿Se puede pedir eliminación? | Sí — por email a soporte |
| **Información personal → Dirección de correo electrónico** | Recopilada · No compartida · Obligatoria · Autenticación del personal |
| **Información personal → Nombre** | Recopilada (comercio/sucursal) · No compartida |
| **Información financiera → Historial de compras** | Recopilada (pedidos y cobros) · No compartida |
| **Actividad en la app → Acciones dentro de la app** | Recopilada · No compartida |
| **Información y rendimiento → Registros de errores** | Recopilado · No compartido |
| **Identificador de publicidad** | **No** |
| **Ubicación** | **No** |
| **Contactos, fotos, archivos, salud** | **No** |

### 4.2 Clasificación de contenido (IARC)

- Categoría: **Utilidad / Negocios**
- Violencia, desnudos, drogas, lenguaje: **No**
- Apuestas, compras dentro de la app: **No**
- Interacción entre usuarios: **No**
- Resultado esperado: **Apto para todo público (3+)**

### 4.3 Permisos sensibles

| Permiso | Declaración |
| :--- | :--- |
| `CAMERA` | Escaneo de códigos de barras. No graba ni transmite imágenes. |
| `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN` | Impresoras térmicas ESC/POS. |
| `USB host` | Impresoras ESC/POS por cable. |

Sin `QUERY_ALL_PACKAGES`, sin ubicación en segundo plano, sin servicios en
primer plano: no aplican formularios adicionales.

### 4.4 Seguridad

- Target API: **37**
- App signing: **Play App Signing** (recomendado).

---

## 5. Capturas de pantalla

Requisitos: JPG/PNG 24 bits, 320–3840 px por lado, sin transparencia.

Con la terminal o emulador apuntando a `https://kda.ar`:

```bash
adb exec-out screencap -p > phoneScreenshots/01-bienvenida.png
```

Guardar en `fastlane/metadata/android/es-AR/images/phoneScreenshots/`
(`01-…png`, `02-…png`, …). Mínimo 2, ideal 4:

1. Pantalla de bienvenida / catálogo
2. Carrito con productos escaneados
3. Selección de medio de pago / QR
4. Ticket generado

---

## 6. Publicar

```bash
./gradlew clean :app:bundleRelease :app:lintRelease
```

1. Play Console → **Crear app** → es-AR, gratis, declaraciones.
2. **Pruebas internas** → subir el `.aab`.
3. Completar *Contenido de la app* y *Presencia en la tienda* con los archivos de
   `fastlane/metadata/android/es-AR/`.
4. Promover a **cerrada** (12 testers × 14 días si la cuenta es personal nueva).
5. Promover a **producción**.

### Incrementar versión antes de cada subida

```bash
versionCode=2
versionName=1.0.1
```

`versionCode` nunca se reutiliza.
