package com.komanda.kiosk

import com.komanda.kiosk.ui.CheckoutAttemptStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CheckoutAttemptStoreTest {

    @Test
    fun computeCartHash_isDeterministic() {
        val items1 = listOf("item-1" to 2, "item-2" to 1)
        val hash1 = CheckoutAttemptStore.computeCartHash(items1, "cash", "Juan Perez")

        val items2 = listOf("item-2" to 1, "item-1" to 2)
        val hash2 = CheckoutAttemptStore.computeCartHash(items2, "cash", "Juan Perez")

        assertEquals(hash1, hash2)
    }

    @Test
    fun computeCartHash_differsOnDifferentMethodOrQuantity() {
        val items1 = listOf("item-1" to 2)
        val hash1 = CheckoutAttemptStore.computeCartHash(items1, "cash", "Juan Perez")
        val hash2 = CheckoutAttemptStore.computeCartHash(items1, "qr", "Juan Perez")

        assertNotEquals(hash1, hash2)
    }
}
