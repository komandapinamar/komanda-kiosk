package com.komanda.kiosk.core.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KioskPaymentItemRequest(
    val catalogItemId: String,
    val quantity: Int
)

@JsonClass(generateAdapter = true)
data class KioskPaymentCustomerRequest(
    val name: String = "Cliente Autoservicio"
)

@JsonClass(generateAdapter = true)
data class KioskPaymentSessionRequest(
    val items: List<KioskPaymentItemRequest>,
    val customer: KioskPaymentCustomerRequest = KioskPaymentCustomerRequest(),
    val notes: String? = null,
    val discountCode: String? = null
)

@JsonClass(generateAdapter = true)
data class KioskDiscountPreviewRequest(
    val items: List<KioskPaymentItemRequest>,
    val discountCode: String,
    val tender: String? = null
)

@JsonClass(generateAdapter = true)
data class KioskDiscountPreviewResponse(
    val eligible: Boolean = false,
    val reason: String? = null,
    val code: String = "",
    val name: String? = null,
    val subtotal: String = "0.00",
    val discountTotal: String = "0.00",
    val total: String = "0.00",
    val applicableTender: String? = null
)

@JsonClass(generateAdapter = true)
data class KioskPaymentSessionResponse(
    val paymentAttemptId: String,
    val cartId: String,
    val total: String,
    val currency: String,
    val qrData: String,
    val expiresAt: String,
    val timeoutSeconds: Int = 120
)

@JsonClass(generateAdapter = true)
data class KioskPaymentCancelResponse(
    val status: String,
    val cancelledAt: String
)

@JsonClass(generateAdapter = true)
data class KioskPaymentStatusResponse(
    val status: String,
    val secondsRemaining: Int? = null,
    val orderId: String? = null,
    val purchaseNumber: String? = null,
    val total: String? = null,
    val paymentId: String? = null,
    val reason: String? = null
)
