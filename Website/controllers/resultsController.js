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
module.exports = function (app) {
    //to open results page
    app.get('/results', function (req, res) {
        results.find().then((arr)=>{
            Allresult = arr;
            btnnumbs=Math.ceil(arr.length/5);
            let x =0;
            minib =[];
        for(let j=0;j<Math.min(5,arr.length);j++)
        {
            minib[x]=arr[j];
            x++;
        }
        res.render('results',{minib,btnnumbs});
        });
    
    });
    app.get("/results/:i",(req,res) => {
        var starting=  (parseInt(req.params.i)-1)*5;
        let k=0;
        minib =[];
        for(let j=starting;j<Math.min(starting+5,Allresult.length);j++)
        {
            minib[k]=Allresult[j];
            k++;
        }
        res.render('results',{minib,btnnumbs});
    });
};