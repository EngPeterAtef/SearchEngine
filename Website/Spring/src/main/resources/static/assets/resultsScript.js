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

        }

        $(window).scrollTop(0);
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
    for (let i = 1; i <= buttonsNumber; i++) {
        $('#buttons').append(`<a id="${i}" class="bb">${i}</a>`);
        $(`#${i}`).click(changePage);
    }
    $('#1').trigger("click");
});
