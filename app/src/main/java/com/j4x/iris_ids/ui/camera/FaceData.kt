package com.j4x.iris_ids.ui.camera

/**
 * Resultado por frame de ML Kit Face Detection.
 * Null en los campos de clasificación indica que el modelo no pudo
 * estimarlos (cara muy inclinada, oclusión parcial, etc.).
 */
data class FaceData(
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    /** Rotación alrededor del eje Y: positivo = cara girada a la derecha del usuario. */
    val headEulerAngleY: Float,
)
