# Política de privacidad — Komanda Kiosk

**Última actualización:** 29 de octubre de 2026
**Responsable del tratamiento:** Komanda — [RAZÓN SOCIAL / CUIT a completar], Pinamar, Provincia de Buenos Aires, Argentina.
**Contacto:** soporte@komanda.ar

> Este documento debe publicarse en una URL pública y estable (por ejemplo
> `https://kda.ar/privacidad/kiosk`) e informarse en Google Play Console.
> El texto no debe modificarse una vez publicado sin actualizar la fecha.

## 1. Qué es Komanda Kiosk

Komanda Kiosk es una terminal de autoservicio para comercios: el cliente escanea
productos, arma su pedido, paga y retira su ticket. La terminal se configura con
credenciales de operador que entrega el comercio. No permite crear cuentas
desde la aplicación y no está dirigida a consumidores finales como usuarios
registrados.

## 2. Qué datos tratamos

**Datos de la cuenta de operador (personal del comercio)**

- Correo electrónico y contraseña ingresados para habilitar la terminal. La
  contraseña **no se almacena en el dispositivo**.
- Token de sesión, fecha de vencimiento, identificador y nombre del comercio y
  de la sucursal.

**Datos de la operación**

- Productos escaneados, carrito, importes, medio de pago elegido y ticket.
- Configuración de impresoras y del modo kiosco.

**Datos técnicos**

- Registros de errores y diagnóstico necesarios para operar el servicio.

No recopilamos identificador de publicidad, ubicación, contactos, fotos ni
archivos personales. La app no identifica a la persona que opera el kiosco.

## 3. Permisos del dispositivo

| Permiso | Para qué se usa |
| :--- | :--- |
| Cámara | Leer códigos de barras de productos. No graba ni transmite imágenes. |
| Bluetooth | Conectar e imprimir en impresoras térmicas ESC/POS Bluetooth. |
| Internet / estado de red | Comunicarse con el servidor de Komanda. |
| USB (host) | Conectar impresoras térmicas ESC/POS por cable. |

## 4. Dónde se guardan los datos

- **En el dispositivo:** el token de sesión y la configuración se guardan en
  almacenamiento privado de la app, cifrado con Android Keystore
  (AES-256-GCM). Las copias de seguridad del sistema están deshabilitadas.
- **En servidores de Komanda:** los datos operativos se almacenan en la
  infraestructura contratada por Komanda, con el comercio como responsable de
  su contenido.

## 5. Con quién se comparten

No vendemos ni cedemos datos personales. Solo se comparten con:

- el comercio titular de la terminal;
- proveedores de infraestructura y de pagos habilitados por el comercio (por
  ejemplo, Mercado Pago), exclusivamente para procesar la operación;
- autoridades, cuando exista obligación legal.

## 6. Conservación

Los datos operativos se conservan mientras el comercio mantenga su cuenta y
luego por el plazo exigido por la normativa contable e impositiva. Los registros
técnicos se conservan por un máximo de 90 días.

## 7. Derechos de las personas

Podés ejercer los derechos de acceso, rectificación, actualización y supresión
previstos en la Ley 25.326 escribiendo a soporte@komanda.ar. La Agencia de
Acceso a la Información Pública es el órgano de control en Argentina.

## 8. Seguridad

Usamos cifrado en tránsito (HTTPS), cifrado en reposo para la sesión local y
control de acceso por comercio (multi-tenant). Si detectamos un incidente que
afecte datos personales, lo notificaremos según corresponda.

## 9. Menores

La app es una herramienta de trabajo y no está dirigida a menores de 18 años.

## 10. Cambios

Si cambiamos esta política, publicaremos la nueva versión con su fecha de
actualización en la misma URL.
