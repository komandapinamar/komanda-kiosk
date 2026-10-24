package com.komanda.kiosk.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.ChecksumException
import com.google.zxing.DecodeHintType
import com.google.zxing.FormatException
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.komanda.kiosk.ui.theme.KomandaTokens
import com.komanda.kiosk.ui.theme.Zinc400
import com.komanda.kiosk.ui.theme.Zinc800
import com.komanda.kiosk.ui.theme.Zinc900
import com.komanda.kiosk.ui.theme.Zinc950
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private val BARCODE_FORMATS = listOf(
    BarcodeFormat.CODE_128,
    BarcodeFormat.CODE_39,
    BarcodeFormat.CODE_93,
    BarcodeFormat.CODABAR,
    BarcodeFormat.EAN_13,
    BarcodeFormat.EAN_8,
    BarcodeFormat.UPC_A,
    BarcodeFormat.UPC_E,
    BarcodeFormat.ITF,
    BarcodeFormat.QR_CODE,
    BarcodeFormat.DATA_MATRIX,
    BarcodeFormat.PDF_417,
    BarcodeFormat.AZTEC
)

/**
 * Full-screen camera barcode scanner backed by CameraX + ZXing.
 * Calls [onBarcodeScanned] once with the first detected code.
 */
@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        permissionDenied = !granted
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            color = Zinc950,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScannerHeader(onClose = onDismiss)
                if (hasPermission) {
                    CameraBarcodePreview(
                        onBarcodeDetected = onBarcodeScanned,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Text(
                        text = "Apuntá al código de barras o QR y mantené el dispositivo quieto.",
                        fontSize = 14.sp,
                        color = Zinc400,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    )
                } else {
                    CameraPermissionNotice(
                        denied = permissionDenied,
                        onRequestPermission = {
                            permissionDenied = false
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onOpenSettings = { openAppSettings(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerHeader(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Zinc900)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = Zinc800, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = KomandaTokens.AccentTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Escanear código",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Cámara del dispositivo",
                fontSize = 12.sp,
                color = Zinc400
            )
        }
        IconButton(onClick = onClose) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cerrar escáner",
                tint = Zinc400
            )
        }
    }
}

@Composable
private fun CameraBarcodePreview(
    onBarcodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnDetected by rememberUpdatedState(onBarcodeDetected)
    val completed = remember { AtomicBoolean(false) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val reader = remember {
        MultiFormatReader().apply {
            setHints(
                mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to BARCODE_FORMATS,
                    DecodeHintType.TRY_HARDER to true
                )
            )
        }
    }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    var cameraError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        var active = true
        var boundProvider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)

        future.addListener(
            {
                if (!active) return@addListener
                try {
                    val provider = future.get()
                    boundProvider = provider

                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        try {
                            if (!completed.get()) {
                                val code = decodeBarcode(imageProxy, reader)
                                if (!code.isNullOrBlank() && completed.compareAndSet(false, true)) {
                                    mainExecutor.execute { currentOnDetected(code) }
                                }
                            }
                        } catch (_: Throwable) {
                            // Keep scanning on transient decoder/camera failures.
                        } finally {
                            imageProxy.close()
                        }
                    }

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                } catch (_: Throwable) {
                    cameraError = "No se pudo iniciar la cámara de este dispositivo."
                }
            },
            mainExecutor
        )

        onDispose {
            active = false
            boundProvider?.unbindAll()
            analysisExecutor.shutdown()
        }
    }

    Box(modifier = modifier.background(Zinc950)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.78f)
                .height(190.dp)
                .border(
                    width = 2.dp,
                    color = KomandaTokens.AccentTertiary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(14.dp)
                )
        )

        cameraError?.let { message ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Zinc900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

private fun decodeBarcode(image: ImageProxy, reader: MultiFormatReader): String? {
    val mediaImage = image.image ?: return null
    val plane = mediaImage.planes.firstOrNull() ?: return null
    val width = mediaImage.width
    val height = mediaImage.height
    if (width <= 0 || height <= 0) return null

    val rowStride = plane.rowStride
    val buffer = plane.buffer.duplicate()
    val data = ByteArray(width * height)
    if (rowStride == width) {
        val available = minOf(data.size, buffer.remaining())
        buffer.get(data, 0, available)
    } else {
        val row = ByteArray(rowStride)
        for (y in 0 until height) {
            val offset = y * rowStride
            if (offset + rowStride > buffer.limit()) break
            buffer.position(offset)
            buffer.get(row, 0, rowStride)
            System.arraycopy(row, 0, data, y * width, width)
        }
    }

    val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
    val uprightSource = when (image.imageInfo.rotationDegrees) {
        90 -> source.rotateCounterClockwise()
            .rotateCounterClockwise()
            .rotateCounterClockwise()
        180 -> source.rotateCounterClockwise().rotateCounterClockwise()
        270 -> source.rotateCounterClockwise()
        else -> source
    }

    return try {
        reader.decodeWithState(BinaryBitmap(HybridBinarizer(uprightSource))).text
    } catch (_: NotFoundException) {
        null
    } catch (_: ChecksumException) {
        null
    } catch (_: FormatException) {
        null
    } finally {
        reader.reset()
    }
}

@Composable
private fun CameraPermissionNotice(
    denied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = Zinc400,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = if (denied) "Permiso de cámara denegado" else "Permiso de cámara requerido",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Para escanear el código del producto necesitamos acceso a la cámara.",
                fontSize = 13.sp,
                color = Zinc400,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KomandaTokens.AccentTertiary,
                        contentColor = KomandaTokens.AccentPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Permitir cámara", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                if (denied) {
                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Zinc800,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Abrir ajustes", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
