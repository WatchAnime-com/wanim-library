plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
}

group = "com.wanim_ms"
version = project.findProperty("version")?.toString() ?: "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

// ================================
// Version Catalog
// ================================
object Versions {
    const val SPRING_BOOT = "3.4.0"
    const val SPRING_DATA = "3.4.0"
    const val JACKSON = "2.18.2"
    const val JWT = "0.12.6"
    const val SLF4J = "2.0.16"
}

dependencies {
    // ================================
    // API Dependencies (Transitive - kullanıcıya geçer)
    // ================================
    api("org.springframework.data:spring-data-jpa:${Versions.SPRING_DATA}")
    api("org.springframework.data:spring-data-commons:${Versions.SPRING_DATA}")
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")
    api("com.fasterxml.jackson.core:jackson-annotations:${Versions.JACKSON}")
    api("com.fasterxml.jackson.core:jackson-databind:${Versions.JACKSON}")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.JACKSON}")
    
    // ================================
    // Implementation Dependencies (Internal)
    // ================================
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.slf4j:slf4j-api:${Versions.SLF4J}")
    
    // JWT (Opsiyonel modül için)
    implementation("io.jsonwebtoken:jjwt-api:${Versions.JWT}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${Versions.JWT}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${Versions.JWT}")
    
    // Security (Opsiyonel)
    compileOnly("org.springframework.security:spring-security-core:6.4.1")
    compileOnly("org.springframework.security:spring-security-web:6.4.1")
    
    // Redis (Opsiyonel modül için)
    compileOnly("org.springframework.data:spring-data-redis:${Versions.SPRING_DATA}")
    
    // Rate Limiting (Opsiyonel)
    compileOnly("com.bucket4j:bucket4j-core:8.10.1")
    
    // Hibernate (compileOnly - kullanıcı kendi versiyonunu sağlar)
    compileOnly("org.hibernate.orm:hibernate-core:6.6.3.Final")
    
    // ================================
    // Test Dependencies
    // ================================
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.SPRING_BOOT}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.h2database:h2:2.3.232")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xjvm-default=all")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// ================================
// Publishing Configuration
// ================================
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            
            pom {
                name.set("Wanim Library")
                description.set("Spring Boot Microservice JPA Library")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "WanimRepo"
            url = uri("https://repo.cr-i.tr/repository/wanim-library/")
            isAllowInsecureProtocol = true
            
            credentials {
                username = providers.environmentVariable("REPO_USERNAME").orNull
                password = providers.environmentVariable("REPO_PASSWORD").orNull
            }
        }
    }
}

// ================================
// Custom Tasks
// ================================
tasks.register("printVersion") {
    doLast {
        println("Version: ${project.version}")
    }
}