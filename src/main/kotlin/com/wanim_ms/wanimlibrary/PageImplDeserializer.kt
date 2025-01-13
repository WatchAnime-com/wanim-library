package com.wanim_ms.wanimlibrary

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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

    fun <T> convertListToPage(list: List<T>, pageable: Pageable): Page<T> {
        val pageSize = pageable.pageSize
        val currentPage = pageable.pageNumber
        val startItem = currentPage * pageSize
        val listSubList: List<T>

        if (list.size < startItem) {
            listSubList = emptyList()
        } else {
            val toIndex = minOf(startItem + pageSize, list.size)
            listSubList = list.subList(startItem, toIndex)
        }

        return PageImpl(listSubList, pageable, list.size.toLong())
    }

}