console.log("connected");
$(document).ready(function(){
  $('#x').hide();
  $('#x').css("opacity", "100%");
  $('form').css("background-color", "#121212");
  $('.searchBox .search').css("background-color", "#121212");
  $('.searchBox').css("background-color", "#121212");
  //--------------Search Bar Responsive Events--------------//
  $('#x').click(function(){
    $('#searchWord').val(' ');
    $('#searchWord').focus();
  });
  //to make onclick event
  var isPressed = false;
  //click out of form
  $(document).mouseup(function(e){
    var container = $("form");
    // If the target of the click isn't the container
    if(!container.is(e.target) && container.has(e.target).length === 0){
      isPressed=false;
      $(this).css("background-color", "#202020");
      $('.searchBox .search').css("background-color", "#202020");
      $('.searchBox').css("background-color", "#202020");
      $('form').css("border", "1px solid #808080");
      $('ul').hide();
      $('hr').css("opacity", "0%");
      $('#x').hide();
    }
});
  $('form').click(function(){
    isPressed=true;
    $(this).css("background-color", "#303030");
    $('.searchBox .search').css("background-color", "#303030");
    $('.searchBox').css("background-color", "#303030");
    $('form').css("border", "1px solid #303030");
    
    $('ul').show();
  });
  //to make hover event
  $('form').mouseover(function(){
    $(this).css("background-color", "#303030");
    $('.searchBox .search').css("background-color", "#303030");
    $('.searchBox').css("background-color", "#303030");
    $('form').css("border", "1px solid #303030");
 
  });
  $('form').mouseout(function(){
    if(!isPressed)
    {
      $(this).css("background-color", "#121212");
      $('.searchBox .search').css("background-color", "#121212");
      $('.searchBox').css("background-color", "#121212");
      $('form').css("border", "1px solid #808080");
      $('hr').css("opacity", "0%");
    }
  });
  //--------------------------------------------------------//
  //----------------ON CLICK ON THE RESULT------------------//
  $("ul").on("click","li", function() {
    var item = $(this).text();
    $('#searchWord').val(item);
    $('#searchWord').focus();
    $('ul').hide();
  });
  //--------------------------------------------------------//
    //------------Navigate between results with arrows------//
    $(document).on('keydown', function(e){
      
    });
    //-------------------------------------------------------//
    //On key up event
    $('#searchWord').on('keyup', function(){
        //var that holds the text input
        let typed = $('#searchWord');
        if (!typed.val()) {
            $('#x').hide();
            $('hr').css("opacity", "0%");
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
                if(i+1 != data.length)
                  $('ul').append(`<li><i class="fa-solid fa-clock-rotate-left"></i>${data[i]._TITLE}</li>`);
                else
                {
                  $('hr').css("opacity", "20%");
                  $('#x').show();
                  $('ul').append(`<li style="border-bottom-right-radius:20px;border-bottom-left-radius:20px"> <i class="fa-solid fa-clock-rotate-left"></i> ${data[i]._TITLE}</li>`);
                }
                
            }
            if(data.length === 0) {
              $('hr').css("opacity", "0%");
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
      $.ajax({
        
        type: 'GET',
        //make request to this url, handled(received) by app.post in searchController.js
        url: '/search/'+typed.val(),

        //here we receive the results
        success: function(data){
          //console.log(data);
          window.location.assign('/results/'+typed.val());
        }
      });
      return false;
  });

});
  