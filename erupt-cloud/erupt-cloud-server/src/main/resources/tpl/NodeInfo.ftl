<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <style>
        * {
            margin: 0;
            padding: 0;
        }
    </style>
</head>
<body>
<h1 style="text-align: center;letter-spacing:16px;margin: 55px 0">
    <#list rows as row>
        ${row.accessToken}
    </#list>
</h1>
</body>
</html>