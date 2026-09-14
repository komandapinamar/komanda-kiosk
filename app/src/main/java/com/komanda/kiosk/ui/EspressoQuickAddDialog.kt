package com.komanda.kiosk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.komanda.kiosk.core.network.BarcodeSuggestionDto
import com.komanda.kiosk.core.network.CatalogCategoryDto
import com.komanda.kiosk.ui.theme.Amber400
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EspressoQuickAddDialog(
    scannedBarcode: String?,
    suggestion: BarcodeSuggestionDto?,
    categories: List<CatalogCategoryDto>,
    onSave: (name: String, price: String, categoryId: String, barcode: String?, isGeneric: Boolean, icon: String?, trackStock: Boolean, stock: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(suggestion?.name ?: "") }
    var price by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var isGeneric by remember { mutableStateOf(scannedBarcode.isNullOrBlank()) }
    var genericIcon by remember { mutableStateOf("General") }
    var trackStock by remember { mutableStateOf(false) }
    var stockQuantity by remember { mutableStateOf("10") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val tags = listOf("General", "Bebida", "Snack", "Café", "Panadería", "Comida", "Golosinas", "Fruta")

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Zinc900,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Alta Rápida de Producto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (scannedBarcode != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Código escaneado: $scannedBarcode",
                        style = MaterialTheme.typography.bodySmall,
                        color = Amber400
                    )
                }

                if (suggestion != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sugerencia autocompletada por la red Komanda",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox Producto Genérico
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isGeneric = !isGeneric }
                ) {
                    Checkbox(
                        checked = isGeneric,
                        onCheckedChange = { isGeneric = it },
                        colors = CheckboxDefaults.colors(checkedColor = Amber400)
                    )
                    Text("Producto genérico táctil (sin código de barra)")
                }

                if (isGeneric) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Etiqueta rápida:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tags) { tag ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (genericIcon == tag) Amber400.copy(alpha = 0.2f) else Zinc950,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { genericIcon = tag }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(tag, fontSize = 12.sp, color = if (genericIcon == tag) Amber400 else Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio de venta ($)") },
                    placeholder = { Text("Ej. 3500.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de Categoría
                if (categories.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        val currentCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Elegir categoría"
                        OutlinedTextField(
                            value = currentCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Control de Stock
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { trackStock = !trackStock }
                ) {
                    Checkbox(
                        checked = trackStock,
                        onCheckedChange = { trackStock = it },
                        colors = CheckboxDefaults.colors(checkedColor = Amber400)
                    )
                    Text("Controlar stock de este producto")
                }

                if (trackStock) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text("Cantidad inicial de stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            val stock = stockQuantity.toIntOrNull() ?: 0
                            val code = if (isGeneric) null else scannedBarcode
                            onSave(name, price, selectedCategoryId, code, isGeneric, if (isGeneric) genericIcon else null, trackStock, stock)
                        },
                        enabled = name.isNotBlank() && price.isNotBlank() && selectedCategoryId.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Zinc950)
                    ) {
                        Text("Guardar y agregar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
