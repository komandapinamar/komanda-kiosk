package com.komanda.kiosk.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface KomandaApi {

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/mobile/sessions")
    suspend fun mobileLogin(
        @Body body: MobileLoginRequest
    ): Response<MobileLoginResponse>

    @retrofit2.http.DELETE("/api/v1/auth/mobile/sessions")
    suspend fun mobileRevokeSession(
        @Header("Authorization") authHeader: String? = null
    ): Response<Unit>

    @GET("/api/v1/auth/mobile/context")
    suspend fun getMobileContext(
        @Header("Authorization") authHeader: String? = null
    ): Response<MobileContextResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/tenants/{tenantId}/orders/from-items")
    suspend fun createDirectOrder(
        @Path("tenantId") tenantId: String,
        @Header("idempotency-key") idempotencyKey: String,
        @Body body: CreateDirectOrderRequest
    ): Response<OrderDto>

    @GET("/api/v1/tenants/{tenantId}/catalog/categories")
    suspend fun listCategories(
        @Path("tenantId") tenantId: String
    ): Response<CatalogResponse<CatalogCategoryDto>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/tenants/{tenantId}/catalog/categories")
    suspend fun createCategory(
        @Path("tenantId") tenantId: String,
        @Body body: CreateCategoryRequest
    ): Response<CatalogCategoryDto>

    @GET("/api/v1/tenants/{tenantId}/catalog/items")
    suspend fun listItems(
        @Path("tenantId") tenantId: String
    ): Response<CatalogResponse<CatalogItemDto>>

    @GET("/api/v1/tenants/{tenantId}/catalog/lookup")
    suspend fun lookupBarcode(
        @Path("tenantId") tenantId: String,
        @Query("barcode") barcode: String
    ): Response<BarcodeLookupResponseDto>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/tenants/{tenantId}/catalog/items")
    suspend fun createCatalogItem(
        @Path("tenantId") tenantId: String,
        @Body body: CreateCatalogItemRequest
    ): Response<CatalogItemDto>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/tenants/{tenantId}/auth/verify-staff")
    suspend fun verifyStaff(
        @Path("tenantId") tenantId: String,
        @Body body: VerifyStaffRequest
    ): Response<VerifyStaffResponse>

    @GET("/api/v1/tenants/{tenantId}/cash-shifts/current")
    suspend fun getCurrentCashShift(
        @Path("tenantId") tenantId: String
    ): Response<CashShiftResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/tenants/{tenantId}/cash-shifts/open")
    suspend fun openCashShift(
        @Path("tenantId") tenantId: String,
        @Body body: OpenCashShiftRequest
    ): Response<CashShiftDto>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/tenants/{tenantId}/cash-shifts/{shiftId}/close")
    suspend fun closeCashShift(
        @Path("tenantId") tenantId: String,
        @Path("shiftId") shiftId: String,
        @Body body: CloseCashShiftRequest
    ): Response<CashShiftDto>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/tenants/{tenantId}/kiosk/payment-sessions")
    suspend fun createKioskPaymentSession(
        @Path("tenantId") tenantId: String,
        @Header("idempotency-key") idempotencyKey: String,
        @Body body: KioskPaymentSessionRequest
    ): Response<KioskPaymentSessionResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/tenants/{tenantId}/kiosk/payment-attempts/{attemptId}/cancel")
    suspend fun cancelKioskPaymentAttempt(
        @Path("tenantId") tenantId: String,
        @Path("attemptId") attemptId: String,
        @Header("idempotency-key") idempotencyKey: String
    ): Response<KioskPaymentCancelResponse>

    @GET("/api/v1/tenants/{tenantId}/kiosk/payment-attempts/{attemptId}/status")
    suspend fun getKioskPaymentStatus(
        @Path("tenantId") tenantId: String,
        @Path("attemptId") attemptId: String
    ): Response<KioskPaymentStatusResponse>
}
