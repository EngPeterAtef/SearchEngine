//----------------------------------------------------------
//----------------------------------------------------------
//----------------------MONGODB SCHEMA----------------------
//----------------------------------------------------------
//----------------------------------------------------------
let mongoose=require('mongoose');
// //make a schema to know what kind of info to expect from db
// //this is not our schema yet..
let indexerSchema = new mongoose.Schema({
    Word: String,
    Websites: [{ URL: String,Count: Number, locations: Number, positions: [] }]
}, {collection: "Indexed_documents"});

// //create a model based on this schema
let indexer = mongoose.model('indexer', indexerSchema);
// //export the model to use it in controller
module.exports = indexer;
