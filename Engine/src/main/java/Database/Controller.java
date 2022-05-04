  package Database;
  import static com.mongodb.client.model.Filters.eq;

import Indexer.Indexer.pair;
  import com.mongodb.BasicDBObject;
  import com.mongodb.MongoException;
 import org.bson.Document;
 import com.mongodb.client.MongoClient;

 import com.mongodb.client.MongoClients;
 import com.mongodb.client.MongoCollection;
 import com.mongodb.client.MongoDatabase;
 import com.mongodb.client.result.InsertOneResult;
 import org.bson.types.ObjectId;

  import java.util.ArrayList;
  import java.util.List;
  import java.util.Map;
  import java.util.BitSet;
 import org.slf4j.LoggerFactory;
 import ch.qos.logback.classic.Level;
 import ch.qos.logback.classic.Logger;

  import java.util.HashMap;
  import java.util.stream.Collectors;
  import java.util.stream.IntStream;

  public class Controller {
     //------to reduce console log-----
     static Logger root = (Logger) LoggerFactory
             .getLogger(Logger.ROOT_LOGGER_NAME);

     static {
         root.setLevel(Level.INFO);
     }
     //------------------------------------
     public static long convert(BitSet bits) {
         long value = 0L;
         for (int i = 0; i < bits.length(); ++i) {
             value += bits.get(i) ? (1L << i) : 0L;
         }
         return value;
     }
     public static void indexerone(HashMap<String,HashMap<String,pair>>mymap)
     {//mongodb+srv://doaa:mbm@cluster0.zu6vd.mongodb.net/myData?retryWrites=true&w=majority
            String uri = "mongodb://localhost:27017/SearchEngine";
            MongoClient mongo = MongoClients.create(uri);
            MongoDatabase database = mongo.getDatabase("myData");
            //get collection
            MongoCollection<org.bson.Document> collection = database.getCollection("Indexed_documents");

            Document doc=new Document();
         for(Map.Entry<String, HashMap<String,pair>> entry : mymap.entrySet())
         {
             doc.append("_id", new ObjectId());
             doc.append("Word",entry.getKey());
            // System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
           //  Document subdoc=new Document();
             List<BasicDBObject>lis=new ArrayList<BasicDBObject>();
             for(Map.Entry<String,pair>subentry:entry.getValue().entrySet())
               {
                   BasicDBObject obj = new BasicDBObject();
                   obj.append("URL",subentry.getKey());
                   obj.append("Count",subentry.getValue().count);
                   long pos = convert(subentry.getValue().location);
                   obj.append("locations",pos);
                   lis.add(obj);
               }
            doc.append("Websites",lis);
             //System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
           //  doc.append("name","baji keisku");
             collection.insertOne(doc);
             System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
         }

     }







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
