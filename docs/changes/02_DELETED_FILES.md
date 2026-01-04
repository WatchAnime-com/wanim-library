# Deleted Files Documentation

## Overview

14 files were deleted during the refactoring process. Each deletion was intentional and served to either:
1. Move content to a better-organized location
2. Remove functionality that doesn't belong in a JPA library
3. Consolidate duplicate code

---

## Files Moved to New Locations

### 1. AppContextUtil.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/AppContextUtil.kt`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/context/AppContextUtil.kt`

**Reason:** Moved to `core/context/` package for better organization. Context utilities are core functionality.

---

### 2. EnableWanimService.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/EnableWanimService.kt`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/EnableWanimLibrary.kt`

**Changes:**
- Renamed from `EnableWanimService` to `EnableWanimLibrary` (more accurate name)
- Updated import references
- Simplified annotation configuration

**Reason:** Name better reflects purpose - enabling a library, not a service.

---

### 3. JwtService.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/JwtService.kt`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/security/JwtService.kt`

**Changes:**
- Moved to dedicated `security/` package
- Removed `@Service` annotation (library classes shouldn't auto-register)
- Made configurable via constructor

**Reason:** Security-related code belongs in security package. Users should explicitly create the bean.

---

### 4. LJPAProjection.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/LJPAProjection.kt`  
**New Location:** Split into `src/main/kotlin/com/wanim_ms/wanimlibrary/core/repository/JpaProjectionExecutor.kt`

**Changes:**
- Renamed to `JpaProjectionExecutor`
- Moved to `core/repository/` package
- Cleaned up interface definition

**Reason:** Projection execution is repository functionality.

---

### 5. PageImplDeserializer.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/PageImplDeserializer.kt`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/util/json/PageSerializers.kt`

**Changes:**
- Renamed to `PageSerializers`
- Added both serializer and deserializer
- Moved to `util/json/` package

**Reason:** JSON utilities belong in util package. Grouping serializers together.

---

### 6. WanimServiceConfig.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/WanimServiceConfig.kt`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/config/WanimLibraryConfig.kt`

**Changes:**
- Renamed to `WanimLibraryConfig`
- Moved to `config/` package
- Changed from `ApplicationReadyEvent` to `ContextRefreshedEvent` (Spring Core event)
- Removed Spring Boot dependencies

**Reason:** Configuration classes belong in config package. Spring Boot events not available in library.

---

## Files Completely Removed

### 7. SystemMetrics.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/SystemMetrics.kt`

**Content (removed):**
```kotlin
@Component
class SystemMetrics {
    fun getCpuUsage(): Double { ... }
    fun getMemoryUsage(): Double { ... }
    fun getDiskUsage(): Double { ... }
}
```

**Reason for Removal:**
- System monitoring has nothing to do with JPA
- Violates single responsibility principle
- Should be in a separate monitoring library
- Consumers likely have their own monitoring (Actuator, Micrometer, etc.)

---

## Folders Completely Removed

### 8. cache/ folder

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/cache/`

**Contents (removed):**
- `CacheConfig.kt`
- `CacheService.kt`
- Redis-related utilities

**Reason for Removal:**
- Redis caching is not JPA functionality
- Should be in a separate caching library
- Adds unnecessary dependency on Redis
- Consumers have their own caching strategies

---

### 9. constant/ folder

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/constant/`

**Reason for Removal:**
- Folder was empty or contained unused constants
- No actual functionality

---

## Model Files Moved

### 10. model/BaseModel.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/model/BaseModel.kt`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/model/BaseModel.kt`

**Changes:**
- Moved to `core/model/` package
- Improved `equals()` and `hashCode()` implementation
- Added comprehensive KDoc documentation
- Made `ID` generic type properly bounded

---

### 11. model/ParameterModel.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/model/ParameterModel.kt`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/model/ParameterModel.kt`

**Changes:**
- Moved to `core/model/` package
- Updated to extend new `BaseModel`

---

## Repository Files Moved

### 12. repo/BaseJpaRepo.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/repo/BaseJpaRepo.kt`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/repository/BaseJpaRepository.kt`

**Changes:**
- Renamed from `BaseJpaRepo` to `BaseJpaRepository` (standard naming)
- Moved to `core/repository/` package
- Added `@NoRepositoryBean` annotation
- Enhanced with additional utility methods

---

## Service Files Moved

### 13. service/BaseServiceHandler.kt

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/service/BaseServiceHandler.kt`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/service/BaseServiceHandler.kt`

**Changes:**
- Moved to `core/service/` package
- Converted to pure interface (removed default implementations)
- Default implementations moved to new `AbstractServiceHandler`

---

## Spec Files Moved

### 14. spec/ folder contents

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/spec/`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/core/spec/`

**Files:**
- `PaginationSpec.kt` → `core/spec/PaginationSpec.kt`
- `BaseModelJpaSpec.kt` → `core/spec/BaseModelJpaSpec.kt`
- `SpecTool.kt` → `core/spec/SpecTool.kt` (consolidated utilities)

---

## Enum Files Moved

### 15. enums/ folder

**Old Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/enums/`  
**New Location:** `src/main/kotlin/com/wanim_ms/wanimlibrary/util/enums/`

**Files:**
- `EnumTool.kt` → `util/enums/EnumTool.kt`

**Reason:** Enum utilities are general utilities, belong in `util/` package.

---

## Summary Table

| File | Action | Reason |
|------|--------|--------|
| AppContextUtil.kt | Moved | Better organization in `core/context/` |
| EnableWanimService.kt | Renamed + Moved | More accurate name |
| JwtService.kt | Moved | Security package |
| LJPAProjection.kt | Renamed + Moved | Repository functionality |
| PageImplDeserializer.kt | Renamed + Moved | JSON utilities |
| WanimServiceConfig.kt | Renamed + Moved | Config package |
| SystemMetrics.kt | **Deleted** | Out of scope for JPA library |
| cache/ folder | **Deleted** | Redis not JPA |
| constant/ folder | **Deleted** | Empty/unused |
| model/ folder | Moved | `core/model/` |
| repo/ folder | Moved | `core/repository/` |
| service/ folder | Moved | `core/service/` |
| spec/ folder | Moved | `core/spec/` |
| enums/ folder | Moved | `util/enums/` |

---

## Build Directory Cleanup

The following generated files will be automatically cleaned on next build:
- `build/classes/kotlin/main/com/wanim_ms/wanimlibrary/cache/`
- `build/classes/kotlin/main/com/wanim_ms/wanimlibrary/constant/`
- `build/classes/kotlin/main/com/wanim_ms/wanimlibrary/enums/`
- `build/classes/kotlin/main/com/wanim_ms/wanimlibrary/model/`
- `build/classes/kotlin/main/com/wanim_ms/wanimlibrary/repo/`
- `build/classes/kotlin/main/com/wanim_ms/wanimlibrary/service/`
- `build/classes/kotlin/main/com/wanim_ms/wanimlibrary/spec/`

Run `./gradlew clean` to remove these artifacts.
