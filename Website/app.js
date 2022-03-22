/**--------------ENTRY POINT--------------- */
//using express
var express = require('express');

//----------------------------------------------------------
//----------------------------------------------------------
//----------------CONNECTION TO MONGODB---------------------
//----------------------------------------------------------
//----------------------------------------------------------
var mongoose=require('mongoose');
//at the end is the database name
mongoose.connect('mongodb+srv://doaa:mbm@cluster0.zu6vd.mongodb.net/myData?retryWrites=true&w=majority');
//----------------------------------------------------------

//using controller
var searchController = require("./controllers/searchController");
var resultsController = require("./controllers/resultsController");
//fire express
var app = express();

//set up template engine
app.set('view engine','ejs');

//static files
app.use(express.static('../Website'));

//fire controller, send to it an instance of app
searchController(app);
resultsController(app);

//listen to port
app.listen(3000);
console.log('you are listening to port 3000');

/**we are going to use mvc structure
 * MVC: model view controller
 * 
 * Model: data, like: todos, users,..
 * 
 * View: the page that we are sending to the user in .ejs like: todo.ejs, account.ejs
 * 
 * Controller: connects the model and the view together
 * grab the data from the model, decide how to give that to the view then send 
 * it to the user
 */