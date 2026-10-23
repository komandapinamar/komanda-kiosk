package com.komanda.kiosk

import com.komanda.kiosk.core.auth.AuthManager
import com.komanda.kiosk.core.auth.AuthSession
import com.komanda.kiosk.core.auth.AuthState
import com.komanda.kiosk.core.auth.SessionStorage
import com.komanda.kiosk.core.network.MobileLocationDto
import com.komanda.kiosk.core.network.MobileTenantDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TenantPresetGatingTest {

    private class FakeSessionStorage : SessionStorage {
        var savedSession: AuthSession? = null
        override suspend fun saveSession(session: AuthSession) { savedSession = session }
        override suspend fun getSession(): AuthSession? = savedSession
        override suspend fun clearSession() { savedSession = null }
    }

    private val api = FakeEspressoApi()
    private val sessionStorage = FakeSessionStorage()
    private val authManager = AuthManager(api, sessionStorage)

    @Test
    fun isExpressTenant_expressRetail_returnsTrue() {
        val tenant = MobileTenantDto(
            id = "t-1",
            name = "Drugstore",
            slug = "drugstore",
            status = "active",
            role = "owner",
            preset = "express_retail"
        )
        assertTrue(authManager.isExpressTenant(tenant))
    }

    @Test
    fun isExpressTenant_nullPreset_defaultsToTrueForBackwardsCompatibility() {
        val tenant = MobileTenantDto(
            id = "t-2",
            name = "Kiosco",
            slug = "kiosco",
            status = "active",
            role = "admin",
            preset = null
        )
        assertTrue(authManager.isExpressTenant(tenant))
    }

    @Test
    fun isExpressTenant_gastronomy_returnsFalse() {
        val tenant = MobileTenantDto(
            id = "t-3",
            name = "Pizzeria",
            slug = "pizzeria",
            status = "active",
            role = "owner",
            preset = "gastronomy"
        )
        assertFalse(authManager.isExpressTenant(tenant))
    }

    @Test
    fun selectTenant_gastronomyTenant_failsAndSetsError() = runTest {
        val gastronomyTenant = MobileTenantDto(
            id = "t-gastro",
            name = "Restaurante Central",
            slug = "restaurante-central",
            status = "active",
            role = "owner",
            preset = "gastronomy"
        )

        val success = authManager.selectTenant(
            token = "dummy-token",
            expiresAt = "2099-12-31T23:59:59Z",
            tenantId = "t-gastro",
            availableTenants = listOf(gastronomyTenant)
        )

        assertFalse("Selecting a gastronomy tenant should fail in Kiosk", success)
        val state = authManager.authState.value
        assertTrue("Auth state should be Error", state is AuthState.Error)
        assertEquals(
            "Este negocio es exclusivo para gastronomía. Utilizá Komanda POS.",
            (state as AuthState.Error).message
        )
    }

    @Test
    fun selectTenant_expressTenant_succeeds() = runTest {
        val expressTenant = MobileTenantDto(
            id = "t-express",
            name = "Kiosco Express",
            slug = "kiosco-express",
            status = "active",
            role = "owner",
            preset = "express_retail",
            primaryLocation = MobileLocationDto("loc-1", "Local 1", "America/Argentina/Buenos_Aires")
        )

        val success = authManager.selectTenant(
            token = "valid-token",
            expiresAt = "2099-12-31T23:59:59Z",
            tenantId = "t-express",
            availableTenants = listOf(expressTenant)
        )

        assertTrue("Selecting an express tenant should succeed in Kiosk", success)
        val state = authManager.authState.value
        assertTrue("Auth state should be Authenticated", state is AuthState.Authenticated)
        assertEquals("t-express", (state as AuthState.Authenticated).session.tenantId)
    }
}
