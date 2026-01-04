package com.wanim_ms.wanimlibrary.core.repository

import com.wanim_ms.wanimlibrary.core.model.BaseModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean

/**
 * Base JPA repository interface combining JpaRepository, JpaSpecificationExecutor,
 * and custom projection capabilities.
 *
 * Usage:
 * ```kotlin
 * interface UserRepository : BaseJpaRepository<User, Long>
 * ```
 *
 * @param T The entity type extending BaseModel
 * @param ID The primary key type
 */
@NoRepositoryBean
interface BaseJpaRepository<T : BaseModel<ID>, ID> : 
    JpaRepository<T, ID>, 
    JpaSpecificationExecutor<T>,
    JpaProjectionExecutor<T, ID>
