//for POST requests 
var bodyParser=require('body-parser');
//Database model (used to find, insert items..)
var results = require('../models/resultsModel');
var ind=require('../models/indexer');
const { db } = require('../models/resultsModel');

let minib=[];
var st=0;
let btnnumbs;
let Allresult = [];
let word = '';

var stemmer = require('porter-stemmer').stemmer;
module.exports = function (app) {
    //to open results page
    app.get('/results/:item', function (req, res) {
        word = stemmer(req.params.item);
        console.log(word);
        results.find({title:{$regex:word} }, function (error, data){
            if (error) {
                throw error;
            }
            Allresult = data;
            btnnumbs = Math.ceil(data.length/5);
            let x =0;
            minib =[];
        // for(let j=0;j<Math.min(5,arr.length);j++)
        // {
        //     minib[x]=arr[j];
        //     x++;
        // }
        // res.render('results',{minib,btnnumbs,word});
        res.redirect('http://localhost:3000/results/'+word+'/1');
        });
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
        res.render('results',{minib,btnnumbs,word});
    });
};