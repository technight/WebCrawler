package store

import org.jsoup.nodes.Document
import java.io.*
import java.net.URI
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import javax.print.DocFlavor
import javax.swing.text.html.HTML


class DiskStore(private val storeLocation : String): Store {
    override fun storePdf(pdf: URL) {
        File(storeLocation).mkdir()
        val file = File("$storeLocation/${pdf.path}.pdf")
        val stream = pdf.openStream()
        val content = stream.bufferedReader().use(BufferedReader::readText)
        file.writeText(content)
    }

    override fun storeHtml(htmlDTO: HtmlDTO) {
        File(storeLocation).mkdir()
        val encodedUrl = getDomain((htmlDTO.url))
        val file = File("$storeLocation/$encodedUrl.json")
        //val htmlobject = HtmlDTO(url, Date.from(Instant.now()),"", Base64.getEncoder().encodeToString(html.toByteArray()))
        file.writeText(htmlDTO.toJson())
    }

    private fun getDomain(url: String): String {
        val uri = URI(url)
        return uri.host.toString() + uri.rawPath.toString().replace("/",".")
    }

}