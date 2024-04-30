package model.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import model.protocol.AAAARData.Companion.toAAAARData
import model.protocol.ARData.Companion.toARData
import model.protocol.CNAMERData.Companion.toCNAMERData
import model.protocol.HexRData.Companion.toHexRData
import model.protocol.MXRData.Companion.toMXRData
import model.protocol.NSRData.Companion.toNSRData
import model.protocol.PTRRData.Companion.toPTRRData
import model.protocol.TXTRData.Companion.toTXTRData

@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
@Serializable
enum class RecordType(
    val value: UShort,
    @Transient var rdataResolve: (ByteArray) -> RData = { it.toHexRData() }
) {
    UNUSED(0u),
    A(1u, { it.toARData() }),
    NS(2u, { it.toNSRData() }),
    CNAME(5u, { it.toCNAMERData() }),
    SOA(6u),
    PTR(12u, { it.toPTRRData() }),
    MX(15u, { it.toMXRData() }),
    TXT(16u, { it.toTXTRData() }),
    AAAA(28u, { it.toAAAARData() }),
    SRV(33u),

    //https://datatracker.ietf.org/doc/html/rfc6891
    OPT(41u),
    ANY(255u);

    companion object {
        fun of(value: UShort): RecordType {
            return entries.find { it.value == value } ?: UNUSED
        }
    }


    override fun toString(): String {
        return "TYPE(value=$value, name=${name})"
    }

}
