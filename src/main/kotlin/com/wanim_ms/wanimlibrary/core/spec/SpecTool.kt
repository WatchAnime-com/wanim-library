package com.wanim_ms.wanimlibrary.core.spec

import com.wanim_ms.wanimlibrary.core.model.BaseModel
import com.wanim_ms.wanimlibrary.core.model.ParameterModel
import com.wanim_ms.wanimlibrary.core.model.SortOrder
import jakarta.persistence.Column
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification

/**
 * Specification tool providing common JPA query building utilities.
 */
open class SpecTool(val base: ParameterModel) {
    
    fun ofPageable(): Pageable = base.ofPageable()
    
    fun ofPageable(sort: Sort): Pageable = base.ofPageable(sort)

    fun ofSortedPageable(): Pageable {
        val sort = base.sortBy?.let {
            Sort.by(Sort.Direction.valueOf(base.sortOrder.value), it)
        } ?: Sort.unsorted()
        return base.ofPageable(sort)
    }

    /**
     * Interface for JPA model specifications.
     * Implement this to create custom specifications for your entities.
     */
    interface JPAModel<T, ID> {
        var deleted: Boolean?
        var archived: Boolean?
        var id: ID?
        
        /**
         * Create the search specification for this model.
         */
        fun ofSearch(): Specification<T>

        /**
         * Build default predicates for common fields (deleted, archived, id).
         */
        fun defaultPredicates(
            root: Root<T>,
            query: CriteriaQuery<*>?,
            builder: CriteriaBuilder,
            params: ParameterModel,
        ): Predicate {
            var predicate = builder.conjunction()

            deleted?.let {
                predicate = builder.and(predicate, builder.equal(root.get<Any>("deleted"), it))
            }

            archived?.let {
                predicate = builder.and(predicate, builder.equal(root.get<Any>("archived"), it))
            }

            id?.let {
                predicate = builder.and(predicate, builder.equal(root.get<Any>("id"), it))
            }

            applySorting(root, query, builder, params)

            return predicate
        }

        /**
         * Apply sorting to the query based on parameters.
         */
        private fun applySorting(
            root: Root<T>,
            query: CriteriaQuery<*>?,
            builder: CriteriaBuilder,
            params: ParameterModel
        ) {
            params.sortBy?.let { sortField ->
                val fields = root.model.javaType.declaredFields
                val isValidField = fields.any { field ->
                    val columnValue = field.getAnnotation(Column::class.java)?.name
                    field.name == sortField || columnValue == sortField
                }

                if (isValidField) {
                    val order = when (params.sortOrder) {
                        SortOrder.ASC -> builder.asc(root.get<Any>(sortField))
                        SortOrder.DESC -> builder.desc(root.get<Any>(sortField))
                    }
                    query?.orderBy(order)
                }
            }
        }

        /**
         * Create a type predicate for entity type discrimination.
         */
        fun <K> typePredicate(builder: CriteriaBuilder, root: Root<T>, type: Class<K>): Predicate {
            return builder.equal(root.type(), type)
        }

        /**
         * Build a search predicate for text fields.
         * Supports multiple search terms and various search types.
         */
        fun searchPredicate(
            predicate: Predicate,
            builder: CriteriaBuilder,
            root: Root<T>,
            search: String,
            vararg fields: String,
            type: SearchType = SearchType.LIKE,
        ): Predicate {
            val terms = search.split(" ")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .map { term ->
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

    /**
     * Search type options for text search predicates.
     */
    enum class SearchType {
        EQUAL,
        STARTS_WITH,
        ENDS_WITH,
        LIKE
    }
}
