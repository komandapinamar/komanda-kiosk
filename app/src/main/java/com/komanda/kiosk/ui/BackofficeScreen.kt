package com.komanda.kiosk.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.komanda.kiosk.hardware.printing.PrinterRouter
import com.komanda.kiosk.ui.settings.PrinterSettingsContent
import com.komanda.kiosk.ui.theme.Emerald600
import com.komanda.kiosk.ui.theme.KomandaTokens
import com.komanda.kiosk.ui.theme.Zinc300
import com.komanda.kiosk.ui.theme.Zinc400
import com.komanda.kiosk.ui.theme.Zinc700
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950
import kotlinx.coroutines.launch

enum class BackofficeTab {
    CAJA,
    IMPRESORAS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackofficeScreen(
    espressoManager: EspressoManager,
    printerRouter: PrinterRouter,
    onBackToKiosk: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(BackofficeTab.CAJA) }
    val activeStaffSession by espressoManager.activeStaffSession.collectAsStateWithLifecycle()
    val activeShift by espressoManager.activeShift.collectAsStateWithLifecycle()
    val isLoading by espressoManager.isLoading.collectAsStateWithLifecycle()
    val statusMessage by espressoManager.statusMessage.collectAsStateWithLifecycle()

    var showOpenShiftDialog by remember { mutableStateOf(false) }
    var showCloseShiftDialog by remember { mutableStateOf(false) }

    val role = activeStaffSession?.role ?: "employee"
    val isEmployee = role == "employee"
    val isPrivileged = role == "admin" || role == "owner"

    BackHandler {
        onBackToKiosk()
    }

    LaunchedEffect(Unit) {
        espressoManager.loadCashShift()
    }

    if (showOpenShiftDialog) {
        EspressoOpenShiftDialog(
            onConfirm = { amount ->
                showOpenShiftDialog = false
                coroutineScope.launch {
                    espressoManager.openCashShift(amount)
                }
            },
            onDismiss = { showOpenShiftDialog = false }
        )
    }

    if (showCloseShiftDialog && activeShift != null) {
        EspressoCloseShiftDialog(
            shift = activeShift!!,
            isEmployee = isEmployee,
            onConfirm = { closingBalance, notes ->
                showCloseShiftDialog = false
                coroutineScope.launch {
                    espressoManager.closeCashShift(closingBalance, notes)
                }
            },
            onDismiss = { showCloseShiftDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Panel de Backoffice",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isPrivileged) KomandaTokens.AccentTertiary.copy(alpha = 0.2f) else Zinc800,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isPrivileged) KomandaTokens.AccentTertiary else Zinc700
                                )
                            ) {
                                val roleLabel = when (role) {
                                    "owner" -> "Propietario"
                                    "admin" -> "Administrador"
                                    else -> "Empleado"
                                }
                                Text(
                                    text = "${activeStaffSession?.operatorEmail ?: "Personal"} • $roleLabel",
                                    color = if (isPrivileged) KomandaTokens.AccentTertiary else Zinc300,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = espressoManager.tenantName.ifBlank { "Komanda Kiosk" },
                            fontSize = 12.sp,
                            color = Zinc400
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToKiosk) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Kiosk",
                            tint = Zinc300
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onBackToKiosk,
                        colors = ButtonDefaults.buttonColors(containerColor = Zinc800, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Volver al Kiosk", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Zinc950)
            )
        },
        containerColor = Zinc950
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            SecondaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Zinc900,
                contentColor = Color.White,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab.ordinal),
                        color = KomandaTokens.AccentTertiary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == BackofficeTab.CAJA,
                    onClick = { selectedTab = BackofficeTab.CAJA },
                    text = { Text("Control de Caja", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = KomandaTokens.AccentTertiary,
                    unselectedContentColor = Zinc400
                )
                Tab(
                    selected = selectedTab == BackofficeTab.IMPRESORAS,
                    onClick = { selectedTab = BackofficeTab.IMPRESORAS },
                    text = { Text("Configuración de Impresoras", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    icon = { Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = KomandaTokens.AccentTertiary,
                    unselectedContentColor = Zinc400
                )
            }

            if (!statusMessage.isNullOrBlank()) {
                Surface(
                    color = Zinc900,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage ?: "",
                        color = KomandaTokens.AccentTertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }

            // Tab Body
            when (selectedTab) {
                BackofficeTab.CAJA -> {
                    BackofficeCashView(
                        activeShift = activeShift,
                        isEmployee = isEmployee,
                        isLoading = isLoading,
                        onOpenShift = { showOpenShiftDialog = true },
                        onCloseShift = { showCloseShiftDialog = true }
                    )
                }
                BackofficeTab.IMPRESORAS -> {
                    PrinterSettingsContent(
                        router = printerRouter,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        showHeaderAction = true
                    )
                }
            }
        }
    }
}

@Composable
private fun BackofficeCashView(
    activeShift: com.komanda.kiosk.core.network.CashShiftDto?,
    isEmployee: Boolean,
    isLoading: Boolean,
    onOpenShift: () -> Unit,
    onCloseShift: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (activeShift != null) {
            // Shift is Open
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Zinc900),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Emerald600,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Caja Abierta",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        Button(
                            onClick = onCloseShift,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Zinc800,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cerrar Caja y Arqueo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Iniciada: ${formatDateDisplay(activeShift.openedAt)}",
                        fontSize = 13.sp,
                        color = Zinc400
                    )

                    if (!activeShift.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Observaciones de inicio: ${activeShift.notes}",
                            fontSize = 13.sp,
                            color = Zinc400
                        )
                    }
                }
            }

            // Metrics / Financial Data Section
            if (isEmployee) {
                // Employee View: Billing data is hidden / restricted
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Zinc800, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Zinc400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Datos de facturación restringidos",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "Por política de seguridad interna, los montos de ventas en efectivo, cantidad de pedidos y balance esperado no son visibles para operadores con rol Empleado.",
                            fontSize = 13.sp,
                            color = Zinc400
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MetricBox(
                                label = "Fondo Inicial (Apertura)",
                                value = "$${activeShift.openingBalance}",
                                subtext = "Efectivo entregado",
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                label = "Ventas Facturadas",
                                value = "🔒 Restringido",
                                subtext = "Solo administradores",
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                label = "Total Esperado",
                                value = "🔒 Restringido",
                                subtext = "Arqueo ciego requerido",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                // Admin / Owner View: Full billing and financial information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricBox(
                        label = "Fondo Inicial",
                        value = "$${activeShift.openingBalance}",
                        subtext = "Apertura de turno",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "Ventas en Efectivo",
                        value = "$${activeShift.currentCashSales ?: "0.00"}",
                        subtext = "${activeShift.orderCount} compras cobradas",
                        highlightColor = KomandaTokens.AccentTertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "Total Esperado en Caja",
                        value = "$${activeShift.expectedCash ?: activeShift.openingBalance}",
                        subtext = "Fondo + Ventas efectivo",
                        highlightColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "Transacciones",
                        value = "${activeShift.orderCount}",
                        subtext = "Pedidos finalizados",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Shift is Closed
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Zinc800, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Zinc900),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Zinc800,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = Zinc400,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Text(
                        text = "Caja Cerrada",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Actualmente no hay ningún turno de caja abierto en este tótem. Abrí el turno para registrar el fondo inicial de cambio.",
                        fontSize = 14.sp,
                        color = Zinc400,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.width(420.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onOpenShift,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KomandaTokens.AccentTertiary,
                            contentColor = KomandaTokens.AccentPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Zinc950, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Abrir Turno de Caja", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = Color.White
) {
    Card(
        modifier = modifier.border(1.dp, Zinc800, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Zinc950),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, fontSize = 12.sp, color = Zinc400, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtext, fontSize = 11.sp, color = Zinc400)
        }
    }
}

private fun formatDateDisplay(iso: String): String {
    return try {
        if (iso.contains("T")) {
            val datePart = iso.substringBefore("T")
            val timePart = iso.substringAfter("T").take(5)
            "$datePart $timePart hs"
        } else {
            iso
        }
    } catch (_: Exception) {
        iso
    }
}
