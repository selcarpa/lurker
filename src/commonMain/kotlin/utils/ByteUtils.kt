package utils

 fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

 fun ByteArray.encodeHex(): String = joinToString("") {
    it.toInt().and(0xff).toString(16).padStart(2, '0')
}


fun ByteArray.toInt(): Int {
    var result = 0
    for (srcByte in this) {
        result = (result shl 8) + (srcByte.toInt() and 0xFF)
    }
    return result
}
