<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Training Monitor</title>
    <style>
        :root {
            --bg: #f8fafc; --surface: #fff; --border: #e2e8f0; --fg: #1e293b; --fg-soft: #334155;
            --muted: #64748b; --faint: #94a3b8; --accent: #2563eb; --accent-soft: #dbeafe;
            --good: #16a34a; --good-soft: #dcfce7; --bad: #dc2626; --bad-soft: #fee2e2;
            --warn: #d97706; --warn-soft: #fef3c7; --valid: #ea580c; --radius: 10px;
            --shadow-sm: 0 1px 2px rgba(15, 23, 42, .05);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: var(--bg); color: var(--fg); padding: 22px 26px 30px; font-size: 13.5px; -webkit-font-smoothing: antialiased; }
        .head { display: flex; align-items: flex-start; gap: 14px; margin-bottom: 16px; }
        .head .label { font-size: 11px; font-weight: 600; letter-spacing: .08em; text-transform: uppercase; color: var(--faint); }
        .head .name { font-size: 19px; font-weight: 600; margin-top: 2px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
        .head .sub { color: var(--muted); margin-top: 5px; font-size: 12.5px; display: flex; gap: 6px; align-items: center; flex-wrap: wrap; }
        .head .sub code { background: var(--surface); border: 1px solid var(--border); border-radius: 5px; padding: 1px 6px; font-size: 12px; color: var(--fg-soft); }
        .head .actions { margin-left: auto; display: flex; align-items: center; gap: 10px; color: var(--faint); font-size: 12px; }
        button.btn { display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border: 1px solid var(--border); border-radius: 7px; background: var(--surface); color: var(--fg-soft); font-size: 13px; cursor: pointer; transition: border-color .15s, background .15s; }
        button.btn:hover { border-color: var(--accent); color: var(--accent); }
        button.btn.primary { background: var(--accent); border-color: var(--accent); color: #fff; }
        .pill { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 99px; background: var(--accent-soft); color: var(--accent); }
        .pill .dot { width: 7px; height: 7px; border-radius: 50%; background: currentColor; }
        .pill.live .dot { animation: pulse 1.2s ease-in-out infinite; }
        @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .3; } }
        .pill.good { background: var(--good-soft); color: var(--good); }
        .pill.bad { background: var(--bad-soft); color: var(--bad); }
        .pill.warn { background: var(--warn-soft); color: var(--warn); }
        .pill.gray { background: #f1f5f9; color: var(--muted); }

        .steps { display: flex; align-items: center; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 14px 18px; margin-bottom: 14px; box-shadow: var(--shadow-sm); }
        .step { display: flex; align-items: center; gap: 9px; color: var(--faint); font-size: 12.5px; white-space: nowrap; }
        .step .n { width: 22px; height: 22px; border-radius: 50%; border: 2px solid var(--border); display: inline-flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; background: var(--surface); }
        .step.done { color: var(--fg-soft); }
        .step.done .n { background: var(--good); border-color: var(--good); color: #fff; }
        .step.current { color: var(--accent); font-weight: 600; }
        .step.current .n { border-color: var(--accent); color: var(--accent); box-shadow: 0 0 0 4px var(--accent-soft); }
        .step.failed { color: var(--bad); font-weight: 600; }
        .step.failed .n { background: var(--bad); border-color: var(--bad); color: #fff; }
        .step.cancelled .n { background: var(--warn); border-color: var(--warn); color: #fff; }
        .step.cancelled { color: var(--warn); font-weight: 600; }
        .line { flex: 1; height: 2px; background: var(--border); margin: 0 12px; min-width: 16px; }
        .line.done { background: var(--good); }

        .banner { border-radius: var(--radius); padding: 11px 14px; margin-bottom: 14px; font-size: 13px; line-height: 1.5; white-space: pre-wrap; word-break: break-word; }
        .banner.bad { background: var(--bad-soft); color: #991b1b; border: 1px solid #fecaca; }
        .banner.good { background: var(--good-soft); color: #166534; border: 1px solid #bbf7d0; }

        .kpis { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 10px; margin-bottom: 14px; }
        .kpi { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 12px 14px; box-shadow: var(--shadow-sm); }
        .kpi .k { font-size: 11px; font-weight: 600; letter-spacing: .06em; text-transform: uppercase; color: var(--faint); }
        .kpi .v { font-size: 20px; font-weight: 600; margin-top: 4px; font-variant-numeric: tabular-nums; color: var(--fg); }
        .kpi .v small { font-size: 12px; font-weight: 500; color: var(--muted); margin-left: 4px; }

        .grid { display: grid; grid-template-columns: 1.6fr 1fr; gap: 14px; }
        @media (max-width: 960px) { .grid { grid-template-columns: 1fr; } }
        .card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); box-shadow: var(--shadow-sm); display: flex; flex-direction: column; min-width: 0; }
        .card .hd { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border-bottom: 1px solid var(--border); font-weight: 600; font-size: 13.5px; }
        .card .hd .right { margin-left: auto; font-weight: 400; color: var(--muted); font-size: 12px; display: flex; gap: 14px; }
        .card .bd { padding: 14px 16px; }
        .legend { display: inline-flex; align-items: center; gap: 6px; }
        .legend i { width: 14px; height: 3px; border-radius: 2px; display: inline-block; }
        .chart { width: 100%; height: 280px; display: block; }
        .chart .grid-line { stroke: #eef2f7; }
        .chart .axis { stroke: var(--border); }
        .chart text { fill: var(--faint); font-size: 10.5px; font-variant-numeric: tabular-nums; }
        .chart .train { fill: none; stroke: var(--accent); stroke-width: 2; stroke-linejoin: round; }
        .chart .valid { fill: none; stroke: var(--valid); stroke-width: 2; stroke-linejoin: round; stroke-dasharray: 5 3; }
        .chart .pt { fill: #fff; stroke-width: 2; }
        .chart .pt.train { stroke: var(--accent); }
        .chart .pt.valid { stroke: var(--valid); }
        .tip { position: fixed; pointer-events: none; background: #0f172a; color: #e2e8f0; font-size: 12px; padding: 7px 10px; border-radius: 6px; display: none; z-index: 9; line-height: 1.5; font-variant-numeric: tabular-nums; }
        .empty { text-align: center; color: var(--faint); padding: 48px 0; font-size: 13px; }

        dl.params { display: grid; grid-template-columns: auto 1fr; gap: 7px 16px; font-size: 13px; }
        dl.params dt { color: var(--muted); }
        dl.params dd { color: var(--fg); font-variant-numeric: tabular-nums; word-break: break-all; }
        dl.params dd.auto { color: var(--faint); font-style: italic; }

        table.cp { width: 100%; border-collapse: collapse; font-size: 12.5px; }
        table.cp th { text-align: left; color: var(--faint); font-weight: 600; font-size: 11px; letter-spacing: .05em; text-transform: uppercase; padding: 0 8px 8px 0; border-bottom: 1px solid var(--border); }
        table.cp td { padding: 8px 8px 8px 0; border-bottom: 1px solid #f1f5f9; font-variant-numeric: tabular-nums; vertical-align: top; word-break: break-all; }
        table.cp code { font-size: 11.5px; background: var(--bg); border-radius: 4px; padding: 1px 5px; }

        .events { max-height: 420px; overflow: auto; font-size: 12.5px; }
        .ev { display: grid; grid-template-columns: 136px 54px 1fr; gap: 10px; padding: 7px 0; border-bottom: 1px solid #f1f5f9; align-items: baseline; }
        .ev:last-child { border-bottom: none; }
        .ev .t { color: var(--faint); font-variant-numeric: tabular-nums; white-space: nowrap; }
        .ev .l { font-size: 10.5px; font-weight: 700; letter-spacing: .05em; padding: 1px 6px; border-radius: 4px; text-align: center; }
        .ev .l.INFO { background: #f1f5f9; color: var(--muted); }
        .ev .l.WARN { background: var(--warn-soft); color: var(--warn); }
        .ev .l.ERROR { background: var(--bad-soft); color: var(--bad); }
        .ev .m { color: var(--fg-soft); white-space: pre-wrap; word-break: break-word; line-height: 1.5; }
        .ev .m .metric { color: var(--accent); font-variant-numeric: tabular-nums; margin-left: 6px; }
    </style>
</head>
<body>
<div class="head">
    <div>
        <div class="label">Fine-tuning Job</div>
        <div class="name"><span id="name">...</span><span id="status" class="pill gray"><span class="dot"></span><span>Loading</span></span></div>
        <div class="sub" id="sub"></div>
    </div>
    <div class="actions">
        <span id="auto"></span>
        <button class="btn" onclick="load(true)">Refresh</button>
    </div>
</div>
<div class="steps" id="steps"></div>
<div id="banner"></div>
<div class="kpis" id="kpis"></div>
<div class="grid">
    <div class="card">
        <div class="hd">Loss Curve
            <div class="right">
                <span class="legend"><i style="background:var(--accent)"></i>train loss</span>
                <span class="legend"><i style="background:var(--valid)"></i>valid loss</span>
                <span id="chartMeta"></span>
            </div>
        </div>
        <div class="bd" id="chartBox"><div class="empty">Waiting for metrics</div></div>
    </div>
    <div class="card">
        <div class="hd">Hyperparameters</div>
        <div class="bd"><dl class="params" id="params"></dl></div>
        <div class="hd" style="border-top:1px solid var(--border)">Checkpoints <div class="right" id="cpMeta"></div></div>
        <div class="bd" id="checkpoints"><div class="empty" style="padding:18px 0">No checkpoints yet</div></div>
    </div>
</div>
<div class="card" style="margin-top:14px">
    <div class="hd">Events <div class="right" id="evMeta"></div></div>
    <div class="bd events" id="events"><div class="empty">No events yet</div></div>
</div>
<div class="tip" id="tip"></div>
<script>
    var JOB_ID = ${rows[0].id?c};
    var TOKEN = new URLSearchParams(location.search).get('_token');
    var BASE = '/${request.contextPath}'.replace(/\/+$/, '');
    var ACTIVE = ['UPLOADING', 'VALIDATING', 'QUEUED', 'RUNNING'];
    var STATUS_LABEL = {DRAFT: 'Draft', UPLOADING: 'Uploading', VALIDATING: 'Validating files', QUEUED: 'Queued', RUNNING: 'Running', SUCCEEDED: 'Succeeded', FAILED: 'Failed', CANCELLED: 'Cancelled'};
    var STEPS = ['DRAFT', 'UPLOADING', 'VALIDATING', 'QUEUED', 'RUNNING'];
    var timer = null, countdown = 0;

    function esc(s) { var d = document.createElement('div'); d.textContent = s == null ? '' : s; return d.innerHTML; }
    function num(v) { if (v == null || v === '') return null; var n = Number(v); return isFinite(n) ? n : null; }
    function fmt(n, digits) { n = num(n); return n == null ? '—' : n.toLocaleString(undefined, {maximumFractionDigits: digits == null ? 0 : digits}); }
    function parseDate(s) { return s ? new Date(String(s).replace(' ', 'T')) : null; }
    function fmtTime(s) { var d = parseDate(s); return d ? d.toLocaleString(undefined, {month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit'}) : ''; }
    function duration(from, to) {
        var a = parseDate(from); if (!a) return '—';
        var b = to ? parseDate(to) : new Date();
        var s = Math.max(0, Math.round((b - a) / 1000));
        var h = Math.floor(s / 3600), m = Math.floor(s % 3600 / 60);
        return h ? h + 'h ' + m + 'm' : m ? m + 'm ' + (s % 60) + 's' : s + 's';
    }
    function pillClass(status) {
        return status === 'SUCCEEDED' ? 'good' : status === 'FAILED' ? 'bad' : status === 'CANCELLED' ? 'warn'
            : status === 'DRAFT' ? 'gray' : 'live';
    }

    function renderSteps(status, reached) {
        var idx = STEPS.indexOf(status), terminal = status === 'SUCCEEDED' || status === 'FAILED' || status === 'CANCELLED';
        // A failed or cancelled job only got as far as the last stage it was seen in
        var doneUpTo = status === 'SUCCEEDED' ? STEPS.length : terminal ? reached : idx;
        var html = '';
        STEPS.forEach(function (s, i) {
            var cls = i < doneUpTo ? 'done' : (!terminal && i === idx) ? 'current' : '';
            html += '<div class="step ' + cls + '"><span class="n">' + (cls === 'done' ? '&#10003;' : i + 1) + '</span>' + STATUS_LABEL[s] + '</div>';
            html += '<div class="line ' + (i < doneUpTo ? 'done' : '') + '"></div>';
        });
        var finalCls = status === 'SUCCEEDED' ? 'done' : status === 'FAILED' ? 'failed' : status === 'CANCELLED' ? 'cancelled' : '';
        var finalLabel = terminal ? STATUS_LABEL[status] : 'Finished';
        html += '<div class="step ' + finalCls + '"><span class="n">' + (status === 'SUCCEEDED' ? '&#10003;' : status === 'FAILED' ? '&#10005;' : status === 'CANCELLED' ? '&#8211;' : STEPS.length + 1) + '</span>' + finalLabel + '</div>';
        document.getElementById('steps').innerHTML = html;
    }

    function renderKpis(j) {
        var lastTrain = null, lastValid = null, lastStep = null;
        (j.events || []).forEach(function (e) {
            if (num(e.trainLoss) != null) lastTrain = num(e.trainLoss);
            if (num(e.validLoss) != null) lastValid = num(e.validLoss);
            if (num(e.step) != null) lastStep = num(e.step);
        });
        var running = ACTIVE.indexOf(j.status) >= 0;
        var kpis = [
            ['Training samples', fmt(j.trainingSamples)],
            ['Dataset tokens', fmt(j.trainingTokens)],
            ['Trained tokens', fmt(j.trainedTokens)],
            [running ? 'Elapsed' : 'Duration', duration(j.startedAt, j.finishedAt)],
            ['Last step', lastStep == null ? '—' : fmt(lastStep)],
            ['Train loss', lastTrain == null ? '—' : lastTrain.toFixed(4)],
            ['Valid loss', lastValid == null ? '—' : lastValid.toFixed(4)],
            ['ETA', j.estimatedFinish && running ? fmtTime(j.estimatedFinish) : '—']
        ];
        document.getElementById('kpis').innerHTML = kpis.map(function (k) {
            return '<div class="kpi"><div class="k">' + k[0] + '</div><div class="v">' + k[1] + '</div></div>';
        }).join('');
    }

    function renderParams(j) {
        var h = j.hyperparameters || {};
        var rows = [
            ['Method', j.method === 'DPO' ? 'Preference (DPO)' : 'Supervised (SFT)'],
            ['Base model', j.baseModel],
            ['Training set', j.trainingDataset],
            ['Validation set', j.validationDataset],
            ['Epochs', h.epochs], ['LR multiplier', h.learningRateMultiplier], ['Batch size', h.batchSize]
        ];
        if (j.method === 'DPO') rows.push(['DPO beta', h.dpoBeta]);
        rows.push(['Seed', h.seed], ['Suffix', h.suffix], ['Remote job', j.remoteJobId], ['Registered LLM', j.registeredLlm]);
        document.getElementById('params').innerHTML = rows.map(function (r) {
            var v = r[1];
            return '<dt>' + r[0] + '</dt>' + (v == null || v === '' ? '<dd class="auto">auto</dd>' : '<dd>' + esc(String(v)) + '</dd>');
        }).join('');
    }

    function renderCheckpoints(j) {
        var cps = j.checkpoints || [];
        document.getElementById('cpMeta').textContent = cps.length ? cps.length + ' saved' : '';
        if (!cps.length) {
            document.getElementById('checkpoints').innerHTML = '<div class="empty" style="padding:18px 0">' + (j.status === 'SUCCEEDED' ? 'The provider kept no intermediate checkpoints' : 'No checkpoints yet') + '</div>';
            return;
        }
        var html = '<table class="cp"><tr><th>Step</th><th>Model</th><th>Metrics</th></tr>';
        cps.forEach(function (c) {
            var m = c.metrics || {}, parts = [];
            Object.keys(m).forEach(function (k) { if (k !== 'step') parts.push(k.replace(/_/g, ' ') + ' ' + Number(m[k]).toFixed(4)); });
            html += '<tr><td>' + fmt(c.step) + '</td><td><code>' + esc(c.model || c.id || '') + '</code></td><td>' + (parts.length ? esc(parts.join(' · ')) : '<span style="color:var(--faint)">—</span>') + '</td></tr>';
        });
        document.getElementById('checkpoints').innerHTML = html + '</table>';
    }

    function renderEvents(j) {
        var events = (j.events || []).slice().reverse();
        document.getElementById('evMeta').textContent = events.length ? events.length + ' events' : '';
        if (!events.length) { document.getElementById('events').innerHTML = '<div class="empty">No events yet</div>'; return; }
        document.getElementById('events').innerHTML = events.map(function (e) {
            var metrics = '';
            if (num(e.step) != null) metrics += '<span class="metric">step ' + fmt(e.step) + '</span>';
            if (num(e.trainLoss) != null) metrics += '<span class="metric">loss ' + num(e.trainLoss).toFixed(4) + '</span>';
            if (num(e.validLoss) != null) metrics += '<span class="metric">valid ' + num(e.validLoss).toFixed(4) + '</span>';
            if (num(e.trainAccuracy) != null) metrics += '<span class="metric">acc ' + num(e.trainAccuracy).toFixed(3) + '</span>';
            return '<div class="ev"><span class="t">' + fmtTime(e.createdAt) + '</span><span class="l ' + esc(e.level || 'INFO') + '">' + esc(e.level || 'INFO') + '</span><span class="m">' + esc(e.message) + metrics + '</span></div>';
        }).join('');
    }

    function renderChart(j) {
        var train = [], valid = [], i = 0;
        (j.events || []).forEach(function (e) {
            var step = num(e.step); if (step == null) step = ++i;
            if (num(e.trainLoss) != null) train.push({x: step, y: num(e.trainLoss)});
            if (num(e.validLoss) != null) valid.push({x: step, y: num(e.validLoss)});
        });
        var box = document.getElementById('chartBox');
        if (!train.length && !valid.length) {
            box.innerHTML = '<div class="empty">' + (ACTIVE.indexOf(j.status) >= 0 ? 'Waiting for the first metrics from the provider' : 'The provider reported no loss metrics for this job') + '</div>';
            document.getElementById('chartMeta').textContent = '';
            return;
        }
        var all = train.concat(valid);
        var W = Math.max(320, box.clientWidth - 2), H = 280, P = {t: 14, r: 16, b: 30, l: 52};
        var xs = all.map(function (p) { return p.x; }), ys = all.map(function (p) { return p.y; });
        var x0 = Math.min.apply(null, xs), x1 = Math.max.apply(null, xs), y0 = Math.min.apply(null, ys), y1 = Math.max.apply(null, ys);
        if (x0 === x1) { x0 -= 1; x1 += 1; }
        var pad = (y1 - y0) * 0.1 || Math.abs(y1) * 0.1 || 0.1; y0 -= pad; y1 += pad;
        function sx(x) { return P.l + (x - x0) / (x1 - x0) * (W - P.l - P.r); }
        function sy(y) { return P.t + (1 - (y - y0) / (y1 - y0)) * (H - P.t - P.b); }
        var svg = '<svg class="chart" viewBox="0 0 ' + W + ' ' + H + '" preserveAspectRatio="none" id="chart">';
        for (var g = 0; g <= 4; g++) {
            var yv = y0 + (y1 - y0) * g / 4, yy = sy(yv);
            svg += '<line class="grid-line" x1="' + P.l + '" x2="' + (W - P.r) + '" y1="' + yy + '" y2="' + yy + '"/>';
            svg += '<text x="' + (P.l - 8) + '" y="' + (yy + 4) + '" text-anchor="end">' + yv.toFixed(yv < 1 ? 3 : 2) + '</text>';
        }
        for (var t = 0; t <= 5; t++) {
            var xv = x0 + (x1 - x0) * t / 5;
            svg += '<text x="' + sx(xv) + '" y="' + (H - 10) + '" text-anchor="middle">' + Math.round(xv) + '</text>';
        }
        svg += '<line class="axis" x1="' + P.l + '" x2="' + (W - P.r) + '" y1="' + (H - P.b) + '" y2="' + (H - P.b) + '"/>';
        function path(points, cls) {
            if (!points.length) return '';
            var d = points.map(function (p, k) { return (k ? 'L' : 'M') + sx(p.x).toFixed(1) + ' ' + sy(p.y).toFixed(1); }).join(' ');
            var dots = points.length <= 60 ? points.map(function (p) { return '<circle class="pt ' + cls + '" cx="' + sx(p.x).toFixed(1) + '" cy="' + sy(p.y).toFixed(1) + '" r="3"/>'; }).join('') : '';
            return '<path class="' + cls + '" d="' + d + '"/>' + dots;
        }
        svg += path(train, 'train') + path(valid, 'valid') + '</svg>';
        box.innerHTML = svg;
        document.getElementById('chartMeta').textContent = train.length + ' train · ' + valid.length + ' valid points';
        // Nearest point tooltip
        var tip = document.getElementById('tip'), chart = document.getElementById('chart');
        chart.addEventListener('mousemove', function (ev) {
            var rect = chart.getBoundingClientRect();
            var mx = (ev.clientX - rect.left) / rect.width * W;
            var best = null, bestD = 1e9;
            all.forEach(function (p) { var d = Math.abs(sx(p.x) - mx); if (d < bestD) { bestD = d; best = p; } });
            if (!best || bestD > 24) { tip.style.display = 'none'; return; }
            var tr = train.filter(function (p) { return p.x === best.x; })[0], va = valid.filter(function (p) { return p.x === best.x; })[0];
            tip.innerHTML = 'step ' + best.x + (tr ? '<br>train ' + tr.y.toFixed(4) : '') + (va ? '<br>valid ' + va.y.toFixed(4) : '');
            tip.style.display = 'block';
            tip.style.left = (ev.clientX + 12) + 'px';
            tip.style.top = (ev.clientY - 10) + 'px';
        });
        chart.addEventListener('mouseleave', function () { tip.style.display = 'none'; });
    }

    function render(j) {
        document.getElementById('name').textContent = j.name;
        var pill = document.getElementById('status');
        pill.className = 'pill ' + pillClass(j.status);
        pill.lastElementChild.textContent = STATUS_LABEL[j.status] || j.status;
        document.getElementById('sub').innerHTML = esc(j.provider || '') + ' · <code>' + esc(j.baseModel || '') + '</code>'
            + (j.fineTunedModel ? ' &rarr; <code>' + esc(j.fineTunedModel) + '</code>' : '')
            + (j.createTime ? ' · created ' + fmtTime(j.createTime) : '');
        var banner = '';
        if (j.status === 'FAILED' && j.errorInfo) banner = '<div class="banner bad">' + esc(j.errorInfo) + '</div>';
        else if (j.status === 'SUCCEEDED') banner = '<div class="banner good">Training finished. ' + (j.registeredLlm ? 'The tuned model is registered as LLM "' + esc(j.registeredLlm) + '".' : 'Use "Register as LLM" on the job row to make the tuned model available in AI Chat.') + '</div>';
        document.getElementById('banner').innerHTML = banner;
        // Without a remote id the job never left the upload stage; with one, the provider at least queued it
        var reached = j.remoteJobId ? (j.trainedTokens || (j.events || []).some(function (e) { return e.step != null; }) ? STEPS.length : STEPS.indexOf('RUNNING')) : STEPS.indexOf('UPLOADING');
        renderSteps(j.status, reached);
        renderKpis(j);
        renderParams(j);
        renderCheckpoints(j);
        renderEvents(j);
        renderChart(j);
        schedule(ACTIVE.indexOf(j.status) >= 0);
    }

    function schedule(active) {
        clearInterval(timer);
        var auto = document.getElementById('auto');
        if (!active) { auto.textContent = ''; return; }
        countdown = 10;
        auto.textContent = 'auto refresh in ' + countdown + 's';
        timer = setInterval(function () {
            if (--countdown <= 0) { load(false); return; }
            auto.textContent = 'auto refresh in ' + countdown + 's';
        }, 1000);
    }

    function load(manual) {
        clearInterval(timer);
        if (manual) document.getElementById('auto').textContent = 'loading...';
        fetch(BASE + '/erupt-api/tune/job/' + JOB_ID + '?_token=' + encodeURIComponent(TOKEN || ''))
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (!res.success) { document.getElementById('banner').innerHTML = '<div class="banner bad">' + esc(res.message || 'Failed to load the job') + '</div>'; return; }
                render(res.data);
            })
            .catch(function (e) { document.getElementById('banner').innerHTML = '<div class="banner bad">' + esc(e.message) + '</div>'; });
    }

    load(false);
</script>
</body>
</html>
