package com.komanda.kiosk

import com.komanda.kiosk.core.network.BarcodeLookupResponseDto
import com.komanda.kiosk.core.network.BarcodeSuggestionDto
import com.komanda.kiosk.core.network.CashShiftDto
import com.komanda.kiosk.core.network.CashShiftResponse
import com.komanda.kiosk.core.network.CatalogCategoryDto
import com.komanda.kiosk.core.network.CatalogItemDto
import com.komanda.kiosk.core.network.CatalogResponse
import com.komanda.kiosk.core.network.CloseCashShiftRequest
import com.komanda.kiosk.core.network.CreateCatalogItemRequest
import com.komanda.kiosk.core.network.CreateDirectOrderRequest
import com.komanda.kiosk.core.network.KioskPaymentCancelResponse
import com.komanda.kiosk.core.network.KioskPaymentSessionRequest
import com.komanda.kiosk.core.network.KioskPaymentSessionResponse
import com.komanda.kiosk.core.network.KomandaApi
import com.komanda.kiosk.core.network.MobileContextResponse
import com.komanda.kiosk.core.network.MobileLoginRequest
import com.komanda.kiosk.core.network.MobileLoginResponse
import com.komanda.kiosk.core.network.OpenCashShiftRequest
import com.komanda.kiosk.core.network.OrderDto
import com.komanda.kiosk.ui.EspressoManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class FakeEspressoApi : KomandaApi {
    var lastDirectOrderRequest: CreateDirectOrderRequest? = null
    val items = mutableListOf(
        CatalogItemDto(
            id = "item-1",
            name = "Coca-Cola 500ml",
            price = "1500.00",
            categoryId = "cat-1",
            barcode = "7790895000997",
            status = "active"
        ),
        CatalogItemDto(
            id = "item-2",
            name = "Café Espresso",
            price = "2000.00",
            categoryId = "cat-2",
            isGeneric = true,
            genericIcon = "☕",
            status = "active"
        )
    )

    override suspend fun mobileLogin(body: MobileLoginRequest): Response<MobileLoginResponse> =
        Response.success(MobileLoginResponse("token", "2026-09-15T00:00:00Z"))

    override suspend fun mobileRevokeSession(authHeader: String?): Response<Unit> =
        Response.success(Unit)

    override suspend fun getMobileContext(authHeader: String?): Response<MobileContextResponse> =
        Response.success(MobileContextResponse(emptyList()))

    override suspend fun createDirectOrder(
        tenantId: String,
        idempotencyKey: String,
        body: CreateDirectOrderRequest
    ): Response<OrderDto> {
        lastDirectOrderRequest = body
        val order = OrderDto(
            id = "order-101",
            tenantId = tenantId,
            locationId = "loc-1",
            purchaseNumber = "1042",
            fulfillmentStatus = "approved",
            paymentStatus = "paid",
            source = "espresso_kiosk",
            total = "3500.00",
            currency = "ARS"
        )
        return Response.success(order)
    }

    override suspend fun listCategories(tenantId: String): Response<CatalogResponse<CatalogCategoryDto>> =
        Response.success(CatalogResponse(listOf(
            CatalogCategoryDto(id = "cat-1", name = "Bebidas"),
            CatalogCategoryDto(id = "cat-2", name = "Cafetería")
        )))

    override suspend fun listItems(tenantId: String): Response<CatalogResponse<CatalogItemDto>> =
        Response.success(CatalogResponse(items))

    override suspend fun lookupBarcode(tenantId: String, barcode: String): Response<BarcodeLookupResponseDto> {
        val existing = items.find { it.barcode == barcode }
        if (existing != null) {
            return Response.success(BarcodeLookupResponseDto(source = "tenant", item = existing))
        }
        if (barcode == "7791234567890") {
            return Response.success(
                BarcodeLookupResponseDto(
                    source = "global",
                    suggestion = BarcodeSuggestionDto(name = "Alfajor Havanna", suggestedCategory = "Snacks")
                )
            )
        }
        return Response.success(BarcodeLookupResponseDto(source = "none"))
    }

    override suspend fun createCatalogItem(
        tenantId: String,
        body: CreateCatalogItemRequest
    ): Response<CatalogItemDto> {
        val newItem = CatalogItemDto(
            id = "item-${items.size + 1}",
            name = body.name,
            price = body.price,
            categoryId = body.categoryId,
            barcode = body.barcode,
            isGeneric = body.isGeneric,
            genericIcon = body.genericIcon,
            trackStock = body.trackStock,
            stockQuantity = body.stockQuantity,
            status = "active"
        )
        items.add(newItem)
        return Response.success(newItem)
    }

    var currentShift: CashShiftDto? = null

    override suspend fun getCurrentCashShift(tenantId: String): Response<CashShiftResponse> =
        Response.success(CashShiftResponse(currentShift))

    override suspend fun openCashShift(
        tenantId: String,
        body: OpenCashShiftRequest
    ): Response<CashShiftDto> {
        val shift = CashShiftDto(
            id = "shift-1",
            tenantId = tenantId,
            openingBalance = body.openingBalance,
            status = "open",
            openedAt = "2026-09-15T08:00:00Z",
            notes = body.notes
        )
        currentShift = shift
        return Response.success(shift)
    }

    override suspend fun closeCashShift(
        tenantId: String,
        shiftId: String,
        body: CloseCashShiftRequest
    ): Response<CashShiftDto> {
        val shift = currentShift!!.copy(
            status = "closed",
            closingBalance = body.closingBalance,
            closedAt = "2026-09-15T20:00:00Z",
            notes = body.notes
        )
        currentShift = null
        return Response.success(shift)
    }

    var lastPaymentSessionRequest: KioskPaymentSessionRequest? = null
    var lastCancelledAttemptId: String? = null

    override suspend fun createKioskPaymentSession(
        tenantId: String,
        idempotencyKey: String,
        body: KioskPaymentSessionRequest
    ): Response<KioskPaymentSessionResponse> {
        lastPaymentSessionRequest = body
        return Response.success(
            KioskPaymentSessionResponse(
                paymentAttemptId = "att-123",
                cartId = "cart-123",
                total = "3500.00",
                currency = "ARS",
                qrData = "https://mercadopago.com/init_point_mock",
                expiresAt = "2026-09-23T16:02:00.000Z",
                timeoutSeconds = 120
            )
        )
    }

    override suspend fun cancelKioskPaymentAttempt(
        tenantId: String,
        attemptId: String,
        idempotencyKey: String
    ): Response<KioskPaymentCancelResponse> {
        lastCancelledAttemptId = attemptId
        return Response.success(
            KioskPaymentCancelResponse(
                status = "cancelled",
                cancelledAt = "2026-09-23T16:01:00.000Z"
            )
        )
    }
}

class EspressoManagerTest {

    @Test
    fun loadCatalog_populatesCategoriesAndItems() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)

        manager.loadCatalog()

        assertEquals(2, manager.categories.value.size)
        assertEquals(2, manager.items.value.size)
    }

    @Test
    fun onBarcodeScanned_whenItemExistsLocally_addsDirectlyToCart() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)
        manager.loadCatalog()

        manager.onBarcodeScanned("7790895000997")

        assertEquals(1, manager.cart.value.size)
        assertEquals("Coca-Cola 500ml", manager.cart.value[0].item.name)
        assertEquals(1, manager.cart.value[0].quantity)
        assertEquals(1500.0, manager.totalAmount, 0.01)
    }

    @Test
    fun onBarcodeScanned_whenItemScannedTwice_incrementsQuantity() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)
        manager.loadCatalog()

        manager.onBarcodeScanned("7790895000997")
        manager.onBarcodeScanned("7790895000997")

        assertEquals(1, manager.cart.value.size)
        assertEquals(2, manager.cart.value[0].quantity)
        assertEquals(3000.0, manager.totalAmount, 0.01)
    }

    @Test
    fun onBarcodeScanned_whenBarcodeUnknown_triggersQuickAddSuggestion() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)
        manager.loadCatalog()

        manager.onBarcodeScanned("7791234567890")

        assertEquals(0, manager.cart.value.size)
        assertNotNull(manager.pendingLookup.value)
        assertEquals("7791234567890", manager.pendingLookup.value?.first)
        assertEquals("Alfajor Havanna", manager.pendingLookup.value?.second?.name)
    }

    @Test
    fun quickCreateItem_persistsAndAddsToCart() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)
        manager.loadCatalog()

        val success = manager.quickCreateItem(
            name = "Galletitas Oreo",
            price = "1200.00",
            categoryId = "cat-1",
            barcode = "7799999999999",
            isGeneric = false,
            genericIcon = null,
            trackStock = true,
            stockQuantity = 50
        )

        assertTrue(success)
        assertEquals(1, manager.cart.value.size)
        assertEquals("Galletitas Oreo", manager.cart.value[0].item.name)
        assertNull(manager.pendingLookup.value)
    }

    @Test
    fun genericItemSelection_addsToCart() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)
        manager.loadCatalog()

        val genericItem = manager.items.value.find { it.isGeneric }!!
        manager.addToCart(genericItem)

        assertEquals(1, manager.cart.value.size)
        assertEquals("Café Espresso", manager.cart.value[0].item.name)
        assertEquals(2000.0, manager.totalAmount, 0.01)
    }

    @Test
    fun checkout_clearsCartAndCallsApi() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)
        manager.loadCatalog()
        manager.onBarcodeScanned("7790895000997")

        val success = manager.checkout(paymentMethod = "cash")

        assertTrue(success)
        assertEquals(0, manager.cart.value.size)
        assertEquals("Pago en efectivo en mostrador", api.lastDirectOrderRequest?.notes)
        assertTrue(manager.statusMessage.value?.contains("Acercate a caja a abonar en efectivo") == true)
    }

    @Test
    fun checkout_qrPaymentMethod_clearsCartAndCallsApi() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)
        manager.loadCatalog()
        manager.onBarcodeScanned("7790895000997")

        val success = manager.checkout(paymentMethod = "qr")

        assertTrue(success)
        assertEquals(0, manager.cart.value.size)
        assertNull(api.lastDirectOrderRequest?.notes)
        assertTrue(manager.statusMessage.value?.contains("¡Cobro confirmado!") == true)
    }

    @Test
    fun startQrPayment_createsSessionAndUpdatesActiveQrSession() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)
        manager.loadCatalog()
        manager.onBarcodeScanned("7790895000997")

        val session = manager.startQrPayment()

        assertNotNull(session)
        assertEquals("att-123", session?.paymentAttemptId)
        assertEquals(120, session?.timeoutSeconds)
        assertEquals(session, manager.activeQrSession.value)
        assertEquals(1, api.lastPaymentSessionRequest?.items?.size)
        // Cart must remain intact while QR is displaying
        assertEquals(1, manager.cart.value.size)
    }

    @Test
    fun cancelQrPayment_callsApiAndClearsActiveQrSessionPreservingCart() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)
        manager.loadCatalog()
        manager.onBarcodeScanned("7790895000997")

        manager.startQrPayment()
        assertNotNull(manager.activeQrSession.value)

        manager.cancelQrPayment("att-123")

        assertEquals("att-123", api.lastCancelledAttemptId)
        assertNull(manager.activeQrSession.value)
        // Cart must be preserved after cancellation
        assertEquals(1, manager.cart.value.size)
        assertTrue(manager.statusMessage.value?.contains("cancelado") == true)
    }

    @Test
    fun cashShift_openAndCloseLifecycle() = runTest {
        val api = FakeEspressoApi()
        val manager = EspressoManager(tenantId = "tenant-1", tenantName = "Kiosco Express", api = api)

        // Initial check: no open shift
        manager.loadCashShift()
        assertNull(manager.activeShift.value)

        // Open shift with 5000 opening balance
        val opened = manager.openCashShift(amount = "5000.00", notes = "Apertura mañana")
        assertTrue(opened)
        assertEquals("open", manager.activeShift.value?.status)
        assertEquals("5000.00", manager.activeShift.value?.openingBalance)

        // Close shift
        val closed = manager.closeCashShift(closingBalance = "12500.00", notes = "Cierre noche")
        assertTrue(closed)
        assertNull(manager.activeShift.value)
    }
}
