import java.util.PriorityQueue
import java.time.LocalDateTime

data class Crawledurls(val url: String, val timestamp: LocalDateTime)


class UrlFrontier {
    val p1 = PriorityQueue<Crawledurls>();
    fun InitPriorityQueues(){

    }
}