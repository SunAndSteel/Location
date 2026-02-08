package com.florent.location.ui.components

import com.florent.location.domain.model.HousingSituation

fun housingSituationLabel(situation: HousingSituation): String =
    when (situation) {
        HousingSituation.LIBRE -> "Libre"
        HousingSituation.OCCUPE -> "Occupé"
        HousingSituation.DRAFT -> "Brouillon"
    }
