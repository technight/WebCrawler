import java.lang.Exception
import java.time.LocalDateTime
import java.util.PriorityQueue
import kotlin.IndexOutOfBoundsException as IndexOutOfBoundsException1


class UrlFrontier(private val queues: List<PriorityQueue<Crawledurls>>): IQueue {
/*
* Returns a list of the pushed URLS
* */
    override fun push(urls: List<Crawledurls>): List<Crawledurls> {
        queues.random().addAll(urls)
        return urls
    }

    override fun pop(index: Int): Crawledurls? {
        if(queues[index].isEmpty()) return null
        else return queues[index].remove()
    }
/*
* This one doesnt do what I want.
* */
    override fun deleteBeforeStoredTime(beforeTime: LocalDateTime): Int {
        return queues.map { if(it.element().timestamp.isBefore(beforeTime)) it.remove() }.size
    }

}