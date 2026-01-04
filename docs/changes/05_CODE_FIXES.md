# Code Fixes Documentation

## Overview

This document details specific code-level fixes and improvements made during the refactoring process.

---

## Fix 1: HTTP Version Fetching

### Problem
```kotlin
// BEFORE - In build.gradle.kts
val versionUrl = "http://192.168.1.150:8888/api/common/version/wanim-library"
val versionResponse = uri(versionUrl).toURL().readText()
val projectVersion = Gson().fromJson(versionResponse, VersionResponse::class.java).version
version = projectVersion
```

### Issues
- Build fails if server is down
- Network dependency for local builds
- HTTP (not HTTPS) security risk
- Gson dependency just for version
- Breaks offline development
- Slow builds due to network latency

### Solution
```kotlin
// AFTER
version = "1.0.0"
```

### Recommendation
For dynamic versioning, use CI/CD environment variables or git tags:
```kotlin
version = System.getenv("VERSION") ?: "1.0.0-SNAPSHOT"
```

---

## Fix 2: Spring Boot Plugin Removal

### Problem
```kotlin
// BEFORE
plugins {
    id("org.springframework.boot") version "3.4.1"
}
```

### Issues
- Creates fat JAR with all dependencies
- Spring Boot dependency management conflicts
- Not designed for libraries
- Increases artifact size

### Solution
```kotlin
// AFTER
plugins {
    `java-library`  // Designed for libraries
}
```

---

## Fix 3: ApplicationReadyEvent → ContextRefreshedEvent

### Problem
```kotlin
// BEFORE - In WanimServiceConfig.kt
import org.springframework.boot.context.event.ApplicationReadyEvent

@Bean
fun appContextInitializer(): ApplicationListener<ApplicationReadyEvent> {
    return ApplicationListener { event ->
        AppContextUtil.setApplicationContext(event.applicationContext)
    }
}
```

### Issue
- `ApplicationReadyEvent` is from `spring-boot` module
- Library shouldn't depend on Spring Boot classes
- Consumers using plain Spring would get `ClassNotFoundException`

### Solution
```kotlin
// AFTER - In WanimLibraryConfig.kt
import org.springframework.context.event.ContextRefreshedEvent  // Spring Core

@Bean
fun appContextInitializer(): ApplicationListener<ContextRefreshedEvent> {
    return ApplicationListener { event ->
        AppContextUtil.setApplicationContext(event.applicationContext)
    }
}
```

### Explanation
- `ContextRefreshedEvent` is from `spring-context` (Spring Core)
- Available in all Spring applications
- Fires when ApplicationContext is initialized or refreshed

---

## Fix 4: Generic Type Constraint

### Problem
```kotlin
// BEFORE
abstract class AbstractServiceHandler<T : BaseModel, ID>(
    protected val repository: BaseJpaRepository<T, ID>
) : BaseServiceHandler<T, ID>
```

### Issue
Kotlin compiler error:
```
Type argument is not within its bounds.
Expected: Any
Found: ID
```

### Solution
```kotlin
// AFTER
abstract class AbstractServiceHandler<T : BaseModel, ID : Any>(
    protected val repository: BaseJpaRepository<T, ID>
) : BaseServiceHandler<T, ID>
```

### Explanation
- `ID` must extend `Any` because Java generics require non-nullable types
- `JpaRepository` defines `ID` as bounded by `Any`
- Without constraint, Kotlin allows `ID` to be nullable (`ID?`)

---

## Fix 5: Missing @NoRepositoryBean

### Problem
```kotlin
// BEFORE
interface BaseJpaRepo<T : BaseModel, ID> : JpaRepository<T, ID>
```

### Issue
- Spring tries to create a bean for the interface
- Causes "Not a managed type" errors
- Multiple bean definition conflicts

### Solution
```kotlin
// AFTER
@NoRepositoryBean
interface BaseJpaRepository<T : BaseModel, ID : Any> : JpaRepository<T, ID>
```

### Explanation
- `@NoRepositoryBean` tells Spring Data not to create a repository instance
- Only concrete interfaces extending this will get implementations

---

## Fix 6: equals() and hashCode() Implementation

### Problem
```kotlin
// BEFORE - Potentially auto-generated or missing
data class BaseModel(var id: Long?)  // data class generates based on all properties
```

### Issue
- JPA entities should NOT be data classes
- `equals()`/`hashCode()` based on mutable properties causes issues
- Collection behavior breaks when entity is modified

### Solution
```kotlin
// AFTER
@MappedSuperclass
abstract class BaseModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,
    // ... other properties
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as BaseModel
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return this::class.hashCode()  // Constant based on class
    }
}
```

### Explanation
- `equals()` compares only by `id` (and only if not null)
- `hashCode()` returns constant for class type
- This follows JPA best practices from Vlad Mihalcea

---

## Fix 7: JwtService Without @Service

### Problem
```kotlin
// BEFORE
@Service
class JwtService {
    @Value("\${jwt.secret}")
    private lateinit var secretKey: String
}
```

### Issues
- Library auto-registers bean without consumer control
- Requires property `jwt.secret` to exist
- Fails at startup if property missing
- Consumer can't customize configuration

### Solution
```kotlin
// AFTER
class JwtService(
    private val secretKey: String,
    private val expirationMs: Long = 86400000
) {
    // No @Service annotation
    // No @Value injection
}
```

### Usage
Consumers create the bean themselves:
```kotlin
@Configuration
class SecurityConfig {
    @Bean
    fun jwtService(@Value("\${jwt.secret}") secret: String) = JwtService(secret)
}
```

### Explanation
- Library classes should not auto-register
- Configuration should be explicit
- Consumers have full control

---

## Fix 8: Dependency Scope Corrections

### Problem
```kotlin
// BEFORE - All dependencies as implementation
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    // ...
}
```

### Issues
- `implementation` hides transitive dependencies
- Consumers can't see classes they need
- Causes "class not found" at runtime

### Solution
```kotlin
// AFTER
dependencies {
    // API - classes used in public interfaces
    api("org.springframework.data:spring-data-jpa:${Version.springData}")
    api("com.fasterxml.jackson.core:jackson-databind:${Version.jackson}")
    
    // Implementation - internal only
    implementation("io.jsonwebtoken:jjwt-impl:${Version.jwt}")
    
    // CompileOnly - provided by consumer
    compileOnly("org.springframework:spring-context:${Version.spring}")
}
```

### Dependency Types Explained

| Type | Transitive | Compile | Runtime | Use Case |
|------|------------|---------|---------|----------|
| `api` | Yes | Yes | Yes | Public API types |
| `implementation` | No | Yes | Yes | Internal types |
| `compileOnly` | No | Yes | No | Provided by consumer |

---

## Fix 9: Soft Delete Implementation

### Problem
```kotlin
// BEFORE - In interface with no implementation
interface BaseServiceHandler {
    fun softDelete(id: ID): Boolean  // Abstract method
}
```

### Issue
- Every service had to implement soft delete
- Duplicate code across services
- Inconsistent implementations

### Solution
```kotlin
// AFTER - In AbstractServiceHandler
@Suppress("UNCHECKED_CAST")
override fun softDelete(id: ID): Boolean {
    val entity = repository.findById(id).orElse(null) ?: return false
    entity.isActive = false
    repository.save(entity)
    return true
}

override fun restore(id: ID): Boolean {
    val entity = repository.findById(id).orElse(null) ?: return false
    entity.isActive = true
    repository.save(entity)
    return true
}
```

### Explanation
- Default implementation provided
- Consistent behavior across all services
- Can be overridden if needed

---

## Fix 10: Specification Builder Type Safety

### Problem
```kotlin
// BEFORE - Raw string paths, no type safety
fun <T> isActive(): Specification<T> = Specification { root, _, cb ->
    cb.isTrue(root.get("isActive"))
}
```

### Issue
- String field names can have typos
- No compile-time checking
- Refactoring doesn't update strings

### Solution
```kotlin
// AFTER - Context wrapper with helper methods
object SpecTool {
    class SpecContext<T>(val root: Root<T>, val cb: CriteriaBuilder) {
        fun isTrue(field: String): Predicate = cb.isTrue(root.get(field))
        fun equal(field: String, value: Any?): Predicate? = 
            value?.let { cb.equal(root.get<Any>(field), it) }
        // ... more helpers
    }

    fun <T> where(block: SpecContext<T>.() -> Predicate?): Specification<T> {
        return Specification { root, query, cb ->
            SpecContext(root, cb).block()
        }
    }
}

// Usage
val spec = SpecTool.where<User> { 
    and(
        equal("name", searchName),
        isTrue("isActive")
    )
}
```

### Explanation
- DSL-style specification building
- Null-safe helpers
- Cleaner syntax
- Still type-safe at Specification level

---

## Summary Table

| # | Issue | Impact | Solution |
|---|-------|--------|----------|
| 1 | HTTP version fetch | Build failure | Static version |
| 2 | Spring Boot plugin | Fat JAR issues | java-library plugin |
| 3 | ApplicationReadyEvent | ClassNotFound | ContextRefreshedEvent |
| 4 | Generic ID unbounded | Compile error | ID : Any constraint |
| 5 | Missing @NoRepositoryBean | Multiple beans | Added annotation |
| 6 | Bad equals/hashCode | Collection bugs | JPA-compliant impl |
| 7 | @Service on JwtService | Config issues | Constructor injection |
| 8 | Wrong dependency scopes | ClassNotFound | api/impl/compileOnly |
| 9 | No soft delete impl | Code duplication | Default implementation |
| 10 | Raw specification strings | No type safety | SpecContext DSL |

---

## Testing Checklist

After applying these fixes, verify:

- [ ] Project builds with `./gradlew build`
- [ ] JAR is not a fat JAR (< 1MB typically)
- [ ] No Spring Boot classes in runtime classpath
- [ ] Consumer application starts correctly
- [ ] Entities work in collections (Set, Map)
- [ ] Soft delete functions work
- [ ] JWT service can be configured
- [ ] Specifications compile and execute
