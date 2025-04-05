<script>
    const url = new URL(window.location.href);
    const params = new URLSearchParams(url.search);
    if (${x.devMode?c}) {
        location.href = 'http://localhost:5173?token=' + params.get("_token")
    } else {
        location.href = "${request.contextPath}/llm-chat.html?token=" + params.get("_token")
    }
</script>