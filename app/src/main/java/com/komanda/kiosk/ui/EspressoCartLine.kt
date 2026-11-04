package com.komanda.kiosk.ui

import com.komanda.kiosk.core.network.CatalogItemDto
import java.util.Locale
import java.util.UUID

data class EspressoCartLine(
    val lineId: String = UUID.randomUUID().toString(),
    val item: CatalogItemDto,
    val quantity: Int,
    val calculatedSubtotal: Double? = null
) {
    val isWeighted: Boolean
        get() = item.itemType == "weighted" || calculatedSubtotal != null

    val lineTotal: Double
        get() = if (isWeighted && calculatedSubtotal != null) {
            calculatedSubtotal
        } else {
            (item.price.toDoubleOrNull() ?: 0.0) * quantity
        }

    val calculatedSubtotalFormatted: String?
        get() = if (isWeighted && calculatedSubtotal != null) {
            String.format(Locale.US, "%.2f", calculatedSubtotal)
        } else {
            null
        }
}
