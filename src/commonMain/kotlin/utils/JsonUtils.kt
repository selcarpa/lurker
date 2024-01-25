package utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val json= Json {
    isLenient = true
    ignoreUnknownKeys = true
    explicitNulls = false
}
