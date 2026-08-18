<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Retrieval Test</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #f5f6f8; padding: 20px; color: #24292f; }
        .bar { display: flex; gap: 10px; margin-bottom: 8px; }
        .bar input[type=text] { flex: 1; padding: 10px 12px; border: 1px solid #d0d7de; border-radius: 6px; font-size: 14px; outline: none; }
        .bar input[type=text]:focus { border-color: #0969da; }
        .bar button { padding: 10px 22px; border: none; border-radius: 6px; background: #0969da; color: #fff; font-size: 14px; cursor: pointer; }
        .bar button:disabled { background: #8c959f; cursor: wait; }
        .opts { display: flex; gap: 18px; margin-bottom: 16px; font-size: 13px; color: #57606a; align-items: center; }
        .opts input { width: 70px; padding: 4px 6px; border: 1px solid #d0d7de; border-radius: 4px; }
        .hit { background: #fff; border: 1px solid #d0d7de; border-radius: 8px; padding: 14px 16px; margin-bottom: 12px; }
        .hit .meta { display: flex; justify-content: space-between; font-size: 12px; color: #57606a; margin-bottom: 8px; }
        .hit .score-track { height: 5px; background: #eaeef2; border-radius: 3px; margin-bottom: 10px; }
        .hit .score-fill { height: 5px; background: #2da44e; border-radius: 3px; }
        .hit .text { font-size: 14px; line-height: 1.6; white-space: pre-wrap; word-break: break-word; }
        .empty { text-align: center; color: #8c959f; padding: 60px 0; font-size: 14px; }
    </style>
</head>
<body>
<div class="bar">
    <input type="text" id="query" placeholder="Ask something to see what this knowledge base recalls..." onkeydown="if(event.key==='Enter')search()">
    <button id="btn" onclick="search()">Search</button>
</div>
<div class="opts">
    <label>Top K <input type="number" id="topK" min="1" max="50" value="${rows[0].topK?c}"></label>
    <label>Min Score <input type="number" id="minScore" min="0" max="1" step="0.05" value="${rows[0].minScore?c}"></label>
</div>
<div id="result"><div class="empty">Retrieval test for knowledge base: ${rows[0].name}</div></div>
<script>
    var KB_ID = ${rows[0].id?c};
    var TOKEN = new URLSearchParams(location.search).get('_token');
    var BASE = '/${request.contextPath}'.replace(/\/+$/, '');

    function esc(s) {
        var div = document.createElement('div');
        div.textContent = s == null ? '' : s;
        return div.innerHTML;
    }

    function search() {
        var query = document.getElementById('query').value.trim();
        if (!query) return;
        var btn = document.getElementById('btn');
        btn.disabled = true;
        var url = BASE + '/erupt-api/rag/retrieve?kbId=' + KB_ID
            + '&query=' + encodeURIComponent(query)
            + '&topK=' + document.getElementById('topK').value
            + '&minScore=' + document.getElementById('minScore').value
            + '&_token=' + encodeURIComponent(TOKEN || '');
        fetch(url).then(function (r) { return r.json(); }).then(function (res) {
            btn.disabled = false;
            var box = document.getElementById('result');
            if (!res.success) {
                box.innerHTML = '<div class="empty">' + esc(res.message || 'Retrieval failed') + '</div>';
                return;
            }
            var hits = res.data || [];
            if (!hits.length) {
                box.innerHTML = '<div class="empty">No chunk above the score threshold</div>';
                return;
            }
            box.innerHTML = hits.map(function (hit) {
                var pct = Math.max(0, Math.min(1, hit.score)) * 100;
                return '<div class="hit">'
                    + '<div class="meta"><span>' + esc(hit.document || '-') + (hit.seq != null ? ' · #' + hit.seq : '') + '</span>'
                    + '<span>score ' + (hit.score != null ? hit.score.toFixed(4) : '-') + '</span></div>'
                    + '<div class="score-track"><div class="score-fill" style="width:' + pct + '%"></div></div>'
                    + '<div class="text">' + esc(hit.text) + '</div>'
                    + '</div>';
            }).join('');
        }).catch(function (e) {
            btn.disabled = false;
            document.getElementById('result').innerHTML = '<div class="empty">' + esc(e.message) + '</div>';
        });
    }
</script>
</body>
</html>
