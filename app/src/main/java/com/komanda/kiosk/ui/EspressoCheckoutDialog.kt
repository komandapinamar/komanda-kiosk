package com.komanda.kiosk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.komanda.kiosk.ui.theme.Amber400
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EspressoCheckoutDialog(
    totalAmount: Double,
    onSelectPaymentMethod: (method: String) -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "Elegí tu medio de pago",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Total a pagar: $${String.format(java.util.Locale.US, "%.2f", totalAmount)} ARS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Amber400
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Option 1: Efectivo
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Zinc950,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Amber400.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPaymentMethod("cash") }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("[Efectivo]", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Amber400)
                            Spacer(modifier = Modifier.padding(8.dp))
                            Column {
                                Text(
                                    text = "Efectivo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Se imprimirá un ticket para abonar en mostrador",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Amber400
                                )
                            }
                        }
                    }

                    // Option 2: QR Mercado Pago
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Zinc950,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPaymentMethod("qr") }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("[QR]", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Amber400)
                            Spacer(modifier = Modifier.padding(8.dp))
                            Column {
                                Text(
                                    text = "QR Mercado Pago",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Escanear desde la app de MP o billeteras interoperables (saldo y tarjetas)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Volver al pedido", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
