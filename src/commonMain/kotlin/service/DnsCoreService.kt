package service

import database.model.DomainRecord
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import model.protocol.Question

fun resolve(questions: List<Question>) = runBlocking {
    //domain records from cache, it may contain multiple records of the same domain and type, in current version, we just choose the first one
    //TODO: choose the best one
    val resolved = mutableListOf<DomainRecord>()
    val questTaskMap = questions.map {
        "${it.qName}${it.qType.value}" to async {
            DomainRecord.selectByNameType(it.qName, it.qType).firstOrNull()
        }
    }.toMap()
    //divide into resolved and unresolved domain records
    val unResolved = questTaskMap.filter {
        val domainRecord = it.value.await()
        if (domainRecord != null) {
            resolved.add(domainRecord)
            return@filter false
        }
        return@filter true

    }
    unResolved

}
