package store

import java.net.URL

interface Store {
    fun storeHtml(htmlDTO: HtmlDTO)
    fun storePdf(pdf: URL)
}