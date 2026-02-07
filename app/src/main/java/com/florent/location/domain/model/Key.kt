package com.florent.location.domain.model

data class Key(
    val id: Long,
    val leaseId: Long,
    val name: String,
    val description: String?
)