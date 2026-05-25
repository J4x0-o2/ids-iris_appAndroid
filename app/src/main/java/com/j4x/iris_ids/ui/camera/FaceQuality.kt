package com.j4x.iris_ids.ui.camera

import kotlin.math.abs

data class FaceQuality(
    val faceDetected: Boolean = false,
    val lightingOk: Boolean = false,
    val angleOk: Boolean = false,
    val faceSizeOk: Boolean = false,
) {
    val isReady: Boolean get() = faceDetected && lightingOk && angleOk && faceSizeOk

    val hint: String get() = when {
        !faceDetected  -> "Centre su rostro en el marco"
        !faceSizeOk    -> "Acérquese a la cámara"
        !lightingOk    -> "Busque mejor iluminación"
        !angleOk       -> "Mire directamente a la cámara"
        else           -> ""
    }

    companion object {
        fun from(data: FaceData): FaceQuality = FaceQuality(
            faceDetected = true,
            lightingOk   = data.brightness in 55f..215f,
            angleOk      = abs(data.headEulerAngleY) < 20f
                        && abs(data.headEulerAngleX) < 15f
                        && abs(data.headEulerAngleZ) < 20f,
            faceSizeOk   = data.faceWidthRatio > 0.22f,
        )
    }
}
