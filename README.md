<h1 align="center">
    <span style="color: #4285F4">N</span><span style="color: red">o</span><span style="color: orange">o</span><span style="color: #4285F4">d</span><span style="color: green">l</span><span style="color: red">e</span>
</h1> 

<div>
    <img src="/search.png" title="Search">
</div>
<h2>
    Description
</h2>
<p>
    Crawler-based search engine that demonstrates the main features of a search engine (web crawling, indexing and ranking) and the interaction between them.
</p>
<h2>
    How does a search engine work?
</h2>
<p>
    <ul>
        <li><h3>Web Crawler (AKA Spider, Robot)</h3></li>
        A software program that traverses web pages, downloads them and follows the hyperlinks that are referenced on these pages. 
        <li><h3>Indexer</h3></li>
        Processes the downloaded HTML documents from the crawler, builds a data structure that stores the words contained in each document in the form of (inverted file) and their importance.
        <li><h3>Ranker</h3></li>
        Sorts documents based on their popularity and relevance to the search query.
    </ul>
</p>

<h2>
    Features
</h2>
<div>
    <img src="/phraseSearch.png" title="Search">
</div>
<ul>
    <li>Voice recognition search</li>
    <li>Suggestion mechanism that auto completes your search query </li>
    <li>Single and multiple words searching</li>
    <li>Phrase searching</li>
    <li>Result appears with snippets of the text containing queries words</li>
    <li>Pagination of results</li>
</ul>
<h2>
    Implemetation
</h2>
<p>
    The main search engine modules (Crawler, Indexer, Ranker) are implemented completely using Java and built using Gradle.<br>
    The website is implemented using Java Spring framework and Thymeleaf template engine which leads us to having the whole project written in java.<br>
    The repo contains another version of the website written in NodeJS but it isn't updated to our latest ranking algorithms due to time limitations, however, this version was not required, we were just practising NodeJS :)
</p>
<h2>
    How To Run
</h2>
<p>
    <h5>Crawler / Indexer:</h5>
    <ol>
        <li>Open "Engine" as a project folder using your IDE.</li>
        <li>Open build.gradle and click "load gradle changes".</li>
        <h6> To run the Crawler:</h6>
        Run the main function in Crawler.java
        <h6>To run the Indexer:</h6>
        Run the main function in Indexer.java
    </ol>
    <h5>Website:</h5>
    <ol>
        <li>Open Website/Spring as a project folder using your IDE.</li>
        <li>Run the main function in Application.java</li>
        <li>Go to localhost:8080/ and start searching!</li>
    </ol> 
</p>
<ul>
  
</ul>

<h2>
    Limitations
</h2>
<ul>
   <li>Order of words in the query doesn't affect the result, ex: (barcelona team) is equal to (team barcelona).</li>
   <li>Search is slow when query contains multiple words and all the query words exists in a large number of pages.</li>
   <li>All snippets are shown in lowercase.</li>
   <li>Snippets in multiple word query may not work properly.</li>
   <li>Snippets may not contain the query word.</li>
   <li>Complex phrase searching is not supported, ex: ("Mark Zuckerberg" "Twilight Saga") is treated as ("Mark Zuckerberg Twilight Saga").</li>
</ul>

<h2>
    Authors
</h2>
<ul>
   <li>Bemoi Erian</li>
   <li>Mark Yasser</li>
   <li>Peter Atef</li>
   <li>Doaa Ashraf</li>
</ul>