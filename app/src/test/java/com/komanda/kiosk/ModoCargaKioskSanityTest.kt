package com.komanda.kiosk

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModoCargaKioskSanityTest {

    @Test
    fun kioskScreen_declaresModoCargaBannerAndExitAction() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val screenFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/EspressoKioskScreen.kt")

        assertTrue("EspressoKioskScreen.kt should exist", screenFile.exists())
        val content = screenFile.readText()

        assertTrue(
            "Screen must declare 'Modo Carga Rápida Activo' banner",
            content.contains("Modo Carga Rápida Activo")
        )

        assertTrue(
            "Screen must declare 'Salir de Modo Carga' action button",
            content.contains("Salir de Modo Carga")
        )

        assertTrue(
            "Screen must listen to activeStaffSession state",
            content.contains("espressoManager.activeStaffSession.collectAsStateWithLifecycle()")
        )
    }

    @Test
    fun quickAddDialog_declaresCategoryCreationAndSync() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val dialogFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/EspressoQuickAddDialog.kt")

        assertTrue("EspressoQuickAddDialog.kt should exist", dialogFile.exists())
        val content = dialogFile.readText()

        assertTrue(
            "Dialog must support on-the-fly category naming",
            content.contains("Nombre de la nueva categoría")
        )

        assertTrue(
            "Dialog must include '+ Nueva categoría...' option in dropdown",
            content.contains("+ Nueva categoría...")
        )

        assertTrue(
            "Dialog must allow toggling to existing categories",
            content.contains("Elegir categoría existente")
        )
    }
}
