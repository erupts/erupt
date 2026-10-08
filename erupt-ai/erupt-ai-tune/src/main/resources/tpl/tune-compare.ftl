<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Compare Models</title>
    <style>
        :root {
            --bg: #f8fafc; --surface: #fff; --border: #e2e8f0; --fg: #1e293b; --fg-soft: #334155;
            --muted: #64748b; --faint: #94a3b8; --accent: #2563eb; --accent-hover: #1d4ed8; --accent-soft: #dbeafe;
            --good: #16a34a; --good-soft: #dcfce7; --bad: #dc2626; --bad-soft: #fee2e2;
            --radius: 10px; --shadow-sm: 0 1px 2px rgba(15, 23, 42, .05); --ring: rgba(37, 99, 235, .18);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        html, body { height: 100%; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: var(--bg); color: var(--fg); padding: 20px 24px; font-size: 13.5px; -webkit-font-smoothing: antialiased; display: flex; flex-direction: column; gap: 14px; }
        .head .label { font-size: 11px; font-weight: 600; letter-spacing: .08em; text-transform: uppercase; color: var(--faint); }
        .head .name { font-size: 19px; font-weight: 600; margin-top: 2px; }
        .head .sub { color: var(--muted); margin-top: 5px; font-size: 12.5px; }
        .head .sub code { background: var(--surface); border: 1px solid var(--border); border-radius: 5px; padding: 1px 6px; font-size: 12px; color: var(--fg-soft); }
        .card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); box-shadow: var(--shadow-sm); }
        .form { padding: 14px 16px; display: grid; grid-template-columns: 1fr; gap: 10px; }
        .form label { font-size: 11px; font-weight: 600; letter-spacing: .06em; text-transform: uppercase; color: var(--faint); display: block; margin-bottom: 5px; }
        textarea { width: 100%; border: 1px solid var(--border); border-radius: 8px; padding: 9px 12px; font: inherit; font-size: 13.5px; line-height: 1.5; resize: vertical; outline: none; background: var(--surface); color: var(--fg); transition: border-color .15s, box-shadow .15s; }
        textarea:focus { border-color: var(--accent); box-shadow: 0 0 0 3px var(--ring); }
        .row { display: flex; gap: 10px; align-items: center; }
        .row .hint { color: var(--faint); font-size: 12px; margin-left: auto; }
        button.btn { display: inline-flex; align-items: center; gap: 7px; padding: 8px 16px; border: 1px solid var(--border); border-radius: 7px; background: var(--surface); color: var(--fg-soft); font-size: 13px; cursor: pointer; transition: background .15s, border-color .15s; }
        button.btn:hover { border-color: var(--accent); color: var(--accent); }
        button.btn.primary { background: var(--accent); border-color: var(--accent); color: #fff; font-weight: 500; }
        button.btn.primary:hover { background: var(--accent-hover); }
        button.btn:disabled { opacity: .6; cursor: wait; }
        .spinner { width: 13px; height: 13px; border-radius: 50%; border: 2px solid rgba(255, 255, 255, .4); border-top-color: #fff; animation: spin .7s linear infinite; display: none; }
        .loading .spinner { display: inline-block; }
        @keyframes spin { to { transform: rotate(360deg); } }

        .results { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; flex: 1; min-height: 0; }
        .ans { display: flex; flex-direction: column; min-height: 220px; }
        .ans .hd { display: flex; align-items: center; gap: 8px; padding: 11px 15px; border-bottom: 1px solid var(--border); }
        .ans .hd .tag { font-size: 10.5px; font-weight: 700; letter-spacing: .06em; text-transform: uppercase; padding: 2px 8px; border-radius: 99px; }
        .ans.base .tag { background: #f1f5f9; color: var(--muted); } .ans.tuned .tag { background: var(--good-soft); color: var(--good); }
        .ans .hd code { font-size: 12px; color: var(--fg-soft); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
        .ans .hd .ms { margin-left: auto; color: var(--faint); font-size: 12px; font-variant-numeric: tabular-nums; white-space: nowrap; }
        .ans .bd { padding: 14px 16px; white-space: pre-wrap; word-break: break-word; line-height: 1.65; font-size: 14px; overflow: auto; flex: 1; color: var(--fg-soft); }
        .ans .bd.err { color: #991b1b; background: var(--bad-soft); font-size: 13px; }
        .ans .bd.empty { color: var(--faint); text-align: center; padding-top: 60px; }
        .skeleton .line { height: 12px; border-radius: 4px; margin-bottom: 10px; background: linear-gradient(90deg, #eef2f7 25%, #f6f8fb 45%, #eef2f7 65%); background-size: 200% 100%; animation: shimmer 1.2s linear infinite; }
        @keyframes shimmer { to { background-position: -200% 0; } }
        .history { font-size: 12.5px; color: var(--muted); padding: 10px 16px; border-top: 1px solid var(--border); display: flex; gap: 14px; flex-wrap: wrap; }
        .history a { color: var(--accent); cursor: pointer; text-decoration: none; }
    </style>
</head>
<body>
<div class="head">
    <div class="label">Compare base vs fine-tuned</div>
    <div class="name" id="name">...</div>
    <div class="sub" id="sub"></div>
</div>
<div class="card">
    <div class="form">
        <div>
            <label>System prompt (optional)</label>
            <textarea id="system" rows="2" placeholder="You are a support assistant for..."></textarea>
        </div>
        <div>
            <label>User prompt</label>
            <textarea id="prompt" rows="4" placeholder="Ask both models the same thing..." autofocus></textarea>
        </div>
        <div class="row">
            <button class="btn primary" id="run" onclick="run()"><span class="spinner"></span><span>Run both</span></button>
            <button class="btn" onclick="example()" id="exampleBtn">Load a training prompt</button>
            <span class="hint">Ctrl/Cmd + Enter runs</span>
        </div>
    </div>
    <div class="history" id="history" style="display:none"></div>
</div>
<div class="results">
    <div class="card ans base">
        <div class="hd"><span class="tag">Base</span><code id="baseModel"></code><span class="ms" id="baseMs"></span></div>
        <div class="bd empty" id="baseOut">The base model's answer appears here</div>
    </div>
    <div class="card ans tuned">
        <div class="hd"><span class="tag">Fine-tuned</span><code id="tunedModel"></code><span class="ms" id="tunedMs"></span></div>
        <div class="bd empty" id="tunedOut">The fine-tuned model's answer appears here</div>
    </div>
</div>
<script>
    var JOB_ID = ${rows[0].id?c};
    var TOKEN = new URLSearchParams(location.search).get('_token');
    var BASE = '/${request.contextPath}'.replace(/\/+$/, '');
    var job = null, history = [];

    function esc(s) { var d = document.createElement('div'); d.textContent = s == null ? '' : s; return d.innerHTML; }
    function api(path) { return BASE + '/erupt-api/tune' + path + (path.indexOf('?') >= 0 ? '&' : '?') + '_token=' + encodeURIComponent(TOKEN || ''); }
    function skeleton() { return '<div class="skeleton"><div class="line" style="width:92%"></div><div class="line" style="width:80%"></div><div class="line" style="width:60%"></div></div>'; }

    fetch(api('/job/' + JOB_ID)).then(function (r) { return r.json(); }).then(function (res) {
        if (!res.success) return;
        job = res.data;
        document.getElementById('name').textContent = job.name;
        document.getElementById('sub').innerHTML = esc(job.provider || '') + ' · <code>' + esc(job.baseModel) + '</code> vs <code>' + esc(job.fineTunedModel || '') + '</code>';
        document.getElementById('baseModel').textContent = job.baseModel || '';
        document.getElementById('tunedModel').textContent = job.fineTunedModel || '';
        if (!job.trainingDatasetId) document.getElementById('exampleBtn').style.display = 'none';
    });

    function example() {
        if (!job || !job.trainingDatasetId) return;
        var ds = job.trainingDatasetId;
        fetch(api('/dataset/' + ds + '/samples?page=1&size=1')).then(function (r) { return r.json(); }).then(function (res) {
            var total = Number(res.data && res.data.total) || 0;
            if (!total) return;
            var pick = 1 + Math.floor(Math.random() * total);
            return fetch(api('/dataset/' + ds + '/samples?page=' + pick + '&size=1')).then(function (r) { return r.json(); });
        }).then(function (res) {
            var it = res && res.data && res.data.items && res.data.items[0]; if (!it) return;
            var obj; try { obj = JSON.parse(it.content); } catch (e) { return; }
            var msgs = (obj.input && obj.input.messages) || obj.messages || [];
            var sys = msgs.filter(function (m) { return m.role === 'system'; })[0];
            var user = msgs.filter(function (m) { return m.role === 'user'; })[0];
            if (sys) document.getElementById('system').value = typeof sys.content === 'string' ? sys.content : '';
            if (user) document.getElementById('prompt').value = typeof user.content === 'string' ? user.content : JSON.stringify(user.content);
        });
    }

    function run() {
        var prompt = document.getElementById('prompt').value.trim();
        if (!prompt) return;
        var system = document.getElementById('system').value.trim();
        var btn = document.getElementById('run');
        btn.disabled = true; btn.classList.add('loading');
        ['base', 'tuned'].forEach(function (k) { var o = document.getElementById(k + 'Out'); o.className = 'bd'; o.innerHTML = skeleton(); document.getElementById(k + 'Ms').textContent = ''; });
        fetch(api('/job/' + JOB_ID + '/compare'), {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({system: system, prompt: prompt})})
            .then(function (r) { return r.json(); })
            .then(function (res) {
                done();
                if (!res.success) { show('base', {error: res.message}); show('tuned', {error: res.message}); return; }
                show('base', res.data.base); show('tuned', res.data.tuned);
                history.unshift(prompt); history = history.slice(0, 6);
                var h = document.getElementById('history');
                h.style.display = 'flex';
                h.innerHTML = '<span>Recent:</span>' + history.map(function (p, i) { return '<a onclick="reuse(' + i + ')">' + esc(p.length > 40 ? p.slice(0, 40) + '...' : p) + '</a>'; }).join('');
            })
            .catch(function (e) { done(); show('base', {error: e.message}); show('tuned', {error: e.message}); });
        function done() { btn.disabled = false; btn.classList.remove('loading'); }
    }

    function show(k, a) {
        var o = document.getElementById(k + 'Out');
        if (a.error) { o.className = 'bd err'; o.textContent = a.error; }
        else { o.className = 'bd'; o.textContent = a.text || ''; }
        document.getElementById(k + 'Ms').textContent = a.millis != null ? Number(a.millis).toLocaleString() + ' ms' : '';
    }
    function reuse(i) { document.getElementById('prompt').value = history[i]; run(); }
    document.addEventListener('keydown', function (e) { if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') run(); });
</script>
</body>
</html>
