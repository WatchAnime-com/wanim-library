package com.wanim_ms.wanimlibrary.repo

import com.wanim_ms.wanimlibrary.LJPAProjection
import com.wanim_ms.wanimlibrary.model.BaseModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseJpaRepo<T, ID> : JpaRepository<T, ID>, JpaSpecificationExecutor<T>
