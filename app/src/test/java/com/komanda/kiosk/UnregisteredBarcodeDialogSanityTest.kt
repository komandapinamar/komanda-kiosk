package com.komanda.kiosk

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UnregisteredBarcodeDialogSanityTest {

    @Test
    fun unregisteredDialog_declaresCustomerFriendlyGuidanceAndSafety() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val dialogFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/EspressoUnregisteredBarcodeDialog.kt")

        assertTrue("EspressoUnregisteredBarcodeDialog.kt should exist", dialogFile.exists())
        val content = dialogFile.readText()

        assertTrue(
            "Dialog title should state 'Producto no registrado'",
            content.contains("Producto no registrado")
        )

        assertTrue(
            "Dialog body should tell customer to approach cash desk or request staff help",
            content.contains("Este artículo aún no figura en el sistema. Por favor acercate a caja con el producto o solicitá asistencia al personal.")
        )

        assertTrue(
            "Dialog should have prominent 'Entendido' button",
            content.contains("Entendido")
        )

        assertTrue(
            "Dialog should provide discrete 'Personal autorizado' option",
            content.contains("Personal autorizado")
        )
    }

    @Test
    fun kioskScreen_routesUnknownBarcodeToCustomerDialogFirst() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val screenFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/EspressoKioskScreen.kt")

        assertTrue("EspressoKioskScreen.kt should exist", screenFile.exists())
        val content = screenFile.readText()

        assertTrue(
            "EspressoKioskScreen should invoke EspressoUnregisteredBarcodeDialog when lookup is pending",
            content.contains("EspressoUnregisteredBarcodeDialog(")
        )
    }
}
