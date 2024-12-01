package com.wanim_ms.wanimlibrary.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseJpaHandler<T, ID> : JpaRepository<T, ID>, JpaSpecificationExecutor<T> {}
