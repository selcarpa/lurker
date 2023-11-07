package model.protocol

import io.ktor.utils.io.core.*

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.1">rfc1035#section-4.1.1</a>
 */
data class DnsPackage(
    val id: Int,
    val qr: Boolean,
    val opcode: Int,
    val aa: Boolean,
    val tc: Boolean,
    val rd: Boolean,
    val ra: Boolean,
    val z: Int,
    val rCode: Int,
    val qdCount: Int,
    val anCount: Int,
    val nsCount: Int,
    val arCount: Int,
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
                val offset = content[index + 1].toInt()
                return Pair(parseDomainName(content, offset).first, i)
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
            val id = this[0].toInt() * 256 + this[1].toInt()
            val qr = this[2].toInt() and 0b10000000 != 0
            val opcode = this[2].toInt() and 0b01111000 shr 3
            val aa = this[2].toInt() and 0b00000100 != 0
            val tc = this[2].toInt() and 0b00000010 != 0
            val rd = this[2].toInt() and 0b00000001 != 0
            val ra = this[3].toInt() and 0b10000000 != 0
            val z = this[3].toInt() and 0b01110000 shr 4
            val rCode = this[3].toInt() and 0b00001111
            val qdCount = this[4].toInt() * 256 + this[5].toInt()
            val anCount = this[6].toInt() * 256 + this[7].toInt()
            val nsCount = this[8].toInt() * 256 + this[9].toInt()
            val arCount = this[10].toInt() * 256 + this[11].toInt()
            val question = mutableListOf<Question>()
            var index = 12
            for (i in 0 until qdCount) {
                val qnPair = parseDomainName(this, index)
                val qName = qnPair.first
                index = qnPair.second
                val qType = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                val qClass = this[index].toInt() * 256 + this[index + 1].toInt()
                index += 2
                question.add(Question(qName, RecordType.of(qType), qClass))
            }
            val answer = mutableListOf<Resource>()
            for (i in 0 until anCount) {
                index = resolveResources(index, answer)
            }
            val authority = mutableListOf<Resource>()
            for (i in 0 until nsCount) {
                index = resolveResources(index, authority)

            }
            val additional = mutableListOf<Resource>()
            for (i in 0 until arCount) {
                resolveResources(index, additional)
            }
            return DnsPackage(
                id,
                qr,
                opcode,
                aa,
                tc,
                rd,
                ra,
                z,
                rCode,
                qdCount,
                anCount,
                nsCount,
                arCount,
                question,
                answer,
                authority,
                additional
            )
        }

        private fun ByteArray.resolveResources(
            index: Int,
            answer: MutableList<Resource>
        ): Int {
            var index1 = index
            val rPair = parseDomainName(this, index1)
            val name = rPair.first
            index1 = rPair.second
            val rType = this[index1].toInt() * 256 + this[index1 + 1].toInt()
            index1 += 2
            val dClass = this[index1].toInt() * 256 + this[index1 + 1].toInt()
            index1 += 2
            val ttl =
                this[index1].toInt() * 256 * 256 * 256 + this[index1 + 1].toInt() * 256 * 256 + this[index1 + 2].toInt() * 256 + this[index1 + 3].toInt()
            index1 += 4
            val rdLength = this[index1].toInt() * 256 + this[index1 + 1].toInt()
            index1 += 2
            val rData = this.copyOfRange(index1, index1 + rdLength).contentToString()
            answer.add(Resource(name, RecordType.of(rType), dClass, ttl, rdLength, rData))
            return index1
        }


        fun DnsPackage.toByteArray(): ByteArray {
            val bytePacketBuilder = BytePacketBuilder()
            bytePacketBuilder.writeShort(this.id.toShort())
            var flags = 0
            if (this.qr) flags += 0b1000000000000000
            flags += this.opcode shl 11
            if (this.aa) flags += 0b0000010000000000
            if (this.tc) flags += 0b0000001000000000
            if (this.rd) flags += 0b0000000100000000
            if (this.ra) flags += 0b0000000010000000
            flags += this.z shl 4
            flags += this.rCode
            bytePacketBuilder.writeShort(flags.toShort())
            bytePacketBuilder.writeShort(this.qdCount.toShort())
            bytePacketBuilder.writeShort(this.anCount.toShort())
            bytePacketBuilder.writeShort(this.nsCount.toShort())
            bytePacketBuilder.writeShort(this.arCount.toShort())
            for (question in this.question) {
                bytePacketBuilder.writeDomain(question.qName)
                bytePacketBuilder.writeByte(0)
                bytePacketBuilder.writeShort(question.qType.value.toShort())
                bytePacketBuilder.writeShort(question.qClass.toShort())
            }
            for (resource in this.answer) {
                resource.write(bytePacketBuilder)
            }
            for (resource in this.authority) {
                resource.write(bytePacketBuilder)
            }
            for (resource in this.additional) {
                resource.write(bytePacketBuilder)
            }
            return bytePacketBuilder.build().readBytes()
        }

        private fun Resource.write(bytePacketBuilder: BytePacketBuilder) {
            bytePacketBuilder.writeShort(this.rType.value.toShort())
            bytePacketBuilder.writeShort(this.rClass.toShort())
            bytePacketBuilder.writeInt(this.ttl)
            bytePacketBuilder.writeShort(this.rdLength.toShort())
            bytePacketBuilder.writeRDATA(this.rData, this.rType)
        }
    }
}

private fun BytePacketBuilder.writeRDATA(rdata: String, type: RecordType) {
    when (type) {
        RecordType.A -> {
            val rdataSegments = rdata.split(".").map { it.toInt().toByte() }
            for (r in rdataSegments) {
                this.writeByte(r)
            }
        }

        RecordType.AAAA -> {
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
    val qName: String,
    val qType: RecordType,
    val qClass: Int
)

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.3">rfc1035#section-4.1.3</a>
 */
data class Resource(
    val name: String,
    val rType: RecordType,
    val rClass: Int,
    val ttl: Int,
    val rdLength: Int,
    val rData: String
)

