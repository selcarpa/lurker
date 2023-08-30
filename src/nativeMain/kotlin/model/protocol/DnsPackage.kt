package model.protocol

import io.ktor.utils.io.core.*

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.1">rfc1035#section-4.1.1</a>
 */
data class DnsPackage(
    val ID: Int,
    val QR: Boolean,
    val OPCODE: Int,
    val AA: Boolean,
    val TC: Boolean,
    val RD: Boolean,
    val RA: Boolean,
    val Z: Int,
    val RCODE: Int,
    val QDCOUNT: Int,
    val ANCOUNT: Int,
    val NSCOUNT: Int,
    val ARCOUNT: Int,
    val question: List<Question>,
    val answer: List<Resource>,
    val authority: List<Resource>,
    val additional: List<Resource>
) {
    companion object {
        private fun parseDomainName(content: ByteArray, index: Int): Pair<String, Int> {
            var i = index
            val domainName = mutableListOf<String>()
            if (content[i] == 0b11000000.toByte()) {
                i += 2
                val offseted = content[index + 1].toInt()
                return Pair(parseDomainName(content, offseted).first, i)
            }
            while (content[i].toInt() != 0) {
                val length = content[i].toInt()
                val domain = String(content.copyOfRange(i + 1, i + 1 + length))
                domainName.add(domain)
                i += length + 1
            }
            return Pair(domainName.joinToString("."), i + 1)
        }

        private fun BytePacketBuilder.writeDomain(domain: String) {
            val domainSegments = domain.split(".")
            for (d in domainSegments) {
                this.writeByte(d.length.toByte())
                this.writeByte(d.toByte())
            }
            this.writeByte(0)
        }

        fun ByteArray.toDnsPackage(): DnsPackage {
            val ID = this[0].toInt() * 256 + this[1].toInt()
            val QR = this[2].toInt() and 0b10000000 != 0
            val OPCODE = this[2].toInt() and 0b01111000 shr 3
            val AA = this[2].toInt() and 0b00000100 != 0
            val TC = this[2].toInt() and 0b00000010 != 0
            val RD = this[2].toInt() and 0b00000001 != 0
            val RA = this[3].toInt() and 0b10000000 != 0
            val Z = this[3].toInt() and 0b01110000 shr 4
            val RCODE = this[3].toInt() and 0b00001111
            val QDCOUNT = this[4].toInt() * 256 + this[5].toInt()
            val ANCOUNT = this[6].toInt() * 256 + this[7].toInt()
            val NSCOUNT = this[8].toInt() * 256 + this[9].toInt()
            val ARCOUNT = this[10].toInt() * 256 + this[11].toInt()
            val question = mutableListOf<Question>()
            var index = 12
            for (i in 0 until QDCOUNT) {
                val qnPair = parseDomainName(this, index)
                val QNAME = qnPair.first
                index = qnPair.second
                val QTYPE = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val QCLASS = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                question.add(Question(QNAME, TYPE.of(QTYPE), QCLASS))
            }
            val answer = mutableListOf<Resource>()
            for (i in 0 until ANCOUNT) {
                val rPair = parseDomainName(this, index)
                val NAME = rPair.first
                index = rPair.second
                val tTYPE = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val CLASS = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val TTL =
                    this[index].toInt() * 256 * 256 * 256 + this[index + 1].toInt() * 256 * 256 + this[index + 2].toInt() * 256 + this[index + 3].toInt()
                index += 4
                val RDLENGTH = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val RDATA = this.copyOfRange(index, index + RDLENGTH).contentToString()
                answer.add(Resource(NAME, TYPE.of(tTYPE), CLASS, TTL, RDLENGTH, RDATA))
            }
            val authority = mutableListOf<Resource>()
            for (i in 0 until NSCOUNT) {
                val rPair = parseDomainName(this, index)
                val NAME = rPair.first
                index = rPair.second
                val tTYPE = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val CLASS = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val TTL =
                    this[index].toInt() * 256 * 256 * 256 + this[index + 1].toInt() * 256 * 256 + this[index + 2].toInt() * 256 + this[index + 3].toInt()
                index += 4
                val RDLENGTH = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val RDATA = this.copyOfRange(index, index + RDLENGTH).contentToString()
                authority.add(Resource(NAME, TYPE.of(tTYPE), CLASS, TTL, RDLENGTH, RDATA))
            }
            val additional = mutableListOf<Resource>()
            for (i in 0 until ARCOUNT) {
                val rPair = parseDomainName(this, index)
                val NAME = rPair.first
                index = rPair.second
                val tTYPE = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val CLASS = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val TTL =
                    this[index].toInt() * 256 * 256 * 256 + this[index + 1].toInt() * 256 * 256 + this[index + 2].toInt() * 256 + this[index + 3].toInt()
                index += 4
                val RDLENGTH = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val RDATA = this.copyOfRange(index, index + RDLENGTH).contentToString()
                authority.add(Resource(NAME, TYPE.of(tTYPE), CLASS, TTL, RDLENGTH, RDATA))
            }
            return DnsPackage(
                ID,
                QR,
                OPCODE,
                AA,
                TC,
                RD,
                RA,
                Z,
                RCODE,
                QDCOUNT,
                ANCOUNT,
                NSCOUNT,
                ARCOUNT,
                question,
                answer,
                authority,
                additional
            )
        }


        fun DnsPackage.toByteArray(): ByteArray {
            val bytePacketBuilder = BytePacketBuilder()
            bytePacketBuilder.writeShort(this.ID.toShort())
            var flags = 0
            if (this.QR) flags += 0b1000000000000000
            flags += this.OPCODE shl 11
            if (this.AA) flags += 0b0000010000000000
            if (this.TC) flags += 0b0000001000000000
            if (this.RD) flags += 0b0000000100000000
            if (this.RA) flags += 0b0000000010000000
            flags += this.Z shl 4
            flags += this.RCODE
            bytePacketBuilder.writeShort(flags.toShort())
            bytePacketBuilder.writeShort(this.QDCOUNT.toShort())
            bytePacketBuilder.writeShort(this.ANCOUNT.toShort())
            bytePacketBuilder.writeShort(this.NSCOUNT.toShort())
            bytePacketBuilder.writeShort(this.ARCOUNT.toShort())
            for (question in this.question) {
                bytePacketBuilder.writeDomain(question.QNAME)
                bytePacketBuilder.writeByte(0)
                bytePacketBuilder.writeShort(question.QTYPE.value.toShort())
                bytePacketBuilder.writeShort(question.QCLASS.toShort())
            }
            for (resource in this.answer) {
                bytePacketBuilder.writeShort(resource.TYPE.value.toShort())
                bytePacketBuilder.writeShort(resource.CLASS.toShort())
                bytePacketBuilder.writeInt(resource.TTL)
                bytePacketBuilder.writeShort(resource.RDLENGTH.toShort())
                bytePacketBuilder.writeRDATA(resource.RDATA, resource.TYPE)
            }
            for (resource in this.authority) {
                bytePacketBuilder.writeShort(resource.TYPE.value.toShort())
                bytePacketBuilder.writeShort(resource.CLASS.toShort())
                bytePacketBuilder.writeInt(resource.TTL)
                bytePacketBuilder.writeShort(resource.RDLENGTH.toShort())
                bytePacketBuilder.writeRDATA(resource.RDATA, resource.TYPE)
            }
            for (resource in this.additional) {
                bytePacketBuilder.writeShort(resource.TYPE.value.toShort())
                bytePacketBuilder.writeShort(resource.CLASS.toShort())
                bytePacketBuilder.writeInt(resource.TTL)
                bytePacketBuilder.writeShort(resource.RDLENGTH.toShort())
                bytePacketBuilder.writeRDATA(resource.RDATA, resource.TYPE)
            }
            return bytePacketBuilder.build().readBytes()
        }
    }
}

private fun BytePacketBuilder.writeRDATA(rdata: String, type: TYPE) {
    when (type) {
        TYPE.A -> {
            val rdataSegments = rdata.split(".").map { it.toInt().toByte() }
            for (r in rdataSegments) {
                this.writeByte(r)
            }
        }

        TYPE.AAAA -> {
            val rdataSegments = rdata.split(":").map { it.toInt().toByte() }
            for (r in rdataSegments) {
                this.writeByte(r)
            }
        }

        else -> {
            TODO("Not implemented")
        }
    }
}


/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.2">rfc1035#section-4.1.2</a>
 */
data class Question(
    val QNAME: String,
    val QTYPE: TYPE,
    val QCLASS: Int
)

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.3">rfc1035#section-4.1.3</a>
 */
data class Resource(
    val NAME: String,
    val TYPE: TYPE,
    val CLASS: Int,
    val TTL: Int,
    val RDLENGTH: Int,
    val RDATA: String
)

class TYPE(val value: Int, val name: String?) {
    constructor(value: Int) : this(value, null)

    companion object {
        val A = TYPE(1, "A")
        val NS = TYPE(2, "NS")
        val CNAME = TYPE(5, "CNAME")
        val SOA = TYPE(6, "SOA")
        val PTR = TYPE(12, "PTR")
        val MX = TYPE(15, "MX")
        val TXT = TYPE(16, "TXT")
        val AAAA = TYPE(28, "AAAA")
        val SRV = TYPE(33, "SRV")
        val OPT = TYPE(41, "OPT")
        val ANY = TYPE(255, "ANY")

        fun of(value: Int): TYPE {
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
                else -> TYPE(value)
            }
        }

    }

    override fun toString(): String {
        return "TYPE(value=$value, name='${name ?: "UNKNOWN"}}')"
    }

}
