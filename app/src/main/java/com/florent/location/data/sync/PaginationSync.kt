package com.florent.location.data.sync

import android.util.Log

internal const val SYNC_PAGE_SIZE = 1000

internal data class PagedFetchResult<T>(
    val rows: List<T>,
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
