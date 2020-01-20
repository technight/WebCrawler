package parser

data class DocumentEntry(val documentID: String, val length: Double, val pageRank: Double, val links : List<String>)