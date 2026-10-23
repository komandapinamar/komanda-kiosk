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
import com.komanda.kiosk.core.network.CreateCategoryRequest
import com.komanda.kiosk.core.network.CreateCatalogItemRequest
import com.komanda.kiosk.core.network.CreateDirectOrderRequest
import com.komanda.kiosk.core.network.DashboardAnalyticsDto
import com.komanda.kiosk.core.network.DirectOrderCustomerRequest
import com.komanda.kiosk.core.network.DirectOrderItemRequest
import com.komanda.kiosk.core.network.KioskDiscountPreviewRequest
import com.komanda.kiosk.core.network.KioskDiscountPreviewResponse
import com.komanda.kiosk.core.network.KioskPaymentCustomerRequest
import com.komanda.kiosk.core.network.KioskPaymentItemRequest
import com.komanda.kiosk.core.network.KioskPaymentSessionRequest
import com.komanda.kiosk.core.network.KioskPaymentSessionResponse
import com.komanda.kiosk.core.network.KioskPaymentStatusResponse
import com.komanda.kiosk.core.network.KomandaApi
import com.komanda.kiosk.core.network.OpenCashShiftRequest
import com.komanda.kiosk.core.network.UpdateCatalogCategoryRequest
import com.komanda.kiosk.core.network.UpdateCatalogItemRequest
import com.komanda.kiosk.core.network.VerifyStaffRequest
import com.komanda.kiosk.hardware.printing.PrinterRouter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class StaffSession(
    val userId: String,
    val role: String,
    val operatorEmail: String,
    val authenticatedAtMillis: Long = System.currentTimeMillis(),
    val lastActivityAtMillis: Long = System.currentTimeMillis()
)

data class ScannedProductFeedback(
    val productName: String,
    val price: String,
    val quantityInCart: Int,
    val barcode: String? = null,
    val scannedAtMillis: Long = System.currentTimeMillis()
)

data class BulkProductRow(
    val name: String,
    val price: String,
    val barcode: String? = null,
    val trackStock: Boolean = false,
    val stockQuantity: Int = 0
)

data class BulkCreateResult(
    val createdCount: Int,
    val failedRows: List<Pair<Int, String>>
)

data class PublishDraftsResult(
    val publishedCategories: Int,
    val publishedItems: Int,
    val errors: List<String>
)

enum class AnalyticsRangePreset(val label: String) {
    TODAY("Hoy"),
    LAST_7_DAYS("Últimos 7 días"),
    LAST_30_DAYS("Últimos 30 días"),
    THIS_MONTH("Este mes")
}

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

    private val _activeStaffSession = MutableStateFlow<StaffSession?>(null)
    val activeStaffSession: StateFlow<StaffSession?> = _activeStaffSession.asStateFlow()

    private val _lastScannedProduct = MutableStateFlow<ScannedProductFeedback?>(null)
    val lastScannedProduct: StateFlow<ScannedProductFeedback?> = _lastScannedProduct.asStateFlow()

    private val _analytics = MutableStateFlow<DashboardAnalyticsDto?>(null)
    val analytics: StateFlow<DashboardAnalyticsDto?> = _analytics.asStateFlow()

    private val _analyticsPreset = MutableStateFlow(AnalyticsRangePreset.LAST_7_DAYS)
    val analyticsPreset: StateFlow<AnalyticsRangePreset> = _analyticsPreset.asStateFlow()

    private val _isAnalyticsLoading = MutableStateFlow(false)
    val isAnalyticsLoading: StateFlow<Boolean> = _isAnalyticsLoading.asStateFlow()

    private val _analyticsError = MutableStateFlow<String?>(null)
    val analyticsError: StateFlow<String?> = _analyticsError.asStateFlow()

    private val _discountPreview = MutableStateFlow<KioskDiscountPreviewResponse?>(null)
    val discountPreview: StateFlow<KioskDiscountPreviewResponse?> = _discountPreview.asStateFlow()

    private val _isDiscountPreviewLoading = MutableStateFlow(false)
    val isDiscountPreviewLoading: StateFlow<Boolean> = _isDiscountPreviewLoading.asStateFlow()

    private val _discountError = MutableStateFlow<String?>(null)
    val discountError: StateFlow<String?> = _discountError.asStateFlow()

    private val staffSessionInactivityTimeoutMillis = 10 * 60 * 1000L // 10 minutes

    fun isStaffSessionActive(): Boolean {
        val session = _activeStaffSession.value ?: return false
        val now = System.currentTimeMillis()
        if (now - session.lastActivityAtMillis > staffSessionInactivityTimeoutMillis) {
            endStaffSession(reason = "Sesión de carga cerrada por inactividad (10 minutos).")
            return false
        }
        return true
    }

    fun startStaffSession(userId: String, role: String, email: String) {
        val now = System.currentTimeMillis()
        _activeStaffSession.value = StaffSession(
            userId = userId,
            role = role,
            operatorEmail = email,
            authenticatedAtMillis = now,
            lastActivityAtMillis = now
        )
        _statusMessage.value = null
    }

    fun touchStaffActivity() {
        val current = _activeStaffSession.value ?: return
        _activeStaffSession.value = current.copy(lastActivityAtMillis = System.currentTimeMillis())
    }

    fun endStaffSession(reason: String = "Modo Carga finalizado. Terminal lista para clientes.") {
        _activeStaffSession.value = null
        _statusMessage.value = reason
    }

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

    suspend fun loadAnalytics(
        preset: AnalyticsRangePreset = _analyticsPreset.value,
        source: String = "all"
    ) {
        _analyticsPreset.value = preset
        _isAnalyticsLoading.value = true
        _analyticsError.value = null
        try {
            val (from, to, granularity) = buildAnalyticsRange(preset)
            val res = api.getDashboardAnalytics(
                tenantId = tenantId,
                from = from,
                to = to,
                granularity = granularity,
                source = source
            )
            if (res.isSuccessful && res.body() != null) {
                _analytics.value = res.body()
            } else if (res.code() == 403) {
                _analyticsError.value = "Las analíticas están disponibles solo para el rol Propietario."
            } else {
                _analyticsError.value = "No se pudieron cargar las analíticas (HTTP ${res.code()})."
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load analytics", e)
            _analyticsError.value = "Error de conexión al cargar las analíticas."
        } finally {
            _isAnalyticsLoading.value = false
        }
    }

    private fun buildAnalyticsRange(preset: AnalyticsRangePreset): Triple<String, String, String> {
        val now = Calendar.getInstance()
        val to = now.time
        val from = Calendar.getInstance().apply {
            when (preset) {
                AnalyticsRangePreset.TODAY -> {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                AnalyticsRangePreset.LAST_7_DAYS -> {
                    time = to
                    add(Calendar.DAY_OF_YEAR, -7)
                }
                AnalyticsRangePreset.LAST_30_DAYS -> {
                    time = to
                    add(Calendar.DAY_OF_YEAR, -30)
                }
                AnalyticsRangePreset.THIS_MONTH -> {
                    time = to
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
        }.time

        val granularity = if (preset == AnalyticsRangePreset.TODAY) "hour" else "day"
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return Triple(formatter.format(from), formatter.format(to), granularity)
    }

    suspend fun bulkCreateItems(
        rows: List<BulkProductRow>,
        categoryId: String
    ): BulkCreateResult {
        val cleanCategoryId = categoryId.trim()
        if (cleanCategoryId.isBlank()) {
            _statusMessage.value = "Debe seleccionar una categoría."
            return BulkCreateResult(createdCount = 0, failedRows = rows.indices.map { it to "Categoría inválida" })
        }
        val validRows = rows.withIndex().filter { it.value.name.isNotBlank() || it.value.price.isNotBlank() }
        if (validRows.isEmpty()) {
            _statusMessage.value = "Agregá al menos un producto para cargar."
            return BulkCreateResult(createdCount = 0, failedRows = emptyList())
        }

        _isLoading.value = true
        touchStaffActivity()
        val failed = mutableListOf<Pair<Int, String>>()
        var createdCount = 0
        try {
            validRows.forEach { (index, row) ->
                val cleanName = row.name.trim()
                val formattedPrice = normalizePriceString(row.price)
                when {
                    cleanName.isBlank() -> {
                        failed.add(index to "Nombre obligatorio")
                        return@forEach
                    }
                    formattedPrice == null -> {
                        failed.add(index to "Precio inválido")
                        return@forEach
                    }
                }

                val request = CreateCatalogItemRequest(
                    categoryId = cleanCategoryId,
                    name = cleanName,
                    price = formattedPrice,
                    barcode = row.barcode?.trim()?.ifBlank { null },
                    isGeneric = false,
                    genericIcon = null,
                    trackStock = row.trackStock,
                    stockQuantity = row.stockQuantity.coerceAtLeast(0),
                    status = "draft"
                )

                try {
                    val res = api.createCatalogItem(tenantId, request)
                    val body = res.body()
                    if (res.isSuccessful && body != null) {
                        _items.value = _items.value + body
                        createdCount++
                    } else {
                        failed.add(index to "Error del servidor (HTTP ${res.code()})")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to create bulk item $index", e)
                    failed.add(index to "Error de red")
                }
            }

            _statusMessage.value = if (failed.isEmpty()) {
                "¡$createdCount productos guardados como borrador! Publicalos desde esta misma solapa."
            } else {
                "$createdCount borradores creados, ${failed.size} con error."
            }
        } finally {
            _isLoading.value = false
        }
        return BulkCreateResult(createdCount = createdCount, failedRows = failed)
    }

    suspend fun publishDrafts(): PublishDraftsResult {
        val draftCategories = _categories.value.filter { it.status == "draft" }
        val draftItems = _items.value.filter { it.status == "draft" }
        if (draftCategories.isEmpty() && draftItems.isEmpty()) {
            _statusMessage.value = "No hay borradores pendientes de publicación."
            return PublishDraftsResult(0, 0, emptyList())
        }

        _isLoading.value = true
        touchStaffActivity()
        val errors = mutableListOf<String>()
        val activeCategoryIds = _categories.value
            .filter { it.status == "active" }
            .map { it.id }
            .toMutableSet()
        var publishedCategories = 0
        var publishedItems = 0

        try {
            for (category in draftCategories) {
                try {
                    val res = api.updateCatalogCategory(
                        tenantId = tenantId,
                        categoryId = category.id,
                        ifMatch = category.version.toString(),
                        body = UpdateCatalogCategoryRequest(
                            status = "active",
                            version = category.version
                        )
                    )
                    val updated = res.body()
                    if (res.isSuccessful && updated != null) {
                        _categories.value = _categories.value.map {
                            if (it.id == updated.id) updated else it
                        }
                        activeCategoryIds.add(updated.id)
                        publishedCategories++
                    } else {
                        errors.add("Categoría ${category.name}: error HTTP ${res.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to publish category ${category.id}", e)
                    errors.add("Categoría ${category.name}: error de red")
                }
            }

            for (item in draftItems) {
                val categoryId = item.categoryId
                if (categoryId == null || categoryId !in activeCategoryIds) {
                    errors.add("Producto ${item.name}: su categoría todavía no está publicada")
                    continue
                }
                try {
                    val res = api.updateCatalogItem(
                        tenantId = tenantId,
                        itemId = item.id,
                        ifMatch = item.version.toString(),
                        body = UpdateCatalogItemRequest(
                            status = "active",
                            version = item.version
                        )
                    )
                    val updated = res.body()
                    if (res.isSuccessful && updated != null) {
                        _items.value = _items.value.map {
                            if (it.id == updated.id) updated else it
                        }
                        publishedItems++
                    } else {
                        errors.add("Producto ${item.name}: error HTTP ${res.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to publish item ${item.id}", e)
                    errors.add("Producto ${item.name}: error de red")
                }
            }

            _statusMessage.value = when {
                errors.isEmpty() -> "¡Publicación completa! $publishedCategories categorías y $publishedItems productos activos."
                else -> "$publishedCategories categorías y $publishedItems productos publicados, ${errors.size} con error."
            }
        } finally {
            _isLoading.value = false
        }

        return PublishDraftsResult(publishedCategories, publishedItems, errors)
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

    fun addToCart(item: CatalogItemDto, fromScan: Boolean = false) {
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

        if (fromScan) {
            val quantity = current.find { it.item.id == item.id }?.quantity ?: 1
            _lastScannedProduct.value = ScannedProductFeedback(
                productName = item.name,
                price = item.price,
                quantityInCart = quantity,
                barcode = item.barcode
            )
        }
    }

    fun clearScannedProductFeedback() {
        _lastScannedProduct.value = null
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
        touchStaffActivity()

        // 1. Fast in-memory lookup (<150ms)
        val localItem = _items.value.find { it.barcode == clean && it.status == "active" }
        if (localItem != null) {
            addToCart(localItem, fromScan = true)
            return
        }

        // 2. Fallback to API lookup
        _isLoading.value = true
        try {
            val res = api.lookupBarcode(tenantId, clean)
            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                if (body.item != null) {
                    addToCart(body.item, fromScan = true)
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

    suspend fun createCategory(name: String): CatalogCategoryDto? {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return null
        _isLoading.value = true
        touchStaffActivity()
        try {
            val res = api.createCategory(tenantId, CreateCategoryRequest(name = cleanName, status = "draft"))
            if (res.isSuccessful && res.body() != null) {
                val created = res.body()!!
                _categories.value = _categories.value + created
                return created
            } else {
                _statusMessage.value = "Error al crear la categoría."
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to create category", e)
            _statusMessage.value = "Error de conexión al crear categoría."
        } finally {
            _isLoading.value = false
        }
        return null
    }

    fun normalizePriceString(price: String): String? = Companion.normalizePriceString(price)

    private fun extractProblemDetail(response: retrofit2.Response<*>): String? {
        return try {
            val raw = response.errorBody()?.string()
            if (raw.isNullOrBlank()) return null
            val json = JSONObject(raw)
            json.optString("detail").takeIf { it.isNotBlank() }
                ?: json.optString("title").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private val MONEY_REGEX = Regex("""^\d{1,10}\.\d{2}$""")

        fun normalizePriceString(price: String): String? {
            val trimmed = price.trim().replace("$", "").replace(" ", "")
            if (trimmed.isBlank()) return null

            // Support numbers with thousand dots and decimal commas (e.g. 1.250,50 or 1.500)
            val clean = if (trimmed.contains(",") && trimmed.contains(".")) {
                trimmed.replace(".", "").replace(",", ".")
            } else {
                trimmed.replace(",", ".")
            }

            val doubleVal = clean.toDoubleOrNull() ?: return null
            if (doubleVal <= 0 || doubleVal.isNaN() || doubleVal.isInfinite()) return null
            val formatted = String.format(java.util.Locale.US, "%.2f", doubleVal)
            if (!formatted.matches(MONEY_REGEX)) return null
            return formatted
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
        stockQuantity: Int = 0,
        addToCart: Boolean = !isStaffSessionActive()
    ): Boolean {
        _isLoading.value = true
        touchStaffActivity()
        try {
            val formattedPrice = normalizePriceString(price)
            if (formattedPrice == null) {
                _statusMessage.value = "Precio inválido. Ingrese un valor numérico válido."
                return false
            }
            val cleanName = name.trim()
            if (cleanName.isBlank()) {
                _statusMessage.value = "El nombre del producto es obligatorio."
                return false
            }
            val cleanCategoryId = categoryId.trim()
            if (cleanCategoryId.isBlank()) {
                _statusMessage.value = "La categoría es obligatoria."
                return false
            }
            val request = CreateCatalogItemRequest(
                categoryId = cleanCategoryId,
                name = cleanName,
                price = formattedPrice,
                barcode = barcode?.trim()?.ifBlank { null },
                isGeneric = isGeneric,
                genericIcon = genericIcon,
                trackStock = trackStock,
                stockQuantity = stockQuantity,
                status = "draft"
            )

            val res = api.createCatalogItem(tenantId, request)
            if (res.isSuccessful && res.body() != null) {
                val created = res.body()!!
                _items.value = _items.value + created
                _pendingLookup.value = null
                _statusMessage.value = "¡Producto \"${created.name}\" guardado como borrador! Publicalo desde Backoffice > Productos."
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
                val body = res.body()!!
                startStaffSession(
                    userId = body.userId ?: "staff",
                    role = body.role ?: "employee",
                    email = email.trim()
                )
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
        customerName: String = "Cliente Autoservicio",
        discountCode: String? = null
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
                notes = if (paymentMethod == "cash") "Pago en efectivo en mostrador" else null,
                tender = if (paymentMethod == "cash") "cash" else null,
                paymentStatus = if (paymentMethod == "cash") "pending" else null,
                discountCode = discountCode?.trim()?.ifBlank { null }
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
                            total = order.total.toDoubleOrNull() ?: totalAmount
                        )
                    )
                    printerRouter?.printEspressoCashTicket(ticketPayload)
                }

                _cart.value = emptyList()
                _discountPreview.value = null
                _statusMessage.value = if (paymentMethod == "cash") {
                    "¡Pedido #$purchaseNum creado! Acercate a caja a abonar en efectivo."
                } else {
                    "¡Cobro confirmado! Ticket #$purchaseNum"
                }
                return true
            } else {
                attempt?.let { attemptStore?.markAttemptUnknown(tenantId, it) }
                _statusMessage.value = extractProblemDetail(res) ?: "Error al procesar el pedido."
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

    suspend fun applyDiscountCode(code: String, tender: String? = null): Boolean {
        val clean = code.trim().uppercase(Locale.US)
        if (clean.isBlank() || _cart.value.isEmpty()) return false

        _isDiscountPreviewLoading.value = true
        _discountError.value = null
        try {
            val request = KioskDiscountPreviewRequest(
                items = _cart.value.map { line ->
                    KioskPaymentItemRequest(
                        catalogItemId = line.item.id,
                        quantity = line.quantity
                    )
                },
                discountCode = clean,
                tender = tender
            )
            val res = api.previewKioskDiscount(tenantId, request)
            val body = res.body()
            if (res.isSuccessful && body != null) {
                if (body.eligible) {
                    _discountPreview.value = body
                    return true
                }
                _discountPreview.value = null
                _discountError.value = discountReasonMessage(body.reason)
            } else {
                _discountPreview.value = null
                _discountError.value = extractProblemDetail(res) ?: "No se pudo validar el código."
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to preview discount", e)
            _discountPreview.value = null
            _discountError.value = "Error de conexión al validar el código."
        } finally {
            _isDiscountPreviewLoading.value = false
        }
        return false
    }

    fun clearDiscountPreview() {
        _discountPreview.value = null
        _discountError.value = null
    }

    fun clearDiscountError() {
        _discountError.value = null
    }

    private fun discountReasonMessage(reason: String?): String {
        return when (reason) {
            "DISCOUNT_NOT_FOUND" -> "Código de descuento inexistente."
            "DISCOUNT_INACTIVE" -> "El cupón está pausado."
            "DISCOUNT_SCHEDULED" -> "El cupón todavía no está vigente."
            "DISCOUNT_EXPIRED" -> "El cupón está vencido."
            "DISCOUNT_LIMIT_REACHED" -> "El cupón alcanzó su límite de canjes."
            "MIN_ORDER_NOT_MET" -> "El pedido no alcanza la compra mínima del cupón."
            "NO_QUALIFYING_ITEMS" -> "El cupón no aplica a los productos del pedido."
            "COUPON_TENDER_MISMATCH" -> "El cupón es exclusivo para pagos en efectivo."
            else -> "El código de descuento no es aplicable."
        }
    }

    suspend fun startQrPayment(
        customerName: String = "Cliente Autoservicio",
        discountCode: String? = null
    ): KioskPaymentSessionResponse? {
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
                customer = KioskPaymentCustomerRequest(name = customerName),
                discountCode = discountCode?.trim()?.ifBlank { null }
            )

            val res = api.createKioskPaymentSession(tenantId, idempotencyKey, request)
            if (res.isSuccessful && res.body() != null) {
                val session = res.body()!!
                _activeQrSession.value = session
                return session
            } else {
                val detail = extractProblemDetail(res)
                _statusMessage.value = when {
                    detail != null -> detail
                    res.code() == 422 -> "Mercado Pago no está configurado en este local."
                    else -> "Error al iniciar sesión de pago con QR."
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
        val currentTotal = statusResponse.total?.toDoubleOrNull() ?: totalAmount
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
        _discountPreview.value = null
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
