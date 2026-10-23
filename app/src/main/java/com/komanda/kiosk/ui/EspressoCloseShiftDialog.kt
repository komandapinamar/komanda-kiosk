package com.komanda.kiosk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.komanda.kiosk.core.network.CashShiftDto
import com.komanda.kiosk.ui.theme.KomandaTokens
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EspressoCloseShiftDialog(
    shift: CashShiftDto,
    onConfirm: (closingBalance: String, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var closingBalance by remember { mutableStateOf(shift.expectedCash ?: shift.openingBalance) }
    var notes by remember { mutableStateOf("") }

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
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Cerrar caja y arqueo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Shift Summary Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Zinc950, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fondo inicial (apertura):", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${shift.openingBalance}", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ventas en efectivo (${shift.orderCount}):", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${shift.currentCashSales ?: "0.00"}", fontWeight = FontWeight.Bold, color = KomandaTokens.AccentTertiary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total esperado en caja:", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "$${shift.expectedCash ?: shift.openingBalance}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = KomandaTokens.AccentTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = closingBalance,
                    onValueChange = { closingBalance = it },
                    label = { Text("Efectivo contado real en caja ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas / Observaciones de cierre") },
                    placeholder = { Text("Ej. Faltaron $50 de cambio") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

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
                            if (closingBalance.isNotBlank()) {
                                onConfirm(closingBalance, notes.ifBlank { null })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KomandaTokens.AccentTertiary, contentColor = KomandaTokens.AccentPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar cierre", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
