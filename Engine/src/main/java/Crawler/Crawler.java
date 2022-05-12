package Crawler;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import Database.Controller;

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
    private static final int MAX_PAGES = 500;
    //number of threads used
    private static int NUM_THREADS = 8;
    //list of visited links to not visit a link again
    private static ArrayList<String> UrlsInQueue = new ArrayList<String>();
    //list of robot.txt links to not visit these links
    private static List<String> RobotLinks = new ArrayList<String>();
    //list of urls existing in queue
    private static List<URLQueue> URLs = null;
    //List of data from data.json file
    private static List<Data> CollectedData = null;
    //list of normalized urs from queue.json file
    private static List<String> normalizedUrls = new ArrayList<>();
    //writer to robot.txt file
    static PrintWriter robotWriter;
    static final Object LOCK1 = new Object();
    static final Object LOCK2 = new Object();

    static Gson gson = null;

    // create a reader
    static Reader queueReader = null;
    static Reader dataReader = null;
    //database controller
    Controller DBControllerObj;
    static int queueSize;

    //class to specify the structure of QUEUE JSON objects
    public static class URLQueue{
        public String url;
        public boolean visited;
        public int threadID;
        public String normalization;
        public URLQueue(String url, boolean visited, int threadID, String normalization){
            this.url = url;
            this.visited = visited;
            this.threadID = threadID;
            this.normalization = normalization;
        }
    }
    //class to specify the structure of HTML (Collected data) JSON objects
    public static class Data{
        public String url;
        public boolean visited;
        public String html;
        public Data(String url, boolean visited, String html){
            this.url = url;
            this.visited = visited;
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
        while (true){
//            System.out.printf("Thread: %s, size: %d\n", Thread.currentThread().getName(), i);
//            System.out.println("Thread: "+ Integer.parseInt(Thread.currentThread().getName()));
            URLQueue URLObj = null;
            synchronized (LOCK1){
                if (URLs.isEmpty()){
                    return;
                }
//                System.out.println(i);
//                i++;
                URLObj = URLs.get(0);
                URLs.remove(0);
            }
                //System.out.println(URLObj.url);
                //if the link is not visited before, visit it (get its html content)
                if (!URLObj.visited) {
//                System.out.println("not visited");
                    String url = URLObj.url;
                    Document doc = request(url);
                    if (doc != null) {
                        //normalize url
                        String n = NormalizeUrl(doc);
                        //check if the normalization existed before, scam
                        if (!normalizedUrls.contains(n)){
                            normalizedUrls.add(n);
                            //-----INSERT DATA TO FILE----
                            URLObj.visited = true;
                            //System.out.println(n);
                            URLObj.normalization = n;
                            //set the site to visited
//                            synchronized (URLs){
//                                URLs.set(i, URLObj);
//                            }
                            Data data = new Data(url, false, doc.html());
                            //update queue file to update the visited status in file
//                            UpdateQueueFile();
                            //mongodb
                            DBControllerObj.UpdateQueue(URLObj);
                            //update data file to add new html
//                            WriteToDataFile(data);
                            DBControllerObj.AddToCollectedData(data);
                            DBControllerObj.AddSiteData(url, doc.title(), doc.body().text());
                            crawl(Integer.parseInt(Thread.currentThread().getName()), doc,url);
                        }
                        else{
//                          //if the normalization existed before, remove site from queue to not visit it again
//                            UpdateQueueFile();
                            //mongodb
                            DBControllerObj.RemoveFromQueue(URLObj);
                            synchronized (LOCK2){
                                queueSize--;
                            }
                        }
                    }
                    else {
                        DBControllerObj.RemoveFromQueue(URLObj);
                        synchronized (LOCK2){
                            queueSize--;
                        }
                    }
                }
            }
    }

    private String NormalizeUrl(Document doc){
        if (doc != null)
        {
            StringBuilder result = new StringBuilder();
            String[] textArray = doc.body().text().split(" ");
                for(String word : textArray){
                    try {
                        result.append(word.toLowerCase().charAt(0));
                    }catch (StringIndexOutOfBoundsException e){

                    }
                }
            return result.toString().replaceAll("[^a-zA-Z]", "");
        }
        return "";
    }
    private void GetRobotFileLinks(String link)
    {
        if (link.charAt(link.length()-1) != '/')
        {
            link += "/";
        }
        try(BufferedReader in = new BufferedReader(
                new InputStreamReader(new URL(link + "robots.txt").openStream()))) {
            String line = null;
            while((line = in.readLine()) != null) {
                String dis = line.substring(0, Math.min(line.length(), 11));
                if (dis.equals("Disallow: /")){
                    System.out.println(line);
                    String d = link+line.substring(11,line.length()) ;
//                    System.out.println(d);
                    synchronized (RobotLinks){
                        robotWriter.println(d);
                        robotWriter.flush();
                        RobotLinks.add(d);
                    }
                }

            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void crawl(int level , Document doc,String url){
        if(doc != null)
        {
            GetRobotFileLinks(url);
//            if(temp != null) RobotLinks.addAll(temp);

            //-------GET OTHER LINKS------
            for(Element link :  doc.select("a[href]")){
                String next_link = link.absUrl("href");

                if(UrlsInQueue.contains(next_link) == false && RobotLinks.contains(next_link) == false){
                    synchronized (LOCK2){
                        if (queueSize >= MAX_PAGES)
                        {
                            return;
                        }
                    }
                    System.out.println("Thread: "+ level);
                    URLQueue nxtLink = new URLQueue(next_link, false, level, "");
                    //insert to json
//                    WriteToQueueFile(nxtLink);
                    //Insert to mongodb
                    synchronized (LOCK2){
                        URLs.add(nxtLink);
                        queueSize++;
                        UrlsInQueue.add(next_link);
                    }
                    DBControllerObj.AddToQueue(nxtLink);
                    synchronized (this){
                        System.out.println("Thread: " + Thread.currentThread().getName() + " ,Notifying " + this);
                        notifyAll();
                    }
                }
                else if (UrlsInQueue.contains(next_link)){
                    synchronized (LOCK2){
                        if (queueSize >= MAX_PAGES)
                        {
                            return;
                        }
                    }
                    DBControllerObj.IncrementPopularity(next_link);
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
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] arg)
    {
        Crawler crawlerObj = new Crawler();
        crawlerObj.DBControllerObj = new Controller();
        try {
            // create Gson instance
//            gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

            // create a reader
//            queueReader = Files.newBufferedReader(Paths.get("queue.json"));
//            dataReader = Files.newBufferedReader(Paths.get("data.json"));

            // convert JSON array to list of users
            URLs = crawlerObj.DBControllerObj.GetUrlQueue();
//            for (URLQueue queue : URLs) {
//                System.out.println(queue.url);
//            }

//            URLs = gson.fromJson(queueReader, new TypeToken<List<URLQueue>>(){}.getType());
            CollectedData = crawlerObj.DBControllerObj.GetCollectedData();
//            CollectedData = gson.fromJson(dataReader, new TypeToken<List<Data>>(){}.getType());
            RobotLinks = Files.readAllLines(Paths.get("robot.txt"));
            if (CollectedData == null) {
                CollectedData = new ArrayList<Data>();
                System.out.println("data NULL");
            }
            else {
                System.out.println("data not NULL");
            }

            //--IF THE FILE IS EMPTY, INSERT SEEDS--
//                System.out.println(URLs.size());
            if (URLs == null) {
                System.out.println("queue NULL");
                URLQueue link1= new URLQueue("https://en.wikipedia.org/wiki/The_Batman_(film)", false, -1, "");
                URLQueue link2= new URLQueue("https://www.bbc.com", false, -1, "");
                URLQueue link3= new URLQueue("https://www.facebook.com/", false, -1, "");
//                WriteToQueueFile(link1);
//                WriteToQueueFile(link2);
//                WriteToQueueFile(link3);
                URLs = new ArrayList<URLQueue>();
                URLs.add(link1);
                URLs.add(link2);
                URLs.add(link3);
                crawlerObj.DBControllerObj.AddToQueue(link1);
                crawlerObj.DBControllerObj.AddToQueue(link2);
                crawlerObj.DBControllerObj.AddToQueue(link3);
            }
            //--FILE IS NOT EMPTY, THEN CONTINUE FROM LAST STATE
            else {
                /********
                 * STILL NOT COMPLETE
                 * ******/
                System.out.println("queue not NULL");
                for (int i = 0; i < URLs.size(); i++) {
                    normalizedUrls.add(URLs.get(i).normalization);
                }
            }
            //initial size of the queue
            queueSize = URLs.size();
            for (int i = 0; i < URLs.size(); i++) {
                UrlsInQueue.add(URLs.get(i).url);
            }
            // close reader
//            queueReader.close();
//            dataReader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        FileWriter fw = null;
        BufferedWriter bw = null;
        try
        {
            fw = new FileWriter("robot.txt", true);
            bw = new BufferedWriter(fw);
            robotWriter = new PrintWriter(bw);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
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
    private static void WriteToQueueFile(URLQueue obj){
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
    private static void UpdateQueueFile(){
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
    private static void WriteToDataFile(Data obj){
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
