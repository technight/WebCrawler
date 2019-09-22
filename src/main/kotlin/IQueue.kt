import java.time.LocalDateTime

interface IQueue {
    fun pop (index: Int): Crawledurls?
    fun push (urls: List<Crawledurls>): List<Crawledurls>
    fun deleteBeforeStoredTime(beforeTime: LocalDateTime): Int
}