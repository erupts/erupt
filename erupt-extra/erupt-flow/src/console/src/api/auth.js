export function getToken() {
    return getQueryVariable("_token")
}

export function getQueryVariable(fieldName) {
    let url = window.location.hash;
    let query = url.substring(url.indexOf('?') + 1).split('&');
    for (let i = 0; i < query.length; i++) {
        let temp = query[i].split('=');
        if (temp[0] === fieldName) {
            if (temp.length < 2) {
                return null;
            } else {
                return temp[1];
            }
        }
    }
    return null; //备用的万能token
}
