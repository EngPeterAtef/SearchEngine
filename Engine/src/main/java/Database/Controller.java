package Database;
import static com.mongodb.client.model.Filters.eq;

import com.mongodb.*;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mongodb.client.MongoCursor;
import Crawler.Crawler.URLQueue;
import Crawler.Crawler.Data;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    String uri;
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> queueCollection;
    MongoCollection<Document> dataCollection;
    MongoCollection<Document> WebsiteDataCollection;

    public Controller(){
        uri = "mongodb://localhost:27017";
        mongoClient = MongoClients.create(uri);
        //connect to db
        database = mongoClient.getDatabase("SearchEngine");
        //get collection
        queueCollection = database.getCollection("queue");
        dataCollection = database.getCollection("CollectedData");
        WebsiteDataCollection = database.getCollection("WebsiteData");

    }
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
    //----------ADD SITE BODY TO DB-----------
    public void AddSiteData(String url, String title,String body){
        //insert
        try {
            InsertOneResult result = WebsiteDataCollection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("url", url)
                    .append("title", title)
                    .append("popularity", 1)
                    .append("body", body));
//            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert site to WebsiteData due to an error: " + me);
        }
    }
    public void IncrementPopularity(String url){
        //get collection
        //update
        try {
            Document query = new Document().append("url",  url);
            BasicDBObject incValue = new BasicDBObject("popularity", 1);
            BasicDBObject intModifier = new BasicDBObject("$inc", incValue);
            WebsiteDataCollection.updateOne(query, intModifier);
        } catch (MongoException me) {
            System.err.println("Unable to increment popularity due to an error: " + me);
        }
    }
    public List<URLQueue> GetUrlQueue(){
        //get collection
        List<URLQueue> urlQueue = null;
        //get an iterator to result
        try (MongoCursor<Document> cursor = queueCollection.find().sort(new Document().append("_id", 1)).iterator()){
            if(cursor.hasNext()) {
                urlQueue = new ArrayList<URLQueue>();
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    urlQueue.add(new URLQueue(doc.getString("url"), doc.getBoolean("visited"), doc.getInteger("thread"), doc.getString("normalization")));
                }
            }
//            for (URLQueue queue : urlQueue) {
//                System.out.println(queue.url);
//            }
        } catch (MongoException me) {
            System.err.println("Unable to find queue due to an error: " + me);
        }
        return urlQueue;
    }
    public List<Data> GetCollectedData(){
        //get collection
        List<Data> collectedData = null;
        //get an iterator to result
        try (MongoCursor<Document> cursor = dataCollection.find().iterator()){
            if (cursor.hasNext()) {
                collectedData = new ArrayList<Data>();
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    collectedData.add(new Data(doc.getString("url"), doc.getBoolean("visited"), doc.getString("html")));
                }
            }
//            for (URLQueue queue : urlQueue) {
//                System.out.println(queue.url);
//            }
        } catch (MongoException me) {
            System.err.println("Unable to find queue due to an error: " + me);
        }
        return collectedData;
    }
    public void AddToQueue(URLQueue obj)
    {
        //insert
        try {
            InsertOneResult result = queueCollection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("url", obj.url)
                    .append("visited", false)
                    .append("thread", obj.threadID)
                    .append("normalization", ""), new InsertOneOptions());
//            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert site to queue due to an error: " + me);
        }
    }
    public void RemoveFromQueue(URLQueue obj)
    {
        //insert
        try {
            queueCollection.deleteOne(eq("url", obj.url));
//            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to remove site from queue due to an error: " + me);
        }
    }
    public void UpdateQueue(URLQueue obj)
    {
        Document query = new Document().append("url",  obj.url);
        Bson updates = Updates.combine(
                Updates.set("visited", obj.visited),
                Updates.set("normalization", obj.normalization));
        try {
            queueCollection.updateOne(query, updates);
        } catch (MongoException me) {
            System.err.println("Unable to update site in queue due to an error: " + me);
        }
    }
    public void AddToCollectedData(Data obj)
    {
        //insert
        try {
            InsertOneResult result = dataCollection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("url", obj.url)
                    .append("visited", obj.visited)
                    .append("html", obj.html));
//            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert site to collected data due to an error: " + me);
        }
    }
}
