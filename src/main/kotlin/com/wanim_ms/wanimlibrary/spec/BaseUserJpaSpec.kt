package com.wanim_ms.wanimlibrary.spec

import com.wanim_ms.wanimlibrary.model.BaseModel
import com.wanim_ms.wanimlibrary.model.ParameterModel

abstract class BaseModelJpaSpec<T: BaseModel<ID>,ID>(params: ParameterModel, val fields: Collection<String>? = null) : SpecTool(params),SpecTool.JPAModel<T,ID>
