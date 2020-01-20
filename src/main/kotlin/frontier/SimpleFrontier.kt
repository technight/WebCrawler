package frontier

import org.litote.kmongo.eq
import store.HtmlDTO
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 *
 */
class SimpleFrontier(private val numberOfPagesToCrawl : Int): UrlFrontier {
    private val atomicInteger = AtomicInteger()
    override val isEmpty: Boolean
        get() = queue.isEmpty()

    private val queue = mutableSetOf<QueueEntityDTO>()

    override fun push(queueitem: QueueEntityDTO) {
        if (atomicInteger.incrementAndGet() < numberOfPagesToCrawl) {
            queue.add(queueitem)
        }
    }
    override fun pop(): QueueEntityDTO {
        val group = queue.groupBy { URI(it.url).host }
        val groupWithUnnormalizedChance = group.map { it.key to 1 / it.value.size.toDouble() }
        val total = groupWithUnnormalizedChance.fold(0.0) { acc, pair ->  acc + pair.second }
        val groupWithNormalizedChance = groupWithUnnormalizedChance.map { it.first to it.second / total }
        val chance = Random.nextDouble()
        var ittChance = 0.0
        val host = groupWithNormalizedChance.first {
            ittChance += it.second
            ittChance > chance
        }.first
        val element = group[host]!!.random()
        queue.remove(element)
        return element
    }
}