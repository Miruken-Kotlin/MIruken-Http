@file:Suppress("unused")

package com.miruken.http.jackson

import com.fasterxml.jackson.annotation.JsonGetter
import com.miruken.validate.ValidationErrors

class ValidationErrorMappingMixin(
        @get:JsonGetter(value = "\$values")
        val errors: Array<ValidationErrors>
)
