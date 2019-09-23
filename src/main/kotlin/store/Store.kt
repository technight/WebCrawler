package store

interface Store {
    fun storeHtml(html: String, url: String)
}