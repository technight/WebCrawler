package frontier

import java.time.LocalDateTime
import java.util.*

/**
 * A data class containing a url as a string, a timestamp for an added day, and an addedBy which is an url
 */
data class QueueEntityDTO(val url: String, val date: Date, val addedBy : String)