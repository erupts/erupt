eruptSiteConfig = {
    //global config
    domain: "",
    attachmentDomain: "http://qa5mh744r.bkt.clouddn.com/",
    title: "Erupt DEMO",
    desc: "Erupt 演示系统",
    routerReuse: false,
    amapKey: "6ba79a8db11b51aeb1176bd4cfa049f4",
    logoPath: "/assets/logo.svg",
    logoText: "Demo",
    r_tools: [{
        text: "功能按钮",
        icon: "fa-eercast",
        mobileHidden: true,
        click: function (event, token) {
            console.log(event);
            alert("test btn");
        },
        load: function () {
            console.log("loaded")
        }
    }]
};
