package model.protocol

import kotlinx.html.MAP


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.4.1">rfc1035#section-3.4.1</a>
 */
data class ARData(
    val address: String
) {
    companion object {
        fun ByteArray.toARData(): ARData {
            return ARData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.11">rfc1035#section-3.3.11</a>
 */
data class NSRData(
    val nsdname: String
) {
    companion object {
        fun ByteArray.toNSRData(): NSRData {
            return NSRData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.1">rfc1035#section-3.3.1</a>
 */
data class CNAMERData(
    val cname: String
) {
    companion object {
        fun ByteArray.toCNAMERData(): CNAMERData {
            return CNAMERData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.13">rfc1035#section-3.3.13</a>
 */
data class SOARData(
    val mname: String,
    val rname: String,
    val serial: Long,
    val refresh: Long,
    val retry: Long,
    val expire: Long,
    val minimum: Long
) {
    companion object {
        fun ByteArray.toSOARData(): SOARData {
            val mname = this.copyOfRange(0, 4).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            val rname = this.copyOfRange(4, 8).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            val serial = this.copyOfRange(8, 12).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            val refresh = this.copyOfRange(12, 16).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            val retry = this.copyOfRange(16, 20).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            val expire = this.copyOfRange(20, 24).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            val minimum = this.copyOfRange(24, 28).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            return SOARData(
                mname, rname, serial.toLong(), refresh.toLong(), retry.toLong(), expire.toLong(), minimum.toLong()
            )
        }
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.12">rfc1035#section-3.3.12</a>
 */
data class PTRRData(
    val ptrdname: String
) {
    companion object {
        fun ByteArray.toPTRRData(): PTRRData {
            return PTRRData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }

    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.9">rfc1035#section-3.3.9</a>
 */
data class MXRData(
    val preference: Int, val exchange: String
) {
    companion object {
        fun ByteArray.toMXRData(): MXRData {
            val preference = this.copyOfRange(0, 2).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            val exchange =
                this.copyOfRange(2, this.size).joinToString(separator = ".") { (it.toInt() and 0xff).toString() }
            return MXRData(preference.toInt(), exchange)
        }
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc1035#section-3.3.14">rfc1035#section-3.3.14</a>
 */
data class TXTRData(
    val txtData: String
) {
    companion object {
        fun ByteArray.toTXTRData(): TXTRData {
            return TXTRData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }
}


/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3596#section-2.2">rfc3596#section-2.2</a>
 */
data class AAAARData(
    val address: String
) {
    companion object {
        fun ByteArray.toAAAARData(): AAAARData {
            return AAAARData(this.joinToString(separator = ".") { (it.toInt() and 0xff).toString() })
        }
    }
}


//todo srv

/**
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6891#section-6.1.2">rfc6891#section-6.1.2</a>
 * todo not complete
 */
data class OPTRData(
    val pairs: MAP
)
