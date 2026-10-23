package com.komanda.kiosk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class KioskBackofficeFeaturesSanityTest {

    private fun source(relativePath: String): String {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val file = File(projectDir, relativePath)
        assertTrue("$relativePath should exist", file.exists())
        return file.readText()
    }

    @Test
    fun scanner_hasNoManualBarcodeInputAndShowsScanFeedback() {
        val content = source("src/main/java/com/komanda/kiosk/ui/EspressoKioskScreen.kt")

        assertFalse(
            "Scanner must not expose the manual barcode text field",
            content.contains("manualBarcodeInput")
        )
        assertFalse(
            "Scanner must not render the 'Escanear o ingresar código' input",
            content.contains("Escanear o ingresar código")
        )
        assertTrue(
            "Scanner must clearly acknowledge the scanned product",
            content.contains("Producto escaneado")
        )
        assertTrue(
            "Scanner must show the scanned product name",
            content.contains("scanned.productName")
        )
    }

    @Test
    fun kioskActivity_hasNoPrinterConfigurationRoute() {
        val content = source("src/main/java/com/komanda/kiosk/ui/KioskActivity.kt")

        assertFalse(
            "Kiosk must not route to printer settings",
            content.contains("PRINTER_SETTINGS")
        )
        assertFalse(
            "Kiosk must not import the printer settings screen",
            content.contains("PrinterSettingsScreen")
        )
    }

    @Test
    fun productsTab_implementsOwnerGatedBulkManualLoad() {
        val content = source("src/main/java/com/komanda/kiosk/ui/BackofficeProductsView.kt")

        assertTrue(
            "Products view must gate bulk load to owner",
            content.contains("if (!isOwner)")
        )
        assertTrue(
            "Products view must support multiple rows",
            content.contains("mutableStateListOf(EditableProductRow())")
        )
        assertTrue(
            "Products view must call bulk creation",
            content.contains("bulkCreateItems(")
        )
        assertTrue(
            "Products view must support the draft-first flow",
            content.contains("Borradores pendientes")
        )
        assertTrue(
            "Products view must publish drafts",
            content.contains("publishDrafts(")
        )
    }

    @Test
    fun cashControl_embedsFullAnalyticsDashboard() {
        val content = source("src/main/java/com/komanda/kiosk/ui/BackofficeAnalytics.kt")

        assertTrue("Analytics must render revenue KPIs", content.contains("Facturación total"))
        assertTrue("Analytics must render dwell metrics", content.contains("Tiempo en carta"))
        assertTrue("Analytics must render payment waterfall", content.contains("Cascada Mercado Pago"))
        assertTrue("Analytics must render top products", content.contains("Productos más vendidos"))
        assertTrue("Analytics must render peak hours", content.contains("Horarios con más trabajo"))
        assertTrue("Analytics must render kitchen delays", content.contains("Demoras en cocina"))
        assertTrue("Analytics must render customer recurrence", content.contains("Clientes y promociones"))
    }

    @Test
    fun cart_exposesDiscountCodeAboveTotal() {
        val content = source("src/main/java/com/komanda/kiosk/ui/EspressoKioskScreen.kt")

        val discountIndex = content.indexOf("Código de descuento")
        val totalIndex = content.indexOf("Checkout Summary Bar")

        assertTrue("Cart must expose a discount code field", discountIndex >= 0)
        assertTrue(
            "Discount code input must appear above the total to pay",
            discountIndex in 0 until totalIndex
        )
    }
}
