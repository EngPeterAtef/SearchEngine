package Indexer;
import Database.Controller;

/* Java program to find a Pair which has maximum score*/

import Database.Controller.Data;
import opennlp.tools.stemmer.PorterStemmer;
import java.lang.Integer;

//import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.*;



public class Indexer {
   private static PorterStemmer stemmer = new PorterStemmer();
    private static LinkedHashSet<String> allWords = new LinkedHashSet<String>();

    Controller DBControllerObj = new Controller(true);
    private static List<String>docs=new ArrayList<String>();
    private static List<Data> CollectedData = null;// areay of websirtes
    private static HashMap<String, HashMap<String,triple>> indexermap = new HashMap<String, HashMap<String, triple>>();
    // word //url //occurrences
    private static List<String> stopwords = null;


    public static class pair{
        public String tagName;
        public Integer tagIndex;
    }

    public static class triple {
       public Integer count;
        public BitSet location;
        public Float TF;


        triple() {
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

            CollectedData = indexerObj.DBControllerObj.GetCollectedData();
            if (CollectedData != null){
                System.out.println(CollectedData.size());
            }
            else {
                System.out.println("Null");
            }
            //-----------------------------------------


            // array of stop wordss
            stopwords = Files.readAllLines(Paths.get("stop_words_english.txt"));



            for (int i = 0; i < CollectedData.size(); i++) {
                if(CollectedData.get(i).visited==true)
                    continue;

                Document adoc=Jsoup.parse(CollectedData.get(i).html.toLowerCase());//with tags
                CollectedData.get(i).html = "";
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
                CollectedData.get(i).visited=true;
                indexerObj.DBControllerObj.UpdateVisitedInCollectedData(CollectedData.get(i).url, true);
                //url string
                System.out.println(i);
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

            indexerObj.DBControllerObj.indexerone(indexermap,CollectedData.size());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}