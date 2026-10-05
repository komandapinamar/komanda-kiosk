package com.komanda.kiosk.core.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DirectOrderItemRequest(
    val kind: String = "item",
    val resourceId: String,
    val quantity: Int = 1,
    val note: String? = null
)

@JsonClass(generateAdapter = true)
data class DirectOrderCustomerRequest(
    val name: String,
    val phone: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateDirectOrderRequest(
    val items: List<DirectOrderItemRequest>,
    val customer: DirectOrderCustomerRequest,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderLineDto(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val unitPrice: String = "0.00",
    val lineTotal: String = "0.00"
)

@JsonClass(generateAdapter = true)
data class OrderDto(
    val id: String,
    val tenantId: String,
    val locationId: String,
    val purchaseNumber: String,
    val fulfillmentStatus: String,
    val paymentStatus: String,
    val source: String,
    val notes: String? = null,
    val lines: List<OrderLineDto> = emptyList(),
    val subtotal: String = "0.00",
    val discountTotal: String = "0.00",
    val total: String = "0.00",
    val currency: String = "ARS"
)

@JsonClass(generateAdapter = true)
data class CatalogCategoryDto(
    val id: String,
    val name: String,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class CatalogItemDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: String,
    val categoryId: String? = null,
    val barcode: String? = null,
    val isGeneric: Boolean = false,
    val genericIcon: String? = null,
    val trackStock: Boolean = false,
    val stockQuantity: Int = 0,
    val status: String = "active"
)

@JsonClass(generateAdapter = true)
data class CreateCategoryRequest(
    val name: String,
    val description: String? = null,
    val status: String = "active"
)

@JsonClass(generateAdapter = true)
data class CreateCatalogItemRequest(
    val categoryId: String,
    val name: String,
    val price: String,
    val currency: String = "ARS",
    val barcode: String? = null,
    val isGeneric: Boolean = false,
    val genericIcon: String? = null,
    val trackStock: Boolean = false,
    val stockQuantity: Int = 0,
    val status: String = "active"
)

@JsonClass(generateAdapter = true)
data class BarcodeSuggestionDto(
    val name: String,
    val suggestedCategory: String? = null,
    val imageUrl: String? = null,
    val brand: String? = null
)

@JsonClass(generateAdapter = true)
data class BarcodeLookupResponseDto(
    val source: String, // "tenant", "global", "external", "none"
    val item: CatalogItemDto? = null,
    val suggestion: BarcodeSuggestionDto? = null
)

@JsonClass(generateAdapter = true)
data class CatalogResponse<T>(
    val data: List<T>
)

@JsonClass(generateAdapter = true)
data class CashShiftDto(
    val id: String,
    val tenantId: String,
    val openingBalance: String,
    val closingBalance: String? = null,
    val expectedCash: String? = null,
    val currentCashSales: String? = null,
    val orderCount: Int = 0,
    val status: String, // "open" | "closed"
    val openedAt: String,
    val closedAt: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class CashShiftResponse(
    val data: CashShiftDto?
)

@JsonClass(generateAdapter = true)
data class OpenCashShiftRequest(
    val openingBalance: String,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class CloseCashShiftRequest(
    val closingBalance: String,
    val notes: String? = null
)
