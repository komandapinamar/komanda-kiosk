package com.komanda.kiosk.ui.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.komanda.kiosk.core.network.KioskPaymentSessionResponse
import com.komanda.kiosk.core.network.KioskPaymentStatusResponse
import com.komanda.kiosk.ui.theme.Amber400
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskQrPaymentDialog(
    session: KioskPaymentSessionResponse,
    onPollStatus: suspend (attemptId: String) -> KioskPaymentStatusResponse?,
    onApproved: (KioskPaymentStatusResponse) -> Unit,
    onCancel: (attemptId: String) -> Unit,
    onTimeout: (attemptId: String) -> Unit,
    onDismiss: () -> Unit
) {
    var secondsRemaining by remember(session.paymentAttemptId) {
        mutableIntStateOf(session.timeoutSeconds)
    }

    LaunchedEffect(session.paymentAttemptId) {
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        onTimeout(session.paymentAttemptId)
    }

    LaunchedEffect(session.paymentAttemptId) {
        while (secondsRemaining > 0) {
            try {
                val status = onPollStatus(session.paymentAttemptId)
                if (status != null && status.status == "approved") {
                    onApproved(status)
                    return@LaunchedEffect
                }
                if (status != null && (status.status == "failed" || status.status == "expired" || status.status == "cancelled")) {
                    onTimeout(session.paymentAttemptId)
                    return@LaunchedEffect
                }
            } catch (_: Exception) {
                // Ignore transient network errors, keep polling while timer remains
            }
            delay(1500L)
        }
    }

    val isUrgent = secondsRemaining <= 30
    val timerColor = if (isUrgent) Color(0xFFEF4444) else Amber400
    val progress = (secondsRemaining.toFloat() / session.timeoutSeconds.toFloat()).coerceIn(0f, 1f)
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val formattedTime = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Zinc900,
            tonalElevation = 8.dp,
            modifier = Modifier.width(420.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✦ PAGO CON QR",
                    color = Amber400,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Escaneá para pagar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Total: $${session.total} ${session.currency}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Amber400
                )

                Spacer(modifier = Modifier.height(18.dp))

                // QR Code Display
                QrCodeDisplay(
                    content = session.qrData,
                    sizeDp = 260.dp,
                    quietZoneDp = 14.dp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Timer & Progress
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUrgent) "¡Tiempo por expirar!" else "Tiempo restante:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isUrgent) Color(0xFFEF4444) else Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = formattedTime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = timerColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = timerColor,
                        trackColor = Zinc950
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Apuntá la cámara con Mercado Pago o tu billetera interoperable favorita.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onCancel(session.paymentAttemptId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Zinc800,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Cancelar y volver",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
