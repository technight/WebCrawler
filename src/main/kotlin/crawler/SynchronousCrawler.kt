package crawler

import frontier.UrlFrontier
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import robot.RobotDecision
import robot.RobotsCrawler
import store.Store
import java.net.MalformedURLException
import java.net.URL

class SynchronousCrawler(
    private val store: Store,
    private val robotsCrawler: RobotsCrawler,
    private val frontier: UrlFrontier
) :
    Crawler {

    override fun crawl(url: String) {
        println("Initializing with seed $url")
        frontier.push(url)
        while (!frontier.isEmpty) {
            val page = frontier.pop()
            println("Crawling page $page")
            handlePageDecision(page)
        }
    }

    private fun getHyperLinks(document: Document) =
        document.select("a").map { col -> col.attr("href") }


    private fun handlePageDecision(url: String) {
        when (val decision = robotsCrawler.crawlDecision(url)) {
            RobotDecision.NoCrawl -> println("Not allowed to crawl $url")
            RobotDecision.CanCrawl -> handlePage(url)
            is RobotDecision.CrawlDelayed -> {
                println("Delaying crawl of $url by ${decision.delay}")
                Thread.sleep(decision.delay * 1000L)
                handlePage(url)
            }
        }
    }

    private fun formatLink(url: String, link: String): URL {
        return URL(if (link.startsWith("/")) URL(url).let { "${it.protocol}://${it.host}" } + link else link)
    }

    private fun handlePage(url: String) {
        println("Fetching content of $url")
        val html = try {
            Jsoup.connect(url).get()
        } catch (e: HttpStatusException) {
            println("Error in url $url")
            return
        }
        val links = getHyperLinks(html)
        println("Adding links to frontier")
        links.forEach { link ->
            try {
                val urlLink = formatLink(url, link)
                if (urlLink.protocol.startsWith("http")) {
                    frontier.push(urlLink.toExternalForm()!!)
                } else {
                    println("Unknown host: ${urlLink.protocol}")
                }
            } catch (e: MalformedURLException) {
                println("Found malformed url ${e.message}")
            }
        }
        println("Storing content to disk")
        store.storeHtml(html.root().toString(), url)
    }
}