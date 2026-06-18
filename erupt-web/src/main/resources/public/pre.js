window.setTimeout(function () {
    if (!window.eruptWebSuccess) {
        location.reload(true)
    }
}, 1000 * 12);
(function (date) {
    var __t__ = '' + date.getFullYear() + date.getMonth() + date.getDate() + date.getHours() + date.getMinutes();
    document.write(
        "<link rel=\"stylesheet\" href=\"app.css?_=" + __t__ + "\" type=\"text/css\"/>" +
        "<script type=\"text/javascript\" src=\"app.js?_=" + __t__ + "\"><\/script>" +
        "<script type=\"module\" src=\"app.module.js?_=" + __t__ + "\" ><\/script>"
    );
    var _preloaderEl = document.querySelector(".preloader");
    _preloaderEl.style.background = "#1890ff";
    var _brandEl = document.getElementById("preload-brand");
    var _colorCheckTimer = setInterval(function () {
        if (window.eruptSiteConfig !== undefined) {
            clearInterval(_colorCheckTimer);
            if (window.eruptSiteConfig.theme && window.eruptSiteConfig.theme.primaryColor) {
                _preloaderEl.style.background = window.eruptSiteConfig.theme.primaryColor;
            }
            if (_brandEl) {
                _brandEl.textContent = window.eruptSiteConfig.logoText || window.eruptSiteConfig.title || "Loading";
            }
        }
    }, 10);
})(new Date())
