//for POST requests 
var bodyParser=require('body-parser');
//Database model (used to find, insert items..)
var searchModel = require('../models/searchModel');

module.exports = function (app) {
    //to open search page
    app.get('/', function (req, res) {
        res.render('search');
    });
    app.get('/search', function (req, res) {
        res.render('search');
    });

    //for search suggestions
    var urlencodedParser = bodyParser.urlencoded({extended:false});
    app.post('/search',urlencodedParser,function (req, res) {
        //find a substring containing the typed word in search bar
        searchModel.find({_TITLE:{$regex: req.body._TITLE}} , function (error, data) {
            console.log(req.body);
            if (error) {
                throw error;
            }
            //return found data to frontend (search.js)
            res.json(data);
        });
    });
};