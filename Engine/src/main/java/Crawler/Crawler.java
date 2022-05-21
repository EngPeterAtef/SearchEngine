package Crawler;
//JSOUP
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
//UTILITY
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
//FROM CONTROLLER
import Database.Controller;
import Database.Controller.Data;

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
    private static final int MAX_PAGES = 5000;
    //number of threads used
    private static int NUM_THREADS;
    //list of visited links to not visit a link again
    private static ArrayList<String> UrlsInQueue = new ArrayList<>();
    //list of robot.txt links to not visit these links
    private static List<String> RobotLinks = new ArrayList<>();
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

    //database controller
    Controller DBControllerObj;
    static int queueSize;
    //class to specify the structure of QUEUE objects
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
    public void StartCrawler()
    {
        while (true){
            URLQueue URLObj = null;
            synchronized (LOCK1){
                if (URLs.isEmpty()){
                    return;
                }
                URLObj = URLs.get(0);
                URLs.remove(0);
            }
            //if the link is not visited before, visit it (get its html content)
            if (!URLObj.visited) {
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
                        URLObj.normalization = n;
                        //set the site to visited
                        Data data = new Data(url, false, doc.html());
                        //mongodb
                        DBControllerObj.UpdateQueue(URLObj);
                        //update data file to add new html
                        DBControllerObj.AddToCollectedData(data);
                        DBControllerObj.AddSiteData(url, doc.title(), doc.body().text());
                        crawl(Integer.parseInt(Thread.currentThread().getName()), doc,url);
                    }
                    else{
                        //if the normalization existed before, remove site from queue to not visit it again
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
//                    System.out.println(line);
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

                if(!UrlsInQueue.contains(next_link) && !RobotLinks.contains(next_link)){
                    synchronized (LOCK2){
                        if (queueSize >= MAX_PAGES)
                        {
                            return;
                        }
                    }
                    System.out.println("Thread: "+ level);
                    URLQueue nxtLink = new URLQueue(next_link, false, level, "");
                    //Insert to mongodb
                    synchronized (LOCK2){
                        URLs.add(nxtLink);
                        queueSize++;
                        UrlsInQueue.add(next_link);
                    }
                    DBControllerObj.AddToQueue(nxtLink);
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
        crawlerObj.DBControllerObj = new Controller(true);
        try {
            // convert JSON array to list of users
            URLs = crawlerObj.DBControllerObj.GetUrlQueue();
//            for (URLQueue queue : URLs) {
//                System.out.println(queue.url);
//            }
            CollectedData = crawlerObj.DBControllerObj.GetCollectedData();
            //READ ROBOT.TXT FILE
            RobotLinks = Files.readAllLines(Paths.get("robot.txt"));
            if (CollectedData == null) {
                CollectedData = new ArrayList<>();
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
                URLQueue link3= new URLQueue("https://www.nytimes.com/", false, -1, "");
                URLQueue link4= new URLQueue("https://dmoz-odp.org/", false, -1, "");
                URLQueue link5= new URLQueue("https://search.yahoo.com/web?fr=", false, -1, "");
                URLQueue link6= new URLQueue("https://www.facebook.com/", false, -1, "");
                URLQueue link7= new URLQueue("https://twitter.com/", false, -1, "");
                URLs = new ArrayList<>();
                URLs.add(link1);
                URLs.add(link2);
                URLs.add(link3);
                URLs.add(link4);
                URLs.add(link5);
                URLs.add(link6);
                URLs.add(link7);
                crawlerObj.DBControllerObj.AddToQueue(link1);
                crawlerObj.DBControllerObj.AddToQueue(link2);
                crawlerObj.DBControllerObj.AddToQueue(link3);
                crawlerObj.DBControllerObj.AddToQueue(link4);
                crawlerObj.DBControllerObj.AddToQueue(link5);
                crawlerObj.DBControllerObj.AddToQueue(link6);
                crawlerObj.DBControllerObj.AddToQueue(link7);
            }
            //--FILE IS NOT EMPTY, THEN CONTINUE FROM LAST STATE
            else {
                System.out.println("queue not NULL");
                int visitedNum = 0;
                for (int i = 0; i < URLs.size(); i++) {
                    normalizedUrls.add(URLs.get(i).normalization);

                }
//                if (visitedNum == URLs.size())
//                    return;
            }
            //initial size of the queue
            queueSize = URLs.size();
            for (int i = 0; i < URLs.size(); i++) {
                UrlsInQueue.add(URLs.get(i).url);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //ROBOT.TXT WRITER
        FileWriter fw = null;
        BufferedWriter bw = null;
        try
        {
            fw = new FileWriter("robot.txt", true);
            bw = new BufferedWriter(fw);
            robotWriter = new PrintWriter(bw);
        } catch (IOException e) {
        }
        System.out.println("\n\nEnter number of threads");
        Scanner scanner = new Scanner(System.in);
        NUM_THREADS = scanner.nextInt();
        scanner.close();

        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(new CrawlerRunnable(crawlerObj));
            threads[i].setName(String.valueOf(i));
            threads[i].start();
            System.out.println("Thread " + i + " started");
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
