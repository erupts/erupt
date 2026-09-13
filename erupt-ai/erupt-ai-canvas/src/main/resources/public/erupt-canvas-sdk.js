// Erupt View SDK — data access for AI-generated view pages.
// Loaded via <script src=".../erupt-canvas-sdk.js">; exposes window.Erupt.
// Handles the base path, the user token and the `erupt` auth header, so
// generated pages never hand-roll HTTP calls against the Erupt API.
(function () {
    'use strict';
    // Script URL: {base}/erupt-canvas-sdk.js — strip the file name to get the base
    var src = document.currentScript.src;
    var base = src.substring(0, src.lastIndexOf('/erupt-canvas-sdk.js'));
    // Token resolution order: injected by the html endpoint (works everywhere,
    // including srcdoc iframes) → URL param → admin frontend's localStorage
    var token = window.eruptToken || new URLSearchParams(location.search).get('_token') || '';
    if (!token) {
        try {
            var stored = localStorage.getItem('_token');
            if (stored) token = JSON.parse(stored).token || '';
        } catch (e) {
        }
    }

    // Runtime error relay: the designer embeds the page in an iframe and shows the
    // errors it receives so the user can ask for a fix. Harmless when the page is
    // viewed standalone (no parent) — nothing is sent. The SDK tag is the first in
    // <head>, so even errors thrown while the page boots are caught
    var reported = {};
    function report(kind, text) {
        if (window.parent === window || !text) return;
        text = String(text).slice(0, 1000);
        if (reported[text]) return;
        reported[text] = true;
        try {
            window.parent.postMessage({type: 'erupt-canvas-error', kind: kind, message: text}, '*');
        } catch (e) {
        }
    }
    window.addEventListener('error', function (e) {
        if (e.target && e.target !== window && (e.target.src || e.target.href)) {
            report('resource', 'Failed to load ' + (e.target.src || e.target.href));
        } else if (e.message) {
            report('error', e.message + (e.lineno ? ' (line ' + e.lineno + ')' : ''));
        }
    }, true);
    window.addEventListener('unhandledrejection', function (e) {
        var reason = e.reason;
        report('rejection', reason && reason.message ? reason.message : reason);
    });
    // Vue reports template compile errors and render failures through the console only.
    // The bundled Vue is the production build, which strips these warnings entirely, so this
    // hook is a safety net for pages that pull a development build from a CDN; pages booted
    // through Erupt.app also relay render errors via app.config.errorHandler
    ['warn', 'error'].forEach(function (level) {
        var original = console[level];
        console[level] = function () {
            var first = arguments[0];
            if (typeof first === 'string' && first.indexOf('[Vue warn]') === 0
                && /compil|not defined|Failed to resolve|Unhandled error|Invalid|Error/i.test(first)) {
                report('vue', first.split('\n')[0]);
            }
            return original.apply(console, arguments);
        };
    });

    function call(method, path, model, body) {
        return fetch(base + '/erupt-api' + path, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'token': token,
                'erupt': model
            },
            body: body ? JSON.stringify(body) : undefined
        }).then(function (resp) {
            if (!resp.ok) {
                return resp.text().then(function (text) {
                    throw new Error('Erupt API ' + resp.status + ': ' + text);
                });
            }
            return resp.json();
        });
    }

    // Normalize the query to the server contract; also accepts the Element Plus
    // el-table conventions ({prop, order: 'ascending'|'descending'}, conditionType)
    // so sort-change events can be passed through directly.
    function normalizeQuery(query) {
        query = query || {};
        var sort = (query.sort || []).map(function (s) {
            var direction = String(s.direction || s.order || 'ASC').toUpperCase();
            return {
                field: s.field || s.prop,
                direction: direction.indexOf('DESC') === 0 ? 'DESC' : 'ASC'
            };
        }).filter(function (s) { return s.field; });
        var condition = (query.condition || []).map(function (c) {
            return {key: c.key, value: c.value, expression: c.expression || c.conditionType || 'EQ'};
        }).filter(function (c) { return c.key; });
        return {
            pageIndex: query.pageIndex || 1,
            pageSize: query.pageSize || 20,
            sort: sort,
            condition: condition
        };
    }

    // Query string from an object, skipping null / empty values; '' when nothing is left
    function qs(params) {
        var parts = [];
        for (var key in params) {
            if (params[key] != null && params[key] !== '') parts.push(key + '=' + encodeURIComponent(params[key]));
        }
        return parts.length ? '?' + parts.join('&') : '';
    }

    // Modify endpoints answer with an R envelope; convert failures into rejections
    function unwrap(promise) {
        return promise.then(function (r) {
            if (r && r.success === false) throw new Error(r.message || 'Operation failed');
            return r;
        });
    }

    // Element Plus helpers live on the single `ElementPlus` global of the UMD bundle;
    // Erupt.app exposes them under their own names so pages can call ElMessage directly
    var ELEMENT_HELPERS = ['ElMessage', 'ElMessageBox', 'ElNotification', 'ElLoading'];

    // Boot a Vue 3 + Element Plus app with all the boilerplate the UMD bundles need:
    // createApp, use(ElementPlus), register every icon component when the icon bundle
    // is loaded, expose the Element Plus helpers as globals, relay render errors to the
    // designer, and mount. Vue / ElementPlus are resolved at call time, never at SDK
    // load time: the SDK tag precedes the framework scripts in <head>.
    function app(options, selector) {
        var Vue = window.Vue, ElementPlus = window.ElementPlus, icons = window.ElementPlusIconsVue;
        if (!Vue || !Vue.createApp) {
            throw new Error('Erupt.app: Vue is not loaded; add <script src="' + base + '/element-plus/vue3.js"></script> before this script');
        }
        var instance = Vue.createApp(options || {});
        if (ElementPlus) {
            instance.use(ElementPlus);
            ELEMENT_HELPERS.forEach(function (name) {
                // A page that already destructured the helper keeps its own binding
                if (!(name in window) && ElementPlus[name]) window[name] = ElementPlus[name];
            });
        }
        if (icons) {
            Object.keys(icons).forEach(function (name) { instance.component(name, icons[name]); });
        }
        instance.config.errorHandler = function (err, vm, info) {
            report('vue', (err && err.message ? err.message : String(err)) + (info ? ' (' + info + ')' : ''));
            console.error(err);
        };
        instance.mount(selector || '#app');
        return instance;
    }

    window.Erupt = {
        base: base,
        token: token,
        // Vue 3 + Element Plus bootstrap; options are the root component (template / setup / data ...),
        // selector defaults to '#app'. Returns the app instance
        app: app,
        // Paged query. query: {pageIndex, pageSize, sort: [{field, direction}], condition: [{key, value, expression}]}
        // Resolves to {pageIndex, pageSize, total, totalPage, list}
        table: function (model, query) {
            return call('POST', '/data/table/' + model, model, normalizeQuery(query));
        },
        // Detail by primary key; resolves to the row keyed by field names (REFERENCE
        // fields as {id, label, ...} objects) — the shape Erupt.update expects back
        row: function (model, id) {
            return call('GET', '/data/' + model + '/' + id, model);
        },
        // Resolves to [{id, label, pid, children}] (tree models only)
        tree: function (model) {
            return call('GET', '/data/tree/' + model, model);
        },
        // Resolves to [{value, label}] for a CHOICE / MULTI_CHOICE field
        choice: function (model, field) {
            return call('GET', '/comp/choice-item/' + model + '/' + field, model);
        },
        // Server-side defaults of a new row (default values, addBehavior hooks);
        // resolves to a row object keyed by field names, the seed of a create form
        initValue: function (model) {
            return call('GET', '/data/init-value/' + model, model);
        },
        // Options of a REFERENCE_TABLE field: paged rows of the target model narrowed
        // by the field's filter; query as in Erupt.table, dependValue only when the
        // field declares a dependField. Resolves to the same page shape as Erupt.table
        referenceTable: function (model, field, query, dependValue) {
            return call('POST', '/data/' + model + '/reference-table/' + field
                + qs({tabRef: false, dependValue: dependValue}), model, normalizeQuery(query));
        },
        // Options of a REFERENCE_TREE field; resolves to [{id, label, pid, children}]
        referenceTree: function (model, field, dependValue) {
            return call('GET', '/data/' + model + '/reference-tree/' + field + qs({dependValue: dependValue}), model);
        },
        // Options of a CHECKBOX field; resolves to [{id, label, remark}]
        checkbox: function (model, field) {
            return call('GET', '/data/' + model + '/checkbox/' + field, model);
        },
        // Create one row; row keys are field names, REFERENCE fields as {id: ...}.
        // Permission-checked server-side: the visitor needs add rights on the model
        add: function (model, row) {
            return unwrap(call('POST', '/data/modify/' + model, model, row));
        },
        // Update one row; submit the FULL object (fetch via Erupt.row first, then mutate)
        update: function (model, row) {
            return unwrap(call('POST', '/data/modify/' + model + '/update', model, row));
        },
        // Delete by primary key(s); accepts a single id or an array of ids
        remove: function (model, ids) {
            return unwrap(call('POST', '/data/modify/' + model + '/delete', model,
                Array.isArray(ids) ? ids : [ids]));
        },
        // Aggregation query against the erupt-cube semantic layer (requires the
        // erupt-cube module). query: {cube, explore, dimensions, measures,
        // filters, sorts, parameter, limit, offset}. Resolves to an array of
        // flat rows keyed by the exact dimension/measure codes passed in.
        cube: function (query) {
            return unwrap(call('POST', '/cube/semantic/query', '', query))
                .then(function (r) { return r.data; });
        }
    };
})();
