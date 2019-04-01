package com.miruken.http

import com.miruken.api.NamedType

data class RawJson(val json: String) : NamedType {
    override val typeName = "raw"
}