package com.wanim_ms.wanimlibrary.core.repository

import com.wanim_ms.wanimlibrary.core.context.AppContextUtil
import com.wanim_ms.wanimlibrary.core.model.BaseModel
import com.wanim_ms.wanimlibrary.core.model.ParameterModel
import com.wanim_ms.wanimlibrary.core.spec.BaseModelJpaSpec
import jakarta.persistence.*
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.JoinType
import org.apache.commons.lang3.reflect.FieldUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.util.*

/**
 * Interface for executing JPA projections with custom specifications.
 * Provides findOne, findAll, and exists methods with projection support.
 *
 * @param T The entity type
 * @param ID The primary key type
 */
@JvmDefaultWithCompatibility
interface JpaProjectionExecutor<T : BaseModel<ID>, ID> {

    /**
     * Find a single entity matching the specification.
     *
     * @param spec The specification to filter entities
     * @param clazz The entity class
     * @param attributes Optional attributes to fetch eagerly
     * @return Optional containing the entity if found
     */
    fun findOne(
        spec: BaseModelJpaSpec<T, ID>, 
        clazz: Class<T>, 
        attributes: Array<String>? = null
    ): Optional<T> {
        val query = createQuery(spec, clazz) ?: return Optional.empty()
        val result = entityManager().createQuery(query).apply {
            attributes?.let { 
                setHint("jakarta.persistence.loadgraph", createEntityGraph(clazz, *it)) 
            }
        }.resultList
        return Optional.ofNullable(result.firstOrNull())
    }

    /**
     * Find all entities matching the specification with pagination.
     *
     * @param spec The specification to filter entities
     * @param clazz The entity class
     * @param attributes Optional attributes to fetch eagerly
     * @return Page of matching entities
     */
    fun findAll(
        spec: BaseModelJpaSpec<T, ID>, 
        clazz: Class<T>, 
        attributes: Array<String>? = null
    ): Page<T> {
        val query = createQuery(spec, clazz) 
            ?: return PageImpl(emptyList(), spec.ofPageable(), 0L)
        
        val pageable = spec.ofSortedPageable()
        val typedQuery = entityManager().createQuery(query).apply {
            firstResult = pageable.pageNumber * pageable.pageSize
            maxResults = pageable.pageSize
            attributes?.let { 
                setHint("jakarta.persistence.loadgraph", createEntityGraph(clazz, *it)) 
            }
        }
        
        val countQuery = entityManager().createQuery(count(spec, clazz))
        val count = countQuery.singleResult

        return PageImpl(typedQuery.resultList, pageable, count)
    }

    /**
     * Check if any entity exists matching the specification.
     *
     * @param spec The specification to filter entities
     * @param clazz The entity class
     * @return true if at least one entity matches
     */
    fun exists(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>): Boolean {
        val query = count(spec, clazz)
        return entityManager().createQuery(query).singleResult > 0
    }

    private fun createEntityGraph(clazz: Class<T>, vararg attributes: String): EntityGraph<T> {
        val entityManager = entityManager()
        val entityGraph = entityManager.createEntityGraph(clazz)
        entityGraph.addAttributeNodes(*attributes)
        return entityGraph
    }

    private fun createQuery(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>): CriteriaQuery<T>? {
        val cb = entityManager().criteriaBuilder
        val query = cb.createQuery(clazz)
        val root = query.from(clazz)

        val predicate = cb.and(
            spec.ofSearch().toPredicate(root, query, cb),
            spec.defaultPredicates(root, query, cb, ParameterModel().apply {
                sortBy = spec.base.sortBy
                sortOrder = spec.base.sortOrder
            })
        )

        return query.where(predicate)
    }

    private fun count(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>): CriteriaQuery<Long> {
        val builder = entityManager().criteriaBuilder
        val query = builder.createQuery(Long::class.java)
        val root = query.from(clazz)
        
        val predicate = builder.and(
            spec.ofSearch().toPredicate(root, query, builder),
            spec.defaultPredicates(root, query, builder, ParameterModel())
        )
        
        query.select(builder.count(root)).where(predicate)
        return query
    }

    private fun projection(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>): CriteriaQuery<Tuple>? {
        val builder = entityManager().criteriaBuilder
        val query = builder.createTupleQuery()
        val root = query.from(clazz)
        
        val fields = spec.fields?.toMutableSet() 
            ?: FieldUtils.getAllFields(clazz).map { it.name }.toMutableSet()
        fields.addAll(listOf("id", "pk", "sk", "deleted", "archived", "updatedAt", "createdAt"))
        
        val search = spec.ofSearch().toPredicate(root, query, builder)
        query.multiselect(fields.map { fieldName ->
            val declaredField = FieldUtils.getField(clazz, fieldName, true)

            if (declaredField.isAnnotationPresent(OneToMany::class.java) ||
                declaredField.isAnnotationPresent(ManyToOne::class.java) ||
                declaredField.isAnnotationPresent(OneToOne::class.java) ||
                declaredField.isAnnotationPresent(ManyToMany::class.java)
            ) {
                root.join<Any, Any>(fieldName, JoinType.LEFT).alias(fieldName)
            } else {
                root.get<Any>(fieldName).alias(fieldName)
            }
        }).where(search)
        
        return query
    }

    private fun entityManager(): EntityManager = AppContextUtil.bean(EntityManager::class.java)
}
