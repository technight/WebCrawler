import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.ext.asString
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.Jsoup

class Crawler(val store: Store) {
    fun getRobot(url: String): String {
        val response: Response = httpGet {
            host = url
            path = "/robots.txt"
        }
        return response.asString() ?: "No Robots.txt"
    }

    fun getRobots(urls: List<String>) = urls.map { getRobot(it) }

    fun getHyperLinks(document: Document) =
        document.select("a").map { col -> col.attr("href") }

    fun crawl(url: String) {
        val html = Jsoup.connect(url).get()
        val links = getHyperLinks(html)
        store.storeHtml(html.root().toString(), url)
    }
}