package utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document


fun getHyperLinks(document: Document) = document.select("a").map { col -> col.attr("href") }
fun getHyperLinks(document: String) = getHyperLinks(Jsoup.parse(document))

