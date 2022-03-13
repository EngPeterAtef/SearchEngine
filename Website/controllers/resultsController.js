//for POST requests 
var bodyParser=require('body-parser');
//Database model (used to find, insert items..)
// var results = require('../models/resultsModel');

module.exports = function (app) {
    //to open results page
    app.get('/results', function (req, res) {
        res.render('results');
    });

    // var urlencodedParser = bodyParser.urlencoded({extended:false});
    // app.post('/results',urlencodedParser,function (req, res) {
    
    // });
};