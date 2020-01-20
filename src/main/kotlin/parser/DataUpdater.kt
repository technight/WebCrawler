package parser

interface DataUpdater {
    fun updateTerm(term: String, tf: Int, url: String)
    fun updateDocument(documentID: String, terms: Map<String, Int>, links: List<String>)
    fun updateShingles(documentID: String, hashedShingles: List<String>)
    fun getShingles(): List<Pair<String, List<String>>>
    
}
