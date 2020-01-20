package robot

import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.ext.asString
import okhttp3.Response
import java.net.URL
import java.time.Instant

data class CacheEntry(val ts: Instant, val content: Set<RobotRule>)

class RobotsCrawler(botName: String) {
    private val parser = RobotsParser(botName)
    private val robotsCache = mutableMapOf<String,  CacheEntry>()

    private fun getRobotRules(baseUrl: String): Set<RobotRule> {
        val response: Response = httpGet {
            host = baseUrl
            path = "/robots.txt"
        }
        return response.asString()?.lines()?.let { parser.parse(it) } ?: emptySet()
    }

    private fun updateRobotRules(baseUrl: String) {
        robotsCache[baseUrl] = CacheEntry(Instant.now(), getRobotRules(baseUrl))
    }

    private val String.baseUrl get() = URL(this).host

    fun crawlDecision(url: String): RobotDecision {
        val baseUrl = url.baseUrl
        if (robotsCache[baseUrl]?.ts?.isBefore(Instant.now().minusSeconds(300)) != false) {
            println("$baseUrl not found in cache, or entry was too old so updating cache entry")
            updateRobotRules(baseUrl)
        }
        return robotDecisionFromRule(url, robotsCache[baseUrl]!!.content)
    }

    private fun robotDecisionFromRule(url: String, rules: Set<RobotRule>): RobotDecision {
        if (rules.all { it is RobotRule.Delay || (it is RobotRule.Disallow && !url.contains(it.path)) }) {
            val delay = rules.find { it is RobotRule.Delay } as? RobotRule.Delay ?: return RobotDecision.CanCrawl
            if (delay.delay > 10) {
                return RobotDecision.NoCrawl
            }
            return RobotDecision.CrawlDelayed(delay.delay)
        }
        return RobotDecision.NoCrawl
    }
}
