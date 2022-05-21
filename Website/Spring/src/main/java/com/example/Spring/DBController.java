package com.example.Spring;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

public class DBController {
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
    public DBController(){
        uri = "mongodb://localhost:27017";
        mongoClient = MongoClients.create(uri);
        //connect to db
        database = mongoClient.getDatabase("SearchEngine");
        collectedDataCollection = database.getCollection("CollectedData");
        websiteDataCollection = database.getCollection("WebsiteData");
        indexedDocumentsCollection = database.getCollection("Indexed_documents");
        suggestionsCollection = database.getCollection("sugg-test");
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
        String patternStr = "^" + query;
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
    public ArrayList<Document> SortDocuments(ArrayList<Document> data){
        MongoCollection<Document> sortCollection = database.getCollection("sort");
        ArrayList<Document> result = null;
        sortCollection.deleteMany(new Document());
        if (!data.isEmpty())
        {
            sortCollection.insertMany(data);
            try (MongoCursor<Document> cursor = sortCollection.find().sort(Sorts.descending("rank")).iterator()){
                if(cursor.hasNext()) {
                    result = new ArrayList<>();
                    while (cursor.hasNext()) {
                        Document doc = cursor.next();
                        result.add(doc);
                    }
                }
//            for (Document doc : result) {
//                System.out.println(doc.getString("url"));
//                System.out.println(doc.getDouble("rank")+"\n\n");
//            }
            } catch (MongoException me) {
                System.err.println("Unable to sort data due to an error: " + me);
            }

        }
        return result;
    }
}
