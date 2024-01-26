package model.protocol

import kotlinx.serialization.Serializable

@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
@Serializable
class RecordType(val value: UInt, val name: String?) {
    constructor(value: UInt) : this(value, null)

    companion object {
        val A = RecordType(1u, "A")
        val NS = RecordType(2u, "NS")
        val CNAME = RecordType(5u, "CNAME")
        val SOA = RecordType(6u, "SOA")
        val PTR = RecordType(12u, "PTR")
        val MX = RecordType(15u, "MX")
        val TXT = RecordType(16u, "TXT")
        val AAAA = RecordType(28u, "AAAA")
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
