package com.j4x.iris_ids.domain.model

enum class LivenessChallenge(val instruction: String) {
    BLINK("Parpadee lentamente"),
    HEAD_LEFT("Gire la cabeza a la izquierda"),
    HEAD_RIGHT("Gire la cabeza a la derecha");

    companion object {
        fun randomSequence(): List<LivenessChallenge> = entries.shuffled()
    }
}
