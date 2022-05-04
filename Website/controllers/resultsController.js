//for POST requests 
let bodyParser=require('body-parser');
//Database model (used to find, insert items..)
let results = require('../models/resultsModel');
// var ind=require('../models/indexer');
let indexer = require('../models/indexerModel');
const { db } = require('../models/resultsModel');
let getTitleAtUrl = require('get-title-at-url');


let minib=[];
var st=0;
let btnnumbs;
let Allresult = [];
let query = '';
function compare(a, b){
    if ( a.occurance < b.occurance ){
      return 1;
    }
    if ( a.occurance > b.occurance ){
      return -1;
    }
    return 0;
};
var stemmer = require('porter-stemmer').stemmer;
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
                data[0].list.sort(compare);
            }
            for (let index = 0; index < data[0].list.length; index++) {
                getTitleAtUrl(data[0].list[index].url, function(title){
                    console.log(title);
                    console.log(data[0].list[index].url);
                    Allresult.push({"url": data[0].list[index].url, "title": title, "snippet":"loremereorververvetg"});
                });
            }
            btnnumbs = Math.ceil(Allresult.length/5);
            let x =0;
            minib =[];
            res.redirect('http://localhost:3000/results/'+query+'/1');
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
        console.log(Allresult);
        var starting = (parseInt(req.params.i)-1)*5;
        let k=0;
        minib =[];
        for(let j=starting;j<Math.min(starting+5,Allresult.length);j++)
        {
            minib[k] = Allresult[j];
            k++;
        }
        res.render('results',{minib,btnnumbs,word: query});
    });
};