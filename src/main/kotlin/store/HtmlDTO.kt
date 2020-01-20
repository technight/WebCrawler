package store

import com.fasterxml.jackson.module.kotlin.*
import java.util.*


data class HtmlDTO(val url: String, val date: Date, val addedBy : String, val html : String){
    fun toJson(): String {
        return jacksonObjectMapper().writeValueAsString(this)
    }
}