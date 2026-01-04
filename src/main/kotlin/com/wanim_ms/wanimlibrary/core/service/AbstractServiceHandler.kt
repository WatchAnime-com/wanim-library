package com.wanim_ms.wanimlibrary.core.service

import com.wanim_ms.wanimlibrary.core.model.BaseModel
import com.wanim_ms.wanimlibrary.core.repository.BaseJpaRepository
import com.wanim_ms.wanimlibrary.core.spec.BaseModelJpaSpec
import org.springframework.data.domain.Page

/**
 * Abstract service handler with default implementations for common operations.
 * Extend this class to get basic CRUD functionality out of the box.
 *
 * @param Entity The entity type
 * @param Create DTO for creating entities
 * @param Update DTO for updating entities
 * @param Response DTO for responses
 * @param Spec Specification type for queries
 * @param ID Primary key type
 */
abstract class AbstractServiceHandler<
    Entity : BaseModel<ID>, 
    Create, 
    Update, 
    Response, 
    Spec : BaseModelJpaSpec<Entity, ID>, 
    ID : Any
>(
    protected val repository: BaseJpaRepository<Entity, ID>,
    protected val entityClass: Class<Entity>
) : BaseServiceHandler<Entity, Create, Update, Response, Spec, ID> {

    /**
     * Convert a Create DTO to an Entity.
     */
    protected abstract fun toEntity(dto: Create): Entity
    
    /**
     * Apply updates from Update DTO to an Entity.
     */
    protected abstract fun applyUpdate(entity: Entity, dto: Update): Entity
    
    /**
     * Convert an Entity to a Response DTO.
     */
    protected abstract fun toResponse(entity: Entity): Response

    override fun create(dto: Create): Response {
        val entity = toEntity(dto)
        val saved = repository.save(entity)
        return toResponse(saved)
    }

    override fun update(entity: Entity, dto: Update): Response {
        val updated = applyUpdate(entity, dto)
        val saved = repository.save(updated)
        return toResponse(saved)
    }

    override fun exists(spec: Spec): Boolean {
        return repository.exists(spec, entityClass)
    }

    override fun find(spec: Spec): Entity {
        return repository.findOne(spec, entityClass)
            .orElseThrow { NoSuchElementException("Entity not found") }
    }

    override fun findAll(spec: Spec): Page<Entity> {
        return repository.findAll(spec, entityClass)
    }

    override fun findById(id: ID): Entity {
        return repository.findById(id)
            .orElseThrow { NoSuchElementException("Entity not found with id: $id") }
    }

    override fun save(entity: Entity): Entity {
        return repository.save(entity)
    }

    override fun delete(id: ID) {
        val entity = findById(id)
        entity.deleted = true
        repository.save(entity)
    }

    override fun restore(id: ID) {
        val entity = findById(id)
        entity.deleted = false
        repository.save(entity)
    }

    override fun deletePermanently(id: ID) {
        repository.deleteById(id)
    }

    override fun archive(id: ID) {
        val entity = findById(id)
        entity.archived = true
        repository.save(entity)
    }

    override fun unArchive(id: ID) {
        val entity = findById(id)
        entity.archived = false
        repository.save(entity)
    }
}
