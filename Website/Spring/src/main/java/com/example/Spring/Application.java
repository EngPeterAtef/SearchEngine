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
	String[] ArrayOfquery;
	int c;
	String query;
//	String queryString;
	PorterStemmer p = new PorterStemmer();
	ArrayList<Document> matchingArray = new ArrayList<Document>();
	ArrayList<Document> dataArray = new ArrayList<Document>();
	@RestController
	public class SpringController {
		//----------SAVE SUGGESTION IN DATABASE-----------
		@RequestMapping(value="/search", method = RequestMethod.GET)
		void SaveSuggestion(@RequestParam(value = "query") String query) {
			dbController.InsertOrUpdateSuggestion(query);
		}
		//------------SEARCHING-----------
		@RequestMapping(value="/results/{query}", method = RequestMethod.GET)
		String search(@PathVariable(value = "query") String query) {
			ArrayOfquery = query.split(" ");
			c = 0;
			dataArray.clear();
			matchingArray.clear();
			FindAndRank(query);
			String ResultsPage = "<!doctype html> <html><body align=\"center\">"+ dataArray +"</body></html>";
			return ResultsPage;
//			ModelAndView modelAndView = new ModelAndView();
//			modelAndView.setViewName("result.html");
//			return modelAndView;
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
	public void FindAndRank(String queryString) {
		ArrayList<Document> resultArray = new ArrayList<Document>();

		System.out.println("Original query" + queryString);
		for (int i = 0; i < ArrayOfquery.length; i++) {
			//porter stemmer
			query = p.stem(ArrayOfquery[i].replaceAll("\"", "")).toLowerCase();
			System.out.println("Query after stemming " + query);
			//database
			resultArray.add(dbController.GetWordFromIndexer(query));
		}

		//resultArray[0][0] contains object of 1st word
		//resultArray[1][0] contains object of 2nd word
		//resultArray[2][0] contains object of 3rd word
		if (queryString.charAt(0) == '"' && queryString.charAt(queryString.length() - 1) == '"') {
			String queryString_2 = queryString.replaceAll("\"", "");
			for (int j = 0; j < resultArray.get(0).getList("Websites", Document.class).size(); j++) // loop on each url in the first word
			{
				Document siteResult = dbController.GetSiteFromCollectedData(resultArray.get(0).getList("Websites", Document.class).get(j).getString("URL"));
				if (siteResult != null) {
					int indx = siteResult.getString("html").indexOf(queryString_2);
					int phraseCount = 0;
					if (indx != -1) {
						int startIndex = indx;
						phraseCount++;

						while ((indx = siteResult.getString("html").indexOf(queryString_2, startIndex)) > -1) {
							phraseCount++;
							startIndex = indx + queryString_2.length();
						}
						matchingArray.add(new Document().append("url", resultArray.get(0).getList("Websites", Document.class).get(j).getString("URL")).append("phraseCount", phraseCount));
					}
				}
			}
			double IDF = Math.log(5000.0 / matchingArray.size());
			double TF = 0;
			double TF_IDF = 0;
			//get all the websites that has this word
			for (int index = 0; index < matchingArray.size(); index++) {
				Document siteResult = dbController.GetSiteFromWebsiteData(matchingArray.get(index).getString("url"));
				if (siteResult != null) {
					String str = siteResult.getString("body");
					for (int i = 0; i < ArrayOfquery.length; i++) {
						for (int j = 0; j < resultArray.get(i).getList("Websites", Document.class).size(); j++) {
							if (matchingArray.get(index).getString("url").equals(resultArray.get(i).getList("Websites", Document.class).get(j).getString("URL"))) {
								TF = resultArray.get(i).getList("Websites", Document.class).get(j).getInteger("Count") / (double) str.length();
								TF_IDF += TF * IDF;
//								System.out.println(matchingArray.get(index).getString("url") + " " + TF_IDF);
							}
						}
					}
					// let str = siteResult[0].body;
					int indexOFquery = str.indexOf(query);
					//resultArray[i][0].Websites[index].title = siteResult[0].title;
					//resultArray[i][0].Websites[index].snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length));
					dataArray.add(new Document().append("url", matchingArray.get(index).getString("url")).append("title", siteResult.getString("title")).append("snippet", str.substring(Math.max(0, indexOFquery - 200), Math.min(indexOFquery + query.length() + 300, str.length()))));
					//TF = resultArray[i][0].Websites[index].locations / str.length;
					//TF_IDF = TF * IDF;
					dataArray.set(c, dataArray.get(c).append("rank", TF_IDF + siteResult.getInteger("popularity") + matchingArray.get(index).getInteger("phraseCount")));
					c++;
				}
			}
		}
	}
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
