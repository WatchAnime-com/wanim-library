package com.wanim_ms.wanimlibrary.spec

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.wanim_ms.wanimlibrary.AppContextUtil
import com.wanim_ms.wanimlibrary.model.BaseModel
import com.wanim_ms.wanimlibrary.model.ParameterModel
import java.util.*

abstract class BaseModelJpaSpec<T : BaseModel<ID>, ID>(params: ParameterModel, val fields: Collection<String>? = null) :
    SpecTool(params), SpecTool.JPAModel<T, ID> {
    fun toKey(): String {
        val mapper = AppContextUtil.bean(ObjectMapper::class.java).apply {
            setAnnotationIntrospector(object : JacksonAnnotationIntrospector() {
                override fun findPropertyIgnoralByName(
                    config: MapperConfig<*>?,
                    a: Annotated?
                ): JsonIgnoreProperties.Value {
                    return JsonIgnoreProperties.Value.empty()
                }
            })
        }
        val json = mapper.writeValueAsString(this)
        return Base64.getEncoder().encodeToString(json.toByteArray())
    }
}
