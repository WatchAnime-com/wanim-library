package com.wanim_ms.wanimlibrary.core.spec

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.wanim_ms.wanimlibrary.core.context.AppContextUtil
import com.wanim_ms.wanimlibrary.core.model.BaseModel
import com.wanim_ms.wanimlibrary.core.model.ParameterModel
import java.util.*

/**
 * Abstract base specification for JPA queries.
 * Combines SpecTool functionality with field selection for projections.
 *
 * @param T The entity type
 * @param ID The entity's primary key type
 * @param params The parameter model containing search/filter criteria
 * @param fields Optional collection of fields for projection
 */
abstract class BaseModelJpaSpec<T : BaseModel<ID>, ID>(
    params: ParameterModel, 
    val fields: Collection<String>? = null
) : SpecTool(params), SpecTool.JPAModel<T, ID> {
    
    /**
     * Generates a cache key from the current specification state.
     * Useful for caching query results.
     */
    fun toCacheKey(): String {
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
    
    @Deprecated("Use toCacheKey() instead", ReplaceWith("toCacheKey()"))
    fun toKey(): String = toCacheKey()
}
