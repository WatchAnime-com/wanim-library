package com.wanim_ms.wanimlibrary

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

class PageImplDeserializer<T> : JsonDeserializer<PageImpl<T>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PageImpl<T> {
        val mapper = (p.codec as ObjectMapper)
        val node: JsonNode = mapper.readTree(p)

        val contentNode = node.get("content")
        val content = mapper.convertValue(contentNode, List::class.java) as List<T>

        val pageableNode = node.get("pageable")
        val pageable = PageRequest.of(
            pageableNode.get("pageNumber").asInt(),
            pageableNode.get("pageSize").asInt()
        )

        val totalElements = node.get("totalElements").asLong()
        return PageImpl(content, pageable, totalElements)
    }

    fun <T> convertListToPage(
        list: List<T>,
        pageable: Pageable,
        totalSize: Long // size parametresi daha açıklayıcı bir isim ile değiştirildi
    ): Page<T> {
        val startIndex = pageable.pageNumber * pageable.pageSize

        val pageContent = when {
            list.size < startIndex -> emptyList()
            else -> list.subList(
                fromIndex = startIndex,
                toIndex = minOf(startIndex + pageable.pageSize, list.size)
            )
        }
        return PageImpl(pageContent, pageable, totalSize)
    }
}


class PageSerializer : JsonSerializer<Page<*>>() {
    override fun serialize(value: Page<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField("content", value.content)
        gen.writeNumberField("page", value.number)
        gen.writeNumberField("size", value.size)
        gen.writeNumberField("totalElements", value.totalElements)
        gen.writeNumberField("totalPages", value.totalPages)
        gen.writeEndObject()
    }
}

class PageDeserializer<T>(private val contentClass: Class<T>) : JsonDeserializer<Page<T>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Page<T> {
        val node = p.codec.readTree<com.fasterxml.jackson.databind.JsonNode>(p)
        val content = node["content"].traverse(p.codec).readValueAs(List::class.java)
        val page = node["page"].asInt()
        val size = node["size"].asInt()
        val totalElements = node["totalElements"].asLong()
        return PageImpl(content as List<T>, PageRequest.of(page, size), totalElements)
    }
}