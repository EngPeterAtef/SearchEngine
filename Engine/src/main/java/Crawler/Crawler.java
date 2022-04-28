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


class CrawlerRunnable implements Runnable{
    Crawler crawler;
    CrawlerRunnable(Crawler c){
        this.crawler = c;
    }

    @Override
    public void run() {
        crawler.StartCrawler();
    }
}
public class Crawler{
    //Maximum number of total visited pages, 5000 in this project
    private static final int MAX_PAGES = 200;
    //number of threads used
    private static int NUM_THREADS = 8;
    //list of visited links to not visit a link again
    private static ArrayList<String> UrlsInQueue = new ArrayList<String>();
    //list of urls existing in queue
    private static List<URLQueue> URLs = null;
    private static List<Data> CollectedData = null;
    private static List<String> normalizedUrls = new ArrayList<>();


    static Gson gson = null;

    // create a reader
    static Reader queueReader = null;
    static Reader dataReader = null;

    //class to specify the structure of QUEUE JSON objects
    private class URLQueue{
        public String url;
        public boolean visited;
        public int threadID;
        public String normalization;
        URLQueue(String url, boolean visited, int threadID, String normalization){
            this.url = url;
            this.visited = visited;
            this.threadID = threadID;
            this.normalization = normalization;
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

    public void StartCrawler()
    {
            synchronized (this) {
                while (Integer.parseInt(Thread.currentThread().getName()) + 1 > URLs.size()){
                    try {
                        System.out.println("Thread: " + Thread.currentThread().getName() + " ,Size: " + URLs.size() + " " + this);
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Thread: " + Thread.currentThread().getName() + " ,woke up");
            }
        for (int i = Integer.parseInt(Thread.currentThread().getName()); i < URLs.size(); i+= NUM_THREADS) {
//            System.out.println("Thread: "+ Integer.parseInt(Thread.currentThread().getName()));
                URLQueue URLObj = URLs.get(i);
                System.out.println(URLObj.url);
                if (!URLObj.visited) {
//                System.out.println("not visited");
                    String url = URLObj.url;
                    Document doc = request(url);
                    if (doc != null) {
                        //normalize url
                        String n = NormalizeUrl(doc);
                        if (!normalizedUrls.contains(n)){
                            normalizedUrls.add(n);

                            //-----INSERT DATA TO FILE----
                            URLObj.visited = true;
                            System.out.println(n);
                            URLObj.normalization = n;
                            URLs.set(i, URLObj);
                            Data data = new Data(url, doc.html());
    //                        synchronized (gson){
                                UpdateQueueFile(gson);
                                WriteToDataFile(data,gson);
                            crawl(Integer.parseInt(Thread.currentThread().getName()), doc);
                        }
                        else{
                            //remove from queue
                            URLs.remove(URLObj);
                            UpdateQueueFile(gson);
                        }
//                        }
                    }
                }
            }
    }

    private String NormalizeUrl(Document doc){
        StringBuilder result = new StringBuilder();
        String[] textArray = doc.text().split(" ");
        for(String word : textArray){
            result.append(word.toLowerCase().charAt(0));
        }
        return result.toString().replaceAll("[^a-zA-Z0-9_-]", "");
    }

    private void crawl(int level , Document doc){
        if(doc != null)
        {
            //-------GET OTHER LINKS------
            for(Element link :  doc.select("a[href]")){
                String next_link = link.absUrl("href");
                if(UrlsInQueue.contains(next_link) == false && URLs.size() < MAX_PAGES){
                    System.out.println("Thread: "+ level);
                    URLQueue nxtLink = new URLQueue(next_link, false, level, "");
                    WriteToQueueFile(nxtLink, gson);
                    UrlsInQueue.add(next_link);
                    synchronized (this){
                        System.out.println("Thread: " + Thread.currentThread().getName() + " ,Notifying " + this);
                        notifyAll();
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
                return doc;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static void main(String[] arg)
    {
        Crawler crawlerObj = new Crawler();
        try {
            // create Gson instance
            gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

            // create a reader
            queueReader = Files.newBufferedReader(Paths.get("queue.json"));
            dataReader = Files.newBufferedReader(Paths.get("data.json"));

            // convert JSON array to list of users
            URLs = gson.fromJson(queueReader, new TypeToken<List<URLQueue>>(){}.getType());
            CollectedData = gson.fromJson(dataReader, new TypeToken<List<Data>>(){}.getType());
            if (CollectedData == null) {
                System.out.println("data NULL");
            }
            else {
                System.out.println("data not NULL");
            }

            //--IF THE FILE IS EMPTY, INSERT SEEDS--
//                System.out.println(URLs.size());
            if (URLs == null) {
                System.out.println("queue NULL");
                URLQueue link1= crawlerObj.new URLQueue("http://www.risemysticct.com/", false, -1, "");
                URLQueue link2= crawlerObj.new URLQueue("https://www.nytimes.com/", false, -1, "");
                URLQueue link3= crawlerObj.new URLQueue("https://www.wikipedia.org/", false, -1, "");
                WriteToQueueFile(link1, gson);
                WriteToQueueFile(link2, gson);
                WriteToQueueFile(link3, gson);
            }
            //--FILE IS NOT EMPTY, THEN CONTINUE FROM LAST STATE
            else {
                /********
                 * STILL NOT COMPLETE
                 * ******/
                System.out.println("queue not NULL");
//                URLQueue link3= crawlerObj.new URLQueue("https://www.yahoo.com/", false, -1);
//                WriteToQueueFile(link3, gson);
//                Counter = URLs.size();
//                System.out.println(URLs.get(0).url);
                for (int i = 0; i < URLs.size(); i++) {
                    normalizedUrls.add(URLs.get(i).normalization);
                }
            }
            for (int i = 0; i < URLs.size(); i++) {
                UrlsInQueue.add(URLs.get(i).url);
            }
            // close reader
            queueReader.close();
            dataReader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(new CrawlerRunnable(crawlerObj));
            threads[i].setName(String.valueOf(i));
            threads[i].start();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    private static void WriteToQueueFile(URLQueue obj,Gson gson){
        synchronized (gson) {
            if (URLs == null){
                URLs = new ArrayList<URLQueue>();
            }
    //        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
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
    }
    private static void UpdateQueueFile( Gson gson){
        synchronized (gson) {
            if (URLs == null){
                URLs = new ArrayList<URLQueue>();
            }
    //        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            Writer writer = null;
            try {
                writer = Files.newBufferedWriter(Paths.get("queue.json"));
                gson.toJson(URLs, writer);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static void WriteToDataFile(Data obj, Gson gson){
        synchronized (gson) {
            if (CollectedData == null){
                CollectedData = new ArrayList<Data>();
            }
    //        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
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
}
