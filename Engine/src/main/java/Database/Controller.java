package Database;
//MONGODB
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.*;
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
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mongodb.client.MongoCursor;
//UTILITY
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.BitSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
//FROM Crawler
import Crawler.Crawler.URLQueue;
//From Indexer
import Indexer.Indexer.triple;


public class Controller {
    String uri;
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> queueCollection;
    MongoCollection<Document> dataCollection;
    MongoCollection<Document> WebsiteDataCollection;
    MongoCollection<Document> indexerCollection;

    //------to reduce console log-----
    static Logger root = (Logger) LoggerFactory
            .getLogger(Logger.ROOT_LOGGER_NAME);

    static {
        root.setLevel(Level.INFO);
    }
    public Controller(boolean connect){
        if (connect){
            uri = "mongodb://localhost:27017";
            mongoClient = MongoClients.create(uri);
            //connect to db
            database = mongoClient.getDatabase("SearchEngine");
            //---------CRAWLER COLLECTIONS----------
            queueCollection = database.getCollection("queue");
            dataCollection = database.getCollection("CollectedData");
            WebsiteDataCollection = database.getCollection("WebsiteData");
            //---------INDEXER COLLECTIONS----------NOT USED YET
            indexerCollection = database.getCollection("Indexed_documents");
        }
    }
    //----------------------------------------
    //-----------CRAWLER FUNCTIONS------------
    //----------------------------------------
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
    //----------------------------------------
    //-----------INDEXER FUNCTIONS------------
    //----------------------------------------
    public static long convert(BitSet bits) {
        long value = 0L;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }
    public void indexerone(HashMap<String,HashMap<String,triple>>mymap)
    {//mongodb+srv://doaa:mbm@cluster0.zu6vd.mongodb.net/myData?retryWrites=true&w=majority
//        String uri = "mongodb://localhost:27017/SearchEngine";
//        MongoClient mongo = MongoClients.create(uri);
//        MongoDatabase database = mongo.getDatabase("myData");
//        //get collection
//        MongoCollection<org.bson.Document> collection = database.getCollection("Indexed_documents");

        Document doc=new Document();
        for(Map.Entry<String, HashMap<String,triple>> entry : mymap.entrySet())
        {
            doc.append("_id", new ObjectId());
            doc.append("Word",entry.getKey());

            List<BasicDBObject>lis=new ArrayList<BasicDBObject>();
            for(Map.Entry<String,triple>subentry:entry.getValue().entrySet())
            {
                BasicDBObject obj = new BasicDBObject();
                obj.append("URL",subentry.getKey());
                obj.append("Count",subentry.getValue().count);
                long pos = convert(subentry.getValue().location);
                obj.append("locations",pos);
                obj.append("positions",subentry.getValue().positions);
                lis.add(obj);
            }
            doc.append("Websites",lis);

            indexerCollection.insertOne(doc);

        }

    }
    public void insertIndexedWord(String word,HashMap <String,triple> siteMap)
    {//mongodb+srv://doaa:mbm@cluster0.zu6vd.mongodb.net/myData?retryWrites=true&w=majority
//        String uri = "mongodb://localhost:27017/SearchEngine";
//        MongoClient mongo = MongoClients.create(uri);
//        MongoDatabase database = mongo.getDatabase("myData");
//        //get collection
//        MongoCollection<org.bson.Document> collection = database.getCollection("Indexed_documents");
        try {


            Document doc = new Document();
            doc.append("_id", new ObjectId());
            doc.append("Word", word);

            List<BasicDBObject> lis = new ArrayList<BasicDBObject>();
            for (Map.Entry<String, triple> subentry : siteMap.entrySet()) {
                BasicDBObject obj = new BasicDBObject();
                obj.append("URL", subentry.getKey());
                obj.append("Count", subentry.getValue().count);
                long pos = convert(subentry.getValue().location);
                obj.append("locations", pos);
                obj.append("positions", subentry.getValue().positions);
                lis.add(obj);
            }
            doc.append("Websites", lis);
            indexerCollection.insertOne(doc);
        }
        catch (org.bson.BsonMaximumSizeExceededException e){
        }
    }
    public void UpdateVisitedInCollectedData(String url, boolean visited)
    {
        Document query = new Document().append("url",  url);
        try {
            dataCollection.updateOne(query, Updates.set("visited", visited));
        } catch (MongoException me) {
            System.err.println("Unable to update site in collected data due to an error: " + me);
        }
    }

    //----------------------------------------
    //-------------UTILITY CLASSES------------
    //----------------------------------------

    //class to specify the structure of HTML (Collected data)
    public static class Data{
        public String url;
        public boolean visited;
        public String html;
        public Data(String url, boolean visited, String html){
            this.url = url;
            this.visited = visited;
            this.html = html;
        }
    }
}
