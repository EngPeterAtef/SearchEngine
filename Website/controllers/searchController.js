//for POST requests 
var bodyParser=require('body-parser');
//Database model (used to find, insert items..)
// var results = require('../models/resultsModel');

module.exports = function (app) {
    //to open search page
    app.get('/search', function (req, res) {
        res.render('search');
    });

    //for search suggestions
    var urlencodedParser = bodyParser.urlencoded({extended:false});
    app.post('/search',urlencodedParser,function (req, res) {
    
    });
};