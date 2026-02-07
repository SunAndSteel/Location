package com.florent.location.domain.model

data class IndexationPolicy(
    val anniversaryEpochDay: Long,
    val nextIndexationEpochDay: Long,
    val frequencyYears: Int = 1,
    val ruleLabel: String = "Indexation annuelle à la date anniversaire"
)
