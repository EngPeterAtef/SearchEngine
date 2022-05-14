//for POST requests 
let bodyParser=require('body-parser');
//Database model (used to find, insert items..)
let results = require('../models/resultsModel');
let indexer = require('../models/indexerModel');
let websiteData = require('../models/websiteDataModel');
const { db } = require('../models/resultsModel');
let getTitleAtUrl = require('get-title-at-url');
const cheerio =  require('cheerio');
const { json } = require('express/lib/response');

let minib=[];
var st=0;
let btnnumbs;
let Allresult = [];
var ArrayOfquery = [];
let query = '';
var queryString = '';
var dataArray = [];
var start;
var c=0;
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
async function FindAndRank()
{
    let fn_arr = [];

    for (let i = 0; i < ArrayOfquery.length; i++) 
    {
        fn_arr.push(indexer.find({word: {$regex:query}}).exec());
    }
    const resultArray = await Promise.all(fn_arr);
    let ads = JSON.parse(resultArray);
    for (let i = 0; i<ads.length; i++){
        console.log(ads[i].word); 
    }
    for (let i = 0; i < ArrayOfquery.length; i++) 
    {   

        query = stemmer(ArrayOfquery[i]);
        //console.log(query);
        // find the word that the user is searching for
        // indexer.find({word: {$regex:query}}, function (error, data)
        // {
        //     if (error) {
        //             console.log("hello error");
        //             throw error;
        //         }
        //         if(data.length >0)
        //         {
        //             let IDF = Math.log(5000/data[0].list.length);
        //             //console.log(IDF);
        //             let TF = 0;
        //             let TF_IDF =0;
        //             //get all the websites that has this word
        //             for (let index = 0; index < data[0].list.length; index++) 
        //             {
        //                 websiteData.find({url: data[0].list[index].url} ,  function (error, Webdata) {
        //                     if (error) {
        //                         throw error;
        //                     }
        //                     if(Webdata[0] != null)
        //                     {
        //                         let str = Webdata[0].body;
        //                         let indexOFquery = str.search(query);
        //                         data[0].list[index].title = Webdata[0].title;
        //                         data[0].list[index].snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length));
        //                         dataArray.push({"url": data[0].list[index].url, "title": data[0].list[index].title, "snippet": data[0].list[index].snippet});
        //                         TF = data[0].list[index].occurance / str.length;
        //                         TF_IDF = TF * IDF;
        //                         dataArray[c].rank =  TF_IDF;
        //                         c++;                   
        //                     }
        //                 });
        //             }
        //         }
        // });
    }
};


function fillResults() {
                
    if(dataArray.length > 0)
    {
        dataArray.sort(compare);
        for (let index = 0; index < dataArray.length; index++) 
        {
            Allresult.push({"url": dataArray[index].url, "title": dataArray[index].title, "snippet":dataArray[index].snippet});
        }
        // console.log(dataArray);
        btnnumbs = Math.ceil(Allresult.length/5);
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
        start = new Date();
        queryString = req.params.item;
        ArrayOfquery = queryString.split(" ");
        c = 0;
        //here we loop on each word of the user's input
        FindAndRank();
        // when the list of data is ready send it to the front end


        //fillResults();
        //res.redirect('http://localhost:3000/results/'+queryString+'/1');

    });
    


    app.get("/results/:item/:i",(req,res) => {
        var starting = (parseInt(req.params.i)-1)*5;
        let k=0;
        minib =[];
        for(let j=starting;j<Math.min(starting+5,Allresult.length);j++)
        {
            minib[k] = Allresult[j];
            k++;
        }
        res.render('results',{minib,btnnumbs,dataFound,endtime,word: queryString});
    });
};

