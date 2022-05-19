package com.example.Spring;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import opennlp.tools.stemmer.PorterStemmer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;
@SpringBootApplication
public class Application {

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
	}
	Controller dbController = new Controller();
	String ArrayOfquery;
	int c;
	@RestController
	public class SpringController {
		//------------SEARCHING-----------
		@RequestMapping(value="/search", method = RequestMethod.GET)
		public String search(@RequestParam(value = "query") String query) {
//			ArrayOfquery = query.split(" ");
//			c =0;
//			FindAndRank();

			String ResultsPage = "<!doctype html> <html><body align=\"center\">"+ query +"</body></html>";
			return ResultsPage;
		}
		//------------SUGGESTIONS------------
		@RequestMapping(value="/search", method = RequestMethod.POST)
		public List<String> suggest(@RequestParam(value = "query") String query) {
			return dbController.GetSuggestions(query);
		}
		//---------DISPLAY SEARCH PAGE-------
		@RequestMapping(value="/")
		ModelAndView index() {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("search.html");
			return modelAndView;
		}

	}
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
