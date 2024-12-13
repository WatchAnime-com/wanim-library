package com.wanim_ms.wanimlibrary.service

import org.springframework.data.domain.Page

interface BaseServiceHandler<MainModel, Create, Update, Response, Spec,ID> {
    fun create(crate: Create): Response
    fun update(main: MainModel, update: Update): Response
    fun exists(spec: Spec): Boolean
    fun find(spec: Spec): MainModel
    fun findAll(spec: Spec): Page<MainModel>
    fun findById(id: ID): MainModel

    fun save(main: MainModel):MainModel

    fun delete(id: ID){
        throw UnsupportedOperationException()
    }

    fun restore(id: ID){
        throw UnsupportedOperationException()
    }

    fun recycleBin(spec: Spec): Page<MainModel>{
        throw UnsupportedOperationException()
    }

    fun deletePermanently(id: ID){
        throw UnsupportedOperationException()
    }

    fun specUpdate(main: MainModel) : MainModel{
        throw UnsupportedOperationException()
    }

    fun findAllArchived(spec: Spec): Page<MainModel>{
        throw NotImplementedError()
    }
    fun archive(id: ID){
        throw UnsupportedOperationException()
    }
    fun unArchive(id: ID){
        throw UnsupportedOperationException()
    }
}