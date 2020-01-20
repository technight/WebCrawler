package crawler

/**
 * Interface for a crawler.
 * Must only implement crawl
 */
interface Crawler {
    fun crawl(url: String)
}