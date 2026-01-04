package com.wanim_ms.wanimlibrary.util.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

/**
 * JSON serializer for Spring Data Page objects.
 * Produces a cleaner JSON structure than the default.
 */
class PageSerializer : JsonSerializer<Page<*>>() {
    override fun serialize(value: Page<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField("content", value.content)
        gen.writeNumberField("page", value.number + 1) // 1-indexed for users
        gen.writeNumberField("size", value.size)
        gen.writeNumberField("totalElements", value.totalElements)
        gen.writeNumberField("totalPages", value.totalPages)
        gen.writeBooleanField("first", value.isFirst)
        gen.writeBooleanField("last", value.isLast)
        gen.writeBooleanField("empty", value.isEmpty)
        gen.writeEndObject()
    }
}

/**
 * Generic JSON deserializer for Spring Data Page objects.
 */
class PageDeserializer<T>(private val contentClass: Class<T>? = null) : JsonDeserializer<Page<T>>() {
    
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): Page<T> {
        val node = parser.codec.readTree<JsonNode>(parser)
        
        val content = node["content"].traverse(parser.codec).readValueAs(List::class.java) as List<T>
        val page = (node["page"]?.asInt() ?: 1) - 1 // Convert from 1-indexed to 0-indexed
        val size = node["size"]?.asInt() ?: 10
        val totalElements = node["totalElements"]?.asLong() ?: content.size.toLong()
        
        return PageImpl(content, PageRequest.of(page.coerceAtLeast(0), size), totalElements)
    }
}

/**
 * Utility functions for Page operations.
 */
object PageUtils {
    
    /**
     * Convert a list to a Page with manual pagination.
     */
    fun <T> listToPage(
        list: List<T>,
        page: Int,
        size: Int,
        totalElements: Long = list.size.toLong()
    ): Page<T> {
        val pageIndex = (page - 1).coerceAtLeast(0)
        val startIndex = pageIndex * size
        
        val pageContent = when {
            startIndex >= list.size -> emptyList()
            else -> list.subList(
                fromIndex = startIndex,
                toIndex = minOf(startIndex + size, list.size)
            )
        }
        
        return PageImpl(pageContent, PageRequest.of(pageIndex, size), totalElements)
    }
}
