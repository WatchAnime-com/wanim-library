package com.wanim_ms.wanimlibrary.exception

import java.time.LocalDateTime

data class ExceptionModel(
    val timestamp: LocalDateTime,
    val status: Int,
    val message: String,
    val path: String
)