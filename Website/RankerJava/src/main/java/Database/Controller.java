package Database;
//MONGODB
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
//to Reduce log
//import org.slf4j.LoggerFactory;
//import ch.qos.logback.classic.Level;
//import ch.qos.logback.classic.Logger;
import com.mongodb.client.MongoCursor;
//UTILITY
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Controller {
    String uri;
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collectedDataCollection;
    MongoCollection<Document> websiteDataCollection;
    MongoCollection<Document> indexedDocumentsCollection;
    MongoCollection<Document> suggestionsCollection;

    //------to reduce console log-----
//    static Logger root = (Logger) LoggerFactory
//            .getLogger(Logger.ROOT_LOGGER_NAME);
//
//    static {
//        root.setLevel(Level.INFO);
//    }
    public Controller(){
        uri = "mongodb://localhost:27017";
        mongoClient = MongoClients.create(uri);
        //connect to db
        database = mongoClient.getDatabase("SearchEngine");
        collectedDataCollection = database.getCollection("CollectedData");
        websiteDataCollection = database.getCollection("WebsiteData");
        indexedDocumentsCollection = database.getCollection("Indexed_documents");
        suggestionsCollection = database.getCollection("suggestions");
    }
    public Document GetWordFromIndexer(String word){
        return indexedDocumentsCollection.find(eq("Word", word)).first();
    }
    public Document GetSiteFromCollectedData(String url){
        return collectedDataCollection.find(eq("url", url)).first();
    }
    public Document GetSiteFromWebsiteData(String url){
        return websiteDataCollection.find(eq("url", url)).first();
    }
    public List<String> GetSuggestions(String query){
        String patternStr = "." + query + ".";
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("title", pattern);
        List<String> suggestions = null;
        //get an iterator to result
        try (MongoCursor<Document> cursor = suggestionsCollection.find(filter).iterator()){
            if (cursor.hasNext()) {
                suggestions = new ArrayList<>();
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    suggestions.add(doc.getString("title"));
                }
            }
//            for (URLQueue queue : urlQueue) {
//                System.out.println(queue.url);
//            }
        } catch (MongoException me) {
            System.err.println("Unable to find queue due to an error: " + me);
        }
        return suggestions;
    }
    public void InsertOrUpdateSuggestion(String suggestion){
        List<String> suggestions = GetSuggestions(suggestion);
        try {
            if (suggestions == null) {
                suggestionsCollection.insertOne(new Document()
                        .append("_id", new ObjectId())
                        .append("title", suggestion)
                        .append("counter", 1));
            }
            else{
                Document query = new Document().append("title",  suggestion);
                BasicDBObject incValue = new BasicDBObject("counter", 1);
                BasicDBObject intModifier = new BasicDBObject("$inc", incValue);
                suggestionsCollection.updateOne(query, intModifier);

            }
        } catch (MongoException me) {
            System.err.println("Unable to increment suggestion due to an error: " + me);
        }
    }
}
