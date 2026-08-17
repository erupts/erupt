# erupt-terminal

Browser-based server terminal for Erupt.

Exposes a WebSocket endpoint that bridges a PTY shell (via [pty4j](https://github.com/JetBrains/pty4j)) to an in-browser terminal (xterm.js). Access is gated by `erupt-upms` token and menu permission checks. Supports terminal resize, idle timeout, and works on both Unix and Windows.
