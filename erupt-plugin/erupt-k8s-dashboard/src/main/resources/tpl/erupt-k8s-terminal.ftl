<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title?html}</title>
    <link rel="stylesheet" href="${base}/erupt-k8s-dashboard/xterm/xterm.css">
    <style>
        html, body { height: 100%; margin: 0; background: #0f1115; color: #d4d4d4;
            font: 12px/1.5 -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
        body { display: flex; flex-direction: column; }
        header { display: flex; align-items: center; gap: 10px; padding: 6px 12px; border-bottom: 1px solid #23262d; flex: none; }
        header .title { font-weight: 600; color: #f0f0f0; }
        header .mode { padding: 1px 8px; border-radius: 10px; background: #23262d; color: #9aa0a6; text-transform: uppercase; font-size: 11px; }
        header .state { margin-left: auto; color: #9aa0a6; }
        header .state.live { color: #3fb950; }
        header .state.dead { color: #f85149; }
        select { background: #181b21; color: #d4d4d4; border: 1px solid #30343c; border-radius: 4px; padding: 2px 6px; font: inherit; }
        #term { flex: 1; min-height: 0; padding: 4px 0 0 8px; }
    </style>
</head>
<body>
<header>
    <span class="title">${title?html}</span>
    <span class="mode">${mode?html}</span>
    <select id="container"></select>
    <span class="state" id="state">connecting</span>
</header>
<div id="term"></div>
<script src="${base}/erupt-k8s-dashboard/xterm/xterm.js"></script>
<script src="${base}/erupt-k8s-dashboard/xterm/addon-fit.js"></script>
<script>
    (function () {
        var BASE = '${base?js_string}';
        var MODE = '${mode?js_string}';
        // Each container has its own one-time ticket; a used ticket needs a page reload
        var CONTAINERS = [<#list containers as c>{name: '${c.name?js_string}', ticket: '${c.ticket?js_string}'}<#sep>, </#sep></#list>];
        var used = {};
        var state = document.getElementById('state');
        var select = document.getElementById('container');
        CONTAINERS.forEach(function (c, i) {
            var option = document.createElement('option');
            option.value = String(i);
            option.textContent = c.name;
            select.appendChild(option);
        });
        select.style.display = CONTAINERS.length > 1 ? '' : 'none';

        var term = new Terminal({cursorBlink: MODE === 'exec', disableStdin: MODE !== 'exec', convertEol: MODE !== 'exec',
            scrollback: 10000, fontFamily: 'Menlo, Monaco, Consolas, monospace', fontSize: 13, theme: {background: '#0f1115'}});
        var fit = new FitAddon.FitAddon();
        term.loadAddon(fit);
        term.open(document.getElementById('term'));
        fit.fit();

        var ws = null;

        function setState(text, cls) {
            state.textContent = text;
            state.className = 'state ' + (cls || '');
        }

        function send(message) {
            if (ws && ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify(message));
        }

        function connect(index) {
            if (ws) ws.close();
            term.reset();
            var c = CONTAINERS[index];
            if (used[index]) {
                setState('reload to reconnect', 'dead');
                return;
            }
            used[index] = true;
            var url = (location.protocol === 'https:' ? 'wss://' : 'ws://') + location.host + BASE
                + '/erupt-k8s-terminal?ticket=' + encodeURIComponent(c.ticket);
            ws = new WebSocket(url);
            ws.binaryType = 'arraybuffer';
            ws.onopen = function () {
                setState('connected', 'live');
                send({type: 'resize', cols: term.cols, rows: term.rows});
                term.focus();
            };
            ws.onmessage = function (event) {
                term.write(typeof event.data === 'string' ? event.data : new Uint8Array(event.data));
            };
            ws.onclose = function (event) {
                setState(event.code === 1000 ? 'ended' : 'disconnected', 'dead');
            };
        }

        term.onData(function (data) {
            if (MODE === 'exec') send({type: 'input', data: data});
        });
        term.onResize(function (size) {
            send({type: 'resize', cols: size.cols, rows: size.rows});
        });
        window.addEventListener('resize', function () {
            fit.fit();
        });
        select.addEventListener('change', function () {
            connect(Number(select.value));
        });
        if (CONTAINERS.length) connect(0);
        else setState('no containers', 'dead');
    })();
</script>
</body>
</html>
