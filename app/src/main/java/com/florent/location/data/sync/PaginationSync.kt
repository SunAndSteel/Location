package com.florent.location.data.sync

import android.util.Log

internal const val SYNC_PAGE_SIZE = 1000

internal data class PagedFetchResult<T>(
    val rows: List<T>,
    val pageCount: Int,
    val durationMs: Long
)

internal data class PagedProcessingResult(
    val processedCount: Int,
    val pageCount: Int,
    val durationMs: Long
)

internal suspend fun <T> fetchAllPagedWithMetrics(
    tag: String,
    pageLabel: String,
    pageSize: Int = SYNC_PAGE_SIZE,
    fetchPage: suspend (fromInclusive: Int, toInclusive: Int) -> List<T>
): PagedFetchResult<T> {
    val startedAt = System.currentTimeMillis()
    val allRows = mutableListOf<T>()
    var pageIndex = 0

    while (true) {
        val from = pageIndex * pageSize
        val to = from + pageSize - 1
        val page = fetchPage(from, to)
        allRows += page
        Log.d(tag, "$pageLabel page=$pageIndex range=$from-$to size=${page.size} total=${allRows.size}")

        if (page.size < pageSize) break
        pageIndex++
    }

    val pageCount = pageIndex + 1
    val durationMs = System.currentTimeMillis() - startedAt
    Log.i(tag, "$pageLabel summary pages=$pageCount volume=${allRows.size} durationMs=$durationMs")
    return PagedFetchResult(rows = allRows, pageCount = pageCount, durationMs = durationMs)
}

internal suspend fun <T> fetchAllPaged(
    tag: String,
    pageLabel: String,
    pageSize: Int = SYNC_PAGE_SIZE,
    fetchPage: suspend (fromInclusive: Int, toInclusive: Int) -> List<T>
): List<T> {
    return fetchAllPagedWithMetrics(tag, pageLabel, pageSize, fetchPage).rows
}

internal suspend fun <T> processKeysetPagedWithMetrics(
    tag: String,
    pageLabel: String,
    initialCursor: CompositeSyncCursor?,
    pageSize: Int = SYNC_PAGE_SIZE,
    fetchPage: suspend (updatedAtFromInclusiveIso: String?, limit: Int) -> List<T>,
    extractUpdatedAt: (T) -> String?,
    extractRemoteId: (T) -> String,
    processPage: suspend (List<T>) -> Unit,
    onCursorAdvanced: suspend (CompositeSyncCursor) -> Unit
): PagedProcessingResult {
    val startedAt = System.currentTimeMillis()
    var cursor = initialCursor
    var pageIndex = 0
    var processedCount = 0

    while (true) {
        val sinceIso = cursor?.let { toServerCursorIso(it.updatedAtEpochMillis) }
        var requestedLimit = pageSize
        var page = fetchPage(sinceIso, requestedLimit)
        var filtered = page.filter { row ->
            isAfterCursor(extractUpdatedAt(row), extractRemoteId(row), cursor)
        }

        while (filtered.isEmpty() && page.size == requestedLimit && requestedLimit < pageSize * 16) {
            requestedLimit *= 2
            page = fetchPage(sinceIso, requestedLimit)
            filtered = page.filter { row ->
                isAfterCursor(extractUpdatedAt(row), extractRemoteId(row), cursor)
            }
        }

        if (filtered.isNotEmpty()) {
            processPage(filtered)
            filtered.maxCompositeCursorOrNull { row ->
                parseServerEpochMillis(extractUpdatedAt(row))?.let { updatedAt ->
                    CompositeSyncCursor(updatedAtEpochMillis = updatedAt, remoteId = extractRemoteId(row))
                }
            }?.let { nextCursor ->
                cursor = nextCursor
                onCursorAdvanced(nextCursor)
            }
        } else if (page.isNotEmpty()) {
            Log.w(tag, "$pageLabel page=$pageIndex received only already-processed rows after overfetch; stopping to avoid infinite loop")
            pageIndex++
            break
        }

        processedCount += filtered.size
        Log.d(
            tag,
            "$pageLabel page=$pageIndex size=${page.size} requestedLimit=$requestedLimit accepted=${filtered.size} processedTotal=$processedCount"
        )

        pageIndex++
        if (page.size < pageSize) break
    }

    val durationMs = System.currentTimeMillis() - startedAt
    Log.i(tag, "$pageLabel summary pages=$pageIndex processed=$processedCount durationMs=$durationMs")
    return PagedProcessingResult(processedCount = processedCount, pageCount = pageIndex, durationMs = durationMs)
}
