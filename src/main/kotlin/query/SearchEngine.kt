package query

interface SearchEngine {
    fun search(query: String): List<String>
}