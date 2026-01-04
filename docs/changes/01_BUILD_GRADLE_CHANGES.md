# Build Configuration Changes (build.gradle.kts)

## Overview

The `build.gradle.kts` file was completely rewritten. The original had fundamental issues that made it unsuitable for a reusable library.

---

## Critical Issues Fixed

### 1. HTTP-Based Version Fetching (REMOVED)

**Before (Problematic):**
```kotlin
val versionUrl = "http://192.168.1.150:8888/api/common/version/wanim-library"
val versionResponse = uri(versionUrl).toURL().readText()
val projectVersion = Gson().fromJson(versionResponse, VersionResponse::class.java).version
```

**Problems:**
- Build fails if server is unreachable
- Network latency slows every build
- Security risk (HTTP, not HTTPS)
- Dependency on external service for basic build info
- Breaks offline development

**After (Fixed):**
```kotlin
version = "1.0.0"
```

**Reasoning:** Version should be a static value in the build file. For dynamic versioning, use git tags or CI/CD environment variables.

---

### 2. Spring Boot Plugin (REMOVED)

**Before:**
```kotlin
plugins {
    id("org.springframework.boot") version "3.4.1"
}
```

**Problems:**
- Creates executable JAR with embedded dependencies
- Causes dependency conflicts when library is used
- Increases JAR size unnecessarily
- Not designed for library projects

**After:**
```kotlin
plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.0.21"
}
```

**Reasoning:** The `java-library` plugin is specifically designed for creating reusable libraries. It properly handles `api` vs `implementation` dependencies.

---

### 3. Dependency Types (REORGANIZED)

**Before (All implementation):**
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework:spring-tx")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    // ... everything as implementation
}
```

**Problems:**
- Consumers can't see transitive dependencies they need
- `implementation` hides dependencies from consumers
- Spring Boot starters bring unwanted dependencies
- No clear separation of concerns

**After (Properly categorized):**
```kotlin
object Version {
    const val spring = "6.2.1"
    const val springData = "3.4.0"
    const val jackson = "2.18.2"
    const val jwt = "0.12.6"
    const val jakartaPersistence = "3.2.0"
    const val hibernateCore = "6.6.4.Final"
    const val slf4j = "2.0.16"
}

dependencies {
    // API - Exposed to library consumers (transitive)
    api("org.springframework.data:spring-data-jpa:${Version.springData}")
    api("jakarta.persistence:jakarta.persistence-api:${Version.jakartaPersistence}")
    api("com.fasterxml.jackson.core:jackson-databind:${Version.jackson}")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jackson}")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Version.jackson}")
    
    // Implementation - Internal use only (not transitive)
    implementation("io.jsonwebtoken:jjwt-api:${Version.jwt}")
    implementation("io.jsonwebtoken:jjwt-impl:${Version.jwt}")
    implementation("io.jsonwebtoken:jjwt-jackson:${Version.jwt}")
    
    // CompileOnly - Provided by consumer application
    compileOnly("org.springframework:spring-context:${Version.spring}")
    compileOnly("org.springframework:spring-web:${Version.spring}")
    compileOnly("org.springframework:spring-tx:${Version.spring}")
    compileOnly("org.hibernate.orm:hibernate-core:${Version.hibernateCore}")
    compileOnly("org.slf4j:slf4j-api:${Version.slf4j}")
}
```

**Dependency Type Explanation:**

| Type | Visibility | Use Case |
|------|------------|----------|
| `api` | Transitive | Classes exposed in public API |
| `implementation` | Not transitive | Internal implementation details |
| `compileOnly` | Compile only | Provided by consumer at runtime |

---

### 4. Version Object (NEW)

**Added:**
```kotlin
object Version {
    const val spring = "6.2.1"
    const val springData = "3.4.0"
    const val jackson = "2.18.2"
    const val jwt = "0.12.6"
    const val jakartaPersistence = "3.2.0"
    const val hibernateCore = "6.6.4.Final"
    const val slf4j = "2.0.16"
}
```

**Benefits:**
- Single source of truth for versions
- Easy to update dependencies
- Prevents version mismatches
- Improves readability

---

### 5. Kotlin Version Upgrade

**Before:**
```kotlin
kotlin("jvm") version "1.9.25"
```

**After:**
```kotlin
kotlin("jvm") version "2.0.21"
```

**Reasoning:** Kotlin 2.0.21 has better compatibility with newer Java versions and Gradle 8.x.

---

### 6. Java Toolchain

**Added:**
```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}
```

**Benefits:**
- Consistent Java version across environments
- Automatic source and Javadoc JAR generation
- Better compatibility management

---

### 7. Publishing Configuration

**Before:**
```kotlin
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            // Basic configuration only
        }
    }
}
```

**After:**
```kotlin
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            
            groupId = "com.wanim-ms"
            artifactId = "wanim-library"
            version = project.version.toString()
            
            pom {
                name.set("WANIM Library")
                description.set("JPA utility library for Spring Boot microservices")
                url.set("https://github.com/wanim-ms/wanim-library")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("wanim")
                        name.set("WANIM Team")
                    }
                }
            }
        }
    }
}
```

**Benefits:**
- Complete POM metadata
- Professional Maven Central-ready configuration
- Clear licensing and developer information

---

### 8. Removed Dependencies

| Dependency | Reason for Removal |
|------------|-------------------|
| `spring-boot-starter-data-redis` | Redis caching is out of scope for JPA library |
| `spring-boot-starter-web` | Web features not needed in library |
| `spring-boot-starter-security` | Security is consumer's responsibility |
| `spring-boot-starter-data-jpa` | Replaced with direct `spring-data-jpa` |
| `gson` | Only used for HTTP version fetching |

---

## Complete Final build.gradle.kts

```kotlin
plugins {
    `java-library`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.0.21"
}

group = "com.wanim-ms"
version = "1.0.0"

object Version {
    const val spring = "6.2.1"
    const val springData = "3.4.0"
    const val jackson = "2.18.2"
    const val jwt = "0.12.6"
    const val jakartaPersistence = "3.2.0"
    const val hibernateCore = "6.6.4.Final"
    const val slf4j = "2.0.16"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // API dependencies - exposed to consumers
    api("org.springframework.data:spring-data-jpa:${Version.springData}")
    api("jakarta.persistence:jakarta.persistence-api:${Version.jakartaPersistence}")
    api("com.fasterxml.jackson.core:jackson-databind:${Version.jackson}")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jackson}")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Version.jackson}")
    
    // Implementation dependencies - internal only
    implementation("io.jsonwebtoken:jjwt-api:${Version.jwt}")
    implementation("io.jsonwebtoken:jjwt-impl:${Version.jwt}")
    implementation("io.jsonwebtoken:jjwt-jackson:${Version.jwt}")
    
    // CompileOnly - provided by consumer
    compileOnly("org.springframework:spring-context:${Version.spring}")
    compileOnly("org.springframework:spring-web:${Version.spring}")
    compileOnly("org.springframework:spring-tx:${Version.spring}")
    compileOnly("org.hibernate.orm:hibernate-core:${Version.hibernateCore}")
    compileOnly("org.slf4j:slf4j-api:${Version.slf4j}")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            
            groupId = "com.wanim-ms"
            artifactId = "wanim-library"
            version = project.version.toString()
            
            pom {
                name.set("WANIM Library")
                description.set("JPA utility library for Spring Boot microservices")
                url.set("https://github.com/wanim-ms/wanim-library")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("wanim")
                        name.set("WANIM Team")
                    }
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "local"
            url = uri("${layout.buildDirectory.get()}/publications/maven")
        }
    }
}
```
