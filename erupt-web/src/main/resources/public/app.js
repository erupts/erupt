window.eruptSiteConfig = {
    domain: "",
    fileDomain: "",
    title: "Erupt Engine",
    desc: "Common Data Framework",
    dialogLogin: false,
    copyright: true, // whether to show the copyright footer
    tabReuse: false, // enable multi-tab route reuse by default (the user's choice in the settings drawer takes precedence)
    logoPath: null,
    logoFoldPath: null,
    loginLogoPath: null,
    logoText: "Erupt",
    registerPage: null,
    amapKey: 'da01c124bff9d9be1ad44e04f23aa32e',
    amapSecurityJsCode: "5bf6c7828a97fe987c8292f00629a6d9",
    // Appearance defaults. Each one only applies until the user picks something in the
    // settings drawer; that choice is remembered in the browser and wins from then on.
    theme: {
        // primaryColor: 'rgb(22, 119, 255)',
        // headerColor: 'primary',   // "primary" (follow the primary color) or any CSS color
        dark: false,        // false | true | "auto" (follow the OS color scheme)
        compact: false,     // denser spacing across the UI
        skin: "default",    // "default" | "brutalist" | "liquid-glass"
        menuMode: "normal", // "normal" (sidebar) | "split" (categories in the header) | "dual" (two-column sidebar) | "top" (whole menu in the header, no sidebar) | "group" (categories as flat group titles) | "top-split" (categories in the header, their children in a second row, no sidebar)
        // formPanelMode: "center"  // "center" (floating dialog) | "side" (right panel) | "full" (fullscreen) — how record forms open
    },
    r_tools: [{
        mobileHidden: true,
        render: () => {
            return `
            <div style="display: flex; align-items: center; gap: 8px;">
            <a href="https://start.erupt.xyz" target="_blank" style="
                display: inline-flex; align-items: center; gap: 5px; margin: 0; padding: 3px 12px; border-radius: 0; border: 2px solid #14120B;
                background: #4FC8EC; color: #14120B; font-size: 12px; font-weight: 800; text-decoration: none; box-shadow: 2px 2px 0 #14120B;">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="#14120B"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
                    Start Erupt
                </a>
                <a class="align-self-center" href='https://gitee.com/erupt/erupt' target="_blank">
                    <img alt='gitee star' src='https://gitee.com/erupt/erupt/badge/star.svg?theme=dark'/>
                </a>
                <a class="align-self-center" href='https://github.com/erupts/erupt' target="_blank">
                    <img alt="github star" src="https://img.shields.io/github/stars/erupts/erupt?style=social">
                </a>
            </div>
            `
        }
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
