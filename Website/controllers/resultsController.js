//for POST requests 
var bodyParser=require('body-parser');
//Database model (used to find, insert items..)
var results = require('../models/resultsModel');
var ind=require('../models/indexer');

module.exports = function (app) {
    //to open results page
    app.get('/results', function (req, res) {

        res.render('results');
        const aname=new resultsModel("no");
        aname.save().then();
    });

    // app.post('../views/results',(req,res)=>{
    //     const aname=new resultsModel(req.body);
    //     aname.save().then();
        
       
    //     });

    // var urlencodedParser = bodyParser.urlencoded({extended:false});
    // app.post('/results',urlencodedParser,function (req, res) {
    
    // });
};