package parser

import com.mongodb.client.MongoCollection
import mongo.updateDocumentInCollection
import mongo.updateTermInCollection
import mongo.updateShinglesInCollection

class MongoDataUpdater(
    private val termCollection: MongoCollection<TermEntry>,
    private val documentCollection: MongoCollection<DocumentEntry>,
    private val hashedShingleCollection: MongoCollection<HashedShingleEntry>
): DataUpdater {
    override fun updateTerm(term: String, tf: Int, url: String) {
        updateTermInCollection(term, tf, url, termCollection)
    }
    override fun updateDocument(documentID: String, terms: Map<String, Int>, links: List<String>) {
        updateDocumentInCollection(documentID, terms, links, documentCollection)
    }

    override fun updateShingles(documentID: String, hashedShingles: List<String>) {
        updateShinglesInCollection(documentID, hashedShingles, hashedShingleCollection)
    }

    override fun getShingles() : List<Pair<String, List<String>>> = hashedShingleCollection.find().map {
        it.documentID to it.hashedShingles
    }.toList()
}