# New Files Documentation

## Overview

15 new files were created during the refactoring process. These files either:
1. Provide new functionality not present before
2. Are reorganized versions of deleted files
3. Combine functionality from multiple sources

---

## New Core Files

### 1. EnableWanimLibrary.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/EnableWanimLibrary.kt`

**Purpose:** Main entry point annotation for enabling the library in consumer applications.

```kotlin
package com.wanim_ms.wanimlibrary

import com.wanim_ms.wanimlibrary.config.WanimLibraryConfig
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(WanimLibraryConfig::class)
annotation class EnableWanimLibrary
```

**Usage:**
```kotlin
@SpringBootApplication
@EnableWanimLibrary
class MyApplication
```

---

### 2. WanimLibraryConfig.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/config/WanimLibraryConfig.kt`

**Purpose:** Auto-configuration class that sets up the library when enabled.

```kotlin
package com.wanim_ms.wanimlibrary.config

import com.wanim_ms.wanimlibrary.core.context.AppContextUtil
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent

@Configuration
@ComponentScan(basePackages = ["com.wanim_ms.wanimlibrary"])
class WanimLibraryConfig {

    @Bean
    fun appContextInitializer(): ApplicationListener<ContextRefreshedEvent> {
        return ApplicationListener { event ->
            AppContextUtil.setApplicationContext(event.applicationContext)
        }
    }
}
```

**Key Points:**
- Uses `ContextRefreshedEvent` (Spring Core) instead of `ApplicationReadyEvent` (Spring Boot)
- Component scanning is scoped to library package only
- Initializes `AppContextUtil` with application context

---

### 3. AppContextUtil.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/context/AppContextUtil.kt`

**Purpose:** Provides static access to Spring ApplicationContext.

```kotlin
package com.wanim_ms.wanimlibrary.core.context

import org.springframework.context.ApplicationContext

object AppContextUtil {
    
    @Volatile
    private var applicationContext: ApplicationContext? = null
    
    fun setApplicationContext(context: ApplicationContext) {
        applicationContext = context
    }
    
    fun getApplicationContext(): ApplicationContext {
        return applicationContext 
            ?: throw IllegalStateException("ApplicationContext not initialized. Ensure @EnableWanimLibrary is present.")
    }
    
    inline fun <reified T : Any> getBean(): T {
        return getApplicationContext().getBean(T::class.java)
    }
    
    inline fun <reified T : Any> getBean(name: String): T {
        return getApplicationContext().getBean(name, T::class.java)
    }
    
    fun <T : Any> getBean(clazz: Class<T>): T {
        return getApplicationContext().getBean(clazz)
    }
    
    fun containsBean(name: String): Boolean {
        return applicationContext?.containsBean(name) ?: false
    }
}
```

**Key Points:**
- Thread-safe with `@Volatile`
- Provides reified generic methods for type-safe bean retrieval
- Clear error message when not initialized

---

## Core Model Files

### 4. BaseModel.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/model/BaseModel.kt`

**Purpose:** Abstract base class for all JPA entities.

```kotlin
package com.wanim_ms.wanimlibrary.core.model

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,
    
    @Column(nullable = false, updatable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    open var isActive: Boolean = true
) : Serializable {

    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as BaseModel
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }
}
```

**Key Points:**
- Uses `@MappedSuperclass` for inheritance
- Automatic timestamp management with `@PrePersist` and `@PreUpdate`
- Proper `equals()` and `hashCode()` for JPA entities
- Default `isActive` flag for soft delete support

---

### 5. ParameterModel.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/model/ParameterModel.kt`

**Purpose:** Base class for parameter/configuration entities.

```kotlin
package com.wanim_ms.wanimlibrary.core.model

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class ParameterModel(
    @Column(nullable = false, unique = true)
    open var code: String = "",
    
    @Column(nullable = false)
    open var name: String = "",
    
    @Column
    open var description: String? = null,
    
    @Column(nullable = false)
    open var sortOrder: Int = 0
) : BaseModel()
```

**Key Points:**
- Extends `BaseModel`
- Provides common fields for lookup/parameter tables
- Unique constraint on `code` field

---

## Core Repository Files

### 6. BaseJpaRepository.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/repository/BaseJpaRepository.kt`

**Purpose:** Enhanced JPA repository interface with common query methods.

```kotlin
package com.wanim_ms.wanimlibrary.core.repository

import com.wanim_ms.wanimlibrary.core.model.BaseModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import java.util.Optional

@NoRepositoryBean
interface BaseJpaRepository<T : BaseModel, ID : Any> : 
    JpaRepository<T, ID>, 
    JpaSpecificationExecutor<T>,
    JpaProjectionExecutor<T> {

    fun findByIdAndIsActiveTrue(id: ID): Optional<T>
    
    fun findAllByIsActiveTrue(): List<T>
    
    fun findAllByIsActiveFalse(): List<T>
    
    fun existsByIdAndIsActiveTrue(id: ID): Boolean
}
```

**Key Points:**
- `@NoRepositoryBean` prevents Spring from creating instance
- Extends `JpaSpecificationExecutor` for dynamic queries
- Extends custom `JpaProjectionExecutor` for projections
- Built-in soft delete support methods

---

### 7. JpaProjectionExecutor.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/repository/JpaProjectionExecutor.kt`

**Purpose:** Interface for executing projection queries.

```kotlin
package com.wanim_ms.wanimlibrary.core.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface JpaProjectionExecutor<T> {

    fun <P> findAllProjectedBy(pageable: Pageable, projectionClass: Class<P>): Page<P>
    
    fun <P> findAllProjectedBy(
        spec: Specification<T>?, 
        pageable: Pageable, 
        projectionClass: Class<P>
    ): Page<P>
    
    fun <P> findProjectedById(id: Any, projectionClass: Class<P>): P?
}
```

**Key Points:**
- Supports typed projections
- Works with specifications for filtered projections
- Paginated projection support

---

## Core Service Files

### 8. BaseServiceHandler.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/service/BaseServiceHandler.kt`

**Purpose:** Interface defining standard CRUD operations.

```kotlin
package com.wanim_ms.wanimlibrary.core.service

import com.wanim_ms.wanimlibrary.core.model.BaseModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.util.Optional

interface BaseServiceHandler<T : BaseModel, ID : Any> {

    fun findById(id: ID): Optional<T>
    
    fun findAll(): List<T>
    
    fun findAll(pageable: Pageable): Page<T>
    
    fun findAll(spec: Specification<T>?, pageable: Pageable): Page<T>
    
    fun save(entity: T): T
    
    fun saveAll(entities: List<T>): List<T>
    
    fun deleteById(id: ID)
    
    fun delete(entity: T)
    
    fun existsById(id: ID): Boolean
    
    fun count(): Long
    
    fun softDelete(id: ID): Boolean
    
    fun restore(id: ID): Boolean
}
```

---

### 9. AbstractServiceHandler.kt (NEW)

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/service/AbstractServiceHandler.kt`

**Purpose:** Abstract implementation providing default CRUD operations.

```kotlin
package com.wanim_ms.wanimlibrary.core.service

import com.wanim_ms.wanimlibrary.core.model.BaseModel
import com.wanim_ms.wanimlibrary.core.repository.BaseJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Transactional
abstract class AbstractServiceHandler<T : BaseModel, ID : Any>(
    protected val repository: BaseJpaRepository<T, ID>
) : BaseServiceHandler<T, ID> {

    @Transactional(readOnly = true)
    override fun findById(id: ID): Optional<T> = repository.findById(id)

    @Transactional(readOnly = true)
    override fun findAll(): List<T> = repository.findAll()

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<T> = repository.findAll(pageable)

    @Transactional(readOnly = true)
    override fun findAll(spec: Specification<T>?, pageable: Pageable): Page<T> =
        repository.findAll(spec, pageable)

    override fun save(entity: T): T = repository.save(entity)

    override fun saveAll(entities: List<T>): List<T> = repository.saveAll(entities)

    override fun deleteById(id: ID) = repository.deleteById(id)

    override fun delete(entity: T) = repository.delete(entity)

    @Transactional(readOnly = true)
    override fun existsById(id: ID): Boolean = repository.existsById(id)

    @Transactional(readOnly = true)
    override fun count(): Long = repository.count()

    @Suppress("UNCHECKED_CAST")
    override fun softDelete(id: ID): Boolean {
        val entity = repository.findById(id).orElse(null) ?: return false
        entity.isActive = false
        repository.save(entity)
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun restore(id: ID): Boolean {
        val entity = repository.findById(id).orElse(null) ?: return false
        entity.isActive = true
        repository.save(entity)
        return true
    }
}
```

**Key Points:**
- Provides default implementations for all CRUD operations
- Proper transaction management with `@Transactional`
- Read operations marked `readOnly = true` for optimization
- Built-in soft delete/restore functionality
- **This is a NEW file not present in original codebase**

---

## Core Specification Files

### 10. PaginationSpec.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/spec/PaginationSpec.kt`

**Purpose:** Utilities for pagination.

```kotlin
package com.wanim_ms.wanimlibrary.core.spec

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

object PaginationSpec {

    fun of(page: Int, size: Int): Pageable = PageRequest.of(page, size)

    fun of(page: Int, size: Int, sort: Sort): Pageable = PageRequest.of(page, size, sort)

    fun of(page: Int, size: Int, direction: Sort.Direction, vararg properties: String): Pageable =
        PageRequest.of(page, size, direction, *properties)

    fun defaultPageable(size: Int = 20): Pageable = PageRequest.of(0, size)

    fun unpaged(): Pageable = Pageable.unpaged()
}
```

---

### 11. SpecTool.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/spec/SpecTool.kt`

**Purpose:** Utility functions for building JPA Specifications.

```kotlin
package com.wanim_ms.wanimlibrary.core.spec

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

object SpecTool {

    fun <T> where(block: SpecContext<T>.() -> Predicate?): Specification<T> {
        return Specification { root, query, cb ->
            SpecContext(root, cb).block()
        }
    }

    fun <T> empty(): Specification<T> = Specification { _, _, _ -> null }

    fun <T> combine(vararg specs: Specification<T>?): Specification<T> {
        return specs.filterNotNull().reduceOrNull { acc, spec -> acc.and(spec) } ?: empty()
    }

    class SpecContext<T>(
        val root: Root<T>,
        val cb: CriteriaBuilder
    ) {
        fun <V> path(name: String): Path<V> = root.get(name)

        fun equal(field: String, value: Any?): Predicate? =
            value?.let { cb.equal(root.get<Any>(field), it) }

        fun like(field: String, value: String?): Predicate? =
            value?.let { cb.like(cb.lower(root.get(field)), "%${it.lowercase()}%") }

        fun isTrue(field: String): Predicate = cb.isTrue(root.get(field))

        fun isFalse(field: String): Predicate = cb.isFalse(root.get(field))

        fun isNull(field: String): Predicate = cb.isNull(root.get<Any>(field))

        fun isNotNull(field: String): Predicate = cb.isNotNull(root.get<Any>(field))

        fun <V : Comparable<V>> between(field: String, start: V?, end: V?): Predicate? {
            return when {
                start != null && end != null -> cb.between(root.get(field), start, end)
                start != null -> cb.greaterThanOrEqualTo(root.get(field), start)
                end != null -> cb.lessThanOrEqualTo(root.get(field), end)
                else -> null
            }
        }

        fun <V> inList(field: String, values: Collection<V>?): Predicate? =
            values?.takeIf { it.isNotEmpty() }?.let { root.get<V>(field).`in`(it) }

        fun and(vararg predicates: Predicate?): Predicate =
            cb.and(*predicates.filterNotNull().toTypedArray())

        fun or(vararg predicates: Predicate?): Predicate =
            cb.or(*predicates.filterNotNull().toTypedArray())
    }
}
```

---

### 12. BaseModelJpaSpec.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/spec/BaseModelJpaSpec.kt`

**Purpose:** Pre-built specifications for BaseModel fields.

```kotlin
package com.wanim_ms.wanimlibrary.core.spec

import com.wanim_ms.wanimlibrary.core.model.BaseModel
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

object BaseModelJpaSpec {

    fun <T : BaseModel> isActive(): Specification<T> =
        SpecTool.where { isTrue("isActive") }

    fun <T : BaseModel> isInactive(): Specification<T> =
        SpecTool.where { isFalse("isActive") }

    fun <T : BaseModel> createdBetween(start: LocalDateTime?, end: LocalDateTime?): Specification<T> =
        SpecTool.where { between("createdAt", start, end) }

    fun <T : BaseModel> updatedBetween(start: LocalDateTime?, end: LocalDateTime?): Specification<T> =
        SpecTool.where { between("updatedAt", start, end) }

    fun <T : BaseModel> hasId(id: Long?): Specification<T> =
        SpecTool.where { equal("id", id) }
}
```

---

## Security Files

### 13. JwtService.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/security/JwtService.kt`

**Purpose:** JWT token generation and validation.

```kotlin
package com.wanim_ms.wanimlibrary.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

class JwtService(
    private val secretKey: String,
    private val expirationMs: Long = 86400000 // 24 hours
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

    fun generateToken(subject: String, claims: Map<String, Any> = emptyMap()): String {
        return Jwts.builder()
            .subject(subject)
            .claims(claims)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseToken(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getSubject(token: String): String = parseToken(token).subject

    fun getClaim(token: String, key: String): Any? = parseToken(token)[key]

    fun <T> getClaim(token: String, key: String, type: Class<T>): T? =
        parseToken(token).get(key, type)

    fun isExpired(token: String): Boolean {
        return parseToken(token).expiration.before(Date())
    }

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
```

**Key Points:**
- No `@Service` annotation - users create bean themselves
- Configurable via constructor
- Uses JJWT library for JWT operations

---

## Utility Files

### 14. PageSerializers.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/util/json/PageSerializers.kt`

**Purpose:** Jackson serializers for Spring Data Page objects.

```kotlin
package com.wanim_ms.wanimlibrary.util.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class PageSerializer : JsonSerializer<Page<*>>() {
    override fun serialize(page: Page<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField("content", page.content)
        gen.writeNumberField("totalElements", page.totalElements)
        gen.writeNumberField("totalPages", page.totalPages)
        gen.writeNumberField("number", page.number)
        gen.writeNumberField("size", page.size)
        gen.writeBooleanField("first", page.isFirst)
        gen.writeBooleanField("last", page.isLast)
        gen.writeBooleanField("empty", page.isEmpty)
        gen.writeEndObject()
    }
}

class PageDeserializer<T>(private val contentType: Class<T>) : JsonDeserializer<Page<T>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Page<T> {
        val mapper = p.codec as ObjectMapper
        val node = mapper.readTree<ObjectNode>(p)
        
        val content = node.get("content").map { mapper.treeToValue(it, contentType) }
        val totalElements = node.get("totalElements").asLong()
        val number = node.get("number").asInt()
        val size = node.get("size").asInt()
        
        return PageImpl(content, PageRequest.of(number, size), totalElements)
    }
}
```

---

### 15. EnumTool.kt

**Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/util/enums/EnumTool.kt`

**Purpose:** Utility functions for working with enums.

```kotlin
package com.wanim_ms.wanimlibrary.util.enums

object EnumTool {

    inline fun <reified T : Enum<T>> fromName(name: String?): T? {
        return name?.let {
            enumValues<T>().firstOrNull { e -> e.name.equals(it, ignoreCase = true) }
        }
    }

    inline fun <reified T : Enum<T>> fromOrdinal(ordinal: Int?): T? {
        return ordinal?.let {
            enumValues<T>().getOrNull(it)
        }
    }

    inline fun <reified T : Enum<T>> values(): List<T> = enumValues<T>().toList()

    inline fun <reified T : Enum<T>> names(): List<String> = enumValues<T>().map { it.name }

    inline fun <reified T : Enum<T>> safeValueOf(name: String, default: T): T {
        return fromName(name) ?: default
    }
}
```

---

## Summary

| File | Type | Lines | Purpose |
|------|------|-------|---------|
| EnableWanimLibrary.kt | Annotation | ~15 | Entry point |
| WanimLibraryConfig.kt | Configuration | ~25 | Auto-config |
| AppContextUtil.kt | Utility | ~40 | Context access |
| BaseModel.kt | Entity | ~50 | Base entity |
| ParameterModel.kt | Entity | ~20 | Parameter base |
| BaseJpaRepository.kt | Interface | ~20 | Enhanced repo |
| JpaProjectionExecutor.kt | Interface | ~20 | Projections |
| BaseServiceHandler.kt | Interface | ~25 | Service contract |
| AbstractServiceHandler.kt | Abstract | ~60 | Default impl |
| PaginationSpec.kt | Utility | ~20 | Pagination |
| SpecTool.kt | Utility | ~70 | Spec builder |
| BaseModelJpaSpec.kt | Utility | ~25 | Common specs |
| JwtService.kt | Service | ~55 | JWT handling |
| PageSerializers.kt | Serializer | ~45 | JSON support |
| EnumTool.kt | Utility | ~25 | Enum helpers |
