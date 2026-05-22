package com.j4x.iris_ids.ui.camera

import android.util.Base64
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/**
 * Puente entre el Composable (que posee el use case de CameraX) y el ViewModel
 * (que decide cuándo capturar).
 *
 * Por qué existe esta clase:
 *   ImageCapture es un use case de CameraX con ciclo de vida propio; no puede
 *   vivir en el ViewModel (que no tiene contexto Android). CaptureController
 *   actúa como holder: el Composable le inyecta el ImageCapture cuando la
 *   cámara está lista, y el ViewModel llama a takePicture() cuando liveness
 *   se completa. El resultado (JPEG en Base64) viaja de vuelta al ViewModel.
 */
class CaptureController {

    private var imageCapture: ImageCapture? = null
    private val executor = Executors.newSingleThreadExecutor()

    internal fun bind(capture: ImageCapture) { imageCapture = capture }
    internal fun unbind() { imageCapture = null }

    /**
     * Captura un frame JPEG y lo devuelve codificado en Base64 (sin saltos de línea).
     * Retorna cadena vacía si la cámara no está lista.
     *
     * JPEG asegurado: ImageCapture.OnImageCapturedCallback entrega siempre JPEG
     * (documentado en CameraX). El buffer de planes[0] es el JPEG completo.
     */
    suspend fun takePicture(): String = suspendCancellableCoroutine { cont ->
        val capture = imageCapture ?: run { cont.resume(""); return@suspendCancellableCoroutine }

        capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val buffer = image.planes[0].buffer
                    val bytes  = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    cont.resume(Base64.encodeToString(bytes, Base64.NO_WRAP))
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                cont.resume("") // no lanzar excepción; en mock vacío es aceptable
            }
        })
    }
}
