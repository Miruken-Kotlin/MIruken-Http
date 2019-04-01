package com.miruken.http.jackson

import com.miruken.api.NamedType
import com.miruken.api.Request


enum class StockType {
    Common {
        override val hasDividend = false
    },
    Preferred {
        override val hasDividend = true
    };

    abstract val hasDividend: Boolean
}

data class StockQuote(
        val symbol: String,
        val value:  Double,
        val type:   StockType,
        override val typeName: String = StockQuote.typeName
): NamedType {
    companion object : NamedType {
        override val typeName = "StockQuote"
    }
}

data class GetStockQuote(
        val symbol: String,
        override val typeName: String = StockQuote.typeName
): Request<StockQuote> {
    companion object : NamedType {
        override val typeName = "GetStockQuote"
    }
}
