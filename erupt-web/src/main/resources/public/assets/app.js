window.eruptSiteConfig = {
    //global config
    domain: "http://localhost:8888",
    title: "YuePeng Erupt",
    desc: "通用数据管理框架",
    routerReuse: false,
    amapKey: "15bc4df0bd6758645308fa591f439799",
    logoPath: "/assets/logo.svg",
    r_tools: [{
            text: "下载",
            icon: "fa-download",
            mobileHidden: true,
            click: function (event, token) {
                console.log(event);
                console.log(token);
            },
            load: function (event, token) {
            }
        }]
};
