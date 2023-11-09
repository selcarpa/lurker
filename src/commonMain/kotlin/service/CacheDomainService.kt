package service

import database.model.DomainRecord
import io.github.oshai.kotlinlogging.KotlinLogging
import model.protocol.DnsPackage
import model.protocol.Question
import model.protocol.RecordType

private val logger = KotlinLogging.logger {}

object CacheDomainService {
    fun save(dnsPackages: List<DnsPackage>) {
        val domainRecordList = dnsPackages.map {
            it.toDomainRecord()
        }.toList()
        DomainRecord.insertBatch(domainRecordList)
    }

    fun get(question: Question) = DomainRecord.selectByNameType(question.qName, RecordType(question.qType.value))

}

private fun DnsPackage.toDomainRecord(): DomainRecord = DomainRecord(
    name = this.question[0].qName,
    recordType = this.question[0].qType.value,
    content = this.answer[0].rData,
    ttl = this.answer[0].ttl,
    cachedDomain = true
)

