package com.komanda.kiosk.core.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnalyticsDateRangeDto(
    val from: String = "",
    val to: String = "",
    val granularity: String = "day"
)

@JsonClass(generateAdapter = true)
data class AnalyticsDwellDto(
    val totalSessions: Int = 0,
    val avgDwellSeconds: Int = 0,
    val maxDwellSeconds: Int = 0,
    val bounceSessions: Int = 0,
    val cartCreatedSessions: Int = 0,
    val orderPlacedSessions: Int = 0,
    val bounceRate: Double = 0.0,
    val conversionRate: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class DwellTimelineItemDto(
    val bucket: String = "",
    val sessions: Int = 0,
    val avgDwellSeconds: Int = 0,
    val orders: Int = 0
)

@JsonClass(generateAdapter = true)
data class RevenueBySourceDto(
    val source: String = "",
    val revenue: String = "0.00",
    val orders: Int = 0
)

@JsonClass(generateAdapter = true)
data class MpWaterfallReleaseItemDto(
    val mpPaymentId: String = "",
    val amount: String = "0.00",
    val expectedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class MpWaterfallDto(
    val grossAmount: String = "0.00",
    val feeAmount: String = "0.00",
    val taxesAmount: String = "0.00",
    val refundsAmount: String = "0.00",
    val netReceivedAmount: String = "0.00",
    val pendingReleaseAmount: String = "0.00",
    val releasedAmount: String = "0.00",
    val effectiveDeductionRate: String = "0.00%",
    val isFeeInclusiveOfTax: Boolean = false,
    val releaseSchedule: List<MpWaterfallReleaseItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CashMovementDto(
    val id: String = "",
    val orderId: String = "",
    val type: String = "sale_deposit",
    val amount: String = "0.00",
    val occurredAt: String = ""
)

@JsonClass(generateAdapter = true)
data class CashLedgerDto(
    val depositsAmount: String = "0.00",
    val withdrawalsAmount: String = "0.00",
    val netCashAmount: String = "0.00",
    val movementsCount: Int = 0,
    val recentMovements: List<CashMovementDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class FinancialSummaryDto(
    val totalRevenue: String = "0.00",
    val subtotal: String = "0.00",
    val totalDiscounts: String = "0.00",
    val paidOrdersCount: Int = 0,
    val avgOrderValue: String = "0.00",
    val revenueBySource: List<RevenueBySourceDto> = emptyList(),
    val netCollections: String = "0.00",
    val mpWaterfall: MpWaterfallDto = MpWaterfallDto(),
    val cashLedger: CashLedgerDto = CashLedgerDto()
)

@JsonClass(generateAdapter = true)
data class RevenueTimelineItemDto(
    val bucket: String = "",
    val revenue: String = "0.00",
    val ordersCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class TopProductDto(
    val productName: String = "",
    val sourceItemId: String? = null,
    val sourceComboId: String? = null,
    val categoryName: String? = null,
    val totalQuantity: Int = 0,
    val totalRevenue: String = "0.00"
)

@JsonClass(generateAdapter = true)
data class TopProductsDto(
    val byQuantity: List<TopProductDto> = emptyList(),
    val byRevenue: List<TopProductDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PercentileMetricsDto(
    val p50: Double = 0.0,
    val p90: Double = 0.0,
    val sampleSize: Int = 0
)

@JsonClass(generateAdapter = true)
data class PendingKitchenOrderDto(
    val orderId: String = "",
    val purchaseNumber: String = "",
    val fulfillmentStatus: String = "approved",
    val waitingAgeMinutes: Int = 0,
    val enteredAt: String = "",
    val total: String = "0.00"
)

@JsonClass(generateAdapter = true)
data class KitchenDelaysDto(
    val waitingMinutes: PercentileMetricsDto = PercentileMetricsDto(),
    val preparationMinutes: PercentileMetricsDto = PercentileMetricsDto(),
    val totalTimeMinutes: PercentileMetricsDto = PercentileMetricsDto(),
    val pendingOrders: List<PendingKitchenOrderDto> = emptyList(),
    val cancelledBeforeCookingCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class PeakHoursHeatmapCellDto(
    val dayOfWeek: Int = 0,
    val dayName: String = "",
    val hour: Int = 0,
    val orderCount: Int = 0,
    val revenue: String = "0.00",
    val isPeak: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PeakHoursHeatmapDto(
    val cells: List<PeakHoursHeatmapCellDto> = emptyList(),
    val maxOrdersInSlot: Int = 0,
    val busiestDay: String? = null,
    val busiestHour: Int? = null
)

@JsonClass(generateAdapter = true)
data class UnconvertedProductDto(
    val itemId: String = "",
    val productName: String = "",
    val categoryName: String? = null,
    val imageUrl: String? = null,
    val price: String? = null,
    val qualifiedViews: Int = 0,
    val purchasesCount: Int = 0,
    val conversionRate: Double = 0.0,
    val suggestion: String = ""
)

@JsonClass(generateAdapter = true)
data class CustomerSegmentMetricsDto(
    val customerCount: Int = 0,
    val orderCount: Int = 0,
    val totalRevenue: String = "0.00",
    val avgTicket: String = "0.00"
)

@JsonClass(generateAdapter = true)
data class CustomerRecurrenceSegmentsDto(
    val firstTime: CustomerSegmentMetricsDto = CustomerSegmentMetricsDto(),
    val recurrent: CustomerSegmentMetricsDto = CustomerSegmentMetricsDto(),
    val unidentified: CustomerSegmentMetricsDto = CustomerSegmentMetricsDto()
)

@JsonClass(generateAdapter = true)
data class CustomerRecurrenceDto(
    val totalCustomers: Int = 0,
    val firstTimeCustomers: Int = 0,
    val recurrentCustomers: Int = 0,
    val repeatRate: Double = 0.0,
    val orderFrequency: Double = 0.0,
    val medianDaysBetweenOrders: Double? = null,
    val medianDaysDisplay: String = "-",
    val identityCoverageRate: Double = 0.0,
    val identifiedOrdersCount: Int = 0,
    val unidentifiedOrdersCount: Int = 0,
    val totalOrdersCount: Int = 0,
    val segments: CustomerRecurrenceSegmentsDto = CustomerRecurrenceSegmentsDto()
)

@JsonClass(generateAdapter = true)
data class TicketComparisonDto(
    val discountedAvgTicket: String = "0.00",
    val fullPriceAvgTicket: String = "0.00",
    val differenceAmount: String = "0.00",
    val differencePercent: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class PromotionsDto(
    val totalOrdersCount: Int = 0,
    val discountedOrdersCount: Int = 0,
    val fullPriceOrdersCount: Int = 0,
    val discountAdoptionRate: Double = 0.0,
    val totalDiscountsApplied: String = "0.00",
    val discountedRevenue: String = "0.00",
    val fullPriceRevenue: String = "0.00",
    val ticketComparison: TicketComparisonDto = TicketComparisonDto()
)

@JsonClass(generateAdapter = true)
data class TenderBreakdownDto(
    val tender: String = "",
    val revenue: String = "0.00",
    val ordersCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class CurrentCashShiftDto(
    val id: String = "",
    val openingBalance: String = "0.00",
    val expectedCash: String = "0.00",
    val currentCashSales: String = "0.00",
    val status: String = "open",
    val openedAt: String = ""
)

@JsonClass(generateAdapter = true)
data class DashboardAnalyticsDto(
    val dateRange: AnalyticsDateRangeDto = AnalyticsDateRangeDto(),
    val dwell: AnalyticsDwellDto = AnalyticsDwellDto(),
    val dwellTimeline: List<DwellTimelineItemDto> = emptyList(),
    val financial: FinancialSummaryDto = FinancialSummaryDto(),
    val revenueTimeline: List<RevenueTimelineItemDto> = emptyList(),
    val topProducts: TopProductsDto = TopProductsDto(),
    val kitchenDelays: KitchenDelaysDto? = null,
    val peakHoursHeatmap: PeakHoursHeatmapDto? = null,
    val unconvertedProducts: List<UnconvertedProductDto>? = null,
    val customerRecurrence: CustomerRecurrenceDto? = null,
    val promotions: PromotionsDto? = null,
    val tenderBreakdown: List<TenderBreakdownDto>? = null,
    val currentCashShift: CurrentCashShiftDto? = null
)
