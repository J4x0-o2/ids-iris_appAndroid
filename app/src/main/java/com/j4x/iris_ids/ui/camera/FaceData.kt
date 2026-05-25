package com.j4x.iris_ids.ui.camera

/**
 * Resultado por frame de ML Kit Face Detection.
 * Null en los campos de clasificación indica que el modelo no pudo
 * estimarlos (cara muy inclinada, oclusión parcial, etc.).
 */
data class FaceData(
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    /** Rotación Y: positivo = cara girada a la derecha del usuario. */
    val headEulerAngleY: Float,
    /** Rotación X: positivo = cara inclinada hacia arriba. */
    val headEulerAngleX: Float = 0f,
    /** Rotación Z: inclinación lateral. */
    val headEulerAngleZ: Float = 0f,
    /** Brillo medio del frame (canal Y, 0-255). */
    val brightness: Float = 128f,
    /** Ancho del bounding box como fracción del ancho de la imagen. */
    val faceWidthRatio: Float = 0f,
)
