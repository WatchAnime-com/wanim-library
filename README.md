# Wanim Library

Spring Boot microservice'ler için JPA tabanlı kütüphane. BaseModel, Repository, Specification ve Service katmanları için hazır altyapı sağlar.

## 📦 Kurulum

### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven {
        url = uri("https://repo.cr-i.tr/repository/wanim-library/")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation("com.wanim_ms:wanim-library:1.0.0")
}
```

### Gradle (Groovy)
```groovy
repositories {
    maven {
        url 'https://repo.cr-i.tr/repository/wanim-library/'
        allowInsecureProtocol true
    }
}

dependencies {
    implementation 'com.wanim_ms:wanim-library:1.0.0'
}
```

## 🚀 Hızlı Başlangıç

### 1. Kütüphaneyi Etkinleştir

```kotlin
@SpringBootApplication
@EnableWanimLibrary
class MyApplication

fun main(args: Array<String>) {
    runApplication<MyApplication>(*args)
}
```

### 2. Entity Oluştur

```kotlin
@Entity
@Table(name = "users")
class User(
    var name: String,
    var email: String,
    var age: Int? = null
) : BaseModel<Long>()
```

### 3. Repository Oluştur

```kotlin
interface UserRepository : BaseJpaRepository<User, Long>
```

### 4. Specification Oluştur

```kotlin
class UserSpec(params: UserSearchParams) : BaseModelJpaSpec<User, Long>(params) {
    
    override var deleted: Boolean? = false
    override var archived: Boolean? = null
    override var id: Long? = params.id
    
    override fun ofSearch(): Specification<User> {
        return Specification { root, query, builder ->
            var predicate = builder.conjunction()
            
            params.search?.let { search ->
                predicate = searchPredicate(predicate, builder, root, search, "name", "email")
            }
            
            params.minAge?.let { minAge ->
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("age"), minAge))
            }
            
            predicate
        }
    }
}

class UserSearchParams : ParameterModel() {
    var id: Long? = null
    var minAge: Int? = null
}
```

### 5. Service Oluştur

```kotlin
@Service
class UserService(
    repository: UserRepository
) : AbstractServiceHandler<User, CreateUserDto, UpdateUserDto, UserResponse, UserSpec, Long>(
    repository, 
    User::class.java
) {
    override fun toEntity(dto: CreateUserDto) = User(
        name = dto.name,
        email = dto.email
    )
    
    override fun applyUpdate(entity: User, dto: UpdateUserDto): User {
        dto.name?.let { entity.name = it }
        dto.email?.let { entity.email = it }
        return entity
    }
    
    override fun toResponse(entity: User) = UserResponse(
        id = entity.getId()!!,
        name = entity.name,
        email = entity.email
    )
}
```

## 📁 Proje Yapısı

```
com.wanim_ms.wanimlibrary/
├── core/                           # Temel yapılar
│   ├── model/
│   │   ├── BaseModel.kt           # Tüm entity'ler için base class
│   │   └── ParameterModel.kt      # Search/filter parametreleri için base
│   ├── repository/
│   │   ├── BaseJpaRepository.kt   # Repository interface
│   │   └── JpaProjectionExecutor.kt # Projection desteği
│   ├── service/
│   │   ├── BaseServiceHandler.kt  # Service interface
│   │   └── AbstractServiceHandler.kt # Default implementasyonlar
│   ├── spec/
│   │   ├── PaginationSpec.kt      # Sayfalama
│   │   ├── SpecTool.kt            # Specification araçları
│   │   └── BaseModelJpaSpec.kt    # Base specification
│   └── context/
│       └── AppContextUtil.kt      # Spring context erişimi
├── config/
│   └── WanimLibraryConfig.kt      # Auto-configuration
├── security/
│   └── JwtService.kt              # JWT token yönetimi
└── util/
    ├── json/
    │   └── PageSerializers.kt     # Page JSON serialization
    └── enums/
        └── EnumTool.kt            # Enum utilities
```

## 🔧 Özellikler

### BaseModel
Tüm entity'ler için ortak alanlar:
- `id` - Primary key
- `sk` - UUID (unique)
- `pk` - Numeric unique key
- `deleted` - Soft delete flag
- `archived` - Archive flag
- `createdAt` - Oluşturma tarihi
- `updatedAt` - Güncelleme tarihi

### BaseJpaRepository
- `JpaRepository` + `JpaSpecificationExecutor` + `JpaProjectionExecutor`
- `findOne(spec, clazz)` - Tek kayıt bulma
- `findAll(spec, clazz)` - Sayfalı liste
- `exists(spec, clazz)` - Varlık kontrolü

### AbstractServiceHandler
Hazır CRUD operasyonları:
- `create(dto)` - Oluşturma
- `update(entity, dto)` - Güncelleme
- `find(spec)` - Tek kayıt
- `findAll(spec)` - Sayfalı liste
- `findById(id)` - ID ile bulma
- `delete(id)` - Soft delete
- `restore(id)` - Geri yükleme
- `archive(id)` / `unArchive(id)` - Arşivleme

### SpecTool
Specification oluşturma araçları:
- `searchPredicate()` - Text arama
- `typePredicate()` - Type discrimination
- `defaultPredicates()` - Standart filtreler

### JwtService
JWT token yönetimi:
```kotlin
val jwtService = JwtService(secretKey = "your-256-bit-secret")

// Token oluştur
val token = jwtService.generateToken("user123", mapOf("role" to "ADMIN"))

// Token doğrula
if (jwtService.isValidToken(token)) {
    val subject = jwtService.extractSubject(token)
}
```

## 📋 Gereksinimler

- Java 21+
- Spring Boot 3.4+
- Kotlin 1.9+

## 🔄 Versiyon Yönetimi

Publish ederken versiyon belirtin:
```bash
./gradlew publish -Pversion=1.2.3
```

## 📄 Lisans

MIT License



