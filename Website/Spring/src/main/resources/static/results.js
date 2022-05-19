$(document).ready(function(){
    let fullPath = window.location.pathname;
    let fullPathArr = fullPath.split('/');
    $('#searchWord').attr('value', fullPathArr[1]);

    // //Get results
    // $.ajax({
    //     type: 'GET',
    //     url: window.location.pathname,
    //     data:{"index": 1},
    //     //here we receive the results
    //     success: function(data){
    //     //console.log(data);
    //     //   window.location.assign('/results/'+typed.val());
    //     }
    // });
});
let results = document.querySelectorAll(`.in-sum`);

if(results !="")
{
    let typed = document.getElementById(`searchWord`).value.toLowerCase();
    results.forEach(result => {
        let para = result.innerText;
        result.innerHTML = para;
    });
}
