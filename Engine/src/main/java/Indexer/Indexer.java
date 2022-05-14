package Indexer;
import Database.Controller;
import Database.Controller.Data;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import opennlp.tools.stemmer.PorterStemmer;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.MongoException;
//  import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.types.ObjectId;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


//import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.*;
import java.util.Map.Entry;



public class Indexer {
    private static LinkedHashSet<String> allWords = new LinkedHashSet<String>();
    private static LinkedHashSet<String> allWords2 = new LinkedHashSet<String>();
    Controller DBControllerObj = new Controller(true);
    private static List<Document>docs=new ArrayList<Document>();
    // private static  FileWriter writ;
    public class syncho implements Runnable{
        //lock  = calling object => which is the thread
        private Indexer x;
        public syncho(Indexer y){
            x = y;
        }
        public void run()
        {
            Integer rem=allWords.size()%10;
            Integer range=allWords.size()/10;
            Integer start=Integer.parseInt(Thread.currentThread().getName())*range;
            Integer end;
            if(Thread.currentThread().getName().equals("9"))
                end=start+range+rem;
            else end=start+range;

            int ew=0;
            Iterator<String> itr = allWords.iterator();
            while(itr.hasNext())
            {
                if(ew==start)
                    break;
                {
                    ew++;
                    itr.next();
                }
            }
            while(ew<end)// ma3aya kelma
            {
                HashMap <String,pair>tempo = new HashMap<String, pair>();
                //Integer k=0;
                ew++;
                String aword=itr.next();
                System.out.println(ew);


                for (int k=0;k<docs.size();k++)// le kol doc
                {
                    pair temppair = new pair();

                    Elements selectResult=docs.get(k).getElementsContainingOwnText(aword);

                    for (Element e : selectResult) {// kol el elements ely gowahom el
                        temppair.count++;
                        Elements parents = e.parents();
                        for (Element h : parents)// each tag
                        {
                            setlocations(h.tagName(), temppair.location);// set all locations


                        }

                    }
                    if(temppair.count!=0)
                        tempo.put(CollectedData.get(k).url, temppair);


                }
                synchronized (this.x) {
//                    indexermap.put(aword,tempo);
                    DBControllerObj.insertIndexedWord(aword, tempo);
                }


            }
        }
    }
    private static List<Data> CollectedData = null;// areay of websirtes
    private static HashMap<String, HashMap<String, pair>> indexermap = new HashMap<String, HashMap<String, pair>>();
    private static List<HashMap<String, HashMap<String, pair>>> subindexermap=new ArrayList<HashMap<String, HashMap<String, pair>>>();
    // word //url //occurrences
    private static List<String> stopwords = null;

    public static class pair {
        public Integer count;
        // public BitSet location ;
        public BitSet location;

        pair() {

            location=new BitSet(15);
            count = 0;
        }
    }



    static void setlocations(String mytag, BitSet locations) {
        switch (mytag) {
            case "title":
                locations.set(14);
                break;
            case "h1":

                locations.set(13);
                break;
            case "h2":
                locations.set(12);
                break;
            case "h3":
                locations.set(11);
                break;
            case "h4":
                locations.set(10);
                break;
            case "h5":
                locations.set(9);
                break;
            case "h6":
                locations.set(8);
                break;
            case "nav":
                locations.set(7);
                break;
            case "header":
                locations.set(6);
                break;
            case "li":
                locations.set(5);
                break;
            case "main":
                locations.set(4);
                break;
            case "section":
                locations.set(3);
                break;
            case "p":
                locations.set(2);
                break;
            case "side":
                locations.set(1);
                break;
            case "footer":
                locations.set(0);
                break;
            default:
                locations.set(0);
                break;

        }
        return;

    }
    static void WriteMapToDatabase()
    {

    }

    public static void main(String[] arg) {
        try {
            // create Gson instance
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // create a reader
//            Reader reader = Files.newBufferedReader(Paths.get("data.json"));

            //-----------------------------------------
            //------TO READ DATA FROM DATABASE---------
            //-----------------------------------------
            Indexer indexerObj = new Indexer();
//            indexerObj.DBControllerObj =  new Controller(true);
            CollectedData = indexerObj.DBControllerObj.GetCollectedData();
            if (CollectedData != null){
                System.out.println(CollectedData.size());
            }
            else {
                System.out.println("Null");
            }
            //-----------------------------------------
//            CollectedData = gson.fromJson(reader, new TypeToken<List<Data>>() {
//            }.getType());// array of json objects websites
//            reader.close();

            // array of stop words
            stopwords = Files.readAllLines(Paths.get("stop_words_english.txt"));

            int collectedDataSize = CollectedData.size();
            // writ=new FileWriter("tm.txt");
            for (int i = 0; i < collectedDataSize; i++) {
                if(CollectedData.get(i).visited==true)
                    continue;
                docs.add(Jsoup.parse(CollectedData.get(i).html));
                allWords2.addAll(Stream.of(docs.get(i).text().replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().split(" "))
                        .collect(Collectors.toCollection(LinkedHashSet<String>::new)));// array of all words in page
                CollectedData.get(i).visited=true;
                indexerObj.DBControllerObj.UpdateVisitedInCollectedData(CollectedData.get(i).url, true);
                CollectedData.get(i).html = "";
//                CollectedData.remove(CollectedData.size() - 1);
            }


            PorterStemmer p = new PorterStemmer();

            Iterator itery = allWords2.iterator();


            allWords2.removeAll(stopwords);// remove all blabla


            Iterator iter = allWords2.iterator();

            while (iter.hasNext())
            {
                allWords.add(p.stem(iter.next().toString())) ;
            }


            allWords.remove("");

            System.out.println(docs.size());
            System.out.println(allWords.size());

            Indexer indo=new Indexer();
            syncho ob=indo.new syncho(indo);
            Thread t0=new Thread(ob);
            Thread t1=new Thread(ob);Thread t2=new Thread(ob);Thread t3=new Thread(ob);Thread t4=new Thread(ob);Thread t5=new Thread(ob);Thread t6=new Thread(ob);Thread t7=new Thread(ob);Thread t8=new Thread(ob);Thread t9=new Thread(ob);
            t0.setName("0");t1.setName("1");t2.setName("2");t3.setName("3");t4.setName("4");t5.setName("5");t6.setName("6");t7.setName("7"); t8.setName("8"); t9.setName("9");
            t0.start();t1.start();t2.start();t3.start();t4.start();t5.start();t6.start();t7.start();   t8.start();   t9.start();
            t0.join();t1.join();t2.join();t3.join();t4.join();t5.join();t6.join();t7.join();t8.join();t9.join();
            System.out.println("joined");
//            System.out.println(indexermap.size());

//            indexerObj.DBControllerObj.indexerone(indexermap);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}