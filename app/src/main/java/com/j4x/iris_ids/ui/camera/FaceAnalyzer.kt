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

        val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                val face = faces.firstOrNull()
                onFaceData(
                    face?.let {
                        FaceData(
                            leftEyeOpenProbability  = it.leftEyeOpenProbability,
                            rightEyeOpenProbability = it.rightEyeOpenProbability,
                            headEulerAngleY         = it.headEulerAngleY,
                        )
                    }
                )
            }
            .addOnFailureListener { onFaceData(null) }
            .addOnCompleteListener { proxy.close() }
    }
}
