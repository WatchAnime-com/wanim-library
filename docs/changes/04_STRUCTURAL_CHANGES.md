# Structural Changes Documentation

## Overview

The entire project structure was reorganized to follow clean architecture principles and standard library conventions.

---

## Before vs After

### Before (Flat Structure)
```
com.wanim_ms.wanimlibrary/
├── AppContextUtil.kt
├── EnableWanimService.kt
├── JwtService.kt
├── LJPAProjection.kt
├── PageImplDeserializer.kt
├── SystemMetrics.kt
├── WanimServiceConfig.kt
├── cache/
│   └── (Redis cache files)
├── constant/
│   └── (Empty)
├── enums/
│   └── EnumTool.kt
├── model/
│   ├── BaseModel.kt
│   └── ParameterModel.kt
├── repo/
│   └── BaseJpaRepo.kt
├── service/
│   └── BaseServiceHandler.kt
└── spec/
    ├── BaseModelJpaSpec.kt
    ├── PaginationSpec.kt
    └── SpecTool.kt
```

### After (Organized Structure)
```
com.wanim_ms.wanimlibrary/
├── EnableWanimLibrary.kt
├── config/
│   └── WanimLibraryConfig.kt
├── core/
│   ├── context/
│   │   └── AppContextUtil.kt
│   ├── model/
│   │   ├── BaseModel.kt
│   │   └── ParameterModel.kt
│   ├── repository/
│   │   ├── BaseJpaRepository.kt
│   │   └── JpaProjectionExecutor.kt
│   ├── service/
│   │   ├── BaseServiceHandler.kt
│   │   └── AbstractServiceHandler.kt
│   └── spec/
│       ├── BaseModelJpaSpec.kt
│       ├── PaginationSpec.kt
│       └── SpecTool.kt
├── security/
│   └── JwtService.kt
└── util/
    ├── enums/
    │   └── EnumTool.kt
    └── json/
        └── PageSerializers.kt
```

---

## Package Organization Philosophy

### Root Package
Contains only the main entry point annotation:
- `EnableWanimLibrary.kt` - The single annotation users need to know

### config/
Contains Spring configuration classes:
- `WanimLibraryConfig.kt` - Auto-configuration

**Why separate:** Configuration classes have Spring-specific concerns and should be isolated.

### core/
Contains the fundamental library functionality:
- `context/` - Application context utilities
- `model/` - Base JPA entities
- `repository/` - Repository interfaces
- `service/` - Service layer abstractions
- `spec/` - JPA Specification utilities

**Why separate:** Core functionality that every consumer will use. Clear hierarchy makes imports predictable.

### security/
Contains security-related utilities:
- `JwtService.kt` - JWT token handling

**Why separate:** Security is a cross-cutting concern. Users who don't need JWT can ignore this package.

### util/
Contains general utilities:
- `enums/` - Enum utilities
- `json/` - JSON serialization utilities

**Why separate:** Utilities that aren't specific to JPA but support the library.

---

## Import Path Changes

### Models
```kotlin
// Before
import com.wanim_ms.wanimlibrary.model.BaseModel

// After
import com.wanim_ms.wanimlibrary.core.model.BaseModel
```

### Repositories
```kotlin
// Before
import com.wanim_ms.wanimlibrary.repo.BaseJpaRepo

// After
import com.wanim_ms.wanimlibrary.core.repository.BaseJpaRepository
```

### Services
```kotlin
// Before
import com.wanim_ms.wanimlibrary.service.BaseServiceHandler

// After
import com.wanim_ms.wanimlibrary.core.service.BaseServiceHandler
import com.wanim_ms.wanimlibrary.core.service.AbstractServiceHandler
```

### Specifications
```kotlin
// Before
import com.wanim_ms.wanimlibrary.spec.SpecTool

// After
import com.wanim_ms.wanimlibrary.core.spec.SpecTool
```

### Utilities
```kotlin
// Before
import com.wanim_ms.wanimlibrary.enums.EnumTool

// After
import com.wanim_ms.wanimlibrary.util.enums.EnumTool
```

### JWT
```kotlin
// Before
import com.wanim_ms.wanimlibrary.JwtService

// After
import com.wanim_ms.wanimlibrary.security.JwtService
```

### Enable Annotation
```kotlin
// Before
import com.wanim_ms.wanimlibrary.EnableWanimService

// After
import com.wanim_ms.wanimlibrary.EnableWanimLibrary
```

---

## Naming Convention Changes

| Before | After | Reason |
|--------|-------|--------|
| `EnableWanimService` | `EnableWanimLibrary` | More accurate - it's a library |
| `BaseJpaRepo` | `BaseJpaRepository` | Standard Spring naming |
| `WanimServiceConfig` | `WanimLibraryConfig` | Consistency with annotation |
| `LJPAProjection` | `JpaProjectionExecutor` | Clearer purpose |
| `PageImplDeserializer` | `PageSerializers` | Includes both ser/deser |

---

## Removed Packages

### cache/
**Reason:** Redis caching is not JPA functionality. It:
- Adds dependency on Redis
- Mixes concerns (caching vs persistence)
- Should be in a separate library
- Consumers typically have their own caching strategy

### constant/
**Reason:** Empty package with no content.

---

## Package Visibility

```
Public API (what consumers should use):
├── EnableWanimLibrary           ✓ Main entry point
├── core/model/*                 ✓ Extend these
├── core/repository/*            ✓ Extend these
├── core/service/*               ✓ Extend these
├── core/spec/*                  ✓ Use these
└── security/JwtService          ✓ Create bean if needed

Internal API (implementation details):
├── config/*                     ⚠ Auto-configured
├── core/context/*               ⚠ Usually internal
└── util/*                       ⚠ Helper utilities
```

---

## Directory Structure Rationale

### Why `core/`?

The `core/` package groups the fundamental building blocks:
1. **Predictable imports** - All essential classes under one namespace
2. **Clear hierarchy** - Subpackages follow DDD layers
3. **Easy discovery** - New users find everything in one place

### Why not flat structure?

Flat structure problems:
1. No logical grouping
2. Import statements are long
3. Hard to find related classes
4. Doesn't scale as library grows

### Why separate `util/`?

Utilities are:
1. Not core JPA functionality
2. Optional for consumers
3. General-purpose helpers
4. May be moved to separate module later

---

## Build Output Changes

### Before
```
build/classes/kotlin/main/com/wanim_ms/wanimlibrary/
├── AppContextUtil.class
├── cache/*.class
├── constant/*.class
├── enums/*.class
├── model/*.class
├── repo/*.class
├── service/*.class
└── spec/*.class
```

### After
```
build/classes/kotlin/main/com/wanim_ms/wanimlibrary/
├── EnableWanimLibrary.class
├── config/*.class
├── core/
│   ├── context/*.class
│   ├── model/*.class
│   ├── repository/*.class
│   ├── service/*.class
│   └── spec/*.class
├── security/*.class
└── util/
    ├── enums/*.class
    └── json/*.class
```

---

## Migration Guide for Existing Users

If you're upgrading from the old structure:

### 1. Update imports
Use IDE's "Organize Imports" feature or find/replace:

```kotlin
// Find → Replace
"com.wanim_ms.wanimlibrary.model" → "com.wanim_ms.wanimlibrary.core.model"
"com.wanim_ms.wanimlibrary.repo" → "com.wanim_ms.wanimlibrary.core.repository"
"com.wanim_ms.wanimlibrary.service" → "com.wanim_ms.wanimlibrary.core.service"
"com.wanim_ms.wanimlibrary.spec" → "com.wanim_ms.wanimlibrary.core.spec"
"com.wanim_ms.wanimlibrary.enums" → "com.wanim_ms.wanimlibrary.util.enums"
"BaseJpaRepo" → "BaseJpaRepository"
"EnableWanimService" → "EnableWanimLibrary"
```

### 2. Update annotations
```kotlin
// Before
@EnableWanimService

// After
@EnableWanimLibrary
```

### 3. Remove cache dependencies
If you were using the cache package, you'll need to implement your own caching or use a different library.

### 4. Update service classes
```kotlin
// Before (may have used interface directly)
class MyService : BaseServiceHandler<MyEntity, Long> {
    // Had to implement all methods
}

// After (extend abstract class)
class MyService(repo: MyRepository) : AbstractServiceHandler<MyEntity, Long>(repo) {
    // All methods provided by default
}
```
