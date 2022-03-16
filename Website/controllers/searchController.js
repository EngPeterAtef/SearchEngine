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

    //TO BE UPDATED - HAS UNDEFINED ERROR
    app.get('/search/:title', function (req, res) {
        searchModel.find({_TITLE: req.params.title}, function (error, data) {
            if (error) {
                throw error;
            }
            //update counter
            if(data.length === 0){
                let newObj ={
                    _TITLE: req.params.title,
                    _COUNTER: 1
                };
                let newSearch = searchModel(newObj).save(function(err,data){
                    if(err)  throw err;
                    res.json(data);
                });
            }
            else{
                let updateSearch = searchModel.updateOne({_TITLE: data[0]._TITLE},{$inc:{_COUNTER: 1}},function(err,result){
                    if(err)  throw err;
                    res.json(result);
                });
            }
        });
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