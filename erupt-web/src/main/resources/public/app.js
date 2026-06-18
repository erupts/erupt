window.eruptSiteConfig = {
    domain: "",
    fileDomain: "",
    title: "Erupt Engine",
    desc: "Common Data Framework",
    dialogLogin: false,
    copyright: true, //是否保留显示版权信息
    logoPath: null,
    logoFoldPath: null,
    loginLogoPath: null,
    logoText: "Erupt",
    registerPage: null,
    amapKey: 'da01c124bff9d9be1ad44e04f23aa32e',
    amapSecurityJsCode: "5bf6c7828a97fe987c8292f00629a6d9",
    theme: {
        primaryColor: '#3f51b5'
    },
    r_tools: [{
        mobileHidden: true,
        render: () => {
            return `<a href="https://start.erupt.xyz" target="_blank" style="
                display: inline-flex; align-items: center; gap: 5px;
                margin: 0;
                padding: 3px 10px;
                border-radius: 4px;
                border: 1px solid #3f51b5;
                color: #3f51b5;
                font-size: 12px;
                font-weight: 500;
                letter-spacing: 0.3px;
                text-decoration: none;
                white-space: nowrap;
                transition: all .2s;
            ">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
                Start Erupt
            </a>`
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
