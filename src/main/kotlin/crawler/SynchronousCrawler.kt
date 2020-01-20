package crawler

import frontier.UrlFrontier

import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import robot.RobotDecision
import robot.RobotsCrawler
import store.Store
import store.HtmlDTO
import java.net.MalformedURLException
import java.net.URL
import java.time.Instant
import java.util.*
import frontier.QueueEntityDTO
import org.jsoup.UnsupportedMimeTypeException
import utils.formatLink
import utils.getHyperLinks
import java.io.FileNotFoundException
import java.lang.Exception
import javax.net.ssl.SSLHandshakeException

/**
 * Synchronous crawler implementation
 * @constructor Instantiates a synchronous crawler implementation, which crawls the web using an initiated frontier (usually only with one url
 * @param store, designating how to store the crawled pages
 * @param robotsCrawler, a crawler for robots.txt files, to determine how we are allowed to crawl a certain webpage
 * @param frontier, an instantiated urlfrontier, containing Url(s) to crawl
 */
class SynchronousCrawler(
    private val store: Store,
    private val robotsCrawler: RobotsCrawler,
    private val frontier: UrlFrontier
) :
    Crawler {
    /**
     * Function to crawl a given url. Will crawl based on the given seed
     * Simply adds the seed to the frontier, and keeps crawling while the frontier is non-empty.
     * @param url, a seed url, which will be added to the frontier, and crawled with
     */
    override fun crawl(url: String) {
        println("Initializing with seed $url")
        frontier.push(QueueEntityDTO(url, Date.from(Instant.now()), "Seed"))
        while (!frontier.isEmpty) {
            val page = frontier.pop()
            println("Crawling page $page")
            try {
                handlePageDecision(page)
            } catch (e: Exception){
                println("coult not crawl ${page}")
            }
        }
    }

    /**
     * Determines how we are allowed to crawl a given URL, based on a sites robots.txt file
     * @param  entity, a queueEntityDTO, which is a data object containg a url, a timestamp and an added by url
     */
    private fun handlePageDecision(entity: QueueEntityDTO) {
        when (val decision = robotsCrawler.crawlDecision(entity.url)) {
            RobotDecision.NoCrawl -> println("Not allowed to crawl $entity.url")
            RobotDecision.CanCrawl -> handlePage(entity)
            is RobotDecision.CrawlDelayed -> {
                println("Delaying crawl of $entity.url by ${decision.delay}")
                Thread.sleep(decision.delay * 1000L)
                handlePage(entity)
            }
        }
    }

    /**
     * Handles a page, extracting raw html, while ignoring errors, and other files types
     * Then extracts all hyperlinks, adding http urls to the frontier using linkToFrontier.
     * Finally it stores the current pages content on the store.
     * Additionally it will also store pdf's if it is running to a local store.
     * @param  entity, a queueEntityDTO, which is a data object containg a url, a timestamp and an added by
     */
    private fun handlePage(entity: QueueEntityDTO) {
        println("Fetching content of ${entity.url}")
        val html = try {
            Jsoup.connect(entity.url).get()
        } catch (e: HttpStatusException) {
            println("Error in url ${entity.url}")
            return
        } catch (e: UnsupportedMimeTypeException){
            println("Found unsupported datatype ${e.message}")
            return
        } catch (e: Exception){
            println(e.message)
            return
        }
        val links = getHyperLinks(html).map { it.dropLastWhile { it == '/' } }
        println("Adding links to frontier")
        links.forEach { link ->
            linkToFrontier(link, entity)
        }
        println("Storing content")
        val htmldto = HtmlDTO(entity.url, Date.from(Instant.now()), entity.addedBy, html.root().toString())
        store.storeHtml(htmldto)
    }

    /**
     * adds a link to the frontier for a given link
     * This is run for every link on a page.
     * @param link, a link as a string. will be a partial link?
     * @param entity,  a queueEntityDTO, which is a data object containing a url, a timestamp and an added by,
     *                  describes the page that contains the link
     */
    private fun linkToFrontier(link: String, entity: QueueEntityDTO) {
        try {
            if (link.contains(".pdf")) {
                store.storePdf(URL(link))
            }
            val urlLink = URL(formatLink(entity.url, link))
            if (urlLink.protocol.startsWith("http")) {
                frontier.push(QueueEntityDTO(urlLink.toExternalForm(), Date.from((Instant.now())), entity.url))
            } else {
                println("Unknown host: ${urlLink.protocol}")
            }
        } catch (e: MalformedURLException) {
            println("Found malformed url ${e.message}")
        } catch (e: UnsupportedMimeTypeException) {
            println("Found unsupported mimetype: ${e.message}")
        } catch (e: FileNotFoundException) {
            println("File not found: ${e.message}")
        }
    }
}