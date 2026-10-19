<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>File Center</title>
    <style>
        :root { --bg: #fff; --line: #e5e7eb; --muted: #6b7280; --text: #111827; --sel: #e8f0fe; --sel-text: #1a56db; --hover: #f3f4f6; --col: 240px; }
        * { box-sizing: border-box; }
        html, body { margin: 0; height: 100%; font: 13px/1.45 -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", "Microsoft YaHei", sans-serif; color: var(--text); background: var(--bg); }
        #app { display: grid; grid-template-columns: 180px 1fr 320px; grid-template-rows: 44px 1fr; height: 100%; }
        .toolbar { grid-column: 1 / -1; display: flex; align-items: center; gap: 6px; padding: 0 10px; border-bottom: 1px solid var(--line); }
        .crumbs { flex: 1; display: flex; align-items: center; gap: 2px; overflow: hidden; white-space: nowrap; color: var(--muted); }
        .crumbs a { color: var(--text); text-decoration: none; padding: 2px 6px; border-radius: 4px; }
        .crumbs a:hover { background: var(--hover); }
        .crumbs span { color: var(--muted); }
        .toolbar input[type=search] { width: 180px; padding: 5px 8px; border: 1px solid var(--line); border-radius: 6px; outline: none; }
        .toolbar input[type=search]:focus { border-color: #93c5fd; }
        button { padding: 5px 10px; border: 1px solid var(--line); border-radius: 6px; background: #fff; color: var(--text); cursor: pointer; }
        button:hover:not(:disabled) { background: var(--hover); }
        button:disabled { opacity: .45; cursor: default; }
        button.primary { background: #1a56db; border-color: #1a56db; color: #fff; }
        button.primary:hover:not(:disabled) { background: #1e40af; }
        .stores { border-right: 1px solid var(--line); overflow: auto; padding: 8px 0; }
        .stores div { padding: 7px 14px; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .stores div:hover { background: var(--hover); }
        .stores div.active { background: var(--sel); color: var(--sel-text); font-weight: 600; }
        .columns { display: flex; overflow-x: auto; overflow-y: hidden; border-right: 1px solid var(--line); }
        .column { flex: 0 0 var(--col); border-right: 1px solid var(--line); overflow-y: auto; outline: none; }
        .column.drop { background: #eff6ff; }
        .row { display: flex; align-items: center; gap: 8px; padding: 5px 10px; cursor: default; white-space: nowrap; user-select: none; }
        .row:hover { background: var(--hover); }
        .row.selected { background: var(--sel); color: var(--sel-text); }
        .row .ico { width: 18px; text-align: center; flex: none; }
        .row .nm { flex: 1; overflow: hidden; text-overflow: ellipsis; }
        .row .arrow { color: var(--muted); flex: none; }
        .empty { padding: 20px; color: var(--muted); text-align: center; }
        .preview { overflow: auto; padding: 14px; }
        .preview .media { display: flex; align-items: center; justify-content: center; min-height: 160px; background: #f9fafb; border: 1px solid var(--line); border-radius: 8px; margin-bottom: 12px; overflow: hidden; }
        .preview img, .preview video { max-width: 100%; max-height: 320px; display: block; }
        .preview audio { width: 100%; }
        .preview iframe { width: 100%; height: 420px; border: 0; background: #fff; }
        .preview pre { margin: 0; padding: 10px; width: 100%; max-height: 420px; overflow: auto; font: 12px/1.5 ui-monospace, SFMono-Regular, Menlo, monospace; white-space: pre-wrap; word-break: break-all; background: #fff; }
        .preview .big { font-size: 48px; color: var(--muted); }
        .preview h3 { margin: 0 0 8px; font-size: 15px; word-break: break-all; }
        .preview dl { display: grid; grid-template-columns: auto 1fr; gap: 4px 12px; margin: 0 0 12px; color: var(--muted); }
        .preview dd { margin: 0; color: var(--text); word-break: break-all; }
        .preview .actions { display: flex; flex-wrap: wrap; gap: 6px; }
        .toast { position: fixed; left: 50%; bottom: 24px; transform: translateX(-50%); background: #111827; color: #fff; padding: 8px 14px; border-radius: 6px; opacity: 0; transition: opacity .2s; pointer-events: none; }
        .toast.show { opacity: .95; }
    </style>
</head>
<body>
<div id="app">
    <div class="toolbar">
        <div class="crumbs" id="crumbs"></div>
        <input type="search" id="filter" placeholder="${labels.search}">
        <button id="btnUpload" class="primary">${labels.upload}</button>
        <button id="btnFolder">${labels.new_folder}</button>
        <button id="btnRename">${labels.rename}</button>
        <button id="btnDelete">${labels.delete}</button>
    </div>
    <div class="stores" id="stores"></div>
    <div class="columns" id="columns"></div>
    <div class="preview" id="preview"></div>
</div>
<input type="file" id="picker" multiple hidden>
<div class="toast" id="toast"></div>
<script>
    const API = '/erupt-api/file-center', TOKEN = '${token?js_string}', WRITABLE = ${writable?c}, TEXT_MAX = ${textPreviewBytes?c};
    const STORES = [<#list stores as s>'${s?js_string}'<#sep>, </#list>];
    const L = {<#list labels?keys as k>'${k?js_string}': '${labels[k]?js_string}'<#sep>, </#list>};
    const state = {store: STORES[0], columns: [], filter: ''};

    const $ = id => document.getElementById(id);
    const esc = s => String(s).replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
    const q = o => Object.entries(o).map(([k, v]) => encodeURIComponent(k) + '=' + encodeURIComponent(v)).join('&');
    const rawUrl = (path, download) => API + '/raw?' + q({store: state.store, path, _token: TOKEN, download: !!download});
    const toast = msg => { const t = $('toast'); t.textContent = msg; t.classList.add('show'); setTimeout(() => t.classList.remove('show'), 1800); };
    const size = n => n < 1024 ? n + ' B' : n < 1048576 ? (n / 1024).toFixed(1) + ' KB' : n < 1073741824 ? (n / 1048576).toFixed(1) + ' MB' : (n / 1073741824).toFixed(2) + ' GB';
    const time = ms => ms ? new Date(ms).toLocaleString() : '';
    const icon = e => e.dir ? '📁' : /^image\//.test(e.contentType) ? '🖼️' : /^video\//.test(e.contentType) ? '🎬' : /^audio\//.test(e.contentType) ? '🎵' : /pdf/.test(e.contentType) ? '📕' : /zip|compressed|tar/.test(e.contentType) ? '🗜️' : '📄';

    async function api(path, opts) {
        const o = Object.assign({headers: {}}, opts || {});
        o.headers.token = TOKEN;
        const r = await fetch(API + path, o).then(x => x.json());
        if (!r.success) throw new Error(r.message || 'error');
        return r.data;
    }

    function selected() {
        for (let i = state.columns.length - 1; i >= 0; i--) if (state.columns[i].selected) return state.columns[i].selected;
        return null;
    }

    function currentDir() {
        const s = selected();
        if (s && s.dir) return s.path;
        return state.columns.length ? state.columns[state.columns.length - 1].path : '/';
    }

    async function open(path, depth) {
        const entries = await api('/list?' + q({store: state.store, path}));
        state.columns = state.columns.slice(0, depth);
        state.columns.push({path, entries, selected: null});
        render();
    }

    function select(depth, entry) {
        state.columns = state.columns.slice(0, depth + 1);
        state.columns[depth].selected = entry;
        if (entry.dir) open(entry.path, depth + 1); else render();
    }

    function renderStores() {
        $('stores').innerHTML = STORES.map(s => '<div class="' + (s === state.store ? 'active' : '') + '" data-s="' + esc(s) + '">' + esc(s) + '</div>').join('');
        $('stores').querySelectorAll('div').forEach(d => d.onclick = () => { state.store = d.dataset.s; state.columns = []; open('/', 0); renderStores(); });
    }

    function render() {
        const cols = $('columns');
        cols.innerHTML = '';
        state.columns.forEach((col, depth) => {
            const el = document.createElement('div');
            el.className = 'column';
            el.tabIndex = 0;
            const last = depth === state.columns.length - 1;
            const shown = last && state.filter ? col.entries.filter(e => e.name.toLowerCase().includes(state.filter)) : col.entries;
            if (!shown.length) el.innerHTML = '<div class="empty">' + esc(L.empty) + '</div>';
            shown.forEach(e => {
                const row = document.createElement('div');
                row.className = 'row' + (col.selected && col.selected.path === e.path ? ' selected' : '');
                row.innerHTML = '<span class="ico">' + icon(e) + '</span><span class="nm" title="' + esc(e.name) + '">' + esc(e.name) + '</span>' + (e.dir ? '<span class="arrow">›</span>' : '');
                row.onclick = () => select(depth, e);
                row.ondblclick = () => { if (!e.dir) window.open(rawUrl(e.path)); };
                el.appendChild(row);
            });
            if (WRITABLE) {
                el.ondragover = ev => { ev.preventDefault(); el.classList.add('drop'); };
                el.ondragleave = () => el.classList.remove('drop');
                el.ondrop = ev => { ev.preventDefault(); el.classList.remove('drop'); upload(col.path, ev.dataTransfer.files); };
            }
            cols.appendChild(el);
        });
        cols.scrollLeft = cols.scrollWidth;
        renderCrumbs();
        renderPreview();
        const s = selected();
        $('btnUpload').disabled = !WRITABLE;
        $('btnFolder').disabled = !WRITABLE;
        $('btnRename').disabled = !WRITABLE || !s;
        $('btnDelete').disabled = !WRITABLE || !s;
    }

    function renderCrumbs() {
        const dir = currentDir();
        const parts = dir === '/' ? [] : dir.slice(1).split('/');
        let html = '<a data-p="/">' + esc(state.store) + '</a>';
        let acc = '';
        parts.forEach(p => { acc += '/' + p; html += '<span>/</span><a data-p="' + esc(acc) + '">' + esc(p) + '</a>'; });
        $('crumbs').innerHTML = html;
        $('crumbs').querySelectorAll('a').forEach(a => a.onclick = () => { const depth = a.dataset.p === '/' ? 0 : a.dataset.p.split('/').length - 1; state.columns = state.columns.slice(0, depth); open(a.dataset.p, depth); });
    }

    async function renderPreview() {
        const p = $('preview'), e = selected();
        if (!e || e.dir) {
            const dir = currentDir(), col = state.columns[state.columns.length - 1];
            p.innerHTML = '<h3>' + esc(e ? e.name : state.store) + '</h3><dl><dt>' + esc(L.type) + '</dt><dd>📁</dd>' + (col ? '<dt>' + esc(L.items) + '</dt><dd>' + col.entries.length + '</dd>' : '') + '</dl>';
            return;
        }
        const url = rawUrl(e.path), t = e.contentType || '';
        let media;
        if (/^image\//.test(t)) media = '<img src="' + esc(url) + '" alt="">';
        else if (/^video\//.test(t)) media = '<video src="' + esc(url) + '" controls></video>';
        else if (/^audio\//.test(t)) media = '<audio src="' + esc(url) + '" controls></audio>';
        else if (/pdf/.test(t)) media = '<iframe src="' + esc(url) + '"></iframe>';
        else if (/^text\/|json|xml|javascript|yaml|csv|sql/.test(t) || /\.(md|log|yml|yaml|properties|conf|ini|sh|sql|ts|java|py|go|rs|toml)$/i.test(e.name)) media = e.size > TEXT_MAX ? '<div class="big">📄</div>' : '<pre id="text">…</pre>';
        else media = '<div class="big">' + icon(e) + '</div>';
        const stat = await api('/stat?' + q({store: state.store, path: e.path}));
        const link = stat.url !== 'null' ? stat.url : null;
        p.innerHTML = '<div class="media">' + media + '</div><h3>' + esc(e.name) + '</h3><dl>'
            + '<dt>' + esc(L.size) + '</dt><dd>' + size(e.size) + '</dd>'
            + '<dt>' + esc(L.modified) + '</dt><dd>' + esc(time(e.modified)) + '</dd>'
            + '<dt>' + esc(L.type) + '</dt><dd>' + esc(t) + '</dd></dl>'
            + '<div class="actions"><button id="pvOpen">' + esc(L.open) + '</button><button id="pvDown">' + esc(L.download) + '</button>'
            + (link ? '<button id="pvCopy">' + esc(L.copy_link) + '</button>' : '') + '</div>';
        $('pvOpen').onclick = () => window.open(url);
        $('pvDown').onclick = () => { location.href = rawUrl(e.path, true); };
        if (link) $('pvCopy').onclick = () => navigator.clipboard.writeText(new URL(link, location.href).href).then(() => toast(L.link_copied));
        if ($('text')) fetch(url).then(r => r.text()).then(txt => { if ($('text')) $('text').textContent = txt; });
    }

    async function upload(dir, files) {
        if (!files || !files.length) return;
        const form = new FormData();
        for (const f of files) form.append('files', f);
        try {
            await api('/upload?' + q({store: state.store, path: dir}), {method: 'POST', body: form});
            await reload(dir);
        } catch (err) { toast(err.message); }
    }

    async function reload(dir) {
        const depth = state.columns.findIndex(c => c.path === dir);
        if (depth < 0) return open(dir, state.columns.length);
        const keep = state.columns[depth].selected;
        state.columns[depth].entries = await api('/list?' + q({store: state.store, path: dir}));
        state.columns[depth].selected = keep && state.columns[depth].entries.find(x => x.path === keep.path) ? keep : null;
        state.columns = state.columns.slice(0, state.columns[depth].selected && state.columns[depth].selected.dir ? depth + 2 : depth + 1);
        render();
    }

    $('btnUpload').onclick = () => $('picker').click();
    $('picker').onchange = () => { upload(currentDir(), $('picker').files); $('picker').value = ''; };
    $('btnFolder').onclick = async () => {
        const name = prompt(L.name);
        if (!name) return;
        try { await api('/mkdir?' + q({store: state.store, path: currentDir(), name}), {method: 'POST'}); await reload(currentDir()); } catch (err) { toast(err.message); }
    };
    $('btnRename').onclick = async () => {
        const e = selected();
        if (!e) return;
        const name = prompt(L.name, e.name);
        if (!name || name === e.name) return;
        const dir = e.path.slice(0, e.path.lastIndexOf('/')) || '/';
        try { await api('/rename?' + q({store: state.store, path: e.path, name}), {method: 'POST'}); state.columns = state.columns.slice(0, state.columns.findIndex(c => c.path === dir) + 1); await reload(dir); } catch (err) { toast(err.message); }
    };
    $('btnDelete').onclick = async () => {
        const e = selected();
        if (!e || !confirm(L.confirm_delete)) return;
        const dir = e.path.slice(0, e.path.lastIndexOf('/')) || '/';
        try {
            await api('/delete?' + q({store: state.store}), {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify([e.path])});
            state.columns = state.columns.slice(0, state.columns.findIndex(c => c.path === dir) + 1);
            state.columns[state.columns.length - 1].selected = null;
            await reload(dir);
        } catch (err) { toast(err.message); }
    };
    $('filter').oninput = () => { state.filter = $('filter').value.trim().toLowerCase(); render(); };
    document.addEventListener('keydown', ev => {
        if (ev.target.tagName === 'INPUT') return;
        const depth = state.columns.findIndex(c => c.selected) < 0 ? state.columns.length - 1 : Math.max(...state.columns.map((c, i) => c.selected ? i : -1));
        const col = state.columns[depth];
        if (!col) return;
        const idx = col.selected ? col.entries.findIndex(x => x.path === col.selected.path) : -1;
        if (ev.key === 'ArrowDown' && idx < col.entries.length - 1) { select(depth, col.entries[idx + 1]); ev.preventDefault(); }
        else if (ev.key === 'ArrowUp' && idx > 0) { select(depth, col.entries[idx - 1]); ev.preventDefault(); }
        else if (ev.key === 'ArrowRight' && col.selected && col.selected.dir && state.columns[depth + 1] && state.columns[depth + 1].entries.length) { select(depth + 1, state.columns[depth + 1].entries[0]); ev.preventDefault(); }
        else if (ev.key === 'ArrowLeft' && depth > 0) { state.columns = state.columns.slice(0, depth + 1); state.columns[depth].selected = null; render(); ev.preventDefault(); }
    });

    renderStores();
    if (STORES.length) open('/', 0).catch(err => toast(err.message));
</script>
</body>
</html>
