package com.wanim_ms.wanimlibrary.model

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.security.SecureRandom
import java.util.*

@MappedSuperclass
class BaseTableRepo(
    @Column(nullable = false)
    var deleted: Boolean = false,

    @Column(nullable = false)
    var archived: Boolean = false,

    @Column(nullable = false, updatable = false)
    var createdAt: Date = Date(),

    @Column(nullable = false)
    var updatedAt: Date = Date()
) {
    fun preUpdate() {
        updatedAt = Date()
    }
    open class SearchParams : ParameterModel()
}