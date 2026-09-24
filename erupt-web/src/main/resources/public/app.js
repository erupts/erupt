window.eruptSiteConfig = {
    domain: "",
    // Attachment host. Leave empty: it is taken from the backend AttachmentProxy at startup.
    // Setting it here still works and overrides the backend value (kept for compatibility).
    fileDomain: "",
    title: "Erupt Engine",
    desc: "Low-Code & AI Harness",
    dialogLogin: false,
    copyright: true, // whether to show the copyright footer
    tabReuse: false, // enable multi-tab route reuse by default (the user's choice in the settings drawer takes precedence)
    // Logos: leave a key out to use the default, set it to null to show nothing there.
    // logoPath: null,  // expanded header logo (default: the bundled erupt mark)
    // logoFoldPath: null,           // collapsed header logo (default: follows logoPath)
    // loginLogoPath: null,          // login page logo (default: follows logoPath)
    logoText: "Erupt",
    registerPage: null,
    // faviconPath: "https://docs.erupt.xyz/icon.svg",
    amapKey: 'da01c124bff9d9be1ad44e04f23aa32e',
    amapSecurityJsCode: "5bf6c7828a97fe987c8292f00629a6d9",
    // Installed app (PWA). Name, description and color come from logoText / title / desc and the
    // top bar; this block only adds the icon and the icon's right-click menu.
    pwa: {
        // icon: "assets/pwa-icon.svg",             // svg, or a png / jpg / webp (512px or larger)
        // shortcuts: [{name: "Home", url: "./#/"}], // hash routes work; optional icon / description
    },
    // Appearance defaults. Each one only applies until the user picks something in the
    // settings drawer; that choice is remembered in the browser and wins from then on.
    theme: {
        // Let users change the branding side of the appearance themselves (theme color, header
        // color, skin, navigation gradient, menu mode) in the settings drawer, the sidebar, the login
        // page and the home page. false hides those controls and ignores choices users saved earlier,
        // so everyone sees the appearance and menu mode configured here. Light/dark and compact stay
        // switchable either way: they are per-user comfort settings, not branding.
        customizable: true,
        // primaryColor: 'rgb(22, 119, 255)',
        // headerColor: 'primary',   // "primary" (follow the primary color) or any CSS color
        dark: false,        // false | true | "auto" (follow the OS color scheme)
        compact: false,     // denser spacing across the UI
        skin: "default",    // "default" | "brutalist" | "liquid-glass" | "workspace" (chat-app frame: dark brand-tinted sidebar + header, content as a rounded card) | "classic" (Ant Design Pro: navy sidebar, white header)
        // workspaceFrame: "sky", // workspace skin only — navigation frame preset (unset = derived from primaryColor):
        //   light: "mist" | "sky" | "azure" | "salt" | "gray" | "mint" | "mint-chip" | "lime" | "citrus" | "banana" | "brass" | "almond" | "peach"
        //          | "dawn" | "blush" | "raspberry" | "mauve" | "lilac" | "lavender-mint"
        //   dark:  "deep-sea" | "lagoon" | "indigo" | "slate" | "starry" | "teal" | "jade" | "pine" | "clementine" | "wine" | "aubergine" | "plum" | "graphite"
        menuMode: "normal", // "normal" (sidebar) | "split" (categories in the header) | "dual" (two-column sidebar) | "top" (whole menu in the header, no sidebar) | "group" (categories as flat group titles) | "top-split" (categories in the header, their children in a second row, no sidebar)
        // formPanelMode: "center"  // "center" (floating dialog) | "side" (right panel) | "full" (fullscreen) — how record forms open
        // loginLayout: "center",   // login page layout: "center" (card on the artwork) | "cover" (form docked right) | "wide" (one wide card, brand left)
        //                          //   | "wallpaper" (full-screen picture, frosted card) | "poster" (headline brand, small card)
        // loginBackground: "https://oos.erupt.xyz/test/2026-09-20/login-bg-2.jpg", // login page picture, replaces the stock artwork in every layout ("wallpaper" adds the frosted card)
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
