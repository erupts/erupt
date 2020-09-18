window.eruptSiteConfig = {
    //选填非空时启用
    domain: "",
    //选填非空时启用
    fileDomain: "",
    title: "Erupt Framework",
    desc: "Erupt 数据框架",
    //选填非空时启用
    amapKey: "6ba79a8db11b51aeb1176bd4cfa049f4",
    logoPath: "assets/logo.svg",
    logoText: "Erupt",
    r_tools: [{
        text: "功能按钮",
        icon: "fa-eercast",
        mobileHidden: true,
        click: function (event) {
            console.log(event);
            alert("test btn");
        },
        load: function () {
            console.log("loaded")
        }
    }]
};

//路由回调函数
window.eruptRouterEvent = {
    Router: {
        load: function (e) {
        },
        unload: function (e) {
        }
    },
    $: {
        load: function (e) {
        },
        unload: function (e) {
        }
    }
};

window.eruptEvent = {
    startup: function () {

    }
}