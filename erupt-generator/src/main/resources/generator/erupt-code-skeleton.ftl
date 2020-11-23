<html>
<head>
    <meta charset="utf-8">
    <style>
        * {
            margin: 0;
            padding: 0;
        }

        body {
            background: #272822;
        }

        .annotation {
            color: #FFEB3B !important;
        }

        pre {
            background: #272822;
        }

        pre code {
            font-family: Courier New, Menlo, Monaco, Consolas, monospace !important;
        }

        .tool div {
            padding: 6px 12px;
            color: #fff;
            background: #607d8b91;
            cursor: pointer;
            border-right: 1px solid #666;
        }

        .tool div:hover {
            transition: all 600ms;
            background: #08f;
        }
    </style>
    <link rel="stylesheet" href="/prism/prism-okaidia.min.css">
</head>
<body>
<div style="display: flex;position: fixed;right: 0;top: 0;" class="tool">
    <div>复制</div>
    <div>下载</div>
</div>
<pre id="erupt-code" style="margin: 0;border-radius: 0"><code
            class="prism language-java"><#include "erupt-code.java"/></code></pre>
<script src="/prism/prism.min.js"></script>
<script src="/prism/prism-java.min.js"></script>
<script>
    Prism.highlightAllUnder(document.getElementById("erupt-code"));
</script>
</body>
</html>