package com.wanim_ms.wanimlibrary.constant

import com.fasterxml.jackson.annotation.JsonProperty

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