package com.wanim_ms.wanimlibrary.core.service

import com.wanim_ms.wanimlibrary.core.model.BaseModel
import com.wanim_ms.wanimlibrary.core.spec.BaseModelJpaSpec
import org.springframework.data.domain.Page

/**
 * Base service handler interface defining standard CRUD operations.
 *
 * @param Entity The entity type
 * @param Create DTO for creating entities
 * @param Update DTO for updating entities
 * @param Response DTO for responses
 * @param Spec Specification type for queries
 * @param ID Primary key type
 */
interface BaseServiceHandler<Entity : BaseModel<ID>, Create, Update, Response, Spec : BaseModelJpaSpec<Entity, ID>, ID> {
    
    /**
     * Create a new entity from the given DTO.
     */
    fun create(dto: Create): Response
    
    /**
     * Update an existing entity.
     */
    fun update(entity: Entity, dto: Update): Response
    
    /**
     * Check if any entity matches the specification.
     */
    fun exists(spec: Spec): Boolean
    
    /**
     * Find a single entity matching the specification.
     * @throws NoSuchElementException if not found
     */
    fun find(spec: Spec): Entity
    
    /**
     * Find all entities matching the specification.
     */
    fun findAll(spec: Spec): Page<Entity>
    
    /**
     * Find an entity by its ID.
     * @throws NoSuchElementException if not found
     */
    fun findById(id: ID): Entity
    
    /**
     * Save an entity (create or update).
     */
    fun save(entity: Entity): Entity

    /**
     * Soft delete an entity by ID.
     */
    fun delete(id: ID) {
        throw UnsupportedOperationException("Delete not implemented")
    }

    /**
     * Restore a soft-deleted entity.
     */
    fun restore(id: ID) {
        throw UnsupportedOperationException("Restore not implemented")
    }

    /**
     * Get all soft-deleted entities (recycle bin).
     */
    fun recycleBin(spec: Spec): Page<Entity> {
        throw UnsupportedOperationException("Recycle bin not implemented")
    }

    /**
     * Permanently delete an entity.
     */
    fun deletePermanently(id: ID) {
        throw UnsupportedOperationException("Permanent delete not implemented")
    }

    /**
     * Archive an entity.
     */
    fun archive(id: ID) {
        throw UnsupportedOperationException("Archive not implemented")
    }

    /**
     * Unarchive an entity.
     */
    fun unArchive(id: ID) {
        throw UnsupportedOperationException("Unarchive not implemented")
    }

    /**
     * Find all archived entities.
     */
    fun findAllArchived(spec: Spec): Page<Entity> {
        throw UnsupportedOperationException("Find archived not implemented")
    }
}
