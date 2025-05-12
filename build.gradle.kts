import org.w3c.dom.Element
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory



plugins {
    id("maven-publish")
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.wanim_ms"

val sharedLibLatestVersion = getLatestVersion()
val newVersion = incrementVersion(sharedLibLatestVersion)
version = newVersion

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2024.0.0"


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.1")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("com.bucket4j:bucket4j-core:8.10.1")


    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.1")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.bouncycastle:bcprov-jdk18on:1.79")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.getByName("kotlinSourcesJar"))
        }
    }
    repositories {
        maven {
            url = uri("https://repo.cr-i.tr/repository/wanim-library/")

            isAllowInsecureProtocol = true // Güvenli olmayan protokole izin verir
            // Kullanıcı adı ve şifreyi çevresel değişkenlerden alıyoruz
            credentials {
                username = System.getenv("REPO_USERNAME") as String
                password = System.getenv("REPO_PASSWORD") as String
            }

        }
    }
}

fun getLatestVersion(): String {
    val client = HttpClient.newHttpClient()
    val url = "https://repo.cr-i.tr/repository/wanim-library/com/wanim_ms/wanim-library/maven-metadata.xml"
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() == 200) {
        val xmlContent = response.body()
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlContent.byteInputStream())
        val latestVersion = getLatestVersionFromXml(document)
        return latestVersion
    } else {
        println("Request failed with status: ${response.statusCode()}")
        return "Error"
    }
}

fun getLatestVersionFromXml(document: org.w3c.dom.Document): String {
    val metadataElement = document.getElementsByTagName("metadata").item(0) as Element
    val versioningElement = metadataElement.getElementsByTagName("versioning").item(0) as Element
    val latestElement = versioningElement.getElementsByTagName("latest").item(0) as Element
    return latestElement.textContent
}

fun incrementVersion(version: String): String {
    val parts = version.split(".")
    if (parts.size == 3) {
        val major = parts[0].toInt()
        val minor = parts[1].toInt()
        val patch = parts[2].toInt()
        val newPatch = patch + 1
        return "$major.$minor.$newPatch"
    } else {
        throw IllegalArgumentException("Invalid version format")
    }
}