package com.komanda.kiosk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.komanda.kiosk.ui.theme.Emerald600
import com.komanda.kiosk.ui.theme.KomandaTokens
import com.komanda.kiosk.ui.theme.Zinc300
import com.komanda.kiosk.ui.theme.Zinc400
import com.komanda.kiosk.ui.theme.Zinc700
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950
import java.util.UUID
import kotlinx.coroutines.launch

private data class EditableProductRow(
    val localId: String = UUID.randomUUID().toString(),
    val name: String = "",
    val price: String = "",
    val barcode: String = "",
    val trackStock: Boolean = false,
    val stockQuantity: String = ""
)

@Composable
fun BackofficeProductsView(
    espressoManager: EspressoManager,
    isOwner: Boolean
) {
    val categories by espressoManager.categories.collectAsStateWithLifecycle()
    val items by espressoManager.items.collectAsStateWithLifecycle()
    val isLoading by espressoManager.isLoading.collectAsStateWithLifecycle()
    val statusMessage by espressoManager.statusMessage.collectAsStateWithLifecycle()

    val draftCategories = categories.filter { it.status == "draft" }
    val draftItems = items.filter { it.status == "draft" }
    val scope = rememberCoroutineScope()
    val rows = remember { mutableStateListOf(EditableProductRow()) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var newCategoryName by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    if (!isOwner) {
        OwnerOnlyNotice(
            title = "Carga de productos restringida",
            description = "La carga manual y en bulk de productos requiere autorización del Propietario del comercio."
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Zinc800, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = KomandaTokens.AccentTertiary.copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = KomandaTokens.AccentTertiary, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Productos",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "La carga se guarda como borrador y se publica en un segundo paso. Solo Propietario.",
                            fontSize = 13.sp,
                            color = Zinc400
                        )
                    }
                }
            }
        }

        if (!statusMessage.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = KomandaTokens.AccentTertiary.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, KomandaTokens.AccentTertiary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statusMessage ?: "",
                    color = KomandaTokens.AccentTertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        // Drafts / publish
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Zinc800, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Borradores pendientes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${draftItems.size} productos · ${draftCategories.size} categorías sin publicar",
                            fontSize = 12.sp,
                            color = Zinc400
                        )
                    }
                    Button(
                        onClick = {
                            scope.launch { espressoManager.publishDrafts() }
                        },
                        enabled = !isLoading && (draftItems.isNotEmpty() || draftCategories.isNotEmpty()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald600,
                            contentColor = Zinc950
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Zinc950, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Publicar borradores", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                if (draftItems.isEmpty() && draftCategories.isEmpty()) {
                    Text(
                        text = "No hay borradores pendientes. Los productos y categorías que cargues quedarán acá hasta que los publiques.",
                        fontSize = 12.sp,
                        color = Zinc400
                    )
                } else {
                    draftCategories.forEach { category ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Zinc950,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Categoría · ${category.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KomandaTokens.AccentTertiary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                    draftItems.forEach { item ->
                        val categoryName = categories.find { it.id == item.categoryId }?.name ?: "Sin categoría"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Zinc950,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.name} · $categoryName",
                                    fontSize = 12.sp,
                                    color = Zinc300,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$${item.price}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Emerald600
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Zinc800, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Categoría destino", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryDropdown(
                        categories = categories.map { it.id to it.name },
                        selectedId = selectedCategoryId,
                        onSelect = {
                            selectedCategoryId = it
                            newCategoryName = ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = {
                            newCategoryName = it
                            if (it.isNotBlank()) selectedCategoryId = null
                        },
                        label = { Text("...o nueva categoría") },
                        singleLine = true,
                        colors = darkFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "Los productos se cargan en la categoría seleccionada. Si escribís una nueva, se crea automáticamente.",
                    fontSize = 12.sp,
                    color = Zinc400
                )
            }
        }

        // Bulk rows
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Zinc800, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Productos a cargar (${rows.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Completá una fila por producto. Se guardan como borrador hasta publicarlos.", fontSize = 12.sp, color = Zinc400)
                    }
                    Button(
                        onClick = { rows.add(EditableProductRow()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Zinc800, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Agregar fila", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                rows.forEachIndexed { index, row ->
                    BulkProductRowEditor(
                        index = index,
                        row = row,
                        canRemove = rows.size > 1,
                        onChange = { updated -> rows[index] = updated },
                        onRemove = { rows.removeAt(index) }
                    )
                }

                if (!formError.isNullOrBlank()) {
                    Text(formError ?: "", color = Color(0xFFF87171), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total en catálogo: ${items.size} productos",
                        fontSize = 12.sp,
                        color = Zinc400
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(
                        onClick = {
                            formError = null
                            scope.launch {
                                var categoryId = selectedCategoryId
                                if (!newCategoryName.isNullOrBlank()) {
                                    val trimmed = newCategoryName.trim()
                                    val existing = espressoManager.categories.value.find { it.name.equals(trimmed, ignoreCase = true) }
                                    if (existing != null) {
                                        categoryId = existing.id
                                    } else {
                                        val created = espressoManager.createCategory(trimmed)
                                        if (created != null) {
                                            categoryId = created.id
                                            newCategoryName = ""
                                        } else {
                                            formError = espressoManager.statusMessage.value ?: "No se pudo crear la categoría."
                                            return@launch
                                        }
                                    }
                                }
                                if (categoryId.isNullOrBlank()) {
                                    formError = "Seleccioná una categoría o ingresá una nueva."
                                    return@launch
                                }

                                val bulkRows = rows.map { editable ->
                                    BulkProductRow(
                                        name = editable.name,
                                        price = editable.price,
                                        barcode = editable.barcode,
                                        trackStock = editable.trackStock,
                                        stockQuantity = editable.stockQuantity.toIntOrNull() ?: 0
                                    )
                                }
                                val result = espressoManager.bulkCreateItems(bulkRows, categoryId)
                                if (result.createdCount > 0) {
                                    rows.clear()
                                    rows.add(EditableProductRow())
                                }
                                if (result.failedRows.isNotEmpty()) {
                                    formError = result.failedRows.joinToString(separator = " · ") { (rowIndex, reason) ->
                                        "Fila ${rowIndex + 1}: $reason"
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KomandaTokens.AccentTertiary,
                            contentColor = KomandaTokens.AccentPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Zinc950, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Cargar ${rows.size} producto(s)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkProductRowEditor(
    index: Int,
    row: EditableProductRow,
    canRemove: Boolean,
    onChange: (EditableProductRow) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Zinc950,
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Zinc800, modifier = Modifier.size(26.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Zinc300)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Fila ${index + 1}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Zinc300)
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Controlar stock", fontSize = 12.sp, color = Zinc400)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = row.trackStock,
                        onCheckedChange = { onChange(row.copy(trackStock = it)) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onRemove,
                        enabled = canRemove
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar fila",
                            tint = if (canRemove) Color(0xFFF87171) else Zinc700
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = row.name,
                    onValueChange = { onChange(row.copy(name = it)) },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    colors = darkFieldColors(),
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = row.price,
                    onValueChange = { onChange(row.copy(price = it)) },
                    label = { Text("Precio") },
                    singleLine = true,
                    colors = darkFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = row.barcode,
                    onValueChange = { onChange(row.copy(barcode = it)) },
                    label = { Text("Código (opcional)") },
                    singleLine = true,
                    colors = darkFieldColors(),
                    modifier = Modifier.weight(1.2f)
                )
                if (row.trackStock) {
                    OutlinedTextField(
                        value = row.stockQuantity,
                        onValueChange = { onChange(row.copy(stockQuantity = it)) },
                        label = { Text("Stock") },
                        singleLine = true,
                        colors = darkFieldColors(),
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<Pair<String, String>>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.find { it.first == selectedId }?.second ?: "Seleccionar categoría"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría existente") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = darkFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Zinc900)
        ) {
            categories.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name, color = Color.White) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Zinc950,
    unfocusedContainerColor = Zinc950,
    focusedBorderColor = KomandaTokens.AccentTertiary,
    unfocusedBorderColor = Zinc800,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = KomandaTokens.AccentTertiary,
    unfocusedLabelColor = Zinc400,
    cursorColor = KomandaTokens.AccentTertiary
)

@Composable
private fun OwnerOnlyNotice(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Zinc950)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Zinc800, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Zinc400, modifier = Modifier.size(40.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    description,
                    fontSize = 13.sp,
                    color = Zinc400,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
