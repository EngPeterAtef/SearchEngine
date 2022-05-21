package com.example.Spring;

import opennlp.tools.stemmer.PorterStemmer;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class SpringController {
    String[] ArrayOfquery;
    int c;
    String query;
    int btnNumber = 0;
    //	String queryString;
    PorterStemmer p = new PorterStemmer();
    ArrayList<Document> matchingArray = new ArrayList<Document>();
    ArrayList<Document> dataArray = new ArrayList<Document>();
    DBController dbController = new DBController();
    //----------SAVE SUGGESTION IN DATABASE-----------
    @RequestMapping(value="/search", method = RequestMethod.GET)
    @ResponseBody
    public void SaveSuggestion(Model model,@RequestParam(value = "query") String query) {
        dbController.InsertOrUpdateSuggestion(query);
    }
    //------------SEARCHING-----------
    @RequestMapping(value="/results/{query}", method = RequestMethod.GET)
    public String search(Model model, @PathVariable(value = "query") String query) {
        long startTime = System.nanoTime();
        ArrayOfquery = query.split(" ");
        c = 0;
        btnNumber = 0;
        dataArray.clear();
        matchingArray.clear();
        FindAndRank(query);
        dataArray = dbController.SortDocuments(dataArray);
        if(dataArray != null)
        {
            btnNumber = (int) Math.ceil(dataArray.size()/10.0);
        }
        else
        {
            dataArray = new ArrayList<>();
        }
        long endTime   = System.nanoTime();
        float totalTime = (float) ((endTime-startTime)/1000000000.0);
        DecimalFormat df = new DecimalFormat("#.###");
        ;
//        String ResultsPage = "<!doctype html> <html><body align=\"center\">"+ dataArray +"</body></html>";
        model.addAttribute("query", query);
        model.addAttribute("results", dataArray);
        model.addAttribute("time",Float.parseFloat(df.format(totalTime))  );
        model.addAttribute("btns",btnNumber );
        return "results";
//			ModelAndView modelAndView = new ModelAndView();
//			modelAndView.setViewName("results.html");
//            modelAndView.addObject("results", dataArray);
//			return modelAndView;
//			return "redirect:/results/" + query;
    }

    //------------SUGGESTIONS------------
    @RequestMapping(value="/search", method = RequestMethod.POST)
    @ResponseBody
    public List<String> suggest(@RequestParam(value = "query") String query) {
        return dbController.GetSuggestions(query);
    }
    //---------DISPLAY SEARCH PAGE-------
    @GetMapping(value="/")
    public String index() {
        return "search";
    }


    public void FindAndRank(String queryString) {
        ArrayList<Document> resultArray = new ArrayList<Document>();

        System.out.println("Original query" + queryString);
        for (int i = 0; i < ArrayOfquery.length; i++) {
            //porter stemmer
            query = p.stem(ArrayOfquery[i].replaceAll("\"", "")).toLowerCase();
            System.out.println("Query after stemming " + query);
            //database
            Document wordDoc = dbController.GetWordFromIndexer(query);
            if (wordDoc != null) {

                System.out.println("found query " + query);
                resultArray.add(wordDoc);
            }
        }
        //--------------------Phrase searching-----------------
        if (queryString.charAt(0) == '"' && queryString.charAt(queryString.length() - 1) == '"')
        {
            String queryString_2 = queryString.replaceAll("\"", ""); //remove quotations
            if (!resultArray.isEmpty()){
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
                double TF = 1;
                double TF_IDF = 1;
                long tags = 0;
                //get all the websites that has this word
                for (int index = 0; index < matchingArray.size(); index++) {
                    Document siteResult = dbController.GetSiteFromWebsiteData(matchingArray.get(index).getString("url"));
                    if (siteResult != null) {
                        String str = siteResult.getString("body");
                            for (int j = 0; j < resultArray.get(0).getList("Websites", Document.class).size(); j++) {
                                if (matchingArray.get(index).getString("url").equals(resultArray.get(0).getList("Websites", Document.class).get(j).getString("URL"))) {
                                    tags = resultArray.get(0).getList("Websites", Document.class).get(j).getLong("Tags");
                                    break;
                                }
                            }
                        TF = matchingArray.get(index).getInteger("phraseCount");
                        TF_IDF = TF * IDF;
                        double rank = tags + TF_IDF*10000 + siteResult.getInteger("popularity");
                        int indexOFquery = str.indexOf(queryString_2);
                        String snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + queryString_2.length() + 350,str.length())).replaceAll(queryString_2,"<span id=\"boldedWord\"> " + queryString_2.toLowerCase() + " </span>");
                        dataArray.add(new Document().append("url", matchingArray.get(index).getString("url")).append("title", siteResult.getString("title")).append("snippet", snippet).append("rank", rank));

                    }
                }
            }
        }
        //--------------------Query processor-----------------
        else
        {
            //loop on each word of the query
            for (int i = 0; i < ArrayOfquery.length; i++)
            {
                if(resultArray.size() <= i)
                {
                    return;
                }
                if(resultArray.get(i).size() > 0)
                {
                    double IDF = resultArray.get(i).getDouble("IDF"); // getting IDF from indexer database
                    double TF = 0;
                    double TF_IDF = 0;
                    long tags = 0;
                    int Popularity = 1;
                    //get all the websites that has this word
                    for (int index = 0; index < resultArray.get(i).getList("Websites", Document.class).size(); index++)
                    {
                        String current_url = resultArray.get(i).getList("Websites", Document.class).get(index).getString("URL");
                        //getting the data (body,title) of the current website
                        Document siteResult = dbController.GetSiteFromWebsiteData(current_url);
                        if(siteResult != null)
                        {
                            String str = siteResult.getString("body");
                            queryString = queryString.toLowerCase();
                            ArrayOfquery[i]= ArrayOfquery[i].toLowerCase();
                            int indexOFquery = str.indexOf(queryString);
                            int indexOFquery2 = str.indexOf(ArrayOfquery[i]);
                            String snippet;

                            if (indexOFquery != -1)
                            {
                                snippet = str.toLowerCase().substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + queryString.length() + 300,str.length())).replaceAll(queryString,"<span id=\"boldedWord\"> " + queryString + " </span>");
                            }
                            else
                            {

                                snippet = str.toLowerCase().substring(Math.max(0,indexOFquery2 - 200),Math.min(indexOFquery2 + ArrayOfquery[i].length() + 300,str.length())).replaceAll(ArrayOfquery[i],"<span id=\"boldedWord\"> " + ArrayOfquery[i] + " </span>");
                            }
                            resultArray.get(i).getList("Websites", Document.class).set(index,resultArray.get(i).getList("Websites", Document.class).get(index).append("title",siteResult.getString("title")));
                            resultArray.get(i).getList("Websites", Document.class).set(index,resultArray.get(i).getList("Websites", Document.class).get(index).append("snippet",snippet));

                            boolean alreadyExist = false;
                            Popularity =  siteResult.getInteger("popularity"); // getting popularity from websiteData database
                            TF = resultArray.get(i).getList("Websites", Document.class).get(index).getDouble("TF"); // getting TF from indexer database
                            tags = resultArray.get(i).getList("Websites", Document.class).get(index).getLong("Tags"); // getting tags from indexer database
                            TF_IDF = TF * IDF;
                            if (ArrayOfquery.length > 1) //if there is more than one word check if this website was visited by another word of the query
                            {
                                for (int k = 0; k < dataArray.size(); k++) {
                                    if (dataArray.get(k).getString("url").equals(current_url))  //if this website is already visited (came from another word)
                                    {
                                        alreadyExist = true;
                                        int oldcount = dataArray.get(k).getInteger("count");
                                        String oldurl = dataArray.get(k).getString("url");
                                        String oldtitle = dataArray.get(k).getString("title");
                                        String oldsnippet = dataArray.get(k).getString("snippet");
                                        double oldrank = dataArray.get(k).getDouble("rank");

                                        dataArray.set(k,new Document().append("url",oldurl).append("title",oldtitle).append("snippet",oldsnippet).append("count",oldcount).append("rank", oldrank + TF_IDF*10000 + 100));
                                    }
                                }
                            }
                            if (!alreadyExist) {
                                Document web_site = resultArray.get(i).getList("Websites", Document.class).get(index);
                                String added_url = web_site.getString("URL");
                                String added_title = web_site.getString("title");
                                String added_snippet = web_site.getString("snippet");
                                int added_count = web_site.getInteger("Count");
                                double rank = Popularity + 10000 * TF_IDF + tags;
                                dataArray.add(new Document().append("url",added_url).append("title",added_title).append("snippet",added_snippet).append("count",added_count).append("rank", rank));
                            }
                        }
                    }
                }
            }
        }
    }
}