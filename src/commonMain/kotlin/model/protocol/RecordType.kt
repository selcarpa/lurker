package model.protocol

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import model.protocol.AAAARData.Companion.toAAAARData
import model.protocol.ARData.Companion.toARData
import model.protocol.CNAMERData.Companion.toCNAMERData
import model.protocol.HexRData.Companion.toHexRData
import model.protocol.MXRData.Companion.toMXRData
import model.protocol.NSRData.Companion.toNSRData
import model.protocol.PTRRData.Companion.toPTRRData
import model.protocol.SOARData.Companion.toSOARData
import model.protocol.TXTRData.Companion.toTXTRData

@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
@Serializable
class RecordType(
    val value: UInt, val name: String?, @Transient var rdataSovle: (ByteArray) -> RData = { it.toHexRData() }
) {
    constructor(value: UInt) : this(value, null)

    companion object {
        val A = RecordType(1u, "A") { it.toARData() }
        val NS = RecordType(2u, "NS") { it.toNSRData() }
        val CNAME = RecordType(5u, "CNAME") { it.toCNAMERData() }
        val SOA = RecordType(6u, "SOA")
        val PTR = RecordType(12u, "PTR") { it.toPTRRData() }
        val MX = RecordType(15u, "MX") { it.toMXRData() }
        val TXT = RecordType(16u, "TXT") { it.toTXTRData() }
        val AAAA = RecordType(28u, "AAAA") { it.toAAAARData() }
        val SRV = RecordType(33u, "SRV")

        //https://datatracker.ietf.org/doc/html/rfc6891
        val OPT = RecordType(41u, "OPT")
        val ANY = RecordType(255u, "ANY")

        fun of(value: UInt): RecordType {
            return when (value) {
                1u -> A
                2u -> NS
                5u -> CNAME
                6u -> SOA
                12u -> PTR
                15u -> MX
                16u -> TXT
                28u -> AAAA
                33u -> SRV
                41u -> OPT
                255u -> ANY
                else -> RecordType(value)
            }
        }

    }

    override fun toString(): String {
        return "TYPE(value=$value, name=${name ?: "UNKNOWN"})"
    }

}
