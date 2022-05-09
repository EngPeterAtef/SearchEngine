//for POST requests 
let bodyParser=require('body-parser');
//Database model (used to find, insert items..)
let results = require('../models/resultsModel');
// var ind=require('../models/indexer');
let indexer = require('../models/indexerModel');
let websiteData = require('../models/websiteDataModel');
const { db } = require('../models/resultsModel');
let getTitleAtUrl = require('get-title-at-url');
const cheerio =  require('cheerio');
//parser
// import { parse } from 'node-html-parser';
//var htmlParser = require('html-parser');


let minib=[];
var st=0;
let btnnumbs;
let Allresult = [];
let query = '';
function compare(a, b){
    if ( a.tf_idf < b.tf_idf ){
      return 1;
    }
    if ( a.tf_idf > b.tf_idf ){
      return -1;
    }
    return 0;
};
var stemmer = require('porter-stemmer').stemmer;
var endtime=0;
module.exports = function (app) {
    //to open results page
    app.get('/results/:item', function (req, res) {
        query = stemmer(req.params.item);
        console.log(query);
        Allresult = [];
        //---------------------------------
        //------------RANKER---------------
        //---------------------------------
        indexer.find({word: {$regex:query}}, function (error, data){
            if (error) {
                throw error;
            }
            if(data !== null){
            }
            var start = new Date();

            let fs = require('fs');
            //let crawlerData = JSON.parse(fs.readFileSync('../Engine/data.json', 'utf8'));
            //let queuedata = JSON.parse(fs.readFileSync('../Engine/queue.json', 'utf8'));
            let IDF = Math.log(5000/data[0].list.length);
            let TF = 0;
            let TF_IDF =0;
            
            for (let index = 0; index < data[0].list.length; index++) {
                //const $ = cheerio.load(markup);
                console.log(data[0].list[index].url);
                websiteData.find({url: data[0].list[index].url} ,  function (error, Webdata) {
                    if (error) {
                        throw error;
                    }
                    //let result = crawlerData.find(obj => obj.url === data[0].list[index].url);
                    let str = Webdata[0].body;
                    let indexOFquery = str.search(query);
                    data[0].list[index].title = Webdata[0].title;
                    
                    data[0].list[index].snippet = str.substring(Math.max(0,indexOFquery - 200),Math.min(indexOFquery + query.length + 300,str.length));
                    TF =  data[0].list[index].occurance / str.length;
                    //console.log(str.length +'\n');
                    TF_IDF = TF * IDF ;
                    data[0].list[index].tf_idf = TF_IDF;

                });
            }
            setTimeout(function () {
                data[0].list.sort(compare);
                for (let index = 0; index < data[0].list.length; index++) {
                    Allresult.push({"url": data[0].list[index].url, "title": data[0].list[index].title, "snippet":data[0].list[index].snippet});
                }
                //console.log( Allresult);
                btnnumbs = Math.ceil(Allresult.length/5);
                let x =0;
                minib =[];
                res.redirect('http://localhost:3000/results/'+query+'/1');
            }, 50);
            

            endtime = new Date() - start;
            
            //let time = console.timeEnd('Execution Time');
            console.log("Found "+data[0].list.length+" results "+endtime + " ms");
            //data[0].t = end;
            data[0].length = data[0].list.length;
        //---------------------------------
        //---------------------------------
        });

        // results.find({title:{$regex:query} }, function (error, data){
        //     if (error) {
        //         throw error;
        //     }
        //     Allresult = data;
        //     btnnumbs = Math.ceil(data.length/5);
        //     let x =0;
        //     minib =[];
        //     // for(let j=0;j<Math.min(5,arr.length);j++)
        //     // {
        //     //     minib[x]=arr[j];
        //     //     x++;
        //     // }
        //     // res.render('results',{minib,btnnumbs,word});

        //     res.redirect('http://localhost:3000/results/'+query+'/1');
        // });
        
    });
    
    app.get("/results/:item/:i",(req,res) => {
        //console.log(Allresult);
        var starting = (parseInt(req.params.i)-1)*5;
        let k=0;
        minib =[];
        for(let j=starting;j<Math.min(starting+5,Allresult.length);j++)
        {
            minib[k] = Allresult[j];
            k++;
        }
        res.render('results',{minib,btnnumbs,endtime,word: query});
    });
};