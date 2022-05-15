let results = document.querySelectorAll(`.in-sum`);

if(results !="")
{
    let typed = document.getElementById(`searchWord`).value.toLowerCase();
    results.forEach(result => {
        let para = result.innerText;
        result.innerHTML = para;
    });
}
