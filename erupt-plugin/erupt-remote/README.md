# erupt-remote

Browser-based remote access for Erupt: VNC desktops and SSH terminals over WebSocket.

Manage remote hosts in the auto-generated `Remote Host` table, then click **Connect** to open the desktop page
(an in-app route, `/remote/{id}`). The server bridges a binary WebSocket (`/erupt-remote`) to the host's VNC port and renders it with
[noVNC](https://github.com/novnc/noVNC). Access is gated by the `erupt-upms` token, the `RemoteHost` menu permission and
a one-time ticket; the browser can never choose the target address itself.

When a password is stored for a host the server answers the VNC authentication on the browser's behalf, so the
credential never leaves the backend. Without a stored password (or with servers that only offer other security types,
e.g. macOS Apple Remote Desktop authentication) the RFB handshake is relayed transparently and noVNC prompts the user.

## SSH

Set the protocol to **SSH**, fill in the login user and either a password or a PEM private key (the password then acts as
the key passphrase). The shell is rendered with xterm.js using the same message protocol as erupt-terminal. Host keys
follow trust-on-first-use: they are recorded in `.erupt/remote_known_hosts` and a changed key is refused.

## Configuration

```yaml
erupt:
  remote:
    secret-key: ${ERUPT_REMOTE_SECRET_KEY}   # optional; defaults to a key generated once into .erupt/remote.key
                                              # multi-node deployments must share one key through this property
    max-sessions: 20                          # global concurrent session limit
    idle-timeout-minutes: 30                  # sessions without browser input are closed after this
    connect-timeout-seconds: 5
```

Behind Nginx, forward the WebSocket upgrade for `/erupt-remote` the same way as `/erupt-terminal`:

```nginx
location /erupt-remote {
    proxy_pass http://backend;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_read_timeout 3600s;
}
```

## Tested targets

Any RFB 3.3 / 3.7 / 3.8 server: macOS Screen Sharing (enable "VNC viewers may control screen with password" for
server-side authentication), TigerVNC / x11vnc on Linux, TightVNC / RealVNC on Windows.
