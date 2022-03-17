//----------------------------------------------------------
//----------------------------------------------------------
//----------------------MONGODB SCHEMA----------------------
//----------------------------------------------------------
//----------------------------------------------------------
var mongoose=require('mongoose');
// //make a schema to know what kind of info to expect from db
// //this is not our schema yet..
var resultsSchema = new mongoose.Schema({
    url: String,
    title: String
}, {collection: "crawler"});

// //create a model based on this schema
var resultss = mongoose.model('resultss', resultsSchema);
// //export the model to use it in controller
module.exports = resultss;
