import java.io.File

class DiskStore: Store {
    override fun storeHtml(html: String, url: String) {
        println(url)
        val newUrl = url.replace('/', '-')
        val file = File("html_pages/$newUrl")
        file.createNewFile()
        file.writeText(html)
    }
}