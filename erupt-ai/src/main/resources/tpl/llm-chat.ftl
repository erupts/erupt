<script>
    const url = new URL(window.location.href);
    const param = "?token=" + new URLSearchParams(url.search).get("_token") + "&llm=${rows[0].id}"
    if (${x.devMode?c}) {
        location.href = 'http://localhost:5173' + param
    } else {
        location.href = "${request.contextPath}/llm-chat.html" + param
    }
</script>