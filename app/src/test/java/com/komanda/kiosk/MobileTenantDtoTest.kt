package com.komanda.kiosk

import com.komanda.kiosk.core.network.MobileTenantDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MobileTenantDtoTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(MobileTenantDto::class.java)

    @Test
    fun deserialize_withExplicitPreset_parsesCorrectly() {
        val json = """
            {
                "id": "tenant-express-1",
                "name": "Drugstore Central",
                "slug": "drugstore-central",
                "status": "active",
                "role": "owner",
                "preset": "express_retail"
            }
        """.trimIndent()

        val dto = adapter.fromJson(json)
        assertNotNull(dto)
        assertEquals("tenant-express-1", dto?.id)
        assertEquals("express_retail", dto?.preset)
    }

    @Test
    fun deserialize_withGastronomyPreset_parsesCorrectly() {
        val json = """
            {
                "id": "tenant-gastro-1",
                "name": "Pizzeria Napoli",
                "slug": "pizzeria-napoli",
                "status": "active",
                "role": "admin",
                "preset": "gastronomy"
            }
        """.trimIndent()

        val dto = adapter.fromJson(json)
        assertNotNull(dto)
        assertEquals("tenant-gastro-1", dto?.id)
        assertEquals("gastronomy", dto?.preset)
    }

    @Test
    fun deserialize_withoutPreset_defaultsToExpressRetail() {
        val json = """
            {
                "id": "tenant-legacy-1",
                "name": "Kiosco Antiguo",
                "slug": "kiosco-antiguo",
                "status": "active",
                "role": "employee"
            }
        """.trimIndent()

        val dto = adapter.fromJson(json)
        assertNotNull(dto)
        assertEquals("tenant-legacy-1", dto?.id)
        assertEquals("express_retail", dto?.preset)
    }
}
