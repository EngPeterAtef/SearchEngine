console.log("connected");
$(document).ready(function(){
    //On key up event
    $('.input-group .form-outline .form-control').on('keyup', function(){
        //var that holds the text input
        let typed = $('form input');
        if (!typed.val()) {
            $('.suggestions ul').empty();
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
            $('.suggestions ul').empty();
            for (let i = 0; i < data.length; i++) {
                //append new results to list
                $('.suggestions ul').append(`<li>${data[i]._TITLE}</li>`);
            }
          }
        });
        return false;
    });
});
  