//----------------------------------------------------------
//----------------------------------------------------------
//----------------------MONGODB SCHEMA----------------------
//----------------------------------------------------------
//----------------------------------------------------------
var mongoose=require('mongoose');
//make a schema to know what kind of info to expect from db
//this is not our schema yet..
var searchSchema = new mongoose.Schema({
    title: String,
    counter: Number
}, {collection: "sugg-test"});

//create a model based on this schema
var results = mongoose.model('results', searchSchema);
//export the model to use it in controller
module.exports = results;
