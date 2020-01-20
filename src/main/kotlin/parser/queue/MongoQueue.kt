package parser.queue

import mongo.getMongoCollection
import org.litote.kmongo.findOneAndDelete
import store.HtmlDTO

class MongoQueue(connectionString: String): QueueHandler {
    private val collection = getMongoCollection<HtmlDTO>(connectionString)
    override fun popFromQueue() = collection.findOneAndDelete("{\"date\":{\"\$lte\":new Date()}}") // Vi tager en som er ældre en nuværende
}