console.log("connected");
let active = -1;
$(document).ready(function(){
  $('#x').hide();
  $('#x').css("opacity", "100%");
  $('form').css("background-color", "#121212");
  $('.searchBox .search').css("background-color", "#121212");
  $('.searchBox').css("background-color", "#121212");
  $('form').css("border", "0px solid #808080");
  $('form').css("border-radius"," 20px 20px 20px 20px");
  //--------------Search Bar Responsive Events--------------//
  $('#x').click(function(){
    $('#searchWord').val(' ');
    $('#searchWord').focus();    
    $('form').css("border-radius"," 20px 20px 20px 20px");
    $('form ul').hide();
  });
  //to make onclick event
  var isPressed = false;
  //click out of form
  $(document).mouseup(function(e){
    var container = $("form");
    // If the target of the click isn't the container
    if(!container.is(e.target) && container.has(e.target).length === 0){
      isPressed=false;
      $('form').css("background-color", "#121212");
      $('form').css("border-radius"," 20px 20px 20px 20px");
      $('.searchBox .search').css("background-color", "#121212");
      $('.searchBox').css("background-color", "#121212");
      $('form').css("border", "0px solid #808080");
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
    $('form').css("border", "0px solid #303030");
    
    $('ul').show();
    $('#x').show();
    if(!($("form ul").children().length == 0))
    {
      $('hr').css("opacity", "20%");
      $('form').css("border-radius"," 20px 20px 0px 0px");
    }
  });
  //to make hover event
  $('form').mouseover(function(){
    $(this).css("background-color", "#303030");
    $('.searchBox .search').css("background-color", "#303030");
    $('.searchBox').css("background-color", "#303030");
    $('form').css("border", "0px solid #303030");
 
  });
  $('form').mouseout(function(){
    if(!isPressed)
    {
      $(this).css("background-color", "#121212");
      $('.searchBox .search').css("background-color", "#121212");
      $('.searchBox').css("background-color", "#121212");
      $('form').css("border", "0px solid #808080");
      $('hr').css("opacity", "0%");
    }
  });
  //--------------------------------------------------------//
  //----------------ON CLICK ON THE RESULT------------------//
  $("ul").on("click","li", function() {
    $('#searchWord').val($(this).text());
    $('#searchWord').focus();
    $('ul').hide();
    $('form').css("border-radius"," 20px 20px 20px 20px");
    $('#searchBtn').trigger('click');
  });
  //--------------------------------------------------------//
  //----------------VOICE RECOGNITION------------------//
  var speechRecognition = window.webkitSpeechRecognition;
  var recognition = new speechRecognition();
  var textbox =  $("#searchWord");
  var mic = $('#voiceRec');
  var content = '';
  recognition.continuous = true;
  recognition.onstart = function(){
    //instructions.text("voice recognition is on");
    mic.css("color", "red");
  }
  recognition.onspeechend = function(){
    //instructions.text("No activity");
    mic.css("color", "#808080");

  }
  recognition.onerror = function(){
    //instructions.text("Try again");
    mic.css("color", "#808080");
  }
  recognition.onresult = function(event){
    var current = event.resultIndex;
    var transcript = event.results[current][0].transcript;
    content+= transcript.substring(0,transcript.length -1).toLowerCase();;
    content+=' ';
    textbox.val(content);
    recognition.stop();
    $('#searchBtn').trigger('click');
    mic.css("color", "#808080");
  }
  $("#voiceRec").click(function(event) {
    content ='';
    recognition.start();
    mic.css("color", "red");
  });
  textbox.on('input',function(){
      content = $(this.val());
  });
    //------------Navigate between results with arrows------//
    $(document).on('keydown', function(e){
      let keycode = (e.keyCode ? e.keyCode :e.which);
      let sugg = $("form ul").children();

          if(keycode == '40') {

            if(active < $("form ul").children().length - 1)
            {
              active++;
              sugg[active].focus();
              //sugg[active].setAttribute("style","background-color:#404040;");
              //sugg[active].style.backgroundColor = "#404040";
            }
            else
            {
                active=0;
                sugg[active].focus();
                //sugg[active].setAttribute("style","background-color:#404040;");
            }
          }
          else if(keycode == '38') {
            if(active > - 1)
            {

              active--;
              sugg[active].focus();
              //sugg[active].setAttribute("style","background-color:#404040;");
            }
            else
            {
                active=$("form ul").children().length - 1;
                sugg[active].focus();
                //sugg[active].setAttribute("style","background-color:#404040;");
            }
          }
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
            $('form').css("border-radius"," 20px 20px 20px 20px");
            return;
        }
        //object containing the data typed in the inpufield
        // let search = {title: typed.val()};
        //Send the data to the server (searchController.js)
        $.ajax({
          type: 'POST',
          //make request to this url, handled(received) by app.post in searchController.js
          url: '/search',
          //we are sending the search var
          data: {"query": typed.val()},
          //here we receive the results
          success: function(data){
            //clear the prev suggestions
            console.log(data);
            
            $('ul').empty();
            if(data.length > 9) data.length =9;
            for (let i = 0; i < data.length; i++) {
                //append new results to list
                if(i+1 != data.length)
                  $('ul').append(`<li><i class="fa-solid fa-clock-rotate-left"></i>${data[i]}</li>`);
                else
                {
                  $('hr').css("opacity", "20%");
                  $('#x').show();
                  $('ul').append(`<li style="border-radius: 0 0 20px 20px"><i class="fa-solid fa-clock-rotate-left"></i>${data[i]}</li>`);
                }
                
            }
            if(data.length === 0) {
              $('hr').css("opacity", "0%");
              $('form').css("border-radius"," 20px 20px 20px 20px");
            }
            else {
              $('form').css("border-radius"," 20px 20px 0px 0px");
            }
          }
        });
        return false;
    });

    $('#searchBtn').on('click', function(){
      //var that holds the text input
      let typed = $('#searchWord');
      //object containing the data typed in the inpufield
      // let search = {title: typed.val()};
      //Send the data to the server (searchController.js)
      $.ajax({
        
        type: 'GET',
        //make request to this url, handled(received) by app.post in searchController.js
        url: '/search',
        data:{"query": typed.val()},

        //here we receive the results
        success: function(data){
          //console.log(data);
          window.location.assign('/results/'+typed.val());
        }
      });
      return false;
  });

});
  
