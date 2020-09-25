window.eruptSiteConfig = {
    //global config
    domain: "http://localhost:9999",
    fileDomain: "",
    title: "Erupt Framework", //通用数据管理框架
    desc: "通用数据管理框架",
    routerReuse: false,
    amapKey: "6ba79a8db11b51aeb1176bd4cfa049f4",
    dialogLogin: false,
    logoPath: "assets/logo.svg",
    logoText: "Erupt",
    // registerPage:"http://www.baidu.com",
    r_tools: [{
        text: "下载",
        icon: "fa-download",
        mobileHidden: true,
        click: function (event) {
            console.log(event);
        },
        load: function () {
            console.log("load");
        }
    }]
};

//路由回调函数
window.eruptRouterEvent = {
    Test: {
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

//全局生命周期回调函数
window.eruptEvent = {
    startup: function () {
        console.log(window.getAppToken());
    }
};
