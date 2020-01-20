package parser.queue

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import store.HtmlDTO
import java.io.File


class DiskQueue(private val path: String, private val deleteOnRead: Boolean = true): QueueHandler {
    private val mapper = jacksonObjectMapper()

    override fun popFromQueue() = File(path).listFiles()?.firstOrNull()?.let{ file ->
        mapper.readValue<HtmlDTO>(file.readText()).also { if (deleteOnRead) file.delete() }
    }
}