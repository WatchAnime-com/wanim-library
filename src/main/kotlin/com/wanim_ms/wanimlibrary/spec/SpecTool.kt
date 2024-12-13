package com.wanim_ms.wanimlibrary.spec

import com.wanim_ms.wanimlibrary.model.BaseModel
import com.wanim_ms.wanimlibrary.model.ParameterModel
import com.wanim_ms.wanimlibrary.model.SortOrder
import jakarta.persistence.Column
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification

open class SpecTool(private val base: ParameterModel) {
    fun ofPageable() = base.ofPageable()
    fun ofPageable(sort: Sort) = base.ofPageable(sort)

    fun ofSortedPageable(): Pageable {
        val sort = base.sortBy?.let { Sort.by(Sort.Direction.valueOf(base.sortOrder.value), it) } ?: Sort.unsorted()
        return base.ofPageable(sort)
    }

    interface JPAModel<T, ID> {
        var deleted: Boolean?
        var archived: Boolean?
        var id: ID?
        fun ofSearch(): Specification<T>

        fun defaultPredicates(
            root: Root<T>,
            query: CriteriaQuery<*>?,
            builder: CriteriaBuilder,
            params: BaseModel.SearchParams,
        ): Predicate {
            var predicate = builder.conjunction()

            deleted?.let {
                predicate = builder.and(predicate, builder.equal(root.get<Any>("deleted"), deleted))
            }

            archived?.let {
                predicate = builder.and(predicate, builder.equal(root.get<Any>("archived"), archived))
            }

            id?.let {
                predicate = builder.and(predicate, builder.equal(root.get<Any>("id"), id))
            }

            val fields = root.model.javaType.declaredFields
            when (params.sortOrder) {
                SortOrder.ASC -> params.sortBy?.let {
                    if (fields.any { field ->
                            val columnValue = field.getAnnotation(Column::class.java)?.name
                            field.name == it || columnValue == it
                        }) query?.orderBy(builder.asc(root.get<Any>(it)))
                }

                SortOrder.DESC -> params.sortBy?.let {
                    if (fields.any { field ->
                            val columnValue = field.getAnnotation(Column::class.java)?.name
                            field.name == it || columnValue == it
                        }) query?.orderBy(builder.desc(root.get<Any>(it)))
                }
            }

            return predicate
        }


        fun <K> typePredicate(builder: CriteriaBuilder, root: Root<T>, type: Class<K>): Predicate {
            return builder.equal(root.type(), type)
        }

        fun searchPredicate(
            predicate: Predicate,
            builder: CriteriaBuilder,
            root: Root<T>,
            search: String,
            vararg fields: String,
            type: SearchType = SearchType.LIKE,
        ): Predicate {
            val terms = search.split(" ").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.map { term ->
                builder.or(
                    *fields.map { field ->
                        when (type) {
                            SearchType.EQUAL -> builder.equal(builder.lower(root.get(field)), term)
                            SearchType.STARTS_WITH -> builder.like(builder.lower(root.get(field)), "$term%")
                            SearchType.ENDS_WITH -> builder.like(builder.lower(root.get(field)), "%$term")
                            SearchType.LIKE -> builder.like(builder.lower(root.get(field)), "%$term%")
                        }
                    }.toTypedArray()
                )
            }
            return builder.and(predicate, builder.or(*terms.toTypedArray()))
        }

    }


    enum class SearchType {
        EQUAL, STARTS_WITH, ENDS_WITH, LIKE
    }
}