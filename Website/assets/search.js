console.log("connected");
$(document).ready(function(){
    //On key up event
    $('#searchWord').on('keyup', function(){
        //var that holds the text input
        let typed = $('#searchWord');
        if (!typed.val()) {
            $('ul').empty();
            return;
        }
        //object containing the data typed in the inpufield
        let search = {_TITLE: typed.val()};
        //Send the data to the server (searchController.js)
        $.ajax({
          type: 'POST',
          //make request to this url, handled(received) by app.post in searchController.js
          url: '/search',
          //we are sending the search var
          data: search,
          //here we receive the results
          success: function(data){
            //clear the prev suggestions
            $('ul').empty();
            for (let i = 0; i < data.length; i++) {
                //append new results to list
                $('ul').append(`<li>${data[i]._TITLE}</li>`);
            }
          }
        });
        return false;
    });

    $('#searchBtn').on('click', function(){
      //var that holds the text input
      let typed = $('#searchWord');
      //object containing the data typed in the inpufield
      let search = {_TITLE: typed.val()};
      //Send the data to the server (searchController.js)
      console.log("ajax start");
      $.ajax({
        
        type: 'GET',
        //make request to this url, handled(received) by app.post in searchController.js
        url: '/search/'+typed.val(),

        //here we receive the results
        success: function(data){
          console.log(data + "added to db");
        }
      });
      return false;
  });

});
  