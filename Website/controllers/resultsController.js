//for POST requests 
let bodyParser=require('body-parser');
//Database model (used to find, insert items..)
let results = require('../models/resultsModel');
let indexer = require('../models/indexerModel');
let websiteData = require('../models/websiteDataModel');
let collectedData = require('../models/collectedDataModel');
const { db } = require('../models/resultsModel');
let getTitleAtUrl = require('get-title-at-url');
const cheerio =  require('cheerio');
const { json } = require('express/lib/response');
const res = require('express/lib/response');

let minib=[];
var st=0;
let btnnumbs;
let Allresult = [];
var ArrayOfquery = [];
let query = '';
var queryString = '';
let dataArray = [];
var start;
var c=0;

let matchingArray=[];
function compare(a, b)
{
    if ( a.rank < b.rank ){
        return 1;
    }
    if ( a.rank > b.rank ){
        return -1;
    }
    return 0;
};
async function FindAndRank(res)
{
    let fn_arr = [];

    for (let i = 0; i < ArrayOfquery.length; i++) 
    {
        query = stemmer(ArrayOfquery[i].replace('"',"")).toLowerCase();
        fn_arr.push(indexer.find({Word: query}).exec());
        
    }
    const resultArray = await Promise.all(fn_arr);
    //resultArray[0][0] contains object of 1st word
    //resultArray[1][0] contains object of 2nd word
    //resultArray[2][0] contains object of 3rd word
    if(queryString[0] === '"' && queryString[queryString.length-1] === '"')
    {
        let queryString_2 = queryString.replaceAll('"',"");
        for (let j = 0; j < resultArray[0][0].Websites.length; j++) // loop on each url in the first word
        {
            const siteResult = await collectedData.find({url: resultArray[0][0].Websites[j].URL}).exec();
            if(siteResult[0] != null)
            {
                if(siteResult[0].html.indexOf(queryString_2)  != -1)
                {
                    matchingArray.push(resultArray[0][0].Websites[j].URL);
                }
            }
        }
        let IDF = Math.log(5000/matchingArray.length);
        let TF = 0;
        let TF_IDF = 0;
        //get all the websites that has this word
        for (let index = 0; index < matchingArray.length; index++) 
        {
            const siteResult = await websiteData.find({url: matchingArray[index]}).exec();
            if(siteResult[0] != null)
            {
                let str = siteResult[0].body;
                let indexOFquery = str.search(query);
                //resultArray[i][0].Websites[index].title = siteResult[0].title;
                //resultArray[i][0].Websites[index].snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length));
                dataArray.push({"url": matchingArray[index], "title": siteResult[0].title, "snippet": str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length))});
                //TF = resultArray[i][0].Websites[index].locations / str.length;
                //TF_IDF = TF * IDF;
                dataArray[c].rank =  0;
                c++;                   
            }
        }
    }
    else
    {
        for (let i = 0; i < ArrayOfquery.length; i++) 
        {
            if(resultArray.length <= i)
            {
                return
            }       
            if(resultArray[i].length > 0)
            {
                let IDF = Math.log(5000/resultArray[i][0].Websites.length);
                let TF = 0;
                let TF_IDF = 0;
                let Popularity = 1;
                //get all the websites that has this word
                for (let index = 0; index < resultArray[i][0].Websites.length; index++) 
                {
                    const siteResult = await websiteData.find({url: resultArray[i][0].Websites[index].URL}).exec();
                    if(siteResult[0] != null)
                    {
                        let str = siteResult[0].body;
                        let indexOFquery = str.search(query);
                        resultArray[i][0].Websites[index].title = siteResult[0].title;
                        resultArray[i][0].Websites[index].snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length));
                        dataArray.push({"url": resultArray[i][0].Websites[index].URL, "title": resultArray[i][0].Websites[index].title, "snippet": resultArray[i][0].Websites[index].snippet});
                        Popularity =  siteResult[0].popularity;
                        TF = resultArray[i][0].Websites[index].positions.length / str.length;
                        TF_IDF = TF * IDF;
                        dataArray[index].rank =  TF_IDF * Popularity;
                    }
                }
            }
        }
    }
    fillResults();
    res.redirect('http://localhost:3000/results/'+queryString+'/1')
};


function fillResults() {
                
    if(dataArray.length > 0)
    {
        dataArray.sort(compare);
        dataArray = Array.from(new Set(dataArray));
        for (let index = 0; index < dataArray.length; index++) 
        {
            Allresult.push({"url": dataArray[index].url, "title": dataArray[index].title, "snippet":dataArray[index].snippet});
        }
        btnnumbs = Math.ceil(Allresult.length/10);
        minib = [];
        endtime = new Date() - start;
        console.log("Found "+dataArray.length+" results "+endtime + " ms");
        dataFound = dataArray.length;
    }
};


var stemmer = require('porter-stemmer').stemmer;
var endtime=0;
var dataFound=0;
module.exports = function (app) {
    //to open results page
    app.get('/results/:item', function (req, res) {
        //---------------------------------
        //------------RANKER---------------
        //---------------------------------
        dataArray = [];
        Allresult = [];
        matchingArray = [];
        dataFound=0;
        btnnumbs=0;
        start = new Date();
        queryString = req.params.item;
        ArrayOfquery = queryString.split(" ");
        c = 0;
        //here we loop on each word of the user's input
        FindAndRank(res);
    });
    


    app.get("/results/:item/:i",(req,res) => {
        var starting = (parseInt(req.params.i)-1)*5;
        let k=0;
        minib =[];
        for(let j=starting;j<Math.min(starting+10,Allresult.length);j++)
        {
            minib[k] = Allresult[j];
            k++;
        }
        res.render('results',{minib,btnnumbs,dataFound,endtime,word: queryString});
    });
};

