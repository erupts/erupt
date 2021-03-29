<!DOCTYPE html>
<html lang="en">
<head>
    <title>magic-api</title>
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
                config.headers.token = parent.getAppToken().token;
                return config;
            }
        }
    }
</script>
<iframe src="${request.contextPath}${web}"
        style="border: 0" width="100%" height="100%"></iframe>
</body>
</html>