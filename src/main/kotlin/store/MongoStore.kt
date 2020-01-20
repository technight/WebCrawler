package store

import com.mongodb.ConnectionString
import org.bson.BsonDocument
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import java.net.URL

class MongoStore(connectionString: String): Store {
    override fun storePdf(pdf: URL) {
        print("Skipping PDF for mongostore")
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val mongoClient  = KMongo.createClient(ConnectionString(connectionString))
    private val database = mongoClient.getDatabase("html_store")
    private val collection = database.getCollection<HtmlDTO>()
    override fun storeHtml(htmlDTO: HtmlDTO) {
        if (collection.find(HtmlDTO::url eq htmlDTO.url).count() == 0) {
            collection.insertOne(htmlDTO)
        }
    }
}