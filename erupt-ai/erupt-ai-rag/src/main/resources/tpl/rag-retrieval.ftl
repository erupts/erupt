<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Retrieval Test</title>
    <style>
        :root {
            --bg: #f8fafc;
            --surface: #ffffff;
            --border: #e2e8f0;
            --fg: #1e293b;
            --fg-soft: #334155;
            --muted: #64748b;
            --faint: #94a3b8;
            --accent: #2563eb;
            --accent-hover: #1d4ed8;
            --ring: rgba(37, 99, 235, .18);
            --good: #16a34a;
            --mid: #2563eb;
            --low: #d97706;
            --radius: 10px;
            --shadow-sm: 0 1px 2px rgba(15, 23, 42, .05);
            --shadow-md: 0 4px 12px rgba(15, 23, 42, .08);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif;
            background: var(--bg); color: var(--fg);
            padding: 24px 28px; font-size: 14px;
            -webkit-font-smoothing: antialiased;
        }
        .head { margin-bottom: 14px; }
        .head .label {
            font-size: 11px; font-weight: 600; letter-spacing: .08em;
            text-transform: uppercase; color: var(--faint);
        }
        .head .kb { font-size: 18px; font-weight: 600; margin-top: 2px; }

        .bar {
            display: flex; align-items: center; gap: 10px;
            background: var(--surface); border: 1px solid var(--border);
            border-radius: var(--radius); padding: 6px 6px 6px 14px;
            box-shadow: var(--shadow-sm);
            transition: border-color .15s, box-shadow .15s;
        }
        .bar:focus-within { border-color: var(--accent); box-shadow: 0 0 0 3px var(--ring); }
        .bar svg { flex: none; color: var(--faint); }
        .bar input[type=text] {
            flex: 1; border: none; outline: none; background: transparent;
            font-size: 15px; color: var(--fg); min-width: 0;
        }
        .bar input[type=text]::placeholder { color: var(--faint); }
        .bar button {
            flex: none; display: inline-flex; align-items: center; gap: 8px;
            padding: 9px 20px; border: none; border-radius: 7px;
            background: var(--accent); color: #fff; font-size: 14px; font-weight: 500;
            cursor: pointer; transition: background .15s, transform .1s;
        }
        .bar button:hover { background: var(--accent-hover); }
        .bar button:active { transform: scale(.97); }
        .bar button:disabled { background: #93b4f5; cursor: wait; transform: none; }
        .spinner {
            width: 14px; height: 14px; border-radius: 50%;
            border: 2px solid rgba(255, 255, 255, .4); border-top-color: #fff;
            animation: spin .7s linear infinite; display: none;
        }
        .loading .spinner { display: inline-block; }
        @keyframes spin { to { transform: rotate(360deg); } }

        .opts {
            display: flex; align-items: center; gap: 20px;
            margin: 12px 2px 18px; font-size: 12.5px; color: var(--muted);
        }
        .opts label { display: inline-flex; align-items: center; gap: 7px; }
        .opts input {
            width: 68px; padding: 5px 8px; font-size: 13px; color: var(--fg);
            border: 1px solid var(--border); border-radius: 6px; background: var(--surface);
            outline: none; transition: border-color .15s, box-shadow .15s;
        }
        .opts input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px var(--ring); }
        .opts .summary { margin-left: auto; color: var(--faint); font-variant-numeric: tabular-nums; }

        .hit {
            background: var(--surface); border: 1px solid var(--border);
            border-radius: var(--radius); padding: 14px 16px 15px; margin-bottom: 12px;
            box-shadow: var(--shadow-sm);
            transition: box-shadow .15s, transform .15s;
            animation: rise .25s ease-out backwards;
        }
        .hit:hover { box-shadow: var(--shadow-md); transform: translateY(-1px); }
        @keyframes rise { from { opacity: 0; transform: translateY(6px); } }

        .hit .meta { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
        .hit .doc { font-weight: 500; color: var(--fg-soft); font-size: 13px; }
        .hit .seq {
            font-size: 11px; color: var(--muted); background: var(--bg);
            border: 1px solid var(--border); border-radius: 99px; padding: 1px 8px;
        }
        .hit .score {
            margin-left: auto; font-size: 12px; font-weight: 600;
            font-variant-numeric: tabular-nums;
        }
        .hit .score-track { height: 4px; background: #eef2f7; border-radius: 2px; margin-bottom: 11px; overflow: hidden; }
        .hit .score-fill { height: 100%; border-radius: 2px; transition: width .3s ease-out; }
        .hit .text { font-size: 14px; line-height: 1.65; color: var(--fg-soft); white-space: pre-wrap; word-break: break-word; }

        .empty { text-align: center; color: var(--faint); padding: 64px 0 56px; }
        .empty svg { display: block; margin: 0 auto 14px; color: #cbd5e1; }
        .empty .hint { font-size: 14px; }

        .skeleton {
            background: var(--surface); border: 1px solid var(--border);
            border-radius: var(--radius); padding: 14px 16px; margin-bottom: 12px;
        }
        .skeleton .line {
            height: 12px; border-radius: 4px; margin-bottom: 10px;
            background: linear-gradient(90deg, #eef2f7 25%, #f6f8fb 45%, #eef2f7 65%);
            background-size: 200% 100%;
            animation: shimmer 1.2s linear infinite;
        }
        .skeleton .line:last-child { margin-bottom: 0; }
        @keyframes shimmer { to { background-position: -200% 0; } }

        @media (prefers-reduced-motion: reduce) {
            .hit, .skeleton .line { animation: none; }
            .hit, .bar, .bar button, .score-fill { transition: none; }
        }
    </style>
</head>
<body>
<div class="head">
    <div class="label">Retrieval Test</div>
    <div class="kb">${rows[0].name}</div>
</div>
<div class="bar">
    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path>
    </svg>
    <input type="text" id="query" placeholder="Ask something to see what this knowledge base recalls..."
           onkeydown="if(event.key==='Enter')search()" autofocus>
    <button id="btn" onclick="search()"><span class="spinner" aria-hidden="true"></span><span>Search</span></button>
</div>
<div class="opts">
    <label>Top K <input type="number" id="topK" min="1" max="50" value="${rows[0].topK?c}"></label>
    <label>Min Score <input type="number" id="minScore" min="0" max="1" step="0.05" value="${rows[0].minScore?c}"></label>
    <span class="summary" id="summary"></span>
</div>
<div id="result"></div>
<script>
    var KB_ID = ${rows[0].id?c};
    var TOKEN = new URLSearchParams(location.search).get('_token');
    var BASE = '/${request.contextPath}'.replace(/\/+$/, '');

    var EMPTY_ICON = '<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">'
        + '<circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>';

    function esc(s) {
        var div = document.createElement('div');
        div.textContent = s == null ? '' : s;
        return div.innerHTML;
    }

    function empty(hint) {
        return '<div class="empty">' + EMPTY_ICON + '<div class="hint">' + hint + '</div></div>';
    }

    function scoreColor(score) {
        return score >= 0.8 ? 'var(--good)' : score >= 0.65 ? 'var(--mid)' : 'var(--low)';
    }

    function skeletons() {
        var card = '<div class="skeleton"><div class="line" style="width:38%"></div>'
            + '<div class="line" style="width:96%"></div><div class="line" style="width:82%"></div></div>';
        return card + card + card;
    }

    document.getElementById('result').innerHTML = empty('Type a question above to see which chunks this knowledge base recalls');

    function search() {
        var query = document.getElementById('query').value.trim();
        if (!query) return;
        var btn = document.getElementById('btn');
        var box = document.getElementById('result');
        var summary = document.getElementById('summary');
        btn.disabled = true;
        btn.classList.add('loading');
        summary.textContent = '';
        box.innerHTML = skeletons();
        var start = Date.now();
        var url = BASE + '/erupt-api/rag/retrieve?kbId=' + KB_ID
            + '&query=' + encodeURIComponent(query)
            + '&topK=' + document.getElementById('topK').value
            + '&minScore=' + document.getElementById('minScore').value
            + '&_token=' + encodeURIComponent(TOKEN || '');
        fetch(url).then(function (r) { return r.json(); }).then(function (res) {
            done();
            if (!res.success) {
                box.innerHTML = empty(esc(res.message || 'Retrieval failed'));
                return;
            }
            var hits = res.data || [];
            if (!hits.length) {
                box.innerHTML = empty('No chunk above the score threshold');
                return;
            }
            summary.textContent = hits.length + (hits.length > 1 ? ' chunks' : ' chunk') + ' · ' + (Date.now() - start) + ' ms';
            box.innerHTML = hits.map(function (hit, i) {
                // The backend GSON writes fractional doubles as strings to dodge JS precision loss
                var score = Number(hit.score);
                var pct = Math.max(0, Math.min(1, isFinite(score) ? score : 0));
                var color = scoreColor(pct);
                return '<div class="hit" style="animation-delay:' + (i * 40) + 'ms">'
                    + '<div class="meta"><span class="doc">' + esc(hit.document || '-') + '</span>'
                    + (hit.seq != null ? '<span class="seq">#' + hit.seq + '</span>' : '')
                    + '<span class="score" style="color:' + color + '">' + (isFinite(score) ? score.toFixed(4) : '-') + '</span></div>'
                    + '<div class="score-track"><div class="score-fill" style="width:' + (pct * 100) + '%;background:' + color + '"></div></div>'
                    + '<div class="text">' + esc(hit.text) + '</div>'
                    + '</div>';
            }).join('');
        }).catch(function (e) {
            done();
            box.innerHTML = empty(esc(e.message));
        });
        function done() {
            btn.disabled = false;
            btn.classList.remove('loading');
        }
    }
</script>
</body>
</html>
