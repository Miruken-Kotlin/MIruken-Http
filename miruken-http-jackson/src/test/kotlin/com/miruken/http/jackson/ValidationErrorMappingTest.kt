package com.miruken.http.jackson

import com.fasterxml.jackson.module.kotlin.readValue
import com.miruken.http.Message
import com.miruken.validate.ValidationErrorMapping
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ValidationErrorMappingTest {
    @Test fun `Deserializes message with validation errors`() {
        val json    = "{\"payload\":{\"\$type\":\"Miruken.Validate.ValidationErrors[], Miruken.Validate\",\"\$values\":[{\"propertyName\":\"MediaNumber\",\"errors\":[\"Not a valid membership\"]}]}}"
        val message = JacksonProvider.mapper.readValue<Message>(json)
        val mapping = message.payload as? ValidationErrorMapping
        assertNotNull(mapping)

        val errors = mapping.errors
        assertEquals(1, errors.size)
        assertEquals("MediaNumber", errors[0].propertyName)
        assertTrue(arrayOf("Not a valid membership").contentEquals(errors[0].errors!!))
    }
}