package com.komanda.kiosk.ui.qr

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.komanda.kiosk.ui.theme.Amber400

@Composable
fun QrCodeDisplay(
    content: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 260.dp,
    quietZoneDp: Dp = 14.dp,
    contentDescriptionText: String = "Código QR de pago Mercado Pago"
) {
    var qrBitmap by remember(content) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(content) { mutableStateOf(true) }

    LaunchedEffect(content) {
        if (content.isNotBlank()) {
            isLoading = true
            qrBitmap = QrCodeGenerator.generateBitmap(content, sizePx = 512)
            isLoading = false
        } else {
            qrBitmap = null
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(quietZoneDp)
            .semantics { contentDescription = contentDescriptionText },
        contentAlignment = Alignment.Center
    ) {
        val bitmap = qrBitmap
        if (bitmap != null && !isLoading) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescriptionText,
                modifier = Modifier.size(sizeDp - (quietZoneDp * 2))
            )
        } else if (isLoading) {
            CircularProgressIndicator(
                color = Amber400,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
