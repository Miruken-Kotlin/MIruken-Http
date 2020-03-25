package com.miruken.http

import java.lang.RuntimeException

class UnknownExceptionPayload : RuntimeException {
    constructor(payload: Any) : super(
            "Unable to map the exception payload '${payload::class}'") {
        this.payload = payload
    }

    constructor(payload: Any, cause: Throwable) : super(
            "Unable to map the exception payload '${payload::class}'", cause) {
        this.payload = payload
    }

    var payload: Any
        private set
}
