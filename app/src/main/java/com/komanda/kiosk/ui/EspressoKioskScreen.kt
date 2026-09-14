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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.komanda.kiosk.ui.theme.Amber400
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
    onNavigateToSettings: () -> Unit,
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

    var viewMode by remember { mutableStateOf(KioskViewMode.HUB) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var manualBarcodeInput by remember { mutableStateOf("") }
    var showOpenShiftDialog by remember { mutableStateOf(false) }
    var showCloseShiftDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        espressoManager.loadCatalog()
        espressoManager.loadCashShift()
    }

    if (showCheckoutDialog) {
        EspressoCheckoutDialog(
            totalAmount = espressoManager.totalAmount,
            onSelectPaymentMethod = { method ->
                showCheckoutDialog = false
                coroutineScope.launch {
                    espressoManager.checkout(method)
                }
            },
            onDismiss = { showCheckoutDialog = false }
        )
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
            onConfirm = { closingBalance, notes ->
                showCloseShiftDialog = false
                coroutineScope.launch {
                    espressoManager.closeCashShift(closingBalance, notes)
                }
            },
            onDismiss = { showCloseShiftDialog = false }
        )
    }

    if (showPinDialog) {
        EspressoPinDialog(
            correctPin = "1234",
            onPinSuccess = {
                showPinDialog = false
                pinAction?.invoke()
                pinAction = null
            },
            onDismiss = {
                showPinDialog = false
                pinAction = null
            }
        )
    }

    if (pendingLookup != null) {
        val (barcode, suggestion) = pendingLookup!!
        EspressoQuickAddDialog(
            scannedBarcode = barcode,
            suggestion = suggestion,
            categories = categories,
            onSave = { name, price, categoryId, code, isGeneric, icon, trackStock, stock ->
                coroutineScope.launch {
                    espressoManager.quickCreateItem(
                        name = name,
                        price = price,
                        categoryId = categoryId,
                        barcode = code,
                        isGeneric = isGeneric,
                        genericIcon = icon,
                        trackStock = trackStock,
                        stockQuantity = stock
                    )
                }
            },
            onDismiss = { espressoManager.dismissPendingLookup() }
        )
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
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "✦ Kiosco Digital",
                            color = Amber400,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Bienvenidos a ${espressoManager.tenantName.ifBlank { "Komanda Espresso" }}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (activeShift != null) Color(0xFF10B981).copy(alpha = 0.2f) else Amber400.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (activeShift != null) Color(0xFF10B981) else Amber400),
                            modifier = Modifier.clickable {
                                if (activeShift == null) {
                                    showOpenShiftDialog = true
                                } else {
                                    pinAction = { showCloseShiftDialog = true }
                                    showPinDialog = true
                                }
                            }
                        ) {
                            Text(
                                text = if (activeShift != null) "💰 Caja: $${activeShift?.expectedCash ?: activeShift?.openingBalance}" else "💰 Abrir caja",
                                color = if (activeShift != null) Color(0xFF10B981) else Amber400,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                pinAction = { onNavigateToSettings() }
                                showPinDialog = true
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Zinc900, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Administración",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Status Message Bar
                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Amber400.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Amber400.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusMessage!!,
                                color = Amber400,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { espressoManager.clearStatusMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Amber400, modifier = Modifier.size(16.dp))
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
                                    Text("🔫", fontSize = 48.sp)
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
                                    Text("🏪", fontSize = 48.sp)
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
                                text = "🔫 Escáner activo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewMode = KioskViewMode.LIST },
                                    colors = ButtonDefaults.buttonColors(containerColor = Zinc900)
                                ) {
                                    Text("Ver catálogo")
                                }
                                Button(
                                    onClick = { viewMode = KioskViewMode.HUB },
                                    colors = ButtonDefaults.buttonColors(containerColor = Zinc800)
                                ) {
                                    Text("Volver")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Zinc900)
                                .border(2.dp, Amber400.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔫", fontSize = 64.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Listo para escanear",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Apuntá el escáner de mano o tipea el código",
                                    color = Color.White.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Manual / USB Barcode input
                                Row(
                                    modifier = Modifier.width(360.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = manualBarcodeInput,
                                        onValueChange = { manualBarcodeInput = it },
                                        placeholder = { Text("Escanear o ingresar código...") },
                                        singleLine = true,
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (manualBarcodeInput.isNotBlank()) {
                                                coroutineScope.launch {
                                                    espressoManager.onBarcodeScanned(manualBarcodeInput)
                                                    manualBarcodeInput = ""
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Zinc950)
                                    ) {
                                        Text("Ok")
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
                                text = "🏪 Catálogo de Productos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewMode = KioskViewMode.SCANNER },
                                    colors = ButtonDefaults.buttonColors(containerColor = Zinc900)
                                ) {
                                    Text("🔫 Modo Escáner")
                                }
                                Button(
                                    onClick = { viewMode = KioskViewMode.HUB },
                                    colors = ButtonDefaults.buttonColors(containerColor = Zinc800)
                                ) {
                                    Text("Volver")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { espressoManager.selectCategory(null) },
                                    label = { Text("Todos") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Amber400,
                                        selectedLabelColor = Zinc950
                                    )
                                )
                            }
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat.id,
                                    onClick = { espressoManager.selectCategory(cat.id) },
                                    label = { Text(cat.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Amber400,
                                        selectedLabelColor = Zinc950
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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
                                Text("No hay productos en esta categoría.", color = Color.White.copy(alpha = 0.5f))
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 160.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            if (item.isGeneric && !item.genericIcon.isNullOrBlank()) {
                                                Text(
                                                    text = item.genericIcon,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Amber400
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$${item.price}",
                                                color = Amber400,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            if (item.barcode != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "EAN: ${item.barcode}",
                                                    style = MaterialTheme.typography.labelSmall,
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
                    .width(360.dp)
                    .fillMaxHeight(),
                color = Zinc900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = Amber400)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tu pedido",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (cart.isNotEmpty()) {
                            TextButton(onClick = { espressoManager.clearCart() }) {
                                Text("Vaciar", color = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🛒", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "El carrito está vacío",
                                    color = Color.White.copy(alpha = 0.4f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Escaneá un producto o seleccionalo de la lista",
                                    color = Color.White.copy(alpha = 0.3f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(cart) { line ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Zinc950, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = line.item.name,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$${String.format(java.util.Locale.US, "%.2f", line.lineTotal)}",
                                            color = Amber400,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(
                                            onClick = { espressoManager.updateQuantity(line.item.id, -1) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Zinc800, CircleShape)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Menos", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = "${line.quantity}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        IconButton(
                                            onClick = { espressoManager.updateQuantity(line.item.id, 1) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Zinc800, CircleShape)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Más", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkout Summary Bar
                    Surface(
                        color = Zinc950,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total a cobrar", color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    text = "$${String.format(java.util.Locale.US, "%.2f", espressoManager.totalAmount)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber400
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showCheckoutDialog = true },
                                enabled = cart.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Amber400,
                                    contentColor = Zinc950,
                                    disabledContainerColor = Zinc800,
                                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Confirmar cobro",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
