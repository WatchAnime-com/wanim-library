package com.wanim_ms.wanimlibrary.enums

import com.wanim_ms.wanimlibrary.AppContextUtil
import com.wanim_ms.wanimlibrary.constant.EnumConstant
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

interface EnumTool {
    val label: String
    val value: String
    val color: String?
    val icon: String?


    /**
     * Converts the enum to a constant.
     * @return The constant.
     */
    fun toConstant(): EnumConstant {
        return EnumConstant(labelLocalized(), value, color, icon)
    }

    /**
     * Retrieves the localized label of the enum.
     * @return The localized label.
     */
    fun labelLocalized(): String {
        val source = AppContextUtil.bean(MessageSource::class.java)
        val locale = LocaleContextHolder.getLocale()
        return source.getMessage(label, null, locale)
    }
}