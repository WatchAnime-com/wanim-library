package com.wanim_ms.wanimlibrary.spec

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

open class PaginationSpec {
    @Transient
    var page: Int = 1
        get() = field.coerceAtLeast(1)
    @Transient
    var size: Int = 10
        get() = field.coerceAtLeast(1)
    fun ofPageable(): Pageable {
        val page = this.page.minus(1).coerceAtLeast(0)
        val size = this.size.coerceAtLeast(1)
        return PageRequest.of(page, size)
    }

    fun ofPageable(sort: Sort): Pageable {
        val page = this.page.minus(1).coerceAtLeast(0)
        val size = this.size.coerceAtLeast(1)
        return PageRequest.of(page, size, sort)
    }
}