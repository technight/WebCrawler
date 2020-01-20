package parser
import ndd.NearDuplicateDetection
import opennlp.tools.stemmer.snowball.SnowballStemmer
import org.jsoup.Jsoup
import parser.queue.QueueHandler
import store.HtmlDTO
import utils.formatLink
import utils.getHyperLinks
import java.io.File

class Parser(private val queue: QueueHandler, private val dataUpdater: DataUpdater, private val times: Int) {
    companion object {
        fun String.stem(stemmer: SnowballStemmer) = stemmer.stem(this).toString()

        private val stopWordsListEng = File("stopwordlists/stopwords_english.txt").let {
            if (it.exists()) it.readLines()
            else emptyList()
        }
        private val stopWordsListDan = File("stopwordlists/stopwords_danish.txt").let {
            if (it.exists()) it.readLines()
            else emptyList()
        }

        private fun removeStopwords(tokens: List<String>): List<String>{
            val mappedTokens = tokens.map {
                it.toLowerCase().replace(Regex("""[^A-Za-z0-9æøå]+"""), "")
            }
            return mappedTokens.filter { it !in stopWordsListEng && it !in stopWordsListDan && it != "" }
        }
        private val engStemmer = SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH)
        private val daneStemmer = SnowballStemmer(SnowballStemmer.ALGORITHM.DANISH)
        fun parse(tokens: List<String>): Map<String, Int> {
            val newTokens = removeStopwords(tokens)
            return newTokens.map { it.stem(engStemmer).stem(daneStemmer) }.groupingBy { it }.eachCount()
        }
    }

    private fun getTextFromHTML(html: HtmlDTO, count: Int): List<String> {
        println("${count+1}: ${html.url}")
        val parsedHtml = Jsoup.parse(html.html)
        val textNodes = parsedHtml.select("p,h1,h2,h3,h4,h5,h6")
        return textNodes.flatMap { it.text().split(" ") }
    }

    fun startParsing() {
        repeat(times) {
            val htmlDTO = queue.popFromQueue() ?: return
            val tokens = getTextFromHTML(htmlDTO, it)
            val stemmedTokens = parse(tokens)
            val links = getHyperLinks(htmlDTO.html).map { formatLink(htmlDTO.url, it) }
            val minHashes = NearDuplicateDetection.minHashes(tokens.joinToString(" "))
            if (isNotDuplicate(minHashes, htmlDTO.url)) {
                dataUpdater.updateDocument(htmlDTO.url, stemmedTokens, links)
                dataUpdater.updateShingles(htmlDTO.url, minHashes)
                stemmedTokens.forEach { dataUpdater.updateTerm(it.key, it.value, htmlDTO.url) }
            }
        }
    }

    private fun isNotDuplicate(minHashes: List<String>, documentId: String) = dataUpdater.getShingles().asSequence()
        .filter { it.first != documentId }
        .map {
            NearDuplicateDetection.isNearDuplicate(it.second, minHashes)
    }.all { !it }
}