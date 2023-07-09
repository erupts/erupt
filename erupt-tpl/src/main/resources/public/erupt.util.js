(function(){
    let param = {};
    let paramsArr = location.search.substring(1).split('&')
    for (let i = 0, len = paramsArr.length; i < len; i++) {
        let arr = paramsArr[i].split('=')
        param[arr[0]] = arr[1];
    }
})()