$(document).ready(function(){
    function changePage() {
        $('#results-card').empty();
        pageNum = parseInt($(this).attr('id'));
        const pageSize = 10;
        if (resultsFromSpring != null) {
            console.log(pageNum * pageSize);
            console.log(resultsFromSpring.length);
            let pageContent = resultsFromSpring.slice((pageNum - 1) * pageSize, Math.min(pageNum * pageSize, resultsFromSpring.length));
            for (let i = 0; i < pageContent.length; i++) {
                $('#results-card').append(`
                    <div class="cont">
                    <span id="resLink">${pageContent[i].url}</span>
                    <h3 class="in-tit">
                        <a href="${pageContent[i].url}">${pageContent[i].title}</a>
                    </h3>
                    <p class="in-sum">${pageContent[i].snippet}</p>
                </div >`);
            }
            let results = document.querySelectorAll(`.in-sum`);
            if(results !="")
            {
                let typed = document.getElementById(`searchWord`).value.toLowerCase();
                results.forEach(result => {
                    let para = result.innerText;
                    result.innerHTML = para;
                });
            }
        }
        $(window).scrollTop(0);
    }
    for (let i = 0; i < buttonsNumber; i++) {
        $('#buttons').append(`<a id="${i+1}" class="bb">${i+1}</a>`);
        $(`#${i+1}`).click(changePage);
    }
    $('#1').trigger("click");
});
