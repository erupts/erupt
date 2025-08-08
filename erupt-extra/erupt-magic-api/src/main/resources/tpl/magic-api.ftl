<!DOCTYPE html>
<html lang="en">
<head>
    <title>erupt-magic-api</title>
    <style>
        html, body {
            padding: 0;
            margin: 0;
            width: 100%;
            height: 100%;
        }
    </style>
</head>
<body>
<script>
    window.MAGIC_EDITOR_CONFIG = {
        request: {
            beforeSend: function (config) {
                config.headers.token = '${token}';
                return config;
            }
        },
        header: {
            skin: true,
            document: true,
            repo: false,
            qqGroup: false
        },
        getMagicTokenValue: function () {
            return '${token}';
        },
        checkUpdate: false,
        title: 'erupt-magic-api'
    }
    <#if web??>
    document.write('<script src="' + document.location.origin + '${request.contextPath}/app.js?v=${hash}"><\/script>');
    document.write('<iframe src="' + document.location.origin + '${request.contextPath}${web!'/not-config-magic.web'}?v=${v}" style="border: 0;vertical-align: bottom;" width="100%" height="100%"></iframe>');
    <#else>
    document.write("<h1 style='margin:0;padding-top:20%;text-align: center;font-family: Monoton,serif'>'magic-api.web' is not configured</h1>")
    </#if>
</script>
</body>
</html>
