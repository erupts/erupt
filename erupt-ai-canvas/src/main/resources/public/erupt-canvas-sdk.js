// Erupt View SDK — data access for AI-generated view pages.
// Loaded via <script src=".../erupt-canvas-sdk.js">; exposes window.Erupt.
// Handles the base path, the user token and the `erupt` auth header, so
// generated pages never hand-roll HTTP calls against the Erupt API.
(function () {
    'use strict';
    // Script URL: {base}/erupt-canvas-sdk.js — strip the file name to get the base
    var src = document.currentScript.src;
    var base = src.substring(0, src.lastIndexOf('/erupt-canvas-sdk.js'));
    var token = new URLSearchParams(location.search).get('_token') || '';

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

    // Modify endpoints answer with an R envelope; convert failures into rejections
    function unwrap(promise) {
        return promise.then(function (r) {
            if (r && r.success === false) throw new Error(r.message || 'Operation failed');
            return r;
        });
    }

    window.Erupt = {
        base: base,
        token: token,
        // Paged query. query: {pageIndex, pageSize, sort: [{field, direction}], condition: [{key, value, expression}]}
        // Resolves to {pageIndex, pageSize, total, totalPage, list}
        table: function (model, query) {
            return call('POST', '/data/table/' + model, model, normalizeQuery(query));
        },
        // Detail by primary key; resolves to the nested entity object
        row: function (model, id) {
            return call('GET', '/data/' + model + '/' + id, model);
        },
        // Resolves to [{id, label, pid, children}] (tree models only)
        tree: function (model) {
            return call('GET', '/data/tree/' + model, model);
        },
        // Resolves to [{value, label}] for a CHOICE field
        choice: function (model, field) {
            return call('GET', '/comp/choice-item/' + model + '/' + field, model);
        },
        // Create one row; row keys are field names, REFERENCE fields as {id: ...}
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
