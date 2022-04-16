package Database;
import static com.mongodb.client.model.Filters.eq;

import com.mongodb.MongoException;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.types.ObjectId;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Controller {
    //------to reduce console log-----
    static Logger root = (Logger) LoggerFactory
            .getLogger(Logger.ROOT_LOGGER_NAME);

    static {
        root.setLevel(Level.INFO);
    }
    //------------------------------------
    public void ControllerTest() {
        // write your code here
        // Replace the uri string with your MongoDB deployment's connection string
        //connection string
        String uri = "mongodb://localhost:27017";
        //create connection
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            //connect to db
            MongoDatabase database = mongoClient.getDatabase("todo-app");
            //get collection
            MongoCollection<Document> collection = database.getCollection("todos");
            //insert example
            try {
                InsertOneResult result = collection.insertOne(new Document()
                        .append("_id", new ObjectId())
                        .append("item", "added from java"));
                System.out.println("Success! Inserted document id: " + result.getInsertedId());
            } catch (MongoException me) {
                System.err.println("Unable to insert due to an error: " + me);
            }
            //find example
            Document doc = collection.find(eq("item", "buy eggs")).first();
//            System.out.println(doc.toJson());
        }
    }
}
