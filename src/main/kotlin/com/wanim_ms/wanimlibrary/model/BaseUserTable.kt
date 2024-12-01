package com.wanim_ms.wanimlibrary.model

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.security.SecureRandom
import java.util.*
import kotlin.math.abs

@MappedSuperclass
open class BaseUserTable(

    @Column(name = "sk", unique = true, updatable = false, nullable = false, length = 11)
    var sk: UUID = UUID.randomUUID(),

    @Column(name = "pk", unique = true, nullable = false, updatable = false, length = 12)
    var pk: Long = String.format(
        "%012d",
        abs(sk.mostSignificantBits - (sk.leastSignificantBits + System.currentTimeMillis())) % 1000000000000
    ).toLong(),

    @Column(nullable = false)
    var deleted: Boolean = false,

    @Column(nullable = false)
    var archived: Boolean = false,

    @Column(nullable = false, updatable = false)
    var createdAt: Date = Date(),

    @Column(nullable = false)
    var updatedAt: Date = Date()
) {
    // Optionally, you can have methods for updating the time fields
    fun preUpdate() {
        updatedAt = Date()
    }

    // Generate a unique primary key
    fun generatePk(): Long {
        val secureRandom = SecureRandom()
        val randomBits = secureRandom.nextLong()
        val nanoTime = System.nanoTime()
        return (randomBits xor nanoTime) and Long.MAX_VALUE // Ensure it is not negative
    }

    open class SearchParams : ParameterModel()
}
/*
@MappedSuperclass
open class BaseModelTable(

    @Column(name = "sk", unique = true, updatable = false, nullable = false, length = 11)
    var sk: UUID = UUID.randomUUID(),

    @Column(name = "pk", unique = true, nullable = false, updatable = false, length = 12)
    var pk: Long = String.format(
        "%012d",
        abs(sk.mostSignificantBits - (sk.leastSignificantBits + System.currentTimeMillis())) % 1000000000000
    ).toLong(),

    @Column(nullable = false)
    var deleted: Boolean = false,

    @Column(nullable = false)
    var archived: Boolean = false,

    @Column(nullable = false, updatable = false)
    var createdAt: Date = Date(),

    @Column(nullable = false)
    var updatedAt: Date = Date()
) {
    // Optionally, you can have methods for updating the time fields
    fun preUpdate() {
        updatedAt = Date()
    }

    // Generate a unique primary key
    fun generatePk(): Long {
        val secureRandom = SecureRandom()
        val randomBits = secureRandom.nextLong()
        val nanoTime = System.nanoTime()
        return (randomBits xor nanoTime) and Long.MAX_VALUE // Ensure it is not negative
    }

}
 */