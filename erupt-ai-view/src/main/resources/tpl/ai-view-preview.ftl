<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <style>
        html, body { margin: 0; height: 100%; overflow: hidden }
        iframe { border: 0; width: 100%; height: 100% }
    </style>
</head>
<body>
<iframe id="view"></iframe>
<script>
    const params = new URLSearchParams(location.search);
    document.getElementById('view').src =
        '${base}/erupt-api/ai-view/render/' + params.get('ids') + '?_token=' + params.get('_token');
</script>
</body>
</html>
