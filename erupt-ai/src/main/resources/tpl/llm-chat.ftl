<script>
    const url = new URL(window.location.href);
    const param = "?_token=" + new URLSearchParams(url.search).get("_token") + "&llm=${rows[0].id}" + "&_t=" + new Date().getTime()
    if (${x.devMode?c}) {
        location.href = 'http://localhost:5173' + param
    } else {
        location.href = "${request.contextPath}/ai-chat.html" + param
    }
</script>