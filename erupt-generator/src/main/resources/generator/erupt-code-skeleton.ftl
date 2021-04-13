<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">

    <link rel="stylesheet" href="${request.contextPath}/assets/bootstrap.min.css">
    <link rel="stylesheet" href="${request.contextPath}/assets/prism/prism-okaidia.min.css">
    <style>
        body {
            background: #272822;
        }

        .annotation {
            color: #FFEB3B !important;
        }

        pre {
            color: #fff;
        }

        pre code {
            font-family: Courier New, Menlo, Monaco, Consolas, monospace !important;
        }

        #copy-success {
            position: fixed;
            top: 10px;
            right: 50px;
            transition: all 1s;
            display: none;
        }
    </style>
    <title></title>
</head>
<body>
<div id="copy-success" class="alert alert-success">复制成功 !</div>
<pre id="erupt-code" style="margin: 0;border-radius: 0;background: #272822;"><code
            class="prism language-java"><#include "erupt-code.java"/></code></pre>
<div style="padding: 18px;position: fixed;bottom: 0;right: 0">
    <button class="btn btn-outline-success btn-sm copy">复制</button>&nbsp;&nbsp;&nbsp;
    <button class="btn btn-outline-warning btn-sm" onclick="download()">下载</button>
</div>
<script src="${request.contextPath}/assets/prism/prism.min.js"></script>
<script src="${request.contextPath}/assets/prism/prism-java.min.js"></script>
<script src="${request.contextPath}/assets/clipboard.min.js"></script>
<script>
    Prism.highlightAllUnder(document.getElementById("erupt-code"));
    window.parent.document.getElementsByClassName("ant-modal-header")[0].remove();
    window.parent.document.getElementsByClassName("ant-modal-close-x")[0].style.color = "#fff";
    window.parent.document.getElementsByTagName("iframe")[0].style.height = '93vh';


    var clipboard = new ClipboardJS('.copy', {
        text: function () {
            return document.getElementById("erupt-code").innerText;
        }
    });
    clipboard.on('success', function (e) {
        e.clearSelection();
        var ele = document.getElementById("copy-success");
        ele.style.display = "block";
        setTimeout(function () {
            ele.style.display = "none";
        }, 1600);
    });

    //无需接口即可实现下载
    function download() {
        var code = document.getElementById("erupt-code").innerText;
        var url = window.URL.createObjectURL(new Blob([code.replace(/&gt;/g, '>').replace(/&lt;/g, '<')]))
        var link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.download = '${rows[0].className}.java'; //下载后文件名
        link.setAttribute('download', link.download)
        document.body.appendChild(link)
        link.click()
        link.remove()
    }
</script>
</body>
</html>