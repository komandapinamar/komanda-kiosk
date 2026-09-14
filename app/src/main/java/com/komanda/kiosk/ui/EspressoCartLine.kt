package com.komanda.kiosk.ui

import com.komanda.kiosk.core.network.CatalogItemDto

data class EspressoCartLine(
    val item: CatalogItemDto,
    val quantity: Int
) {
    val lineTotal: Double
        get() = (item.price.toDoubleOrNull() ?: 0.0) * quantity
}
