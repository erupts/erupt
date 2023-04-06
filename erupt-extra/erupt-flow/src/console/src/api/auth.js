import request from "@/api/request";

export function getToken() {
    return getQueryVariable("_token")
}

export function getQueryVariable(fieldName) {
    let url = window.location.hash;
    let querys = url.substring(url.indexOf('?') + 1).split('&');
    for(let i=0;i<querys.length;i++){
        let temp = querys[i].split('=');
        if(temp[0]===fieldName) {
            if(temp.length<2){
                return null;
            }else{
                return temp[1];
            }
        }
    }
    return null;//备用的万能token
}

/**
 * 获取登陆人信息
 * @returns
 */
export function getLoginInfo() {
    return request({
        url: '/userinfo',
        method: 'get'
    })
}
