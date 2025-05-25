var start = true;
window.eruptSiteConfig = {
    domain: "",
    fileDomain: "",
    title: "Erupt Framework",
    desc: "通用后台管理框架",
    dialogLogin: false,
    copyright: true, //是否保留显示版权信息
    // copyrightTxt: () => {
    //     return "x_x_x<a>xxx</a>";
    // },
    logoPath: null,
    logoFoldPath: null,
    loginLogoPath: null,
    logoText: "Erupt",
    registerPage: null,
    amapKey: '6ed167be6d22b8f8fa8e0402724df150',
    amapSecurityJsCode: "ee3e78fcb019e6078fcfc5b53b0a63ec",
    userTools: [{
        text: "自定义用户工具栏",
        icon: "fa fa-snowflake-o",
        click: function (event) {
            alert("On Click")
        }
    }],
    r_tools: [{
        mobileHidden: true,
        icon: "fa-qq",
        click: function (event) {
            window.open("https://jq.qq.com/?_wv=1027&k=MCd4plZ0")
        }
    }, {
        mobileHidden: true,
        icon: "fa-github",
        click: function (event) {
            window.open("https://github.com/erupts/erupt")
        }
    }, {
        mobileHidden: true,
        icon: "fa-github-alt",
        click: function (event) {
            window.open("https://gitee.com/erupt/erupt")
        }
    }, {
        render: () => {
            return `<div style="padding: 0 6px">
                    <a class="align-self-center" href='https://gitee.com/erupt/erupt' target="_blank">
                        <img alt='gitee star' src='https://gitee.com/erupt/erupt/badge/star.svg?theme=dark'/>
                    </a>&nbsp;<a class="align-self-center" href='https://github.com/erupts/erupt' target="_blank">
                        <img alt="github star" src="https://img.shields.io/github/stars/erupts/erupt?style=social">
                    </a>
                </div>`
        },
        mobileHidden: true
    }],
    login: function (e) {

    },
    upload: function (files) {
        return {
            url: "",
            headers: {}
        }
    }
};

window.eruptRouterEvent = {
    // $: {
    //     load(e) {
    //         // console.log('load', e)
    //     },
    //     unload(e) {
    //         // console.log("unload ", e)
    //     }
    // },
}

let eruptEvent = {
    login() {

    },
    logout() {

    },
    upload() {

    }
}
