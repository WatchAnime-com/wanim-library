package com.wanim_ms.wanimlibrary

import com.wanim_ms.wanimlibrary.model.BaseModel
import com.wanim_ms.wanimlibrary.spec.BaseModelJpaSpec
import jakarta.persistence.*
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.JoinType
import org.apache.commons.lang3.reflect.FieldUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.util.*

/**
 * Interface for JPA projections.
 *
 * @param T the type of the entity.
 *
 */
@JvmDefaultWithCompatibility
interface LJPAProjection<T : BaseModel<ID>, ID> {

    /**
     * Finds a single entity matching the given specification.
     *
     * @param spec the specification to filter entities.
     * @param clazz the class of the entity.
     * @return an optional containing the entity if found, or empty if not found.
     */
    fun findOne(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>, attributes: Array<String>? = null): Optional<T> {
        val query = createQuery(spec, clazz) ?: return Optional.empty()
        val result = manager().createQuery(query).apply {
            attributes?.let { setHint("javax.persistence.loadgraph", createEntityGraph(clazz, *it)) }
        }.resultList
        return result.firstOrNull()?.let { Optional.of(it) } ?: Optional.empty()
    }

    /**
     * Finds all entities matching the given specification.
     *
     * @param spec the specification to filter entities.
     * @param clazz the class of the entity.
     * @return a page of entities matching the specification.
     */
    fun findAll(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>, attributes: Array<String>? = null): Page<T> {
        val query = createQuery(spec, clazz) ?: return PageImpl(emptyList(), spec.ofPageable(), 0L)
        val pageable = spec.ofSortedPageable()
        val typedQuery = manager().createQuery(query).apply {
            firstResult = pageable.pageNumber * pageable.pageSize
            maxResults = pageable.pageSize
            attributes?.let { setHint("javax.persistence.loadgraph", createEntityGraph(clazz, *it)) }
        }
        val countQuery = manager().createQuery(count(spec, clazz))
        val count = countQuery.singleResult as Long

        return PageImpl(typedQuery.resultList, pageable, count)
    }

    private fun createEntityGraph(clazz: Class<T>, vararg attributes: String): EntityGraph<T> {
        val entityManager = manager()
        val entityGraph = entityManager.createEntityGraph(clazz)
        entityGraph.addAttributeNodes(*attributes)
        return entityGraph
    }

    private fun createQuery(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>): CriteriaQuery<T>? {
        val cb = manager().criteriaBuilder
        val query = cb.createQuery(clazz)
        val root = query.from(clazz)
        val predicate = cb.and(
            spec.ofSearch().toPredicate(root, query, cb),
            spec.defaultPredicates(root, query, cb, BaseModel.SearchParams())
        )
        return query.where(predicate)
    }

    /**
     * Checks if an entity matching the given specification exists.
     *
     * @param spec the specification to filter entities.
     * @param clazz the class of the entity.
     * @return true if an entity exists, false otherwise.
     */
    fun exists(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>): Boolean {
        val query = count(spec, clazz)
        return manager().createQuery(query).singleResult > 0
    }

    /**
     * Creates a projection query based on the given specification and entity class.
     *
     * @param spec the specification to filter entities.
     * @param clazz the class of the entity.
     * @return a criteria query for the projection.
     */
    private fun projection(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>): CriteriaQuery<Tuple>? {
        val builder = manager().criteriaBuilder
        val query = builder.createTupleQuery()
        val root = query.from(clazz)
        val fields =
            spec.fields?.map { it }?.toMutableSet() ?: FieldUtils.getAllFields(clazz).map { it.name }.toMutableSet()
        fields.addAll(listOf("id", "pk", "sk", "deleted", "archived", "updatedAt", "createdAt"))
        val search = spec.ofSearch().toPredicate(root, query, builder)
        query.multiselect(fields.map {
            val declaredField = FieldUtils.getField(clazz, it, true)

            if (declaredField.isAnnotationPresent(OneToMany::class.java) ||
                declaredField.isAnnotationPresent(ManyToOne::class.java) ||
                declaredField.isAnnotationPresent(OneToOne::class.java) ||
                declaredField.isAnnotationPresent(ManyToMany::class.java)
            ) {

                val join = root.join<Any, Any>(it, JoinType.LEFT)
                join.alias(it)

            } else {
                root.get<Any>(it).alias(it)
            }
        }).where(search)
        return query
    }

    /**
     * Creates a count query based on the given specification and entity class.
     *
     * @param spec the specification to filter entities.
     * @param clazz the class of the entity.
     * @return a criteria query for counting the entities.
     */
     fun count(spec: BaseModelJpaSpec<T, ID>, clazz: Class<T>): CriteriaQuery<Long> {
        val builder = manager().criteriaBuilder
        val query = builder.createQuery(Long::class.java)
        val root = query.from(clazz)
        val predicate = builder.and(
            spec.ofSearch().toPredicate(root, query, builder),
            spec.defaultPredicates(root, query, builder, BaseModel.SearchParams())
        )
        query.select(builder.count(root))
            .where(predicate)
        query.orderBy()
        return query
    }

    /**
     * Retrieves the entity manager from the application context.
     *
     * @return the entity manager.
     */
    private fun manager(): EntityManager = AppContextUtil.bean(EntityManager::class.java)

    @Suppress("UNCHECKED_CAST")
    private fun setFields(entity: T, tuple: Tuple) {
        val fields = FieldUtils.getAllFields(entity::class.java)
        fields.filter { field -> field.name in tuple.elements.map { element -> element.alias } }
            .forEach { field ->
                field.isAccessible = true
                val value = tuple.get(field.name)
                if (Collection::class.java.isAssignableFrom(field.type)) {
                    val collection = field.get(entity) as MutableCollection<Any>? ?: mutableListOf()
                    if (value != null)
                        collection.add(value)

                    field.set(entity, collection)
                } else {
                    field.set(entity, value)
                }
            }
    }
}

