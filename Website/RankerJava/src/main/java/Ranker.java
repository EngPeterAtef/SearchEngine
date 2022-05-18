
import Database.Controller;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.bson.Document;
import java.util.ArrayList;
import opennlp.tools.stemmer.PorterStemmer;

public class Ranker extends HttpServlet {

    String query;
    String queryString;
    String[] ArrayOfquery;
    Controller controller = new Controller();
    PorterStemmer p = new PorterStemmer();
    ArrayList<Document> matchingArray = new ArrayList<Document>();
    ArrayList<Document> dataArray = new ArrayList<Document>();
    int c =0;


    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        queryString = request.getParameter("query");
        ArrayOfquery = queryString.split(" ");
        c =0;
        FindAndRank();
        response.setContentType("text/html");

        String ResultsPage = "<!doctype html> <html><body align=\"center\"></body></html>";
        response.getWriter().println(ResultsPage);
    }

    public void FindAndRank()
    {
        ArrayList<Document> resultArray = new ArrayList<Document>();

        for (int i = 0; i < ArrayOfquery.length; i++)
        {
            //porter stemmer
            query = p.stem(ArrayOfquery[i].replaceAll("\"","")).toLowerCase();
            //database
            resultArray.add(controller.GetWordFromIndexer(query));
        }

        //resultArray[0][0] contains object of 1st word
        //resultArray[1][0] contains object of 2nd word
        //resultArray[2][0] contains object of 3rd word
        if(queryString.charAt(0) == '"' && queryString.charAt(queryString.length()-1) == '"')
        {
            String queryString_2 = queryString.replaceAll("\"","");
            for (int j = 0; j < resultArray.get(0).getList("Websites",Document.class).size(); j++) // loop on each url in the first word
            {
            Document siteResult = controller.GetSiteFromCollectedData(resultArray.get(0).getList("Websites",Document.class).get(j).getString("URL"));
                if(siteResult != null)
                {
                    int indx = siteResult.getString("html").indexOf(queryString_2) ;
                    int phraseCount = 0;
                    if(indx  != -1)
                    {
                        int startIndex = indx;
                        phraseCount++;

                        while ((indx = siteResult.getString("html").indexOf(queryString_2, startIndex)) > -1) {
                            phraseCount++;
                            startIndex = indx + queryString_2.length();
                        }
                        matchingArray.add(new Document().append("url",resultArray.get(0).getList("Websites",Document.class).get(j).getString("URL")).append( "phraseCount", phraseCount));
                    }
                }
            }
            double IDF = Math.log(5000.0/matchingArray.size());
            double TF = 0;
            double TF_IDF = 0;
            //get all the websites that has this word
            for (int index = 0; index < matchingArray.size(); index++)
            {
            Document siteResult = controller.GetSiteFromWebsiteData(matchingArray.get(index).getString("url"));
                if(siteResult != null)
                {
                    String str = siteResult.getString("body");
                    for (int i = 0; i < ArrayOfquery.length; i++) {
                        for (int j = 0; j < resultArray.get(i).getList("Websites",Document.class).size(); j++)
                        {
                            if(matchingArray.get(index).getString("url").equals(resultArray.get(i).getList("Websites",Document.class).get(j).getString("URL")))
                            {
                                TF = resultArray.get(i).getList("Websites",Document.class).get(j).getInteger("count") / (double)str.length();
                                TF_IDF += TF * IDF;
                                System.out.println(matchingArray.get(index).getString("url") + " "+TF_IDF);
                            }
                        }
                    }
                    // let str = siteResult[0].body;
                    int indexOFquery = str.indexOf(query);
                    //resultArray[i][0].Websites[index].title = siteResult[0].title;
                    //resultArray[i][0].Websites[index].snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length));
                    dataArray.add(new Document().append("url", matchingArray.get(index).getString("url")).append("title", siteResult.getString("title")).append("snippet",str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length() + 300,str.length())))) ;
                    //TF = resultArray[i][0].Websites[index].locations / str.length;
                    //TF_IDF = TF * IDF;
                    dataArray.set(c,dataArray.get(c).append("rank", TF_IDF + siteResult.getInteger("popularity") + matchingArray.get(index).getInteger("phraseCount"))) ;
                    c++;
                }
            }
        }
//        else
//        {
//            for (int i = 0; i < ArrayOfquery.length; i++)
//            {
//                if(resultArray.size() <= i)
//                {
//                    return;
//                }
//                if(resultArray[i].length > 0)
//                {
//                    int IDF = Math.log(5000/resultArray[i][0].Websites.length);
//                    int TF = 0;
//                    int TF_IDF = 0;
//                    int Popularity = 1;
//                    //get all the websites that has this word
//                    for (int index = 0; index < resultArray[i][0].Websites.length; index++)
//                    {
//                    const siteResult = await websiteData.find({url: resultArray[i][0].Websites[index].URL}).exec();
//                        if(siteResult[0] != null)
//                        {
//                            String str = siteResult[0].body;
//                            int indexOFquery = str.search(query);
//                            resultArray[i][0].Websites[index].title = siteResult[0].title;
//                            resultArray[i][0].Websites[index].snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length));
//                            int alreadyExist = false;
//                            Popularity =  siteResult[0].popularity;
//                            TF = resultArray[i][0].Websites[index].Count / str.length;
//                            TF_IDF = TF * IDF;
//                            for (int k = 0; k < dataArray.length; k++) {
//                                if (dataArray[k].url === resultArray[i][0].Websites[index].URL) {
//                                    dataArray[k].count++;
//                                    alreadyExist = true;
//                                    dataArray[k].rank +=  TF_IDF + 100;
//                                    // console.log(dataArray[k].url + " " + dataArray[k].count);
//                                }
//                            }
//                            if (!alreadyExist) {
//                                dataArray.push({"url": resultArray[i][0].Websites[index].URL, "title": resultArray[i][0].Websites[index].title, "snippet": resultArray[i][0].Websites[index].snippet, "count": 1});
//                                dataArray[index].rank =  TF_IDF + Popularity;
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }
}

