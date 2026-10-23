package com.komanda.kiosk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.komanda.kiosk.core.network.CatalogItemDto
import com.komanda.kiosk.ui.qr.KioskQrPaymentDialog
import com.komanda.kiosk.ui.theme.KomandaTokens
import com.komanda.kiosk.ui.theme.Zinc400
import com.komanda.kiosk.ui.theme.Zinc700
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950
import kotlinx.coroutines.launch

enum class KioskViewMode {
    HUB,
    SCANNER,
    LIST
}

@Composable
fun EspressoKioskScreen(
    espressoManager: EspressoManager,
    onNavigateToBackoffice: () -> Unit,
    onNavigateToSettings: () -> Unit = onNavigateToBackoffice,
    onConfirmCheckout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val categories by espressoManager.categories.collectAsStateWithLifecycle()
    val items by espressoManager.items.collectAsStateWithLifecycle()
    val cart by espressoManager.cart.collectAsStateWithLifecycle()
    val selectedCategory by espressoManager.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by espressoManager.searchQuery.collectAsStateWithLifecycle()
    val isLoading by espressoManager.isLoading.collectAsStateWithLifecycle()
    val statusMessage by espressoManager.statusMessage.collectAsStateWithLifecycle()
    val pendingLookup by espressoManager.pendingLookup.collectAsStateWithLifecycle()
    val activeShift by espressoManager.activeShift.collectAsStateWithLifecycle()
    val activeQrSession by espressoManager.activeQrSession.collectAsStateWithLifecycle()
    val approvedPayment by espressoManager.approvedPayment.collectAsStateWithLifecycle()
    val activeStaffSession by espressoManager.activeStaffSession.collectAsStateWithLifecycle()

    var viewMode by remember { mutableStateOf(KioskViewMode.SCANNER) }
    var showBackofficeAuthDialog by remember { mutableStateOf(false) }
    var manualBarcodeInput by remember { mutableStateOf("") }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var quickAddError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        espressoManager.loadCatalog()
        espressoManager.loadCashShift()
    }

    LaunchedEffect(pendingLookup) {
        quickAddError = null
    }

    LaunchedEffect(activeStaffSession) {
        while (activeStaffSession != null) {
            kotlinx.coroutines.delay(15_000L)
            espressoManager.isStaffSessionActive()
        }
    }

    if (showCheckoutDialog) {
        EspressoCheckoutDialog(
            totalAmount = espressoManager.totalAmount,
            onSelectPaymentMethod = { method ->
                showCheckoutDialog = false
                coroutineScope.launch {
                    if (method == "qr") {
                        espressoManager.startQrPayment()
                    } else {
                        espressoManager.checkout(method)
                    }
                }
            },
            onDismiss = { showCheckoutDialog = false }
        )
    }

    if (activeQrSession != null) {
        KioskQrPaymentDialog(
            session = activeQrSession!!,
            onPollStatus = { attemptId ->
                espressoManager.pollPaymentStatus(attemptId)
            },
            onApproved = { statusResponse ->
                espressoManager.onPaymentApproved(statusResponse)
            },
            onCancel = { attemptId ->
                coroutineScope.launch {
                    espressoManager.cancelQrPayment(attemptId)
                }
            },
            onTimeout = { attemptId ->
                coroutineScope.launch {
                    espressoManager.cancelQrPayment(attemptId)
                }
            },
            onDismiss = {
                espressoManager.dismissQrPayment()
            }
        )
    }

    if (approvedPayment != null) {
        val payment = approvedPayment!!
        KioskSuccessScreen(
            total = payment.total ?: "0.00",
            purchaseNumber = payment.purchaseNumber,
            paymentMethod = "Mercado Pago",
            onDismiss = {
                espressoManager.dismissApprovedPayment()
                viewMode = KioskViewMode.SCANNER
            }
        )
    }

    if (showBackofficeAuthDialog) {
        var backofficeAuthError by remember { mutableStateOf<String?>(null) }
        EspressoStaffAuthDialog(
            title = "Acceso al Backoffice",
            description = "Ingresá tus credenciales de empleado o administrador para acceder al panel de control.",
            isLockedOut = espressoManager.isStaffLockedOut,
            remainingLockoutSeconds = espressoManager.remainingLockoutSeconds,
            isLoading = isLoading,
            errorMessage = backofficeAuthError,
            onConfirm = { email, password ->
                coroutineScope.launch {
                    backofficeAuthError = null
                    val success = espressoManager.verifyStaffCredentials(email, password)
                    if (success) {
                        showBackofficeAuthDialog = false
                        onNavigateToBackoffice()
                    } else {
                        backofficeAuthError = espressoManager.statusMessage.value
                    }
                }
            },
            onDismiss = {
                showBackofficeAuthDialog = false
            }
        )
    }

    if (pendingLookup != null) {
        val (barcode, suggestion) = pendingLookup!!

        if (activeStaffSession != null) {
            EspressoQuickAddDialog(
                scannedBarcode = barcode,
                suggestion = suggestion,
                categories = categories,
                errorMessage = quickAddError,
                isLoading = isLoading,
                onSave = { name, price, categoryId, newCategoryName, code, isGeneric, icon, trackStock, stock ->
                    coroutineScope.launch {
                        quickAddError = null
                        var finalCatId = categoryId
                        if (!newCategoryName.isNullOrBlank()) {
                            val trimmedName = newCategoryName.trim()
                            val existingCat = espressoManager.categories.value.find { it.name.equals(trimmedName, ignoreCase = true) }
                            if (existingCat != null) {
                                finalCatId = existingCat.id
                            } else {
                                val createdCat = espressoManager.createCategory(trimmedName)
                                if (createdCat != null) {
                                    finalCatId = createdCat.id
                                } else {
                                    quickAddError = espressoManager.statusMessage.value ?: "Error al crear la categoría."
                                    return@launch
                                }
                            }
                        }
                        if (finalCatId.isNullOrBlank()) {
                            quickAddError = "Debe seleccionar o ingresar una categoría válida."
                            return@launch
                        }
                        val success = espressoManager.quickCreateItem(
                            name = name,
                            price = price,
                            categoryId = finalCatId,
                            barcode = code,
                            isGeneric = isGeneric,
                            genericIcon = icon,
                            trackStock = trackStock,
                            stockQuantity = stock,
                            addToCart = false
                        )
                        if (success) {
                            quickAddError = null
                            espressoManager.dismissPendingLookup()
                        } else {
                            quickAddError = espressoManager.statusMessage.value ?: "No se pudo crear el producto."
                        }
                    }
                },
                onDismiss = {
                    quickAddError = null
                    espressoManager.dismissPendingLookup()
                }
            )
        } else {
            var staffAuthError by remember { mutableStateOf<String?>(null) }
            EspressoStaffAuthDialog(
                scannedBarcode = barcode,
                isLockedOut = espressoManager.isStaffLockedOut,
                remainingLockoutSeconds = espressoManager.remainingLockoutSeconds,
                isLoading = isLoading,
                errorMessage = staffAuthError,
                onConfirm = { email, password ->
                    coroutineScope.launch {
                        staffAuthError = null
                        val success = espressoManager.verifyStaffCredentials(email, password)
                        if (!success) {
                            staffAuthError = espressoManager.statusMessage.value
                        }
                    }
                },
                onDismiss = {
                    espressoManager.dismissPendingLookup()
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Zinc950)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Main Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(24.dp)
            ) {
                if (activeStaffSession != null) {
                    Surface(
                        color = Zinc900,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, KomandaTokens.AccentTertiary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = KomandaTokens.AccentTertiary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Modo Carga Rápida Activo",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = "Operador: ${activeStaffSession?.operatorEmail} • Escaneá para registrar productos",
                                        color = Zinc400,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { espressoManager.endStaffSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = Zinc800, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Salir de Modo Carga", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Komanda Kiosk",
                            color = KomandaTokens.AccentTertiary,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bienvenidos a ${espressoManager.tenantName.ifBlank { "Komanda Espresso" }}",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            showBackofficeAuthDialog = true
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Zinc900, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Administración",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Status Message Bar
                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = KomandaTokens.AccentTertiary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KomandaTokens.AccentTertiary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusMessage!!,
                                color = KomandaTokens.AccentTertiary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { espressoManager.clearStatusMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KomandaTokens.AccentTertiary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // View Modes
                when (viewMode) {
                    KioskViewMode.HUB -> {
                        Text(
                            text = "Escaneá un producto o elegí una opción para comenzar.",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Card 1: Escanear
                            Card(
                                onClick = { viewMode = KioskViewMode.SCANNER },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(220.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Zinc900),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Zinc800)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Escanear producto",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Apuntá el escáner al código de barras",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Card 2: Elegir de la lista
                            Card(
                                onClick = { viewMode = KioskViewMode.LIST },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(220.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Zinc900),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Zinc800)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Elegir de la lista",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Ver todos los productos disponibles",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    KioskViewMode.SCANNER -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Escáner activo",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Button(
                                onClick = { viewMode = KioskViewMode.LIST },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Zinc800,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Volver al catálogo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Zinc900)
                                .border(2.dp, KomandaTokens.AccentTertiary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(110.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Listo para escanear",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Apuntá el escáner de mano o tipea el código del producto",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                                    color = Color.White.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                // Manual / USB Barcode input
                                Row(
                                    modifier = Modifier.width(520.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = manualBarcodeInput,
                                        onValueChange = { manualBarcodeInput = it },
                                        placeholder = { Text("Escanear o ingresar código...", fontSize = 17.sp) },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = Color.White),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = {
                                            if (manualBarcodeInput.isNotBlank()) {
                                                coroutineScope.launch {
                                                    espressoManager.onBarcodeScanned(manualBarcodeInput)
                                                    manualBarcodeInput = ""
                                                }
                                            }
                                        }),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        onClick = {
                                            if (manualBarcodeInput.isNotBlank()) {
                                                coroutineScope.launch {
                                                    espressoManager.onBarcodeScanned(manualBarcodeInput)
                                                    manualBarcodeInput = ""
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = KomandaTokens.AccentTertiary, contentColor = KomandaTokens.AccentPrimary),
                                        modifier = Modifier.height(56.dp)
                                    ) {
                                        Text("Ok", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }

                    KioskViewMode.LIST -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Catálogo de Productos",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = Color.White
                            )
                            Button(
                                onClick = { viewMode = KioskViewMode.SCANNER },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Zinc800,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Volver al escáner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Category chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { espressoManager.selectCategory(null) },
                                    label = { Text("Todos", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                                    modifier = Modifier.height(56.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KomandaTokens.AccentTertiary,
                                        selectedLabelColor = KomandaTokens.AccentPrimary
                                    )
                                )
                            }
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat.id,
                                    onClick = { espressoManager.selectCategory(cat.id) },
                                    label = { Text(cat.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                                    modifier = Modifier.height(56.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KomandaTokens.AccentTertiary,
                                        selectedLabelColor = KomandaTokens.AccentPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        val filteredItems = items.filter { item ->
                            val matchCat = selectedCategory == null || item.categoryId == selectedCategory
                            val matchQuery = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
                            matchCat && matchQuery
                        }

                        if (filteredItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay productos en esta categoría.", color = Color.White.copy(alpha = 0.5f), fontSize = 20.sp)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 220.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filteredItems) { item ->
                                    Card(
                                        onClick = { espressoManager.addToCart(item) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Zinc900),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            if (item.isGeneric && !item.genericIcon.isNullOrBlank()) {
                                                Text(
                                                    text = item.genericIcon,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = KomandaTokens.AccentTertiary
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "$${item.price}",
                                                color = KomandaTokens.AccentTertiary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 26.sp
                                            )
                                            if (item.barcode != null) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "EAN: ${item.barcode}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 14.sp,
                                                    color = Color.White.copy(alpha = 0.4f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Cart Panel (Right Sidebar)
            Surface(
                modifier = Modifier
                    .width(420.dp)
                    .fillMaxHeight(),
                color = Zinc900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = KomandaTokens.AccentTertiary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Tu pedido",
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (cart.isNotEmpty()) {
                            TextButton(onClick = { espressoManager.clearCart() }) {
                                Text("Vaciar", color = Color.Red.copy(alpha = 0.8f), fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp),
                                    tint = Color.White.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "El carrito está vacío",
                                    color = Color.White.copy(alpha = 0.4f),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Escaneá un producto o seleccionalo de la lista",
                                    color = Color.White.copy(alpha = 0.3f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(cart) { line ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Zinc950, RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = line.item.name,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            fontSize = 19.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$${String.format(java.util.Locale.US, "%.2f", line.lineTotal)}",
                                            color = KomandaTokens.AccentTertiary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        IconButton(
                                            onClick = { espressoManager.updateQuantity(line.item.id, -1) },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Zinc800, CircleShape)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Menos", tint = Color.White, modifier = Modifier.size(22.dp))
                                        }

                                        Text(
                                            text = "${line.quantity}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        IconButton(
                                            onClick = { espressoManager.updateQuantity(line.item.id, 1) },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Zinc800, CircleShape)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Más", tint = Color.White, modifier = Modifier.size(22.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Checkout Summary Bar
                    Surface(
                        color = Zinc950,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total a pagar", color = Color.White.copy(alpha = 0.7f), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "$${String.format(java.util.Locale.US, "%.2f", espressoManager.totalAmount)}",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KomandaTokens.AccentTertiary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showCheckoutDialog = true },
                                enabled = cart.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = KomandaTokens.AccentTertiary,
                                    contentColor = KomandaTokens.AccentPrimary,
                                    disabledContainerColor = Zinc800,
                                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "Pagar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
