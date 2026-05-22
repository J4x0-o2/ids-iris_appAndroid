package com.j4x.iris_ids.ui.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

/**
 * Composable compartido para FaceLoginScreen y CaptureScreen.
 *
 * Encapsula todo el ciclo de vida de CameraX: Preview, ImageAnalysis (ML Kit)
 * e ImageCapture. El resultado del análisis llega vía [onFaceData]; la
 * captura de frames vía [captureController].
 *
 * Diseño de threading:
 *   - Preview y lifecycle binding: hilo principal (mainExecutor de CameraX).
 *   - ImageAnalysis: hilo dedicado (singleThreadExecutor) para no bloquear UI.
 *   - onFaceData callback: llamado desde el hilo de análisis → el ViewModel
 *     deberá ser thread-safe (MutableStateFlow.update lo es por diseño).
 *
 * rememberUpdatedState garantiza que aunque el lambda [onFaceData] se
 * recomponga (ej. ViewModel recrea la referencia), FaceAnalyzer siempre
 * invoca la versión más reciente sin recrear el analyzer completo.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    captureController: CaptureController,
    onFaceData: (FaceData?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner    = LocalLifecycleOwner.current
    val currentOnFaceData by rememberUpdatedState(onFaceData)
    val analyzerExecutor  = Executors.newSingleThreadExecutor()

    AndroidView(
        modifier = modifier,
        factory  = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val cameraFuture = ProcessCameraProvider.getInstance(ctx)
            cameraFuture.addListener({
                val provider = cameraFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { ia ->
                        ia.setAnalyzer(analyzerExecutor, FaceAnalyzer { data ->
                            currentOnFaceData(data)
                        })
                    }

                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                captureController.bind(capture)

                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        analysis,
                        capture,
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "bindToLifecycle failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
    )

    DisposableEffect(Unit) {
        onDispose { captureController.unbind() }
    }
}
