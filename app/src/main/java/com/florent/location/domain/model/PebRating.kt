package com.florent.location.domain.model

enum class PebRating {
    A_PLUS,
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    UNKNOWN
}

fun PebRating.toDisplayLabel(): String =
    name.replace("_PLUS", "+")
