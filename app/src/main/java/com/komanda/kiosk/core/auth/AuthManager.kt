package com.komanda.kiosk.core.auth

import android.util.Log
import com.komanda.kiosk.core.network.KomandaApi
import com.komanda.kiosk.core.network.MobileLoginRequest
import com.komanda.kiosk.core.network.MobileTenantDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AuthManager(
    private val api: KomandaApi,
    private val sessionStorage: SessionStorage,
    private val currentTimeProvider: () -> Long = { System.currentTimeMillis() }
) {
    private val tag = "AuthManager"

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentToken: String?
        get() = when (val state = _authState.value) {
            is AuthState.Authenticated -> state.session.token
            is AuthState.SelectTenant -> state.token
            is AuthState.NoActiveTenant -> state.token
            else -> null
        }

    val currentSession: AuthSession?
        get() = (_authState.value as? AuthState.Authenticated)?.session

    suspend fun initialize() {
        val session = sessionStorage.getSession()
        if (session == null) {
            _authState.value = AuthState.LoggedOut
            return
        }

        if (isExpired(session.expiresAt)) {
            Log.w(tag, "Persisted session is expired. Clearing storage.")
            sessionStorage.clearSession()
            _authState.value = AuthState.LoggedOut
            return
        }

        try {
            val response = api.getMobileContext("Bearer ${session.token}")
            if (!response.isSuccessful || response.body() == null) {
                if (response.code() == 401) {
                    Log.w(tag, "Persisted session revoked or expired on server (401). Clearing storage.")
                    sessionStorage.clearSession()
                    _authState.value = AuthState.LoggedOut
                } else {
                    Log.w(tag, "Transient server error during context validation (${response.code()}). Keeping session.")
                    _authState.value = AuthState.Error("Error de conexión con el servidor (${response.code()}).")
                }
                return
            }

            val activeTenants = response.body()!!.activeTenants
            val currentTenant = activeTenants.find { it.id == session.tenantId }

            if (currentTenant != null && isExpressTenant(currentTenant)) {
                _authState.value = AuthState.Authenticated(session, activeTenants)
            } else {
                val expressTenants = activeTenants.filter { isExpressTenant(it) }
                when {
                    expressTenants.isEmpty() -> {
                        sessionStorage.clearSession()
                        _authState.value = AuthState.NoActiveTenant(session.token)
                    }
                    expressTenants.size == 1 && activeTenants.size == 1 -> {
                        val single = expressTenants.first()
                        val updated = AuthSession(
                            token = session.token,
                            expiresAt = session.expiresAt,
                            tenantId = single.id,
                            tenantName = single.name,
                            locationId = single.primaryLocation?.id ?: "",
                            locationName = single.primaryLocation?.name ?: ""
                        )
                        sessionStorage.saveSession(updated)
                        _authState.value = AuthState.Authenticated(updated, activeTenants)
                    }
                    else -> {
                        _authState.value = AuthState.SelectTenant(
                            token = session.token,
                            expiresAt = session.expiresAt,
                            tenants = activeTenants
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to validate session context", e)
            if (e is java.io.IOException) {
                // Network unreachable or timeout — do not destroy stored credentials
                _authState.value = AuthState.Error("No se pudo conectar con el servidor. Verificá la conexión.")
            } else {
                sessionStorage.clearSession()
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        _authState.value = AuthState.Loading
        try {
            val loginRes = api.mobileLogin(
                MobileLoginRequest(
                    email = email.trim(),
                    password = password
                )
            )

            if (!loginRes.isSuccessful || loginRes.body() == null) {
                val errorMsg = if (loginRes.code() == 401) {
                    "Credenciales inválidas. Verificá tu usuario y contraseña."
                } else {
                    "Error de autenticación (${loginRes.code()}). Intente nuevamente."
                }
                _authState.value = AuthState.Error(errorMsg)
                return false
            }

            val loginData = loginRes.body()!!
            val contextRes = api.getMobileContext("Bearer ${loginData.token}")

            if (!contextRes.isSuccessful || contextRes.body() == null) {
                _authState.value = AuthState.Error("Error al obtener contexto operativo (${contextRes.code()}).")
                return false
            }

            val tenants = contextRes.body()!!.activeTenants
            val expressTenants = tenants.filter { isExpressTenant(it) }

            when {
                expressTenants.isEmpty() -> {
                    _authState.value = AuthState.NoActiveTenant(loginData.token)
                }
                expressTenants.size == 1 && tenants.size == 1 -> {
                    // HAPPY_PATH: Automatic single-tenant selection only if the sole business is express
                    val single = expressTenants.first()
                    val session = AuthSession(
                        token = loginData.token,
                        expiresAt = loginData.expiresAt,
                        tenantId = single.id,
                        tenantName = single.name,
                        locationId = single.primaryLocation?.id ?: "",
                        locationName = single.primaryLocation?.name ?: ""
                    )
                    sessionStorage.saveSession(session)
                    _authState.value = AuthState.Authenticated(session, tenants)
                }
                else -> {
                    // MULTIPLE_TENANTS: Explicit selection required
                    _authState.value = AuthState.SelectTenant(
                        token = loginData.token,
                        expiresAt = loginData.expiresAt,
                        tenants = tenants
                    )
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(tag, "Login exception", e)
            val message = if (e is java.io.IOException) {
                "No se pudo conectar con el servidor. Verificá tu conexión a internet."
            } else {
                "Error inesperado al iniciar sesión."
            }
            _authState.value = AuthState.Error(message)
            return false
        }
    }

    suspend fun selectTenant(
        token: String,
        expiresAt: String,
        tenantId: String,
        availableTenants: List<MobileTenantDto>
    ): Boolean {
        if (isExpired(expiresAt)) {
            Log.w(tag, "Session expired while selecting tenant.")
            sessionStorage.clearSession()
            _authState.value = AuthState.LoggedOut
            return false
        }

        val selected = availableTenants.find { it.id == tenantId }
        if (selected == null) {
            _authState.value = AuthState.Error("Comercio no autorizado.")
            return false
        }

        if (!isExpressTenant(selected)) {
            Log.e(tag, "Attempted to select non-express tenant: ${selected.preset}")
            _authState.value = AuthState.Error("Este negocio es exclusivo para gastronomía. Utilizá Komanda POS.")
            return false
        }

        val session = AuthSession(
            token = token,
            expiresAt = expiresAt,
            tenantId = selected.id,
            tenantName = selected.name,
            locationId = selected.primaryLocation?.id ?: "",
            locationName = selected.primaryLocation?.name ?: ""
        )
        sessionStorage.saveSession(session)
        _authState.value = AuthState.Authenticated(session, availableTenants)
        return true
    }

    suspend fun logout() {
        val token = currentToken
        sessionStorage.clearSession()
        _authState.value = AuthState.LoggedOut
        if (token != null) {
            try {
                api.mobileRevokeSession("Bearer $token")
            } catch (e: Exception) {
                Log.w(tag, "Failed to revoke session on server during logout: ${e.message}")
            }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.LoggedOut
        }
    }

    fun isExpressTenant(tenant: MobileTenantDto): Boolean {
        return tenant.preset == null || tenant.preset == "express_retail"
    }

    private fun isExpired(expiresAt: String): Boolean {
        val expiryMillis = parseIsoTimestamp(expiresAt) ?: return true
        return expiryMillis <= currentTimeProvider()
    }

    private fun parseIsoTimestamp(isoDate: String): Long? {
        return try {
            java.time.Instant.parse(isoDate).toEpochMilli()
        } catch (_: Throwable) {
            try {
                val cleaned = isoDate.split(".")[0].replace("Z", "")
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                format.parse(cleaned)?.time
            } catch (_: Exception) {
                null
            }
        }
    }
}
