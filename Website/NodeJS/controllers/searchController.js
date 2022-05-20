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

    app.get('/search/:title', function (req, res) {
        searchModel.find({title: req.params.title}, function (error, data) {
            if (error) {
                throw error;
            }
            let dummy1 = [];
            let dummy2 = [];
            //update counter
            if(data.length === 0){
                let newObj ={
                    title: req.params.title,
                    counter: 1
                };
                let newSearch = searchModel(newObj).save(function(err,data){
                    if(err)  throw err;
                    res.json(data);
                    //res.render('/=results',{dummy1,dummy1});
                    //res.redirect('/results',{dummy1,dummy1});
                });
            }
            else{
                let updateSearch = searchModel.updateOne({title: data[0].title},{$inc:{counter: 1}},function(err,result){
                    if(err)  throw err;
                    res.json(data);
                    //res.render('results',{dummy1,dummy1});
                    //res.redirect('/results',{dummy1,dummy1});
                });
            }
            
        });
    });

    //for search suggestions
    var urlencodedParser = bodyParser.urlencoded({extended:false});
    app.post('/search',urlencodedParser,function (req, res) {
        //find a substring containing the typed word in search bar
        searchModel.find({title:{$regex: req.body.title}} , function (error, data) {
            console.log(req.body);
            if (error) {
                throw error;
            }
            //return found data to frontend (search.js)
            res.json(data);
        });
    });
};