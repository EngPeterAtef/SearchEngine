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
    public String SaveSuggestion(Model model,@RequestParam(value = "query") String query) {
        dbController.InsertOrUpdateSuggestion(query);
        ArrayOfquery = query.split(" ");
        c = 0;
        dataArray.clear();
        matchingArray.clear();
        FindAndRank(query);
//        String ResultsPage = "<!doctype html> <html><body align=\"center\">"+ dataArray +"</body></html>";
        model.addAttribute("query", query);
        model.addAttribute("results", dataArray);
        return "results";
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
        btnNumber = (int) Math.ceil(dataArray.size()/10.0);
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

        //resultArray[0][0] contains object of 1st word
        //resultArray[1][0] contains object of 2nd word
        //resultArray[2][0] contains object of 3rd word
        if (queryString.charAt(0) == '"' && queryString.charAt(queryString.length() - 1) == '"') {
            String queryString_2 = queryString.replaceAll("\"", "");
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

        else
        {
            for (int i = 0; i < ArrayOfquery.length; i++)
            {
                if(resultArray.size() <= i)
                {
                    return;
                }
                if(resultArray.get(i).size() > 0)
                {
                    double IDF = Math.log(5000.0/resultArray.get(i).getList("Websites", Document.class).size());
                    double TF = 0;
                    double TF_IDF = 0;
                    int Popularity = 1;
                    //get all the websites that has this word
                    for (int index = 0; index < resultArray.get(i).getList("Websites", Document.class).size(); index++)
                    {
                        Document siteResult = dbController.GetSiteFromWebsiteData(resultArray.get(i).getList("Websites", Document.class).get(index).getString("URL"));
//                        System.out.println(siteResult);
                        if(siteResult != null)
                        {
                            String str = siteResult.getString("body");
                            int indexOFquery = str.indexOf(query);
                            resultArray.get(i).getList("Websites", Document.class).set(index,resultArray.get(i).getList("Websites", Document.class).get(index).append("title",siteResult.getString("title")));
                            resultArray.get(i).getList("Websites", Document.class).set(index,resultArray.get(i).getList("Websites", Document.class).get(index).append("snippet",str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length() + 300,str.length())).toLowerCase().replaceAll(query,"<span id=\"boldedWord\"> " + query + " </span>")));

                            boolean alreadyExist = false;
                            Popularity =  siteResult.getInteger("popularity");
                            TF = resultArray.get(i).getList("Websites", Document.class).get(index).getInteger("Count") / (double)str.length();
                            TF_IDF = TF * IDF;
                            for (int k = 0; k < dataArray.size(); k++) {
                                if (dataArray.get(k).getString("url").equals(resultArray.get(i).getList("Websites", Document.class).get(index).getString("URL")) ) {

                                    alreadyExist = true;
                                    double oldcount = dataArray.get(k).getDouble("count");
                                    dataArray.set(k,dataArray.get(k).append("count",oldcount+1));

                                    double oldrank = dataArray.get(k).getDouble("rank");
                                    dataArray.set(k,dataArray.get(k).append("rank",oldrank + TF_IDF + 100));
                                    System.out.println(dataArray.get(k).getString("url") + " " + dataArray.get(k).getString("count"));
                                }
                            }
                            if (!alreadyExist) {
                                //dataArray.push({"url": resultArray[i][0].Websites[index].URL, "title": resultArray[i][0].Websites[index].title, "snippet": resultArray[i][0].Websites[index].snippet, "count": 1});

                                Document web_site = resultArray.get(i).getList("Websites", Document.class).get(index);
                                String added_url = web_site.getString("URL");
                                String added_title = web_site.getString("title");
                                String added_snippet = web_site.getString("snippet");
                                String added_count = web_site.getString("count");
                                dataArray.add(new Document().append("url",added_url).append("title",added_title).append("snippet",added_snippet).append("count",added_count).append("rank",Popularity + TF_IDF));
                                //dataArray[index].rank =  TF_IDF + Popularity;
                                //double oldrank = dataArray.get(index).getDouble("rank");
								//dataArray.set(index,dataArray.get(index).append("rank",Popularity + TF_IDF));
                            }
                        }
                    }
                }
            }
        }
    }
}