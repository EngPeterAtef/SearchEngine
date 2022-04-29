package Indexer;
import opennlp.tools.stemmer.PorterStemmer;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
// import org.jsoup.Connection;
// import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;
// import org.jsoup.nodes.Element;
//import java.lang.*;



//import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.*;
import java.util.Map.Entry;

public class Indexer{
    private static List<Data> CollectedData = null;// areay of websirtes
    private static HashMap<String, HashMap<String, pair>> indexermap = new HashMap<String, HashMap<String, pair>>();
    // word //url //occurrences
    private static List<String> stopwords = null;

    // class to specify the structure of HTML (Collected data) JSON objects
    public class Data {
        public String url;
        public String html;

        Data(String url, String html) {
            this.url = url;
            this.html = html;
        }
    }

    public static class pair {
        public Integer count;
        // public BitSet location ;
        public BitSet location;

        pair() {

            location=new BitSet(15);
            count = 0;
            // location = new char[15];
            //  location = 0;
            // this.count = count;
            // this.location = location;
        }
    }

//    static void setlocations(String mytag, Integer locations) {
//        switch (mytag) {
//            case "title":
//                locations =1;
//                System.out.println("yes");break;
//            case "h1":
//                locations = 2;
//                System.out.println("yes");break;
//            case "h2":
//                locations= 3;
//                System.out.println("yes");break;
//            case "h3":
//                locations= 4;
//                System.out.println("yes");break;
//                case "h4":
//                locations=5;
//                System.out.println("yes");break;
//            case "h5":
//                locations=6;
//                System.out.println("yes");break;
//            case "h6":
//                locations=7;
//                System.out.println("yes");break;
//            case "nav":
//                locations=8;
//                System.out.println("yes");break;
//            case "header":
//                locations=9;
//                System.out.println("yes");break;
//            case "li":
//                locations=10;
//                System.out.println("yes");break;
//            case "main":
//                locations=11;
//                System.out.println("yes");break;
//            case "section":
//                locations=12;
//                System.out.println("yes"+locations.toString());break;
//            case "p":
//                locations=13;
//                System.out.println("yes");break;
//            case "side":
//                locations=14;
//                System.out.println("yes");break;
//            case "footer":
//                locations=15;
//                System.out.println("yes");break;
//                case "<div>":
//                locations=16;
//                System.out.println("yes");break;
//            default:
//                System.out.println("no");break;
//        }
//        return;
//    }

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
                break;

        }
        return;

    }

    public static void main(String[] arg) {
        try {
            // create Gson instance
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("E:/Koleya/APT/proj/SearchEngine/Engine/data.json"));
            CollectedData = gson.fromJson(reader, new TypeToken<List<Data>>() {
            }.getType());// array of json objects websites
            reader.close();
            // array of stop words
            stopwords = Files.readAllLines(Paths.get("E:/Koleya/APT/proj/SearchEngine/Engine/stop_words_english.txt"));

            // intilaise alll words strings
            LinkedHashSet<String> allWords = new LinkedHashSet<String>();
            LinkedHashSet<String> allWords2 = new LinkedHashSet<String>();

            for (Data w : CollectedData) {
                allWords.addAll(Stream.of(Jsoup.parse(w.html).text().toLowerCase().split(" "))
                        .collect(Collectors.toCollection(LinkedHashSet<String>::new)));// array of all words in page


            }
            PorterStemmer p = new PorterStemmer();

            FileWriter writerr = new FileWriter("E:/Koleya/APT/proj/mywords.txt");


            Iterator itery = allWords.iterator();

            while (itery.hasNext())
            {

                allWords2.add(itery.next().toString().replaceAll("[^a-zA-Z0-9_]", "")) ;



            }

            allWords2.removeAll(stopwords);// remove all blabla


            Iterator iter = allWords2.iterator();
            allWords.clear();
          //  int cou=0;
            while (iter.hasNext())
            {
                allWords.add(p.stem(iter.next().toString().replaceAll("[^a-zA-Z0-9_-]", ""))) ;
               // cou++;
            }
           int z=0;
//            for (String elemett:allWords)
//            {
//
//                writerr.write(elemett);
//
//                writerr.write(System.lineSeparator());
//            }
            ////////////////////////////////////////////
            writerr.close();
            // h1 h2 h3 h4
            // 0 0 0 0
            // 1 0 0 0

            // hero ->wikipidea->hero title
            // hero footer
            // hero body
            // yahoo
            // facebook
            // vilian
            //     FileWriter writerr = new FileWriter("E:/Koleya/APT/proj/SearchEngine/Engine/output.txt");
            //    // Document doc = Jsoup.parse(CollectedData.get(0).html);
            //     writerr.write(CollectedData.get(0).html.replaceAll("\\<.*?\\>", ""));

int k=0;
int ew=0;

            FileWriter writer2 = new FileWriter("E:/Koleya/APT/proj/SearchEngine/Engine/tm.txt");

            writer2.write("WORD       URL          count         locations\n");

             for (String aword : allWords)// ma3aya kelma
             {
               // String aword="ukraine";
                 if(aword.equals(""))
                     continue;
                HashMap <String,pair>tempo = new HashMap<String, pair>();
                ew++;System.out.println(ew);
                // indexermap.put(aword,tempo);//add my word
                for (Data w : CollectedData)// le kol doc
                {
                    k++;if(k==4)break;
                    pair temppair = new pair();
                    Document doc = Jsoup.parse(w.html);

                   // int counter = 0;

                    for (Element e : doc.select("*:containsOwn(" + aword + ")")) {// kol el elements ely gowahom el
                                                                                  // kelma dyh
                        // indexermap.get(aword).get(w.url).count++;//increment occurence
                        temppair.count++;
//                        for (Element h : e.parents())// each tag
//                        {
//                           // System.out.println(h.tagName());
//                            setlocations(h.tagName(), temppair.location);// set all locations
//
//
//                        }

                    }

                    tempo.put(w.url, temppair);
                    writer2.write(aword+"        "+w.url+"       "+temppair.count+"       "+temppair.location);
                }

                indexermap.put(aword, tempo);
               // System.out.println("hello there");

             }
            // HashMap te=new HashMap<String,char[]>();
            // te.put("mimi",'y');
            // indexermap.put("hello",te);

         //   FileWriter writer = new FileWriter("E:/Koleya/APT/proj/SearchEngine/Engine/tm.txt");

//writer.write("WORD       URL          count         locations\n");
         //   Iterator<Entry<String, HashMap<String, pair>>> it = indexermap.entrySet().iterator();

//            while (it.hasNext()) {
//                // System.out.println("nono");
//                Entry<String, HashMap<String, pair>> entry = it.next();
//                writer.write(entry.getKey()+" ------> ");
//                Iterator<Entry<String, pair>> itt = entry.getValue().entrySet().iterator();
//                while (itt.hasNext()) {
//                    Entry<String, pair> en2 = itt.next();
//                   String kk= IntStream.range(0, en2.getValue().location.length())
//                            .mapToObj(b -> String.valueOf(en2.getValue().location.get(b) ? 1 : 0))
//                            .collect(Collectors.joining());
//                   writer.write(en2.getKey()+" ------> ");
//                    writer.write(en2.getValue().count.toString()+" ---------> "+kk+System.lineSeparator());
//
//
//                }
//            }
//            writer.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
