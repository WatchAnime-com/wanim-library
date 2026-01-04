package com.wanim_ms.wanimlibrary.core.model

import jakarta.persistence.*
import java.security.SecureRandom
import java.util.*
import kotlin.math.abs

/**
 * Base entity model for all JPA entities.
 * Provides common fields: id, sk (UUID), pk (numeric key), deleted, archived, timestamps.
 *
 * @param ID The type of the entity's primary key
 */
@MappedSuperclass
abstract class BaseModel<ID>(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: ID? = null,

    @Column(name = "sk", unique = true, updatable = false, nullable = false, length = 36)
    var sk: UUID = UUID.randomUUID(),

    @Column(name = "pk", unique = true, nullable = false, updatable = false)
    var pk: Long = generatePk(sk),

    @Column(nullable = false)
    var deleted: Boolean = false,

    @Column(nullable = false)
    var archived: Boolean = false,

    @Column(nullable = false, updatable = false)
    var createdAt: Date = Date(),

    @Column(nullable = false)
    var updatedAt: Date = Date()
) {
    
    companion object {
        private fun generatePk(uuid: UUID): Long {
            return String.format(
                "%012d",
                abs(uuid.mostSignificantBits - (uuid.leastSignificantBits + System.currentTimeMillis())) % 1000000000000
            ).toLong()
        }
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Date()
    }

    /**
     * Generate a cryptographically secure unique primary key
     */
    fun generateNewPk(): Long {
        val secureRandom = SecureRandom()
        val randomBits = secureRandom.nextLong()
        val nanoTime = System.nanoTime()
        return (randomBits xor nanoTime) and Long.MAX_VALUE
    }

    open fun getId(): ID? = id

    open fun setId(id: ID) {
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseModel<*>) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
