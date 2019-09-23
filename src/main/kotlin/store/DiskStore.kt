package store

import java.io.File
import java.util.*

class DiskStore: Store {
    override fun storeHtml(html: String, url: String) {
        File("html_pages").mkdir()
        val encodedUrl = Base64.getEncoder().encodeToString(url.toByteArray()).replace("/", "-")
        val file = File("html_pages/$encodedUrl.html")
        file.writeText(html)
    }
}