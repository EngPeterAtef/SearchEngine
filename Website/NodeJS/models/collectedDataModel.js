//----------------------------------------------------------
//----------------------------------------------------------
//----------------------MONGODB SCHEMA----------------------
//----------------------------------------------------------
//----------------------------------------------------------
let mongoose=require('mongoose');
// //make a schema to know what kind of info to expect from db
// //this is not our schema yet..
let collectedDataSchema = new mongoose.Schema({
    url: String,
    visited: Boolean,
    html: String
}, {collection: "CollectedData"});

// //create a model based on this schema
let collectedData = mongoose.model('collectedData', collectedDataSchema);
// //export the model to use it in controller
module.exports = collectedData;
