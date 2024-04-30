package model.protocol

import io.ktor.utils.io.core.*
import kotlinx.html.MAP
import kotlinx.serialization.Serializable
import utils.decodeHex
import utils.encodeHex

@Serializable
sealed class RData {
    abstract fun encode(): ByteArray
}

@Serializable
data class HexRData(val plainHex: String) : RData() {
    companion object {
        fun ByteArray.toHexRData(): HexRData {
            return HexRData(this.encodeHex())
        }
    }

    override fun encode(): ByteArray {
        return plainHex.decodeHex()
    }
}

/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.4.1">rfc1035#section-3.4.1</a>
 */
@Serializable
data class ARData(
    val address: String
) : RData() {
    companion object {
        fun ByteArray.toARData(): ARData {
            return ARData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }

    override fun encode(): ByteArray {
        return address.split(".").map { it.toInt().toByte() }.toByteArray()
    }

}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.11">rfc1035#section-3.3.11</a>
 */
@Serializable
data class NSRData(
    val nsdname: String
) : RData() {
    companion object {
        fun ByteArray.toNSRData(): NSRData {
            return NSRData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }

    override fun encode(): ByteArray {
        return nsdname.split(".").map { it.toInt().toByte() }.toByteArray()
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.1">rfc1035#section-3.3.1</a>
 */
@Serializable
data class CNAMERData(
    val cname: String
) : RData() {
    companion object {
        fun ByteArray.toCNAMERData(): CNAMERData {
            return CNAMERData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }

    override fun encode(): ByteArray {
        return cname.split(".").map { it.toInt().toByte() }.toByteArray()
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.13">rfc1035#section-3.3.13</a>
 */
@Serializable
data class SOARData(
    val mname: String,
    val rname: String,
    val serial: Long,
    val refresh: Long,
    val retry: Long,
    val expire: Long,
    val minimum: Long
) : RData() {
    companion object {
        fun ByteArray.toSOARData(): SOARData {
            TODO("Not yet implemented")
        }
    }

    override fun encode(): ByteArray {
        TODO("Not yet implemented")
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.12">rfc1035#section-3.3.12</a>
 */
@Serializable
data class PTRRData(
    val ptrdname: String
) : RData() {
    companion object {
        fun ByteArray.toPTRRData(): PTRRData {
            return PTRRData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }

    override fun encode(): ByteArray {
        return ptrdname.split(".").map { it.toInt().toByte() }.toByteArray()
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.9">rfc1035#section-3.3.9</a>
 */
@Serializable
data class MXRData(
    val preference: Int, val exchange: String
) : RData() {
    companion object {
        fun ByteArray.toMXRData(): MXRData {
            val preference = this.copyOfRange(0, 2).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            val exchange =
                this.copyOfRange(2, this.size).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            return MXRData(preference.toInt(), exchange)
        }
    }

    override fun encode(): ByteArray {
        return preference.toString().toByteArray() + exchange.split(".").map { it.toInt().toByte() }.toByteArray()
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.14">rfc1035#section-3.3.14</a>
 */
@Serializable
data class TXTRData(
    val txtData: String
) : RData() {
    companion object {
        fun ByteArray.toTXTRData(): TXTRData {
            return TXTRData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }

    override fun encode(): ByteArray {
        return txtData.split(".").map { it.toInt().toByte() }.toByteArray()
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3596#section-2.2">rfc3596#section-2.2</a>
 */
@Serializable
data class AAAARData(
    val address: String
) : RData() {
    companion object {
        fun ByteArray.toAAAARData(): AAAARData {
            return AAAARData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }

    override fun encode(): ByteArray {
        return address.split(".").map { it.toInt().toByte() }.toByteArray()
    }
}


//todo srv

/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6891#section-6.1.2">rfc6891#section-6.1.2</a>
 * todo not complete
 */
data class OPTRData(
    val pairs: MAP
) : RData() {
    override fun encode(): ByteArray {
        TODO("Not yet implemented")
    }
}
