package service

import database.model.DomainRecord
import model.protocol.DnsPackage
import model.protocol.Question
import model.protocol.RecordType

object CacheDomainService {
    fun save(dnsPackages: List<DnsPackage>) {
        val domainRecordList = dnsPackages.map {
            it.toDomainRecord()
        }.toList()
        DomainRecord.insertBatch(domainRecordList)
    }

    fun get(questions: List<Question>) {
        val domainRecordList = questions.map {
            DomainRecord.selectByNameType(it.qName, RecordType(it.qType.value))
        }.toList()
        println(domainRecordList)
    }


}

private fun DnsPackage.toDomainRecord(): DomainRecord = DomainRecord(
    name = this.question[0].qName,
    recordType = this.question[0].qType.value,
    content = this.answer[0].rData,
    ttl = this.answer[0].ttl,
    cachedDomain = true
)

