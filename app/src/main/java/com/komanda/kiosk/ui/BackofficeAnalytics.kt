package com.komanda.kiosk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.komanda.kiosk.core.network.CashMovementDto
import com.komanda.kiosk.core.network.CustomerRecurrenceDto
import com.komanda.kiosk.core.network.DashboardAnalyticsDto
import com.komanda.kiosk.core.network.KitchenDelaysDto
import com.komanda.kiosk.core.network.PeakHoursHeatmapCellDto
import com.komanda.kiosk.core.network.PromotionsDto
import com.komanda.kiosk.core.network.TenderBreakdownDto
import com.komanda.kiosk.core.network.TopProductDto
import com.komanda.kiosk.core.network.UnconvertedProductDto
import com.komanda.kiosk.ui.theme.Emerald600
import com.komanda.kiosk.ui.theme.KomandaTokens
import com.komanda.kiosk.ui.theme.Zinc300
import com.komanda.kiosk.ui.theme.Zinc400
import com.komanda.kiosk.ui.theme.Zinc700
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

private val Emerald400 = Color(0xFF34D399)
private val Red400 = Color(0xFFF87171)
private val Amber300 = Color(0xFFFCD34D)

@Composable
fun BackofficeAnalyticsSection(
    analytics: DashboardAnalyticsDto?,
    preset: AnalyticsRangePreset,
    isLoading: Boolean,
    error: String?,
    onPresetChange: (AnalyticsRangePreset) -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Zinc800, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analíticas del negocio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Mismas métricas del Backoffice Komanda (solo Propietario)",
                        fontSize = 12.sp,
                        color = Zinc400
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRefresh, enabled = !isLoading) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar analíticas",
                            tint = if (isLoading) Zinc700 else KomandaTokens.AccentTertiary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Período:", fontSize = 12.sp, color = Zinc400, fontWeight = FontWeight.SemiBold)
                AnalyticsRangePreset.values().forEach { option ->
                    val selected = option == preset
                    Button(
                        onClick = { onPresetChange(option) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) KomandaTokens.AccentTertiary else Zinc800,
                            contentColor = if (selected) KomandaTokens.AccentPrimary else Zinc300
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(option.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (!error.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Red400.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Red400.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = Red400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            if (analytics == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = KomandaTokens.AccentTertiary)
                    } else {
                        Text("Sin datos analíticos para mostrar.", color = Zinc400, fontSize = 13.sp)
                    }
                }
                return@Column
            }

            AnalyticsKpiCards(analytics)
            AnalyticsKioskCashPanel(analytics)
            AnalyticsRevenueTimeline(analytics)
            AnalyticsProductsPanel(analytics)
            AnalyticsFinancialPanel(analytics)
            AnalyticsPeakHoursPanel(analytics)
            AnalyticsKitchenPanel(analytics.kitchenDelays)
            AnalyticsCustomersAndPromotions(
                recurrence = analytics.customerRecurrence,
                promotions = analytics.promotions
            )
        }
    }
}

@Composable
private fun AnalyticsKpiCards(data: DashboardAnalyticsDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KpiCard(
            label = "Facturación total",
            value = formatMoney(data.financial.totalRevenue),
            subtext = "Subtotal: ${formatMoney(data.financial.subtotal)}",
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label = "Tiempo en carta",
            value = formatSeconds(data.dwell.avgDwellSeconds),
            subtext = "${data.dwell.totalSessions} visitas · ${data.dwell.orderPlacedSessions} con compra",
            highlight = KomandaTokens.AccentTertiary,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label = "Ticket promedio",
            value = formatMoney(data.financial.avgOrderValue),
            subtext = "${data.financial.paidOrdersCount} pedidos pagados",
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label = "Conversión & rebote",
            value = "${formatDecimal(data.dwell.conversionRate)}%",
            subtext = "Rebote: ${formatDecimal(data.dwell.bounceRate)}% · ${data.dwell.bounceSessions} sesiones",
            highlight = Emerald400,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AnalyticsKioskCashPanel(data: DashboardAnalyticsDto) {
    val cashTender = data.tenderBreakdown?.find { it.tender == "cash" }
    val digitalTender = data.tenderBreakdown?.find { it.tender == "posnet" || it.tender == "mercadopago" }
    val shift = data.currentCashShift

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KioskCashCard(
            title = "Total en caja hoy",
            value = if (shift != null) formatMoney(shift.expectedCash) else formatMoney(data.financial.cashLedger.netCashAmount),
            badge = if (shift != null) "Turno abierto" else "Caja cerrada",
            detail = if (shift != null) {
                "Apertura: ${formatMoney(shift.openingBalance)}\nEfectivo: ${formatMoney(shift.currentCashSales)}"
            } else {
                "Iniciá turno desde Control de Caja."
            },
            modifier = Modifier.weight(1f)
        )
        KioskCashCard(
            title = "Cobros en efectivo",
            value = formatMoney(cashTender?.revenue ?: "0.00"),
            badge = "${cashTender?.ordersCount ?: 0} pedidos",
            detail = "Recaudado en caja física en el período.",
            modifier = Modifier.weight(1f)
        )
        KioskCashCard(
            title = "Tarjetas / digital",
            value = formatMoney(digitalTender?.revenue ?: "0.00"),
            badge = "${digitalTender?.ordersCount ?: 0} pedidos",
            detail = "Cobros procesados por terminal electrónica.",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AnalyticsRevenueTimeline(data: DashboardAnalyticsDto) {
    PanelCard(title = "Evolución de facturación y pedidos", subtitle = "Ingresos y volumen por intervalo") {
        if (data.revenueTimeline.isEmpty()) {
            EmptyHint("Sin registros de facturación en el período.")
            return@PanelCard
        }

        val maxRevenue = data.revenueTimeline.maxOfOrNull { it.revenue.toDoubleOrNull() ?: 0.0 } ?: 0.0
        val safeMax = max(maxRevenue, 1.0)
        val granularity = data.dateRange.granularity

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.revenueTimeline.forEach { item ->
                val revenue = item.revenue.toDoubleOrNull() ?: 0.0
                val ratio = (revenue / safeMax).coerceIn(0.04, 1.0)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(52.dp)
                ) {
                    Text(
                        text = formatCompactMoney(revenue),
                        fontSize = 10.sp,
                        color = Zinc400,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height((110 * ratio).dp.coerceAtLeast(6.dp))
                            .background(Emerald600, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatBucket(item.bucket, granularity),
                        fontSize = 10.sp,
                        color = Zinc400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.ordersCount} ped.",
                        fontSize = 9.sp,
                        color = Zinc700
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsProductsPanel(data: DashboardAnalyticsDto) {
    var byRevenue by remember { mutableStateOf(false) }
    val topProducts: List<TopProductDto> =
        if (byRevenue) data.topProducts.byRevenue else data.topProducts.byQuantity
    val unconverted = data.unconvertedProducts ?: emptyList()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PanelCard(
            title = "Productos más vendidos",
            subtitle = "Ranking por unidades o facturación",
            modifier = Modifier.weight(1f)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniToggle("Por unidades", !byRevenue) { byRevenue = false }
                MiniToggle("Por facturación", byRevenue) { byRevenue = true }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (topProducts.isEmpty()) {
                EmptyHint("Sin ventas en el período.")
            } else {
                val maxValue = max(
                    topProducts.maxOfOrNull { if (byRevenue) it.totalRevenue.toDoubleOrNull() ?: 0.0 else it.totalQuantity.toDouble() } ?: 1.0,
                    1.0
                )
                topProducts.take(7).forEachIndexed { index, product ->
                    val value = if (byRevenue) product.totalRevenue.toDoubleOrNull() ?: 0.0 else product.totalQuantity.toDouble()
                    val fraction = (value / maxValue).toFloat().coerceIn(0.05f, 1f)
                    Column(modifier = Modifier.padding(vertical = 5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "#${index + 1}  ${product.productName}",
                                fontSize = 12.sp,
                                color = Zinc300,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${product.totalQuantity} u. · ${formatMoney(product.totalRevenue)}",
                                fontSize = 11.sp,
                                color = Emerald400,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .background(Zinc800, RoundedCornerShape(20.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(7.dp)
                                    .background(KomandaTokens.AccentTertiary, RoundedCornerShape(20.dp))
                            )
                        }
                    }
                }
            }
        }

        PanelCard(
            title = "Miran mucho pero no piden",
            subtitle = "Alta atención y baja conversión",
            modifier = Modifier.weight(1f)
        ) {
            if (unconverted.isEmpty()) {
                EmptyHint("Sin fricción detectada en el menú para este período.")
            } else {
                unconverted.take(6).forEach { product ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Zinc950,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    product.productName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                SuggestionBadge(product.suggestion)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${product.qualifiedViews} visitas · ${product.purchasesCount} pedidos · ${formatDecimal(product.conversionRate)}% conversión",
                                fontSize = 11.sp,
                                color = Zinc400
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsFinancialPanel(data: DashboardAnalyticsDto) {
    val financial = data.financial
    val mp = financial.mpWaterfall
    val cash = financial.cashLedger

    PanelCard(
        title = "Liquidación Mercado Pago y caja en efectivo",
        subtitle = "Cascada de cobros con deducciones reales y movimientos de mostrador"
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Zinc950,
            border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Monto neto luego de comisiones e impuestos", fontSize = 11.sp, color = Zinc400, fontWeight = FontWeight.SemiBold)
                    Text(formatMoney(financial.netCollections), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Emerald400)
                }
                if ((mp.pendingReleaseAmount.toDoubleOrNull() ?: 0.0) > 0.0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Plata por liberar", fontSize = 11.sp, color = Amber300, fontWeight = FontWeight.SemiBold)
                        Text(formatMoney(mp.pendingReleaseAmount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Amber300)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Cascada Mercado Pago", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                LedgerRow("Total cobrado", formatMoney(mp.grossAmount), Zinc300)
                LedgerRow("Comisiones", "-${formatMoney(mp.feeAmount)}", Amber300)
                LedgerRow("Impuestos y retenciones", "-${formatMoney(mp.taxesAmount)}", Amber300)
                LedgerRow("Devoluciones", "-${formatMoney(mp.refundsAmount)}", Red400)
                LedgerRow("Neto recibido", formatMoney(mp.netReceivedAmount), Emerald400, bold = true)
                Text("Deducción efectiva: ${mp.effectiveDeductionRate}", fontSize = 10.sp, color = Zinc400)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Libro de caja (efectivo)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                LedgerRow("Ingresos por ventas", "+${formatMoney(cash.depositsAmount)}", Emerald400)
                LedgerRow("Retiros por cancelaciones", "-${formatMoney(cash.withdrawalsAmount)}", Red400)
                LedgerRow("Movimiento neto", formatMoney(cash.netCashAmount), Zinc300, bold = true)
                Spacer(modifier = Modifier.height(6.dp))
                Text("${cash.movementsCount} movimientos registrados", fontSize = 10.sp, color = Zinc400)
                cash.recentMovements.take(3).forEach { movement ->
                    MovementRow(movement)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsPeakHoursPanel(data: DashboardAnalyticsDto) {
    val heatmap = data.peakHoursHeatmap
    val cells = heatmap?.cells?.filter { it.orderCount > 0 }?.sortedByDescending { it.orderCount } ?: emptyList()

    PanelCard(
        title = "Horarios con más trabajo",
        subtitle = buildString {
            append("Demanda observada por día y hora")
            heatmap?.busiestDay?.let { day ->
                append(" · Pico semanal: ")
                append(day)
                heatmap.busiestHour?.let { hour ->
                    append(" ")
                    append(String.format(Locale.US, "%02d", hour))
                    append(":00 hs")
                }
            }
        }
    ) {
        if (cells.isEmpty()) {
            EmptyHint("Sin pedidos registrados para calcular horarios.")
            return@PanelCard
        }
        val maxOrders = max(heatmap?.maxOrdersInSlot ?: 1, 1)
        cells.take(8).forEach { cell ->
            PeakHourRow(cell, maxOrders)
        }
    }
}

@Composable
private fun AnalyticsKitchenPanel(kitchen: KitchenDelaysDto?) {
    if (kitchen == null) return

    PanelCard(
        title = "Demoras en cocina",
        subtitle = "Tiempos observados de preparación y comandas en curso"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KitchenMetric("Espera", kitchen.waitingMinutes.p50, kitchen.waitingMinutes.p90, kitchen.waitingMinutes.sampleSize, Modifier.weight(1f))
            KitchenMetric("Preparación", kitchen.preparationMinutes.p50, kitchen.preparationMinutes.p90, kitchen.preparationMinutes.sampleSize, Modifier.weight(1f))
            KitchenMetric("Total hasta listo", kitchen.totalTimeMinutes.p50, kitchen.totalTimeMinutes.p90, kitchen.totalTimeMinutes.sampleSize, Modifier.weight(1f))
        }

        if (kitchen.pendingOrders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text("Comandas en curso (${kitchen.pendingOrders.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            kitchen.pendingOrders.sortedByDescending { it.waitingAgeMinutes }.take(6).forEach { order ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "#${order.purchaseNumber} · ${if (order.fulfillmentStatus == "preparing") "En cocina" else "En cola"}",
                        fontSize = 12.sp,
                        color = Zinc300
                    )
                    Text(
                        "${order.waitingAgeMinutes} min · ${formatMoney(order.total)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (order.waitingAgeMinutes >= 25) Red400 else Amber300
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsCustomersAndPromotions(
    recurrence: CustomerRecurrenceDto?,
    promotions: PromotionsDto?
) {
    if (recurrence == null && promotions == null) return

    PanelCard(
        title = "Clientes y promociones",
        subtitle = "Recompra, frecuencia y desempeño de descuentos"
    ) {
        if (recurrence != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallMetric("Tasa de recompra", "${formatDecimal(recurrence.repeatRate)}%", Modifier.weight(1f))
                SmallMetric("Tiempo entre compras", recurrence.medianDaysDisplay, Modifier.weight(1f))
                SmallMetric("Frecuencia", "${formatDecimal(recurrence.orderFrequency)} /cliente", Modifier.weight(1f))
                SmallMetric("Cobertura identidad", "${formatDecimal(recurrence.identityCoverageRate)}%", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SegmentCard("Recurrentes", recurrence.segments.recurrent.customerCount, recurrence.segments.recurrent.orderCount, recurrence.segments.recurrent.totalRevenue, recurrence.segments.recurrent.avgTicket, Modifier.weight(1f))
                SegmentCard("Nuevos", recurrence.segments.firstTime.customerCount, recurrence.segments.firstTime.orderCount, recurrence.segments.firstTime.totalRevenue, recurrence.segments.firstTime.avgTicket, Modifier.weight(1f))
                SegmentCard("Sin identificar", null, recurrence.segments.unidentified.orderCount, recurrence.segments.unidentified.totalRevenue, recurrence.segments.unidentified.avgTicket, Modifier.weight(1f))
            }
        }

        if (promotions != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallMetric("Uso de descuentos", "${formatDecimal(promotions.discountAdoptionRate)}%", Modifier.weight(1f))
                SmallMetric("Descuentos otorgados", formatMoney(promotions.totalDiscountsApplied), Modifier.weight(1f))
                SmallMetric("Ticket con promo", formatMoney(promotions.ticketComparison.discountedAvgTicket), Modifier.weight(1f))
                SmallMetric("Ticket sin promo", formatMoney(promotions.ticketComparison.fullPriceAvgTicket), Modifier.weight(1f))
            }
        }
    }
}

// ---------- Reusable pieces ----------

@Composable
private fun KpiCard(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier,
    highlight: Color = Color.White
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Zinc950,
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label.uppercase(Locale.US), fontSize = 10.sp, color = Zinc400, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = highlight)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtext, fontSize = 10.sp, color = Zinc400, maxLines = 2)
        }
    }
}

@Composable
private fun KioskCashCard(
    title: String,
    value: String,
    badge: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Zinc950,
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title.uppercase(Locale.US), fontSize = 10.sp, color = Zinc400, fontWeight = FontWeight.SemiBold)
                Surface(shape = RoundedCornerShape(20.dp), color = Zinc800) {
                    Text(badge, fontSize = 9.sp, color = Zinc300, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = KomandaTokens.AccentTertiary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(detail, fontSize = 10.sp, color = Zinc400, maxLines = 3)
        }
    }
}

@Composable
private fun PanelCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Zinc950,
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 11.sp, color = Zinc400)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 11.sp, color = Zinc400, textAlign = TextAlign.Center)
    }
}

@Composable
private fun MiniToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) KomandaTokens.AccentTertiary else Zinc800,
            contentColor = if (selected) KomandaTokens.AccentPrimary else Zinc300
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SuggestionBadge(suggestion: String) {
    val color = when {
        suggestion.contains("foto", ignoreCase = true) -> Red400
        suggestion.contains("precio", ignoreCase = true) -> Color(0xFF7DD3FC)
        else -> Amber300
    }
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.14f)) {
        Text(
            suggestion,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun LedgerRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Zinc400)
        Text(value, fontSize = 11.sp, color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold)
    }
}

@Composable
private fun MovementRow(movement: CashMovementDto) {
    val isDeposit = movement.type == "sale_deposit"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            if (isDeposit) "Ingreso por venta" else "Retiro por cancelación",
            fontSize = 10.sp,
            color = Zinc400
        )
        Text(
            "${if (isDeposit) "+" else "-"}${formatMoney(movement.amount)}",
            fontSize = 10.sp,
            color = if (isDeposit) Emerald400 else Red400
        )
    }
}

@Composable
private fun PeakHourRow(cell: PeakHoursHeatmapCellDto, maxOrders: Int) {
    val fraction = (cell.orderCount.toFloat() / maxOrders).coerceIn(0.05f, 1f)
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${cell.dayName} ${String.format(Locale.US, "%02d", cell.hour)}:00 hs",
                fontSize = 11.sp,
                color = Zinc300
            )
            Text(
                text = "${cell.orderCount} pedidos · ${formatMoney(cell.revenue)}",
                fontSize = 11.sp,
                color = if (cell.isPeak) Amber300 else Emerald400,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Zinc800, RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(if (cell.isPeak) Amber300 else KomandaTokens.AccentTertiary, RoundedCornerShape(20.dp))
            )
        }
    }
}

@Composable
private fun KitchenMetric(label: String, p50: Double, p90: Double, sampleSize: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Zinc900,
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label.uppercase(Locale.US), fontSize = 9.sp, color = Zinc400, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${formatDecimal(p50)} min", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("p90: ${formatDecimal(p90)} min · ${sampleSize} ped.", fontSize = 9.sp, color = Zinc400)
        }
    }
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Zinc900,
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label.uppercase(Locale.US), fontSize = 9.sp, color = Zinc400, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun SegmentCard(
    title: String,
    customers: Int?,
    orders: Int,
    revenue: String,
    avgTicket: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Zinc900,
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Zinc300)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$orders pedidos", fontSize = 10.sp, color = Zinc400)
            Text(formatMoney(revenue), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald400)
            Text("Ticket: ${formatMoney(avgTicket)}", fontSize = 10.sp, color = Zinc400)
            if (customers != null) {
                Text("$customers clientes", fontSize = 9.sp, color = Zinc700)
            }
        }
    }
}

// ---------- Formatting helpers ----------

private fun formatMoney(raw: String?): String {
    val value = raw?.toDoubleOrNull() ?: 0.0
    val formatter = NumberFormat.getNumberInstance(Locale("es", "AR")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "$ " + formatter.format(value)
}

private fun formatCompactMoney(value: Double): String {
    return when {
        value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000)
        value >= 1_000 -> String.format(Locale.US, "%.0fk", value / 1_000)
        else -> String.format(Locale.US, "%.0f", value)
    }
}

private fun formatDecimal(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private fun formatSeconds(seconds: Int): String {
    if (seconds < 60) return "${seconds}s"
    val minutes = seconds / 60
    val remaining = seconds % 60
    return if (remaining == 0) "${minutes}m" else "${minutes}m ${remaining}s"
}

private fun formatBucket(bucket: String, granularity: String): String {
    return try {
        val datePart = bucket.substringBefore("T")
        val timePart = bucket.substringAfter("T", "")
        if (granularity == "hour") {
            timePart.take(5)
        } else {
            val parts = datePart.split("-")
            if (parts.size == 3) "${parts[2]}/${parts[1]}" else datePart
        }
    } catch (_: Exception) {
        bucket
    }
}
