package com.komanda.kiosk

import com.komanda.kiosk.core.network.VerifyStaffRequest
import com.komanda.kiosk.core.network.VerifyStaffResponse
import com.komanda.kiosk.ui.EspressoManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.File

class BackofficeSanityAndRoleGatingTest {

    @Test
    fun kioskScreen_declaresOnlyLockIconAndNoCashButtonInHeader() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val screenFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/EspressoKioskScreen.kt")

        assertTrue("EspressoKioskScreen.kt should exist", screenFile.exists())
        val content = screenFile.readText()

        // The cash button in header must be removed
        assertFalse(
            "Screen must not declare cash button 'Abrir caja' in the kiosk top bar",
            content.contains("\"Abrir caja\"")
        )
        assertFalse(
            "Screen must not declare cash balance button 'Caja: $' in the kiosk top bar",
            content.contains("\"Caja: $")
        )

        // PIN dialog must no longer be invoked on lock press
        assertFalse(
            "Screen must not declare EspressoPinDialog",
            content.contains("EspressoPinDialog(")
        )

        // Lock button must trigger backoffice login dialog
        assertTrue(
            "Lock button must open backoffice auth dialog",
            content.contains("showBackofficeAuthDialog = true")
        )
        assertTrue(
            "Screen must declare onNavigateToBackoffice parameter",
            content.contains("onNavigateToBackoffice")
        )
    }

    @Test
    fun backofficeScreen_declaresTabsAndRoleBasedBillingGating() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val backofficeFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/BackofficeScreen.kt")

        assertTrue("BackofficeScreen.kt should exist", backofficeFile.exists())
        val content = backofficeFile.readText()

        // Tabs
        assertTrue("BackofficeScreen must declare CAJA tab", content.contains("BackofficeTab.CAJA"))
        assertTrue("BackofficeScreen must declare IMPRESORAS tab", content.contains("BackofficeTab.IMPRESORAS"))
        assertTrue("BackofficeScreen must declare Control de Caja tab label", content.contains("Control de Caja"))
        assertTrue("BackofficeScreen must declare Configuración de Impresoras tab label", content.contains("Configuración de Impresoras"))

        // Role-based gating
        assertTrue("BackofficeScreen must check if user is employee", content.contains("val isEmployee = role == \"employee\""))
        assertTrue(
            "BackofficeScreen must hide billing data for employees",
            content.contains("Datos de facturación restringidos")
        )
        assertTrue(
            "BackofficeScreen must indicate restricted values for employees",
            content.contains("🔒 Restringido")
        )
        assertTrue(
            "BackofficeScreen must display full metrics for admin/owner",
            content.contains("Ventas en Efectivo") && content.contains("Total Esperado en Caja")
        )

        // Printer integration
        assertTrue(
            "BackofficeScreen must integrate PrinterSettingsContent",
            content.contains("PrinterSettingsContent(")
        )

        // Exit / Back handler
        assertTrue("BackofficeScreen must declare BackHandler", content.contains("BackHandler"))
        assertTrue("BackofficeScreen must declare Volver al Kiosk action", content.contains("Volver al Kiosk"))
    }

    @Test
    fun closeShiftDialog_gatesBillingDataForEmployees() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val dialogFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/EspressoCloseShiftDialog.kt")

        assertTrue("EspressoCloseShiftDialog.kt should exist", dialogFile.exists())
        val content = dialogFile.readText()

        assertTrue(
            "Dialog must accept isEmployee parameter",
            content.contains("isEmployee: Boolean = false")
        )
        assertTrue(
            "Dialog must declare blind cash count mode for employee",
            content.contains("Arqueo de Caja (Conteo Ciego)") || content.contains("Cierre y Arqueo Ciego de Caja")
        )
        assertTrue(
            "Dialog must inform that billing data is restricted for employee",
            content.contains("Restringido (Perfil empleado)")
        )
    }

    @Test
    fun kioskActivity_declaresBackofficeScreenRouting() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val activityFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/KioskActivity.kt")

        assertTrue("KioskActivity.kt should exist", activityFile.exists())
        val content = activityFile.readText()

        assertTrue(
            "EspressoScreen must declare BACKOFFICE enum value",
            content.contains("BACKOFFICE")
        )
        assertTrue(
            "KioskActivity must route to BackofficeScreen",
            content.contains("BackofficeScreen(")
        )
        assertTrue(
            "KioskActivity must clear staff session when returning to kiosk",
            content.contains("manager.endStaffSession()")
        )
    }

    @Test
    fun staffSessionRoleGating_employeeVsAdmin_reflectedInManager() = runTest {
        val fakeApi = FakeEspressoApi()

        val manager = EspressoManager(
            tenantId = "tenant-test-1",
            tenantName = "Test Kiosk",
            api = fakeApi
        )

        // Login as employee
        fakeApi.mockVerifyStaffRole = "employee"
        val employeeSuccess = manager.verifyStaffCredentials("employee@komanda.com", "pass123")
        assertTrue(employeeSuccess)
        assertNotNull(manager.activeStaffSession.value)
        assertEquals("employee", manager.activeStaffSession.value?.role)
        assertEquals("employee@komanda.com", manager.activeStaffSession.value?.operatorEmail)

        // Exit / end staff session
        manager.endStaffSession()
        assertNull(manager.activeStaffSession.value)

        // Login as admin
        fakeApi.mockVerifyStaffRole = "admin"
        val adminSuccess = manager.verifyStaffCredentials("admin@komanda.com", "admin123")
        assertTrue(adminSuccess)
        assertNotNull(manager.activeStaffSession.value)
        assertEquals("admin", manager.activeStaffSession.value?.role)
        assertEquals("admin@komanda.com", manager.activeStaffSession.value?.operatorEmail)
    }
}
