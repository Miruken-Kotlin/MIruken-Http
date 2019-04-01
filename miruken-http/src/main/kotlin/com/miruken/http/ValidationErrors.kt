package com.miruken.http

import com.miruken.api.NamedType

@Suppress("ArrayInDataClass")
data class ValidationErrors(
        val propertyName: String?                  = null,
        val errors:       Array<String>?           = null,
        val nested:       Array<ValidationErrors>? = null,
        override val typeName: String = ValidationErrors.typeName
): NamedType {
    companion object : NamedType {
        override val typeName: String
            get() = "Miruken.Validate.ValidationErrors,Miruken.Validate"
    }
}