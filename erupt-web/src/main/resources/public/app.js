var start = true;
window.eruptSiteConfig = {
    domain: "",
    fileDomain: "https://oos.erupt.xyz",
    title: "Erupt Framework",
    desc: "通用后台管理框架",
    dialogLogin: false,
    copyright: true, //是否保留显示版权信息
    logoPath: null,
    loginLogoPath: null,
    logoText: "Erupt",
    registerPage: null,
    amapKey: '6ed167be6d22b8f8fa8e0402724df150',
    amapSecurityJsCode: "ee3e78fcb019e6078fcfc5b53b0a63ec",
    r_tools: [{
        text: "qq",
        mobileHidden: true,
        icon: "fa-qq",
        click: function (event) {
            window.open("https://jq.qq.com/?_wv=1027&k=MCd4plZ0")
        }
    }, {
        text: "github",
        mobileHidden: true,
        icon: "fa-github",
        click: function (event) {
            window.open("https://github.com/erupts/erupt")
        }
    }, {
        text: "gitee",
        mobileHidden: true,
        icon: "fa-github-alt",
        click: function (event) {
            window.open("https://gitee.com/erupt/erupt")
        }
    }, {
        text: "star",
        icon: "fa-eercast",
        mobileHidden: true,
        click: function (event) {
            let ele = `<a class="align-self-center" href='https://gitee.com/erupt/erupt' target="_blank">
                    <img alt='gitee star' src='https://gitee.com/erupt/erupt/badge/star.svg?theme=dark'/>
                </a>&nbsp;<a class="align-self-center" href='https://github.com/erupts/erupt' target="_blank">
                    <img alt="github star" src="https://img.shields.io/github/stars/erupts/erupt?style=social">
                </a> `
            if (start) {
                event.target.outerHTML = ele;
                start = false;
            }
        }
    }],
    login: function (e) {

    },
    upload: function (eruptName, eruptFieldName) {
        return {
            url: "",
            headers: {}
        }
    }
};
