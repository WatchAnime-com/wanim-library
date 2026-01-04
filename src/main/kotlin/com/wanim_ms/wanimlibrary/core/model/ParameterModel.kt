package com.wanim_ms.wanimlibrary.core.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.wanim_ms.wanimlibrary.core.spec.PaginationSpec

/**
 * Base parameter model for search/filter operations.
 * Extends PaginationSpec and adds search, sorting capabilities.
 */
@JsonIgnoreProperties(value = ["page", "size", "sortBy", "sortOrder", "search"])
open class ParameterModel : PaginationSpec() {
    
    @Transient
    var search: String? = null
        get() = field?.trim()?.lowercase()

    @Transient
    var sortBy: String? = null
        get() = field?.trim()

    @Transient
    var sortOrder: SortOrder = SortOrder.ASC
}

/**
 * Sort order enumeration for query sorting.
 */
enum class SortOrder {
    ASC {
        override val label: String = "sort.order.asc"
    },
    DESC {
        override val label: String = "sort.order.desc"
    };

    abstract val label: String
    
    open val value: String
        get() = name.uppercase()
    
    open val color: String? = null
    open val icon: String? = null
}
