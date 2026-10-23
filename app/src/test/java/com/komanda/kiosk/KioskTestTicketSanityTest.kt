package com.komanda.kiosk

import com.komanda.kiosk.core.model.TicketItem
import com.komanda.kiosk.core.model.TicketPayload
import com.komanda.kiosk.core.model.TicketSummary
import com.komanda.kiosk.hardware.printing.EscPosTicketRenderer
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class KioskTestTicketSanityTest {

    @Test
    fun printerSettings_declaresKioskTestPrintButton() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val settingsFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/settings/PrinterSettingsScreen.kt")

        assertTrue("PrinterSettingsScreen.kt should exist", settingsFile.exists())
        val content = settingsFile.readText()

        assertTrue(
            "PrinterSettingsScreen should declare 'Probar Impresión Kiosk'",
            content.contains("Probar Impresión Kiosk")
        )
    }

    @Test
    fun renderKioskTestTicket_emitsExpectedHeadersAndCutCommand() {
        val testPayload = TicketPayload(
            orderId = "test-check-diag",
            purchaseNumber = "DIAG",
            source = "kiosk_test",
            tenant = "Komanda Kiosk",
            items = listOf(
                TicketItem(
                    id = "t-1",
                    name = "Item de Prueba",
                    quantity = 1,
                    unitPrice = 50.0,
                    lineTotal = 50.0
                )
            ),
            summary = TicketSummary(subtotal = 50.0, total = 50.0)
        )

        val bytes = EscPosTicketRenderer.renderKioskTestTicket(testPayload)
        val text = String(bytes, Charsets.ISO_8859_1)

        assertTrue("Must include TICKET DE PRUEBA KIOSK", text.contains("TICKET DE PRUEBA KIOSK"))
        assertTrue("Must include confirmation state", text.contains("ESTADO: IMPRESION DE PRUEBA OK"))
        assertTrue("Must include totem mode description", text.contains("AUTOSERVICIO / TOTEM KIOSK"))
        assertTrue("Must include Kiosk ready branding", text.contains("¡KOMANDA KIOSK LISTO!"))

        // Feed & cut bytes: GS V 'B' 2 (0x1D, 0x56, 0x42, 0x02)
        val cutSequence = byteArrayOf(0x1D.toByte(), 0x56.toByte(), 0x42.toByte(), 0x02.toByte())
        var foundCut = false
        for (i in 0 until bytes.size - 3) {
            if (bytes[i] == cutSequence[0] && bytes[i + 1] == cutSequence[1] && bytes[i + 2] == cutSequence[2] && bytes[i + 3] == cutSequence[3]) {
                foundCut = true
                break
            }
        }
        assertTrue("Must contain paper feed and cut command", foundCut)
    }
}
