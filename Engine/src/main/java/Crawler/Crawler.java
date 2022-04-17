package Crawler;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Crawler implements Runnable{
    //Maximum depth of crawler recursive calls
    private static final int MAX_DEPTH = 2;
    //Maximum number of total visited pages, 5000 in this project
    private static final int MAX_PAGES = 10;
    //number of threads used
    private static int NUM_THREADS = 2;
    //first link to start crawling from
    private String first_link;
    //list of visited links to not visit a link again
    private ArrayList<String> visitedLinks = new ArrayList<String>();
    //ID of current thread
    private int ID;
    //To count number of visited pages
    private static int Counter;
    //list of urls existing in queue
    private static List<URLQueue> URLs = null;
    private static List<Data> CollectedData = null;

    //class to specify the structure of QUEUE JSON objects
    private class URLQueue{
        public String url;
        public boolean visited;
        public int threadID;
        URLQueue(String url, boolean visited, int threadID){
            this.url = url;
            this.visited = visited;
            this.threadID = threadID;
        }
    }
    //class to specify the structure of HTML (Collected data) JSON objects
    private class Data{
        public String url;
        public String html;
        Data(String url, String html){
            this.url = url;
            this.html = html;
        }
    }

    public Crawler(String link){
        first_link = link;
    }
    public void run()
    {
        this.ID = Integer.parseInt(Thread.currentThread().getName());
        crawl(1, first_link);
    }
    private void crawl(int level , String url){
        if(level <= MAX_DEPTH && Counter <= MAX_PAGES){
            Document doc = request(url);
            if(doc != null)
            {
                //-----INSERT DATA TO FILE----
                Data data = new Data(url, doc.outerHtml());
                WriteToDataFile(data);
                //-------GET OTHER LINKS------
                for(Element link :  doc.select("a[href]")){
                    String next_link = link.absUrl("href");
                    if(visitedLinks.contains(next_link) == false){
                        crawl(level++,next_link);
                    }
                }
            }
        }
    }
    private Document request(String url)
    {
        try {
            Connection con = Jsoup.connect(url);
            Document doc = con.get();
            if(con.response().statusCode() == 200)
            {
//                System.out.println("Bot ID : "+ID+" Recieved Webpage at : "+url);
                URLQueue link = new URLQueue(url, false, ID);
                WriteToQueueFile(link);
                String title = doc.title();
//                System.out.println(title);
//                WriteToFile(title);
                visitedLinks.add(url);
//                Counter++;
                return doc;
            }
            return null;
        } catch (IOException e) {
            return null;

        }
    }

    public static void main(String[] arg)
    {
        Crawler crawlerObj = new Crawler("");
        try {
            // create Gson instance
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("queue.json"));

            // convert JSON array to list of users
            URLs = gson.fromJson(reader, new TypeToken<List<URLQueue>>(){}.getType());
            reader.close();

            //--IF THE FILE IS EMPTY, INSERT SEEDS--
            if (URLs == null) {
                URLQueue link1= crawlerObj.new URLQueue("https://www.wikipedia.org/", false, 0);
                URLQueue link2= crawlerObj.new URLQueue("https://www.nytimes.com/", false, 0);
                WriteToQueueFile(link1);
                WriteToQueueFile(link2);
            }
            //--FILE IS NOT EMPTY, THEN CONTINUE FROM LAST STATE
            else {

                /********
                 * STILL NOT COMPLETE
                 * ******/

//                URLQueue link3= crawlerObj.new URLQueue("https://www.google.com/", false, 0);
//                WriteToFile(link3);
                Counter = URLs.size();
                System.out.println(URLs.get(0).url);
            }

            // close reader
            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Thread[] threads = new Thread[NUM_THREADS];
        String[] startLinks = new String[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            startLinks[i] = URLs.get(i).url;
//            System.out.println(startLinks[i]);
            threads[i] = new Thread(new Crawler(startLinks[i]));
            threads[i].setName(String.valueOf(i + 1));
            threads[i].start();
        }
    }
    private static void WriteToQueueFile(URLQueue obj){
        if (URLs == null){
            URLs = new ArrayList<URLQueue>();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Writer writer = null;
        try {
            writer = Files.newBufferedWriter(Paths.get("queue.json"));
            URLs.add(obj);
            gson.toJson(URLs, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void WriteToDataFile(Data obj){
        if (CollectedData == null){
            CollectedData = new ArrayList<Data>();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Writer writer = null;
        try {
            writer = Files.newBufferedWriter(Paths.get("data.json"));
            CollectedData.add(obj);
            gson.toJson(CollectedData, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
