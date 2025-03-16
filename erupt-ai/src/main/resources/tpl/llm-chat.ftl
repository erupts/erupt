<script>
    const url = new URL(window.location.href);
    const params = new URLSearchParams(url.search);
    location.href = 'http://localhost:5173?token=' + params.get("_token")
</script>