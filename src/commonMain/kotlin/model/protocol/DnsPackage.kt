@file:OptIn(ExperimentalUnsignedTypes::class)

package model.protocol

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
import utils.decodeHex
import utils.encodeHex

private val logger = KotlinLogging.logger {}

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.1">rfc1035#section-4.1.1</a>
 */
@Serializable
data class DnsPackage(
    /**
     *  A 16 bit identifier assigned by the program that
     *  generates any kind of query.  This identifier is copied
     *  the corresponding reply and can be used by the requester
     *  to match up replies to outstanding queries.
     */
    val id: String,
    /**
     *  A one bit field that specifies whether this message is a
     *  query (0), or a response (1).
     */
    val qr: Boolean,
    /**
     * A four bit field that specifies kind of query in this
     *  message.  This value is set by the originator of a query
     *  and copied into the response.  The values are:
     *
     *  0               a standard query (QUERY)
     *
     *  1               an inverse query (IQUERY)
     *
     *  2               a server status request (STATUS)
     *
     *  3-15            reserved for future use
     */
    val opcode: Int,
    /**
     * Authoritative Answer - this bit is valid in responses,
     * and specifies that the responding name server is an
     * authority for the domain name in question section.
     * Note that the contents of the answer section may have
     * multiple owner names because of aliases.  The AA bit
     * corresponds to the name which matches the query name, or
     * the first owner name in the answer section.
     */
    val aa: Boolean,
    /**
     * TrunCation - specifies that this message was truncated
     * due to length greater than that permitted on the
     * transmission channel.
     */
    val tc: Boolean,
    /**
     * Recursion Desired - this bit may be set in a query and
     * is copied into the response.  If RD is set, it directs
     * the name server to pursue the query recursively.
     * Recursive query support is optional.
     */
    val rd: Boolean,
    /**
     * Recursion Available - this be is set or cleared in a
     * response, and denotes whether recursive query support is
     * available in the name server.
     */
    val ra: Boolean,
    /**
     * Reserved for future use.  Must be zero in all queries
     * and responses.
     */
    val z: Int,
    /**
     * Response code - this 4 bit field is set as part of
     * responses. The values have the following
     * interpretation:
     *
     * 0               No error condition
     * 1               Format error - The name server was
     *                 unable to interpret the query.
     * 2               Server failure - The name server was
     *                 unable to process this query due to a
     *                 problem with the name server.
     * 3               Name Error - Meaningful only for
     *                 responses from an authoritative name
     *                 server, this code signifies that the
     *                 domain name referenced in the query does
     *                 not exist.
     * 4               Not Implemented - The name server does
     *                 not support the requested kind of query.
     * 5               Refused - The name server refuses to
     *                 perform the specified operation for
     *                 policy reasons.  For example, a name
     *                 server may not wish to provide the
     *                 information to the particular requester,
     *                 or a name server may not wish to perform
     *                 a particular operation (e.g., zone
     *                 transfer) for particular data.
     * 6-15            Reserved for future use.
     */
    val rCode: Int,
    /**
     * an unsigned 16 bit integer specifying the number of
     * entries in the question section.
     */
    val qdCount: UInt,
    /**
     * an unsigned 16 bit integer specifying the number of
     * resource records in the answer section.
     */
    val anCount: UInt,
    /**
     * an unsigned 16 bit integer specifying the number of name
     * server resource records in the authority records
     * section.
     */
    val nsCount: UInt,
    /**
     * an unsigned 16 bit integer specifying the number of
     * resource records in the additional records section.
     */
    val arCount: UInt,

    val questions: List<Question>,
    val answers: List<Resource>,
    val authorities: List<Resource>,
    val additional: List<Resource>
) {
    companion object {
        /**
         * @see <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.2>Question section format</a>
         */
        private fun parseDomainName(content: ByteArray, index: Int): Pair<String, Int> {
            var i = index
            val domainName = mutableListOf<String>()
            //https://www.rfc-editor.org/rfc/rfc1035#section-4.1.4
            if (content[i] == 0b11000000.toByte()) {
                val offset = content[index + 1].toInt()
                return Pair(parseDomainName(content, offset).first, i + 2)
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
                d.toByteArray().forEach {
                    this.writeByte(it)
                }
            }
            this.writeByte(0)
        }

        fun ByteArray.toDnsPackage(): DnsPackage {
            val id = this.copyOfRange(0, 2).encodeHex()
            val qr = this[2].toInt() and 0b10000000 != 0
            val opcode = this[2].toInt() and 0b01111000 shr 3
            val aa = this[2].toInt() and 0b00000100 != 0
            val tc = this[2].toInt() and 0b00000010 != 0
            val rd = this[2].toInt() and 0b00000001 != 0
            val ra = this[3].toInt() and 0b10000000 != 0
            val z = this[3].toInt() and 0b01110000 shr 4
            val rCode = this[3].toInt() and 0b00001111
            val qdCount = this[4].toUInt() * 256u + this[5].toUInt()
            val anCount = this[6].toUInt() * 256u + this[7].toUInt()
            val nsCount = this[8].toUInt() * 256u + this[9].toUInt()
            val arCount = this[10].toUInt() * 256u + this[11].toUInt()
            mutableListOf<Question>()
            var index = 12
            val question = IntRange(0, (qdCount - 1u).toInt()).toList().map {
                val qnPair = parseDomainName(this, index)
                val qName = qnPair.first
                index = qnPair.second
                val qType = this[index].toUInt() * 256u + this[index + 1].toUInt()
                index += 2
                val qClass = this[index].toUInt() * 256u + this[index + 1].toUInt()
                index += 2
                return@map Question(qName, RecordType.of(qType), qClass)
            }

            val answer = IntRange(0, (anCount - 1u).toInt()).toList().map {
                val resourcePair = resolveResources(index)
                index = resourcePair.second
                resourcePair.first
            }.toList()
            val authority = IntRange(0, (nsCount - 1u).toInt()).toList().map {
                val resourcePair = resolveResources(index)
                index = resourcePair.second
                resourcePair.first
            }.toList()
            val additional = IntRange(0, (arCount - 1u).toInt()).map {
                val resourcePair = resolveResources(index)
                index = resourcePair.second
                resourcePair.first
            }.toList()
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

        //fixme: offset returned is wrong
        private fun ByteArray.resolveResources(
            index: Int
        ): Pair<Resource, Int> {
            var i = index
            val rPair = parseDomainName(this, i)
            val name = rPair.first
            i = rPair.second
            val rType = this[i].toUInt() * 256u + this[i + 1].toUInt()
            i += 2
            val dClass = this[i].toUInt() * 256u + this[i + 1].toUInt()
            i += 2
            val ttl =
                this[i].toUInt() * 256u * 256u * 256u + this[i + 1].toUInt() * 256u * 256u + this[i + 2].toUInt() * 256u + this[i + 3].toUInt()
            i += 4
            val rdLength = this[i].toUInt() * 256u + this[i + 1].toUInt()
            i += 2
            val rData = this.copyOfRange(i, i + rdLength.toInt())
            i += rdLength.toInt()
            val type = RecordType.of(rType)
            return Pair(Resource(name, type, dClass, ttl, rdLength, type.rdataSovle(rData)), i)
        }


        fun DnsPackage.toByteArray(): ByteArray {
            val bytePacketBuilder = BytePacketBuilder()
            bytePacketBuilder.writeBytes(this.id.decodeHex())
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
            for (question in this.questions) {
                bytePacketBuilder.writeDomain(question.qName)
                bytePacketBuilder.writeByte(0)
                bytePacketBuilder.writeShort(question.qType.value.toShort())
                bytePacketBuilder.writeShort(question.qClass.toShort())
            }
            for (resource in this.answers) {
                resource.write(bytePacketBuilder)
            }
            for (resource in this.authorities) {
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
            bytePacketBuilder.writeUInt(this.ttl)
            bytePacketBuilder.writeShort(this.rdLength.toShort())
            bytePacketBuilder.writeBytes(this.rData.encode())
        }
    }
}

private fun BytePacketBuilder.writeBytes(bytes: ByteArray) {
    bytes.forEach {
        this.writeByte(it)
    }
}


/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.2">rfc1035#section-4.1.2</a>
 */
@Serializable
data class Question(
    val qName: String, val qType: RecordType, val qClass: UInt
)

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.3">rfc1035#section-4.1.3</a>
 */
@Serializable
class Resource(
    val rName: String, val rType: RecordType, val rClass: UInt, val ttl: UInt, val rdLength: UInt, val rData: RData
)
