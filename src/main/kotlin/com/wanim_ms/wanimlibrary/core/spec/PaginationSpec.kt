package com.wanim_ms.wanimlibrary.core.spec

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * Base pagination specification for pageable requests.
 * Page is 1-indexed (user-friendly), converted to 0-indexed for Spring Data.
 */
open class PaginationSpec {
    
    @Transient
    var page: Int = 1
        get() = field.coerceAtLeast(1)
    
    @Transient
    var size: Int = 10
        get() = field.coerceIn(1, MAX_PAGE_SIZE)

    /**
     * Create a Pageable without sorting.
     */
    fun ofPageable(): Pageable {
        return PageRequest.of(
            (page - 1).coerceAtLeast(0),
            size.coerceAtLeast(1)
        )
    }

    /**
     * Create a Pageable with the specified sort.
     */
    fun ofPageable(sort: Sort): Pageable {
        return PageRequest.of(
            (page - 1).coerceAtLeast(0),
            size.coerceAtLeast(1),
            sort
        )
    }

    companion object {
        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_PAGE_SIZE = 10
    }
}
