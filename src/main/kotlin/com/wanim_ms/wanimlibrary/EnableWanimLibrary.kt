package com.wanim_ms.wanimlibrary

import com.wanim_ms.wanimlibrary.config.WanimLibraryConfig
import org.springframework.context.annotation.Import

/**
 * Enable WanimLibrary features in your Spring Boot application.
 *
 * Usage:
 * ```kotlin
 * @SpringBootApplication
 * @EnableWanimLibrary
 * class MyApplication
 * ```
 */
@Import(WanimLibraryConfig::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EnableWanimLibrary
