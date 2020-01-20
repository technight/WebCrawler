import crawler.SynchronousCrawler
import frontier.SimpleFrontier
import parser.Parser
import robot.RobotsCrawler
import store.DiskStore
import com.fasterxml.jackson.module.kotlin.*
import mongo.getDocumentCount
import parser.queue.MongoQueue
import java.io.File
import store.MongoStore
import mongo.getMongoCollection
import pageranker.MongoPageRanker
import parser.DocumentEntry
import parser.MongoDataUpdater
import parser.TermEntry
import query.MongoCosineQuery
import store.HtmlDTO


fun main() {
    print("(c)rawl, (p)arse, (s)earch, (r)ank, (purge) or HTML cou(n)t\n> ")
    val r = readLine()
    val config = readFromConfig()
    when(r?.trim()) {
        "p" -> parse(config)
        "purge" -> purge(config)
        "c" -> crawl(config)
        "r" -> rank(config)
        "n" -> println("${getDocumentCount<HtmlDTO>(config.ConnectionString)} documents in database")
        "s" -> query(config)
        else -> println("That is not a recognized command!!!!!!!!!!!!!!")
    }
}

fun purge(config: ConfigObject) {
    getMongoCollection<DocumentEntry>(config.ConnectionString).drop()
    getMongoCollection<TermEntry>(config.ConnectionString).drop()
}

fun rank(config: ConfigObject) {
    val pageRanker = MongoPageRanker(getMongoCollection(config.ConnectionString))
    pageRanker.rankPages()
}

fun query(config: ConfigObject) {
    println("Initializing search... Write 'q' to quit.")
    val searchEngine = MongoCosineQuery(
        getMongoCollection(config.ConnectionString),
        getMongoCollection(config.ConnectionString)
    )
    while (true) {
        print("> ")
        val q = readLine()
        if (q == null || q == "q") break
        searchEngine.search(q).forEachIndexed { idx, url -> println("${ idx + 1 }: $url") }
    }
}

fun parse(config : ConfigObject) {
    val queue = MongoQueue(config.ConnectionString)
    val parser = Parser(queue,
        MongoDataUpdater(
            getMongoCollection(config.ConnectionString),
            getMongoCollection(config.ConnectionString),
            getMongoCollection(config.ConnectionString)
        ), config.TimesToParse)
    parser.startParsing()
}

fun crawl(config : ConfigObject) {
    val store = DiskStore(config.HTMLStorePath)
    val robotsCrawler = RobotsCrawler("SatomiBot")
    val frontier = SimpleFrontier(config.pagesToCrawl)
    print("Crawl using (d)isk or (m)ongo: ")
    val r = readLine()
    when(r?.trim()) {
        "d" -> { val crawler = SynchronousCrawler(store, robotsCrawler, frontier); crawler.crawl(config.seedURL) }
        "m" -> {
            val mongoStore = MongoStore(config.ConnectionString)
            val crawler = SynchronousCrawler(mongoStore, robotsCrawler, frontier)
            crawler.crawl(config.seedURL) }
    }
}

fun readFromConfig(): ConfigObject {
    val mapper = jacksonObjectMapper()
    return mapper.readValue(File("./config/config.json"))
}
data class ConfigObject(val pagesToCrawl: Int, val seedURL : String, val HTMLStorePath : String, val ConnectionString : String, val TimesToParse: Int) {

}