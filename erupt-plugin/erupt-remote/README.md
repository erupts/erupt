# erupt-remote

Browser-based remote access for Erupt: VNC desktops and SSH terminals over WebSocket.

Manage remote hosts in the auto-generated `Remote Host` table, then click **Connect** to open the desktop page
(an in-app route, `/remote/{id}`). The server bridges a binary WebSocket (`/erupt-remote`) to the host's VNC port and renders it with
[noVNC](https://github.com/novnc/noVNC). Access is gated by the `erupt-upms` token, the `RemoteHost` menu permission (`@EruptMenuAuth` on the ticket API,
the same check on the WebSocket) and a one-time ticket; the browser can never choose the target address itself.

## Authorized users

The menu permission says a user works with remote hosts at all; **Authorized Users** on a host says which ones,
and nothing else grants them.

| | |
|---|---|
| one or more named | exactly those users see the host and may connect, whatever their role grants |
| nobody named | super admins only |
| super admin | sees every host, always |

The list is filtered in the query, and the ticket endpoint repeats the check: a host left out of the table is
also refused when its id is typed by hand, and a host whose authorization changed after a page was loaded is
judged on its current state at the next connect.

When a password is stored for a host the server answers the VNC authentication on the browser's behalf, so the
credential never leaves the backend. Without a stored password (or with servers that only offer other security types,
e.g. macOS Apple Remote Desktop authentication) the RFB handshake is relayed transparently and noVNC prompts the user.

## SSH

Set the protocol to **SSH**, fill in the login user and either a password or a PEM private key (the password then acts as
the key passphrase). The shell is rendered with xterm.js using the same message protocol as erupt-terminal. Host keys
follow trust-on-first-use: they are recorded in `.erupt/remote_known_hosts` and a changed key is refused.

## File transfer (SFTP)

SSH hosts get a **Files** button on the terminal page that opens an SFTP panel next to the shell: browse, upload
(button or drag-and-drop), download, create a folder, delete a file or an empty directory, and type an entry's path
into the terminal. It uses the same credentials, host-key policy and authorized-user check as the shell, so the
remote permissions are those of the SSH login user. Switch it off per host with **File Transfer** when users should
type but not carry files in or out.

Each operation opens its own short-lived SSH session (`/erupt-api/remote/sftp/{id}/…`), so a transfer is never tied
to the terminal WebSocket. Uploads stream the raw request body into SFTP — no temp file, no multipart size limit;
size a reverse proxy's `client_max_body_size` accordingly. Deletion is never recursive: clearing a tree is a shell job.
VNC hosts have no file channel (the RFB protocol has none); configure the same machine as an SSH host for files.

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
