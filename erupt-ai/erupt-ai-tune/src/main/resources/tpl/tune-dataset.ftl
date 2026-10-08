<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dataset Preview</title>
    <style>
        :root {
            --bg: #f8fafc; --surface: #fff; --border: #e2e8f0; --fg: #1e293b; --fg-soft: #334155;
            --muted: #64748b; --faint: #94a3b8; --accent: #2563eb; --accent-soft: #dbeafe;
            --good: #16a34a; --good-soft: #dcfce7; --bad: #dc2626; --bad-soft: #fee2e2; --warn: #d97706; --warn-soft: #fef3c7;
            --radius: 10px; --shadow-sm: 0 1px 2px rgba(15, 23, 42, .05);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        html, body { height: 100%; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: var(--bg); color: var(--fg); padding: 20px 24px; font-size: 13.5px; -webkit-font-smoothing: antialiased; display: flex; flex-direction: column; }
        .head { display: flex; align-items: flex-start; gap: 14px; margin-bottom: 14px; }
        .head .label { font-size: 11px; font-weight: 600; letter-spacing: .08em; text-transform: uppercase; color: var(--faint); }
        .head .name { font-size: 19px; font-weight: 600; margin-top: 2px; display: flex; align-items: center; gap: 10px; }
        .head .actions { margin-left: auto; display: flex; gap: 8px; align-items: center; }
        .pill { display: inline-flex; align-items: center; font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 99px; background: var(--accent-soft); color: var(--accent); }
        .pill.good { background: var(--good-soft); color: var(--good); }
        .pill.bad { background: var(--bad-soft); color: var(--bad); }
        .pill.gray { background: #f1f5f9; color: var(--muted); }
        button.btn { display: inline-flex; align-items: center; gap: 6px; padding: 7px 13px; border: 1px solid var(--border); border-radius: 7px; background: var(--surface); color: var(--fg-soft); font-size: 13px; cursor: pointer; }
        button.btn:hover { border-color: var(--accent); color: var(--accent); }
        button.btn.on { background: var(--accent-soft); border-color: var(--accent); color: var(--accent); }
        input.search { padding: 7px 11px; border: 1px solid var(--border); border-radius: 7px; font-size: 13px; width: 220px; outline: none; background: var(--surface); }
        input.search:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(37, 99, 235, .15); }

        .kpis { display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 10px; margin-bottom: 14px; }
        .kpi { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 10px 14px; box-shadow: var(--shadow-sm); }
        .kpi .k { font-size: 11px; font-weight: 600; letter-spacing: .06em; text-transform: uppercase; color: var(--faint); }
        .kpi .v { font-size: 19px; font-weight: 600; margin-top: 3px; font-variant-numeric: tabular-nums; }
        .kpi .v.bad { color: var(--bad); }
        .roles { display: flex; height: 6px; border-radius: 3px; overflow: hidden; margin-top: 8px; background: #f1f5f9; }
        .roles i { display: block; height: 100%; }
        .rolesLegend { display: flex; gap: 12px; font-size: 11px; color: var(--muted); margin-top: 6px; flex-wrap: wrap; }
        .rolesLegend b { display: inline-block; width: 8px; height: 8px; border-radius: 2px; margin-right: 4px; vertical-align: middle; }

        .main { display: grid; grid-template-columns: 340px 1fr; gap: 14px; flex: 1; min-height: 0; }
        .card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); box-shadow: var(--shadow-sm); display: flex; flex-direction: column; min-height: 0; min-width: 0; }
        .card .hd { display: flex; align-items: center; gap: 8px; padding: 10px 14px; border-bottom: 1px solid var(--border); font-weight: 600; font-size: 13px; }
        .card .hd .right { margin-left: auto; font-weight: 400; color: var(--muted); font-size: 12px; display: flex; gap: 6px; align-items: center; }
        .list { overflow: auto; flex: 1; }
        .item { padding: 10px 14px; border-bottom: 1px solid #f1f5f9; cursor: pointer; display: grid; grid-template-columns: auto 1fr auto; gap: 8px; align-items: start; }
        .item:hover { background: #f8fafc; }
        .item.sel { background: var(--accent-soft); }
        .item .seq { font-size: 11px; color: var(--faint); font-variant-numeric: tabular-nums; padding-top: 2px; min-width: 32px; }
        .item .snip { font-size: 12.5px; color: var(--fg-soft); overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.45; }
        .item .meta { font-size: 11px; color: var(--faint); white-space: nowrap; font-variant-numeric: tabular-nums; }
        .item .meta.bad { color: var(--bad); font-weight: 600; }
        .pager { display: flex; align-items: center; gap: 8px; padding: 8px 12px; border-top: 1px solid var(--border); font-size: 12px; color: var(--muted); }
        .pager button { padding: 4px 9px; }
        .pager .pg { margin-left: auto; font-variant-numeric: tabular-nums; }

        .detail { overflow: auto; flex: 1; padding: 16px 18px; }
        .msg { display: flex; gap: 10px; margin-bottom: 12px; }
        .msg .role { flex: none; width: 76px; font-size: 11px; font-weight: 700; letter-spacing: .05em; text-transform: uppercase; padding-top: 9px; text-align: right; }
        .msg .bubble { flex: 1; min-width: 0; padding: 9px 13px; border-radius: 10px; white-space: pre-wrap; word-break: break-word; line-height: 1.6; font-size: 13.5px; border: 1px solid var(--border); background: var(--surface); }
        .msg.system .role { color: var(--muted); } .msg.system .bubble { background: #f8fafc; color: var(--muted); font-size: 12.5px; }
        .msg.user .role { color: var(--accent); } .msg.user .bubble { background: #eff6ff; border-color: #bfdbfe; }
        .msg.assistant .role { color: var(--good); } .msg.assistant .bubble { background: #f0fdf4; border-color: #bbf7d0; }
        .msg.tool .role { color: var(--warn); } .msg.tool .bubble { background: #fffbeb; border-color: #fde68a; font-family: ui-monospace, Menlo, monospace; font-size: 12px; }
        .msg .bubble code.tc { display: block; font-family: ui-monospace, Menlo, monospace; font-size: 12px; background: rgba(15, 23, 42, .05); border-radius: 6px; padding: 6px 8px; margin-top: 6px; white-space: pre-wrap; }
        .pref { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-top: 4px; }
        .pref .col .cap { font-size: 11px; font-weight: 700; letter-spacing: .05em; text-transform: uppercase; margin-bottom: 6px; }
        .pref .col.yes .cap { color: var(--good); } .pref .col.no .cap { color: var(--bad); }
        .pref .col .bubble { padding: 9px 13px; border-radius: 10px; white-space: pre-wrap; word-break: break-word; line-height: 1.6; border: 1px solid var(--border); }
        .pref .col.yes .bubble { background: #f0fdf4; border-color: #bbf7d0; } .pref .col.no .bubble { background: #fef2f2; border-color: #fecaca; }
        .err { background: var(--bad-soft); color: #991b1b; border: 1px solid #fecaca; border-radius: 8px; padding: 9px 12px; margin-bottom: 12px; font-size: 13px; }
        pre.raw { font-family: ui-monospace, Menlo, monospace; font-size: 12px; background: #0f172a; color: #e2e8f0; border-radius: 8px; padding: 12px 14px; white-space: pre-wrap; word-break: break-all; line-height: 1.55; }
        .empty { text-align: center; color: var(--faint); padding: 56px 0; font-size: 13px; }
        .toggle { font-size: 12px; color: var(--accent); cursor: pointer; background: none; border: none; }
    </style>
</head>
<body>
<div class="head">
    <div>
        <div class="label">Training Dataset</div>
        <div class="name"><span id="name">${rows[0].name}</span><span id="format" class="pill gray"></span><span id="status" class="pill gray"></span></div>
    </div>
    <div class="actions">
        <input class="search" id="kw" placeholder="Search sample text..." onkeydown="if(event.key==='Enter'){page=1;loadSamples()}">
        <button class="btn" id="invalidBtn" onclick="toggleInvalid()">Invalid only</button>
        <button class="btn" onclick="exportJsonl()">Export JSONL</button>
    </div>
</div>
<div class="kpis" id="kpis"></div>
<div class="main">
    <div class="card">
        <div class="hd">Samples <div class="right" id="listMeta"></div></div>
        <div class="list" id="list"><div class="empty">Loading...</div></div>
        <div class="pager">
            <button class="btn" onclick="go(-1)">&larr;</button>
            <button class="btn" onclick="go(1)">&rarr;</button>
            <span class="pg" id="pg"></span>
        </div>
    </div>
    <div class="card">
        <div class="hd"><span id="detailTitle">Sample</span>
            <div class="right"><span id="detailMeta"></span><button class="toggle" id="rawBtn" onclick="toggleRaw()">raw JSON</button></div>
        </div>
        <div class="detail" id="detail"><div class="empty">Select a sample on the left</div></div>
    </div>
</div>
<script>
    var DS_ID = ${rows[0].id?c};
    var TOKEN = new URLSearchParams(location.search).get('_token');
    var BASE = '/${request.contextPath}'.replace(/\/+$/, '');
    var page = 1, size = 30, total = 0, invalidOnly = false, items = [], selected = null, raw = false, dataset = null;
    var ROLE_COLORS = {system: '#94a3b8', user: '#2563eb', assistant: '#16a34a', tool: '#d97706', developer: '#7c3aed'};

    function esc(s) { var d = document.createElement('div'); d.textContent = s == null ? '' : s; return d.innerHTML; }
    function num(v) { if (v == null || v === '') return null; var n = Number(v); return isFinite(n) ? n : null; }
    function fmt(n) { n = num(n); return n == null ? '—' : n.toLocaleString(); }
    function api(path) { return BASE + '/erupt-api/tune' + path + (path.indexOf('?') >= 0 ? '&' : '?') + '_token=' + encodeURIComponent(TOKEN || ''); }
    function text(content) {
        if (content == null) return '';
        if (typeof content === 'string') return content;
        if (Array.isArray(content)) return content.map(function (p) { return p && p.type === 'text' ? p.text : '[' + (p && p.type || 'part') + ']'; }).join('\n');
        return JSON.stringify(content);
    }
    function parse(sample) { try { return JSON.parse(sample.content); } catch (e) { return null; } }
    function messagesOf(obj, format) {
        if (!obj) return [];
        return format === 'PREFERENCE' ? (obj.input && obj.input.messages) || [] : obj.messages || [];
    }

    function loadDataset() {
        fetch(api('/dataset/' + DS_ID)).then(function (r) { return r.json(); }).then(function (res) {
            if (!res.success) return;
            dataset = res.data;
            document.getElementById('name').textContent = dataset.name;
            var f = document.getElementById('format'); f.textContent = dataset.format === 'PREFERENCE' ? 'Preference (DPO)' : 'Chat (SFT)'; f.className = 'pill';
            var s = document.getElementById('status'); s.textContent = dataset.status; s.className = 'pill ' + (dataset.status === 'READY' ? 'good' : dataset.status === 'FAILED' ? 'bad' : 'gray');
            var totalN = num(dataset.sampleCount) || 0, valid = num(dataset.validCount) || 0, tokens = num(dataset.tokenEstimate) || 0;
            var kpis = [
                ['Samples', fmt(totalN), ''], ['Valid', fmt(valid), ''], ['Invalid', fmt(totalN - valid), totalN - valid ? 'bad' : ''],
                ['Est. tokens', fmt(tokens), ''], ['Avg tokens / sample', valid ? fmt(Math.round(tokens / valid)) : '—', '']
            ];
            document.getElementById('kpis').innerHTML = kpis.map(function (k) {
                return '<div class="kpi"><div class="k">' + k[0] + '</div><div class="v ' + k[2] + '">' + k[1] + '</div></div>';
            }).join('') + '<div class="kpi" id="rolesKpi"><div class="k">Roles on this page</div><div class="roles" id="roles"></div><div class="rolesLegend" id="rolesLegend"></div></div>';
            renderRoles();
        });
    }

    function renderRoles() {
        var counts = {}, sum = 0;
        items.forEach(function (it) {
            var obj = parse(it); var msgs = messagesOf(obj, dataset && dataset.format);
            if (dataset && dataset.format === 'PREFERENCE' && obj) msgs = msgs.concat(obj.preferred_output || [], obj.non_preferred_output || []);
            msgs.forEach(function (m) { if (m && m.role) { counts[m.role] = (counts[m.role] || 0) + 1; sum++; } });
        });
        var bar = document.getElementById('roles'), legend = document.getElementById('rolesLegend');
        if (!bar) return;
        bar.innerHTML = Object.keys(counts).map(function (r) { return '<i style="width:' + (counts[r] / sum * 100) + '%;background:' + (ROLE_COLORS[r] || '#cbd5e1') + '" title="' + r + '"></i>'; }).join('');
        legend.innerHTML = Object.keys(counts).map(function (r) { return '<span><b style="background:' + (ROLE_COLORS[r] || '#cbd5e1') + '"></b>' + esc(r) + ' ' + counts[r] + '</span>'; }).join('');
    }

    function loadSamples() {
        var kw = document.getElementById('kw').value.trim();
        fetch(api('/dataset/' + DS_ID + '/samples?page=' + page + '&size=' + size + '&invalidOnly=' + invalidOnly + '&keyword=' + encodeURIComponent(kw)))
            .then(function (r) { return r.json(); }).then(function (res) {
                if (!res.success) { document.getElementById('list').innerHTML = '<div class="empty">' + esc(res.message) + '</div>'; return; }
                total = num(res.data.total) || 0; items = res.data.items || [];
                document.getElementById('listMeta').textContent = total + (invalidOnly ? ' invalid' : '') + (kw ? ' matching' : '');
                var pages = Math.max(1, Math.ceil(total / size));
                document.getElementById('pg').textContent = 'page ' + page + ' / ' + pages;
                if (!items.length) { document.getElementById('list').innerHTML = '<div class="empty">No samples' + (invalidOnly ? ' are invalid' : '') + '</div>'; document.getElementById('detail').innerHTML = '<div class="empty">Nothing to show</div>'; renderRoles(); return; }
                document.getElementById('list').innerHTML = items.map(function (it, i) {
                    var obj = parse(it), msgs = messagesOf(obj, dataset && dataset.format);
                    var firstUser = msgs.filter(function (m) { return m && m.role === 'user'; })[0];
                    var snip = firstUser ? text(firstUser.content) : (it.errorInfo || it.content);
                    return '<div class="item" id="item' + i + '" onclick="select(' + i + ')"><span class="seq">#' + it.seq + '</span><span class="snip">' + esc(snip) + '</span>'
                        + '<span class="meta ' + (it.valid === false ? 'bad' : '') + '">' + (it.valid === false ? 'invalid' : fmt(it.tokens) + ' tok') + '</span></div>';
                }).join('');
                renderRoles();
                select(0);
            });
    }

    function select(i) {
        selected = i;
        var it = items[i]; if (!it) return;
        Array.prototype.forEach.call(document.querySelectorAll('.item'), function (el, k) { el.classList.toggle('sel', k === i); });
        document.getElementById('detailTitle').textContent = 'Sample #' + it.seq;
        document.getElementById('detailMeta').textContent = (it.source || '') + ' · ' + (it.turns || 0) + ' turns · ' + fmt(it.tokens) + ' tokens';
        var detail = document.getElementById('detail');
        var html = it.valid === false ? '<div class="err">' + esc(it.errorInfo || 'Invalid sample') + '</div>' : '';
        var obj = parse(it);
        if (raw || !obj) {
            var pretty = it.content; try { pretty = JSON.stringify(JSON.parse(it.content), null, 2); } catch (e) {}
            detail.innerHTML = html + '<pre class="raw">' + esc(pretty) + '</pre>';
            return;
        }
        var format = dataset ? dataset.format : (obj.input ? 'PREFERENCE' : 'CHAT');
        html += messagesOf(obj, format).map(renderMessage).join('');
        if (format === 'PREFERENCE') {
            html += '<div class="pref"><div class="col yes"><div class="cap">&#10003; Preferred</div>' + (obj.preferred_output || []).map(function (m) { return '<div class="bubble">' + esc(text(m.content)) + '</div>'; }).join('') + '</div>'
                + '<div class="col no"><div class="cap">&#10005; Non-preferred</div>' + (obj.non_preferred_output || []).map(function (m) { return '<div class="bubble">' + esc(text(m.content)) + '</div>'; }).join('') + '</div></div>';
        }
        if (obj.tools) html += '<div class="msg tool"><div class="role">tools</div><div class="bubble">' + esc(JSON.stringify(obj.tools, null, 2)) + '</div></div>';
        detail.innerHTML = html;
    }

    function renderMessage(m) {
        if (!m || typeof m !== 'object') return '';
        var role = m.role || 'unknown';
        var body = esc(text(m.content));
        if (m.tool_calls) body += '<code class="tc">' + esc(JSON.stringify(m.tool_calls, null, 2)) + '</code>';
        if (m.weight === 0) body += '<div style="font-size:11px;color:var(--faint);margin-top:4px">weight 0 — not trained on</div>';
        return '<div class="msg ' + esc(role) + '"><div class="role">' + esc(role) + '</div><div class="bubble">' + body + '</div></div>';
    }

    function go(delta) {
        var pages = Math.max(1, Math.ceil(total / size));
        var next = Math.min(pages, Math.max(1, page + delta));
        if (next === page) return;
        page = next; loadSamples();
    }
    function toggleInvalid() { invalidOnly = !invalidOnly; page = 1; document.getElementById('invalidBtn').classList.toggle('on', invalidOnly); loadSamples(); }
    function toggleRaw() { raw = !raw; document.getElementById('rawBtn').textContent = raw ? 'rendered' : 'raw JSON'; if (selected != null) select(selected); }
    function exportJsonl() { window.open(api('/dataset/' + DS_ID + '/export'), '_blank'); }

    loadDataset();
    loadSamples();
</script>
</body>
</html>
