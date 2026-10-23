package com.komanda.kiosk

import com.komanda.kiosk.ui.qr.QrCodeGenerator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test

class QrCodeGeneratorTest {

    @Test
    fun generateBitmap_blankOrEmptyInput_returnsNull() = runTest {
        val emptyResult = QrCodeGenerator.generateBitmap("")
        assertNull(emptyResult)

        val blankResult = QrCodeGenerator.generateBitmap("   ")
        assertNull(blankResult)
    }

    @Test
    fun generateBitmap_validInput_doesNotThrow() = runTest {
        val validUrl = "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=123456"
        // Under standard unitTests.isReturnDefaultValues = true, android.graphics.Bitmap.createBitmap returns default (null/mock)
        // This test asserts that the coroutine runs without unhandled exception and completes safely.
        val result = QrCodeGenerator.generateBitmap(validUrl, sizePx = 256)
        // Safely completed execution without exception
    }
}
