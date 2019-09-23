package frontier

import java.util.concurrent.atomic.AtomicInteger

class SimpleFrontier: UrlFrontier {
    private val atomicInteger = AtomicInteger()
    override val isEmpty: Boolean
        get() = queue.isEmpty()

    private val queue = mutableSetOf<String>()

    override fun push(url: String) {
        if (atomicInteger.incrementAndGet() < 1000) {
            queue.add(url)
        }
    }
    override fun pop() = queue.random().also { queue.remove(it) }
}