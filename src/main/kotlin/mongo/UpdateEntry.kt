package mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import parser.DocumentEntry
import parser.HashedShingleEntry
import parser.TermEntry
import kotlin.math.log10
import kotlin.math.sqrt

infix fun String.withTf(tf: Int) = mapOf(this to 1 + log10(tf.toDouble()))

fun updateTermInCollection(term: String, tf: Int, url: String, mongo: MongoCollection<TermEntry>) {
    val currentEntry = mongo.findOne(TermEntry::term eq term)
    if (currentEntry == null) {
        mongo.insertOne(TermEntry(term, 1, url withTf tf))
    } else {
        val newEntry = currentEntry.copy(df = currentEntry.df + 1, docs = currentEntry.docs + (url withTf tf))
        mongo.replaceOne(TermEntry::term eq term, newEntry)
    }
}

fun updateDocumentInCollection(
    documentId: String,
    terms: Map<String, Int>,
    links: List<String>,
    mongo: MongoCollection<DocumentEntry>
) {
    val vectorLength = sqrt(terms.toList().sumBy { it.second * it.second }.toDouble()) // <-- also known as magic
                                                                                       // Kvadratroden af alle forekomster af ord
    mongo.replaceOne(
        DocumentEntry::documentID eq documentId,
        DocumentEntry(documentId, vectorLength, 0.0, links),
        ReplaceOptions().upsert(true)
    )
}

fun updateShinglesInCollection(
    documentId: String,
    hashedShingles: List<String>,
    mongo: MongoCollection<HashedShingleEntry>
) {
    mongo.replaceOne(
        HashedShingleEntry::documentID eq documentId,
        HashedShingleEntry(documentId, hashedShingles),
        ReplaceOptions().upsert(true)
    )
}
