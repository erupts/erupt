window.eruptSiteConfig = {
    domain: "",
    fileDomain: "",
    title: "Erupt Framework",
    desc: "通用后台管理框架",
    dialogLogin: false,
    copyright: true, //是否保留显示版权信息
    logoPath: null,
    loginLogoPath: null,
    logoText: "Erupt",
    registerPage: null,
    amapKey: null,
    login: function (e) {

    },
    upload: function (eruptName, eruptFieldName) {
        return {
            url: "",
            headers: {}
        }
    }
};

window.eruptI18n = {
    "en-US": {
        "接口配置": "Api Config"
    }
}
