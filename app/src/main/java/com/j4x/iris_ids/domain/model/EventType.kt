package com.j4x.iris_ids.domain.model

enum class EventType(val id: String) {
    IN("IN"),
    BREAK_OUT("BREAK_OUT"),
    BREAK_IN("BREAK_IN"),
    FOOD_OUT("FOOD_OUT"),
    FOOD_IN("FOOD_IN"),
    OUT("OUT");

    val isEntrada: Boolean get() = this == IN || this == BREAK_IN || this == FOOD_IN

    companion object {
        fun from(id: String): EventType = entries.first { it.id == id }
    }
}
