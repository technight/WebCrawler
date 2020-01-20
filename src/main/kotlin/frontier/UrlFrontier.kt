package frontier

/**
 * A URL frontier
 * Must have a push function, a pop and a check for if the frontier is empty.
 */
interface UrlFrontier {
    fun push(queueitem: QueueEntityDTO)
    fun pop(): QueueEntityDTO
    val isEmpty: Boolean

}