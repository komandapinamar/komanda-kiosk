package com.komanda.kiosk.ui

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.komanda.kiosk.core.auth.AuthManager
import com.komanda.kiosk.core.auth.AuthState
import com.komanda.kiosk.core.auth.SecureSessionStorage
import com.komanda.kiosk.core.auth.ui.LoginScreen
import com.komanda.kiosk.core.auth.ui.NoActiveTenantScreen
import com.komanda.kiosk.core.auth.ui.TenantSelectionScreen
import com.komanda.kiosk.core.launcher.LauncherManager
import com.komanda.kiosk.core.network.KomandaApi
import com.komanda.kiosk.core.network.NetworkClient
import com.komanda.kiosk.ui.settings.PrinterSettingsScreen
import com.komanda.kiosk.hardware.printing.PrinterRouter
import com.komanda.kiosk.ui.theme.KomandaTokens
import com.komanda.kiosk.ui.theme.KomandaTheme
import com.komanda.kiosk.ui.theme.Zinc950
import kotlinx.coroutines.launch

enum class EspressoScreen {
    KIOSK,
    BACKOFFICE,
    PRINTER_SETTINGS
}

class KioskActivity : ComponentActivity() {

    private val tag = "KioskActivity"
    private lateinit var authManager: AuthManager
    private lateinit var api: KomandaApi
    private lateinit var printerRouter: PrinterRouter
    private var activeEspressoManager: EspressoManager? = null

    private var currentBaseUrl by mutableStateOf("https://throwing-dust-public.ngrok-free.dev")
    private val barcodeBuffer = StringBuilder()
    private var lastKeyTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemUI()
        LauncherManager(this).startKioskLockTask(this)

        val prefs = getSharedPreferences("komanda_pos_prefs", MODE_PRIVATE)
        val savedBaseUrl = prefs.getString("server_base_url", null)
        if (!savedBaseUrl.isNullOrBlank()) {
            currentBaseUrl = savedBaseUrl
        }

        printerRouter = PrinterRouter(this)

        api = NetworkClient.create(
            baseUrl = currentBaseUrl,
            tokenProvider = { authManager.currentToken }
        )

        val sessionStorage = SecureSessionStorage(this)
        authManager = AuthManager(
            api = api,
            sessionStorage = sessionStorage
        )

        lifecycleScope.launch {
            authManager.initialize()
        }

        setContent {
            KomandaTheme {
                val authState by authManager.authState.collectAsStateWithLifecycle()
                var currentScreen by remember { mutableStateOf(EspressoScreen.KIOSK) }

                when (val state = authState) {
                    is AuthState.Initializing -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Zinc950),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = KomandaTokens.AccentTertiary)
                        }
                    }
                    is AuthState.LoggedOut, is AuthState.Loading, is AuthState.Error -> {
                        activeEspressoManager = null
                        LoginScreen(
                            isLoading = state is AuthState.Loading,
                            errorMessage = (state as? AuthState.Error)?.message,
                            serverUrl = currentBaseUrl,
                            onServerUrlChanged = { newUrl ->
                                val trimmed = newUrl.trim().trimEnd('/')
                                if (trimmed.isNotBlank()) {
                                    currentBaseUrl = trimmed
                                    prefs.edit().putString("server_base_url", trimmed).apply()
                                    api = NetworkClient.create(trimmed) { authManager.currentToken }
                                    authManager = AuthManager(api, sessionStorage)
                                    lifecycleScope.launch { authManager.initialize() }
                                }
                            },
                            onLogin = { email, password ->
                                lifecycleScope.launch { authManager.login(email, password) }
                            }
                        )
                    }
                    is AuthState.SelectTenant -> {
                        activeEspressoManager = null
                        TenantSelectionScreen(
                            tenants = state.tenants,
                            onSelectTenant = { tenantId ->
                                lifecycleScope.launch {
                                    authManager.selectTenant(
                                        token = state.token,
                                        expiresAt = state.expiresAt,
                                        tenantId = tenantId,
                                        availableTenants = state.tenants
                                    )
                                }
                            },
                            onLogout = { lifecycleScope.launch { authManager.logout() } }
                        )
                    }
                    is AuthState.NoActiveTenant -> {
                        activeEspressoManager = null
                        NoActiveTenantScreen(onLogout = { lifecycleScope.launch { authManager.logout() } })
                    }
                    is AuthState.Authenticated -> {
                        val session = state.session
                        val context = LocalContext.current
                        val attemptStore = remember(context) { CheckoutAttemptStore(context) }
                        val manager = remember(session.tenantId) {
                            EspressoManager(
                                tenantId = session.tenantId,
                                tenantName = session.tenantName,
                                api = api,
                                printerRouter = printerRouter,
                                attemptStore = attemptStore
                            ).also { activeEspressoManager = it }
                        }

                        when (currentScreen) {
                            EspressoScreen.KIOSK -> {
                                EspressoKioskScreen(
                                    espressoManager = manager,
                                    onNavigateToBackoffice = { currentScreen = EspressoScreen.BACKOFFICE },
                                    onNavigateToSettings = { currentScreen = EspressoScreen.BACKOFFICE },
                                    onConfirmCheckout = {
                                        Log.i(tag, "Checkout requested for ${manager.cart.value.size} items")
                                    }
                                )
                            }
                            EspressoScreen.BACKOFFICE -> {
                                BackofficeScreen(
                                    espressoManager = manager,
                                    printerRouter = printerRouter,
                                    onBackToKiosk = {
                                        manager.endStaffSession()
                                        currentScreen = EspressoScreen.KIOSK
                                    }
                                )
                            }
                            EspressoScreen.PRINTER_SETTINGS -> {
                                PrinterSettingsScreen(
                                    router = printerRouter,
                                    onBack = { currentScreen = EspressoScreen.BACKOFFICE }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Intercepts physical USB / Bluetooth barcode scanner keyboard events.
     * Barcode scanners type rapid keystrokes ending with KeyEvent.KEYCODE_ENTER.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()
            // Barcode scanners type characters with very small latency (<50ms between keys)
            if (now - lastKeyTime > 500) {
                barcodeBuffer.clear()
            }
            lastKeyTime = now

            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val barcode = barcodeBuffer.toString().trim()
                barcodeBuffer.clear()
                if (barcode.isNotBlank()) {
                    Log.i(tag, "Scanned barcode from hardware scanner: $barcode")
                    activeEspressoManager?.let { mgr ->
                        lifecycleScope.launch {
                            mgr.onBarcodeScanned(barcode)
                        }
                    }
                    return true
                }
            } else {
                val unicodeChar = event.unicodeChar
                if (unicodeChar > 0) {
                    barcodeBuffer.append(unicodeChar.toChar())
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
