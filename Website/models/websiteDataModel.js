//----------------------------------------------------------
//----------------------------------------------------------
//----------------------MONGODB SCHEMA----------------------
//----------------------------------------------------------
//----------------------------------------------------------
let mongoose=require('mongoose');
// //make a schema to know what kind of info to expect from db
// //this is not our schema yet..
let websiteSchema = new mongoose.Schema({
    url: String,
    title: String,
    popularity: Number,
    body: String
}, {collection: "WebsiteData"});

// //create a model based on this schema
let websiteData = mongoose.model('websiteData', websiteSchema);
// //export the model to use it in controller
module.exports = websiteData;
