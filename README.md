# wanim-library

A lightweight Kotlin utility library for modular Spring Boot backends.  
Includes common DTOs, helper functions, and backend abstractions to accelerate development across microservices.

---

## 📦 Features

- Shared models and DTOs
- Utility classes and functions
- Consistent backend architecture support
- Designed for modular Kotlin Spring Boot applications

---

## 📥 Installation

### Gradle (Kotlin DSL)

```kotlin
val baseLibLatestVersion = "0.0.1"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.cr-i.tr/repository/wanim-library/")
    }
}

dependencies {
    implementation("com.wanim_ms:wanim-library:$baseLibLatestVersion")
}
````

---

### Gradle (Groovy)

```groovy
ext {
    baseLibLatestVersion = '0.0.1'
}

repositories {
    mavenCentral()
    maven {
        url 'https://repo.cr-i.tr/repository/wanim-library/'
    }
}

dependencies {
    implementation "com.wanim_ms:wanim-library:$baseLibLatestVersion"
}
```

---

### Maven

```xml
<repositories>
    <repository>
        <id>wanim-library</id>
        <url>https://repo.cr-i.tr/repository/wanim-library/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.wanim_ms</groupId>
        <artifactId>wanim-library</artifactId>
        <version>0.0.1</version>
    </dependency>
</dependencies>
```

---

## 🔗 Repository

This library is hosted on a private Nexus repository:

```
https://repo.cr-i.tr/repository/wanim-library/
```

Make sure your project has access to the above repository to resolve dependencies.

---

## 👨‍💻 Author

**L0rdL0ther**
Creator of [SmartHome](https://github.com/L0rdL0ther/WSHome) ecosystem



