package com.komanda.kiosk

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LargeQrDialogSanityTest {

    @Test
    fun qrDialog_declaresLargeBitmapDimensionsAndTotalTypography() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val dialogFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/qr/KioskQrPaymentDialog.kt")

        assertTrue("KioskQrPaymentDialog.kt should exist", dialogFile.exists())
        val content = dialogFile.readText()

        assertTrue(
            "QrCodeDisplay should be sized to at least 360.dp",
            content.contains("sizeDp = 360.dp")
        )

        assertTrue(
            "QrCodeDisplay should declare a quiet zone of 16.dp",
            content.contains("quietZoneDp = 16.dp")
        )

        assertTrue(
            "Dialog width should be expanded to at least 520.dp (540.dp)",
            content.contains(".width(540.dp)")
        )

        assertTrue(
            "Total amount text should be styled at 28.sp",
            content.contains("fontSize = 28.sp")
        )
    }
}
