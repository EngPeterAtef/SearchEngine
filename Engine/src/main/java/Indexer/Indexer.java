package Indexer;
import Database.Controller;

/* Java program to find a Pair which has maximum score*/

import Database.Controller.Data;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import opennlp.tools.stemmer.PorterStemmer;
import com.google.gson.*;
import java.lang.Integer;
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
import org.jsoup.safety.Safelist;
import org.jsoup.safety.Whitelist;
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
   private static PorterStemmer stemmer = new PorterStemmer();
    private static LinkedHashSet<String> allWords = new LinkedHashSet<String>();

    Controller DBControllerObj = new Controller(true);
    private static List<String>docs=new ArrayList<String>();
    // private static  FileWriter writ;
   /* public class syncho implements Runnable{
        //lock  = calling object => which is the thread
        private Indexer x;
        public syncho(Indexer y){
            x = y;
        }
        public void run()
        {
            Stack<String>tagstack=new Stack<>();
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
                HashMap <String,triple>tempo = new HashMap<String,triple>();

                ew++;
                String aword=itr.next();



                for (int k=0;k<docs.size();k++)// le kol doc
                {
                    triple temppair = new triple();
                    // temppair.positions=new ArrayList<>();

                    //  Elements selectResult=docs.get(k).getElementsContainingOwnText(aword);
                    int index = 0;
                    int tobegin=0,toend=0;
                    while (true)
                    {

                        index = docs.get(k).indexOf(aword, index);

                        if (index != -1)
                        {

                            tobegin=Math.max(docs.get(k).substring(0,index).lastIndexOf(" "),docs.get(k).substring(0,index).lastIndexOf(">"));
                            if(tobegin<0)
                            tobegin=0;

                            toend=Math.min(docs.get(k).indexOf("<", index),docs.get(k).indexOf(" ", index));
                         //   System.out.println("tobegin "+tobegin+" toend "+toend+" lenght"+docs.get(k).length()+" "+docs.get(k).length());
                            if(toend<=0)
                            {
                                //System.out.println("me"+docs.get(k).substring(tobegin,docs.get(k).length())+"me");
                                toend=docs.get(k).length();
                            }
//                            if(tobegin<=0)System.out.println("tobegin "+tobegin+"index "+index+" + "+" aword "+aword+" k "+k);
//                            if(toend<0)System.out.println("toend "+toend);
//                            if(k<0)System.out.println("k XD "+k);
                            //System.out.println(docs.get(k).substring(tobegin+1, toend));
                            //System.out.println("kio");
                            String l;
                            if(tobegin>=0)
                            l=docs.get(k).substring(tobegin+1, toend).replaceAll(" ","");
                            else
                                l=docs.get(k).substring(tobegin, toend).replaceAll(" ","");
                            System.out.println(l);
                            if(!aword.equals( p.stem(l) ) )
                            {

                                index=toend;
                                continue;
                            }
                            //System.out.println("hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                            temppair.count++;
                            temppair.positions.add(index);


                            index=toend;
                        }
                        else {
                            break;
                        }
                    }

                    if(temppair.count!=0)
                    {
//                        if(temppair.positions.size()!=0)
//                        {
                            Stack<String>ast=new Stack<String>();
                            getallparents(temppair.positions.get(0),k,ast);
                            BitSet allparen=new BitSet(15);
                            for(String g:ast)
                                setlocations(g,allparen);
                            temppair.location=allparen;
                        //}
                        tempo.put(CollectedData.get(k).url, temppair);
                    }



                }
                synchronized (this.x) {
//                    indexermap.put(aword,tempo);
                   // System.out.println(ew);
                    DBControllerObj.insertIndexedWord(aword, tempo);
                }


            }
        }
    }*/

    private static List<List<pair>>tagsList=new ArrayList<>();
    private static List<Data> CollectedData = null;// areay of websirtes
    private static HashMap<String, HashMap<String,triple>> indexermap = new HashMap<String, HashMap<String, triple>>();
    private static List<HashMap<String, HashMap<String, triple>>> subindexermap=new ArrayList<HashMap<String, HashMap<String,triple>>>();
    // word //url //occurrences
    private static List<String> stopwords = null;


    public static class pair{
        public String tagName;
        public Integer tagIndex;
    }

    public static class triple {
       public Integer count;
        // public BitSet location ;
        public BitSet location;
       // public List<Integer>positions;
        public Float TF;


        triple() {

//            location=new BitSet(11);
//            positions=new ArrayList<>();
            count = 0;
            TF = 0.0f;
        }
    }



    public static HashMap<String, BitSet> tosettags(Document adoc)
    {

        LinkedHashMap<String,BitSet>tempPerDoc=new LinkedHashMap<>();

        LinkedHashSet<String> title=new LinkedHashSet<>(Arrays.asList(adoc.title().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>h1=new LinkedHashSet<>(Arrays.asList(adoc.select("h1").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>h2=new LinkedHashSet<>(Arrays.asList(adoc.select("h2").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>h3=new LinkedHashSet<>(Arrays.asList(adoc.select("h3").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>h4=new LinkedHashSet<>(Arrays.asList(adoc.select("h4").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>h5=new LinkedHashSet<>(Arrays.asList(adoc.select("h5").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>h6=new LinkedHashSet<>(Arrays.asList(adoc.select("h6").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>nav=new LinkedHashSet<>(Arrays.asList(adoc.select("nav").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>header=new LinkedHashSet<>(Arrays.asList(adoc.select("header").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>main=new LinkedHashSet<>(Arrays.asList(adoc.select("main").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        LinkedHashSet<String>p=new LinkedHashSet<>(Arrays.asList(adoc.select("p").text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" ")));
        BitSet returns;
        title.removeAll(stopwords);h1.removeAll(stopwords);h2.removeAll(stopwords);h3.removeAll(stopwords);
        h4.removeAll(stopwords);h5.removeAll(stopwords);h6.removeAll(stopwords);nav.removeAll(stopwords);
        header.removeAll(stopwords);main.removeAll(stopwords);p.removeAll(stopwords);

        LinkedHashSet<String> atempset=new LinkedHashSet<>();

///////////////////////////////////////////////////////////
        Iterator iter = title.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        title.clear();
        title.addAll(atempset);
        atempset.clear();

        iter = h1.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        h1.clear();
        h1.addAll(atempset);
        atempset.clear();


        iter = h2.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        h2.clear();
        h2.addAll(atempset);
        atempset.clear();

        iter = h3.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        h3.clear();
        h3.addAll(atempset);
        atempset.clear();

        iter = h4.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        h4.clear();
        h4.addAll(atempset);
        atempset.clear();

        iter = h5.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        h5.clear();
        h5.addAll(atempset);
        atempset.clear();

        iter = h6.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        h6.clear();
        h6.addAll(atempset);
        atempset.clear();

        iter = nav.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        nav.clear();
        nav.addAll(atempset);
        atempset.clear();

        iter = header.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        header.clear();
        header.addAll(atempset);
        atempset.clear();

        iter = main.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        main.clear();
        main.addAll(atempset);
        atempset.clear();

        iter = p.iterator();
        while (iter.hasNext())
        {

            atempset.add(stemmer.stem(iter.next().toString())) ;
        }
        p.clear();
        p.addAll(atempset);
        atempset.clear();
       /////////////////////////////////////////////////////////////////////////////////
        for(String s:title)
        {
            BitSet b=new BitSet(11);
            b.set(10);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(10);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:h1)
        {
            BitSet b=new BitSet(11);
            b.set(9);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(9);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:h2)
        {
            BitSet b=new BitSet(11);
            b.set(8);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(8);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:h3)
        {
            BitSet b=new BitSet(11);
            b.set(7);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(7);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:h4)
        {
            BitSet b=new BitSet(11);
            b.set(6);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(6);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:h5)
        {
            BitSet b=new BitSet(11);
            b.set(5);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(5);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:h6)
        {
            BitSet b=new BitSet(11);
            b.set(4);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(4);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:nav)
        {
            BitSet b=new BitSet(11);
            b.set(3);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(3);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:header)
        {
            BitSet b=new BitSet(11);
            b.set(2);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(2);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:main)
        {
            BitSet b=new BitSet(11);
            b.set(1);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(1);
                tempPerDoc.put(s,returns);
            }

        }
        for(String s:p)
        {
            BitSet b=new BitSet(11);
            b.set(0);

            returns=tempPerDoc.put(s,b);
            if(returns!=null)
            {
                returns.set(0);
                tempPerDoc.put(s,returns);
            }

        }
          return tempPerDoc;

    }

    public static void main(String[] arg) {
        try {

            Indexer indexerObj = new Indexer();

//            CollectedData = indexerObj.DBControllerObj.GetCollectedData();
//            if (CollectedData != null){
//                System.out.println(CollectedData.size());
//            }
//            else {
//                System.out.println("Null");
//            }
            //-----------------------------------------

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // create a reader
          Reader reader = Files.newBufferedReader(Paths.get("E:/Koleya/APT/proj/SearchEngine/Engine/CollectedData.json"));


          CollectedData = gson.fromJson(reader, new TypeToken<List<Data>>() {}.getType());// array of json objects websites
            reader.close();
            // array of stop words
            stopwords = Files.readAllLines(Paths.get("stop_words_english.txt"));



            for (int i = 0; i < CollectedData.size(); i++) {
//                if(CollectedData.get(i).visited==true)
//                    continue;

                Document adoc=Jsoup.parse(CollectedData.get(i).html.toLowerCase());//with tags

                 List<String> allWords = new ArrayList<String>();
                List<String> allWordstemp = new ArrayList<String>();
                HashMap<String,BitSet>importantWords=  tosettags(adoc);



                //select tags to be added to list of tags in each web
                adoc.select("script,.hidden,style,img,link,figure,pre,path,meta,br,base,col,command,area,param,wbr,track,source,keygen,embed").remove();
                allWords.addAll(Stream.of(adoc.text().replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split(" "))
                        .collect(Collectors.toCollection(LinkedHashSet<String>::new)));// array of all words in page
                allWords.removeAll(stopwords);// remove all blabla
                Iterator iter = allWords.iterator();

                while (iter.hasNext())
                {

                   allWordstemp.add(stemmer.stem(iter.next().toString())) ;
                }

                allWords.clear();

//                allWords.addAll(allWordstemp);
//                allWordstemp.clear();

                for(String s:allWordstemp)
                {
                    if(s.equals("")||s.equals(" "))
                        continue;
                   if(!indexermap.containsKey(s))//word not yet in db
                   {


                       HashMap<String,triple>aNewWeb=new HashMap<>();
                       triple WebTriple=new triple();

                       WebTriple.count=1;
                       if(importantWords.containsKey(s))
                           WebTriple.location=importantWords.get(s);
                       else WebTriple.location=new BitSet();
                       WebTriple.TF=1.0f/allWordstemp.size();
                       aNewWeb.put(CollectedData.get(i).url,WebTriple);
                       indexermap.put(s,aNewWeb);
                   }
                   else if(!indexermap.get(s).containsKey(CollectedData.get(i).url))//word in db but in another web
                   {
                       triple WebTriple=new triple();
                       WebTriple.count=1;
                       if(importantWords.containsKey(s))
                           WebTriple.location=importantWords.get(s);
                       else WebTriple.location=new BitSet();
                       WebTriple.TF=1.0f/allWordstemp.size();
                       indexermap.get(s).put(CollectedData.get(i).url,WebTriple);

                   }
                else{//word in db and same website
                       indexermap.get(s).get(CollectedData.get(i).url).TF+=1.0f/allWordstemp.size();
                       indexermap.get(s).get(CollectedData.get(i).url).count++;

                }

                }

                   //url string


                System.out.println("mo");
                CollectedData.get(i).html = "";

            }















            System.out.println(docs.size());
            System.out.println(allWords.size());

//            Indexer indo=new Indexer();
//            syncho ob=indo.new syncho(indo);
//            Thread t0=new Thread(ob);
//            Thread t1=new Thread(ob);Thread t2=new Thread(ob);Thread t3=new Thread(ob);Thread t4=new Thread(ob);Thread t5=new Thread(ob);Thread t6=new Thread(ob);Thread t7=new Thread(ob);Thread t8=new Thread(ob);Thread t9=new Thread(ob);
//            t0.setName("0");t1.setName("1");t2.setName("2");t3.setName("3");t4.setName("4");t5.setName("5");t6.setName("6");t7.setName("7"); t8.setName("8"); t9.setName("9");
//            t0.start();t1.start();t2.start();t3.start();t4.start();t5.start();t6.start();t7.start();   t8.start();   t9.start();
//            t0.join();t1.join();t2.join();t3.join();t4.join();t5.join();t6.join();t7.join();t8.join();t9.join();
            System.out.println("joined");
//            System.out.println(indexermap.size());

            indexerObj.DBControllerObj.indexerone(indexermap,CollectedData.size());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}