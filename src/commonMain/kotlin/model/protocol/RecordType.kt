package model.protocol

@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
class RecordType(val value: Int, val name: String?) {
    constructor(value: Int) : this(value, null)

    companion object {
        val A = RecordType(1, "A")
        val NS = RecordType(2, "NS")
        val CNAME = RecordType(5, "CNAME")
        val SOA = RecordType(6, "SOA")
        val PTR = RecordType(12, "PTR")
        val MX = RecordType(15, "MX")
        val TXT = RecordType(16, "TXT")
        val AAAA = RecordType(28, "AAAA")
        val SRV = RecordType(33, "SRV")
        val OPT = RecordType(41, "OPT")
        val ANY = RecordType(255, "ANY")

        fun of(value: Int): RecordType {
            return when (value) {
                1 -> A
                2 -> NS
                5 -> CNAME
                6 -> SOA
                12 -> PTR
                15 -> MX
                16 -> TXT
                28 -> AAAA
                33 -> SRV
                41 -> OPT
                255 -> ANY
                else -> RecordType(value)
            }
        }

    }

    override fun toString(): String {
        return "TYPE(value=$value, name='${name ?: "UNKNOWN"}}')"
    }

}
