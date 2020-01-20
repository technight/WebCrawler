package query

import com.mongodb.client.MongoCollection
import org.litote.kmongo.eq
import parser.DocumentEntry
import parser.Parser
import parser.TermEntry
import kotlin.math.log10
import kotlin.math.sqrt

class MongoCosineQuery(
    private val termCollection: MongoCollection<TermEntry>,
    private val documentCollection: MongoCollection<DocumentEntry>
) : SearchEngine {
    override fun search(query: String): List<String> {
        val documents = documentCollection.find().map { it.documentID to it }.toMap()
        val documentCount = documents.count()
        val score = mutableMapOf<String, Double>()

        val queryParts = Parser.parse(query.split(" ")).map { it.key }
        val occurrences = queryParts.groupingBy { it }.eachCount()
        val vectorLengthQ = sqrt(occurrences.toList().sumBy { it.second * it.second }.toDouble())

        queryParts.forEach { term ->
            val termEntry = termCollection.find(TermEntry::term eq term).first()
            val df = termEntry?.df ?: 0
            val normWeightq =
                ((1 + log10(occurrences.getValue(term).toDouble())) * (documentCount * df)) / vectorLengthQ
            val docs = termEntry?.docs ?: emptyMap()
            docs.forEach { doc ->
                val vectorLengthD =
                    documentCollection.find(DocumentEntry::documentID eq doc.key).first()?.length ?: 0.0
                val normWeightd = if (vectorLengthD != 0.0) doc.value / vectorLengthD else 0.0
                score[doc.key] = (score.getOrDefault(doc.key, 0.0) + (normWeightd * normWeightq))
            }
        }
        val pageRankScore = score.map { Pair(it.key, it.value * documents.getValue(it.key).pageRank) }
        return pageRankScore.sortedByDescending { it.second }.map { "Score: ${it.second} -  ${it.first}" }.take(10)
    }
}