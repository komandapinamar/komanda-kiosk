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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.komanda.kiosk.ui.theme.KomandaTokens
import com.komanda.kiosk.ui.theme.Red400
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EspressoQuickAddDialog(
    scannedBarcode: String?,
    suggestion: BarcodeSuggestionDto?,
    categories: List<CatalogCategoryDto>,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    onSave: (
        name: String,
        price: String,
        categoryId: String?,
        newCategoryName: String?,
        barcode: String?,
        isGeneric: Boolean,
        icon: String?,
        trackStock: Boolean,
        stock: Int
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(suggestion?.name ?: "") }
    var price by remember { mutableStateOf("") }
    val initialMatchedCategory = suggestion?.suggestedCategory?.let { sug ->
        categories.find { it.name.equals(sug, ignoreCase = true) }
    }
    var isCreatingNewCategory by remember { mutableStateOf(categories.isEmpty()) }
    var newCategoryName by remember {
        mutableStateOf(if (categories.isEmpty()) (suggestion?.suggestedCategory ?: "") else "")
    }
    var selectedCategoryId by remember {
        mutableStateOf(initialMatchedCategory?.id ?: categories.firstOrNull()?.id ?: "")
    }
    var isGeneric by remember { mutableStateOf(scannedBarcode.isNullOrBlank()) }
    var trackStock by remember { mutableStateOf(false) }
    var stockQuantity by remember { mutableStateOf("10") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
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
                        color = KomandaTokens.AccentTertiary
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

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Red400,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
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
                        colors = CheckboxDefaults.colors(checkedColor = KomandaTokens.AccentTertiary)
                    )
                    Text("Producto genérico táctil (sin código de barra)")
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

                // Selector de Categoría (sincronizado con Backoffice)
                if (isCreatingNewCategory || categories.isEmpty()) {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Nombre de la nueva categoría") },
                            placeholder = { Text("Ej. Golosinas, Bebidas, Cigarrillos") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KomandaTokens.AccentTertiary,
                                unfocusedBorderColor = Zinc800
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { isCreatingNewCategory = false },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Elegir categoría existente", color = KomandaTokens.AccentTertiary, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    Column {
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
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KomandaTokens.AccentTertiary,
                                    unfocusedBorderColor = Zinc800
                                ),
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
                                DropdownMenuItem(
                                    text = { Text("+ Nueva categoría...", fontWeight = FontWeight.Bold, color = KomandaTokens.AccentTertiary) },
                                    onClick = {
                                        isCreatingNewCategory = true
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
                        colors = CheckboxDefaults.colors(checkedColor = KomandaTokens.AccentTertiary)
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

                val isCategoryValid = if (isCreatingNewCategory || categories.isEmpty()) {
                    newCategoryName.isNotBlank()
                } else {
                    selectedCategoryId.isNotBlank()
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            val stock = (stockQuantity.toIntOrNull() ?: 0).coerceAtLeast(0)
                            val code = if (isGeneric) null else scannedBarcode?.trim()
                            onSave(
                                name,
                                price,
                                if (isCreatingNewCategory || categories.isEmpty()) null else selectedCategoryId,
                                if (isCreatingNewCategory || categories.isEmpty()) newCategoryName.trim() else null,
                                code,
                                isGeneric,
                                null,
                                trackStock,
                                stock
                            )
                        },
                        enabled = name.isNotBlank() && price.isNotBlank() && isCategoryValid && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = KomandaTokens.AccentTertiary, contentColor = KomandaTokens.AccentPrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = KomandaTokens.AccentPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar producto", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
