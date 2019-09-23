package robot

class RobotsParser(botName: String) {
    private val userAgents = setOf(botName, "*")
    private val String.isUserAgent get() = toLowerCase().startsWith("user-agent:")
    private val String.isDisallow get() = toLowerCase().startsWith("disallow:")
    private val String.isCrawlDelay get() = toLowerCase().startsWith("crawl-delay:")
    private val String.userAgent get() = toLowerCase().replace("user-agent: ", "")
    private val String.disallowRule get() = toLowerCase().replace("disallow: ", "")
    private val String.crawlDelayRule get() = toLowerCase().replace("crawl-delay: ", "").toInt()

    private fun parseRules(lines: List<String>): Set<RobotRule> {
        val line = lines.firstOrNull() ?: return emptySet()
        return when {
            line.isUserAgent -> emptySet()
            line.isDisallow -> setOf(RobotRule.Disallow(line.disallowRule)) + parseRules(lines.drop(1))
            line.isCrawlDelay -> setOf(RobotRule.Delay(line.crawlDelayRule)) + parseRules(lines.drop(1))
            else -> parseRules(lines.drop(1))
        }
    }

    /*
    Parses lines of a robots.txt file and returns the set of paths that crawlers are not allowed to access
    */
    fun parse(lines: List<String>): Set<RobotRule> {
        val line = lines.firstOrNull()?.trim() ?: return emptySet()
        if (line.isUserAgent) {
            val userAgent = line.userAgent
            if (userAgents.contains(userAgent)) {
                return parseRules(lines.drop(1)).let { it + parse(lines.drop(1 + it.size)) }
            }
        }
        return parse(lines.drop(1))
    }
}