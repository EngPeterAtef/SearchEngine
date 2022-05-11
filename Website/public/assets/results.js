let results = document.querySelectorAll(`.in-sum`);
let typed = document.getElementById(`searchWord`).value.toLowerCase();

results.forEach(result => {
    let para = result.innerText;
    result.innerHTML = para;
});
