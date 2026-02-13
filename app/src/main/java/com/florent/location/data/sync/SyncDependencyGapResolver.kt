package com.florent.location.data.sync

internal data class DependencyGapResolution<T>(
    val mapped: List<T>,
    val stoppedOnMissingDependency: Boolean
)

internal suspend inline fun <Row, Entity> mapRowsStoppingAtDependencyGap(
    rows: List<Row>,
    crossinline mapRow: suspend (Row) -> Entity?,
    crossinline onMissingDependency: suspend (Row) -> Unit
): DependencyGapResolution<Entity> {
    val mapped = mutableListOf<Entity>()
    for (row in rows) {
        val entity = mapRow(row)
        if (entity == null) {
            onMissingDependency(row)
            return DependencyGapResolution(mapped = mapped, stoppedOnMissingDependency = true)
        }
        mapped += entity
    }
    return DependencyGapResolution(mapped = mapped, stoppedOnMissingDependency = false)
}

