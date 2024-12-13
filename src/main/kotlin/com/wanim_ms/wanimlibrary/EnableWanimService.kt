package com.wanim_ms.wanimlibrary

import org.springframework.context.annotation.Import

@Import(
    WanimServiceConfig::class
)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)

annotation class EnableWanimService