package utils

import java.net.URL

fun formatLink(url: String, link: String): String {
    return if (link.startsWith("/")) URL(url).let { "${it.protocol}://${it.host}" } + link else link
}
