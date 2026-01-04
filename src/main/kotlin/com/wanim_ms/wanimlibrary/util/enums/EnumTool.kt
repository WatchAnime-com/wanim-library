package com.wanim_ms.wanimlibrary.util.enums

import com.fasterxml.jackson.annotation.JsonProperty
import com.wanim_ms.wanimlibrary.core.context.AppContextUtil
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

/**
 * Data class representing an enum constant for API responses.
 */
data class EnumConstant(
    @JsonProperty("label")
    val label: String,
    @JsonProperty("value")
    val value: String,
    @JsonProperty("color")
    val color: String? = null,
    @JsonProperty("icon")
    val icon: String? = null
)

/**
 * Interface for enums that can be serialized to EnumConstant.
 * Provides i18n support for labels.
 *
 * Usage:
 * ```kotlin
 * enum class Status : EnumTool {
 *     ACTIVE {
 *         override val label = "status.active"
 *         override val color = "green"
 *     },
 *     INACTIVE {
 *         override val label = "status.inactive"
 *         override val color = "red"
 *     };
 *
 *     override val value: String get() = name
 *     override val icon: String? = null
 * }
 * ```
 */
interface EnumTool {
    val label: String
    val value: String
    val color: String?
    val icon: String?

    /**
     * Convert to EnumConstant with localized label.
     */
    fun toConstant(): EnumConstant {
        return EnumConstant(
            label = labelLocalized(),
            value = value,
            color = color,
            icon = icon
        )
    }

    /**
     * Get the localized label using Spring MessageSource.
     */
    fun labelLocalized(): String {
        return try {
            val source = AppContextUtil.bean(MessageSource::class.java)
            val locale = LocaleContextHolder.getLocale()
            source.getMessage(label, null, label, locale) ?: label
        } catch (e: Exception) {
            label
        }
    }
}

/**
 * Extension function to convert all enum values to EnumConstants.
 */
inline fun <reified T> enumToConstants(): List<EnumConstant> where T : Enum<T>, T : EnumTool {
    return enumValues<T>().map { it.toConstant() }
}
