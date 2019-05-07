var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
        return extendStatics(d, b);
    }
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var __values = (this && this.__values) || function (o) {
    var m = typeof Symbol === "function" && o[Symbol.iterator], i = 0;
    if (m) return m.call(o);
    return {
        next: function () {
            if (o && i >= o.length) o = void 0;
            return { value: o && o[i++], done: !o };
        }
    };
};
/*!
 * @license Copyright (c) 2003-2019, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md.
 */
!function (t) { t.en = Object.assign(t.en || {}, { a: "Cannot upload file:", b: "Font Family", c: "Default", d: "Font Size", e: "Tiny", f: "Small", g: "Big", h: "Huge", i: "Align left", j: "Align right", k: "Align center", l: "Justify", m: "Text alignment", n: "Yellow marker", o: "Green marker", p: "Pink marker", q: "Blue marker", r: "Red pen", s: "Green pen", t: "Remove highlight", u: "Highlight", v: "Italic", w: "Block quote", x: "Insert image or file", y: "Choose heading", z: "Heading", aa: "Bold", ab: "Underline", ac: "image widget", ad: "Numbered List", ae: "Bulleted List", af: "Insert table", ag: "Header column", ah: "Insert column left", ai: "Insert column right", aj: "Delete column", ak: "Column", al: "Header row", am: "Insert row below", an: "Insert row above", ao: "Delete row", ap: "Row", aq: "Merge cell up", ar: "Merge cell right", as: "Merge cell down", at: "Merge cell left", au: "Split cell vertically", av: "Split cell horizontally", aw: "Merge cells", ax: "Strikethrough", ay: "Enter image caption", az: "Full size image", ba: "Side image", bb: "Left aligned image", bc: "Centered image", bd: "Right aligned image", be: "Insert image", bf: "media widget", bg: "Link", bh: "Upload failed", bi: "Could not obtain resized image URL.", bj: "Selecting resized image failed", bk: "Could not insert image at the current position.", bl: "Inserting image failed", bm: "Upload in progress", bn: "Save", bo: "Cancel", bp: "Link URL", bq: "Unlink", br: "Edit link", bs: "Open link in new tab", bt: "This link has no URL", bu: "Undo", bv: "Redo", bw: "Rich Text Editor, %0", bx: "Change image text alternative", by: "Paragraph", bz: "Heading 1", ca: "Heading 2", cb: "Heading 3", cc: "Heading 4", cd: "Heading 5", ce: "Heading 6", cf: "Text alternative", cg: "Insert media", ch: "The URL must not be empty.", ci: "This media URL is not supported.", cj: "Paste the media URL in the input.", ck: "Tip: Paste the URL into the content to embed faster.", cl: "Media URL" }); }(window.CKEDITOR_TRANSLATIONS || (window.CKEDITOR_TRANSLATIONS = {})), function (t, e) { "object" == typeof exports && "object" == typeof module ? module.exports = e() : "function" == typeof define && define.amd ? define([], e) : "object" == typeof exports ? exports.DecoupledEditor = e() : t.DecoupledEditor = e(); }(window, function () { return function (t) { var e = {}; function n(i) { if (e[i])
    return e[i].exports; var o = e[i] = { i: i, l: !1, exports: {} }; return t[i].call(o.exports, o, o.exports, n), o.l = !0, o.exports; } return n.m = t, n.c = e, n.d = function (t, e, i) { n.o(t, e) || Object.defineProperty(t, e, { enumerable: !0, get: i }); }, n.r = function (t) { "undefined" != typeof Symbol && Symbol.toStringTag && Object.defineProperty(t, Symbol.toStringTag, { value: "Module" }), Object.defineProperty(t, "__esModule", { value: !0 }); }, n.t = function (t, e) { if (1 & e && (t = n(t)), 8 & e)
    return t; if (4 & e && "object" == typeof t && t && t.__esModule)
    return t; var i = Object.create(null); if (n.r(i), Object.defineProperty(i, "default", { enumerable: !0, value: t }), 2 & e && "string" != typeof t)
    for (var o in t)
        n.d(i, o, function (e) { return t[e]; }.bind(null, o)); return i; }, n.n = function (t) { var e = t && t.__esModule ? function () { return t.default; } : function () { return t; }; return n.d(e, "a", e), e; }, n.o = function (t, e) { return Object.prototype.hasOwnProperty.call(t, e); }, n.p = "", n(n.s = 130); }([function (t, e, n) {
        "use strict";
        n.d(e, "b", function () { return o; }), n.d(e, "a", function () { return r; });
        var i = "https://ckeditor.com/docs/ckeditor5/latest/framework/guides/support/error-codes.html";
        var o = /** @class */ (function (_super) {
            __extends(o, _super);
            function o(t, e) {
                var _this = this;
                t = r(t), e && (t += " " + JSON.stringify(e)), _this = _super.call(this, t) || this, _this.name = "CKEditorError", _this.data = e;
                return _this;
            }
            o.isCKEditorError = function (t) { return t instanceof o; };
            return o;
        }(Error));
        function r(t) { var e = t.match(/^([^:]+):/); return e ? t + (" Read more: " + i + "#error-" + e[1] + "\n") : t; }
    }, function (t, e, n) {
        "use strict";
        var i = n(0);
        var o = { error: function (t, e) { console.error(Object(i.a)(t), e); }, warn: function (t, e) { console.warn(Object(i.a)(t), e); } };
        e.a = o;
    }, function (t, e, n) { var i = {}, o = function (t) { var e; return function () { return void 0 === e && (e = t.apply(this, arguments)), e; }; }(function () { return window && document && document.all && !window.atob; }), r = function (t) { var e = {}; return function (t, n) { if ("function" == typeof t)
        return t(); if (void 0 === e[t]) {
        var i = function (t, e) { return e ? e.querySelector(t) : document.querySelector(t); }.call(this, t, n);
        if (window.HTMLIFrameElement && i instanceof window.HTMLIFrameElement)
            try {
                i = i.contentDocument.head;
            }
            catch (t) {
                i = null;
            }
        e[t] = i;
    } return e[t]; }; }(), s = null, a = 0, c = [], l = n(56); function d(t, e) { for (var n = 0; n < t.length; n++) {
        var o = t[n], r = i[o.id];
        if (r) {
            r.refs++;
            for (var s = 0; s < r.parts.length; s++)
                r.parts[s](o.parts[s]);
            for (; s < o.parts.length; s++)
                r.parts.push(p(o.parts[s], e));
        }
        else {
            var a = [];
            for (s = 0; s < o.parts.length; s++)
                a.push(p(o.parts[s], e));
            i[o.id] = { id: o.id, refs: 1, parts: a };
        }
    } } function u(t, e) { for (var n = [], i = {}, o = 0; o < t.length; o++) {
        var r = t[o], s = e.base ? r[0] + e.base : r[0], a = { css: r[1], media: r[2], sourceMap: r[3] };
        i[s] ? i[s].parts.push(a) : n.push(i[s] = { id: s, parts: [a] });
    } return n; } function h(t, e) { var n = r(t.insertInto); if (!n)
        throw new Error("Couldn't find a style target. This probably means that the value for the 'insertInto' parameter is invalid."); var i = c[c.length - 1]; if ("top" === t.insertAt)
        i ? i.nextSibling ? n.insertBefore(e, i.nextSibling) : n.appendChild(e) : n.insertBefore(e, n.firstChild), c.push(e);
    else if ("bottom" === t.insertAt)
        n.appendChild(e);
    else {
        if ("object" != typeof t.insertAt || !t.insertAt.before)
            throw new Error("[Style Loader]\n\n Invalid value for parameter 'insertAt' ('options.insertAt') found.\n Must be 'top', 'bottom', or Object.\n (https://github.com/webpack-contrib/style-loader#insertat)\n");
        var o = r(t.insertAt.before, n);
        n.insertBefore(e, o);
    } } function f(t) { if (null === t.parentNode)
        return !1; t.parentNode.removeChild(t); var e = c.indexOf(t); e >= 0 && c.splice(e, 1); } function m(t) { var e = document.createElement("style"); if (void 0 === t.attrs.type && (t.attrs.type = "text/css"), void 0 === t.attrs.nonce) {
        var i = function () { 0; return n.nc; }();
        i && (t.attrs.nonce = i);
    } return g(e, t.attrs), h(t, e), e; } function g(t, e) { Object.keys(e).forEach(function (n) { t.setAttribute(n, e[n]); }); } function p(t, e) { var n, i, o, r; if (e.transform && t.css) {
        if (!(r = "function" == typeof e.transform ? e.transform(t.css) : e.transform.default(t.css)))
            return function () { };
        t.css = r;
    } if (e.singleton) {
        var c = a++;
        n = s || (s = m(e)), i = w.bind(null, n, c, !1), o = w.bind(null, n, c, !0);
    }
    else
        t.sourceMap && "function" == typeof URL && "function" == typeof URL.createObjectURL && "function" == typeof URL.revokeObjectURL && "function" == typeof Blob && "function" == typeof btoa ? (n = function (t) { var e = document.createElement("link"); return void 0 === t.attrs.type && (t.attrs.type = "text/css"), t.attrs.rel = "stylesheet", g(e, t.attrs), h(t, e), e; }(e), i = function (t, e, n) { var i = n.css, o = n.sourceMap, r = void 0 === e.convertToAbsoluteUrls && o; (e.convertToAbsoluteUrls || r) && (i = l(i)); o && (i += "\n/*# sourceMappingURL=data:application/json;base64," + btoa(unescape(encodeURIComponent(JSON.stringify(o)))) + " */"); var s = new Blob([i], { type: "text/css" }), a = t.href; t.href = URL.createObjectURL(s), a && URL.revokeObjectURL(a); }.bind(null, n, e), o = function () { f(n), n.href && URL.revokeObjectURL(n.href); }) : (n = m(e), i = function (t, e) { var n = e.css, i = e.media; i && t.setAttribute("media", i); if (t.styleSheet)
            t.styleSheet.cssText = n;
        else {
            for (; t.firstChild;)
                t.removeChild(t.firstChild);
            t.appendChild(document.createTextNode(n));
        } }.bind(null, n), o = function () { f(n); }); return i(t), function (e) { if (e) {
        if (e.css === t.css && e.media === t.media && e.sourceMap === t.sourceMap)
            return;
        i(t = e);
    }
    else
        o(); }; } t.exports = function (t, e) { if ("undefined" != typeof DEBUG && DEBUG && "object" != typeof document)
        throw new Error("The style-loader cannot be used in a non-browser environment"); (e = e || {}).attrs = "object" == typeof e.attrs ? e.attrs : {}, e.singleton || "boolean" == typeof e.singleton || (e.singleton = o()), e.insertInto || (e.insertInto = "head"), e.insertAt || (e.insertAt = "bottom"); var n = u(t, e); return d(n, e), function (t) { for (var o = [], r = 0; r < n.length; r++) {
        var s = n[r];
        (a = i[s.id]).refs--, o.push(a);
    } t && d(u(t, e), e); for (r = 0; r < o.length; r++) {
        var a;
        if (0 === (a = o[r]).refs) {
            for (var c = 0; c < a.parts.length; c++)
                a.parts[c]();
            delete i[a.id];
        }
    } }; }; var b = function () { var t = []; return function (e, n) { return t[e] = n, t.filter(Boolean).join("\n"); }; }(); function w(t, e, n, i) { var o = n ? "" : i.css; if (t.styleSheet)
        t.styleSheet.cssText = b(e, o);
    else {
        var r = document.createTextNode(o), s = t.childNodes;
        s[e] && t.removeChild(s[e]), s.length ? t.insertBefore(r, s[e]) : t.appendChild(r);
    } } }, , function (t, e, n) {
        "use strict";
        var i = n(9), o = "object" == typeof self && self && self.Object === Object && self, r = i.a || o || Function("return this")();
        e.a = r;
    }, function (t, e, n) {
        "use strict";
        (function (t) { var i = n(9), o = "object" == typeof exports && exports && !exports.nodeType && exports, r = o && "object" == typeof t && t && !t.nodeType && t, s = r && r.exports === o && i.a.process, a = function () { try {
            var t = r && r.require && r.require("util").types;
            return t || s && s.binding && s.binding("util");
        }
        catch (t) { } }(); e.a = a; }).call(this, n(12)(t));
    }, function (t, e, n) {
        "use strict";
        (function (t) { var i = n(4), o = n(20), r = "object" == typeof exports && exports && !exports.nodeType && exports, s = r && "object" == typeof t && t && !t.nodeType && t, a = s && s.exports === r ? i.a.Buffer : void 0, c = (a ? a.isBuffer : void 0) || o.a; e.a = c; }).call(this, n(12)(t));
    }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M6.972 16.615a.997.997 0 0 1-.744-.292l-4.596-4.596a1 1 0 1 1 1.414-1.414l3.926 3.926 9.937-9.937a1 1 0 0 1 1.414 1.415L7.717 16.323a.997.997 0 0 1-.745.292z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M11.591 10.177l4.243 4.242a1 1 0 0 1-1.415 1.415l-4.242-4.243-4.243 4.243a1 1 0 0 1-1.414-1.415l4.243-4.242L4.52 5.934A1 1 0 0 1 5.934 4.52l4.243 4.243 4.242-4.243a1 1 0 1 1 1.415 1.414l-4.243 4.243z"/></svg>'; }, function (t, e, n) {
        "use strict";
        (function (t) { var n = "object" == typeof t && t && t.Object === Object && t; e.a = n; }).call(this, n(18));
    }, function (t, e) { t.exports = '<svg viewBox="0 0 10 10" xmlns="http://www.w3.org/2000/svg"><path d="M.941 4.523a.75.75 0 1 1 1.06-1.06l3.006 3.005 3.005-3.005a.75.75 0 1 1 1.06 1.06l-3.549 3.55a.75.75 0 0 1-1.168-.136L.941 4.523z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M2 4.5V3h16v1.5zm0 3V6h5.674v1.5zm0 3V9h5.674v1.5zm0 3V12h5.674v1.5zm8.5-6V12h6V7.5h-6zM9.682 6h7.636c.377 0 .682.407.682.91v5.68c0 .503-.305.91-.682.91H9.682c-.377 0-.682-.407-.682-.91V6.91c0-.503.305-.91.682-.91zM2 16.5V15h16v1.5z"/></svg>'; }, function (t, e) { t.exports = function (t) { if (!t.webpackPolyfill) {
        var e = Object.create(t);
        e.children || (e.children = []), Object.defineProperty(e, "loaded", { enumerable: !0, get: function () { return e.l; } }), Object.defineProperty(e, "id", { enumerable: !0, get: function () { return e.i; } }), Object.defineProperty(e, "exports", { enumerable: !0 }), e.webpackPolyfill = 1;
    } return e; }; }, function (t) { t.exports = { a: "12.0.0" }; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M2 3.75c0 .414.336.75.75.75h14.5a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75zm0 8c0 .414.336.75.75.75h14.5a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75zm0 4c0 .414.336.75.75.75h9.929a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75zm0-8c0 .414.336.75.75.75h9.929a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M2 4.5V3h16v1.5zm2.5 3V12h11V7.5h-11zM4.061 6H15.94c.586 0 1.061.407 1.061.91v5.68c0 .503-.475.91-1.061.91H4.06c-.585 0-1.06-.407-1.06-.91V6.91C3 6.406 3.475 6 4.061 6zM2 16.5V15h16v1.5z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg" clip-rule="evenodd" stroke-linejoin="round" stroke-miterlimit="1.414"><path d="M18 4.5V3H2v1.5h16zm0 3V6h-5.674v1.5H18zm0 3V9h-5.674v1.5H18zm0 3V12h-5.674v1.5H18zm-8.5-6V12h-6V7.5h6zm.818-1.5H2.682C2.305 6 2 6.407 2 6.91v5.68c0 .503.305.91.682.91h7.636c.377 0 .682-.407.682-.91V6.91c0-.503-.305-.91-.682-.91zM18 16.5V15H2v1.5h16z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M2 4.5V3h16v1.5zm4.5 3V12h7V7.5h-7zM5.758 6h8.484c.419 0 .758.407.758.91v5.681c0 .502-.34.909-.758.909H5.758c-.419 0-.758-.407-.758-.91V6.91c0-.503.34-.91.758-.91zM2 16.5V15h16v1.5z"/></svg>'; }, function (t, e) { var n; n = function () { return this; }(); try {
        n = n || Function("return this")() || (0, eval)("this");
    }
    catch (t) {
        "object" == typeof window && (n = window);
    } t.exports = n; }, function (t, e, n) { var i = n(107); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e, n) {
        "use strict";
        e.a = function () { return !1; };
    }, function (t, e, n) {
        "use strict";
        (function (t) { var i = n(4), o = "object" == typeof exports && exports && !exports.nodeType && exports, r = o && "object" == typeof t && t && !t.nodeType && t, s = r && r.exports === o ? i.a.Buffer : void 0, a = s ? s.allocUnsafe : void 0; e.a = function (t, e) { if (e)
            return t.slice(); var n = t.length, i = a ? a(n) : new t.constructor(n); return t.copy(i), i; }; }).call(this, n(12)(t));
    }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M5.042 9.367l2.189 1.837a.75.75 0 0 1-.965 1.149l-3.788-3.18a.747.747 0 0 1-.21-.284.75.75 0 0 1 .17-.945L6.23 4.762a.75.75 0 1 1 .964 1.15L4.863 7.866h8.917A.75.75 0 0 1 14 7.9a4 4 0 1 1-1.477 7.718l.344-1.489a2.5 2.5 0 1 0 1.094-4.73l.008-.032H5.042z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M14.958 9.367l-2.189 1.837a.75.75 0 0 0 .965 1.149l3.788-3.18a.747.747 0 0 0 .21-.284.75.75 0 0 0-.17-.945L13.77 4.762a.75.75 0 1 0-.964 1.15l2.331 1.955H6.22A.75.75 0 0 0 6 7.9a4 4 0 1 0 1.477 7.718l-.344-1.489A2.5 2.5 0 1 1 6.039 9.4l-.008-.032h8.927z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M18 3.75a.75.75 0 0 1-.75.75H2.75a.75.75 0 1 1 0-1.5h14.5a.75.75 0 0 1 .75.75zm0 8a.75.75 0 0 1-.75.75H2.75a.75.75 0 1 1 0-1.5h14.5a.75.75 0 0 1 .75.75zm0 4a.75.75 0 0 1-.75.75H7.321a.75.75 0 1 1 0-1.5h9.929a.75.75 0 0 1 .75.75zm0-8a.75.75 0 0 1-.75.75H7.321a.75.75 0 1 1 0-1.5h9.929a.75.75 0 0 1 .75.75z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M2 3.75c0 .414.336.75.75.75h14.5a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75zm0 8c0 .414.336.75.75.75h14.5a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75zm2.286 4c0 .414.336.75.75.75h9.928a.75.75 0 1 0 0-1.5H5.036a.75.75 0 0 0-.75.75zm0-8c0 .414.336.75.75.75h9.928a.75.75 0 1 0 0-1.5H5.036a.75.75 0 0 0-.75.75z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M2 3.75c0 .414.336.75.75.75h14.5a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75zm0 8c0 .414.336.75.75.75h14.5a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75zm0 4c0 .414.336.75.75.75h9.929a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75zm0-8c0 .414.336.75.75.75h14.5a.75.75 0 1 0 0-1.5H2.75a.75.75 0 0 0-.75.75z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M9.816 11.5L7.038 4.785 4.261 11.5h5.555zm.62 1.5H3.641l-1.666 4.028H.312l5.789-14h1.875l5.789 14h-1.663L10.436 13zm7.55 2.279l.779-.779.707.707-2.265 2.265-2.193-2.265.707-.707.765.765V4.825c0-.042 0-.083.002-.123l-.77.77-.707-.707L17.207 2.5l2.265 2.265-.707.707-.782-.782c.002.043.003.089.003.135v10.454z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M11.03 3h6.149a.75.75 0 1 1 0 1.5h-5.514L11.03 3zm1.27 3h4.879a.75.75 0 1 1 0 1.5h-4.244L12.3 6zm1.27 3h3.609a.75.75 0 1 1 0 1.5h-2.973L13.57 9zm-2.754 2.5L8.038 4.785 5.261 11.5h5.555zm.62 1.5H4.641l-1.666 4.028H1.312l5.789-14h1.875l5.789 14h-1.663L11.436 13z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><g><path class="ck-icon__fill" d="M10.798 1.59L3.002 12.875l1.895 1.852 2.521 1.402 6.997-12.194z"/><path d="M2.556 16.727l.234-.348c-.297-.151-.462-.293-.498-.426-.036-.137.002-.416.115-.837.094-.25.15-.449.169-.595a4.495 4.495 0 0 0 0-.725c-.209-.621-.303-1.041-.284-1.26.02-.218.178-.506.475-.862l6.77-9.414c.539-.91 1.605-.85 3.199.18 1.594 1.032 2.188 1.928 1.784 2.686l-5.877 10.36c-.158.412-.333.673-.526.782-.193.108-.604.179-1.232.21-.362.131-.608.237-.738.318-.13.081-.305.238-.526.47-.293.265-.504.397-.632.397-.096 0-.27-.075-.524-.226l-.31.41-1.6-1.12zm-.279.415l1.575 1.103-.392.515H1.19l1.087-1.618zm8.1-13.656l-4.953 6.9L8.75 12.57l4.247-7.574c.175-.25-.188-.647-1.092-1.192-.903-.546-1.412-.652-1.528-.32zM8.244 18.5L9.59 17h9.406v1.5H8.245z"/></g></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path class="ck-icon__fill" d="M10.126 2.268L2.002 13.874l1.895 1.852 2.521 1.402L14.47 5.481l-1.543-2.568-2.801-.645z"/><path d="M4.5 18.088l-2.645-1.852-.04-2.95-.006-.005.006-.008v-.025l.011.008L8.73 2.97c.165-.233.356-.417.567-.557l-1.212.308L4.604 7.9l-.83-.558 3.694-5.495 2.708-.69 1.65 1.145.046.018.85-1.216 2.16 1.512-.856 1.222c.828.967 1.144 2.141.432 3.158L7.55 17.286l.006.005-3.055.797H4.5zm-.634.166l-1.976.516-.026-1.918 2.002 1.402zM9.968 3.817l-.006-.004-6.123 9.184 3.277 2.294 6.108-9.162.005.003c.317-.452-.16-1.332-1.064-1.966-.891-.624-1.865-.776-2.197-.349zM8.245 18.5L9.59 17h9.406v1.5H8.245z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><g><path d="M8.636 9.531l-2.758 3.94a.5.5 0 0 0 .122.696l3.224 2.284h1.314l2.636-3.736L8.636 9.53zm.288 8.451L5.14 15.396a2 2 0 0 1-.491-2.786l6.673-9.53a2 2 0 0 1 2.785-.49l3.742 2.62a2 2 0 0 1 .491 2.785l-7.269 10.053-2.147-.066z"/><path d="M4 18h5.523v-1H4zm-2 0h1v-1H2z"/></g></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M10.187 17H5.773c-.637 0-1.092-.138-1.364-.415-.273-.277-.409-.718-.409-1.323V4.738c0-.617.14-1.062.419-1.332.279-.27.73-.406 1.354-.406h4.68c.69 0 1.288.041 1.793.124.506.083.96.242 1.36.478.341.197.644.447.906.75a3.262 3.262 0 0 1 .808 2.162c0 1.401-.722 2.426-2.167 3.075C15.05 10.175 16 11.315 16 13.01a3.756 3.756 0 0 1-2.296 3.504 6.1 6.1 0 0 1-1.517.377c-.571.073-1.238.11-2 .11zm-.217-6.217H7v4.087h3.069c1.977 0 2.965-.69 2.965-2.072 0-.707-.256-1.22-.768-1.537-.512-.319-1.277-.478-2.296-.478zM7 5.13v3.619h2.606c.729 0 1.292-.067 1.69-.2a1.6 1.6 0 0 0 .91-.765c.165-.267.247-.566.247-.897 0-.707-.26-1.176-.778-1.409-.519-.232-1.31-.348-2.375-.348H7z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M9.586 14.633l.021.004c-.036.335.095.655.393.962.082.083.173.15.274.201h1.474a.6.6 0 1 1 0 1.2H5.304a.6.6 0 0 1 0-1.2h1.15c.474-.07.809-.182 1.005-.334.157-.122.291-.32.404-.597l2.416-9.55a1.053 1.053 0 0 0-.281-.823 1.12 1.12 0 0 0-.442-.296H8.15a.6.6 0 0 1 0-1.2h6.443a.6.6 0 1 1 0 1.2h-1.195c-.376.056-.65.155-.823.296-.215.175-.423.439-.623.79l-2.366 9.347z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><g><path d="M7 16.4c-.8-.4-1.5-.9-2.2-1.5a.6.6 0 0 1-.2-.5l.3-.6h1c1 1.2 2.1 1.7 3.7 1.7 1 0 1.8-.3 2.3-.6.6-.4.6-1.2.6-1.3.2-1.2-.9-2.1-.9-2.1h2.1c.3.7.4 1.2.4 1.7v.8l-.6 1.2c-.6.8-1.1 1-1.6 1.2a6 6 0 0 1-2.4.6c-1 0-1.8-.3-2.5-.6zM6.8 9L6 8.3c-.4-.5-.5-.8-.5-1.6 0-.7.1-1.3.5-1.8.4-.6 1-1 1.6-1.3a6.3 6.3 0 0 1 4.7 0 4 4 0 0 1 1.7 1l.3.7c0 .1.2.4-.2.7-.4.2-.9.1-1 0a3 3 0 0 0-1.2-1c-.4-.2-1-.3-2-.4-.7 0-1.4.2-2 .6-.8.6-1 .8-1 1.5 0 .8.5 1 1.2 1.5.6.4 1.1.7 1.9 1H6.8z"/><path d="M3 10.5V9h14v1.5z"/></g></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><g><path d="M3 18v-1.5h14V18z"/><path d="M5.2 10V3.6c0-.4.4-.6.8-.6.3 0 .7.2.7.6v6.2c0 2 1.3 2.8 3.2 2.8 1.9 0 3.4-.9 3.4-2.9V3.6c0-.3.4-.5.8-.5.3 0 .7.2.7.5V10c0 2.7-2.2 4-4.9 4-2.6 0-4.7-1.2-4.7-4z"/></g></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M3 10.423a6.5 6.5 0 0 1 6.056-6.408l.038.67C6.448 5.423 5.354 7.663 5.22 10H9c.552 0 .5.432.5.986v4.511c0 .554-.448.503-1 .503h-5c-.552 0-.5-.449-.5-1.003v-4.574zm8 0a6.5 6.5 0 0 1 6.056-6.408l.038.67c-2.646.739-3.74 2.979-3.873 5.315H17c.552 0 .5.432.5.986v4.511c0 .554-.448.503-1 .503h-5c-.552 0-.5-.449-.5-1.003v-4.574z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M11.627 16.5a3.496 3.496 0 0 1 0 0zm5.873-.196a3.484 3.484 0 0 1 0 0zm0-7.001V8h-13v8.5h4.341c.191.54.457 1.044.785 1.5H2a1.5 1.5 0 0 1-1.5-1.5v-13A1.5 1.5 0 0 1 2 2h4.5a1.5 1.5 0 0 1 1.06.44L9.122 4H16a1.5 1.5 0 0 1 1.5 1.5v1A1.5 1.5 0 0 1 19 8v2.531a6.027 6.027 0 0 0-1.5-1.228zM16 6.5v-1H8.5l-2-2H2v13h1V8a1.5 1.5 0 0 1 1.5-1.5H16z"/><path d="M14.5 19.5a5 5 0 1 1 0-10 5 5 0 0 1 0 10zM15 14v-2h-1v2h-2v1h2v2h1v-2h2v-1h-2z"/></svg>\n'; }, function (t, e) { t.exports = '<svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg"><path d="M4 0v1H1v3H0V.5A.5.5 0 0 1 .5 0H4zm8 0h3.5a.5.5 0 0 1 .5.5V4h-1V1h-3V0zM4 16H.5a.5.5 0 0 1-.5-.5V12h1v3h3v1zm8 0v-1h3v-3h1v3.5a.5.5 0 0 1-.5.5H12z"/><path fill-opacity=".256" d="M1 1h14v14H1z"/><g class="ck-icon__selected-indicator"><path d="M7 0h2v1H7V0zM0 7h1v2H0V7zm15 0h1v2h-1V7zm-8 8h2v1H7v-1z"/><path fill-opacity=".254" d="M1 1h14v14H1z"/></g></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M5.085 6.22L2.943 4.078a.75.75 0 1 1 1.06-1.06l2.592 2.59A11.094 11.094 0 0 1 10 5.068c4.738 0 8.578 3.101 8.578 5.083 0 1.197-1.401 2.803-3.555 3.887l1.714 1.713a.75.75 0 0 1-.09 1.138.488.488 0 0 1-.15.084.75.75 0 0 1-.821-.16L6.17 7.304c-.258.11-.51.233-.757.365l6.239 6.24-.006.005.78.78c-.388.094-.78.166-1.174.215l-1.11-1.11h.011L4.55 8.197a7.2 7.2 0 0 0-.665.514l-.112.098 4.897 4.897-.005.006 1.276 1.276a10.164 10.164 0 0 1-1.477-.117l-.479-.479-.009.009-4.863-4.863-.022.031a2.563 2.563 0 0 0-.124.2c-.043.077-.08.158-.108.241a.534.534 0 0 0-.028.133.29.29 0 0 0 .008.072.927.927 0 0 0 .082.226c.067.133.145.26.234.379l3.242 3.365.025.01.59.623c-3.265-.918-5.59-3.155-5.59-4.668 0-1.194 1.448-2.838 3.663-3.93zm7.07.531a4.632 4.632 0 0 1 1.108 5.992l.345.344.046-.018a9.313 9.313 0 0 0 2-1.112c.256-.187.5-.392.727-.613.137-.134.27-.277.392-.431.072-.091.141-.185.203-.286.057-.093.107-.19.148-.292a.72.72 0 0 0 .036-.12.29.29 0 0 0 .008-.072.492.492 0 0 0-.028-.133.999.999 0 0 0-.036-.096 2.165 2.165 0 0 0-.071-.145 2.917 2.917 0 0 0-.125-.2 3.592 3.592 0 0 0-.263-.335 5.444 5.444 0 0 0-.53-.523 7.955 7.955 0 0 0-1.054-.768 9.766 9.766 0 0 0-1.879-.891c-.337-.118-.68-.219-1.027-.301zm-2.85.21l-.069.002a.508.508 0 0 0-.254.097.496.496 0 0 0-.104.679.498.498 0 0 0 .326.199l.045.005c.091.003.181.003.272.012a2.45 2.45 0 0 1 2.017 1.513c.024.061.043.125.069.185a.494.494 0 0 0 .45.287h.008a.496.496 0 0 0 .35-.158.482.482 0 0 0 .13-.335.638.638 0 0 0-.048-.219 3.379 3.379 0 0 0-.36-.723 3.438 3.438 0 0 0-2.791-1.543l-.028-.001h-.013z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M6.91 10.54c.26-.23.64-.21.88.03l3.36 3.14 2.23-2.06a.64.64 0 0 1 .87 0l2.52 2.97V4.5H3.2v10.12l3.71-4.08zm10.27-7.51c.6 0 1.09.47 1.09 1.05v11.84c0 .59-.49 1.06-1.09 1.06H2.79c-.6 0-1.09-.47-1.09-1.06V4.08c0-.58.49-1.05 1.1-1.05h14.38zm-5.22 5.56a1.96 1.96 0 1 1 3.4-1.96 1.96 1.96 0 0 1-3.4 1.96z"/></svg>'; }, function (t, e) { t.exports = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 700 250"><rect rx="4"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M11.077 15l.991-1.416a.75.75 0 1 1 1.229.86l-1.148 1.64a.748.748 0 0 1-.217.206 5.251 5.251 0 0 1-8.503-5.955.741.741 0 0 1 .12-.274l1.147-1.639a.75.75 0 1 1 1.228.86L4.933 10.7l.006.003a3.75 3.75 0 0 0 6.132 4.294l.006.004zm5.494-5.335a.748.748 0 0 1-.12.274l-1.147 1.639a.75.75 0 1 1-1.228-.86l.86-1.23a3.75 3.75 0 0 0-6.144-4.301l-.86 1.229a.75.75 0 0 1-1.229-.86l1.148-1.64a.748.748 0 0 1 .217-.206 5.251 5.251 0 0 1 8.503 5.955zm-4.563-2.532a.75.75 0 0 1 .184 1.045l-3.155 4.505a.75.75 0 1 1-1.229-.86l3.155-4.506a.75.75 0 0 1 1.045-.184zm4.919 10.562l-1.414 1.414a.75.75 0 1 1-1.06-1.06l1.414-1.415-1.415-1.414a.75.75 0 0 1 1.061-1.06l1.414 1.414 1.414-1.415a.75.75 0 0 1 1.061 1.061l-1.414 1.414 1.414 1.415a.75.75 0 0 1-1.06 1.06l-1.415-1.414z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M7.3 17.37l-.061.088a1.518 1.518 0 0 1-.934.535l-4.178.663-.806-4.153a1.495 1.495 0 0 1 .187-1.058l.056-.086L8.77 2.639c.958-1.351 2.803-1.076 4.296-.03 1.497 1.047 2.387 2.693 1.433 4.055L7.3 17.37zM9.14 4.728l-5.545 8.346 3.277 2.294 5.544-8.346L9.14 4.728zM6.07 16.512l-3.276-2.295.53 2.73 2.746-.435zM9.994 3.506L13.271 5.8c.316-.452-.16-1.333-1.065-1.966-.905-.634-1.895-.78-2.212-.328zM8 18.5L9.375 17H19v1.5H8z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M11.077 15l.991-1.416a.75.75 0 1 1 1.229.86l-1.148 1.64a.748.748 0 0 1-.217.206 5.251 5.251 0 0 1-8.503-5.955.741.741 0 0 1 .12-.274l1.147-1.639a.75.75 0 1 1 1.228.86L4.933 10.7l.006.003a3.75 3.75 0 0 0 6.132 4.294l.006.004zm5.494-5.335a.748.748 0 0 1-.12.274l-1.147 1.639a.75.75 0 1 1-1.228-.86l.86-1.23a3.75 3.75 0 0 0-6.144-4.301l-.86 1.229a.75.75 0 0 1-1.229-.86l1.148-1.64a.748.748 0 0 1 .217-.206 5.251 5.251 0 0 1 8.503 5.955zm-4.563-2.532a.75.75 0 0 1 .184 1.045l-3.155 4.505a.75.75 0 1 1-1.229-.86l3.155-4.506a.75.75 0 0 1 1.045-.184z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M7 5.75c0 .414.336.75.75.75h9.5a.75.75 0 1 0 0-1.5h-9.5a.75.75 0 0 0-.75.75zM3.5 3v5H2V3.7H1v-1h2.5V3zM.343 17.857l2.59-3.257H2.92a.6.6 0 1 0-1.04 0H.302a2 2 0 1 1 3.995 0h-.001c-.048.405-.16.734-.333.988-.175.254-.59.692-1.244 1.312H4.3v1h-4l.043-.043zM7 14.75a.75.75 0 0 1 .75-.75h9.5a.75.75 0 1 1 0 1.5h-9.5a.75.75 0 0 1-.75-.75z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M7 5.75c0 .414.336.75.75.75h9.5a.75.75 0 1 0 0-1.5h-9.5a.75.75 0 0 0-.75.75zm-6 0C1 4.784 1.777 4 2.75 4c.966 0 1.75.777 1.75 1.75 0 .966-.777 1.75-1.75 1.75C1.784 7.5 1 6.723 1 5.75zm6 9c0 .414.336.75.75.75h9.5a.75.75 0 1 0 0-1.5h-9.5a.75.75 0 0 0-.75.75zm-6 0c0-.966.777-1.75 1.75-1.75.966 0 1.75.777 1.75 1.75 0 .966-.777 1.75-1.75 1.75-.966 0-1.75-.777-1.75-1.75z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 64 42" xmlns="http://www.w3.org/2000/svg"><path d="M47.426 17V3.713L63.102 0v19.389h-.001l.001.272c0 1.595-2.032 3.43-4.538 4.098-2.506.668-4.538-.083-4.538-1.678 0-1.594 2.032-3.43 4.538-4.098.914-.244 2.032-.565 2.888-.603V4.516L49.076 7.447v9.556A1.014 1.014 0 0 0 49 17h-1.574zM29.5 17h-8.343a7.073 7.073 0 1 0-4.657 4.06v3.781H3.3a2.803 2.803 0 0 1-2.8-2.804V8.63a2.803 2.803 0 0 1 2.8-2.805h4.082L8.58 2.768A1.994 1.994 0 0 1 10.435 1.5h8.985c.773 0 1.477.448 1.805 1.149l1.488 3.177H26.7c1.546 0 2.8 1.256 2.8 2.805V17zm-11.637 0H17.5a1 1 0 0 0-1 1v.05A4.244 4.244 0 1 1 17.863 17zm29.684 2c.97 0 .953-.048.953.889v20.743c0 .953.016.905-.953.905H19.453c-.97 0-.953.048-.953-.905V19.89c0-.937-.016-.889.97-.889h28.077zm-4.701 19.338V22.183H24.154v16.155h18.692zM20.6 21.375v1.616h1.616v-1.616H20.6zm0 3.231v1.616h1.616v-1.616H20.6zm0 3.231v1.616h1.616v-1.616H20.6zm0 3.231v1.616h1.616v-1.616H20.6zm0 3.231v1.616h1.616v-1.616H20.6zm0 3.231v1.616h1.616V37.53H20.6zm24.233-16.155v1.616h1.615v-1.616h-1.615zm0 3.231v1.616h1.615v-1.616h-1.615zm0 3.231v1.616h1.615v-1.616h-1.615zm0 3.231v1.616h1.615v-1.616h-1.615zm0 3.231v1.616h1.615v-1.616h-1.615zm0 3.231v1.616h1.615V37.53h-1.615zM29.485 25.283a.4.4 0 0 1 .593-.35l9.05 4.977a.4.4 0 0 1 0 .701l-9.05 4.978a.4.4 0 0 1-.593-.35v-9.956z"/></svg>\n'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M18.68 2.53c.6 0 .59-.03.59.55v12.84c0 .59.01.56-.59.56H1.29c-.6 0-.59.03-.59-.56V3.08c0-.58-.01-.55.6-.55h17.38zM15.77 14.5v-10H4.2v10h11.57zM2 4v1h1V4H2zm0 2v1h1V6H2zm0 2v1h1V8H2zm0 2v1h1v-1H2zm0 2v1h1v-1H2zm0 2v1h1v-1H2zM17 4v1h1V4h-1zm0 2v1h1V6h-1zm0 2v1h1V8h-1zm0 2v1h1v-1h-1zm0 2v1h1v-1h-1zm0 2v1h1v-1h-1zM7.5 6.677a.4.4 0 0 1 .593-.351l5.133 2.824a.4.4 0 0 1 0 .7l-5.133 2.824a.4.4 0 0 1-.593-.35V6.676z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path d="M3 6v3h4V6H3zm0 4v3h4v-3H3zm0 4v3h4v-3H3zm5 3h4v-3H8v3zm5 0h4v-3h-4v3zm4-4v-3h-4v3h4zm0-4V6h-4v3h4zm1.5 8a1.5 1.5 0 0 1-1.5 1.5H3A1.5 1.5 0 0 1 1.5 17V4c.222-.863 1.068-1.5 2-1.5h13c.932 0 1.778.637 2 1.5v13zM12 13v-3H8v3h4zm0-4V6H8v3h4z"/></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><g><path d="M2.5 1h15A1.5 1.5 0 0 1 19 2.5v15a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 1 17.5v-15A1.5 1.5 0 0 1 2.5 1zM2 2v16h16V2H2z" opacity=".6"/><path d="M18 7v1H2V7h16zm0 5v1H2v-1h16z" opacity=".6"/><path d="M14 1v18a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V1a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1zm-2 1H8v4h4V2zm0 6H8v4h4V8zm0 6H8v4h4v-4z"/></g></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><g><path d="M2.5 1h15A1.5 1.5 0 0 1 19 2.5v15a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 1 17.5v-15A1.5 1.5 0 0 1 2.5 1zM2 2v16h16V2H2z" opacity=".6"/><path d="M7 2h1v16H7V2zm5 0h1v16h-1V2z" opacity=".6"/><path d="M1 6h18a1 1 0 0 1 1 1v6a1 1 0 0 1-1 1H1a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1zm1 2v4h4V8H2zm6 0v4h4V8H8zm6 0v4h4V8h-4z"/></g></svg>'; }, function (t, e) { t.exports = '<svg viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><g><path d="M2.5 1h15A1.5 1.5 0 0 1 19 2.5v15a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 1 17.5v-15A1.5 1.5 0 0 1 2.5 1zM2 2v16h16V2H2z" opacity=".6"/><path d="M7 2h1v16H7V2zm5 0h1v7h-1V2zm6 5v1H2V7h16zM8 12v1H2v-1h6z" opacity=".6"/><path d="M7 7h12a1 1 0 0 1 1 1v11a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1zm1 2v9h10V9H8z"/></g></svg>'; }, function (t, e, n) {
        "use strict";
        (function (t) { var e = n(1), i = n(13); var o = "object" == typeof window ? window : t; o.CKEDITOR_VERSION ? e.a.error("ckeditor-version-collision: The global CKEDITOR_VERSION constant has already been set.", { collidingVersion: o.CKEDITOR_VERSION, version: i.a }) : o.CKEDITOR_VERSION = i.a; }).call(this, n(18));
    }, function (t, e, n) { var i = n(55); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-placeholder:before,.ck .ck-placeholder:before{content:attr(data-placeholder);pointer-events:none;cursor:text;color:var(--ck-color-engine-placeholder-text)}"; }, function (t, e) { t.exports = function (t) { var e = "undefined" != typeof window && window.location; if (!e)
        throw new Error("fixUrls requires window.location"); if (!t || "string" != typeof t)
        return t; var n = e.protocol + "//" + e.host, i = n + e.pathname.replace(/\/[^\/]*$/, "/"); return t.replace(/url\s*\(((?:[^)(]|\((?:[^)(]+|\([^)(]*\))*\))*)\)/gi, function (t, e) { var o, r = e.trim().replace(/^"(.*)"$/, function (t, e) { return e; }).replace(/^'(.*)'$/, function (t, e) { return e; }); return /^(#|data:|http:\/\/|https:\/\/|file:\/\/\/|\s*$)/i.test(r) ? t : (o = 0 === r.indexOf("//") ? r : 0 === r.indexOf("/") ? n + r : i + r.replace(/^\.\//, ""), "url(" + JSON.stringify(o) + ")"); }); }; }, function (t, e, n) { var i = n(58); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck-hidden{display:none!important}.ck.ck-reset,.ck.ck-reset_all,.ck.ck-reset_all *{box-sizing:border-box;width:auto;height:auto;position:static}:root{--ck-z-default:1;--ck-z-modal:calc(var(--ck-z-default) + 999);--ck-color-base-foreground:#fafafa;--ck-color-base-background:#fff;--ck-color-base-border:#c4c4c4;--ck-color-base-action:#61b045;--ck-color-base-focus:#6cb5f9;--ck-color-base-text:#333;--ck-color-base-active:#198cf0;--ck-color-base-active-focus:#0e7fe1;--ck-color-base-error:#db3700;--ck-color-focus-border:#47a4f5;--ck-color-focus-shadow:rgba(119,186,248,0.5);--ck-color-focus-disabled-shadow:rgba(119,186,248,0.3);--ck-color-focus-error-shadow:rgba(255,64,31,0.3);--ck-color-text:var(--ck-color-base-text);--ck-color-shadow-drop:rgba(0,0,0,0.15);--ck-color-shadow-drop-active:rgba(0,0,0,0.2);--ck-color-shadow-inner:rgba(0,0,0,0.1);--ck-color-button-default-background:transparent;--ck-color-button-default-hover-background:#e6e6e6;--ck-color-button-default-active-background:#d9d9d9;--ck-color-button-default-active-shadow:#bfbfbf;--ck-color-button-default-disabled-background:transparent;--ck-color-button-on-background:#dedede;--ck-color-button-on-hover-background:#c4c4c4;--ck-color-button-on-active-background:#bababa;--ck-color-button-on-active-shadow:#a1a1a1;--ck-color-button-on-disabled-background:#dedede;--ck-color-button-action-background:var(--ck-color-base-action);--ck-color-button-action-hover-background:#579e3d;--ck-color-button-action-active-background:#53973b;--ck-color-button-action-active-shadow:#498433;--ck-color-button-action-disabled-background:#7ec365;--ck-color-button-action-text:var(--ck-color-base-background);--ck-color-button-save:#008a00;--ck-color-button-cancel:#db3700;--ck-color-switch-button-off-background:#b0b0b0;--ck-color-switch-button-on-background:var(--ck-color-button-action-background);--ck-color-switch-button-inner-background:var(--ck-color-base-background);--ck-color-dropdown-panel-background:var(--ck-color-base-background);--ck-color-dropdown-panel-border:var(--ck-color-base-border);--ck-color-input-background:var(--ck-color-base-background);--ck-color-input-border:#c7c7c7;--ck-color-input-error-border:var(--ck-color-base-error);--ck-color-input-text:var(--ck-color-base-text);--ck-color-input-disabled-background:#f2f2f2;--ck-color-input-disabled-border:#c7c7c7;--ck-color-input-disabled-text:#5c5c5c;--ck-color-list-background:var(--ck-color-base-background);--ck-color-list-button-hover-background:var(--ck-color-button-default-hover-background);--ck-color-list-button-on-background:var(--ck-color-base-active);--ck-color-list-button-on-background-focus:var(--ck-color-base-active-focus);--ck-color-list-button-on-text:var(--ck-color-base-background);--ck-color-panel-background:var(--ck-color-base-background);--ck-color-panel-border:var(--ck-color-base-border);--ck-color-toolbar-background:var(--ck-color-base-foreground);--ck-color-toolbar-border:var(--ck-color-base-border);--ck-color-tooltip-background:var(--ck-color-base-text);--ck-color-tooltip-text:var(--ck-color-base-background);--ck-color-engine-placeholder-text:#707070;--ck-color-upload-bar-background:#6cb5f9;--ck-color-upload-infinite-background:rgba(0,0,0,0.1);--ck-color-link-default:#0000f0;--ck-color-link-selected-background:rgba(31,177,255,0.1);--ck-disabled-opacity:.5;--ck-focus-outer-shadow-geometry:0 0 0 3px;--ck-focus-outer-shadow:var(--ck-focus-outer-shadow-geometry) var(--ck-color-focus-shadow);--ck-focus-disabled-outer-shadow:var(--ck-focus-outer-shadow-geometry) var(--ck-color-focus-disabled-shadow);--ck-focus-error-outer-shadow:var(--ck-focus-outer-shadow-geometry) var(--ck-color-focus-error-shadow);--ck-focus-ring:1px solid var(--ck-color-focus-border);--ck-font-size-base:13px;--ck-line-height-base:1.84615;--ck-font-face:Helvetica,Arial,Tahoma,Verdana,Sans-Serif;--ck-font-size-tiny:0.7em;--ck-font-size-small:0.75em;--ck-font-size-normal:1em;--ck-font-size-big:1.4em;--ck-font-size-large:1.8em;--ck-ui-component-min-height:2.3em}.ck.ck-reset,.ck.ck-reset_all,.ck.ck-reset_all *{margin:0;padding:0;border:0;background:transparent;text-decoration:none;vertical-align:middle;transition:none;word-wrap:break-word}.ck.ck-reset_all,.ck.ck-reset_all *{border-collapse:collapse;font:normal normal normal var(--ck-font-size-base)/var(--ck-line-height-base) var(--ck-font-face);color:var(--ck-color-text);text-align:left;white-space:nowrap;cursor:auto;float:none}.ck.ck-reset_all .ck-rtl *{text-align:right}.ck.ck-reset_all iframe{vertical-align:inherit}.ck.ck-reset_all textarea{white-space:pre-wrap}.ck.ck-reset_all input[type=password],.ck.ck-reset_all input[type=text],.ck.ck-reset_all textarea{cursor:text}.ck.ck-reset_all input[type=password][disabled],.ck.ck-reset_all input[type=text][disabled],.ck.ck-reset_all textarea[disabled]{cursor:default}.ck.ck-reset_all fieldset{padding:10px;border:2px groove #dfdee3}.ck.ck-reset_all button::-moz-focus-inner{padding:0;border:0}:root{--ck-border-radius:2px;--ck-inner-shadow:2px 2px 3px var(--ck-color-shadow-inner) inset;--ck-drop-shadow:0 1px 2px 1px var(--ck-color-shadow-drop);--ck-drop-shadow-active:0 3px 6px 1px var(--ck-color-shadow-drop-active);--ck-spacing-unit:0.6em;--ck-spacing-large:calc(var(--ck-spacing-unit)*1.5);--ck-spacing-standard:var(--ck-spacing-unit);--ck-spacing-medium:calc(var(--ck-spacing-unit)*0.8);--ck-spacing-small:calc(var(--ck-spacing-unit)*0.5);--ck-spacing-tiny:calc(var(--ck-spacing-unit)*0.3);--ck-spacing-extra-tiny:calc(var(--ck-spacing-unit)*0.16)}"; }, function (t, e, n) { var i = n(60); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-editor__editable:not(.ck-editor__nested-editable){border-radius:0}.ck-rounded-corners .ck.ck-editor__editable:not(.ck-editor__nested-editable),.ck.ck-editor__editable:not(.ck-editor__nested-editable).ck-rounded-corners{border-radius:var(--ck-border-radius)}.ck.ck-editor__editable:not(.ck-editor__nested-editable).ck-focused{outline:none;border:var(--ck-focus-ring);box-shadow:var(--ck-inner-shadow),0 0}.ck.ck-editor__editable_inline{overflow:auto;padding:0 var(--ck-spacing-standard);border:1px solid transparent}.ck.ck-editor__editable_inline>:first-child{margin-top:var(--ck-spacing-large)}.ck.ck-editor__editable_inline>:last-child{margin-bottom:var(--ck-spacing-large)}.ck.ck-balloon-panel.ck-toolbar-container[class*=arrow_n]:after{border-bottom-color:var(--ck-color-base-foreground)}.ck.ck-balloon-panel.ck-toolbar-container[class*=arrow_s]:after{border-top-color:var(--ck-color-base-foreground)}"; }, function (t, e, n) { var i = n(62); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-toolbar{-moz-user-select:none;-webkit-user-select:none;-ms-user-select:none;user-select:none;display:flex;flex-flow:row wrap;align-items:center}.ck.ck-toolbar.ck-toolbar_vertical{flex-direction:column}.ck.ck-toolbar.ck-toolbar_floating{flex-wrap:nowrap}.ck.ck-toolbar__separator{display:inline-block}.ck.ck-toolbar__newline{display:block;width:100%}.ck.ck-toolbar{border-radius:0}.ck-rounded-corners .ck.ck-toolbar,.ck.ck-toolbar.ck-rounded-corners{border-radius:var(--ck-border-radius)}.ck.ck-toolbar{background:var(--ck-color-toolbar-background);padding:0 var(--ck-spacing-small);border:1px solid var(--ck-color-toolbar-border)}.ck.ck-toolbar>*{margin-right:var(--ck-spacing-small);margin-top:var(--ck-spacing-small);margin-bottom:var(--ck-spacing-small)}.ck.ck-toolbar.ck-toolbar_vertical{padding:0}.ck.ck-toolbar.ck-toolbar_vertical>*{width:100%;margin:0;border-radius:0;border:0}.ck.ck-toolbar>:last-child{margin-right:0}.ck-toolbar-container .ck.ck-toolbar{border:0}.ck.ck-toolbar__separator{align-self:stretch;width:1px;margin-top:0;margin-bottom:0;background:var(--ck-color-toolbar-border)}.ck.ck-toolbar__newline{margin:0}"; }, function (t, e, n) { var i = n(64); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-icon{vertical-align:middle}:root{--ck-icon-size:calc(var(--ck-line-height-base)*var(--ck-font-size-normal))}.ck.ck-icon{width:var(--ck-icon-size);height:var(--ck-icon-size);font-size:.8333350694em;will-change:transform}.ck.ck-icon,.ck.ck-icon *{color:inherit;cursor:inherit}.ck.ck-icon :not([fill]){fill:currentColor}"; }, function (t, e, n) { var i = n(66); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = '.ck.ck-tooltip,.ck.ck-tooltip .ck-tooltip__text:after{position:absolute;pointer-events:none;-webkit-backface-visibility:hidden}.ck-tooltip{visibility:hidden;opacity:0;display:none;z-index:var(--ck-z-modal)}.ck-tooltip .ck-tooltip__text{display:inline-block}.ck-tooltip .ck-tooltip__text:after{content:"";width:0;height:0}:root{--ck-tooltip-arrow-size:5px}.ck.ck-tooltip{left:50%}.ck.ck-tooltip.ck-tooltip_s{bottom:calc(-1*var(--ck-tooltip-arrow-size));transform:translateY(100%)}.ck.ck-tooltip.ck-tooltip_s .ck-tooltip__text:after{top:calc(-1*var(--ck-tooltip-arrow-size));transform:translateX(-50%);border-left-color:transparent;border-bottom-color:var(--ck-color-tooltip-background);border-right-color:transparent;border-top-color:transparent;border-left-width:var(--ck-tooltip-arrow-size);border-bottom-width:var(--ck-tooltip-arrow-size);border-right-width:var(--ck-tooltip-arrow-size);border-top-width:0}.ck.ck-tooltip.ck-tooltip_n{top:calc(-1*var(--ck-tooltip-arrow-size));transform:translateY(-100%)}.ck.ck-tooltip.ck-tooltip_n .ck-tooltip__text:after{bottom:calc(-1*var(--ck-tooltip-arrow-size));transform:translateX(-50%);border-left-color:transparent;border-bottom-color:transparent;border-right-color:transparent;border-top-color:var(--ck-color-tooltip-background);border-left-width:var(--ck-tooltip-arrow-size);border-bottom-width:0;border-right-width:var(--ck-tooltip-arrow-size);border-top-width:var(--ck-tooltip-arrow-size)}.ck.ck-tooltip .ck-tooltip__text{border-radius:0}.ck-rounded-corners .ck.ck-tooltip .ck-tooltip__text,.ck.ck-tooltip .ck-tooltip__text.ck-rounded-corners{border-radius:var(--ck-border-radius)}.ck.ck-tooltip .ck-tooltip__text{font-size:.9em;line-height:1.5;color:var(--ck-color-tooltip-text);padding:var(--ck-spacing-small) var(--ck-spacing-medium);background:var(--ck-color-tooltip-background);position:relative;left:-50%}.ck.ck-tooltip .ck-tooltip__text:after{border-style:solid;left:50%}.ck.ck-tooltip,.ck.ck-tooltip .ck-tooltip__text:after{transition:opacity .2s ease-in-out .2s}'; }, function (t, e, n) { var i = n(68); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-button,a.ck.ck-button{-moz-user-select:none;-webkit-user-select:none;-ms-user-select:none;user-select:none}.ck.ck-button .ck-tooltip,a.ck.ck-button .ck-tooltip{display:block}@media (hover:none){.ck.ck-button .ck-tooltip,a.ck.ck-button .ck-tooltip{display:none}}.ck.ck-button,a.ck.ck-button{position:relative;display:inline-flex;align-items:center;justify-content:left}.ck.ck-button.ck-button_with-text .ck-button__label,a.ck.ck-button.ck-button_with-text .ck-button__label{display:inline-block}.ck.ck-button:not(.ck-button_with-text),a.ck.ck-button:not(.ck-button_with-text){justify-content:center}.ck.ck-button:hover .ck-tooltip,a.ck.ck-button:hover .ck-tooltip{visibility:visible;opacity:1}.ck.ck-button .ck-button__label,.ck.ck-button:focus:not(:hover) .ck-tooltip,a.ck.ck-button .ck-button__label,a.ck.ck-button:focus:not(:hover) .ck-tooltip{display:none}.ck.ck-button,a.ck.ck-button{background:var(--ck-color-button-default-background)}.ck.ck-button:not(.ck-disabled):hover,a.ck.ck-button:not(.ck-disabled):hover{background:var(--ck-color-button-default-hover-background)}.ck.ck-button:not(.ck-disabled):active,a.ck.ck-button:not(.ck-disabled):active{background:var(--ck-color-button-default-active-background);box-shadow:inset 0 2px 2px var(--ck-color-button-default-active-shadow)}.ck.ck-button.ck-disabled,a.ck.ck-button.ck-disabled{background:var(--ck-color-button-default-disabled-background)}.ck.ck-button,a.ck.ck-button{border-radius:0}.ck-rounded-corners .ck.ck-button,.ck-rounded-corners a.ck.ck-button,.ck.ck-button.ck-rounded-corners,a.ck.ck-button.ck-rounded-corners{border-radius:var(--ck-border-radius)}.ck.ck-button,a.ck.ck-button{white-space:nowrap;cursor:default;vertical-align:middle;padding:var(--ck-spacing-tiny);text-align:center;min-width:var(--ck-ui-component-min-height);min-height:var(--ck-ui-component-min-height);line-height:1;font-size:inherit;border:1px solid transparent;transition:box-shadow .2s ease-in-out;-webkit-appearance:none}.ck.ck-button:active,.ck.ck-button:focus,a.ck.ck-button:active,a.ck.ck-button:focus{outline:none;border:var(--ck-focus-ring);box-shadow:var(--ck-focus-outer-shadow),0 0;border-color:transparent}.ck.ck-button.ck-disabled:active,.ck.ck-button.ck-disabled:focus,a.ck.ck-button.ck-disabled:active,a.ck.ck-button.ck-disabled:focus{box-shadow:var(--ck-focus-disabled-outer-shadow),0 0}.ck.ck-button.ck-disabled .ck-button__icon,a.ck.ck-button.ck-disabled .ck-button__icon{opacity:var(--ck-disabled-opacity)}.ck.ck-button.ck-disabled .ck-button__label,a.ck.ck-button.ck-disabled .ck-button__label{opacity:var(--ck-disabled-opacity)}.ck.ck-button.ck-button_with-text,a.ck.ck-button.ck-button_with-text{padding:var(--ck-spacing-tiny) var(--ck-spacing-standard)}.ck.ck-button.ck-button_with-text .ck-button__icon,a.ck.ck-button.ck-button_with-text .ck-button__icon{margin-left:calc(-1*var(--ck-spacing-small));margin-right:var(--ck-spacing-small)}.ck.ck-button.ck-on,a.ck.ck-button.ck-on{background:var(--ck-color-button-on-background)}.ck.ck-button.ck-on:not(.ck-disabled):hover,a.ck.ck-button.ck-on:not(.ck-disabled):hover{background:var(--ck-color-button-on-hover-background)}.ck.ck-button.ck-on:not(.ck-disabled):active,a.ck.ck-button.ck-on:not(.ck-disabled):active{background:var(--ck-color-button-on-active-background);box-shadow:inset 0 2px 2px var(--ck-color-button-on-active-shadow)}.ck.ck-button.ck-on.ck-disabled,a.ck.ck-button.ck-on.ck-disabled{background:var(--ck-color-button-on-disabled-background)}.ck.ck-button.ck-button-save,a.ck.ck-button.ck-button-save{color:var(--ck-color-button-save)}.ck.ck-button.ck-button-cancel,a.ck.ck-button.ck-button-cancel{color:var(--ck-color-button-cancel)}.ck.ck-button .ck-button__icon use,.ck.ck-button .ck-button__icon use *,a.ck.ck-button .ck-button__icon use,a.ck.ck-button .ck-button__icon use *{color:inherit}.ck.ck-button .ck-button__label,a.ck.ck-button .ck-button__label{font-size:inherit;font-weight:inherit;color:inherit;cursor:inherit;vertical-align:middle}.ck.ck-button-action,a.ck.ck-button-action{background:var(--ck-color-button-action-background)}.ck.ck-button-action:not(.ck-disabled):hover,a.ck.ck-button-action:not(.ck-disabled):hover{background:var(--ck-color-button-action-hover-background)}.ck.ck-button-action:not(.ck-disabled):active,a.ck.ck-button-action:not(.ck-disabled):active{background:var(--ck-color-button-action-active-background);box-shadow:inset 0 2px 2px var(--ck-color-button-action-active-shadow)}.ck.ck-button-action.ck-disabled,a.ck.ck-button-action.ck-disabled{background:var(--ck-color-button-action-disabled-background)}.ck.ck-button-action,a.ck.ck-button-action{color:var(--ck-color-button-action-text)}.ck.ck-button-bold,a.ck.ck-button-bold{font-weight:700}"; }, function (t, e, n) { var i = n(70); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-dropdown{display:inline-block;position:relative}.ck.ck-dropdown .ck-dropdown__arrow{pointer-events:none;z-index:var(--ck-z-default)}.ck.ck-dropdown .ck-button.ck-dropdown__button{width:100%}.ck.ck-dropdown .ck-button.ck-dropdown__button.ck-on .ck-tooltip{display:none}.ck.ck-dropdown .ck-dropdown__panel{-webkit-backface-visibility:hidden;display:none;z-index:var(--ck-z-modal);position:absolute}.ck.ck-dropdown .ck-dropdown__panel.ck-dropdown__panel-visible{display:inline-block;will-change:transform}.ck.ck-dropdown .ck-dropdown__panel.ck-dropdown__panel_ne,.ck.ck-dropdown .ck-dropdown__panel.ck-dropdown__panel_nw{bottom:100%}.ck.ck-dropdown .ck-dropdown__panel.ck-dropdown__panel_se,.ck.ck-dropdown .ck-dropdown__panel.ck-dropdown__panel_sw{transform:translate3d(0,100%,0)}.ck.ck-dropdown .ck-dropdown__panel.ck-dropdown__panel_ne,.ck.ck-dropdown .ck-dropdown__panel.ck-dropdown__panel_se{left:0}.ck.ck-dropdown .ck-dropdown__panel.ck-dropdown__panel_nw,.ck.ck-dropdown .ck-dropdown__panel.ck-dropdown__panel_sw{right:0}:root{--ck-dropdown-arrow-size:calc(0.5*var(--ck-icon-size))}.ck.ck-dropdown{font-size:inherit}.ck.ck-dropdown .ck-dropdown__arrow{right:var(--ck-spacing-standard);width:var(--ck-dropdown-arrow-size);margin-left:var(--ck-spacing-small)}.ck.ck-dropdown.ck-disabled .ck-dropdown__arrow{opacity:var(--ck-disabled-opacity)}.ck.ck-dropdown .ck-button.ck-dropdown__button:not(.ck-button_with-text){padding-left:var(--ck-spacing-small)}.ck.ck-dropdown .ck-button.ck-dropdown__button.ck-disabled .ck-button__label{opacity:var(--ck-disabled-opacity)}.ck.ck-dropdown .ck-button.ck-dropdown__button.ck-on{border-bottom-left-radius:0;border-bottom-right-radius:0}.ck.ck-dropdown .ck-button.ck-dropdown__button .ck-button__label{width:7em;overflow:hidden;text-overflow:ellipsis}.ck.ck-dropdown__panel{box-shadow:var(--ck-drop-shadow),0 0;border-radius:0}.ck-rounded-corners .ck.ck-dropdown__panel,.ck.ck-dropdown__panel.ck-rounded-corners{border-radius:var(--ck-border-radius);border-top-left-radius:0}.ck.ck-dropdown__panel{background:var(--ck-color-dropdown-panel-background);border:1px solid var(--ck-color-dropdown-panel-border);bottom:0;min-width:100%}"; }, function (t, e, n) { var i = n(72); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-list{-moz-user-select:none;-webkit-user-select:none;-ms-user-select:none;user-select:none;display:flex;flex-direction:column}.ck.ck-list .ck-list__item,.ck.ck-list .ck-list__separator{display:block}.ck.ck-list .ck-list__item>:focus{position:relative;z-index:var(--ck-z-default)}.ck.ck-list{border-radius:0}.ck-rounded-corners .ck.ck-list,.ck.ck-list.ck-rounded-corners{border-radius:var(--ck-border-radius)}.ck.ck-list{list-style-type:none;background:var(--ck-color-list-background)}.ck.ck-list__item{cursor:default;min-width:12em}.ck.ck-list__item .ck-button{min-height:unset;width:100%;text-align:left;border-radius:0;border:0;padding:calc(0.2*var(--ck-line-height-base)*var(--ck-font-size-base)) calc(0.4*var(--ck-line-height-base)*var(--ck-font-size-base))}.ck.ck-list__item .ck-button .ck-button__label{line-height:calc(1.2*var(--ck-line-height-base)*var(--ck-font-size-base))}.ck.ck-list__item .ck-button:active{box-shadow:none}.ck.ck-list__item .ck-button.ck-on{background:var(--ck-color-list-button-on-background);color:var(--ck-color-list-button-on-text)}.ck.ck-list__item .ck-button.ck-on:hover:not(ck-disabled){background:var(--ck-color-list-button-on-background-focus)}.ck.ck-list__item .ck-button.ck-on:active{box-shadow:none}.ck.ck-list__item .ck-button:hover:not(.ck-disabled){background:var(--ck-color-list-button-hover-background)}.ck.ck-list__item .ck-switchbutton.ck-on{background:var(--ck-color-list-background);color:inherit}.ck.ck-list__item .ck-switchbutton.ck-on:hover:not(ck-disabled){background:var(--ck-color-list-button-hover-background);color:inherit}.ck.ck-list__separator{height:1px;width:100%;background:var(--ck-color-base-border)}"; }, function (t, e, n) { var i = n(74); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-button.ck-switchbutton .ck-button__toggle,.ck.ck-button.ck-switchbutton .ck-button__toggle .ck-button__toggle__inner{display:block}:root{--ck-switch-button-toggle-width:2.6153846154em;--ck-switch-button-toggle-inner-size:1.0769230769em;--ck-switch-button-toggle-spacing:1px}.ck.ck-button.ck-switchbutton .ck-button__label{margin-right:calc(2*var(--ck-spacing-large))}.ck.ck-button.ck-switchbutton.ck-disabled .ck-button__toggle{opacity:var(--ck-disabled-opacity)}.ck.ck-button.ck-switchbutton .ck-button__toggle{border-radius:0}.ck-rounded-corners .ck.ck-button.ck-switchbutton .ck-button__toggle,.ck.ck-button.ck-switchbutton .ck-button__toggle.ck-rounded-corners{border-radius:var(--ck-border-radius)}.ck.ck-button.ck-switchbutton .ck-button__toggle{margin-left:auto;transition:background .4s ease;width:var(--ck-switch-button-toggle-width);background:var(--ck-color-switch-button-off-background)}.ck.ck-button.ck-switchbutton .ck-button__toggle .ck-button__toggle__inner{border-radius:0}.ck-rounded-corners .ck.ck-button.ck-switchbutton .ck-button__toggle .ck-button__toggle__inner,.ck.ck-button.ck-switchbutton .ck-button__toggle .ck-button__toggle__inner.ck-rounded-corners{border-radius:var(--ck-border-radius);border-radius:calc(0.5*var(--ck-border-radius))}.ck.ck-button.ck-switchbutton .ck-button__toggle .ck-button__toggle__inner{margin:var(--ck-switch-button-toggle-spacing);width:var(--ck-switch-button-toggle-inner-size);height:var(--ck-switch-button-toggle-inner-size);background:var(--ck-color-switch-button-inner-background);transition:transform .3s ease}.ck.ck-button.ck-switchbutton.ck-on .ck-button__toggle{background:var(--ck-color-switch-button-on-background)}.ck.ck-button.ck-switchbutton.ck-on .ck-button__toggle .ck-button__toggle__inner{transform:translateX(1.3846153847em)}"; }, function (t, e, n) { var i = n(76); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-toolbar-dropdown .ck-toolbar{flex-wrap:nowrap}.ck.ck-toolbar-dropdown .ck-dropdown__panel .ck-button:focus{z-index:calc(var(--ck-z-default) + 1)}.ck.ck-toolbar-dropdown .ck-toolbar{border:0}"; }, function (t, e, n) { var i = n(78); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-dropdown .ck-dropdown__panel .ck-list{border-radius:0}.ck-rounded-corners .ck.ck-dropdown .ck-dropdown__panel .ck-list,.ck.ck-dropdown .ck-dropdown__panel .ck-list.ck-rounded-corners{border-radius:var(--ck-border-radius);border-top-left-radius:0}.ck.ck-dropdown .ck-dropdown__panel .ck-list .ck-list__item:first-child .ck-button{border-radius:0}.ck-rounded-corners .ck.ck-dropdown .ck-dropdown__panel .ck-list .ck-list__item:first-child .ck-button,.ck.ck-dropdown .ck-dropdown__panel .ck-list .ck-list__item:first-child .ck-button.ck-rounded-corners{border-radius:var(--ck-border-radius);border-top-left-radius:0;border-bottom-left-radius:0;border-bottom-right-radius:0}.ck.ck-dropdown .ck-dropdown__panel .ck-list .ck-list__item:last-child .ck-button{border-radius:0}.ck-rounded-corners .ck.ck-dropdown .ck-dropdown__panel .ck-list .ck-list__item:last-child .ck-button,.ck.ck-dropdown .ck-dropdown__panel .ck-list .ck-list__item:last-child .ck-button.ck-rounded-corners{border-radius:var(--ck-border-radius);border-top-left-radius:0;border-top-right-radius:0}"; }, function (t, e, n) { var i = n(80); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".text-tiny{font-size:.7em}.text-small{font-size:.85em}.text-big{font-size:1.4em}.text-huge{font-size:1.8em}"; }, function (t, e, n) { var i = n(82); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-splitbutton{font-size:inherit}.ck.ck-splitbutton .ck-splitbutton__action:focus{z-index:calc(var(--ck-z-default) + 1)}.ck.ck-splitbutton.ck-splitbutton_open>.ck-button .ck-tooltip{display:none}:root{--ck-color-split-button-hover-background:#ebebeb;--ck-color-split-button-hover-border:#b3b3b3}.ck.ck-splitbutton>.ck-splitbutton__action{border-radius:0}.ck-rounded-corners .ck.ck-splitbutton>.ck-splitbutton__action,.ck.ck-splitbutton>.ck-splitbutton__action.ck-rounded-corners{border-radius:var(--ck-border-radius);border-top-right-radius:unset;border-bottom-right-radius:unset}.ck.ck-splitbutton>.ck-splitbutton__arrow{min-width:unset;border-radius:0}.ck-rounded-corners .ck.ck-splitbutton>.ck-splitbutton__arrow,.ck.ck-splitbutton>.ck-splitbutton__arrow.ck-rounded-corners{border-radius:var(--ck-border-radius);border-top-left-radius:unset;border-bottom-left-radius:unset}.ck.ck-splitbutton>.ck-splitbutton__arrow svg{width:var(--ck-dropdown-arrow-size)}.ck.ck-splitbutton.ck-splitbutton_open>.ck-button:not(.ck-on):not(:hover),.ck.ck-splitbutton:hover>.ck-button:not(.ck-on):not(:hover){background:var(--ck-color-split-button-hover-background)}.ck.ck-splitbutton.ck-splitbutton_open>.ck-splitbutton__arrow,.ck.ck-splitbutton:hover>.ck-splitbutton__arrow{border-left-color:var(--ck-color-split-button-hover-border)}.ck.ck-splitbutton.ck-splitbutton_open{border-radius:0}.ck-rounded-corners .ck.ck-splitbutton.ck-splitbutton_open,.ck.ck-splitbutton.ck-splitbutton_open.ck-rounded-corners{border-radius:var(--ck-border-radius)}.ck-rounded-corners .ck.ck-splitbutton.ck-splitbutton_open>.ck-splitbutton__action,.ck.ck-splitbutton.ck-splitbutton_open.ck-rounded-corners>.ck-splitbutton__action{border-bottom-left-radius:0}.ck-rounded-corners .ck.ck-splitbutton.ck-splitbutton_open>.ck-splitbutton__arrow,.ck.ck-splitbutton.ck-splitbutton_open.ck-rounded-corners>.ck-splitbutton__arrow{border-bottom-right-radius:0}"; }, function (t, e, n) { var i = n(84); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ":root{--ck-highlight-marker-yellow:#fdfd77;--ck-highlight-marker-green:#63f963;--ck-highlight-marker-pink:#fc7999;--ck-highlight-marker-blue:#72cdfd;--ck-highlight-pen-red:#e91313;--ck-highlight-pen-green:#180}.marker-yellow{background-color:var(--ck-highlight-marker-yellow)}.marker-green{background-color:var(--ck-highlight-marker-green)}.marker-pink{background-color:var(--ck-highlight-marker-pink)}.marker-blue{background-color:var(--ck-highlight-marker-blue)}.pen-red{color:var(--ck-highlight-pen-red)}.pen-green,.pen-red{background-color:transparent}.pen-green{color:var(--ck-highlight-pen-green)}"; }, function (t, e, n) { var i = n(86); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck-content blockquote{overflow:hidden;padding-right:1.5em;padding-left:1.5em;margin-left:0;font-style:italic;border-left:5px solid #ccc}"; }, function (t, e, n) { var i = n(88); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck .ck-widget.ck-widget_with-selection-handler{position:relative}.ck .ck-widget.ck-widget_with-selection-handler .ck-widget__selection-handler{visibility:hidden;position:absolute}.ck .ck-widget.ck-widget_with-selection-handler .ck-widget__selection-handler .ck-icon{display:block}.ck .ck-widget.ck-widget_with-selection-handler.ck-widget_selected .ck-widget__selection-handler,.ck .ck-widget.ck-widget_with-selection-handler:hover .ck-widget__selection-handler{visibility:visible}:root{--ck-widget-outline-thickness:3px;--ck-widget-handler-icon-size:16px;--ck-widget-handler-animation-duration:200ms;--ck-widget-handler-animation-curve:ease;--ck-color-widget-blurred-border:#dedede;--ck-color-widget-hover-border:#ffc83d;--ck-color-widget-editable-focus-background:var(--ck-color-base-background);--ck-color-widget-drag-handler-icon-color:var(--ck-color-base-background)}.ck .ck-widget{outline-width:var(--ck-widget-outline-thickness);outline-style:solid;outline-color:transparent;transition:outline-color var(--ck-widget-handler-animation-duration) var(--ck-widget-handler-animation-curve)}.ck .ck-widget.ck-widget_selected,.ck .ck-widget.ck-widget_selected:hover{outline:var(--ck-widget-outline-thickness) solid var(--ck-color-focus-border)}.ck .ck-widget:hover{outline-color:var(--ck-color-widget-hover-border)}.ck .ck-editor__nested-editable{border:1px solid transparent}.ck .ck-editor__nested-editable.ck-editor__nested-editable_focused,.ck .ck-editor__nested-editable:focus{outline:none;border:var(--ck-focus-ring);box-shadow:var(--ck-inner-shadow),0 0;background-color:var(--ck-color-widget-editable-focus-background)}.ck-editor__editable>.ck-widget.ck-widget_with-selection-handler:first-child,.ck-editor__editable blockquote>.ck-widget.ck-widget_with-selection-handler:first-child{margin-top:calc(1em + var(--ck-widget-handler-icon-size))}.ck .ck-widget.ck-widget_with-selection-handler .ck-widget__selection-handler{padding:4px;box-sizing:border-box;background-color:transparent;opacity:0;transition:background-color var(--ck-widget-handler-animation-duration) var(--ck-widget-handler-animation-curve),visibility var(--ck-widget-handler-animation-duration) var(--ck-widget-handler-animation-curve),opacity var(--ck-widget-handler-animation-duration) var(--ck-widget-handler-animation-curve);border-radius:var(--ck-border-radius) var(--ck-border-radius) 0 0;transform:translateY(-100%);left:calc(0px - var(--ck-widget-outline-thickness))}.ck .ck-widget.ck-widget_with-selection-handler .ck-widget__selection-handler:hover .ck-icon .ck-icon__selected-indicator{opacity:1}.ck .ck-widget.ck-widget_with-selection-handler .ck-widget__selection-handler .ck-icon{width:var(--ck-widget-handler-icon-size);height:var(--ck-widget-handler-icon-size);color:var(--ck-color-widget-drag-handler-icon-color)}.ck .ck-widget.ck-widget_with-selection-handler .ck-widget__selection-handler .ck-icon .ck-icon__selected-indicator{opacity:0;transition:opacity .3s var(--ck-widget-handler-animation-curve)}.ck .ck-widget.ck-widget_with-selection-handler.ck-widget_selected .ck-widget__selection-handler,.ck .ck-widget.ck-widget_with-selection-handler.ck-widget_selected:hover .ck-widget__selection-handler{opacity:1;background-color:var(--ck-color-focus-border)}.ck .ck-widget.ck-widget_with-selection-handler.ck-widget_selected .ck-widget__selection-handler .ck-icon .ck-icon__selected-indicator,.ck .ck-widget.ck-widget_with-selection-handler.ck-widget_selected:hover .ck-widget__selection-handler .ck-icon .ck-icon__selected-indicator{opacity:1}.ck .ck-widget.ck-widget_with-selection-handler:hover .ck-widget__selection-handler{opacity:1;background-color:var(--ck-color-widget-hover-border)}.ck-editor__editable.ck-blurred .ck-widget.ck-widget_selected,.ck-editor__editable.ck-blurred .ck-widget.ck-widget_selected:hover{outline-color:var(--ck-color-widget-blurred-border)}.ck-editor__editable.ck-blurred .ck-widget.ck-widget_selected .ck-widget__selection-handler,.ck-editor__editable.ck-blurred .ck-widget.ck-widget_selected .ck-widget__selection-handler:hover,.ck-editor__editable.ck-blurred .ck-widget.ck-widget_selected:hover .ck-widget__selection-handler,.ck-editor__editable.ck-blurred .ck-widget.ck-widget_selected:hover .ck-widget__selection-handler:hover{background:var(--ck-color-widget-blurred-border)}.ck-editor__editable.ck-read-only .ck-widget{--ck-widget-outline-thickness:0}"; }, function (t, e, n) { var i = n(90); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-label{display:block}.ck.ck-voice-label{display:none}.ck.ck-label{font-weight:700}"; }, function (t, e, n) { var i = n(92); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-labeled-input .ck-labeled-input__status{font-size:var(--ck-font-size-small);margin-top:var(--ck-spacing-small);white-space:normal}.ck.ck-labeled-input .ck-labeled-input__status_error{color:var(--ck-color-base-error)}"; }, function (t, e, n) { var i = n(94); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ":root{--ck-input-text-width:18em}.ck.ck-input-text{border-radius:0}.ck-rounded-corners .ck.ck-input-text,.ck.ck-input-text.ck-rounded-corners{border-radius:var(--ck-border-radius)}.ck.ck-input-text{box-shadow:var(--ck-inner-shadow),0 0;background:var(--ck-color-input-background);border:1px solid var(--ck-color-input-border);padding:var(--ck-spacing-extra-tiny) var(--ck-spacing-medium);min-width:var(--ck-input-text-width);min-height:var(--ck-ui-component-min-height);transition-property:box-shadow,border;transition:.2s ease-in-out}.ck.ck-input-text:focus{outline:none;border:var(--ck-focus-ring);box-shadow:var(--ck-focus-outer-shadow),var(--ck-inner-shadow)}.ck.ck-input-text[readonly]{border:1px solid var(--ck-color-input-disabled-border);background:var(--ck-color-input-disabled-background);color:var(--ck-color-input-disabled-text)}.ck.ck-input-text[readonly]:focus{box-shadow:var(--ck-focus-disabled-outer-shadow),var(--ck-inner-shadow)}.ck.ck-input-text.ck-error{border-color:var(--ck-color-input-error-border);animation:ck-text-input-shake .3s ease both}.ck.ck-input-text.ck-error:focus{box-shadow:var(--ck-focus-error-outer-shadow),var(--ck-inner-shadow)}@keyframes ck-text-input-shake{20%{transform:translateX(-2px)}40%{transform:translateX(2px)}60%{transform:translateX(-1px)}80%{transform:translateX(1px)}}"; }, function (t, e, n) { var i = n(96); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-text-alternative-form{display:flex;flex-direction:row;flex-wrap:nowrap}.ck.ck-text-alternative-form .ck-labeled-input{display:inline-block}.ck.ck-text-alternative-form .ck-label{display:none}@media screen and (max-width:600px){.ck.ck-text-alternative-form{flex-wrap:wrap}.ck.ck-text-alternative-form .ck-labeled-input{flex-basis:100%}.ck.ck-text-alternative-form .ck-button{flex-basis:50%}}.ck.ck-text-alternative-form{padding:var(--ck-spacing-standard)}.ck.ck-text-alternative-form:focus{outline:none}.ck.ck-text-alternative-form>:not(:first-child){margin-left:var(--ck-spacing-standard)}@media screen and (max-width:600px){.ck.ck-text-alternative-form{padding:0;width:calc(0.8*var(--ck-input-text-width))}.ck.ck-text-alternative-form .ck-labeled-input{margin:var(--ck-spacing-standard) var(--ck-spacing-standard) 0}.ck.ck-text-alternative-form .ck-labeled-input .ck-input-text{min-width:0;width:100%}.ck.ck-text-alternative-form .ck-button{padding:var(--ck-spacing-standard);margin-top:var(--ck-spacing-standard);margin-left:0;border-radius:0;border:0;border-top:1px solid var(--ck-color-base-border)}.ck.ck-text-alternative-form .ck-button:first-of-type{border-right:1px solid var(--ck-color-base-border)}}"; }, function (t, e, n) { var i = n(98); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ':root{--ck-balloon-panel-arrow-z-index:calc(var(--ck-z-default) - 3)}.ck.ck-balloon-panel{display:none;position:absolute;z-index:var(--ck-z-modal)}.ck.ck-balloon-panel.ck-balloon-panel_with-arrow:after,.ck.ck-balloon-panel.ck-balloon-panel_with-arrow:before{content:"";position:absolute}.ck.ck-balloon-panel.ck-balloon-panel_with-arrow:before{z-index:var(--ck-balloon-panel-arrow-z-index)}.ck.ck-balloon-panel.ck-balloon-panel_with-arrow:after{z-index:calc(var(--ck-balloon-panel-arrow-z-index) + 1)}.ck.ck-balloon-panel[class*=arrow_n]:before{z-index:var(--ck-balloon-panel-arrow-z-index)}.ck.ck-balloon-panel[class*=arrow_n]:after{z-index:calc(var(--ck-balloon-panel-arrow-z-index) + 1)}.ck.ck-balloon-panel[class*=arrow_s]:before{z-index:var(--ck-balloon-panel-arrow-z-index)}.ck.ck-balloon-panel[class*=arrow_s]:after{z-index:calc(var(--ck-balloon-panel-arrow-z-index) + 1)}.ck.ck-balloon-panel.ck-balloon-panel_visible{display:block}:root{--ck-balloon-arrow-offset:2px;--ck-balloon-arrow-height:10px;--ck-balloon-arrow-half-width:8px}.ck.ck-balloon-panel{border-radius:0}.ck-rounded-corners .ck.ck-balloon-panel,.ck.ck-balloon-panel.ck-rounded-corners{border-radius:var(--ck-border-radius)}.ck.ck-balloon-panel{box-shadow:var(--ck-drop-shadow),0 0;min-height:15px;background:var(--ck-color-panel-background);border:1px solid var(--ck-color-panel-border)}.ck.ck-balloon-panel.ck-balloon-panel_with-arrow:after,.ck.ck-balloon-panel.ck-balloon-panel_with-arrow:before{width:0;height:0;border-style:solid}.ck.ck-balloon-panel[class*=arrow_n]:after,.ck.ck-balloon-panel[class*=arrow_n]:before{border-left-width:var(--ck-balloon-arrow-half-width);border-bottom-width:var(--ck-balloon-arrow-height);border-right-width:var(--ck-balloon-arrow-half-width);border-top-width:0}.ck.ck-balloon-panel[class*=arrow_n]:before{border-bottom-color:var(--ck-color-panel-border)}.ck.ck-balloon-panel[class*=arrow_n]:after,.ck.ck-balloon-panel[class*=arrow_n]:before{border-left-color:transparent;border-right-color:transparent;border-top-color:transparent}.ck.ck-balloon-panel[class*=arrow_n]:after{border-bottom-color:var(--ck-color-panel-background);margin-top:var(--ck-balloon-arrow-offset)}.ck.ck-balloon-panel[class*=arrow_s]:after,.ck.ck-balloon-panel[class*=arrow_s]:before{border-left-width:var(--ck-balloon-arrow-half-width);border-bottom-width:0;border-right-width:var(--ck-balloon-arrow-half-width);border-top-width:var(--ck-balloon-arrow-height)}.ck.ck-balloon-panel[class*=arrow_s]:before{border-top-color:var(--ck-color-panel-border)}.ck.ck-balloon-panel[class*=arrow_s]:after,.ck.ck-balloon-panel[class*=arrow_s]:before{border-left-color:transparent;border-bottom-color:transparent;border-right-color:transparent}.ck.ck-balloon-panel[class*=arrow_s]:after{border-top-color:var(--ck-color-panel-background);margin-bottom:var(--ck-balloon-arrow-offset)}.ck.ck-balloon-panel.ck-balloon-panel_arrow_n:after,.ck.ck-balloon-panel.ck-balloon-panel_arrow_n:before{left:50%;margin-left:calc(-1*var(--ck-balloon-arrow-half-width));top:calc(-1*var(--ck-balloon-arrow-height))}.ck.ck-balloon-panel.ck-balloon-panel_arrow_nw:after,.ck.ck-balloon-panel.ck-balloon-panel_arrow_nw:before{left:calc(2*var(--ck-balloon-arrow-half-width));top:calc(-1*var(--ck-balloon-arrow-height))}.ck.ck-balloon-panel.ck-balloon-panel_arrow_ne:after,.ck.ck-balloon-panel.ck-balloon-panel_arrow_ne:before{right:calc(2*var(--ck-balloon-arrow-half-width));top:calc(-1*var(--ck-balloon-arrow-height))}.ck.ck-balloon-panel.ck-balloon-panel_arrow_s:after,.ck.ck-balloon-panel.ck-balloon-panel_arrow_s:before{left:50%;margin-left:calc(-1*var(--ck-balloon-arrow-half-width));bottom:calc(-1*var(--ck-balloon-arrow-height))}.ck.ck-balloon-panel.ck-balloon-panel_arrow_sw:after,.ck.ck-balloon-panel.ck-balloon-panel_arrow_sw:before{left:calc(2*var(--ck-balloon-arrow-half-width));bottom:calc(-1*var(--ck-balloon-arrow-height))}.ck.ck-balloon-panel.ck-balloon-panel_arrow_se:after,.ck.ck-balloon-panel.ck-balloon-panel_arrow_se:before{right:calc(2*var(--ck-balloon-arrow-half-width));bottom:calc(-1*var(--ck-balloon-arrow-height))}'; }, function (t, e, n) { var i = n(100); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck-content .image{clear:both;text-align:center;margin:1em 0}.ck-content .image>img{display:block;margin:0 auto;max-width:100%}"; }, function (t, e, n) { var i = n(102); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck-content .image{position:relative;overflow:hidden}.ck-content .image .ck-progress-bar{position:absolute;top:0;left:0}:root{--ck-image-upload-progress-line-width:30px}.ck-content .image.ck-appear{animation:fadeIn .7s}.ck-content .image .ck-progress-bar{height:2px;width:0;background:var(--ck-color-upload-bar-background);transition:width .1s}@keyframes fadeIn{0%{opacity:0}to{opacity:1}}"; }, function (t, e, n) { var i = n(104); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = '.ck-image-upload-complete-icon{display:block;position:absolute;top:10px;right:10px;border-radius:50%}.ck-image-upload-complete-icon:after{content:"";position:absolute}:root{--ck-color-image-upload-icon:#fff;--ck-color-image-upload-icon-background:#008a00;--ck-image-upload-icon-size:20px;--ck-image-upload-icon-width:2px}.ck-image-upload-complete-icon{width:var(--ck-image-upload-icon-size);height:var(--ck-image-upload-icon-size);opacity:0;background:var(--ck-color-image-upload-icon-background);animation-name:ck-upload-complete-icon-show,ck-upload-complete-icon-hide;animation-fill-mode:forwards,forwards;animation-duration:.5s,.5s;font-size:var(--ck-image-upload-icon-size);animation-delay:0ms,3s}.ck-image-upload-complete-icon:after{left:25%;top:50%;opacity:0;height:0;width:0;transform:scaleX(-1) rotate(135deg);transform-origin:left top;border-top:var(--ck-image-upload-icon-width) solid var(--ck-color-image-upload-icon);border-right:var(--ck-image-upload-icon-width) solid var(--ck-color-image-upload-icon);animation-name:ck-upload-complete-icon-check;animation-duration:.5s;animation-delay:.5s;animation-fill-mode:forwards;box-sizing:border-box}@keyframes ck-upload-complete-icon-show{0%{opacity:0}to{opacity:1}}@keyframes ck-upload-complete-icon-hide{0%{opacity:1}to{opacity:0}}@keyframes ck-upload-complete-icon-check{0%{opacity:1;width:0;height:0}33%{width:.3em;height:0}to{opacity:1;width:.3em;height:.45em}}'; }, function (t, e, n) { var i = n(106); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = '.ck .ck-upload-placeholder-loader{position:absolute;display:flex;align-items:center;justify-content:center;top:0;left:0}.ck .ck-upload-placeholder-loader:before{content:"";position:relative}:root{--ck-color-upload-placeholder-loader:#b3b3b3;--ck-upload-placeholder-loader-size:32px}.ck .ck-image-upload-placeholder{width:100%;margin:0}.ck .ck-upload-placeholder-loader{width:100%;height:100%}.ck .ck-upload-placeholder-loader:before{width:var(--ck-upload-placeholder-loader-size);height:var(--ck-upload-placeholder-loader-size);border-radius:50%;border-top:3px solid var(--ck-color-upload-placeholder-loader);border-right:2px solid transparent;animation:ck-upload-placeholder-loader 1s linear infinite}@keyframes ck-upload-placeholder-loader{to{transform:rotate(1turn)}}'; }, function (t, e) { t.exports = ".ck.ck-heading_heading1{font-size:20px}.ck.ck-heading_heading2{font-size:17px}.ck.ck-heading_heading3{font-size:14px}.ck[class*=ck-heading_heading]{font-weight:700}.ck.ck-dropdown.ck-heading-dropdown .ck-dropdown__button .ck-button__label{width:8em}.ck.ck-dropdown.ck-heading-dropdown .ck-dropdown__panel .ck-list__item{min-width:18em}"; }, function (t, e, n) { var i = n(109); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck-content .image>figcaption{color:#333;background-color:#f7f7f7;padding:.6em;font-size:.75em;outline-offset:-1px}"; }, function (t, e, n) { var i = n(111); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ":root{--ck-image-style-spacing:1.5em}.ck-content .image-style-align-center,.ck-content .image-style-align-left,.ck-content .image-style-align-right,.ck-content .image-style-side{max-width:50%}.ck-content .image-style-side{float:right;margin-left:var(--ck-image-style-spacing)}.ck-content .image-style-align-left{float:left;margin-right:var(--ck-image-style-spacing)}.ck-content .image-style-align-center{margin-left:auto;margin-right:auto}.ck-content .image-style-align-right{float:right;margin-left:var(--ck-image-style-spacing)}"; }, function (t, e, n) { var i = n(113); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck .ck-link_selected{background:var(--ck-color-link-selected-background)}"; }, function (t, e, n) { var i = n(115); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-link-form{display:flex;flex-direction:row;flex-wrap:nowrap}.ck.ck-link-form .ck-label{display:none}@media screen and (max-width:600px){.ck.ck-link-form{flex-wrap:wrap}.ck.ck-link-form .ck-labeled-input{flex-basis:100%}.ck.ck-link-form .ck-button{flex-basis:50%}}.ck.ck-link-form{padding:var(--ck-spacing-standard)}.ck.ck-link-form:focus{outline:none}.ck.ck-link-form>:not(:first-child){margin-left:var(--ck-spacing-standard)}@media screen and (max-width:600px){.ck.ck-link-form{padding:0;width:calc(0.8*var(--ck-input-text-width))}.ck.ck-link-form .ck-labeled-input{margin:var(--ck-spacing-standard) var(--ck-spacing-standard) 0}.ck.ck-link-form .ck-labeled-input .ck-input-text{min-width:0;width:100%}.ck.ck-link-form .ck-button{padding:var(--ck-spacing-standard);margin-top:var(--ck-spacing-standard);margin-left:0;border-radius:0;border:0;border-top:1px solid var(--ck-color-base-border)}.ck.ck-link-form .ck-button:first-of-type{border-right:1px solid var(--ck-color-base-border)}}"; }, function (t, e, n) { var i = n(117); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-link-actions{display:flex;flex-direction:row;flex-wrap:nowrap}.ck.ck-link-actions .ck-link-actions__preview{display:inline-block}.ck.ck-link-actions .ck-link-actions__preview .ck-button__label{overflow:hidden}@media screen and (max-width:600px){.ck.ck-link-actions{flex-wrap:wrap}.ck.ck-link-actions .ck-link-actions__preview{flex-basis:100%}.ck.ck-link-actions .ck-button:not(.ck-link-actions__preview){flex-basis:50%}}.ck.ck-link-actions{padding:var(--ck-spacing-standard)}.ck.ck-link-actions .ck-button.ck-link-actions__preview{padding-left:0;padding-right:0}.ck.ck-link-actions .ck-button.ck-link-actions__preview,.ck.ck-link-actions .ck-button.ck-link-actions__preview:active,.ck.ck-link-actions .ck-button.ck-link-actions__preview:focus,.ck.ck-link-actions .ck-button.ck-link-actions__preview:hover{background:none}.ck.ck-link-actions .ck-button.ck-link-actions__preview:active{box-shadow:none}.ck.ck-link-actions .ck-button.ck-link-actions__preview:focus .ck-button__label{text-decoration:underline}.ck.ck-link-actions .ck-button.ck-link-actions__preview .ck-button__label{padding:0 var(--ck-spacing-medium);color:var(--ck-color-link-default);text-overflow:ellipsis;cursor:pointer;max-width:var(--ck-input-text-width);min-width:3em;text-align:center}.ck.ck-link-actions .ck-button.ck-link-actions__preview .ck-button__label:hover{text-decoration:underline}.ck.ck-link-actions:focus{outline:none}.ck.ck-link-actions .ck-button:not(.ck-link-actions__preview){margin-left:var(--ck-spacing-standard)}@media screen and (max-width:600px){.ck.ck-link-actions{padding:0;width:calc(0.8*var(--ck-input-text-width))}.ck.ck-link-actions .ck-button.ck-link-actions__preview{margin:var(--ck-spacing-standard) var(--ck-spacing-standard) 0}.ck.ck-link-actions .ck-button.ck-link-actions__preview .ck-button__label{min-width:0;max-width:100%}.ck.ck-link-actions .ck-button:not(.ck-link-actions__preview){padding:var(--ck-spacing-standard);margin-top:var(--ck-spacing-standard);margin-left:0;border-radius:0;border:0;border-top:1px solid var(--ck-color-base-border)}.ck.ck-link-actions .ck-button:not(.ck-link-actions__preview):first-of-type{border-right:1px solid var(--ck-color-base-border)}}"; }, function (t, e, n) { var i = n(119); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = '.ck-media__wrapper .ck-media__placeholder{display:flex;flex-direction:column;align-items:center}.ck-media__wrapper .ck-media__placeholder .ck-media__placeholder__url .ck-tooltip{display:block}@media (hover:none){.ck-media__wrapper .ck-media__placeholder .ck-media__placeholder__url .ck-tooltip{display:none}}.ck-media__wrapper .ck-media__placeholder .ck-media__placeholder__url{max-width:100%;position:relative}.ck-media__wrapper .ck-media__placeholder .ck-media__placeholder__url:hover .ck-tooltip{visibility:visible;opacity:1}.ck-media__wrapper .ck-media__placeholder .ck-media__placeholder__url .ck-media__placeholder__url__text{overflow:hidden;display:block}.ck-media__wrapper[data-oembed-url*="facebook.com"] .ck-media__placeholder__icon *,.ck-media__wrapper[data-oembed-url*="google.com/maps"] .ck-media__placeholder__icon *,.ck-media__wrapper[data-oembed-url*="instagram.com"] .ck-media__placeholder__icon *,.ck-media__wrapper[data-oembed-url*="twitter.com"] .ck-media__placeholder__icon *{display:none}.ck-editor__editable:not(.ck-read-only) .ck-media__wrapper>:not(.ck-media__placeholder){pointer-events:none}:root{--ck-media-embed-placeholder-icon-size:3em;--ck-color-media-embed-placeholder-url-text:#757575;--ck-color-media-embed-placeholder-url-text-hover:var(--ck-color-base-text)}.ck-media__wrapper{margin:0 auto}.ck-media__wrapper .ck-media__placeholder{padding:calc(3*var(--ck-spacing-standard));background:var(--ck-color-base-foreground)}.ck-media__wrapper .ck-media__placeholder .ck-media__placeholder__icon{min-width:var(--ck-media-embed-placeholder-icon-size);height:var(--ck-media-embed-placeholder-icon-size);margin-bottom:var(--ck-spacing-large);background-position:50%;background-size:cover}.ck-media__wrapper .ck-media__placeholder .ck-media__placeholder__icon .ck-icon{width:100%;height:100%}.ck-media__wrapper .ck-media__placeholder .ck-media__placeholder__url .ck-media__placeholder__url__text{color:var(--ck-color-media-embed-placeholder-url-text);white-space:nowrap;text-align:center;font-style:italic;text-overflow:ellipsis}.ck-media__wrapper .ck-media__placeholder .ck-media__placeholder__url .ck-media__placeholder__url__text:hover{color:var(--ck-color-media-embed-placeholder-url-text-hover);cursor:pointer;text-decoration:underline}.ck-media__wrapper[data-oembed-url*="open.spotify.com"]{max-width:300px;max-height:380px}.ck-media__wrapper[data-oembed-url*="twitter.com"] .ck.ck-media__placeholder{background:linear-gradient(90deg,#71c6f4,#0d70a5)}.ck-media__wrapper[data-oembed-url*="twitter.com"] .ck.ck-media__placeholder .ck-media__placeholder__icon{background-image:url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0MDAgNDAwIj48cGF0aCBkPSJNNDAwIDIwMGMwIDExMC41LTg5LjUgMjAwLTIwMCAyMDBTMCAzMTAuNSAwIDIwMCA4OS41IDAgMjAwIDBzMjAwIDg5LjUgMjAwIDIwMHpNMTYzLjQgMzA1LjVjODguNyAwIDEzNy4yLTczLjUgMTM3LjItMTM3LjIgMC0yLjEgMC00LjItLjEtNi4yIDkuNC02LjggMTcuNi0xNS4zIDI0LjEtMjUtOC42IDMuOC0xNy45IDYuNC0yNy43IDcuNiAxMC02IDE3LjYtMTUuNCAyMS4yLTI2LjctOS4zIDUuNS0xOS42IDkuNS0zMC42IDExLjctOC44LTkuNC0yMS4zLTE1LjItMzUuMi0xNS4yLTI2LjYgMC00OC4yIDIxLjYtNDguMiA0OC4yIDAgMy44LjQgNy41IDEuMyAxMS00MC4xLTItNzUuNi0yMS4yLTk5LjQtNTAuNC00LjEgNy4xLTYuNSAxNS40LTYuNSAyNC4yIDAgMTYuNyA4LjUgMzEuNSAyMS41IDQwLjEtNy45LS4yLTE1LjMtMi40LTIxLjgtNnYuNmMwIDIzLjQgMTYuNiA0Mi44IDM4LjcgNDcuMy00IDEuMS04LjMgMS43LTEyLjcgMS43LTMuMSAwLTYuMS0uMy05LjEtLjkgNi4xIDE5LjIgMjMuOSAzMy4xIDQ1IDMzLjUtMTYuNSAxMi45LTM3LjMgMjAuNi01OS45IDIwLjYtMy45IDAtNy43LS4yLTExLjUtLjcgMjEuMSAxMy44IDQ2LjUgMjEuOCA3My43IDIxLjgiIGZpbGw9IiNmZmYiLz48L3N2Zz4=)}.ck-media__wrapper[data-oembed-url*="twitter.com"] .ck.ck-media__placeholder .ck-media__placeholder__url__text{color:#b8e6ff}.ck-media__wrapper[data-oembed-url*="twitter.com"] .ck.ck-media__placeholder .ck-media__placeholder__url__text:hover{color:#fff}.ck-media__wrapper[data-oembed-url*="google.com/maps"] .ck-media__placeholder__icon{background-image:url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNTAuMzc4IiBoZWlnaHQ9IjI1NC4xNjciIHZpZXdCb3g9IjAgMCA2Ni4yNDYgNjcuMjQ4Ij48ZyB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTcyLjUzMSAtMjE4LjQ1NSkgc2NhbGUoLjk4MDEyKSI+PHJlY3Qgcnk9IjUuMjM4IiByeD0iNS4yMzgiIHk9IjIzMS4zOTkiIHg9IjE3Ni4wMzEiIGhlaWdodD0iNjAuMDk5IiB3aWR0aD0iNjAuMDk5IiBmaWxsPSIjMzRhNjY4IiBwYWludC1vcmRlcj0ibWFya2VycyBzdHJva2UgZmlsbCIvPjxwYXRoIGQ9Ik0yMDYuNDc3IDI2MC45bC0yOC45ODcgMjguOTg3YTUuMjE4IDUuMjE4IDAgMCAwIDMuNzggMS42MWg0OS42MjFjMS42OTQgMCAzLjE5LS43OTggNC4xNDYtMi4wMzd6IiBmaWxsPSIjNWM4OGM1Ii8+PHBhdGggZD0iTTIyNi43NDIgMjIyLjk4OGMtOS4yNjYgMC0xNi43NzcgNy4xNy0xNi43NzcgMTYuMDE0LjAwNyAyLjc2Mi42NjMgNS40NzQgMi4wOTMgNy44NzUuNDMuNzAzLjgzIDEuNDA4IDEuMTkgMi4xMDcuMzMzLjUwMi42NSAxLjAwNS45NSAxLjUwOC4zNDMuNDc3LjY3My45NTcuOTg4IDEuNDQgMS4zMSAxLjc2OSAyLjUgMy41MDIgMy42MzcgNS4xNjguNzkzIDEuMjc1IDEuNjgzIDIuNjQgMi40NjYgMy45OSAyLjM2MyA0LjA5NCA0LjAwNyA4LjA5MiA0LjYgMTMuOTE0di4wMTJjLjE4Mi40MTIuNTE2LjY2Ni44NzkuNjY3LjQwMy0uMDAxLjc2OC0uMzE0LjkzLS43OTkuNjAzLTUuNzU2IDIuMjM4LTkuNzI5IDQuNTg1LTEzLjc5NC43ODItMS4zNSAxLjY3My0yLjcxNSAyLjQ2NS0zLjk5IDEuMTM3LTEuNjY2IDIuMzI4LTMuNCAzLjYzOC01LjE2OS4zMTUtLjQ4Mi42NDUtLjk2Mi45ODgtMS40MzkuMy0uNTAzLjYxNy0xLjAwNi45NS0xLjUwOC4zNTktLjcuNzYtMS40MDQgMS4xOS0yLjEwNyAxLjQyNi0yLjQwMiAyLTUuMTE0IDIuMDA0LTcuODc1IDAtOC44NDQtNy41MTEtMTYuMDE0LTE2Ljc3Ni0xNi4wMTR6IiBmaWxsPSIjZGQ0YjNlIiBwYWludC1vcmRlcj0ibWFya2VycyBzdHJva2UgZmlsbCIvPjxlbGxpcHNlIHJ5PSI1LjU2NCIgcng9IjUuODI4IiBjeT0iMjM5LjAwMiIgY3g9IjIyNi43NDIiIGZpbGw9IiM4MDJkMjciIHBhaW50LW9yZGVyPSJtYXJrZXJzIHN0cm9rZSBmaWxsIi8+PHBhdGggZD0iTTE5MC4zMDEgMjM3LjI4M2MtNC42NyAwLTguNDU3IDMuODUzLTguNDU3IDguNjA2czMuNzg2IDguNjA3IDguNDU3IDguNjA3YzMuMDQzIDAgNC44MDYtLjk1OCA2LjMzNy0yLjUxNiAxLjUzLTEuNTU3IDIuMDg3LTMuOTEzIDIuMDg3LTYuMjkgMC0uMzYyLS4wMjMtLjcyMi0uMDY0LTEuMDc5aC04LjI1N3YzLjA0M2g0Ljg1Yy0uMTk3Ljc1OS0uNTMxIDEuNDUtMS4wNTggMS45ODYtLjk0Mi45NTgtMi4wMjggMS41NDgtMy45MDEgMS41NDgtMi44NzYgMC01LjIwOC0yLjM3Mi01LjIwOC01LjI5OSAwLTIuOTI2IDIuMzMyLTUuMjk5IDUuMjA4LTUuMjk5IDEuMzk5IDAgMi42MTguNDA3IDMuNTg0IDEuMjkzbDIuMzgxLTIuMzhjMC0uMDAyLS4wMDMtLjAwNC0uMDA0LS4wMDUtMS41ODgtMS41MjQtMy42Mi0yLjIxNS01Ljk1NS0yLjIxNXptNC40MyA1LjY2bC4wMDMuMDA2di0uMDAzeiIgZmlsbD0iI2ZmZiIgcGFpbnQtb3JkZXI9Im1hcmtlcnMgc3Ryb2tlIGZpbGwiLz48cGF0aCBkPSJNMjE1LjE4NCAyNTEuOTI5bC03Ljk4IDcuOTc5IDI4LjQ3NyAyOC40NzVhNS4yMzMgNS4yMzMgMCAwIDAgLjQ0OS0yLjEyM3YtMzEuMTY1Yy0uNDY5LjY3NS0uOTM0IDEuMzQ5LTEuMzgyIDIuMDA1LS43OTIgMS4yNzUtMS42ODIgMi42NC0yLjQ2NSAzLjk5LTIuMzQ3IDQuMDY1LTMuOTgyIDguMDM4LTQuNTg1IDEzLjc5NC0uMTYyLjQ4NS0uNTI3Ljc5OC0uOTMuNzk5LS4zNjMtLjAwMS0uNjk3LS4yNTUtLjg3OS0uNjY3di0uMDEyYy0uNTkzLTUuODIyLTIuMjM3LTkuODItNC42LTEzLjkxNC0uNzgzLTEuMzUtMS42NzMtMi43MTUtMi40NjYtMy45OS0xLjEzNy0xLjY2Ni0yLjMyNy0zLjQtMy42MzctNS4xNjlsLS4wMDItLjAwM3oiIGZpbGw9IiNjM2MzYzMiLz48cGF0aCBkPSJNMjEyLjk4MyAyNDguNDk1bC0zNi45NTIgMzYuOTUzdi44MTJhNS4yMjcgNS4yMjcgMCAwIDAgNS4yMzggNS4yMzhoMS4wMTVsMzUuNjY2LTM1LjY2NmExMzYuMjc1IDEzNi4yNzUgMCAwIDAtMi43NjQtMy45IDM3LjU3NSAzNy41NzUgMCAwIDAtLjk4OS0xLjQ0IDM1LjEyNyAzNS4xMjcgMCAwIDAtLjk1LTEuNTA4Yy0uMDgzLS4xNjItLjE3Ni0uMzI2LS4yNjQtLjQ4OXoiIGZpbGw9IiNmZGRjNGYiIHBhaW50LW9yZGVyPSJtYXJrZXJzIHN0cm9rZSBmaWxsIi8+PHBhdGggZD0iTTIxMS45OTggMjYxLjA4M2wtNi4xNTIgNi4xNTEgMjQuMjY0IDI0LjI2NGguNzgxYTUuMjI3IDUuMjI3IDAgMCAwIDUuMjM5LTUuMjM4di0xLjA0NXoiIGZpbGw9IiNmZmYiIHBhaW50LW9yZGVyPSJtYXJrZXJzIHN0cm9rZSBmaWxsIi8+PC9nPjwvc3ZnPg==)}.ck-media__wrapper[data-oembed-url*="facebook.com"] .ck-media__placeholder{background:#4268b3}.ck-media__wrapper[data-oembed-url*="facebook.com"] .ck-media__placeholder .ck-media__placeholder__icon{background-image:url(data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAyNCIgaGVpZ2h0PSIxMDI0IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxwYXRoIGQ9Ik05NjcuNDg0IDBINTYuNTE3QzI1LjMwNCAwIDAgMjUuMzA0IDAgNTYuNTE3djkxMC45NjZDMCA5OTguNjk0IDI1LjI5NyAxMDI0IDU2LjUyMiAxMDI0SDU0N1Y2MjhINDE0VjQ3M2gxMzNWMzU5LjAyOWMwLTEzMi4yNjIgODAuNzczLTIwNC4yODIgMTk4Ljc1Ni0yMDQuMjgyIDU2LjUxMyAwIDEwNS4wODYgNC4yMDggMTE5LjI0NCA2LjA4OVYyOTlsLTgxLjYxNi4wMzdjLTYzLjk5MyAwLTc2LjM4NCAzMC40OTItNzYuMzg0IDc1LjIzNlY0NzNoMTUzLjQ4N2wtMTkuOTg2IDE1NUg3MDd2Mzk2aDI2MC40ODRjMzEuMjEzIDAgNTYuNTE2LTI1LjMwMyA1Ni41MTYtNTYuNTE2VjU2LjUxNUMxMDI0IDI1LjMwMyA5OTguNjk3IDAgOTY3LjQ4NCAwIiBmaWxsPSIjRkZGRkZFIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiLz48L3N2Zz4=)}.ck-media__wrapper[data-oembed-url*="facebook.com"] .ck-media__placeholder .ck-media__placeholder__url__text{color:#cdf}.ck-media__wrapper[data-oembed-url*="facebook.com"] .ck-media__placeholder .ck-media__placeholder__url__text:hover{color:#fff}.ck-media__wrapper[data-oembed-url*="instagram.com"] .ck-media__placeholder{background:linear-gradient(-135deg,#1400c8,#b900b4,#f50000)}.ck-media__wrapper[data-oembed-url*="instagram.com"] .ck-media__placeholder .ck-media__placeholder__icon{background-image:url(data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTA0IiBoZWlnaHQ9IjUwNCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayI+PGRlZnM+PHBhdGggaWQ9ImEiIGQ9Ik0wIC4xNTloNTAzLjg0MVY1MDMuOTRIMHoiLz48L2RlZnM+PGcgZmlsbD0ibm9uZSIgZmlsbC1ydWxlPSJldmVub2RkIj48bWFzayBpZD0iYiIgZmlsbD0iI2ZmZiI+PHVzZSB4bGluazpocmVmPSIjYSIvPjwvbWFzaz48cGF0aCBkPSJNMjUxLjkyMS4xNTljLTY4LjQxOCAwLTc2Ljk5Ny4yOS0xMDMuODY3IDEuNTE2LTI2LjgxNCAxLjIyMy00NS4xMjcgNS40ODItNjEuMTUxIDExLjcxLTE2LjU2NiA2LjQzNy0zMC42MTUgMTUuMDUxLTQ0LjYyMSAyOS4wNTYtMTQuMDA1IDE0LjAwNi0yMi42MTkgMjguMDU1LTI5LjA1NiA0NC42MjEtNi4yMjggMTYuMDI0LTEwLjQ4NyAzNC4zMzctMTEuNzEgNjEuMTUxQy4yOSAxNzUuMDgzIDAgMTgzLjY2MiAwIDI1Mi4wOGMwIDY4LjQxNy4yOSA3Ni45OTYgMS41MTYgMTAzLjg2NiAxLjIyMyAyNi44MTQgNS40ODIgNDUuMTI3IDExLjcxIDYxLjE1MSA2LjQzNyAxNi41NjYgMTUuMDUxIDMwLjYxNSAyOS4wNTYgNDQuNjIxIDE0LjAwNiAxNC4wMDUgMjguMDU1IDIyLjYxOSA0NC42MjEgMjkuMDU3IDE2LjAyNCA2LjIyNyAzNC4zMzcgMTAuNDg2IDYxLjE1MSAxMS43MDkgMjYuODcgMS4yMjYgMzUuNDQ5IDEuNTE2IDEwMy44NjcgMS41MTYgNjguNDE3IDAgNzYuOTk2LS4yOSAxMDMuODY2LTEuNTE2IDI2LjgxNC0xLjIyMyA0NS4xMjctNS40ODIgNjEuMTUxLTExLjcwOSAxNi41NjYtNi40MzggMzAuNjE1LTE1LjA1MiA0NC42MjEtMjkuMDU3IDE0LjAwNS0xNC4wMDYgMjIuNjE5LTI4LjA1NSAyOS4wNTctNDQuNjIxIDYuMjI3LTE2LjAyNCAxMC40ODYtMzQuMzM3IDExLjcwOS02MS4xNTEgMS4yMjYtMjYuODcgMS41MTYtMzUuNDQ5IDEuNTE2LTEwMy44NjYgMC02OC40MTgtLjI5LTc2Ljk5Ny0xLjUxNi0xMDMuODY3LTEuMjIzLTI2LjgxNC01LjQ4Mi00NS4xMjctMTEuNzA5LTYxLjE1MS02LjQzOC0xNi41NjYtMTUuMDUyLTMwLjYxNS0yOS4wNTctNDQuNjIxLTE0LjAwNi0xNC4wMDUtMjguMDU1LTIyLjYxOS00NC42MjEtMjkuMDU2LTE2LjAyNC02LjIyOC0zNC4zMzctMTAuNDg3LTYxLjE1MS0xMS43MUMzMjguOTE3LjQ0OSAzMjAuMzM4LjE1OSAyNTEuOTIxLjE1OXptMCA0NS4zOTFjNjcuMjY1IDAgNzUuMjMzLjI1NyAxMDEuNzk3IDEuNDY5IDI0LjU2MiAxLjEyIDM3LjkwMSA1LjIyNCA0Ni43NzggOC42NzQgMTEuNzU5IDQuNTcgMjAuMTUxIDEwLjAyOSAyOC45NjYgMTguODQ1IDguODE2IDguODE1IDE0LjI3NSAxNy4yMDcgMTguODQ1IDI4Ljk2NiAzLjQ1IDguODc3IDcuNTU0IDIyLjIxNiA4LjY3NCA0Ni43NzggMS4yMTIgMjYuNTY0IDEuNDY5IDM0LjUzMiAxLjQ2OSAxMDEuNzk4IDAgNjcuMjY1LS4yNTcgNzUuMjMzLTEuNDY5IDEwMS43OTctMS4xMiAyNC41NjItNS4yMjQgMzcuOTAxLTguNjc0IDQ2Ljc3OC00LjU3IDExLjc1OS0xMC4wMjkgMjAuMTUxLTE4Ljg0NSAyOC45NjYtOC44MTUgOC44MTYtMTcuMjA3IDE0LjI3NS0yOC45NjYgMTguODQ1LTguODc3IDMuNDUtMjIuMjE2IDcuNTU0LTQ2Ljc3OCA4LjY3NC0yNi41NiAxLjIxMi0zNC41MjcgMS40NjktMTAxLjc5NyAxLjQ2OS02Ny4yNzEgMC03NS4yMzctLjI1Ny0xMDEuNzk4LTEuNDY5LTI0LjU2Mi0xLjEyLTM3LjkwMS01LjIyNC00Ni43NzgtOC42NzQtMTEuNzU5LTQuNTctMjAuMTUxLTEwLjAyOS0yOC45NjYtMTguODQ1LTguODE1LTguODE1LTE0LjI3NS0xNy4yMDctMTguODQ1LTI4Ljk2Ni0zLjQ1LTguODc3LTcuNTU0LTIyLjIxNi04LjY3NC00Ni43NzgtMS4yMTItMjYuNTY0LTEuNDY5LTM0LjUzMi0xLjQ2OS0xMDEuNzk3IDAtNjcuMjY2LjI1Ny03NS4yMzQgMS40NjktMTAxLjc5OCAxLjEyLTI0LjU2MiA1LjIyNC0zNy45MDEgOC42NzQtNDYuNzc4IDQuNTctMTEuNzU5IDEwLjAyOS0yMC4xNTEgMTguODQ1LTI4Ljk2NiA4LjgxNS04LjgxNiAxNy4yMDctMTQuMjc1IDI4Ljk2Ni0xOC44NDUgOC44NzctMy40NSAyMi4yMTYtNy41NTQgNDYuNzc4LTguNjc0IDI2LjU2NC0xLjIxMiAzNC41MzItMS40NjkgMTAxLjc5OC0xLjQ2OXoiIGZpbGw9IiNGRkYiIG1hc2s9InVybCgjYikiLz48cGF0aCBkPSJNMjUxLjkyMSAzMzYuMDUzYy00Ni4zNzggMC04My45NzQtMzcuNTk2LTgzLjk3NC04My45NzMgMC00Ni4zNzggMzcuNTk2LTgzLjk3NCA4My45NzQtODMuOTc0IDQ2LjM3NyAwIDgzLjk3MyAzNy41OTYgODMuOTczIDgzLjk3NCAwIDQ2LjM3Ny0zNy41OTYgODMuOTczLTgzLjk3MyA4My45NzN6bTAtMjEzLjMzOGMtNzEuNDQ3IDAtMTI5LjM2NSA1Ny45MTgtMTI5LjM2NSAxMjkuMzY1IDAgNzEuNDQ2IDU3LjkxOCAxMjkuMzY0IDEyOS4zNjUgMTI5LjM2NCA3MS40NDYgMCAxMjkuMzY0LTU3LjkxOCAxMjkuMzY0LTEyOS4zNjQgMC03MS40NDctNTcuOTE4LTEyOS4zNjUtMTI5LjM2NC0xMjkuMzY1ek00MTYuNjI3IDExNy42MDRjMCAxNi42OTYtMTMuNTM1IDMwLjIzLTMwLjIzMSAzMC4yMy0xNi42OTUgMC0zMC4yMy0xMy41MzQtMzAuMjMtMzAuMjMgMC0xNi42OTYgMTMuNTM1LTMwLjIzMSAzMC4yMy0zMC4yMzEgMTYuNjk2IDAgMzAuMjMxIDEzLjUzNSAzMC4yMzEgMzAuMjMxIiBmaWxsPSIjRkZGIi8+PC9nPjwvc3ZnPg==)}.ck-media__wrapper[data-oembed-url*="instagram.com"] .ck-media__placeholder .ck-media__placeholder__url__text{color:#ffe0fe}.ck-media__wrapper[data-oembed-url*="instagram.com"] .ck-media__placeholder .ck-media__placeholder__url__text:hover{color:#fff}'; }, function (t, e, n) { var i = n(121); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck.ck-media-form{display:flex;align-items:flex-start;flex-direction:row;flex-wrap:nowrap}.ck.ck-media-form .ck-labeled-input{display:inline-block}.ck.ck-media-form .ck-label{display:none}@media screen and (max-width:600px){.ck.ck-media-form{flex-wrap:wrap}.ck.ck-media-form .ck-labeled-input{flex-basis:100%}.ck.ck-media-form .ck-button{flex-basis:50%}}.ck.ck-media-form{padding:var(--ck-spacing-standard)}.ck.ck-media-form:focus{outline:none}.ck.ck-media-form>:not(:first-child){margin-left:var(--ck-spacing-standard)}@media screen and (max-width:600px){.ck.ck-media-form{padding:0;width:calc(0.8*var(--ck-input-text-width))}.ck.ck-media-form .ck-labeled-input{margin:var(--ck-spacing-standard) var(--ck-spacing-standard) 0}.ck.ck-media-form .ck-labeled-input .ck-input-text{min-width:0;width:100%}.ck.ck-media-form .ck-labeled-input .ck-labeled-input__error{white-space:normal}.ck.ck-media-form .ck-button{padding:var(--ck-spacing-standard);margin-top:var(--ck-spacing-standard);margin-left:0;border-radius:0;border:0;border-top:1px solid var(--ck-color-base-border)}.ck.ck-media-form .ck-button:first-of-type{border-right:1px solid var(--ck-color-base-border)}}"; }, function (t, e, n) { var i = n(123); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck-content .media{clear:both;margin:1em 0;display:block;min-width:15em}"; }, function (t, e, n) { var i = n(125); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ":root{--ck-color-table-focused-cell-background:#f5fafe}.ck-widget.table td.ck-editor__nested-editable.ck-editor__nested-editable_focused,.ck-widget.table th.ck-editor__nested-editable.ck-editor__nested-editable_focused{background:var(--ck-color-table-focused-cell-background);border-style:none;outline:1px solid var(--ck-color-focus-border);outline-offset:-1px}"; }, function (t, e, n) { var i = n(127); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ":root{--ck-insert-table-dropdown-padding:10px;--ck-insert-table-dropdown-box-height:11px;--ck-insert-table-dropdown-box-width:12px;--ck-insert-table-dropdown-box-margin:1px;--ck-insert-table-dropdown-box-border-color:#bfbfbf;--ck-insert-table-dropdown-box-border-active-color:#53a0e4;--ck-insert-table-dropdown-box-active-background:#c7e5ff}.ck .ck-insert-table-dropdown__grid{display:flex;flex-direction:row;flex-wrap:wrap;width:calc(var(--ck-insert-table-dropdown-box-width)*10 + var(--ck-insert-table-dropdown-box-margin)*20 + var(--ck-insert-table-dropdown-padding)*2);padding:var(--ck-insert-table-dropdown-padding) var(--ck-insert-table-dropdown-padding) 0}.ck .ck-insert-table-dropdown__label{text-align:center}.ck .ck-insert-table-dropdown-grid-box{width:var(--ck-insert-table-dropdown-box-width);height:var(--ck-insert-table-dropdown-box-height);margin:var(--ck-insert-table-dropdown-box-margin);border:1px solid var(--ck-insert-table-dropdown-box-border-color);border-radius:1px}.ck .ck-insert-table-dropdown-grid-box.ck-on{border-color:var(--ck-insert-table-dropdown-box-border-active-color);background:var(--ck-insert-table-dropdown-box-active-background)}"; }, function (t, e, n) { var i = n(129); "string" == typeof i && (i = [[t.i, i, ""]]); var o = { singleton: !0, hmr: !0, transform: void 0, insertInto: void 0 }; n(2)(i, o); i.locals && (t.exports = i.locals); }, function (t, e) { t.exports = ".ck-content .table{margin:1em auto;display:table}.ck-content .table table{border-collapse:collapse;border-spacing:0;border:1px double #b3b3b3}.ck-content .table table td,.ck-content .table table th{min-width:2em;padding:.4em;border-color:#d9d9d9}.ck-content .table table th{font-weight:700;background:#fafafa}"; }, function (t, e, n) {
        "use strict";
        n.r(e);
        var i = n(4), o = i.a.Symbol, r = Object.prototype, s = r.hasOwnProperty, a = r.toString, c = o ? o.toStringTag : void 0;
        var l = function (t) { var e = s.call(t, c), n = t[c]; try {
            t[c] = void 0;
            var i = !0;
        }
        catch (t) { } var o = a.call(t); return i && (e ? t[c] = n : delete t[c]), o; }, d = Object.prototype.toString;
        var u = function (t) { return d.call(t); }, h = "[object Null]", f = "[object Undefined]", m = o ? o.toStringTag : void 0;
        var g = function (t) { return null == t ? void 0 === t ? f : h : m && m in Object(t) ? l(t) : u(t); };
        var p = function (t, e) { return function (n) { return t(e(n)); }; }, b = p(Object.getPrototypeOf, Object);
        var w = function (t) { return null != t && "object" == typeof t; }, _ = "[object Object]", k = Function.prototype, v = Object.prototype, y = k.toString, x = v.hasOwnProperty, A = y.call(Object);
        var C = function (t) { if (!w(t) || g(t) != _)
            return !1; var e = b(t); if (null === e)
            return !0; var n = x.call(e, "constructor") && e.constructor; return "function" == typeof n && n instanceof n && y.call(n) == A; };
        var T = function () { this.__data__ = [], this.size = 0; };
        var P = function (t, e) { return t === e || t != t && e != e; };
        var M = function (t, e) { for (var n = t.length; n--;)
            if (P(t[n][0], e))
                return n; return -1; }, E = Array.prototype.splice;
        var S = function (t) { var e = this.__data__, n = M(e, t); return !(n < 0 || (n == e.length - 1 ? e.pop() : E.call(e, n, 1), --this.size, 0)); };
        var I = function (t) { var e = this.__data__, n = M(e, t); return n < 0 ? void 0 : e[n][1]; };
        var N = function (t) { return M(this.__data__, t) > -1; };
        var O = function (t, e) { var n = this.__data__, i = M(n, t); return i < 0 ? (++this.size, n.push([t, e])) : n[i][1] = e, this; };
        function R(t) { var e = -1, n = null == t ? 0 : t.length; for (this.clear(); ++e < n;) {
            var i = t[e];
            this.set(i[0], i[1]);
        } }
        R.prototype.clear = T, R.prototype.delete = S, R.prototype.get = I, R.prototype.has = N, R.prototype.set = O;
        var D = R;
        var L = function () { this.__data__ = new D, this.size = 0; };
        var j = function (t) { var e = this.__data__, n = e.delete(t); return this.size = e.size, n; };
        var V = function (t) { return this.__data__.get(t); };
        var z = function (t) { return this.__data__.has(t); };
        var B = function (t) { var e = typeof t; return null != t && ("object" == e || "function" == e); }, F = "[object AsyncFunction]", U = "[object Function]", H = "[object GeneratorFunction]", q = "[object Proxy]";
        var W = function (t) { if (!B(t))
            return !1; var e = g(t); return e == U || e == H || e == F || e == q; }, Y = i.a["__core-js_shared__"], $ = function () { var t = /[^.]+$/.exec(Y && Y.keys && Y.keys.IE_PROTO || ""); return t ? "Symbol(src)_1." + t : ""; }();
        var G = function (t) { return !!$ && $ in t; }, Q = Function.prototype.toString;
        var K = function (t) { if (null != t) {
            try {
                return Q.call(t);
            }
            catch (t) { }
            try {
                return t + "";
            }
            catch (t) { }
        } return ""; }, J = /^\[object .+?Constructor\]$/, Z = Function.prototype, X = Object.prototype, tt = Z.toString, et = X.hasOwnProperty, nt = RegExp("^" + tt.call(et).replace(/[\\^$.*+?()[\]{}|]/g, "\\$&").replace(/hasOwnProperty|(function).*?(?=\\\()| for .+?(?=\\\])/g, "$1.*?") + "$");
        var it = function (t) { return !(!B(t) || G(t)) && (W(t) ? nt : J).test(K(t)); };
        var ot = function (t, e) { return null == t ? void 0 : t[e]; };
        var rt = function (t, e) { var n = ot(t, e); return it(n) ? n : void 0; }, st = rt(i.a, "Map"), at = rt(Object, "create");
        var ct = function () { this.__data__ = at ? at(null) : {}, this.size = 0; };
        var lt = function (t) { var e = this.has(t) && delete this.__data__[t]; return this.size -= e ? 1 : 0, e; }, dt = "__lodash_hash_undefined__", ut = Object.prototype.hasOwnProperty;
        var ht = function (t) { var e = this.__data__; if (at) {
            var n = e[t];
            return n === dt ? void 0 : n;
        } return ut.call(e, t) ? e[t] : void 0; }, ft = Object.prototype.hasOwnProperty;
        var mt = function (t) { var e = this.__data__; return at ? void 0 !== e[t] : ft.call(e, t); }, gt = "__lodash_hash_undefined__";
        var pt = function (t, e) { var n = this.__data__; return this.size += this.has(t) ? 0 : 1, n[t] = at && void 0 === e ? gt : e, this; };
        function bt(t) { var e = -1, n = null == t ? 0 : t.length; for (this.clear(); ++e < n;) {
            var i = t[e];
            this.set(i[0], i[1]);
        } }
        bt.prototype.clear = ct, bt.prototype.delete = lt, bt.prototype.get = ht, bt.prototype.has = mt, bt.prototype.set = pt;
        var wt = bt;
        var _t = function () { this.size = 0, this.__data__ = { hash: new wt, map: new (st || D), string: new wt }; };
        var kt = function (t) { var e = typeof t; return "string" == e || "number" == e || "symbol" == e || "boolean" == e ? "__proto__" !== t : null === t; };
        var vt = function (t, e) { var n = t.__data__; return kt(e) ? n["string" == typeof e ? "string" : "hash"] : n.map; };
        var yt = function (t) { var e = vt(this, t).delete(t); return this.size -= e ? 1 : 0, e; };
        var xt = function (t) { return vt(this, t).get(t); };
        var At = function (t) { return vt(this, t).has(t); };
        var Ct = function (t, e) { var n = vt(this, t), i = n.size; return n.set(t, e), this.size += n.size == i ? 0 : 1, this; };
        function Tt(t) { var e = -1, n = null == t ? 0 : t.length; for (this.clear(); ++e < n;) {
            var i = t[e];
            this.set(i[0], i[1]);
        } }
        Tt.prototype.clear = _t, Tt.prototype.delete = yt, Tt.prototype.get = xt, Tt.prototype.has = At, Tt.prototype.set = Ct;
        var Pt = Tt, Mt = 200;
        var Et = function (t, e) { var n = this.__data__; if (n instanceof D) {
            var i = n.__data__;
            if (!st || i.length < Mt - 1)
                return i.push([t, e]), this.size = ++n.size, this;
            n = this.__data__ = new Pt(i);
        } return n.set(t, e), this.size = n.size, this; };
        function St(t) { var e = this.__data__ = new D(t); this.size = e.size; }
        St.prototype.clear = L, St.prototype.delete = j, St.prototype.get = V, St.prototype.has = z, St.prototype.set = Et;
        var It = St;
        var Nt = function (t, e) { for (var n = -1, i = null == t ? 0 : t.length; ++n < i && !1 !== e(t[n], n, t);)
            ; return t; }, Ot = function () { try {
            var t = rt(Object, "defineProperty");
            return t({}, "", {}), t;
        }
        catch (t) { } }();
        var Rt = function (t, e, n) { "__proto__" == e && Ot ? Ot(t, e, { configurable: !0, enumerable: !0, value: n, writable: !0 }) : t[e] = n; }, Dt = Object.prototype.hasOwnProperty;
        var Lt = function (t, e, n) { var i = t[e]; Dt.call(t, e) && P(i, n) && (void 0 !== n || e in t) || Rt(t, e, n); };
        var jt = function (t, e, n, i) { var o = !n; n || (n = {}); for (var r = -1, s = e.length; ++r < s;) {
            var a = e[r], c = i ? i(n[a], t[a], a, n, t) : void 0;
            void 0 === c && (c = t[a]), o ? Rt(n, a, c) : Lt(n, a, c);
        } return n; };
        var Vt = function (t, e) { for (var n = -1, i = Array(t); ++n < t;)
            i[n] = e(n); return i; }, zt = "[object Arguments]";
        var Bt = function (t) { return w(t) && g(t) == zt; }, Ft = Object.prototype, Ut = Ft.hasOwnProperty, Ht = Ft.propertyIsEnumerable, qt = Bt(function () { return arguments; }()) ? Bt : function (t) { return w(t) && Ut.call(t, "callee") && !Ht.call(t, "callee"); }, Wt = Array.isArray, Yt = n(6), $t = 9007199254740991, Gt = /^(?:0|[1-9]\d*)$/;
        var Qt = function (t, e) { var n = typeof t; return !!(e = null == e ? $t : e) && ("number" == n || "symbol" != n && Gt.test(t)) && t > -1 && t % 1 == 0 && t < e; }, Kt = 9007199254740991;
        var Jt = function (t) { return "number" == typeof t && t > -1 && t % 1 == 0 && t <= Kt; }, Zt = {};
        Zt["[object Float32Array]"] = Zt["[object Float64Array]"] = Zt["[object Int8Array]"] = Zt["[object Int16Array]"] = Zt["[object Int32Array]"] = Zt["[object Uint8Array]"] = Zt["[object Uint8ClampedArray]"] = Zt["[object Uint16Array]"] = Zt["[object Uint32Array]"] = !0, Zt["[object Arguments]"] = Zt["[object Array]"] = Zt["[object ArrayBuffer]"] = Zt["[object Boolean]"] = Zt["[object DataView]"] = Zt["[object Date]"] = Zt["[object Error]"] = Zt["[object Function]"] = Zt["[object Map]"] = Zt["[object Number]"] = Zt["[object Object]"] = Zt["[object RegExp]"] = Zt["[object Set]"] = Zt["[object String]"] = Zt["[object WeakMap]"] = !1;
        var Xt = function (t) { return w(t) && Jt(t.length) && !!Zt[g(t)]; };
        var te = function (t) { return function (e) { return t(e); }; }, ee = n(5), ne = ee.a && ee.a.isTypedArray, ie = ne ? te(ne) : Xt, oe = Object.prototype.hasOwnProperty;
        var re = function (t, e) { var n = Wt(t), i = !n && qt(t), o = !n && !i && Object(Yt.a)(t), r = !n && !i && !o && ie(t), s = n || i || o || r, a = s ? Vt(t.length, String) : [], c = a.length; for (var l in t)
            !e && !oe.call(t, l) || s && ("length" == l || o && ("offset" == l || "parent" == l) || r && ("buffer" == l || "byteLength" == l || "byteOffset" == l) || Qt(l, c)) || a.push(l); return a; }, se = Object.prototype;
        var ae = function (t) { var e = t && t.constructor; return t === ("function" == typeof e && e.prototype || se); }, ce = p(Object.keys, Object), le = Object.prototype.hasOwnProperty;
        var de = function (t) { if (!ae(t))
            return ce(t); var e = []; for (var n in Object(t))
            le.call(t, n) && "constructor" != n && e.push(n); return e; };
        var ue = function (t) { return null != t && Jt(t.length) && !W(t); };
        var he = function (t) { return ue(t) ? re(t) : de(t); };
        var fe = function (t, e) { return t && jt(e, he(e), t); };
        var me = function (t) { var e = []; if (null != t)
            for (var n in Object(t))
                e.push(n); return e; }, ge = Object.prototype.hasOwnProperty;
        var pe = function (t) { if (!B(t))
            return me(t); var e = ae(t), n = []; for (var i in t)
            ("constructor" != i || !e && ge.call(t, i)) && n.push(i); return n; };
        var be = function (t) { return ue(t) ? re(t, !0) : pe(t); };
        var we = function (t, e) { return t && jt(e, be(e), t); }, _e = n(21);
        var ke = function (t, e) { var n = -1, i = t.length; for (e || (e = Array(i)); ++n < i;)
            e[n] = t[n]; return e; };
        var ve = function (t, e) { for (var n = -1, i = null == t ? 0 : t.length, o = 0, r = []; ++n < i;) {
            var s = t[n];
            e(s, n, t) && (r[o++] = s);
        } return r; };
        var ye = function () { return []; }, xe = Object.prototype.propertyIsEnumerable, Ae = Object.getOwnPropertySymbols, Ce = Ae ? function (t) { return null == t ? [] : (t = Object(t), ve(Ae(t), function (e) { return xe.call(t, e); })); } : ye;
        var Te = function (t, e) { return jt(t, Ce(t), e); };
        var Pe = function (t, e) { for (var n = -1, i = e.length, o = t.length; ++n < i;)
            t[o + n] = e[n]; return t; }, Me = Object.getOwnPropertySymbols ? function (t) { for (var e = []; t;)
            Pe(e, Ce(t)), t = b(t); return e; } : ye;
        var Ee = function (t, e) { return jt(t, Me(t), e); };
        var Se = function (t, e, n) { var i = e(t); return Wt(t) ? i : Pe(i, n(t)); };
        var Ie = function (t) { return Se(t, he, Ce); };
        var Ne = function (t) { return Se(t, be, Me); }, Oe = rt(i.a, "DataView"), Re = rt(i.a, "Promise"), De = rt(i.a, "Set"), Le = rt(i.a, "WeakMap"), je = K(Oe), Ve = K(st), ze = K(Re), Be = K(De), Fe = K(Le), Ue = g;
        (Oe && "[object DataView]" != Ue(new Oe(new ArrayBuffer(1))) || st && "[object Map]" != Ue(new st) || Re && "[object Promise]" != Ue(Re.resolve()) || De && "[object Set]" != Ue(new De) || Le && "[object WeakMap]" != Ue(new Le)) && (Ue = function (t) { var e = g(t), n = "[object Object]" == e ? t.constructor : void 0, i = n ? K(n) : ""; if (i)
            switch (i) {
                case je: return "[object DataView]";
                case Ve: return "[object Map]";
                case ze: return "[object Promise]";
                case Be: return "[object Set]";
                case Fe: return "[object WeakMap]";
            } return e; });
        var He = Ue, qe = Object.prototype.hasOwnProperty;
        var We = function (t) { var e = t.length, n = new t.constructor(e); return e && "string" == typeof t[0] && qe.call(t, "index") && (n.index = t.index, n.input = t.input), n; }, Ye = i.a.Uint8Array;
        var $e = function (t) { var e = new t.constructor(t.byteLength); return new Ye(e).set(new Ye(t)), e; };
        var Ge = function (t, e) { var n = e ? $e(t.buffer) : t.buffer; return new t.constructor(n, t.byteOffset, t.byteLength); }, Qe = /\w*$/;
        var Ke = function (t) { var e = new t.constructor(t.source, Qe.exec(t)); return e.lastIndex = t.lastIndex, e; }, Je = o ? o.prototype : void 0, Ze = Je ? Je.valueOf : void 0;
        var Xe = function (t) { return Ze ? Object(Ze.call(t)) : {}; };
        var tn = function (t, e) { var n = e ? $e(t.buffer) : t.buffer; return new t.constructor(n, t.byteOffset, t.length); }, en = "[object Boolean]", nn = "[object Date]", on = "[object Map]", rn = "[object Number]", sn = "[object RegExp]", an = "[object Set]", cn = "[object String]", ln = "[object Symbol]", dn = "[object ArrayBuffer]", un = "[object DataView]", hn = "[object Float32Array]", fn = "[object Float64Array]", mn = "[object Int8Array]", gn = "[object Int16Array]", pn = "[object Int32Array]", bn = "[object Uint8Array]", wn = "[object Uint8ClampedArray]", _n = "[object Uint16Array]", kn = "[object Uint32Array]";
        var vn = function (t, e, n) { var i = t.constructor; switch (e) {
            case dn: return $e(t);
            case en:
            case nn: return new i(+t);
            case un: return Ge(t, n);
            case hn:
            case fn:
            case mn:
            case gn:
            case pn:
            case bn:
            case wn:
            case _n:
            case kn: return tn(t, n);
            case on: return new i;
            case rn:
            case cn: return new i(t);
            case sn: return Ke(t);
            case an: return new i;
            case ln: return Xe(t);
        } }, yn = Object.create, xn = function () { function t() { } return function (e) { if (!B(e))
            return {}; if (yn)
            return yn(e); t.prototype = e; var n = new t; return t.prototype = void 0, n; }; }();
        var An = function (t) { return "function" != typeof t.constructor || ae(t) ? {} : xn(b(t)); }, Cn = "[object Map]";
        var Tn = function (t) { return w(t) && He(t) == Cn; }, Pn = ee.a && ee.a.isMap, Mn = Pn ? te(Pn) : Tn, En = "[object Set]";
        var Sn = function (t) { return w(t) && He(t) == En; }, In = ee.a && ee.a.isSet, Nn = In ? te(In) : Sn, On = 1, Rn = 2, Dn = 4, Ln = "[object Arguments]", jn = "[object Function]", Vn = "[object GeneratorFunction]", zn = "[object Object]", Bn = {};
        Bn[Ln] = Bn["[object Array]"] = Bn["[object ArrayBuffer]"] = Bn["[object DataView]"] = Bn["[object Boolean]"] = Bn["[object Date]"] = Bn["[object Float32Array]"] = Bn["[object Float64Array]"] = Bn["[object Int8Array]"] = Bn["[object Int16Array]"] = Bn["[object Int32Array]"] = Bn["[object Map]"] = Bn["[object Number]"] = Bn[zn] = Bn["[object RegExp]"] = Bn["[object Set]"] = Bn["[object String]"] = Bn["[object Symbol]"] = Bn["[object Uint8Array]"] = Bn["[object Uint8ClampedArray]"] = Bn["[object Uint16Array]"] = Bn["[object Uint32Array]"] = !0, Bn["[object Error]"] = Bn[jn] = Bn["[object WeakMap]"] = !1;
        var Fn = function t(e, n, i, o, r, s) { var a, c = n & On, l = n & Rn, d = n & Dn; if (i && (a = r ? i(e, o, r, s) : i(e)), void 0 !== a)
            return a; if (!B(e))
            return e; var u = Wt(e); if (u) {
            if (a = We(e), !c)
                return ke(e, a);
        }
        else {
            var h = He(e), f = h == jn || h == Vn;
            if (Object(Yt.a)(e))
                return Object(_e.a)(e, c);
            if (h == zn || h == Ln || f && !r) {
                if (a = l || f ? {} : An(e), !c)
                    return l ? Ee(e, we(a, e)) : Te(e, fe(a, e));
            }
            else {
                if (!Bn[h])
                    return r ? e : {};
                a = vn(e, h, c);
            }
        } s || (s = new It); var m = s.get(e); if (m)
            return m; if (s.set(e, a), Nn(e))
            return e.forEach(function (o) { a.add(t(o, n, i, o, e, s)); }), a; if (Mn(e))
            return e.forEach(function (o, r) { a.set(r, t(o, n, i, r, e, s)); }), a; var g = d ? l ? Ne : Ie : l ? keysIn : he, p = u ? void 0 : g(e); return Nt(p || e, function (o, r) { p && (o = e[r = o]), Lt(a, r, t(o, n, i, r, e, s)); }), a; }, Un = 1, Hn = 4;
        var qn = function (t, e) { return Fn(t, Un | Hn, e = "function" == typeof e ? e : void 0); };
        var Wn = function (t) { return w(t) && 1 === t.nodeType && !C(t); };
        var Yn = /** @class */ (function () {
            function Yn(t, e) {
                this._config = {}, e && this.define(e), t && this._setObjectToTarget(this._config, t);
            }
            Yn.prototype.set = function (t, e) { this._setToTarget(this._config, t, e); };
            Yn.prototype.define = function (t, e) { this._setToTarget(this._config, t, e, !0); };
            Yn.prototype.get = function (t) { return this._getFromSource(this._config, t); };
            Yn.prototype._setToTarget = function (t, e, n, i) {
                if (i === void 0) { i = !1; }
                if (C(e))
                    return void this._setObjectToTarget(t, e, i);
                var o = e.split(".");
                e = o.pop();
                for (var _j = 0, o_1 = o; _j < o_1.length; _j++) {
                    var e_1 = o_1[_j];
                    C(t[e_1]) || (t[e_1] = {}), t = t[e_1];
                }
                if (C(n))
                    return C(t[e]) || (t[e] = {}), t = t[e], void this._setObjectToTarget(t, n, i);
                i && void 0 !== t[e] || (t[e] = n);
            };
            Yn.prototype._getFromSource = function (t, e) { var n = e.split("."); e = n.pop(); for (var _j = 0, n_1 = n; _j < n_1.length; _j++) {
                var e_2 = n_1[_j];
                if (!C(t[e_2])) {
                    t = null;
                    break;
                }
                t = t[e_2];
            } return t ? function (t) { return qn(t, $n); }(t[e]) : void 0; };
            Yn.prototype._setObjectToTarget = function (t, e, n) {
                var _this = this;
                Object.keys(e).forEach(function (i) { _this._setToTarget(t, i, e[i], n); });
            };
            return Yn;
        }());
        function $n(t) { return Wn(t) ? t : void 0; }
        var Gn = n(0);
        var Qn = function () { return function t() { t.called = !0; }; };
        var Kn = /** @class */ (function () {
            function Kn(t, e) {
                this.source = t, this.name = e, this.path = [], this.stop = Qn(), this.off = Qn();
            }
            return Kn;
        }());
        function Jn() { var t = "e"; for (var e_3 = 0; e_3 < 8; e_3++)
            t += Math.floor(65536 * (1 + Math.random())).toString(16).substring(1); return t; }
        var Zn = { get: function (t) { return "number" != typeof t ? this[t] || this.normal : t; }, highest: 1e5, high: 1e3, normal: 0, low: -1e3, lowest: -1e5 };
        var Xn = Symbol("listeningTo"), ti = Symbol("emitterId");
        var ei = { on: function (t, e, n) {
                if (n === void 0) { n = {}; }
                this.listenTo(this, t, e, n);
            }, once: function (t, e, n) { var i = !1; this.listenTo(this, t, function (t) {
                var n = [];
                for (var _j = 1; _j < arguments.length; _j++) {
                    n[_j - 1] = arguments[_j];
                }
                i || (i = !0, t.off(), e.call.apply(e, [this, t].concat(n)));
            }, n); }, off: function (t, e) { this.stopListening(this, t, e); }, listenTo: function (t, e, n, i) {
                if (i === void 0) { i = {}; }
                var o, r;
                this[Xn] || (this[Xn] = {});
                var s = this[Xn];
                ii(t) || ni(t);
                var a = ii(t);
                (o = s[a]) || (o = s[a] = { emitter: t, callbacks: {} }), (r = o.callbacks[e]) || (r = o.callbacks[e] = []), r.push(n), function (t, e) { var n = oi(t); if (n[e])
                    return; var i = e, o = null; var r = []; for (; "" !== i && !n[i];)
                    n[i] = { callbacks: [], childEvents: [] }, r.push(n[i]), o && n[i].childEvents.push(o), o = i, i = i.substr(0, i.lastIndexOf(":")); if ("" !== i) {
                    for (var _j = 0, r_1 = r; _j < r_1.length; _j++) {
                        var t_1 = r_1[_j];
                        t_1.callbacks = n[i].callbacks.slice();
                    }
                    n[i].childEvents.push(o);
                } }(t, e);
                var c = ri(t, e), l = Zn.get(i.priority), d = { callback: n, priority: l };
                for (var _j = 0, c_1 = c; _j < c_1.length; _j++) {
                    var t_2 = c_1[_j];
                    var e_4 = !1;
                    for (var n_2 = 0; n_2 < t_2.length; n_2++)
                        if (t_2[n_2].priority < l) {
                            t_2.splice(n_2, 0, d), e_4 = !0;
                            break;
                        }
                    e_4 || t_2.push(d);
                }
            }, stopListening: function (t, e, n) { var i = this[Xn]; var o = t && ii(t); var r = i && o && i[o], s = r && e && r.callbacks[e]; if (!(!i || t && !r || e && !s))
                if (n)
                    ai(t, e, n);
                else if (s) {
                    for (; n = s.pop();)
                        ai(t, e, n);
                    delete r.callbacks[e];
                }
                else if (r) {
                    for (e in r.callbacks)
                        this.stopListening(t, e);
                    delete i[o];
                }
                else {
                    for (o in i)
                        this.stopListening(i[o].emitter);
                    delete this[Xn];
                } }, fire: function (t) {
                var e = [];
                for (var _j = 1; _j < arguments.length; _j++) {
                    e[_j - 1] = arguments[_j];
                }
                var n = t instanceof Kn ? t : new Kn(this, t), i = n.name;
                var o = function t(e, n) { var i; if (!e._events || !(i = e._events[n]) || !i.callbacks.length)
                    return n.indexOf(":") > -1 ? t(e, n.substr(0, n.lastIndexOf(":"))) : null; return i.callbacks; }(this, i);
                if (n.path.push(this), o) {
                    var t_3 = [n].concat(e);
                    o = Array.from(o);
                    for (var e_5 = 0; e_5 < o.length && (o[e_5].callback.apply(this, t_3), n.off.called && (delete n.off.called, ai(this, i, o[e_5].callback)), !n.stop.called); e_5++)
                        ;
                }
                if (this._delegations) {
                    var t_4 = this._delegations.get(i), o_2 = this._delegations.get("*");
                    t_4 && si(t_4, n, e), o_2 && si(o_2, n, e);
                }
                return n.return;
            }, delegate: function () {
                var _this = this;
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                return { to: function (e, n) { _this._delegations || (_this._delegations = new Map), t.forEach(function (t) { var i = _this._delegations.get(t); i ? i.set(e, n) : _this._delegations.set(t, new Map([[e, n]])); }); } };
            }, stopDelegating: function (t, e) { if (this._delegations)
                if (t)
                    if (e) {
                        var n_3 = this._delegations.get(t);
                        n_3 && n_3.delete(e);
                    }
                    else
                        this._delegations.delete(t);
                else
                    this._delegations.clear(); } };
        function ni(t, e) { t[ti] || (t[ti] = e || Jn()); }
        function ii(t) { return t[ti]; }
        function oi(t) { return t._events || Object.defineProperty(t, "_events", { value: {} }), t._events; }
        function ri(t, e) { var n = oi(t)[e]; if (!n)
            return []; var i = [n.callbacks]; for (var e_6 = 0; e_6 < n.childEvents.length; e_6++) {
            var o_3 = ri(t, n.childEvents[e_6]);
            i = i.concat(o_3);
        } return i; }
        function si(t, e, n) { for (var _j = 0, t_5 = t; _j < t_5.length; _j++) {
            var _k = t_5[_j], i_1 = _k[0], o_4 = _k[1];
            o_4 ? "function" == typeof o_4 && (o_4 = o_4(e.name)) : o_4 = e.name;
            var t_6 = new Kn(e.source, o_4);
            t_6.path = e.path.slice(), i_1.fire.apply(i_1, [t_6].concat(n));
        } }
        function ai(t, e, n) { var i = ri(t, e); for (var _j = 0, i_2 = i; _j < i_2.length; _j++) {
            var t_7 = i_2[_j];
            for (var e_7 = 0; e_7 < t_7.length; e_7++)
                t_7[e_7].callback == n && (t_7.splice(e_7, 1), e_7--);
        } }
        function ci(t) {
            var e = [];
            for (var _j = 1; _j < arguments.length; _j++) {
                e[_j - 1] = arguments[_j];
            }
            e.forEach(function (e) { Object.getOwnPropertyNames(e).concat(Object.getOwnPropertySymbols(e)).forEach(function (n) { if (n in t.prototype)
                return; var i = Object.getOwnPropertyDescriptor(e, n); i.enumerable = !1, Object.defineProperty(t.prototype, n, i); }); });
        }
        function li(t, e) { var n = Math.min(t.length, e.length); for (var i_3 = 0; i_3 < n; i_3++)
            if (t[i_3] != e[i_3])
                return i_3; return t.length == e.length ? "same" : t.length < e.length ? "prefix" : "extension"; }
        var di = 4;
        var ui = function (t) { return Fn(t, di); };
        var hi = /** @class */ (function () {
            function hi() {
                this.parent = null;
            }
            Object.defineProperty(hi.prototype, "index", {
                get: function () { var t; if (!this.parent)
                    return null; if (-1 == (t = this.parent.getChildIndex(this)))
                    throw new Gn.b("view-node-not-found-in-parent: The node's parent does not contain this node."); return t; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(hi.prototype, "nextSibling", {
                get: function () { var t = this.index; return null !== t && this.parent.getChild(t + 1) || null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(hi.prototype, "previousSibling", {
                get: function () { var t = this.index; return null !== t && this.parent.getChild(t - 1) || null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(hi.prototype, "root", {
                get: function () { var t = this; for (; t.parent;)
                    t = t.parent; return t; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(hi.prototype, "document", {
                get: function () { return this.parent instanceof hi ? this.parent.document : null; },
                enumerable: true,
                configurable: true
            });
            hi.prototype.getPath = function () { var t = []; var e = this; for (; e.parent;)
                t.unshift(e.index), e = e.parent; return t; };
            hi.prototype.getAncestors = function (t) {
                if (t === void 0) { t = { includeSelf: !1, parentFirst: !1 }; }
                var e = [];
                var n = t.includeSelf ? this : this.parent;
                for (; n;)
                    e[t.parentFirst ? "push" : "unshift"](n), n = n.parent;
                return e;
            };
            hi.prototype.getCommonAncestor = function (t, e) {
                if (e === void 0) { e = {}; }
                var n = this.getAncestors(e), i = t.getAncestors(e);
                var o = 0;
                for (; n[o] == i[o] && n[o];)
                    o++;
                return 0 === o ? null : n[o - 1];
            };
            hi.prototype.isBefore = function (t) { if (this == t)
                return !1; if (this.root !== t.root)
                return !1; var e = this.getPath(), n = t.getPath(), i = li(e, n); switch (i) {
                case "prefix": return !0;
                case "extension": return !1;
                default: return e[i] < n[i];
            } };
            hi.prototype.isAfter = function (t) { return this != t && (this.root === t.root && !this.isBefore(t)); };
            hi.prototype._remove = function () { this.parent._removeChildren(this.index); };
            hi.prototype._fireChange = function (t, e) { this.fire("change:" + t, e), this.parent && this.parent._fireChange(t, e); };
            hi.prototype.toJSON = function () { var t = ui(this); return delete t.parent, t; };
            hi.prototype.is = function (t) { return "node" == t; };
            return hi;
        }());
        ci(hi, ei);
        var fi = /** @class */ (function (_super) {
            __extends(fi, _super);
            function fi(t) {
                var _this = this;
                _this = _super.call(this) || this, _this._textData = t;
                return _this;
            }
            fi.prototype.is = function (t) { return "text" == t || _super.prototype.is.call(this, t); };
            Object.defineProperty(fi.prototype, "data", {
                get: function () { return this._textData; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(fi.prototype, "_data", {
                get: function () { return this.data; },
                set: function (t) { this._fireChange("text", this), this._textData = t; },
                enumerable: true,
                configurable: true
            });
            fi.prototype.isSimilar = function (t) { return t instanceof fi && (this === t || this.data === t.data); };
            fi.prototype._clone = function () { return new fi(this.data); };
            return fi;
        }(hi));
        var mi = /** @class */ (function () {
            function mi(t, e, n) {
                if (this.textNode = t, e < 0 || e > t.data.length)
                    throw new Gn.b("view-textproxy-wrong-offsetintext: Given offsetInText value is incorrect.");
                if (n < 0 || e + n > t.data.length)
                    throw new Gn.b("view-textproxy-wrong-length: Given length value is incorrect.");
                this.data = t.data.substring(e, e + n), this.offsetInText = e;
            }
            Object.defineProperty(mi.prototype, "offsetSize", {
                get: function () { return this.data.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(mi.prototype, "isPartial", {
                get: function () { return this.data.length !== this.textNode.data.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(mi.prototype, "parent", {
                get: function () { return this.textNode.parent; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(mi.prototype, "root", {
                get: function () { return this.textNode.root; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(mi.prototype, "document", {
                get: function () { return this.textNode.document; },
                enumerable: true,
                configurable: true
            });
            mi.prototype.is = function (t) { return "textProxy" == t; };
            mi.prototype.getAncestors = function (t) {
                if (t === void 0) { t = { includeSelf: !1, parentFirst: !1 }; }
                var e = [];
                var n = t.includeSelf ? this.textNode : this.parent;
                for (; null !== n;)
                    e[t.parentFirst ? "push" : "unshift"](n), n = n.parent;
                return e;
            };
            return mi;
        }());
        function gi(t) { var e = new Map; for (var n_4 in t)
            e.set(n_4, t[n_4]); return e; }
        function pi(t) { return !(!t || !t[Symbol.iterator]); }
        var bi = /** @class */ (function () {
            function bi() {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                this._patterns = [], this.add.apply(this, t);
            }
            bi.prototype.add = function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                for (var _k = 0, t_8 = t; _k < t_8.length; _k++) {
                    var e_8 = t_8[_k];
                    ("string" == typeof e_8 || e_8 instanceof RegExp) && (e_8 = { name: e_8 }), e_8.classes && ("string" == typeof e_8.classes || e_8.classes instanceof RegExp) && (e_8.classes = [e_8.classes]), this._patterns.push(e_8);
                }
            };
            bi.prototype.match = function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                for (var _k = 0, t_9 = t; _k < t_9.length; _k++) {
                    var e_9 = t_9[_k];
                    for (var _q = 0, _v = this._patterns; _q < _v.length; _q++) {
                        var t_10 = _v[_q];
                        var n_5 = wi(e_9, t_10);
                        if (n_5)
                            return { element: e_9, pattern: t_10, match: n_5 };
                    }
                }
                return null;
            };
            bi.prototype.matchAll = function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                var e = [];
                for (var _k = 0, t_11 = t; _k < t_11.length; _k++) {
                    var n_6 = t_11[_k];
                    for (var _q = 0, _v = this._patterns; _q < _v.length; _q++) {
                        var t_12 = _v[_q];
                        var i_4 = wi(n_6, t_12);
                        i_4 && e.push({ element: n_6, pattern: t_12, match: i_4 });
                    }
                }
                return e.length > 0 ? e : null;
            };
            bi.prototype.getElementName = function () { if (1 !== this._patterns.length)
                return null; var t = this._patterns[0], e = t.name; return "function" == typeof t || !e || e instanceof RegExp ? null : e; };
            return bi;
        }());
        function wi(t, e) { if ("function" == typeof e)
            return e(t); var n = {}; return e.name && (n.name = function (t, e) { if (t instanceof RegExp)
            return t.test(e); return t === e; }(e.name, t.name), !n.name) ? null : e.attributes && (n.attributes = function (t, e) { var n = []; for (var i_5 in t) {
            var o_5 = t[i_5];
            if (!e.hasAttribute(i_5))
                return null;
            {
                var t_13 = e.getAttribute(i_5);
                if (!0 === o_5)
                    n.push(i_5);
                else if (o_5 instanceof RegExp) {
                    if (!o_5.test(t_13))
                        return null;
                    n.push(i_5);
                }
                else {
                    if (t_13 !== o_5)
                        return null;
                    n.push(i_5);
                }
            }
        } return n; }(e.attributes, t), !n.attributes) ? null : !(e.classes && (n.classes = function (t, e) { var n = []; for (var _j = 0, t_14 = t; _j < t_14.length; _j++) {
            var i_6 = t_14[_j];
            if (i_6 instanceof RegExp) {
                var t_16 = e.getClassNames();
                for (var _k = 0, t_15 = t_16; _k < t_15.length; _k++) {
                    var e_10 = t_15[_k];
                    i_6.test(e_10) && n.push(e_10);
                }
                if (0 === n.length)
                    return null;
            }
            else {
                if (!e.hasClass(i_6))
                    return null;
                n.push(i_6);
            }
        } return n; }(e.classes, t), !n.classes)) && (!(e.styles && (n.styles = function (t, e) { var n = []; for (var i_7 in t) {
            var o_6 = t[i_7];
            if (!e.hasStyle(i_7))
                return null;
            {
                var t_17 = e.getStyle(i_7);
                if (o_6 instanceof RegExp) {
                    if (!o_6.test(t_17))
                        return null;
                    n.push(i_7);
                }
                else {
                    if (t_17 !== o_6)
                        return null;
                    n.push(i_7);
                }
            }
        } return n; }(e.styles, t), !n.styles)) && n); }
        var _i = /** @class */ (function (_super) {
            __extends(_i, _super);
            function _i(t, e, n) {
                var _this = this;
                if (_this = _super.call(this) || this, _this.name = t, _this._attrs = function (t) { t = C(t) ? gi(t) : new Map(t); for (var _j = 0, t_18 = t; _j < t_18.length; _j++) {
                    var _k = t_18[_j], e_11 = _k[0], n_7 = _k[1];
                    null === n_7 ? t.delete(e_11) : "string" != typeof n_7 && t.set(e_11, String(n_7));
                } return t; }(e), _this._children = [], n && _this._insertChild(0, n), _this._classes = new Set, _this._attrs.has("class")) {
                    var t_19 = _this._attrs.get("class");
                    vi(_this._classes, t_19), _this._attrs.delete("class");
                }
                _this._styles = new Map, _this._attrs.has("style") && (ki(_this._styles, _this._attrs.get("style")), _this._attrs.delete("style")), _this._customProperties = new Map;
                return _this;
            }
            Object.defineProperty(_i.prototype, "childCount", {
                get: function () { return this._children.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(_i.prototype, "isEmpty", {
                get: function () { return 0 === this._children.length; },
                enumerable: true,
                configurable: true
            });
            _i.prototype.is = function (t, e) {
                if (e === void 0) { e = null; }
                return e ? "element" == t && e == this.name : "element" == t || t == this.name || _super.prototype.is.call(this, t);
            };
            _i.prototype.getChild = function (t) { return this._children[t]; };
            _i.prototype.getChildIndex = function (t) { return this._children.indexOf(t); };
            _i.prototype.getChildren = function () { return this._children[Symbol.iterator](); };
            _i.prototype.getAttributeKeys = function () { var _j, _k; return __generator(this, function (_q) {
                switch (_q.label) {
                    case 0:
                        _j = this._classes.size > 0;
                        if (!_j) return [3 /*break*/, 2];
                        return [4 /*yield*/, "class"];
                    case 1:
                        _j = (_q.sent());
                        _q.label = 2;
                    case 2:
                        _j;
                        _k = this._styles.size > 0;
                        if (!_k) return [3 /*break*/, 4];
                        return [4 /*yield*/, "style"];
                    case 3:
                        _k = (_q.sent());
                        _q.label = 4;
                    case 4:
                        _k;
                        return [5 /*yield**/, __values(this._attrs.keys())];
                    case 5:
                        _q.sent();
                        return [2 /*return*/];
                }
            }); };
            _i.prototype.getAttributes = function () { var _j, _k; return __generator(this, function (_q) {
                switch (_q.label) {
                    case 0: return [5 /*yield**/, __values(this._attrs.entries())];
                    case 1:
                        _q.sent();
                        _j = this._classes.size > 0;
                        if (!_j) return [3 /*break*/, 3];
                        return [4 /*yield*/, ["class", this.getAttribute("class")]];
                    case 2:
                        _j = (_q.sent());
                        _q.label = 3;
                    case 3:
                        _j;
                        _k = this._styles.size > 0;
                        if (!_k) return [3 /*break*/, 5];
                        return [4 /*yield*/, ["style", this.getAttribute("style")]];
                    case 4:
                        _k = (_q.sent());
                        _q.label = 5;
                    case 5:
                        _k;
                        return [2 /*return*/];
                }
            }); };
            _i.prototype.getAttribute = function (t) { if ("class" == t)
                return this._classes.size > 0 ? this._classes.slice().join(" ") : void 0; if ("style" != t)
                return this._attrs.get(t); if (this._styles.size > 0) {
                var t_20 = "";
                for (var _j = 0, _k = this._styles; _j < _k.length; _j++) {
                    var _q = _k[_j], e_12 = _q[0], n_8 = _q[1];
                    t_20 += e_12 + ":" + n_8 + ";";
                }
                return t_20;
            } };
            _i.prototype.hasAttribute = function (t) { return "class" == t ? this._classes.size > 0 : "style" == t ? this._styles.size > 0 : this._attrs.has(t); };
            _i.prototype.isSimilar = function (t) { if (!(t instanceof _i))
                return !1; if (this === t)
                return !0; if (this.name != t.name)
                return !1; if (this._attrs.size !== t._attrs.size || this._classes.size !== t._classes.size || this._styles.size !== t._styles.size)
                return !1; for (var _j = 0, _k = this._attrs; _j < _k.length; _j++) {
                var _q = _k[_j], e_13 = _q[0], n_9 = _q[1];
                if (!t._attrs.has(e_13) || t._attrs.get(e_13) !== n_9)
                    return !1;
            } for (var _v = 0, _w = this._classes; _v < _w.length; _v++) {
                var e_14 = _w[_v];
                if (!t._classes.has(e_14))
                    return !1;
            } for (var _x = 0, _y = this._styles; _x < _y.length; _x++) {
                var _z = _y[_x], e_15 = _z[0], n_10 = _z[1];
                if (!t._styles.has(e_15) || t._styles.get(e_15) !== n_10)
                    return !1;
            } return !0; };
            _i.prototype.hasClass = function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                for (var _k = 0, t_21 = t; _k < t_21.length; _k++) {
                    var e_16 = t_21[_k];
                    if (!this._classes.has(e_16))
                        return !1;
                }
                return !0;
            };
            _i.prototype.getClassNames = function () { return this._classes.keys(); };
            _i.prototype.getStyle = function (t) { return this._styles.get(t); };
            _i.prototype.getStyleNames = function () { return this._styles.keys(); };
            _i.prototype.hasStyle = function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                for (var _k = 0, t_22 = t; _k < t_22.length; _k++) {
                    var e_17 = t_22[_k];
                    if (!this._styles.has(e_17))
                        return !1;
                }
                return !0;
            };
            _i.prototype.findAncestor = function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                var e = new (bi.bind.apply(bi, [void 0].concat(t)))();
                var n = this.parent;
                for (; n;) {
                    if (e.match(n))
                        return n;
                    n = n.parent;
                }
                return null;
            };
            _i.prototype.getCustomProperty = function (t) { return this._customProperties.get(t); };
            _i.prototype.getCustomProperties = function () { return __generator(this, function (_j) {
                switch (_j.label) {
                    case 0: return [5 /*yield**/, __values(this._customProperties.entries())];
                    case 1:
                        _j.sent();
                        return [2 /*return*/];
                }
            }); };
            _i.prototype.getIdentity = function () { var t = Array.from(this._classes).sort().join(","), e = Array.from(this._styles).map(function (t) { return t[0] + ":" + t[1]; }).sort().join(";"), n = Array.from(this._attrs).map(function (t) { return t[0] + "=\"" + t[1] + "\""; }).sort().join(" "); return this.name + ("" == t ? "" : " class=\"" + t + "\"") + ("" == e ? "" : " style=\"" + e + "\"") + ("" == n ? "" : " " + n); };
            _i.prototype._clone = function (t) {
                if (t === void 0) { t = !1; }
                var e = [];
                if (t)
                    for (var _j = 0, _k = this.getChildren(); _j < _k.length; _j++) {
                        var n_11 = _k[_j];
                        e.push(n_11._clone(t));
                    }
                var n = new this.constructor(this.name, this._attrs, e);
                return n._classes = new Set(this._classes), n._styles = new Map(this._styles), n._customProperties = new Map(this._customProperties), n.getFillerOffset = this.getFillerOffset, n;
            };
            _i.prototype._appendChild = function (t) { return this._insertChild(this.childCount, t); };
            _i.prototype._insertChild = function (t, e) { this._fireChange("children", this); var n = 0; var i = function (t) { if ("string" == typeof t)
                return [new fi(t)]; pi(t) || (t = [t]); return Array.from(t).map(function (t) { return "string" == typeof t ? new fi(t) : t instanceof mi ? new fi(t.data) : t; }); }(e); for (var _j = 0, i_8 = i; _j < i_8.length; _j++) {
                var e_18 = i_8[_j];
                null !== e_18.parent && e_18._remove(), e_18.parent = this, this._children.splice(t, 0, e_18), t++, n++;
            } return n; };
            _i.prototype._removeChildren = function (t, e) {
                if (e === void 0) { e = 1; }
                this._fireChange("children", this);
                for (var n_12 = t; n_12 < t + e; n_12++)
                    this._children[n_12].parent = null;
                return this._children.splice(t, e);
            };
            _i.prototype._setAttribute = function (t, e) { e = String(e), this._fireChange("attributes", this), "class" == t ? vi(this._classes, e) : "style" == t ? ki(this._styles, e) : this._attrs.set(t, e); };
            _i.prototype._removeAttribute = function (t) { return this._fireChange("attributes", this), "class" == t ? this._classes.size > 0 && (this._classes.clear(), !0) : "style" == t ? this._styles.size > 0 && (this._styles.clear(), !0) : this._attrs.delete(t); };
            _i.prototype._addClass = function (t) {
                var _this = this;
                this._fireChange("attributes", this), (t = Array.isArray(t) ? t : [t]).forEach(function (t) { return _this._classes.add(t); });
            };
            _i.prototype._removeClass = function (t) {
                var _this = this;
                this._fireChange("attributes", this), (t = Array.isArray(t) ? t : [t]).forEach(function (t) { return _this._classes.delete(t); });
            };
            _i.prototype._setStyle = function (t, e) { if (this._fireChange("attributes", this), C(t)) {
                var e_20 = Object.keys(t);
                for (var _j = 0, e_19 = e_20; _j < e_19.length; _j++) {
                    var n_13 = e_19[_j];
                    this._styles.set(n_13, t[n_13]);
                }
            }
            else
                this._styles.set(t, e); };
            _i.prototype._removeStyle = function (t) {
                var _this = this;
                this._fireChange("attributes", this), (t = Array.isArray(t) ? t : [t]).forEach(function (t) { return _this._styles.delete(t); });
            };
            _i.prototype._setCustomProperty = function (t, e) { this._customProperties.set(t, e); };
            _i.prototype._removeCustomProperty = function (t) { return this._customProperties.delete(t); };
            return _i;
        }(hi));
        function ki(t, e) { var n = null, i = 0, o = 0, r = null; if (t.clear(), "" !== e) {
            ";" != e.charAt(e.length - 1) && (e += ";");
            for (var s_1 = 0; s_1 < e.length; s_1++) {
                var a_1 = e.charAt(s_1);
                if (null === n)
                    switch (a_1) {
                        case ":":
                            r || (r = e.substr(i, s_1 - i), o = s_1 + 1);
                            break;
                        case '"':
                        case "'":
                            n = a_1;
                            break;
                        case ";": {
                            var n_14 = e.substr(o, s_1 - o);
                            r && t.set(r.trim(), n_14.trim()), r = null, i = s_1 + 1;
                            break;
                        }
                    }
                else
                    a_1 === n && (n = null);
            }
        } }
        function vi(t, e) { var n = e.split(/\s+/); t.clear(), n.forEach(function (e) { return t.add(e); }); }
        var yi = /** @class */ (function (_super) {
            __extends(yi, _super);
            function yi(t, e, n) {
                var _this = this;
                _this = _super.call(this, t, e, n) || this, _this.getFillerOffset = xi;
                return _this;
            }
            yi.prototype.is = function (t, e) {
                if (e === void 0) { e = null; }
                return e ? "containerElement" == t && e == this.name || _super.prototype.is.call(this, t, e) : "containerElement" == t || _super.prototype.is.call(this, t);
            };
            return yi;
        }(_i));
        function xi() { var t = this.getChildren().slice(), e = t[this.childCount - 1]; if (e && e.is("element", "br"))
            return this.childCount; for (var _j = 0, t_23 = t; _j < t_23.length; _j++) {
            var e_21 = t_23[_j];
            if (!e_21.is("uiElement"))
                return null;
        } return this.childCount; }
        var Ai = function (t) { return t; };
        var Ci = function (t, e, n) { switch (n.length) {
            case 0: return t.call(e);
            case 1: return t.call(e, n[0]);
            case 2: return t.call(e, n[0], n[1]);
            case 3: return t.call(e, n[0], n[1], n[2]);
        } return t.apply(e, n); }, Ti = Math.max;
        var Pi = function (t, e, n) { return e = Ti(void 0 === e ? t.length - 1 : e, 0), function () { for (var i = arguments, o = -1, r = Ti(i.length - e, 0), s = Array(r); ++o < r;)
            s[o] = i[e + o]; o = -1; for (var a = Array(e + 1); ++o < e;)
            a[o] = i[o]; return a[e] = n(s), Ci(t, this, a); }; };
        var Mi = function (t) { return function () { return t; }; }, Ei = Ot ? function (t, e) { return Ot(t, "toString", { configurable: !0, enumerable: !1, value: Mi(e), writable: !0 }); } : Ai, Si = 800, Ii = 16, Ni = Date.now;
        var Oi = function (t) { var e = 0, n = 0; return function () { var i = Ni(), o = Ii - (i - n); if (n = i, o > 0) {
            if (++e >= Si)
                return arguments[0];
        }
        else
            e = 0; return t.apply(void 0, arguments); }; }(Ei);
        var Ri = function (t, e) { return Oi(Pi(t, e, Ai), t + ""); };
        var Di = function (t, e, n) { if (!B(n))
            return !1; var i = typeof e; return !!("number" == i ? ue(n) && Qt(e, n.length) : "string" == i && e in n) && P(n[e], t); };
        var Li = function (t) { return Ri(function (e, n) { var i = -1, o = n.length, r = o > 1 ? n[o - 1] : void 0, s = o > 2 ? n[2] : void 0; for (r = t.length > 3 && "function" == typeof r ? (o--, r) : void 0, s && Di(n[0], n[1], s) && (r = o < 3 ? void 0 : r, o = 1), e = Object(e); ++i < o;) {
            var a = n[i];
            a && t(e, a, i, r);
        } return e; }); }(function (t, e) { jt(e, be(e), t); });
        var ji = Symbol("observableProperties"), Vi = Symbol("boundObservables"), zi = Symbol("boundProperties"), Bi = { set: function (t, e) {
                var _this = this;
                if (B(t))
                    return void Object.keys(t).forEach(function (e) { _this.set(e, t[e]); }, this);
                Ui(this);
                var n = this[ji];
                if (t in this && !n.has(t))
                    throw new Gn.b("observable-set-cannot-override: Cannot override an existing property.");
                Object.defineProperty(this, t, { enumerable: !0, configurable: !0, get: function () { return n.get(t); }, set: function (e) { var i = n.get(t); var o = this.fire("set:" + t, t, e, i); void 0 === o && (o = e), i === o && n.has(t) || (n.set(t, o), this.fire("change:" + t, t, o, i)); } }), this[t] = e;
            }, bind: function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                if (!t.length || !Wi(t))
                    throw new Gn.b("observable-bind-wrong-properties: All properties must be strings.");
                if (new Set(t).size !== t.length)
                    throw new Gn.b("observable-bind-duplicate-properties: Properties must be unique.");
                Ui(this);
                var e = this[zi];
                t.forEach(function (t) { if (e.has(t))
                    throw new Gn.b("observable-bind-rebind: Cannot bind the same property more that once."); });
                var n = new Map;
                return t.forEach(function (t) { var i = { property: t, to: [] }; e.set(t, i), n.set(t, i); }), { to: Hi, toMany: qi, _observable: this, _bindProperties: t, _to: [], _bindings: n };
            }, unbind: function () {
                var _this = this;
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                if (!(ji in this))
                    return;
                var e = this[zi], n = this[Vi];
                if (t.length) {
                    if (!Wi(t))
                        throw new Gn.b("observable-unbind-wrong-properties: Properties must be strings.");
                    t.forEach(function (t) { var i = e.get(t); if (!i)
                        return; var o, r, s, a; i.to.forEach(function (t) { o = t[0], r = t[1], s = n.get(o), (a = s[r]).delete(i), a.size || delete s[r], Object.keys(s).length || (n.delete(o), _this.stopListening(o, "change")); }), e.delete(t); });
                }
                else
                    n.forEach(function (t, e) { _this.stopListening(e, "change"); }), n.clear(), e.clear();
            }, decorate: function (t) {
                var _this = this;
                var e = this[t];
                if (!e)
                    throw new Gn.b("observablemixin-cannot-decorate-undefined: Cannot decorate an undefined method.", { object: this, methodName: t });
                this.on(t, function (t, n) { t.return = e.apply(_this, n); }), this[t] = function () {
                    var e = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        e[_j] = arguments[_j];
                    }
                    return this.fire(t, e);
                };
            } };
        Li(Bi, ei);
        var Fi = Bi;
        function Ui(t) { ji in t || (Object.defineProperty(t, ji, { value: new Map }), Object.defineProperty(t, Vi, { value: new Map }), Object.defineProperty(t, zi, { value: new Map })); }
        function Hi() {
            var _this = this;
            var t = [];
            for (var _j = 0; _j < arguments.length; _j++) {
                t[_j] = arguments[_j];
            }
            var e = function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                if (!t.length)
                    throw new Gn.b("observable-bind-to-parse-error: Invalid argument syntax in `to()`.");
                var e = { to: [] };
                var n;
                "function" == typeof t[t.length - 1] && (e.callback = t.pop());
                return t.forEach(function (t) { if ("string" == typeof t)
                    n.properties.push(t);
                else {
                    if ("object" != typeof t)
                        throw new Gn.b("observable-bind-to-parse-error: Invalid argument syntax in `to()`.");
                    n = { observable: t, properties: [] }, e.to.push(n);
                } }), e;
            }.apply(void 0, t), n = Array.from(this._bindings.keys()), i = n.length;
            if (!e.callback && e.to.length > 1)
                throw new Gn.b("observable-bind-to-no-callback: Binding multiple observables only possible with callback.");
            if (i > 1 && e.callback)
                throw new Gn.b("observable-bind-to-extra-callback: Cannot bind multiple properties and use a callback in one binding.");
            e.to.forEach(function (t) { if (t.properties.length && t.properties.length !== i)
                throw new Gn.b("observable-bind-to-properties-length: The number of properties must match."); t.properties.length || (t.properties = _this._bindProperties); }), this._to = e.to, e.callback && (this._bindings.get(n[0]).callback = e.callback), function (t, e) { e.forEach(function (e) { var n = t[Vi]; var i; n.get(e.observable) || t.listenTo(e.observable, "change", function (o, r) { (i = n.get(e.observable)[r]) && i.forEach(function (e) { Yi(t, e.property); }); }); }); }(this._observable, this._to), function (t) { var e; t._bindings.forEach(function (n, i) { t._to.forEach(function (o) { e = o.properties[n.callback ? 0 : t._bindProperties.indexOf(i)], n.to.push([o.observable, e]), function (t, e, n, i) { var o = t[Vi], r = o.get(n), s = r || {}; s[i] || (s[i] = new Set); s[i].add(e), r || o.set(n, s); }(t._observable, n, o.observable, e); }); }); }(this), this._bindProperties.forEach(function (t) { Yi(_this._observable, t); });
        }
        function qi(t, e, n) { if (this._bindings.size > 1)
            throw new Gn.b("observable-bind-to-many-not-one-binding: Cannot bind multiple properties with toMany()."); this.to.apply(this, function (t, e) { var n = t.map(function (t) { return [t, e]; }); return Array.prototype.concat.apply([], n); }(t, e).concat([n])); }
        function Wi(t) { return t.every(function (t) { return "string" == typeof t; }); }
        function Yi(t, e) { var n = t[zi].get(e); var i; i = n.callback ? n.callback.apply(t, n.to.map(function (t) { return t[0][t[1]]; })) : (i = n.to[0])[0][i[1]], t.hasOwnProperty(e) ? t[e] = i : t.set(e, i); }
        var $i = Symbol("document");
        var Gi = /** @class */ (function (_super) {
            __extends(Gi, _super);
            function Gi(t, e, n) {
                var _this = this;
                _this = _super.call(this, t, e, n) || this, _this.set("isReadOnly", !1), _this.set("isFocused", !1);
                return _this;
            }
            Gi.prototype.is = function (t, e) {
                if (e === void 0) { e = null; }
                return e ? "editableElement" == t && e == this.name || _super.prototype.is.call(this, t, e) : "editableElement" == t || _super.prototype.is.call(this, t);
            };
            Gi.prototype.destroy = function () { this.stopListening(); };
            Object.defineProperty(Gi.prototype, "document", {
                get: function () { return this.getCustomProperty($i); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Gi.prototype, "_document", {
                set: function (t) {
                    var _this = this;
                    if (this.getCustomProperty($i))
                        throw new Gn.b("view-editableelement-document-already-set: View document is already set.");
                    this._setCustomProperty($i, t), this.bind("isReadOnly").to(t), this.bind("isFocused").to(t, "isFocused", function (e) { return e && t.selection.editableElement == _this; }), this.listenTo(t.selection, "change", function () { _this.isFocused = t.isFocused && t.selection.editableElement == _this; });
                },
                enumerable: true,
                configurable: true
            });
            return Gi;
        }(yi));
        ci(Gi, Fi);
        var Qi = Symbol("rootName");
        var Ki = /** @class */ (function (_super) {
            __extends(Ki, _super);
            function Ki(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.rootName = "main";
                return _this;
            }
            Ki.prototype.is = function (t, e) {
                if (e === void 0) { e = null; }
                return e ? "rootElement" == t && e == this.name || _super.prototype.is.call(this, t, e) : "rootElement" == t || _super.prototype.is.call(this, t);
            };
            Object.defineProperty(Ki.prototype, "rootName", {
                get: function () { return this.getCustomProperty(Qi); },
                set: function (t) { this._setCustomProperty(Qi, t); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Ki.prototype, "_name", {
                set: function (t) { this.name = t; },
                enumerable: true,
                configurable: true
            });
            return Ki;
        }(Gi));
        var Ji = /** @class */ (function () {
            function Ji(t) {
                if (t === void 0) { t = {}; }
                if (!t.boundaries && !t.startPosition)
                    throw new Gn.b("view-tree-walker-no-start-position: Neither boundaries nor starting position have been defined.");
                if (t.direction && "forward" != t.direction && "backward" != t.direction)
                    throw new Gn.b("view-tree-walker-unknown-direction: Only `backward` and `forward` direction allowed.", { direction: t.direction });
                this.boundaries = t.boundaries || null, t.startPosition ? this.position = Zi._createAt(t.startPosition) : this.position = Zi._createAt(t.boundaries["backward" == t.direction ? "end" : "start"]), this.direction = t.direction || "forward", this.singleCharacters = !!t.singleCharacters, this.shallow = !!t.shallow, this.ignoreElementEnd = !!t.ignoreElementEnd, this._boundaryStartParent = this.boundaries ? this.boundaries.start.parent : null, this._boundaryEndParent = this.boundaries ? this.boundaries.end.parent : null;
            }
            Ji.prototype[Symbol.iterator] = function () { return this; };
            Ji.prototype.skip = function (t) {
                var _j;
                var e, n, i;
                do {
                    i = this.position, (_j = this.next(), e = _j.done, n = _j.value, _j);
                } while (!e && t(n));
                e || (this.position = i);
            };
            Ji.prototype.next = function () { return "forward" == this.direction ? this._next() : this._previous(); };
            Ji.prototype._next = function () { var t = this.position.clone(); var e = this.position, n = t.parent; if (null === n.parent && t.offset === n.childCount)
                return { done: !0 }; if (n === this._boundaryEndParent && t.offset == this.boundaries.end.offset)
                return { done: !0 }; var i; if (n instanceof fi) {
                if (t.isAtEnd)
                    return this.position = Zi._createAfter(n), this._next();
                i = n.data[t.offset];
            }
            else
                i = n.getChild(t.offset); if (i instanceof _i)
                return this.shallow ? t.offset++ : t = new Zi(i, 0), this.position = t, this._formatReturnValue("elementStart", i, e, t, 1); if (i instanceof fi) {
                if (this.singleCharacters)
                    return t = new Zi(i, 0), this.position = t, this._next();
                {
                    var n_15, o_7 = i.data.length;
                    return i == this._boundaryEndParent ? (o_7 = this.boundaries.end.offset, n_15 = new mi(i, 0, o_7), t = Zi._createAfter(n_15)) : (n_15 = new mi(i, 0, i.data.length), t.offset++), this.position = t, this._formatReturnValue("text", n_15, e, t, o_7);
                }
            } if ("string" == typeof i) {
                var i_9;
                if (this.singleCharacters)
                    i_9 = 1;
                else {
                    i_9 = (n === this._boundaryEndParent ? this.boundaries.end.offset : n.data.length) - t.offset;
                }
                var o_8 = new mi(n, t.offset, i_9);
                return t.offset += i_9, this.position = t, this._formatReturnValue("text", o_8, e, t, i_9);
            } return t = Zi._createAfter(n), this.position = t, this.ignoreElementEnd ? this._next() : this._formatReturnValue("elementEnd", n, e, t); };
            Ji.prototype._previous = function () { var t = this.position.clone(); var e = this.position, n = t.parent; if (null === n.parent && 0 === t.offset)
                return { done: !0 }; if (n == this._boundaryStartParent && t.offset == this.boundaries.start.offset)
                return { done: !0 }; var i; if (n instanceof fi) {
                if (t.isAtStart)
                    return this.position = Zi._createBefore(n), this._previous();
                i = n.data[t.offset - 1];
            }
            else
                i = n.getChild(t.offset - 1); if (i instanceof _i)
                return this.shallow ? (t.offset--, this.position = t, this._formatReturnValue("elementStart", i, e, t, 1)) : (t = new Zi(i, i.childCount), this.position = t, this.ignoreElementEnd ? this._previous() : this._formatReturnValue("elementEnd", i, e, t)); if (i instanceof fi) {
                if (this.singleCharacters)
                    return t = new Zi(i, i.data.length), this.position = t, this._previous();
                {
                    var n_16, o_9 = i.data.length;
                    if (i == this._boundaryStartParent) {
                        var e_22 = this.boundaries.start.offset;
                        o_9 = (n_16 = new mi(i, e_22, i.data.length - e_22)).data.length, t = Zi._createBefore(n_16);
                    }
                    else
                        n_16 = new mi(i, 0, i.data.length), t.offset--;
                    return this.position = t, this._formatReturnValue("text", n_16, e, t, o_9);
                }
            } if ("string" == typeof i) {
                var i_10;
                if (this.singleCharacters)
                    i_10 = 1;
                else {
                    var e_23 = n === this._boundaryStartParent ? this.boundaries.start.offset : 0;
                    i_10 = t.offset - e_23;
                }
                t.offset -= i_10;
                var o_10 = new mi(n, t.offset, i_10);
                return this.position = t, this._formatReturnValue("text", o_10, e, t, i_10);
            } return t = Zi._createBefore(n), this.position = t, this._formatReturnValue("elementStart", n, e, t, 1); };
            Ji.prototype._formatReturnValue = function (t, e, n, i, o) { return e instanceof mi && (e.offsetInText + e.data.length == e.textNode.data.length && ("forward" != this.direction || this.boundaries && this.boundaries.end.isEqual(this.position) ? n = Zi._createAfter(e.textNode) : (i = Zi._createAfter(e.textNode), this.position = i)), 0 === e.offsetInText && ("backward" != this.direction || this.boundaries && this.boundaries.start.isEqual(this.position) ? n = Zi._createBefore(e.textNode) : (i = Zi._createBefore(e.textNode), this.position = i))), { done: !1, value: { type: t, item: e, previousPosition: n, nextPosition: i, length: o } }; };
            return Ji;
        }());
        var Zi = /** @class */ (function () {
            function Zi(t, e) {
                this.parent = t, this.offset = e;
            }
            Object.defineProperty(Zi.prototype, "nodeAfter", {
                get: function () { return this.parent.is("text") ? null : this.parent.getChild(this.offset) || null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Zi.prototype, "nodeBefore", {
                get: function () { return this.parent.is("text") ? null : this.parent.getChild(this.offset - 1) || null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Zi.prototype, "isAtStart", {
                get: function () { return 0 === this.offset; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Zi.prototype, "isAtEnd", {
                get: function () { var t = this.parent.is("text") ? this.parent.data.length : this.parent.childCount; return this.offset === t; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Zi.prototype, "root", {
                get: function () { return this.parent.root; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Zi.prototype, "editableElement", {
                get: function () { var t = this.parent; for (; !(t instanceof Gi);) {
                    if (!t.parent)
                        return null;
                    t = t.parent;
                } return t; },
                enumerable: true,
                configurable: true
            });
            Zi.prototype.getShiftedBy = function (t) { var e = Zi._createAt(this), n = e.offset + t; return e.offset = n < 0 ? 0 : n, e; };
            Zi.prototype.getLastMatchingPosition = function (t, e) {
                if (e === void 0) { e = {}; }
                e.startPosition = this;
                var n = new Ji(e);
                return n.skip(t), n.position;
            };
            Zi.prototype.getAncestors = function () { return this.parent.is("documentFragment") ? [this.parent] : this.parent.getAncestors({ includeSelf: !0 }); };
            Zi.prototype.getCommonAncestor = function (t) { var e = this.getAncestors(), n = t.getAncestors(); var i = 0; for (; e[i] == n[i] && e[i];)
                i++; return 0 === i ? null : e[i - 1]; };
            Zi.prototype.isEqual = function (t) { return this.parent == t.parent && this.offset == t.offset; };
            Zi.prototype.isBefore = function (t) { return "before" == this.compareWith(t); };
            Zi.prototype.isAfter = function (t) { return "after" == this.compareWith(t); };
            Zi.prototype.compareWith = function (t) { if (this.root !== t.root)
                return "different"; if (this.isEqual(t))
                return "same"; var e = this.parent.is("node") ? this.parent.getPath() : [], n = t.parent.is("node") ? t.parent.getPath() : []; e.push(this.offset), n.push(t.offset); var i = li(e, n); switch (i) {
                case "prefix": return "before";
                case "extension": return "after";
                default: return e[i] < n[i] ? "before" : "after";
            } };
            Zi.prototype.getWalker = function (t) {
                if (t === void 0) { t = {}; }
                return t.startPosition = this, new Ji(t);
            };
            Zi.prototype.clone = function () { return new Zi(this.parent, this.offset); };
            Zi._createAt = function (t, e) { if (t instanceof Zi)
                return new this(t.parent, t.offset); {
                var n_17 = t;
                if ("end" == e)
                    e = n_17.is("text") ? n_17.data.length : n_17.childCount;
                else {
                    if ("before" == e)
                        return this._createBefore(n_17);
                    if ("after" == e)
                        return this._createAfter(n_17);
                    if (0 !== e && !e)
                        throw new Gn.b("view-createPositionAt-offset-required: View#createPositionAt() requires the offset when the first parameter is a view item.");
                }
                return new Zi(n_17, e);
            } };
            Zi._createAfter = function (t) { if (t.is("textProxy"))
                return new Zi(t.textNode, t.offsetInText + t.data.length); if (!t.parent)
                throw new Gn.b("view-position-after-root: You can not make position after root.", { root: t }); return new Zi(t.parent, t.index + 1); };
            Zi._createBefore = function (t) { if (t.is("textProxy"))
                return new Zi(t.textNode, t.offsetInText); if (!t.parent)
                throw new Gn.b("view-position-before-root: You can not make position before root.", { root: t }); return new Zi(t.parent, t.index); };
            return Zi;
        }());
        var Xi = /** @class */ (function () {
            function Xi(t, e) {
                if (e === void 0) { e = null; }
                this.start = t.clone(), this.end = e ? e.clone() : t.clone();
            }
            Xi.prototype[Symbol.iterator] = function () { return __generator(this, function (_j) {
                switch (_j.label) {
                    case 0: return [5 /*yield**/, __values(new Ji({ boundaries: this, ignoreElementEnd: !0 }))];
                    case 1:
                        _j.sent();
                        return [2 /*return*/];
                }
            }); };
            Object.defineProperty(Xi.prototype, "isCollapsed", {
                get: function () { return this.start.isEqual(this.end); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Xi.prototype, "isFlat", {
                get: function () { return this.start.parent === this.end.parent; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Xi.prototype, "root", {
                get: function () { return this.start.root; },
                enumerable: true,
                configurable: true
            });
            Xi.prototype.getEnlarged = function () { var t = this.start.getLastMatchingPosition(to, { direction: "backward" }), e = this.end.getLastMatchingPosition(to); return t.parent.is("text") && t.isAtStart && (t = Zi._createBefore(t.parent)), e.parent.is("text") && e.isAtEnd && (e = Zi._createAfter(e.parent)), new Xi(t, e); };
            Xi.prototype.getTrimmed = function () { var t = this.start.getLastMatchingPosition(to); if (t.isAfter(this.end) || t.isEqual(this.end))
                return new Xi(t, t); var e = this.end.getLastMatchingPosition(to, { direction: "backward" }); var n = t.nodeAfter, i = e.nodeBefore; return n && n.is("text") && (t = new Zi(n, 0)), i && i.is("text") && (e = new Zi(i, i.data.length)), new Xi(t, e); };
            Xi.prototype.isEqual = function (t) { return this == t || this.start.isEqual(t.start) && this.end.isEqual(t.end); };
            Xi.prototype.containsPosition = function (t) { return t.isAfter(this.start) && t.isBefore(this.end); };
            Xi.prototype.containsRange = function (t, e) {
                if (e === void 0) { e = !1; }
                t.isCollapsed && (e = !1);
                var n = this.containsPosition(t.start) || e && this.start.isEqual(t.start), i = this.containsPosition(t.end) || e && this.end.isEqual(t.end);
                return n && i;
            };
            Xi.prototype.getDifference = function (t) { var e = []; return this.isIntersecting(t) ? (this.containsPosition(t.start) && e.push(new Xi(this.start, t.start)), this.containsPosition(t.end) && e.push(new Xi(t.end, this.end))) : e.push(this.clone()), e; };
            Xi.prototype.getIntersection = function (t) { if (this.isIntersecting(t)) {
                var e_24 = this.start, n_18 = this.end;
                return this.containsPosition(t.start) && (e_24 = t.start), this.containsPosition(t.end) && (n_18 = t.end), new Xi(e_24, n_18);
            } return null; };
            Xi.prototype.getWalker = function (t) {
                if (t === void 0) { t = {}; }
                return t.boundaries = this, new Ji(t);
            };
            Xi.prototype.getCommonAncestor = function () { return this.start.getCommonAncestor(this.end); };
            Xi.prototype.clone = function () { return new Xi(this.start, this.end); };
            Xi.prototype.getItems = function (t) {
                var e, _j, e_25, t_24;
                if (t === void 0) { t = {}; }
                return __generator(this, function (_k) {
                    switch (_k.label) {
                        case 0:
                            t.boundaries = this, t.ignoreElementEnd = !0;
                            e = new Ji(t);
                            _j = 0, e_25 = e;
                            _k.label = 1;
                        case 1:
                            if (!(_j < e_25.length)) return [3 /*break*/, 4];
                            t_24 = e_25[_j];
                            return [4 /*yield*/, t_24.item];
                        case 2:
                            _k.sent();
                            _k.label = 3;
                        case 3:
                            _j++;
                            return [3 /*break*/, 1];
                        case 4: return [2 /*return*/];
                    }
                });
            };
            Xi.prototype.getPositions = function (t) {
                var e, _j, e_26, t_25;
                if (t === void 0) { t = {}; }
                return __generator(this, function (_k) {
                    switch (_k.label) {
                        case 0:
                            t.boundaries = this;
                            e = new Ji(t);
                            return [4 /*yield*/, e.position];
                        case 1:
                            _k.sent();
                            _j = 0, e_26 = e;
                            _k.label = 2;
                        case 2:
                            if (!(_j < e_26.length)) return [3 /*break*/, 5];
                            t_25 = e_26[_j];
                            return [4 /*yield*/, t_25.nextPosition];
                        case 3:
                            _k.sent();
                            _k.label = 4;
                        case 4:
                            _j++;
                            return [3 /*break*/, 2];
                        case 5: return [2 /*return*/];
                    }
                });
            };
            Xi.prototype.isIntersecting = function (t) { return this.start.isBefore(t.end) && this.end.isAfter(t.start); };
            Xi._createFromParentsAndOffsets = function (t, e, n, i) { return new this(new Zi(t, e), new Zi(n, i)); };
            Xi._createFromPositionAndShift = function (t, e) { var n = t, i = t.getShiftedBy(e); return e > 0 ? new this(n, i) : new this(i, n); };
            Xi._createIn = function (t) { return this._createFromParentsAndOffsets(t, 0, t, t.childCount); };
            Xi._createOn = function (t) { var e = t.is("textProxy") ? t.offsetSize : 1; return this._createFromPositionAndShift(Zi._createBefore(t), e); };
            return Xi;
        }());
        function to(t) { return !(!t.item.is("attributeElement") && !t.item.is("uiElement")); }
        function eo(t) { var e = 0; for (var _j = 0, t_26 = t; _j < t_26.length; _j++) {
            var n_19 = t_26[_j];
            e++;
        } return e; }
        var no = /** @class */ (function () {
            function no(t, e, n) {
                if (t === void 0) { t = null; }
                this._ranges = [], this._lastRangeBackward = !1, this._isFake = !1, this._fakeSelectionLabel = "", this.setTo(t, e, n);
            }
            Object.defineProperty(no.prototype, "isFake", {
                get: function () { return this._isFake; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(no.prototype, "fakeSelectionLabel", {
                get: function () { return this._fakeSelectionLabel; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(no.prototype, "anchor", {
                get: function () { if (!this._ranges.length)
                    return null; var t = this._ranges[this._ranges.length - 1]; return (this._lastRangeBackward ? t.end : t.start).clone(); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(no.prototype, "focus", {
                get: function () { if (!this._ranges.length)
                    return null; var t = this._ranges[this._ranges.length - 1]; return (this._lastRangeBackward ? t.start : t.end).clone(); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(no.prototype, "isCollapsed", {
                get: function () { return 1 === this.rangeCount && this._ranges[0].isCollapsed; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(no.prototype, "rangeCount", {
                get: function () { return this._ranges.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(no.prototype, "isBackward", {
                get: function () { return !this.isCollapsed && this._lastRangeBackward; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(no.prototype, "editableElement", {
                get: function () { return this.anchor ? this.anchor.editableElement : null; },
                enumerable: true,
                configurable: true
            });
            no.prototype.getRanges = function () { var _j, _k, t_27; return __generator(this, function (_q) {
                switch (_q.label) {
                    case 0:
                        _j = 0, _k = this._ranges;
                        _q.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 4];
                        t_27 = _k[_j];
                        return [4 /*yield*/, t_27.clone()];
                    case 2:
                        _q.sent();
                        _q.label = 3;
                    case 3:
                        _j++;
                        return [3 /*break*/, 1];
                    case 4: return [2 /*return*/];
                }
            }); };
            no.prototype.getFirstRange = function () { var t = null; for (var _j = 0, _k = this._ranges; _j < _k.length; _j++) {
                var e_27 = _k[_j];
                t && !e_27.start.isBefore(t.start) || (t = e_27);
            } return t ? t.clone() : null; };
            no.prototype.getLastRange = function () { var t = null; for (var _j = 0, _k = this._ranges; _j < _k.length; _j++) {
                var e_28 = _k[_j];
                t && !e_28.end.isAfter(t.end) || (t = e_28);
            } return t ? t.clone() : null; };
            no.prototype.getFirstPosition = function () { var t = this.getFirstRange(); return t ? t.start.clone() : null; };
            no.prototype.getLastPosition = function () { var t = this.getLastRange(); return t ? t.end.clone() : null; };
            no.prototype.isEqual = function (t) { if (this.isFake != t.isFake)
                return !1; if (this.isFake && this.fakeSelectionLabel != t.fakeSelectionLabel)
                return !1; if (this.rangeCount != t.rangeCount)
                return !1; if (0 === this.rangeCount)
                return !0; if (!this.anchor.isEqual(t.anchor) || !this.focus.isEqual(t.focus))
                return !1; for (var _j = 0, _k = this._ranges; _j < _k.length; _j++) {
                var e_29 = _k[_j];
                var n_20 = !1;
                for (var _q = 0, _v = t._ranges; _q < _v.length; _q++) {
                    var i_11 = _v[_q];
                    if (e_29.isEqual(i_11)) {
                        n_20 = !0;
                        break;
                    }
                }
                if (!n_20)
                    return !1;
            } return !0; };
            no.prototype.isSimilar = function (t) { if (this.isBackward != t.isBackward)
                return !1; var e = eo(this.getRanges()); if (e != eo(t.getRanges()))
                return !1; if (0 == e)
                return !0; for (var _j = 0, _k = this.getRanges(); _j < _k.length; _j++) {
                var e_30 = _k[_j];
                e_30 = e_30.getTrimmed();
                var n_21 = !1;
                for (var _q = 0, _v = t.getRanges(); _q < _v.length; _q++) {
                    var i_12 = _v[_q];
                    if (i_12 = i_12.getTrimmed(), e_30.start.isEqual(i_12.start) && e_30.end.isEqual(i_12.end)) {
                        n_21 = !0;
                        break;
                    }
                }
                if (!n_21)
                    return !1;
            } return !0; };
            no.prototype.getSelectedElement = function () { if (1 !== this.rangeCount)
                return null; var t = this.getFirstRange(); var e = t.start.nodeAfter, n = t.end.nodeBefore; return t.start.parent.is("text") && t.start.isAtEnd && t.start.parent.nextSibling && (e = t.start.parent.nextSibling), t.end.parent.is("text") && t.end.isAtStart && t.end.parent.previousSibling && (n = t.end.parent.previousSibling), e instanceof _i && e == n ? e : null; };
            no.prototype.setTo = function (t, e, n) { if (null === t)
                this._setRanges([]), this._setFakeOptions(e);
            else if (t instanceof no || t instanceof io)
                this._setRanges(t.getRanges(), t.isBackward), this._setFakeOptions({ fake: t.isFake, label: t.fakeSelectionLabel });
            else if (t instanceof Xi)
                this._setRanges([t], e && e.backward), this._setFakeOptions(e);
            else if (t instanceof Zi)
                this._setRanges([new Xi(t)]), this._setFakeOptions(e);
            else if (t instanceof hi) {
                var i_13 = !!n && !!n.backward;
                var o_11;
                if (void 0 === e)
                    throw new Gn.b("view-selection-setTo-required-second-parameter: selection.setTo requires the second parameter when the first parameter is a node.");
                o_11 = "in" == e ? Xi._createIn(t) : "on" == e ? Xi._createOn(t) : new Xi(Zi._createAt(t, e)), this._setRanges([o_11], i_13), this._setFakeOptions(n);
            }
            else {
                if (!pi(t))
                    throw new Gn.b("view-selection-setTo-not-selectable: Cannot set selection to given place.");
                this._setRanges(t, e && e.backward), this._setFakeOptions(e);
            } this.fire("change"); };
            no.prototype.setFocus = function (t, e) { if (null === this.anchor)
                throw new Gn.b("view-selection-setFocus-no-ranges: Cannot set selection focus if there are no ranges in selection."); var n = Zi._createAt(t, e); if ("same" == n.compareWith(this.focus))
                return; var i = this.anchor; this._ranges.pop(), "before" == n.compareWith(i) ? this._addRange(new Xi(n, i), !0) : this._addRange(new Xi(i, n)), this.fire("change"); };
            no.prototype.is = function (t) { return "selection" == t; };
            no.prototype._setRanges = function (t, e) {
                if (e === void 0) { e = !1; }
                t = Array.from(t), this._ranges = [];
                for (var _j = 0, t_28 = t; _j < t_28.length; _j++) {
                    var e_31 = t_28[_j];
                    this._addRange(e_31);
                }
                this._lastRangeBackward = !!e;
            };
            no.prototype._setFakeOptions = function (t) {
                if (t === void 0) { t = {}; }
                this._isFake = !!t.fake, this._fakeSelectionLabel = t.fake && t.label || "";
            };
            no.prototype._addRange = function (t, e) {
                if (e === void 0) { e = !1; }
                if (!(t instanceof Xi))
                    throw new Gn.b("view-selection-add-range-not-range: Selection range set to an object that is not an instance of view.Range");
                this._pushRange(t), this._lastRangeBackward = !!e;
            };
            no.prototype._pushRange = function (t) { for (var _j = 0, _k = this._ranges; _j < _k.length; _j++) {
                var e_32 = _k[_j];
                if (t.isIntersecting(e_32))
                    throw new Gn.b("view-selection-range-intersects: Trying to add a range that intersects with another range from selection.", { addedRange: t, intersectingRange: e_32 });
            } this._ranges.push(new Xi(t.start, t.end)); };
            return no;
        }());
        ci(no, ei);
        var io = /** @class */ (function () {
            function io(t, e, n) {
                if (t === void 0) { t = null; }
                this._selection = new no, this._selection.delegate("change").to(this), this._selection.setTo(t, e, n);
            }
            Object.defineProperty(io.prototype, "isFake", {
                get: function () { return this._selection.isFake; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(io.prototype, "fakeSelectionLabel", {
                get: function () { return this._selection.fakeSelectionLabel; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(io.prototype, "anchor", {
                get: function () { return this._selection.anchor; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(io.prototype, "focus", {
                get: function () { return this._selection.focus; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(io.prototype, "isCollapsed", {
                get: function () { return this._selection.isCollapsed; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(io.prototype, "rangeCount", {
                get: function () { return this._selection.rangeCount; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(io.prototype, "isBackward", {
                get: function () { return this._selection.isBackward; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(io.prototype, "editableElement", {
                get: function () { return this._selection.editableElement; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(io.prototype, "_ranges", {
                get: function () { return this._selection._ranges; },
                enumerable: true,
                configurable: true
            });
            io.prototype.getRanges = function () { return __generator(this, function (_j) {
                switch (_j.label) {
                    case 0: return [5 /*yield**/, __values(this._selection.getRanges())];
                    case 1:
                        _j.sent();
                        return [2 /*return*/];
                }
            }); };
            io.prototype.getFirstRange = function () { return this._selection.getFirstRange(); };
            io.prototype.getLastRange = function () { return this._selection.getLastRange(); };
            io.prototype.getFirstPosition = function () { return this._selection.getFirstPosition(); };
            io.prototype.getLastPosition = function () { return this._selection.getLastPosition(); };
            io.prototype.getSelectedElement = function () { return this._selection.getSelectedElement(); };
            io.prototype.isEqual = function (t) { return this._selection.isEqual(t); };
            io.prototype.isSimilar = function (t) { return this._selection.isSimilar(t); };
            io.prototype.is = function (t) { return "selection" == t || "documentSelection" == t; };
            io.prototype._setTo = function (t, e, n) { this._selection.setTo(t, e, n); };
            io.prototype._setFocus = function (t, e) { this._selection.setFocus(t, e); };
            return io;
        }());
        ci(io, ei);
        var oo = /** @class */ (function () {
            function oo(t) {
                if (t === void 0) { t = {}; }
                this._items = [], this._itemMap = new Map, this._idProperty = t.idProperty || "id", this._bindToExternalToInternalMap = new WeakMap, this._bindToInternalToExternalMap = new WeakMap, this._skippedIndexesFromExternal = [];
            }
            Object.defineProperty(oo.prototype, "length", {
                get: function () { return this._items.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(oo.prototype, "first", {
                get: function () { return this._items[0] || null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(oo.prototype, "last", {
                get: function () { return this._items[this.length - 1] || null; },
                enumerable: true,
                configurable: true
            });
            oo.prototype.add = function (t, e) { var n; var i = this._idProperty; if (i in t) {
                if ("string" != typeof (n = t[i]))
                    throw new Gn.b("collection-add-invalid-id");
                if (this.get(n))
                    throw new Gn.b("collection-add-item-already-exists");
            }
            else
                t[i] = n = Jn(); if (void 0 === e)
                e = this._items.length;
            else if (e > this._items.length || e < 0)
                throw new Gn.b("collection-add-item-invalid-index"); return this._items.splice(e, 0, t), this._itemMap.set(n, t), this.fire("add", t, e), this; };
            oo.prototype.get = function (t) { var e; if ("string" == typeof t)
                e = this._itemMap.get(t);
            else {
                if ("number" != typeof t)
                    throw new Gn.b("collection-get-invalid-arg: Index or id must be given.");
                e = this._items[t];
            } return e || null; };
            oo.prototype.has = function (t) { if ("string" == typeof t)
                return this._itemMap.has(t); {
                var e_33 = t[this._idProperty];
                return this._itemMap.has(e_33);
            } };
            oo.prototype.getIndex = function (t) { var e; return e = "string" == typeof t ? this._itemMap.get(t) : t, this._items.indexOf(e); };
            oo.prototype.remove = function (t) { var e, n, i, o = !1; var r = this._idProperty; if ("string" == typeof t ? (n = t, o = !(i = this._itemMap.get(n)), i && (e = this._items.indexOf(i))) : "number" == typeof t ? (e = t, o = !(i = this._items[e]), i && (n = i[r])) : (n = (i = t)[r], o = -1 == (e = this._items.indexOf(i)) || !this._itemMap.get(n)), o)
                throw new Gn.b("collection-remove-404: Item not found."); this._items.splice(e, 1), this._itemMap.delete(n); var s = this._bindToInternalToExternalMap.get(i); return this._bindToInternalToExternalMap.delete(i), this._bindToExternalToInternalMap.delete(s), this.fire("remove", i, e), i; };
            oo.prototype.map = function (t, e) { return this._items.map(t, e); };
            oo.prototype.find = function (t, e) { return this._items.find(t, e); };
            oo.prototype.filter = function (t, e) { return this._items.filter(t, e); };
            oo.prototype.clear = function () { for (this._bindToCollection && (this.stopListening(this._bindToCollection), this._bindToCollection = null); this.length;)
                this.remove(0); };
            oo.prototype.bindTo = function (t) {
                var _this = this;
                if (this._bindToCollection)
                    throw new Gn.b("collection-bind-to-rebind: The collection cannot be bound more than once.");
                return this._bindToCollection = t, { as: function (t) { _this._setUpBindToBinding(function (e) { return new t(e); }); }, using: function (t) { "function" == typeof t ? _this._setUpBindToBinding(function (e) { return t(e); }) : _this._setUpBindToBinding(function (e) { return e[t]; }); } };
            };
            oo.prototype._setUpBindToBinding = function (t) {
                var _this = this;
                var e = this._bindToCollection, n = function (n, i, o) { var r = e._bindToCollection == _this, s = e._bindToInternalToExternalMap.get(i); if (r && s)
                    _this._bindToExternalToInternalMap.set(i, s), _this._bindToInternalToExternalMap.set(s, i);
                else {
                    var n_22 = t(i);
                    if (!n_22)
                        return void _this._skippedIndexesFromExternal.push(o);
                    var r_2 = o;
                    for (var _j = 0, _k = _this._skippedIndexesFromExternal; _j < _k.length; _j++) {
                        var t_29 = _k[_j];
                        o > t_29 && r_2--;
                    }
                    for (var _q = 0, _v = e._skippedIndexesFromExternal; _q < _v.length; _q++) {
                        var t_30 = _v[_q];
                        r_2 >= t_30 && r_2++;
                    }
                    _this._bindToExternalToInternalMap.set(i, n_22), _this._bindToInternalToExternalMap.set(n_22, i), _this.add(n_22, r_2);
                    for (var t_31 = 0; t_31 < e._skippedIndexesFromExternal.length; t_31++)
                        r_2 <= e._skippedIndexesFromExternal[t_31] && e._skippedIndexesFromExternal[t_31]++;
                } };
                for (var _j = 0, e_34 = e; _j < e_34.length; _j++) {
                    var t_32 = e_34[_j];
                    n(0, t_32, e.getIndex(t_32));
                }
                this.listenTo(e, "add", n), this.listenTo(e, "remove", function (t, e, n) { var i = _this._bindToExternalToInternalMap.get(e); i && _this.remove(i), _this._skippedIndexesFromExternal = _this._skippedIndexesFromExternal.reduce(function (t, e) { return (n < e && t.push(e - 1), n > e && t.push(e), t); }, []); });
            };
            oo.prototype[Symbol.iterator] = function () { return this._items[Symbol.iterator](); };
            return oo;
        }());
        ci(oo, ei);
        var ro = /** @class */ (function () {
            function ro() {
                this.selection = new io, this.roots = new oo({ idProperty: "rootName" }), this.set("isReadOnly", !1), this.set("isFocused", !1), this.set("isComposing", !1), this._postFixers = new Set;
            }
            ro.prototype.getRoot = function (t) {
                if (t === void 0) { t = "main"; }
                return this.roots.get(t);
            };
            ro.prototype.registerPostFixer = function (t) { this._postFixers.add(t); };
            ro.prototype.destroy = function () { this.roots.map(function (t) { return t.destroy(); }), this.stopListening(); };
            ro.prototype._callPostFixers = function (t) { var e = !1; do {
                for (var _j = 0, _k = this._postFixers; _j < _k.length; _j++) {
                    var n_23 = _k[_j];
                    if (e = n_23(t))
                        break;
                }
            } while (e); };
            return ro;
        }());
        ci(ro, Fi);
        var so = 10;
        var ao = /** @class */ (function (_super) {
            __extends(ao, _super);
            function ao(t, e, n) {
                var _this = this;
                _this = _super.call(this, t, e, n) || this, _this.getFillerOffset = co, _this._priority = so, _this._id = null, _this._clonesGroup = null;
                return _this;
            }
            Object.defineProperty(ao.prototype, "priority", {
                get: function () { return this._priority; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(ao.prototype, "id", {
                get: function () { return this._id; },
                enumerable: true,
                configurable: true
            });
            ao.prototype.getElementsWithSameId = function () { if (null === this.id)
                throw new Gn.b("attribute-element-get-elements-with-same-id-no-id: Cannot get elements with the same id for an attribute element without id."); return new Set(this._clonesGroup); };
            ao.prototype.is = function (t, e) {
                if (e === void 0) { e = null; }
                return e ? "attributeElement" == t && e == this.name || _super.prototype.is.call(this, t, e) : "attributeElement" == t || _super.prototype.is.call(this, t);
            };
            ao.prototype.isSimilar = function (t) { return null !== this.id || null !== t.id ? this.id === t.id : _super.prototype.isSimilar.call(this, t) && this.priority == t.priority; };
            ao.prototype._clone = function (t) { var e = _super.prototype._clone.call(this, t); return e._priority = this._priority, e._id = this._id, e; };
            return ao;
        }(_i));
        function co() { if (lo(this))
            return null; var t = this.parent; for (; t && t.is("attributeElement");) {
            if (lo(t) > 1)
                return null;
            t = t.parent;
        } return !t || lo(t) > 1 ? null : this.childCount; }
        function lo(t) { return Array.from(t.getChildren()).filter(function (t) { return !t.is("uiElement"); }).length; }
        ao.DEFAULT_PRIORITY = so;
        var uo = /** @class */ (function (_super) {
            __extends(uo, _super);
            function uo(t, e, n) {
                var _this = this;
                _this = _super.call(this, t, e, n) || this, _this.getFillerOffset = ho;
                return _this;
            }
            uo.prototype.is = function (t, e) {
                if (e === void 0) { e = null; }
                return e ? "emptyElement" == t && e == this.name || _super.prototype.is.call(this, t, e) : "emptyElement" == t || _super.prototype.is.call(this, t);
            };
            uo.prototype._insertChild = function (t, e) { if (e && (e instanceof hi || Array.from(e).length > 0))
                throw new Gn.b("view-emptyelement-cannot-add: Cannot add child nodes to EmptyElement instance."); };
            return uo;
        }(_i));
        function ho() { return null; }
        var fo = navigator.userAgent.toLowerCase();
        var mo = { isMac: function (t) { return t.indexOf("macintosh") > -1; }(fo), isEdge: function (t) { return !!t.match(/edge\/(\d+.?\d*)/); }(fo), isGecko: function (t) { return !!t.match(/gecko\/\d+/); }(fo) };
        var go = { "⌘": "ctrl", "⇧": "shift", "⌥": "alt" }, po = { ctrl: "⌘", shift: "⇧", alt: "⌥" }, bo = function () { var t = { arrowleft: 37, arrowup: 38, arrowright: 39, arrowdown: 40, backspace: 8, delete: 46, enter: 13, space: 32, esc: 27, tab: 9, ctrl: 1114112, cmd: 1114112, shift: 2228224, alt: 4456448 }; for (var e_35 = 65; e_35 <= 90; e_35++) {
            var n_24 = String.fromCharCode(e_35);
            t[n_24.toLowerCase()] = e_35;
        } for (var e_36 = 48; e_36 <= 57; e_36++)
            t[e_36 - 48] = e_36; for (var e_37 = 112; e_37 <= 123; e_37++)
            t["f" + (e_37 - 111)] = e_37; return t; }();
        function wo(t) { var e; if ("string" == typeof t) {
            if (!(e = bo[t.toLowerCase()]))
                throw new Gn.b("keyboard-unknown-key: Unknown key name.", { key: t });
        }
        else
            e = t.keyCode + (t.altKey ? bo.alt : 0) + (t.ctrlKey ? bo.ctrl : 0) + (t.shiftKey ? bo.shift : 0); return e; }
        function _o(t) { return "string" == typeof t && (t = ko(t)), t.map(function (t) { return "string" == typeof t ? wo(t) : t; }).reduce(function (t, e) { return e + t; }, 0); }
        function ko(t) { return t.split(/\s*\+\s*/); }
        var vo = /** @class */ (function (_super) {
            __extends(vo, _super);
            function vo(t, e, n) {
                var _this = this;
                _this = _super.call(this, t, e, n) || this, _this.getFillerOffset = xo;
                return _this;
            }
            vo.prototype.is = function (t, e) {
                if (e === void 0) { e = null; }
                return e ? "uiElement" == t && e == this.name || _super.prototype.is.call(this, t, e) : "uiElement" == t || _super.prototype.is.call(this, t);
            };
            vo.prototype._insertChild = function (t, e) { if (e && (e instanceof hi || Array.from(e).length > 0))
                throw new Gn.b("view-uielement-cannot-add: Cannot add child nodes to UIElement instance."); };
            vo.prototype.render = function (t) { return this.toDomElement(t); };
            vo.prototype.toDomElement = function (t) { var e = t.createElement(this.name); for (var _j = 0, _k = this.getAttributeKeys(); _j < _k.length; _j++) {
                var t_33 = _k[_j];
                e.setAttribute(t_33, this.getAttribute(t_33));
            } return e; };
            return vo;
        }(_i));
        function yo(t) { t.document.on("keydown", function (e, n) { return (function (t, e, n) { if (e.keyCode == bo.arrowright) {
            var t_34 = e.domTarget.ownerDocument.defaultView.getSelection(), i_14 = 1 == t_34.rangeCount && t_34.getRangeAt(0).collapsed;
            if (i_14 || e.shiftKey) {
                var e_38 = t_34.focusNode, o_12 = t_34.focusOffset, r_3 = n.domPositionToView(e_38, o_12);
                if (null === r_3)
                    return;
                var s_2 = !1;
                var a_2 = r_3.getLastMatchingPosition(function (t) { return (t.item.is("uiElement") && (s_2 = !0), !(!t.item.is("uiElement") && !t.item.is("attributeElement"))); });
                if (s_2) {
                    var e_39 = n.viewPositionToDom(a_2);
                    i_14 ? t_34.collapse(e_39.parent, e_39.offset) : t_34.extend(e_39.parent, e_39.offset);
                }
            }
        } })(0, n, t.domConverter); }); }
        function xo() { return null; }
        var Ao = /** @class */ (function () {
            function Ao(t) {
                this._children = [], t && this._insertChild(0, t);
            }
            Ao.prototype[Symbol.iterator] = function () { return this._children[Symbol.iterator](); };
            Object.defineProperty(Ao.prototype, "childCount", {
                get: function () { return this._children.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Ao.prototype, "isEmpty", {
                get: function () { return 0 === this.childCount; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Ao.prototype, "root", {
                get: function () { return this; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Ao.prototype, "parent", {
                get: function () { return null; },
                enumerable: true,
                configurable: true
            });
            Ao.prototype.is = function (t) { return "documentFragment" == t; };
            Ao.prototype._appendChild = function (t) { return this._insertChild(this.childCount, t); };
            Ao.prototype.getChild = function (t) { return this._children[t]; };
            Ao.prototype.getChildIndex = function (t) { return this._children.indexOf(t); };
            Ao.prototype.getChildren = function () { return this._children[Symbol.iterator](); };
            Ao.prototype._insertChild = function (t, e) { this._fireChange("children", this); var n = 0; var i = function (t) { if ("string" == typeof t)
                return [new fi(t)]; pi(t) || (t = [t]); return Array.from(t).map(function (t) { return "string" == typeof t ? new fi(t) : t instanceof mi ? new fi(t.data) : t; }); }(e); for (var _j = 0, i_15 = i; _j < i_15.length; _j++) {
                var e_40 = i_15[_j];
                null !== e_40.parent && e_40._remove(), e_40.parent = this, this._children.splice(t, 0, e_40), t++, n++;
            } return n; };
            Ao.prototype._removeChildren = function (t, e) {
                if (e === void 0) { e = 1; }
                this._fireChange("children", this);
                for (var n_25 = t; n_25 < t + e; n_25++)
                    this._children[n_25].parent = null;
                return this._children.splice(t, e);
            };
            Ao.prototype._fireChange = function (t, e) { this.fire("change:" + t, e); };
            return Ao;
        }());
        ci(Ao, ei);
        var Co = /** @class */ (function () {
            function Co(t) {
                this.document = t, this._cloneGroups = new Map;
            }
            Co.prototype.setSelection = function (t, e, n) { this.document.selection._setTo(t, e, n); };
            Co.prototype.setSelectionFocus = function (t, e) { this.document.selection._setFocus(t, e); };
            Co.prototype.createText = function (t) { return new fi(t); };
            Co.prototype.createAttributeElement = function (t, e, n) {
                if (n === void 0) { n = {}; }
                var i = new ao(t, e);
                return n.priority && (i._priority = n.priority), n.id && (i._id = n.id), i;
            };
            Co.prototype.createContainerElement = function (t, e) { return new yi(t, e); };
            Co.prototype.createEditableElement = function (t, e) { var n = new Gi(t, e); return n._document = this.document, n; };
            Co.prototype.createEmptyElement = function (t, e) { return new uo(t, e); };
            Co.prototype.createUIElement = function (t, e, n) { var i = new vo(t, e); return n && (i.render = n), i; };
            Co.prototype.setAttribute = function (t, e, n) { n._setAttribute(t, e); };
            Co.prototype.removeAttribute = function (t, e) { e._removeAttribute(t); };
            Co.prototype.addClass = function (t, e) { e._addClass(t); };
            Co.prototype.removeClass = function (t, e) { e._removeClass(t); };
            Co.prototype.setStyle = function (t, e, n) { C(t) && void 0 === n && (n = e), n._setStyle(t, e); };
            Co.prototype.removeStyle = function (t, e) { e._removeStyle(t); };
            Co.prototype.setCustomProperty = function (t, e, n) { n._setCustomProperty(t, e); };
            Co.prototype.removeCustomProperty = function (t, e) { return e._removeCustomProperty(t); };
            Co.prototype.breakAttributes = function (t) { return t instanceof Zi ? this._breakAttributes(t) : this._breakAttributesRange(t); };
            Co.prototype.breakContainer = function (t) { var e = t.parent; if (!e.is("containerElement"))
                throw new Gn.b("view-writer-break-non-container-element: Trying to break an element which is not a container element."); if (!e.parent)
                throw new Gn.b("view-writer-break-root: Trying to break root element."); if (t.isAtStart)
                return Zi._createBefore(e); if (!t.isAtEnd) {
                var n_26 = e._clone(!1);
                this.insert(Zi._createAfter(e), n_26);
                var i_16 = new Xi(t, Zi._createAt(e, "end")), o_13 = new Zi(n_26, 0);
                this.move(i_16, o_13);
            } return Zi._createAfter(e); };
            Co.prototype.mergeAttributes = function (t) { var e = t.offset, n = t.parent; if (n.is("text"))
                return t; if (n.is("attributeElement") && 0 === n.childCount) {
                var t_35 = n.parent, e_41 = n.index;
                return n._remove(), this._removeFromClonedElementsGroup(n), this.mergeAttributes(new Zi(t_35, e_41));
            } var i = n.getChild(e - 1), o = n.getChild(e); if (!i || !o)
                return t; if (i.is("text") && o.is("text"))
                return So(i, o); if (i.is("attributeElement") && o.is("attributeElement") && i.isSimilar(o)) {
                var t_36 = i.childCount;
                return i._appendChild(o.getChildren()), o._remove(), this._removeFromClonedElementsGroup(o), this.mergeAttributes(new Zi(i, t_36));
            } return t; };
            Co.prototype.mergeContainers = function (t) { var e = t.nodeBefore, n = t.nodeAfter; if (!(e && n && e.is("containerElement") && n.is("containerElement")))
                throw new Gn.b("view-writer-merge-containers-invalid-position: Element before and after given position cannot be merged."); var i = e.getChild(e.childCount - 1), o = i instanceof fi ? Zi._createAt(i, "end") : Zi._createAt(e, "end"); return this.move(Xi._createIn(n), Zi._createAt(e, "end")), this.remove(Xi._createOn(n)), o; };
            Co.prototype.insert = function (t, e) { (function t(e) { var _loop_1 = function (n_27) {
                if (!Io.some(function (t) { return n_27 instanceof t; }))
                    throw new Gn.b("view-writer-insert-invalid-node");
                n_27.is("text") || t(n_27.getChildren());
            }; for (var _j = 0, e_43 = e; _j < e_43.length; _j++) {
                var n_27 = e_43[_j];
                _loop_1(n_27);
            } })(e = pi(e) ? e.slice() : [e]); var n = To(t); if (!n)
                throw new Gn.b("view-writer-invalid-position-container"); var i = this._breakAttributes(t, !0), o = n._insertChild(i.offset, e); for (var _j = 0, e_42 = e; _j < e_42.length; _j++) {
                var t_37 = e_42[_j];
                this._addToClonedElementsGroup(t_37);
            } var r = i.getShiftedBy(o), s = this.mergeAttributes(i); if (0 === o)
                return new Xi(s, s); {
                s.isEqual(i) || r.offset--;
                var t_38 = this.mergeAttributes(r);
                return new Xi(s, t_38);
            } };
            Co.prototype.remove = function (t) { var e = t instanceof Xi ? t : Xi._createOn(t); if (Oo(e), e.isCollapsed)
                return new Ao; var _j = this._breakAttributesRange(e, !0), n = _j.start, i = _j.end, o = n.parent, r = i.offset - n.offset, s = o._removeChildren(n.offset, r); for (var _k = 0, s_3 = s; _k < s_3.length; _k++) {
                var t_39 = s_3[_k];
                this._removeFromClonedElementsGroup(t_39);
            } var a = this.mergeAttributes(n); return e.start = a, e.end = a.clone(), new Ao(s); };
            Co.prototype.clear = function (t, e) { Oo(t); var n = t.getWalker({ direction: "backward", ignoreElementEnd: !0 }); for (var _j = 0, n_28 = n; _j < n_28.length; _j++) {
                var i_17 = n_28[_j];
                var n_29 = i_17.item;
                var o_14 = void 0;
                if (n_29.is("element") && e.isSimilar(n_29))
                    o_14 = Xi._createOn(n_29);
                else if (!i_17.nextPosition.isAfter(t.start) && n_29.is("textProxy")) {
                    var t_40 = n_29.getAncestors().find(function (t) { return t.is("element") && e.isSimilar(t); });
                    t_40 && (o_14 = Xi._createIn(t_40));
                }
                o_14 && (o_14.end.isAfter(t.end) && (o_14.end = t.end), o_14.start.isBefore(t.start) && (o_14.start = t.start), this.remove(o_14));
            } };
            Co.prototype.move = function (t, e) { var n; if (e.isAfter(t.end)) {
                var i_18 = (e = this._breakAttributes(e, !0)).parent, o_15 = i_18.childCount;
                t = this._breakAttributesRange(t, !0), n = this.remove(t), e.offset += i_18.childCount - o_15;
            }
            else
                n = this.remove(t); return this.insert(e, n); };
            Co.prototype.wrap = function (t, e) { if (!(e instanceof ao))
                throw new Gn.b("view-writer-wrap-invalid-attribute"); if (Oo(t), t.isCollapsed) {
                var n_30 = t.start;
                n_30.parent.is("element") && !function (t) { return Array.from(t.getChildren()).some(function (t) { return !t.is("uiElement"); }); }(n_30.parent) && (n_30 = n_30.getLastMatchingPosition(function (t) { return t.item.is("uiElement"); })), n_30 = this._wrapPosition(n_30, e);
                var i_19 = this.document.selection;
                return i_19.isCollapsed && i_19.getFirstPosition().isEqual(t.start) && this.setSelection(n_30), new Xi(n_30);
            } return this._wrapRange(t, e); };
            Co.prototype.unwrap = function (t, e) { if (!(e instanceof ao))
                throw new Gn.b("view-writer-unwrap-invalid-attribute"); if (Oo(t), t.isCollapsed)
                return t; var _j = this._breakAttributesRange(t, !0), n = _j.start, i = _j.end; if (i.isEqual(n.getShiftedBy(1))) {
                var t_41 = n.nodeAfter;
                if (!e.isSimilar(t_41) && t_41 instanceof ao && this._unwrapAttributeElement(e, t_41)) {
                    var t_42 = this.mergeAttributes(n);
                    t_42.isEqual(n) || i.offset--;
                    var e_44 = this.mergeAttributes(i);
                    return new Xi(t_42, e_44);
                }
            } var o = n.parent, r = this._unwrapChildren(o, n.offset, i.offset, e), s = this.mergeAttributes(r.start); s.isEqual(r.start) || r.end.offset--; var a = this.mergeAttributes(r.end); return new Xi(s, a); };
            Co.prototype.rename = function (t, e) { var n = new yi(t, e.getAttributes()); return this.insert(Zi._createAfter(e), n), this.move(Xi._createIn(e), Zi._createAt(n, 0)), this.remove(Xi._createOn(e)), n; };
            Co.prototype.clearClonedElementsGroup = function (t) { this._cloneGroups.delete(t); };
            Co.prototype.createPositionAt = function (t, e) { return Zi._createAt(t, e); };
            Co.prototype.createPositionAfter = function (t) { return Zi._createAfter(t); };
            Co.prototype.createPositionBefore = function (t) { return Zi._createBefore(t); };
            Co.prototype.createRange = function (t, e) { return new Xi(t, e); };
            Co.prototype.createRangeOn = function (t) { return Xi._createOn(t); };
            Co.prototype.createRangeIn = function (t) { return Xi._createIn(t); };
            Co.prototype.createSelection = function (t, e, n) { return new no(t, e, n); };
            Co.prototype._wrapChildren = function (t, e, n, i) { var o = e; var r = []; for (; o < n;) {
                var e_45 = t.getChild(o), n_31 = e_45.is("text"), s_4 = e_45.is("attributeElement"), a_3 = e_45.is("emptyElement"), c_2 = e_45.is("uiElement");
                if (n_31 || a_3 || c_2 || s_4 && Po(i, e_45)) {
                    var n_32 = i._clone();
                    e_45._remove(), n_32._appendChild(e_45), t._insertChild(o, n_32), this._addToClonedElementsGroup(n_32), r.push(new Zi(t, o));
                }
                else
                    s_4 && this._wrapChildren(e_45, 0, e_45.childCount, i);
                o++;
            } var s = 0; for (var _j = 0, r_4 = r; _j < r_4.length; _j++) {
                var t_43 = r_4[_j];
                if (t_43.offset -= s, t_43.offset == e)
                    continue;
                this.mergeAttributes(t_43).isEqual(t_43) || (s++, n--);
            } return Xi._createFromParentsAndOffsets(t, e, t, n); };
            Co.prototype._unwrapChildren = function (t, e, n, i) { var o = e; var r = []; for (; o < n;) {
                var e_46 = t.getChild(o);
                if (e_46.isSimilar(i)) {
                    var i_20 = e_46.getChildren(), s_5 = e_46.childCount;
                    e_46._remove(), t._insertChild(o, i_20), this._removeFromClonedElementsGroup(e_46), r.push(new Zi(t, o), new Zi(t, o + s_5)), o += s_5, n += s_5 - 1;
                }
                else
                    e_46.is("attributeElement") && this._unwrapChildren(e_46, 0, e_46.childCount, i), o++;
            } var s = 0; for (var _j = 0, r_5 = r; _j < r_5.length; _j++) {
                var t_44 = r_5[_j];
                if (t_44.offset -= s, t_44.offset == e || t_44.offset == n)
                    continue;
                this.mergeAttributes(t_44).isEqual(t_44) || (s++, n--);
            } return Xi._createFromParentsAndOffsets(t, e, t, n); };
            Co.prototype._wrapRange = function (t, e) { if (function (t) { return t.start.parent == t.end.parent && t.start.parent.is("attributeElement") && 0 === t.start.offset && t.end.offset === t.start.parent.childCount; }(t) && this._wrapAttributeElement(e, t.start.parent)) {
                var e_47 = t.start.parent, n_33 = this.mergeAttributes(Zi._createAfter(e_47)), i_21 = this.mergeAttributes(Zi._createBefore(e_47));
                return new Xi(i_21, n_33);
            } var _j = this._breakAttributesRange(t, !0), n = _j.start, i = _j.end; if (i.isEqual(n.getShiftedBy(1))) {
                var t_45 = n.nodeAfter;
                if (t_45 instanceof ao && this._wrapAttributeElement(e, t_45)) {
                    var t_46 = this.mergeAttributes(n);
                    t_46.isEqual(n) || i.offset--;
                    var e_48 = this.mergeAttributes(i);
                    return new Xi(t_46, e_48);
                }
            } var o = n.parent, r = this._unwrapChildren(o, n.offset, i.offset, e), s = this._wrapChildren(o, r.start.offset, r.end.offset, e), a = this.mergeAttributes(s.start); a.isEqual(s.start) || s.end.offset--; var c = this.mergeAttributes(s.end); return new Xi(a, c); };
            Co.prototype._wrapPosition = function (t, e) { if (e.isSimilar(t.parent))
                return Mo(t.clone()); t.parent.is("text") && (t = Eo(t)); var n = this.createAttributeElement(); n._priority = Number.POSITIVE_INFINITY, n.isSimilar = (function () { return !1; }), t.parent._insertChild(t.offset, n); var i = new Xi(t, t.getShiftedBy(1)); this.wrap(i, e); var o = new Zi(n.parent, n.index); n._remove(); var r = o.nodeBefore, s = o.nodeAfter; return r instanceof fi && s instanceof fi ? So(r, s) : Mo(o); };
            Co.prototype._wrapAttributeElement = function (t, e) { if (!Ro(t, e))
                return !1; if (t.name !== e.name || t.priority !== e.priority)
                return !1; for (var _j = 0, _k = t.getAttributeKeys(); _j < _k.length; _j++) {
                var n_34 = _k[_j];
                if ("class" !== n_34 && "style" !== n_34 && e.hasAttribute(n_34) && e.getAttribute(n_34) !== t.getAttribute(n_34))
                    return !1;
            } for (var _q = 0, _v = t.getStyleNames(); _q < _v.length; _q++) {
                var n_35 = _v[_q];
                if (e.hasStyle(n_35) && e.getStyle(n_35) !== t.getStyle(n_35))
                    return !1;
            } for (var _w = 0, _x = t.getAttributeKeys(); _w < _x.length; _w++) {
                var n_36 = _x[_w];
                "class" !== n_36 && "style" !== n_36 && (e.hasAttribute(n_36) || this.setAttribute(n_36, t.getAttribute(n_36), e));
            } for (var _y = 0, _z = t.getStyleNames(); _y < _z.length; _y++) {
                var n_37 = _z[_y];
                e.hasStyle(n_37) || this.setStyle(n_37, t.getStyle(n_37), e);
            } for (var _0 = 0, _1 = t.getClassNames(); _0 < _1.length; _0++) {
                var n_38 = _1[_0];
                e.hasClass(n_38) || this.addClass(n_38, e);
            } return !0; };
            Co.prototype._unwrapAttributeElement = function (t, e) { if (!Ro(t, e))
                return !1; if (t.name !== e.name || t.priority !== e.priority)
                return !1; for (var _j = 0, _k = t.getAttributeKeys(); _j < _k.length; _j++) {
                var n_39 = _k[_j];
                if ("class" !== n_39 && "style" !== n_39 && (!e.hasAttribute(n_39) || e.getAttribute(n_39) !== t.getAttribute(n_39)))
                    return !1;
            } if (!e.hasClass.apply(e, t.getClassNames()))
                return !1; for (var _q = 0, _v = t.getStyleNames(); _q < _v.length; _q++) {
                var n_40 = _v[_q];
                if (!e.hasStyle(n_40) || e.getStyle(n_40) !== t.getStyle(n_40))
                    return !1;
            } for (var _w = 0, _x = t.getAttributeKeys(); _w < _x.length; _w++) {
                var n_41 = _x[_w];
                "class" !== n_41 && "style" !== n_41 && this.removeAttribute(n_41, e);
            } return this.removeClass(Array.from(t.getClassNames()), e), this.removeStyle(Array.from(t.getStyleNames()), e), !0; };
            Co.prototype._breakAttributesRange = function (t, e) {
                if (e === void 0) { e = !1; }
                var n = t.start, i = t.end;
                if (Oo(t), t.isCollapsed) {
                    var n_42 = this._breakAttributes(t.start, e);
                    return new Xi(n_42, n_42);
                }
                var o = this._breakAttributes(i, e), r = o.parent.childCount, s = this._breakAttributes(n, e);
                return o.offset += o.parent.childCount - r, new Xi(s, o);
            };
            Co.prototype._breakAttributes = function (t, e) {
                if (e === void 0) { e = !1; }
                var n = t.offset, i = t.parent;
                if (t.parent.is("emptyElement"))
                    throw new Gn.b("view-writer-cannot-break-empty-element");
                if (t.parent.is("uiElement"))
                    throw new Gn.b("view-writer-cannot-break-ui-element");
                if (!e && i.is("text") && No(i.parent))
                    return t.clone();
                if (No(i))
                    return t.clone();
                if (i.is("text"))
                    return this._breakAttributes(Eo(t), e);
                if (n == i.childCount) {
                    var t_47 = new Zi(i.parent, i.index + 1);
                    return this._breakAttributes(t_47, e);
                }
                if (0 === n) {
                    var t_48 = new Zi(i.parent, i.index);
                    return this._breakAttributes(t_48, e);
                }
                {
                    var t_49 = i.index + 1, o_16 = i._clone();
                    i.parent._insertChild(t_49, o_16), this._addToClonedElementsGroup(o_16);
                    var r_6 = i.childCount - n, s_6 = i._removeChildren(n, r_6);
                    o_16._appendChild(s_6);
                    var a_4 = new Zi(i.parent, t_49);
                    return this._breakAttributes(a_4, e);
                }
            };
            Co.prototype._addToClonedElementsGroup = function (t) { if (!t.root.is("rootElement"))
                return; if (t.is("element"))
                for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
                    var e_49 = _k[_j];
                    this._addToClonedElementsGroup(e_49);
                } var e = t.id; if (!e)
                return; var n = this._cloneGroups.get(e); n || (n = new Set, this._cloneGroups.set(e, n)), n.add(t), t._clonesGroup = n; };
            Co.prototype._removeFromClonedElementsGroup = function (t) { if (t.is("element"))
                for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
                    var e_50 = _k[_j];
                    this._removeFromClonedElementsGroup(e_50);
                } var e = t.id; if (!e)
                return; var n = this._cloneGroups.get(e); n && n.delete(t); };
            return Co;
        }());
        function To(t) { var e = t.parent; for (; !No(e);) {
            if (!e)
                return;
            e = e.parent;
        } return e; }
        function Po(t, e) { return t.priority < e.priority || !(t.priority > e.priority) && t.getIdentity() < e.getIdentity(); }
        function Mo(t) { var e = t.nodeBefore; if (e && e.is("text"))
            return new Zi(e, e.data.length); var n = t.nodeAfter; return n && n.is("text") ? new Zi(n, 0) : t; }
        function Eo(t) { if (t.offset == t.parent.data.length)
            return new Zi(t.parent.parent, t.parent.index + 1); if (0 === t.offset)
            return new Zi(t.parent.parent, t.parent.index); var e = t.parent.data.slice(t.offset); return t.parent._data = t.parent.data.slice(0, t.offset), t.parent.parent._insertChild(t.parent.index + 1, new fi(e)), new Zi(t.parent.parent, t.parent.index + 1); }
        function So(t, e) { var n = t.data.length; return t._data += e.data, e._remove(), new Zi(t, n); }
        var Io = [fi, ao, yi, uo, vo];
        function No(t) { return t && (t.is("containerElement") || t.is("documentFragment")); }
        function Oo(t) { var e = To(t.start), n = To(t.end); if (!e || !n || e !== n)
            throw new Gn.b("view-writer-invalid-range-container"); }
        function Ro(t, e) { return null === t.id && null === e.id; }
        function Do(t) { return "[object Text]" == Object.prototype.toString.call(t); }
        var Lo = function (t) { var e = t.createElement("br"); return e.dataset.ckeFiller = !0, e; }, jo = function (t) { return t.createTextNode(" "); }, Vo = 7;
        var zo = "";
        for (var t_50 = 0; t_50 < Vo; t_50++)
            zo += "​";
        function Bo(t) { return Do(t) && t.data.substr(0, Vo) === zo; }
        function Fo(t) { return t.data.length == Vo && Bo(t); }
        function Uo(t) { return Bo(t) ? t.data.slice(Vo) : t.data; }
        var Ho = new WeakMap;
        function qo(t, e) { var n = Ho.get(e); return n || (n = e(window.document), Ho.set(e, n)), t.isEqualNode(n); }
        function Wo(t, e) { if (e.keyCode == bo.arrowleft) {
            var t_51 = e.domTarget.ownerDocument.defaultView.getSelection();
            if (1 == t_51.rangeCount && t_51.getRangeAt(0).collapsed) {
                var e_51 = t_51.getRangeAt(0).startContainer, n_43 = t_51.getRangeAt(0).startOffset;
                Bo(e_51) && n_43 <= Vo && t_51.collapse(e_51, 0);
            }
        } }
        function Yo(t, e, n, i) {
            if (i === void 0) { i = !1; }
            n = n || function (t, e) { return t === e; }, Array.isArray(t) || (t = Array.from(t)), Array.isArray(e) || (e = Array.from(e));
            var o = function (t, e, n) { var i = $o(t, e, n); if (-1 === i)
                return { firstIndex: -1, lastIndexOld: -1, lastIndexNew: -1 }; var o = Go(t, i), r = Go(e, i), s = $o(o, r, n), a = t.length - s, c = e.length - s; return { firstIndex: i, lastIndexOld: a, lastIndexNew: c }; }(t, e, n);
            return i ? function (t, e) { var n = t.firstIndex, i = t.lastIndexOld, o = t.lastIndexNew; if (-1 === n)
                return Array(e).fill("equal"); var r = []; n > 0 && (r = r.concat(Array(n).fill("equal"))); o - n > 0 && (r = r.concat(Array(o - n).fill("insert"))); i - n > 0 && (r = r.concat(Array(i - n).fill("delete"))); o < e && (r = r.concat(Array(e - o).fill("equal"))); return r; }(o, e.length) : function (t, e) { var n = [], i = e.firstIndex, o = e.lastIndexOld, r = e.lastIndexNew; r - i > 0 && n.push({ index: i, type: "insert", values: t.slice(i, r) }); o - i > 0 && n.push({ index: i + (r - i), type: "delete", howMany: o - i }); return n; }(e, o);
        }
        function $o(t, e, n) { for (var i_22 = 0; i_22 < Math.max(t.length, e.length); i_22++)
            if (void 0 === t[i_22] || void 0 === e[i_22] || !n(t[i_22], e[i_22]))
                return i_22; return -1; }
        function Go(t, e) { return t.slice(e).reverse(); }
        function Qo(t, e, n) { n = n || function (t, e) { return t === e; }; var i = t.length, o = e.length; if (i > 200 || o > 200 || i + o > 300)
            return Qo.fastDiff(t, e, n, !0); var r, s; if (o < i) {
            var n_44 = t;
            t = e, e = n_44, r = "delete", s = "insert";
        }
        else
            r = "insert", s = "delete"; var a = t.length, c = e.length, l = c - a, d = {}, u = {}; function h(i) { var o = (void 0 !== u[i - 1] ? u[i - 1] : -1) + 1, l = void 0 !== u[i + 1] ? u[i + 1] : -1, h = o > l ? -1 : 1; d[i + h] && (d[i] = d[i + h].slice(0)), d[i] || (d[i] = []), d[i].push(o > l ? r : s); var f = Math.max(o, l), m = f - i; for (; m < a && f < c && n(t[m], e[f]);)
            m++, f++, d[i].push("equal"); return f; } var f, m = 0; do {
            for (f = -m; f < l; f++)
                u[f] = h(f);
            for (f = l + m; f > l; f--)
                u[f] = h(f);
            u[l] = h(l), m++;
        } while (u[l] !== c); return d[l].slice(1); }
        function Ko(t, e, n) { t.insertBefore(n, t.childNodes[e] || null); }
        function Jo(t) { var e = t.parentNode; e && e.removeChild(t); }
        function Zo(t) { if (t) {
            if (t.defaultView)
                return t instanceof t.defaultView.Document;
            if (t.ownerDocument && t.ownerDocument.defaultView)
                return t instanceof t.ownerDocument.defaultView.Node;
        } return !1; }
        Qo.fastDiff = Yo;
        var Xo = /** @class */ (function () {
            function Xo(t, e) {
                this.domDocuments = new Set, this.domConverter = t, this.markedAttributes = new Set, this.markedChildren = new Set, this.markedTexts = new Set, this.selection = e, this.isFocused = !1, this._inlineFiller = null, this._fakeSelectionContainer = null;
            }
            Xo.prototype.markToSync = function (t, e) { if ("text" === t)
                this.domConverter.mapViewToDom(e.parent) && this.markedTexts.add(e);
            else {
                if (!this.domConverter.mapViewToDom(e))
                    return;
                if ("attributes" === t)
                    this.markedAttributes.add(e);
                else {
                    if ("children" !== t)
                        throw new Gn.b("view-renderer-unknown-type: Unknown type passed to Renderer.markToSync.");
                    this.markedChildren.add(e);
                }
            } };
            Xo.prototype.render = function () { var t; for (var _j = 0, _k = this.markedChildren; _j < _k.length; _j++) {
                var t_52 = _k[_j];
                this._updateChildrenMappings(t_52);
            } this._inlineFiller && !this._isSelectionInInlineFiller() && this._removeInlineFiller(), this._inlineFiller ? t = this._getInlineFillerPosition() : this._needsInlineFillerAtSelection() && (t = this.selection.getFirstPosition(), this.markedChildren.add(t.parent)); for (var _q = 0, _v = this.markedAttributes; _q < _v.length; _q++) {
                var t_53 = _v[_q];
                this._updateAttrs(t_53);
            } for (var _w = 0, _x = this.markedChildren; _w < _x.length; _w++) {
                var e_52 = _x[_w];
                this._updateChildren(e_52, { inlineFillerPosition: t });
            } for (var _y = 0, _z = this.markedTexts; _y < _z.length; _y++) {
                var e_53 = _z[_y];
                !this.markedChildren.has(e_53.parent) && this.domConverter.mapViewToDom(e_53.parent) && this._updateText(e_53, { inlineFillerPosition: t });
            } if (t) {
                var e_54 = this.domConverter.viewPositionToDom(t), n_45 = e_54.parent.ownerDocument;
                Bo(e_54.parent) ? this._inlineFiller = e_54.parent : this._inlineFiller = tr(n_45, e_54.parent, e_54.offset);
            }
            else
                this._inlineFiller = null; this._updateSelection(), this._updateFocus(), this.markedTexts.clear(), this.markedAttributes.clear(), this.markedChildren.clear(); };
            Xo.prototype._updateChildrenMappings = function (t) { var e = this.domConverter.mapViewToDom(t); if (!e)
                return; var n = this.domConverter.mapViewToDom(t).childNodes, i = Array.from(this.domConverter.viewChildrenToDom(t, e.ownerDocument, { withChildren: !1 })), o = this._diffNodeLists(n, i), r = this._findReplaceActions(o, n, i); if (-1 !== r.indexOf("replace")) {
                var e_55 = { equal: 0, insert: 0, delete: 0 };
                for (var _j = 0, r_7 = r; _j < r_7.length; _j++) {
                    var o_17 = r_7[_j];
                    if ("replace" === o_17) {
                        var o_18 = e_55.equal + e_55.insert, r_8 = e_55.equal + e_55.delete, s_7 = t.getChild(o_18);
                        s_7 && !s_7.is("uiElement") && this._updateElementMappings(s_7, n[r_8]), Jo(i[o_18]), e_55.equal++;
                    }
                    else
                        e_55[o_17]++;
                }
            } };
            Xo.prototype._updateElementMappings = function (t, e) { this.domConverter.unbindDomElement(e), this.domConverter.bindElements(e, t), this.markedChildren.add(t), this.markedAttributes.add(t); };
            Xo.prototype._getInlineFillerPosition = function () { var t = this.selection.getFirstPosition(); return t.parent.is("text") ? Zi._createBefore(this.selection.getFirstPosition().parent) : t; };
            Xo.prototype._isSelectionInInlineFiller = function () { if (1 != this.selection.rangeCount || !this.selection.isCollapsed)
                return !1; var t = this.selection.getFirstPosition(), e = this.domConverter.viewPositionToDom(t); return !!(e && Do(e.parent) && Bo(e.parent)); };
            Xo.prototype._removeInlineFiller = function () { var t = this._inlineFiller; if (!Bo(t))
                throw new Gn.b("view-renderer-filler-was-lost: The inline filler node was lost."); Fo(t) ? t.parentNode.removeChild(t) : t.data = t.data.substr(Vo), this._inlineFiller = null; };
            Xo.prototype._needsInlineFillerAtSelection = function () { if (1 != this.selection.rangeCount || !this.selection.isCollapsed)
                return !1; var t = this.selection.getFirstPosition(), e = t.parent, n = t.offset; if (!this.domConverter.mapViewToDom(e.root))
                return !1; if (!e.is("element"))
                return !1; if (!function (t) { if ("false" == t.getAttribute("contenteditable"))
                return !1; var e = t.findAncestor(function (t) { return t.hasAttribute("contenteditable"); }); return !e || "true" == e.getAttribute("contenteditable"); }(e))
                return !1; if (n === e.getFillerOffset())
                return !1; var i = t.nodeBefore, o = t.nodeAfter; return !(i instanceof fi || o instanceof fi); };
            Xo.prototype._updateText = function (t, e) { var n = this.domConverter.findCorrespondingDomText(t), i = this.domConverter.viewToDom(t, n.ownerDocument), o = n.data; var r = i.data; var s = e.inlineFillerPosition; if (s && s.parent == t.parent && s.offset == t.index && (r = zo + r), o != r) {
                var t_55 = Yo(o, r);
                for (var _j = 0, t_54 = t_55; _j < t_54.length; _j++) {
                    var e_56 = t_54[_j];
                    "insert" === e_56.type ? n.insertData(e_56.index, e_56.values.join("")) : n.deleteData(e_56.index, e_56.howMany);
                }
            } };
            Xo.prototype._updateAttrs = function (t) { var e = this.domConverter.mapViewToDom(t); if (!e)
                return; var n = Array.from(e.attributes).map(function (t) { return t.name; }), i = t.getAttributeKeys(); for (var _j = 0, i_23 = i; _j < i_23.length; _j++) {
                var n_47 = i_23[_j];
                e.setAttribute(n_47, t.getAttribute(n_47));
            } for (var _k = 0, n_46 = n; _k < n_46.length; _k++) {
                var i_24 = n_46[_k];
                t.hasAttribute(i_24) || e.removeAttribute(i_24);
            } };
            Xo.prototype._updateChildren = function (t, e) { var n = this.domConverter.mapViewToDom(t); if (!n)
                return; var i = e.inlineFillerPosition, o = this.domConverter.mapViewToDom(t).childNodes, r = Array.from(this.domConverter.viewChildrenToDom(t, n.ownerDocument, { bind: !0, inlineFillerPosition: i })); i && i.parent === t && tr(n.ownerDocument, r, i.offset); var s = this._diffNodeLists(o, r); var a = 0; var c = new Set; for (var _j = 0, s_8 = s; _j < s_8.length; _j++) {
                var t_56 = s_8[_j];
                "insert" === t_56 ? (Ko(n, a, r[a]), a++) : "delete" === t_56 ? (c.add(o[a]), Jo(o[a])) : (this._markDescendantTextToSync(this.domConverter.domToView(r[a])), a++);
            } for (var _k = 0, c_3 = c; _k < c_3.length; _k++) {
                var t_57 = c_3[_k];
                t_57.parentNode || this.domConverter.unbindDomElement(t_57);
            } };
            Xo.prototype._diffNodeLists = function (t, e) { return Qo(t = function (t, e) { var n = Array.from(t); if (0 == n.length || !e)
                return n; n[n.length - 1] == e && n.pop(); return n; }(t, this._fakeSelectionContainer), e, function (t, e, n) { if (e === n)
                return !0; if (Do(e) && Do(n))
                return e.data === n.data; if (qo(e, t) && qo(n, t))
                return !0; return !1; }.bind(null, this.domConverter.blockFiller)); };
            Xo.prototype._findReplaceActions = function (t, e, n) { if (-1 === t.indexOf("insert") || -1 === t.indexOf("delete"))
                return t; var i = [], o = [], r = []; var s = { equal: 0, insert: 0, delete: 0 }; for (var _j = 0, t_58 = t; _j < t_58.length; _j++) {
                var a_5 = t_58[_j];
                "insert" === a_5 ? r.push(n[s.equal + s.insert]) : "delete" === a_5 ? o.push(e[s.equal + s.delete]) : ((i = i.concat(Qo(o, r, er).map(function (t) { return "equal" === t ? "replace" : t; }))).push("equal"), o = [], r = []), s[a_5]++;
            } return i.concat(Qo(o, r, er).map(function (t) { return "equal" === t ? "replace" : t; })); };
            Xo.prototype._markDescendantTextToSync = function (t) { if (t)
                if (t.is("text"))
                    this.markedTexts.add(t);
                else if (t.is("element"))
                    for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
                        var e_57 = _k[_j];
                        this._markDescendantTextToSync(e_57);
                    } };
            Xo.prototype._updateSelection = function () { if (0 === this.selection.rangeCount)
                return this._removeDomSelection(), void this._removeFakeSelection(); var t = this.domConverter.mapViewToDom(this.selection.editableElement); this.isFocused && t && (this.selection.isFake ? this._updateFakeSelection(t) : (this._removeFakeSelection(), this._updateDomSelection(t))); };
            Xo.prototype._updateFakeSelection = function (t) { var e = t.ownerDocument; var n = this._fakeSelectionContainer; n || (this._fakeSelectionContainer = n = e.createElement("div"), Object.assign(n.style, { position: "fixed", top: 0, left: "-9999px", width: "42px" }), n.appendChild(e.createTextNode(" "))), n.parentElement && n.parentElement == t || t.appendChild(n), n.firstChild.data = this.selection.fakeSelectionLabel || " "; var i = e.getSelection(), o = e.createRange(); i.removeAllRanges(), o.selectNodeContents(n), i.addRange(o), this.domConverter.bindFakeSelection(n, this.selection); };
            Xo.prototype._updateDomSelection = function (t) { var e = t.ownerDocument.defaultView.getSelection(); if (!this._domSelectionNeedsUpdate(e))
                return; var n = this.domConverter.viewPositionToDom(this.selection.anchor), i = this.domConverter.viewPositionToDom(this.selection.focus); t.focus(), e.collapse(n.parent, n.offset), e.extend(i.parent, i.offset), mo.isGecko && function (t, e) { var n = t.parent; if (n.nodeType != Node.ELEMENT_NODE || t.offset != n.childNodes.length - 1)
                return; var i = n.childNodes[t.offset]; i && "BR" == i.tagName && e.addRange(e.getRangeAt(0)); }(i, e); };
            Xo.prototype._domSelectionNeedsUpdate = function (t) { if (!this.domConverter.isDomSelectionCorrect(t))
                return !0; var e = t && this.domConverter.domSelectionToView(t); return (!e || !this.selection.isEqual(e)) && !(!this.selection.isCollapsed && this.selection.isSimilar(e)); };
            Xo.prototype._removeDomSelection = function () { for (var _j = 0, _k = this.domDocuments; _j < _k.length; _j++) {
                var t_59 = _k[_j];
                if (t_59.getSelection().rangeCount) {
                    var e_58 = t_59.activeElement, n_48 = this.domConverter.mapDomToView(e_58);
                    e_58 && n_48 && t_59.getSelection().removeAllRanges();
                }
            } };
            Xo.prototype._removeFakeSelection = function () { var t = this._fakeSelectionContainer; t && t.remove(); };
            Xo.prototype._updateFocus = function () { if (this.isFocused) {
                var t_60 = this.selection.editableElement;
                t_60 && this.domConverter.focus(t_60);
            } };
            return Xo;
        }());
        function tr(t, e, n) { var i = e instanceof Array ? e : e.childNodes, o = i[n]; if (Do(o))
            return o.data = zo + o.data, o; {
            var o_19 = t.createTextNode(zo);
            return Array.isArray(e) ? i.splice(n, 0, o_19) : Ko(e, n, o_19), o_19;
        } }
        function er(t, e) { return Zo(t) && Zo(e) && !Do(t) && !Do(e) && t.tagName.toLowerCase() === e.tagName.toLowerCase(); }
        ci(Xo, Fi);
        var nr = { window: window, document: document };
        function ir(t) { var e = 0; for (; t.previousSibling;)
            t = t.previousSibling, e++; return e; }
        function or(t) { var e = []; for (; t && t.nodeType != Node.DOCUMENT_NODE;)
            e.unshift(t), t = t.parentNode; return e; }
        var rr = /** @class */ (function () {
            function rr(t) {
                if (t === void 0) { t = {}; }
                this.blockFiller = t.blockFiller || Lo, this.preElements = ["pre"], this.blockElements = ["p", "div", "h1", "h2", "h3", "h4", "h5", "h6"], this._domToViewMapping = new WeakMap, this._viewToDomMapping = new WeakMap, this._fakeSelectionMapping = new WeakMap;
            }
            rr.prototype.bindFakeSelection = function (t, e) { this._fakeSelectionMapping.set(t, new no(e)); };
            rr.prototype.fakeSelectionToView = function (t) { return this._fakeSelectionMapping.get(t); };
            rr.prototype.bindElements = function (t, e) { this._domToViewMapping.set(t, e), this._viewToDomMapping.set(e, t); };
            rr.prototype.unbindDomElement = function (t) { var e = this._domToViewMapping.get(t); if (e) {
                this._domToViewMapping.delete(t), this._viewToDomMapping.delete(e);
                for (var _j = 0, _k = Array.from(t.childNodes); _j < _k.length; _j++) {
                    var e_59 = _k[_j];
                    this.unbindDomElement(e_59);
                }
            } };
            rr.prototype.bindDocumentFragments = function (t, e) { this._domToViewMapping.set(t, e), this._viewToDomMapping.set(e, t); };
            rr.prototype.viewToDom = function (t, e, n) {
                if (n === void 0) { n = {}; }
                if (t.is("text")) {
                    var n_49 = this._processDataFromViewText(t);
                    return e.createTextNode(n_49);
                }
                {
                    if (this.mapViewToDom(t))
                        return this.mapViewToDom(t);
                    var i_25;
                    if (t.is("documentFragment"))
                        i_25 = e.createDocumentFragment(), n.bind && this.bindDocumentFragments(i_25, t);
                    else {
                        if (t.is("uiElement"))
                            return i_25 = t.render(e), n.bind && this.bindElements(i_25, t), i_25;
                        i_25 = e.createElement(t.name), n.bind && this.bindElements(i_25, t);
                        for (var _j = 0, _k = t.getAttributeKeys(); _j < _k.length; _j++) {
                            var e_60 = _k[_j];
                            i_25.setAttribute(e_60, t.getAttribute(e_60));
                        }
                    }
                    if (n.withChildren || void 0 === n.withChildren)
                        for (var _q = 0, _v = this.viewChildrenToDom(t, e, n); _q < _v.length; _q++) {
                            var o_20 = _v[_q];
                            i_25.appendChild(o_20);
                        }
                    return i_25;
                }
            };
            rr.prototype.viewChildrenToDom = function (t, e, n) {
                var i, o, _j, _k, r_9, _q, _v;
                if (n === void 0) { n = {}; }
                return __generator(this, function (_w) {
                    switch (_w.label) {
                        case 0:
                            i = t.getFillerOffset && t.getFillerOffset();
                            o = 0;
                            _j = 0, _k = t.getChildren();
                            _w.label = 1;
                        case 1:
                            if (!(_j < _k.length)) return [3 /*break*/, 6];
                            r_9 = _k[_j];
                            _q = i === o;
                            if (!_q) return [3 /*break*/, 3];
                            return [4 /*yield*/, this.blockFiller(e)];
                        case 2:
                            _q = (_w.sent());
                            _w.label = 3;
                        case 3:
                            _q;
                            return [4 /*yield*/, this.viewToDom(r_9, e, n)];
                        case 4:
                            _w.sent(), o++;
                            _w.label = 5;
                        case 5:
                            _j++;
                            return [3 /*break*/, 1];
                        case 6:
                            _v = i === o;
                            if (!_v) return [3 /*break*/, 8];
                            return [4 /*yield*/, this.blockFiller(e)];
                        case 7:
                            _v = (_w.sent());
                            _w.label = 8;
                        case 8:
                            _v;
                            return [2 /*return*/];
                    }
                });
            };
            rr.prototype.viewRangeToDom = function (t) { var e = this.viewPositionToDom(t.start), n = this.viewPositionToDom(t.end), i = document.createRange(); return i.setStart(e.parent, e.offset), i.setEnd(n.parent, n.offset), i; };
            rr.prototype.viewPositionToDom = function (t) { var e = t.parent; if (e.is("text")) {
                var n_50 = this.findCorrespondingDomText(e);
                if (!n_50)
                    return null;
                var i_26 = t.offset;
                return Bo(n_50) && (i_26 += Vo), { parent: n_50, offset: i_26 };
            } {
                var n_51, i_27, o_21;
                if (0 === t.offset) {
                    if (!(n_51 = this.mapViewToDom(e)))
                        return null;
                    o_21 = n_51.childNodes[0];
                }
                else {
                    var e_61 = t.nodeBefore;
                    if (!(i_27 = e_61.is("text") ? this.findCorrespondingDomText(e_61) : this.mapViewToDom(t.nodeBefore)))
                        return null;
                    n_51 = i_27.parentNode, o_21 = i_27.nextSibling;
                }
                if (Do(o_21) && Bo(o_21))
                    return { parent: o_21, offset: Vo };
                return { parent: n_51, offset: i_27 ? ir(i_27) + 1 : 0 };
            } };
            rr.prototype.domToView = function (t, e) {
                if (e === void 0) { e = {}; }
                if (qo(t, this.blockFiller))
                    return null;
                var n = this.getParentUIElement(t, this._domToViewMapping);
                if (n)
                    return n;
                if (Do(t)) {
                    if (Fo(t))
                        return null;
                    {
                        var e_62 = this._processDataFromDomText(t);
                        return "" === e_62 ? null : new fi(e_62);
                    }
                }
                if (this.isComment(t))
                    return null;
                {
                    if (this.mapDomToView(t))
                        return this.mapDomToView(t);
                    var n_52;
                    if (this.isDocumentFragment(t))
                        n_52 = new Ao, e.bind && this.bindDocumentFragments(t, n_52);
                    else {
                        var i_28 = e.keepOriginalCase ? t.tagName : t.tagName.toLowerCase();
                        n_52 = new _i(i_28), e.bind && this.bindElements(t, n_52);
                        var o_22 = t.attributes;
                        for (var t_61 = o_22.length - 1; t_61 >= 0; t_61--)
                            n_52._setAttribute(o_22[t_61].name, o_22[t_61].value);
                    }
                    if (e.withChildren || void 0 === e.withChildren)
                        for (var _j = 0, _k = this.domChildrenToView(t, e); _j < _k.length; _j++) {
                            var i_29 = _k[_j];
                            n_52._appendChild(i_29);
                        }
                    return n_52;
                }
            };
            rr.prototype.domChildrenToView = function (t, e) {
                var n_53, i_30, o_23, _j;
                if (e === void 0) { e = {}; }
                return __generator(this, function (_k) {
                    switch (_k.label) {
                        case 0:
                            n_53 = 0;
                            _k.label = 1;
                        case 1:
                            if (!(n_53 < t.childNodes.length)) return [3 /*break*/, 5];
                            i_30 = t.childNodes[n_53], o_23 = this.domToView(i_30, e);
                            _j = null !== o_23;
                            if (!_j) return [3 /*break*/, 3];
                            return [4 /*yield*/, o_23];
                        case 2:
                            _j = (_k.sent());
                            _k.label = 3;
                        case 3:
                            _j;
                            _k.label = 4;
                        case 4:
                            n_53++;
                            return [3 /*break*/, 1];
                        case 5: return [2 /*return*/];
                    }
                });
            };
            rr.prototype.domSelectionToView = function (t) { if (1 === t.rangeCount) {
                var e_63 = t.getRangeAt(0).startContainer;
                Do(e_63) && (e_63 = e_63.parentNode);
                var n_54 = this.fakeSelectionToView(e_63);
                if (n_54)
                    return n_54;
            } var e = this.isDomSelectionBackward(t), n = []; for (var e_64 = 0; e_64 < t.rangeCount; e_64++) {
                var i_31 = t.getRangeAt(e_64), o_24 = this.domRangeToView(i_31);
                o_24 && n.push(o_24);
            } return new no(n, { backward: e }); };
            rr.prototype.domRangeToView = function (t) { var e = this.domPositionToView(t.startContainer, t.startOffset), n = this.domPositionToView(t.endContainer, t.endOffset); return e && n ? new Xi(e, n) : null; };
            rr.prototype.domPositionToView = function (t, e) { if (qo(t, this.blockFiller))
                return this.domPositionToView(t.parentNode, ir(t)); var n = this.mapDomToView(t); if (n && n.is("uiElement"))
                return Zi._createBefore(n); if (Do(t)) {
                if (Fo(t))
                    return this.domPositionToView(t.parentNode, ir(t));
                var n_55 = this.findCorrespondingViewText(t);
                var i_32 = e;
                return n_55 ? (Bo(t) && (i_32 = (i_32 -= Vo) < 0 ? 0 : i_32), new Zi(n_55, i_32)) : null;
            } if (0 === e) {
                var e_65 = this.mapDomToView(t);
                if (e_65)
                    return new Zi(e_65, 0);
            }
            else {
                var n_56 = t.childNodes[e - 1], i_33 = Do(n_56) ? this.findCorrespondingViewText(n_56) : this.mapDomToView(n_56);
                if (i_33 && i_33.parent)
                    return new Zi(i_33.parent, i_33.index + 1);
            } return null; };
            rr.prototype.mapDomToView = function (t) { return this.getParentUIElement(t) || this._domToViewMapping.get(t); };
            rr.prototype.findCorrespondingViewText = function (t) { if (Fo(t))
                return null; var e = this.getParentUIElement(t); if (e)
                return e; var n = t.previousSibling; if (n) {
                if (!this.isElement(n))
                    return null;
                var t_62 = this.mapDomToView(n);
                if (t_62) {
                    return t_62.nextSibling instanceof fi ? t_62.nextSibling : null;
                }
            }
            else {
                var e_66 = this.mapDomToView(t.parentNode);
                if (e_66) {
                    var t_63 = e_66.getChild(0);
                    return t_63 instanceof fi ? t_63 : null;
                }
            } return null; };
            rr.prototype.mapViewToDom = function (t) { return this._viewToDomMapping.get(t); };
            rr.prototype.findCorrespondingDomText = function (t) { var e = t.previousSibling; return e && this.mapViewToDom(e) ? this.mapViewToDom(e).nextSibling : !e && t.parent && this.mapViewToDom(t.parent) ? this.mapViewToDom(t.parent).childNodes[0] : null; };
            rr.prototype.focus = function (t) { var e = this.mapViewToDom(t); if (e && e.ownerDocument.activeElement !== e) {
                var _j = nr.window, t_64 = _j.scrollX, n_57 = _j.scrollY, i_34 = [];
                ar(e, function (t) { var e = t.scrollLeft, n = t.scrollTop; i_34.push([e, n]); }), e.focus(), ar(e, function (t) { var _j = i_34.shift(), e = _j[0], n = _j[1]; t.scrollLeft = e, t.scrollTop = n; }), nr.window.scrollTo(t_64, n_57);
            } };
            rr.prototype.isElement = function (t) { return t && t.nodeType == Node.ELEMENT_NODE; };
            rr.prototype.isDocumentFragment = function (t) { return t && t.nodeType == Node.DOCUMENT_FRAGMENT_NODE; };
            rr.prototype.isComment = function (t) { return t && t.nodeType == Node.COMMENT_NODE; };
            rr.prototype.isDomSelectionBackward = function (t) { if (t.isCollapsed)
                return !1; var e = document.createRange(); e.setStart(t.anchorNode, t.anchorOffset), e.setEnd(t.focusNode, t.focusOffset); var n = e.collapsed; return e.detach(), n; };
            rr.prototype.getParentUIElement = function (t) { var e = or(t); for (e.pop(); e.length;) {
                var t_65 = e.pop(), n_58 = this._domToViewMapping.get(t_65);
                if (n_58 && n_58.is("uiElement"))
                    return n_58;
            } return null; };
            rr.prototype.isDomSelectionCorrect = function (t) { return this._isDomSelectionPositionCorrect(t.anchorNode, t.anchorOffset) && this._isDomSelectionPositionCorrect(t.focusNode, t.focusOffset); };
            rr.prototype._isDomSelectionPositionCorrect = function (t, e) { if (Do(t) && Bo(t) && e < Vo)
                return !1; if (this.isElement(t) && Bo(t.childNodes[e]))
                return !1; var n = this.mapDomToView(t); return !n || !n.is("uiElement"); };
            rr.prototype._processDataFromViewText = function (t) {
                var _this = this;
                var e = t.data;
                if (t.getAncestors().some(function (t) { return _this.preElements.includes(t.name); }))
                    return e;
                if (" " == e.charAt(0)) {
                    var n_59 = this._getTouchingViewTextNode(t, !1);
                    !(n_59 && this._nodeEndsWithSpace(n_59)) && n_59 || (e = " " + e.substr(1));
                }
                if (" " == e.charAt(e.length - 1)) {
                    this._getTouchingViewTextNode(t, !0) || (e = e.substr(0, e.length - 1) + " ");
                }
                return e.replace(/ {2}/g, "  ");
            };
            rr.prototype._nodeEndsWithSpace = function (t) {
                var _this = this;
                if (t.getAncestors().some(function (t) { return _this.preElements.includes(t.name); }))
                    return !1;
                var e = this._processDataFromViewText(t);
                return " " == e.charAt(e.length - 1);
            };
            rr.prototype._processDataFromDomText = function (t) { var e = t.data; if (sr(t, this.preElements))
                return Uo(t); e = e.replace(/[ \n\t\r]{1,}/g, " "); var n = this._getTouchingInlineDomNode(t, !1), i = this._getTouchingInlineDomNode(t, !0), o = this._checkShouldLeftTrimDomText(n), r = this._checkShouldRightTrimDomText(t, i); return o && (e = e.replace(/^ /, "")), r && (e = e.replace(/ $/, "")), e = (e = Uo(new Text(e))).replace(/ \u00A0/g, "  "), o && (e = e.replace(/^\u00A0/, " ")), Do(i) && " " != i.data.charAt(0) || (e = e.replace(/\u00A0( *)$/, " $1")), e; };
            rr.prototype._checkShouldLeftTrimDomText = function (t) { return !t || (!!Wn(t) || /[^\S\u00A0]/.test(t.data.charAt(t.data.length - 1))); };
            rr.prototype._checkShouldRightTrimDomText = function (t, e) { return !e && !Bo(t); };
            rr.prototype._getTouchingViewTextNode = function (t, e) { var n = new Ji({ startPosition: e ? Zi._createAfter(t) : Zi._createBefore(t), direction: e ? "forward" : "backward" }); for (var _j = 0, n_60 = n; _j < n_60.length; _j++) {
                var t_66 = n_60[_j];
                if (t_66.item.is("containerElement"))
                    return null;
                if (t_66.item.is("br"))
                    return null;
                if (t_66.item.is("textProxy"))
                    return t_66.item;
            } return null; };
            rr.prototype._getTouchingInlineDomNode = function (t, e) { if (!t.parentNode)
                return null; var n = e ? "nextNode" : "previousNode", i = t.ownerDocument, o = or(t)[0], r = i.createTreeWalker(o, NodeFilter.SHOW_TEXT | NodeFilter.SHOW_ELEMENT, { acceptNode: function (t) { return Do(t) ? NodeFilter.FILTER_ACCEPT : "BR" == t.tagName ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_SKIP; } }); r.currentNode = t; var s = r[n](); if (null !== s) {
                var e_67 = function (t, e) { var n = or(t), i = or(e); var o = 0; for (; n[o] == i[o] && n[o];)
                    o++; return 0 === o ? null : n[o - 1]; }(t, s);
                if (e_67 && !sr(t, this.blockElements, e_67) && !sr(s, this.blockElements, e_67))
                    return s;
            } return null; };
            return rr;
        }());
        function sr(t, e, n) { var i = or(t); return n && (i = i.slice(i.indexOf(n) + 1)), i.some(function (t) { return t.tagName && e.includes(t.tagName.toLowerCase()); }); }
        function ar(t, e) { for (; t && t != nr.document;)
            e(t), t = t.parentNode; }
        function cr(t) { var e = Object.prototype.toString.apply(t); return "[object Window]" == e || "[object global]" == e; }
        var lr = Li({}, ei, { listenTo: function (t) {
                var e = [];
                for (var _j = 1; _j < arguments.length; _j++) {
                    e[_j - 1] = arguments[_j];
                }
                var _k;
                if (Zo(t) || cr(t)) {
                    var n_61 = this._getProxyEmitter(t) || new dr(t);
                    n_61.attach.apply(n_61, e), t = n_61;
                }
                (_k = ei.listenTo).call.apply(_k, [this, t].concat(e));
            }, stopListening: function (t, e, n) { if (Zo(t) || cr(t)) {
                var e_68 = this._getProxyEmitter(t);
                if (!e_68)
                    return;
                t = e_68;
            } ei.stopListening.call(this, t, e, n), t instanceof dr && t.detach(e); }, _getProxyEmitter: function (t) { return function (t, e) { return t[Xn] && t[Xn][e] ? t[Xn][e].emitter : null; }(this, ur(t)); } });
        var dr = /** @class */ (function () {
            function dr(t) {
                ni(this, ur(t)), this._domNode = t;
            }
            return dr;
        }());
        function ur(t) { return t["data-ck-expando"] || (t["data-ck-expando"] = Jn()); }
        Li(dr.prototype, ei, { attach: function (t, e, n) {
                if (n === void 0) { n = {}; }
                if (this._domListeners && this._domListeners[t])
                    return;
                var i = this._createDomListener(t, !!n.useCapture);
                this._domNode.addEventListener(t, i, !!n.useCapture), this._domListeners || (this._domListeners = {}), this._domListeners[t] = i;
            }, detach: function (t) { var e; !this._domListeners[t] || (e = this._events[t]) && e.callbacks.length || this._domListeners[t].removeListener(); }, _createDomListener: function (t, e) {
                var _this = this;
                var n = function (e) { _this.fire(t, e); };
                return n.removeListener = (function () { _this._domNode.removeEventListener(t, n, e), delete _this._domListeners[t]; }), n;
            } });
        var hr = /** @class */ (function () {
            function hr(t) {
                this.view = t, this.document = t.document, this.isEnabled = !1;
            }
            hr.prototype.enable = function () { this.isEnabled = !0; };
            hr.prototype.disable = function () { this.isEnabled = !1; };
            hr.prototype.destroy = function () { this.disable(), this.stopListening(); };
            return hr;
        }());
        ci(hr, lr);
        var fr = "__lodash_hash_undefined__";
        var mr = function (t) { return this.__data__.set(t, fr), this; };
        var gr = function (t) { return this.__data__.has(t); };
        function pr(t) { var e = -1, n = null == t ? 0 : t.length; for (this.__data__ = new Pt; ++e < n;)
            this.add(t[e]); }
        pr.prototype.add = pr.prototype.push = mr, pr.prototype.has = gr;
        var br = pr;
        var wr = function (t, e) { for (var n = -1, i = null == t ? 0 : t.length; ++n < i;)
            if (e(t[n], n, t))
                return !0; return !1; };
        var _r = function (t, e) { return t.has(e); }, kr = 1, vr = 2;
        var yr = function (t, e, n, i, o, r) { var s = n & kr, a = t.length, c = e.length; if (a != c && !(s && c > a))
            return !1; var l = r.get(t); if (l && r.get(e))
            return l == e; var d = -1, u = !0, h = n & vr ? new br : void 0; for (r.set(t, e), r.set(e, t); ++d < a;) {
            var f = t[d], m = e[d];
            if (i)
                var g = s ? i(m, f, d, e, t, r) : i(f, m, d, t, e, r);
            if (void 0 !== g) {
                if (g)
                    continue;
                u = !1;
                break;
            }
            if (h) {
                if (!wr(e, function (t, e) { if (!_r(h, e) && (f === t || o(f, t, n, i, r)))
                    return h.push(e); })) {
                    u = !1;
                    break;
                }
            }
            else if (f !== m && !o(f, m, n, i, r)) {
                u = !1;
                break;
            }
        } return r.delete(t), r.delete(e), u; };
        var xr = function (t) { var e = -1, n = Array(t.size); return t.forEach(function (t, i) { n[++e] = [i, t]; }), n; };
        var Ar = function (t) { var e = -1, n = Array(t.size); return t.forEach(function (t) { n[++e] = t; }), n; }, Cr = 1, Tr = 2, Pr = "[object Boolean]", Mr = "[object Date]", Er = "[object Error]", Sr = "[object Map]", Ir = "[object Number]", Nr = "[object RegExp]", Or = "[object Set]", Rr = "[object String]", Dr = "[object Symbol]", Lr = "[object ArrayBuffer]", jr = "[object DataView]", Vr = o ? o.prototype : void 0, zr = Vr ? Vr.valueOf : void 0;
        var Br = function (t, e, n, i, o, r, s) { switch (n) {
            case jr:
                if (t.byteLength != e.byteLength || t.byteOffset != e.byteOffset)
                    return !1;
                t = t.buffer, e = e.buffer;
            case Lr: return !(t.byteLength != e.byteLength || !r(new Ye(t), new Ye(e)));
            case Pr:
            case Mr:
            case Ir: return P(+t, +e);
            case Er: return t.name == e.name && t.message == e.message;
            case Nr:
            case Rr: return t == e + "";
            case Sr: var a = xr;
            case Or:
                var c = i & Cr;
                if (a || (a = Ar), t.size != e.size && !c)
                    return !1;
                var l = s.get(t);
                if (l)
                    return l == e;
                i |= Tr, s.set(t, e);
                var d = yr(a(t), a(e), i, o, r, s);
                return s.delete(t), d;
            case Dr: if (zr)
                return zr.call(t) == zr.call(e);
        } return !1; }, Fr = 1, Ur = Object.prototype.hasOwnProperty;
        var Hr = function (t, e, n, i, o, r) { var s = n & Fr, a = Ie(t), c = a.length; if (c != Ie(e).length && !s)
            return !1; for (var l = c; l--;) {
            var d = a[l];
            if (!(s ? d in e : Ur.call(e, d)))
                return !1;
        } var u = r.get(t); if (u && r.get(e))
            return u == e; var h = !0; r.set(t, e), r.set(e, t); for (var f = s; ++l < c;) {
            var m = t[d = a[l]], g = e[d];
            if (i)
                var p = s ? i(g, m, d, e, t, r) : i(m, g, d, t, e, r);
            if (!(void 0 === p ? m === g || o(m, g, n, i, r) : p)) {
                h = !1;
                break;
            }
            f || (f = "constructor" == d);
        } if (h && !f) {
            var b = t.constructor, w = e.constructor;
            b != w && "constructor" in t && "constructor" in e && !("function" == typeof b && b instanceof b && "function" == typeof w && w instanceof w) && (h = !1);
        } return r.delete(t), r.delete(e), h; }, qr = 1, Wr = "[object Arguments]", Yr = "[object Array]", $r = "[object Object]", Gr = Object.prototype.hasOwnProperty;
        var Qr = function (t, e, n, i, o, r) { var s = Wt(t), a = Wt(e), c = s ? Yr : He(t), l = a ? Yr : He(e), d = (c = c == Wr ? $r : c) == $r, u = (l = l == Wr ? $r : l) == $r, h = c == l; if (h && Object(Yt.a)(t)) {
            if (!Object(Yt.a)(e))
                return !1;
            s = !0, d = !1;
        } if (h && !d)
            return r || (r = new It), s || ie(t) ? yr(t, e, n, i, o, r) : Br(t, e, c, n, i, o, r); if (!(n & qr)) {
            var f = d && Gr.call(t, "__wrapped__"), m = u && Gr.call(e, "__wrapped__");
            if (f || m) {
                var g = f ? t.value() : t, p = m ? e.value() : e;
                return r || (r = new It), o(g, p, n, i, r);
            }
        } return !!h && (r || (r = new It), Hr(t, e, n, i, o, r)); };
        var Kr = function t(e, n, i, o, r) { return e === n || (null == e || null == n || !w(e) && !w(n) ? e != e && n != n : Qr(e, n, i, o, t, r)); };
        var Jr = function (t, e, n) { var i = (n = "function" == typeof n ? n : void 0) ? n(t, e) : void 0; return void 0 === i ? Kr(t, e, void 0, n) : !!i; };
        var Zr = /** @class */ (function (_super) {
            __extends(Zr, _super);
            function Zr(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this._config = { childList: !0, characterData: !0, characterDataOldValue: !0, subtree: !0 }, _this.domConverter = t.domConverter, _this.renderer = t._renderer, _this._domElements = [], _this._mutationObserver = new window.MutationObserver(_this._onMutations.bind(_this));
                return _this;
            }
            Zr.prototype.flush = function () { this._onMutations(this._mutationObserver.takeRecords()); };
            Zr.prototype.observe = function (t) { this._domElements.push(t), this.isEnabled && this._mutationObserver.observe(t, this._config); };
            Zr.prototype.enable = function () { _super.prototype.enable.call(this); for (var _j = 0, _k = this._domElements; _j < _k.length; _j++) {
                var t_67 = _k[_j];
                this._mutationObserver.observe(t_67, this._config);
            } };
            Zr.prototype.disable = function () { _super.prototype.disable.call(this), this._mutationObserver.disconnect(); };
            Zr.prototype.destroy = function () { _super.prototype.destroy.call(this), this._mutationObserver.disconnect(); };
            Zr.prototype._onMutations = function (t) { if (0 === t.length)
                return; var e = this.domConverter, n = new Map, i = new Set; for (var _j = 0, t_68 = t; _j < t_68.length; _j++) {
                var n_62 = t_68[_j];
                if ("childList" === n_62.type) {
                    var t_70 = e.mapDomToView(n_62.target);
                    if (t_70 && t_70.is("uiElement"))
                        continue;
                    t_70 && !this._isBogusBrMutation(n_62) && i.add(t_70);
                }
            } for (var _k = 0, t_69 = t; _k < t_69.length; _k++) {
                var o_25 = t_69[_k];
                var t_71 = e.mapDomToView(o_25.target);
                if ((!t_71 || !t_71.is("uiElement")) && "characterData" === o_25.type) {
                    var t_72 = e.findCorrespondingViewText(o_25.target);
                    t_72 && !i.has(t_72.parent) ? n.set(t_72, { type: "text", oldText: t_72.data, newText: Uo(o_25.target), node: t_72 }) : !t_72 && Bo(o_25.target) && i.add(e.mapDomToView(o_25.target.parentNode));
                }
            } var o = []; for (var _q = 0, _v = n.values(); _q < _v.length; _q++) {
                var t_73 = _v[_q];
                this.renderer.markToSync("text", t_73.node), o.push(t_73);
            } for (var _w = 0, i_35 = i; _w < i_35.length; _w++) {
                var t_74 = i_35[_w];
                var n_63 = e.mapViewToDom(t_74), i_36 = Array.from(t_74.getChildren()), r_10 = Array.from(e.domChildrenToView(n_63, { withChildren: !1 }));
                Jr(i_36, r_10, a) || (this.renderer.markToSync("children", t_74), o.push({ type: "children", oldChildren: i_36, newChildren: r_10, node: t_74 }));
            } var r = t[0].target.ownerDocument.getSelection(); var s = null; if (r && r.anchorNode) {
                var t_75 = e.domPositionToView(r.anchorNode, r.anchorOffset), n_64 = e.domPositionToView(r.focusNode, r.focusOffset);
                t_75 && n_64 && (s = new no(t_75)).setFocus(n_64);
            } function a(t, e) { if (!Array.isArray(t))
                return t === e || !(!t.is("text") || !e.is("text")) && t.data === e.data; } this.document.fire("mutations", o, s), this.view.forceRender(); };
            Zr.prototype._isBogusBrMutation = function (t) { var e = null; return null === t.nextSibling && 0 === t.removedNodes.length && 1 == t.addedNodes.length && (e = this.domConverter.domToView(t.addedNodes[0], { withChildren: !1 })), e && e.is("element", "br"); };
            return Zr;
        }(hr));
        var Xr = /** @class */ (function () {
            function Xr(t, e, n) {
                this.view = t, this.document = t.document, this.domEvent = e, this.domTarget = e.target, Li(this, n);
            }
            Object.defineProperty(Xr.prototype, "target", {
                get: function () { return this.view.domConverter.mapDomToView(this.domTarget); },
                enumerable: true,
                configurable: true
            });
            Xr.prototype.preventDefault = function () { this.domEvent.preventDefault(); };
            Xr.prototype.stopPropagation = function () { this.domEvent.stopPropagation(); };
            return Xr;
        }());
        var ts = /** @class */ (function (_super) {
            __extends(ts, _super);
            function ts(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.useCapture = !1;
                return _this;
            }
            ts.prototype.observe = function (t) {
                var _this = this;
                ("string" == typeof this.domEventType ? [this.domEventType] : this.domEventType).forEach(function (e) { _this.listenTo(t, e, function (t, e) { _this.isEnabled && _this.onDomEvent(e); }, { useCapture: _this.useCapture }); });
            };
            ts.prototype.fire = function (t, e, n) { this.isEnabled && this.document.fire(t, new Xr(this.view, e, n)); };
            return ts;
        }(hr));
        var es = /** @class */ (function (_super) {
            __extends(es, _super);
            function es(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.domEventType = ["keydown", "keyup"];
                return _this;
            }
            es.prototype.onDomEvent = function (t) { this.fire(t.type, t, { keyCode: t.keyCode, altKey: t.altKey, ctrlKey: t.ctrlKey || t.metaKey, shiftKey: t.shiftKey, get keystroke() { return wo(this); } }); };
            return es;
        }(ts));
        var ns = function () { return i.a.Date.now(); }, is = "[object Symbol]";
        var os = function (t) { return "symbol" == typeof t || w(t) && g(t) == is; }, rs = NaN, ss = /^\s+|\s+$/g, as = /^[-+]0x[0-9a-f]+$/i, cs = /^0b[01]+$/i, ls = /^0o[0-7]+$/i, ds = parseInt;
        var us = function (t) { if ("number" == typeof t)
            return t; if (os(t))
            return rs; if (B(t)) {
            var e = "function" == typeof t.valueOf ? t.valueOf() : t;
            t = B(e) ? e + "" : e;
        } if ("string" != typeof t)
            return 0 === t ? t : +t; t = t.replace(ss, ""); var n = cs.test(t); return n || ls.test(t) ? ds(t.slice(2), n ? 2 : 8) : as.test(t) ? rs : +t; }, hs = "Expected a function", fs = Math.max, ms = Math.min;
        var gs = function (t, e, n) { var i, o, r, s, a, c, l = 0, d = !1, u = !1, h = !0; if ("function" != typeof t)
            throw new TypeError(hs); function f(e) { var n = i, r = o; return i = o = void 0, l = e, s = t.apply(r, n); } function m(t) { var n = t - c; return void 0 === c || n >= e || n < 0 || u && t - l >= r; } function g() { var t = ns(); if (m(t))
            return p(t); a = setTimeout(g, function (t) { var n = e - (t - c); return u ? ms(n, r - (t - l)) : n; }(t)); } function p(t) { return a = void 0, h && i ? f(t) : (i = o = void 0, s); } function b() { var t = ns(), n = m(t); if (i = arguments, o = this, c = t, n) {
            if (void 0 === a)
                return function (t) { return l = t, a = setTimeout(g, e), d ? f(t) : s; }(c);
            if (u)
                return a = setTimeout(g, e), f(c);
        } return void 0 === a && (a = setTimeout(g, e)), s; } return e = us(e) || 0, B(n) && (d = !!n.leading, r = (u = "maxWait" in n) ? fs(us(n.maxWait) || 0, e) : r, h = "trailing" in n ? !!n.trailing : h), b.cancel = function () { void 0 !== a && clearTimeout(a), l = 0, i = c = o = a = void 0; }, b.flush = function () { return void 0 === a ? s : p(ns()); }, b; };
        var ps = /** @class */ (function (_super) {
            __extends(ps, _super);
            function ps(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this._fireSelectionChangeDoneDebounced = gs(function (t) { return _this.document.fire("selectionChangeDone", t); }, 200);
                return _this;
            }
            ps.prototype.observe = function () {
                var _this = this;
                var t = this.document;
                t.on("keydown", function (e, n) { t.selection.isFake && function (t) { return t == bo.arrowright || t == bo.arrowleft || t == bo.arrowup || t == bo.arrowdown; }(n.keyCode) && _this.isEnabled && (n.preventDefault(), _this._handleSelectionMove(n.keyCode)); }, { priority: "lowest" });
            };
            ps.prototype.destroy = function () { _super.prototype.destroy.call(this), this._fireSelectionChangeDoneDebounced.cancel(); };
            ps.prototype._handleSelectionMove = function (t) { var e = this.document.selection, n = new no(e.getRanges(), { backward: e.isBackward, fake: !1 }); t != bo.arrowleft && t != bo.arrowup || n.setTo(n.getFirstPosition()), t != bo.arrowright && t != bo.arrowdown || n.setTo(n.getLastPosition()); var i = { oldSelection: e, newSelection: n, domSelection: null }; this.document.fire("selectionChange", i), this._fireSelectionChangeDoneDebounced(i); };
            return ps;
        }(hr));
        var bs = n(1);
        var ws = /** @class */ (function (_super) {
            __extends(ws, _super);
            function ws(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.mutationObserver = t.getObserver(Zr), _this.selection = _this.document.selection, _this.domConverter = t.domConverter, _this._documents = new WeakSet, _this._fireSelectionChangeDoneDebounced = gs(function (t) { return _this.document.fire("selectionChangeDone", t); }, 200), _this._clearInfiniteLoopInterval = setInterval(function () { return _this._clearInfiniteLoop(); }, 1e3), _this._loopbackCounter = 0;
                return _this;
            }
            ws.prototype.observe = function (t) {
                var _this = this;
                var e = t.ownerDocument;
                this._documents.has(e) || (this.listenTo(e, "selectionchange", function () { _this._handleSelectionChange(e); }), this._documents.add(e));
            };
            ws.prototype.destroy = function () { _super.prototype.destroy.call(this), clearInterval(this._clearInfiniteLoopInterval), this._fireSelectionChangeDoneDebounced.cancel(); };
            ws.prototype._handleSelectionChange = function (t) { if (!this.isEnabled || !this.document.isFocused && !this.document.isReadOnly)
                return; this.mutationObserver.flush(); var e = t.defaultView.getSelection(), n = this.domConverter.domSelectionToView(e); if (!this.selection.isEqual(n) || !this.domConverter.isDomSelectionCorrect(e))
                if (++this._loopbackCounter > 60)
                    bs.a.warn("selectionchange-infinite-loop: Selection change observer detected an infinite rendering loop.");
                else if (this.selection.isSimilar(n))
                    this.view.forceRender();
                else {
                    var t_76 = { oldSelection: this.selection, newSelection: n, domSelection: e };
                    this.document.fire("selectionChange", t_76), this._fireSelectionChangeDoneDebounced(t_76);
                } };
            ws.prototype._clearInfiniteLoop = function () { this._loopbackCounter = 0; };
            return ws;
        }(hr));
        var _s = /** @class */ (function (_super) {
            __extends(_s, _super);
            function _s(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.domEventType = ["focus", "blur"], _this.useCapture = !0;
                var e = _this.document;
                e.on("focus", function () { e.isFocused = !0, _this._renderTimeoutId = setTimeout(function () { return t.forceRender(); }, 50); }), e.on("blur", function (n, i) { var o = e.selection.editableElement; null !== o && o !== i.target || (e.isFocused = !1, t.forceRender()); });
                return _this;
            }
            _s.prototype.onDomEvent = function (t) { this.fire(t.type, t); };
            _s.prototype.destroy = function () { this._renderTimeoutId && clearTimeout(this._renderTimeoutId), _super.prototype.destroy.call(this); };
            return _s;
        }(ts));
        var ks = /** @class */ (function (_super) {
            __extends(ks, _super);
            function ks(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.domEventType = ["compositionstart", "compositionupdate", "compositionend"];
                var e = _this.document;
                e.on("compositionstart", function () { e.isComposing = !0; }), e.on("compositionend", function () { e.isComposing = !1; });
                return _this;
            }
            ks.prototype.onDomEvent = function (t) { this.fire(t.type, t); };
            return ks;
        }(ts));
        function vs(t) { return "[object Range]" == Object.prototype.toString.apply(t); }
        function ys(t) { var e = t.ownerDocument.defaultView.getComputedStyle(t); return { top: parseInt(e.borderTopWidth, 10), right: parseInt(e.borderRightWidth, 10), bottom: parseInt(e.borderBottomWidth, 10), left: parseInt(e.borderLeftWidth, 10) }; }
        var xs = ["top", "right", "bottom", "left", "width", "height"];
        var As = /** @class */ (function () {
            function As(t) {
                var e = vs(t);
                if (Object.defineProperty(this, "_source", { value: t._source || t, writable: !0, enumerable: !1 }), Wn(t) || e) {
                    var n_65 = e ? t.startContainer : t;
                    n_65.ownerDocument && n_65.ownerDocument.body.contains(n_65) || bs.a.warn("rect-source-not-in-dom: The source of this rect does not belong to any rendered DOM tree.", { source: t }), Cs(this, e ? As.getDomRangeRects(t)[0] : t.getBoundingClientRect());
                }
                else if (cr(t)) {
                    var e_69 = t.innerWidth, n_66 = t.innerHeight;
                    Cs(this, { top: 0, right: e_69, bottom: n_66, left: 0, width: e_69, height: n_66 });
                }
                else
                    Cs(this, t);
            }
            As.prototype.clone = function () { return new As(this); };
            As.prototype.moveTo = function (t, e) { return this.top = e, this.right = t + this.width, this.bottom = e + this.height, this.left = t, this; };
            As.prototype.moveBy = function (t, e) { return this.top += e, this.right += t, this.left += t, this.bottom += e, this; };
            As.prototype.getIntersection = function (t) { var e = { top: Math.max(this.top, t.top), right: Math.min(this.right, t.right), bottom: Math.min(this.bottom, t.bottom), left: Math.max(this.left, t.left) }; return e.width = e.right - e.left, e.height = e.bottom - e.top, e.width < 0 || e.height < 0 ? null : new As(e); };
            As.prototype.getIntersectionArea = function (t) { var e = this.getIntersection(t); return e ? e.getArea() : 0; };
            As.prototype.getArea = function () { return this.width * this.height; };
            As.prototype.getVisible = function () { var t = this._source; var e = this.clone(); if (!Ts(t)) {
                var n_67 = t.parentNode || t.commonAncestorContainer;
                for (; n_67 && !Ts(n_67);) {
                    var t_77 = new As(n_67), i_37 = e.getIntersection(t_77);
                    if (!i_37)
                        return null;
                    i_37.getArea() < e.getArea() && (e = i_37), n_67 = n_67.parentNode;
                }
            } return e; };
            As.prototype.isEqual = function (t) { for (var _j = 0, xs_1 = xs; _j < xs_1.length; _j++) {
                var e_70 = xs_1[_j];
                if (this[e_70] !== t[e_70])
                    return !1;
            } return !0; };
            As.prototype.contains = function (t) { var e = this.getIntersection(t); return !(!e || !e.isEqual(t)); };
            As.prototype.excludeScrollbarsAndBorders = function () { var t = this._source; var e, n; if (cr(t))
                e = t.innerWidth - t.document.documentElement.clientWidth, n = t.innerHeight - t.document.documentElement.clientHeight;
            else {
                var i_38 = ys(this._source);
                e = t.offsetWidth - t.clientWidth, n = t.offsetHeight - t.clientHeight, this.moveBy(i_38.left, i_38.top);
            } return this.width -= e, this.right -= e, this.height -= n, this.bottom -= n, this; };
            As.getDomRangeRects = function (t) { var e = [], n = Array.from(t.getClientRects()); if (n.length)
                for (var _j = 0, n_68 = n; _j < n_68.length; _j++) {
                    var t_78 = n_68[_j];
                    e.push(new As(t_78));
                }
            else {
                var n_69 = t.startContainer;
                Do(n_69) && (n_69 = n_69.parentNode);
                var i_39 = new As(n_69.getBoundingClientRect());
                i_39.right = i_39.left, i_39.width = 0, e.push(i_39);
            } return e; };
            return As;
        }());
        function Cs(t, e) { for (var _j = 0, xs_2 = xs; _j < xs_2.length; _j++) {
            var n_70 = xs_2[_j];
            t[n_70] = e[n_70];
        } }
        function Ts(t) { return !!Wn(t) && t === t.ownerDocument.body; }
        function Ps(_j) {
            var t = _j.target, _k = _j.viewportOffset, e = _k === void 0 ? 0 : _k;
            var n = Rs(t);
            var i = n, o = null;
            for (; i;) {
                var r_11 = void 0;
                Es(r_11 = Ds(i == n ? t : o), function () { return Ls(t, i); });
                var s_9 = Ls(t, i);
                if (Ms(i, s_9, e), i.parent != i) {
                    if (o = i.frameElement, i = i.parent, !o)
                        return;
                }
                else
                    i = null;
            }
        }
        function Ms(t, e, n) { var i = e.clone().moveBy(0, n), o = e.clone().moveBy(0, -n), r = new As(t).excludeScrollbarsAndBorders(); if (![o, i].every(function (t) { return r.contains(t); })) {
            var s_10 = t.scrollX, a_6 = t.scrollY;
            Is(o, r) ? a_6 -= r.top - e.top + n : Ss(i, r) && (a_6 += e.bottom - r.bottom + n), Ns(e, r) ? s_10 -= r.left - e.left + n : Os(e, r) && (s_10 += e.right - r.right + n), t.scrollTo(s_10, a_6);
        } }
        function Es(t, e) { var n = Rs(t); var i, o; for (; t != n.document.body;)
            o = e(), (i = new As(t).excludeScrollbarsAndBorders()).contains(o) || (Is(o, i) ? t.scrollTop -= i.top - o.top : Ss(o, i) && (t.scrollTop += o.bottom - i.bottom), Ns(o, i) ? t.scrollLeft -= i.left - o.left : Os(o, i) && (t.scrollLeft += o.right - i.right)), t = t.parentNode; }
        function Ss(t, e) { return t.bottom > e.bottom; }
        function Is(t, e) { return t.top < e.top; }
        function Ns(t, e) { return t.left < e.left; }
        function Os(t, e) { return t.right > e.right; }
        function Rs(t) { return vs(t) ? t.startContainer.ownerDocument.defaultView : t.ownerDocument.defaultView; }
        function Ds(t) { if (vs(t)) {
            var e_71 = t.commonAncestorContainer;
            return Do(e_71) && (e_71 = e_71.parentNode), e_71;
        } return t.parentNode; }
        function Ls(t, e) { var n = Rs(t), i = new As(t); if (n === e)
            return i; {
            var t_79 = n;
            for (; t_79 != e;) {
                var e_72 = t_79.frameElement, n_71 = new As(e_72).excludeScrollbarsAndBorders();
                i.moveBy(n_71.left, n_71.top), t_79 = t_79.parent;
            }
        } return i; }
        Object.assign({}, { scrollViewportToShowTarget: Ps, scrollAncestorsToShowTarget: function (t) { Es(Ds(t), function () { return new As(t); }); } });
        var js = /** @class */ (function () {
            function js() {
                var _this = this;
                this.document = new ro, this.domConverter = new rr, this.domRoots = new Map, this.set("isRenderingInProgress", !1), this._renderer = new Xo(this.domConverter, this.document.selection), this._renderer.bind("isFocused").to(this.document), this._initialDomRootAttributes = new WeakMap, this._observers = new Map, this._ongoingChange = !1, this._postFixersInProgress = !1, this._renderingDisabled = !1, this._hasChangedSinceTheLastRendering = !1, this._writer = new Co(this.document), this.addObserver(Zr), this.addObserver(ws), this.addObserver(_s), this.addObserver(es), this.addObserver(ps), this.addObserver(ks), function (t) { t.document.on("keydown", Wo); }(this), yo(this), this.on("render", function () { _this._render(), _this.document.fire("layoutChanged"), _this._hasChangedSinceTheLastRendering = !1; }), this.listenTo(this.document.selection, "change", function () { _this._hasChangedSinceTheLastRendering = !0; });
            }
            js.prototype.attachDomRoot = function (t, e) {
                var _this = this;
                if (e === void 0) { e = "main"; }
                var n = this.document.getRoot(e);
                n._name = t.tagName.toLowerCase();
                var i = {};
                for (var _j = 0, _k = Array.from(t.attributes); _j < _k.length; _j++) {
                    var _q = _k[_j], e_73 = _q.name, o_26 = _q.value;
                    i[e_73] = o_26, "class" === e_73 ? this._writer.addClass(o_26.split(" "), n) : this._writer.setAttribute(e_73, o_26, n);
                }
                this._initialDomRootAttributes.set(t, i);
                var o = function () { _this._writer.setAttribute("contenteditable", !n.isReadOnly, n), n.isReadOnly ? _this._writer.addClass("ck-read-only", n) : _this._writer.removeClass("ck-read-only", n); };
                o(), this.domRoots.set(e, t), this.domConverter.bindElements(t, n), this._renderer.markToSync("children", n), this._renderer.markToSync("attributes", n), this._renderer.domDocuments.add(t.ownerDocument), n.on("change:children", function (t, e) { return _this._renderer.markToSync("children", e); }), n.on("change:attributes", function (t, e) { return _this._renderer.markToSync("attributes", e); }), n.on("change:text", function (t, e) { return _this._renderer.markToSync("text", e); }), n.on("change:isReadOnly", function () { return _this.change(o); }), n.on("change", function () { _this._hasChangedSinceTheLastRendering = !0; });
                for (var _v = 0, _w = this._observers.values(); _v < _w.length; _v++) {
                    var n_72 = _w[_v];
                    n_72.observe(t, e);
                }
            };
            js.prototype.detachDomRoot = function (t) { var e = this.domRoots.get(t); Array.from(e.attributes).forEach(function (_j) {
                var t = _j.name;
                return e.removeAttribute(t);
            }); var n = this._initialDomRootAttributes.get(e); for (var t_80 in n)
                e.setAttribute(t_80, n[t_80]); this.domRoots.delete(t), this.domConverter.unbindDomElement(e); };
            js.prototype.getDomRoot = function (t) {
                if (t === void 0) { t = "main"; }
                return this.domRoots.get(t);
            };
            js.prototype.addObserver = function (t) { var e = this._observers.get(t); if (e)
                return e; e = new t(this), this._observers.set(t, e); for (var _j = 0, _k = this.domRoots; _j < _k.length; _j++) {
                var _q = _k[_j], t_81 = _q[0], n_73 = _q[1];
                e.observe(n_73, t_81);
            } return e.enable(), e; };
            js.prototype.getObserver = function (t) { return this._observers.get(t); };
            js.prototype.disableObservers = function () { for (var _j = 0, _k = this._observers.values(); _j < _k.length; _j++) {
                var t_82 = _k[_j];
                t_82.disable();
            } };
            js.prototype.enableObservers = function () { for (var _j = 0, _k = this._observers.values(); _j < _k.length; _j++) {
                var t_83 = _k[_j];
                t_83.enable();
            } };
            js.prototype.scrollToTheSelection = function () { var t = this.document.selection.getFirstRange(); t && Ps({ target: this.domConverter.viewRangeToDom(t), viewportOffset: 20 }); };
            js.prototype.focus = function () { if (!this.document.isFocused) {
                var t_84 = this.document.selection.editableElement;
                t_84 ? (this.domConverter.focus(t_84), this.forceRender()) : bs.a.warn("view-focus-no-selection: There is no selection in any editable to focus.");
            } };
            js.prototype.change = function (t) { if (this.isRenderingInProgress || this._postFixersInProgress)
                throw new Gn.b("cannot-change-view-tree: Attempting to make changes to the view when it is in an incorrect state: rendering or post-fixers are in progress. This may cause some unexpected behavior and inconsistency between the DOM and the view."); if (this._ongoingChange)
                return t(this._writer); this._ongoingChange = !0; var e = t(this._writer); return this._ongoingChange = !1, !this._renderingDisabled && this._hasChangedSinceTheLastRendering && (this._postFixersInProgress = !0, this.document._callPostFixers(this._writer), this._postFixersInProgress = !1, this.fire("render")), e; };
            js.prototype.forceRender = function () { this._hasChangedSinceTheLastRendering = !0, this.change(function () { }); };
            js.prototype.destroy = function () { for (var _j = 0, _k = this._observers.values(); _j < _k.length; _j++) {
                var t_85 = _k[_j];
                t_85.destroy();
            } this.document.destroy(), this.stopListening(); };
            js.prototype.createPositionAt = function (t, e) { return Zi._createAt(t, e); };
            js.prototype.createPositionAfter = function (t) { return Zi._createAfter(t); };
            js.prototype.createPositionBefore = function (t) { return Zi._createBefore(t); };
            js.prototype.createRange = function (t, e) { return new Xi(t, e); };
            js.prototype.createRangeOn = function (t) { return Xi._createOn(t); };
            js.prototype.createRangeIn = function (t) { return Xi._createIn(t); };
            js.prototype.createSelection = function (t, e, n) { return new no(t, e, n); };
            js.prototype._disableRendering = function (t) { this._renderingDisabled = t, 0 == t && this.change(function () { }); };
            js.prototype._render = function () { this.isRenderingInProgress = !0, this.disableObservers(), this._renderer.render(), this.enableObservers(), this.isRenderingInProgress = !1; };
            return js;
        }());
        function Vs(t) { return C(t) ? gi(t) : new Map(t); }
        ci(js, Fi);
        var zs = /** @class */ (function () {
            function zs(t) {
                this.parent = null, this._attrs = Vs(t);
            }
            Object.defineProperty(zs.prototype, "index", {
                get: function () { var t; if (!this.parent)
                    return null; if (null === (t = this.parent.getChildIndex(this)))
                    throw new Gn.b("model-node-not-found-in-parent: The node's parent does not contain this node."); return t; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(zs.prototype, "startOffset", {
                get: function () { var t; if (!this.parent)
                    return null; if (null === (t = this.parent.getChildStartOffset(this)))
                    throw new Gn.b("model-node-not-found-in-parent: The node's parent does not contain this node."); return t; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(zs.prototype, "offsetSize", {
                get: function () { return 1; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(zs.prototype, "endOffset", {
                get: function () { return this.parent ? this.startOffset + this.offsetSize : null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(zs.prototype, "nextSibling", {
                get: function () { var t = this.index; return null !== t && this.parent.getChild(t + 1) || null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(zs.prototype, "previousSibling", {
                get: function () { var t = this.index; return null !== t && this.parent.getChild(t - 1) || null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(zs.prototype, "root", {
                get: function () { var t = this; for (; t.parent;)
                    t = t.parent; return t; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(zs.prototype, "document", {
                get: function () { return this.root == this ? null : this.root.document || null; },
                enumerable: true,
                configurable: true
            });
            zs.prototype.getPath = function () { var t = []; var e = this; for (; e.parent;)
                t.unshift(e.startOffset), e = e.parent; return t; };
            zs.prototype.getAncestors = function (t) {
                if (t === void 0) { t = { includeSelf: !1, parentFirst: !1 }; }
                var e = [];
                var n = t.includeSelf ? this : this.parent;
                for (; n;)
                    e[t.parentFirst ? "push" : "unshift"](n), n = n.parent;
                return e;
            };
            zs.prototype.getCommonAncestor = function (t, e) {
                if (e === void 0) { e = {}; }
                var n = this.getAncestors(e), i = t.getAncestors(e);
                var o = 0;
                for (; n[o] == i[o] && n[o];)
                    o++;
                return 0 === o ? null : n[o - 1];
            };
            zs.prototype.isBefore = function (t) { if (this == t)
                return !1; if (this.root !== t.root)
                return !1; var e = this.getPath(), n = t.getPath(), i = li(e, n); switch (i) {
                case "prefix": return !0;
                case "extension": return !1;
                default: return e[i] < n[i];
            } };
            zs.prototype.isAfter = function (t) { return this != t && (this.root === t.root && !this.isBefore(t)); };
            zs.prototype.hasAttribute = function (t) { return this._attrs.has(t); };
            zs.prototype.getAttribute = function (t) { return this._attrs.get(t); };
            zs.prototype.getAttributes = function () { return this._attrs.entries(); };
            zs.prototype.getAttributeKeys = function () { return this._attrs.keys(); };
            zs.prototype.toJSON = function () { var t = {}; return this._attrs.size && (t.attributes = Array.from(this._attrs).reduce(function (t, e) { return (t[e[0]] = e[1], t); }, {})), t; };
            zs.prototype._clone = function () { return new zs(this._attrs); };
            zs.prototype._remove = function () { this.parent._removeChildren(this.index); };
            zs.prototype._setAttribute = function (t, e) { this._attrs.set(t, e); };
            zs.prototype._setAttributesTo = function (t) { this._attrs = Vs(t); };
            zs.prototype._removeAttribute = function (t) { return this._attrs.delete(t); };
            zs.prototype._clearAttributes = function () { this._attrs.clear(); };
            zs.prototype.is = function (t) { return "node" == t; };
            return zs;
        }());
        var Bs = /** @class */ (function (_super) {
            __extends(Bs, _super);
            function Bs(t, e) {
                var _this = this;
                _this = _super.call(this, e) || this, _this._data = t || "";
                return _this;
            }
            Object.defineProperty(Bs.prototype, "offsetSize", {
                get: function () { return this.data.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Bs.prototype, "data", {
                get: function () { return this._data; },
                enumerable: true,
                configurable: true
            });
            Bs.prototype.is = function (t) { return "text" == t || _super.prototype.is.call(this, t); };
            Bs.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return t.data = this.data, t; };
            Bs.prototype._clone = function () { return new Bs(this.data, this.getAttributes()); };
            Bs.fromJSON = function (t) { return new Bs(t.data, t.attributes); };
            return Bs;
        }(zs));
        var Fs = /** @class */ (function () {
            function Fs(t, e, n) {
                if (this.textNode = t, e < 0 || e > t.offsetSize)
                    throw new Gn.b("model-textproxy-wrong-offsetintext: Given offsetInText value is incorrect.");
                if (n < 0 || e + n > t.offsetSize)
                    throw new Gn.b("model-textproxy-wrong-length: Given length value is incorrect.");
                this.data = t.data.substring(e, e + n), this.offsetInText = e;
            }
            Object.defineProperty(Fs.prototype, "startOffset", {
                get: function () { return null !== this.textNode.startOffset ? this.textNode.startOffset + this.offsetInText : null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Fs.prototype, "offsetSize", {
                get: function () { return this.data.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Fs.prototype, "endOffset", {
                get: function () { return null !== this.startOffset ? this.startOffset + this.offsetSize : null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Fs.prototype, "isPartial", {
                get: function () { return this.offsetSize !== this.textNode.offsetSize; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Fs.prototype, "parent", {
                get: function () { return this.textNode.parent; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Fs.prototype, "root", {
                get: function () { return this.textNode.root; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Fs.prototype, "document", {
                get: function () { return this.textNode.document; },
                enumerable: true,
                configurable: true
            });
            Fs.prototype.is = function (t) { return "textProxy" == t; };
            Fs.prototype.getPath = function () { var t = this.textNode.getPath(); return t.length > 0 && (t[t.length - 1] += this.offsetInText), t; };
            Fs.prototype.getAncestors = function (t) {
                if (t === void 0) { t = { includeSelf: !1, parentFirst: !1 }; }
                var e = [];
                var n = t.includeSelf ? this : this.parent;
                for (; n;)
                    e[t.parentFirst ? "push" : "unshift"](n), n = n.parent;
                return e;
            };
            Fs.prototype.hasAttribute = function (t) { return this.textNode.hasAttribute(t); };
            Fs.prototype.getAttribute = function (t) { return this.textNode.getAttribute(t); };
            Fs.prototype.getAttributes = function () { return this.textNode.getAttributes(); };
            Fs.prototype.getAttributeKeys = function () { return this.textNode.getAttributeKeys(); };
            return Fs;
        }());
        var Us = /** @class */ (function () {
            function Us(t) {
                this._nodes = [], t && this._insertNodes(0, t);
            }
            Us.prototype[Symbol.iterator] = function () { return this._nodes[Symbol.iterator](); };
            Object.defineProperty(Us.prototype, "length", {
                get: function () { return this._nodes.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Us.prototype, "maxOffset", {
                get: function () { return this._nodes.reduce(function (t, e) { return t + e.offsetSize; }, 0); },
                enumerable: true,
                configurable: true
            });
            Us.prototype.getNode = function (t) { return this._nodes[t] || null; };
            Us.prototype.getNodeIndex = function (t) { var e = this._nodes.indexOf(t); return -1 == e ? null : e; };
            Us.prototype.getNodeStartOffset = function (t) { var e = this.getNodeIndex(t); return null === e ? null : this._nodes.slice(0, e).reduce(function (t, e) { return t + e.offsetSize; }, 0); };
            Us.prototype.indexToOffset = function (t) { if (t == this._nodes.length)
                return this.maxOffset; var e = this._nodes[t]; if (!e)
                throw new Gn.b("model-nodelist-index-out-of-bounds: Given index cannot be found in the node list."); return this.getNodeStartOffset(e); };
            Us.prototype.offsetToIndex = function (t) { var e = 0; for (var _j = 0, _k = this._nodes; _j < _k.length; _j++) {
                var n_74 = _k[_j];
                if (t >= e && t < e + n_74.offsetSize)
                    return this.getNodeIndex(n_74);
                e += n_74.offsetSize;
            } if (e != t)
                throw new Gn.b("model-nodelist-offset-out-of-bounds: Given offset cannot be found in the node list.", { offset: t, nodeList: this }); return this.length; };
            Us.prototype._insertNodes = function (t, e) {
                var _j;
                for (var _k = 0, e_74 = e; _k < e_74.length; _k++) {
                    var t_86 = e_74[_k];
                    if (!(t_86 instanceof zs))
                        throw new Gn.b("model-nodelist-insertNodes-not-node: Trying to insert an object which is not a Node instance.");
                }
                (_j = this._nodes).splice.apply(_j, [t, 0].concat(e));
            };
            Us.prototype._removeNodes = function (t, e) {
                if (e === void 0) { e = 1; }
                return this._nodes.splice(t, e);
            };
            Us.prototype.toJSON = function () { return this._nodes.map(function (t) { return t.toJSON(); }); };
            return Us;
        }());
        var Hs = /** @class */ (function (_super) {
            __extends(Hs, _super);
            function Hs(t, e, n) {
                var _this = this;
                _this = _super.call(this, e) || this, _this.name = t, _this._children = new Us, n && _this._insertChild(0, n);
                return _this;
            }
            Object.defineProperty(Hs.prototype, "childCount", {
                get: function () { return this._children.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Hs.prototype, "maxOffset", {
                get: function () { return this._children.maxOffset; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Hs.prototype, "isEmpty", {
                get: function () { return 0 === this.childCount; },
                enumerable: true,
                configurable: true
            });
            Hs.prototype.is = function (t, e) {
                if (e === void 0) { e = null; }
                return e ? "element" == t && e == this.name : "element" == t || t == this.name || _super.prototype.is.call(this, t);
            };
            Hs.prototype.getChild = function (t) { return this._children.getNode(t); };
            Hs.prototype.getChildren = function () { return this._children[Symbol.iterator](); };
            Hs.prototype.getChildIndex = function (t) { return this._children.getNodeIndex(t); };
            Hs.prototype.getChildStartOffset = function (t) { return this._children.getNodeStartOffset(t); };
            Hs.prototype.offsetToIndex = function (t) { return this._children.offsetToIndex(t); };
            Hs.prototype.getNodeByPath = function (t) { var e = this; for (var _j = 0, t_87 = t; _j < t_87.length; _j++) {
                var n_75 = t_87[_j];
                e = e.getChild(e.offsetToIndex(n_75));
            } return e; };
            Hs.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); if (t.name = this.name, this._children.length > 0) {
                t.children = [];
                for (var _j = 0, _k = this._children; _j < _k.length; _j++) {
                    var e_75 = _k[_j];
                    t.children.push(e_75.toJSON());
                }
            } return t; };
            Hs.prototype._clone = function (t) {
                if (t === void 0) { t = !1; }
                var e = t ? Array.from(this._children).map(function (t) { return t._clone(!0); }) : null;
                return new Hs(this.name, this.getAttributes(), e);
            };
            Hs.prototype._appendChild = function (t) { this._insertChild(this.childCount, t); };
            Hs.prototype._insertChild = function (t, e) { var n = function (t) { if ("string" == typeof t)
                return [new Bs(t)]; pi(t) || (t = [t]); return Array.from(t).map(function (t) { return "string" == typeof t ? new Bs(t) : t instanceof Fs ? new Bs(t.data, t.getAttributes()) : t; }); }(e); for (var _j = 0, n_76 = n; _j < n_76.length; _j++) {
                var t_88 = n_76[_j];
                null !== t_88.parent && t_88._remove(), t_88.parent = this;
            } this._children._insertNodes(t, n); };
            Hs.prototype._removeChildren = function (t, e) {
                if (e === void 0) { e = 1; }
                var n = this._children._removeNodes(t, e);
                for (var _j = 0, n_77 = n; _j < n_77.length; _j++) {
                    var t_89 = n_77[_j];
                    t_89.parent = null;
                }
                return n;
            };
            Hs.fromJSON = function (t) { var e = null; if (t.children) {
                e = [];
                for (var _j = 0, _k = t.children; _j < _k.length; _j++) {
                    var n_78 = _k[_j];
                    n_78.name ? e.push(Hs.fromJSON(n_78)) : e.push(Bs.fromJSON(n_78));
                }
            } return new Hs(t.name, t.attributes, e); };
            return Hs;
        }(zs));
        var qs = /** @class */ (function () {
            function qs(t) {
                if (t === void 0) { t = {}; }
                if (!t.boundaries && !t.startPosition)
                    throw new Gn.b("model-tree-walker-no-start-position: Neither boundaries nor starting position have been defined.");
                var e = t.direction || "forward";
                if ("forward" != e && "backward" != e)
                    throw new Gn.b("model-tree-walker-unknown-direction: Only `backward` and `forward` direction allowed.", { direction: e });
                this.direction = e, this.boundaries = t.boundaries || null, t.startPosition ? this.position = t.startPosition.clone() : this.position = $s._createAt(this.boundaries["backward" == this.direction ? "end" : "start"]), this.position.stickiness = "toNone", this.singleCharacters = !!t.singleCharacters, this.shallow = !!t.shallow, this.ignoreElementEnd = !!t.ignoreElementEnd, this._boundaryStartParent = this.boundaries ? this.boundaries.start.parent : null, this._boundaryEndParent = this.boundaries ? this.boundaries.end.parent : null, this._visitedParent = this.position.parent;
            }
            qs.prototype[Symbol.iterator] = function () { return this; };
            qs.prototype.skip = function (t) {
                var _j;
                var e, n, i, o;
                do {
                    i = this.position, o = this._visitedParent, (_j = this.next(), e = _j.done, n = _j.value, _j);
                } while (!e && t(n));
                e || (this.position = i, this._visitedParent = o);
            };
            qs.prototype.next = function () { return "forward" == this.direction ? this._next() : this._previous(); };
            qs.prototype._next = function () { var t = this.position, e = this.position.clone(), n = this._visitedParent; if (null === n.parent && e.offset === n.maxOffset)
                return { done: !0 }; if (n === this._boundaryEndParent && e.offset == this.boundaries.end.offset)
                return { done: !0 }; var i = e.textNode ? e.textNode : e.nodeAfter; if (i instanceof Hs)
                return this.shallow ? e.offset++ : (e.path.push(0), this._visitedParent = i), this.position = e, Ws("elementStart", i, t, e, 1); if (i instanceof Bs) {
                var o_27;
                if (this.singleCharacters)
                    o_27 = 1;
                else {
                    var t_90 = i.endOffset;
                    this._boundaryEndParent == n && this.boundaries.end.offset < t_90 && (t_90 = this.boundaries.end.offset), o_27 = t_90 - e.offset;
                }
                var r_12 = e.offset - i.startOffset, s_11 = new Fs(i, r_12, o_27);
                return e.offset += o_27, this.position = e, Ws("text", s_11, t, e, o_27);
            } return e.path.pop(), e.offset++, this.position = e, this._visitedParent = n.parent, this.ignoreElementEnd ? this._next() : Ws("elementEnd", n, t, e); };
            qs.prototype._previous = function () { var t = this.position, e = this.position.clone(), n = this._visitedParent; if (null === n.parent && 0 === e.offset)
                return { done: !0 }; if (n == this._boundaryStartParent && e.offset == this.boundaries.start.offset)
                return { done: !0 }; var i = e.textNode ? e.textNode : e.nodeBefore; if (i instanceof Hs)
                return e.offset--, this.shallow ? (this.position = e, Ws("elementStart", i, t, e, 1)) : (e.path.push(i.maxOffset), this.position = e, this._visitedParent = i, this.ignoreElementEnd ? this._previous() : Ws("elementEnd", i, t, e)); if (i instanceof Bs) {
                var o_28;
                if (this.singleCharacters)
                    o_28 = 1;
                else {
                    var t_91 = i.startOffset;
                    this._boundaryStartParent == n && this.boundaries.start.offset > t_91 && (t_91 = this.boundaries.start.offset), o_28 = e.offset - t_91;
                }
                var r_13 = e.offset - i.startOffset, s_12 = new Fs(i, r_13 - o_28, o_28);
                return e.offset -= o_28, this.position = e, Ws("text", s_12, t, e, o_28);
            } return e.path.pop(), this.position = e, this._visitedParent = n.parent, Ws("elementStart", n, t, e, 1); };
            return qs;
        }());
        function Ws(t, e, n, i, o) { return { done: !1, value: { type: t, item: e, previousPosition: n, nextPosition: i, length: o } }; }
        var Ys = function (t) { var e = null == t ? 0 : t.length; return e ? t[e - 1] : void 0; };
        var $s = /** @class */ (function () {
            function $s(t, e, n) {
                if (n === void 0) { n = "toNone"; }
                if (!t.is("element") && !t.is("documentFragment"))
                    throw new Gn.b("model-position-root-invalid: Position root invalid.");
                if (!(e instanceof Array) || 0 === e.length)
                    throw new Gn.b("model-position-path-incorrect: Position path must be an array with at least one item.", { path: e });
                e = t.getPath().concat(e), t = t.root, this.root = t, this.path = e, this.stickiness = n;
            }
            Object.defineProperty($s.prototype, "offset", {
                get: function () { return Ys(this.path); },
                set: function (t) { this.path[this.path.length - 1] = t; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty($s.prototype, "parent", {
                get: function () { var t = this.root; for (var e_76 = 0; e_76 < this.path.length - 1; e_76++)
                    t = t.getChild(t.offsetToIndex(this.path[e_76])); return t; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty($s.prototype, "index", {
                get: function () { return this.parent.offsetToIndex(this.offset); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty($s.prototype, "textNode", {
                get: function () { var t = this.parent.getChild(this.index); return t instanceof Bs && t.startOffset < this.offset ? t : null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty($s.prototype, "nodeAfter", {
                get: function () { return null === this.textNode ? this.parent.getChild(this.index) : null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty($s.prototype, "nodeBefore", {
                get: function () { return null === this.textNode ? this.parent.getChild(this.index - 1) : null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty($s.prototype, "isAtStart", {
                get: function () { return 0 === this.offset; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty($s.prototype, "isAtEnd", {
                get: function () { return this.offset == this.parent.maxOffset; },
                enumerable: true,
                configurable: true
            });
            $s.prototype.compareWith = function (t) { if (this.root != t.root)
                return "different"; var e = li(this.path, t.path); switch (e) {
                case "same": return "same";
                case "prefix": return "before";
                case "extension": return "after";
                default: return this.path[e] < t.path[e] ? "before" : "after";
            } };
            $s.prototype.getLastMatchingPosition = function (t, e) {
                if (e === void 0) { e = {}; }
                e.startPosition = this;
                var n = new qs(e);
                return n.skip(t), n.position;
            };
            $s.prototype.getParentPath = function () { return this.path.slice(0, -1); };
            $s.prototype.getAncestors = function () { return this.parent.is("documentFragment") ? [this.parent] : this.parent.getAncestors({ includeSelf: !0 }); };
            $s.prototype.getCommonPath = function (t) { if (this.root != t.root)
                return []; var e = li(this.path, t.path), n = "string" == typeof e ? Math.min(this.path.length, t.path.length) : e; return this.path.slice(0, n); };
            $s.prototype.getCommonAncestor = function (t) { var e = this.getAncestors(), n = t.getAncestors(); var i = 0; for (; e[i] == n[i] && e[i];)
                i++; return 0 === i ? null : e[i - 1]; };
            $s.prototype.getShiftedBy = function (t) { var e = this.clone(), n = e.offset + t; return e.offset = n < 0 ? 0 : n, e; };
            $s.prototype.isAfter = function (t) { return "after" == this.compareWith(t); };
            $s.prototype.isBefore = function (t) { return "before" == this.compareWith(t); };
            $s.prototype.isEqual = function (t) { return "same" == this.compareWith(t); };
            $s.prototype.isTouching = function (t) { var e = null, n = null; switch (this.compareWith(t)) {
                case "same": return !0;
                case "before":
                    e = $s._createAt(this), n = $s._createAt(t);
                    break;
                case "after":
                    e = $s._createAt(t), n = $s._createAt(this);
                    break;
                default: return !1;
            } var i = e.parent; for (; e.path.length + n.path.length;) {
                if (e.isEqual(n))
                    return !0;
                if (e.path.length > n.path.length) {
                    if (e.offset !== i.maxOffset)
                        return !1;
                    e.path = e.path.slice(0, -1), i = i.parent, e.offset++;
                }
                else {
                    if (0 !== n.offset)
                        return !1;
                    n.path = n.path.slice(0, -1);
                }
            } };
            $s.prototype.hasSameParentAs = function (t) { if (this.root !== t.root)
                return !1; return "same" == li(this.getParentPath(), t.getParentPath()); };
            $s.prototype.getTransformedByOperation = function (t) { var e; switch (t.type) {
                case "insert":
                    e = this._getTransformedByInsertOperation(t);
                    break;
                case "move":
                case "remove":
                case "reinsert":
                    e = this._getTransformedByMoveOperation(t);
                    break;
                case "split":
                    e = this._getTransformedBySplitOperation(t);
                    break;
                case "merge":
                    e = this._getTransformedByMergeOperation(t);
                    break;
                default: e = $s._createAt(this);
            } return e; };
            $s.prototype._getTransformedByInsertOperation = function (t) { return this._getTransformedByInsertion(t.position, t.howMany); };
            $s.prototype._getTransformedByMoveOperation = function (t) { return this._getTransformedByMove(t.sourcePosition, t.targetPosition, t.howMany); };
            $s.prototype._getTransformedBySplitOperation = function (t) { var e = t.movedRange; return e.containsPosition(this) || e.start.isEqual(this) && "toNext" == this.stickiness ? this._getCombined(t.splitPosition, t.moveTargetPosition) : t.graveyardPosition ? this._getTransformedByMove(t.graveyardPosition, t.insertionPosition, 1) : this._getTransformedByInsertion(t.insertionPosition, 1); };
            $s.prototype._getTransformedByMergeOperation = function (t) { var e = t.movedRange; var n; return e.containsPosition(this) || e.start.isEqual(this) ? (n = this._getCombined(t.sourcePosition, t.targetPosition), t.sourcePosition.isBefore(t.targetPosition) && (n = n._getTransformedByDeletion(t.deletionPosition, 1))) : n = this.isEqual(t.deletionPosition) ? $s._createAt(t.deletionPosition) : this._getTransformedByMove(t.deletionPosition, t.graveyardPosition, 1), n; };
            $s.prototype._getTransformedByDeletion = function (t, e) { var n = $s._createAt(this); if (this.root != t.root)
                return n; if ("same" == li(t.getParentPath(), this.getParentPath())) {
                if (t.offset < this.offset) {
                    if (t.offset + e > this.offset)
                        return null;
                    n.offset -= e;
                }
            }
            else if ("prefix" == li(t.getParentPath(), this.getParentPath())) {
                var i_40 = t.path.length - 1;
                if (t.offset <= this.path[i_40]) {
                    if (t.offset + e > this.path[i_40])
                        return null;
                    n.path[i_40] -= e;
                }
            } return n; };
            $s.prototype._getTransformedByInsertion = function (t, e) { var n = $s._createAt(this); if (this.root != t.root)
                return n; if ("same" == li(t.getParentPath(), this.getParentPath()))
                (t.offset < this.offset || t.offset == this.offset && "toPrevious" != this.stickiness) && (n.offset += e);
            else if ("prefix" == li(t.getParentPath(), this.getParentPath())) {
                var i_41 = t.path.length - 1;
                t.offset <= this.path[i_41] && (n.path[i_41] += e);
            } return n; };
            $s.prototype._getTransformedByMove = function (t, e, n) { if (e = e._getTransformedByDeletion(t, n), t.isEqual(e))
                return $s._createAt(this); var i = this._getTransformedByDeletion(t, n); return null === i || t.isEqual(this) && "toNext" == this.stickiness || t.getShiftedBy(n).isEqual(this) && "toPrevious" == this.stickiness ? this._getCombined(t, e) : i._getTransformedByInsertion(e, n); };
            $s.prototype._getCombined = function (t, e) { var n = t.path.length - 1, i = $s._createAt(e); return i.stickiness = this.stickiness, i.offset = i.offset + this.path[n] - t.offset, i.path = i.path.concat(this.path.slice(n + 1)), i; };
            $s.prototype.toJSON = function () { return { root: this.root.toJSON(), path: Array.from(this.path), stickiness: this.stickiness }; };
            $s.prototype.clone = function () { return new this.constructor(this.root, this.path, this.stickiness); };
            $s._createAt = function (t, e) { if (t instanceof $s)
                return new $s(t.root, t.path, t.stickiness); {
                var n_79 = t;
                if ("end" == e)
                    e = n_79.maxOffset;
                else {
                    if ("before" == e)
                        return this._createBefore(n_79);
                    if ("after" == e)
                        return this._createAfter(n_79);
                    if (0 !== e && !e)
                        throw new Gn.b("model-createPositionAt-offset-required: Model#createPositionAt() requires the offset when the first parameter is a model item.");
                }
                if (!n_79.is("element") && !n_79.is("documentFragment"))
                    throw new Gn.b("model-position-parent-incorrect: Position parent have to be a element or document fragment.");
                var i_42 = n_79.getPath();
                return i_42.push(e), new this(n_79.root, i_42);
            } };
            $s._createAfter = function (t) { if (!t.parent)
                throw new Gn.b("model-position-after-root: You cannot make a position after root.", { root: t }); return this._createAt(t.parent, t.endOffset); };
            $s._createBefore = function (t) { if (!t.parent)
                throw new Gn.b("model-position-before-root: You cannot make a position before root.", { root: t }); return this._createAt(t.parent, t.startOffset); };
            $s.fromJSON = function (t, e) { if ("$graveyard" === t.root) {
                var n_80 = new $s(e.graveyard, t.path);
                return n_80.stickiness = t.stickiness, n_80;
            } if (!e.getRoot(t.root))
                throw new Gn.b("model-position-fromjson-no-root: Cannot create position for document. Root with specified name does not exist.", { rootName: t.root }); var n = new $s(e.getRoot(t.root), t.path); return n.stickiness = t.stickiness, n; };
            return $s;
        }());
        var Gs = /** @class */ (function () {
            function Gs(t, e) {
                if (e === void 0) { e = null; }
                this.start = $s._createAt(t), this.end = e ? $s._createAt(e) : $s._createAt(t), this.start.stickiness = this.isCollapsed ? "toNone" : "toNext", this.end.stickiness = this.isCollapsed ? "toNone" : "toPrevious";
            }
            Gs.prototype[Symbol.iterator] = function () { return __generator(this, function (_j) {
                switch (_j.label) {
                    case 0: return [5 /*yield**/, __values(new qs({ boundaries: this, ignoreElementEnd: !0 }))];
                    case 1:
                        _j.sent();
                        return [2 /*return*/];
                }
            }); };
            Object.defineProperty(Gs.prototype, "isCollapsed", {
                get: function () { return this.start.isEqual(this.end); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Gs.prototype, "isFlat", {
                get: function () { return "same" == li(this.start.getParentPath(), this.end.getParentPath()); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Gs.prototype, "root", {
                get: function () { return this.start.root; },
                enumerable: true,
                configurable: true
            });
            Gs.prototype.containsPosition = function (t) { return t.isAfter(this.start) && t.isBefore(this.end); };
            Gs.prototype.containsRange = function (t, e) {
                if (e === void 0) { e = !1; }
                t.isCollapsed && (e = !1);
                var n = this.containsPosition(t.start) || e && this.start.isEqual(t.start), i = this.containsPosition(t.end) || e && this.end.isEqual(t.end);
                return n && i;
            };
            Gs.prototype.containsItem = function (t) { var e = $s._createBefore(t); return this.containsPosition(e) || this.start.isEqual(e); };
            Gs.prototype.isEqual = function (t) { return this.start.isEqual(t.start) && this.end.isEqual(t.end); };
            Gs.prototype.isIntersecting = function (t) { return this.start.isBefore(t.end) && this.end.isAfter(t.start); };
            Gs.prototype.getDifference = function (t) { var e = []; return this.isIntersecting(t) ? (this.containsPosition(t.start) && e.push(new Gs(this.start, t.start)), this.containsPosition(t.end) && e.push(new Gs(t.end, this.end))) : e.push(new Gs(this.start, this.end)), e; };
            Gs.prototype.getIntersection = function (t) { if (this.isIntersecting(t)) {
                var e_77 = this.start, n_81 = this.end;
                return this.containsPosition(t.start) && (e_77 = t.start), this.containsPosition(t.end) && (n_81 = t.end), new Gs(e_77, n_81);
            } return null; };
            Gs.prototype.getMinimalFlatRanges = function () { var t = [], e = this.start.getCommonPath(this.end).length, n = $s._createAt(this.start); var i = n.parent; for (; n.path.length > e + 1;) {
                var e_78 = i.maxOffset - n.offset;
                0 !== e_78 && t.push(new Gs(n, n.getShiftedBy(e_78))), n.path = n.path.slice(0, -1), n.offset++, i = i.parent;
            } for (; n.path.length <= this.end.path.length;) {
                var e_79 = this.end.path[n.path.length - 1], i_43 = e_79 - n.offset;
                0 !== i_43 && t.push(new Gs(n, n.getShiftedBy(i_43))), n.offset = e_79, n.path.push(0);
            } return t; };
            Gs.prototype.getWalker = function (t) {
                if (t === void 0) { t = {}; }
                return t.boundaries = this, new qs(t);
            };
            Gs.prototype.getItems = function (t) {
                var e, _j, e_80, t_92;
                if (t === void 0) { t = {}; }
                return __generator(this, function (_k) {
                    switch (_k.label) {
                        case 0:
                            t.boundaries = this, t.ignoreElementEnd = !0;
                            e = new qs(t);
                            _j = 0, e_80 = e;
                            _k.label = 1;
                        case 1:
                            if (!(_j < e_80.length)) return [3 /*break*/, 4];
                            t_92 = e_80[_j];
                            return [4 /*yield*/, t_92.item];
                        case 2:
                            _k.sent();
                            _k.label = 3;
                        case 3:
                            _j++;
                            return [3 /*break*/, 1];
                        case 4: return [2 /*return*/];
                    }
                });
            };
            Gs.prototype.getPositions = function (t) {
                var e, _j, e_81, t_93;
                if (t === void 0) { t = {}; }
                return __generator(this, function (_k) {
                    switch (_k.label) {
                        case 0:
                            t.boundaries = this;
                            e = new qs(t);
                            return [4 /*yield*/, e.position];
                        case 1:
                            _k.sent();
                            _j = 0, e_81 = e;
                            _k.label = 2;
                        case 2:
                            if (!(_j < e_81.length)) return [3 /*break*/, 5];
                            t_93 = e_81[_j];
                            return [4 /*yield*/, t_93.nextPosition];
                        case 3:
                            _k.sent();
                            _k.label = 4;
                        case 4:
                            _j++;
                            return [3 /*break*/, 2];
                        case 5: return [2 /*return*/];
                    }
                });
            };
            Gs.prototype.getTransformedByOperation = function (t) { switch (t.type) {
                case "insert": return this._getTransformedByInsertOperation(t);
                case "move":
                case "remove":
                case "reinsert": return this._getTransformedByMoveOperation(t);
                case "split": return [this._getTransformedBySplitOperation(t)];
                case "merge": return [this._getTransformedByMergeOperation(t)];
            } return [new Gs(this.start, this.end)]; };
            Gs.prototype.getTransformedByOperations = function (t) { var e = [new Gs(this.start, this.end)]; for (var _j = 0, t_94 = t; _j < t_94.length; _j++) {
                var n_82 = t_94[_j];
                for (var t_95 = 0; t_95 < e.length; t_95++) {
                    var i_44 = e[t_95].getTransformedByOperation(n_82);
                    e.splice.apply(e, [t_95, 1].concat(i_44)), t_95 += i_44.length - 1;
                }
            } for (var t_96 = 0; t_96 < e.length; t_96++) {
                var n_83 = e[t_96];
                for (var i_45 = t_96 + 1; i_45 < e.length; i_45++) {
                    var t_97 = e[i_45];
                    (n_83.containsRange(t_97) || t_97.containsRange(n_83) || n_83.isEqual(t_97)) && e.splice(i_45, 1);
                }
            } return e; };
            Gs.prototype.getCommonAncestor = function () { return this.start.getCommonAncestor(this.end); };
            Gs.prototype.toJSON = function () { return { start: this.start.toJSON(), end: this.end.toJSON() }; };
            Gs.prototype.clone = function () { return new this.constructor(this.start, this.end); };
            Gs.prototype._getTransformedByInsertOperation = function (t, e) {
                if (e === void 0) { e = !1; }
                return this._getTransformedByInsertion(t.position, t.howMany, e);
            };
            Gs.prototype._getTransformedByMoveOperation = function (t, e) {
                if (e === void 0) { e = !1; }
                var n = t.sourcePosition, i = t.howMany, o = t.targetPosition;
                return this._getTransformedByMove(n, o, i, e);
            };
            Gs.prototype._getTransformedBySplitOperation = function (t) { var e = this.start._getTransformedBySplitOperation(t); var n = this.end._getTransformedBySplitOperation(t); return this.end.isEqual(t.insertionPosition) && (n = this.end.getShiftedBy(1)), e.root != n.root && (n = this.end.getShiftedBy(-1)), new Gs(e, n); };
            Gs.prototype._getTransformedByMergeOperation = function (t) { if (this.start.isEqual(t.targetPosition) && this.end.isEqual(t.deletionPosition))
                return new Gs(this.start); var e = this.start._getTransformedByMergeOperation(t), n = this.end._getTransformedByMergeOperation(t); return e.root != n.root && (n = this.end.getShiftedBy(-1)), e.isAfter(n) ? (t.sourcePosition.isBefore(t.targetPosition) ? (e = $s._createAt(n)).offset = 0 : (t.deletionPosition.isEqual(e) || (n = t.deletionPosition), e = t.targetPosition), new Gs(e, n)) : new Gs(e, n); };
            Gs.prototype._getTransformedByInsertion = function (t, e, n) {
                if (n === void 0) { n = !1; }
                if (n && this.containsPosition(t))
                    return [new Gs(this.start, t), new Gs(t.getShiftedBy(e), this.end._getTransformedByInsertion(t, e))];
                {
                    var n_84 = new Gs(this.start, this.end);
                    return n_84.start = n_84.start._getTransformedByInsertion(t, e), n_84.end = n_84.end._getTransformedByInsertion(t, e), [n_84];
                }
            };
            Gs.prototype._getTransformedByMove = function (t, e, n, i) {
                if (i === void 0) { i = !1; }
                if (this.isCollapsed) {
                    var i_46 = this.start._getTransformedByMove(t, e, n);
                    return [new Gs(i_46)];
                }
                var o = Gs._createFromPositionAndShift(t, n), r = e._getTransformedByDeletion(t, n);
                if (this.containsPosition(e) && !i && (o.containsPosition(this.start) || o.containsPosition(this.end))) {
                    var i_47 = this.start._getTransformedByMove(t, e, n), o_29 = this.end._getTransformedByMove(t, e, n);
                    return [new Gs(i_47, o_29)];
                }
                var s;
                var a = this.getDifference(o);
                var c = null;
                var l = this.getIntersection(o);
                if (1 == a.length ? c = new Gs(a[0].start._getTransformedByDeletion(t, n), a[0].end._getTransformedByDeletion(t, n)) : 2 == a.length && (c = new Gs(this.start, this.end._getTransformedByDeletion(t, n))), s = c ? c._getTransformedByInsertion(r, n, null !== l || i) : [], l) {
                    var t_98 = new Gs(l.start._getCombined(o.start, r), l.end._getCombined(o.start, r));
                    2 == s.length ? s.splice(1, 0, t_98) : s.push(t_98);
                }
                return s;
            };
            Gs.prototype._getTransformedByDeletion = function (t, e) { var n = this.start._getTransformedByDeletion(t, e), i = this.end._getTransformedByDeletion(t, e); return null == n && null == i ? null : (null == n && (n = t), null == i && (i = t), new Gs(n, i)); };
            Gs._createFromPositionAndShift = function (t, e) { var n = t, i = t.getShiftedBy(e); return e > 0 ? new this(n, i) : new this(i, n); };
            Gs._createIn = function (t) { return new this($s._createAt(t, 0), $s._createAt(t, t.maxOffset)); };
            Gs._createOn = function (t) { return this._createFromPositionAndShift($s._createBefore(t), t.offsetSize); };
            Gs._createFromRanges = function (t) { if (0 === t.length)
                throw new Gn.b("range-create-from-ranges-empty-array: At least one range has to be passed."); if (1 == t.length)
                return t[0].clone(); var e = t[0]; t.sort(function (t, e) { return t.start.isAfter(e.start) ? 1 : -1; }); var n = t.indexOf(e), i = new this(e.start, e.end); if (n > 0)
                for (var e_82 = n - 1; t[e_82].end.isEqual(i.start); e_82++)
                    i.start = $s._createAt(t[e_82].start); for (var e_83 = n + 1; e_83 < t.length && t[e_83].start.isEqual(i.end); e_83++)
                i.end = $s._createAt(t[e_83].end); return i; };
            Gs.fromJSON = function (t, e) { return new this($s.fromJSON(t.start, e), $s.fromJSON(t.end, e)); };
            return Gs;
        }());
        var Qs = /** @class */ (function () {
            function Qs() {
                var _this = this;
                this._modelToViewMapping = new WeakMap, this._viewToModelMapping = new WeakMap, this._viewToModelLengthCallbacks = new Map, this._markerNameToElements = new Map, this.on("modelToViewPosition", function (t, e) { if (e.viewPosition)
                    return; var n = _this._modelToViewMapping.get(e.modelPosition.parent); e.viewPosition = _this._findPositionIn(n, e.modelPosition.offset); }, { priority: "low" }), this.on("viewToModelPosition", function (t, e) { if (e.modelPosition)
                    return; var n = _this.findMappedViewAncestor(e.viewPosition), i = _this._viewToModelMapping.get(n), o = _this._toModelOffset(e.viewPosition.parent, e.viewPosition.offset, n); e.modelPosition = $s._createAt(i, o); }, { priority: "low" });
            }
            Qs.prototype.bindElements = function (t, e) { this._modelToViewMapping.set(t, e), this._viewToModelMapping.set(e, t); };
            Qs.prototype.unbindViewElement = function (t) { var e = this.toModelElement(t); this._viewToModelMapping.delete(t), this._modelToViewMapping.get(e) == t && this._modelToViewMapping.delete(e); };
            Qs.prototype.unbindModelElement = function (t) { var e = this.toViewElement(t); this._modelToViewMapping.delete(t), this._viewToModelMapping.get(e) == t && this._viewToModelMapping.delete(e); };
            Qs.prototype.bindElementToMarker = function (t, e) { var n = this._markerNameToElements.get(e) || new Set; n.add(t), this._markerNameToElements.set(e, n); };
            Qs.prototype.unbindElementsFromMarkerName = function (t) { this._markerNameToElements.delete(t); };
            Qs.prototype.clearBindings = function () { this._modelToViewMapping = new WeakMap, this._viewToModelMapping = new WeakMap, this._markerNameToElements = new Map; };
            Qs.prototype.toModelElement = function (t) { return this._viewToModelMapping.get(t); };
            Qs.prototype.toViewElement = function (t) { return this._modelToViewMapping.get(t); };
            Qs.prototype.toModelRange = function (t) { return new Gs(this.toModelPosition(t.start), this.toModelPosition(t.end)); };
            Qs.prototype.toViewRange = function (t) { return new Xi(this.toViewPosition(t.start), this.toViewPosition(t.end)); };
            Qs.prototype.toModelPosition = function (t) { var e = { viewPosition: t, mapper: this }; return this.fire("viewToModelPosition", e), e.modelPosition; };
            Qs.prototype.toViewPosition = function (t, e) {
                if (e === void 0) { e = { isPhantom: !1 }; }
                var n = { modelPosition: t, mapper: this, isPhantom: e.isPhantom };
                return this.fire("modelToViewPosition", n), n.viewPosition;
            };
            Qs.prototype.markerNameToElements = function (t) { var e = this._markerNameToElements.get(t); if (!e)
                return null; var n = new Set; for (var _j = 0, e_84 = e; _j < e_84.length; _j++) {
                var t_99 = e_84[_j];
                if (t_99.is("attributeElement"))
                    for (var _k = 0, _q = t_99.getElementsWithSameId(); _k < _q.length; _k++) {
                        var e_85 = _q[_k];
                        n.add(e_85);
                    }
                else
                    n.add(t_99);
            } return n; };
            Qs.prototype.registerViewToModelLength = function (t, e) { this._viewToModelLengthCallbacks.set(t, e); };
            Qs.prototype.findMappedViewAncestor = function (t) { var e = t.parent; for (; !this._viewToModelMapping.has(e);)
                e = e.parent; return e; };
            Qs.prototype._toModelOffset = function (t, e, n) { if (n != t) {
                return this._toModelOffset(t.parent, t.index, n) + this._toModelOffset(t, e, t);
            } if (t.is("text"))
                return e; var i = 0; for (var n_85 = 0; n_85 < e; n_85++)
                i += this.getModelLength(t.getChild(n_85)); return i; };
            Qs.prototype.getModelLength = function (t) { if (this._viewToModelLengthCallbacks.get(t.name)) {
                return this._viewToModelLengthCallbacks.get(t.name)(t);
            } if (this._viewToModelMapping.has(t))
                return 1; if (t.is("text"))
                return t.data.length; if (t.is("uiElement"))
                return 0; {
                var e_86 = 0;
                for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
                    var n_86 = _k[_j];
                    e_86 += this.getModelLength(n_86);
                }
                return e_86;
            } };
            Qs.prototype._findPositionIn = function (t, e) { var n, i = 0, o = 0, r = 0; if (t.is("text"))
                return new Zi(t, e); for (; o < e;)
                n = t.getChild(r), o += i = this.getModelLength(n), r++; return o == e ? this._moveViewPositionToTextNode(new Zi(t, r)) : this._findPositionIn(n, e - (o - i)); };
            Qs.prototype._moveViewPositionToTextNode = function (t) { var e = t.nodeBefore, n = t.nodeAfter; return e instanceof fi ? new Zi(e, e.data.length) : n instanceof fi ? new Zi(n, 0) : t; };
            return Qs;
        }());
        ci(Qs, ei);
        var Ks = /** @class */ (function () {
            function Ks() {
                this._consumable = new Map, this._textProxyRegistry = new Map;
            }
            Ks.prototype.add = function (t, e) { e = Js(e), t instanceof Fs && (t = this._getSymbolForTextProxy(t)), this._consumable.has(t) || this._consumable.set(t, new Map), this._consumable.get(t).set(e, !0); };
            Ks.prototype.consume = function (t, e) { return e = Js(e), t instanceof Fs && (t = this._getSymbolForTextProxy(t)), !!this.test(t, e) && (this._consumable.get(t).set(e, !1), !0); };
            Ks.prototype.test = function (t, e) { e = Js(e), t instanceof Fs && (t = this._getSymbolForTextProxy(t)); var n = this._consumable.get(t); if (void 0 === n)
                return null; var i = n.get(e); return void 0 === i ? null : i; };
            Ks.prototype.revert = function (t, e) { e = Js(e), t instanceof Fs && (t = this._getSymbolForTextProxy(t)); var n = this.test(t, e); return !1 === n ? (this._consumable.get(t).set(e, !0), !0) : !0 !== n && null; };
            Ks.prototype._getSymbolForTextProxy = function (t) { var e = null; var n = this._textProxyRegistry.get(t.startOffset); if (n) {
                var i_48 = n.get(t.endOffset);
                i_48 && (e = i_48.get(t.parent));
            } return e || (e = this._addSymbolForTextProxy(t.startOffset, t.endOffset, t.parent)), e; };
            Ks.prototype._addSymbolForTextProxy = function (t, e, n) { var i = Symbol("textProxySymbol"); var o, r; return (o = this._textProxyRegistry.get(t)) || (o = new Map, this._textProxyRegistry.set(t, o)), (r = o.get(e)) || (r = new Map, o.set(e, r)), r.set(n, i), i; };
            return Ks;
        }());
        function Js(t) { var e = t.split(":"); return e.length > 1 ? e[0] + ":" + e[1] : e[0]; }
        var Zs = /** @class */ (function () {
            function Zs(t) {
                if (t === void 0) { t = {}; }
                this.conversionApi = Li({ dispatcher: this }, t);
            }
            Zs.prototype.convertChanges = function (t, e) { for (var _j = 0, _k = t.getMarkersToRemove(); _j < _k.length; _j++) {
                var n_87 = _k[_j];
                this.convertMarkerRemove(n_87.name, n_87.range, e);
            } for (var _q = 0, _v = t.getChanges(); _q < _v.length; _q++) {
                var n_88 = _v[_q];
                "insert" == n_88.type ? this.convertInsert(Gs._createFromPositionAndShift(n_88.position, n_88.length), e) : "remove" == n_88.type ? this.convertRemove(n_88.position, n_88.length, n_88.name, e) : this.convertAttribute(n_88.range, n_88.attributeKey, n_88.attributeOldValue, n_88.attributeNewValue, e);
            } for (var _w = 0, _x = t.getMarkersToAdd(); _w < _x.length; _w++) {
                var n_89 = _x[_w];
                this.convertMarkerAdd(n_89.name, n_89.range, e);
            } };
            Zs.prototype.convertInsert = function (t, e) { this.conversionApi.writer = e, this.conversionApi.consumable = this._createInsertConsumable(t); for (var _j = 0, t_100 = t; _j < t_100.length; _j++) {
                var e_87 = t_100[_j];
                var t_101 = e_87.item, n_90 = { item: t_101, range: Gs._createFromPositionAndShift(e_87.previousPosition, e_87.length) };
                this._testAndFire("insert", n_90);
                for (var _k = 0, _q = t_101.getAttributeKeys(); _k < _q.length; _k++) {
                    var e_88 = _q[_k];
                    n_90.attributeKey = e_88, n_90.attributeOldValue = null, n_90.attributeNewValue = t_101.getAttribute(e_88), this._testAndFire("attribute:" + e_88, n_90);
                }
            } this._clearConversionApi(); };
            Zs.prototype.convertRemove = function (t, e, n, i) { this.conversionApi.writer = i, this.fire("remove:" + n, { position: t, length: e }, this.conversionApi), this._clearConversionApi(); };
            Zs.prototype.convertAttribute = function (t, e, n, i, o) { this.conversionApi.writer = o, this.conversionApi.consumable = this._createConsumableForRange(t, "attribute:" + e); for (var _j = 0, t_102 = t; _j < t_102.length; _j++) {
                var o_30 = t_102[_j];
                var t_103 = { item: o_30.item, range: Gs._createFromPositionAndShift(o_30.previousPosition, o_30.length), attributeKey: e, attributeOldValue: n, attributeNewValue: i };
                this._testAndFire("attribute:" + e, t_103);
            } this._clearConversionApi(); };
            Zs.prototype.convertSelection = function (t, e, n) { var i = Array.from(e.getMarkersAtPosition(t.getFirstPosition())); if (this.conversionApi.writer = n, this.conversionApi.consumable = this._createSelectionConsumable(t, i), this.fire("selection", { selection: t }, this.conversionApi), t.isCollapsed) {
                for (var _j = 0, i_49 = i; _j < i_49.length; _j++) {
                    var e_89 = i_49[_j];
                    var n_91 = e_89.getRange();
                    if (!Xs(t.getFirstPosition(), e_89, this.conversionApi.mapper))
                        continue;
                    var i_50 = { item: t, markerName: e_89.name, markerRange: n_91 };
                    this.conversionApi.consumable.test(t, "addMarker:" + e_89.name) && this.fire("addMarker:" + e_89.name, i_50, this.conversionApi);
                }
                for (var _k = 0, _q = t.getAttributeKeys(); _k < _q.length; _k++) {
                    var e_90 = _q[_k];
                    var n_92 = { item: t, range: t.getFirstRange(), attributeKey: e_90, attributeOldValue: null, attributeNewValue: t.getAttribute(e_90) };
                    this.conversionApi.consumable.test(t, "attribute:" + n_92.attributeKey) && this.fire("attribute:" + n_92.attributeKey + ":$text", n_92, this.conversionApi);
                }
                this._clearConversionApi();
            } };
            Zs.prototype.convertMarkerAdd = function (t, e, n) { if (!e.root.document || "$graveyard" == e.root.rootName)
                return; this.conversionApi.writer = n; var i = "addMarker:" + t, o = new Ks; if (o.add(e, i), this.conversionApi.consumable = o, this.fire(i, { markerName: t, markerRange: e }, this.conversionApi), o.test(e, i)) {
                this.conversionApi.consumable = this._createConsumableForRange(e, i);
                for (var _j = 0, _k = e.getItems(); _j < _k.length; _j++) {
                    var n_93 = _k[_j];
                    if (!this.conversionApi.consumable.test(n_93, i))
                        continue;
                    var o_31 = { item: n_93, range: Gs._createOn(n_93), markerName: t, markerRange: e };
                    this.fire(i, o_31, this.conversionApi);
                }
                this._clearConversionApi();
            } };
            Zs.prototype.convertMarkerRemove = function (t, e, n) { e.root.document && "$graveyard" != e.root.rootName && (this.conversionApi.writer = n, this.fire("removeMarker:" + t, { markerName: t, markerRange: e }, this.conversionApi), this._clearConversionApi()); };
            Zs.prototype._createInsertConsumable = function (t) { var e = new Ks; for (var _j = 0, t_104 = t; _j < t_104.length; _j++) {
                var n_94 = t_104[_j];
                var t_105 = n_94.item;
                e.add(t_105, "insert");
                for (var _k = 0, _q = t_105.getAttributeKeys(); _k < _q.length; _k++) {
                    var n_95 = _q[_k];
                    e.add(t_105, "attribute:" + n_95);
                }
            } return e; };
            Zs.prototype._createConsumableForRange = function (t, e) { var n = new Ks; for (var _j = 0, _k = t.getItems(); _j < _k.length; _j++) {
                var i_51 = _k[_j];
                n.add(i_51, e);
            } return n; };
            Zs.prototype._createSelectionConsumable = function (t, e) { var n = new Ks; n.add(t, "selection"); for (var _j = 0, e_91 = e; _j < e_91.length; _j++) {
                var i_52 = e_91[_j];
                n.add(t, "addMarker:" + i_52.name);
            } for (var _k = 0, _q = t.getAttributeKeys(); _k < _q.length; _k++) {
                var e_92 = _q[_k];
                n.add(t, "attribute:" + e_92);
            } return n; };
            Zs.prototype._testAndFire = function (t, e) { if (!this.conversionApi.consumable.test(e.item, t))
                return; var n = e.item.name || "$text"; this.fire(t + ":" + n, e, this.conversionApi); };
            Zs.prototype._clearConversionApi = function () { delete this.conversionApi.writer, delete this.conversionApi.consumable; };
            return Zs;
        }());
        function Xs(t, e, n) { var i = e.getRange(), o = Array.from(t.getAncestors()); return o.shift(), o.reverse(), !o.some(function (t) { if (i.containsItem(t)) {
            return !!n.toViewElement(t).getCustomProperty("addHighlight");
        } }); }
        ci(Zs, ei);
        var ta = /** @class */ (function () {
            function ta(t, e, n) {
                this._lastRangeBackward = !1, this._ranges = [], this._attrs = new Map, t && this.setTo(t, e, n);
            }
            Object.defineProperty(ta.prototype, "anchor", {
                get: function () { if (this._ranges.length > 0) {
                    var t_106 = this._ranges[this._ranges.length - 1];
                    return this._lastRangeBackward ? t_106.end : t_106.start;
                } return null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(ta.prototype, "focus", {
                get: function () { if (this._ranges.length > 0) {
                    var t_107 = this._ranges[this._ranges.length - 1];
                    return this._lastRangeBackward ? t_107.start : t_107.end;
                } return null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(ta.prototype, "isCollapsed", {
                get: function () { return 1 === this._ranges.length && this._ranges[0].isCollapsed; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(ta.prototype, "rangeCount", {
                get: function () { return this._ranges.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(ta.prototype, "isBackward", {
                get: function () { return !this.isCollapsed && this._lastRangeBackward; },
                enumerable: true,
                configurable: true
            });
            ta.prototype.isEqual = function (t) { if (this.rangeCount != t.rangeCount)
                return !1; if (0 === this.rangeCount)
                return !0; if (!this.anchor.isEqual(t.anchor) || !this.focus.isEqual(t.focus))
                return !1; for (var _j = 0, _k = this._ranges; _j < _k.length; _j++) {
                var e_93 = _k[_j];
                var n_96 = !1;
                for (var _q = 0, _v = t._ranges; _q < _v.length; _q++) {
                    var i_53 = _v[_q];
                    if (e_93.isEqual(i_53)) {
                        n_96 = !0;
                        break;
                    }
                }
                if (!n_96)
                    return !1;
            } return !0; };
            ta.prototype.getRanges = function () { var _j, _k, t_108; return __generator(this, function (_q) {
                switch (_q.label) {
                    case 0:
                        _j = 0, _k = this._ranges;
                        _q.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 4];
                        t_108 = _k[_j];
                        return [4 /*yield*/, new Gs(t_108.start, t_108.end)];
                    case 2:
                        _q.sent();
                        _q.label = 3;
                    case 3:
                        _j++;
                        return [3 /*break*/, 1];
                    case 4: return [2 /*return*/];
                }
            }); };
            ta.prototype.getFirstRange = function () { var t = null; for (var _j = 0, _k = this._ranges; _j < _k.length; _j++) {
                var e_94 = _k[_j];
                t && !e_94.start.isBefore(t.start) || (t = e_94);
            } return t ? new Gs(t.start, t.end) : null; };
            ta.prototype.getLastRange = function () { var t = null; for (var _j = 0, _k = this._ranges; _j < _k.length; _j++) {
                var e_95 = _k[_j];
                t && !e_95.end.isAfter(t.end) || (t = e_95);
            } return t ? new Gs(t.start, t.end) : null; };
            ta.prototype.getFirstPosition = function () { var t = this.getFirstRange(); return t ? t.start.clone() : null; };
            ta.prototype.getLastPosition = function () { var t = this.getLastRange(); return t ? t.end.clone() : null; };
            ta.prototype.setTo = function (t, e, n) { if (null === t)
                this._setRanges([]);
            else if (t instanceof ta)
                this._setRanges(t.getRanges(), t.isBackward);
            else if (t && "function" == typeof t.getRanges)
                this._setRanges(t.getRanges(), t.isBackward);
            else if (t instanceof Gs)
                this._setRanges([t], !!e && !!e.backward);
            else if (t instanceof $s)
                this._setRanges([new Gs(t)]);
            else if (t instanceof zs) {
                var i_54 = !!n && !!n.backward;
                var o_32;
                if ("in" == e)
                    o_32 = Gs._createIn(t);
                else if ("on" == e)
                    o_32 = Gs._createOn(t);
                else {
                    if (void 0 === e)
                        throw new Gn.b("model-selection-setTo-required-second-parameter: selection.setTo requires the second parameter when the first parameter is a node.");
                    o_32 = new Gs($s._createAt(t, e));
                }
                this._setRanges([o_32], i_54);
            }
            else {
                if (!pi(t))
                    throw new Gn.b("model-selection-setTo-not-selectable: Cannot set selection to given place.");
                this._setRanges(t, e && !!e.backward);
            } };
            ta.prototype._setRanges = function (t, e) {
                var _this = this;
                if (e === void 0) { e = !1; }
                var n = (t = Array.from(t)).some(function (t) { if (!(t instanceof Gs))
                    throw new Gn.b("model-selection-set-ranges-not-range: Selection range set to an object that is not an instance of model.Range."); return _this._ranges.every(function (e) { return !e.isEqual(t); }); });
                if (t.length !== this._ranges.length || n) {
                    this._removeAllRanges();
                    for (var _j = 0, t_109 = t; _j < t_109.length; _j++) {
                        var e_96 = t_109[_j];
                        this._pushRange(e_96);
                    }
                    this._lastRangeBackward = !!e, this.fire("change:range", { directChange: !0 });
                }
            };
            ta.prototype.setFocus = function (t, e) { if (null === this.anchor)
                throw new Gn.b("model-selection-setFocus-no-ranges: Cannot set selection focus if there are no ranges in selection."); var n = $s._createAt(t, e); if ("same" == n.compareWith(this.focus))
                return; var i = this.anchor; this._ranges.length && this._popRange(), "before" == n.compareWith(i) ? (this._pushRange(new Gs(n, i)), this._lastRangeBackward = !0) : (this._pushRange(new Gs(i, n)), this._lastRangeBackward = !1), this.fire("change:range", { directChange: !0 }); };
            ta.prototype.getAttribute = function (t) { return this._attrs.get(t); };
            ta.prototype.getAttributes = function () { return this._attrs.entries(); };
            ta.prototype.getAttributeKeys = function () { return this._attrs.keys(); };
            ta.prototype.hasAttribute = function (t) { return this._attrs.has(t); };
            ta.prototype.removeAttribute = function (t) { this.hasAttribute(t) && (this._attrs.delete(t), this.fire("change:attribute", { attributeKeys: [t], directChange: !0 })); };
            ta.prototype.setAttribute = function (t, e) { this.getAttribute(t) !== e && (this._attrs.set(t, e), this.fire("change:attribute", { attributeKeys: [t], directChange: !0 })); };
            ta.prototype.getSelectedElement = function () { if (1 !== this.rangeCount)
                return null; var t = this.getFirstRange(), e = t.start.nodeAfter, n = t.end.nodeBefore; return e instanceof Hs && e == n ? e : null; };
            ta.prototype.is = function (t) { return "selection" == t; };
            ta.prototype.getSelectedBlocks = function () { var t, _j, _k, e_97, n_97, _q, _v, _w, n_98, _x, i_55, _y; return __generator(this, function (_z) {
                switch (_z.label) {
                    case 0:
                        t = new WeakSet;
                        _j = 0, _k = this.getRanges();
                        _z.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 12];
                        e_97 = _k[_j];
                        n_97 = na(e_97.start, t);
                        _q = n_97;
                        if (!_q) return [3 /*break*/, 3];
                        return [4 /*yield*/, n_97];
                    case 2:
                        _q = (_z.sent());
                        _z.label = 3;
                    case 3:
                        _q;
                        _v = 0, _w = e_97.getWalker();
                        _z.label = 4;
                    case 4:
                        if (!(_v < _w.length)) return [3 /*break*/, 8];
                        n_98 = _w[_v];
                        _x = "elementEnd" == n_98.type && ea(n_98.item, t);
                        if (!_x) return [3 /*break*/, 6];
                        return [4 /*yield*/, n_98.item];
                    case 5:
                        _x = (_z.sent());
                        _z.label = 6;
                    case 6:
                        _x;
                        _z.label = 7;
                    case 7:
                        _v++;
                        return [3 /*break*/, 4];
                    case 8:
                        i_55 = na(e_97.end, t);
                        _y = i_55 && !e_97.end.isTouching($s._createAt(i_55, 0));
                        if (!_y) return [3 /*break*/, 10];
                        return [4 /*yield*/, i_55];
                    case 9:
                        _y = (_z.sent());
                        _z.label = 10;
                    case 10:
                        _y;
                        _z.label = 11;
                    case 11:
                        _j++;
                        return [3 /*break*/, 1];
                    case 12: return [2 /*return*/];
                }
            }); };
            ta.prototype.getTopMostBlocks = function () { var t, _j, t_110, e_98, n_99, _k; return __generator(this, function (_q) {
                switch (_q.label) {
                    case 0:
                        t = Array.from(this.getSelectedBlocks());
                        _j = 0, t_110 = t;
                        _q.label = 1;
                    case 1:
                        if (!(_j < t_110.length)) return [3 /*break*/, 5];
                        e_98 = t_110[_j];
                        n_99 = ia(e_98);
                        _k = n_99 && t.includes(n_99);
                        if (_k) return [3 /*break*/, 3];
                        return [4 /*yield*/, e_98];
                    case 2:
                        _k = (_q.sent());
                        _q.label = 3;
                    case 3:
                        _k;
                        _q.label = 4;
                    case 4:
                        _j++;
                        return [3 /*break*/, 1];
                    case 5: return [2 /*return*/];
                }
            }); };
            ta.prototype.containsEntireContent = function (t) {
                if (t === void 0) { t = this.anchor.root; }
                var e = $s._createAt(t, 0), n = $s._createAt(t, "end");
                return e.isTouching(this.getFirstPosition()) && n.isTouching(this.getLastPosition());
            };
            ta.prototype._pushRange = function (t) { this._checkRange(t), this._ranges.push(new Gs(t.start, t.end)); };
            ta.prototype._checkRange = function (t) { for (var e_99 = 0; e_99 < this._ranges.length; e_99++)
                if (t.isIntersecting(this._ranges[e_99]))
                    throw new Gn.b("model-selection-range-intersects: Trying to add a range that intersects with another range in the selection.", { addedRange: t, intersectingRange: this._ranges[e_99] }); };
            ta.prototype._removeAllRanges = function () { for (; this._ranges.length > 0;)
                this._popRange(); };
            ta.prototype._popRange = function () { this._ranges.pop(); };
            return ta;
        }());
        function ea(t, e) { return !e.has(t) && (e.add(t), t.document.model.schema.isBlock(t) && t.parent); }
        function na(t, e) { var n = t.parent.document.model.schema, i = t.parent.getAncestors({ parentFirst: !0, includeSelf: !0 }); var o = !1; var r = i.find(function (t) { return !o && (!(o = n.isLimit(t)) && ea(t, e)); }); return i.forEach(function (t) { return e.add(t); }), r; }
        function ia(t) { var e = t.document.model.schema; var n = t.parent; for (; n;) {
            if (e.isBlock(n))
                return n;
            n = n.parent;
        } }
        ci(ta, ei);
        var oa = /** @class */ (function (_super) {
            __extends(oa, _super);
            function oa(t, e) {
                var _this = this;
                _this = _super.call(this, t, e) || this, function () {
                    var _this = this;
                    this.listenTo(this.root.document.model, "applyOperation", function (t, e) { var n = e[0]; n.isDocumentOperation && function (t) { var e = this.getTransformedByOperation(t), n = Gs._createFromRanges(e), i = !n.isEqual(this), o = function (t, e) { switch (e.type) {
                        case "insert": return t.containsPosition(e.position);
                        case "move":
                        case "remove":
                        case "reinsert":
                        case "merge": return t.containsPosition(e.sourcePosition) || t.start.isEqual(e.sourcePosition) || t.containsPosition(e.targetPosition);
                        case "split": return t.containsPosition(e.splitPosition) || t.containsPosition(e.insertionPosition);
                    } return !1; }(this, t); var r = null; if (i) {
                        "$graveyard" == n.root.rootName && (r = "remove" == t.type ? t.sourcePosition : t.deletionPosition);
                        var e_100 = this.toRange();
                        this.start = n.start, this.end = n.end, this.fire("change:range", e_100, { deletionPosition: r });
                    }
                    else
                        o && this.fire("change:content", this.toRange(), { deletionPosition: r }); }.call(_this, n); }, { priority: "low" });
                }.call(_this);
                return _this;
            }
            oa.prototype.detach = function () { this.stopListening(); };
            oa.prototype.toRange = function () { return new Gs(this.start, this.end); };
            oa.fromRange = function (t) { return new oa(t.start, t.end); };
            return oa;
        }(Gs));
        ci(oa, ei);
        var ra = "selection:";
        var sa = /** @class */ (function () {
            function sa(t) {
                this._selection = new aa(t), this._selection.delegate("change:range").to(this), this._selection.delegate("change:attribute").to(this);
            }
            Object.defineProperty(sa.prototype, "isCollapsed", {
                get: function () { return this._selection.isCollapsed; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sa.prototype, "anchor", {
                get: function () { return this._selection.anchor; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sa.prototype, "focus", {
                get: function () { return this._selection.focus; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sa.prototype, "rangeCount", {
                get: function () { return this._selection.rangeCount; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sa.prototype, "hasOwnRange", {
                get: function () { return this._selection.hasOwnRange; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sa.prototype, "isBackward", {
                get: function () { return this._selection.isBackward; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sa.prototype, "isGravityOverridden", {
                get: function () { return this._selection.isGravityOverridden; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sa.prototype, "markers", {
                get: function () { return this._selection.markers; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sa.prototype, "_ranges", {
                get: function () { return this._selection._ranges; },
                enumerable: true,
                configurable: true
            });
            sa.prototype.getRanges = function () { return this._selection.getRanges(); };
            sa.prototype.getFirstPosition = function () { return this._selection.getFirstPosition(); };
            sa.prototype.getLastPosition = function () { return this._selection.getLastPosition(); };
            sa.prototype.getFirstRange = function () { return this._selection.getFirstRange(); };
            sa.prototype.getLastRange = function () { return this._selection.getLastRange(); };
            sa.prototype.getSelectedBlocks = function () { return this._selection.getSelectedBlocks(); };
            sa.prototype.getTopMostBlocks = function () { return this._selection.getTopMostBlocks(); };
            sa.prototype.getSelectedElement = function () { return this._selection.getSelectedElement(); };
            sa.prototype.containsEntireContent = function (t) { return this._selection.containsEntireContent(t); };
            sa.prototype.destroy = function () { this._selection.destroy(); };
            sa.prototype.getAttributeKeys = function () { return this._selection.getAttributeKeys(); };
            sa.prototype.getAttributes = function () { return this._selection.getAttributes(); };
            sa.prototype.getAttribute = function (t) { return this._selection.getAttribute(t); };
            sa.prototype.hasAttribute = function (t) { return this._selection.hasAttribute(t); };
            sa.prototype.is = function (t) { return "selection" == t || "documentSelection" == t; };
            sa.prototype._setFocus = function (t, e) { this._selection.setFocus(t, e); };
            sa.prototype._setTo = function (t, e, n) { this._selection.setTo(t, e, n); };
            sa.prototype._setAttribute = function (t, e) { this._selection.setAttribute(t, e); };
            sa.prototype._removeAttribute = function (t) { this._selection.removeAttribute(t); };
            sa.prototype._getStoredAttributes = function () { return this._selection._getStoredAttributes(); };
            sa.prototype._overrideGravity = function () { return this._selection.overrideGravity(); };
            sa.prototype._restoreGravity = function (t) { this._selection.restoreGravity(t); };
            sa._getStoreAttributeKey = function (t) { return ra + t; };
            sa._isStoreAttributeKey = function (t) { return t.startsWith(ra); };
            return sa;
        }());
        ci(sa, ei);
        var aa = /** @class */ (function (_super) {
            __extends(aa, _super);
            function aa(t) {
                var _this = this;
                _this = _super.call(this) || this, _this.markers = new oo({ idProperty: "name" }), _this._model = t.model, _this._document = t, _this._attributePriority = new Map, _this._fixGraveyardRangesData = [], _this._hasChangedRange = !1, _this._overriddenGravityRegister = new Set, _this.on("change:range", function () { for (var _j = 0, _k = _this.getRanges(); _j < _k.length; _j++) {
                    var t_111 = _k[_j];
                    if (!_this._document._validateSelectionRange(t_111))
                        throw new Gn.b("document-selection-wrong-position: Range from document selection starts or ends at incorrect position.", { range: t_111 });
                } _this._updateMarkers(), _this._updateAttributes(!1); }), _this.listenTo(_this._model.markers, "update", function () { return _this._updateMarkers(); }), _this.listenTo(_this._model, "applyOperation", function (t, e) { var n = e[0]; if (n.isDocumentOperation && "marker" != n.type && "rename" != n.type && "noop" != n.type) {
                    for (; _this._fixGraveyardRangesData.length;) {
                        var _j = _this._fixGraveyardRangesData.shift(), t_112 = _j.liveRange, e_101 = _j.sourcePosition;
                        _this._fixGraveyardSelection(t_112, e_101);
                    }
                    _this._hasChangedRange && (_this._hasChangedRange = !1, _this.fire("change:range", { directChange: !1 })), _this._updateMarkers(), _this._updateAttributes(!1);
                } }, { priority: "lowest" }), _this.listenTo(_this._document, "change", function (t, e) { !function (t, e) { var n = t.document.differ; var _loop_2 = function (i_56) {
                    if ("insert" != i_56.type)
                        return "continue";
                    var n_100 = i_56.position.parent, o_33 = i_56.length === n_100.maxOffset;
                    o_33 && t.enqueueChange(e, function (t) { var e = Array.from(n_100.getAttributeKeys()).filter(function (t) { return t.startsWith(ra); }); for (var _j = 0, e_102 = e; _j < e_102.length; _j++) {
                        var i_57 = e_102[_j];
                        t.removeAttribute(i_57, n_100);
                    } });
                }; for (var _j = 0, _k = n.getChanges(); _j < _k.length; _j++) {
                    var i_56 = _k[_j];
                    _loop_2(i_56);
                } }(_this._model, e); });
                return _this;
            }
            Object.defineProperty(aa.prototype, "isCollapsed", {
                get: function () { return 0 === this._ranges.length ? this._document._getDefaultRange().isCollapsed : _super.prototype.isCollapsed; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(aa.prototype, "anchor", {
                get: function () { return _super.prototype.anchor || this._document._getDefaultRange().start; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(aa.prototype, "focus", {
                get: function () { return _super.prototype.focus || this._document._getDefaultRange().end; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(aa.prototype, "rangeCount", {
                get: function () { return this._ranges.length ? this._ranges.length : 1; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(aa.prototype, "hasOwnRange", {
                get: function () { return this._ranges.length > 0; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(aa.prototype, "isGravityOverridden", {
                get: function () { return !!this._overriddenGravityRegister.size; },
                enumerable: true,
                configurable: true
            });
            aa.prototype.destroy = function () { for (var t_113 = 0; t_113 < this._ranges.length; t_113++)
                this._ranges[t_113].detach(); this.stopListening(); };
            aa.prototype.getRanges = function () { var _j; return __generator(this, function (_k) {
                switch (_k.label) {
                    case 0:
                        if (!this._ranges.length) return [3 /*break*/, 2];
                        return [5 /*yield**/, __values(_super.prototype.getRanges.call(this))];
                    case 1:
                        _j = _k.sent();
                        return [3 /*break*/, 4];
                    case 2: return [4 /*yield*/, this._document._getDefaultRange()];
                    case 3:
                        _j = _k.sent();
                        _k.label = 4;
                    case 4:
                        _j;
                        return [2 /*return*/];
                }
            }); };
            aa.prototype.getFirstRange = function () { return _super.prototype.getFirstRange.call(this) || this._document._getDefaultRange(); };
            aa.prototype.getLastRange = function () { return _super.prototype.getLastRange.call(this) || this._document._getDefaultRange(); };
            aa.prototype.setTo = function (t, e, n) { _super.prototype.setTo.call(this, t, e, n), this._refreshAttributes(); };
            aa.prototype.setFocus = function (t, e) { _super.prototype.setFocus.call(this, t, e), this._refreshAttributes(); };
            aa.prototype.setAttribute = function (t, e) { if (this._setAttribute(t, e)) {
                var e_103 = [t];
                this.fire("change:attribute", { attributeKeys: e_103, directChange: !0 });
            } };
            aa.prototype.removeAttribute = function (t) { if (this._removeAttribute(t)) {
                var e_104 = [t];
                this.fire("change:attribute", { attributeKeys: e_104, directChange: !0 });
            } };
            aa.prototype.overrideGravity = function () { var t = Jn(); return this._overriddenGravityRegister.add(t), 1 === this._overriddenGravityRegister.size && this._refreshAttributes(), t; };
            aa.prototype.restoreGravity = function (t) { if (!this._overriddenGravityRegister.has(t))
                throw new Gn.b("document-selection-gravity-wrong-restore: Attempting to restore the selection gravity for an unknown UID.", { uid: t }); this._overriddenGravityRegister.delete(t), this.isGravityOverridden || this._refreshAttributes(); };
            aa.prototype._refreshAttributes = function () { this._updateAttributes(!0); };
            aa.prototype._popRange = function () { this._ranges.pop().detach(); };
            aa.prototype._pushRange = function (t) { var e = this._prepareRange(t); e && this._ranges.push(e); };
            aa.prototype._prepareRange = function (t) {
                var _this = this;
                if (this._checkRange(t), t.root == this._document.graveyard)
                    return void bs.a.warn("model-selection-range-in-graveyard: Trying to add a Range that is in the graveyard root. Range rejected.");
                var e = oa.fromRange(t);
                return e.on("change:range", function (t, n, i) { _this._hasChangedRange = !0, e.root == _this._document.graveyard && _this._fixGraveyardRangesData.push({ liveRange: e, sourcePosition: i.deletionPosition }); }), e;
            };
            aa.prototype._updateMarkers = function () { var t = []; for (var _j = 0, _k = this._model.markers; _j < _k.length; _j++) {
                var e_105 = _k[_j];
                var n_101 = e_105.getRange();
                for (var _q = 0, _v = this.getRanges(); _q < _v.length; _q++) {
                    var i_58 = _v[_q];
                    n_101.containsRange(i_58, !i_58.isCollapsed) && t.push(e_105);
                }
            } for (var _w = 0, t_114 = t; _w < t_114.length; _w++) {
                var e_106 = t_114[_w];
                this.markers.has(e_106) || this.markers.add(e_106);
            } for (var _x = 0, _y = Array.from(this.markers); _x < _y.length; _x++) {
                var e_107 = _y[_x];
                t.includes(e_107) || this.markers.remove(e_107);
            } };
            aa.prototype._updateAttributes = function (t) { var e = Vs(this._getSurroundingAttributes()), n = Vs(this.getAttributes()); if (t)
                this._attributePriority = new Map, this._attrs = new Map;
            else
                for (var _j = 0, _k = this._attributePriority; _j < _k.length; _j++) {
                    var _q = _k[_j], t_115 = _q[0], e_108 = _q[1];
                    "low" == e_108 && (this._attrs.delete(t_115), this._attributePriority.delete(t_115));
                } this._setAttributesTo(e); var i = []; for (var _v = 0, _w = this.getAttributes(); _v < _w.length; _v++) {
                var _x = _w[_v], t_116 = _x[0], e_109 = _x[1];
                n.has(t_116) && n.get(t_116) === e_109 || i.push(t_116);
            } for (var _y = 0, n_102 = n; _y < n_102.length; _y++) {
                var t_117 = n_102[_y][0];
                this.hasAttribute(t_117) || i.push(t_117);
            } i.length > 0 && this.fire("change:attribute", { attributeKeys: i, directChange: !1 }); };
            aa.prototype._setAttribute = function (t, e, n) {
                if (n === void 0) { n = !0; }
                var i = n ? "normal" : "low";
                return ("low" != i || "normal" != this._attributePriority.get(t)) && (_super.prototype.getAttribute.call(this, t) !== e && (this._attrs.set(t, e), this._attributePriority.set(t, i), !0));
            };
            aa.prototype._removeAttribute = function (t, e) {
                if (e === void 0) { e = !0; }
                var n = e ? "normal" : "low";
                return ("low" != n || "normal" != this._attributePriority.get(t)) && (this._attributePriority.set(t, n), !!_super.prototype.hasAttribute.call(this, t) && (this._attrs.delete(t), !0));
            };
            aa.prototype._setAttributesTo = function (t) { var e = new Set; for (var _j = 0, _k = this.getAttributes(); _j < _k.length; _j++) {
                var _q = _k[_j], e_110 = _q[0], n_103 = _q[1];
                t.get(e_110) !== n_103 && this._removeAttribute(e_110, !1);
            } for (var _v = 0, t_118 = t; _v < t_118.length; _v++) {
                var _w = t_118[_v], n_104 = _w[0], i_59 = _w[1];
                this._setAttribute(n_104, i_59, !1) && e.add(n_104);
            } return e; };
            aa.prototype._getStoredAttributes = function () { var t, _j, _k, e_111; return __generator(this, function (_q) {
                switch (_q.label) {
                    case 0:
                        t = this.getFirstPosition().parent;
                        if (!(this.isCollapsed && t.isEmpty)) return [3 /*break*/, 4];
                        _j = 0, _k = t.getAttributeKeys();
                        _q.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 4];
                        e_111 = _k[_j];
                        if (!e_111.startsWith(ra)) return [3 /*break*/, 3];
                        return [4 /*yield*/, [e_111.substr(ra.length), t.getAttribute(e_111)]];
                    case 2:
                        _q.sent();
                        _q.label = 3;
                    case 3:
                        _j++;
                        return [3 /*break*/, 1];
                    case 4: return [2 /*return*/];
                }
            }); };
            aa.prototype._getSurroundingAttributes = function () { var t = this.getFirstPosition(), e = this._model.schema; var n = null; if (this.isCollapsed) {
                var e_112 = t.textNode ? t.textNode : t.nodeBefore, i_60 = t.textNode ? t.textNode : t.nodeAfter;
                if (this.isGravityOverridden || (n = ca(e_112)), n || (n = ca(i_60)), !this.isGravityOverridden && !n) {
                    var t_120 = e_112;
                    for (; t_120 && !n;)
                        n = ca(t_120 = t_120.previousSibling);
                }
                if (!n) {
                    var t_121 = i_60;
                    for (; t_121 && !n;)
                        n = ca(t_121 = t_121.nextSibling);
                }
                n || (n = this._getStoredAttributes());
            }
            else {
                var t_122 = this.getFirstRange();
                for (var _j = 0, t_119 = t_122; _j < t_119.length; _j++) {
                    var i_61 = t_119[_j];
                    if (i_61.item.is("element") && e.isObject(i_61.item))
                        break;
                    if ("text" == i_61.type) {
                        n = i_61.item.getAttributes();
                        break;
                    }
                }
            } return n; };
            aa.prototype._fixGraveyardSelection = function (t, e) { var n = e.clone(), i = this._model.schema.getNearestSelectionRange(n), o = this._ranges.indexOf(t); if (this._ranges.splice(o, 1), t.detach(), i) {
                var t_123 = this._prepareRange(i);
                this._ranges.splice(o, 0, t_123);
            } };
            return aa;
        }(ta));
        function ca(t) { return t instanceof Fs || t instanceof Bs ? t.getAttributes() : null; }
        var la = /** @class */ (function () {
            function la(t) {
                this._dispatchers = t;
            }
            la.prototype.add = function (t) { for (var _j = 0, _k = this._dispatchers; _j < _k.length; _j++) {
                var e_113 = _k[_j];
                t(e_113);
            } return this; };
            return la;
        }());
        var da = 1, ua = 4;
        var ha = function (t) { return Fn(t, da | ua); };
        var fa = /** @class */ (function (_super) {
            __extends(fa, _super);
            function fa() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            fa.prototype.elementToElement = function (t) { return this.add(function (t) { return (t = ha(t)).view = ga(t.view, "container"), function (e) { e.on("insert:" + t.model, function (t) { return function (e, n, i) { var o = t(n.item, i.writer); if (!o)
                return; if (!i.consumable.consume(n.item, "insert"))
                return; var r = i.mapper.toViewPosition(n.range.start); i.mapper.bindElements(n.item, o), i.writer.insert(r, o); }; }(t.view), { priority: t.converterPriority || "normal" }); }; }(t)); };
            fa.prototype.attributeToElement = function (t) { return this.add(function (t) { var e = "attribute:" + ((t = ha(t)).model.key ? t.model.key : t.model); t.model.name && (e += ":" + t.model.name); if (t.model.values)
                for (var _j = 0, _k = t.model.values; _j < _k.length; _j++) {
                    var e_114 = _k[_j];
                    t.view[e_114] = ga(t.view[e_114], "attribute");
                }
            else
                t.view = ga(t.view, "attribute"); var n = pa(t); return function (i) { i.on(e, function (t) { return function (e, n, i) { var o = t(n.attributeOldValue, i.writer), r = t(n.attributeNewValue, i.writer); if (!o && !r)
                return; if (!i.consumable.consume(n.item, e.name))
                return; var s = i.writer, a = s.document.selection; if (n.item instanceof ta || n.item instanceof sa)
                s.wrap(a.getFirstRange(), r);
            else {
                var t_124 = i.mapper.toViewRange(n.range);
                null !== n.attributeOldValue && o && (t_124 = s.unwrap(t_124, o)), null !== n.attributeNewValue && r && s.wrap(t_124, r);
            } }; }(n), { priority: t.converterPriority || "normal" }); }; }(t)); };
            fa.prototype.attributeToAttribute = function (t) { return this.add(function (t) { var e = "attribute:" + ((t = ha(t)).model.key ? t.model.key : t.model); t.model.name && (e += ":" + t.model.name); if (t.model.values)
                for (var _j = 0, _k = t.model.values; _j < _k.length; _j++) {
                    var e_115 = _k[_j];
                    t.view[e_115] = ba(t.view[e_115]);
                }
            else
                t.view = ba(t.view); var n = pa(t); return function (i) { i.on(e, function (t) { return function (e, n, i) { var o = t(n.attributeOldValue, n), r = t(n.attributeNewValue, n); if (!o && !r)
                return; if (!i.consumable.consume(n.item, e.name))
                return; var s = i.mapper.toViewElement(n.item), a = i.writer; if (s) {
                if (null !== n.attributeOldValue && o)
                    if ("class" == o.key) {
                        var t_129 = Array.isArray(o.value) ? o.value : [o.value];
                        for (var _j = 0, t_125 = t_129; _j < t_125.length; _j++) {
                            var e_116 = t_125[_j];
                            a.removeClass(e_116, s);
                        }
                    }
                    else if ("style" == o.key) {
                        var t_130 = Object.keys(o.value);
                        for (var _k = 0, t_126 = t_130; _k < t_126.length; _k++) {
                            var e_117 = t_126[_k];
                            a.removeStyle(e_117, s);
                        }
                    }
                    else
                        a.removeAttribute(o.key, s);
                if (null !== n.attributeNewValue && r)
                    if ("class" == r.key) {
                        var t_131 = Array.isArray(r.value) ? r.value : [r.value];
                        for (var _q = 0, t_127 = t_131; _q < t_127.length; _q++) {
                            var e_118 = t_127[_q];
                            a.addClass(e_118, s);
                        }
                    }
                    else if ("style" == r.key) {
                        var t_132 = Object.keys(r.value);
                        for (var _v = 0, t_128 = t_132; _v < t_128.length; _v++) {
                            var e_119 = t_128[_v];
                            a.setStyle(e_119, r.value[e_119], s);
                        }
                    }
                    else
                        a.setAttribute(r.key, r.value, s);
            }
            else
                bs.a.warn("conversion-attribute-to-attribute-on-text: Trying to convert text node's attribute with attribute-to-attribute converter."); }; }(n), { priority: t.converterPriority || "normal" }); }; }(t)); };
            fa.prototype.markerToElement = function (t) { return this.add(function (t) { return (t = ha(t)).view = ga(t.view, "ui"), function (e) { e.on("addMarker:" + t.model, function (t) { return function (e, n, i) { n.isOpening = !0; var o = t(n, i.writer); n.isOpening = !1; var r = t(n, i.writer); if (!o || !r)
                return; var s = n.markerRange; if (s.isCollapsed && !i.consumable.consume(s, e.name))
                return; for (var _j = 0, s_13 = s; _j < s_13.length; _j++) {
                var t_133 = s_13[_j];
                if (!i.consumable.consume(t_133.item, e.name))
                    return;
            } var a = i.mapper, c = i.writer; c.insert(a.toViewPosition(s.start), o), i.mapper.bindElementToMarker(o, n.markerName), s.isCollapsed || (c.insert(a.toViewPosition(s.end), r), i.mapper.bindElementToMarker(r, n.markerName)), e.stop(); }; }(t.view), { priority: t.converterPriority || "normal" }), e.on("removeMarker:" + t.model, (t.view, function (t, e, n) { var i = n.mapper.markerNameToElements(e.markerName); if (i) {
                n.mapper.unbindElementsFromMarkerName(e.markerName);
                for (var _j = 0, i_62 = i; _j < i_62.length; _j++) {
                    var t_134 = i_62[_j];
                    n.writer.clear(n.writer.createRangeOn(t_134), t_134);
                }
                n.writer.clearClonedElementsGroup(e.markerName), t.stop();
            } }), { priority: t.converterPriority || "normal" }); }; }(t)); };
            fa.prototype.markerToHighlight = function (t) { return this.add(function (t) { return function (e) { e.on("addMarker:" + t.model, function (t) { return function (e, n, i) { if (!n.item)
                return; if (!(n.item instanceof ta || n.item instanceof sa || n.item.is("textProxy")))
                return; var o = wa(t, n, i); if (!o)
                return; if (!i.consumable.consume(n.item, e.name))
                return; var r = ma(o), s = i.writer, a = s.document.selection; if (n.item instanceof ta || n.item instanceof sa)
                s.wrap(a.getFirstRange(), r, a);
            else {
                var t_135 = i.mapper.toViewRange(n.range), e_120 = s.wrap(t_135, r);
                for (var _j = 0, _k = e_120.getItems(); _j < _k.length; _j++) {
                    var t_136 = _k[_j];
                    if (t_136.is("attributeElement") && t_136.isSimilar(r)) {
                        i.mapper.bindElementToMarker(t_136, n.markerName);
                        break;
                    }
                }
            } }; }(t.view), { priority: t.converterPriority || "normal" }), e.on("addMarker:" + t.model, function (t) { return function (e, n, i) { if (!n.item)
                return; if (!(n.item instanceof Hs))
                return; var o = wa(t, n, i); if (!o)
                return; if (!i.consumable.test(n.item, e.name))
                return; var r = i.mapper.toViewElement(n.item); if (r && r.getCustomProperty("addHighlight")) {
                i.consumable.consume(n.item, e.name);
                for (var _j = 0, _k = Gs._createIn(n.item); _j < _k.length; _j++) {
                    var t_137 = _k[_j];
                    i.consumable.consume(t_137.item, e.name);
                }
                r.getCustomProperty("addHighlight")(r, o, i.writer), i.mapper.bindElementToMarker(r, n.markerName);
            } }; }(t.view), { priority: t.converterPriority || "normal" }), e.on("removeMarker:" + t.model, function (t) { return function (e, n, i) { if (n.markerRange.isCollapsed)
                return; var o = wa(t, n, i); if (!o)
                return; var r = ma(o), s = i.mapper.markerNameToElements(n.markerName); if (s) {
                i.mapper.unbindElementsFromMarkerName(n.markerName);
                for (var _j = 0, s_14 = s; _j < s_14.length; _j++) {
                    var t_138 = s_14[_j];
                    t_138.is("attributeElement") ? i.writer.unwrap(i.writer.createRangeOn(t_138), r) : t_138.getCustomProperty("removeHighlight")(t_138, o.id, i.writer);
                }
                i.writer.clearClonedElementsGroup(n.markerName), e.stop();
            } }; }(t.view), { priority: t.converterPriority || "normal" }); }; }(t)); };
            return fa;
        }(la));
        function ma(t) { var e = new ao("span", t.attributes); return t.classes && e._addClass(t.classes), t.priority && (e._priority = t.priority), e._id = t.id, e; }
        function ga(t, e) { return "function" == typeof t ? t : function (n, i) { return (function (t, e, n) { "string" == typeof t && (t = { name: t }); var i; var o = Object.assign({}, t.attributes); if ("container" == n)
            i = e.createContainerElement(t.name, o);
        else if ("attribute" == n) {
            var n_107 = { priority: t.priority || ao.DEFAULT_PRIORITY };
            i = e.createAttributeElement(t.name, o, n_107);
        }
        else
            i = e.createUIElement(t.name, o); if (t.styles) {
            var n_108 = Object.keys(t.styles);
            for (var _j = 0, n_105 = n_108; _j < n_105.length; _j++) {
                var o_34 = n_105[_j];
                e.setStyle(o_34, t.styles[o_34], i);
            }
        } if (t.classes) {
            var n_109 = t.classes;
            if ("string" == typeof n_109)
                e.addClass(n_109, i);
            else
                for (var _k = 0, n_106 = n_109; _k < n_106.length; _k++) {
                    var t_139 = n_106[_k];
                    e.addClass(t_139, i);
                }
        } return i; })(t, i, e); }; }
        function pa(t) { return t.model.values ? function (e, n) { var i = t.view[e]; return i ? i(e, n) : null; } : t.view; }
        function ba(t) { return "string" == typeof t ? function (e) { return ({ key: t, value: e }); } : "object" == typeof t ? t.value ? function () { return t; } : function (e) { return ({ key: t.key, value: e }); } : t; }
        function wa(t, e, n) { var i = "function" == typeof t ? t(e, n) : t; return i ? (i.priority || (i.priority = 10), i.id || (i.id = e.markerName), i) : null; }
        var _a = /** @class */ (function (_super) {
            __extends(_a, _super);
            function _a() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            _a.prototype.elementToElement = function (t) { return this.add(ka(t)); };
            _a.prototype.elementToAttribute = function (t) { return this.add(function (t) { ya(t = ha(t)); var e = xa(t, !1), n = va(t), i = n ? "element:" + n : "element"; return function (n) { n.on(i, e, { priority: t.converterPriority || "low" }); }; }(t)); };
            _a.prototype.attributeToAttribute = function (t) { return this.add(function (t) { var e = null; ("string" == typeof (t = ha(t)).view || t.view.key) && (e = function (t) { var _j, _k; "string" == typeof t.view && (t.view = { key: t.view }); var e = t.view.key; var n; if ("class" == e || "style" == e) {
                var i_63 = "class" == e ? "classes" : "styles";
                n = (_j = {}, _j[i_63] = t.view.value, _j);
            }
            else {
                var i_64 = void 0 === t.view.value ? /[\s\S]*/ : t.view.value;
                n = { attributes: (_k = {}, _k[e] = i_64, _k) };
            } t.view.name && (n.name = t.view.name); return t.view = n, e; }(t)); ya(t, e); var n = xa(t, !0); return function (e) { e.on("element", n, { priority: t.converterPriority || "low" }); }; }(t)); };
            _a.prototype.elementToMarker = function (t) { return this.add(function (t) { return function (t) { var e = t.model; t.model = (function (t, n) { var i = "string" == typeof e ? e : e(t); return n.createElement("$marker", { "data-name": i }); }); }(t = ha(t)), ka(t); }(t)); };
            return _a;
        }(la));
        function ka(t) { var e = function (t) { var e = new bi(t.view); return function (n, i, o) { var r = e.match(i.viewItem); if (!r)
            return; r.match.name = !0; var s = function (t, e, n) { return t instanceof Function ? t(e, n) : n.createElement(t); }(t.model, i.viewItem, o.writer); if (!s)
            return; if (!o.consumable.test(i.viewItem, r.match))
            return; var a = o.splitToAllowedParent(s, i.modelCursor); if (!a)
            return; o.writer.insert(s, a.position); var c = o.convertChildren(i.viewItem, o.writer.createPositionAt(s, 0)); o.consumable.consume(i.viewItem, r.match), i.modelRange = new Gs(o.writer.createPositionBefore(s), o.writer.createPositionAfter(c.modelCursor.parent)), a.cursorParent ? i.modelCursor = o.writer.createPositionAt(a.cursorParent, 0) : i.modelCursor = i.modelRange.end; }; }(t = ha(t)), n = va(t), i = n ? "element:" + n : "element"; return function (n) { n.on(i, e, { priority: t.converterPriority || "normal" }); }; }
        function va(t) { return "string" == typeof t.view ? t.view : "object" == typeof t.view && "string" == typeof t.view.name ? t.view.name : null; }
        function ya(t, e) {
            if (e === void 0) { e = null; }
            var n = null === e || (function (t) { return t.getAttribute(e); }), i = "object" != typeof t.model ? t.model : t.model.key, o = "object" != typeof t.model || void 0 === t.model.value ? n : t.model.value;
            t.model = { key: i, value: o };
        }
        function xa(t, e) { var n = new bi(t.view); return function (i, o, r) { var s = n.match(o.viewItem); if (!s)
            return; var a = t.model.key, c = "function" == typeof t.model.value ? t.model.value(o.viewItem) : t.model.value; null !== c && (!function (t) { if ("object" == typeof t.view && !va(t))
            return !1; return !t.view.classes && !t.view.attributes && !t.view.styles; }(t) ? delete s.match.name : s.match.name = !0, r.consumable.test(o.viewItem, s.match) && (o.modelRange || (o = Object.assign(o, r.convertChildren(o.viewItem, o.modelCursor))), function (t, e, n, i) { var o = !1; for (var _j = 0, _k = Array.from(t.getItems({ shallow: n })); _j < _k.length; _j++) {
            var r_14 = _k[_j];
            i.schema.checkAttribute(r_14, e.key) && (i.writer.setAttribute(e.key, e.value, r_14), o = !0);
        } return o; }(o.modelRange, { key: a, value: c }, e, r) && r.consumable.consume(o.viewItem, s.match))); }; }
        var Aa = /** @class */ (function () {
            function Aa(t) {
                var _this = this;
                this.model = t, this.view = new js, this.mapper = new Qs, this.downcastDispatcher = new Zs({ mapper: this.mapper });
                var e = this.model.document, n = e.selection, i = this.model.markers;
                this.listenTo(this.model, "_beforeChanges", function () { _this.view._disableRendering(!0); }, { priority: "highest" }), this.listenTo(this.model, "_afterChanges", function () { _this.view._disableRendering(!1); }, { priority: "lowest" }), this.listenTo(e, "change", function () { _this.view.change(function (t) { _this.downcastDispatcher.convertChanges(e.differ, t), _this.downcastDispatcher.convertSelection(n, i, t); }); }, { priority: "low" }), this.listenTo(this.view.document, "selectionChange", function (t, e) { return function (n, i) { var o = i.newSelection, r = new ta, s = []; for (var _j = 0, _k = o.getRanges(); _j < _k.length; _j++) {
                    var t_140 = _k[_j];
                    s.push(e.toModelRange(t_140));
                } r.setTo(s, { backward: o.isBackward }), r.isEqual(t.document.selection) || t.change(function (t) { t.setSelection(r); }); }; }(this.model, this.mapper)), this.downcastDispatcher.on("insert:$text", function (t, e, n) { if (!n.consumable.consume(e.item, "insert"))
                    return; var i = n.writer, o = n.mapper.toViewPosition(e.range.start), r = i.createText(e.item.data); i.insert(o, r); }, { priority: "lowest" }), this.downcastDispatcher.on("remove", function (t, e, n) { var i = n.mapper.toViewPosition(e.position), o = e.position.getShiftedBy(e.length), r = n.mapper.toViewPosition(o, { isPhantom: !0 }), s = n.writer.createRange(i, r), a = n.writer.remove(s.getTrimmed()); for (var _j = 0, _k = n.writer.createRangeIn(a).getItems(); _j < _k.length; _j++) {
                    var t_141 = _k[_j];
                    n.mapper.unbindViewElement(t_141);
                } }, { priority: "low" }), this.downcastDispatcher.on("selection", function (t, e, n) { var i = n.writer, o = i.document.selection; for (var _j = 0, _k = o.getRanges(); _j < _k.length; _j++) {
                    var t_142 = _k[_j];
                    t_142.isCollapsed && t_142.end.parent.document && n.writer.mergeAttributes(t_142.start);
                } i.setSelection(null); }, { priority: "low" }), this.downcastDispatcher.on("selection", function (t, e, n) { var i = e.selection; if (i.isCollapsed)
                    return; if (!n.consumable.consume(i, "selection"))
                    return; var o = []; for (var _j = 0, _k = i.getRanges(); _j < _k.length; _j++) {
                    var t_143 = _k[_j];
                    var e_121 = n.mapper.toViewRange(t_143);
                    o.push(e_121);
                } n.writer.setSelection(o, { backward: i.isBackward }); }, { priority: "low" }), this.downcastDispatcher.on("selection", function (t, e, n) { var i = e.selection; if (!i.isCollapsed)
                    return; if (!n.consumable.consume(i, "selection"))
                    return; var o = n.writer, r = i.getFirstPosition(), s = n.mapper.toViewPosition(r), a = o.breakAttributes(s); o.setSelection(a); }, { priority: "low" }), this.view.document.roots.bindTo(this.model.document.roots).using(function (t) { if ("$graveyard" == t.rootName)
                    return null; var e = new Ki(t.name); return e.rootName = t.rootName, e._document = _this.view.document, _this.mapper.bindElements(t, e), e; });
            }
            Aa.prototype.destroy = function () { this.view.destroy(), this.stopListening(); };
            return Aa;
        }());
        ci(Aa, Fi);
        var Ca = /** @class */ (function () {
            function Ca(t, e) {
                if (e === void 0) { e = []; }
                this._editor = t, this._availablePlugins = new Map, this._plugins = new Map;
                for (var _j = 0, e_122 = e; _j < e_122.length; _j++) {
                    var t_144 = e_122[_j];
                    this._availablePlugins.set(t_144, t_144), t_144.pluginName && this._availablePlugins.set(t_144.pluginName, t_144);
                }
            }
            Ca.prototype[Symbol.iterator] = function () { var _j, _k, t_145, _q; return __generator(this, function (_v) {
                switch (_v.label) {
                    case 0:
                        _j = 0, _k = this._plugins;
                        _v.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 5];
                        t_145 = _k[_j];
                        _q = "function" == typeof t_145[0];
                        if (!_q) return [3 /*break*/, 3];
                        return [4 /*yield*/, t_145];
                    case 2:
                        _q = (_v.sent());
                        _v.label = 3;
                    case 3:
                        _q;
                        _v.label = 4;
                    case 4:
                        _j++;
                        return [3 /*break*/, 1];
                    case 5: return [2 /*return*/];
                }
            }); };
            Ca.prototype.get = function (t) { var e = this._plugins.get(t); if (!e) {
                var e_123 = "plugincollection-plugin-not-loaded: The requested plugin is not loaded.";
                var n_110 = t;
                throw "function" == typeof t && (n_110 = t.pluginName || t.name), new Gn.b(e_123, { plugin: n_110 });
            } return e; };
            Ca.prototype.has = function (t) { return this._plugins.has(t); };
            Ca.prototype.init = function (t, e) {
                if (e === void 0) { e = []; }
                var n = this, i = this._editor, o = new Set, r = [], s = h(t), a = h(e), c = function (t) { var e = []; for (var _j = 0, t_146 = t; _j < t_146.length; _j++) {
                    var n_111 = t_146[_j];
                    u(n_111) || e.push(n_111);
                } return e.length ? e : null; }(t);
                if (c) {
                    var t_147 = "plugincollection-plugin-not-found: Some plugins are not available and could not be loaded.";
                    return bs.a.error(t_147, { plugins: c }), Promise.reject(new Gn.b(t_147, { plugins: c }));
                }
                return Promise.all(s.map(l)).then(function () { return d(r, "init"); }).then(function () { return d(r, "afterInit"); }).then(function () { return r; });
                function l(t) { if (!a.includes(t) && !n._plugins.has(t) && !o.has(t))
                    return function (t) { return new Promise(function (s) { o.add(t), t.requires && t.requires.forEach(function (n) { var i = u(n); if (e.includes(i))
                        throw new Gn.b("plugincollection-required: Cannot load a plugin because one of its dependencies is listed inthe `removePlugins` option.", { plugin: i, requiredBy: t }); l(i); }); var a = new t(i); n._add(t, a), r.push(a), s(); }); }(t).catch(function (e) { throw bs.a.error("plugincollection-load: It was not possible to load the plugin.", { plugin: t }), e; }); }
                function d(t, e) { return t.reduce(function (t, n) { return n[e] ? t.then(n[e].bind(n)) : t; }, Promise.resolve()); }
                function u(t) { return "function" == typeof t ? t : n._availablePlugins.get(t); }
                function h(t) { return t.map(function (t) { return u(t); }).filter(function (t) { return !!t; }); }
            };
            Ca.prototype.destroy = function () { var t = Array.from(this).map(function (_j) {
                var t = _j[1];
                return t;
            }).filter(function (t) { return "function" == typeof t.destroy; }).map(function (t) { return t.destroy(); }); return Promise.all(t); };
            Ca.prototype._add = function (t, e) { this._plugins.set(t, e); var n = t.pluginName; n && (this._plugins.has(n) ? bs.a.warn("plugincollection-plugin-name-conflict: Two plugins with the same name were loaded.", { pluginName: n, plugin1: this._plugins.get(n).constructor, plugin2: t }) : this._plugins.set(n, e)); };
            return Ca;
        }());
        ci(Ca, ei);
        var Ta = /** @class */ (function () {
            function Ta() {
                this._commands = new Map;
            }
            Ta.prototype.add = function (t, e) { this._commands.set(t, e); };
            Ta.prototype.get = function (t) { return this._commands.get(t); };
            Ta.prototype.execute = function (t) {
                var e = [];
                for (var _j = 1; _j < arguments.length; _j++) {
                    e[_j - 1] = arguments[_j];
                }
                var n = this.get(t);
                if (!n)
                    throw new Gn.b("commandcollection-command-not-found: Command does not exist.", { commandName: t });
                n.execute.apply(n, e);
            };
            Ta.prototype.names = function () { return __generator(this, function (_j) {
                switch (_j.label) {
                    case 0: return [5 /*yield**/, __values(this._commands.keys())];
                    case 1:
                        _j.sent();
                        return [2 /*return*/];
                }
            }); };
            Ta.prototype.commands = function () { return __generator(this, function (_j) {
                switch (_j.label) {
                    case 0: return [5 /*yield**/, __values(this._commands.values())];
                    case 1:
                        _j.sent();
                        return [2 /*return*/];
                }
            }); };
            Ta.prototype[Symbol.iterator] = function () { return this._commands[Symbol.iterator](); };
            Ta.prototype.destroy = function () { for (var _j = 0, _k = this.commands(); _j < _k.length; _j++) {
                var t_148 = _k[_j];
                t_148.destroy();
            } };
            return Ta;
        }());
        function Pa(t, e) { var n = Object.keys(window.CKEDITOR_TRANSLATIONS).length; return 1 === n && (t = Object.keys(window.CKEDITOR_TRANSLATIONS)[0]), 0 !== n && function (t, e) { return t in window.CKEDITOR_TRANSLATIONS && e in window.CKEDITOR_TRANSLATIONS[t]; }(t, e) ? window.CKEDITOR_TRANSLATIONS[t][e].replace(/ \[context: [^\]]+\]$/, "") : e.replace(/ \[context: [^\]]+\]$/, ""); }
        window.CKEDITOR_TRANSLATIONS || (window.CKEDITOR_TRANSLATIONS = {});
        var Ma = /** @class */ (function () {
            function Ma(t) {
                var _this = this;
                this.language = t || "en", this.t = (function () {
                    var t = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        t[_j] = arguments[_j];
                    }
                    return _this._t.apply(_this, t);
                });
            }
            Ma.prototype._t = function (t, e) { var n = Pa(this.language, t); return e && (n = n.replace(/%(\d+)/g, function (t, n) { return n < e.length ? e[n] : t; })), n; };
            return Ma;
        }());
        var Ea = /** @class */ (function () {
            function Ea() {
                this._consumables = new Map;
            }
            Ea.prototype.add = function (t, e) { var n; t.is("text") || t.is("documentFragment") ? this._consumables.set(t, !0) : (this._consumables.has(t) ? n = this._consumables.get(t) : (n = new Sa, this._consumables.set(t, n)), n.add(e)); };
            Ea.prototype.test = function (t, e) { var n = this._consumables.get(t); return void 0 === n ? null : t.is("text") || t.is("documentFragment") ? n : n.test(e); };
            Ea.prototype.consume = function (t, e) { return !!this.test(t, e) && (t.is("text") || t.is("documentFragment") ? this._consumables.set(t, !1) : this._consumables.get(t).consume(e), !0); };
            Ea.prototype.revert = function (t, e) { var n = this._consumables.get(t); void 0 !== n && (t.is("text") || t.is("documentFragment") ? this._consumables.set(t, !0) : n.revert(e)); };
            Ea.consumablesFromElement = function (t) { var e = { name: !0, attributes: [], classes: [], styles: [] }, n = t.getAttributeKeys(); for (var _j = 0, n_112 = n; _j < n_112.length; _j++) {
                var t_149 = n_112[_j];
                "style" != t_149 && "class" != t_149 && e.attributes.push(t_149);
            } var i = t.getClassNames(); for (var _k = 0, i_65 = i; _k < i_65.length; _k++) {
                var t_150 = i_65[_k];
                e.classes.push(t_150);
            } var o = t.getStyleNames(); for (var _q = 0, o_35 = o; _q < o_35.length; _q++) {
                var t_151 = o_35[_q];
                e.styles.push(t_151);
            } return e; };
            Ea.createFrom = function (t, e) { if (e || (e = new Ea), t.is("text"))
                return e.add(t), e; t.is("element") && e.add(t, Ea.consumablesFromElement(t)), t.is("documentFragment") && e.add(t); for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
                var n_113 = _k[_j];
                e = Ea.createFrom(n_113, e);
            } return e; };
            return Ea;
        }());
        var Sa = /** @class */ (function () {
            function Sa() {
                this._canConsumeName = null, this._consumables = { attributes: new Map, styles: new Map, classes: new Map };
            }
            Sa.prototype.add = function (t) { t.name && (this._canConsumeName = !0); for (var e_124 in this._consumables)
                e_124 in t && this._add(e_124, t[e_124]); };
            Sa.prototype.test = function (t) { if (t.name && !this._canConsumeName)
                return this._canConsumeName; for (var e_125 in this._consumables)
                if (e_125 in t) {
                    var n_114 = this._test(e_125, t[e_125]);
                    if (!0 !== n_114)
                        return n_114;
                } return !0; };
            Sa.prototype.consume = function (t) { t.name && (this._canConsumeName = !1); for (var e_126 in this._consumables)
                e_126 in t && this._consume(e_126, t[e_126]); };
            Sa.prototype.revert = function (t) { t.name && (this._canConsumeName = !0); for (var e_127 in this._consumables)
                e_127 in t && this._revert(e_127, t[e_127]); };
            Sa.prototype._add = function (t, e) { var n = Wt(e) ? e : [e], i = this._consumables[t]; for (var _j = 0, n_115 = n; _j < n_115.length; _j++) {
                var e_128 = n_115[_j];
                if ("attributes" === t && ("class" === e_128 || "style" === e_128))
                    throw new Gn.b("viewconsumable-invalid-attribute: Classes and styles should be handled separately.");
                i.set(e_128, !0);
            } };
            Sa.prototype._test = function (t, e) { var n = Wt(e) ? e : [e], i = this._consumables[t]; for (var _j = 0, n_116 = n; _j < n_116.length; _j++) {
                var e_129 = n_116[_j];
                if ("attributes" !== t || "class" !== e_129 && "style" !== e_129) {
                    var t_152 = i.get(e_129);
                    if (void 0 === t_152)
                        return null;
                    if (!t_152)
                        return !1;
                }
                else {
                    var t_153 = "class" == e_129 ? "classes" : "styles", n_117 = this._test(t_153, this._consumables[t_153].keys().slice());
                    if (!0 !== n_117)
                        return n_117;
                }
            } return !0; };
            Sa.prototype._consume = function (t, e) { var n = Wt(e) ? e : [e], i = this._consumables[t]; for (var _j = 0, n_118 = n; _j < n_118.length; _j++) {
                var e_130 = n_118[_j];
                if ("attributes" !== t || "class" !== e_130 && "style" !== e_130)
                    i.set(e_130, !1);
                else {
                    var t_154 = "class" == e_130 ? "classes" : "styles";
                    this._consume(t_154, this._consumables[t_154].keys().slice());
                }
            } };
            Sa.prototype._revert = function (t, e) { var n = Wt(e) ? e : [e], i = this._consumables[t]; for (var _j = 0, n_119 = n; _j < n_119.length; _j++) {
                var e_131 = n_119[_j];
                if ("attributes" !== t || "class" !== e_131 && "style" !== e_131) {
                    !1 === i.get(e_131) && i.set(e_131, !0);
                }
                else {
                    var t_155 = "class" == e_131 ? "classes" : "styles";
                    this._revert(t_155, this._consumables[t_155].keys().slice());
                }
            } };
            return Sa;
        }());
        var Ia = /** @class */ (function () {
            function Ia() {
                var _this = this;
                this._sourceDefinitions = {}, this.decorate("checkChild"), this.decorate("checkAttribute"), this.on("checkAttribute", function (t, e) { e[0] = new Na(e[0]); }, { priority: "highest" }), this.on("checkChild", function (t, e) { e[0] = new Na(e[0]), e[1] = _this.getDefinition(e[1]); }, { priority: "highest" });
            }
            Ia.prototype.register = function (t, e) { if (this._sourceDefinitions[t])
                throw new Gn.b("schema-cannot-register-item-twice: A single item cannot be registered twice in the schema.", { itemName: t }); this._sourceDefinitions[t] = [Object.assign({}, e)], this._clearCache(); };
            Ia.prototype.extend = function (t, e) { if (!this._sourceDefinitions[t])
                throw new Gn.b("schema-cannot-extend-missing-item: Cannot extend an item which was not registered yet.", { itemName: t }); this._sourceDefinitions[t].push(Object.assign({}, e)), this._clearCache(); };
            Ia.prototype.getDefinitions = function () { return this._compiledDefinitions || this._compile(), this._compiledDefinitions; };
            Ia.prototype.getDefinition = function (t) { var e; return e = "string" == typeof t ? t : t.is && (t.is("text") || t.is("textProxy")) ? "$text" : t.name, this.getDefinitions()[e]; };
            Ia.prototype.isRegistered = function (t) { return !!this.getDefinition(t); };
            Ia.prototype.isBlock = function (t) { var e = this.getDefinition(t); return !(!e || !e.isBlock); };
            Ia.prototype.isLimit = function (t) { var e = this.getDefinition(t); return !!e && !(!e.isLimit && !e.isObject); };
            Ia.prototype.isObject = function (t) { var e = this.getDefinition(t); return !(!e || !e.isObject); };
            Ia.prototype.isInline = function (t) { var e = this.getDefinition(t); return !(!e || !e.isInline); };
            Ia.prototype.checkChild = function (t, e) { return !!e && this._checkContextMatch(e, t); };
            Ia.prototype.checkAttribute = function (t, e) { var n = this.getDefinition(t.last); return !!n && n.allowAttributes.includes(e); };
            Ia.prototype.checkMerge = function (t, e) {
                if (e === void 0) { e = null; }
                if (t instanceof $s) {
                    var e_132 = t.nodeBefore, n_120 = t.nodeAfter;
                    if (!(e_132 instanceof Hs))
                        throw new Gn.b("schema-check-merge-no-element-before: The node before the merge position must be an element.");
                    if (!(n_120 instanceof Hs))
                        throw new Gn.b("schema-check-merge-no-element-after: The node after the merge position must be an element.");
                    return this.checkMerge(e_132, n_120);
                }
                for (var _j = 0, _k = e.getChildren(); _j < _k.length; _j++) {
                    var n_121 = _k[_j];
                    if (!this.checkChild(t, n_121))
                        return !1;
                }
                return !0;
            };
            Ia.prototype.addChildCheck = function (t) { this.on("checkChild", function (e, _j) {
                var n = _j[0], i = _j[1];
                if (!i)
                    return;
                var o = t(n, i);
                "boolean" == typeof o && (e.stop(), e.return = o);
            }, { priority: "high" }); };
            Ia.prototype.addAttributeCheck = function (t) { this.on("checkAttribute", function (e, _j) {
                var n = _j[0], i = _j[1];
                var o = t(n, i);
                "boolean" == typeof o && (e.stop(), e.return = o);
            }, { priority: "high" }); };
            Ia.prototype.getLimitElement = function (t) { var e; if (t instanceof $s)
                e = t.parent;
            else {
                e = (t instanceof Gs ? [t] : Array.from(t.getRanges())).reduce(function (t, e) { var n = e.getCommonAncestor(); return t ? t.getCommonAncestor(n, { includeSelf: !0 }) : n; }, null);
            } for (; !this.isLimit(e) && e.parent;)
                e = e.parent; return e; };
            Ia.prototype.checkAttributeInSelection = function (t, e) { if (t.isCollapsed) {
                var n_123 = t.getFirstPosition().getAncestors().concat([new Bs("", t.getAttributes())]);
                return this.checkAttribute(n_123, e);
            } {
                var n_124 = t.getRanges();
                for (var _j = 0, n_122 = n_124; _j < n_122.length; _j++) {
                    var t_157 = n_122[_j];
                    for (var _k = 0, t_156 = t_157; _k < t_156.length; _k++) {
                        var n_125 = t_156[_k];
                        if (this.checkAttribute(n_125.item, e))
                            return !0;
                    }
                }
            } return !1; };
            Ia.prototype.getValidRanges = function (t, e) { var _j, t_158, n_126; return __generator(this, function (_k) {
                switch (_k.label) {
                    case 0:
                        t = function (t) { var _j, t_159, e_133; return __generator(this, function (_k) {
                            switch (_k.label) {
                                case 0:
                                    _j = 0, t_159 = t;
                                    _k.label = 1;
                                case 1:
                                    if (!(_j < t_159.length)) return [3 /*break*/, 4];
                                    e_133 = t_159[_j];
                                    return [5 /*yield**/, __values(e_133.getMinimalFlatRanges())];
                                case 2:
                                    _k.sent();
                                    _k.label = 3;
                                case 3:
                                    _j++;
                                    return [3 /*break*/, 1];
                                case 4: return [2 /*return*/];
                            }
                        }); }(t);
                        _j = 0, t_158 = t;
                        _k.label = 1;
                    case 1:
                        if (!(_j < t_158.length)) return [3 /*break*/, 4];
                        n_126 = t_158[_j];
                        return [5 /*yield**/, __values(this._getValidRangesForRange(n_126, e))];
                    case 2:
                        _k.sent();
                        _k.label = 3;
                    case 3:
                        _j++;
                        return [3 /*break*/, 1];
                    case 4: return [2 /*return*/];
                }
            }); };
            Ia.prototype.getNearestSelectionRange = function (t, e) {
                if (e === void 0) { e = "both"; }
                if (this.checkChild(t, "$text"))
                    return new Gs(t);
                var n, i;
                "both" != e && "backward" != e || (n = new qs({ startPosition: t, direction: "backward" })), "both" != e && "forward" != e || (i = new qs({ startPosition: t }));
                for (var _j = 0, _k = function (t, e) { var n, e_134, _j, t_160, _k; return __generator(this, function (_q) {
                    switch (_q.label) {
                        case 0:
                            n = !1;
                            _q.label = 1;
                        case 1:
                            if (!!n) return [3 /*break*/, 8];
                            if (!(n = !0, t)) return [3 /*break*/, 4];
                            e_134 = t.next();
                            _j = e_134.done;
                            if (_j) return [3 /*break*/, 3];
                            n = !1;
                            return [4 /*yield*/, { walker: t, value: e_134.value }];
                        case 2:
                            _j = (_q.sent());
                            _q.label = 3;
                        case 3:
                            _j;
                            _q.label = 4;
                        case 4:
                            if (!e) return [3 /*break*/, 7];
                            t_160 = e.next();
                            _k = t_160.done;
                            if (_k) return [3 /*break*/, 6];
                            n = !1;
                            return [4 /*yield*/, { walker: e, value: t_160.value }];
                        case 5:
                            _k = (_q.sent());
                            _q.label = 6;
                        case 6:
                            _k;
                            _q.label = 7;
                        case 7: return [3 /*break*/, 1];
                        case 8: return [2 /*return*/];
                    }
                }); }(n, i); _j < _k.length; _j++) {
                    var t_161 = _k[_j];
                    var e_135 = t_161.walker == n ? "elementEnd" : "elementStart", i_66 = t_161.value;
                    if (i_66.type == e_135 && this.isObject(i_66.item))
                        return Gs._createOn(i_66.item);
                    if (this.checkChild(i_66.nextPosition, "$text"))
                        return new Gs(i_66.nextPosition);
                }
                return null;
            };
            Ia.prototype.findAllowedParent = function (t, e) { var n = t.parent; for (; n;) {
                if (this.checkChild(n, e))
                    return n;
                if (this.isLimit(n))
                    return null;
                n = n.parent;
            } return null; };
            Ia.prototype.removeDisallowedAttributes = function (t, e) { for (var _j = 0, t_162 = t; _j < t_162.length; _j++) {
                var n_127 = t_162[_j];
                for (var _k = 0, _q = n_127.getAttributeKeys(); _k < _q.length; _k++) {
                    var t_163 = _q[_k];
                    this.checkAttribute(n_127, t_163) || e.removeAttribute(t_163, n_127);
                }
                n_127.is("element") && this.removeDisallowedAttributes(n_127.getChildren(), e);
            } };
            Ia.prototype.createContext = function (t) { return new Na(t); };
            Ia.prototype._clearCache = function () { this._compiledDefinitions = null; };
            Ia.prototype._compile = function () { var t = {}, e = this._sourceDefinitions, n = Object.keys(e); for (var _j = 0, n_128 = n; _j < n_128.length; _j++) {
                var i_67 = n_128[_j];
                t[i_67] = Oa(e[i_67], i_67);
            } for (var _k = 0, n_129 = n; _k < n_129.length; _k++) {
                var e_136 = n_129[_k];
                Ra(t, e_136);
            } for (var _q = 0, n_130 = n; _q < n_130.length; _q++) {
                var e_137 = n_130[_q];
                Da(t, e_137);
            } for (var _v = 0, n_131 = n; _v < n_131.length; _v++) {
                var e_138 = n_131[_v];
                La(t, e_138), ja(t, e_138);
            } for (var _w = 0, n_132 = n; _w < n_132.length; _w++) {
                var e_139 = n_132[_w];
                Va(t, e_139), za(t, e_139);
            } this._compiledDefinitions = t; };
            Ia.prototype._checkContextMatch = function (t, e, n) {
                if (n === void 0) { n = e.length - 1; }
                var i = e.getItem(n);
                if (t.allowIn.includes(i.name)) {
                    if (0 == n)
                        return !0;
                    {
                        var t_164 = this.getDefinition(i);
                        return this._checkContextMatch(t_164, e, n - 1);
                    }
                }
                return !1;
            };
            Ia.prototype._getValidRangesForRange = function (t, e) { var n, i, _j, _k, o_36, _q, _v, _w, _x; return __generator(this, function (_y) {
                switch (_y.label) {
                    case 0:
                        n = t.start, i = t.start;
                        _j = 0, _k = t.getItems({ shallow: !0 });
                        _y.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 8];
                        o_36 = _k[_j];
                        _q = o_36.is("element");
                        if (!_q) return [3 /*break*/, 3];
                        return [5 /*yield**/, __values(this._getValidRangesForRange(Gs._createIn(o_36), e))];
                    case 2:
                        _q = (_y.sent());
                        _y.label = 3;
                    case 3:
                        _q;
                        _v = this.checkAttribute(o_36, e);
                        if (_v) return [3 /*break*/, 6];
                        _w = n.isEqual(i);
                        if (_w) return [3 /*break*/, 5];
                        return [4 /*yield*/, new Gs(n, i)];
                    case 4:
                        _w = (_y.sent());
                        _y.label = 5;
                    case 5:
                        _v = (_w, n = $s._createAfter(o_36));
                        _y.label = 6;
                    case 6:
                        _v, i = $s._createAfter(o_36);
                        _y.label = 7;
                    case 7:
                        _j++;
                        return [3 /*break*/, 1];
                    case 8:
                        _x = n.isEqual(i);
                        if (_x) return [3 /*break*/, 10];
                        return [4 /*yield*/, new Gs(n, i)];
                    case 9:
                        _x = (_y.sent());
                        _y.label = 10;
                    case 10:
                        _x;
                        return [2 /*return*/];
                }
            }); };
            return Ia;
        }());
        ci(Ia, Fi);
        var Na = /** @class */ (function () {
            function Na(t) {
                if (t instanceof Na)
                    return t;
                "string" == typeof t ? t = [t] : Array.isArray(t) || (t = t.getAncestors({ includeSelf: !0 })), t[0] && "string" != typeof t[0] && t[0].is("documentFragment") && t.shift(), this._items = t.map(Ua);
            }
            Object.defineProperty(Na.prototype, "length", {
                get: function () { return this._items.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Na.prototype, "last", {
                get: function () { return this._items[this._items.length - 1]; },
                enumerable: true,
                configurable: true
            });
            Na.prototype[Symbol.iterator] = function () { return this._items[Symbol.iterator](); };
            Na.prototype.push = function (t) { var e = new Na([t]); return e._items = this._items.concat(e._items), e; };
            Na.prototype.getItem = function (t) { return this._items[t]; };
            Na.prototype.getNames = function () { return __generator(this, function (_j) {
                switch (_j.label) {
                    case 0: return [5 /*yield**/, __values(this._items.map(function (t) { return t.name; }))];
                    case 1:
                        _j.sent();
                        return [2 /*return*/];
                }
            }); };
            Na.prototype.endsWith = function (t) { return Array.from(this.getNames()).join(" ").endsWith(t); };
            return Na;
        }());
        function Oa(t, e) { var n = { name: e, allowIn: [], allowContentOf: [], allowWhere: [], allowAttributes: [], allowAttributesOf: [], inheritTypesFrom: [] }; return function (t, e) { for (var _j = 0, t_165 = t; _j < t_165.length; _j++) {
            var n_133 = t_165[_j];
            var t_167 = Object.keys(n_133).filter(function (t) { return t.startsWith("is"); });
            for (var _k = 0, t_166 = t_167; _k < t_166.length; _k++) {
                var i_68 = t_166[_k];
                e[i_68] = n_133[i_68];
            }
        } }(t, n), Ba(t, n, "allowIn"), Ba(t, n, "allowContentOf"), Ba(t, n, "allowWhere"), Ba(t, n, "allowAttributes"), Ba(t, n, "allowAttributesOf"), Ba(t, n, "inheritTypesFrom"), function (t, e) { for (var _j = 0, t_168 = t; _j < t_168.length; _j++) {
            var n_134 = t_168[_j];
            var t_169 = n_134.inheritAllFrom;
            t_169 && (e.allowContentOf.push(t_169), e.allowWhere.push(t_169), e.allowAttributesOf.push(t_169), e.inheritTypesFrom.push(t_169));
        } }(t, n), n; }
        function Ra(t, e) { for (var _j = 0, _k = t[e].allowContentOf; _j < _k.length; _j++) {
            var n_135 = _k[_j];
            if (t[n_135]) {
                Fa(t, n_135).forEach(function (t) { t.allowIn.push(e); });
            }
        } delete t[e].allowContentOf; }
        function Da(t, e) { var _j; for (var _k = 0, _q = t[e].allowWhere; _k < _q.length; _k++) {
            var n_136 = _q[_k];
            var i_69 = t[n_136];
            if (i_69) {
                var n_137 = i_69.allowIn;
                (_j = t[e].allowIn).push.apply(_j, n_137);
            }
        } delete t[e].allowWhere; }
        function La(t, e) { var _j; for (var _k = 0, _q = t[e].allowAttributesOf; _k < _q.length; _k++) {
            var n_138 = _q[_k];
            var i_70 = t[n_138];
            if (i_70) {
                var n_139 = i_70.allowAttributes;
                (_j = t[e].allowAttributes).push.apply(_j, n_139);
            }
        } delete t[e].allowAttributesOf; }
        function ja(t, e) { var n = t[e]; for (var _j = 0, _k = n.inheritTypesFrom; _j < _k.length; _j++) {
            var e_140 = _k[_j];
            var i_71 = t[e_140];
            if (i_71) {
                var t_171 = Object.keys(i_71).filter(function (t) { return t.startsWith("is"); });
                for (var _q = 0, t_170 = t_171; _q < t_170.length; _q++) {
                    var e_141 = t_170[_q];
                    e_141 in n || (n[e_141] = i_71[e_141]);
                }
            }
        } delete n.inheritTypesFrom; }
        function Va(t, e) { var n = t[e], i = n.allowIn.filter(function (e) { return t[e]; }); n.allowIn = Array.from(new Set(i)); }
        function za(t, e) { var n = t[e]; n.allowAttributes = Array.from(new Set(n.allowAttributes)); }
        function Ba(t, e, n) { var _j; for (var _k = 0, t_172 = t; _k < t_172.length; _k++) {
            var i_72 = t_172[_k];
            "string" == typeof i_72[n] ? e[n].push(i_72[n]) : Array.isArray(i_72[n]) && (_j = e[n]).push.apply(_j, i_72[n]);
        } }
        function Fa(t, e) { var n = t[e]; return function (t) { return Object.keys(t).map(function (e) { return t[e]; }); }(t).filter(function (t) { return t.allowIn.includes(n.name); }); }
        function Ua(t) { return "string" == typeof t ? { name: t, getAttributeKeys: function () { return __generator(this, function (_j) {
                return [2 /*return*/];
            }); }, getAttribute: function () { } } : { name: t.is("element") ? t.name : "$text", getAttributeKeys: function () { return __generator(this, function (_j) {
                switch (_j.label) {
                    case 0: return [5 /*yield**/, __values(t.getAttributeKeys())];
                    case 1:
                        _j.sent();
                        return [2 /*return*/];
                }
            }); }, getAttribute: function (e) { return t.getAttribute(e); } }; }
        var Ha = /** @class */ (function () {
            function Ha(t) {
                if (t === void 0) { t = {}; }
                this._removeIfEmpty = new Set, this._modelCursor = null, this.conversionApi = Object.assign({}, t), this.conversionApi.convertItem = this._convertItem.bind(this), this.conversionApi.convertChildren = this._convertChildren.bind(this), this.conversionApi.splitToAllowedParent = this._splitToAllowedParent.bind(this);
            }
            Ha.prototype.convert = function (t, e, n) {
                if (n === void 0) { n = ["$root"]; }
                this.fire("viewCleanup", t), this._modelCursor = function (t, e) { var n; for (var _j = 0, _k = new Na(t); _j < _k.length; _j++) {
                    var i_73 = _k[_j];
                    var t_173 = {};
                    for (var _q = 0, _v = i_73.getAttributeKeys(); _q < _v.length; _q++) {
                        var e_142 = _v[_q];
                        t_173[e_142] = i_73.getAttribute(e_142);
                    }
                    var o_37 = e.createElement(i_73.name, t_173);
                    n && e.append(o_37, n), n = $s._createAt(o_37, 0);
                } return n; }(n, e), this.conversionApi.writer = e, this.conversionApi.consumable = Ea.createFrom(t), this.conversionApi.store = {};
                var i = this._convertItem(t, this._modelCursor).modelRange, o = e.createDocumentFragment();
                if (i) {
                    this._removeEmptyElements();
                    for (var _j = 0, _k = Array.from(this._modelCursor.parent.getChildren()); _j < _k.length; _j++) {
                        var t_174 = _k[_j];
                        e.append(t_174, o);
                    }
                    o.markers = function (t, e) { var n = new Set, i = new Map, o = Gs._createIn(t).getItems(); for (var _j = 0, o_38 = o; _j < o_38.length; _j++) {
                        var t_175 = o_38[_j];
                        "$marker" == t_175.name && n.add(t_175);
                    } for (var _k = 0, n_140 = n; _k < n_140.length; _k++) {
                        var t_176 = n_140[_k];
                        var n_141 = t_176.getAttribute("data-name"), o_39 = e.createPositionBefore(t_176);
                        i.has(n_141) ? i.get(n_141).end = o_39.clone() : i.set(n_141, new Gs(o_39.clone())), e.remove(t_176);
                    } return i; }(o, e);
                }
                return this._modelCursor = null, this._removeIfEmpty.clear(), this.conversionApi.writer = null, this.conversionApi.store = null, o;
            };
            Ha.prototype._convertItem = function (t, e) { var n = Object.assign({ viewItem: t, modelCursor: e, modelRange: null }); if (t.is("element") ? this.fire("element:" + t.name, n, this.conversionApi) : t.is("text") ? this.fire("text", n, this.conversionApi) : this.fire("documentFragment", n, this.conversionApi), n.modelRange && !(n.modelRange instanceof Gs))
                throw new Gn.b("view-conversion-dispatcher-incorrect-result: Incorrect conversion result was dropped."); return { modelRange: n.modelRange, modelCursor: n.modelCursor }; };
            Ha.prototype._convertChildren = function (t, e) { var n = new Gs(e); var i = e; for (var _j = 0, _k = Array.from(t.getChildren()); _j < _k.length; _j++) {
                var e_143 = _k[_j];
                var t_177 = this._convertItem(e_143, i);
                t_177.modelRange instanceof Gs && (n.end = t_177.modelRange.end, i = t_177.modelCursor);
            } return { modelRange: n, modelCursor: i }; };
            Ha.prototype._splitToAllowedParent = function (t, e) { var n = this.conversionApi.schema.findAllowedParent(e, t); if (!n)
                return null; if (n === e.parent)
                return { position: e }; if (this._modelCursor.parent.getAncestors().includes(n))
                return null; var i = this.conversionApi.writer.split(e, n); for (var _j = 0, _k = i.range.getPositions(); _j < _k.length; _j++) {
                var t_178 = _k[_j];
                t_178.isEqual(i.position) || this._removeIfEmpty.add(t_178.parent);
            } return { position: i.position, cursorParent: i.range.end.parent }; };
            Ha.prototype._removeEmptyElements = function () { var t = !1; for (var _j = 0, _k = this._removeIfEmpty; _j < _k.length; _j++) {
                var e_144 = _k[_j];
                e_144.isEmpty && (this.conversionApi.writer.remove(e_144), this._removeIfEmpty.delete(e_144), t = !0);
            } t && this._removeEmptyElements(); };
            return Ha;
        }());
        ci(Ha, ei);
        var qa = /** @class */ (function () {
            function qa(t, e) {
                var _this = this;
                this.model = t, this.processor = e, this.mapper = new Qs, this.downcastDispatcher = new Zs({ mapper: this.mapper }), this.downcastDispatcher.on("insert:$text", function (t, e, n) { if (!n.consumable.consume(e.item, "insert"))
                    return; var i = n.writer, o = n.mapper.toViewPosition(e.range.start), r = i.createText(e.item.data); i.insert(o, r); }, { priority: "lowest" }), this.upcastDispatcher = new Ha({ schema: t.schema }), this.upcastDispatcher.on("text", function (t, e, n) { if (n.schema.checkChild(e.modelCursor, "$text") && n.consumable.consume(e.viewItem)) {
                    var t_179 = n.writer.createText(e.viewItem.data);
                    n.writer.insert(t_179, e.modelCursor), e.modelRange = Gs._createFromPositionAndShift(e.modelCursor, t_179.offsetSize), e.modelCursor = e.modelRange.end;
                } }, { priority: "lowest" }), this.upcastDispatcher.on("element", function (t, e, n) { if (!e.modelRange && n.consumable.consume(e.viewItem, { name: !0 })) {
                    var _j = n.convertChildren(e.viewItem, e.modelCursor), t_180 = _j.modelRange, i_74 = _j.modelCursor;
                    e.modelRange = t_180, e.modelCursor = i_74;
                } }, { priority: "lowest" }), this.upcastDispatcher.on("documentFragment", function (t, e, n) { if (!e.modelRange && n.consumable.consume(e.viewItem, { name: !0 })) {
                    var _j = n.convertChildren(e.viewItem, e.modelCursor), t_181 = _j.modelRange, i_75 = _j.modelCursor;
                    e.modelRange = t_181, e.modelCursor = i_75;
                } }, { priority: "lowest" }), this.decorate("init"), this.on("init", function () { _this.fire("ready"); }, { priority: "lowest" });
            }
            qa.prototype.get = function (t) { var _j = t || {}, _k = _j.rootName, e = _k === void 0 ? "main" : _k, _q = _j.trim, n = _q === void 0 ? "empty" : _q; if (!this._checkIfRootsExists([e]))
                throw new Gn.b("datacontroller-get-non-existent-root: Attempting to get data from a non-existing root."); var i = this.model.document.getRoot(e); return "empty" !== n || this.model.hasContent(i, { ignoreWhitespaces: !0 }) ? this.stringify(i) : ""; };
            qa.prototype.stringify = function (t) { var e = this.toView(t); return this.processor.toData(e); };
            qa.prototype.toView = function (t) { this.mapper.clearBindings(); var e = Gs._createIn(t), n = new Ao, i = new Co(new ro); if (this.mapper.bindElements(t, n), this.downcastDispatcher.convertInsert(e, i), !t.is("documentFragment")) {
                var e_146 = function (t) { var e = [], n = t.root.document; if (!n)
                    return []; var i = Gs._createIn(t); for (var _j = 0, _k = n.model.markers; _j < _k.length; _j++) {
                    var t_182 = _k[_j];
                    var n_142 = i.getIntersection(t_182.getRange());
                    n_142 && e.push([t_182.name, n_142]);
                } return e; }(t);
                for (var _j = 0, e_145 = e_146; _j < e_145.length; _j++) {
                    var _k = e_145[_j], t_183 = _k[0], n_143 = _k[1];
                    this.downcastDispatcher.convertMarkerAdd(t_183, n_143, i);
                }
            } return n; };
            qa.prototype.init = function (t) {
                var _this = this;
                if (this.model.document.version)
                    throw new Gn.b("datacontroller-init-document-not-empty: Trying to set initial data to not empty document.");
                var e = {};
                if ("string" == typeof t ? e.main = t : e = t, !this._checkIfRootsExists(Object.keys(e)))
                    throw new Gn.b("datacontroller-init-non-existent-root: Attempting to init data on a non-existing root.");
                return this.model.enqueueChange("transparent", function (t) { for (var _j = 0, _k = Object.keys(e); _j < _k.length; _j++) {
                    var n_144 = _k[_j];
                    var i_76 = _this.model.document.getRoot(n_144);
                    t.insert(_this.parse(e[n_144], i_76), i_76, 0);
                } }), Promise.resolve();
            };
            qa.prototype.set = function (t) {
                var _this = this;
                var e = {};
                if ("string" == typeof t ? e.main = t : e = t, !this._checkIfRootsExists(Object.keys(e)))
                    throw new Gn.b("datacontroller-set-non-existent-root: Attempting to set data on a non-existing root.");
                this.model.enqueueChange("transparent", function (t) { t.setSelection(null), t.removeSelectionAttribute(_this.model.document.selection.getAttributeKeys()); for (var _j = 0, _k = Object.keys(e); _j < _k.length; _j++) {
                    var n_145 = _k[_j];
                    var i_77 = _this.model.document.getRoot(n_145);
                    t.remove(t.createRangeIn(i_77)), t.insert(_this.parse(e[n_145], i_77), i_77, 0);
                } });
            };
            qa.prototype.parse = function (t, e) {
                if (e === void 0) { e = "$root"; }
                var n = this.processor.toView(t);
                return this.toModel(n, e);
            };
            qa.prototype.toModel = function (t, e) {
                var _this = this;
                if (e === void 0) { e = "$root"; }
                return this.model.change(function (n) { return _this.upcastDispatcher.convert(t, n, e); });
            };
            qa.prototype.destroy = function () { this.stopListening(); };
            qa.prototype._checkIfRootsExists = function (t) { for (var _j = 0, t_184 = t; _j < t_184.length; _j++) {
                var e_147 = t_184[_j];
                if (!this.model.document.getRootNames().includes(e_147))
                    return !1;
            } return !0; };
            return qa;
        }());
        ci(qa, Fi);
        var Wa = /** @class */ (function () {
            function Wa(t, e) {
                this._helpers = new Map, this._downcast = Array.isArray(t) ? t : [t], this._createConversionHelpers({ name: "downcast", dispatchers: this._downcast, isDowncast: !0 }), this._upcast = Array.isArray(e) ? e : [e], this._createConversionHelpers({ name: "upcast", dispatchers: this._upcast, isDowncast: !1 });
            }
            Wa.prototype.addAlias = function (t, e) { var n = this._downcast.includes(e); if (!this._upcast.includes(e) && !n)
                throw new Gn.b("conversion-add-alias-dispatcher-not-registered: Trying to register and alias for a dispatcher that nas not been registered."); this._createConversionHelpers({ name: t, dispatchers: [e], isDowncast: n }); };
            Wa.prototype.for = function (t) { if (!this._helpers.has(t))
                throw new Gn.b("conversion-for-unknown-group: Trying to add a converter to an unknown dispatchers group."); return this._helpers.get(t); };
            Wa.prototype.elementToElement = function (t) { this.for("downcast").elementToElement(t); for (var _j = 0, _k = Ya(t); _j < _k.length; _j++) {
                var _q = _k[_j], e_148 = _q.model, n_146 = _q.view;
                this.for("upcast").elementToElement({ model: e_148, view: n_146, converterPriority: t.converterPriority });
            } };
            Wa.prototype.attributeToElement = function (t) { this.for("downcast").attributeToElement(t); for (var _j = 0, _k = Ya(t); _j < _k.length; _j++) {
                var _q = _k[_j], e_149 = _q.model, n_147 = _q.view;
                this.for("upcast").elementToAttribute({ view: n_147, model: e_149, converterPriority: t.converterPriority });
            } };
            Wa.prototype.attributeToAttribute = function (t) { this.for("downcast").attributeToAttribute(t); for (var _j = 0, _k = Ya(t); _j < _k.length; _j++) {
                var _q = _k[_j], e_150 = _q.model, n_148 = _q.view;
                this.for("upcast").attributeToAttribute({ view: n_148, model: e_150 });
            } };
            Wa.prototype._createConversionHelpers = function (_j) {
                var t = _j.name, e = _j.dispatchers, n = _j.isDowncast;
                if (this._helpers.has(t))
                    throw new Gn.b("conversion-group-exists: Trying to register a group name that has already been registered.");
                var i = n ? new fa(e) : new _a(e);
                this._helpers.set(t, i);
            };
            return Wa;
        }());
        function Ya(t) { var _j, _k, e_151; return __generator(this, function (_q) {
            switch (_q.label) {
                case 0:
                    if (!t.model.values) return [3 /*break*/, 5];
                    _j = 0, _k = t.model.values;
                    _q.label = 1;
                case 1:
                    if (!(_j < _k.length)) return [3 /*break*/, 4];
                    e_151 = _k[_j];
                    return [5 /*yield**/, __values($a({ key: t.model.key, value: e_151 }, t.view[e_151], t.upcastAlso ? t.upcastAlso[e_151] : void 0))];
                case 2:
                    _q.sent();
                    _q.label = 3;
                case 3:
                    _j++;
                    return [3 /*break*/, 1];
                case 4: return [3 /*break*/, 7];
                case 5: return [5 /*yield**/, __values($a(t.model, t.view, t.upcastAlso))];
                case 6:
                    _q.sent();
                    _q.label = 7;
                case 7: return [2 /*return*/];
            }
        }); }
        function $a(t, e, n) { var _j, n_149, e_152; return __generator(this, function (_k) {
            switch (_k.label) {
                case 0: return [4 /*yield*/, { model: t, view: e }];
                case 1:
                    if (!(_k.sent(), n)) return [3 /*break*/, 5];
                    n = Array.isArray(n) ? n : [n];
                    _j = 0, n_149 = n;
                    _k.label = 2;
                case 2:
                    if (!(_j < n_149.length)) return [3 /*break*/, 5];
                    e_152 = n_149[_j];
                    return [4 /*yield*/, { model: t, view: e_152 }];
                case 3:
                    _k.sent();
                    _k.label = 4;
                case 4:
                    _j++;
                    return [3 /*break*/, 2];
                case 5: return [2 /*return*/];
            }
        }); }
        var Ga = /** @class */ (function () {
            function Ga(t) {
                if (t === void 0) { t = "default"; }
                this.operations = [], this.type = t;
            }
            Object.defineProperty(Ga.prototype, "baseVersion", {
                get: function () { for (var _j = 0, _k = this.operations; _j < _k.length; _j++) {
                    var t_185 = _k[_j];
                    if (null !== t_185.baseVersion)
                        return t_185.baseVersion;
                } return null; },
                enumerable: true,
                configurable: true
            });
            Ga.prototype.addOperation = function (t) { return t.batch = this, this.operations.push(t), t; };
            return Ga;
        }());
        var Qa = /** @class */ (function () {
            function Qa(t) {
                this.baseVersion = t, this.isDocumentOperation = null !== this.baseVersion, this.batch = null;
            }
            Qa.prototype._validate = function () { };
            Qa.prototype.toJSON = function () { var t = Object.assign({}, this); return t.__className = this.constructor.className, delete t.batch, delete t.isDocumentOperation, t; };
            Object.defineProperty(Qa, "className", {
                get: function () { return "Operation"; },
                enumerable: true,
                configurable: true
            });
            Qa.fromJSON = function (t) { return new this(t.baseVersion); };
            return Qa;
        }());
        var Ka = /** @class */ (function () {
            function Ka(t) {
                this.markers = new Map, this._children = new Us, t && this._insertChild(0, t);
            }
            Ka.prototype[Symbol.iterator] = function () { return this.getChildren(); };
            Object.defineProperty(Ka.prototype, "childCount", {
                get: function () { return this._children.length; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Ka.prototype, "maxOffset", {
                get: function () { return this._children.maxOffset; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Ka.prototype, "isEmpty", {
                get: function () { return 0 === this.childCount; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Ka.prototype, "root", {
                get: function () { return this; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Ka.prototype, "parent", {
                get: function () { return null; },
                enumerable: true,
                configurable: true
            });
            Ka.prototype.is = function (t) { return "documentFragment" == t; };
            Ka.prototype.getChild = function (t) { return this._children.getNode(t); };
            Ka.prototype.getChildren = function () { return this._children[Symbol.iterator](); };
            Ka.prototype.getChildIndex = function (t) { return this._children.getNodeIndex(t); };
            Ka.prototype.getChildStartOffset = function (t) { return this._children.getNodeStartOffset(t); };
            Ka.prototype.getPath = function () { return []; };
            Ka.prototype.getNodeByPath = function (t) { var e = this; for (var _j = 0, t_186 = t; _j < t_186.length; _j++) {
                var n_150 = t_186[_j];
                e = e.getChild(e.offsetToIndex(n_150));
            } return e; };
            Ka.prototype.offsetToIndex = function (t) { return this._children.offsetToIndex(t); };
            Ka.prototype.toJSON = function () { var t = []; for (var _j = 0, _k = this._children; _j < _k.length; _j++) {
                var e_153 = _k[_j];
                t.push(e_153.toJSON());
            } return t; };
            Ka.fromJSON = function (t) { var e = []; for (var _j = 0, t_187 = t; _j < t_187.length; _j++) {
                var n_151 = t_187[_j];
                n_151.name ? e.push(Hs.fromJSON(n_151)) : e.push(Bs.fromJSON(n_151));
            } return new Ka(e); };
            Ka.prototype._appendChild = function (t) { this._insertChild(this.childCount, t); };
            Ka.prototype._insertChild = function (t, e) { var n = function (t) { if ("string" == typeof t)
                return [new Bs(t)]; pi(t) || (t = [t]); return Array.from(t).map(function (t) { return "string" == typeof t ? new Bs(t) : t instanceof Fs ? new Bs(t.data, t.getAttributes()) : t; }); }(e); for (var _j = 0, n_152 = n; _j < n_152.length; _j++) {
                var t_188 = n_152[_j];
                null !== t_188.parent && t_188._remove(), t_188.parent = this;
            } this._children._insertNodes(t, n); };
            Ka.prototype._removeChildren = function (t, e) {
                if (e === void 0) { e = 1; }
                var n = this._children._removeNodes(t, e);
                for (var _j = 0, n_153 = n; _j < n_153.length; _j++) {
                    var t_189 = n_153[_j];
                    t_189.parent = null;
                }
                return n;
            };
            return Ka;
        }());
        function Ja(t, e) { var n = (e = tc(e)).reduce(function (t, e) { return t + e.offsetSize; }, 0), i = t.parent; nc(t); var o = t.index; return i._insertChild(o, e), ec(i, o + e.length), ec(i, o), new Gs(t, t.getShiftedBy(n)); }
        function Za(t) { if (!t.isFlat)
            throw new Gn.b("operation-utils-remove-range-not-flat: Trying to remove a range which starts and ends in different element."); var e = t.start.parent; nc(t.start), nc(t.end); var n = e._removeChildren(t.start.index, t.end.index - t.start.index); return ec(e, t.start.index), n; }
        function Xa(t, e) { if (!t.isFlat)
            throw new Gn.b("operation-utils-move-range-not-flat: Trying to move a range which starts and ends in different element."); var n = Za(t); return Ja(e = e._getTransformedByDeletion(t.start, t.end.offset - t.start.offset), n); }
        function tc(t) { var e = []; t instanceof Array || (t = [t]); for (var n_154 = 0; n_154 < t.length; n_154++)
            if ("string" == typeof t[n_154])
                e.push(new Bs(t[n_154]));
            else if (t[n_154] instanceof Fs)
                e.push(new Bs(t[n_154].data, t[n_154].getAttributes()));
            else if (t[n_154] instanceof Ka || t[n_154] instanceof Us)
                for (var _j = 0, _k = t[n_154]; _j < _k.length; _j++) {
                    var i_78 = _k[_j];
                    e.push(i_78);
                }
            else
                t[n_154] instanceof zs && e.push(t[n_154]); for (var t_190 = 1; t_190 < e.length; t_190++) {
            var n_155 = e[t_190], i_79 = e[t_190 - 1];
            n_155 instanceof Bs && i_79 instanceof Bs && ic(n_155, i_79) && (e.splice(t_190 - 1, 2, new Bs(i_79.data + n_155.data, i_79.getAttributes())), t_190--);
        } return e; }
        function ec(t, e) { var n = t.getChild(e - 1), i = t.getChild(e); if (n && i && n.is("text") && i.is("text") && ic(n, i)) {
            var o_40 = new Bs(n.data + i.data, n.getAttributes());
            t._removeChildren(e - 1, 2), t._insertChild(e - 1, o_40);
        } }
        function nc(t) { var e = t.textNode, n = t.parent; if (e) {
            var i_80 = t.offset - e.startOffset, o_41 = e.index;
            n._removeChildren(o_41, 1);
            var r_15 = new Bs(e.data.substr(0, i_80), e.getAttributes()), s_15 = new Bs(e.data.substr(i_80), e.getAttributes());
            n._insertChild(o_41, [r_15, s_15]);
        } }
        function ic(t, e) { var n = t.getAttributes(), i = e.getAttributes(); for (var _j = 0, n_156 = n; _j < n_156.length; _j++) {
            var t_191 = n_156[_j];
            if (t_191[1] !== e.getAttribute(t_191[0]))
                return !1;
            i.next();
        } return i.next().done; }
        var oc = function (t, e) { return Kr(t, e); };
        var rc = /** @class */ (function (_super) {
            __extends(rc, _super);
            function rc(t, e, n, i, o) {
                var _this = this;
                _this = _super.call(this, o) || this, _this.range = t.clone(), _this.key = e, _this.oldValue = void 0 === n ? null : n, _this.newValue = void 0 === i ? null : i;
                return _this;
            }
            Object.defineProperty(rc.prototype, "type", {
                get: function () { return null === this.oldValue ? "addAttribute" : null === this.newValue ? "removeAttribute" : "changeAttribute"; },
                enumerable: true,
                configurable: true
            });
            rc.prototype.clone = function () { return new rc(this.range, this.key, this.oldValue, this.newValue, this.baseVersion); };
            rc.prototype.getReversed = function () { return new rc(this.range, this.key, this.newValue, this.oldValue, this.baseVersion + 1); };
            rc.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return t.range = this.range.toJSON(), t; };
            rc.prototype._validate = function () { if (!this.range.isFlat)
                throw new Gn.b("attribute-operation-range-not-flat: The range to change is not flat."); for (var _j = 0, _k = this.range.getItems({ shallow: !0 }); _j < _k.length; _j++) {
                var t_192 = _k[_j];
                if (null !== this.oldValue && !oc(t_192.getAttribute(this.key), this.oldValue))
                    throw new Gn.b("attribute-operation-wrong-old-value: Changed node has different attribute value than operation's old attribute value.", { item: t_192, key: this.key, value: this.oldValue });
                if (null === this.oldValue && null !== this.newValue && t_192.hasAttribute(this.key))
                    throw new Gn.b("attribute-operation-attribute-exists: The attribute with given key already exists.", { node: t_192, key: this.key });
            } };
            rc.prototype._execute = function () { oc(this.oldValue, this.newValue) || function (t, e, n) { nc(t.start), nc(t.end); for (var _j = 0, _k = t.getItems({ shallow: !0 }); _j < _k.length; _j++) {
                var i_81 = _k[_j];
                var t_193 = i_81.is("textProxy") ? i_81.textNode : i_81;
                null !== n ? t_193._setAttribute(e, n) : t_193._removeAttribute(e), ec(t_193.parent, t_193.index);
            } ec(t.end.parent, t.end.index); }(this.range, this.key, this.newValue); };
            Object.defineProperty(rc, "className", {
                get: function () { return "AttributeOperation"; },
                enumerable: true,
                configurable: true
            });
            rc.fromJSON = function (t, e) { return new rc(Gs.fromJSON(t.range, e), t.key, t.oldValue, t.newValue, t.baseVersion); };
            return rc;
        }(Qa));
        var sc = /** @class */ (function (_super) {
            __extends(sc, _super);
            function sc(t, e) {
                var _this = this;
                _this = _super.call(this, null) || this, _this.sourcePosition = t.clone(), _this.howMany = e;
                return _this;
            }
            Object.defineProperty(sc.prototype, "type", {
                get: function () { return "detach"; },
                enumerable: true,
                configurable: true
            });
            sc.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return t.sourcePosition = this.sourcePosition.toJSON(), t; };
            sc.prototype._validate = function () { if (this.sourcePosition.root.document)
                throw new Gn.b("detach-operation-on-document-node: Cannot detach document node."); };
            sc.prototype._execute = function () { Za(Gs._createFromPositionAndShift(this.sourcePosition, this.howMany)); };
            Object.defineProperty(sc, "className", {
                get: function () { return "DetachOperation"; },
                enumerable: true,
                configurable: true
            });
            return sc;
        }(Qa));
        var ac = /** @class */ (function (_super) {
            __extends(ac, _super);
            function ac(t, e, n, i) {
                var _this = this;
                _this = _super.call(this, i) || this, _this.sourcePosition = t.clone(), _this.sourcePosition.stickiness = "toNext", _this.howMany = e, _this.targetPosition = n.clone(), _this.targetPosition.stickiness = "toNone";
                return _this;
            }
            Object.defineProperty(ac.prototype, "type", {
                get: function () { return "$graveyard" == this.targetPosition.root.rootName ? "remove" : "$graveyard" == this.sourcePosition.root.rootName ? "reinsert" : "move"; },
                enumerable: true,
                configurable: true
            });
            ac.prototype.clone = function () { return new this.constructor(this.sourcePosition, this.howMany, this.targetPosition, this.baseVersion); };
            ac.prototype.getMovedRangeStart = function () { return this.targetPosition._getTransformedByDeletion(this.sourcePosition, this.howMany); };
            ac.prototype.getReversed = function () { var t = this.sourcePosition._getTransformedByInsertion(this.targetPosition, this.howMany); return new this.constructor(this.getMovedRangeStart(), this.howMany, t, this.baseVersion + 1); };
            ac.prototype._validate = function () { var t = this.sourcePosition.parent, e = this.targetPosition.parent, n = this.sourcePosition.offset, i = this.targetPosition.offset; if (!t || !e)
                throw new Gn.b("move-operation-position-invalid: Source position or target position is invalid."); if (n + this.howMany > t.maxOffset)
                throw new Gn.b("move-operation-nodes-do-not-exist: The nodes which should be moved do not exist."); if (t === e && n < i && i < n + this.howMany)
                throw new Gn.b("move-operation-range-into-itself: Trying to move a range of nodes to the inside of that range."); if (this.sourcePosition.root == this.targetPosition.root && "prefix" == li(this.sourcePosition.getParentPath(), this.targetPosition.getParentPath())) {
                var t_194 = this.sourcePosition.path.length - 1;
                if (this.targetPosition.path[t_194] >= n && this.targetPosition.path[t_194] < n + this.howMany)
                    throw new Gn.b("move-operation-node-into-itself: Trying to move a range of nodes into one of nodes from that range.");
            } };
            ac.prototype._execute = function () { Xa(Gs._createFromPositionAndShift(this.sourcePosition, this.howMany), this.targetPosition); };
            ac.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return t.sourcePosition = this.sourcePosition.toJSON(), t.targetPosition = this.targetPosition.toJSON(), t; };
            Object.defineProperty(ac, "className", {
                get: function () { return "MoveOperation"; },
                enumerable: true,
                configurable: true
            });
            ac.fromJSON = function (t, e) { var n = $s.fromJSON(t.sourcePosition, e), i = $s.fromJSON(t.targetPosition, e); return new this(n, t.howMany, i, t.baseVersion); };
            return ac;
        }(Qa));
        var cc = /** @class */ (function (_super) {
            __extends(cc, _super);
            function cc(t, e, n) {
                var _this = this;
                _this = _super.call(this, n) || this, _this.position = t.clone(), _this.position.stickiness = "toNone", _this.nodes = new Us(tc(e)), _this.shouldReceiveAttributes = !1;
                return _this;
            }
            Object.defineProperty(cc.prototype, "type", {
                get: function () { return "insert"; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(cc.prototype, "howMany", {
                get: function () { return this.nodes.maxOffset; },
                enumerable: true,
                configurable: true
            });
            cc.prototype.clone = function () { var t = new Us(this.nodes.slice().map(function (t) { return t._clone(!0); })), e = new cc(this.position, t, this.baseVersion); return e.shouldReceiveAttributes = this.shouldReceiveAttributes, e; };
            cc.prototype.getReversed = function () { var t = this.position.root.document.graveyard, e = new $s(t, [0]); return new ac(this.position, this.nodes.maxOffset, e, this.baseVersion + 1); };
            cc.prototype._validate = function () { var t = this.position.parent; if (!t || t.maxOffset < this.position.offset)
                throw new Gn.b("insert-operation-position-invalid: Insertion position is invalid."); };
            cc.prototype._execute = function () { var t = this.nodes; this.nodes = new Us(t.slice().map(function (t) { return t._clone(!0); })), Ja(this.position, t); };
            cc.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return t.position = this.position.toJSON(), t.nodes = this.nodes.toJSON(), t; };
            Object.defineProperty(cc, "className", {
                get: function () { return "InsertOperation"; },
                enumerable: true,
                configurable: true
            });
            cc.fromJSON = function (t, e) { var n = []; for (var _j = 0, _k = t.nodes; _j < _k.length; _j++) {
                var e_154 = _k[_j];
                e_154.name ? n.push(Hs.fromJSON(e_154)) : n.push(Bs.fromJSON(e_154));
            } var i = new cc($s.fromJSON(t.position, e), n, t.baseVersion); return i.shouldReceiveAttributes = t.shouldReceiveAttributes, i; };
            return cc;
        }(Qa));
        var lc = /** @class */ (function (_super) {
            __extends(lc, _super);
            function lc(t, e, n, i, o, r) {
                var _this = this;
                _this = _super.call(this, r) || this, _this.name = t, _this.oldRange = e ? e.clone() : null, _this.newRange = n ? n.clone() : null, _this.affectsData = o, _this._markers = i;
                return _this;
            }
            Object.defineProperty(lc.prototype, "type", {
                get: function () { return "marker"; },
                enumerable: true,
                configurable: true
            });
            lc.prototype.clone = function () { return new lc(this.name, this.oldRange, this.newRange, this._markers, this.affectsData, this.baseVersion); };
            lc.prototype.getReversed = function () { return new lc(this.name, this.newRange, this.oldRange, this._markers, this.affectsData, this.baseVersion + 1); };
            lc.prototype._execute = function () { var t = this.newRange ? "_set" : "_remove"; this._markers[t](this.name, this.newRange, !0, this.affectsData); };
            lc.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return this.oldRange && (t.oldRange = this.oldRange.toJSON()), this.newRange && (t.newRange = this.newRange.toJSON()), delete t._markers, t; };
            Object.defineProperty(lc, "className", {
                get: function () { return "MarkerOperation"; },
                enumerable: true,
                configurable: true
            });
            lc.fromJSON = function (t, e) { return new lc(t.name, t.oldRange ? Gs.fromJSON(t.oldRange, e) : null, t.newRange ? Gs.fromJSON(t.newRange, e) : null, e.model.markers, t.affectsData, t.baseVersion); };
            return lc;
        }(Qa));
        var dc = /** @class */ (function (_super) {
            __extends(dc, _super);
            function dc(t, e, n, i) {
                var _this = this;
                _this = _super.call(this, i) || this, _this.position = t, _this.position.stickiness = "toNext", _this.oldName = e, _this.newName = n;
                return _this;
            }
            Object.defineProperty(dc.prototype, "type", {
                get: function () { return "rename"; },
                enumerable: true,
                configurable: true
            });
            dc.prototype.clone = function () { return new dc(this.position.clone(), this.oldName, this.newName, this.baseVersion); };
            dc.prototype.getReversed = function () { return new dc(this.position.clone(), this.newName, this.oldName, this.baseVersion + 1); };
            dc.prototype._validate = function () { var t = this.position.nodeAfter; if (!(t instanceof Hs))
                throw new Gn.b("rename-operation-wrong-position: Given position is invalid or node after it is not an instance of Element."); if (t.name !== this.oldName)
                throw new Gn.b("rename-operation-wrong-name: Element to change has different name than operation's old name."); };
            dc.prototype._execute = function () { this.position.nodeAfter.name = this.newName; };
            dc.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return t.position = this.position.toJSON(), t; };
            Object.defineProperty(dc, "className", {
                get: function () { return "RenameOperation"; },
                enumerable: true,
                configurable: true
            });
            dc.fromJSON = function (t, e) { return new dc($s.fromJSON(t.position, e), t.oldName, t.newName, t.baseVersion); };
            return dc;
        }(Qa));
        var uc = /** @class */ (function (_super) {
            __extends(uc, _super);
            function uc(t, e, n, i, o) {
                var _this = this;
                _this = _super.call(this, o) || this, _this.root = t, _this.key = e, _this.oldValue = n, _this.newValue = i;
                return _this;
            }
            Object.defineProperty(uc.prototype, "type", {
                get: function () { return null === this.oldValue ? "addRootAttribute" : null === this.newValue ? "removeRootAttribute" : "changeRootAttribute"; },
                enumerable: true,
                configurable: true
            });
            uc.prototype.clone = function () { return new uc(this.root, this.key, this.oldValue, this.newValue, this.baseVersion); };
            uc.prototype.getReversed = function () { return new uc(this.root, this.key, this.newValue, this.oldValue, this.baseVersion + 1); };
            uc.prototype._validate = function () { if (this.root != this.root.root || this.root.is("documentFragment"))
                throw new Gn.b("rootattribute-operation-not-a-root: The element to change is not a root element.", { root: this.root, key: this.key }); if (null !== this.oldValue && this.root.getAttribute(this.key) !== this.oldValue)
                throw new Gn.b("rootattribute-operation-wrong-old-value: Changed node has different attribute value than operation's old attribute value.", { root: this.root, key: this.key }); if (null === this.oldValue && null !== this.newValue && this.root.hasAttribute(this.key))
                throw new Gn.b("rootattribute-operation-attribute-exists: The attribute with given key already exists.", { root: this.root, key: this.key }); };
            uc.prototype._execute = function () { null !== this.newValue ? this.root._setAttribute(this.key, this.newValue) : this.root._removeAttribute(this.key); };
            uc.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return t.root = this.root.toJSON(), t; };
            Object.defineProperty(uc, "className", {
                get: function () { return "RootAttributeOperation"; },
                enumerable: true,
                configurable: true
            });
            uc.fromJSON = function (t, e) { if (!e.getRoot(t.root))
                throw new Gn.b("rootattribute-operation-fromjson-no-root: Cannot create RootAttributeOperation. Root with specified name does not exist.", { rootName: t.root }); return new uc(e.getRoot(t.root), t.key, t.oldValue, t.newValue, t.baseVersion); };
            return uc;
        }(Qa));
        var hc = /** @class */ (function (_super) {
            __extends(hc, _super);
            function hc(t, e, n, i, o) {
                var _this = this;
                _this = _super.call(this, o) || this, _this.sourcePosition = t.clone(), _this.sourcePosition.stickiness = "toPrevious", _this.howMany = e, _this.targetPosition = n.clone(), _this.targetPosition.stickiness = "toNext", _this.graveyardPosition = i.clone();
                return _this;
            }
            Object.defineProperty(hc.prototype, "type", {
                get: function () { return "merge"; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(hc.prototype, "deletionPosition", {
                get: function () { return new $s(this.sourcePosition.root, this.sourcePosition.path.slice(0, -1)); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(hc.prototype, "movedRange", {
                get: function () { var t = this.sourcePosition.getShiftedBy(Number.POSITIVE_INFINITY); return new Gs(this.sourcePosition, t); },
                enumerable: true,
                configurable: true
            });
            hc.prototype.clone = function () { return new this.constructor(this.sourcePosition, this.howMany, this.targetPosition, this.graveyardPosition, this.baseVersion); };
            hc.prototype.getReversed = function () { var t = this.targetPosition._getTransformedByMergeOperation(this), e = this.sourcePosition.path.slice(0, -1), n = new $s(this.sourcePosition.root, e)._getTransformedByMergeOperation(this), i = new fc(t, this.howMany, this.graveyardPosition, this.baseVersion + 1); return i.insertionPosition = n, i; };
            hc.prototype._validate = function () { var t = this.sourcePosition.parent, e = this.targetPosition.parent; if (!(t && t.is("element") && t.parent))
                throw new Gn.b("merge-operation-source-position-invalid: Merge source position is invalid."); if (!(e && e.is("element") && e.parent))
                throw new Gn.b("merge-operation-target-position-invalid: Merge target position is invalid."); if (this.howMany != t.maxOffset)
                throw new Gn.b("merge-operation-how-many-invalid: Merge operation specifies wrong number of nodes to move."); };
            hc.prototype._execute = function () { var t = this.sourcePosition.parent; Xa(Gs._createIn(t), this.targetPosition), Xa(Gs._createOn(t), this.graveyardPosition); };
            hc.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return t.sourcePosition = t.sourcePosition.toJSON(), t.targetPosition = t.targetPosition.toJSON(), t.graveyardPosition = t.graveyardPosition.toJSON(), t; };
            Object.defineProperty(hc, "className", {
                get: function () { return "MergeOperation"; },
                enumerable: true,
                configurable: true
            });
            hc.fromJSON = function (t, e) { var n = $s.fromJSON(t.sourcePosition, e), i = $s.fromJSON(t.targetPosition, e), o = $s.fromJSON(t.graveyardPosition, e); return new this(n, t.howMany, i, o, t.baseVersion); };
            return hc;
        }(Qa));
        var fc = /** @class */ (function (_super) {
            __extends(fc, _super);
            function fc(t, e, n, i) {
                var _this = this;
                _this = _super.call(this, i) || this, _this.splitPosition = t.clone(), _this.splitPosition.stickiness = "toNext", _this.howMany = e, _this.insertionPosition = fc.getInsertionPosition(t), _this.insertionPosition.stickiness = "toNone", _this.graveyardPosition = n ? n.clone() : null, _this.graveyardPosition && (_this.graveyardPosition.stickiness = "toNext");
                return _this;
            }
            Object.defineProperty(fc.prototype, "type", {
                get: function () { return "split"; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(fc.prototype, "moveTargetPosition", {
                get: function () { var t = this.insertionPosition.path.slice(); return t.push(0), new $s(this.insertionPosition.root, t); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(fc.prototype, "movedRange", {
                get: function () { var t = this.splitPosition.getShiftedBy(Number.POSITIVE_INFINITY); return new Gs(this.splitPosition, t); },
                enumerable: true,
                configurable: true
            });
            fc.prototype.clone = function () { var t = new this.constructor(this.splitPosition, this.howMany, this.graveyardPosition, this.baseVersion); return t.insertionPosition = this.insertionPosition, t; };
            fc.prototype.getReversed = function () { var t = this.splitPosition.root.document.graveyard, e = new $s(t, [0]); return new hc(this.moveTargetPosition, this.howMany, this.splitPosition, e, this.baseVersion + 1); };
            fc.prototype._validate = function () { var t = this.splitPosition.parent, e = this.splitPosition.offset; if (!t || t.maxOffset < e)
                throw new Gn.b("split-operation-position-invalid: Split position is invalid."); if (!t.parent)
                throw new Gn.b("split-operation-split-in-root: Cannot split root element."); if (this.howMany != t.maxOffset - this.splitPosition.offset)
                throw new Gn.b("split-operation-how-many-invalid: Split operation specifies wrong number of nodes to move."); if (this.graveyardPosition && !this.graveyardPosition.nodeAfter)
                throw new Gn.b("split-operation-graveyard-position-invalid: Graveyard position invalid."); };
            fc.prototype._execute = function () { var t = this.splitPosition.parent; if (this.graveyardPosition)
                Xa(Gs._createFromPositionAndShift(this.graveyardPosition, 1), this.insertionPosition);
            else {
                var e_155 = t._clone();
                Ja(this.insertionPosition, e_155);
            } Xa(new Gs($s._createAt(t, this.splitPosition.offset), $s._createAt(t, t.maxOffset)), this.moveTargetPosition); };
            fc.prototype.toJSON = function () { var t = _super.prototype.toJSON.call(this); return t.splitPosition = this.splitPosition.toJSON(), t.insertionPosition = this.insertionPosition.toJSON(), this.graveyardPosition && (t.graveyardPosition = this.graveyardPosition.toJSON()), t; };
            Object.defineProperty(fc, "className", {
                get: function () { return "SplitOperation"; },
                enumerable: true,
                configurable: true
            });
            fc.getInsertionPosition = function (t) { var e = t.path.slice(0, -1); return e[e.length - 1]++, new $s(t.root, e); };
            fc.fromJSON = function (t, e) { var n = $s.fromJSON(t.splitPosition, e), i = $s.fromJSON(t.insertionPosition, e), o = t.graveyardPosition ? $s.fromJSON(t.graveyardPosition, e) : null, r = new this(n, t.howMany, o, t.baseVersion); return r.insertionPosition = i, r; };
            return fc;
        }(Qa));
        var mc = /** @class */ (function (_super) {
            __extends(mc, _super);
            function mc(t, e, n) {
                if (n === void 0) { n = "main"; }
                var _this = this;
                _this = _super.call(this, e) || this, _this._doc = t, _this.rootName = n;
                return _this;
            }
            Object.defineProperty(mc.prototype, "document", {
                get: function () { return this._doc; },
                enumerable: true,
                configurable: true
            });
            mc.prototype.is = function (t, e) { return e ? "rootElement" == t && e == this.name || _super.prototype.is.call(this, t, e) : "rootElement" == t || _super.prototype.is.call(this, t); };
            mc.prototype.toJSON = function () { return this.rootName; };
            return mc;
        }(Hs));
        var gc = /** @class */ (function () {
            function gc(t, e) {
                this.model = t, this.batch = e;
            }
            gc.prototype.createText = function (t, e) { return new Bs(t, e); };
            gc.prototype.createElement = function (t, e) { return new Hs(t, e); };
            gc.prototype.createDocumentFragment = function () { return new Ka; };
            gc.prototype.insert = function (t, e, n) {
                if (n === void 0) { n = 0; }
                if (this._assertWriterUsedCorrectly(), t instanceof Bs && "" == t.data)
                    return;
                var i = $s._createAt(e, n);
                if (t.parent) {
                    if (kc(t.root, i.root))
                        return void this.move(Gs._createOn(t), i);
                    if (t.root.document)
                        throw new Error("model-writer-insert-forbidden-move: Cannot move a node from a document to a different tree.");
                    this.remove(t);
                }
                var o = i.root.document ? i.root.document.version : null, r = new cc(i, t, o);
                if (t instanceof Bs && (r.shouldReceiveAttributes = !0), this.batch.addOperation(r), this.model.applyOperation(r), t instanceof Ka)
                    for (var _j = 0, _k = t.markers; _j < _k.length; _j++) {
                        var _q = _k[_j], e_156 = _q[0], n_157 = _q[1];
                        var t_195 = $s._createAt(n_157.root, 0), o_42 = new Gs(n_157.start._getCombined(t_195, i), n_157.end._getCombined(t_195, i));
                        this.addMarker(e_156, { range: o_42, usingOperation: !0, affectsData: !0 });
                    }
            };
            gc.prototype.insertText = function (t, e, n, i) { e instanceof Ka || e instanceof Hs || e instanceof $s ? this.insert(this.createText(t), e, n) : this.insert(this.createText(t, e), n, i); };
            gc.prototype.insertElement = function (t, e, n, i) { e instanceof Ka || e instanceof Hs || e instanceof $s ? this.insert(this.createElement(t), e, n) : this.insert(this.createElement(t, e), n, i); };
            gc.prototype.append = function (t, e) { this.insert(t, e, "end"); };
            gc.prototype.appendText = function (t, e, n) { e instanceof Ka || e instanceof Hs ? this.insert(this.createText(t), e, "end") : this.insert(this.createText(t, e), n, "end"); };
            gc.prototype.appendElement = function (t, e, n) { e instanceof Ka || e instanceof Hs ? this.insert(this.createElement(t), e, "end") : this.insert(this.createElement(t, e), n, "end"); };
            gc.prototype.setAttribute = function (t, e, n) { if (this._assertWriterUsedCorrectly(), n instanceof Gs) {
                var i_83 = n.getMinimalFlatRanges();
                for (var _j = 0, i_82 = i_83; _j < i_82.length; _j++) {
                    var n_158 = i_82[_j];
                    pc(this, t, e, n_158);
                }
            }
            else
                bc(this, t, e, n); };
            gc.prototype.setAttributes = function (t, e) { for (var _j = 0, _k = Vs(t); _j < _k.length; _j++) {
                var _q = _k[_j], n_159 = _q[0], i_84 = _q[1];
                this.setAttribute(n_159, i_84, e);
            } };
            gc.prototype.removeAttribute = function (t, e) { if (this._assertWriterUsedCorrectly(), e instanceof Gs) {
                var n_161 = e.getMinimalFlatRanges();
                for (var _j = 0, n_160 = n_161; _j < n_160.length; _j++) {
                    var e_157 = n_160[_j];
                    pc(this, t, null, e_157);
                }
            }
            else
                bc(this, t, null, e); };
            gc.prototype.clearAttributes = function (t) {
                var _this = this;
                this._assertWriterUsedCorrectly();
                var e = function (t) { for (var _j = 0, _k = t.getAttributeKeys(); _j < _k.length; _j++) {
                    var e_158 = _k[_j];
                    _this.removeAttribute(e_158, t);
                } };
                if (t instanceof Gs)
                    for (var _j = 0, _k = t.getItems(); _j < _k.length; _j++) {
                        var n_162 = _k[_j];
                        e(n_162);
                    }
                else
                    e(t);
            };
            gc.prototype.move = function (t, e, n) { if (this._assertWriterUsedCorrectly(), !(t instanceof Gs))
                throw new Gn.b("writer-move-invalid-range: Invalid range to move."); if (!t.isFlat)
                throw new Gn.b("writer-move-range-not-flat: Range to move is not flat."); var i = $s._createAt(e, n); if (i.isEqual(t.start))
                return; if (this._addOperationForAffectedMarkers("move", t), !kc(t.root, i.root))
                throw new Gn.b("writer-move-different-document: Range is going to be moved between different documents."); var o = t.root.document ? t.root.document.version : null, r = new ac(t.start, t.end.offset - t.start.offset, i, o); this.batch.addOperation(r), this.model.applyOperation(r); };
            gc.prototype.remove = function (t) { this._assertWriterUsedCorrectly(); var e = (t instanceof Gs ? t : Gs._createOn(t)).getMinimalFlatRanges().reverse(); for (var _j = 0, e_159 = e; _j < e_159.length; _j++) {
                var t_196 = e_159[_j];
                this._addOperationForAffectedMarkers("move", t_196), _c(t_196.start, t_196.end.offset - t_196.start.offset, this.batch, this.model);
            } };
            gc.prototype.merge = function (t) { this._assertWriterUsedCorrectly(); var e = t.nodeBefore, n = t.nodeAfter; if (this._addOperationForAffectedMarkers("merge", t), !(e instanceof Hs))
                throw new Gn.b("writer-merge-no-element-before: Node before merge position must be an element."); if (!(n instanceof Hs))
                throw new Gn.b("writer-merge-no-element-after: Node after merge position must be an element."); t.root.document ? this._merge(t) : this._mergeDetached(t); };
            gc.prototype.createPositionFromPath = function (t, e, n) { return this.model.createPositionFromPath(t, e, n); };
            gc.prototype.createPositionAt = function (t, e) { return this.model.createPositionAt(t, e); };
            gc.prototype.createPositionAfter = function (t) { return this.model.createPositionAfter(t); };
            gc.prototype.createPositionBefore = function (t) { return this.model.createPositionBefore(t); };
            gc.prototype.createRange = function (t, e) { return this.model.createRange(t, e); };
            gc.prototype.createRangeIn = function (t) { return this.model.createRangeIn(t); };
            gc.prototype.createRangeOn = function (t) { return this.model.createRangeOn(t); };
            gc.prototype.createSelection = function (t, e, n) { return this.model.createSelection(t, e, n); };
            gc.prototype._mergeDetached = function (t) { var e = t.nodeBefore, n = t.nodeAfter; this.move(Gs._createIn(n), $s._createAt(e, "end")), this.remove(n); };
            gc.prototype._merge = function (t) { var e = $s._createAt(t.nodeBefore, "end"), n = $s._createAt(t.nodeAfter, 0), i = t.root.document.graveyard, o = new $s(i, [0]), r = t.root.document.version, s = new hc(n, t.nodeAfter.maxOffset, e, o, r); this.batch.addOperation(s), this.model.applyOperation(s); };
            gc.prototype.rename = function (t, e) { if (this._assertWriterUsedCorrectly(), !(t instanceof Hs))
                throw new Gn.b("writer-rename-not-element-instance: Trying to rename an object which is not an instance of Element."); var n = t.root.document ? t.root.document.version : null, i = new dc($s._createBefore(t), t.name, e, n); this.batch.addOperation(i), this.model.applyOperation(i); };
            gc.prototype.split = function (t, e) { this._assertWriterUsedCorrectly(); var n, i, o = t.parent; if (!o.parent)
                throw new Gn.b("writer-split-element-no-parent: Element with no parent can not be split."); if (e || (e = o.parent), !t.parent.getAncestors({ includeSelf: !0 }).includes(e))
                throw new Gn.b("writer-split-invalid-limit-element: Limit element is not a position ancestor."); do {
                var e_160 = o.root.document ? o.root.document.version : null, r_16 = o.maxOffset - t.offset, s_16 = new fc(t, r_16, null, e_160);
                this.batch.addOperation(s_16), this.model.applyOperation(s_16), n || i || (n = o, i = t.parent.nextSibling), o = (t = this.createPositionAfter(t.parent)).parent;
            } while (o !== e); return { position: t, range: new Gs($s._createAt(n, "end"), $s._createAt(i, 0)) }; };
            gc.prototype.wrap = function (t, e) { if (this._assertWriterUsedCorrectly(), !t.isFlat)
                throw new Gn.b("writer-wrap-range-not-flat: Range to wrap is not flat."); var n = e instanceof Hs ? e : new Hs(e); if (n.childCount > 0)
                throw new Gn.b("writer-wrap-element-not-empty: Element to wrap with is not empty."); if (null !== n.parent)
                throw new Gn.b("writer-wrap-element-attached: Element to wrap with is already attached to tree model."); var i = t.root.document ? t.root.document.version : null, o = new cc(t.start, n, i); this.batch.addOperation(o), this.model.applyOperation(o); var r = new ac(t.start.getShiftedBy(1), t.end.offset - t.start.offset, $s._createAt(n, 0), null === i ? null : i + 1); this.batch.addOperation(r), this.model.applyOperation(r); };
            gc.prototype.unwrap = function (t) { if (this._assertWriterUsedCorrectly(), null === t.parent)
                throw new Gn.b("writer-unwrap-element-no-parent: Trying to unwrap an element which has no parent."); this.move(Gs._createIn(t), this.createPositionAfter(t)), this.remove(t); };
            gc.prototype.addMarker = function (t, e) { if (this._assertWriterUsedCorrectly(), !e || "boolean" != typeof e.usingOperation)
                throw new Gn.b("writer-addMarker-no-usingOperation: The options.usingOperation parameter is required when adding a new marker."); var n = e.usingOperation, i = e.range, o = void 0 !== e.affectsData && e.affectsData; if (this.model.markers.has(t))
                throw new Gn.b("writer-addMarker-marker-exists: Marker with provided name already exists."); if (!i)
                throw new Gn.b("writer-addMarker-no-range: Range parameter is required when adding a new marker."); return n ? (wc(this, t, null, i, o), this.model.markers.get(t)) : this.model.markers._set(t, i, n, o); };
            gc.prototype.updateMarker = function (t, e) {
                if (e === void 0) { e = {}; }
                this._assertWriterUsedCorrectly();
                var n = "string" == typeof t ? t : t.name, i = this.model.markers.get(n);
                if (!i)
                    throw new Gn.b("writer-updateMarker-marker-not-exists: Marker with provided name does not exists.");
                var o = "boolean" == typeof e.usingOperation, r = "boolean" == typeof e.affectsData, s = r ? e.affectsData : i.affectsData;
                if (!o && !e.range && !r)
                    throw new Gn.b("writer-updateMarker-wrong-options: One of the options is required - provide range, usingOperations or affectsData.");
                var a = i.getRange(), c = e.range ? e.range : a;
                o && e.usingOperation !== i.managedUsingOperations ? e.usingOperation ? wc(this, n, null, c, s) : (wc(this, n, a, null, s), this.model.markers._set(n, c, void 0, s)) : i.managedUsingOperations ? wc(this, n, a, c, s) : this.model.markers._set(n, c, void 0, s);
            };
            gc.prototype.removeMarker = function (t) { this._assertWriterUsedCorrectly(); var e = "string" == typeof t ? t : t.name; if (!this.model.markers.has(e))
                throw new Gn.b("writer-removeMarker-no-marker: Trying to remove marker which does not exist."); var n = this.model.markers.get(e); n.managedUsingOperations ? wc(this, e, n.getRange(), null, n.affectsData) : this.model.markers._remove(e); };
            gc.prototype.setSelection = function (t, e, n) { this._assertWriterUsedCorrectly(), this.model.document.selection._setTo(t, e, n); };
            gc.prototype.setSelectionFocus = function (t, e) { this._assertWriterUsedCorrectly(), this.model.document.selection._setFocus(t, e); };
            gc.prototype.setSelectionAttribute = function (t, e) { if (this._assertWriterUsedCorrectly(), "string" == typeof t)
                this._setSelectionAttribute(t, e);
            else
                for (var _j = 0, _k = Vs(t); _j < _k.length; _j++) {
                    var _q = _k[_j], e_161 = _q[0], n_163 = _q[1];
                    this._setSelectionAttribute(e_161, n_163);
                } };
            gc.prototype.removeSelectionAttribute = function (t) { if (this._assertWriterUsedCorrectly(), "string" == typeof t)
                this._removeSelectionAttribute(t);
            else
                for (var _j = 0, t_197 = t; _j < t_197.length; _j++) {
                    var e_162 = t_197[_j];
                    this._removeSelectionAttribute(e_162);
                } };
            gc.prototype.overrideSelectionGravity = function () { return this.model.document.selection._overrideGravity(); };
            gc.prototype.restoreSelectionGravity = function (t) { this.model.document.selection._restoreGravity(t); };
            gc.prototype._setSelectionAttribute = function (t, e) { var n = this.model.document.selection; if (n.isCollapsed && n.anchor.parent.isEmpty) {
                var i_85 = sa._getStoreAttributeKey(t);
                this.setAttribute(i_85, e, n.anchor.parent);
            } n._setAttribute(t, e); };
            gc.prototype._removeSelectionAttribute = function (t) { var e = this.model.document.selection; if (e.isCollapsed && e.anchor.parent.isEmpty) {
                var n_164 = sa._getStoreAttributeKey(t);
                this.removeAttribute(n_164, e.anchor.parent);
            } e._removeAttribute(t); };
            gc.prototype._assertWriterUsedCorrectly = function () { if (this.model._currentWriter !== this)
                throw new Gn.b("writer-incorrect-use: Trying to use a writer outside the change() block."); };
            gc.prototype._addOperationForAffectedMarkers = function (t, e) { for (var _j = 0, _k = this.model.markers; _j < _k.length; _j++) {
                var n_165 = _k[_j];
                if (!n_165.managedUsingOperations)
                    continue;
                var i_86 = n_165.getRange();
                var o_43 = !1;
                if ("move" == t)
                    o_43 = e.containsPosition(i_86.start) || e.start.isEqual(i_86.start) || e.containsPosition(i_86.end) || e.end.isEqual(i_86.end);
                else {
                    var t_198 = e.nodeBefore, n_166 = e.nodeAfter, r_17 = i_86.start.parent == t_198 && i_86.start.isAtEnd, s_17 = i_86.end.parent == n_166 && 0 == i_86.end.offset;
                    o_43 = r_17 || s_17;
                }
                o_43 && this.updateMarker(n_165.name, { range: i_86 });
            } };
            return gc;
        }());
        function pc(t, e, n, i) { var o = t.model, r = o.document; var s, a, c, l = i.start; for (var _j = 0, _k = i.getWalker({ shallow: !0 }); _j < _k.length; _j++) {
            var t_199 = _k[_j];
            c = t_199.item.getAttribute(e), s && a != c && (a != n && d(), l = s), s = t_199.nextPosition, a = c;
        } function d() { var i = new Gs(l, s), c = i.root.document ? r.version : null, d = new rc(i, e, a, n, c); t.batch.addOperation(d), o.applyOperation(d); } s instanceof $s && s != l && a != n && d(); }
        function bc(t, e, n, i) { var o = t.model, r = o.document, s = i.getAttribute(e); var a, c; if (s != n) {
            if (i.root === i) {
                var t_200 = i.document ? r.version : null;
                c = new uc(i, e, s, n, t_200);
            }
            else {
                var o_44 = (a = new Gs($s._createBefore(i), t.createPositionAfter(i))).root.document ? r.version : null;
                c = new rc(a, e, s, n, o_44);
            }
            t.batch.addOperation(c), o.applyOperation(c);
        } }
        function wc(t, e, n, i, o) { var r = t.model, s = r.document, a = new lc(e, n, i, r.markers, o, s.version); t.batch.addOperation(a), r.applyOperation(a); }
        function _c(t, e, n, i) { var o; if (t.root.document) {
            var n_167 = i.document, r_18 = new $s(n_167.graveyard, [0]);
            o = new ac(t, e, r_18, n_167.version);
        }
        else
            o = new sc(t, e); n.addOperation(o), i.applyOperation(o); }
        function kc(t, e) { return t === e || t instanceof mc && e instanceof mc; }
        var vc = /** @class */ (function () {
            function vc(t) {
                this._markerCollection = t, this._changesInElement = new Map, this._elementSnapshots = new Map, this._changedMarkers = new Map, this._changeCount = 0, this._cachedChanges = null, this._cachedChangesWithGraveyard = null;
            }
            Object.defineProperty(vc.prototype, "isEmpty", {
                get: function () { return 0 == this._changesInElement.size && 0 == this._changedMarkers.size; },
                enumerable: true,
                configurable: true
            });
            vc.prototype.bufferOperation = function (t) { switch (t.type) {
                case "insert":
                    if (this._isInInsertedElement(t.position.parent))
                        return;
                    this._markInsert(t.position.parent, t.position.offset, t.nodes.maxOffset);
                    break;
                case "addAttribute":
                case "removeAttribute":
                case "changeAttribute":
                    for (var _j = 0, _k = t.range.getItems(); _j < _k.length; _j++) {
                        var e_163 = _k[_j];
                        this._isInInsertedElement(e_163.parent) || this._markAttribute(e_163);
                    }
                    break;
                case "remove":
                case "move":
                case "reinsert": {
                    if (t.sourcePosition.isEqual(t.targetPosition) || t.sourcePosition.getShiftedBy(t.howMany).isEqual(t.targetPosition))
                        return;
                    var e_164 = this._isInInsertedElement(t.sourcePosition.parent), n_168 = this._isInInsertedElement(t.targetPosition.parent);
                    e_164 || this._markRemove(t.sourcePosition.parent, t.sourcePosition.offset, t.howMany), n_168 || this._markInsert(t.targetPosition.parent, t.getMovedRangeStart().offset, t.howMany);
                    break;
                }
                case "rename": {
                    if (this._isInInsertedElement(t.position.parent))
                        return;
                    this._markRemove(t.position.parent, t.position.offset, 1), this._markInsert(t.position.parent, t.position.offset, 1);
                    var e_165 = Gs._createFromPositionAndShift(t.position, 1);
                    for (var _q = 0, _v = this._markerCollection.getMarkersIntersectingRange(e_165); _q < _v.length; _q++) {
                        var t_201 = _v[_q];
                        var e_166 = t_201.getRange();
                        this.bufferMarkerChange(t_201.name, e_166, e_166, t_201.affectsData);
                    }
                    break;
                }
                case "split": {
                    var e_167 = t.splitPosition.parent;
                    this._isInInsertedElement(e_167) || this._markRemove(e_167, t.splitPosition.offset, t.howMany), this._isInInsertedElement(t.insertionPosition.parent) || this._markInsert(t.insertionPosition.parent, t.insertionPosition.offset, 1), t.graveyardPosition && this._markRemove(t.graveyardPosition.parent, t.graveyardPosition.offset, 1);
                    break;
                }
                case "merge": {
                    var e_168 = t.sourcePosition.parent;
                    this._isInInsertedElement(e_168.parent) || this._markRemove(e_168.parent, e_168.startOffset, 1);
                    var n_169 = t.graveyardPosition.parent;
                    this._markInsert(n_169, t.graveyardPosition.offset, 1);
                    var i_87 = t.targetPosition.parent;
                    this._isInInsertedElement(i_87) || this._markInsert(i_87, t.targetPosition.offset, e_168.maxOffset);
                    break;
                }
            } this._cachedChanges = null; };
            vc.prototype.bufferMarkerChange = function (t, e, n, i) { var o = this._changedMarkers.get(t); o ? (o.newRange = n, o.affectsData = i, null == o.oldRange && null == o.newRange && this._changedMarkers.delete(t)) : this._changedMarkers.set(t, { oldRange: e, newRange: n, affectsData: i }); };
            vc.prototype.getMarkersToRemove = function () { var t = []; for (var _j = 0, _k = this._changedMarkers; _j < _k.length; _j++) {
                var _q = _k[_j], e_169 = _q[0], n_170 = _q[1];
                null != n_170.oldRange && t.push({ name: e_169, range: n_170.oldRange });
            } return t; };
            vc.prototype.getMarkersToAdd = function () { var t = []; for (var _j = 0, _k = this._changedMarkers; _j < _k.length; _j++) {
                var _q = _k[_j], e_170 = _q[0], n_171 = _q[1];
                null != n_171.newRange && t.push({ name: e_170, range: n_171.newRange });
            } return t; };
            vc.prototype.getChangedMarkers = function () { return Array.from(this._changedMarkers).map(function (t) { return ({ name: t[0], data: { oldRange: t[1].oldRange, newRange: t[1].newRange } }); }); };
            vc.prototype.hasDataChanges = function () { for (var _j = 0, _k = this._changedMarkers; _j < _k.length; _j++) {
                var _q = _k[_j], t_202 = _q[1];
                if (t_202.affectsData)
                    return !0;
            } return this._changesInElement.size > 0; };
            vc.prototype.getChanges = function (t) {
                if (t === void 0) { t = { includeChangesInGraveyard: !1 }; }
                if (this._cachedChanges)
                    return t.includeChangesInGraveyard ? this._cachedChangesWithGraveyard.slice() : this._cachedChanges.slice();
                var e = [];
                for (var _j = 0, _k = this._changesInElement.keys(); _j < _k.length; _j++) {
                    var t_203 = _k[_j];
                    var n_172 = this._changesInElement.get(t_203).sort(function (t, e) { return t.offset === e.offset ? t.type != e.type ? "remove" == t.type ? -1 : 1 : 0 : t.offset < e.offset ? -1 : 1; }), i_88 = this._elementSnapshots.get(t_203), o_45 = yc(t_203.getChildren()), r_20 = xc(i_88.length, n_172);
                    var s_18 = 0, a_7 = 0;
                    for (var _q = 0, r_19 = r_20; _q < r_19.length; _q++) {
                        var n_173 = r_19[_q];
                        if ("i" === n_173)
                            e.push(this._getInsertDiff(t_203, s_18, o_45[s_18].name)), s_18++;
                        else if ("r" === n_173)
                            e.push(this._getRemoveDiff(t_203, s_18, i_88[a_7].name)), a_7++;
                        else if ("a" === n_173) {
                            var n_174 = o_45[s_18].attributes, r_21 = i_88[a_7].attributes;
                            var c_4 = void 0;
                            if ("$text" == o_45[s_18].name)
                                c_4 = new Gs($s._createAt(t_203, s_18), $s._createAt(t_203, s_18 + 1));
                            else {
                                var e_172 = t_203.offsetToIndex(s_18);
                                c_4 = new Gs($s._createAt(t_203, s_18), $s._createAt(t_203.getChild(e_172), 0));
                            }
                            e.push.apply(e, this._getAttributesDiff(c_4, r_21, n_174)), s_18++, a_7++;
                        }
                        else
                            s_18++, a_7++;
                    }
                }
                e.sort(function (t, e) { return t.position.root != e.position.root ? t.position.root.rootName < e.position.root.rootName ? -1 : 1 : t.position.isEqual(e.position) ? t.changeCount - e.changeCount : t.position.isBefore(e.position) ? -1 : 1; });
                for (var t_204 = 1; t_204 < e.length; t_204++) {
                    var n_175 = e[t_204 - 1], i_89 = e[t_204], o_46 = "remove" == n_175.type && "remove" == i_89.type && "$text" == n_175.name && "$text" == i_89.name && n_175.position.isEqual(i_89.position), r_22 = "insert" == n_175.type && "insert" == i_89.type && "$text" == n_175.name && "$text" == i_89.name && n_175.position.parent == i_89.position.parent && n_175.position.offset + n_175.length == i_89.position.offset, s_19 = "attribute" == n_175.type && "attribute" == i_89.type && n_175.position.parent == i_89.position.parent && n_175.range.isFlat && i_89.range.isFlat && n_175.position.offset + n_175.length == i_89.position.offset && n_175.attributeKey == i_89.attributeKey && n_175.attributeOldValue == i_89.attributeOldValue && n_175.attributeNewValue == i_89.attributeNewValue;
                    (o_46 || r_22 || s_19) && (e[t_204 - 1].length++, s_19 && (e[t_204 - 1].range.end = e[t_204 - 1].range.end.getShiftedBy(1)), e.splice(t_204, 1), t_204--);
                }
                for (var _v = 0, e_171 = e; _v < e_171.length; _v++) {
                    var t_205 = e_171[_v];
                    delete t_205.changeCount, "attribute" == t_205.type && (delete t_205.position, delete t_205.length);
                }
                return this._changeCount = 0, this._cachedChangesWithGraveyard = e.slice(), this._cachedChanges = e.slice().filter(Ac), t.includeChangesInGraveyard ? this._cachedChangesWithGraveyard : this._cachedChanges;
            };
            vc.prototype.reset = function () { this._changesInElement.clear(), this._elementSnapshots.clear(), this._changedMarkers.clear(), this._cachedChanges = null; };
            vc.prototype._markInsert = function (t, e, n) { var i = { type: "insert", offset: e, howMany: n, count: this._changeCount++ }; this._markChange(t, i); };
            vc.prototype._markRemove = function (t, e, n) { var i = { type: "remove", offset: e, howMany: n, count: this._changeCount++ }; this._markChange(t, i), this._removeAllNestedChanges(t, e, n); };
            vc.prototype._markAttribute = function (t) { var e = { type: "attribute", offset: t.startOffset, howMany: t.offsetSize, count: this._changeCount++ }; this._markChange(t.parent, e); };
            vc.prototype._markChange = function (t, e) { this._makeSnapshot(t); var n = this._getChangesForElement(t); this._handleChange(e, n), n.push(e); for (var t_206 = 0; t_206 < n.length; t_206++)
                n[t_206].howMany < 1 && (n.splice(t_206, 1), t_206--); };
            vc.prototype._getChangesForElement = function (t) { var e; return this._changesInElement.has(t) ? e = this._changesInElement.get(t) : (e = [], this._changesInElement.set(t, e)), e; };
            vc.prototype._makeSnapshot = function (t) { this._elementSnapshots.has(t) || this._elementSnapshots.set(t, yc(t.getChildren())); };
            vc.prototype._handleChange = function (t, e) { t.nodesToHandle = t.howMany; for (var _j = 0, e_173 = e; _j < e_173.length; _j++) {
                var n_176 = e_173[_j];
                var i_90 = t.offset + t.howMany, o_47 = n_176.offset + n_176.howMany;
                if ("insert" == t.type && ("insert" == n_176.type && (t.offset <= n_176.offset ? n_176.offset += t.howMany : t.offset < o_47 && (n_176.howMany += t.nodesToHandle, t.nodesToHandle = 0)), "remove" == n_176.type && t.offset < n_176.offset && (n_176.offset += t.howMany), "attribute" == n_176.type))
                    if (t.offset <= n_176.offset)
                        n_176.offset += t.howMany;
                    else if (t.offset < o_47) {
                        var o_48 = n_176.howMany;
                        n_176.howMany = t.offset - n_176.offset, e.unshift({ type: "attribute", offset: i_90, howMany: o_48 - n_176.howMany, count: this._changeCount++ });
                    }
                if ("remove" == t.type) {
                    if ("insert" == n_176.type)
                        if (i_90 <= n_176.offset)
                            n_176.offset -= t.howMany;
                        else if (i_90 <= o_47)
                            if (t.offset < n_176.offset) {
                                var e_174 = i_90 - n_176.offset;
                                n_176.offset = t.offset, n_176.howMany -= e_174, t.nodesToHandle -= e_174;
                            }
                            else
                                n_176.howMany -= t.nodesToHandle, t.nodesToHandle = 0;
                        else if (t.offset <= n_176.offset)
                            t.nodesToHandle -= n_176.howMany, n_176.howMany = 0;
                        else if (t.offset < o_47) {
                            var e_175 = o_47 - t.offset;
                            n_176.howMany -= e_175, t.nodesToHandle -= e_175;
                        }
                    if ("remove" == n_176.type && (i_90 <= n_176.offset ? n_176.offset -= t.howMany : t.offset < n_176.offset && (t.nodesToHandle += n_176.howMany, n_176.howMany = 0)), "attribute" == n_176.type)
                        if (i_90 <= n_176.offset)
                            n_176.offset -= t.howMany;
                        else if (t.offset < n_176.offset) {
                            var e_176 = i_90 - n_176.offset;
                            n_176.offset = t.offset, n_176.howMany -= e_176;
                        }
                        else if (t.offset < o_47)
                            if (i_90 <= o_47) {
                                var i_91 = n_176.howMany;
                                n_176.howMany = t.offset - n_176.offset;
                                var o_49 = i_91 - n_176.howMany - t.nodesToHandle;
                                e.unshift({ type: "attribute", offset: t.offset, howMany: o_49, count: this._changeCount++ });
                            }
                            else
                                n_176.howMany -= o_47 - t.offset;
                }
                if ("attribute" == t.type) {
                    if ("insert" == n_176.type)
                        if (t.offset < n_176.offset && i_90 > n_176.offset) {
                            if (i_90 > o_47) {
                                var t_207 = { type: "attribute", offset: o_47, howMany: i_90 - o_47, count: this._changeCount++ };
                                this._handleChange(t_207, e), e.push(t_207);
                            }
                            t.nodesToHandle = n_176.offset - t.offset, t.howMany = t.nodesToHandle;
                        }
                        else
                            t.offset >= n_176.offset && t.offset < o_47 && (i_90 > o_47 ? (t.nodesToHandle = i_90 - o_47, t.offset = o_47) : t.nodesToHandle = 0);
                    "attribute" == n_176.type && (t.offset >= n_176.offset && i_90 <= o_47 ? (t.nodesToHandle = 0, t.howMany = 0, t.offset = 0) : t.offset <= n_176.offset && i_90 >= o_47 && (n_176.howMany = 0));
                }
            } t.howMany = t.nodesToHandle, delete t.nodesToHandle; };
            vc.prototype._getInsertDiff = function (t, e, n) { return { type: "insert", position: $s._createAt(t, e), name: n, length: 1, changeCount: this._changeCount++ }; };
            vc.prototype._getRemoveDiff = function (t, e, n) { return { type: "remove", position: $s._createAt(t, e), name: n, length: 1, changeCount: this._changeCount++ }; };
            vc.prototype._getAttributesDiff = function (t, e, n) { var i = []; n = new Map(n); for (var _j = 0, e_177 = e; _j < e_177.length; _j++) {
                var _k = e_177[_j], o_50 = _k[0], r_23 = _k[1];
                var e_178 = n.has(o_50) ? n.get(o_50) : null;
                e_178 !== r_23 && i.push({ type: "attribute", position: t.start, range: t.clone(), length: 1, attributeKey: o_50, attributeOldValue: r_23, attributeNewValue: e_178, changeCount: this._changeCount++ }), n.delete(o_50);
            } for (var _q = 0, n_177 = n; _q < n_177.length; _q++) {
                var _v = n_177[_q], e_179 = _v[0], o_51 = _v[1];
                i.push({ type: "attribute", position: t.start, range: t.clone(), length: 1, attributeKey: e_179, attributeOldValue: null, attributeNewValue: o_51, changeCount: this._changeCount++ });
            } return i; };
            vc.prototype._isInInsertedElement = function (t) { var e = t.parent; if (!e)
                return !1; var n = this._changesInElement.get(e), i = t.startOffset; if (n)
                for (var _j = 0, n_178 = n; _j < n_178.length; _j++) {
                    var t_208 = n_178[_j];
                    if ("insert" == t_208.type && i >= t_208.offset && i < t_208.offset + t_208.howMany)
                        return !0;
                } return this._isInInsertedElement(e); };
            vc.prototype._removeAllNestedChanges = function (t, e, n) { var i = new Gs($s._createAt(t, e), $s._createAt(t, e + n)); for (var _j = 0, _k = i.getItems({ shallow: !0 }); _j < _k.length; _j++) {
                var t_209 = _k[_j];
                t_209.is("element") && (this._elementSnapshots.delete(t_209), this._changesInElement.delete(t_209), this._removeAllNestedChanges(t_209, 0, t_209.maxOffset));
            } };
            return vc;
        }());
        function yc(t) { var e = []; for (var _j = 0, t_210 = t; _j < t_210.length; _j++) {
            var n_179 = t_210[_j];
            if (n_179.is("text"))
                for (var t_211 = 0; t_211 < n_179.data.length; t_211++)
                    e.push({ name: "$text", attributes: new Map(n_179.getAttributes()) });
            else
                e.push({ name: n_179.name, attributes: new Map(n_179.getAttributes()) });
        } return e; }
        function xc(t, e) { var n = []; var i = 0, o = 0; for (var _j = 0, e_180 = e; _j < e_180.length; _j++) {
            var t_212 = e_180[_j];
            t_212.offset > i && (n.push.apply(n, "e".repeat(t_212.offset - i).split("")), o += t_212.offset - i), "insert" == t_212.type ? (n.push.apply(n, "i".repeat(t_212.howMany).split("")), i = t_212.offset + t_212.howMany) : "remove" == t_212.type ? (n.push.apply(n, "r".repeat(t_212.howMany).split("")), i = t_212.offset, o += t_212.howMany) : (n.push.apply(n, "a".repeat(t_212.howMany).split("")), i = t_212.offset + t_212.howMany, o += t_212.howMany);
        } return o < t && n.push.apply(n, "e".repeat(t - o).split("")), n; }
        function Ac(t) { var e = t.position && "$graveyard" == t.position.root.rootName, n = t.range && "$graveyard" == t.range.root.rootName; return !e && !n; }
        var Cc = /** @class */ (function () {
            function Cc() {
                this._operations = [], this._undoPairs = new Map, this._undoneOperations = new Set;
            }
            Cc.prototype.addOperation = function (t) { this._operations.includes(t) || this._operations.push(t); };
            Cc.prototype.getOperations = function (t, e) {
                if (t === void 0) { t = 0; }
                if (e === void 0) { e = Number.POSITIVE_INFINITY; }
                return t < 0 ? [] : this._operations.slice(t, e);
            };
            Cc.prototype.getOperation = function (t) { return this._operations[t]; };
            Cc.prototype.setOperationAsUndone = function (t, e) { this._undoPairs.set(e, t), this._undoneOperations.add(t); };
            Cc.prototype.isUndoingOperation = function (t) { return this._undoPairs.has(t); };
            Cc.prototype.isUndoneOperation = function (t) { return this._undoneOperations.has(t); };
            Cc.prototype.getUndoneOperation = function (t) { return this._undoPairs.get(t); };
            return Cc;
        }());
        function Tc(t, e) { return function (t) { return !!t && 1 == t.length && /[\ud800-\udbff]/.test(t); }(t.charAt(e - 1)) && function (t) { return !!t && 1 == t.length && /[\udc00-\udfff]/.test(t); }(t.charAt(e)); }
        function Pc(t, e) { return function (t) { return !!t && 1 == t.length && /[\u0300-\u036f\u1ab0-\u1aff\u1dc0-\u1dff\u20d0-\u20ff\ufe20-\ufe2f]/.test(t); }(t.charAt(e)); }
        var Mc = "$graveyard";
        var Ec = /** @class */ (function () {
            function Ec(t) {
                var _this = this;
                this.model = t, this.version = 0, this.history = new Cc(this), this.selection = new sa(this), this.roots = new oo({ idProperty: "rootName" }), this.differ = new vc(t.markers), this._postFixers = new Set, this._hasSelectionChangedFromTheLastChangeBlock = !1, this.createRoot("$root", Mc), this.listenTo(t, "applyOperation", function (t, e) { var n = e[0]; if (n.isDocumentOperation && n.baseVersion !== _this.version)
                    throw new Gn.b("model-document-applyOperation-wrong-version: Only operations with matching versions can be applied.", { operation: n }); }, { priority: "highest" }), this.listenTo(t, "applyOperation", function (t, e) { var n = e[0]; n.isDocumentOperation && _this.differ.bufferOperation(n); }, { priority: "high" }), this.listenTo(t, "applyOperation", function (t, e) { var n = e[0]; n.isDocumentOperation && (_this.version++, _this.history.addOperation(n)); }, { priority: "low" }), this.listenTo(this.selection, "change", function () { _this._hasSelectionChangedFromTheLastChangeBlock = !0; }), this.listenTo(t.markers, "update", function (t, e, n, i) { _this.differ.bufferMarkerChange(e.name, n, i, e.affectsData), null === n && e.on("change", function (t, n) { _this.differ.bufferMarkerChange(e.name, n, e.getRange(), e.affectsData); }); });
            }
            Object.defineProperty(Ec.prototype, "graveyard", {
                get: function () { return this.getRoot(Mc); },
                enumerable: true,
                configurable: true
            });
            Ec.prototype.createRoot = function (t, e) {
                if (t === void 0) { t = "$root"; }
                if (e === void 0) { e = "main"; }
                if (this.roots.get(e))
                    throw new Gn.b("model-document-createRoot-name-exists: Root with specified name already exists.", { name: e });
                var n = new mc(this, t, e);
                return this.roots.add(n), n;
            };
            Ec.prototype.destroy = function () { this.selection.destroy(), this.stopListening(); };
            Ec.prototype.getRoot = function (t) {
                if (t === void 0) { t = "main"; }
                return this.roots.get(t);
            };
            Ec.prototype.getRootNames = function () { return Array.from(this.roots, function (t) { return t.rootName; }).filter(function (t) { return t != Mc; }); };
            Ec.prototype.registerPostFixer = function (t) { this._postFixers.add(t); };
            Ec.prototype.toJSON = function () { var t = ui(this); return t.selection = "[engine.model.DocumentSelection]", t.model = "[engine.model.Model]", t; };
            Ec.prototype._handleChangeBlock = function (t) { this._hasDocumentChangedFromTheLastChangeBlock() && (this._callPostFixers(t), this.differ.hasDataChanges() ? this.fire("change:data", t.batch) : this.fire("change", t.batch), this.differ.reset()), this._hasSelectionChangedFromTheLastChangeBlock = !1; };
            Ec.prototype._hasDocumentChangedFromTheLastChangeBlock = function () { return !this.differ.isEmpty || this._hasSelectionChangedFromTheLastChangeBlock; };
            Ec.prototype._getDefaultRoot = function () { for (var _j = 0, _k = this.roots; _j < _k.length; _j++) {
                var t_213 = _k[_j];
                if (t_213 !== this.graveyard)
                    return t_213;
            } return this.graveyard; };
            Ec.prototype._getDefaultRange = function () { var t = this._getDefaultRoot(), e = this.model, n = e.schema, i = e.createPositionFromPath(t, [0]); return n.getNearestSelectionRange(i) || e.createRange(i); };
            Ec.prototype._validateSelectionRange = function (t) { return Sc(t.start) && Sc(t.end); };
            Ec.prototype._callPostFixers = function (t) { var e = !1; do {
                for (var _j = 0, _k = this._postFixers; _j < _k.length; _j++) {
                    var n_180 = _k[_j];
                    if (e = n_180(t))
                        break;
                }
            } while (e); };
            return Ec;
        }());
        function Sc(t) { var e = t.textNode; if (e) {
            var n_181 = e.data, i_92 = t.offset - e.startOffset;
            return !Tc(n_181, i_92) && !Pc(n_181, i_92);
        } return !0; }
        ci(Ec, ei);
        var Ic = /** @class */ (function () {
            function Ic() {
                this._markers = new Map;
            }
            Ic.prototype[Symbol.iterator] = function () { return this._markers.values(); };
            Ic.prototype.has = function (t) { return this._markers.has(t); };
            Ic.prototype.get = function (t) { return this._markers.get(t) || null; };
            Ic.prototype._set = function (t, e, n, i) {
                if (n === void 0) { n = !1; }
                if (i === void 0) { i = !1; }
                var o = t instanceof Nc ? t.name : t, r = this._markers.get(o);
                if (r) {
                    var t_214 = r.getRange();
                    var s_20 = !1;
                    return t_214.isEqual(e) || (r._attachLiveRange(oa.fromRange(e)), s_20 = !0), n != r.managedUsingOperations && (r._managedUsingOperations = n, s_20 = !0), "boolean" == typeof i && i != r.affectsData && (r._affectsData = i, s_20 = !0), s_20 && this.fire("update:" + o, r, t_214, e), r;
                }
                var s = oa.fromRange(e), a = new Nc(o, s, n, i);
                return this._markers.set(o, a), this.fire("update:" + o, a, null, e), a;
            };
            Ic.prototype._remove = function (t) { var e = t instanceof Nc ? t.name : t, n = this._markers.get(e); return !!n && (this._markers.delete(e), this.fire("update:" + e, n, n.getRange(), null), this._destroyMarker(n), !0); };
            Ic.prototype.getMarkersAtPosition = function (t) { var _j, _k, e_181, _q; return __generator(this, function (_v) {
                switch (_v.label) {
                    case 0:
                        _j = 0, _k = this;
                        _v.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 5];
                        e_181 = _k[_j];
                        _q = e_181.getRange().containsPosition(t);
                        if (!_q) return [3 /*break*/, 3];
                        return [4 /*yield*/, e_181];
                    case 2:
                        _q = (_v.sent());
                        _v.label = 3;
                    case 3:
                        _q;
                        _v.label = 4;
                    case 4:
                        _j++;
                        return [3 /*break*/, 1];
                    case 5: return [2 /*return*/];
                }
            }); };
            Ic.prototype.getMarkersIntersectingRange = function (t) { var _j, _k, e_182, _q; return __generator(this, function (_v) {
                switch (_v.label) {
                    case 0:
                        _j = 0, _k = this;
                        _v.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 5];
                        e_182 = _k[_j];
                        _q = null !== e_182.getRange().getIntersection(t);
                        if (!_q) return [3 /*break*/, 3];
                        return [4 /*yield*/, e_182];
                    case 2:
                        _q = (_v.sent());
                        _v.label = 3;
                    case 3:
                        _q;
                        _v.label = 4;
                    case 4:
                        _j++;
                        return [3 /*break*/, 1];
                    case 5: return [2 /*return*/];
                }
            }); };
            Ic.prototype.destroy = function () { for (var _j = 0, _k = this._markers.values(); _j < _k.length; _j++) {
                var t_215 = _k[_j];
                this._destroyMarker(t_215);
            } this._markers = null, this.stopListening(); };
            Ic.prototype.getMarkersGroup = function (t) { var _j, _k, e_183, _q; return __generator(this, function (_v) {
                switch (_v.label) {
                    case 0:
                        _j = 0, _k = this._markers.values();
                        _v.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 5];
                        e_183 = _k[_j];
                        _q = e_183.name.startsWith(t + ":");
                        if (!_q) return [3 /*break*/, 3];
                        return [4 /*yield*/, e_183];
                    case 2:
                        _q = (_v.sent());
                        _v.label = 3;
                    case 3:
                        _q;
                        _v.label = 4;
                    case 4:
                        _j++;
                        return [3 /*break*/, 1];
                    case 5: return [2 /*return*/];
                }
            }); };
            Ic.prototype._destroyMarker = function (t) { t.stopListening(), t._detachLiveRange(); };
            return Ic;
        }());
        ci(Ic, ei);
        var Nc = /** @class */ (function () {
            function Nc(t, e, n, i) {
                this.name = t, this._liveRange = this._attachLiveRange(e), this._managedUsingOperations = n, this._affectsData = i;
            }
            Object.defineProperty(Nc.prototype, "managedUsingOperations", {
                get: function () { if (!this._liveRange)
                    throw new Gn.b("marker-destroyed: Cannot use a destroyed marker instance."); return this._managedUsingOperations; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Nc.prototype, "affectsData", {
                get: function () { if (!this._liveRange)
                    throw new Gn.b("marker-destroyed: Cannot use a destroyed marker instance."); return this._affectsData; },
                enumerable: true,
                configurable: true
            });
            Nc.prototype.getStart = function () { if (!this._liveRange)
                throw new Gn.b("marker-destroyed: Cannot use a destroyed marker instance."); return this._liveRange.start.clone(); };
            Nc.prototype.getEnd = function () { if (!this._liveRange)
                throw new Gn.b("marker-destroyed: Cannot use a destroyed marker instance."); return this._liveRange.end.clone(); };
            Nc.prototype.getRange = function () { if (!this._liveRange)
                throw new Gn.b("marker-destroyed: Cannot use a destroyed marker instance."); return this._liveRange.toRange(); };
            Nc.prototype._attachLiveRange = function (t) { return this._liveRange && this._detachLiveRange(), t.delegate("change:range").to(this), t.delegate("change:content").to(this), this._liveRange = t, t; };
            Nc.prototype._detachLiveRange = function () { this._liveRange.stopDelegating("change:range", this), this._liveRange.stopDelegating("change:content", this), this._liveRange.detach(), this._liveRange = null; };
            return Nc;
        }());
        ci(Nc, ei);
        var Oc = /** @class */ (function (_super) {
            __extends(Oc, _super);
            function Oc(t, e, n) {
                if (n === void 0) { n = "toNone"; }
                var _this = this;
                if (_this = _super.call(this, t, e, n) || this, !_this.root.is("rootElement"))
                    throw new Gn.b("model-liveposition-root-not-rootelement: LivePosition's root has to be an instance of RootElement.");
                (function () {
                    var _this = this;
                    this.listenTo(this.root.document.model, "applyOperation", function (t, e) { var n = e[0]; n.isDocumentOperation && function (t) { var e = this.getTransformedByOperation(t); if (!this.isEqual(e)) {
                        var t_216 = this.toPosition();
                        this.path = e.path, this.root = e.root, this.fire("change", t_216);
                    } }.call(_this, n); }, { priority: "low" });
                }).call(_this);
                return _this;
            }
            Oc.prototype.detach = function () { this.stopListening(); };
            Oc.prototype.toPosition = function () { return new $s(this.root, this.path.slice(), this.stickiness); };
            Oc.fromPosition = function (t, e) { return new this(t.root, t.path.slice(), e || t.stickiness); };
            return Oc;
        }($s));
        ci(Oc, ei);
        var Rc = /** @class */ (function () {
            function Rc(t, e, n) {
                this.model = t, this.writer = e, this.position = n, this.canMergeWith = new Set([this.position.parent]), this.schema = t.schema, this._filterAttributesOf = [];
            }
            Rc.prototype.handleNodes = function (t, e) { t = Array.from(t); for (var n_182 = 0; n_182 < t.length; n_182++) {
                var i_93 = t[n_182];
                this._handleNode(i_93, { isFirst: 0 === n_182 && e.isFirst, isLast: n_182 === t.length - 1 && e.isLast });
            } this.schema.removeDisallowedAttributes(this._filterAttributesOf, this.writer), this._filterAttributesOf = []; };
            Rc.prototype.getSelectionRange = function () { return this.nodeToSelect ? Gs._createOn(this.nodeToSelect) : this.model.schema.getNearestSelectionRange(this.position); };
            Rc.prototype._handleNode = function (t, e) { if (this.schema.isObject(t))
                return void this._handleObject(t, e); this._checkAndSplitToAllowedPosition(t, e) ? (this._insert(t), this._mergeSiblingsOf(t, e)) : this._handleDisallowedNode(t, e); };
            Rc.prototype._handleObject = function (t, e) { this._checkAndSplitToAllowedPosition(t) ? this._insert(t) : this._tryAutoparagraphing(t, e); };
            Rc.prototype._handleDisallowedNode = function (t, e) { t.is("element") ? this.handleNodes(t.getChildren(), e) : this._tryAutoparagraphing(t, e); };
            Rc.prototype._insert = function (t) { if (!this.schema.checkChild(this.position, t))
                return void bs.a.error("insertcontent-wrong-position: The node cannot be inserted on the given position.", { node: t, position: this.position }); var e = Oc.fromPosition(this.position, "toNext"); this.writer.insert(t, this.position), this.position = e.toPosition(), e.detach(), this.schema.isObject(t) && !this.schema.checkChild(this.position, "$text") ? this.nodeToSelect = t : this.nodeToSelect = null, this._filterAttributesOf.push(t); };
            Rc.prototype._mergeSiblingsOf = function (t, e) { if (!(t instanceof Hs))
                return; var n = this._canMergeLeft(t, e), i = this._canMergeRight(t, e), o = Oc._createBefore(t); o.stickiness = "toNext"; var r = Oc._createAfter(t); if (r.stickiness = "toNext", n) {
                var t_217 = Oc.fromPosition(this.position);
                t_217.stickiness = "toNext", this.writer.merge(o), this.position = t_217.toPosition(), t_217.detach();
            } if (i) {
                this.position.isEqual(r) || bs.a.error("insertcontent-wrong-position-on-merge: The insertion position should equal the merge position"), this.position = $s._createAt(r.nodeBefore, "end");
                var t_218 = new Oc(this.position.root, this.position.path, "toPrevious");
                this.writer.merge(r), this.position = t_218.toPosition(), t_218.detach();
            } (n || i) && this._filterAttributesOf.push(this.position.parent), o.detach(), r.detach(); };
            Rc.prototype._canMergeLeft = function (t, e) { var n = t.previousSibling; return e.isFirst && n instanceof Hs && this.canMergeWith.has(n) && this.model.schema.checkMerge(n, t); };
            Rc.prototype._canMergeRight = function (t, e) { var n = t.nextSibling; return e.isLast && n instanceof Hs && this.canMergeWith.has(n) && this.model.schema.checkMerge(t, n); };
            Rc.prototype._tryAutoparagraphing = function (t, e) { var n = this.writer.createElement("paragraph"); this._getAllowedIn(n, this.position.parent) && this.schema.checkChild(n, t) && (n._appendChild(t), this._handleNode(n, e)); };
            Rc.prototype._checkAndSplitToAllowedPosition = function (t) { var e = this._getAllowedIn(t, this.position.parent); if (!e)
                return !1; for (; e != this.position.parent;) {
                if (this.schema.isLimit(this.position.parent))
                    return !1;
                if (this.position.isAtStart) {
                    var t_219 = this.position.parent;
                    this.position = this.writer.createPositionBefore(t_219), t_219.isEmpty && this.writer.remove(t_219);
                }
                else if (this.position.isAtEnd)
                    this.position = this.writer.createPositionAfter(this.position.parent);
                else {
                    var t_220 = this.writer.createPositionAfter(this.position.parent);
                    this.writer.split(this.position), this.position = t_220, this.canMergeWith.add(this.position.nodeAfter);
                }
            } return !0; };
            Rc.prototype._getAllowedIn = function (t, e) { return this.schema.checkChild(e, t) ? e : e.parent ? this._getAllowedIn(t, e.parent) : null; };
            return Rc;
        }());
        function Dc(t, e, n) {
            if (n === void 0) { n = {}; }
            if (e.isCollapsed)
                return;
            var i = t.schema;
            t.change(function (t) { if (!n.doNotResetEntireContent && function (t, e) { var n = t.getLimitElement(e); if (!e.containsEntireContent(n))
                return !1; var i = e.getFirstRange(); if (i.start.parent == i.end.parent)
                return !1; return t.checkChild(n, "paragraph"); }(i, e))
                return void function (t, e) { var n = t.model.schema.getLimitElement(e); t.remove(t.createRangeIn(n)), Lc(t, t.createPositionAt(n, 0), e); }(t, e); var o = e.getFirstRange(), r = o.start, s = Oc.fromPosition(o.end, "toNext"); o.start.isTouching(o.end) || t.remove(o), n.leaveUnmerged || (!function t(e, n, i) { var o = n.parent; var r = i.parent; if (o == r)
                return; if (e.model.schema.isLimit(o) || e.model.schema.isLimit(r))
                return; if (!function (t, e, n) { var i = new Gs(t, e); for (var _j = 0, _k = i.getWalker(); _j < _k.length; _j++) {
                var t_221 = _k[_j];
                if (n.isLimit(t_221.item))
                    return !1;
            } return !0; }(n, i, e.model.schema))
                return; n = e.createPositionAfter(o); i = e.createPositionBefore(r); i.isEqual(n) || e.insert(r, n); e.merge(n); for (; i.parent.isEmpty;) {
                var t_222 = i.parent;
                i = e.createPositionBefore(t_222), e.remove(t_222);
            } t(e, n, i); }(t, r, s), i.removeDisallowedAttributes(r.parent.getChildren(), t)), e instanceof sa ? t.setSelection(r) : e.setTo(r), function (t, e) { var n = t.checkChild(e, "$text"), i = t.checkChild(e, "paragraph"); return !n && i; }(i, r) && Lc(t, r, e), s.detach(); });
        }
        function Lc(t, e, n) { var i = t.createElement("paragraph"); t.insert(i, e), n instanceof sa ? t.setSelection(i, 0) : n.setTo(i, 0); }
        var jc = ' ,.?!:;"-()';
        function Vc(t, e, n) {
            if (n === void 0) { n = {}; }
            var i = t.schema, o = "backward" != n.direction, r = n.unit ? n.unit : "character", s = e.focus, a = new qs({ boundaries: function (t, e) { var n = t.root, i = $s._createAt(n, e ? "end" : 0); return e ? new Gs(t, i) : new Gs(i, t); }(s, o), singleCharacters: !0, direction: o ? "forward" : "backward" }), c = { walker: a, schema: i, isForward: o, unit: r };
            var l;
            var _loop_3 = function () {
                if (l.done)
                    return { value: void 0 };
                var n_183 = zc(c, l.value);
                if (n_183)
                    return { value: void (e instanceof sa ? t.change(function (t) { t.setSelectionFocus(n_183); }) : e.setFocus(n_183)) };
            };
            for (; l = a.next();) {
                var state_1 = _loop_3();
                if (typeof state_1 === "object")
                    return state_1.value;
            }
        }
        function zc(t, e) { if ("text" == e.type)
            return "word" === t.unit ? function (t, e) { var n = t.position.textNode; if (n) {
                var i_94 = t.position.offset - n.startOffset;
                for (; !Bc(n.data, i_94, e) && !Fc(n, i_94, e);) {
                    t.next();
                    var o_52 = e ? t.position.nodeAfter : t.position.nodeBefore;
                    if (o_52 && o_52.is("text")) {
                        var i_95 = o_52.data.charAt(e ? 0 : o_52.data.length - 1);
                        jc.includes(i_95) || (t.next(), n = t.position.textNode);
                    }
                    i_94 = t.position.offset - n.startOffset;
                }
            } return t.position; }(t.walker, t.isForward) : function (t, e) { var n = t.position.textNode; if (n) {
                var i_96 = n.data;
                var o_53 = t.position.offset - n.startOffset;
                for (; Tc(i_96, o_53) || "character" == e && Pc(i_96, o_53);)
                    t.next(), o_53 = t.position.offset - n.startOffset;
            } return t.position; }(t.walker, t.unit, t.isForward); if (e.type == (t.isForward ? "elementStart" : "elementEnd")) {
            if (t.schema.isObject(e.item))
                return $s._createAt(e.item, t.isForward ? "after" : "before");
            if (t.schema.checkChild(e.nextPosition, "$text"))
                return e.nextPosition;
        }
        else {
            if (t.schema.isLimit(e.item))
                return void t.walker.skip(function () { return !0; });
            if (t.schema.checkChild(e.nextPosition, "$text"))
                return e.nextPosition;
        } }
        function Bc(t, e, n) { var i = e + (n ? 0 : -1); return jc.includes(t.charAt(i)); }
        function Fc(t, e, n) { return e === (n ? t.endOffset : 0); }
        function Uc(t, e) { var n = []; Array.from(t.getItems({ direction: "backward" })).map(function (t) { return e.createRangeOn(t); }).filter(function (e) { return (e.start.isAfter(t.start) || e.start.isEqual(t.start)) && (e.end.isBefore(t.end) || e.end.isEqual(t.end)); }).forEach(function (t) { n.push(t.start.parent), e.remove(t); }), n.forEach(function (t) { var n = t; for (; n.parent && n.isEmpty;) {
            var t_223 = e.createRangeOn(n);
            n = n.parent, e.remove(t_223);
        } }); }
        function Hc(t) { t.document.registerPostFixer(function (e) { return (function (t, e) { var n = e.document.selection, i = e.schema, o = []; var r = !1; for (var _j = 0, _k = n.getRanges(); _j < _k.length; _j++) {
            var t_224 = _k[_j];
            var e_184 = qc(t_224, i);
            e_184 ? (o.push(e_184), r = !0) : o.push(t_224);
        } if (r) {
            var e_185 = o;
            if (o.length > 1) {
                var t_225 = o[0].start, n_184 = o[o.length - 1].end;
                e_185 = [new Gs(t_225, n_184)];
            }
            t.setSelection(e_185, { backward: n.isBackward });
        } })(e, t); }); }
        function qc(t, e) { return t.isCollapsed ? function (t, e) { var n = t.start, i = e.getNearestSelectionRange(n); if (!i)
            return null; var o = i.start; if (n.isEqual(o))
            return null; if (o.nodeAfter && e.isLimit(o.nodeAfter))
            return new Gs(o, $s._createAfter(o.nodeAfter)); return new Gs(o); }(t, e) : function (t, e) { var n = t.start, i = t.end, o = e.checkChild(n, "$text"), r = e.checkChild(i, "$text"), s = e.getLimitElement(n), a = e.getLimitElement(i); if (s === a) {
            if (o && r)
                return null;
            if (function (t, e, n) { var i = t.nodeAfter && !n.isLimit(t.nodeAfter) || n.checkChild(t, "$text"), o = e.nodeBefore && !n.isLimit(e.nodeBefore) || n.checkChild(e, "$text"); return i || o; }(n, i, e)) {
                var t_226 = n.nodeAfter && e.isObject(n.nodeAfter), o_54 = t_226 ? null : e.getNearestSelectionRange(n, "forward"), r_24 = i.nodeBefore && e.isObject(i.nodeBefore), s_21 = r_24 ? null : e.getNearestSelectionRange(i, "backward"), a_8 = o_54 ? o_54.start : n, c_5 = s_21 ? s_21.start : i;
                return new Gs(a_8, c_5);
            }
        } var c = s && !s.is("rootElement"), l = a && !a.is("rootElement"); if (c || l) {
            var t_227 = n.nodeAfter && i.nodeBefore && n.nodeAfter.parent === i.nodeBefore.parent, o_55 = c && (!t_227 || !Yc(n.nodeAfter, e)), r_25 = l && (!t_227 || !Yc(i.nodeBefore, e));
            var d_1 = n, u_1 = i;
            return o_55 && (d_1 = $s._createBefore(Wc(s, e))), r_25 && (u_1 = $s._createAfter(Wc(a, e))), new Gs(d_1, u_1);
        } return null; }(t, e); }
        function Wc(t, e) { var n = t, i = n; for (; e.isLimit(i) && i.parent;)
            n = i, i = i.parent; return n; }
        function Yc(t, e) { return t && e.isObject(t); }
        var $c = /** @class */ (function () {
            function $c() {
                var _this = this;
                this.markers = new Ic, this.document = new Ec(this), this.schema = new Ia, this._pendingChanges = [], this._currentWriter = null, ["insertContent", "deleteContent", "modifySelection", "getSelectedContent", "applyOperation"].forEach(function (t) { return _this.decorate(t); }), this.on("applyOperation", function (t, e) { e[0]._validate(); }, { priority: "highest" }), this.schema.register("$root", { isLimit: !0 }), this.schema.register("$block", { allowIn: "$root", isBlock: !0 }), this.schema.register("$text", { allowIn: "$block", isInline: !0 }), this.schema.register("$clipboardHolder", { allowContentOf: "$root", isLimit: !0 }), this.schema.extend("$text", { allowIn: "$clipboardHolder" }), this.schema.register("$marker", { allowIn: ["$root", "$block"] }), Hc(this);
            }
            $c.prototype.change = function (t) { return 0 === this._pendingChanges.length ? (this._pendingChanges.push({ batch: new Ga, callback: t }), this._runPendingChanges()[0]) : t(this._currentWriter); };
            $c.prototype.enqueueChange = function (t, e) { "string" == typeof t ? t = new Ga(t) : "function" == typeof t && (e = t, t = new Ga), this._pendingChanges.push({ batch: t, callback: e }), 1 == this._pendingChanges.length && this._runPendingChanges(); };
            $c.prototype.applyOperation = function (t) { t._execute(); };
            $c.prototype.insertContent = function (t, e, n) { !function (t, e, n, i) { t.change(function (o) { var r; (r = n ? n instanceof ta || n instanceof sa ? n : o.createSelection(n, i) : t.document.selection).isCollapsed || t.deleteContent(r); var s = new Rc(t, o, r.anchor); var a; a = e.is("documentFragment") ? e.getChildren() : [e], s.handleNodes(a, { isFirst: !0, isLast: !0 }); var c = s.getSelectionRange(); c ? r instanceof sa ? o.setSelection(c) : r.setTo(c) : bs.a.warn("insertcontent-no-range: Cannot determine a proper selection range after insertion."); }); }(this, t, e, n); };
            $c.prototype.deleteContent = function (t, e) { Dc(this, t, e); };
            $c.prototype.modifySelection = function (t, e) { Vc(this, t, e); };
            $c.prototype.getSelectedContent = function (t) { return function (t, e) { return t.change(function (t) { var n = t.createDocumentFragment(), i = e.getFirstRange(); if (!i || i.isCollapsed)
                return n; var o = i.start.root, r = i.start.getCommonPath(i.end), s = o.getNodeByPath(r); var a; var c = (a = i.start.parent == i.end.parent ? i : t.createRange(t.createPositionAt(s, i.start.path[r.length]), t.createPositionAt(s, i.end.path[r.length] + 1))).end.offset - a.start.offset; for (var _j = 0, _k = a.getItems({ shallow: !0 }); _j < _k.length; _j++) {
                var e_186 = _k[_j];
                e_186.is("textProxy") ? t.appendText(e_186.data, e_186.getAttributes(), n) : t.append(e_186._clone(!0), n);
            } if (a != i) {
                var e_187 = i._getTransformedByMove(a.start, t.createPositionAt(n, 0), c)[0], o_56 = t.createRange(t.createPositionAt(n, 0), e_187.start);
                Uc(t.createRange(e_187.end, t.createPositionAt(n, "end")), t), Uc(o_56, t);
            } return n; }); }(this, t); };
            $c.prototype.hasContent = function (t, e) { var n = t instanceof Hs ? Gs._createIn(t) : t; if (n.isCollapsed)
                return !1; for (var _j = 0, _k = this.markers.getMarkersIntersectingRange(n); _j < _k.length; _j++) {
                var t_228 = _k[_j];
                if (t_228.affectsData)
                    return !0;
            } var _q = (e || {}).ignoreWhitespaces, i = _q === void 0 ? !1 : _q; for (var _v = 0, _w = n.getItems(); _v < _w.length; _v++) {
                var t_229 = _w[_v];
                if (t_229.is("textProxy")) {
                    if (!i)
                        return !0;
                    if (-1 !== t_229.data.search(/\S/))
                        return !0;
                }
                else if (this.schema.isObject(t_229))
                    return !0;
            } return !1; };
            $c.prototype.createPositionFromPath = function (t, e, n) { return new $s(t, e, n); };
            $c.prototype.createPositionAt = function (t, e) { return $s._createAt(t, e); };
            $c.prototype.createPositionAfter = function (t) { return $s._createAfter(t); };
            $c.prototype.createPositionBefore = function (t) { return $s._createBefore(t); };
            $c.prototype.createRange = function (t, e) { return new Gs(t, e); };
            $c.prototype.createRangeIn = function (t) { return Gs._createIn(t); };
            $c.prototype.createRangeOn = function (t) { return Gs._createOn(t); };
            $c.prototype.createSelection = function (t, e, n) { return new ta(t, e, n); };
            $c.prototype.createBatch = function () { return new Ga; };
            $c.prototype.destroy = function () { this.document.destroy(), this.stopListening(); };
            $c.prototype._runPendingChanges = function () { var t = []; for (this.fire("_beforeChanges"); this._pendingChanges.length;) {
                var e_188 = this._pendingChanges[0].batch;
                this._currentWriter = new gc(this, e_188);
                var n_185 = this._pendingChanges[0].callback(this._currentWriter);
                t.push(n_185), this.fire("_change", this._currentWriter), this.document._handleChangeBlock(this._currentWriter), this._pendingChanges.shift(), this._currentWriter = null;
            } return this.fire("_afterChanges"), t; };
            return $c;
        }());
        ci($c, Fi);
        var Gc = /** @class */ (function () {
            function Gc() {
                this._listener = Object.create(lr);
            }
            Gc.prototype.listenTo = function (t) {
                var _this = this;
                this._listener.listenTo(t, "keydown", function (t, e) { _this._listener.fire("_keydown:" + wo(e), e); });
            };
            Gc.prototype.set = function (t, e, n) {
                if (n === void 0) { n = {}; }
                var i = _o(t), o = n.priority;
                this._listener.listenTo(this._listener, "_keydown:" + i, function (t, n) { e(n, function () { n.preventDefault(), n.stopPropagation(), t.stop(); }), t.return = !0; }, { priority: o });
            };
            Gc.prototype.press = function (t) { return !!this._listener.fire("_keydown:" + wo(t), t); };
            Gc.prototype.destroy = function () { this._listener.stopListening(); };
            return Gc;
        }());
        var Qc = /** @class */ (function (_super) {
            __extends(Qc, _super);
            function Qc(t) {
                var _this = this;
                _this = _super.call(this) || this, _this.editor = t;
                return _this;
            }
            Qc.prototype.set = function (t, e, n) {
                var _this = this;
                if (n === void 0) { n = {}; }
                if ("string" == typeof e) {
                    var t_230 = e;
                    e = (function (e, n) { _this.editor.execute(t_230), n(); });
                }
                _super.prototype.set.call(this, t, e, n);
            };
            return Qc;
        }(Gc));
        n(53);
        var Kc = /** @class */ (function () {
            function Kc(t) {
                var _this = this;
                var e = this.constructor.builtinPlugins;
                this.config = new Yn(t, this.constructor.defaultConfig), this.config.define("plugins", e), this.plugins = new Ca(this, e), this.commands = new Ta, this.locale = new Ma(this.config.get("language")), this.t = this.locale.t, this.set("state", "initializing"), this.once("ready", function () { return _this.state = "ready"; }, { priority: "high" }), this.once("destroy", function () { return _this.state = "destroyed"; }, { priority: "high" }), this.set("isReadOnly", !1), this.model = new $c, this.data = new qa(this.model), this.editing = new Aa(this.model), this.editing.view.document.bind("isReadOnly").to(this), this.conversion = new Wa([this.editing.downcastDispatcher, this.data.downcastDispatcher], this.data.upcastDispatcher), this.conversion.addAlias("dataDowncast", this.data.downcastDispatcher), this.conversion.addAlias("editingDowncast", this.editing.downcastDispatcher), this.keystrokes = new Qc(this), this.keystrokes.listenTo(this.editing.view.document);
            }
            Kc.prototype.initPlugins = function () { var t = this.config, e = t.get("plugins") || [], n = t.get("removePlugins") || [], i = t.get("extraPlugins") || []; return this.plugins.init(e.concat(i), n); };
            Kc.prototype.destroy = function () {
                var _this = this;
                var t = Promise.resolve();
                return "initializing" == this.state && (t = new Promise(function (t) { return _this.once("ready", t); })), t.then(function () { _this.fire("destroy"), _this.stopListening(), _this.commands.destroy(); }).then(function () { return _this.plugins.destroy(); }).then(function () { _this.model.destroy(), _this.data.destroy(), _this.editing.destroy(), _this.keystrokes.destroy(); });
            };
            Kc.prototype.execute = function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                var _k;
                (_k = this.commands).execute.apply(_k, t);
            };
            return Kc;
        }());
        ci(Kc, Fi);
        var Jc = { setData: function (t) { this.data.set(t); }, getData: function (t) { return this.data.get(t); } };
        var Zc = /** @class */ (function () {
            function Zc() {
            }
            Zc.prototype.getHtml = function (t) { var e = document.implementation.createHTMLDocument("").createElement("div"); return e.appendChild(t), e.innerHTML; };
            return Zc;
        }());
        var Xc = /** @class */ (function () {
            function Xc() {
                this._domParser = new DOMParser, this._domConverter = new rr({ blockFiller: jo }), this._htmlWriter = new Zc;
            }
            Xc.prototype.toData = function (t) { var e = this._domConverter.viewToDom(t, document); return this._htmlWriter.getHtml(e); };
            Xc.prototype.toView = function (t) { var e = this._toDom(t); return this._domConverter.domToView(e); };
            Xc.prototype._toDom = function (t) { var e = this._domParser.parseFromString(t, "text/html"), n = e.createDocumentFragment(), i = e.body.childNodes; for (; i.length > 0;)
                n.appendChild(i[0]); return n; };
            return Xc;
        }());
        var tl = /** @class */ (function () {
            function tl(t) {
                this.editor = t, this._components = new Map;
            }
            tl.prototype.names = function () { var _j, _k, t_231; return __generator(this, function (_q) {
                switch (_q.label) {
                    case 0:
                        _j = 0, _k = this._components.values();
                        _q.label = 1;
                    case 1:
                        if (!(_j < _k.length)) return [3 /*break*/, 4];
                        t_231 = _k[_j];
                        return [4 /*yield*/, t_231.originalName];
                    case 2:
                        _q.sent();
                        _q.label = 3;
                    case 3:
                        _j++;
                        return [3 /*break*/, 1];
                    case 4: return [2 /*return*/];
                }
            }); };
            tl.prototype.add = function (t, e) { if (this.has(t))
                throw new Gn.b("componentfactory-item-exists: The item already exists in the component factory.", { name: t }); this._components.set(el(t), { callback: e, originalName: t }); };
            tl.prototype.create = function (t) { if (!this.has(t))
                throw new Gn.b("componentfactory-item-missing: The required component is not registered in the factory.", { name: t }); return this._components.get(el(t)).callback(this.editor.locale); };
            tl.prototype.has = function (t) { return this._components.has(el(t)); };
            return tl;
        }());
        function el(t) { return String(t).toLowerCase(); }
        var nl = /** @class */ (function () {
            function nl() {
                this.set("isFocused", !1), this.focusedElement = null, this._elements = new Set, this._nextEventLoopTimeout = null;
            }
            nl.prototype.add = function (t) {
                var _this = this;
                if (this._elements.has(t))
                    throw new Gn.b("focusTracker-add-element-already-exist");
                this.listenTo(t, "focus", function () { return _this._focus(t); }, { useCapture: !0 }), this.listenTo(t, "blur", function () { return _this._blur(); }, { useCapture: !0 }), this._elements.add(t);
            };
            nl.prototype.remove = function (t) { t === this.focusedElement && this._blur(t), this._elements.has(t) && (this.stopListening(t), this._elements.delete(t)); };
            nl.prototype.destroy = function () { this.stopListening(); };
            nl.prototype._focus = function (t) { clearTimeout(this._nextEventLoopTimeout), this.focusedElement = t, this.isFocused = !0; };
            nl.prototype._blur = function () {
                var _this = this;
                clearTimeout(this._nextEventLoopTimeout), this._nextEventLoopTimeout = setTimeout(function () { _this.focusedElement = null, _this.isFocused = !1; }, 0);
            };
            return nl;
        }());
        ci(nl, lr), ci(nl, Fi);
        var il = /** @class */ (function () {
            function il(t) {
                var _this = this;
                this.editor = t, this.componentFactory = new tl(t), this.focusTracker = new nl, this._editableElements = new Map, this.listenTo(t.editing.view.document, "layoutChanged", function () { return _this.update(); });
            }
            Object.defineProperty(il.prototype, "element", {
                get: function () { return null; },
                enumerable: true,
                configurable: true
            });
            il.prototype.update = function () { this.fire("update"); };
            il.prototype.destroy = function () { this.stopListening(), this.focusTracker.destroy(), this._editableElements = new Map; };
            il.prototype.getEditableElement = function (t) {
                if (t === void 0) { t = "main"; }
                return this._editableElements.get(t);
            };
            il.prototype.getEditableElementsNames = function () { return this._editableElements.keys(); };
            return il;
        }());
        ci(il, ei);
        n(54);
        var ol = new WeakMap;
        function rl(t) { var e = t.view, n = t.element, i = t.text, _j = t.isDirectHost, o = _j === void 0 ? !0 : _j, r = e.document; ol.has(r) || (ol.set(r, new Map), r.registerPostFixer(function (t) { return al(r, t); })), ol.get(r).set(n, { text: i, isDirectHost: o }), e.change(function (t) { return al(r, t); }); }
        function sl(t, e) { return !!e.hasClass("ck-placeholder") && (t.removeClass("ck-placeholder", e), !0); }
        function al(t, e) { var n = ol.get(t); var i = !1; for (var _j = 0, n_186 = n; _j < n_186.length; _j++) {
            var _k = n_186[_j], t_232 = _k[0], o_57 = _k[1];
            cl(e, t_232, o_57) && (i = !0);
        } return i; }
        function cl(t, e, n) { var i = n.text, o = n.isDirectHost, r = o ? e : function (t) { if (1 === t.childCount) {
            var e_189 = t.getChild(0);
            if (e_189.is("element") && !e_189.is("uiElement"))
                return e_189;
        } return null; }(e); var s = !1; return !!r && (n.hostElement = r, r.getAttribute("data-placeholder") !== i && (t.setAttribute("data-placeholder", i, r), s = !0), !function (t) { var e = t.document; if (!e)
            return !1; var n = !Array.from(t.getChildren()).some(function (t) { return !t.is("uiElement"); }); if (!e.isFocused && n)
            return !0; var i = e.selection.anchor; return !(!n || !i || i.parent === t); }(r) ? sl(t, r) && (s = !0) : function (t, e) { return !e.hasClass("ck-placeholder") && (t.addClass("ck-placeholder", e), !0); }(t, r) && (s = !0), s); }
        var ll = /** @class */ (function (_super) {
            __extends(ll, _super);
            function ll(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.view = e, _this._toolbarConfig = function (t) { return Array.isArray(t) ? { items: t } : t ? Object.assign({ items: [] }, t) : { items: [] }; }(t.config.get("toolbar"));
                return _this;
            }
            ll.prototype.init = function () { var t = this.editor, e = this.view, n = t.editing.view, i = e.editable, o = n.document.getRoot(); e.editable.name = o.rootName, e.render(); var r = i.element; this._editableElements.set(i.name, r), this.focusTracker.add(r), e.editable.bind("isFocused").to(this.focusTracker), n.attachDomRoot(r), this._initPlaceholder(), this._initToolbar(), this.fire("ready"); };
            ll.prototype.destroy = function () { var t = this.view; this.editor.editing.view.detachDomRoot(t.editable.name), t.destroy(), _super.prototype.destroy.call(this); };
            ll.prototype._initToolbar = function () { var t = this.editor, e = this.view.toolbar; e.fillFromConfig(this._toolbarConfig.items, this.componentFactory), function (_j) {
                var t = _j.origin, e = _j.originKeystrokeHandler, n = _j.originFocusTracker, i = _j.toolbar, o = _j.beforeFocus, r = _j.afterBlur;
                n.add(i.element), e.set("Alt+F10", function (t, e) { n.isFocused && !i.focusTracker.isFocused && (o && o(), i.focus(), e()); }), i.keystrokes.set("Esc", function (e, n) { i.focusTracker.isFocused && (t.focus(), r && r(), n()); });
            }({ origin: t.editing.view, originFocusTracker: this.focusTracker, originKeystrokeHandler: t.keystrokes, toolbar: e }); };
            ll.prototype._initPlaceholder = function () { var t = this.editor, e = t.editing.view, n = e.document.getRoot(), i = t.sourceElement, o = t.config.get("placeholder") || i && "textarea" === i.tagName.toLowerCase() && i.getAttribute("placeholder"); o && rl({ view: e, element: n, text: o, isDirectHost: !1 }); };
            return ll;
        }(il));
        var dl = /** @class */ (function (_super) {
            __extends(dl, _super);
            function dl(t) {
                var _this = this;
                _this = _super.call(this, { idProperty: "viewUid" }) || this, _this.on("add", function (t, e, n) { e.isRendered || e.render(), e.element && _this._parentElement && _this._parentElement.insertBefore(e.element, _this._parentElement.children[n]); }), _this.on("remove", function (t, e) { e.element && _this._parentElement && e.element.remove(); }), _this.locale = t, _this._parentElement = null;
                return _this;
            }
            dl.prototype.destroy = function () { this.map(function (t) { return t.destroy(); }); };
            dl.prototype.setParent = function (t) { this._parentElement = t; };
            dl.prototype.delegate = function () {
                var _this = this;
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                if (!t.length || !function (t) { return t.every(function (t) { return "string" == typeof t; }); }(t))
                    throw new Gn.b("ui-viewcollection-delegate-wrong-events: All event names must be strings.");
                return { to: function (e) { for (var _j = 0, _k = _this; _j < _k.length; _j++) {
                        var n_187 = _k[_j];
                        for (var _q = 0, t_233 = t; _q < t_233.length; _q++) {
                            var i_97 = t_233[_q];
                            n_187.delegate(i_97).to(e);
                        }
                    } _this.on("add", function (n, i) { for (var _j = 0, t_234 = t; _j < t_234.length; _j++) {
                        var n_188 = t_234[_j];
                        i.delegate(n_188).to(e);
                    } }), _this.on("remove", function (n, i) { for (var _j = 0, t_235 = t; _j < t_235.length; _j++) {
                        var n_189 = t_235[_j];
                        i.stopDelegating(n_189, e);
                    } }); } };
            };
            return dl;
        }(oo));
        ci(oo, Fi);
        var ul = "http://www.w3.org/1999/xhtml";
        var hl = /** @class */ (function () {
            function hl(t) {
                Object.assign(this, vl(kl(t))), this._isRendered = !1, this._revertData = null;
            }
            hl.prototype.render = function () { var t = this._renderNode({ intoFragment: !0 }); return this._isRendered = !0, t; };
            hl.prototype.apply = function (t) { return this._revertData = { children: [], bindings: [], attributes: {} }, this._renderNode({ node: t, isApplying: !0, revertData: this._revertData }), t; };
            hl.prototype.revert = function (t) { if (!this._revertData)
                throw new Gn.b("ui-template-revert-not-applied: Attempting to revert a template which has not been applied yet."); this._revertTemplateFromNode(t, this._revertData); };
            hl.prototype.getViews = function () { return __generator(this, function (_j) {
                switch (_j.label) {
                    case 0: return [5 /*yield**/, __values(function t(e) { var _j, _k, n_190, _q, _v; return __generator(this, function (_w) {
                            switch (_w.label) {
                                case 0:
                                    if (!e.children) return [3 /*break*/, 8];
                                    _j = 0, _k = e.children;
                                    _w.label = 1;
                                case 1:
                                    if (!(_j < _k.length)) return [3 /*break*/, 8];
                                    n_190 = _k[_j];
                                    if (!Tl(n_190)) return [3 /*break*/, 3];
                                    return [4 /*yield*/, n_190];
                                case 2:
                                    _q = _w.sent();
                                    return [3 /*break*/, 6];
                                case 3:
                                    _v = Pl(n_190);
                                    if (!_v) return [3 /*break*/, 5];
                                    return [5 /*yield**/, __values(t(n_190))];
                                case 4:
                                    _v = (_w.sent());
                                    _w.label = 5;
                                case 5:
                                    _q = _v;
                                    _w.label = 6;
                                case 6:
                                    _q;
                                    _w.label = 7;
                                case 7:
                                    _j++;
                                    return [3 /*break*/, 1];
                                case 8: return [2 /*return*/];
                            }
                        }); }(this))];
                    case 1:
                        _j.sent();
                        return [2 /*return*/];
                }
            }); };
            hl.bind = function (t, e) { return { to: function (n, i) { return new ml({ eventNameOrFunction: n, attribute: n, observable: t, emitter: e, callback: i }); }, if: function (n, i, o) { return new gl({ observable: t, emitter: e, attribute: n, valueIfTrue: i, callback: o }); } }; };
            hl.extend = function (t, e) { t._isRendered && bs.a.warn("template-extend-render: Attempting to extend a template which has already been rendered."), function t(e, n) { var _j; n.attributes && (e.attributes || (e.attributes = {}), Al(e.attributes, n.attributes)); n.eventListeners && (e.eventListeners || (e.eventListeners = {}), Al(e.eventListeners, n.eventListeners)); n.text && (_j = e.text).push.apply(_j, n.text); if (n.children && n.children.length) {
                if (e.children.length != n.children.length)
                    throw new Gn.b("ui-template-extend-children-mismatch: The number of children in extended definition does not match.");
                var i_98 = 0;
                for (var _k = 0, _q = n.children; _k < _q.length; _k++) {
                    var o_58 = _q[_k];
                    t(e.children[i_98++], o_58);
                }
            } }(t, vl(kl(e))); };
            hl.prototype._renderNode = function (t) { var e; if (e = t.node ? this.tag && this.text : this.tag ? this.text : !this.text)
                throw new Gn.b('ui-template-wrong-syntax: Node definition must have either "tag" or "text" when rendering a new Node.'); return this.text ? this._renderText(t) : this._renderElement(t); };
            hl.prototype._renderElement = function (t) { var e = t.node; return e || (e = t.node = document.createElementNS(this.ns || ul, this.tag)), this._renderAttributes(t), this._renderElementChildren(t), this._setUpListeners(t), e; };
            hl.prototype._renderText = function (t) { var e = t.node; return e ? t.revertData.text = e.textContent : e = t.node = document.createTextNode(""), pl(this.text) ? this._bindToObservable({ schema: this.text, updater: function (t) { return { set: function (e) { t.textContent = e; }, remove: function () { t.textContent = ""; } }; }(e), data: t }) : e.textContent = this.text.join(""), e; };
            hl.prototype._renderAttributes = function (t) { var e, n, i, o; if (!this.attributes)
                return; var r = t.node, s = t.revertData; for (e in this.attributes)
                if (i = r.getAttribute(e), n = this.attributes[e], s && (s.attributes[e] = i), o = B(n[0]) && n[0].ns ? n[0].ns : null, pl(n)) {
                    var a_9 = o ? n[0].value : n;
                    s && El(e) && a_9.unshift(i), this._bindToObservable({ schema: a_9, updater: wl(r, e, o), data: t });
                }
                else
                    "style" == e && "string" != typeof n[0] ? this._renderStyleAttribute(n[0], t) : (s && i && El(e) && n.unshift(i), Cl(n = n.map(function (t) { return t && t.value || t; }).reduce(function (t, e) { return t.concat(e); }, []).reduce(xl, "")) || r.setAttributeNS(o, e, n)); };
            hl.prototype._renderStyleAttribute = function (t, e) { var n = e.node; for (var i_99 in t) {
                var o_59 = t[i_99];
                pl(o_59) ? this._bindToObservable({ schema: [o_59], updater: _l(n, i_99), data: e }) : n.style[i_99] = o_59;
            } };
            hl.prototype._renderElementChildren = function (t) { var e = t.node, n = t.intoFragment ? document.createDocumentFragment() : e, i = t.isApplying; var o = 0; for (var _j = 0, _k = this.children; _j < _k.length; _j++) {
                var r_27 = _k[_j];
                if (Ml(r_27)) {
                    if (!i) {
                        r_27.setParent(e);
                        for (var _q = 0, r_26 = r_27; _q < r_26.length; _q++) {
                            var t_236 = r_26[_q];
                            n.appendChild(t_236.element);
                        }
                    }
                }
                else if (Tl(r_27))
                    i || (r_27.isRendered || r_27.render(), n.appendChild(r_27.element));
                else if (Zo(r_27))
                    n.appendChild(r_27);
                else if (i) {
                    var e_190 = { children: [], bindings: [], attributes: {} };
                    t.revertData.children.push(e_190), r_27._renderNode({ node: n.childNodes[o++], isApplying: !0, revertData: e_190 });
                }
                else
                    n.appendChild(r_27.render());
            } t.intoFragment && e.appendChild(n); };
            hl.prototype._setUpListeners = function (t) { if (this.eventListeners) {
                var _loop_4 = function (e_191) {
                    var n_191 = this_1.eventListeners[e_191].map(function (n) { var _j = e_191.split("@"), i = _j[0], o = _j[1]; return n.activateDomEventListener(i, o, t); });
                    t.revertData && t.revertData.bindings.push(n_191);
                };
                var this_1 = this;
                for (var e_191 in this.eventListeners) {
                    _loop_4(e_191);
                }
            } };
            hl.prototype._bindToObservable = function (_j) {
                var t = _j.schema, e = _j.updater, n = _j.data;
                var i = n.revertData;
                bl(t, e, n);
                var o = t.filter(function (t) { return !Cl(t); }).filter(function (t) { return t.observable; }).map(function (i) { return i.activateAttributeListener(t, e, n); });
                i && i.bindings.push(o);
            };
            hl.prototype._revertTemplateFromNode = function (t, e) { for (var _j = 0, _k = e.bindings; _j < _k.length; _j++) {
                var t_238 = _k[_j];
                for (var _q = 0, t_237 = t_238; _q < t_237.length; _q++) {
                    var e_192 = t_237[_q];
                    e_192();
                }
            } if (e.text)
                t.textContent = e.text;
            else {
                for (var n_192 in e.attributes) {
                    var i_100 = e.attributes[n_192];
                    null === i_100 ? t.removeAttribute(n_192) : t.setAttribute(n_192, i_100);
                }
                for (var n_193 = 0; n_193 < e.children.length; ++n_193)
                    this._revertTemplateFromNode(t.childNodes[n_193], e.children[n_193]);
            } };
            return hl;
        }());
        ci(hl, ei);
        var fl = /** @class */ (function () {
            function fl(t) {
                Object.assign(this, t);
            }
            fl.prototype.getValue = function (t) { var e = this.observable[this.attribute]; return this.callback ? this.callback(e, t) : e; };
            fl.prototype.activateAttributeListener = function (t, e, n) {
                var _this = this;
                var i = function () { return bl(t, e, n); };
                return this.emitter.listenTo(this.observable, "change:" + this.attribute, i), function () { _this.emitter.stopListening(_this.observable, "change:" + _this.attribute, i); };
            };
            return fl;
        }());
        var ml = /** @class */ (function (_super) {
            __extends(ml, _super);
            function ml() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            ml.prototype.activateDomEventListener = function (t, e, n) {
                var _this = this;
                var i = function (t, n) { e && !n.target.matches(e) || ("function" == typeof _this.eventNameOrFunction ? _this.eventNameOrFunction(n) : _this.observable.fire(_this.eventNameOrFunction, n)); };
                return this.emitter.listenTo(n.node, t, i), function () { _this.emitter.stopListening(n.node, t, i); };
            };
            return ml;
        }(fl));
        var gl = /** @class */ (function (_super) {
            __extends(gl, _super);
            function gl() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            gl.prototype.getValue = function (t) { return !Cl(_super.prototype.getValue.call(this, t)) && (this.valueIfTrue || !0); };
            return gl;
        }(fl));
        function pl(t) { return !!t && (t.value && (t = t.value), Array.isArray(t) ? t.some(pl) : t instanceof fl); }
        function bl(t, e, _j) {
            var n = _j.node;
            var i = function (t, e) { return t.map(function (t) { return t instanceof fl ? t.getValue(e) : t; }); }(t, n);
            Cl(i = 1 == t.length && t[0] instanceof gl ? i[0] : i.reduce(xl, "")) ? e.remove() : e.set(i);
        }
        function wl(t, e, n) { return { set: function (i) { t.setAttributeNS(n, e, i); }, remove: function () { t.removeAttributeNS(n, e); } }; }
        function _l(t, e) { return { set: function (n) { t.style[e] = n; }, remove: function () { t.style[e] = null; } }; }
        function kl(t) { return qn(t, function (t) { if (t && (t instanceof fl || Pl(t) || Tl(t) || Ml(t)))
            return t; }); }
        function vl(t) { if ("string" == typeof t ? t = function (t) { return { text: [t] }; }(t) : t.text && function (t) { Array.isArray(t.text) || (t.text = [t.text]); }(t), t.on && (t.eventListeners = function (t) { for (var e_193 in t)
            yl(t, e_193); return t; }(t.on), delete t.on), !t.text) {
            t.attributes && function (t) { for (var e_194 in t)
                t[e_194].value && (t[e_194].value = [].concat(t[e_194].value)), yl(t, e_194); }(t.attributes);
            var e_195 = [];
            if (t.children)
                if (Ml(t.children))
                    e_195.push(t.children);
                else
                    for (var _j = 0, _k = t.children; _j < _k.length; _j++) {
                        var n_194 = _k[_j];
                        Pl(n_194) || Tl(n_194) || Zo(n_194) ? e_195.push(n_194) : e_195.push(new hl(n_194));
                    }
            t.children = e_195;
        } return t; }
        function yl(t, e) { Array.isArray(t[e]) || (t[e] = [t[e]]); }
        function xl(t, e) { return Cl(e) ? t : Cl(t) ? e : t + " " + e; }
        function Al(t, e) { var _j; for (var n_195 in e)
            t[n_195] ? (_j = t[n_195]).push.apply(_j, e[n_195]) : t[n_195] = e[n_195]; }
        function Cl(t) { return !t && 0 !== t; }
        function Tl(t) { return t instanceof Sl; }
        function Pl(t) { return t instanceof hl; }
        function Ml(t) { return t instanceof dl; }
        function El(t) { return "class" == t || "style" == t; }
        n(57);
        var Sl = /** @class */ (function () {
            function Sl(t) {
                this.element = null, this.isRendered = !1, this.locale = t, this.t = t && t.t, this._viewCollections = new oo, this._unboundChildren = this.createCollection(), this._viewCollections.on("add", function (e, n) { n.locale = t; }), this.decorate("render");
            }
            Object.defineProperty(Sl.prototype, "bindTemplate", {
                get: function () { return this._bindTemplate ? this._bindTemplate : this._bindTemplate = hl.bind(this, this); },
                enumerable: true,
                configurable: true
            });
            Sl.prototype.createCollection = function () { var t = new dl; return this._viewCollections.add(t), t; };
            Sl.prototype.registerChild = function (t) { pi(t) || (t = [t]); for (var _j = 0, t_239 = t; _j < t_239.length; _j++) {
                var e_196 = t_239[_j];
                this._unboundChildren.add(e_196);
            } };
            Sl.prototype.deregisterChild = function (t) { pi(t) || (t = [t]); for (var _j = 0, t_240 = t; _j < t_240.length; _j++) {
                var e_197 = t_240[_j];
                this._unboundChildren.remove(e_197);
            } };
            Sl.prototype.setTemplate = function (t) { this.template = new hl(t); };
            Sl.prototype.extendTemplate = function (t) { hl.extend(this.template, t); };
            Sl.prototype.render = function () { if (this.isRendered)
                throw new Gn.b("ui-view-render-already-rendered: This View has already been rendered."); this.template && (this.element = this.template.render(), this.registerChild(this.template.getViews())), this.isRendered = !0; };
            Sl.prototype.destroy = function () { this.stopListening(), this._viewCollections.map(function (t) { return t.destroy(); }), this.template && this.template._revertData && this.template.revert(this.element); };
            return Sl;
        }());
        ci(Sl, lr), ci(Sl, Fi);
        n(59);
        var Il = /** @class */ (function (_super) {
            __extends(Il, _super);
            function Il(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.body = _this.createCollection();
                return _this;
            }
            Il.prototype.render = function () { _super.prototype.render.call(this), this._renderBodyCollection(); };
            Il.prototype.destroy = function () { return this._bodyCollectionContainer.remove(), _super.prototype.destroy.call(this); };
            Il.prototype._renderBodyCollection = function () { var t = this._bodyCollectionContainer = new hl({ tag: "div", attributes: { class: ["ck", "ck-reset_all", "ck-body", "ck-rounded-corners"] }, children: this.body }).render(); document.body.appendChild(t); };
            return Il;
        }(Sl));
        var Nl = /** @class */ (function (_super) {
            __extends(Nl, _super);
            function Nl(t, e, n) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.setTemplate({ tag: "div", attributes: { class: ["ck", "ck-content", "ck-editor__editable", "ck-rounded-corners"] } }), _this.name = null, _this.set("isFocused", !1), _this._editableElement = n, _this._hasExternalElement = !!_this._editableElement, _this._editingView = e;
                return _this;
            }
            Nl.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this), this._hasExternalElement ? this.template.apply(this.element = this._editableElement) : this._editableElement = this.element, this.on("change:isFocused", function () { return _this._updateIsFocusedClasses(); }), this._updateIsFocusedClasses();
            };
            Nl.prototype.destroy = function () { this._hasExternalElement && this.template.revert(this._editableElement), _super.prototype.destroy.call(this); };
            Nl.prototype._updateIsFocusedClasses = function () {
                var _this = this;
                var t = this._editingView;
                function e(e) { t.change(function (n) { var i = t.document.getRoot(e.name); n.addClass(e.isFocused ? "ck-focused" : "ck-blurred", i), n.removeClass(e.isFocused ? "ck-blurred" : "ck-focused", i); }); }
                t.isRenderingInProgress ? t.once("change:isRenderingInProgress", function () { return e(_this); }) : e(this);
            };
            return Nl;
        }(Sl));
        var Ol = /** @class */ (function (_super) {
            __extends(Ol, _super);
            function Ol(t, e, n) {
                var _this = this;
                _this = _super.call(this, t, e, n) || this, _this.extendTemplate({ attributes: { role: "textbox", class: "ck-editor__editable_inline" } });
                return _this;
            }
            Ol.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this);
                var t = this._editingView, e = this.t;
                t.change(function (n) { var i = t.document.getRoot(_this.name); n.setAttribute("aria-label", e("bw", [_this.name]), i); });
            };
            return Ol;
        }(Nl));
        var Rl = /** @class */ (function () {
            function Rl(t) {
                var _this = this;
                if (Object.assign(this, t), t.actions && t.keystrokeHandler) {
                    var _loop_5 = function (e_198) {
                        var n_197 = t.actions[e_198];
                        "string" == typeof n_197 && (n_197 = [n_197]);
                        for (var _j = 0, n_196 = n_197; _j < n_196.length; _j++) {
                            var i_101 = n_196[_j];
                            t.keystrokeHandler.set(i_101, function (t, n) { _this[e_198](), n(); });
                        }
                    };
                    for (var e_198 in t.actions) {
                        _loop_5(e_198);
                    }
                }
            }
            Object.defineProperty(Rl.prototype, "first", {
                get: function () { return this.focusables.find(Dl) || null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Rl.prototype, "last", {
                get: function () { return this.focusables.filter(Dl).slice(-1)[0] || null; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Rl.prototype, "next", {
                get: function () { return this._getFocusableItem(1); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Rl.prototype, "previous", {
                get: function () { return this._getFocusableItem(-1); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Rl.prototype, "current", {
                get: function () {
                    var _this = this;
                    var t = null;
                    return null === this.focusTracker.focusedElement ? null : (this.focusables.find(function (e, n) { var i = e.element === _this.focusTracker.focusedElement; return i && (t = n), i; }), t);
                },
                enumerable: true,
                configurable: true
            });
            Rl.prototype.focusFirst = function () { this._focus(this.first); };
            Rl.prototype.focusLast = function () { this._focus(this.last); };
            Rl.prototype.focusNext = function () { this._focus(this.next); };
            Rl.prototype.focusPrevious = function () { this._focus(this.previous); };
            Rl.prototype._focus = function (t) { t && t.focus(); };
            Rl.prototype._getFocusableItem = function (t) { var e = this.current, n = this.focusables.length; if (!n)
                return null; if (null === e)
                return this[1 === t ? "first" : "last"]; var i = (e + n + t) % n; do {
                var e_199 = this.focusables.get(i);
                if (Dl(e_199))
                    return e_199;
                i = (i + n + t) % n;
            } while (i !== e); return null; };
            return Rl;
        }());
        function Dl(t) { return !(!t.focus || "none" == nr.window.getComputedStyle(t.element).display); }
        var Ll = /** @class */ (function (_super) {
            __extends(Ll, _super);
            function Ll(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.setTemplate({ tag: "span", attributes: { class: ["ck", "ck-toolbar__separator"] } });
                return _this;
            }
            return Ll;
        }(Sl));
        n(61);
        var jl = /** @class */ (function (_super) {
            __extends(jl, _super);
            function jl(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.bindTemplate;
                _this.items = _this.createCollection(), _this.focusTracker = new nl, _this.keystrokes = new Gc, _this.set("isVertical", !1), _this.set("class"), _this._focusCycler = new Rl({ focusables: _this.items, focusTracker: _this.focusTracker, keystrokeHandler: _this.keystrokes, actions: { focusPrevious: ["arrowleft", "arrowup"], focusNext: ["arrowright", "arrowdown"] } }), _this.setTemplate({ tag: "div", attributes: { class: ["ck", "ck-toolbar", e.if("isVertical", "ck-toolbar_vertical"), e.to("class")] }, children: _this.items, on: { mousedown: function (t) { return t.bindTemplate.to(function (e) { e.target === t.element && e.preventDefault(); }); }(_this) } });
                return _this;
            }
            jl.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this);
                for (var _j = 0, _k = this.items; _j < _k.length; _j++) {
                    var t_241 = _k[_j];
                    this.focusTracker.add(t_241.element);
                }
                this.items.on("add", function (t, e) { _this.focusTracker.add(e.element); }), this.items.on("remove", function (t, e) { _this.focusTracker.remove(e.element); }), this.keystrokes.listenTo(this.element);
            };
            jl.prototype.focus = function () { this._focusCycler.focusFirst(); };
            jl.prototype.focusLast = function () { this._focusCycler.focusLast(); };
            jl.prototype.fillFromConfig = function (t, e) {
                var _this = this;
                t.map(function (t) { "|" == t ? _this.items.add(new Ll) : e.has(t) ? _this.items.add(e.create(t)) : bs.a.warn("toolbarview-item-unavailable: The requested toolbar item is unavailable.", { name: t }); });
            };
            return jl;
        }(Sl));
        var Vl = /** @class */ (function (_super) {
            __extends(Vl, _super);
            function Vl(t, e, n) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.toolbar = new jl(t), _this.editable = new Ol(t, e, n), hl.extend(_this.toolbar.template, { attributes: { class: ["ck-reset_all", "ck-rounded-corners"] } });
                return _this;
            }
            Vl.prototype.render = function () { _super.prototype.render.call(this), this.registerChild([this.toolbar, this.editable]); };
            return Vl;
        }(Il));
        var zl = /** @class */ (function (_super) {
            __extends(zl, _super);
            function zl(t, e) {
                var _this = this;
                _this = _super.call(this, e) || this, Wn(t) && (_this.sourceElement = t), _this.data.processor = new Xc, _this.model.document.createRoot();
                var n = new Vl(_this.locale, _this.editing.view, _this.sourceElement);
                _this.ui = new ll(_this, n);
                return _this;
            }
            zl.prototype.destroy = function () {
                var _this = this;
                var t = this.getData();
                return this.ui.destroy(), _super.prototype.destroy.call(this).then(function () { _this.sourceElement && function (t, e) { t instanceof HTMLTextAreaElement && (t.value = e), t.innerHTML = e; }(_this.sourceElement, t); });
            };
            zl.create = function (t, e) {
                var _this = this;
                return new Promise(function (n) { var i = new _this(t, e); n(i.initPlugins().then(function () { i.ui.init(); }).then(function () { var e = Wn(t) ? function (t) { return t instanceof HTMLTextAreaElement ? t.value : t.innerHTML; }(t) : t; return i.data.init(e); }).then(function () { return i.fire("ready"); }).then(function () { return i; })); });
            };
            return zl;
        }(Kc));
        ci(zl, Jc);
        var Bl = /** @class */ (function () {
            function Bl(t) {
                this.editor = t;
            }
            Bl.prototype.destroy = function () { this.stopListening(); };
            return Bl;
        }());
        ci(Bl, Fi);
        var Fl = /** @class */ (function () {
            function Fl(t) {
                this.files = function (t) { var e = t.files ? Array.from(t.files) : [], n = t.items ? Array.from(t.items) : []; if (e.length)
                    return e; return n.filter(function (t) { return "file" === t.kind; }).map(function (t) { return t.getAsFile(); }); }(t), this._native = t;
            }
            Object.defineProperty(Fl.prototype, "types", {
                get: function () { return this._native.types; },
                enumerable: true,
                configurable: true
            });
            Fl.prototype.getData = function (t) { return this._native.getData(t); };
            Fl.prototype.setData = function (t, e) { this._native.setData(t, e); };
            return Fl;
        }());
        var Ul = /** @class */ (function (_super) {
            __extends(Ul, _super);
            function Ul(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.document;
                function n(t, n) { n.preventDefault(); var i = n.dropRange ? [n.dropRange] : Array.from(e.selection.getRanges()); e.fire("clipboardInput", { dataTransfer: n.dataTransfer, targetRanges: i }); }
                _this.domEventType = ["paste", "copy", "cut", "drop", "dragover"], _this.listenTo(e, "paste", n, { priority: "low" }), _this.listenTo(e, "drop", n, { priority: "low" });
                return _this;
            }
            Ul.prototype.onDomEvent = function (t) { var e = { dataTransfer: new Fl(t.clipboardData ? t.clipboardData : t.dataTransfer) }; "drop" == t.type && (e.dropRange = function (t, e) { var n = e.target.ownerDocument, i = e.clientX, o = e.clientY; var r; n.caretRangeFromPoint && n.caretRangeFromPoint(i, o) ? r = n.caretRangeFromPoint(i, o) : e.rangeParent && ((r = n.createRange()).setStart(e.rangeParent, e.rangeOffset), r.collapse(!0)); return r ? t.domConverter.domRangeToView(r) : t.document.selection.getFirstRange(); }(this.view, t)), this.fire(t.type, t, e); };
            return Ul;
        }(ts));
        var Hl = ["figcaption", "li"];
        var ql = /** @class */ (function (_super) {
            __extends(ql, _super);
            function ql() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(ql, "pluginName", {
                get: function () { return "Clipboard"; },
                enumerable: true,
                configurable: true
            });
            ql.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.model.document, n = t.editing.view, i = n.document;
                function o(n, o) { var r = o.dataTransfer; o.preventDefault(); var s = t.data.toView(t.model.getSelectedContent(e.selection)); i.fire("clipboardOutput", { dataTransfer: r, content: s, method: n.name }); }
                this._htmlDataProcessor = new Xc, n.addObserver(Ul), this.listenTo(i, "clipboardInput", function (e) { t.isReadOnly && e.stop(); }, { priority: "highest" }), this.listenTo(i, "clipboardInput", function (t, e) { var i = e.dataTransfer; var o = ""; i.getData("text/html") ? o = function (t) { return t.replace(/<span(?: class="Apple-converted-space"|)>(\s+)<\/span>/g, function (t, e) { return 1 == e.length ? " " : e; }); }(i.getData("text/html")) : i.getData("text/plain") && (o = function (t) { return (t = t.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\n\n/g, "</p><p>").replace(/\n/g, "<br>").replace(/^\s/, "&nbsp;").replace(/\s$/, "&nbsp;").replace(/\s\s/g, " &nbsp;")).indexOf("</p><p>") > -1 && (t = "<p>" + t + "</p>"), t; }(i.getData("text/plain"))), o = _this._htmlDataProcessor.toView(o), _this.fire("inputTransformation", { content: o, dataTransfer: i }), n.scrollToTheSelection(); }, { priority: "low" }), this.listenTo(this, "inputTransformation", function (t, e) { if (!e.content.isEmpty) {
                    var t_242 = _this.editor.data, n_198 = _this.editor.model, i_102 = t_242.toModel(e.content, "$clipboardHolder");
                    if (0 == i_102.childCount)
                        return;
                    n_198.insertContent(i_102);
                } }, { priority: "low" }), this.listenTo(i, "copy", o, { priority: "low" }), this.listenTo(i, "cut", function (e, n) { t.isReadOnly ? n.preventDefault() : o(e, n); }, { priority: "low" }), this.listenTo(i, "clipboardOutput", function (n, i) { i.content.isEmpty || (i.dataTransfer.setData("text/html", _this._htmlDataProcessor.toData(i.content)), i.dataTransfer.setData("text/plain", function t(e) { var n = ""; if (e.is("text") || e.is("textProxy"))
                    n = e.data;
                else if (e.is("img") && e.hasAttribute("alt"))
                    n = e.getAttribute("alt");
                else {
                    var i_103 = null;
                    for (var _j = 0, _k = e.getChildren(); _j < _k.length; _j++) {
                        var o_60 = _k[_j];
                        var e_200 = t(o_60);
                        i_103 && (i_103.is("containerElement") || o_60.is("containerElement")) && (Hl.includes(i_103.name) || Hl.includes(o_60.name) ? n += "\n" : n += "\n\n"), n += e_200, i_103 = o_60;
                    }
                } return n; }(i.content))), "cut" == i.method && t.model.deleteContent(e.selection); }, { priority: "low" });
            };
            return ql;
        }(Bl));
        var Wl = /** @class */ (function () {
            function Wl(t) {
                var _this = this;
                this.editor = t, this.set("value", void 0), this.set("isEnabled", !1), this.decorate("execute"), this.listenTo(this.editor.model.document, "change", function () { _this.refresh(); }), this.on("execute", function (t) { _this.isEnabled || t.stop(); }, { priority: "high" }), this.listenTo(t, "change:isReadOnly", function (t, e, n) { n ? (_this.on("set:isEnabled", Yl, { priority: "highest" }), _this.isEnabled = !1) : (_this.off("set:isEnabled", Yl), _this.refresh()); });
            }
            Wl.prototype.refresh = function () { this.isEnabled = !0; };
            Wl.prototype.execute = function () { };
            Wl.prototype.destroy = function () { this.stopListening(); };
            return Wl;
        }());
        function Yl(t) { t.return = !1, t.stop(); }
        ci(Wl, Fi);
        var $l = /** @class */ (function (_super) {
            __extends($l, _super);
            function $l() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            $l.prototype.execute = function () {
                var _this = this;
                var t = this.editor.model, e = t.document;
                t.change(function (n) { !function (t, e, n, i) { var o = n.isCollapsed, r = n.getFirstRange(), s = r.start.parent, a = r.end.parent; if (i.isLimit(s) || i.isLimit(a))
                    return void (o || s != a || t.deleteContent(n)); if (o)
                    Gl(e, r.start);
                else {
                    var i_104 = !(r.start.isAtStart && r.end.isAtEnd), o_61 = s == a;
                    t.deleteContent(n, { leaveUnmerged: i_104 }), i_104 && (o_61 ? Gl(e, n.focus) : e.setSelection(a, 0));
                } }(_this.editor.model, n, e.selection, t.schema), _this.fire("afterExecute", { writer: n }); });
            };
            return $l;
        }(Wl));
        function Gl(t, e) { t.split(e), t.setSelection(e.parent.nextSibling, 0); }
        var Ql = /** @class */ (function (_super) {
            __extends(Ql, _super);
            function Ql(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.document;
                e.on("keydown", function (t, n) { if (_this.isEnabled && n.keyCode == bo.enter) {
                    var i_105;
                    e.once("enter", function (t) { return i_105 = t; }, { priority: "highest" }), e.fire("enter", new Xr(e, n.domEvent, { isSoft: n.shiftKey })), i_105 && i_105.stop.called && t.stop();
                } });
                return _this;
            }
            Ql.prototype.observe = function () { };
            return Ql;
        }(hr));
        var Kl = /** @class */ (function (_super) {
            __extends(Kl, _super);
            function Kl() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Kl, "pluginName", {
                get: function () { return "Enter"; },
                enumerable: true,
                configurable: true
            });
            Kl.prototype.init = function () { var t = this.editor, e = t.editing.view, n = e.document; e.addObserver(Ql), t.commands.add("enter", new $l(t)), this.listenTo(n, "enter", function (n, i) { i.preventDefault(), i.isSoft || (t.execute("enter"), e.scrollToTheSelection()); }, { priority: "low" }); };
            return Kl;
        }(Bl));
        var Jl = /** @class */ (function (_super) {
            __extends(Jl, _super);
            function Jl() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Jl.prototype.execute = function () {
                var _this = this;
                var t = this.editor.model, e = t.document;
                t.change(function (n) { !function (t, e, n) { var i = n.isCollapsed, o = n.getFirstRange(), r = o.start.parent, s = o.end.parent, a = r == s; if (i)
                    Zl(e, o.end);
                else {
                    var i_106 = !(o.start.isAtStart && o.end.isAtEnd);
                    t.deleteContent(n, { leaveUnmerged: i_106 }), a ? Zl(e, n.focus) : i_106 && e.setSelection(s, 0);
                } }(t, n, e.selection), _this.fire("afterExecute", { writer: n }); });
            };
            Jl.prototype.refresh = function () { var t = this.editor.model, e = t.document; this.isEnabled = function (t, e) { if (e.rangeCount > 1)
                return !1; var n = e.anchor; if (!n || !t.checkChild(n, "softBreak"))
                return !1; var i = e.getFirstRange(), o = i.start.parent, r = i.end.parent; if ((Xl(o, t) || Xl(r, t)) && o !== r)
                return !1; return !0; }(t.schema, e.selection); };
            return Jl;
        }(Wl));
        function Zl(t, e) { var n = t.createElement("softBreak"); t.insert(n, e), t.setSelection(n, "after"); }
        function Xl(t, e) { return !t.is("rootElement") && (e.isLimit(t) || Xl(t.parent, e)); }
        var td = /** @class */ (function (_super) {
            __extends(td, _super);
            function td() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(td, "pluginName", {
                get: function () { return "ShiftEnter"; },
                enumerable: true,
                configurable: true
            });
            td.prototype.init = function () { var t = this.editor, e = t.model.schema, n = t.conversion, i = t.editing.view, o = i.document; e.register("softBreak", { allowWhere: "$text", isInline: !0 }), n.for("upcast").elementToElement({ model: "softBreak", view: "br" }), n.for("downcast").elementToElement({ model: "softBreak", view: function (t, e) { return e.createEmptyElement("br"); } }), i.addObserver(Ql), t.commands.add("shiftEnter", new Jl(t)), this.listenTo(o, "enter", function (e, n) { n.preventDefault(), n.isSoft && (t.execute("shiftEnter"), i.scrollToTheSelection()); }, { priority: "low" }); };
            return td;
        }(Bl));
        var ed = /** @class */ (function () {
            function ed(t, e) {
                if (e === void 0) { e = 20; }
                var _this = this;
                this.model = t, this.size = 0, this.limit = e, this.isLocked = !1, this._changeCallback = (function (t, e) { "transparent" != e.type && e !== _this._batch && _this._reset(!0); }), this._selectionChangeCallback = (function () { _this._reset(); }), this.model.document.on("change", this._changeCallback), this.model.document.selection.on("change:range", this._selectionChangeCallback), this.model.document.selection.on("change:attribute", this._selectionChangeCallback);
            }
            Object.defineProperty(ed.prototype, "batch", {
                get: function () { return this._batch || (this._batch = this.model.createBatch()), this._batch; },
                enumerable: true,
                configurable: true
            });
            ed.prototype.input = function (t) { this.size += t, this.size >= this.limit && this._reset(!0); };
            ed.prototype.lock = function () { this.isLocked = !0; };
            ed.prototype.unlock = function () { this.isLocked = !1; };
            ed.prototype.destroy = function () { this.model.document.off("change", this._changeCallback), this.model.document.selection.off("change:range", this._selectionChangeCallback), this.model.document.selection.off("change:attribute", this._selectionChangeCallback); };
            ed.prototype._reset = function (t) { this.isLocked && !t || (this._batch = null, this.size = 0); };
            return ed;
        }());
        var nd = /** @class */ (function (_super) {
            __extends(nd, _super);
            function nd(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this._buffer = new ed(t.model, e);
                return _this;
            }
            Object.defineProperty(nd.prototype, "buffer", {
                get: function () { return this._buffer; },
                enumerable: true,
                configurable: true
            });
            nd.prototype.destroy = function () { _super.prototype.destroy.call(this), this._buffer.destroy(); };
            nd.prototype.execute = function (t) {
                var _this = this;
                if (t === void 0) { t = {}; }
                var e = this.editor.model, n = e.document, i = t.text || "", o = i.length, r = t.range || n.selection.getFirstRange(), s = t.resultRange;
                e.enqueueChange(this._buffer.batch, function (t) { var a = r.isCollapsed; _this._buffer.lock(), a || e.deleteContent(e.createSelection(r)), i && t.insertText(i, n.selection.getAttributes(), r.start), s ? t.setSelection(s) : a && t.setSelection(r.start.getShiftedBy(o)), _this._buffer.unlock(), _this._buffer.input(o); });
            };
            return nd;
        }(Wl));
        function id(t) { var e = null; var n = t.model, i = t.editing.view, o = t.commands.get("input"); function r() { var t = o.buffer; t.lock(), n.enqueueChange(t.batch, function () { n.deleteContent(n.document.selection); }), t.unlock(); } i.document.on("keydown", function (t, s) { return (function (t) { var s = n.document, a = i.document.isComposing, c = e && e.isEqual(s.selection); if (e = null, !o.isEnabled)
            return; if (function (t) { if (t.ctrlKey)
            return !0; return od.includes(t.keyCode); }(t) || s.selection.isCollapsed)
            return; if (a && 229 === t.keyCode)
            return; if (!a && 229 === t.keyCode && c)
            return; r(); })(s); }, { priority: "lowest" }), i.document.on("compositionstart", function () { var t = n.document, e = 1 !== t.selection.rangeCount || t.selection.getFirstRange().isFlat; if (t.selection.isCollapsed || e)
            return; r(); }, { priority: "lowest" }), i.document.on("compositionend", function () { e = n.createSelection(n.document.selection); }, { priority: "lowest" }); }
        var od = [wo("arrowUp"), wo("arrowRight"), wo("arrowDown"), wo("arrowLeft"), 9, 16, 17, 18, 19, 20, 27, 33, 34, 35, 36, 45, 91, 93, 144, 145, 173, 174, 175, 176, 177, 178, 179, 255];
        for (var t_243 = 112; t_243 <= 135; t_243++)
            od.push(t_243);
        function rd(t) { if (0 == t.length)
            return !1; for (var _j = 0, t_244 = t; _j < t_244.length; _j++) {
            var e_201 = t_244[_j];
            if ("children" === e_201.type && !sd(e_201))
                return !0;
        } return !1; }
        function sd(t) { if (t.newChildren.length - t.oldChildren.length != 1)
            return; var e = function (t, e) { var n = []; var i, o = 0; return t.forEach(function (t) { "equal" == t ? (r(), o++) : "insert" == t ? (s("insert") ? i.values.push(e[o]) : (r(), i = { type: "insert", index: o, values: [e[o]] }), o++) : s("delete") ? i.howMany++ : (r(), i = { type: "delete", index: o, howMany: 1 }); }), r(), n; function r() { i && (n.push(i), i = null); } function s(t) { return i && i.type == t; } }(Qo(t.oldChildren, t.newChildren, ad), t.newChildren); if (e.length > 1)
            return; var n = e[0]; return n.values[0] && n.values[0].is("text") ? n : void 0; }
        function ad(t, e) { return t && t.is("text") && e && e.is("text") ? t.data === e.data : t === e; }
        var cd = /** @class */ (function () {
            function cd(t) {
                this.editor = t, this.editing = this.editor.editing;
            }
            cd.prototype.handle = function (t, e) { if (rd(t))
                this._handleContainerChildrenMutations(t, e);
            else
                for (var _j = 0, t_245 = t; _j < t_245.length; _j++) {
                    var n_199 = t_245[_j];
                    this._handleTextMutation(n_199, e), this._handleTextNodeInsertion(n_199);
                } };
            cd.prototype._handleContainerChildrenMutations = function (t, e) { var n = function (t) { var e = t.map(function (t) { return t.node; }).reduce(function (t, e) { return t.getCommonAncestor(e, { includeSelf: !0 }); }); if (!e)
                return; return e.getAncestors({ includeSelf: !0, parentFirst: !0 }).find(function (t) { return t.is("containerElement") || t.is("rootElement"); }); }(t); if (!n)
                return; var i = this.editor.editing.view.domConverter.mapViewToDom(n), o = new rr, r = this.editor.data.toModel(o.domToView(i)).getChild(0), s = this.editor.editing.mapper.toModelElement(n); if (!s)
                return; var a = Array.from(r.getChildren()), c = Array.from(s.getChildren()), l = a[a.length - 1], d = c[c.length - 1]; l && l.is("softBreak") && d && !d.is("softBreak") && a.pop(); var u = this.editor.model.schema; if (!ld(a, u) || !ld(c, u))
                return; var h = a.map(function (t) { return t.is("text") ? t.data : "@"; }).join("").replace(/\u00A0/g, " "), f = c.map(function (t) { return t.is("text") ? t.data : "@"; }).join("").replace(/\u00A0/g, " "); if (f === h)
                return; var m = Qo(f, h), _j = dd(m), g = _j.firstChangeAt, p = _j.insertions, b = _j.deletions; var w = null; e && (w = this.editing.mapper.toModelRange(e.getFirstRange())); var _ = h.substr(g, p), k = this.editor.model.createRange(this.editor.model.createPositionAt(s, g), this.editor.model.createPositionAt(s, g + b)); this.editor.execute("input", { text: _, range: k, resultRange: w }); };
            cd.prototype._handleTextMutation = function (t, e) { if ("text" != t.type)
                return; var n = t.newText.replace(/\u00A0/g, " "), i = t.oldText.replace(/\u00A0/g, " "); if (i === n)
                return; var o = Qo(i, n), _j = dd(o), r = _j.firstChangeAt, s = _j.insertions, a = _j.deletions; var c = null; e && (c = this.editing.mapper.toModelRange(e.getFirstRange())); var l = this.editing.view.createPositionAt(t.node, r), d = this.editing.mapper.toModelPosition(l), u = this.editor.model.createRange(d, d.getShiftedBy(a)), h = n.substr(r, s); this.editor.execute("input", { text: h, range: u, resultRange: c }); };
            cd.prototype._handleTextNodeInsertion = function (t) { if ("children" != t.type)
                return; var e = sd(t), n = this.editing.view.createPositionAt(t.node, e.index), i = this.editing.mapper.toModelPosition(n), o = e.values[0].data; this.editor.execute("input", { text: o.replace(/\u00A0/g, " "), range: this.editor.model.createRange(i) }); };
            return cd;
        }());
        function ld(t, e) { return t.every(function (t) { return e.isInline(t); }); }
        function dd(t) { var e = null, n = null; for (var i_107 = 0; i_107 < t.length; i_107++) {
            "equal" != t[i_107] && (e = null === e ? i_107 : e, n = i_107);
        } var i = 0, o = 0; for (var r_28 = e; r_28 <= n; r_28++)
            "insert" != t[r_28] && i++, "delete" != t[r_28] && o++; return { insertions: o, deletions: i, firstChangeAt: e }; }
        var ud = /** @class */ (function (_super) {
            __extends(ud, _super);
            function ud() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(ud, "pluginName", {
                get: function () { return "Input"; },
                enumerable: true,
                configurable: true
            });
            ud.prototype.init = function () { var t = this.editor, e = new nd(t, t.config.get("typing.undoStep") || 20); t.commands.add("input", e), id(t), function (t) { t.editing.view.document.on("mutations", function (e, n, i) { new cd(t).handle(n, i); }); }(t); };
            return ud;
        }(Bl));
        var hd = /** @class */ (function (_super) {
            __extends(hd, _super);
            function hd(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.direction = e, _this._buffer = new ed(t.model, t.config.get("typing.undoStep"));
                return _this;
            }
            Object.defineProperty(hd.prototype, "buffer", {
                get: function () { return this._buffer; },
                enumerable: true,
                configurable: true
            });
            hd.prototype.execute = function (t) {
                var _this = this;
                if (t === void 0) { t = {}; }
                var e = this.editor.model, n = e.document;
                e.enqueueChange(this._buffer.batch, function (i) { _this._buffer.lock(); var o = i.createSelection(n.selection), r = o.isCollapsed; if (o.isCollapsed && e.modifySelection(o, { direction: _this.direction, unit: t.unit }), _this._shouldEntireContentBeReplacedWithParagraph(t.sequence || 1))
                    return void _this._replaceEntireContentWithParagraph(i); if (o.isCollapsed)
                    return; var s = 0; o.getFirstRange().getMinimalFlatRanges().forEach(function (t) { s += eo(t.getWalker({ singleCharacters: !0, ignoreElementEnd: !0, shallow: !0 })); }), e.deleteContent(o, { doNotResetEntireContent: r }), _this._buffer.input(s), i.setSelection(o), _this._buffer.unlock(); });
            };
            hd.prototype._shouldEntireContentBeReplacedWithParagraph = function (t) { if (t > 1)
                return !1; var e = this.editor.model, n = e.document.selection, i = e.schema.getLimitElement(n); if (!(n.isCollapsed && n.containsEntireContent(i)))
                return !1; if (!e.schema.checkChild(i, "paragraph"))
                return !1; var o = i.getChild(0); return !o || "paragraph" !== o.name; };
            hd.prototype._replaceEntireContentWithParagraph = function (t) { var e = this.editor.model, n = e.document.selection, i = e.schema.getLimitElement(n), o = t.createElement("paragraph"); t.remove(t.createRangeIn(i)), t.insert(o, i), t.setSelection(o, 0); };
            return hd;
        }(Wl));
        var fd = /** @class */ (function (_super) {
            __extends(fd, _super);
            function fd(t) {
                var _this = _super.call(this, t) || this;
                var e = t.document;
                var n = 0;
                e.on("keyup", function (t, e) { e.keyCode != bo.delete && e.keyCode != bo.backspace || (n = 0); }), e.on("keydown", function (t, i) { var o = {}; if (i.keyCode == bo.delete)
                    o.direction = "forward", o.unit = "character";
                else {
                    if (i.keyCode != bo.backspace)
                        return;
                    o.direction = "backward", o.unit = "codePoint";
                } var r = mo.isMac ? i.altKey : i.ctrlKey; var s; o.unit = r ? "word" : o.unit, o.sequence = ++n, e.once("delete", function (t) { return s = t; }, { priority: "highest" }), e.fire("delete", new Xr(e, i.domEvent, o)), s && s.stop.called && t.stop(); });
                return _this;
            }
            fd.prototype.observe = function () { };
            return fd;
        }(hr));
        function md(t) { var e = t.model, n = t.editing.view, i = 200; var o = null, r = e.createSelection(e.document.selection), s = Date.now(); e.document.selection.on("change", function (t) { var n = e.createSelection(t.source); r.isEqual(n) || (o = r, r = n, s = Date.now()); }), n.document.on("mutations", function (n, a) { rd(a) && function (t) { for (var _j = 0, t_246 = t; _j < t_246.length; _j++) {
            var e_202 = t_246[_j];
            if ("children" !== e_202.type)
                continue;
            var t_247 = e_202.oldChildren, n_200 = e_202.newChildren;
            if (!gd(t_247))
                continue;
            var i_108 = Qo(t_247, n_200), o_62 = i_108.some(function (t) { return "delete" === t; }), r_29 = i_108.some(function (t) { return "insert" === t; });
            if (o_62 && !r_29)
                return !0;
        } return !1; }(a) && (!function () { Date.now() - s < i && o && !o.isCollapsed && r.isCollapsed && r.getLastPosition().isEqual(o.getLastPosition()) && e.enqueueChange(function (t) { t.setSelection(o); }); t.execute("delete"); }(), n.stop()); }, { priority: "highest" }); }
        function gd(t) { return t.every(function (t) { return t.is("containerElement"); }); }
        var pd = /** @class */ (function (_super) {
            __extends(pd, _super);
            function pd() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(pd, "pluginName", {
                get: function () { return "Delete"; },
                enumerable: true,
                configurable: true
            });
            pd.prototype.init = function () { var t = this.editor, e = t.editing.view, n = e.document; e.addObserver(fd), t.commands.add("forwardDelete", new hd(t, "forward")), t.commands.add("delete", new hd(t, "backward")), this.listenTo(n, "delete", function (n, i) { t.execute("forward" == i.direction ? "forwardDelete" : "delete", { unit: i.unit, sequence: i.sequence }), i.preventDefault(), e.scrollToTheSelection(); }), md(t); };
            return pd;
        }(Bl));
        var bd = /** @class */ (function (_super) {
            __extends(bd, _super);
            function bd() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(bd, "requires", {
                get: function () { return [ud, pd]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(bd, "pluginName", {
                get: function () { return "Typing"; },
                enumerable: true,
                configurable: true
            });
            return bd;
        }(Bl));
        var wd = /** @class */ (function (_super) {
            __extends(wd, _super);
            function wd() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(wd.prototype, "type", {
                get: function () { return "noop"; },
                enumerable: true,
                configurable: true
            });
            wd.prototype.clone = function () { return new wd(this.baseVersion); };
            wd.prototype.getReversed = function () { return new wd(this.baseVersion + 1); };
            wd.prototype._execute = function () { };
            Object.defineProperty(wd, "className", {
                get: function () { return "NoOperation"; },
                enumerable: true,
                configurable: true
            });
            return wd;
        }(Qa));
        var _d = new Map;
        function kd(t, e, n) { var i = _d.get(t); i || (i = new Map, _d.set(t, i)), i.set(e, n); }
        function vd(t) { return [t]; }
        function yd(t, e, n) {
            if (n === void 0) { n = {}; }
            var i = function (t, e) { var n = _d.get(t); return n && n.has(e) ? n.get(e) : vd; }(t.constructor, e.constructor);
            try {
                return i(t = t.clone(), e, n);
            }
            catch (i) {
                throw bs.a.error("Error during operation transformation!", i.message), bs.a.error("Transformed operation", t), bs.a.error("Operation transformed by", e), bs.a.error("context.aIsStrong", n.aIsStrong), bs.a.error("context.aWasUndone", n.aWasUndone), bs.a.error("context.bWasUndone", n.bWasUndone), bs.a.error("context.abRelation", n.abRelation), bs.a.error("context.baRelation", n.baRelation), i;
            }
        }
        function xd(t, e, n) { if (t = t.slice(), e = e.slice(), 0 == t.length || 0 == e.length)
            return { operationsA: t, operationsB: e }; var i = new WeakMap; for (var _j = 0, t_248 = t; _j < t_248.length; _j++) {
            var e_203 = t_248[_j];
            i.set(e_203, 0);
        } var o = { nextBaseVersionA: t[t.length - 1].baseVersion + 1, nextBaseVersionB: e[e.length - 1].baseVersion + 1, originalOperationsACount: t.length, originalOperationsBCount: e.length }, r = new Ad(n.document, n.useRelations, n.forceWeakRemove); r.setOriginalOperations(t), r.setOriginalOperations(e); var s = 0; for (; s < t.length;) {
            var n_201 = t[s], o_63 = i.get(n_201);
            if (o_63 == e.length) {
                s++;
                continue;
            }
            var a_10 = e[o_63], c_7 = yd(n_201, a_10, r.getContext(n_201, a_10, !0)), l_1 = yd(a_10, n_201, r.getContext(a_10, n_201, !1));
            r.updateRelation(n_201, a_10), r.setOriginalOperations(c_7, n_201), r.setOriginalOperations(l_1, a_10);
            for (var _k = 0, c_6 = c_7; _k < c_6.length; _k++) {
                var t_249 = c_6[_k];
                i.set(t_249, o_63 + l_1.length);
            }
            t.splice.apply(t, [s, 1].concat(c_7)), e.splice.apply(e, [o_63, 1].concat(l_1));
        } if (n.padWithNoOps) {
            var n_202 = t.length - o.originalOperationsACount, i_109 = e.length - o.originalOperationsBCount;
            Td(t, i_109 - n_202), Td(e, n_202 - i_109);
        } return Cd(t, o.nextBaseVersionB), Cd(e, o.nextBaseVersionA), { operationsA: t, operationsB: e }; }
        var Ad = /** @class */ (function () {
            function Ad(t, e, n) {
                if (n === void 0) { n = !1; }
                this._history = t.history, this._useRelations = e, this._forceWeakRemove = !!n, this._originalOperations = new Map, this._relations = new Map;
            }
            Ad.prototype.setOriginalOperations = function (t, e) {
                if (e === void 0) { e = null; }
                var n = e ? this._originalOperations.get(e) : null;
                for (var _j = 0, t_250 = t; _j < t_250.length; _j++) {
                    var e_204 = t_250[_j];
                    this._originalOperations.set(e_204, n || e_204);
                }
            };
            Ad.prototype.updateRelation = function (t, e) { switch (t.constructor) {
                case ac:
                    switch (e.constructor) {
                        case hc:
                            t.targetPosition.isEqual(e.sourcePosition) || e.movedRange.containsPosition(t.targetPosition) ? this._setRelation(t, e, "insertAtSource") : t.targetPosition.isEqual(e.deletionPosition) ? this._setRelation(t, e, "insertBetween") : t.targetPosition.isAfter(e.sourcePosition) && this._setRelation(t, e, "moveTargetAfter");
                            break;
                        case ac: t.targetPosition.isEqual(e.sourcePosition) || t.targetPosition.isBefore(e.sourcePosition) ? this._setRelation(t, e, "insertBefore") : this._setRelation(t, e, "insertAfter");
                    }
                    break;
                case fc:
                    switch (e.constructor) {
                        case hc:
                            t.splitPosition.isBefore(e.sourcePosition) && this._setRelation(t, e, "splitBefore");
                            break;
                        case ac: (t.splitPosition.isEqual(e.sourcePosition) || t.splitPosition.isBefore(e.sourcePosition)) && this._setRelation(t, e, "splitBefore");
                    }
                    break;
                case hc:
                    switch (e.constructor) {
                        case hc:
                            t.targetPosition.isEqual(e.sourcePosition) || this._setRelation(t, e, "mergeTargetNotMoved"), t.sourcePosition.isEqual(e.sourcePosition) && this._setRelation(t, e, "mergeSameElement");
                            break;
                        case fc: t.sourcePosition.isEqual(e.splitPosition) && this._setRelation(t, e, "splitAtSource");
                    }
                    break;
                case lc: {
                    var n_203 = t.newRange;
                    if (!n_203)
                        return;
                    switch (e.constructor) {
                        case ac: {
                            var i_110 = Gs._createFromPositionAndShift(e.sourcePosition, e.howMany), o_64 = i_110.containsPosition(n_203.start) || i_110.start.isEqual(n_203.start), r_30 = i_110.containsPosition(n_203.end) || i_110.end.isEqual(n_203.end);
                            !o_64 && !r_30 || i_110.containsRange(n_203) || this._setRelation(t, e, { side: o_64 ? "left" : "right", path: o_64 ? n_203.start.path.slice() : n_203.end.path.slice() });
                            break;
                        }
                        case hc: {
                            var i_111 = n_203.start.isEqual(e.targetPosition), o_65 = n_203.start.isEqual(e.deletionPosition), r_31 = n_203.end.isEqual(e.deletionPosition), s_22 = n_203.end.isEqual(e.sourcePosition);
                            (i_111 || o_65 || r_31 || s_22) && this._setRelation(t, e, { wasInLeftElement: i_111, wasStartBeforeMergedElement: o_65, wasEndBeforeMergedElement: r_31, wasInRightElement: s_22 });
                            break;
                        }
                    }
                    break;
                }
            } };
            Ad.prototype.getContext = function (t, e, n) { return { aIsStrong: n, aWasUndone: this._wasUndone(t), bWasUndone: this._wasUndone(e), abRelation: this._useRelations ? this._getRelation(t, e) : null, baRelation: this._useRelations ? this._getRelation(e, t) : null, forceWeakRemove: this._forceWeakRemove }; };
            Ad.prototype._wasUndone = function (t) { var e = this._originalOperations.get(t); return e.wasUndone || this._history.isUndoneOperation(e); };
            Ad.prototype._getRelation = function (t, e) { var n = this._originalOperations.get(e), i = this._history.getUndoneOperation(n); if (!i)
                return null; var o = this._originalOperations.get(t), r = this._relations.get(o); return r && r.get(i) || null; };
            Ad.prototype._setRelation = function (t, e, n) { var i = this._originalOperations.get(t), o = this._originalOperations.get(e); var r = this._relations.get(i); r || (r = new Map, this._relations.set(i, r)), r.set(o, n); };
            return Ad;
        }());
        function Cd(t, e) { for (var _j = 0, t_251 = t; _j < t_251.length; _j++) {
            var n_204 = t_251[_j];
            n_204.baseVersion = e++;
        } }
        function Td(t, e) { for (var n_205 = 0; n_205 < e; n_205++)
            t.push(new wd(0)); }
        function Pd(t, e, n) { var i = t.nodes.getNode(0).getAttribute(e); if (i == n)
            return null; var o = new Gs(t.position, t.position.getShiftedBy(t.howMany)); return new rc(o, e, i, n, 0); }
        function Md(t, e) { return null === t.targetPosition._getTransformedByDeletion(e.sourcePosition, e.howMany); }
        function Ed(t, e) { var n = []; for (var i_112 = 0; i_112 < t.length; i_112++) {
            var o_66 = t[i_112], r_32 = new ac(o_66.start, o_66.end.offset - o_66.start.offset, e, 0);
            n.push(r_32);
            for (var e_205 = i_112 + 1; e_205 < t.length; e_205++)
                t[e_205] = t[e_205]._getTransformedByMove(r_32.sourcePosition, r_32.targetPosition, r_32.howMany)[0];
            e = e._getTransformedByMove(r_32.sourcePosition, r_32.targetPosition, r_32.howMany);
        } return n; }
        kd(rc, rc, function (t, e, n) { if (t.key === e.key) {
            var i_113 = t.range.getDifference(e.range).map(function (e) { return new rc(e, t.key, t.oldValue, t.newValue, 0); }), o_67 = t.range.getIntersection(e.range);
            return o_67 && n.aIsStrong && i_113.push(new rc(o_67, e.key, e.newValue, t.newValue, 0)), 0 == i_113.length ? [new wd(0)] : i_113;
        } return [t]; }), kd(rc, cc, function (t, e) { if (t.range.start.hasSameParentAs(e.position) && t.range.containsPosition(e.position)) {
            var n_206 = t.range._getTransformedByInsertion(e.position, e.howMany, !e.shouldReceiveAttributes).map(function (e) { return new rc(e, t.key, t.oldValue, t.newValue, t.baseVersion); });
            if (e.shouldReceiveAttributes) {
                var i_114 = Pd(e, t.key, t.oldValue);
                i_114 && n_206.unshift(i_114);
            }
            return n_206;
        } return t.range = t.range._getTransformedByInsertion(e.position, e.howMany, !1)[0], [t]; }), kd(rc, hc, function (t, e) { var n = []; t.range.start.hasSameParentAs(e.deletionPosition) && (t.range.containsPosition(e.deletionPosition) || t.range.start.isEqual(e.deletionPosition)) && n.push(Gs._createFromPositionAndShift(e.graveyardPosition, 1)); var i = t.range._getTransformedByMergeOperation(e); return i.isCollapsed || n.push(i), n.map(function (e) { return new rc(e, t.key, t.oldValue, t.newValue, t.baseVersion); }); }), kd(rc, ac, function (t, e) { return function (t, e) { var n = Gs._createFromPositionAndShift(e.sourcePosition, e.howMany); var i = null, o = []; n.containsRange(t, !0) ? i = t : t.start.hasSameParentAs(n.start) ? (o = t.getDifference(n), i = t.getIntersection(n)) : o = [t]; var r = []; for (var _j = 0, o_68 = o; _j < o_68.length; _j++) {
            var t_252 = o_68[_j];
            t_252 = t_252._getTransformedByDeletion(e.sourcePosition, e.howMany);
            var n_207 = e.getMovedRangeStart(), i_115 = t_252.start.hasSameParentAs(n_207);
            t_252 = t_252._getTransformedByInsertion(n_207, e.howMany, i_115), r.push.apply(r, t_252);
        } i && r.push(i._getTransformedByMove(e.sourcePosition, e.targetPosition, e.howMany, !1)[0]); return r; }(t.range, e).map(function (e) { return new rc(e, t.key, t.oldValue, t.newValue, t.baseVersion); }); }), kd(rc, fc, function (t, e) { if (t.range.end.isEqual(e.insertionPosition))
            return e.graveyardPosition || t.range.end.offset++, [t]; if (t.range.start.hasSameParentAs(e.splitPosition) && t.range.containsPosition(e.splitPosition)) {
            var n_208 = t.clone();
            return n_208.range = new Gs(e.moveTargetPosition.clone(), t.range.end._getCombined(e.splitPosition, e.moveTargetPosition)), t.range.end = e.splitPosition.clone(), t.range.end.stickiness = "toPrevious", [t, n_208];
        } return t.range = t.range._getTransformedBySplitOperation(e), [t]; }), kd(cc, rc, function (t, e) { var n = [t]; if (t.shouldReceiveAttributes && t.position.hasSameParentAs(e.range.start) && e.range.containsPosition(t.position)) {
            var i_116 = Pd(t, e.key, e.newValue);
            i_116 && n.push(i_116);
        } return n; }), kd(cc, cc, function (t, e, n) { return t.position.isEqual(e.position) && n.aIsStrong ? [t] : (t.position = t.position._getTransformedByInsertOperation(e), [t]); }), kd(cc, ac, function (t, e) { return (t.position = t.position._getTransformedByMoveOperation(e), [t]); }), kd(cc, fc, function (t, e) { return (t.position = t.position._getTransformedBySplitOperation(e), [t]); }), kd(cc, hc, function (t, e) { return (t.position = t.position._getTransformedByMergeOperation(e), [t]); }), kd(lc, cc, function (t, e) { return (t.oldRange && (t.oldRange = t.oldRange._getTransformedByInsertOperation(e)[0]), t.newRange && (t.newRange = t.newRange._getTransformedByInsertOperation(e)[0]), [t]); }), kd(lc, lc, function (t, e, n) { if (t.name == e.name) {
            if (!n.aIsStrong)
                return [new wd(0)];
            t.oldRange = e.newRange ? e.newRange.clone() : null;
        } return [t]; }), kd(lc, hc, function (t, e) { return (t.oldRange && (t.oldRange = t.oldRange._getTransformedByMergeOperation(e)), t.newRange && (t.newRange = t.newRange._getTransformedByMergeOperation(e)), [t]); }), kd(lc, ac, function (t, e, n) { if (t.oldRange && (t.oldRange = Gs._createFromRanges(t.oldRange._getTransformedByMoveOperation(e))), t.newRange) {
            if (n.abRelation) {
                var i_117 = Gs._createFromRanges(t.newRange._getTransformedByMoveOperation(e));
                if ("left" == n.abRelation.side && e.targetPosition.isEqual(t.newRange.start))
                    return t.newRange.start.path = n.abRelation.path, t.newRange.end = i_117.end, [t];
                if ("right" == n.abRelation.side && e.targetPosition.isEqual(t.newRange.end))
                    return t.newRange.start = i_117.start, t.newRange.end.path = n.abRelation.path, [t];
            }
            t.newRange = Gs._createFromRanges(t.newRange._getTransformedByMoveOperation(e));
        } return [t]; }), kd(lc, fc, function (t, e, n) { if (t.oldRange && (t.oldRange = t.oldRange._getTransformedBySplitOperation(e)), t.newRange) {
            if (n.abRelation) {
                var i_118 = t.newRange._getTransformedBySplitOperation(e);
                return t.newRange.start.isEqual(e.splitPosition) && n.abRelation.wasStartBeforeMergedElement ? t.newRange.start = $s._createAt(e.insertionPosition) : t.newRange.start.isEqual(e.splitPosition) && !n.abRelation.wasInLeftElement && (t.newRange.start = $s._createAt(e.moveTargetPosition)), t.newRange.end.isEqual(e.splitPosition) && n.abRelation.wasInRightElement ? t.newRange.end = $s._createAt(e.moveTargetPosition) : t.newRange.end.isEqual(e.splitPosition) && n.abRelation.wasEndBeforeMergedElement ? t.newRange.end = $s._createAt(e.insertionPosition) : t.newRange.end = i_118.end, [t];
            }
            t.newRange = t.newRange._getTransformedBySplitOperation(e);
        } return [t]; }), kd(hc, cc, function (t, e) { return (t.sourcePosition.hasSameParentAs(e.position) && (t.howMany += e.howMany), t.sourcePosition = t.sourcePosition._getTransformedByInsertOperation(e), t.targetPosition = t.targetPosition._getTransformedByInsertOperation(e), [t]); }), kd(hc, hc, function (t, e, n) { if (t.sourcePosition.isEqual(e.sourcePosition) && t.targetPosition.isEqual(e.targetPosition)) {
            if (n.bWasUndone) {
                var n_209 = e.graveyardPosition.path.slice();
                return n_209.push(0), t.sourcePosition = new $s(e.graveyardPosition.root, n_209), t.howMany = 0, [t];
            }
            return [new wd(0)];
        } if (t.sourcePosition.isEqual(e.sourcePosition) && !t.targetPosition.isEqual(e.targetPosition) && !n.bWasUndone && "splitAtSource" != n.abRelation) {
            var i_119 = "$graveyard" == t.targetPosition.root.rootName, o_69 = "$graveyard" == e.targetPosition.root.rootName;
            if (o_69 && !i_119 || !(i_119 && !o_69) && n.aIsStrong) {
                var n_210 = e.targetPosition._getTransformedByMergeOperation(e), i_120 = t.targetPosition._getTransformedByMergeOperation(e);
                return [new ac(n_210, t.howMany, i_120, 0)];
            }
            return [new wd(0)];
        } return t.sourcePosition.hasSameParentAs(e.targetPosition) && (t.howMany += e.howMany), t.sourcePosition = t.sourcePosition._getTransformedByMergeOperation(e), t.targetPosition = t.targetPosition._getTransformedByMergeOperation(e), t.graveyardPosition.isEqual(e.graveyardPosition) && n.aIsStrong || (t.graveyardPosition = t.graveyardPosition._getTransformedByMergeOperation(e)), [t]; }), kd(hc, ac, function (t, e, n) { var i = Gs._createFromPositionAndShift(e.sourcePosition, e.howMany); return "remove" == e.type && !n.bWasUndone && !n.forceWeakRemove && t.deletionPosition.hasSameParentAs(e.sourcePosition) && i.containsPosition(t.sourcePosition) ? [new wd(0)] : (t.sourcePosition.hasSameParentAs(e.targetPosition) && (t.howMany += e.howMany), t.sourcePosition.hasSameParentAs(e.sourcePosition) && (t.howMany -= e.howMany), t.sourcePosition = t.sourcePosition._getTransformedByMoveOperation(e), t.targetPosition = t.targetPosition._getTransformedByMoveOperation(e), t.graveyardPosition.isEqual(e.targetPosition) || (t.graveyardPosition = t.graveyardPosition._getTransformedByMoveOperation(e)), [t]); }), kd(hc, fc, function (t, e, n) { if (e.graveyardPosition && (t.graveyardPosition = t.graveyardPosition._getTransformedByDeletion(e.graveyardPosition, 1), t.deletionPosition.isEqual(e.graveyardPosition) && (t.howMany = e.howMany)), t.targetPosition.isEqual(e.splitPosition)) {
            var i_121 = 0 != e.howMany, o_70 = e.graveyardPosition && t.deletionPosition.isEqual(e.graveyardPosition);
            if (i_121 || o_70 || "mergeTargetNotMoved" == n.abRelation)
                return t.sourcePosition = t.sourcePosition._getTransformedBySplitOperation(e), [t];
        } return t.sourcePosition.isEqual(e.splitPosition) && ("mergeSameElement" == n.abRelation || t.sourcePosition.offset > 0) ? (t.sourcePosition = e.moveTargetPosition.clone(), t.targetPosition = t.targetPosition._getTransformedBySplitOperation(e), [t]) : (t.sourcePosition.hasSameParentAs(e.splitPosition) && (t.howMany = e.splitPosition.offset), t.sourcePosition = t.sourcePosition._getTransformedBySplitOperation(e), t.targetPosition = t.targetPosition._getTransformedBySplitOperation(e), [t]); }), kd(ac, cc, function (t, e) { var n = Gs._createFromPositionAndShift(t.sourcePosition, t.howMany)._getTransformedByInsertOperation(e, !1)[0]; return t.sourcePosition = n.start, t.howMany = n.end.offset - n.start.offset, t.targetPosition.isEqual(e.position) || (t.targetPosition = t.targetPosition._getTransformedByInsertOperation(e)), [t]; }), kd(ac, ac, function (t, e, n) { var i = Gs._createFromPositionAndShift(t.sourcePosition, t.howMany), o = Gs._createFromPositionAndShift(e.sourcePosition, e.howMany); var r, s = n.aIsStrong, a = !n.aIsStrong; if ("insertBefore" == n.abRelation || "insertAfter" == n.baRelation ? a = !0 : "insertAfter" != n.abRelation && "insertBefore" != n.baRelation || (a = !1), r = t.targetPosition.isEqual(e.targetPosition) && a ? t.targetPosition._getTransformedByDeletion(e.sourcePosition, e.howMany) : t.targetPosition._getTransformedByMove(e.sourcePosition, e.targetPosition, e.howMany), Md(t, e) && Md(e, t))
            return [e.getReversed()]; if (i.containsPosition(e.targetPosition) && i.containsRange(o, !0))
            return i.start = i.start._getTransformedByMove(e.sourcePosition, e.targetPosition, e.howMany), i.end = i.end._getTransformedByMove(e.sourcePosition, e.targetPosition, e.howMany), Ed([i], r); if (o.containsPosition(t.targetPosition) && o.containsRange(i, !0))
            return i.start = i.start._getCombined(e.sourcePosition, e.getMovedRangeStart()), i.end = i.end._getCombined(e.sourcePosition, e.getMovedRangeStart()), Ed([i], r); var c = li(t.sourcePosition.getParentPath(), e.sourcePosition.getParentPath()); if ("prefix" == c || "extension" == c)
            return i.start = i.start._getTransformedByMove(e.sourcePosition, e.targetPosition, e.howMany), i.end = i.end._getTransformedByMove(e.sourcePosition, e.targetPosition, e.howMany), Ed([i], r); "remove" != t.type || "remove" == e.type || n.aWasUndone || n.forceWeakRemove ? "remove" == t.type || "remove" != e.type || n.bWasUndone || n.forceWeakRemove || (s = !1) : s = !0; var l = [], d = i.getDifference(o); for (var _j = 0, d_2 = d; _j < d_2.length; _j++) {
            var t_253 = d_2[_j];
            t_253.start = t_253.start._getTransformedByDeletion(e.sourcePosition, e.howMany), t_253.end = t_253.end._getTransformedByDeletion(e.sourcePosition, e.howMany);
            var n_211 = "same" == li(t_253.start.getParentPath(), e.getMovedRangeStart().getParentPath()), i_122 = t_253._getTransformedByInsertion(e.getMovedRangeStart(), e.howMany, n_211);
            l.push.apply(l, i_122);
        } var u = i.getIntersection(o); return null !== u && s && (u.start = u.start._getCombined(e.sourcePosition, e.getMovedRangeStart()), u.end = u.end._getCombined(e.sourcePosition, e.getMovedRangeStart()), 0 === l.length ? l.push(u) : 1 == l.length ? o.start.isBefore(i.start) || o.start.isEqual(i.start) ? l.unshift(u) : l.push(u) : l.splice(1, 0, u)), 0 === l.length ? [new wd(t.baseVersion)] : Ed(l, r); }), kd(ac, fc, function (t, e, n) { var i = t.targetPosition.clone(); t.targetPosition.isEqual(e.insertionPosition) && e.graveyardPosition && "moveTargetAfter" != n.abRelation || (i = t.targetPosition._getTransformedBySplitOperation(e)); var o = Gs._createFromPositionAndShift(t.sourcePosition, t.howMany); if (o.end.isEqual(e.insertionPosition))
            return e.graveyardPosition || t.howMany++, t.targetPosition = i, [t]; if (o.start.hasSameParentAs(e.splitPosition) && o.containsPosition(e.splitPosition)) {
            var t_254 = new Gs(e.splitPosition, o.end);
            return t_254 = t_254._getTransformedBySplitOperation(e), Ed([new Gs(o.start, e.splitPosition), t_254], i);
        } t.targetPosition.isEqual(e.splitPosition) && "insertAtSource" == n.abRelation && (i = e.moveTargetPosition), t.targetPosition.isEqual(e.insertionPosition) && "insertBetween" == n.abRelation && (i = t.targetPosition); var r = [o._getTransformedBySplitOperation(e)]; if (e.graveyardPosition) {
            var i_123 = o.start.isEqual(e.graveyardPosition) || o.containsPosition(e.graveyardPosition);
            t.howMany > 1 && i_123 && !n.aWasUndone && r.push(Gs._createFromPositionAndShift(e.insertionPosition, 1));
        } return Ed(r, i); }), kd(ac, hc, function (t, e, n) { var i = Gs._createFromPositionAndShift(t.sourcePosition, t.howMany); if (e.deletionPosition.hasSameParentAs(t.sourcePosition) && i.containsPosition(e.sourcePosition))
            if ("remove" != t.type || n.forceWeakRemove) {
                if (1 == t.howMany)
                    return n.bWasUndone ? (t.sourcePosition = e.graveyardPosition.clone(), t.targetPosition = t.targetPosition._getTransformedByMergeOperation(e), [t]) : [new wd(0)];
            }
            else if (!n.aWasUndone) {
                var n_212 = [];
                var i_124 = e.graveyardPosition.clone(), o_71 = e.targetPosition._getTransformedByMergeOperation(e);
                t.howMany > 1 && (n_212.push(new ac(t.sourcePosition, t.howMany - 1, t.targetPosition, 0)), i_124 = i_124._getTransformedByMove(t.sourcePosition, t.targetPosition, t.howMany - 1), o_71 = o_71._getTransformedByMove(t.sourcePosition, t.targetPosition, t.howMany - 1));
                var r_33 = e.deletionPosition._getCombined(t.sourcePosition, t.targetPosition), s_23 = new ac(i_124, 1, r_33, 0), a_11 = s_23.getMovedRangeStart().path.slice();
                a_11.push(0);
                var c_8 = new $s(s_23.targetPosition.root, a_11);
                o_71 = o_71._getTransformedByMove(i_124, r_33, 1);
                var l_2 = new ac(o_71, e.howMany, c_8, 0);
                return n_212.push(s_23), n_212.push(l_2), n_212;
            } var o = Gs._createFromPositionAndShift(t.sourcePosition, t.howMany)._getTransformedByMergeOperation(e); return t.sourcePosition = o.start, t.howMany = o.end.offset - o.start.offset, t.targetPosition = t.targetPosition._getTransformedByMergeOperation(e), [t]; }), kd(dc, cc, function (t, e) { return (t.position = t.position._getTransformedByInsertOperation(e), [t]); }), kd(dc, hc, function (t, e) { return t.position.isEqual(e.deletionPosition) ? (t.position = e.graveyardPosition.clone(), t.position.stickiness = "toNext", [t]) : (t.position = t.position._getTransformedByMergeOperation(e), [t]); }), kd(dc, ac, function (t, e) { return (t.position = t.position._getTransformedByMoveOperation(e), [t]); }), kd(dc, dc, function (t, e, n) { if (t.position.isEqual(e.position)) {
            if (!n.aIsStrong)
                return [new wd(0)];
            t.oldName = e.newName;
        } return [t]; }), kd(dc, fc, function (t, e) { if ("same" == li(t.position.path, e.splitPosition.getParentPath()) && !e.graveyardPosition) {
            return [t, new dc(t.position.getShiftedBy(1), t.oldName, t.newName, 0)];
        } return t.position = t.position._getTransformedBySplitOperation(e), [t]; }), kd(uc, uc, function (t, e, n) { if (t.root === e.root && t.key === e.key) {
            if (!n.aIsStrong || t.newValue === e.newValue)
                return [new wd(0)];
            t.oldValue = e.newValue;
        } return [t]; }), kd(fc, cc, function (t, e) { return (t.splitPosition.hasSameParentAs(e.position) && t.splitPosition.offset < e.position.offset && (t.howMany += e.howMany), t.splitPosition = t.splitPosition._getTransformedByInsertOperation(e), t.insertionPosition = fc.getInsertionPosition(t.splitPosition), [t]); }), kd(fc, hc, function (t, e, n) { if (!t.graveyardPosition && !n.bWasUndone && t.splitPosition.hasSameParentAs(e.sourcePosition)) {
            var n_213 = e.graveyardPosition.path.slice();
            n_213.push(0);
            var i_125 = new $s(e.graveyardPosition.root, n_213), o_72 = fc.getInsertionPosition(new $s(e.graveyardPosition.root, n_213)), r_34 = new fc(i_125, 0, null, 0);
            return r_34.insertionPosition = o_72, t.splitPosition = t.splitPosition._getTransformedByMergeOperation(e), t.insertionPosition = fc.getInsertionPosition(t.splitPosition), t.graveyardPosition = r_34.insertionPosition.clone(), t.graveyardPosition.stickiness = "toNext", [r_34, t];
        } return t.splitPosition.hasSameParentAs(e.deletionPosition) && !t.splitPosition.isAfter(e.deletionPosition) && t.howMany--, t.splitPosition.hasSameParentAs(e.targetPosition) && (t.howMany += e.howMany), t.splitPosition = t.splitPosition._getTransformedByMergeOperation(e), t.insertionPosition = fc.getInsertionPosition(t.splitPosition), t.graveyardPosition && (t.graveyardPosition = t.graveyardPosition._getTransformedByMergeOperation(e)), [t]; }), kd(fc, ac, function (t, e, n) { var i = Gs._createFromPositionAndShift(e.sourcePosition, e.howMany); if (t.graveyardPosition) {
            var o_73 = i.start.isEqual(t.graveyardPosition) || i.containsPosition(t.graveyardPosition);
            if (!n.bWasUndone && o_73) {
                var n_214 = t.splitPosition._getTransformedByMoveOperation(e), i_126 = t.graveyardPosition._getTransformedByMoveOperation(e), o_74 = i_126.path.slice();
                o_74.push(0);
                var r_35 = new $s(i_126.root, o_74);
                return [new ac(n_214, t.howMany, r_35, 0)];
            }
            t.graveyardPosition = t.graveyardPosition._getTransformedByMoveOperation(e);
        } if (t.splitPosition.hasSameParentAs(e.sourcePosition) && i.containsPosition(t.splitPosition)) {
            var n_215 = e.howMany - (t.splitPosition.offset - e.sourcePosition.offset);
            return t.howMany -= n_215, t.splitPosition.hasSameParentAs(e.targetPosition) && t.splitPosition.offset < e.targetPosition.offset && (t.howMany += e.howMany), t.splitPosition = e.sourcePosition.clone(), t.insertionPosition = fc.getInsertionPosition(t.splitPosition), [t];
        } return !t.splitPosition.isEqual(e.targetPosition) || "insertAtSource" != n.baRelation && "splitBefore" != n.abRelation ? (e.sourcePosition.isEqual(e.targetPosition) || (t.splitPosition.hasSameParentAs(e.sourcePosition) && t.splitPosition.offset <= e.sourcePosition.offset && (t.howMany -= e.howMany), t.splitPosition.hasSameParentAs(e.targetPosition) && t.splitPosition.offset < e.targetPosition.offset && (t.howMany += e.howMany)), t.splitPosition.stickiness = "toNone", t.splitPosition = t.splitPosition._getTransformedByMoveOperation(e), t.splitPosition.stickiness = "toNext", t.graveyardPosition ? t.insertionPosition = t.insertionPosition._getTransformedByMoveOperation(e) : t.insertionPosition = fc.getInsertionPosition(t.splitPosition), [t]) : (t.howMany += e.howMany, t.splitPosition = t.splitPosition._getTransformedByDeletion(e.sourcePosition, e.howMany), t.insertionPosition = fc.getInsertionPosition(t.splitPosition), [t]); }), kd(fc, fc, function (t, e, n) { if (t.splitPosition.isEqual(e.splitPosition)) {
            if (!t.graveyardPosition && !e.graveyardPosition)
                return [new wd(0)];
            if (t.graveyardPosition && e.graveyardPosition && t.graveyardPosition.isEqual(e.graveyardPosition))
                return [new wd(0)];
        } if (t.graveyardPosition && e.graveyardPosition && t.graveyardPosition.isEqual(e.graveyardPosition)) {
            var i_127 = "$graveyard" == t.splitPosition.root.rootName, o_75 = "$graveyard" == e.splitPosition.root.rootName;
            if (o_75 && !i_127 || !(i_127 && !o_75) && n.aIsStrong) {
                var n_216 = [];
                return e.howMany && n_216.push(new ac(e.moveTargetPosition, e.howMany, e.splitPosition, 0)), t.howMany && n_216.push(new ac(t.splitPosition, t.howMany, t.moveTargetPosition, 0)), n_216;
            }
            return [new wd(0)];
        } if (t.graveyardPosition && (t.graveyardPosition = t.graveyardPosition._getTransformedBySplitOperation(e)), t.splitPosition.isEqual(e.insertionPosition) && "splitBefore" == n.abRelation)
            return t.howMany++, [t]; if (e.splitPosition.isEqual(t.insertionPosition) && "splitBefore" == n.baRelation) {
            var n_217 = e.insertionPosition.path.slice();
            n_217.push(0);
            var i_128 = new $s(e.insertionPosition.root, n_217);
            return [t, new ac(t.insertionPosition, 1, i_128, 0)];
        } return t.splitPosition.hasSameParentAs(e.splitPosition) && t.splitPosition.offset < e.splitPosition.offset && (t.howMany -= e.howMany), t.splitPosition = t.splitPosition._getTransformedBySplitOperation(e), t.insertionPosition = fc.getInsertionPosition(t.splitPosition), [t]; });
        var Sd = /** @class */ (function (_super) {
            __extends(Sd, _super);
            function Sd(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this._stack = [], _this._createdBatches = new WeakSet, _this.refresh();
                return _this;
            }
            Sd.prototype.refresh = function () { this.isEnabled = this._stack.length > 0; };
            Sd.prototype.addBatch = function (t) { var e = this.editor.model.document.selection, n = { ranges: e.hasOwnRange ? Array.from(e.getRanges()) : [], isBackward: e.isBackward }; this._stack.push({ batch: t, selection: n }), this.refresh(); };
            Sd.prototype.clearStack = function () { this._stack = [], this.refresh(); };
            Sd.prototype._restoreSelection = function (t, e, n) { var i = this.editor.model, o = i.document, r = []; for (var _j = 0, t_255 = t; _j < t_255.length; _j++) {
                var e_206 = t_255[_j];
                var t_256 = Id(e_206, n).find(function (t) { return t.start.root != o.graveyard; });
                t_256 && r.push(t_256);
            } r.length && i.change(function (t) { t.setSelection(r, { backward: e }); }); };
            Sd.prototype._undo = function (t, e) { var n = this.editor.model, i = n.document; this._createdBatches.add(e); var o = t.operations.slice().filter(function (t) { return t.isDocumentOperation; }); o.reverse(); for (var _j = 0, o_76 = o; _j < o_76.length; _j++) {
                var t_257 = o_76[_j];
                var o_77 = t_257.baseVersion + 1, r_36 = Array.from(i.history.getOperations(o_77)), s_25 = xd([t_257.getReversed()], r_36, { useRelations: !0, document: this.editor.model.document, padWithNoOps: !1, forceWeakRemove: !0 }).operationsA;
                for (var _k = 0, s_24 = s_25; _k < s_24.length; _k++) {
                    var o_78 = s_24[_k];
                    e.addOperation(o_78), n.applyOperation(o_78), i.history.setOperationAsUndone(t_257, o_78);
                }
            } };
            return Sd;
        }(Wl));
        function Id(t, e) { var n = t.getTransformedByOperations(e); n.sort(function (t, e) { return t.start.isBefore(e.start) ? -1 : 1; }); for (var t_258 = 1; t_258 < n.length; t_258++) {
            var e_207 = n[t_258 - 1], i_129 = n[t_258];
            e_207.end.isTouching(i_129.start) && (e_207.end = i_129.end, n.splice(t_258, 1), t_258--);
        } return n; }
        var Nd = /** @class */ (function (_super) {
            __extends(Nd, _super);
            function Nd() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Nd.prototype.execute = function (t) {
                var _this = this;
                if (t === void 0) { t = null; }
                var e = t ? this._stack.findIndex(function (e) { return e.batch == t; }) : this._stack.length - 1, n = this._stack.splice(e, 1)[0], i = this.editor.model.createBatch();
                this.editor.model.enqueueChange(i, function () { _this._undo(n.batch, i); var t = _this.editor.model.document.history.getOperations(n.batch.baseVersion); _this._restoreSelection(n.selection.ranges, n.selection.isBackward, t), _this.fire("revert", n.batch, i); }), this.refresh();
            };
            return Nd;
        }(Sd));
        var Od = /** @class */ (function (_super) {
            __extends(Od, _super);
            function Od() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Od.prototype.execute = function () {
                var _this = this;
                var t = this._stack.pop(), e = this.editor.model.createBatch();
                this.editor.model.enqueueChange(e, function () { var n = t.batch.operations[t.batch.operations.length - 1].baseVersion + 1, i = _this.editor.model.document.history.getOperations(n); _this._restoreSelection(t.selection.ranges, t.selection.isBackward, i), _this._undo(t.batch, e); }), this.refresh();
            };
            return Od;
        }(Sd));
        var Rd = /** @class */ (function (_super) {
            __extends(Rd, _super);
            function Rd(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this._batchRegistry = new WeakSet;
                return _this;
            }
            Rd.prototype.init = function () {
                var _this = this;
                var t = this.editor;
                this._undoCommand = new Nd(t), this._redoCommand = new Od(t), t.commands.add("undo", this._undoCommand), t.commands.add("redo", this._redoCommand), this.listenTo(t.model, "applyOperation", function (t, e) { var n = e[0]; if (!n.isDocumentOperation)
                    return; var i = n.batch; _this._batchRegistry.has(i) || "transparent" == i.type || (_this._redoCommand._createdBatches.has(i) ? _this._undoCommand.addBatch(i) : _this._undoCommand._createdBatches.has(i) || (_this._undoCommand.addBatch(i), _this._redoCommand.clearStack()), _this._batchRegistry.add(i)); }, { priority: "highest" }), this.listenTo(this._undoCommand, "revert", function (t, e, n) { _this._redoCommand.addBatch(n); }), t.keystrokes.set("CTRL+Z", "undo"), t.keystrokes.set("CTRL+Y", "redo"), t.keystrokes.set("CTRL+SHIFT+Z", "redo");
            };
            return Rd;
        }(Bl));
        n(63);
        var Dd = /** @class */ (function (_super) {
            __extends(Dd, _super);
            function Dd() {
                var _this = _super.call(this) || this;
                var t = _this.bindTemplate;
                _this.set("content", ""), _this.set("viewBox", "0 0 20 20"), _this.set("fillColor", ""), _this.setTemplate({ tag: "svg", ns: "http://www.w3.org/2000/svg", attributes: { class: ["ck", "ck-icon"], viewBox: t.to("viewBox") } });
                return _this;
            }
            Dd.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this), this._updateXMLContent(), this._colorFillPaths(), this.on("change:content", function () { _this._updateXMLContent(), _this._colorFillPaths(); }), this.on("change:fillColor", function () { _this._colorFillPaths(); });
            };
            Dd.prototype._updateXMLContent = function () { if (this.content) {
                var t_259 = (new DOMParser).parseFromString(this.content.trim(), "image/svg+xml").querySelector("svg"), e_208 = t_259.getAttribute("viewBox");
                for (e_208 && (this.viewBox = e_208), this.element.innerHTML = ""; t_259.childNodes.length > 0;)
                    this.element.appendChild(t_259.childNodes[0]);
            } };
            Dd.prototype._colorFillPaths = function () {
                var _this = this;
                this.fillColor && this.element.querySelectorAll(".ck-icon__fill").forEach(function (t) { t.style.fill = _this.fillColor; });
            };
            return Dd;
        }(Sl));
        n(65);
        var Ld = /** @class */ (function (_super) {
            __extends(Ld, _super);
            function Ld(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.set("text", ""), _this.set("position", "s");
                var e = _this.bindTemplate;
                _this.setTemplate({ tag: "span", attributes: { class: ["ck", "ck-tooltip", e.to("position", function (t) { return "ck-tooltip_" + t; }), e.if("text", "ck-hidden", function (t) { return !t.trim(); })] }, children: [{ tag: "span", attributes: { class: ["ck", "ck-tooltip__text"] }, children: [{ text: e.to("text") }] }] });
                return _this;
            }
            return Ld;
        }(Sl));
        n(67);
        var jd = /** @class */ (function (_super) {
            __extends(jd, _super);
            function jd(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.bindTemplate, n = Jn();
                _this.set("class"), _this.set("labelStyle"), _this.set("icon"), _this.set("isEnabled", !0), _this.set("isOn", !1), _this.set("isVisible", !0), _this.set("keystroke"), _this.set("label"), _this.set("tabindex", -1), _this.set("tooltip"), _this.set("tooltipPosition", "s"), _this.set("type", "button"), _this.set("withText", !1), _this.children = _this.createCollection(), _this.tooltipView = _this._createTooltipView(), _this.labelView = _this._createLabelView(n), _this.iconView = new Dd, _this.iconView.extendTemplate({ attributes: { class: "ck-button__icon" } }), _this.bind("_tooltipString").to(_this, "tooltip", _this, "label", _this, "keystroke", _this._getTooltipString.bind(_this)), _this.setTemplate({ tag: "button", attributes: { class: ["ck", "ck-button", e.to("class"), e.if("isEnabled", "ck-disabled", function (t) { return !t; }), e.if("isVisible", "ck-hidden", function (t) { return !t; }), e.to("isOn", function (t) { return t ? "ck-on" : "ck-off"; }), e.if("withText", "ck-button_with-text")], type: e.to("type", function (t) { return t || "button"; }), tabindex: e.to("tabindex"), "aria-labelledby": "ck-editor__aria-label_" + n, "aria-disabled": e.if("isEnabled", !0, function (t) { return !t; }), "aria-pressed": e.if("isOn", !0) }, children: _this.children, on: { mousedown: e.to(function (t) { t.preventDefault(); }), click: e.to(function (t) { _this.isEnabled ? _this.fire("execute") : t.preventDefault(); }) } });
                return _this;
            }
            jd.prototype.render = function () { _super.prototype.render.call(this), this.icon && (this.iconView.bind("content").to(this, "icon"), this.children.add(this.iconView)), this.children.add(this.tooltipView), this.children.add(this.labelView); };
            jd.prototype.focus = function () { this.element.focus(); };
            jd.prototype._createTooltipView = function () { var t = new Ld; return t.bind("text").to(this, "_tooltipString"), t.bind("position").to(this, "tooltipPosition"), t; };
            jd.prototype._createLabelView = function (t) { var e = new Sl, n = this.bindTemplate; return e.setTemplate({ tag: "span", attributes: { class: ["ck", "ck-button__label"], style: n.to("labelStyle"), id: "ck-editor__aria-label_" + t }, children: [{ text: this.bindTemplate.to("label") }] }), e; };
            jd.prototype._getTooltipString = function (t, e, n) { return t ? "string" == typeof t ? t : (n && (n = function (t) { return mo.isMac ? ko(t).map(function (t) { return po[t.toLowerCase()] || t; }).reduce(function (t, e) { return t.slice(-1) in go ? t + e : t + "+" + e; }) : t; }(n)), t instanceof Function ? t(e, n) : "" + e + (n ? " (" + n + ")" : "")) : ""; };
            return jd;
        }(Sl));
        var Vd = n(22), zd = n.n(Vd), Bd = n(23), Fd = n.n(Bd);
        var Ud = /** @class */ (function (_super) {
            __extends(Ud, _super);
            function Ud() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Ud.prototype.init = function () { var t = this.editor.t; this._addButton("undo", t("bu"), "CTRL+Z", zd.a), this._addButton("redo", t("bv"), "CTRL+Y", Fd.a); };
            Ud.prototype._addButton = function (t, e, n, i) {
                var _this = this;
                var o = this.editor;
                o.ui.componentFactory.add(t, function (r) { var s = o.commands.get(t), a = new jd(r); return a.set({ label: e, icon: i, keystroke: n, tooltip: !0 }), a.bind("isEnabled").to(s, "isEnabled"), _this.listenTo(a, "execute", function () { return o.execute(t); }), a; });
            };
            return Ud;
        }(Bl));
        var Hd = /** @class */ (function (_super) {
            __extends(Hd, _super);
            function Hd() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Hd, "requires", {
                get: function () { return [Rd, Ud]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Hd, "pluginName", {
                get: function () { return "Undo"; },
                enumerable: true,
                configurable: true
            });
            return Hd;
        }(Bl));
        function qd(t) { var e = t.next(); return e.done ? null : e.value; }
        var Wd = ["left", "right", "center", "justify"];
        function Yd(t) { return Wd.includes(t); }
        function $d(t) { return "left" === t; }
        var Gd = "alignment";
        var Qd = /** @class */ (function (_super) {
            __extends(Qd, _super);
            function Qd() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Qd.prototype.refresh = function () { var t = qd(this.editor.model.document.selection.getSelectedBlocks()); this.isEnabled = !!t && this._canBeAligned(t), this.value = this.isEnabled && t.hasAttribute("alignment") ? t.getAttribute("alignment") : "left"; };
            Qd.prototype.execute = function (t) {
                var _this = this;
                if (t === void 0) { t = {}; }
                var e = this.editor.model, n = e.document, i = t.value;
                e.change(function (t) { var e = Array.from(n.selection.getSelectedBlocks()).filter(function (t) { return _this._canBeAligned(t); }), o = e[0].getAttribute("alignment"); $d(i) || o === i || !i ? function (t, e) { for (var _j = 0, t_260 = t; _j < t_260.length; _j++) {
                    var n_218 = t_260[_j];
                    e.removeAttribute(Gd, n_218);
                } }(e, t) : function (t, e, n) { for (var _j = 0, t_261 = t; _j < t_261.length; _j++) {
                    var i_130 = t_261[_j];
                    e.setAttribute(Gd, n, i_130);
                } }(e, t, i); });
            };
            Qd.prototype._canBeAligned = function (t) { return this.editor.model.schema.checkAttribute(t, Gd); };
            return Qd;
        }(Wl));
        var Kd = /** @class */ (function (_super) {
            __extends(Kd, _super);
            function Kd(t) {
                var _this = this;
                _this = _super.call(this, t) || this, t.config.define("alignment", { options: Wd.slice() });
                return _this;
            }
            Kd.prototype.init = function () { var t = this.editor, e = t.model.schema, n = t.config.get("alignment.options").filter(Yd); e.extend("$block", { allowAttributes: "alignment" }); var i = function (t) { var e = { model: { key: "alignment", values: t.slice() }, view: {} }; for (var _j = 0, t_262 = t; _j < t_262.length; _j++) {
                var n_219 = t_262[_j];
                e.view[n_219] = { key: "style", value: { "text-align": n_219 } };
            } return e; }(n.filter(function (t) { return !$d(t); })); t.conversion.attributeToAttribute(i), t.commands.add("alignment", new Qd(t)); };
            return Kd;
        }(Bl));
        var Jd = /** @class */ (function (_super) {
            __extends(Jd, _super);
            function Jd(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.bindTemplate;
                _this.set("isVisible", !1), _this.set("position", "se"), _this.children = _this.createCollection(), _this.setTemplate({ tag: "div", attributes: { class: ["ck", "ck-reset", "ck-dropdown__panel", e.to("position", function (t) { return "ck-dropdown__panel_" + t; }), e.if("isVisible", "ck-dropdown__panel-visible")] }, children: _this.children, on: { selectstart: e.to(function (t) { return t.preventDefault(); }) } });
                return _this;
            }
            Jd.prototype.focus = function () { this.children.length && this.children.first.focus(); };
            Jd.prototype.focusLast = function () { if (this.children.length) {
                var t_263 = this.children.last;
                "function" == typeof t_263.focusLast ? t_263.focusLast() : t_263.focus();
            } };
            return Jd;
        }(Sl));
        n(69);
        function Zd(_j) {
            var t = _j.element, e = _j.target, n = _j.positions, i = _j.limiter, o = _j.fitInViewport;
            var _k, _q;
            W(e) && (e = e()), W(i) && (i = i());
            var r = function (t) { for (; t && "html" != t.tagName.toLowerCase();) {
                if ("static" != nr.window.getComputedStyle(t).position)
                    return t;
                t = t.parentElement;
            } return null; }(t.parentElement), s = new As(t), a = new As(e);
            var c, l;
            if (i || o) {
                var t_264 = i && new As(i).getVisible(), e_209 = o && new As(nr.window);
                _k = function (t, e, n, i, o) { var r, s, a = 0, c = 0; var l = n.getArea(); return t.some(function (t) { var _j = Xd(t, e, n), d = _j[0], u = _j[1]; var h, f; if (i)
                    if (o) {
                        var t_265 = i.getIntersection(o);
                        h = t_265 ? t_265.getIntersectionArea(u) : 0;
                    }
                    else
                        h = i.getIntersectionArea(u); function m() { c = f, a = h, r = u, s = d; } return o && (f = o.getIntersectionArea(u)), o && !i ? f > c && m() : !o && i ? h > a && m() : f > c && h >= a ? m() : f >= c && h > a && m(), h === l; }), r ? [s, r] : null; }(n, a, s, t_264, e_209) || Xd(n[0], a, s), l = _k[0], c = _k[1];
            }
            else
                _q = Xd(n[0], a, s), l = _q[0], c = _q[1];
            var _v = tu(c), d = _v.left, u = _v.top;
            if (r) {
                var t_266 = tu(new As(r)), e_210 = ys(r);
                d -= t_266.left, u -= t_266.top, d += r.scrollLeft, u += r.scrollTop, d -= e_210.left, u -= e_210.top;
            }
            return { left: d, top: u, name: l };
        }
        function Xd(t, e, n) { var _j = t(e, n), i = _j.left, o = _j.top, r = _j.name; return [r, n.clone().moveTo(i, o)]; }
        function tu(_j) {
            var t = _j.left, e = _j.top;
            var _k = nr.window, n = _k.scrollX, i = _k.scrollY;
            return { left: t + n, top: e + i };
        }
        var eu = /** @class */ (function (_super) {
            __extends(eu, _super);
            function eu(t, e, n) {
                var _this = _super.call(this, t) || this;
                var i = _this.bindTemplate;
                _this.buttonView = e, _this.panelView = n, _this.set("isOpen", !1), _this.set("isEnabled", !0), _this.set("class"), _this.set("panelPosition", "auto"), _this.focusTracker = new nl, _this.keystrokes = new Gc, _this.setTemplate({ tag: "div", attributes: { class: ["ck", "ck-dropdown", i.to("class"), i.if("isEnabled", "ck-disabled", function (t) { return !t; })] }, children: [e, n] }), e.extendTemplate({ attributes: { class: ["ck-dropdown__button"] } });
                return _this;
            }
            eu.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this), this.listenTo(this.buttonView, "open", function () { _this.isOpen = !_this.isOpen; }), this.panelView.bind("isVisible").to(this, "isOpen"), this.on("change:isOpen", function () { if (_this.isOpen)
                    if ("auto" === _this.panelPosition) {
                        var t_267 = eu.defaultPanelPositions;
                        _this.panelView.position = Zd({ element: _this.panelView.element, target: _this.buttonView.element, fitInViewport: !0, positions: [t_267.southEast, t_267.southWest, t_267.northEast, t_267.northWest] }).name;
                    }
                    else
                        _this.panelView.position = _this.panelPosition; }), this.keystrokes.listenTo(this.element), this.focusTracker.add(this.element);
                var t = function (t, e) { _this.isOpen && (_this.buttonView.focus(), _this.isOpen = !1, e()); };
                this.keystrokes.set("arrowdown", function (t, e) { _this.buttonView.isEnabled && !_this.isOpen && (_this.isOpen = !0, e()); }), this.keystrokes.set("arrowright", function (t, e) { _this.isOpen && e(); }), this.keystrokes.set("arrowleft", t), this.keystrokes.set("esc", t);
            };
            eu.prototype.focus = function () { this.buttonView.focus(); };
            return eu;
        }(Sl));
        eu.defaultPanelPositions = { southEast: function (t) { return ({ top: t.bottom, left: t.left, name: "se" }); }, southWest: function (t, e) { return ({ top: t.bottom, left: t.left - e.width + t.width, name: "sw" }); }, northEast: function (t, e) { return ({ top: t.top - e.height, left: t.left, name: "ne" }); }, northWest: function (t, e) { return ({ top: t.bottom - e.height, left: t.left - e.width + t.width, name: "nw" }); } };
        var nu = n(10), iu = n.n(nu);
        var ou = /** @class */ (function (_super) {
            __extends(ou, _super);
            function ou(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.arrowView = _this._createArrowView(), _this.extendTemplate({ attributes: { "aria-haspopup": !0 } }), _this.delegate("execute").to(_this, "open");
                return _this;
            }
            ou.prototype.render = function () { _super.prototype.render.call(this), this.children.add(this.arrowView); };
            ou.prototype._createArrowView = function () { var t = new Dd; return t.content = iu.a, t.extendTemplate({ attributes: { class: "ck-dropdown__arrow" } }), t; };
            return ou;
        }(jd));
        n(71);
        var ru = /** @class */ (function (_super) {
            __extends(ru, _super);
            function ru() {
                var _this = this;
                _this = _super.call(this) || this, _this.items = _this.createCollection(), _this.focusTracker = new nl, _this.keystrokes = new Gc, _this._focusCycler = new Rl({ focusables: _this.items, focusTracker: _this.focusTracker, keystrokeHandler: _this.keystrokes, actions: { focusPrevious: "arrowup", focusNext: "arrowdown" } }), _this.setTemplate({ tag: "ul", attributes: { class: ["ck", "ck-reset", "ck-list"] }, children: _this.items });
                return _this;
            }
            ru.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this);
                for (var _j = 0, _k = this.items; _j < _k.length; _j++) {
                    var t_268 = _k[_j];
                    this.focusTracker.add(t_268.element);
                }
                this.items.on("add", function (t, e) { _this.focusTracker.add(e.element); }), this.items.on("remove", function (t, e) { _this.focusTracker.remove(e.element); }), this.keystrokes.listenTo(this.element);
            };
            ru.prototype.focus = function () { this._focusCycler.focusFirst(); };
            ru.prototype.focusLast = function () { this._focusCycler.focusLast(); };
            return ru;
        }(Sl));
        var su = /** @class */ (function (_super) {
            __extends(su, _super);
            function su(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.children = _this.createCollection(), _this.setTemplate({ tag: "li", attributes: { class: ["ck", "ck-list__item"] }, children: _this.children });
                return _this;
            }
            su.prototype.focus = function () { this.children.first.focus(); };
            return su;
        }(Sl));
        var au = /** @class */ (function (_super) {
            __extends(au, _super);
            function au(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.setTemplate({ tag: "li", attributes: { class: ["ck", "ck-list__separator"] } });
                return _this;
            }
            return au;
        }(Sl));
        n(73);
        var cu = /** @class */ (function (_super) {
            __extends(cu, _super);
            function cu(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.toggleSwitchView = _this._createToggleView(), _this.extendTemplate({ attributes: { class: "ck-switchbutton" } });
                return _this;
            }
            cu.prototype.render = function () { _super.prototype.render.call(this), this.children.add(this.toggleSwitchView); };
            cu.prototype._createToggleView = function () { var t = new Sl; return t.setTemplate({ tag: "span", attributes: { class: ["ck", "ck-button__toggle"] }, children: [{ tag: "span", attributes: { class: ["ck", "ck-button__toggle__inner"] } }] }), t; };
            return cu;
        }(jd));
        function lu(_j) {
            var t = _j.emitter, e = _j.activator, n = _j.callback, i = _j.contextElements;
            t.listenTo(document, "mousedown", function (t, _j) {
                var o = _j.target;
                if (e()) {
                    for (var _k = 0, i_131 = i; _k < i_131.length; _k++) {
                        var t_269 = i_131[_k];
                        if (t_269.contains(o))
                            return;
                    }
                    n();
                }
            });
        }
        n(75), n(77);
        function du(t, e) {
            if (e === void 0) { e = ou; }
            var n = new e(t), i = new Jd(t), o = new eu(t, n, i);
            return n.bind("isEnabled").to(o), n instanceof ou ? n.bind("isOn").to(o, "isOpen") : n.arrowView.bind("isOn").to(o, "isOpen"), function (t) { (function (t) { t.on("render", function () { lu({ emitter: t, activator: function () { return t.isOpen; }, callback: function () { t.isOpen = !1; }, contextElements: [t.element] }); }); })(t), function (t) { t.on("execute", function (e) { e.source instanceof cu || (t.isOpen = !1); }); }(t), function (t) { t.keystrokes.set("arrowdown", function (e, n) { t.isOpen && (t.panelView.focus(), n()); }), t.keystrokes.set("arrowup", function (e, n) { t.isOpen && (t.panelView.focusLast(), n()); }); }(t); }(o), o;
        }
        function uu(t, e) { var n = t.toolbarView = new jl; t.extendTemplate({ attributes: { class: ["ck-toolbar-dropdown"] } }), e.map(function (t) { return n.items.add(t); }), t.panelView.children.add(n), n.items.delegate("execute").to(t); }
        function hu(t, e) { var n = t.locale, i = t.listView = new ru(n); i.items.bindTo(e).using(function (_j) {
            var t = _j.type, e = _j.model;
            var _k;
            if ("separator" === t)
                return new au(n);
            if ("button" === t || "switchbutton" === t) {
                var i_132 = new su(n);
                var o_79;
                return (_k = (o_79 = "button" === t ? new jd(n) : new cu(n))).bind.apply(_k, Object.keys(e)).to(e), o_79.delegate("execute").to(i_132), i_132.children.add(o_79), i_132;
            }
        }), t.panelView.children.add(i), i.items.delegate("execute").to(t); }
        var fu = n(14), mu = n.n(fu), gu = n(24), pu = n.n(gu), bu = n(25), wu = n.n(bu), _u = n(26), ku = n.n(_u);
        var vu = new Map([["left", mu.a], ["right", pu.a], ["center", wu.a], ["justify", ku.a]]);
        var yu = /** @class */ (function (_super) {
            __extends(yu, _super);
            function yu() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(yu.prototype, "localizedOptionTitles", {
                get: function () { var t = this.editor.t; return { left: t("i"), right: t("j"), center: t("k"), justify: t("l") }; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(yu, "pluginName", {
                get: function () { return "AlignmentUI"; },
                enumerable: true,
                configurable: true
            });
            yu.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.ui.componentFactory, n = t.t, i = t.config.get("alignment.options");
                i.filter(Yd).forEach(function (t) { return _this._addButton(t); }), e.add("alignment", function (t) { var o = du(t), r = i.map(function (t) { return e.create("alignment:" + t); }); uu(o, r), o.buttonView.set({ label: n("m"), tooltip: !0 }), o.toolbarView.isVertical = !0, o.extendTemplate({ attributes: { class: "ck-alignment-dropdown" } }); var s = mu.a; return o.buttonView.bind("icon").toMany(r, "isOn", function () {
                    var t = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        t[_j] = arguments[_j];
                    }
                    var e = t.findIndex(function (t) { return t; });
                    return e < 0 ? s : r[e].icon;
                }), o.bind("isEnabled").toMany(r, "isEnabled", function () {
                    var t = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        t[_j] = arguments[_j];
                    }
                    return t.some(function (t) { return t; });
                }), o; });
            };
            yu.prototype._addButton = function (t) {
                var _this = this;
                var e = this.editor;
                e.ui.componentFactory.add("alignment:" + t, function (n) { var i = e.commands.get("alignment"), o = new jd(n); return o.set({ label: _this.localizedOptionTitles[t], icon: vu.get(t), tooltip: !0 }), o.bind("isEnabled").to(i), o.bind("isOn").to(i, "value", function (e) { return e === t; }), _this.listenTo(o, "execute", function () { e.execute("alignment", { value: t }), e.editing.view.focus(); }), o; });
            };
            return yu;
        }(Bl));
        var xu = /** @class */ (function (_super) {
            __extends(xu, _super);
            function xu(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.attributeKey = e;
                return _this;
            }
            xu.prototype.refresh = function () { var t = this.editor.model, e = t.document; this.value = e.selection.getAttribute(this.attributeKey), this.isEnabled = t.schema.checkAttributeInSelection(e.selection, this.attributeKey); };
            xu.prototype.execute = function (t) {
                var _this = this;
                if (t === void 0) { t = {}; }
                var e = this.editor.model, n = e.document.selection, i = t.value;
                e.change(function (t) { if (n.isCollapsed)
                    i ? t.setSelectionAttribute(_this.attributeKey, i) : t.removeSelectionAttribute(_this.attributeKey);
                else {
                    var o_81 = e.schema.getValidRanges(n.getRanges(), _this.attributeKey);
                    for (var _j = 0, o_80 = o_81; _j < o_80.length; _j++) {
                        var e_211 = o_80[_j];
                        i ? t.setAttribute(_this.attributeKey, i, e_211) : t.removeAttribute(_this.attributeKey, e_211);
                    }
                } });
            };
            return xu;
        }(Wl));
        var Au = /** @class */ (function (_super) {
            __extends(Au, _super);
            function Au(t) {
                return _super.call(this, t, "fontSize") || this;
            }
            return Au;
        }(xu));
        function Cu(t) { return t.map(Pu).filter(function (t) { return !!t; }); }
        var Tu = { tiny: { title: "Tiny", model: "tiny", view: { name: "span", classes: "text-tiny", priority: 5 } }, small: { title: "Small", model: "small", view: { name: "span", classes: "text-small", priority: 5 } }, big: { title: "Big", model: "big", view: { name: "span", classes: "text-big", priority: 5 } }, huge: { title: "Huge", model: "huge", view: { name: "span", classes: "text-huge", priority: 5 } } };
        function Pu(t) { if ("object" == typeof t)
            return t; if (Tu[t])
            return Tu[t]; if ("default" === t)
            return { model: void 0, title: "Default" }; var e = parseFloat(t); return isNaN(e) ? void 0 : function (t) { return { title: String(t), model: t, view: { name: "span", styles: { "font-size": t + "px" }, priority: 5 } }; }(e); }
        function Mu(t, e) { var n = { model: { key: t, values: [] }, view: {}, upcastAlso: {} }; for (var _j = 0, e_212 = e; _j < e_212.length; _j++) {
            var t_270 = e_212[_j];
            n.model.values.push(t_270.model), n.view[t_270.model] = t_270.view, t_270.upcastAlso && (n.upcastAlso[t_270.model] = t_270.upcastAlso);
        } return n; }
        var Eu = "fontSize";
        var Su = /** @class */ (function (_super) {
            __extends(Su, _super);
            function Su(t) {
                var _this = this;
                _this = _super.call(this, t) || this, t.config.define(Eu, { options: ["tiny", "small", "default", "big", "huge"] });
                var e = Cu(_this.editor.config.get("fontSize.options")).filter(function (t) { return t.model; }), n = Mu(Eu, e);
                t.conversion.attributeToElement(n), t.commands.add(Eu, new Au(t));
                return _this;
            }
            Su.prototype.init = function () { this.editor.model.schema.extend("$text", { allowAttributes: Eu }); };
            return Su;
        }(Bl));
        var Iu = /** @class */ (function () {
            function Iu(t, e) {
                e && Li(this, e), t && this.set(t);
            }
            return Iu;
        }());
        ci(Iu, Fi);
        var Nu = n(27), Ou = n.n(Nu);
        n(79);
        var Ru = /** @class */ (function (_super) {
            __extends(Ru, _super);
            function Ru() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Ru.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.t, n = this._getLocalizedOptions(), i = t.commands.get("fontSize");
                t.ui.componentFactory.add("fontSize", function (o) { var r = du(o); return hu(r, function (t, e) { var n = new oo; var _loop_6 = function (i_133) {
                    var t_272 = { type: "button", model: new Iu({ commandName: "fontSize", commandParam: i_133.model, label: i_133.title, class: "ck-fontsize-option", withText: !0 }) };
                    i_133.view && i_133.view.styles && t_272.model.set("labelStyle", "font-size:" + i_133.view.styles["font-size"]), i_133.view && i_133.view.classes && t_272.model.set("class", t_272.model.class + " " + i_133.view.classes), t_272.model.bind("isOn").to(e, "value", function (t) { return t === i_133.model; }), n.add(t_272);
                }; for (var _j = 0, t_271 = t; _j < t_271.length; _j++) {
                    var i_133 = t_271[_j];
                    _loop_6(i_133);
                } return n; }(n, i)), r.buttonView.set({ label: e("d"), icon: Ou.a, tooltip: !0 }), r.extendTemplate({ attributes: { class: ["ck-font-size-dropdown"] } }), r.bind("isEnabled").to(i), _this.listenTo(r, "execute", function (e) { t.execute(e.source.commandName, { value: e.source.commandParam }), t.editing.view.focus(); }), r; });
            };
            Ru.prototype._getLocalizedOptions = function () { var t = this.editor, e = t.t, n = { Default: e("c"), Tiny: e("e"), Small: e("f"), Big: e("g"), Huge: e("h") }; return Cu(t.config.get("fontSize.options")).map(function (t) { var e = n[t.title]; return e && e != t.title && (t = Object.assign({}, t, { title: e })), t; }); };
            return Ru;
        }(Bl));
        var Du = /** @class */ (function (_super) {
            __extends(Du, _super);
            function Du(t) {
                return _super.call(this, t, "fontFamily") || this;
            }
            return Du;
        }(xu));
        function Lu(t) { return t.map(ju).filter(function (t) { return !!t; }); }
        function ju(t) { return "object" == typeof t ? t : "default" === t ? { title: "Default", model: void 0 } : "string" == typeof t ? function (t) { var e = t.replace(/"|'/g, "").split(","), n = e[0], i = e.map(Vu).join(", "); return { title: n, model: n, view: { name: "span", styles: { "font-family": i }, priority: 5 } }; }(t) : void 0; }
        function Vu(t) { return (t = t.trim()).indexOf(" ") > 0 && (t = "'" + t + "'"), t; }
        var zu = "fontFamily";
        var Bu = /** @class */ (function (_super) {
            __extends(Bu, _super);
            function Bu(t) {
                var _this = this;
                _this = _super.call(this, t) || this, t.config.define(zu, { options: ["default", "Arial, Helvetica, sans-serif", "Courier New, Courier, monospace", "Georgia, serif", "Lucida Sans Unicode, Lucida Grande, sans-serif", "Tahoma, Geneva, sans-serif", "Times New Roman, Times, serif", "Trebuchet MS, Helvetica, sans-serif", "Verdana, Geneva, sans-serif"] });
                return _this;
            }
            Bu.prototype.init = function () { var t = this.editor; t.model.schema.extend("$text", { allowAttributes: zu }); var e = Lu(t.config.get("fontFamily.options")).filter(function (t) { return t.model; }), n = Mu(zu, e); t.conversion.attributeToElement(n), t.commands.add(zu, new Du(t)); };
            return Bu;
        }(Bl));
        var Fu = n(28), Uu = n.n(Fu);
        var Hu = /** @class */ (function (_super) {
            __extends(Hu, _super);
            function Hu() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Hu.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.t, n = this._getLocalizedOptions(), i = t.commands.get("fontFamily");
                t.ui.componentFactory.add("fontFamily", function (o) { var r = du(o); return hu(r, function (t, e) { var n = new oo; var _loop_7 = function (i_134) {
                    var t_274 = { type: "button", model: new Iu({ commandName: "fontFamily", commandParam: i_134.model, label: i_134.title, withText: !0 }) };
                    t_274.model.bind("isOn").to(e, "value", function (t) { return t === i_134.model; }), i_134.view && i_134.view.styles && t_274.model.set("labelStyle", "font-family: " + i_134.view.styles["font-family"]), n.add(t_274);
                }; for (var _j = 0, t_273 = t; _j < t_273.length; _j++) {
                    var i_134 = t_273[_j];
                    _loop_7(i_134);
                } return n; }(n, i)), r.buttonView.set({ label: e("b"), icon: Uu.a, tooltip: !0 }), r.extendTemplate({ attributes: { class: "ck-font-family-dropdown" } }), r.bind("isEnabled").to(i), _this.listenTo(r, "execute", function (e) { t.execute(e.source.commandName, { value: e.source.commandParam }), t.editing.view.focus(); }), r; });
            };
            Hu.prototype._getLocalizedOptions = function () { var t = this.editor, e = t.t; return Lu(t.config.get("fontFamily.options")).map(function (t) { return ("Default" === t.title && (t.title = e("c")), t); }); };
            return Hu;
        }(Bl));
        var qu = /** @class */ (function (_super) {
            __extends(qu, _super);
            function qu() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            qu.prototype.refresh = function () { var t = this.editor.model, e = t.document; this.value = e.selection.getAttribute("highlight"), this.isEnabled = t.schema.checkAttributeInSelection(e.selection, "highlight"); };
            qu.prototype.execute = function (t) {
                var _this = this;
                if (t === void 0) { t = {}; }
                var e = this.editor.model, n = e.document.selection, i = t.value;
                e.change(function (t) { var o = e.schema.getValidRanges(n.getRanges(), "highlight"); if (n.isCollapsed) {
                    var e_213 = n.getFirstPosition();
                    if (n.hasAttribute("highlight")) {
                        var n_220 = function (t) { return t.item.hasAttribute("highlight") && t.item.getAttribute("highlight") === _this.value; }, o_83 = e_213.getLastMatchingPosition(n_220, { direction: "backward" }), r_37 = e_213.getLastMatchingPosition(n_220), s_26 = t.createRange(o_83, r_37);
                        i && _this.value !== i ? (t.setAttribute("highlight", i, s_26), t.setSelectionAttribute("highlight", i)) : (t.removeAttribute("highlight", s_26), t.removeSelectionAttribute("highlight"));
                    }
                    else
                        i && t.setSelectionAttribute("highlight", i);
                }
                else
                    for (var _j = 0, o_82 = o; _j < o_82.length; _j++) {
                        var e_214 = o_82[_j];
                        i ? t.setAttribute("highlight", i, e_214) : t.removeAttribute("highlight", e_214);
                    } });
            };
            return qu;
        }(Wl));
        var Wu = /** @class */ (function (_super) {
            __extends(Wu, _super);
            function Wu(t) {
                var _this = this;
                _this = _super.call(this, t) || this, t.config.define("highlight", { options: [{ model: "yellowMarker", class: "marker-yellow", title: "Yellow marker", color: "var(--ck-highlight-marker-yellow)", type: "marker" }, { model: "greenMarker", class: "marker-green", title: "Green marker", color: "var(--ck-highlight-marker-green)", type: "marker" }, { model: "pinkMarker", class: "marker-pink", title: "Pink marker", color: "var(--ck-highlight-marker-pink)", type: "marker" }, { model: "blueMarker", class: "marker-blue", title: "Blue marker", color: "var(--ck-highlight-marker-blue)", type: "marker" }, { model: "redPen", class: "pen-red", title: "Red pen", color: "var(--ck-highlight-pen-red)", type: "pen" }, { model: "greenPen", class: "pen-green", title: "Green pen", color: "var(--ck-highlight-pen-green)", type: "pen" }] });
                return _this;
            }
            Wu.prototype.init = function () { var t = this.editor; t.model.schema.extend("$text", { allowAttributes: "highlight" }); var e = t.config.get("highlight.options"); t.conversion.attributeToElement(function (t) { var e = { model: { key: "highlight", values: [] }, view: {} }; for (var _j = 0, t_275 = t; _j < t_275.length; _j++) {
                var n_221 = t_275[_j];
                e.model.values.push(n_221.model), e.view[n_221.model] = { name: "mark", classes: n_221.class };
            } return e; }(e)), t.commands.add("highlight", new qu(t)); };
            return Wu;
        }(Bl));
        var Yu = n(29), $u = n.n(Yu), Gu = n(30), Qu = n.n(Gu), Ku = n(31), Ju = n.n(Ku);
        n(81);
        var Zu = /** @class */ (function (_super) {
            __extends(Zu, _super);
            function Zu(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.bindTemplate;
                _this.set("icon"), _this.set("isEnabled", !0), _this.set("isOn", !1), _this.set("isVisible", !0), _this.set("keystroke"), _this.set("label"), _this.set("tabindex", -1), _this.set("tooltip"), _this.set("tooltipPosition", "s"), _this.set("type", "button"), _this.set("withText", !1), _this.children = _this.createCollection(), _this.actionView = _this._createActionView(), _this.arrowView = _this._createArrowView(), _this.keystrokes = new Gc, _this.focusTracker = new nl, _this.setTemplate({ tag: "div", attributes: { class: ["ck", "ck-splitbutton", e.if("isVisible", "ck-hidden", function (t) { return !t; }), _this.arrowView.bindTemplate.if("isOn", "ck-splitbutton_open")] }, children: _this.children });
                return _this;
            }
            Zu.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this), this.children.add(this.actionView), this.children.add(this.arrowView), this.focusTracker.add(this.actionView.element), this.focusTracker.add(this.arrowView.element), this.keystrokes.listenTo(this.element), this.keystrokes.set("arrowright", function (t, e) { _this.focusTracker.focusedElement === _this.actionView.element && (_this.arrowView.focus(), e()); }), this.keystrokes.set("arrowleft", function (t, e) { _this.focusTracker.focusedElement === _this.arrowView.element && (_this.actionView.focus(), e()); });
            };
            Zu.prototype.focus = function () { this.actionView.focus(); };
            Zu.prototype._createActionView = function () { var t = new jd; return t.bind("icon", "isEnabled", "isOn", "keystroke", "label", "tabindex", "tooltip", "tooltipPosition", "type", "withText").to(this), t.extendTemplate({ attributes: { class: "ck-splitbutton__action" } }), t.delegate("execute").to(this), t; };
            Zu.prototype._createArrowView = function () { var t = new jd; return t.icon = iu.a, t.extendTemplate({ attributes: { class: "ck-splitbutton__arrow", "aria-haspopup": !0 } }), t.bind("isEnabled").to(this), t.delegate("execute").to(this, "open"), t; };
            return Zu;
        }(Sl));
        n(83);
        var Xu = /** @class */ (function (_super) {
            __extends(Xu, _super);
            function Xu() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Xu.prototype, "localizedOptionTitles", {
                get: function () { var t = this.editor.t; return { "Yellow marker": t("n"), "Green marker": t("o"), "Pink marker": t("p"), "Blue marker": t("q"), "Red pen": t("r"), "Green pen": t("s") }; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Xu, "pluginName", {
                get: function () { return "HighlightUI"; },
                enumerable: true,
                configurable: true
            });
            Xu.prototype.init = function () { var t = this.editor.config.get("highlight.options"); for (var _j = 0, t_276 = t; _j < t_276.length; _j++) {
                var e_215 = t_276[_j];
                this._addHighlighterButton(e_215);
            } this._addRemoveHighlightButton(), this._addDropdown(t); };
            Xu.prototype._addRemoveHighlightButton = function () { var t = this.editor.t; this._addButton("removeHighlight", t("t"), Ju.a); };
            Xu.prototype._addHighlighterButton = function (t) { var e = this.editor.commands.get("highlight"); this._addButton("highlight:" + t.model, t.title, th(t.type), t.model, function (n) { n.bind("isEnabled").to(e, "isEnabled"), n.bind("isOn").to(e, "value", function (e) { return e === t.model; }), n.iconView.fillColor = t.color; }); };
            Xu.prototype._addButton = function (t, e, n, i, o) {
                var _this = this;
                if (o === void 0) { o = (function () { }); }
                var r = this.editor;
                r.ui.componentFactory.add(t, function (t) { var s = new jd(t), a = _this.localizedOptionTitles[e] ? _this.localizedOptionTitles[e] : e; return s.set({ label: a, icon: n, tooltip: !0 }), s.on("execute", function () { r.execute("highlight", { value: i }), r.editing.view.focus(); }), o(s), s; });
            };
            Xu.prototype._addDropdown = function (t) {
                var _this = this;
                var e = this.editor, n = e.t, i = e.ui.componentFactory, o = t[0], r = t.reduce(function (t, e) { return (t[e.model] = e, t); }, {});
                i.add("highlight", function (s) { var a = e.commands.get("highlight"), c = du(s, Zu), l = c.buttonView; l.set({ tooltip: n("u"), lastExecuted: o.model, commandValue: o.model }), l.bind("icon").to(a, "value", function (t) { return th(u(t, "type")); }), l.bind("color").to(a, "value", function (t) { return u(t, "color"); }), l.bind("commandValue").to(a, "value", function (t) { return u(t, "model"); }), l.bind("isOn").to(a, "value", function (t) { return !!t; }), l.delegate("execute").to(c); var d = t.map(function (t) { var e = i.create("highlight:" + t.model); return _this.listenTo(e, "execute", function () { return c.buttonView.set({ lastExecuted: t.model }); }), e; }); function u(t, e) { var n = t && t !== l.lastExecuted ? t : l.lastExecuted; return r[n][e]; } return c.bind("isEnabled").toMany(d, "isEnabled", function () {
                    var t = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        t[_j] = arguments[_j];
                    }
                    return t.some(function (t) { return t; });
                }), d.push(new Ll), d.push(i.create("removeHighlight")), uu(c, d), function (t) { t.buttonView.actionView.iconView.bind("fillColor").to(t.buttonView, "color"); }(c), l.on("execute", function () { e.execute("highlight", { value: l.commandValue }), e.editing.view.focus(); }), c; });
            };
            return Xu;
        }(Bl));
        function th(t) { return "marker" === t ? $u.a : Qu.a; }
        var eh = /** @class */ (function (_super) {
            __extends(eh, _super);
            function eh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(eh, "pluginName", {
                get: function () { return "PendingActions"; },
                enumerable: true,
                configurable: true
            });
            eh.prototype.init = function () { this.set("hasAny", !1), this._actions = new oo({ idProperty: "_id" }), this._actions.delegate("add", "remove").to(this); };
            eh.prototype.add = function (t) { if ("string" != typeof t)
                throw new Gn.b("pendingactions-add-invalid-message: The message must be a string."); var e = Object.create(Fi); return e.set("message", t), this._actions.add(e), this.hasAny = !0, e; };
            eh.prototype.remove = function (t) { this._actions.remove(t), this.hasAny = !!this._actions.length; };
            Object.defineProperty(eh.prototype, "first", {
                get: function () { return this._actions.get(0); },
                enumerable: true,
                configurable: true
            });
            eh.prototype[Symbol.iterator] = function () { return this._actions[Symbol.iterator](); };
            return eh;
        }(Bl));
        var nh = /** @class */ (function () {
            function nh() {
                var _this = this;
                var t = new window.FileReader;
                this._reader = t, this.set("loaded", 0), t.onprogress = (function (t) { _this.loaded = t.loaded; });
            }
            Object.defineProperty(nh.prototype, "error", {
                get: function () { return this._reader.error; },
                enumerable: true,
                configurable: true
            });
            nh.prototype.read = function (t) {
                var _this = this;
                var e = this._reader;
                return this.total = t.size, new Promise(function (n, i) { e.onload = (function () { n(e.result); }), e.onerror = (function () { i("error"); }), e.onabort = (function () { i("aborted"); }), _this._reader.readAsDataURL(t); });
            };
            nh.prototype.abort = function () { this._reader.abort(); };
            return nh;
        }());
        ci(nh, Fi);
        var ih = /** @class */ (function (_super) {
            __extends(ih, _super);
            function ih() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(ih, "pluginName", {
                get: function () { return "FileRepository"; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(ih, "requires", {
                get: function () { return [eh]; },
                enumerable: true,
                configurable: true
            });
            ih.prototype.init = function () {
                var _this = this;
                this.loaders = new oo, this.loaders.on("add", function () { return _this._updatePendingAction(); }), this.loaders.on("remove", function () { return _this._updatePendingAction(); }), this._loadersMap = new Map, this._pendingAction = null, this.set("uploaded", 0), this.set("uploadTotal", null), this.bind("uploadedPercent").to(this, "uploaded", this, "uploadTotal", function (t, e) { return e ? t / e * 100 : 0; });
            };
            ih.prototype.getLoader = function (t) { return this._loadersMap.get(t) || null; };
            ih.prototype.createLoader = function (t) {
                var _this = this;
                if (!this.createUploadAdapter)
                    return bs.a.error("filerepository-no-upload-adapter: Upload adapter is not defined."), null;
                var e = new oh(Promise.resolve(t), this.createUploadAdapter);
                return this.loaders.add(e), this._loadersMap.set(t, e), t instanceof Promise && e.file.then(function (t) { _this._loadersMap.set(t, e); }), e.file.catch(function () { }), e.on("change:uploaded", function () { var t = 0; for (var _j = 0, _k = _this.loaders; _j < _k.length; _j++) {
                    var e_216 = _k[_j];
                    t += e_216.uploaded;
                } _this.uploaded = t; }), e.on("change:uploadTotal", function () { var t = 0; for (var _j = 0, _k = _this.loaders; _j < _k.length; _j++) {
                    var e_217 = _k[_j];
                    e_217.uploadTotal && (t += e_217.uploadTotal);
                } _this.uploadTotal = t; }), e;
            };
            ih.prototype.destroyLoader = function (t) {
                var _this = this;
                var e = t instanceof oh ? t : this.getLoader(t);
                e._destroy(), this.loaders.remove(e), this._loadersMap.forEach(function (t, n) { t === e && _this._loadersMap.delete(n); });
            };
            ih.prototype._updatePendingAction = function () { var t = this.editor.plugins.get(eh); if (this.loaders.length) {
                if (!this._pendingAction) {
                    var e_218 = this.editor.t, n_222 = function (t) { return e_218("bm") + " " + parseInt(t) + "%."; };
                    this._pendingAction = t.add(n_222(this.uploadedPercent)), this._pendingAction.bind("message").to(this, "uploadedPercent", n_222);
                }
            }
            else
                t.remove(this._pendingAction), this._pendingAction = null; };
            return ih;
        }(Bl));
        ci(ih, Fi);
        var oh = /** @class */ (function () {
            function oh(t, e) {
                this.id = Jn(), this._filePromiseWrapper = this._createFilePromiseWrapper(t), this._adapter = e(this), this._reader = new nh, this.set("status", "idle"), this.set("uploaded", 0), this.set("uploadTotal", null), this.bind("uploadedPercent").to(this, "uploaded", this, "uploadTotal", function (t, e) { return e ? t / e * 100 : 0; }), this.set("uploadResponse", null);
            }
            Object.defineProperty(oh.prototype, "file", {
                get: function () {
                    var _this = this;
                    return this._filePromiseWrapper ? this._filePromiseWrapper.promise.then(function (t) { return _this._filePromiseWrapper ? t : null; }) : Promise.resolve(null);
                },
                enumerable: true,
                configurable: true
            });
            oh.prototype.read = function () {
                var _this = this;
                if ("idle" != this.status)
                    throw new Gn.b("filerepository-read-wrong-status: You cannot call read if the status is different than idle.");
                return this.status = "reading", this._filePromiseWrapper.promise.then(function (t) { return _this._reader.read(t); }).then(function (t) { return (_this.status = "idle", t); }).catch(function (t) { if ("aborted" === t)
                    throw _this.status = "aborted", "aborted"; throw _this.status = "error", _this._reader.error ? _this._reader.error : t; });
            };
            oh.prototype.upload = function () {
                var _this = this;
                if ("idle" != this.status)
                    throw new Gn.b("filerepository-upload-wrong-status: You cannot call upload if the status is different than idle.");
                return this.status = "uploading", this._filePromiseWrapper.promise.then(function () { return _this._adapter.upload(); }).then(function (t) { return (_this.uploadResponse = t, _this.status = "idle", t); }).catch(function (t) { if ("aborted" === _this.status)
                    throw "aborted"; throw _this.status = "error", t; });
            };
            oh.prototype.abort = function () { var t = this.status; this.status = "aborted", this._filePromiseWrapper.isFulfilled ? "reading" == t ? this._reader.abort() : "uploading" == t && this._adapter.abort && this._adapter.abort() : this._filePromiseWrapper.rejecter("aborted"), this._destroy(); };
            oh.prototype._destroy = function () { this._filePromiseWrapper = void 0, this._reader = void 0, this._adapter = void 0, this.data = void 0, this.uploadResponse = void 0; };
            oh.prototype._createFilePromiseWrapper = function (t) { var e = {}; return e.promise = new Promise(function (n, i) { e.resolver = n, e.rejecter = i, e.isFulfilled = !1, t.then(function (t) { e.isFulfilled = !0, n(t); }).catch(function (t) { e.isFulfilled = !0, i(t); }); }), e; };
            return oh;
        }());
        ci(oh, Fi);
        var rh = "ckCsrfToken", sh = 40, ah = "abcdefghijklmnopqrstuvwxyz0123456789";
        function ch() { var t = function (t) { t = t.toLowerCase(); var e = document.cookie.split(";"); for (var _j = 0, e_219 = e; _j < e_219.length; _j++) {
            var n_223 = e_219[_j];
            var e_220 = n_223.split("="), i_135 = decodeURIComponent(e_220[0].trim().toLowerCase());
            if (i_135 === t)
                return decodeURIComponent(e_220[1]);
        } return null; }(rh); return t && t.length == sh || (t = function (t) { var e = ""; var n = new Uint8Array(t); window.crypto.getRandomValues(n); for (var t_277 = 0; t_277 < n.length; t_277++) {
            var i_136 = ah.charAt(n[t_277] % ah.length);
            e += Math.random() > .5 ? i_136.toUpperCase() : i_136;
        } return e; }(sh), function (t, e) { document.cookie = encodeURIComponent(t) + "=" + encodeURIComponent(e) + ";path=/"; }(rh, t)), t; }
        var lh = /** @class */ (function (_super) {
            __extends(lh, _super);
            function lh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(lh, "requires", {
                get: function () { return [ih]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(lh, "pluginName", {
                get: function () { return "CKFinderUploadAdapter"; },
                enumerable: true,
                configurable: true
            });
            lh.prototype.init = function () {
                var _this = this;
                var t = this.editor.config.get("ckfinder.uploadUrl");
                t && (this.editor.plugins.get(ih).createUploadAdapter = (function (e) { return new dh(e, t, _this.editor.t); }));
            };
            return lh;
        }(Bl));
        var dh = /** @class */ (function () {
            function dh(t, e, n) {
                this.loader = t, this.url = e, this.t = n;
            }
            dh.prototype.upload = function () {
                var _this = this;
                return this.loader.file.then(function (t) { return new Promise(function (e, n) { _this._initRequest(), _this._initListeners(e, n, t), _this._sendRequest(t); }); });
            };
            dh.prototype.abort = function () { this.xhr && this.xhr.abort(); };
            dh.prototype._initRequest = function () { var t = this.xhr = new XMLHttpRequest; t.open("POST", this.url, !0), t.responseType = "json"; };
            dh.prototype._initListeners = function (t, e, n) { var i = this.xhr, o = this.loader, r = (0, this.t)("a") + (" " + n.name + "."); i.addEventListener("error", function () { return e(r); }), i.addEventListener("abort", function () { return e(); }), i.addEventListener("load", function () { var n = i.response; if (!n || !n.uploaded)
                return e(n && n.error && n.error.message ? n.error.message : r); t({ default: n.url }); }), i.upload && i.upload.addEventListener("progress", function (t) { t.lengthComputable && (o.uploadTotal = t.total, o.uploaded = t.loaded); }); };
            dh.prototype._sendRequest = function (t) { var e = new FormData; e.append("upload", t), e.append("ckCsrfToken", ch()), this.xhr.send(e); };
            return dh;
        }());
        var uh = /** @class */ (function () {
            function uh(t, e, n) {
                var i, o = null;
                "function" == typeof n ? i = n : (o = t.commands.get(n), i = (function () { t.execute(n); })), t.model.document.on("change", function (n, r) { if (o && !o.isEnabled)
                    return; if ("transparent" == r.type)
                    return; var s = Array.from(t.model.document.differ.getChanges()), a = s[0]; if (1 != s.length || "insert" !== a.type || "$text" != a.name || 1 != a.length)
                    return; var c = a.position.textNode || a.position.nodeAfter; if (!c.parent.is("paragraph"))
                    return; var l = e.exec(c.data); l && t.model.enqueueChange(function (t) { var e = t.createPositionAt(c.parent, 0), n = t.createPositionAt(c.parent, l[0].length), o = new oa(e, n); !1 !== i({ match: l }) && t.remove(o), o.detach(); }); });
            }
            return uh;
        }());
        var hh = /** @class */ (function () {
            function hh(t, e, n) {
                var i, o, r, s;
                e instanceof RegExp ? i = e : r = e, "string" == typeof n ? o = n : s = n, r = r || (function (t) { var e; var n = [], o = []; for (; null !== (e = i.exec(t)) && !(e && e.length < 4);) {
                    var t_278 = e.index, i_137 = e[1], r_38 = e[2], s_27 = e[3];
                    var a_12 = i_137 + r_38 + s_27, c_9 = [t_278 += e[0].length - a_12.length, t_278 + i_137.length], l_3 = [t_278 + i_137.length + r_38.length, t_278 + i_137.length + r_38.length + s_27.length];
                    n.push(c_9), n.push(l_3), o.push([t_278 + i_137.length, t_278 + i_137.length + r_38.length]);
                } return { remove: n, format: o }; }), s = s || (function (e, n) { var i = t.model.schema.getValidRanges(n, o); for (var _j = 0, i_138 = i; _j < i_138.length; _j++) {
                    var t_279 = i_138[_j];
                    e.setAttribute(o, !0, t_279);
                } e.removeSelectionAttribute(o); }), t.model.document.on("change", function (e, n) { if ("transparent" == n.type)
                    return; var i = t.model.document.selection; if (!i.isCollapsed)
                    return; var o = Array.from(t.model.document.differ.getChanges()), a = o[0]; if (1 != o.length || "insert" !== a.type || "$text" != a.name || 1 != a.length)
                    return; var c = i.focus.parent, l = function (t) { return Array.from(t.getChildren()).reduce(function (t, e) { return t + e.data; }, ""); }(c).slice(0, i.focus.offset), d = r(l), u = fh(c, d.format, t.model), h = fh(c, d.remove, t.model); u.length && h.length && t.model.enqueueChange(function (t) { if (!1 !== s(t, u))
                    for (var _j = 0, _k = h.reverse(); _j < _k.length; _j++) {
                        var e_221 = _k[_j];
                        t.remove(e_221);
                    } }); });
            }
            return hh;
        }());
        function fh(t, e, n) { return e.filter(function (t) { return void 0 !== t[0] && void 0 !== t[1]; }).map(function (e) { return n.createRange(n.createPositionAt(t, e[0]), n.createPositionAt(t, e[1])); }); }
        function mh(t, e) { return function (n, i) { if (!t.commands.get(e).isEnabled)
            return !1; var o = t.model.schema.getValidRanges(i, e); for (var _j = 0, o_84 = o; _j < o_84.length; _j++) {
            var t_280 = o_84[_j];
            n.setAttribute(e, !0, t_280);
        } n.removeSelectionAttribute(e); }; }
        var gh = /** @class */ (function (_super) {
            __extends(gh, _super);
            function gh(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.attributeKey = e;
                return _this;
            }
            gh.prototype.refresh = function () { var t = this.editor.model, e = t.document; this.value = this._getValueFromFirstAllowedNode(), this.isEnabled = t.schema.checkAttributeInSelection(e.selection, this.attributeKey); };
            gh.prototype.execute = function (t) {
                var _this = this;
                if (t === void 0) { t = {}; }
                var e = this.editor.model, n = e.document.selection, i = void 0 === t.forceValue ? !this.value : t.forceValue;
                e.change(function (t) { if (n.isCollapsed)
                    i ? t.setSelectionAttribute(_this.attributeKey, !0) : t.removeSelectionAttribute(_this.attributeKey);
                else {
                    var o_86 = e.schema.getValidRanges(n.getRanges(), _this.attributeKey);
                    for (var _j = 0, o_85 = o_86; _j < o_85.length; _j++) {
                        var e_222 = o_85[_j];
                        i ? t.setAttribute(_this.attributeKey, i, e_222) : t.removeAttribute(_this.attributeKey, e_222);
                    }
                } });
            };
            gh.prototype._getValueFromFirstAllowedNode = function () { var t = this.editor.model, e = t.schema, n = t.document.selection; if (n.isCollapsed)
                return n.hasAttribute(this.attributeKey); for (var _j = 0, _k = n.getRanges(); _j < _k.length; _j++) {
                var t_281 = _k[_j];
                for (var _q = 0, _v = t_281.getItems(); _q < _v.length; _q++) {
                    var n_224 = _v[_q];
                    if (e.checkAttribute(n_224, this.attributeKey))
                        return n_224.hasAttribute(this.attributeKey);
                }
            } return !1; };
            return gh;
        }(Wl));
        var ph = "bold";
        var bh = /** @class */ (function (_super) {
            __extends(bh, _super);
            function bh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            bh.prototype.init = function () { var t = this.editor; t.model.schema.extend("$text", { allowAttributes: ph }), t.conversion.attributeToElement({ model: ph, view: "strong", upcastAlso: ["b", { styles: { "font-weight": "bold" } }] }), t.commands.add(ph, new gh(t, ph)), t.keystrokes.set("CTRL+B", ph); };
            return bh;
        }(Bl));
        var wh = n(32), _h = n.n(wh);
        var kh = "bold";
        var vh = /** @class */ (function (_super) {
            __extends(vh, _super);
            function vh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            vh.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.t;
                t.ui.componentFactory.add(kh, function (n) { var i = t.commands.get(kh), o = new jd(n); return o.set({ label: e("aa"), icon: _h.a, keystroke: "CTRL+B", tooltip: !0 }), o.bind("isOn", "isEnabled").to(i, "value", "isEnabled"), _this.listenTo(o, "execute", function () { return t.execute(kh); }), o; });
            };
            return vh;
        }(Bl));
        var yh = "italic";
        var xh = /** @class */ (function (_super) {
            __extends(xh, _super);
            function xh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            xh.prototype.init = function () { var t = this.editor; t.model.schema.extend("$text", { allowAttributes: yh }), t.conversion.attributeToElement({ model: yh, view: "i", upcastAlso: ["em", { styles: { "font-style": "italic" } }] }), t.commands.add(yh, new gh(t, yh)), t.keystrokes.set("CTRL+I", yh); };
            return xh;
        }(Bl));
        var Ah = n(33), Ch = n.n(Ah);
        var Th = "italic";
        var Ph = /** @class */ (function (_super) {
            __extends(Ph, _super);
            function Ph() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Ph.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.t;
                t.ui.componentFactory.add(Th, function (n) { var i = t.commands.get(Th), o = new jd(n); return o.set({ label: e("v"), icon: Ch.a, keystroke: "CTRL+I", tooltip: !0 }), o.bind("isOn", "isEnabled").to(i, "value", "isEnabled"), _this.listenTo(o, "execute", function () { return t.execute(Th); }), o; });
            };
            return Ph;
        }(Bl));
        var Mh = "strikethrough";
        var Eh = /** @class */ (function (_super) {
            __extends(Eh, _super);
            function Eh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Eh.prototype.init = function () { var t = this.editor; t.model.schema.extend("$text", { allowAttributes: Mh }), t.conversion.attributeToElement({ model: Mh, view: "s", upcastAlso: ["del", "strike", { styles: { "text-decoration": "line-through" } }] }), t.commands.add(Mh, new gh(t, Mh)), t.keystrokes.set("CTRL+SHIFT+X", "strikethrough"); };
            return Eh;
        }(Bl));
        var Sh = n(34), Ih = n.n(Sh);
        var Nh = "strikethrough";
        var Oh = /** @class */ (function (_super) {
            __extends(Oh, _super);
            function Oh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Oh.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.t;
                t.ui.componentFactory.add(Nh, function (n) { var i = t.commands.get(Nh), o = new jd(n); return o.set({ label: e("ax"), icon: Ih.a, keystroke: "CTRL+SHIFT+X", tooltip: !0 }), o.bind("isOn", "isEnabled").to(i, "value", "isEnabled"), _this.listenTo(o, "execute", function () { return t.execute(Nh); }), o; });
            };
            return Oh;
        }(Bl));
        var Rh = "underline";
        var Dh = /** @class */ (function (_super) {
            __extends(Dh, _super);
            function Dh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Dh.prototype.init = function () { var t = this.editor; t.model.schema.extend("$text", { allowAttributes: Rh }), t.conversion.attributeToElement({ model: Rh, view: "u", upcastAlso: { styles: { "text-decoration": "underline" } } }), t.commands.add(Rh, new gh(t, Rh)), t.keystrokes.set("CTRL+U", "underline"); };
            return Dh;
        }(Bl));
        var Lh = n(35), jh = n.n(Lh);
        var Vh = "underline";
        var zh = /** @class */ (function (_super) {
            __extends(zh, _super);
            function zh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            zh.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.t;
                t.ui.componentFactory.add(Vh, function (n) { var i = t.commands.get(Vh), o = new jd(n); return o.set({ label: e("ab"), icon: jh.a, keystroke: "CTRL+U", tooltip: !0 }), o.bind("isOn", "isEnabled").to(i, "value", "isEnabled"), _this.listenTo(o, "execute", function () { return t.execute(Vh); }), o; });
            };
            return zh;
        }(Bl));
        var Bh = /** @class */ (function (_super) {
            __extends(Bh, _super);
            function Bh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Bh.prototype.refresh = function () { this.value = this._getValue(), this.isEnabled = this._checkEnabled(); };
            Bh.prototype.execute = function () {
                var _this = this;
                var t = this.editor.model, e = t.schema, n = t.document.selection, i = Array.from(n.getTopMostBlocks());
                t.change(function (t) { if (_this.value)
                    _this._removeQuote(t, i.filter(Fh));
                else {
                    var n_225 = i.filter(function (t) { return Fh(t) || Hh(e, t); });
                    _this._applyQuote(t, n_225);
                } });
            };
            Bh.prototype._getValue = function () { var t = qd(this.editor.model.document.selection.getTopMostBlocks()); return !(!t || !Fh(t)); };
            Bh.prototype._checkEnabled = function () { if (this.value)
                return !0; var t = this.editor.model.document.selection, e = this.editor.model.schema, n = qd(t.getSelectedBlocks()); return !!n && Hh(e, n); };
            Bh.prototype._removeQuote = function (t, e) { Uh(t, e).reverse().forEach(function (e) { if (e.start.isAtStart && e.end.isAtEnd)
                return void t.unwrap(e.start.parent); if (e.start.isAtStart) {
                var n_226 = t.createPositionBefore(e.start.parent);
                return void t.move(e, n_226);
            } e.end.isAtEnd || t.split(e.end); var n = t.createPositionAfter(e.end.parent); t.move(e, n); }); };
            Bh.prototype._applyQuote = function (t, e) { var n = []; Uh(t, e).reverse().forEach(function (e) { var i = Fh(e.start); i || (i = t.createElement("blockQuote"), t.wrap(e, i)), n.push(i); }), n.reverse().reduce(function (e, n) { return e.nextSibling == n ? (t.merge(t.createPositionAfter(e)), e) : n; }); };
            return Bh;
        }(Wl));
        function Fh(t) { return "blockQuote" == t.parent.name ? t.parent : null; }
        function Uh(t, e) { var n, i = 0; var o = []; for (; i < e.length;) {
            var r_39 = e[i], s_28 = e[i + 1];
            n || (n = t.createPositionBefore(r_39)), s_28 && r_39.nextSibling == s_28 || (o.push(t.createRange(n, t.createPositionAfter(r_39))), n = null), i++;
        } return o; }
        function Hh(t, e) { var n = t.checkChild(e.parent, "blockQuote"), i = t.checkChild(["$root", "blockQuote"], e); return n && i; }
        var qh = /** @class */ (function (_super) {
            __extends(qh, _super);
            function qh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            qh.prototype.init = function () { var t = this.editor, e = t.model.schema; t.commands.add("blockQuote", new Bh(t)), e.register("blockQuote", { allowWhere: "$block", allowContentOf: "$root" }), e.addChildCheck(function (t, e) { if (t.endsWith("blockQuote") && "blockQuote" == e.name)
                return !1; }), t.conversion.elementToElement({ model: "blockQuote", view: "blockquote" }), t.model.document.registerPostFixer(function (n) { var i = t.model.document.differ.getChanges(); for (var _j = 0, i_139 = i; _j < i_139.length; _j++) {
                var t_282 = i_139[_j];
                if ("insert" == t_282.type) {
                    var i_140 = t_282.position.nodeAfter;
                    if (!i_140)
                        continue;
                    if (i_140.is("blockQuote") && i_140.isEmpty)
                        return n.remove(i_140), !0;
                    if (i_140.is("blockQuote") && !e.checkChild(t_282.position, i_140))
                        return n.unwrap(i_140), !0;
                    if (i_140.is("element")) {
                        var t_283 = n.createRangeIn(i_140);
                        for (var _k = 0, _q = t_283.getItems(); _k < _q.length; _k++) {
                            var i_141 = _q[_k];
                            if (i_141.is("blockQuote") && !e.checkChild(n.createPositionBefore(i_141), i_141))
                                return n.unwrap(i_141), !0;
                        }
                    }
                }
                else if ("remove" == t_282.type) {
                    var e_223 = t_282.position.parent;
                    if (e_223.is("blockQuote") && e_223.isEmpty)
                        return n.remove(e_223), !0;
                }
            } return !1; }); };
            qh.prototype.afterInit = function () {
                var _this = this;
                var t = this.editor.commands.get("blockQuote");
                this.listenTo(this.editor.editing.view.document, "enter", function (e, n) { var i = _this.editor.model.document, o = i.selection.getLastPosition().parent; i.selection.isCollapsed && o.isEmpty && t.value && (_this.editor.execute("blockQuote"), _this.editor.editing.view.scrollToTheSelection(), n.preventDefault(), e.stop()); });
            };
            return qh;
        }(Bl));
        var Wh = n(36), Yh = n.n(Wh);
        n(85);
        var $h = /** @class */ (function (_super) {
            __extends($h, _super);
            function $h() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            $h.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.t;
                t.ui.componentFactory.add("blockQuote", function (n) { var i = t.commands.get("blockQuote"), o = new jd(n); return o.set({ label: e("w"), icon: Yh.a, tooltip: !0 }), o.bind("isOn", "isEnabled").to(i, "value", "isEnabled"), _this.listenTo(o, "execute", function () { return t.execute("blockQuote"); }), o; });
            };
            return $h;
        }(Bl));
        var Gh = n(37), Qh = n.n(Gh);
        var Kh = /** @class */ (function (_super) {
            __extends(Kh, _super);
            function Kh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Kh, "pluginName", {
                get: function () { return "CKFinderUI"; },
                enumerable: true,
                configurable: true
            });
            Kh.prototype.init = function () { var t = this.editor, e = t.ui.componentFactory, n = t.t; e.add("ckfinder", function (e) { var i = t.commands.get("ckfinder"), o = new jd(e); return o.set({ label: n("x"), icon: Qh.a, tooltip: !0 }), o.bind("isEnabled").to(i), o.on("execute", function () { t.execute("ckfinder"), t.editing.view.focus(); }), o; }); };
            return Kh;
        }(Bl));
        var Jh = /** @class */ (function (_super) {
            __extends(Jh, _super);
            function Jh() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Jh, "pluginName", {
                get: function () { return "Notification"; },
                enumerable: true,
                configurable: true
            });
            Jh.prototype.init = function () { this.on("show:warning", function (t, e) { window.alert(e.message); }, { priority: "lowest" }); };
            Jh.prototype.showSuccess = function (t, e) {
                if (e === void 0) { e = {}; }
                this._showNotification({ message: t, type: "success", namespace: e.namespace, title: e.title });
            };
            Jh.prototype.showInfo = function (t, e) {
                if (e === void 0) { e = {}; }
                this._showNotification({ message: t, type: "info", namespace: e.namespace, title: e.title });
            };
            Jh.prototype.showWarning = function (t, e) {
                if (e === void 0) { e = {}; }
                this._showNotification({ message: t, type: "warning", namespace: e.namespace, title: e.title });
            };
            Jh.prototype._showNotification = function (t) { var e = "show:" + t.type + (t.namespace ? ":" + t.namespace : ""); this.fire(e, { message: t.message, type: t.type, title: t.title || "" }); };
            return Jh;
        }(Bl));
        var Zh = /** @class */ (function (_super) {
            __extends(Zh, _super);
            function Zh(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.stopListening(_this.editor.model.document, "change"), _this.listenTo(_this.editor.model.document, "change", function () { return _this.refresh(); }, { priority: "low" });
                return _this;
            }
            Zh.prototype.refresh = function () { var t = this.editor.commands.get("imageUpload"), e = this.editor.commands.get("link"); this.isEnabled = t && e && (t.isEnabled || e.isEnabled); };
            Zh.prototype.execute = function () { var t = this.editor, e = this.editor.config.get("ckfinder.openerMethod") || "modal"; if ("popup" != e && "modal" != e)
                throw new Gn.b('ckfinder-unknown-openerMethod: The openerMethod config option must by "popup" or "modal".'); var n = this.editor.config.get("ckfinder.options") || {}; n.chooseFiles = !0; var i = n.onInit; n.language || (n.language = t.locale.language), n.onInit = (function (e) { i && i(), e.on("files:choose", function (n) { var i = n.data.files.toArray(), o = i.filter(function (t) { return !t.isImage(); }), r = i.filter(function (t) { return t.isImage(); }); for (var _j = 0, o_87 = o; _j < o_87.length; _j++) {
                var e_224 = o_87[_j];
                t.execute("link", e_224.getUrl());
            } var s = []; for (var _k = 0, r_40 = r; _k < r_40.length; _k++) {
                var t_284 = r_40[_k];
                var n_227 = t_284.getUrl();
                s.push(n_227 || e.request("file:getProxyUrl", { file: t_284 }));
            } s.length && Xh(t, s); }), e.on("file:choose:resizedImage", function (e) { var n = e.data.resizedUrl; if (n)
                Xh(t, [n]);
            else {
                var e_225 = t.plugins.get("Notification"), n_228 = t.locale.t;
                e_225.showWarning(n_228("bi"), { title: n_228("bj"), namespace: "ckfinder" });
            } }); }), window.CKFinder[e](n); };
            return Zh;
        }(Wl));
        function Xh(t, e) { if (t.commands.get("imageUpload").isEnabled)
            t.execute("imageInsert", { source: e });
        else {
            var e_226 = t.plugins.get("Notification"), n_229 = t.locale.t;
            e_226.showWarning(n_229("bk"), { title: n_229("bl"), namespace: "ckfinder" });
        } }
        var tf = /** @class */ (function (_super) {
            __extends(tf, _super);
            function tf() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(tf, "pluginName", {
                get: function () { return "CKFinderEditing"; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(tf, "requires", {
                get: function () { return [Jh]; },
                enumerable: true,
                configurable: true
            });
            tf.prototype.init = function () { var t = this.editor; t.commands.add("ckfinder", new Zh(t)); };
            return tf;
        }(Bl));
        var ef = /^data:(\S*?);base64,/;
        var nf = /** @class */ (function () {
            function nf(t, e, n) {
                if (!t)
                    throw new Error("File must be provided");
                if (!e)
                    throw new Error("Token must be provided");
                if (!n)
                    throw new Error("Api address must be provided");
                this.file = function (t) { if ("string" != typeof t)
                    return !1; var e = t.match(ef); return !(!e || !e.length); }(t) ? function (t, e) {
                    if (e === void 0) { e = 512; }
                    try {
                        var n_230 = t.match(ef)[1], i_142 = atob(t.replace(ef, "")), o_88 = [];
                        for (var t_285 = 0; t_285 < i_142.length; t_285 += e) {
                            var n_231 = i_142.slice(t_285, t_285 + e), r_41 = new Array(n_231.length);
                            for (var t_286 = 0; t_286 < n_231.length; t_286++)
                                r_41[t_286] = n_231.charCodeAt(t_286);
                            o_88.push(new Uint8Array(r_41));
                        }
                        return new Blob(o_88, { type: n_230 });
                    }
                    catch (t) {
                        throw new Error("Problem with decoding Base64 image data.");
                    }
                }(t) : t, this._token = e, this._apiAddress = n;
            }
            nf.prototype.onProgress = function (t) { return this.on("progress", function (e, n) { return t(n); }), this; };
            nf.prototype.onError = function (t) { return this.once("error", function (e, n) { return t(n); }), this; };
            nf.prototype.abort = function () { this.xhr.abort(); };
            nf.prototype.send = function () { return this._prepareRequest(), this._attachXHRListeners(), this._sendRequest(); };
            nf.prototype._prepareRequest = function () { var t = new XMLHttpRequest; t.open("POST", this._apiAddress), t.setRequestHeader("Authorization", this._token.value), t.responseType = "json", this.xhr = t; };
            nf.prototype._attachXHRListeners = function () {
                var _this = this;
                var t = this, e = this.xhr;
                function n(e) { return function () { return t.fire("error", e); }; }
                e.addEventListener("error", n("Network Error")), e.addEventListener("abort", n("Abort")), e.upload && e.upload.addEventListener("progress", function (t) { t.lengthComputable && _this.fire("progress", { total: t.total, uploaded: t.loaded }); }), e.addEventListener("load", function () { var t = e.status, n = e.response; if (t < 200 || t > 299)
                    return _this.fire("error", n.message || n.error); });
            };
            nf.prototype._sendRequest = function () { var t = new FormData, e = this.xhr; return t.append("file", this.file), new Promise(function (n, i) { e.addEventListener("load", function () { var t = e.status, o = e.response; return t < 200 || t > 299 ? o.message ? i(new Error(o.message)) : i(o.error) : n(o); }), e.addEventListener("error", function () { return i(new Error("Network Error")); }), e.addEventListener("abort", function () { return i(new Error("Abort")); }), e.send(t); }); };
            return nf;
        }());
        ci(nf, ei);
        var of = nf;
        var rf = { refreshInterval: 36e5, autoRefresh: !0 };
        var sf = /** @class */ (function () {
            function sf(t, e) {
                if (e === void 0) { e = rf; }
                if (!t)
                    throw new Error("A `tokenUrl` must be provided as the first constructor argument.");
                this.set("value", e.initValue), this._refresh = "function" == typeof t ? t : function () { return (function (t) { return new Promise(function (e, n) { var i = new XMLHttpRequest; i.open("GET", t), i.addEventListener("load", function () { var t = i.status, o = i.response; return t < 200 || t > 299 ? n(new Error("Cannot download new token!")) : e(o); }), i.addEventListener("error", function () { return n(new Error("Network Error")); }), i.addEventListener("abort", function () { return n(new Error("Abort")); }), i.send(); }); })(t); }, this._options = Object.assign({}, rf, e);
            }
            sf.prototype.init = function () {
                var _this = this;
                return new Promise(function (t, e) { _this._options.autoRefresh && _this._startRefreshing(), _this.value ? t(_this) : _this._refreshToken().then(t).catch(e); });
            };
            sf.prototype._refreshToken = function () {
                var _this = this;
                return this._refresh().then(function (t) { return _this.set("value", t); }).then(function () { return _this; });
            };
            sf.prototype.destroy = function () { this._stopRefreshing(); };
            sf.prototype._startRefreshing = function () {
                var _this = this;
                this._refreshInterval = setInterval(function () { return _this._refreshToken(); }, this._options.refreshInterval);
            };
            sf.prototype._stopRefreshing = function () { clearInterval(this._refreshInterval); };
            sf.create = function (t, e) {
                if (e === void 0) { e = rf; }
                return new sf(t, e).init();
            };
            return sf;
        }());
        ci(sf, Fi);
        var af = sf;
        var cf = /** @class */ (function (_super) {
            __extends(cf, _super);
            function cf() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(cf, "pluginName", {
                get: function () { return "CloudServices"; },
                enumerable: true,
                configurable: true
            });
            cf.prototype.init = function () { var t = this.editor.config.get("cloudServices") || {}; for (var e_227 in t)
                this[e_227] = t[e_227]; if (this.tokenUrl)
                return this.token = new cf.Token(this.tokenUrl), this.token.init(); this.token = null; };
            return cf;
        }(Bl));
        cf.Token = af;
        var lf = /** @class */ (function (_super) {
            __extends(lf, _super);
            function lf() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(lf, "requires", {
                get: function () { return [ih, cf]; },
                enumerable: true,
                configurable: true
            });
            lf.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.plugins.get(cf), n = e.token, i = e.uploadUrl;
                n && (this._uploadGateway = new lf._UploadGateway(n, i), t.plugins.get(ih).createUploadAdapter = (function (t) { return new df(_this._uploadGateway, t); }));
            };
            return lf;
        }(Bl));
        var df = /** @class */ (function () {
            function df(t, e) {
                this.uploadGateway = t, this.loader = e;
            }
            df.prototype.upload = function () {
                var _this = this;
                return this.loader.file.then(function (t) { return (_this.fileUploader = _this.uploadGateway.upload(t), _this.fileUploader.on("progress", function (t, e) { _this.loader.uploadTotal = e.total, _this.loader.uploaded = e.uploaded; }), _this.fileUploader.send()); });
            };
            df.prototype.abort = function () { this.fileUploader.abort(); };
            return df;
        }());
        lf._UploadGateway = /** @class */ (function () {
            function _UploadGateway(t, e) {
                if (!t)
                    throw new Error("Token must be provided");
                if (!e)
                    throw new Error("Api address must be provided");
                this._token = t, this._apiAddress = e;
            }
            _UploadGateway.prototype.upload = function (t) { return new of(t, this._token, this._apiAddress); };
            return _UploadGateway;
        }());
        var uf = /** @class */ (function (_super) {
            __extends(uf, _super);
            function uf(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this._observedElements = new Set;
                return _this;
            }
            uf.prototype.observe = function (t, e) {
                var _this = this;
                this.document.getRoot(e).on("change:children", function (e, n) { _this.view.once("render", function () { return _this._updateObservedElements(t, n); }); });
            };
            uf.prototype._updateObservedElements = function (t, e) {
                var _this = this;
                if (!e.is("element") || e.is("attributeElement"))
                    return;
                var n = this.view.domConverter.mapViewToDom(e);
                if (n) {
                    for (var _j = 0, _k = n.querySelectorAll("img"); _j < _k.length; _j++) {
                        var t_287 = _k[_j];
                        this._observedElements.has(t_287) || (this.listenTo(t_287, "load", function (t, e) { return _this._fireEvents(e); }), this._observedElements.add(t_287));
                    }
                    for (var _q = 0, _v = this._observedElements; _q < _v.length; _q++) {
                        var e_228 = _v[_q];
                        t.contains(e_228) || (this.stopListening(e_228), this._observedElements.delete(e_228));
                    }
                }
            };
            uf.prototype._fireEvents = function (t) { this.isEnabled && (this.document.fire("layoutChanged"), this.document.fire("imageLoaded", t)); };
            uf.prototype.destroy = function () { this._observedElements.clear(), _super.prototype.destroy.call(this); };
            return uf;
        }(hr));
        function hf(t) { return function (n) { n.on("attribute:" + t + ":image", e); }; function e(t, e, n) { if (!n.consumable.consume(e.item, t.name))
            return; var i = n.writer, o = n.mapper.toViewElement(e.item).getChild(0); null !== e.attributeNewValue ? i.setAttribute(e.attributeKey, e.attributeNewValue, o) : i.removeAttribute(e.attributeKey, o); } }
        var ff = /** @class */ (function () {
            function ff() {
                this._stack = [];
            }
            ff.prototype.add = function (t, e) { var n = this._stack, i = n[0]; this._insertDescriptor(t); var o = n[0]; i === o || mf(i, o) || this.fire("change:top", { oldDescriptor: i, newDescriptor: o, writer: e }); };
            ff.prototype.remove = function (t, e) { var n = this._stack, i = n[0]; this._removeDescriptor(t); var o = n[0]; i === o || mf(i, o) || this.fire("change:top", { oldDescriptor: i, newDescriptor: o, writer: e }); };
            ff.prototype._insertDescriptor = function (t) { var e = this._stack, n = e.findIndex(function (e) { return e.id === t.id; }); if (mf(t, e[n]))
                return; n > -1 && e.splice(n, 1); var i = 0; for (; e[i] && gf(e[i], t);)
                i++; e.splice(i, 0, t); };
            ff.prototype._removeDescriptor = function (t) { var e = this._stack, n = e.findIndex(function (e) { return e.id === t; }); n > -1 && e.splice(n, 1); };
            return ff;
        }());
        function mf(t, e) { return t && e && t.priority == e.priority && pf(t.classes) == pf(e.classes); }
        function gf(t, e) { return t.priority > e.priority || !(t.priority < e.priority) && pf(t.classes) > pf(e.classes); }
        function pf(t) { return Array.isArray(t) ? t.sort().join(",") : t; }
        ci(ff, ei);
        var bf = n(38), wf = n.n(bf);
        var _f = "ck-widget", kf = "ck-widget_selected";
        function vf(t) { return !!t.is("element") && !!t.getCustomProperty("widget"); }
        function yf(t, e, n) {
            if (n === void 0) { n = {}; }
            return mo.isEdge || e.setAttribute("contenteditable", "false", t), e.addClass(_f, t), e.setCustomProperty("widget", !0, t), t.getFillerOffset = Tf, n.label && function (t, e, n) { n.setCustomProperty("widgetLabel", e, t); }(t, n.label, e), n.hasSelectionHandler && function (t, e) { var n = e.createUIElement("div", { class: "ck ck-widget__selection-handler" }, function (t) { var e = this.toDomElement(t), n = new Dd; return n.set("content", wf.a), n.render(), e.appendChild(n.element), e; }); e.insert(e.createPositionAt(t, 0), n), e.addClass(["ck-widget_with-selection-handler"], t); }(t, e), function (t, e, n, i) { var o = new ff; o.on("change:top", function (e, o) { o.oldDescriptor && i(t, o.oldDescriptor, o.writer), o.newDescriptor && n(t, o.newDescriptor, o.writer); }), e.setCustomProperty("addHighlight", function (t, e, n) { return o.add(e, n); }, t), e.setCustomProperty("removeHighlight", function (t, e, n) { return o.remove(e, n); }, t); }(t, e, function (t, e, n) { return n.addClass(i(e.classes), t); }, function (t, e, n) { return n.removeClass(i(e.classes), t); }), t;
            function i(t) { return Array.isArray(t) ? t : [t]; }
        }
        function xf(t) { var e = t.getCustomProperty("widgetLabel"); return e ? "function" == typeof e ? e() : e : ""; }
        function Af(t, e) { return e.addClass(["ck-editor__editable", "ck-editor__nested-editable"], t), mo.isEdge || (e.setAttribute("contenteditable", t.isReadOnly ? "false" : "true", t), t.on("change:isReadOnly", function (n, i, o) { e.setAttribute("contenteditable", o ? "false" : "true", t); })), t.on("change:isFocused", function (n, i, o) { o ? e.addClass("ck-editor__nested-editable_focused", t) : e.removeClass("ck-editor__nested-editable_focused", t); }), t; }
        function Cf(t, e) { var n = t.getSelectedElement(); if (n && e.schema.isBlock(n))
            return e.createPositionAfter(n); var i = t.getSelectedBlocks().next().value; if (i) {
            if (i.isEmpty)
                return e.createPositionAt(i, 0);
            var n_232 = e.createPositionAfter(i);
            return t.focus.isTouching(n_232) ? n_232 : e.createPositionBefore(i);
        } return t.focus; }
        function Tf() { return null; }
        function Pf(t) { var e = t.getSelectedElement(); return e && function (t) { return !!t.getCustomProperty("image") && vf(t); }(e) ? e : null; }
        function Mf(t) { return !!t && t.is("image"); }
        function Ef(t, e, n) {
            if (n === void 0) { n = {}; }
            var i = t.createElement("image", n), o = Cf(e.document.selection, e);
            e.insertContent(i, o), i.parent && t.setSelection(i, "on");
        }
        function Sf(t) { var e = t.schema, n = t.document.selection; return function (t, e, n) { var i = function (t, e) { var n = Cf(t, e).parent; if (n.isEmpty && !n.is("$root"))
            return n.parent; return n; }(t, n); return e.checkChild(i, "image"); }(n, e, t) && !function (t, e) { var n = t.getSelectedElement(); return n && e.isObject(n); }(n, e) && function (t) { return t.focus.getAncestors().slice().every(function (t) { return !t.is("image"); }); }(n); }
        var If = /** @class */ (function (_super) {
            __extends(If, _super);
            function If() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            If.prototype.refresh = function () { this.isEnabled = Sf(this.editor.model); };
            If.prototype.execute = function (t) { var e = this.editor.model; e.change(function (n) { var i = Array.isArray(t.source) ? t.source : [t.source]; for (var _j = 0, i_143 = i; _j < i_143.length; _j++) {
                var t_288 = i_143[_j];
                Ef(n, e, { src: t_288 });
            } }); };
            return If;
        }(Wl));
        var Nf = /** @class */ (function (_super) {
            __extends(Nf, _super);
            function Nf() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Nf.prototype.init = function () { var t = this.editor, e = t.model.schema, n = t.t, i = t.conversion; t.editing.view.addObserver(uf), e.register("image", { isObject: !0, isBlock: !0, allowWhere: "$block", allowAttributes: ["alt", "src", "srcset"] }), i.for("dataDowncast").elementToElement({ model: "image", view: function (t, e) { return Of(e); } }), i.for("editingDowncast").elementToElement({ model: "image", view: function (t, e) { return (function (t, e, n) { return e.setCustomProperty("image", !0, t), yf(t, e, { label: function () { var e = t.getChild(0).getAttribute("alt"); return e ? e + " " + n : n; } }); })(Of(e), e, n("ac")); } }), i.for("downcast").add(hf("src")).add(hf("alt")).add(function () { return function (e) { e.on("attribute:srcset:image", t); }; function t(t, e, n) { if (!n.consumable.consume(e.item, t.name))
                return; var i = n.writer, o = n.mapper.toViewElement(e.item).getChild(0); if (null === e.attributeNewValue) {
                var t_289 = e.attributeOldValue;
                t_289.data && (i.removeAttribute("srcset", o), i.removeAttribute("sizes", o), t_289.width && i.removeAttribute("width", o));
            }
            else {
                var t_290 = e.attributeNewValue;
                t_290.data && (i.setAttribute("srcset", t_290.data, o), i.setAttribute("sizes", "100vw", o), t_290.width && i.setAttribute("width", t_290.width, o));
            } } }()), i.for("upcast").elementToElement({ view: { name: "img", attributes: { src: !0 } }, model: function (t, e) { return e.createElement("image", { src: t.getAttribute("src") }); } }).attributeToAttribute({ view: { name: "img", key: "alt" }, model: "alt" }).attributeToAttribute({ view: { name: "img", key: "srcset" }, model: { key: "srcset", value: function (t) { var e = { data: t.getAttribute("srcset") }; return t.hasAttribute("width") && (e.width = t.getAttribute("width")), e; } } }).add(function () { return function (e) { e.on("element:figure", t); }; function t(t, e, n) { if (!n.consumable.test(e.viewItem, { name: !0, classes: "image" }))
                return; var i = Array.from(e.viewItem.getChildren()).find(function (t) { return t.is("img"); }); if (!i || !i.hasAttribute("src") || !n.consumable.test(i, { name: !0 }))
                return; var o = n.convertItem(i, e.modelCursor), r = qd(o.modelRange.getItems()); r && (n.convertChildren(e.viewItem, n.writer.createPositionAt(r, 0)), e.modelRange = o.modelRange, e.modelCursor = o.modelCursor); } }()), t.commands.add("imageInsert", new If(t)); };
            return Nf;
        }(Bl));
        function Of(t) { var e = t.createEmptyElement("img"), n = t.createContainerElement("figure", { class: "image" }); return t.insert(t.createPositionAt(n, 0), e), n; }
        var Rf = /** @class */ (function (_super) {
            __extends(Rf, _super);
            function Rf(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.domEventType = "mousedown";
                return _this;
            }
            Rf.prototype.onDomEvent = function (t) { this.fire(t.type, t); };
            return Rf;
        }(ts));
        n(87);
        var Df = _o("Ctrl+A");
        var Lf = /** @class */ (function (_super) {
            __extends(Lf, _super);
            function Lf() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Lf, "pluginName", {
                get: function () { return "Widget"; },
                enumerable: true,
                configurable: true
            });
            Lf.prototype.init = function () {
                var _this = this;
                var t = this.editor.editing.view, e = t.document;
                this._previouslySelected = new Set, this.editor.editing.downcastDispatcher.on("selection", function (t, e, n) { _this._clearPreviouslySelectedWidgets(n.writer); var i = n.writer, o = i.document.selection, r = o.getSelectedElement(); var s = null; for (var _j = 0, _k = o.getRanges(); _j < _k.length; _j++) {
                    var t_292 = _k[_j];
                    for (var _q = 0, t_291 = t_292; _q < t_291.length; _q++) {
                        var e_229 = t_291[_q];
                        var t_293 = e_229.item;
                        vf(t_293) && !jf(t_293, s) && (i.addClass(kf, t_293), _this._previouslySelected.add(t_293), s = t_293, t_293 == r && i.setSelection(o.getRanges(), { fake: !0, label: xf(r) }));
                    }
                } }, { priority: "low" }), t.addObserver(Rf), this.listenTo(e, "mousedown", function () {
                    var t = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        t[_j] = arguments[_j];
                    }
                    return _this._onMousedown.apply(_this, t);
                }), this.listenTo(e, "keydown", function () {
                    var t = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        t[_j] = arguments[_j];
                    }
                    return _this._onKeydown.apply(_this, t);
                }, { priority: "high" }), this.listenTo(e, "delete", function (t, e) { _this._handleDelete("forward" == e.direction) && (e.preventDefault(), t.stop()); }, { priority: "high" });
            };
            Lf.prototype._onMousedown = function (t, e) { var n = this.editor, i = n.editing.view, o = i.document; var r = e.target; if (function (t) { for (; t;) {
                if (t.is("editableElement") && !t.is("rootElement"))
                    return !0;
                if (vf(t))
                    return !1;
                t = t.parent;
            } return !1; }(r))
                return; if (!vf(r) && !(r = r.findAncestor(vf)))
                return; e.preventDefault(), o.isFocused || i.focus(); var s = n.editing.mapper.toModelElement(r); this._setSelectionOverElement(s); };
            Lf.prototype._onKeydown = function (t, e) { var n = e.keyCode, i = n == bo.arrowdown || n == bo.arrowright; var o = !1; !function (t) { return t == bo.arrowright || t == bo.arrowleft || t == bo.arrowup || t == bo.arrowdown; }(n) ? !function (t) { return wo(t) == Df; }(e) ? n === bo.enter && (o = this._handleEnterKey(e.shiftKey)) : o = this._selectAllNestedEditableContent() || this._selectAllContent() : o = this._handleArrowKeys(i), o && (e.preventDefault(), t.stop()); };
            Lf.prototype._handleDelete = function (t) {
                var _this = this;
                if (this.editor.isReadOnly)
                    return;
                var e = this.editor.model.document.selection;
                if (!e.isCollapsed)
                    return;
                var n = this._getObjectElementNextToSelection(t);
                return n ? (this.editor.model.change(function (t) { var i = e.anchor.parent; for (; i.isEmpty;) {
                    var e_230 = i;
                    i = e_230.parent, t.remove(e_230);
                } _this._setSelectionOverElement(n); }), !0) : void 0;
            };
            Lf.prototype._handleArrowKeys = function (t) { var e = this.editor.model, n = e.schema, i = e.document.selection, o = i.getSelectedElement(); if (o && n.isObject(o)) {
                var o_89 = t ? i.getLastPosition() : i.getFirstPosition(), r_42 = n.getNearestSelectionRange(o_89, t ? "forward" : "backward");
                return r_42 && e.change(function (t) { t.setSelection(r_42); }), !0;
            } if (!i.isCollapsed)
                return; var r = this._getObjectElementNextToSelection(t); return r && n.isObject(r) ? (this._setSelectionOverElement(r), !0) : void 0; };
            Lf.prototype._handleEnterKey = function (t) { var e = this.editor.model, n = e.document.selection.getSelectedElement(); if (function (t, e) { return t && e.isObject(t) && !e.isInline(t); }(n, e.schema))
                return e.change(function (i) { var o = i.createPositionAt(n, t ? "before" : "after"); var r = i.createElement("paragraph"); if (e.schema.isBlock(n.parent)) {
                    var t_294 = e.schema.findAllowedParent(o, r);
                    o = i.split(o, t_294).position;
                } i.insert(r, o), i.setSelection(r, "in"); }), !0; };
            Lf.prototype._selectAllNestedEditableContent = function () { var t = this.editor.model, e = t.document.selection, n = t.schema.getLimitElement(e); return e.getFirstRange().root != n && (t.change(function (t) { t.setSelection(t.createRangeIn(n)); }), !0); };
            Lf.prototype._selectAllContent = function () { var t = this.editor.model, e = this.editor.editing, n = e.view.document.selection.getSelectedElement(); if (n && vf(n)) {
                var i_144 = e.mapper.toModelElement(n.parent);
                return t.change(function (t) { t.setSelection(t.createRangeIn(i_144)); }), !0;
            } return !1; };
            Lf.prototype._setSelectionOverElement = function (t) { this.editor.model.change(function (e) { e.setSelection(e.createRangeOn(t)); }); };
            Lf.prototype._getObjectElementNextToSelection = function (t) { var e = this.editor.model, n = e.schema, i = e.document.selection, o = e.createSelection(i); e.modifySelection(o, { direction: t ? "forward" : "backward" }); var r = t ? o.focus.nodeBefore : o.focus.nodeAfter; return r && n.isObject(r) ? r : null; };
            Lf.prototype._clearPreviouslySelectedWidgets = function (t) { for (var _j = 0, _k = this._previouslySelected; _j < _k.length; _j++) {
                var e_231 = _k[_j];
                t.removeClass(kf, e_231);
            } this._previouslySelected.clear(); };
            return Lf;
        }(Bl));
        function jf(t, e) { return !!e && Array.from(t.getAncestors()).includes(e); }
        var Vf = /** @class */ (function (_super) {
            __extends(Vf, _super);
            function Vf() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Vf.prototype.refresh = function () { var t = this.editor.model.document.selection.getSelectedElement(); this.isEnabled = Mf(t), Mf(t) && t.hasAttribute("alt") ? this.value = t.getAttribute("alt") : this.value = !1; };
            Vf.prototype.execute = function (t) { var e = this.editor.model, n = e.document.selection.getSelectedElement(); e.change(function (e) { e.setAttribute("alt", t.newValue, n); }); };
            return Vf;
        }(Wl));
        var zf = /** @class */ (function (_super) {
            __extends(zf, _super);
            function zf() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            zf.prototype.init = function () { this.editor.commands.add("imageTextAlternative", new Vf(this.editor)); };
            return zf;
        }(Bl));
        n(89);
        var Bf = /** @class */ (function (_super) {
            __extends(Bf, _super);
            function Bf(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.set("text"), _this.set("for");
                var e = _this.bindTemplate;
                _this.setTemplate({ tag: "label", attributes: { class: ["ck", "ck-label"], for: e.to("for") }, children: [{ text: e.to("text") }] });
                return _this;
            }
            return Bf;
        }(Sl));
        n(91);
        var Ff = /** @class */ (function (_super) {
            __extends(Ff, _super);
            function Ff(t, e) {
                var _this = _super.call(this, t) || this;
                var n = "ck-input-" + Jn(), i = "ck-status-" + Jn();
                _this.set("label"), _this.set("value"), _this.set("isReadOnly", !1), _this.set("errorText", null), _this.set("infoText", null), _this.labelView = _this._createLabelView(n), _this.inputView = _this._createInputView(e, n, i), _this.statusView = _this._createStatusView(i), _this.bind("_statusText").to(_this, "errorText", _this, "infoText", function (t, e) { return t || e; });
                var o = _this.bindTemplate;
                _this.setTemplate({ tag: "div", attributes: { class: ["ck", "ck-labeled-input", o.if("isReadOnly", "ck-disabled")] }, children: [_this.labelView, _this.inputView, _this.statusView] });
                return _this;
            }
            Ff.prototype._createLabelView = function (t) { var e = new Bf(this.locale); return e.for = t, e.bind("text").to(this, "label"), e; };
            Ff.prototype._createInputView = function (t, e, n) {
                var _this = this;
                var i = new t(this.locale, n);
                return i.id = e, i.ariaDesribedById = n, i.bind("value").to(this), i.bind("isReadOnly").to(this), i.bind("hasError").to(this, "errorText", function (t) { return !!t; }), i.on("input", function () { _this.errorText = null; }), i;
            };
            Ff.prototype._createStatusView = function (t) { var e = new Sl(this.locale), n = this.bindTemplate; return e.setTemplate({ tag: "div", attributes: { class: ["ck", "ck-labeled-input__status", n.if("errorText", "ck-labeled-input__status_error"), n.if("_statusText", "ck-hidden", function (t) { return !t; })], id: t }, children: [{ text: n.to("_statusText") }] }), e; };
            Ff.prototype.select = function () { this.inputView.select(); };
            Ff.prototype.focus = function () { this.inputView.focus(); };
            return Ff;
        }(Sl));
        n(93);
        var Uf = /** @class */ (function (_super) {
            __extends(Uf, _super);
            function Uf(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.set("value"), _this.set("id"), _this.set("placeholder"), _this.set("isReadOnly", !1), _this.set("hasError", !1), _this.set("ariaDesribedById");
                var e = _this.bindTemplate;
                _this.setTemplate({ tag: "input", attributes: { type: "text", class: ["ck", "ck-input", "ck-input-text", e.if("hasError", "ck-error")], id: e.to("id"), placeholder: e.to("placeholder"), readonly: e.to("isReadOnly"), "aria-invalid": e.if("hasError", !0), "aria-describedby": e.to("ariaDesribedById") }, on: { input: e.to("input") } });
                return _this;
            }
            Uf.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this);
                var t = function (t) { _this.element.value = t || 0 === t ? t : ""; };
                t(this.value), this.on("change:value", function (e, n, i) { t(i); });
            };
            Uf.prototype.select = function () { this.element.select(); };
            Uf.prototype.focus = function () { this.element.focus(); };
            return Uf;
        }(Sl));
        function Hf(_j) {
            var t = _j.view;
            t.listenTo(t.element, "submit", function (e, n) { n.preventDefault(), t.fire("submit"); }, { useCapture: !0 });
        }
        var qf = n(7), Wf = n.n(qf), Yf = n(8), $f = n.n(Yf);
        n(95);
        var Gf = /** @class */ (function (_super) {
            __extends(Gf, _super);
            function Gf(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.locale.t;
                _this.focusTracker = new nl, _this.keystrokes = new Gc, _this.labeledInput = _this._createLabeledInputView(), _this.saveButtonView = _this._createButton(e("bn"), Wf.a, "ck-button-save"), _this.saveButtonView.type = "submit", _this.cancelButtonView = _this._createButton(e("bo"), $f.a, "ck-button-cancel", "cancel"), _this._focusables = new dl, _this._focusCycler = new Rl({ focusables: _this._focusables, focusTracker: _this.focusTracker, keystrokeHandler: _this.keystrokes, actions: { focusPrevious: "shift + tab", focusNext: "tab" } }), _this.setTemplate({ tag: "form", attributes: { class: ["ck", "ck-text-alternative-form"], tabindex: "-1" }, children: [_this.labeledInput, _this.saveButtonView, _this.cancelButtonView] });
                return _this;
            }
            Gf.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this), this.keystrokes.listenTo(this.element), Hf({ view: this }), [this.labeledInput, this.saveButtonView, this.cancelButtonView].forEach(function (t) { _this._focusables.add(t), _this.focusTracker.add(t.element); });
            };
            Gf.prototype._createButton = function (t, e, n, i) { var o = new jd(this.locale); return o.set({ label: t, icon: e, tooltip: !0 }), o.extendTemplate({ attributes: { class: n } }), i && o.delegate("execute").to(this, i), o; };
            Gf.prototype._createLabeledInputView = function () { var t = this.locale.t, e = new Ff(this.locale, Uf); return e.label = t("cf"), e.inputView.placeholder = t("cf"), e; };
            return Gf;
        }(Sl));
        n(97);
        var Qf = function (t) { return function (e) { return e + t; }; }("px"), Kf = nr.document.body;
        var Jf = /** @class */ (function (_super) {
            __extends(Jf, _super);
            function Jf(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.bindTemplate;
                _this.set("top", 0), _this.set("left", 0), _this.set("position", "arrow_nw"), _this.set("isVisible", !1), _this.set("withArrow", !0), _this.set("class"), _this.content = _this.createCollection(), _this.setTemplate({ tag: "div", attributes: { class: ["ck", "ck-balloon-panel", e.to("position", function (t) { return "ck-balloon-panel_" + t; }), e.if("isVisible", "ck-balloon-panel_visible"), e.if("withArrow", "ck-balloon-panel_with-arrow"), e.to("class")], style: { top: e.to("top", Qf), left: e.to("left", Qf) } }, children: _this.content });
                return _this;
            }
            Jf.prototype.show = function () { this.isVisible = !0; };
            Jf.prototype.hide = function () { this.isVisible = !1; };
            Jf.prototype.attachTo = function (t) { this.show(); var e = Jf.defaultPositions, n = Object.assign({}, { element: this.element, positions: [e.southArrowNorth, e.southArrowNorthWest, e.southArrowNorthEast, e.northArrowSouth, e.northArrowSouthWest, e.northArrowSouthEast], limiter: Kf, fitInViewport: !0 }, t), _j = Jf._getOptimalPosition(n), i = _j.top, o = _j.left, r = _j.name; Object.assign(this, { top: i, left: o, position: r }); };
            Jf.prototype.pin = function (t) {
                var _this = this;
                this.unpin(), this._pinWhenIsVisibleCallback = (function () { _this.isVisible ? _this._startPinning(t) : _this._stopPinning(); }), this._startPinning(t), this.listenTo(this, "change:isVisible", this._pinWhenIsVisibleCallback);
            };
            Jf.prototype.unpin = function () { this._pinWhenIsVisibleCallback && (this._stopPinning(), this.stopListening(this, "change:isVisible", this._pinWhenIsVisibleCallback), this._pinWhenIsVisibleCallback = null, this.hide()); };
            Jf.prototype._startPinning = function (t) {
                var _this = this;
                this.attachTo(t);
                var e = Zf(t.target), n = t.limiter ? Zf(t.limiter) : Kf;
                this.listenTo(nr.document, "scroll", function (i, o) { var r = o.target, s = e && r.contains(e), a = n && r.contains(n); !s && !a && e && n || _this.attachTo(t); }, { useCapture: !0 }), this.listenTo(nr.window, "resize", function () { _this.attachTo(t); });
            };
            Jf.prototype._stopPinning = function () { this.stopListening(nr.document, "scroll"), this.stopListening(nr.window, "resize"); };
            return Jf;
        }(Sl));
        function Zf(t) { return Wn(t) ? t : vs(t) ? t.commonAncestorContainer : "function" == typeof t ? Zf(t()) : null; }
        function Xf(t, e) { return t.top - e.height - Jf.arrowVerticalOffset; }
        function tm(t) { return t.bottom + Jf.arrowVerticalOffset; }
        Jf.arrowHorizontalOffset = 25, Jf.arrowVerticalOffset = 10, Jf._getOptimalPosition = Zd, Jf.defaultPositions = { northArrowSouth: function (t, e) { return ({ top: Xf(t, e), left: t.left + t.width / 2 - e.width / 2, name: "arrow_s" }); }, northArrowSouthEast: function (t, e) { return ({ top: Xf(t, e), left: t.left + t.width / 2 - e.width + Jf.arrowHorizontalOffset, name: "arrow_se" }); }, northArrowSouthWest: function (t, e) { return ({ top: Xf(t, e), left: t.left + t.width / 2 - Jf.arrowHorizontalOffset, name: "arrow_sw" }); }, northWestArrowSouth: function (t, e) { return ({ top: Xf(t, e), left: t.left - e.width / 2, name: "arrow_s" }); }, northWestArrowSouthWest: function (t, e) { return ({ top: Xf(t, e), left: t.left - Jf.arrowHorizontalOffset, name: "arrow_sw" }); }, northWestArrowSouthEast: function (t, e) { return ({ top: Xf(t, e), left: t.left - e.width + Jf.arrowHorizontalOffset, name: "arrow_se" }); }, northEastArrowSouth: function (t, e) { return ({ top: Xf(t, e), left: t.right - e.width / 2, name: "arrow_s" }); }, northEastArrowSouthEast: function (t, e) { return ({ top: Xf(t, e), left: t.right - e.width + Jf.arrowHorizontalOffset, name: "arrow_se" }); }, northEastArrowSouthWest: function (t, e) { return ({ top: Xf(t, e), left: t.right - Jf.arrowHorizontalOffset, name: "arrow_sw" }); }, southArrowNorth: function (t, e) { return ({ top: tm(t), left: t.left + t.width / 2 - e.width / 2, name: "arrow_n" }); }, southArrowNorthEast: function (t, e) { return ({ top: tm(t), left: t.left + t.width / 2 - e.width + Jf.arrowHorizontalOffset, name: "arrow_ne" }); }, southArrowNorthWest: function (t, e) { return ({ top: tm(t), left: t.left + t.width / 2 - Jf.arrowHorizontalOffset, name: "arrow_nw" }); }, southWestArrowNorth: function (t, e) { return ({ top: tm(t), left: t.left - e.width / 2, name: "arrow_n" }); }, southWestArrowNorthWest: function (t, e) { return ({ top: tm(t), left: t.left - Jf.arrowHorizontalOffset, name: "arrow_nw" }); }, southWestArrowNorthEast: function (t, e) { return ({ top: tm(t), left: t.left - e.width + Jf.arrowHorizontalOffset, name: "arrow_ne" }); }, southEastArrowNorth: function (t, e) { return ({ top: tm(t), left: t.right - e.width / 2, name: "arrow_n" }); }, southEastArrowNorthEast: function (t, e) { return ({ top: tm(t), left: t.right - e.width + Jf.arrowHorizontalOffset, name: "arrow_ne" }); }, southEastArrowNorthWest: function (t, e) { return ({ top: tm(t), left: t.right - Jf.arrowHorizontalOffset, name: "arrow_nw" }); } };
        var em = /** @class */ (function (_super) {
            __extends(em, _super);
            function em() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(em, "pluginName", {
                get: function () { return "ContextualBalloon"; },
                enumerable: true,
                configurable: true
            });
            em.prototype.init = function () {
                var _this = this;
                this.view = new Jf, this.positionLimiter = (function () { var t = _this.editor.editing.view, e = t.document.selection.editableElement; return e ? t.domConverter.mapViewToDom(e.root) : null; }), this._stack = new Map, this.editor.ui.view.body.add(this.view), this.editor.ui.focusTracker.add(this.view.element);
            };
            Object.defineProperty(em.prototype, "visibleView", {
                get: function () { var t = this._stack.get(this.view.content.get(0)); return t ? t.view : null; },
                enumerable: true,
                configurable: true
            });
            em.prototype.hasView = function (t) { return this._stack.has(t); };
            em.prototype.add = function (t) { if (this.hasView(t.view))
                throw new Gn.b("contextualballoon-add-view-exist: Cannot add configuration of the same view twice."); this.visibleView && this.view.content.remove(this.visibleView), this._stack.set(t.view, t), this._show(t); };
            em.prototype.remove = function (t) { if (!this.hasView(t))
                throw new Gn.b("contextualballoon-remove-view-not-exist: Cannot remove configuration of not existing view."); if (this.visibleView === t) {
                this.view.content.remove(t), this._stack.delete(t);
                var e_232 = Array.from(this._stack.values()).pop();
                e_232 ? this._show(e_232) : this.view.hide();
            }
            else
                this._stack.delete(t); };
            em.prototype.updatePosition = function (t) { t && (this._stack.get(this.visibleView).position = t), this.view.pin(this._getBalloonPosition()); };
            em.prototype._show = function (_j) {
                var t = _j.view, _k = _j.balloonClassName, e = _k === void 0 ? "" : _k;
                this.view.class = e, this.view.content.add(t), this.view.pin(this._getBalloonPosition());
            };
            em.prototype._getBalloonPosition = function () { var t = Array.from(this._stack.values()).pop().position; return t && !t.limiter && (t = Object.assign({}, t, { limiter: this.positionLimiter })), t; };
            return em;
        }(Bl));
        var nm = n(39), im = n.n(nm);
        function om(t) { var e = t.editing.view, n = Jf.defaultPositions; return { target: e.domConverter.viewToDom(e.document.selection.getSelectedElement()), positions: [n.northArrowSouth, n.northArrowSouthWest, n.northArrowSouthEast, n.southArrowNorth, n.southArrowNorthWest, n.southArrowNorthEast] }; }
        var rm = /** @class */ (function (_super) {
            __extends(rm, _super);
            function rm() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(rm, "requires", {
                get: function () { return [em]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(rm, "pluginName", {
                get: function () { return "ImageTextAlternativeUI"; },
                enumerable: true,
                configurable: true
            });
            rm.prototype.init = function () { this._createButton(), this._createForm(); };
            rm.prototype.destroy = function () { _super.prototype.destroy.call(this), this._form.destroy(); };
            rm.prototype._createButton = function () {
                var _this = this;
                var t = this.editor, e = t.t;
                t.ui.componentFactory.add("imageTextAlternative", function (n) { var i = t.commands.get("imageTextAlternative"), o = new jd(n); return o.set({ label: e("bx"), icon: im.a, tooltip: !0 }), o.bind("isEnabled").to(i, "isEnabled"), _this.listenTo(o, "execute", function () { return _this._showForm(); }), o; });
            };
            rm.prototype._createForm = function () {
                var _this = this;
                var t = this.editor, e = t.editing.view.document;
                this._balloon = this.editor.plugins.get("ContextualBalloon"), this._form = new Gf(t.locale), this._form.render(), this.listenTo(this._form, "submit", function () { t.execute("imageTextAlternative", { newValue: _this._form.labeledInput.inputView.element.value }), _this._hideForm(!0); }), this.listenTo(this._form, "cancel", function () { _this._hideForm(!0); }), this._form.keystrokes.set("Esc", function (t, e) { _this._hideForm(!0), e(); }), this.listenTo(t.ui, "update", function () { Pf(e.selection) ? _this._isVisible && function (t) { var e = t.plugins.get("ContextualBalloon"); if (Pf(t.editing.view.document.selection)) {
                    var n_233 = om(t);
                    e.updatePosition(n_233);
                } }(t) : _this._hideForm(!0); }), lu({ emitter: this._form, activator: function () { return _this._isVisible; }, contextElements: [this._form.element], callback: function () { return _this._hideForm(); } });
            };
            rm.prototype._showForm = function () { if (this._isVisible)
                return; var t = this.editor, e = t.commands.get("imageTextAlternative"), n = this._form.labeledInput; this._balloon.hasView(this._form) || this._balloon.add({ view: this._form, position: om(t) }), n.value = n.inputView.element.value = e.value || "", this._form.labeledInput.select(); };
            rm.prototype._hideForm = function (t) { this._isVisible && (this._form.saveButtonView.focus(), this._balloon.remove(this._form), t && this.editor.editing.view.focus()); };
            Object.defineProperty(rm.prototype, "_isVisible", {
                get: function () { return this._balloon.visibleView == this._form; },
                enumerable: true,
                configurable: true
            });
            return rm;
        }(Bl));
        var sm = /** @class */ (function (_super) {
            __extends(sm, _super);
            function sm() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(sm, "requires", {
                get: function () { return [zf, rm]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sm, "pluginName", {
                get: function () { return "ImageTextAlternative"; },
                enumerable: true,
                configurable: true
            });
            return sm;
        }(Bl));
        n(99);
        var am = /** @class */ (function (_super) {
            __extends(am, _super);
            function am() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(am, "requires", {
                get: function () { return [Nf, Lf, sm]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(am, "pluginName", {
                get: function () { return "Image"; },
                enumerable: true,
                configurable: true
            });
            return am;
        }(Bl));
        var cm = /** @class */ (function (_super) {
            __extends(cm, _super);
            function cm(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.buttonView = new jd(t), _this._fileInputView = new lm(t), _this._fileInputView.bind("acceptedType").to(_this), _this._fileInputView.bind("allowMultipleFiles").to(_this), _this._fileInputView.delegate("done").to(_this), _this.setTemplate({ tag: "span", attributes: { class: "ck-file-dialog-button" }, children: [_this.buttonView, _this._fileInputView] }), _this.buttonView.on("execute", function () { _this._fileInputView.open(); });
                return _this;
            }
            cm.prototype.focus = function () { this.buttonView.focus(); };
            return cm;
        }(Sl));
        var lm = /** @class */ (function (_super) {
            __extends(lm, _super);
            function lm(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.set("acceptedType"), _this.set("allowMultipleFiles", !1);
                var e = _this.bindTemplate;
                _this.setTemplate({ tag: "input", attributes: { class: ["ck-hidden"], type: "file", tabindex: "-1", accept: e.to("acceptedType"), multiple: e.to("allowMultipleFiles") }, on: { change: e.to(function () { _this.element && _this.element.files && _this.element.files.length && _this.fire("done", _this.element.files), _this.element.value = ""; }) } });
                return _this;
            }
            lm.prototype.open = function () { this.element.click(); };
            return lm;
        }(Sl));
        var dm = n(40), um = n.n(dm);
        function hm(t) { return /^image\/(jpeg|png|gif|bmp)$/.test(t.type); }
        function fm(t) { return new Promise(function (e, n) { var i = t.getAttribute("src"); fetch(i).then(function (t) { return t.blob(); }).then(function (t) { var o = function (t, e) { return t.type ? t.type : e.match(/data:(image\/\w+);base64/) ? e.match(/data:(image\/\w+);base64/)[1].toLowerCase() : "image/jpeg"; }(t, i), r = function (t, e, n) { try {
            return new File([t], e, { type: n });
        }
        catch (t) {
            return null;
        } }(t, "image." + o.replace("image/", ""), o); r ? e(r) : n(); }).catch(n); }); }
        var mm = /** @class */ (function (_super) {
            __extends(mm, _super);
            function mm() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            mm.prototype.init = function () { var t = this.editor, e = t.t; t.ui.componentFactory.add("imageUpload", function (n) { var i = new cm(n), o = t.commands.get("imageUpload"); return i.set({ acceptedType: "image/*", allowMultipleFiles: !0 }), i.buttonView.set({ label: e("be"), icon: um.a, tooltip: !0 }), i.buttonView.bind("isEnabled").to(o), i.on("done", function (e, n) { var i = Array.from(n).filter(hm); i.length && t.execute("imageUpload", { file: i }); }), i; }); };
            return mm;
        }(Bl));
        var gm = n(41), pm = n.n(gm);
        n(101), n(103), n(105);
        var bm = /** @class */ (function (_super) {
            __extends(bm, _super);
            function bm(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.placeholder = "data:image/svg+xml;utf8," + encodeURIComponent(pm.a);
                return _this;
            }
            bm.prototype.init = function () {
                var _this = this;
                this.editor.editing.downcastDispatcher.on("attribute:uploadStatus:image", function () {
                    var t = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        t[_j] = arguments[_j];
                    }
                    return _this.uploadStatusChange.apply(_this, t);
                });
            };
            bm.prototype.uploadStatusChange = function (t, e, n) { var i = this.editor, o = e.item, r = o.getAttribute("uploadId"); if (!n.consumable.consume(e.item, t.name))
                return; var s = i.plugins.get(ih), a = r ? e.attributeNewValue : null, c = this.placeholder, l = i.editing.mapper.toViewElement(o), d = n.writer; if ("reading" == a)
                return wm(l, d), void _m(c, l, d); if ("uploading" == a) {
                var t_295 = s.loaders.get(r);
                return wm(l, d), void (t_295 ? (km(l, d), function (t, e, n, i) { var o = function (t) { var e = t.createUIElement("div", { class: "ck-progress-bar" }); return t.setCustomProperty("progressBar", !0, e), e; }(e); e.insert(e.createPositionAt(t, "end"), o), n.on("change:uploadedPercent", function (t, e, n) { i.change(function (t) { t.setStyle("width", n + "%", o); }); }); }(l, d, t_295, i.editing.view)) : _m(c, l, d));
            } "complete" == a && s.loaders.get(r) && !mo.isEdge && function (t, e, n) { var i = e.createUIElement("div", { class: "ck-image-upload-complete-icon" }); e.insert(e.createPositionAt(t, "end"), i), setTimeout(function () { n.change(function (t) { return t.remove(t.createRangeOn(i)); }); }, 3e3); }(l, d, i.editing.view), function (t, e) { ym(t, e, "progressBar"); }(l, d), km(l, d), function (t, e) { e.removeClass("ck-appear", t); }(l, d); };
            return bm;
        }(Bl));
        function wm(t, e) { t.hasClass("ck-appear") || e.addClass("ck-appear", t); }
        function _m(t, e, n) { e.hasClass("ck-image-upload-placeholder") || n.addClass("ck-image-upload-placeholder", e); var i = e.getChild(0); i.getAttribute("src") !== t && n.setAttribute("src", t, i), vm(e, "placeholder") || n.insert(n.createPositionAfter(i), function (t) { var e = t.createUIElement("div", { class: "ck-upload-placeholder-loader" }); return t.setCustomProperty("placeholder", !0, e), e; }(n)); }
        function km(t, e) { t.hasClass("ck-image-upload-placeholder") && e.removeClass("ck-image-upload-placeholder", t), ym(t, e, "placeholder"); }
        function vm(t, e) { for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
            var n_234 = _k[_j];
            if (n_234.getCustomProperty(e))
                return n_234;
        } }
        function ym(t, e, n) { var i = vm(t, n); i && e.remove(e.createRangeOn(i)); }
        var xm = /** @class */ (function () {
            function xm() {
            }
            xm.prototype.createDocumentFragment = function (t) { return new Ao(t); };
            xm.prototype.createElement = function (t, e, n) { return new _i(t, e, n); };
            xm.prototype.createText = function (t) { return new fi(t); };
            xm.prototype.clone = function (t, e) {
                if (e === void 0) { e = !1; }
                return t._clone(e);
            };
            xm.prototype.appendChild = function (t, e) { return e._appendChild(t); };
            xm.prototype.insertChild = function (t, e, n) { return n._insertChild(t, e); };
            xm.prototype.removeChildren = function (t, e, n) { return n._removeChildren(t, e); };
            xm.prototype.remove = function (t) { var e = t.parent; return e ? this.removeChildren(e.getChildIndex(t), 1, e) : []; };
            xm.prototype.replace = function (t, e) { var n = t.parent; if (n) {
                var i_145 = n.getChildIndex(t);
                return this.removeChildren(i_145, 1, n), this.insertChild(i_145, e, n), !0;
            } return !1; };
            xm.prototype.rename = function (t, e) { var n = new _i(t, e.getAttributes(), e.getChildren()); return this.replace(e, n) ? n : null; };
            xm.prototype.setAttribute = function (t, e, n) { n._setAttribute(t, e); };
            xm.prototype.removeAttribute = function (t, e) { e._removeAttribute(t); };
            xm.prototype.addClass = function (t, e) { e._addClass(t); };
            xm.prototype.removeClass = function (t, e) { e._removeClass(t); };
            xm.prototype.setStyle = function (t, e, n) { C(t) && void 0 === n && (n = e), n._setStyle(t, e); };
            xm.prototype.removeStyle = function (t, e) { e._removeStyle(t); };
            xm.prototype.setCustomProperty = function (t, e, n) { n._setCustomProperty(t, e); };
            xm.prototype.removeCustomProperty = function (t, e) { return e._removeCustomProperty(t); };
            xm.prototype.createPositionAt = function (t, e) { return Zi._createAt(t, e); };
            xm.prototype.createPositionAfter = function (t) { return Zi._createAfter(t); };
            xm.prototype.createPositionBefore = function (t) { return Zi._createBefore(t); };
            xm.prototype.createRange = function (t, e) { return new Xi(t, e); };
            xm.prototype.createRangeOn = function (t) { return Xi._createOn(t); };
            xm.prototype.createRangeIn = function (t) { return Xi._createIn(t); };
            xm.prototype.createSelection = function (t, e, n) { return new no(t, e, n); };
            return xm;
        }());
        var Am = /** @class */ (function (_super) {
            __extends(Am, _super);
            function Am() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Am.prototype.refresh = function () { this.isEnabled = Sf(this.editor.model); };
            Am.prototype.execute = function (t) { var e = this.editor, n = e.model, i = e.plugins.get(ih); n.change(function (e) { var o = Array.isArray(t.file) ? t.file : [t.file]; for (var _j = 0, o_90 = o; _j < o_90.length; _j++) {
                var t_296 = o_90[_j];
                Cm(e, n, i, t_296);
            } }); };
            return Am;
        }(Wl));
        function Cm(t, e, n, i) { var o = n.createLoader(i); o && Ef(t, e, { uploadId: o.id }); }
        var Tm = /** @class */ (function (_super) {
            __extends(Tm, _super);
            function Tm() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Tm, "requires", {
                get: function () { return [ih, Jh]; },
                enumerable: true,
                configurable: true
            });
            Tm.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.model.document, n = t.model.schema, i = t.conversion, o = t.plugins.get(ih);
                n.extend("image", { allowAttributes: ["uploadId", "uploadStatus"] }), t.commands.add("imageUpload", new Am(t)), i.for("upcast").attributeToAttribute({ view: { name: "img", key: "uploadId" }, model: "uploadId" }), this.listenTo(t.editing.view.document, "clipboardInput", function (e, n) { if (function (t) { return Array.from(t.types).includes("text/html") && "" !== t.getData("text/html"); }(n.dataTransfer))
                    return; var i = Array.from(n.dataTransfer.files).filter(function (t) { return !!t && hm(t); }), o = n.targetRanges.map(function (e) { return t.editing.mapper.toModelRange(e); }); t.model.change(function (n) { n.setSelection(o), i.length && (e.stop(), t.model.enqueueChange("default", function () { t.execute("imageUpload", { file: i }); })); }); }), t.plugins.has("Clipboard") && this.listenTo(t.plugins.get("Clipboard"), "inputTransformation", function (e, n) { var i = Array.from(t.editing.view.createRangeIn(n.content)).filter(function (t) { return (function (t) { return !(!t.is("element", "img") || !t.getAttribute("src")) && (t.getAttribute("src").match(/^data:image\/\w+;base64,/g) || t.getAttribute("src").match(/^blob:/g)); })(t.item) && !t.item.getAttribute("uploadProcessed"); }).map(function (t) { return ({ promise: fm(t.item), imageElement: t.item }); }); if (!i.length)
                    return; var r = new xm; for (var _j = 0, i_146 = i; _j < i_146.length; _j++) {
                    var t_297 = i_146[_j];
                    r.setAttribute("uploadProcessed", !0, t_297.imageElement);
                    var e_233 = o.createLoader(t_297.promise);
                    e_233 && (r.setAttribute("src", "", t_297.imageElement), r.setAttribute("uploadId", e_233.id, t_297.imageElement));
                } }), t.editing.view.document.on("dragover", function (t, e) { e.preventDefault(); }), e.on("change", function () { var t = e.differ.getChanges({ includeChangesInGraveyard: !0 }); for (var _j = 0, t_298 = t; _j < t_298.length; _j++) {
                    var e_234 = t_298[_j];
                    if ("insert" == e_234.type && "image" == e_234.name) {
                        var t_299 = e_234.position.nodeAfter, n_235 = "$graveyard" == e_234.position.root.rootName, i_147 = t_299.getAttribute("uploadId");
                        if (!i_147)
                            continue;
                        var r_43 = o.loaders.get(i_147);
                        if (!r_43)
                            continue;
                        n_235 ? r_43.abort() : "idle" == r_43.status && _this._readAndUpload(r_43, t_299);
                    }
                } });
            };
            Tm.prototype._readAndUpload = function (t, e) {
                var _this = this;
                var n = this.editor, i = n.model, o = n.locale.t, r = n.plugins.get(ih), s = n.plugins.get(Jh);
                return i.enqueueChange("transparent", function (t) { t.setAttribute("uploadStatus", "reading", e); }), t.read().then(function (o) { var r = n.editing.mapper.toViewElement(e).getChild(0), s = t.upload(); return n.editing.view.change(function (t) { t.setAttribute("src", o, r); }), i.enqueueChange("transparent", function (t) { t.setAttribute("uploadStatus", "uploading", e); }), s; }).then(function (t) { i.enqueueChange("transparent", function (n) { n.setAttributes({ uploadStatus: "complete", src: t.default }, e), _this._parseAndSetSrcsetAttributeOnImage(t, e, n); }), a(); }).catch(function (n) { if ("error" !== t.status && "aborted" !== t.status)
                    throw n; "error" == t.status && n && s.showWarning(n, { title: o("bh"), namespace: "upload" }), a(), i.enqueueChange("transparent", function (t) { t.remove(e); }); });
                function a() { i.enqueueChange("transparent", function (t) { t.removeAttribute("uploadId", e), t.removeAttribute("uploadStatus", e); }), r.destroyLoader(t); }
            };
            Tm.prototype._parseAndSetSrcsetAttributeOnImage = function (t, e, n) { var i = 0; var o = Object.keys(t).filter(function (t) { var e = parseInt(t, 10); if (!isNaN(e))
                return i = Math.max(i, e), !0; }).map(function (e) { return t[e] + " " + e + "w"; }).join(", "); "" != o && n.setAttribute("srcset", { data: o, width: i }, e); };
            return Tm;
        }(Bl));
        var Pm = /** @class */ (function (_super) {
            __extends(Pm, _super);
            function Pm() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Pm, "pluginName", {
                get: function () { return "ImageUpload"; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Pm, "requires", {
                get: function () { return [Tm, mm, bm]; },
                enumerable: true,
                configurable: true
            });
            return Pm;
        }(Bl));
        var Mm = /** @class */ (function (_super) {
            __extends(Mm, _super);
            function Mm() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Mm.prototype.refresh = function () { var t = this.editor.model, e = qd(t.document.selection.getSelectedBlocks()); this.value = !!e && e.is("paragraph"), this.isEnabled = !!e && Em(e, t.schema); };
            Mm.prototype.execute = function (t) {
                if (t === void 0) { t = {}; }
                var e = this.editor.model, n = e.document;
                e.change(function (i) { var o = (t.selection || n.selection).getSelectedBlocks(); for (var _j = 0, o_91 = o; _j < o_91.length; _j++) {
                    var t_300 = o_91[_j];
                    !t_300.is("paragraph") && Em(t_300, e.schema) && i.rename(t_300, "paragraph");
                } });
            };
            return Mm;
        }(Wl));
        function Em(t, e) { return e.checkChild(t.parent, "paragraph") && !e.isObject(t); }
        var Sm = /** @class */ (function (_super) {
            __extends(Sm, _super);
            function Sm() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Sm, "pluginName", {
                get: function () { return "Paragraph"; },
                enumerable: true,
                configurable: true
            });
            Sm.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.model, n = t.data;
                t.commands.add("paragraph", new Mm(t)), e.schema.register("paragraph", { inheritAllFrom: "$block" }), t.conversion.elementToElement({ model: "paragraph", view: "p" }), n.upcastDispatcher.on("element", function (t, e, n) { var i = n.writer; if (n.consumable.test(e.viewItem, { name: e.viewItem.name }))
                    if (Sm.paragraphLikeElements.has(e.viewItem.name)) {
                        if (e.viewItem.isEmpty)
                            return;
                        var t_301 = i.createElement("paragraph"), o_92 = n.splitToAllowedParent(t_301, e.modelCursor);
                        if (!o_92)
                            return;
                        i.insert(t_301, o_92.position);
                        var r_44 = n.convertChildren(e.viewItem, i.createPositionAt(t_301, 0)).modelRange;
                        e.modelRange = i.createRange(i.createPositionBefore(t_301), r_44.end), e.modelCursor = e.modelRange.end;
                    }
                    else
                        Nm(e.viewItem, e.modelCursor, n.schema) && (e = Object.assign(e, Im(e.viewItem, e.modelCursor, n))); }, { priority: "low" }), n.upcastDispatcher.on("text", function (t, e, n) { e.modelRange || Nm(e.viewItem, e.modelCursor, n.schema) && (e = Object.assign(e, Im(e.viewItem, e.modelCursor, n))); }, { priority: "lowest" }), e.document.registerPostFixer(function (t) { return _this._autoparagraphEmptyRoots(t); }), t.data.on("ready", function () { e.enqueueChange("transparent", function (t) { return _this._autoparagraphEmptyRoots(t); }); }, { priority: "lowest" });
            };
            Sm.prototype._autoparagraphEmptyRoots = function (t) { var e = this.editor.model; for (var _j = 0, _k = e.document.getRootNames(); _j < _k.length; _j++) {
                var n_236 = _k[_j];
                var i_148 = e.document.getRoot(n_236);
                if (i_148.isEmpty && "$graveyard" != i_148.rootName && e.schema.checkChild(i_148, "paragraph"))
                    return t.insertElement("paragraph", i_148), !0;
            } };
            return Sm;
        }(Bl));
        function Im(t, e, n) { var i = n.writer.createElement("paragraph"); return n.writer.insert(i, e), n.convertItem(t, n.writer.createPositionAt(i, 0)); }
        function Nm(t, e, n) { var i = n.createContext(e); return !!n.checkChild(i, "paragraph") && !!n.checkChild(i.push("paragraph"), t); }
        Sm.paragraphLikeElements = new Set(["blockquote", "dd", "div", "dt", "h1", "h2", "h3", "h4", "h5", "h6", "li", "p", "td"]);
        var Om = /** @class */ (function (_super) {
            __extends(Om, _super);
            function Om(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.modelElements = e;
                return _this;
            }
            Om.prototype.refresh = function () {
                var _this = this;
                var t = qd(this.editor.model.document.selection.getSelectedBlocks());
                this.value = !!t && this.modelElements.includes(t.name) && t.name, this.isEnabled = !!t && this.modelElements.some(function (e) { return Rm(t, e, _this.editor.model.schema); });
            };
            Om.prototype.execute = function (t) { var e = this.editor.model, n = e.document, i = t.value; e.change(function (t) { var o = Array.from(n.selection.getSelectedBlocks()).filter(function (t) { return Rm(t, i, e.schema); }); for (var _j = 0, o_93 = o; _j < o_93.length; _j++) {
                var e_235 = o_93[_j];
                e_235.is(i) || t.rename(e_235, i);
            } }); };
            return Om;
        }(Wl));
        function Rm(t, e, n) { return n.checkChild(t.parent, e) && !n.isObject(t); }
        var Dm = "paragraph";
        var Lm = /** @class */ (function (_super) {
            __extends(Lm, _super);
            function Lm(t) {
                var _this = this;
                _this = _super.call(this, t) || this, t.config.define("heading", { options: [{ model: "paragraph", title: "Paragraph", class: "ck-heading_paragraph" }, { model: "heading1", view: "h2", title: "Heading 1", class: "ck-heading_heading1" }, { model: "heading2", view: "h3", title: "Heading 2", class: "ck-heading_heading2" }, { model: "heading3", view: "h4", title: "Heading 3", class: "ck-heading_heading3" }] });
                return _this;
            }
            Object.defineProperty(Lm, "requires", {
                get: function () { return [Sm]; },
                enumerable: true,
                configurable: true
            });
            Lm.prototype.init = function () { var t = this.editor, e = t.config.get("heading.options"), n = []; for (var _j = 0, e_236 = e; _j < e_236.length; _j++) {
                var i_149 = e_236[_j];
                i_149.model !== Dm && (t.model.schema.register(i_149.model, { inheritAllFrom: "$block" }), t.conversion.elementToElement(i_149), n.push(i_149.model));
            } this._addDefaultH1Conversion(t), t.commands.add("heading", new Om(t, n)); };
            Lm.prototype.afterInit = function () { var t = this.editor, e = t.commands.get("enter"), n = t.config.get("heading.options"); e && this.listenTo(e, "afterExecute", function (e, i) { var o = t.model.document.selection.getFirstPosition().parent; n.some(function (t) { return o.is(t.model); }) && !o.is(Dm) && 0 === o.childCount && i.writer.rename(o, Dm); }); };
            Lm.prototype._addDefaultH1Conversion = function (t) { t.conversion.for("upcast").elementToElement({ model: "heading1", view: "h1", converterPriority: Zn.get("low") + 1 }); };
            return Lm;
        }(Bl));
        n(19);
        var jm = /** @class */ (function (_super) {
            __extends(jm, _super);
            function jm() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            jm.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.t, n = function (t) { var e = t.t, n = { Paragraph: e("by"), "Heading 1": e("bz"), "Heading 2": e("ca"), "Heading 3": e("cb"), "Heading 4": e("cc"), "Heading 5": e("cd"), "Heading 6": e("ce") }; return t.config.get("heading.options").map(function (t) { var e = n[t.title]; return e && e != t.title && (t.title = e), t; }); }(t), i = e("y"), o = e("z");
                t.ui.componentFactory.add("heading", function (e) { var r = {}, s = new oo, a = t.commands.get("heading"), c = t.commands.get("paragraph"), l = [a]; var _loop_8 = function (t_302) {
                    var e_237 = { type: "button", model: new Iu({ label: t_302.title, class: t_302.class, withText: !0 }) };
                    "paragraph" === t_302.model ? (e_237.model.bind("isOn").to(c, "value"), e_237.model.set("commandName", "paragraph"), l.push(c)) : (e_237.model.bind("isOn").to(a, "value", function (e) { return e === t_302.model; }), e_237.model.set({ commandName: "heading", commandValue: t_302.model })), s.add(e_237), r[t_302.model] = t_302.title;
                }; for (var _j = 0, n_237 = n; _j < n_237.length; _j++) {
                    var t_302 = n_237[_j];
                    _loop_8(t_302);
                } var d = du(e); return hu(d, s), d.buttonView.set({ isOn: !1, withText: !0, tooltip: o }), d.extendTemplate({ attributes: { class: ["ck-heading-dropdown"] } }), d.bind("isEnabled").toMany(l, "isEnabled", function () {
                    var t = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        t[_j] = arguments[_j];
                    }
                    return t.some(function (t) { return t; });
                }), d.buttonView.bind("label").to(a, "value", c, "value", function (t, e) { var n = t || e && "paragraph"; return r[n] ? r[n] : i; }), _this.listenTo(d, "execute", function (e) { t.execute(e.source.commandName, e.source.commandValue ? { value: e.source.commandValue } : void 0), t.editing.view.focus(); }), d; });
            };
            return jm;
        }(Bl));
        function Vm(t) { for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
            var e_238 = _k[_j];
            if (e_238 && e_238.is("caption"))
                return e_238;
        } return null; }
        function zm(t) { var e = t.parent; return "figcaption" == t.name && e && "figure" == e.name && e.hasClass("image") ? { name: !0 } : null; }
        var Bm = /** @class */ (function (_super) {
            __extends(Bm, _super);
            function Bm() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Bm.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.editing.view, n = t.model.schema, i = t.data, o = t.editing, r = t.t;
                n.register("caption", { allowIn: "image", allowContentOf: "$block", isLimit: !0 }), t.model.document.registerPostFixer(function (t) { return _this._insertMissingModelCaptionElement(t); }), t.conversion.for("upcast").elementToElement({ view: zm, model: "caption" });
                i.downcastDispatcher.on("insert:caption", Fm(function (t) { return t.createContainerElement("figcaption"); }, !1));
                var s = function (t, e) { return function (n) { var i = n.createEditableElement("figcaption"); return n.setCustomProperty("imageCaption", !0, i), rl({ view: t, element: i, text: e }), Af(i, n); }; }(e, r("ay"));
                o.downcastDispatcher.on("insert:caption", Fm(s)), o.downcastDispatcher.on("insert", this._fixCaptionVisibility(function (t) { return t.item; }), { priority: "high" }), o.downcastDispatcher.on("remove", this._fixCaptionVisibility(function (t) { return t.position.parent; }), { priority: "high" }), e.document.registerPostFixer(function (t) { return _this._updateCaptionVisibility(t); });
            };
            Bm.prototype._updateCaptionVisibility = function (t) { var e = this.editor.editing.mapper, n = this._lastSelectedCaption; var i; var o = this.editor.model.document.selection, r = o.getSelectedElement(); if (r && r.is("image")) {
                var t_303 = Vm(r);
                i = e.toViewElement(t_303);
            } var s = Um(o.getFirstPosition().parent); if (s && (i = e.toViewElement(s)), i)
                return n ? n === i ? qm(i, t) : (Hm(n, t), this._lastSelectedCaption = i, qm(i, t)) : (this._lastSelectedCaption = i, qm(i, t)); if (n) {
                var e_239 = Hm(n, t);
                return this._lastSelectedCaption = null, e_239;
            } return !1; };
            Bm.prototype._fixCaptionVisibility = function (t) {
                var _this = this;
                return function (e, n, i) { var o = Um(t(n)), r = _this.editor.editing.mapper, s = i.writer; if (o) {
                    var t_304 = r.toViewElement(o);
                    t_304 && (o.childCount ? s.removeClass("ck-hidden", t_304) : s.addClass("ck-hidden", t_304));
                } };
            };
            Bm.prototype._insertMissingModelCaptionElement = function (t) { var e = this.editor.model, n = e.document.differ.getChanges(), i = []; for (var _j = 0, n_238 = n; _j < n_238.length; _j++) {
                var t_305 = n_238[_j];
                if ("insert" == t_305.type && "$text" != t_305.name) {
                    var n_239 = t_305.position.nodeAfter;
                    if (n_239.is("image") && !Vm(n_239) && i.push(n_239), !n_239.is("image") && n_239.childCount)
                        for (var _k = 0, _q = e.createRangeIn(n_239).getItems(); _k < _q.length; _k++) {
                            var t_306 = _q[_k];
                            t_306.is("image") && !Vm(t_306) && i.push(t_306);
                        }
                }
            } for (var _v = 0, i_150 = i; _v < i_150.length; _v++) {
                var e_240 = i_150[_v];
                t.appendElement("caption", e_240);
            } return !!i.length; };
            return Bm;
        }(Bl));
        function Fm(t, e) {
            if (e === void 0) { e = !0; }
            return function (n, i, o) { var r = i.item; if ((r.childCount || e) && Mf(r.parent)) {
                if (!o.consumable.consume(i.item, "insert"))
                    return;
                var e_241 = o.mapper.toViewElement(i.range.start.parent), n_240 = t(o.writer), s_29 = o.writer;
                r.childCount || s_29.addClass("ck-hidden", n_240), function (t, e, n, i) { var o = i.writer.createPositionAt(n, "end"); i.writer.insert(o, t), i.mapper.bindElements(e, t); }(n_240, i.item, e_241, o);
            } };
        }
        function Um(t) { var e = t.getAncestors({ includeSelf: !0 }).find(function (t) { return "caption" == t.name; }); return e && e.parent && "image" == e.parent.name ? e : null; }
        function Hm(t, e) { return !t.childCount && !t.hasClass("ck-hidden") && (e.addClass("ck-hidden", t), !0); }
        function qm(t, e) { return !!t.hasClass("ck-hidden") && (e.removeClass("ck-hidden", t), !0); }
        n(108);
        var Wm = /** @class */ (function (_super) {
            __extends(Wm, _super);
            function Wm(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this._defaultStyle = !1, _this.styles = e.reduce(function (t, e) { return (t[e.name] = e, e.isDefault && (_this._defaultStyle = e.name), t); }, {});
                return _this;
            }
            Wm.prototype.refresh = function () { var t = this.editor.model.document.selection.getSelectedElement(); if (this.isEnabled = Mf(t), t)
                if (t.hasAttribute("imageStyle")) {
                    var e_242 = t.getAttribute("imageStyle");
                    this.value = !!this.styles[e_242] && e_242;
                }
                else
                    this.value = this._defaultStyle;
            else
                this.value = !1; };
            Wm.prototype.execute = function (t) {
                var _this = this;
                var e = t.value, n = this.editor.model, i = n.document.selection.getSelectedElement();
                n.change(function (t) { _this.styles[e].isDefault ? t.removeAttribute("imageStyle", i) : t.setAttribute("imageStyle", e, i); });
            };
            return Wm;
        }(Wl));
        function Ym(t, e) { for (var _j = 0, e_243 = e; _j < e_243.length; _j++) {
            var n_241 = e_243[_j];
            if (n_241.name === t)
                return n_241;
        } }
        var $m = n(15), Gm = n.n($m), Qm = n(16), Km = n.n(Qm), Jm = n(17), Zm = n.n(Jm), Xm = n(11), tg = n.n(Xm);
        var eg = { full: { name: "full", title: "Full size image", icon: Gm.a, isDefault: !0 }, side: { name: "side", title: "Side image", icon: tg.a, className: "image-style-side" }, alignLeft: { name: "alignLeft", title: "Left aligned image", icon: Km.a, className: "image-style-align-left" }, alignCenter: { name: "alignCenter", title: "Centered image", icon: Zm.a, className: "image-style-align-center" }, alignRight: { name: "alignRight", title: "Right aligned image", icon: tg.a, className: "image-style-align-right" } }, ng = { full: Gm.a, left: Km.a, right: tg.a, center: Zm.a };
        function ig(t) {
            if (t === void 0) { t = []; }
            return t.map(og);
        }
        function og(t) { if ("string" == typeof t) {
            var e_244 = t;
            eg[e_244] ? t = Object.assign({}, eg[e_244]) : (bs.a.warn("image-style-not-found: There is no such image style of given name.", { name: e_244 }), t = { name: e_244 });
        }
        else if (eg[t.name]) {
            var e_245 = eg[t.name], n_242 = Object.assign({}, t);
            for (var i_151 in e_245)
                t.hasOwnProperty(i_151) || (n_242[i_151] = e_245[i_151]);
            t = n_242;
        } return "string" == typeof t.icon && ng[t.icon] && (t.icon = ng[t.icon]), t; }
        var rg = /** @class */ (function (_super) {
            __extends(rg, _super);
            function rg() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(rg, "pluginName", {
                get: function () { return "ImageStyleEditing"; },
                enumerable: true,
                configurable: true
            });
            rg.prototype.init = function () { var t = this.editor, e = t.model.schema, n = t.data, i = t.editing; t.config.define("image.styles", ["full", "side"]); var o = ig(t.config.get("image.styles")); e.extend("image", { allowAttributes: "imageStyle" }); var r = function (t) { return function (e, n, i) { if (!i.consumable.consume(n.item, e.name))
                return; var o = Ym(n.attributeNewValue, t), r = Ym(n.attributeOldValue, t), s = i.mapper.toViewElement(n.item), a = i.writer; r && a.removeClass(r.className, s), o && a.addClass(o.className, s); }; }(o); i.downcastDispatcher.on("attribute:imageStyle:image", r), n.downcastDispatcher.on("attribute:imageStyle:image", r), n.upcastDispatcher.on("element:figure", function (t) { var e = t.filter(function (t) { return !t.isDefault; }); return function (t, n, i) { if (!n.modelRange)
                return; var o = n.viewItem, r = qd(n.modelRange.getItems()); if (i.schema.checkAttribute(r, "imageStyle"))
                for (var _j = 0, e_246 = e; _j < e_246.length; _j++) {
                    var t_307 = e_246[_j];
                    i.consumable.consume(o, { classes: t_307.className }) && i.writer.setAttribute("imageStyle", t_307.name, r);
                } }; }(o), { priority: "low" }), t.commands.add("imageStyle", new Wm(t, o)); };
            return rg;
        }(Bl));
        n(110);
        var sg = /** @class */ (function (_super) {
            __extends(sg, _super);
            function sg() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(sg, "pluginName", {
                get: function () { return "ImageStyleUI"; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(sg.prototype, "localizedDefaultStylesTitles", {
                get: function () { var t = this.editor.t; return { "Full size image": t("az"), "Side image": t("ba"), "Left aligned image": t("bb"), "Centered image": t("bc"), "Right aligned image": t("bd") }; },
                enumerable: true,
                configurable: true
            });
            sg.prototype.init = function () { var t = function (t, e) { for (var _j = 0, t_309 = t; _j < t_309.length; _j++) {
                var n_243 = t_309[_j];
                e[n_243.title] && (n_243.title = e[n_243.title]);
            } return t; }(ig(this.editor.config.get("image.styles")), this.localizedDefaultStylesTitles); for (var _j = 0, t_308 = t; _j < t_308.length; _j++) {
                var e_247 = t_308[_j];
                this._createButton(e_247);
            } };
            sg.prototype._createButton = function (t) {
                var _this = this;
                var e = this.editor, n = "imageStyle:" + t.name;
                e.ui.componentFactory.add(n, function (n) { var i = e.commands.get("imageStyle"), o = new jd(n); return o.set({ label: t.title, icon: t.icon, tooltip: !0 }), o.bind("isEnabled").to(i, "isEnabled"), o.bind("isOn").to(i, "value", function (e) { return e === t.name; }), _this.listenTo(o, "execute", function () { return e.execute("imageStyle", { value: t.name }); }), o; });
            };
            return sg;
        }(Bl));
        var ag = /** @class */ (function (_super) {
            __extends(ag, _super);
            function ag() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(ag, "requires", {
                get: function () { return [em]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(ag, "pluginName", {
                get: function () { return "WidgetToolbarRepository"; },
                enumerable: true,
                configurable: true
            });
            ag.prototype.init = function () {
                var _this = this;
                var t = this.editor;
                if (t.plugins.has("BalloonToolbar")) {
                    var e_248 = t.plugins.get("BalloonToolbar");
                    this.listenTo(e_248, "show", function (e) { (function (t) { var e = t.getSelectedElement(); return !(!e || !vf(e)); })(t.editing.view.document.selection) && e.stop(); }, { priority: "high" });
                }
                this._toolbarDefinitions = new Map, this._balloon = this.editor.plugins.get("ContextualBalloon"), this.listenTo(t.ui, "update", function () { _this._updateToolbarsVisibility(); }), this.listenTo(t.ui.focusTracker, "change:isFocused", function () { _this._updateToolbarsVisibility(); }, { priority: "low" });
            };
            ag.prototype.destroy = function () { _super.prototype.destroy.call(this); for (var _j = 0, _k = this._toolbarDefinitions.values(); _j < _k.length; _j++) {
                var t_310 = _k[_j];
                t_310.view.destroy();
            } };
            ag.prototype.register = function (t, _j) {
                var e = _j.items, n = _j.getRelatedElement, _k = _j.balloonClassName, i = _k === void 0 ? "ck-toolbar-container" : _k;
                var o = this.editor, r = new jl;
                if (this._toolbarDefinitions.has(t))
                    throw new Gn.b("widget-toolbar-duplicated: Toolbar with the given id was already added.", { toolbarId: t });
                r.fillFromConfig(e, o.ui.componentFactory), this._toolbarDefinitions.set(t, { view: r, getRelatedElement: n, balloonClassName: i });
            };
            ag.prototype._updateToolbarsVisibility = function () { var t = 0, e = null, n = null; for (var _j = 0, _k = this._toolbarDefinitions.values(); _j < _k.length; _j++) {
                var i_152 = _k[_j];
                var o_94 = i_152.getRelatedElement(this.editor.editing.view.document.selection);
                if (this.editor.ui.focusTracker.isFocused && o_94) {
                    var r_45 = o_94.getAncestors().length;
                    r_45 > t && (t = r_45, e = o_94, n = i_152);
                }
                else
                    this._hideToolbar(i_152);
            } n && this._showToolbar(n, e); };
            ag.prototype._hideToolbar = function (t) { this._isToolbarVisible(t) && this._balloon.remove(t.view); };
            ag.prototype._showToolbar = function (t, e) { this._isToolbarVisible(t) ? function (t, e) { var n = t.plugins.get("ContextualBalloon"), i = cg(t, e); n.updatePosition(i); }(this.editor, e) : this._balloon.hasView(t.view) || this._balloon.add({ view: t.view, position: cg(this.editor, e), balloonClassName: t.balloonClassName }); };
            ag.prototype._isToolbarVisible = function (t) { return this._balloon.visibleView == t.view; };
            return ag;
        }(Bl));
        function cg(t, e) { var n = t.editing.view, i = Jf.defaultPositions; return { target: n.domConverter.viewToDom(e), positions: [i.northArrowSouth, i.northArrowSouthWest, i.northArrowSouthEast, i.southArrowNorth, i.southArrowNorthWest, i.southArrowNorthEast] }; }
        function lg(t, e, n) { return n.createRange(dg(t, e, !0, n), dg(t, e, !1, n)); }
        function dg(t, e, n, i) { var o = t.textNode || (n ? t.nodeBefore : t.nodeAfter), r = null; for (; o && o.getAttribute("linkHref") == e;)
            r = o, o = n ? o.previousSibling : o.nextSibling; return r ? i.createPositionAt(r, n ? "before" : "after") : t; }
        var ug = /** @class */ (function (_super) {
            __extends(ug, _super);
            function ug() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            ug.prototype.refresh = function () { var t = this.editor.model, e = t.document; this.value = e.selection.getAttribute("linkHref"), this.isEnabled = t.schema.checkAttributeInSelection(e.selection, "linkHref"); };
            ug.prototype.execute = function (t) { var e = this.editor.model, n = e.document.selection; e.change(function (i) { if (n.isCollapsed) {
                var o_96 = n.getFirstPosition();
                if (n.hasAttribute("linkHref")) {
                    var o_97 = lg(n.getFirstPosition(), n.getAttribute("linkHref"), e);
                    i.setAttribute("linkHref", t, o_97), i.setSelection(o_97);
                }
                else if ("" !== t) {
                    var e_249 = Vs(n.getAttributes());
                    e_249.set("linkHref", t);
                    var r_46 = i.createText(t, e_249);
                    i.insert(r_46, o_96), i.setSelection(i.createRangeOn(r_46));
                }
            }
            else {
                var o_98 = e.schema.getValidRanges(n.getRanges(), "linkHref");
                for (var _j = 0, o_95 = o_98; _j < o_95.length; _j++) {
                    var e_250 = o_95[_j];
                    i.setAttribute("linkHref", t, e_250);
                }
            } }); };
            return ug;
        }(Wl));
        var hg = /** @class */ (function (_super) {
            __extends(hg, _super);
            function hg() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            hg.prototype.refresh = function () { this.isEnabled = this.editor.model.document.selection.hasAttribute("linkHref"); };
            hg.prototype.execute = function () { var t = this.editor.model, e = t.document.selection; t.change(function (n) { var i = e.isCollapsed ? [lg(e.getFirstPosition(), e.getAttribute("linkHref"), t)] : e.getRanges(); for (var _j = 0, i_153 = i; _j < i_153.length; _j++) {
                var t_311 = i_153[_j];
                n.removeAttribute("linkHref", t_311);
            } }); };
            return hg;
        }(Wl));
        var fg = /[\u0000-\u0020\u00A0\u1680\u180E\u2000-\u2029\u205f\u3000]/g, mg = /^(?:(?:https?|ftps?|mailto):|[^a-z]|[a-z+.-]+(?:[^a-z+.:-]|$))/i;
        function gg(t, e) { var n = e.createAttributeElement("a", { href: t }, { priority: 5 }); return e.setCustomProperty("link", !0, n), n; }
        function pg(t) { return function (t) { return t.replace(fg, "").match(mg); }(t = String(t)) ? t : "#"; }
        var bg = /** @class */ (function () {
            function bg(t, e, n) {
                var _this = this;
                this.model = t, this.attribute = n, this._modelSelection = t.document.selection, this._overrideUid = null, this._isNextGravityRestorationSkipped = !1, e.listenTo(this._modelSelection, "change:range", function (t, e) { _this._isNextGravityRestorationSkipped ? _this._isNextGravityRestorationSkipped = !1 : _this._isGravityOverridden && (!e.directChange && wg(_this._modelSelection.getFirstPosition(), n) || _this._restoreGravity()); });
            }
            bg.prototype.handleForwardMovement = function (t, e) { var n = this.attribute; if (!(this._isGravityOverridden || t.isAtStart && this._hasSelectionAttribute))
                return vg(t, n) && this._hasSelectionAttribute ? (this._preventCaretMovement(e), this._removeSelectionAttribute(), !0) : _g(t, n) ? (this._preventCaretMovement(e), this._overrideGravity(), !0) : kg(t, n) && this._hasSelectionAttribute ? (this._preventCaretMovement(e), this._overrideGravity(), !0) : void 0; };
            bg.prototype.handleBackwardMovement = function (t, e) { var n = this.attribute; return this._isGravityOverridden ? vg(t, n) && this._hasSelectionAttribute ? (this._preventCaretMovement(e), this._restoreGravity(), this._removeSelectionAttribute(), !0) : (this._preventCaretMovement(e), this._restoreGravity(), t.isAtStart && this._removeSelectionAttribute(), !0) : vg(t, n) && !this._hasSelectionAttribute ? (this._preventCaretMovement(e), this._setSelectionAttributeFromTheNodeBefore(t), !0) : t.isAtEnd && kg(t, n) ? this._hasSelectionAttribute ? void (yg(t, n) && (this._skipNextAutomaticGravityRestoration(), this._overrideGravity())) : (this._preventCaretMovement(e), this._setSelectionAttributeFromTheNodeBefore(t), !0) : t.isAtStart ? this._hasSelectionAttribute ? (this._removeSelectionAttribute(), this._preventCaretMovement(e), !0) : void 0 : void (yg(t, n) && (this._skipNextAutomaticGravityRestoration(), this._overrideGravity())); };
            Object.defineProperty(bg.prototype, "_isGravityOverridden", {
                get: function () { return !!this._overrideUid; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(bg.prototype, "_hasSelectionAttribute", {
                get: function () { return this._modelSelection.hasAttribute(this.attribute); },
                enumerable: true,
                configurable: true
            });
            bg.prototype._overrideGravity = function () { this._overrideUid = this.model.change(function (t) { return t.overrideSelectionGravity(); }); };
            bg.prototype._restoreGravity = function () {
                var _this = this;
                this.model.change(function (t) { t.restoreSelectionGravity(_this._overrideUid), _this._overrideUid = null; });
            };
            bg.prototype._preventCaretMovement = function (t) { t.preventDefault(); };
            bg.prototype._removeSelectionAttribute = function () {
                var _this = this;
                this.model.change(function (t) { t.removeSelectionAttribute(_this.attribute); });
            };
            bg.prototype._setSelectionAttributeFromTheNodeBefore = function (t) {
                var _this = this;
                var e = this.attribute;
                this.model.change(function (n) { n.setSelectionAttribute(_this.attribute, t.nodeBefore.getAttribute(e)); });
            };
            bg.prototype._skipNextAutomaticGravityRestoration = function () { this._isNextGravityRestorationSkipped = !0; };
            return bg;
        }());
        function wg(t, e) { return _g(t, e) || kg(t, e); }
        function _g(t, e) { var n = t.nodeBefore, i = t.nodeAfter, o = !!n && n.hasAttribute(e); return !!i && i.hasAttribute(e) && (!o || n.getAttribute(e) !== i.getAttribute(e)); }
        function kg(t, e) { var n = t.nodeBefore, i = t.nodeAfter, o = !!n && n.hasAttribute(e), r = !!i && i.hasAttribute(e); return o && (!r || n.getAttribute(e) !== i.getAttribute(e)); }
        function vg(t, e) { var n = t.nodeBefore, i = t.nodeAfter, o = !!n && n.hasAttribute(e); if (!!i && i.hasAttribute(e) && o)
            return i.getAttribute(e) !== n.getAttribute(e); }
        function yg(t, e) { return wg(t.getShiftedBy(-1), e); }
        n(112);
        var xg = "ck-link_selected";
        var Ag = /** @class */ (function (_super) {
            __extends(Ag, _super);
            function Ag() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Ag.prototype.init = function () { var t = this.editor; t.model.schema.extend("$text", { allowAttributes: "linkHref" }), t.conversion.for("dataDowncast").attributeToElement({ model: "linkHref", view: gg }), t.conversion.for("editingDowncast").attributeToElement({ model: "linkHref", view: function (t, e) { return gg(pg(t), e); } }), t.conversion.for("upcast").elementToAttribute({ view: { name: "a", attributes: { href: !0 } }, model: { key: "linkHref", value: function (t) { return t.getAttribute("href"); } } }), t.commands.add("link", new ug(t)), t.commands.add("unlink", new hg(t)), function (t, e, n, i) { var o = new bg(e, n, i), r = e.document.selection; n.listenTo(t.document, "keydown", function (t, e) { if (!r.isCollapsed)
                return; if (e.shiftKey || e.altKey || e.ctrlKey)
                return; var n = e.keyCode == bo.arrowright, i = e.keyCode == bo.arrowleft; if (!n && !i)
                return; var s = r.getFirstPosition(); var a; (a = n ? o.handleForwardMovement(s, e) : o.handleBackwardMovement(s, e)) && t.stop(); }, { priority: Zn.get("high") + 1 }); }(t.editing.view, t.model, this, "linkHref"), this._setupLinkHighlight(); };
            Ag.prototype._setupLinkHighlight = function () { var t = this.editor, e = t.editing.view, n = new Set; e.document.registerPostFixer(function (e) { var i = t.model.document.selection; if (i.hasAttribute("linkHref")) {
                var o_99 = lg(i.getFirstPosition(), i.getAttribute("linkHref"), t.model), r_47 = t.editing.mapper.toViewRange(o_99);
                for (var _j = 0, _k = r_47.getItems(); _j < _k.length; _j++) {
                    var t_312 = _k[_j];
                    t_312.is("a") && (e.addClass(xg, t_312), n.add(t_312));
                }
            } }), t.conversion.for("editingDowncast").add(function (t) { function i() { e.change(function (t) { for (var _j = 0, _k = n.values(); _j < _k.length; _j++) {
                var e_251 = _k[_j];
                t.removeClass(xg, e_251), n.delete(e_251);
            } }); } t.on("insert", i, { priority: "highest" }), t.on("remove", i, { priority: "highest" }), t.on("attribute", i, { priority: "highest" }), t.on("selection", i, { priority: "highest" }); }); };
            return Ag;
        }(Bl));
        var Cg = /** @class */ (function (_super) {
            __extends(Cg, _super);
            function Cg(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.domEventType = "click";
                return _this;
            }
            Cg.prototype.onDomEvent = function (t) { this.fire(t.type, t); };
            return Cg;
        }(ts));
        n(114);
        var Tg = /** @class */ (function (_super) {
            __extends(Tg, _super);
            function Tg(t) {
                var _this = _super.call(this, t) || this;
                var e = t.t;
                _this.focusTracker = new nl, _this.keystrokes = new Gc, _this.urlInputView = _this._createUrlInput(), _this.saveButtonView = _this._createButton(e("bn"), Wf.a, "ck-button-save"), _this.saveButtonView.type = "submit", _this.cancelButtonView = _this._createButton(e("bo"), $f.a, "ck-button-cancel", "cancel"), _this._focusables = new dl, _this._focusCycler = new Rl({ focusables: _this._focusables, focusTracker: _this.focusTracker, keystrokeHandler: _this.keystrokes, actions: { focusPrevious: "shift + tab", focusNext: "tab" } }), _this.setTemplate({ tag: "form", attributes: { class: ["ck", "ck-link-form"], tabindex: "-1" }, children: [_this.urlInputView, _this.saveButtonView, _this.cancelButtonView] });
                return _this;
            }
            Tg.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this), Hf({ view: this }), [this.urlInputView, this.saveButtonView, this.cancelButtonView].forEach(function (t) { _this._focusables.add(t), _this.focusTracker.add(t.element); }), this.keystrokes.listenTo(this.element);
            };
            Tg.prototype.focus = function () { this._focusCycler.focusFirst(); };
            Tg.prototype._createUrlInput = function () { var t = this.locale.t, e = new Ff(this.locale, Uf); return e.label = t("bp"), e.inputView.placeholder = "https://example.com", e; };
            Tg.prototype._createButton = function (t, e, n, i) { var o = new jd(this.locale); return o.set({ label: t, icon: e, tooltip: !0 }), o.extendTemplate({ attributes: { class: n } }), i && o.delegate("execute").to(this, i), o; };
            return Tg;
        }(Sl));
        var Pg = n(42), Mg = n.n(Pg), Eg = n(43), Sg = n.n(Eg);
        n(116);
        var Ig = /** @class */ (function (_super) {
            __extends(Ig, _super);
            function Ig(t) {
                var _this = _super.call(this, t) || this;
                var e = t.t;
                _this.focusTracker = new nl, _this.keystrokes = new Gc, _this.previewButtonView = _this._createPreviewButton(), _this.unlinkButtonView = _this._createButton(e("bq"), Mg.a, "unlink"), _this.editButtonView = _this._createButton(e("br"), Sg.a, "edit"), _this.set("href"), _this._focusables = new dl, _this._focusCycler = new Rl({ focusables: _this._focusables, focusTracker: _this.focusTracker, keystrokeHandler: _this.keystrokes, actions: { focusPrevious: "shift + tab", focusNext: "tab" } }), _this.setTemplate({ tag: "div", attributes: { class: ["ck", "ck-link-actions"], tabindex: "-1" }, children: [_this.previewButtonView, _this.editButtonView, _this.unlinkButtonView] });
                return _this;
            }
            Ig.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this), [this.previewButtonView, this.editButtonView, this.unlinkButtonView].forEach(function (t) { _this._focusables.add(t), _this.focusTracker.add(t.element); }), this.keystrokes.listenTo(this.element);
            };
            Ig.prototype.focus = function () { this._focusCycler.focusFirst(); };
            Ig.prototype._createButton = function (t, e, n) { var i = new jd(this.locale); return i.set({ label: t, icon: e, tooltip: !0 }), i.delegate("execute").to(this, n), i; };
            Ig.prototype._createPreviewButton = function () { var t = new jd(this.locale), e = this.bindTemplate, n = this.t; return t.set({ withText: !0, tooltip: n("bs") }), t.extendTemplate({ attributes: { class: ["ck", "ck-link-actions__preview"], href: e.to("href", function (t) { return t && pg(t); }), target: "_blank" } }), t.bind("label").to(this, "href", function (t) { return t || n("bt"); }), t.bind("isEnabled").to(this, "href", function (t) { return !!t; }), t.template.tag = "a", t.template.eventListeners = {}, t; };
            return Ig;
        }(Sl));
        var Ng = n(44), Og = n.n(Ng);
        var Rg = "Ctrl+K";
        var Dg = /** @class */ (function (_super) {
            __extends(Dg, _super);
            function Dg() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Dg, "requires", {
                get: function () { return [em]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Dg, "pluginName", {
                get: function () { return "LinkUI"; },
                enumerable: true,
                configurable: true
            });
            Dg.prototype.init = function () { var t = this.editor; t.editing.view.addObserver(Cg), this.actionsView = this._createActionsView(), this.formView = this._createFormView(), this._balloon = t.plugins.get(em), this._createToolbarLinkButton(), this._enableUserBalloonInteractions(); };
            Dg.prototype.destroy = function () { _super.prototype.destroy.call(this), this.formView.destroy(); };
            Dg.prototype._createActionsView = function () {
                var _this = this;
                var t = this.editor, e = new Ig(t.locale), n = t.commands.get("link"), i = t.commands.get("unlink");
                return e.bind("href").to(n, "value"), e.editButtonView.bind("isEnabled").to(n), e.unlinkButtonView.bind("isEnabled").to(i), this.listenTo(e, "edit", function () { _this._addFormView(); }), this.listenTo(e, "unlink", function () { t.execute("unlink"), _this._hideUI(); }), e.keystrokes.set("Esc", function (t, e) { _this._hideUI(), e(); }), e.keystrokes.set(Rg, function (t, e) { _this._addFormView(), e(); }), e;
            };
            Dg.prototype._createFormView = function () {
                var _this = this;
                var t = this.editor, e = new Tg(t.locale), n = t.commands.get("link");
                return e.urlInputView.bind("value").to(n, "value"), e.urlInputView.bind("isReadOnly").to(n, "isEnabled", function (t) { return !t; }), e.saveButtonView.bind("isEnabled").to(n), this.listenTo(e, "submit", function () { t.execute("link", e.urlInputView.inputView.element.value), _this._removeFormView(); }), this.listenTo(e, "cancel", function () { _this._removeFormView(); }), e.keystrokes.set("Esc", function (t, e) { _this._removeFormView(), e(); }), e;
            };
            Dg.prototype._createToolbarLinkButton = function () {
                var _this = this;
                var t = this.editor, e = t.commands.get("link"), n = t.t;
                t.keystrokes.set(Rg, function (t, n) { n(), e.isEnabled && _this._showUI(); }), t.ui.componentFactory.add("link", function (t) { var i = new jd(t); return i.isEnabled = !0, i.label = n("bg"), i.icon = Og.a, i.keystroke = Rg, i.tooltip = !0, i.bind("isOn", "isEnabled").to(e, "value", "isEnabled"), _this.listenTo(i, "execute", function () { return _this._showUI(); }), i; });
            };
            Dg.prototype._enableUserBalloonInteractions = function () {
                var _this = this;
                var t = this.editor.editing.view.document;
                this.listenTo(t, "click", function () { _this._getSelectedLinkElement() && _this._showUI(); }), this.editor.keystrokes.set("Tab", function (t, e) { _this._areActionsVisible && !_this.actionsView.focusTracker.isFocused && (_this.actionsView.focus(), e()); }, { priority: "high" }), this.editor.keystrokes.set("Esc", function (t, e) { _this._isUIVisible && (_this._hideUI(), e()); }), lu({ emitter: this.formView, activator: function () { return _this._isUIVisible; }, contextElements: [this._balloon.view.element], callback: function () { return _this._hideUI(); } });
            };
            Dg.prototype._addActionsView = function () { this._areActionsInPanel || this._balloon.add({ view: this.actionsView, position: this._getBalloonPositionData() }); };
            Dg.prototype._addFormView = function () { if (this._isFormInPanel)
                return; var t = this.editor.commands.get("link"); this._balloon.add({ view: this.formView, position: this._getBalloonPositionData() }), this.formView.urlInputView.select(), this.formView.urlInputView.inputView.element.value = t.value || ""; };
            Dg.prototype._removeFormView = function () { this._isFormInPanel && (this.formView.saveButtonView.focus(), this._balloon.remove(this.formView), this.editor.editing.view.focus()); };
            Dg.prototype._showUI = function () { this.editor.commands.get("link").isEnabled && (this._getSelectedLinkElement() ? this._areActionsVisible ? this._addFormView() : this._addActionsView() : (this._addActionsView(), this._addFormView()), this._startUpdatingUI()); };
            Dg.prototype._hideUI = function () { if (!this._isUIInPanel)
                return; var t = this.editor; this.stopListening(t.ui, "update"), t.editing.view.focus(), this._removeFormView(), this._balloon.remove(this.actionsView); };
            Dg.prototype._startUpdatingUI = function () {
                var _this = this;
                var t = this.editor, e = t.editing.view.document;
                var n = this._getSelectedLinkElement(), i = o();
                function o() { return e.selection.focus.getAncestors().reverse().find(function (t) { return t.is("element"); }); }
                this.listenTo(t.ui, "update", function () { var t = _this._getSelectedLinkElement(), e = o(); n && !t || !n && e !== i ? _this._hideUI() : _this._balloon.updatePosition(_this._getBalloonPositionData()), n = t, i = e; });
            };
            Object.defineProperty(Dg.prototype, "_isFormInPanel", {
                get: function () { return this._balloon.hasView(this.formView); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Dg.prototype, "_areActionsInPanel", {
                get: function () { return this._balloon.hasView(this.actionsView); },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Dg.prototype, "_areActionsVisible", {
                get: function () { return this._balloon.visibleView === this.actionsView; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Dg.prototype, "_isUIInPanel", {
                get: function () { return this._isFormInPanel || this._areActionsInPanel; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Dg.prototype, "_isUIVisible", {
                get: function () { return this._balloon.visibleView == this.formView || this._areActionsVisible; },
                enumerable: true,
                configurable: true
            });
            Dg.prototype._getBalloonPositionData = function () { var t = this.editor.editing.view, e = t.document, n = this._getSelectedLinkElement(); return { target: n ? t.domConverter.mapViewToDom(n) : t.domConverter.viewRangeToDom(e.selection.getFirstRange()) }; };
            Dg.prototype._getSelectedLinkElement = function () { var t = this.editor.editing.view, e = t.document.selection; if (e.isCollapsed)
                return Lg(e.getFirstPosition()); {
                var n_244 = e.getFirstRange().getTrimmed(), i_154 = Lg(n_244.start), o_100 = Lg(n_244.end);
                return i_154 && i_154 == o_100 && t.createRangeIn(i_154).getTrimmed().isEqual(n_244) ? i_154 : null;
            } };
            return Dg;
        }(Bl));
        function Lg(t) { return t.getAncestors().find(function (t) { return (function (t) { return t.is("attributeElement") && !!t.getCustomProperty("link"); })(t); }); }
        var jg = /** @class */ (function (_super) {
            __extends(jg, _super);
            function jg(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.type = "bulleted" == e ? "bulleted" : "numbered";
                return _this;
            }
            jg.prototype.refresh = function () { this.value = this._getValue(), this.isEnabled = this._checkEnabled(); };
            jg.prototype.execute = function () {
                var _this = this;
                var t = this.editor.model, e = t.document, n = Array.from(e.selection.getSelectedBlocks()).filter(function (e) { return zg(e, t.schema); }), i = !0 === this.value;
                t.change(function (t) { if (i) {
                    var e_252 = n[n.length - 1].nextSibling, i_155 = Number.POSITIVE_INFINITY, o_102 = [];
                    for (; e_252 && "listItem" == e_252.name && 0 !== e_252.getAttribute("listIndent");) {
                        var t_313 = e_252.getAttribute("listIndent");
                        t_313 < i_155 && (i_155 = t_313);
                        var n_246 = t_313 - i_155;
                        o_102.push({ element: e_252, listIndent: n_246 }), e_252 = e_252.nextSibling;
                    }
                    o_102 = o_102.reverse();
                    for (var _j = 0, o_101 = o_102; _j < o_101.length; _j++) {
                        var e_253 = o_101[_j];
                        t.setAttribute("listIndent", e_253.listIndent, e_253.element);
                    }
                } if (!i) {
                    var t_314 = Number.POSITIVE_INFINITY;
                    for (var _k = 0, n_245 = n; _k < n_245.length; _k++) {
                        var e_254 = n_245[_k];
                        e_254.is("listItem") && e_254.getAttribute("listIndent") < t_314 && (t_314 = e_254.getAttribute("listIndent"));
                    }
                    Vg(n, !0, t_314 = 0 === t_314 ? 1 : t_314), Vg(n, !1, t_314);
                } for (var _q = 0, _v = n.reverse(); _q < _v.length; _q++) {
                    var e_255 = _v[_q];
                    i && "listItem" == e_255.name ? t.rename(e_255, "paragraph") : i || "listItem" == e_255.name ? i || "listItem" != e_255.name || e_255.getAttribute("listType") == _this.type || t.setAttribute("listType", _this.type, e_255) : (t.setAttributes({ listType: _this.type, listIndent: 0 }, e_255), t.rename(e_255, "listItem"));
                } });
            };
            jg.prototype._getValue = function () { var t = qd(this.editor.model.document.selection.getSelectedBlocks()); return !!t && t.is("listItem") && t.getAttribute("listType") == this.type; };
            jg.prototype._checkEnabled = function () { if (this.value)
                return !0; var t = this.editor.model.document.selection, e = this.editor.model.schema, n = qd(t.getSelectedBlocks()); return !!n && zg(n, e); };
            return jg;
        }(Wl));
        function Vg(t, e, n) { var i = e ? t[0] : t[t.length - 1]; if (i.is("listItem")) {
            var o_103 = i[e ? "previousSibling" : "nextSibling"], r_48 = i.getAttribute("listIndent");
            for (; o_103 && o_103.is("listItem") && o_103.getAttribute("listIndent") >= n;)
                r_48 > o_103.getAttribute("listIndent") && (r_48 = o_103.getAttribute("listIndent")), o_103.getAttribute("listIndent") == r_48 && t[e ? "unshift" : "push"](o_103), o_103 = o_103[e ? "previousSibling" : "nextSibling"];
        } }
        function zg(t, e) { return e.checkChild(t.parent, "listItem") && !e.isObject(t); }
        var Bg = /** @class */ (function (_super) {
            __extends(Bg, _super);
            function Bg(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this._indentBy = "forward" == e ? 1 : -1;
                return _this;
            }
            Bg.prototype.refresh = function () { this.isEnabled = this._checkEnabled(); };
            Bg.prototype.execute = function () {
                var _this = this;
                var t = this.editor.model, e = t.document;
                var n = Array.from(e.selection.getSelectedBlocks());
                t.change(function (t) { var e = n[n.length - 1]; var i = e.nextSibling; for (; i && "listItem" == i.name && i.getAttribute("listIndent") > e.getAttribute("listIndent");)
                    n.push(i), i = i.nextSibling; _this._indentBy < 0 && (n = n.reverse()); for (var _j = 0, n_247 = n; _j < n_247.length; _j++) {
                    var e_256 = n_247[_j];
                    var n_248 = e_256.getAttribute("listIndent") + _this._indentBy;
                    n_248 < 0 ? t.rename(e_256, "paragraph") : t.setAttribute("listIndent", n_248, e_256);
                } });
            };
            Bg.prototype._checkEnabled = function () { var t = qd(this.editor.model.document.selection.getSelectedBlocks()); if (!t || !t.is("listItem"))
                return !1; if (this._indentBy > 0) {
                var e_257 = t.getAttribute("listIndent"), n_249 = t.getAttribute("listType");
                var i_156 = t.previousSibling;
                for (; i_156 && i_156.is("listItem") && i_156.getAttribute("listIndent") >= e_257;) {
                    if (i_156.getAttribute("listIndent") == e_257)
                        return i_156.getAttribute("listType") == n_249;
                    i_156 = i_156.previousSibling;
                }
                return !1;
            } return !0; };
            return Bg;
        }(Wl));
        function Fg() { var t = !this.isEmpty && ("ul" == this.getChild(0).name || "ol" == this.getChild(0).name); return this.isEmpty || t ? 0 : xi.call(this); }
        function Ug(t) { return function (e, n, i) { var o = i.consumable; if (!o.test(n.item, "insert") || !o.test(n.item, "attribute:listType") || !o.test(n.item, "attribute:listIndent"))
            return; o.consume(n.item, "insert"), o.consume(n.item, "attribute:listType"), o.consume(n.item, "attribute:listIndent"); var r = n.item; np(r, function (t, e) { var n = e.mapper, i = e.writer, o = "numbered" == t.getAttribute("listType") ? "ol" : "ul", r = function (t) { var e = t.createContainerElement("li"); return e.getFillerOffset = Fg, e; }(i), s = i.createContainerElement(o, null); return i.insert(i.createPositionAt(s, 0), r), n.bindElements(t, r), r; }(r, i), i, t); }; }
        function Hg(t) { return function (e, n, i) { var o = i.mapper.toViewPosition(n.position).getLastMatchingPosition(function (t) { return !t.item.is("li"); }).nodeAfter, r = i.writer; r.breakContainer(r.createPositionBefore(o)), r.breakContainer(r.createPositionAfter(o)); var s = o.parent, a = s.previousSibling, c = r.createRangeOn(s), l = r.remove(c); a && a.nextSibling && ep(r, a, a.nextSibling), ip(i.mapper.toModelElement(o).getAttribute("listIndent") + 1, n.position, c.start, o, i, t); for (var _j = 0, _k = r.createRangeIn(l).getItems(); _j < _k.length; _j++) {
            var t_315 = _k[_j];
            i.mapper.unbindViewElement(t_315);
        } e.stop(); }; }
        function qg(t, e, n) { if (!n.consumable.consume(e.item, "attribute:listType"))
            return; var i = n.mapper.toViewElement(e.item), o = n.writer; o.breakContainer(o.createPositionBefore(i)), o.breakContainer(o.createPositionAfter(i)); var r = i.parent; var s = "numbered" == e.attributeNewValue ? "ol" : "ul"; ep(o, r = o.rename(s, r), r.nextSibling), ep(o, r.previousSibling, r); for (var _j = 0, _k = e.item.getChildren(); _j < _k.length; _j++) {
            var t_316 = _k[_j];
            n.consumable.consume(t_316, "insert");
        } }
        function Wg(t) { return function (e, n, i) { if (!i.consumable.consume(n.item, "attribute:listIndent"))
            return; var o = i.mapper.toViewElement(n.item), r = i.writer; r.breakContainer(r.createPositionBefore(o)), r.breakContainer(r.createPositionAfter(o)); var s = o.parent, a = s.previousSibling, c = r.createRangeOn(s); r.remove(c), a && a.nextSibling && ep(r, a, a.nextSibling), ip(n.attributeOldValue + 1, n.range.start, c.start, o, i, t), np(n.item, o, i, t); for (var _j = 0, _k = n.item.getChildren(); _j < _k.length; _j++) {
            var t_317 = _k[_j];
            i.consumable.consume(t_317, "insert");
        } }; }
        function Yg(t, e, n) { if ("listItem" != e.item.name) {
            var t_318 = n.mapper.toViewPosition(e.range.start);
            var i_157 = n.writer, o_104 = [];
            for (; ("ul" == t_318.parent.name || "ol" == t_318.parent.name) && "li" == (t_318 = i_157.breakContainer(t_318)).parent.name;) {
                var e_258 = t_318, n_250 = i_157.createPositionAt(t_318.parent, "end");
                if (!e_258.isEqual(n_250)) {
                    var t_319 = i_157.remove(i_157.createRange(e_258, n_250));
                    o_104.push(t_319);
                }
                t_318 = i_157.createPositionAfter(t_318.parent);
            }
            if (o_104.length > 0) {
                for (var e_259 = 0; e_259 < o_104.length; e_259++) {
                    var n_251 = t_318.nodeBefore;
                    if (t_318 = i_157.insert(t_318, o_104[e_259]).end, e_259 > 0) {
                        var e_260 = ep(i_157, n_251, n_251.nextSibling);
                        e_260 && e_260.parent == n_251 && t_318.offset--;
                    }
                }
                ep(i_157, t_318.nodeBefore, t_318.nodeAfter);
            }
        } }
        function $g(t, e, n) { var i = n.mapper.toViewPosition(e.position), o = i.nodeBefore, r = i.nodeAfter; ep(n.writer, o, r); }
        function Gg(t, e, n) { if (n.consumable.consume(e.viewItem, { name: !0 })) {
            var t_320 = n.writer, i_158 = this.conversionApi.store, o_105 = t_320.createElement("listItem");
            i_158.indent = i_158.indent || 0, t_320.setAttribute("listIndent", i_158.indent, o_105);
            var r_49 = e.viewItem.parent && "ol" == e.viewItem.parent.name ? "numbered" : "bulleted";
            t_320.setAttribute("listType", r_49, o_105), i_158.indent++;
            var s_30 = n.splitToAllowedParent(o_105, e.modelCursor);
            if (!s_30)
                return;
            t_320.insert(o_105, s_30.position);
            var a_13 = function (t, e, n) { var i = n.writer, o = n.schema; var r = i.createPositionAfter(t); for (var _j = 0, e_261 = e; _j < e_261.length; _j++) {
                var s_31 = e_261[_j];
                if ("ul" == s_31.name || "ol" == s_31.name)
                    r = n.convertItem(s_31, r).modelCursor;
                else {
                    var e_262 = n.convertItem(s_31, i.createPositionAt(t, "end")), a_14 = e_262.modelRange.start.nodeAfter, c_10 = a_14 && a_14.is("element") && !o.checkChild(t, a_14.name);
                    c_10 && (t = e_262.modelCursor.parent.is("listItem") ? e_262.modelCursor.parent : Xg(e_262.modelCursor), r = i.createPositionAfter(t));
                }
            } return r; }(o_105, e.viewItem.getChildren(), n);
            i_158.indent--, e.modelRange = t_320.createRange(e.modelCursor, a_13), s_30.cursorParent ? e.modelCursor = t_320.createPositionAt(s_30.cursorParent, 0) : e.modelCursor = e.modelRange.end;
        } }
        function Qg(t, e, n) { if (n.consumable.test(e.viewItem, { name: !0 })) {
            var t_322 = Array.from(e.viewItem.getChildren());
            for (var _j = 0, t_321 = t_322; _j < t_321.length; _j++) {
                var e_263 = t_321[_j];
                e_263.is("li") || e_263._remove();
            }
        } }
        function Kg(t, e, n) { if (n.consumable.test(e.viewItem, { name: !0 })) {
            if (0 === e.viewItem.childCount)
                return;
            var t_324 = e.viewItem.getChildren().slice();
            var n_252 = !1, i_159 = !0;
            for (var _j = 0, t_323 = t_324; _j < t_323.length; _j++) {
                var e_264 = t_323[_j];
                !n_252 || e_264.is("ul") || e_264.is("ol") || e_264._remove(), e_264.is("text") ? (i_159 && (e_264._data = e_264.data.replace(/^\s+/, "")), (!e_264.nextSibling || e_264.nextSibling.is("ul") || e_264.nextSibling.is("ol")) && (e_264._data = e_264.data.replace(/\s+$/, ""))) : (e_264.is("ul") || e_264.is("ol")) && (n_252 = !0), i_159 = !1;
            }
        } }
        function Jg(t) { return function (e, n) { if (n.isPhantom)
            return; var i = n.modelPosition.nodeBefore; if (i && i.is("listItem")) {
            var e_265 = n.mapper.toViewElement(i), o_106 = e_265.getAncestors().find(function (t) { return t.is("ul") || t.is("ol"); }), r_51 = t.createPositionAt(e_265, 0).getWalker();
            for (var _j = 0, r_50 = r_51; _j < r_50.length; _j++) {
                var t_325 = r_50[_j];
                if ("elementStart" == t_325.type && t_325.item.is("li")) {
                    n.viewPosition = t_325.previousPosition;
                    break;
                }
                if ("elementEnd" == t_325.type && t_325.item == o_106) {
                    n.viewPosition = t_325.nextPosition;
                    break;
                }
            }
        } }; }
        function Zg(t, _j) {
            var e = _j[0], n = _j[1];
            var i, o = e.is("documentFragment") ? e.getChild(0) : e;
            if (i = n ? this.createSelection(n) : this.document.selection, o && o.is("listItem")) {
                var t_326 = i.getFirstPosition();
                var e_266 = null;
                if (t_326.parent.is("listItem") ? e_266 = t_326.parent : t_326.nodeBefore && t_326.nodeBefore.is("listItem") && (e_266 = t_326.nodeBefore), e_266) {
                    var t_327 = e_266.getAttribute("listIndent");
                    if (t_327 > 0)
                        for (; o && o.is("listItem");)
                            o._setAttribute("listIndent", o.getAttribute("listIndent") + t_327), o = o.nextSibling;
                }
            }
        }
        function Xg(t) { var e = new qs({ startPosition: t }); var n; do {
            n = e.next();
        } while (!n.value.item.is("listItem")); return n.value.item; }
        function tp(t, e) { var n = !!e.sameIndent, i = !!e.smallerIndent, o = e.listIndent; var r = t; for (; r && "listItem" == r.name;) {
            var t_328 = r.getAttribute("listIndent");
            if (n && o == t_328 || i && o > t_328)
                return r;
            r = r.previousSibling;
        } return null; }
        function ep(t, e, n) { return e && n && ("ul" == e.name || "ol" == e.name) && e.name == n.name ? t.mergeContainers(t.createPositionAfter(e)) : null; }
        function np(t, e, n, i) { var o = e.parent, r = n.mapper, s = n.writer; var a = r.toViewPosition(i.createPositionBefore(t)); var c = tp(t.previousSibling, { sameIndent: !0, smallerIndent: !0, listIndent: t.getAttribute("listIndent") }), l = t.previousSibling; if (c && c.getAttribute("listIndent") == t.getAttribute("listIndent")) {
            var t_329 = r.toViewElement(c);
            a = s.breakContainer(s.createPositionAfter(t_329));
        }
        else
            a = l && "listItem" == l.name ? r.toViewPosition(i.createPositionAt(l, "end")) : r.toViewPosition(i.createPositionBefore(t)); if (a = op(a), s.insert(a, o), l && "listItem" == l.name) {
            var t_330 = r.toViewElement(l), n_254 = s.createRange(s.createPositionAt(t_330, 0), a).getWalker({ ignoreElementEnd: !0 });
            for (var _j = 0, n_253 = n_254; _j < n_253.length; _j++) {
                var t_331 = n_253[_j];
                if (t_331.item.is("li")) {
                    var i_160 = s.breakContainer(s.createPositionBefore(t_331.item)), o_107 = t_331.item.parent, r_52 = s.createPositionAt(e, "end");
                    ep(s, r_52.nodeBefore, r_52.nodeAfter), s.move(s.createRangeOn(o_107), r_52), n_254.position = i_160;
                }
            }
        }
        else {
            var n_255 = o.nextSibling;
            if (n_255 && (n_255.is("ul") || n_255.is("ol"))) {
                var i_161 = null;
                for (var _k = 0, _q = n_255.getChildren(); _k < _q.length; _k++) {
                    var e_267 = _q[_k];
                    var n_256 = r.toModelElement(e_267);
                    if (!(n_256 && n_256.getAttribute("listIndent") > t.getAttribute("listIndent")))
                        break;
                    i_161 = e_267;
                }
                i_161 && (s.breakContainer(s.createPositionAfter(i_161)), s.move(s.createRangeOn(i_161.parent), s.createPositionAt(e, "end")));
            }
        } ep(s, o, o.nextSibling), ep(s, o.previousSibling, o); }
        function ip(t, e, n, i, o, r) { var s = tp(e.nodeBefore, { sameIndent: !0, smallerIndent: !0, listIndent: t, foo: "b" }), a = o.mapper, c = o.writer, l = s ? s.getAttribute("listIndent") : null; var d; if (s)
            if (l == t) {
                var t_332 = a.toViewElement(s).parent;
                d = c.createPositionAfter(t_332);
            }
            else {
                var t_333 = r.createPositionAt(s, "end");
                d = a.toViewPosition(t_333);
            }
        else
            d = n; d = op(d); for (var _j = 0, _k = i.getChildren().slice(); _j < _k.length; _j++) {
            var t_334 = _k[_j];
            (t_334.is("ul") || t_334.is("ol")) && (d = c.move(c.createRangeOn(t_334), d).end, ep(c, t_334, t_334.nextSibling), ep(c, t_334.previousSibling, t_334));
        } }
        function op(t) { return t.getLastMatchingPosition(function (t) { return t.item.is("uiElement"); }); }
        var rp = /** @class */ (function (_super) {
            __extends(rp, _super);
            function rp() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(rp, "requires", {
                get: function () { return [Sm]; },
                enumerable: true,
                configurable: true
            });
            rp.prototype.init = function () {
                var _this = this;
                var t = this.editor;
                t.model.schema.register("listItem", { inheritAllFrom: "$block", allowAttributes: ["listType", "listIndent"] });
                var e = t.data, n = t.editing;
                t.model.document.registerPostFixer(function (e) { return (function (t, e) { var n = t.document.differ.getChanges(), i = new Map; var o = !1; for (var _j = 0, n_257 = n; _j < n_257.length; _j++) {
                    var t_335 = n_257[_j];
                    if ("insert" == t_335.type && "listItem" == t_335.name)
                        r(t_335.position);
                    else if ("insert" == t_335.type && "listItem" != t_335.name) {
                        if ("$text" != t_335.name) {
                            var n_258 = t_335.position.nodeAfter;
                            n_258.hasAttribute("listIndent") && (e.removeAttribute("listIndent", n_258), o = !0), n_258.hasAttribute("listType") && (e.removeAttribute("listType", n_258), o = !0);
                        }
                        r(t_335.position.getShiftedBy(t_335.length));
                    }
                    else
                        "remove" == t_335.type && "listItem" == t_335.name ? r(t_335.position) : "attribute" == t_335.type && "listIndent" == t_335.attributeKey ? r(t_335.range.start) : "attribute" == t_335.type && "listType" == t_335.attributeKey && r(t_335.range.start);
                } for (var _k = 0, _q = i.values(); _k < _q.length; _k++) {
                    var t_336 = _q[_k];
                    s(t_336), a(t_336);
                } return o; function r(t) { var e = t.nodeBefore; if (e && e.is("listItem")) {
                    var n_259 = e;
                    if (i.has(n_259))
                        return;
                    for (; n_259.previousSibling && n_259.previousSibling.is("listItem");)
                        if (n_259 = n_259.previousSibling, i.has(n_259))
                            return;
                    i.set(t.nodeBefore, n_259);
                }
                else {
                    var e_268 = t.nodeAfter;
                    e_268 && e_268.is("listItem") && i.set(e_268, e_268);
                } } function s(t) { var n = 0, i = null; for (; t && t.is("listItem");) {
                    var r_53 = t.getAttribute("listIndent");
                    if (r_53 > n) {
                        var s_32 = void 0;
                        null === i ? (i = r_53 - n, s_32 = n) : (i > r_53 && (i = r_53), s_32 = r_53 - i), e.setAttribute("listIndent", s_32, t), o = !0;
                    }
                    else
                        i = null, n = t.getAttribute("listIndent") + 1;
                    t = t.nextSibling;
                } } function a(t) { var n = [], i = null; for (; t && t.is("listItem");) {
                    var r_54 = t.getAttribute("listIndent");
                    if (i && i.getAttribute("listIndent") > r_54 && (n = n.slice(0, r_54 + 1)), 0 != r_54)
                        if (n[r_54]) {
                            var i_162 = n[r_54];
                            t.getAttribute("listType") != i_162 && (e.setAttribute("listType", i_162, t), o = !0);
                        }
                        else
                            n[r_54] = t.getAttribute("listType");
                    i = t, t = t.nextSibling;
                } } })(t.model, e); }), n.mapper.registerViewToModelLength("li", sp), e.mapper.registerViewToModelLength("li", sp), n.mapper.on("modelToViewPosition", Jg(n.view)), n.mapper.on("viewToModelPosition", function (t) { return function (e, n) { var i = n.viewPosition, o = i.parent, r = n.mapper; if ("ul" == o.name || "ol" == o.name) {
                    if (i.isAtEnd) {
                        var e_269 = r.toModelElement(i.nodeBefore), o_108 = r.getModelLength(i.nodeBefore);
                        n.modelPosition = t.createPositionBefore(e_269).getShiftedBy(o_108);
                    }
                    else {
                        var e_270 = r.toModelElement(i.nodeAfter);
                        n.modelPosition = t.createPositionBefore(e_270);
                    }
                    e.stop();
                }
                else if ("li" == o.name && i.nodeBefore && ("ul" == i.nodeBefore.name || "ol" == i.nodeBefore.name)) {
                    var s_33 = r.toModelElement(o);
                    var a_15 = 1, c_11 = i.nodeBefore;
                    for (; c_11 && (c_11.is("ul") || c_11.is("ol"));)
                        a_15 += r.getModelLength(c_11), c_11 = c_11.previousSibling;
                    n.modelPosition = t.createPositionBefore(s_33).getShiftedBy(a_15), e.stop();
                } }; }(t.model)), e.mapper.on("modelToViewPosition", Jg(n.view)), n.downcastDispatcher.on("insert", Yg, { priority: "high" }), n.downcastDispatcher.on("insert:listItem", Ug(t.model)), e.downcastDispatcher.on("insert", Yg, { priority: "high" }), e.downcastDispatcher.on("insert:listItem", Ug(t.model)), n.downcastDispatcher.on("attribute:listType:listItem", qg), e.downcastDispatcher.on("attribute:listType:listItem", qg), n.downcastDispatcher.on("attribute:listIndent:listItem", Wg(t.model)), e.downcastDispatcher.on("attribute:listIndent:listItem", Wg(t.model)), n.downcastDispatcher.on("remove:listItem", Hg(t.model)), n.downcastDispatcher.on("remove", $g, { priority: "low" }), e.downcastDispatcher.on("remove:listItem", Hg(t.model)), e.downcastDispatcher.on("remove", $g, { priority: "low" }), e.upcastDispatcher.on("element:ul", Qg, { priority: "high" }), e.upcastDispatcher.on("element:ol", Qg, { priority: "high" }), e.upcastDispatcher.on("element:li", Kg, { priority: "high" }), e.upcastDispatcher.on("element:li", Gg), t.model.on("insertContent", Zg, { priority: "high" }), t.commands.add("numberedList", new jg(t, "numbered")), t.commands.add("bulletedList", new jg(t, "bulleted")), t.commands.add("indentList", new Bg(t, "forward")), t.commands.add("outdentList", new Bg(t, "backward"));
                var i = this.editor.editing.view.document;
                this.listenTo(i, "enter", function (t, e) { var n = _this.editor.model.document, i = n.selection.getLastPosition().parent; n.selection.isCollapsed && "listItem" == i.name && i.isEmpty && (_this.editor.execute("outdentList"), e.preventDefault(), t.stop()); }), this.listenTo(i, "delete", function (t, e) { if ("backward" !== e.direction)
                    return; var n = _this.editor.model.document.selection; if (!n.isCollapsed)
                    return; var i = n.getFirstPosition(); if (!i.isAtStart)
                    return; var o = i.parent; "listItem" === o.name && (o.previousSibling && "listItem" === o.previousSibling.name || (_this.editor.execute("outdentList"), e.preventDefault(), t.stop())); }, { priority: "high" });
                var o = function (t) { return function (e, n) { _this.editor.commands.get(t).isEnabled && (_this.editor.execute(t), n()); }; };
                this.editor.keystrokes.set("Tab", o("indentList")), this.editor.keystrokes.set("Shift+Tab", o("outdentList"));
            };
            return rp;
        }(Bl));
        function sp(t) { var e = 1; for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
            var n_260 = _k[_j];
            if ("ul" == n_260.name || "ol" == n_260.name)
                for (var _q = 0, _v = n_260.getChildren(); _q < _v.length; _q++) {
                    var t_337 = _v[_q];
                    e += sp(t_337);
                }
        } return e; }
        var ap = n(45), cp = n.n(ap), lp = n(46), dp = n.n(lp);
        var up = /** @class */ (function (_super) {
            __extends(up, _super);
            function up() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            up.prototype.init = function () { var t = this.editor.t; this._addButton("numberedList", t("ad"), cp.a), this._addButton("bulletedList", t("ae"), dp.a); };
            up.prototype._addButton = function (t, e, n) {
                var _this = this;
                var i = this.editor;
                i.ui.componentFactory.add(t, function (o) { var r = i.commands.get(t), s = new jd(o); return s.set({ label: e, icon: n, tooltip: !0 }), s.bind("isOn", "isEnabled").to(r, "value", "isEnabled"), _this.listenTo(s, "execute", function () { return i.execute(t); }), s; });
            };
            return up;
        }(Bl));
        function hp(t, e) { return function (t) { t.on("attribute:url:media", n); }; function n(n, i, o) { if (!o.consumable.consume(i.item, n.name))
            return; var r = i.attributeNewValue, s = o.writer, a = o.mapper.toViewElement(i.item); s.remove(s.createRangeIn(a)); var c = t.getMediaViewElement(s, r, e); s.insert(s.createPositionAt(a, 0), c); } }
        function fp(t, e, n, i) { var o = t.createContainerElement("figure", { class: "media" }); return o.getFillerOffset = pp, t.insert(t.createPositionAt(o, 0), e.getMediaViewElement(t, n, i)), o; }
        function mp(t) { var e = t.getSelectedElement(); return e && e.is("media") ? e : null; }
        function gp(t, e, n) { t.change(function (i) { var o = i.createElement("media", { url: e }); t.insertContent(o, n), i.setSelection(o, "on"); }); }
        function pp() { return null; }
        var bp = /** @class */ (function (_super) {
            __extends(bp, _super);
            function bp() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            bp.prototype.refresh = function () { var t = this.editor.model, e = t.document.selection, n = t.schema, i = e.getFirstPosition(), o = mp(e); var r = i.parent; r != r.root && (r = r.parent), this.value = o ? o.getAttribute("url") : null, this.isEnabled = n.checkChild(r, "media"); };
            bp.prototype.execute = function (t) { var e = this.editor.model, n = e.document.selection, i = mp(n); if (i)
                e.change(function (e) { e.setAttribute("url", t, i); });
            else {
                var i_163 = Cf(n, e);
                gp(e, t, i_163);
            } };
            return bp;
        }(Wl));
        var wp = n(47), _p = n.n(wp);
        var kp = "0 0 64 42";
        var vp = /** @class */ (function () {
            function vp(t, e) {
                var n = e.providers, i = e.extraProviders || [], o = new Set(e.removeProviders), r = n.concat(i).filter(function (t) { var e = t.name; return e ? !o.has(e) : (bs.a.warn("media-embed-no-provider-name: The configured media provider has no name and cannot be used.", { provider: t }), !1); });
                this.locale = t, this.providerDefinitions = r;
            }
            vp.prototype.hasMedia = function (t) { return !!this._getMedia(t); };
            vp.prototype.getMediaViewElement = function (t, e, n) { return this._getMedia(e).getViewElement(t, n); };
            vp.prototype._getMedia = function (t) { if (!t)
                return new yp(this.locale); t = t.trim(); for (var _j = 0, _k = this.providerDefinitions; _j < _k.length; _j++) {
                var e_271 = _k[_j];
                var n_261 = e_271.html;
                var i_165 = e_271.url;
                Array.isArray(i_165) || (i_165 = [i_165]);
                for (var _q = 0, i_164 = i_165; _q < i_164.length; _q++) {
                    var e_272 = i_164[_q];
                    var i_166 = this._getUrlMatches(t, e_272);
                    if (i_166)
                        return new yp(this.locale, t, i_166, n_261);
                }
            } return null; };
            vp.prototype._getUrlMatches = function (t, e) { var n = t.match(e); if (n)
                return n; var i = t.replace(/^https?:\/\//, ""); return (n = i.match(e)) ? n : (n = (i = i.replace(/^www\./, "")).match(e)) || null; };
            return vp;
        }());
        var yp = /** @class */ (function () {
            function yp(t, e, n, i) {
                this.url = this._getValidUrl(e), this._t = t.t, this._match = n, this._previewRenderer = i;
            }
            yp.prototype.getViewElement = function (t, e) { var n = {}; if (e.renderForEditingView || e.renderMediaPreview && this.url && this._previewRenderer) {
                this.url && (n["data-oembed-url"] = this.url), e.renderForEditingView && (n.class = "ck-media__wrapper");
                var i_167 = this._getPreviewHtml(e);
                return t.createUIElement("div", n, function (t) { var e = this.toDomElement(t); return e.innerHTML = i_167, e; });
            } return this.url && (n.url = this.url), t.createEmptyElement("oembed", n); };
            yp.prototype._getPreviewHtml = function (t) { return this._previewRenderer ? this._previewRenderer(this._match) : this.url && t.renderForEditingView ? this._getPlaceholderHtml() : ""; };
            yp.prototype._getPlaceholderHtml = function () { var t = new Ld, e = new Dd; return t.text = this._t("Open media in new tab"), e.content = _p.a, e.viewBox = kp, new hl({ tag: "div", attributes: { class: "ck ck-reset_all ck-media__placeholder" }, children: [{ tag: "div", attributes: { class: "ck-media__placeholder__icon" }, children: [e] }, { tag: "a", attributes: { class: "ck-media__placeholder__url", target: "new", href: this.url }, children: [{ tag: "span", attributes: { class: "ck-media__placeholder__url__text" }, children: [this.url] }, t] }] }).render().outerHTML; };
            yp.prototype._getValidUrl = function (t) { return t ? t.match(/^https?/) ? t : "https://" + t : null; };
            return yp;
        }());
        n(118);
        var xp = /** @class */ (function (_super) {
            __extends(xp, _super);
            function xp(t) {
                var _this = this;
                _this = _super.call(this, t) || this, t.config.define("mediaEmbed", { providers: [{ name: "dailymotion", url: /^dailymotion\.com\/video\/(\w+)/, html: function (t) { return '<div style="position: relative; padding-bottom: 100%; height: 0; ">' + ("<iframe src=\"https://www.dailymotion.com/embed/video/" + t[1] + "\" ") + 'style="position: absolute; width: 100%; height: 100%; top: 0; left: 0;" frameborder="0" width="480" height="270" allowfullscreen allow="autoplay"></iframe></div>'; } }, { name: "spotify", url: [/^open\.spotify\.com\/(artist\/\w+)/, /^open\.spotify\.com\/(album\/\w+)/, /^open\.spotify\.com\/(track\/\w+)/], html: function (t) { return '<div style="position: relative; padding-bottom: 100%; height: 0; padding-bottom: 126%;">' + ("<iframe src=\"https://open.spotify.com/embed/" + t[1] + "\" ") + 'style="position: absolute; width: 100%; height: 100%; top: 0; left: 0;" frameborder="0" allowtransparency="true" allow="encrypted-media"></iframe></div>'; } }, { name: "youtube", url: [/^youtube\.com\/watch\?v=([\w-]+)/, /^youtube\.com\/v\/([\w-]+)/, /^youtube\.com\/embed\/([\w-]+)/, /^youtu\.be\/([\w-]+)/], html: function (t) { return '<div style="position: relative; padding-bottom: 100%; height: 0; padding-bottom: 56.2493%;">' + ("<iframe src=\"https://www.youtube.com/embed/" + t[1] + "\" ") + 'style="position: absolute; width: 100%; height: 100%; top: 0; left: 0;" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe></div>'; } }, { name: "vimeo", url: [/^vimeo\.com\/(\d+)/, /^vimeo\.com\/[^/]+\/[^/]+\/video\/(\d+)/, /^vimeo\.com\/album\/[^/]+\/video\/(\d+)/, /^vimeo\.com\/channels\/[^/]+\/(\d+)/, /^vimeo\.com\/groups\/[^/]+\/videos\/(\d+)/, /^vimeo\.com\/ondemand\/[^/]+\/(\d+)/, /^player\.vimeo\.com\/video\/(\d+)/], html: function (t) { return '<div style="position: relative; padding-bottom: 100%; height: 0; padding-bottom: 56.2493%;">' + ("<iframe src=\"https://player.vimeo.com/video/" + t[1] + "\" ") + 'style="position: absolute; width: 100%; height: 100%; top: 0; left: 0;" frameborder="0" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe></div>'; } }, { name: "instagram", url: /^instagram\.com\/p\/(\w+)/ }, { name: "twitter", url: /^twitter\.com/ }, { name: "googleMaps", url: /^google\.com\/maps/ }, { name: "flickr", url: /^flickr\.com/ }, { name: "facebook", url: /^facebook\.com/ }] }), _this.registry = new vp(t.locale, t.config.get("mediaEmbed"));
                return _this;
            }
            Object.defineProperty(xp, "pluginName", {
                get: function () { return "MediaEmbedEditing"; },
                enumerable: true,
                configurable: true
            });
            xp.prototype.init = function () { var t = this.editor, e = t.model.schema, n = t.t, i = t.conversion, o = t.config.get("mediaEmbed.previewsInData"), r = this.registry; t.commands.add("mediaEmbed", new bp(t)), e.register("media", { isObject: !0, isBlock: !0, allowWhere: "$block", allowAttributes: ["url"] }), i.for("dataDowncast").elementToElement({ model: "media", view: function (t, e) { var n = t.getAttribute("url"); return fp(e, r, n, { renderMediaPreview: n && o }); } }), i.for("dataDowncast").add(hp(r, { renderMediaPreview: o })), i.for("editingDowncast").elementToElement({ model: "media", view: function (t, e) { var i = t.getAttribute("url"); return function (t, e, n) { return e.setCustomProperty("media", !0, t), yf(t, e, { label: n }); }(fp(e, r, i, { renderForEditingView: !0 }), e, n("bf")); } }), i.for("editingDowncast").add(hp(r, { renderForEditingView: !0 })), i.for("upcast").elementToElement({ view: { name: "oembed", attributes: { url: !0 } }, model: function (t, e) { var n = t.getAttribute("url"); if (r.hasMedia(n))
                    return e.createElement("media", { url: n }); } }).elementToElement({ view: { name: "div", attributes: { "data-oembed-url": !0 } }, model: function (t, e) { var n = t.getAttribute("data-oembed-url"); if (r.hasMedia(n))
                    return e.createElement("media", { url: n }); } }); };
            return xp;
        }(Bl));
        var Ap = /^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w.-]+)+[\w\-._~:/?#[\]@!$&'()*+,;=]+$/;
        var Cp = /** @class */ (function (_super) {
            __extends(Cp, _super);
            function Cp(t) {
                var _this = this;
                _this = _super.call(this, t) || this, _this._timeoutId = null, _this._positionToInsert = null;
                return _this;
            }
            Object.defineProperty(Cp, "requires", {
                get: function () { return [ql, Hd]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Cp, "pluginName", {
                get: function () { return "AutoMediaEmbed"; },
                enumerable: true,
                configurable: true
            });
            Cp.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.model.document;
                this.listenTo(t.plugins.get(ql), "inputTransformation", function () { var t = e.selection.getFirstRange(), n = Oc.fromPosition(t.start); n.stickiness = "toPrevious"; var i = Oc.fromPosition(t.end); i.stickiness = "toNext", e.once("change:data", function () { _this._embedMediaBetweenPositions(n, i), n.detach(), i.detach(); }, { priority: "high" }); }), t.commands.get("undo").on("execute", function () { _this._timeoutId && (nr.window.clearTimeout(_this._timeoutId), _this._positionToInsert.detach(), _this._timeoutId = null, _this._positionToInsert = null); }, { priority: "high" });
            };
            Cp.prototype._embedMediaBetweenPositions = function (t, e) {
                var _this = this;
                var n = this.editor, i = n.plugins.get(xp).registry, o = new oa(t, e), r = o.getWalker({ ignoreElementEnd: !0 });
                var s = "";
                for (var _j = 0, r_55 = r; _j < r_55.length; _j++) {
                    var t_338 = r_55[_j];
                    t_338.item.is("textProxy") && (s += t_338.item.data);
                }
                if (!(s = s.trim()).match(Ap))
                    return;
                if (!i.hasMedia(s))
                    return;
                n.commands.get("mediaEmbed").isEnabled && (this._positionToInsert = Oc.fromPosition(t), this._timeoutId = nr.window.setTimeout(function () { n.model.change(function (t) { var e; _this._timeoutId = null, t.remove(o), "$graveyard" !== _this._positionToInsert.root.rootName && (e = _this._positionToInsert), gp(n.model, s, e), _this._positionToInsert.detach(), _this._positionToInsert = null; }); }, 100));
            };
            return Cp;
        }(Bl));
        n(120);
        var Tp = /** @class */ (function (_super) {
            __extends(Tp, _super);
            function Tp(t, e) {
                var _this = _super.call(this, e) || this;
                var n = e.t;
                _this.focusTracker = new nl, _this.keystrokes = new Gc, _this.urlInputView = _this._createUrlInput(), _this.saveButtonView = _this._createButton(n("bn"), Wf.a, "ck-button-save"), _this.saveButtonView.type = "submit", _this.cancelButtonView = _this._createButton(n("bo"), $f.a, "ck-button-cancel", "cancel"), _this._focusables = new dl, _this._focusCycler = new Rl({ focusables: _this._focusables, focusTracker: _this.focusTracker, keystrokeHandler: _this.keystrokes, actions: { focusPrevious: "shift + tab", focusNext: "tab" } }), _this._validators = t, _this.setTemplate({ tag: "form", attributes: { class: ["ck", "ck-media-form"], tabindex: "-1" }, children: [_this.urlInputView, _this.saveButtonView, _this.cancelButtonView] });
                return _this;
            }
            Tp.prototype.render = function () {
                var _this = this;
                _super.prototype.render.call(this), Hf({ view: this }), [this.urlInputView, this.saveButtonView, this.cancelButtonView].forEach(function (t) { _this._focusables.add(t), _this.focusTracker.add(t.element); }), this.keystrokes.listenTo(this.element);
                var t = function (t) { return t.stopPropagation(); };
                this.keystrokes.set("arrowright", t), this.keystrokes.set("arrowleft", t), this.keystrokes.set("arrowup", t), this.keystrokes.set("arrowdown", t), this.listenTo(this.urlInputView.element, "selectstart", function (t, e) { e.stopPropagation(); }, { priority: "high" });
            };
            Tp.prototype.focus = function () { this._focusCycler.focusFirst(); };
            Object.defineProperty(Tp.prototype, "url", {
                get: function () { return this.urlInputView.inputView.element.value.trim(); },
                set: function (t) { this.urlInputView.inputView.element.value = t.trim(); },
                enumerable: true,
                configurable: true
            });
            Tp.prototype.isValid = function () { this.resetFormStatus(); for (var _j = 0, _k = this._validators; _j < _k.length; _j++) {
                var t_339 = _k[_j];
                var e_273 = t_339(this);
                if (e_273)
                    return this.urlInputView.errorText = e_273, !1;
            } return !0; };
            Tp.prototype.resetFormStatus = function () { this.urlInputView.errorText = null, this.urlInputView.infoText = this._urlInputViewInfoDefault; };
            Tp.prototype._createUrlInput = function () {
                var _this = this;
                var t = this.locale.t, e = new Ff(this.locale, Uf), n = e.inputView;
                return this._urlInputViewInfoDefault = t("cj"), this._urlInputViewInfoTip = t("ck"), e.label = t("cl"), e.infoText = this._urlInputViewInfoDefault, n.placeholder = "https://example.com", n.on("input", function () { e.infoText = n.element.value ? _this._urlInputViewInfoTip : _this._urlInputViewInfoDefault; }), e;
            };
            Tp.prototype._createButton = function (t, e, n, i) { var o = new jd(this.locale); return o.set({ label: t, icon: e, tooltip: !0 }), o.extendTemplate({ attributes: { class: n } }), i && o.delegate("execute").to(this, i), o; };
            return Tp;
        }(Sl));
        var Pp = n(48), Mp = n.n(Pp);
        var Ep = /** @class */ (function (_super) {
            __extends(Ep, _super);
            function Ep() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(Ep, "requires", {
                get: function () { return [xp]; },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(Ep, "pluginName", {
                get: function () { return "MediaEmbedUI"; },
                enumerable: true,
                configurable: true
            });
            Ep.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.commands.get("mediaEmbed"), n = t.plugins.get(xp).registry;
                this.form = new Tp(function (t, e) { return [function (e) { if (!e.url.length)
                        return t("ch"); }, function (n) { if (!e.hasMedia(n.url))
                        return t("ci"); }]; }(t.t, n), t.locale), t.ui.componentFactory.add("mediaEmbed", function (n) { var i = du(n); return _this._setUpDropdown(i, _this.form, e, t), _this._setUpForm(_this.form, i, e), i; });
            };
            Ep.prototype._setUpDropdown = function (t, e, n) { var i = this.editor, o = i.t, r = t.buttonView; function s() { i.editing.view.focus(), t.isOpen = !1; } t.bind("isEnabled").to(n), t.panelView.children.add(e), r.set({ label: o("cg"), icon: Mp.a, tooltip: !0 }), r.on("open", function () { e.url = n.value || "", e.urlInputView.select(), e.focus(); }, { priority: "low" }), t.on("submit", function () { e.isValid() && (i.execute("mediaEmbed", e.url), s()); }), t.on("change:isOpen", function () { return e.resetFormStatus(); }), t.on("cancel", function () { return s(); }); };
            Ep.prototype._setUpForm = function (t, e, n) { t.delegate("submit", "cancel").to(e), t.urlInputView.bind("value").to(n, "value"), t.urlInputView.bind("isReadOnly").to(n, "isEnabled", function (t) { return !t; }), t.saveButtonView.bind("isEnabled").to(n); };
            return Ep;
        }(Bl));
        n(122);
        function Sp(t) { return t.replace(/<span(?: class="Apple-converted-space"|)>(\s+)<\/span>/g, function (t, e) { return 1 === e.length ? " " : Array(e.length + 1).join("  ").substr(0, e.length); }); }
        function Ip(t) { var e = new DOMParser, n = function (t) { return Sp(Sp(t)).replace(/(<span style=['"]mso-spacerun:yes['"]>[\s]*?)[\r\n]+(\s*<\/span>)/g, "$1$2").replace(/<span style=['"]mso-spacerun:yes['"]><\/span>/g, "").replace(/ <\//g, " </").replace(/ <o:p><\/o:p>/g, " <o:p></o:p>").replace(/>(\s*[\r\n]\s*)</g, "><"); }(function (t) { var e = t.match(/<\/body>(.*?)(<\/html>|$)/); e && e[1] && (t = t.slice(0, e.index) + t.slice(e.index).replace(e[1], "")); return t; }(t = t.replace(/<!--\[if gte vml 1]>/g, ""))), i = e.parseFromString(n, "text/html"); !function (t) { t.querySelectorAll("span[style*=spacerun]").forEach(function (t) { var e = t.childNodes[0].data.length; t.innerHTML = Array(e + 1).join("  ").substr(0, e); }); }(i); var o = i.body.innerHTML, r = function (t) { var e = new rr({ blockFiller: jo }), n = t.createDocumentFragment(), i = t.body.childNodes; for (; i.length > 0;)
            n.appendChild(i[0]); return e.domToView(n); }(i), s = function (t) { var e = [], n = [], i = Array.from(t.getElementsByTagName("style")); for (var _j = 0, i_168 = i; _j < i_168.length; _j++) {
            var t_340 = i_168[_j];
            t_340.sheet && t_340.sheet.cssRules && t_340.sheet.cssRules.length && (e.push(t_340.sheet), n.push(t_340.innerHTML));
        } return { styles: e, stylesString: n.join(" ") }; }(i); return { body: r, bodyString: o, styles: s.styles, stylesString: s.stylesString }; }
        function Np(t, e) { if (!t.childCount)
            return; var n = new xm, i = function (t, e) { var n = e.createRangeIn(t), i = new bi({ name: /^p|h\d+$/, styles: { "mso-list": /.*/ } }), o = []; for (var _j = 0, n_262 = n; _j < n_262.length; _j++) {
            var t_341 = n_262[_j];
            if ("elementStart" === t_341.type && i.match(t_341.item)) {
                var e_274 = Op(t_341.item);
                o.push({ element: t_341.item, id: e_274.id, order: e_274.order, indent: e_274.indent });
            }
        } return o; }(t, n); if (!i.length)
            return; var o = null; i.forEach(function (t, r) { if (!o || function (t, e) { if (t.id !== e.id)
            return !0; var n = e.element.previousSibling; if (!n)
            return !0; return !n.is("ul") && !n.is("ol"); }(i[r - 1], t)) {
            var i_169 = function (t, e) { var n = /mso-level-number-format:([^;]*);/gi, i = new RegExp("@list l" + t.id + ":level" + t.indent + "\\s*({[^}]*)", "gi").exec(e); var o = "decimal"; if (i && i[1]) {
                var t_342 = n.exec(i[1]);
                t_342 && t_342[1] && (o = t_342[1].trim());
            } return { type: "bullet" !== o && "image" !== o ? "ol" : "ul", style: o }; }(t, e);
            o = function (t, e, n) { var i = new _i(t.type), o = e.parent.getChildIndex(e); return n.insertChild(o, i, e.parent), i; }(i_169, t.element, n);
        } var s = function (t, e) { return function (t, e) { var n = new bi({ name: "span", styles: { "mso-list": "Ignore" } }), i = e.createRangeIn(t); for (var _j = 0, i_170 = i; _j < i_170.length; _j++) {
            var t_343 = i_170[_j];
            "elementStart" === t_343.type && n.match(t_343.item) && e.remove(t_343.item);
        } }(t, e), e.rename("li", t); }(t.element, n); n.appendChild(s, o); }); }
        function Op(t) { var e = {}, n = t.getStyle("mso-list"); return n && (e.id = parseInt(n.match(/(^|\s+)l(\d+)/i)[2]), e.order = parseInt(n.match(/\s*lfo(\d+)/i)[1]), e.indent = parseInt(n.match(/\s*level(\d+)/i)[1])), e; }
        function Rp(t, e) { if (!t.childCount)
            return; var n = new xm; !function (t, e, n) { var i = n.createRangeIn(e), o = new bi({ name: "img" }), r = []; for (var _j = 0, i_171 = i; _j < i_171.length; _j++) {
            var e_275 = i_171[_j];
            if (o.match(e_275.item)) {
                var n_263 = e_275.item, i_172 = n_263.getAttribute("v:shapes") ? n_263.getAttribute("v:shapes").split(" ") : [];
                i_172.length && i_172.every(function (e) { return t.indexOf(e) > -1; }) ? r.push(n_263) : n_263.getAttribute("src") || r.push(n_263);
            }
        } for (var _k = 0, r_56 = r; _k < r_56.length; _k++) {
            var t_344 = r_56[_k];
            n.remove(t_344);
        } }(function (t, e) { var n = e.createRangeIn(t), i = new bi({ name: /v:(.+)/ }), o = []; for (var _j = 0, n_264 = n; _j < n_264.length; _j++) {
            var t_345 = n_264[_j];
            var e_276 = t_345.item, n_265 = e_276.previousSibling && e_276.previousSibling.name || null;
            i.match(e_276) && e_276.getAttribute("o:gfxdata") && "v:shapetype" !== n_265 && o.push(t_345.item.getAttribute("id"));
        } return o; }(t, n), t, n), function (t, e) { var n = e.createRangeIn(t), i = new bi({ name: /v:(.+)/ }), o = []; for (var _j = 0, n_266 = n; _j < n_266.length; _j++) {
            var t_346 = n_266[_j];
            i.match(t_346.item) && o.push(t_346.item);
        } for (var _k = 0, o_109 = o; _k < o_109.length; _k++) {
            var t_347 = o_109[_k];
            e.remove(t_347);
        } }(t, n); var i = function (t, e) { var n = e.createRangeIn(t), i = new bi({ name: "img" }), o = []; for (var _j = 0, n_267 = n; _j < n_267.length; _j++) {
            var t_348 = n_267[_j];
            i.match(t_348.item) && t_348.item.getAttribute("src").startsWith("file://") && o.push(t_348.item);
        } return o; }(t, n); i.length && function (t, e, n) { if (t.length === e.length)
            for (var i_173 = 0; i_173 < t.length; i_173++) {
                var o_110 = "data:" + e[i_173].type + ";base64," + Dp(e[i_173].hex);
                n.setAttribute("src", o_110, t[i_173]);
            } }(i, function (t) { if (!t)
            return []; var e = /{\\pict[\s\S]+?\\bliptag-?\d+(\\blipupi-?\d+)?({\\\*\\blipuid\s?[\da-fA-F]+)?[\s}]*?/, n = new RegExp("(?:(" + e.source + "))([\\da-fA-F\\s]+)\\}", "g"), i = t.match(n), o = []; if (i)
            for (var _j = 0, i_174 = i; _j < i_174.length; _j++) {
                var t_349 = i_174[_j];
                var n_268 = !1;
                t_349.includes("\\pngblip") ? n_268 = "image/png" : t_349.includes("\\jpegblip") && (n_268 = "image/jpeg"), n_268 && o.push({ hex: t_349.replace(e, "").replace(/[^\da-fA-F]/g, ""), type: n_268 });
            } return o; }(e), n); }
        function Dp(t) { return btoa(t.match(/\w{2}/g).map(function (t) { return String.fromCharCode(parseInt(t, 16)); }).join("")); }
        function Lp(t, e) { var n = e.parent; for (; n;) {
            if (n.name === t)
                return n;
            n = n.parent;
        } }
        function jp(t, e, n, i, o) {
            if (o === void 0) { o = 1; }
            e > o ? i.setAttribute(t, e, n) : i.removeAttribute(t, n);
        }
        function Vp(t, e, n) {
            if (n === void 0) { n = {}; }
            var i = t.createElement("tableCell", n);
            t.insertElement("paragraph", i), t.insert(i, e);
        }
        function zp() { return function (t) { t.on("element:table", function (t, e, n) { var i = e.viewItem; if (!n.consumable.test(i, { name: !0 }))
            return; var _j = function (t) { var e = { headingRows: 0, headingColumns: 0 }, n = [], i = []; var o; for (var _j = 0, _k = Array.from(t.getChildren()); _j < _k.length; _j++) {
            var r_57 = _k[_j];
            if ("tbody" === r_57.name || "thead" === r_57.name || "tfoot" === r_57.name) {
                "thead" !== r_57.name || o || (o = r_57);
                var t_351 = Array.from(r_57.getChildren()).filter(function (t) { return t.is("element", "tr"); });
                for (var _q = 0, t_350 = t_351; _q < t_350.length; _q++) {
                    var r_58 = t_350[_q];
                    if ("thead" === r_58.parent.name && r_58.parent === o)
                        e.headingRows++, n.push(r_58);
                    else {
                        i.push(r_58);
                        var t_352 = Fp(r_58);
                        t_352 > e.headingColumns && (e.headingColumns = t_352);
                    }
                }
            }
        } return e.rows = n.concat(i), e; }(i), o = _j.rows, r = _j.headingRows, s = _j.headingColumns, a = {}; s && (a.headingColumns = s), r && (a.headingRows = r); var c = n.writer.createElement("table", a), l = n.splitToAllowedParent(c, e.modelCursor); if (l) {
            if (n.writer.insert(c, l.position), n.consumable.consume(i, { name: !0 }), o.length)
                o.forEach(function (t) { return n.convertItem(t, n.writer.createPositionAt(c, "end")); });
            else {
                var t_353 = n.writer.createElement("tableRow");
                n.writer.insert(t_353, n.writer.createPositionAt(c, "end")), Vp(n.writer, n.writer.createPositionAt(t_353, "end"));
            }
            e.modelRange = n.writer.createRange(n.writer.createPositionBefore(c), n.writer.createPositionAfter(c)), l.cursorParent ? e.modelCursor = n.writer.createPositionAt(l.cursorParent, 0) : e.modelCursor = e.modelRange.end;
        } }); }; }
        function Bp(t) { return function (e) { e.on("element:" + t, function (t, e, n) { var i = e.viewItem; if (!n.consumable.test(i, { name: !0 }))
            return; var o = n.writer.createElement("tableCell"), r = n.splitToAllowedParent(o, e.modelCursor); if (!r)
            return; n.writer.insert(o, r.position), n.consumable.consume(i, { name: !0 }); var s = n.writer.createPositionAt(o, 0); n.convertChildren(i, s), o.childCount || n.writer.insertElement("paragraph", s), e.modelRange = n.writer.createRange(n.writer.createPositionBefore(o), n.writer.createPositionAfter(o)), e.modelCursor = e.modelRange.end; }); }; }
        function Fp(t) { var e = 0, n = 0; var i = Array.from(t.getChildren()).filter(function (t) { return "th" === t.name || "td" === t.name; }); for (; n < i.length && "th" === i[n].name;) {
            var t_354 = i[n];
            e += parseInt(t_354.getAttribute("colspan") || 1), n++;
        } return e; }
        var Up = /** @class */ (function () {
            function Up(t, e) {
                if (e === void 0) { e = {}; }
                this.table = t, this.startRow = e.startRow || 0, this.endRow = "number" == typeof e.endRow ? e.endRow : void 0, this.includeSpanned = !!e.includeSpanned, this.column = "number" == typeof e.column ? e.column : void 0, this._skipRows = new Set, this._row = 0, this._column = 0, this._cell = 0, this._spannedCells = new Map;
            }
            Up.prototype[Symbol.iterator] = function () { return this; };
            Up.prototype.next = function () { var t = this.table.getChild(this._row); if (!t || this._isOverEndRow())
                return { done: !0 }; if (this._isSpanned(this._row, this._column)) {
                var t_355 = this._column, e_277 = this._formatOutValue(void 0, t_355);
                return this._column++, !this.includeSpanned || this._shouldSkipRow() || this._shouldSkipColumn(t_355, 1) ? this.next() : e_277;
            } var e = t.getChild(this._cell); if (!e)
                return this._row++, this._column = 0, this._cell = 0, this.next(); var n = parseInt(e.getAttribute("colspan") || 1), i = parseInt(e.getAttribute("rowspan") || 1); (n > 1 || i > 1) && this._recordSpans(this._row, this._column, i, n); var o = this._column, r = this._formatOutValue(e, o, i, n); return this._column++, this._cell++, this._shouldSkipRow() || this._shouldSkipColumn(o, n) ? this.next() : r; };
            Up.prototype.skipRow = function (t) { this._skipRows.add(t); };
            Up.prototype._isOverEndRow = function () { return void 0 !== this.endRow && this._row > this.endRow; };
            Up.prototype._formatOutValue = function (t, e, n, i) {
                if (n === void 0) { n = 1; }
                if (i === void 0) { i = 1; }
                return { done: !1, value: { cell: t, row: this._row, column: e, rowspan: n, colspan: i, cellIndex: this._cell } };
            };
            Up.prototype._shouldSkipRow = function () { var t = this._row < this.startRow, e = this._skipRows.has(this._row); return t || e; };
            Up.prototype._shouldSkipColumn = function (t, e) { if (void 0 === this.column)
                return !1; var n = t === this.column, i = t < this.column && t + e > this.column; return !n && !i; };
            Up.prototype._isSpanned = function (t, e) { if (!this._spannedCells.has(t))
                return !1; return this._spannedCells.get(t).has(e); };
            Up.prototype._recordSpans = function (t, e, n, i) { for (var n_269 = e + 1; n_269 <= e + i - 1; n_269++)
                this._markSpannedCell(t, n_269); for (var o_111 = t + 1; o_111 < t + n; o_111++)
                for (var t_356 = e; t_356 <= e + i - 1; t_356++)
                    this._markSpannedCell(o_111, t_356); };
            Up.prototype._markSpannedCell = function (t, e) { this._spannedCells.has(t) || this._spannedCells.set(t, new Map), this._spannedCells.get(t).set(e, !0); };
            return Up;
        }());
        function Hp(t) { return !!t.getCustomProperty("table") && vf(t); }
        function qp(t) { var e = t.getSelectedElement(); return e && Hp(e) ? e : null; }
        function Wp(t) { var e = Lp("table", t.getFirstPosition()); return e && Hp(e.parent) ? e.parent : null; }
        function Yp(t) {
            if (t === void 0) { t = {}; }
            return function (e) { return e.on("insert:table", function (e, n, i) { var o = n.item; if (!i.consumable.consume(o, "insert"))
                return; i.consumable.consume(o, "attribute:headingRows:table"), i.consumable.consume(o, "attribute:headingColumns:table"); var r = t && t.asWidget, s = i.writer.createContainerElement("figure", { class: "table" }), a = i.writer.createContainerElement("table"); var c; i.writer.insert(i.writer.createPositionAt(s, 0), a), r && (c = function (t, e) { return e.setCustomProperty("table", !0, t), yf(t, e, { hasSelectionHandler: !0 }); }(s, i.writer)); var l = new Up(o), d = { headingRows: o.getAttribute("headingRows") || 0, headingColumns: o.getAttribute("headingColumns") || 0 }, u = new Map; for (var _j = 0, l_4 = l; _j < l_4.length; _j++) {
                var e_278 = l_4[_j];
                var n_270 = e_278.row, r_59 = e_278.cell, s_34 = ib(nb(n_270, d), a, i), c_12 = o.getChild(n_270), l_5 = u.get(n_270) || tb(c_12, n_270, s_34, i);
                u.set(n_270, l_5), i.consumable.consume(r_59, "insert"), Xp(e_278, d, i.writer.createPositionAt(l_5, "end"), i, t);
            } var h = i.mapper.toViewPosition(n.range.start); i.mapper.bindElements(o, r ? c : s), i.writer.insert(h, r ? c : s); }); };
        }
        function $p(t) {
            if (t === void 0) { t = {}; }
            return function (e) { return e.on("insert:tableRow", function (e, n, i) { var o = n.item; if (!i.consumable.consume(o, "insert"))
                return; var r = o.parent, s = ab(i.mapper.toViewElement(r)), a = r.getChildIndex(o), c = new Up(r, { startRow: a, endRow: a }), l = { headingRows: r.getAttribute("headingRows") || 0, headingColumns: r.getAttribute("headingColumns") || 0 }, d = new Map; for (var _j = 0, c_13 = c; _j < c_13.length; _j++) {
                var e_279 = c_13[_j];
                var n_271 = ib(nb(a, l), s, i), r_60 = d.get(a) || tb(o, a, n_271, i);
                d.set(a, r_60), i.consumable.consume(e_279.cell, "insert"), Xp(e_279, l, i.writer.createPositionAt(r_60, "end"), i, t);
            } }); };
        }
        function Gp(t) {
            if (t === void 0) { t = {}; }
            return function (e) { return e.on("insert:tableCell", function (e, n, i) { var o = n.item; if (!i.consumable.consume(o, "insert"))
                return; var r = o.parent, s = r.parent, a = s.getChildIndex(r), c = new Up(s, { startRow: a, endRow: a }), l = { headingRows: s.getAttribute("headingRows") || 0, headingColumns: s.getAttribute("headingColumns") || 0 }; for (var _j = 0, c_14 = c; _j < c_14.length; _j++) {
                var e_280 = c_14[_j];
                if (e_280.cell === o) {
                    var n_272 = i.mapper.toViewElement(r);
                    return void Xp(e_280, l, i.writer.createPositionAt(n_272, r.getChildIndex(o)), i, t);
                }
            } }); };
        }
        function Qp(t) {
            if (t === void 0) { t = {}; }
            var e = !!t.asWidget;
            return function (t) { return t.on("attribute:headingRows:table", function (t, n, i) { var o = n.item; if (!i.consumable.consume(n.item, t.name))
                return; var r = ab(i.mapper.toViewElement(o)), s = n.attributeOldValue, a = n.attributeNewValue; if (a > s) {
                var t_359 = Array.from(o.getChildren()).filter(function (_j) {
                    var t = _j.index;
                    return c(t, s - 1, a);
                });
                sb(t_359, ib("thead", r, i), i, "end");
                for (var _j = 0, t_357 = t_359; _j < t_357.length; _j++) {
                    var n_273 = t_357[_j];
                    for (var _k = 0, _q = n_273.getChildren(); _k < _q.length; _k++) {
                        var t_360 = _q[_k];
                        Jp(t_360, "th", i, e);
                    }
                }
                rb("tbody", r, i);
            }
            else {
                sb(Array.from(o.getChildren()).filter(function (_j) {
                    var t = _j.index;
                    return c(t, a - 1, s);
                }).reverse(), ib("tbody", r, i), i, 0);
                var t_361 = new Up(o, { startRow: a ? a - 1 : a, endRow: s - 1 }), n_274 = { headingRows: o.getAttribute("headingRows") || 0, headingColumns: o.getAttribute("headingColumns") || 0 };
                for (var _v = 0, t_358 = t_361; _v < t_358.length; _v++) {
                    var o_112 = t_358[_v];
                    Zp(o_112, n_274, i, e);
                }
                rb("thead", r, i);
            } function c(t, e, n) { return t > e && t < n; } }); };
        }
        function Kp(t) {
            if (t === void 0) { t = {}; }
            var e = !!t.asWidget;
            return function (t) { return t.on("attribute:headingColumns:table", function (t, n, i) { var o = n.item; if (!i.consumable.consume(n.item, t.name))
                return; var r = { headingRows: o.getAttribute("headingRows") || 0, headingColumns: o.getAttribute("headingColumns") || 0 }, s = n.attributeOldValue, a = n.attributeNewValue, c = (s > a ? s : a) - 1; for (var _j = 0, _k = new Up(o); _j < _k.length; _j++) {
                var t_362 = _k[_j];
                t_362.column > c || Zp(t_362, r, i, e);
            } }); };
        }
        function Jp(t, e, n, i) { var o = n.writer, r = n.mapper.toViewElement(t); if (!r)
            return; var s; if (i) {
            s = Af(o.createEditableElement(e, r.getAttributes()), o), o.insert(o.createPositionAfter(r), s), o.move(o.createRangeIn(r), o.createPositionAt(s, 0)), o.remove(o.createRangeOn(r));
        }
        else
            s = o.rename(e, r); n.mapper.bindElements(t, s); }
        function Zp(t, e, n, i) { var o = t.cell, r = eb(t, e), s = n.mapper.toViewElement(o); s && s.name !== r && Jp(o, r, n, i); }
        function Xp(t, e, n, i, o) { var r = o && o.asWidget, s = eb(t, e), a = r ? Af(i.writer.createEditableElement(s), i.writer) : i.writer.createContainerElement(s), c = t.cell, l = 1 === c.childCount && "paragraph" === c.getChild(0).name; if (i.writer.insert(n, a), l) {
            var t_363 = c.getChild(0), e_281 = i.writer.createPositionAt(a, "end");
            if (i.consumable.consume(t_363, "insert"), o.asWidget) {
                var n_275 = t_363.getAttributeKeys().slice().length ? "p" : "span", o_113 = i.writer.createContainerElement(n_275);
                i.mapper.bindElements(t_363, o_113), i.writer.insert(e_281, o_113), i.mapper.bindElements(c, a);
            }
            else
                i.mapper.bindElements(c, a), i.mapper.bindElements(t_363, a);
        }
        else
            i.mapper.bindElements(c, a); }
        function tb(t, e, n, i) { i.consumable.consume(t, "insert"); var o = i.writer.createContainerElement("tr"); i.mapper.bindElements(t, o); var r = t.parent.getAttribute("headingRows") || 0, s = r > 0 && e >= r ? e - r : e, a = i.writer.createPositionAt(n, s); return i.writer.insert(a, o), o; }
        function eb(t, e) { var n = t.row, i = t.column, o = e.headingColumns, r = e.headingRows; return r && r > n ? "th" : o && o > i ? "th" : "td"; }
        function nb(t, e) { return t < e.headingRows ? "thead" : "tbody"; }
        function ib(t, e, n) { var i = ob(t, e); return i || function (t, e, n) { var i = n.writer.createContainerElement(t), o = n.writer.createPositionAt(e, "tbody" == t ? "end" : 0); return n.writer.insert(o, i), i; }(t, e, n); }
        function ob(t, e) { for (var _j = 0, _k = e.getChildren(); _j < _k.length; _j++) {
            var n_276 = _k[_j];
            if (n_276.name == t)
                return n_276;
        } }
        function rb(t, e, n) { var i = ob(t, e); i && 0 === i.childCount && n.writer.remove(n.writer.createRangeOn(i)); }
        function sb(t, e, n, i) { for (var _j = 0, t_364 = t; _j < t_364.length; _j++) {
            var o_114 = t_364[_j];
            var t_365 = n.mapper.toViewElement(o_114);
            t_365 && n.writer.move(n.writer.createRangeOn(t_365), n.writer.createPositionAt(e, i));
        } }
        function ab(t) { for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
            var e_282 = _k[_j];
            if ("table" === e_282.name)
                return e_282;
        } }
        var cb = /** @class */ (function (_super) {
            __extends(cb, _super);
            function cb() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            cb.prototype.refresh = function () { var t = this.editor.model, e = t.document.selection, n = t.schema, i = function (t) { var e = t.parent; return e === e.root ? e : e.parent; }(e.getFirstPosition()); this.isEnabled = n.checkChild(i, "table"); };
            cb.prototype.execute = function (t) {
                if (t === void 0) { t = {}; }
                var e = this.editor.model, n = e.document.selection, i = this.editor.plugins.get("TableUtils"), o = parseInt(t.rows) || 2, r = parseInt(t.columns) || 2, s = Cf(n, e);
                e.change(function (t) { var n = i.createTable(t, o, r); e.insertContent(n, s), t.setSelection(t.createPositionAt(n.getNodeByPath([0, 0, 0]), 0)); });
            };
            return cb;
        }(Wl));
        var lb = /** @class */ (function (_super) {
            __extends(lb, _super);
            function lb(t, e) {
                if (e === void 0) { e = {}; }
                var _this = this;
                _this = _super.call(this, t) || this, _this.order = e.order || "below";
                return _this;
            }
            lb.prototype.refresh = function () { var t = Lp("table", this.editor.model.document.selection.getFirstPosition()); this.isEnabled = !!t; };
            lb.prototype.execute = function () { var t = this.editor, e = t.model.document.selection, n = t.plugins.get("TableUtils"), i = Lp("tableCell", e.getFirstPosition()).parent, o = i.parent, r = o.getChildIndex(i), s = "below" === this.order ? r + 1 : r; n.insertRows(o, { rows: 1, at: s }); };
            return lb;
        }(Wl));
        var db = /** @class */ (function (_super) {
            __extends(db, _super);
            function db(t, e) {
                if (e === void 0) { e = {}; }
                var _this = this;
                _this = _super.call(this, t) || this, _this.order = e.order || "right";
                return _this;
            }
            db.prototype.refresh = function () { var t = Lp("table", this.editor.model.document.selection.getFirstPosition()); this.isEnabled = !!t; };
            db.prototype.execute = function () { var t = this.editor, e = t.model.document.selection, n = t.plugins.get("TableUtils"), i = Lp("tableCell", e.getFirstPosition()), o = i.parent.parent, r = n.getCellLocation(i).column, s = "right" === this.order ? r + 1 : r; n.insertColumns(o, { columns: 1, at: s }); };
            return db;
        }(Wl));
        var ub = /** @class */ (function (_super) {
            __extends(ub, _super);
            function ub(t, e) {
                if (e === void 0) { e = {}; }
                var _this = this;
                _this = _super.call(this, t) || this, _this.direction = e.direction || "horizontally";
                return _this;
            }
            ub.prototype.refresh = function () { var t = Lp("tableCell", this.editor.model.document.selection.getFirstPosition()); this.isEnabled = !!t; };
            ub.prototype.execute = function () { var t = Lp("tableCell", this.editor.model.document.selection.getFirstPosition()), e = "horizontally" === this.direction, n = this.editor.plugins.get("TableUtils"); e ? n.splitCellHorizontally(t, 2) : n.splitCellVertically(t, 2); };
            return ub;
        }(Wl));
        var hb = /** @class */ (function (_super) {
            __extends(hb, _super);
            function hb(t, e) {
                var _this = this;
                _this = _super.call(this, t) || this, _this.direction = e.direction, _this.isHorizontal = "right" == _this.direction || "left" == _this.direction;
                return _this;
            }
            hb.prototype.refresh = function () { var t = this._getMergeableCell(); this.isEnabled = !!t, this.value = t; };
            hb.prototype.execute = function () {
                var _this = this;
                var t = this.editor.model, e = Lp("tableCell", t.document.selection.getFirstPosition()), n = this.value, i = this.direction;
                t.change(function (t) { var o = "right" == i || "down" == i, r = o ? e : n, s = o ? n : e, a = s.parent; !function (t, e, n) { fb(t) || (fb(e) && n.remove(n.createRangeIn(e)), n.move(n.createRangeIn(t), n.createPositionAt(e, "end"))); n.remove(t); }(s, r, t); var c = _this.isHorizontal ? "colspan" : "rowspan", l = parseInt(e.getAttribute(c) || 1), d = parseInt(n.getAttribute(c) || 1); t.setAttribute(c, l + d, r), t.setSelection(t.createRangeIn(r)), a.childCount || function (t, e) { var n = t.parent, i = n.getChildIndex(t); for (var _j = 0, _k = new Up(n, { endRow: i }); _j < _k.length; _j++) {
                    var _q = _k[_j], t_366 = _q.cell, o_115 = _q.row, r_61 = _q.rowspan;
                    var n_277 = o_115 + r_61 - 1 >= i;
                    n_277 && jp("rowspan", r_61 - 1, t_366, e);
                } e.remove(t); }(a, t); });
            };
            hb.prototype._getMergeableCell = function () { var t = Lp("tableCell", this.editor.model.document.selection.getFirstPosition()); if (!t)
                return; var e = this.editor.plugins.get("TableUtils"), n = this.isHorizontal ? function (t, e, n) { var i = "right" == e ? t.nextSibling : t.previousSibling; if (!i)
                return; var o = "right" == e ? t : i, r = "right" == e ? i : t, s = n.getCellLocation(o).column, a = n.getCellLocation(r).column, c = parseInt(o.getAttribute("colspan") || 1); return s + c === a ? i : void 0; }(t, this.direction, e) : function (t, e) { var n = t.parent, i = n.parent, o = i.getChildIndex(n); if ("down" == e && o === i.childCount - 1 || "up" == e && 0 === o)
                return; var r = parseInt(t.getAttribute("rowspan") || 1), s = i.getAttribute("headingRows") || 0; if (s && ("down" == e && o + r === s || "up" == e && o === s))
                return; var a = parseInt(t.getAttribute("rowspan") || 1), c = "down" == e ? o + a : o, l = new Up(i, { endRow: c }).slice(), d = l.find(function (e) { return e.cell === t; }).column, u = l.find(function (_j) {
                var t = _j.row, n = _j.rowspan, i = _j.column;
                return i === d && ("down" == e ? t === c : c === t + n);
            }); return u && u.cell; }(t, this.direction); if (!n)
                return; var i = this.isHorizontal ? "rowspan" : "colspan", o = parseInt(t.getAttribute(i) || 1); return parseInt(n.getAttribute(i) || 1) === o ? n : void 0; };
            return hb;
        }(Wl));
        function fb(t) { return 1 == t.childCount && t.getChild(0).is("paragraph") && t.getChild(0).isEmpty; }
        var mb = /** @class */ (function (_super) {
            __extends(mb, _super);
            function mb() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            mb.prototype.refresh = function () { var t = Lp("tableCell", this.editor.model.document.selection.getFirstPosition()); this.isEnabled = !!t && t.parent.parent.childCount > 1; };
            mb.prototype.execute = function () { var t = this.editor.model, e = Lp("tableCell", t.document.selection.getFirstPosition()).parent, n = e.parent, i = n.getChildIndex(e), o = n.getAttribute("headingRows") || 0; t.change(function (t) { o && i <= o && jp("headingRows", o - 1, n, t, 0); var r = new Up(n, { endRow: i }).slice(), s = new Map; r.filter(function (_j) {
                var t = _j.row, e = _j.rowspan;
                return t === i && e > 1;
            }).forEach(function (_j) {
                var t = _j.column, e = _j.cell, n = _j.rowspan;
                return s.set(t, { cell: e, rowspanToSet: n - 1 });
            }), r.filter(function (_j) {
                var t = _j.row, e = _j.rowspan;
                return t <= i - 1 && t + e > i;
            }).forEach(function (_j) {
                var e = _j.cell, n = _j.rowspan;
                return jp("rowspan", n - 1, e, t);
            }); var a = i + 1, c = new Up(n, { includeSpanned: !0, startRow: a, endRow: a }); var l; for (var _j = 0, _k = c.slice(); _j < _k.length; _j++) {
                var _q = _k[_j], e_283 = _q.row, i_175 = _q.column, o_116 = _q.cell;
                if (s.has(i_175)) {
                    var _v = s.get(i_175), o_117 = _v.cell, r_62 = _v.rowspanToSet, a_16 = l ? t.createPositionAfter(l) : t.createPositionAt(n.getChild(e_283), 0);
                    t.move(t.createRangeOn(o_117), a_16), jp("rowspan", r_62, o_117, t), l = o_117;
                }
                else
                    l = o_116;
            } t.remove(e); }); };
            return mb;
        }(Wl));
        var gb = /** @class */ (function (_super) {
            __extends(gb, _super);
            function gb() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            gb.prototype.refresh = function () { var t = this.editor, e = t.model.document.selection, n = t.plugins.get("TableUtils"), i = Lp("tableCell", e.getFirstPosition()); this.isEnabled = !!i && n.getColumns(i.parent.parent) > 1; };
            gb.prototype.execute = function () { var t = this.editor.model, e = Lp("tableCell", t.document.selection.getFirstPosition()), n = e.parent, i = n.parent, o = i.getAttribute("headingColumns") || 0, r = i.getChildIndex(n), s = new Up(i).slice(), a = s.find(function (t) { return t.cell === e; }).column; t.change(function (t) { o && r <= o && t.setAttribute("headingColumns", o - 1, i); for (var _j = 0, s_35 = s; _j < s_35.length; _j++) {
                var _k = s_35[_j], e_284 = _k.cell, n_278 = _k.column, i_176 = _k.colspan;
                n_278 <= a && i_176 > 1 && n_278 + i_176 > a ? jp("colspan", i_176 - 1, e_284, t) : n_278 === a && t.remove(e_284);
            } }); };
            return gb;
        }(Wl));
        var pb = /** @class */ (function (_super) {
            __extends(pb, _super);
            function pb() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            pb.prototype.refresh = function () { var t = Lp("tableCell", this.editor.model.document.selection.getFirstPosition()), e = !!t; this.isEnabled = e, this.value = e && this._isInHeading(t, t.parent.parent); };
            pb.prototype.execute = function () { var t = this.editor.model, e = Lp("tableCell", t.document.selection.getFirstPosition()).parent, n = e.parent, i = n.getAttribute("headingRows") || 0, o = e.index, r = i > o ? o : o + 1; t.change(function (t) { if (r) {
                var e_286 = function (t, e, n) { var i = [], o = new Up(t, { startRow: e > n ? n : 0, endRow: e - 1 }); for (var _j = 0, o_118 = o; _j < o_118.length; _j++) {
                    var _k = o_118[_j], t_367 = _k.row, n_279 = _k.rowspan, r_63 = _k.cell;
                    n_279 > 1 && t_367 + n_279 > e && i.push(r_63);
                } return i; }(n, r, i);
                for (var _j = 0, e_285 = e_286; _j < e_285.length; _j++) {
                    var n_280 = e_285[_j];
                    bb(n_280, r, t);
                }
            } jp("headingRows", r, n, t, 0); }); };
            pb.prototype._isInHeading = function (t, e) { var n = parseInt(e.getAttribute("headingRows") || 0); return !!n && t.parent.index < n; };
            return pb;
        }(Wl));
        function bb(t, e, n) { var i = t.parent, o = i.parent, r = e - i.index, s = {}, a = parseInt(t.getAttribute("rowspan")) - r; a > 1 && (s.rowspan = a); var c = o.getChildIndex(i), l = c + r, d = new Up(o, { startRow: c, endRow: l, includeSpanned: !0 }).slice(); var u; for (var _j = 0, d_3 = d; _j < d_3.length; _j++) {
            var _k = d_3[_j], e_287 = _k.row, i_177 = _k.column, r_64 = _k.cell, a_17 = _k.colspan, c_15 = _k.cellIndex;
            if (r_64 === t && (u = i_177, a_17 > 1 && (s.colspan = a_17)), void 0 !== u && u === i_177 && e_287 === l) {
                var t_368 = o.getChild(e_287);
                Vp(n, n.createPositionAt(t_368, c_15), s);
            }
        } jp("rowspan", r, t, n); }
        var wb = /** @class */ (function (_super) {
            __extends(wb, _super);
            function wb() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            wb.prototype.refresh = function () { var t = Lp("tableCell", this.editor.model.document.selection.getFirstPosition()), e = !!t; this.isEnabled = e, this.value = e && this._isInHeading(t, t.parent.parent); };
            wb.prototype.execute = function () { var t = this.editor.model, e = t.document.selection, n = this.editor.plugins.get("TableUtils"), i = Lp("tableCell", e.getFirstPosition().parent), o = i.parent.parent, r = parseInt(o.getAttribute("headingColumns") || 0), s = n.getCellLocation(i).column, a = r > s ? s : s + 1; t.change(function (t) { jp("headingColumns", a, o, t, 0); }); };
            wb.prototype._isInHeading = function (t, e) { var n = parseInt(e.getAttribute("headingColumns") || 0), i = this.editor.plugins.get("TableUtils"), o = i.getCellLocation(t).column; return !!n && o < n; };
            return wb;
        }(Wl));
        var _b = /** @class */ (function (_super) {
            __extends(_b, _super);
            function _b() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Object.defineProperty(_b, "pluginName", {
                get: function () { return "TableUtils"; },
                enumerable: true,
                configurable: true
            });
            _b.prototype.getCellLocation = function (t) { var e = t.parent, n = e.parent, i = n.getChildIndex(e), o = new Up(n, { startRow: i, endRow: i }); for (var _j = 0, o_119 = o; _j < o_119.length; _j++) {
                var _k = o_119[_j], e_288 = _k.cell, n_281 = _k.row, i_178 = _k.column;
                if (e_288 === t)
                    return { row: n_281, column: i_178 };
            } };
            _b.prototype.createTable = function (t, e, n) { var i = t.createElement("table"); return kb(t, i, 0, e, n), i; };
            _b.prototype.insertRows = function (t, e) {
                var _this = this;
                if (e === void 0) { e = {}; }
                var n = this.editor.model, i = e.at || 0, o = e.rows || 1;
                n.change(function (e) { var n = t.getAttribute("headingRows") || 0; if (n > i && e.setAttribute("headingRows", n + o, t), 0 === i || i === t.childCount)
                    return void kb(e, t, i, o, _this.getColumns(t)); var r = new Up(t, { endRow: i }); var s = 0; for (var _j = 0, r_65 = r; _j < r_65.length; _j++) {
                    var _k = r_65[_j], t_369 = _k.row, n_282 = _k.rowspan, a_18 = _k.colspan, c_16 = _k.cell;
                    t_369 < i && t_369 + n_282 > i && e.setAttribute("rowspan", n_282 + o, c_16), t_369 === i && (s += a_18);
                } kb(e, t, i, o, s); });
            };
            _b.prototype.insertColumns = function (t, e) {
                var _this = this;
                if (e === void 0) { e = {}; }
                var n = this.editor.model, i = e.at || 0, o = e.columns || 1;
                n.change(function (e) { var n = t.getAttribute("headingColumns"); i < n && e.setAttribute("headingColumns", n + o, t); var r = _this.getColumns(t); if (0 === i || r === i) {
                    for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
                        var n_283 = _k[_j];
                        vb(o, e, e.createPositionAt(n_283, i ? "end" : 0));
                    }
                    return;
                } var s = new Up(t, { column: i, includeSpanned: !0 }); for (var _q = 0, s_36 = s; _q < s_36.length; _q++) {
                    var _v = s_36[_q], n_284 = _v.row, r_66 = _v.column, a_19 = _v.cell, c_17 = _v.colspan, l_6 = _v.rowspan, d_4 = _v.cellIndex;
                    if (r_66 !== i) {
                        if (e.setAttribute("colspan", c_17 + o, a_19), s.skipRow(n_284), l_6 > 1)
                            for (var t_370 = n_284 + 1; t_370 < n_284 + l_6; t_370++)
                                s.skipRow(t_370);
                    }
                    else {
                        var i_179 = e.createPositionAt(t.getChild(n_284), d_4);
                        vb(o, e, i_179);
                    }
                } });
            };
            _b.prototype.splitCellVertically = function (t, e) {
                if (e === void 0) { e = 2; }
                var n = this.editor.model, i = t.parent.parent, o = parseInt(t.getAttribute("rowspan") || 1), r = parseInt(t.getAttribute("colspan") || 1);
                n.change(function (n) { if (r > 1) {
                    var _j = yb(r, e), i_180 = _j.newCellsSpan, s_37 = _j.updatedSpan;
                    jp("colspan", s_37, t, n);
                    var a_20 = {};
                    i_180 > 1 && (a_20.colspan = i_180), o > 1 && (a_20.rowspan = o), vb(r > e ? e - 1 : r - 1, n, n.createPositionAfter(t), a_20);
                } if (r < e) {
                    var s_38 = e - r, a_21 = new Up(i).slice(), c_18 = a_21.find(function (_j) {
                        var e = _j.cell;
                        return e === t;
                    }).column, l_8 = a_21.filter(function (_j) {
                        var e = _j.cell, n = _j.colspan, i = _j.column;
                        return e !== t && i === c_18 || i < c_18 && i + n > c_18;
                    });
                    for (var _k = 0, l_7 = l_8; _k < l_7.length; _k++) {
                        var _q = l_7[_k], t_371 = _q.cell, e_289 = _q.colspan;
                        n.setAttribute("colspan", e_289 + s_38, t_371);
                    }
                    var d_5 = {};
                    o > 1 && (d_5.rowspan = o), vb(s_38, n, n.createPositionAfter(t), d_5);
                    var u_2 = i.getAttribute("headingColumns") || 0;
                    u_2 > c_18 && jp("headingColumns", u_2 + s_38, i, n);
                } });
            };
            _b.prototype.splitCellHorizontally = function (t, e) {
                if (e === void 0) { e = 2; }
                var n = this.editor.model, i = t.parent, o = i.parent, r = o.getChildIndex(i), s = parseInt(t.getAttribute("rowspan") || 1), a = parseInt(t.getAttribute("colspan") || 1);
                n.change(function (n) { if (s > 1) {
                    var i_182 = new Up(o, { startRow: r, endRow: r + s - 1, includeSpanned: !0 }).slice(), _j = yb(s, e), c_20 = _j.newCellsSpan, l_9 = _j.updatedSpan;
                    jp("rowspan", l_9, t, n);
                    var d_6 = i_182.find(function (_j) {
                        var e = _j.cell;
                        return e === t;
                    }).column, u_3 = {};
                    c_20 > 1 && (u_3.rowspan = c_20), a > 1 && (u_3.colspan = a);
                    for (var _k = 0, i_181 = i_182; _k < i_181.length; _k++) {
                        var _q = i_181[_k], t_372 = _q.column, e_290 = _q.row, s_39 = _q.cellIndex;
                        if (e_290 >= r + l_9 && t_372 === d_6 && (e_290 + r + l_9) % c_20 == 0) {
                            vb(1, n, n.createPositionAt(o.getChild(e_290), s_39), u_3);
                        }
                    }
                } if (s < e) {
                    var i_183 = e - s, c_21 = new Up(o, { startRow: 0, endRow: r }).slice();
                    for (var _v = 0, c_19 = c_21; _v < c_19.length; _v++) {
                        var _w = c_19[_v], e_291 = _w.cell, o_120 = _w.rowspan, s_40 = _w.row;
                        if (e_291 !== t && s_40 + o_120 > r) {
                            var t_373 = o_120 + i_183;
                            n.setAttribute("rowspan", t_373, e_291);
                        }
                    }
                    var l_10 = {};
                    a > 1 && (l_10.colspan = a), kb(n, o, r + 1, i_183, 1, l_10);
                    var d_7 = o.getAttribute("headingRows") || 0;
                    d_7 > r && jp("headingRows", d_7 + i_183, o, n);
                } });
            };
            _b.prototype.getColumns = function (t) { return t.getChild(0).getChildren().slice().reduce(function (t, e) { return t + parseInt(e.getAttribute("colspan") || 1); }, 0); };
            return _b;
        }(Bl));
        function kb(t, e, n, i, o, r) {
            if (r === void 0) { r = {}; }
            for (var s_41 = 0; s_41 < i; s_41++) {
                var i_184 = t.createElement("tableRow");
                t.insert(i_184, e, n), vb(o, t, t.createPositionAt(i_184, "end"), r);
            }
        }
        function vb(t, e, n, i) {
            if (i === void 0) { i = {}; }
            for (var o_121 = 0; o_121 < t; o_121++)
                Vp(e, n, i);
        }
        function yb(t, e) { if (t < e)
            return { newCellsSpan: 1, updatedSpan: 1 }; var n = Math.floor(t / e); return { newCellsSpan: n, updatedSpan: t - n * e + n }; }
        function xb(t) { t.document.registerPostFixer(function (e) { return (function (t, e) { var n = e.document.differ.getChanges(); var i = !1; var o = new Set; for (var _j = 0, n_285 = n; _j < n_285.length; _j++) {
            var e_292 = n_285[_j];
            var n_286 = void 0;
            "table" == e_292.name && "insert" == e_292.type && (n_286 = e_292.position.nodeAfter), "tableRow" != e_292.name && "tableCell" != e_292.name || (n_286 = Lp("table", e_292.position)), Tb(e_292) && (n_286 = Lp("table", e_292.range.start)), n_286 && !o.has(n_286) && (i = Ab(n_286, t) || i, i = Cb(n_286, t) || i, o.add(n_286));
        } return i; })(e, t); }); }
        function Ab(t, e) { var n = !1; var i = function (t) { var e = parseInt(t.getAttribute("headingRows") || 0), n = t.childCount, i = []; for (var _j = 0, _k = new Up(t); _j < _k.length; _j++) {
            var _q = _k[_j], o_122 = _q.row, r_67 = _q.rowspan, s_42 = _q.cell;
            if (r_67 < 2)
                continue;
            var t_374 = o_122 < e, a_22 = t_374 ? e : n;
            if (o_122 + r_67 > a_22) {
                var t_375 = a_22 - o_122;
                i.push({ cell: s_42, rowspan: t_375 });
            }
        } return i; }(t); if (i.length) {
            n = !0;
            for (var _j = 0, i_185 = i; _j < i_185.length; _j++) {
                var t_376 = i_185[_j];
                jp("rowspan", t_376.rowspan, t_376.cell, e, 1);
            }
        } return n; }
        function Cb(t, e) { var n = !1; var i = function (t) { var e = {}; for (var _j = 0, _k = new Up(t, { includeSpanned: !0 }); _j < _k.length; _j++) {
            var n_287 = _k[_j].row;
            e[n_287] || (e[n_287] = 0), e[n_287] += 1;
        } return e; }(t), o = i[0]; if (!Object.values(i).every(function (t) { return t === o; })) {
            var o_123 = Object.values(i).reduce(function (t, e) { return e > t ? e : t; }, 0);
            for (var _j = 0, _k = Object.entries(i); _j < _k.length; _j++) {
                var _q = _k[_j], r_68 = _q[0], s_43 = _q[1];
                var i_186 = o_123 - s_43;
                if (i_186) {
                    for (var n_288 = 0; n_288 < i_186; n_288++)
                        Vp(e, e.createPositionAt(t.getChild(r_68), "end"));
                    n = !0;
                }
            }
        } return n; }
        function Tb(t) { var e = "attribute" === t.type, n = t.attributeKey; return e && ("headingRows" === n || "colspan" === n || "rowspan" === n); }
        function Pb(t) { t.document.registerPostFixer(function (e) { return (function (t, e) { var n = e.document.differ.getChanges(); var i = !1; for (var _j = 0, n_289 = n; _j < n_289.length; _j++) {
            var e_293 = n_289[_j];
            "remove" == e_293.type && e_293.position.parent.is("tableCell") && (i = Sb(e_293.position.parent, t) || i), "insert" == e_293.type && ("table" == e_293.name && (i = Mb(e_293.position.nodeAfter, t) || i), "tableRow" == e_293.name && (i = Eb(e_293.position.nodeAfter, t) || i), "tableCell" == e_293.name && (i = Sb(e_293.position.nodeAfter, t) || i));
        } return i; })(e, t); }); }
        function Mb(t, e) { var n = !1; for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
            var i_187 = _k[_j];
            n = Eb(i_187, e) || n;
        } return n; }
        function Eb(t, e) { var n = !1; for (var _j = 0, _k = t.getChildren(); _j < _k.length; _j++) {
            var i_188 = _k[_j];
            n = Sb(i_188, e) || n;
        } return n; }
        function Sb(t, e) { if (0 == t.childCount)
            return e.insertElement("paragraph", t), !0; var n = Array.from(t.getChildren()).filter(function (t) { return t.is("text"); }); for (var _j = 0, n_290 = n; _j < n_290.length; _j++) {
            var t_377 = n_290[_j];
            e.wrap(e.createRangeOn(t_377), "paragraph");
        } return !!n.length; }
        function Ib(t, e) { e.view.document.registerPostFixer(function (n) { return (function (t, e, n, i) { var o = !1; var r = function (t) { var e = Array.from(t._renderer.markedAttributes).filter(function (t) { return !!t.parent; }).filter(Ob).filter(function (t) { return Rb(t.parent); }), n = Array.from(t._renderer.markedChildren).filter(function (t) { return !!t.parent; }).filter(Rb).reduce(function (t, e) { var n = Array.from(e.getChildren()).filter(Ob); return t.concat(n); }, []); return e.concat(n); }(i); for (var _j = 0, r_69 = r; _j < r_69.length; _j++) {
            var e_294 = r_69[_j];
            o = Nb(e_294, n, t) || o;
        } o && function (t, e, n) { var i = Array.from(t.getRanges()).map(function (t) { return e.toViewRange(t); }); n.setSelection(i, { backward: t.isBackward }); }(e.document.selection, n, t); return o; })(n, t, e.mapper, e.view); }); }
        function Nb(t, e, n) { var i = e.toModelElement(t), o = function (t, e) { var n = t.childCount > 1, i = !!e.getAttributes().slice().length; return n || i ? "p" : "span"; }(i.parent, i); if (t.name !== o) {
            e.unbindViewElement(t);
            var r_70 = n.rename(o, t);
            return e.bindElements(i, r_70), !0;
        } return !1; }
        function Ob(t) { return t.is("p") || t.is("span"); }
        function Rb(t) { return t.is("td") || t.is("th"); }
        n(124);
        var Db = /** @class */ (function (_super) {
            __extends(Db, _super);
            function Db() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Db.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = t.model, n = e.schema, i = t.conversion;
                n.register("table", { allowWhere: "$block", allowAttributes: ["headingRows", "headingColumns"], isLimit: !0, isObject: !0, isBlock: !0 }), n.register("tableRow", { allowIn: "table", isLimit: !0 }), n.register("tableCell", { allowIn: "tableRow", allowAttributes: ["colspan", "rowspan"], isLimit: !0 }), n.extend("$block", { allowIn: "tableCell" }), n.addChildCheck(function (t, e) { if ("table" == e.name && Array.from(t.getNames()).includes("table"))
                    return !1; }), i.for("upcast").add(zp()), i.for("editingDowncast").add(Yp({ asWidget: !0 })), i.for("dataDowncast").add(Yp()), i.for("upcast").elementToElement({ model: "tableRow", view: "tr" }), i.for("editingDowncast").add($p({ asWidget: !0 })), i.for("dataDowncast").add($p()), i.for("downcast").add(function (t) { return t.on("remove:tableRow", function (t, e, n) { t.stop(); var i = n.writer, o = n.mapper, r = o.toViewPosition(e.position).getLastMatchingPosition(function (t) { return !t.item.is("tr"); }).nodeAfter, s = r.parent, a = i.createRangeOn(r), c = i.remove(a); for (var _j = 0, _k = i.createRangeIn(c).getItems(); _j < _k.length; _j++) {
                    var t_378 = _k[_j];
                    o.unbindViewElement(t_378);
                } s.childCount || i.remove(i.createRangeOn(s)); }, { priority: "higher" }); }), i.for("upcast").add(Bp("td")), i.for("upcast").add(Bp("th")), i.for("editingDowncast").add(Gp({ asWidget: !0 })), i.for("dataDowncast").add(Gp()), i.attributeToAttribute({ model: "colspan", view: "colspan" }), i.attributeToAttribute({ model: "rowspan", view: "rowspan" }), i.for("editingDowncast").add(Kp({ asWidget: !0 })), i.for("dataDowncast").add(Kp()), i.for("editingDowncast").add(Qp({ asWidget: !0 })), i.for("dataDowncast").add(Qp()), Ib(t.model, t.editing), t.commands.add("insertTable", new cb(t)), t.commands.add("insertTableRowAbove", new lb(t, { order: "above" })), t.commands.add("insertTableRowBelow", new lb(t, { order: "below" })), t.commands.add("insertTableColumnLeft", new db(t, { order: "left" })), t.commands.add("insertTableColumnRight", new db(t, { order: "right" })), t.commands.add("removeTableRow", new mb(t)), t.commands.add("removeTableColumn", new gb(t)), t.commands.add("splitTableCellVertically", new ub(t, { direction: "vertically" })), t.commands.add("splitTableCellHorizontally", new ub(t, { direction: "horizontally" })), t.commands.add("mergeTableCellRight", new hb(t, { direction: "right" })), t.commands.add("mergeTableCellLeft", new hb(t, { direction: "left" })), t.commands.add("mergeTableCellDown", new hb(t, { direction: "down" })), t.commands.add("mergeTableCellUp", new hb(t, { direction: "up" })), t.commands.add("setTableColumnHeader", new wb(t)), t.commands.add("setTableRowHeader", new pb(t)), xb(e), Pb(e), this.editor.keystrokes.set("Tab", function () {
                    var t = [];
                    for (var _j = 0; _j < arguments.length; _j++) {
                        t[_j] = arguments[_j];
                    }
                    return _this._handleTabOnSelectedTable.apply(_this, t);
                }, { priority: "low" }), this.editor.keystrokes.set("Tab", this._getTabHandler(!0), { priority: "low" }), this.editor.keystrokes.set("Shift+Tab", this._getTabHandler(!1), { priority: "low" });
            };
            Object.defineProperty(Db, "requires", {
                get: function () { return [_b]; },
                enumerable: true,
                configurable: true
            });
            Db.prototype._handleTabOnSelectedTable = function (t, e) { var n = this.editor, i = n.model.document.selection; if (!i.isCollapsed && 1 === i.rangeCount && i.getFirstRange().isFlat) {
                var t_379 = i.getSelectedElement();
                if (!t_379 || !t_379.is("table"))
                    return;
                e(), n.model.change(function (e) { e.setSelection(e.createRangeIn(t_379.getChild(0).getChild(0))); });
            } };
            Db.prototype._getTabHandler = function (t) { var e = this.editor; return function (n, i) { var o = Lp("tableCell", e.model.document.selection.getFirstPosition()); if (!o)
                return; i(); var r = o.parent, s = r.parent, a = s.getChildIndex(r), c = r.getChildIndex(o), l = 0 === c; if (!t && l && 0 === a)
                return; var d = c === r.childCount - 1, u = a === s.childCount - 1; var h; if (t && u && d && e.plugins.get("TableUtils").insertRows(s, { at: s.childCount }), t && d) {
                var t_380 = s.getChild(a + 1);
                h = t_380.getChild(0);
            }
            else if (!t && l) {
                var t_381 = s.getChild(a - 1);
                h = t_381.getChild(t_381.childCount - 1);
            }
            else
                h = r.getChild(c + (t ? 1 : -1)); e.model.change(function (t) { t.setSelection(t.createRangeIn(h)); }); }; };
            return Db;
        }(Bl));
        n(126);
        var Lb = /** @class */ (function (_super) {
            __extends(Lb, _super);
            function Lb(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.bindTemplate;
                _this.items = _this.createCollection(), _this.set("rows", 0), _this.set("columns", 0), _this.bind("label").to(_this, "columns", _this, "rows", function (t, e) { return e + " x " + t; }), _this.setTemplate({ tag: "div", attributes: { class: ["ck"] }, children: [{ tag: "div", attributes: { class: ["ck-insert-table-dropdown__grid"] }, children: _this.items }, { tag: "div", attributes: { class: ["ck-insert-table-dropdown__label"] }, children: [{ text: e.to("label") }] }], on: { mousedown: e.to(function (t) { t.preventDefault(); }), click: e.to(function () { _this.fire("execute"); }) } });
                var _loop_9 = function (t_382) {
                    var e_295 = new jb;
                    e_295.on("over", function () { var e = Math.floor(t_382 / 10), n = t_382 % 10; _this.set("rows", e + 1), _this.set("columns", n + 1); }), this_2.items.add(e_295);
                };
                var this_2 = this;
                for (var t_382 = 0; t_382 < 100; t_382++) {
                    _loop_9(t_382);
                }
                _this.on("change:columns", function () { _this._highlightGridBoxes(); }), _this.on("change:rows", function () { _this._highlightGridBoxes(); });
                return _this;
            }
            Lb.prototype.focus = function () { };
            Lb.prototype.focusLast = function () { };
            Lb.prototype._highlightGridBoxes = function () { var t = this.rows, e = this.columns; this.items.map(function (n, i) { var o = Math.floor(i / 10) < t && i % 10 < e; n.set("isOn", o); }); };
            return Lb;
        }(Sl));
        var jb = /** @class */ (function (_super) {
            __extends(jb, _super);
            function jb(t) {
                var _this = _super.call(this, t) || this;
                var e = _this.bindTemplate;
                _this.set("isOn", !1), _this.setTemplate({ tag: "div", attributes: { class: ["ck-insert-table-dropdown-grid-box", e.if("isOn", "ck-on")] }, on: { mouseover: e.to("over") } });
                return _this;
            }
            return jb;
        }(Sl));
        var Vb = n(49), zb = n.n(Vb), Bb = n(50), Fb = n.n(Bb), Ub = n(51), Hb = n.n(Ub), qb = n(52), Wb = n.n(qb);
        var Yb = /** @class */ (function (_super) {
            __extends(Yb, _super);
            function Yb() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            Yb.prototype.init = function () {
                var _this = this;
                var t = this.editor, e = this.editor.t;
                t.ui.componentFactory.add("insertTable", function (n) { var i = t.commands.get("insertTable"), o = du(n); o.bind("isEnabled").to(i), o.buttonView.set({ icon: zb.a, label: e("af"), tooltip: !0 }); var r = new Lb(n); return o.panelView.children.add(r), r.delegate("execute").to(o), o.buttonView.on("open", function () { r.rows = 0, r.columns = 0; }), o.on("execute", function () { t.execute("insertTable", { rows: r.rows, columns: r.columns }), t.editing.view.focus(); }), o; }), t.ui.componentFactory.add("tableColumn", function (t) { var n = [{ type: "switchbutton", model: { commandName: "setTableColumnHeader", label: e("ag"), bindIsOn: !0 } }, { type: "separator" }, { type: "button", model: { commandName: "insertTableColumnLeft", label: e("ah") } }, { type: "button", model: { commandName: "insertTableColumnRight", label: e("ai") } }, { type: "button", model: { commandName: "removeTableColumn", label: e("aj") } }]; return _this._prepareDropdown(e("ak"), Fb.a, n, t); }), t.ui.componentFactory.add("tableRow", function (t) { var n = [{ type: "switchbutton", model: { commandName: "setTableRowHeader", label: e("al"), bindIsOn: !0 } }, { type: "separator" }, { type: "button", model: { commandName: "insertTableRowBelow", label: e("am") } }, { type: "button", model: { commandName: "insertTableRowAbove", label: e("an") } }, { type: "button", model: { commandName: "removeTableRow", label: e("ao") } }]; return _this._prepareDropdown(e("ap"), Hb.a, n, t); }), t.ui.componentFactory.add("mergeTableCells", function (t) { var n = [{ type: "button", model: { commandName: "mergeTableCellUp", label: e("aq") } }, { type: "button", model: { commandName: "mergeTableCellRight", label: e("ar") } }, { type: "button", model: { commandName: "mergeTableCellDown", label: e("as") } }, { type: "button", model: { commandName: "mergeTableCellLeft", label: e("at") } }, { type: "separator" }, { type: "button", model: { commandName: "splitTableCellVertically", label: e("au") } }, { type: "button", model: { commandName: "splitTableCellHorizontally", label: e("av") } }]; return _this._prepareDropdown(e("aw"), Wb.a, n, t); });
            };
            Yb.prototype._prepareDropdown = function (t, e, n, i) { var o = this.editor, r = du(i), s = [], a = new oo; for (var _j = 0, n_291 = n; _j < n_291.length; _j++) {
                var t_383 = n_291[_j];
                $b(t_383, o, s, a);
            } return hu(r, a), r.buttonView.set({ label: t, icon: e, tooltip: !0 }), r.bind("isEnabled").toMany(s, "isEnabled", function () {
                var t = [];
                for (var _j = 0; _j < arguments.length; _j++) {
                    t[_j] = arguments[_j];
                }
                return t.some(function (t) { return t; });
            }), this.listenTo(r, "execute", function (t) { o.execute(t.source.commandName), o.editing.view.focus(); }), r; };
            return Yb;
        }(Bl));
        function $b(t, e, n, i) { var o = t.model = new Iu(t.model), _j = t.model, r = _j.commandName, s = _j.bindIsOn; if ("separator" !== t.type) {
            var t_384 = e.commands.get(r);
            n.push(t_384), o.set({ commandName: r }), o.bind("isEnabled").to(t_384), s && o.bind("isOn").to(t_384, "value");
        } o.set({ withText: !0 }), i.add(t); }
        n(128);
        n.d(e, "default", function () { return Gb; });
        var Gb = /** @class */ (function (_super) {
            __extends(Gb, _super);
            function Gb() {
                return _super !== null && _super.apply(this, arguments) || this;
            }
            return Gb;
        }(zl));
        Gb.builtinPlugins = [/** @class */ (function (_super) {
                __extends(class_1, _super);
                function class_1() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_1, "requires", {
                    get: function () { return [ql, Kl, td, bd, Hd]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_1, "pluginName", {
                    get: function () { return "Essentials"; },
                    enumerable: true,
                    configurable: true
                });
                return class_1;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_2, _super);
                function class_2() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_2, "requires", {
                    get: function () { return [Kd, yu]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_2, "pluginName", {
                    get: function () { return "Alignment"; },
                    enumerable: true,
                    configurable: true
                });
                return class_2;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_3, _super);
                function class_3() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_3, "requires", {
                    get: function () { return [Su, Ru]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_3, "pluginName", {
                    get: function () { return "FontSize"; },
                    enumerable: true,
                    configurable: true
                });
                return class_3;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_4, _super);
                function class_4() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_4, "requires", {
                    get: function () { return [Bu, Hu]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_4, "pluginName", {
                    get: function () { return "FontFamily"; },
                    enumerable: true,
                    configurable: true
                });
                return class_4;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_5, _super);
                function class_5() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_5, "requires", {
                    get: function () { return [Wu, Xu]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_5, "pluginName", {
                    get: function () { return "Highlight"; },
                    enumerable: true,
                    configurable: true
                });
                return class_5;
            }(Bl)), lh, /** @class */ (function (_super) {
                __extends(class_6, _super);
                function class_6() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_6, "pluginName", {
                    get: function () { return "Autoformat"; },
                    enumerable: true,
                    configurable: true
                });
                class_6.prototype.afterInit = function () { this._addListAutoformats(), this._addBasicStylesAutoformats(), this._addHeadingAutoformats(), this._addBlockQuoteAutoformats(); };
                class_6.prototype._addListAutoformats = function () { var t = this.editor.commands; t.get("bulletedList") && new uh(this.editor, /^[*-]\s$/, "bulletedList"), t.get("numberedList") && new uh(this.editor, /^\d+[.|)]\s$/, "numberedList"); };
                class_6.prototype._addBasicStylesAutoformats = function () { var t = this.editor.commands; if (t.get("bold")) {
                    var t_385 = mh(this.editor, "bold");
                    new hh(this.editor, /(\*\*)([^*]+)(\*\*)$/g, t_385), new hh(this.editor, /(__)([^_]+)(__)$/g, t_385);
                } if (t.get("italic")) {
                    var t_386 = mh(this.editor, "italic");
                    new hh(this.editor, /(?:^|[^*])(\*)([^*_]+)(\*)$/g, t_386), new hh(this.editor, /(?:^|[^_])(_)([^_]+)(_)$/g, t_386);
                } if (t.get("code")) {
                    var t_387 = mh(this.editor, "code");
                    new hh(this.editor, /(`)([^`]+)(`)$/g, t_387);
                } };
                class_6.prototype._addHeadingAutoformats = function () {
                    var _this = this;
                    var t = this.editor.commands.get("heading");
                    t && t.modelElements.filter(function (t) { return t.match(/^heading[1-6]$/); }).forEach(function (e) { var n = e[7], i = new RegExp("^(#{" + n + "})\\s$"); new uh(_this.editor, i, function () { if (!t.isEnabled)
                        return !1; _this.editor.execute("heading", { value: e }); }); });
                };
                class_6.prototype._addBlockQuoteAutoformats = function () { this.editor.commands.get("blockQuote") && new uh(this.editor, /^>\s$/, "blockQuote"); };
                return class_6;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_7, _super);
                function class_7() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_7, "requires", {
                    get: function () { return [bh, vh]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_7, "pluginName", {
                    get: function () { return "Bold"; },
                    enumerable: true,
                    configurable: true
                });
                return class_7;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_8, _super);
                function class_8() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_8, "requires", {
                    get: function () { return [xh, Ph]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_8, "pluginName", {
                    get: function () { return "Italic"; },
                    enumerable: true,
                    configurable: true
                });
                return class_8;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_9, _super);
                function class_9() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_9, "requires", {
                    get: function () { return [Eh, Oh]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_9, "pluginName", {
                    get: function () { return "Strikethrough"; },
                    enumerable: true,
                    configurable: true
                });
                return class_9;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_10, _super);
                function class_10() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_10, "requires", {
                    get: function () { return [Dh, zh]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_10, "pluginName", {
                    get: function () { return "Underline"; },
                    enumerable: true,
                    configurable: true
                });
                return class_10;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_11, _super);
                function class_11() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_11, "requires", {
                    get: function () { return [qh, $h]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_11, "pluginName", {
                    get: function () { return "BlockQuote"; },
                    enumerable: true,
                    configurable: true
                });
                return class_11;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_12, _super);
                function class_12() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_12, "pluginName", {
                    get: function () { return "CKFinder"; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_12, "requires", {
                    get: function () { return [tf, Kh, lh]; },
                    enumerable: true,
                    configurable: true
                });
                return class_12;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_13, _super);
                function class_13() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_13, "requires", {
                    get: function () { return [lf, am, Pm]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_13, "pluginName", {
                    get: function () { return "EasyImage"; },
                    enumerable: true,
                    configurable: true
                });
                return class_13;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_14, _super);
                function class_14() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_14, "requires", {
                    get: function () { return [Lm, jm]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_14, "pluginName", {
                    get: function () { return "Heading"; },
                    enumerable: true,
                    configurable: true
                });
                return class_14;
            }(Bl)), am, /** @class */ (function (_super) {
                __extends(class_15, _super);
                function class_15() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_15, "requires", {
                    get: function () { return [Bm]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_15, "pluginName", {
                    get: function () { return "ImageCaption"; },
                    enumerable: true,
                    configurable: true
                });
                return class_15;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_16, _super);
                function class_16() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_16, "requires", {
                    get: function () { return [rg, sg]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_16, "pluginName", {
                    get: function () { return "ImageStyle"; },
                    enumerable: true,
                    configurable: true
                });
                return class_16;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_17, _super);
                function class_17() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_17, "requires", {
                    get: function () { return [ag]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_17, "pluginName", {
                    get: function () { return "ImageToolbar"; },
                    enumerable: true,
                    configurable: true
                });
                class_17.prototype.afterInit = function () { var t = this.editor; t.plugins.get(ag).register("image", { items: t.config.get("image.toolbar") || [], getRelatedElement: Pf }); };
                return class_17;
            }(Bl)), Pm, /** @class */ (function (_super) {
                __extends(class_18, _super);
                function class_18() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_18, "requires", {
                    get: function () { return [Ag, Dg]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_18, "pluginName", {
                    get: function () { return "Link"; },
                    enumerable: true,
                    configurable: true
                });
                return class_18;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_19, _super);
                function class_19() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_19, "requires", {
                    get: function () { return [rp, up]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_19, "pluginName", {
                    get: function () { return "List"; },
                    enumerable: true,
                    configurable: true
                });
                return class_19;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_20, _super);
                function class_20() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_20, "requires", {
                    get: function () { return [xp, Ep, Cp, Lf]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_20, "pluginName", {
                    get: function () { return "MediaEmbed"; },
                    enumerable: true,
                    configurable: true
                });
                return class_20;
            }(Bl)), Sm, /** @class */ (function (_super) {
                __extends(class_21, _super);
                function class_21() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_21, "pluginName", {
                    get: function () { return "PasteFromOffice"; },
                    enumerable: true,
                    configurable: true
                });
                class_21.prototype.init = function () {
                    var _this = this;
                    var t = this.editor;
                    this.listenTo(t.plugins.get(ql), "inputTransformation", function (t, e) { var n = e.dataTransfer.getData("text/html"); !0 !== e.pasteFromOfficeProcessed && function (t) { return !(!t || !t.match(/<meta\s*name="?generator"?\s*content="?microsoft\s*word\s*\d+"?\/?>/gi) && !t.match(/xmlns:o="urn:schemas-microsoft-com/gi)); }(n) && (e.content = _this._normalizeWordInput(n, e.dataTransfer), e.pasteFromOfficeProcessed = !0); }, { priority: "high" });
                };
                class_21.prototype._normalizeWordInput = function (t, e) { var _j = Ip(t), n = _j.body, i = _j.stylesString; return Np(n, i), Rp(n, e.getData("text/rtf")), n; };
                return class_21;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_22, _super);
                function class_22() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_22, "requires", {
                    get: function () { return [Db, Yb, Lf]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_22, "pluginName", {
                    get: function () { return "Table"; },
                    enumerable: true,
                    configurable: true
                });
                return class_22;
            }(Bl)), /** @class */ (function (_super) {
                __extends(class_23, _super);
                function class_23() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                Object.defineProperty(class_23, "requires", {
                    get: function () { return [ag]; },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(class_23, "pluginName", {
                    get: function () { return "TableToolbar"; },
                    enumerable: true,
                    configurable: true
                });
                class_23.prototype.afterInit = function () { var t = this.editor, e = t.plugins.get(ag), n = t.config.get("table.contentToolbar"), i = t.config.get("table.toolbar"), o = t.config.get("table.tableToolbar"); i && console.warn("`config.table.toolbar` is deprecated and will be removed in the next major release. Use `config.table.contentToolbar` instead."), (n || i) && e.register("tableContent", { items: n || i, getRelatedElement: Wp }), o && e.register("table", { items: o, getRelatedElement: qp }); };
                return class_23;
            }(Bl))], Gb.defaultConfig = { toolbar: { items: ["heading", "|", "fontsize", "fontfamily", "|", "bold", "italic", "underline", "strikethrough", "highlight", "|", "alignment", "|", "numberedList", "bulletedList", "|", "link", "blockquote", "imageUpload", "insertTable", "mediaEmbed", "|", "undo", "redo"] }, image: { styles: ["full", "alignLeft", "alignRight"], toolbar: ["imageStyle:alignLeft", "imageStyle:full", "imageStyle:alignRight", "|", "imageTextAlternative"] }, table: { contentToolbar: ["tableColumn", "tableRow", "mergeTableCells"] }, language: "en" };
    }]).default; });
//# sourceMappingURL=ckeditor.js.map
