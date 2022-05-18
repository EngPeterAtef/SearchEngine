
import Database.Controller;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.bson.Document;
import java.util.ArrayList;

public class Ranker extends HttpServlet {

    String query;
    String queryString;
    String[] ArrayOfquery;
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        queryString = request.getParameter("query");
        ArrayOfquery = queryString.split(" ");
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
            query = stemmer(ArrayOfquery[i].replace('"',"")).toLowerCase();
            //database
            resultArray.push(indexer.find({Word: query}).exec());

        }
        const resultArray = await Promise.all(fn_arr);
        //resultArray[0][0] contains object of 1st word
        //resultArray[1][0] contains object of 2nd word
        //resultArray[2][0] contains object of 3rd word
        if(queryString[0] == '"' && queryString[queryString.length-1] == '"')
        {
            let queryString_2 = queryString.replaceAll('"',"");
            for (let j = 0; j < resultArray[0][0].Websites.length; j++) // loop on each url in the first word
            {
            const siteResult = await collectedData.find({url: resultArray[0][0].Websites[j].URL}).exec();
                if(siteResult[0] != null)
                {
                    int indx;
                    int phraseCount = 0;
                    if(indx = siteResult[0].html.indexOf(queryString_2)  != -1)
                    {
                        int startIndex = indx;
                        phraseCount++;
                        while ((indx = siteResult[0].html.indexOf(queryString_2, startIndex)) > -1) {
                            phraseCount++;
                            startIndex = indx + queryString_2.length;
                        }
                        matchingArray.push({"url": resultArray[0][0].Websites[j].URL, "phraseCount": phraseCount});
                    }
                }
            }
            int IDF = Math.log(5000/matchingArray.length);
            int TF = 0;
            int TF_IDF = 0;
            //get all the websites that has this word
            for (int index = 0; index < matchingArray.length; index++)
            {
            String siteResult = await websiteData.find({url: matchingArray[index].url}).exec();
                if(siteResult[0] != null)
                {
                    String str = siteResult[0].body;
                    for (int i = 0; i < ArrayOfquery.length; i++) {
                        for (int j = 0; j < resultArray[i][0].Websites.length; j++)
                        {
                            if(matchingArray[index].url == resultArray[i][0].Websites[j].URL)
                            {
                                TF = resultArray[i][0].Websites[j].Count / str.length;
                                TF_IDF += TF * IDF;
                                System.out.println(matchingArray[index].url + " "+TF_IDF);
                            }
                        }
                    }
                    // let str = siteResult[0].body;
                    int indexOFquery = str.indexOf(query);
                    //resultArray[i][0].Websites[index].title = siteResult[0].title;
                    //resultArray[i][0].Websites[index].snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length));
                    dataArray.push({"url": matchingArray[index].url, "title": siteResult[0].title, "snippet": str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length))});
                    //TF = resultArray[i][0].Websites[index].locations / str.length;
                    //TF_IDF = TF * IDF;
                    dataArray[c].rank =  TF_IDF + siteResult[0].popularity + matchingArray[index].phraseCount;
                    c++;
                }
            }
        }
        else
        {
            for (int i = 0; i < ArrayOfquery.length; i++)
            {
                if(resultArray.length <= i)
                {
                    return;
                }
                if(resultArray[i].length > 0)
                {
                    int IDF = Math.log(5000/resultArray[i][0].Websites.length);
                    int TF = 0;
                    int TF_IDF = 0;
                    int Popularity = 1;
                    //get all the websites that has this word
                    for (int index = 0; index < resultArray[i][0].Websites.length; index++)
                    {
                    const siteResult = await websiteData.find({url: resultArray[i][0].Websites[index].URL}).exec();
                        if(siteResult[0] != null)
                        {
                            String str = siteResult[0].body;
                            int indexOFquery = str.search(query);
                            resultArray[i][0].Websites[index].title = siteResult[0].title;
                            resultArray[i][0].Websites[index].snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length));
                            int alreadyExist = false;
                            Popularity =  siteResult[0].popularity;
                            TF = resultArray[i][0].Websites[index].Count / str.length;
                            TF_IDF = TF * IDF;
                            for (int k = 0; k < dataArray.length; k++) {
                                if (dataArray[k].url === resultArray[i][0].Websites[index].URL) {
                                    dataArray[k].count++;
                                    alreadyExist = true;
                                    dataArray[k].rank +=  TF_IDF + 100;
                                    // console.log(dataArray[k].url + " " + dataArray[k].count);
                                }
                            }
                            if (!alreadyExist) {
                                dataArray.push({"url": resultArray[i][0].Websites[index].URL, "title": resultArray[i][0].Websites[index].title, "snippet": resultArray[i][0].Websites[index].snippet, "count": 1});
                                dataArray[index].rank =  TF_IDF + Popularity;
                            }
                        }
                    }
                }
            }
        }
    }
}

