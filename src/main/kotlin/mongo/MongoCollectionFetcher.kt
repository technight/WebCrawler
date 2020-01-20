package mongo

import com.mongodb.ConnectionString
import com.mongodb.client.MongoCollection
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import store.HtmlDTO

inline fun<reified T: Any> getMongoCollection(connectionString: String): MongoCollection<T> {
    val mongoClient = KMongo.createClient(ConnectionString(connectionString))
    val database = mongoClient.getDatabase("html_store")
    return database.getCollection()
}
inline fun<reified T: Any> getDocumentCount(connectionString: String): Int{
    return getMongoCollection<T>(connectionString).find().count()
}
