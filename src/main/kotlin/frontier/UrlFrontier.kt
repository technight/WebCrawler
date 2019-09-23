package frontier

interface UrlFrontier {
    fun push(url: String)
    fun pop(): String
    val isEmpty: Boolean

}