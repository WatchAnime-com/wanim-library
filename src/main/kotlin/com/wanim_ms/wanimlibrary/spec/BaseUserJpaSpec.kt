package com.wanim_ms.wanimlibrary.spec

import com.wanim_ms.wanimlibrary.model.BaseTableRepo
import com.wanim_ms.wanimlibrary.model.BaseUserTable
import com.wanim_ms.wanimlibrary.model.ParameterModel

abstract class BaseUserJpaSpec<T: BaseUserTable>(params: ParameterModel, val fields: Collection<String>? = null) : SpecTool(params),SpecTool.JPAModel<T>

abstract class BaseTableJpaSpec<T: BaseTableRepo>(params: ParameterModel, val fields: Collection<String>? = null) : SpecTool(params),SpecTool.JPAModel<T>