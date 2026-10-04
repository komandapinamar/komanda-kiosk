package com.komanda.kiosk.ui

import android.util.Log
import com.komanda.kiosk.core.model.TicketItem
import com.komanda.kiosk.core.model.TicketPayload
import com.komanda.kiosk.core.model.TicketSummary
import com.komanda.kiosk.core.network.BarcodeLookupResponseDto
import com.komanda.kiosk.core.network.BarcodeSuggestionDto
import com.komanda.kiosk.core.network.CashShiftDto
import com.komanda.kiosk.core.network.CatalogCategoryDto
import com.komanda.kiosk.core.network.CatalogItemDto
import com.komanda.kiosk.core.network.CloseCashShiftRequest
import com.komanda.kiosk.core.network.CreateCatalogItemRequest
import com.komanda.kiosk.core.network.CreateDirectOrderRequest
import com.komanda.kiosk.core.network.DirectOrderCustomerRequest
import com.komanda.kiosk.core.network.DirectOrderItemRequest
import com.komanda.kiosk.core.network.KioskPaymentCustomerRequest
import com.komanda.kiosk.core.network.KioskPaymentItemRequest
import com.komanda.kiosk.core.network.KioskPaymentSessionRequest
import com.komanda.kiosk.core.network.KioskPaymentSessionResponse
import com.komanda.kiosk.core.network.KioskPaymentStatusResponse
import com.komanda.kiosk.core.network.KomandaApi
import com.komanda.kiosk.core.network.OpenCashShiftRequest
import com.komanda.kiosk.core.network.VerifyStaffRequest
import com.komanda.kiosk.hardware.printing.PrinterRouter
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EspressoManager(
    val tenantId: String,
    val tenantName: String,
    private val api: KomandaApi,
    private val printerRouter: PrinterRouter? = null,
    private val attemptStore: CheckoutAttemptStore? = null
) {
    private val tag = "EspressoManager"

    private val _categories = MutableStateFlow<List<CatalogCategoryDto>>(emptyList())
    val categories: StateFlow<List<CatalogCategoryDto>> = _categories.asStateFlow()

    private val _items = MutableStateFlow<List<CatalogItemDto>>(emptyList())
    val items: StateFlow<List<CatalogItemDto>> = _items.asStateFlow()

    private val _cart = MutableStateFlow<List<EspressoCartLine>>(emptyList())
    val cart: StateFlow<List<EspressoCartLine>> = _cart.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _activeShift = MutableStateFlow<CashShiftDto?>(null)
    val activeShift: StateFlow<CashShiftDto?> = _activeShift.asStateFlow()

    private val _pendingLookup = MutableStateFlow<Pair<String, BarcodeSuggestionDto?>?>(null)
    val pendingLookup: StateFlow<Pair<String, BarcodeSuggestionDto?>?> = _pendingLookup.asStateFlow()

    private val _activeQrSession = MutableStateFlow<KioskPaymentSessionResponse?>(null)
    val activeQrSession: StateFlow<KioskPaymentSessionResponse?> = _activeQrSession.asStateFlow()

    private val _approvedPayment = MutableStateFlow<KioskPaymentStatusResponse?>(null)
    val approvedPayment: StateFlow<KioskPaymentStatusResponse?> = _approvedPayment.asStateFlow()

    val totalAmount: Double
        get() = _cart.value.sumOf { it.lineTotal }

    suspend fun loadCashShift() {
        try {
            val res = api.getCurrentCashShift(tenantId)
            if (res.isSuccessful && res.body() != null) {
                _activeShift.value = res.body()!!.data
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load current cash shift", e)
        }
    }

    suspend fun openCashShift(amount: String, notes: String? = null): Boolean {
        _isLoading.value = true
        try {
            val formatted = if (amount.contains(".")) amount else "$amount.00"
            val res = api.openCashShift(tenantId, OpenCashShiftRequest(openingBalance = formatted, notes = notes))
            if (res.isSuccessful && res.body() != null) {
                _activeShift.value = res.body()!!
                _statusMessage.value = "Caja abierta con fondo inicial de $$formatted"
                return true
            } else {
                _statusMessage.value = "No se pudo abrir la caja."
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to open cash shift", e)
            _statusMessage.value = "Error al abrir la caja."
        } finally {
            _isLoading.value = false
        }
        return false
    }

    suspend fun closeCashShift(closingBalance: String, notes: String? = null): Boolean {
        val current = _activeShift.value ?: return false
        _isLoading.value = true
        try {
            val formatted = if (closingBalance.contains(".")) closingBalance else "$closingBalance.00"
            val res = api.closeCashShift(tenantId, current.id, CloseCashShiftRequest(closingBalance = formatted, notes = notes))
            if (res.isSuccessful) {
                _activeShift.value = null
                _statusMessage.value = "Caja cerrada y arqueo completado."
                return true
            } else {
                _statusMessage.value = "No se pudo cerrar la caja."
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to close cash shift", e)
            _statusMessage.value = "Error al cerrar la caja."
        } finally {
            _isLoading.value = false
        }
        return false
    }

    suspend fun loadCatalog() {
        _isLoading.value = true
        try {
            val catRes = api.listCategories(tenantId)
            if (catRes.isSuccessful && catRes.body() != null) {
                _categories.value = catRes.body()!!.data
            }

            val itemRes = api.listItems(tenantId)
            if (itemRes.isSuccessful && itemRes.body() != null) {
                _items.value = itemRes.body()!!.data.filter { it.status == "active" || it.status == "draft" }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load catalog", e)
            _statusMessage.value = "Error al cargar el catálogo."
        } finally {
            _isLoading.value = false
        }
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun dismissPendingLookup() {
        _pendingLookup.value = null
    }

    fun addToCart(item: CatalogItemDto) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.item.id == item.id }
        if (index >= 0) {
            val line = current[index]
            current[index] = line.copy(quantity = line.quantity + 1)
        } else {
            current.add(EspressoCartLine(item = item, quantity = 1))
        }
        _cart.value = current
        _statusMessage.value = "Agregado: ${item.name}"
    }

    fun updateQuantity(itemId: String, delta: Int) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.item.id == itemId }
        if (index >= 0) {
            val line = current[index]
            val newQty = line.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = line.copy(quantity = newQty)
            }
            _cart.value = current
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    suspend fun onBarcodeScanned(barcode: String) {
        val clean = barcode.trim()
        if (clean.isBlank()) return

        // 1. Fast in-memory lookup (<150ms)
        val localItem = _items.value.find { it.barcode == clean }
        if (localItem != null) {
            addToCart(localItem)
            return
        }

        // 2. Fallback to API lookup
        _isLoading.value = true
        try {
            val res = api.lookupBarcode(tenantId, clean)
            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                if (body.item != null) {
                    addToCart(body.item)
                } else if (body.suggestion != null) {
                    _pendingLookup.value = Pair(clean, body.suggestion)
                } else {
                    _pendingLookup.value = Pair(clean, null)
                }
            } else {
                _pendingLookup.value = Pair(clean, null)
            }
        } catch (e: Exception) {
            Log.e(tag, "Barcode lookup failed", e)
            _pendingLookup.value = Pair(clean, null)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun quickCreateItem(
        name: String,
        price: String,
        categoryId: String,
        barcode: String?,
        isGeneric: Boolean = false,
        genericIcon: String? = null,
        trackStock: Boolean = false,
        stockQuantity: Int = 0
    ): Boolean {
        _isLoading.value = true
        try {
            val formattedPrice = if (price.contains(".")) price else "$price.00"
            val request = CreateCatalogItemRequest(
                categoryId = categoryId,
                name = name,
                price = formattedPrice,
                barcode = barcode,
                isGeneric = isGeneric,
                genericIcon = genericIcon,
                trackStock = trackStock,
                stockQuantity = stockQuantity,
                status = "active"
            )

            val res = api.createCatalogItem(tenantId, request)
            if (res.isSuccessful && res.body() != null) {
                val created = res.body()!!
                _items.value = _items.value + created
                addToCart(created)
                _pendingLookup.value = null
                _statusMessage.value = "Producto creado y agregado al carrito!"
                return true
            } else {
                _statusMessage.value = "No se pudo crear el producto."
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to create catalog item", e)
            _statusMessage.value = "Error de red al crear el producto."
        } finally {
            _isLoading.value = false
        }
        return false
    }

    private var failedStaffAuthAttempts = 0
    private var staffLockoutUntilMillis = 0L

    val isStaffLockedOut: Boolean
        get() = System.currentTimeMillis() < staffLockoutUntilMillis

    val remainingLockoutSeconds: Int
        get() = ((staffLockoutUntilMillis - System.currentTimeMillis()) / 1000).coerceAtLeast(0).toInt()

    fun resetStaffLockoutForTesting() {
        failedStaffAuthAttempts = 0
        staffLockoutUntilMillis = 0L
    }

    suspend fun verifyStaffCredentials(email: String, password: String): Boolean {
        if (isStaffLockedOut) {
            _statusMessage.value = "Acceso bloqueado por seguridad. Intente en $remainingLockoutSeconds s."
            return false
        }
        _isLoading.value = true
        try {
            val res = api.verifyStaff(tenantId, VerifyStaffRequest(email = email.trim(), password = password))
            if (res.isSuccessful && res.body()?.authorized == true) {
                failedStaffAuthAttempts = 0
                return true
            } else {
                failedStaffAuthAttempts++
                if (failedStaffAuthAttempts >= 3) {
                    staffLockoutUntilMillis = System.currentTimeMillis() + (5 * 60 * 1000L) // 5 minutes
                    _statusMessage.value = "3 intentos fallidos. Acceso bloqueado por 5 minutos."
                } else {
                    val remaining = 3 - failedStaffAuthAttempts
                    _statusMessage.value = "Credenciales incorrectas o usuario no asignado a este local. ($remaining intentos restantes)"
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Staff verification error", e)
            _statusMessage.value = "Error de conexión al verificar credenciales."
        } finally {
            _isLoading.value = false
        }
        return false
    }

    suspend fun checkout(
        paymentMethod: String, // "cash", "qr" (card deferred)
        customerName: String = "Cliente Autoservicio"
    ): Boolean {
        if (_cart.value.isEmpty()) return false
        _isLoading.value = true
        val itemsList = _cart.value.map { it.item.id to it.quantity }
        val cartHash = CheckoutAttemptStore.computeCartHash(itemsList, paymentMethod, customerName)
        val attempt = attemptStore?.getOrStartAttempt(tenantId, cartHash)
        val idempotencyKey = attempt?.idempotencyKey ?: UUID.randomUUID().toString()

        try {
            val request = CreateDirectOrderRequest(
                items = _cart.value.map { line ->
                    DirectOrderItemRequest(
                        kind = "item",
                        resourceId = line.item.id,
                        quantity = line.quantity
                    )
                },
                customer = DirectOrderCustomerRequest(name = customerName),
                notes = if (paymentMethod == "cash") "Pago en efectivo en mostrador" else null
            )

            val res = api.createDirectOrder(tenantId, idempotencyKey, request)
            if (res.isSuccessful && res.body() != null) {
                val order = res.body()!!
                attempt?.let {
                    attemptStore?.markAttemptConfirmed(tenantId, it, order.id)
                    attemptStore?.clearConfirmedAttempt(tenantId)
                }

                val purchaseNum = order.purchaseNumber.toString()

                if (paymentMethod == "cash") {
                    val ticketPayload = TicketPayload(
                        orderId = order.id,
                        purchaseNumber = purchaseNum,
                        tenant = tenantName,
                        items = _cart.value.map { line ->
                            TicketItem(
                                id = line.item.id,
                                name = line.item.name,
                                quantity = line.quantity,
                                unitPrice = line.item.price.toDoubleOrNull() ?: 0.0,
                                lineTotal = line.lineTotal
                            )
                        },
                        summary = TicketSummary(
                            subtotal = totalAmount,
                            total = totalAmount
                        )
                    )
                    printerRouter?.printEspressoCashTicket(ticketPayload)
                }

                _cart.value = emptyList()
                _statusMessage.value = if (paymentMethod == "cash") {
                    "¡Pedido #$purchaseNum creado! Acercate a caja a abonar en efectivo."
                } else {
                    "¡Cobro confirmado! Ticket #$purchaseNum"
                }
                return true
            } else {
                attempt?.let { attemptStore?.markAttemptUnknown(tenantId, it) }
                _statusMessage.value = "Error al procesar el pedido."
            }
        } catch (e: Exception) {
            Log.e(tag, "Checkout failed", e)
            attempt?.let { attemptStore?.markAttemptUnknown(tenantId, it) }
            _statusMessage.value = "Error de red al procesar el pedido."
        } finally {
            _isLoading.value = false
        }
        return false
    }

    suspend fun startQrPayment(customerName: String = "Cliente Autoservicio"): KioskPaymentSessionResponse? {
        if (_cart.value.isEmpty()) return null
        _isLoading.value = true
        val itemsList = _cart.value.map { it.item.id to it.quantity }
        val cartHash = CheckoutAttemptStore.computeCartHash(itemsList, "qr", customerName)
        val attempt = attemptStore?.getOrStartAttempt(tenantId, cartHash)
        val idempotencyKey = attempt?.idempotencyKey ?: UUID.randomUUID().toString()

        try {
            val request = KioskPaymentSessionRequest(
                items = _cart.value.map { line ->
                    KioskPaymentItemRequest(
                        catalogItemId = line.item.id,
                        quantity = line.quantity
                    )
                },
                customer = KioskPaymentCustomerRequest(name = customerName)
            )

            val res = api.createKioskPaymentSession(tenantId, idempotencyKey, request)
            if (res.isSuccessful && res.body() != null) {
                val session = res.body()!!
                _activeQrSession.value = session
                return session
            } else {
                if (res.code() == 422) {
                    _statusMessage.value = "Mercado Pago no está configurado en este local."
                } else {
                    _statusMessage.value = "Error al iniciar sesión de pago con QR."
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Start QR payment failed", e)
            _statusMessage.value = "Error de red al conectar con Mercado Pago."
        } finally {
            _isLoading.value = false
        }
        return null
    }

    suspend fun cancelQrPayment(attemptId: String) {
        _activeQrSession.value = null
        try {
            val idempotencyKey = UUID.randomUUID().toString()
            api.cancelKioskPaymentAttempt(tenantId, attemptId, idempotencyKey)
            _statusMessage.value = "Pago cancelado. Podés continuar con tu compra."
        } catch (e: Exception) {
            Log.e(tag, "Cancel QR payment failed", e)
        }
    }

    suspend fun pollPaymentStatus(attemptId: String): KioskPaymentStatusResponse? {
        try {
            val res = api.getKioskPaymentStatus(tenantId, attemptId)
            if (res.isSuccessful && res.body() != null) {
                return res.body()
            }
        } catch (e: Exception) {
            Log.e(tag, "Polling payment status failed", e)
        }
        return null
    }

    fun onPaymentApproved(statusResponse: KioskPaymentStatusResponse) {
        val itemsSnapshot = _cart.value.map { line ->
            TicketItem(
                id = line.item.id,
                name = line.item.name,
                quantity = line.quantity,
                unitPrice = line.item.price.toDoubleOrNull() ?: 0.0,
                lineTotal = line.lineTotal
            )
        }
        val currentTotal = totalAmount
        val purchaseNum = statusResponse.purchaseNumber ?: ""

        val payload = TicketPayload(
            orderId = statusResponse.orderId ?: UUID.randomUUID().toString(),
            purchaseNumber = purchaseNum,
            tenant = tenantName,
            items = itemsSnapshot,
            summary = TicketSummary(
                subtotal = currentTotal,
                total = currentTotal
            )
        )

        _activeQrSession.value = null
        _cart.value = emptyList()
        _approvedPayment.value = statusResponse
        _statusMessage.value = "¡Cobro confirmado! Ticket #$purchaseNum"

        printerRouter?.let { router ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    router.printKioskQrApprovedTicket(payload, statusResponse.paymentId)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to print approved QR ticket", e)
                }
            }
        }
    }

    fun dismissApprovedPayment() {
        _approvedPayment.value = null
    }

    fun dismissQrPayment() {
        _activeQrSession.value = null
    }
}
