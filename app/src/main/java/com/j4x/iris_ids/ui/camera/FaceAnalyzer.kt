package com.j4x.iris_ids.ui.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * Analiza cada frame de CameraX con ML Kit Face Detection y emite [FaceData].
 *
 * Configuración elegida:
 *   - PERFORMANCE_MODE_FAST: prioriza latencia sobre precisión, adecuado para
 *     detección en tiempo real (liveness a ≥15 fps).
 *   - CLASSIFICATION_MODE_ALL: activa probabilidades de ojos abiertos/cerrados,
 *     necesarias para el desafío BLINK.
 *   - enableTracking: mantiene un ID de cara entre frames, evita confusión si
 *     hay varias caras en el encuadre.
 *   - setMinFaceSize(0.15f): ignora caras muy pequeñas (alejadas de la cámara).
 *
 * El detector se crea una sola vez y se reutiliza entre frames para evitar
 * el coste de inicialización del modelo.
 */
class FaceAnalyzer(
    private val onFaceData: (FaceData?) -> Unit,
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
    )

    @ExperimentalGetImage
    override fun analyze(proxy: ImageProxy) {
        val mediaImage = proxy.image
        if (mediaImage == null) {
            proxy.close()
            return
        }

        // Brillo medio del canal Y (luminancia) — sin copiar el buffer completo
        val brightness = proxy.planes[0].buffer.let { buf ->
            val step = (buf.remaining() / 1024).coerceAtLeast(1)
            var sum = 0L; var count = 0
            while (buf.hasRemaining()) {
                sum += (buf.get().toInt() and 0xFF)
                count++
                if (buf.remaining() >= step) repeat(step - 1) { buf.get() }
            }
            if (count > 0) sum.toFloat() / count else 128f
        }

        val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                val face = faces.firstOrNull()
                onFaceData(
                    face?.let {
                        val widthRatio = it.boundingBox.width().toFloat() / proxy.width
                        FaceData(
                            leftEyeOpenProbability  = it.leftEyeOpenProbability,
                            rightEyeOpenProbability = it.rightEyeOpenProbability,
                            headEulerAngleY         = it.headEulerAngleY,
                            headEulerAngleX         = it.headEulerAngleX,
                            headEulerAngleZ         = it.headEulerAngleZ,
                            brightness              = brightness,
                            faceWidthRatio          = widthRatio,
                        )
                    }
                )
            }
            .addOnFailureListener { onFaceData(null) }
            .addOnCompleteListener { proxy.close() }
    }
}
