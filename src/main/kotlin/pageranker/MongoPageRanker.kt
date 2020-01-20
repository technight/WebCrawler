package pageranker

import com.mongodb.client.MongoCollection
import koma.extensions.set
import koma.extensions.get
import koma.fill
import koma.matrix.Matrix
import koma.zeros
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import parser.DocumentEntry


class MongoPageRanker(private val mongoCollection: MongoCollection<DocumentEntry>, private val alpha: Double = 0.10) :
    PageRanker {
    override fun rankPages() {
        val documents = mongoCollection.find().toList()
        val docsLength = documents.count()
        val q = qZeroVector(docsLength)
        val p = zeros(docsLength, docsLength)

        (0 until docsLength).forEach { rowNumber ->
            val links = documents[rowNumber].links.toSet()

            val linkCount = (0 until docsLength).sumBy { colNumber ->
                 if (links.contains(documents[colNumber].documentID)) {
                     1
                 } else 0
            }
            val probSplit = if (linkCount == 0) 0.0 else 1 / linkCount.toDouble()
            (0 until docsLength).forEach { colNumber ->
                if (links.contains(documents[colNumber].documentID)){
                    p[rowNumber, colNumber] = probSplit
                }
            }
        }
        val pPageRank = p * (1 - alpha) + fill(docsLength, docsLength) { _, _ -> 1 / docsLength.toDouble() } * alpha
        val pageRank = pageRankIterations(q, pPageRank, 20)
        updatePageRankInDatabase(documents, pageRank)
    }

    private fun qZeroVector(docsLength: Int): Matrix<Double> = zeros(rows = 1, cols = docsLength).also { it[0, 0] = 1 }

    private fun pageRankIterations(q: Matrix<Double>, p: Matrix<Double>, iterations: Int): Matrix<Double> =
        if (iterations <= 1) pageRankIteration(q, p)
        else pageRankIterations(pageRankIteration(q, p), p, iterations - 1)
    private fun pageRankIteration(q: Matrix<Double>, p: Matrix<Double>) = q * p

    private fun updatePageRankInDatabase(documentList: List<DocumentEntry>, pageRank: Matrix<Double>) {
        documentList.forEachIndexed { index, document ->
            mongoCollection.updateOne(
                DocumentEntry::documentID eq document.documentID,
                setValue(DocumentEntry::pageRank, pageRank[0, index])
            )
        }
    }
}