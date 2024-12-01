package com.wanim_ms.wanimlibrary.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.wanim_ms.wanimlibrary.spec.PaginationSpec


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
 * This enum represents the order to sort in.
 * It provides labels, values, colors, and icons for each order.
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
