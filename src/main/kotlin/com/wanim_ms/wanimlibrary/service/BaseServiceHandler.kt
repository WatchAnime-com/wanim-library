package com.wanim_ms.wanimlibrary.service

import org.springframework.data.domain.Page

interface BaseServiceHandler<MainModel, Create, Update, Response, Spec> {
    fun crate(crate: Create): Response
    fun update(main: MainModel, update: Update): Response
    fun exists(spec: Spec): Boolean
    fun find(spec: Spec): MainModel
    fun findAll(spec: Spec): Page<MainModel>
    fun findById(id: String): MainModel
    fun delete(id: String)
    fun restore(id: String)
    fun recycleBin(spec: Spec): Page<MainModel>
    fun deletePermanently(id: String)
    fun findAllArchived(spec: Spec): Page<MainModel>
    fun archive(id: String)
    fun unArchive(id: String)
}