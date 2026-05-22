package com.j4x.iris_ids.ui.camera

import com.j4x.iris_ids.domain.model.LivenessChallenge

/**
 * Máquina de estados de liveness — pura Kotlin, sin dependencias Android.
 *
 * Recibe un [FaceData] por frame (llamar [process]) y avanza
 * internamente cuando detecta que el desafío actual fue completado.
 *
 * Umbrales por desafío:
 *   BLINK      — ambos ojos < 0.2 durante ≥ 2 frames, luego > 0.7 durante ≥ 2 frames
 *   HEAD_LEFT  — headEulerAngleY > +20° durante ≥ 3 frames consecutivos
 *   HEAD_RIGHT — headEulerAngleY < -20° durante ≥ 3 frames consecutivos
 *
 * Nota sobre cámara frontal:
 *   CameraX frontal no espeja el buffer de ImageAnalysis (solo el Preview).
 *   ML Kit ve la imagen sin espejo → cuando el usuario gira a su izquierda,
 *   la cara rota a la derecha desde la perspectiva de la cámara → ángulo Y positivo.
 */
class LivenessStateMachine(private val challenges: List<LivenessChallenge>) {

    private var currentIndex = 0

    // Contadores para BLINK
    private var closedFrames = 0
    private var openFrames = 0
    private var blinkPhase = BlinkPhase.WAITING_CLOSE

    // Contador compartido para HEAD_LEFT / HEAD_RIGHT
    private var headFrames = 0

    val currentChallenge: LivenessChallenge? get() = challenges.getOrNull(currentIndex)
    val completedCount: Int get() = currentIndex
    val isComplete: Boolean get() = currentIndex >= challenges.size

    /** Procesa un frame. Retorna true si en este frame se completó algún desafío. */
    fun process(data: FaceData): Boolean {
        val challenge = currentChallenge ?: return false
        val before = currentIndex
        when (challenge) {
            LivenessChallenge.BLINK      -> processBlink(data)
            LivenessChallenge.HEAD_LEFT  -> processHead(data, threshold = +20f, positive = true)
            LivenessChallenge.HEAD_RIGHT -> processHead(data, threshold = -20f, positive = false)
        }
        return currentIndex > before
    }

    private fun processBlink(data: FaceData) {
        val left  = data.leftEyeOpenProbability  ?: return
        val right = data.rightEyeOpenProbability ?: return

        when (blinkPhase) {
            BlinkPhase.WAITING_CLOSE -> {
                if (left < 0.2f && right < 0.2f) {
                    if (++closedFrames >= 2) {
                        blinkPhase = BlinkPhase.WAITING_OPEN
                        openFrames = 0
                    }
                } else {
                    closedFrames = 0
                }
            }
            BlinkPhase.WAITING_OPEN -> {
                if (left > 0.7f && right > 0.7f) {
                    if (++openFrames >= 2) advance()
                } else {
                    openFrames = 0
                }
            }
        }
    }

    private fun processHead(data: FaceData, threshold: Float, positive: Boolean) {
        val inRange = if (positive) data.headEulerAngleY > threshold
                      else          data.headEulerAngleY < threshold
        if (inRange) { if (++headFrames >= 3) advance() }
        else headFrames = 0
    }

    private fun advance() {
        currentIndex++
        closedFrames = 0; openFrames = 0; headFrames = 0
        blinkPhase = BlinkPhase.WAITING_CLOSE
    }

    private enum class BlinkPhase { WAITING_CLOSE, WAITING_OPEN }
}
