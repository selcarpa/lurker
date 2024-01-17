package service

import database.model.DomainRecord
import database.model.toDomainRecord
import io.github.oshai.kotlinlogging.KotlinLogging
import model.protocol.DnsPackage


private val logger = KotlinLogging.logger {}

fun addCache(dnsPackage: DnsPackage) {
    dnsPackage.answers.map {
        it.toDomainRecord()
    }.toList().also {
        if (it.isNotEmpty()) {
            DomainRecord.replaceBatch(
                it
            )
        }
    }

}
