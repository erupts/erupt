# Laya sidecar

[Laya](https://github.com/NandhaKishorM/laya) is an Apache 2.0 System One decision model that
runs on your own machine and answers in the same shape as TypeSafe Jev. Its python package has
no HTTP server, so this directory wraps it in one. Point the **Laya** provider in
*AI Decision → Decision Model* at it and everything else in erupt-ai-decision works unchanged.

## Run

```bash
pip install -r requirements.txt
uvicorn server:app --host 0.0.0.0 --port 8000
```

or

```bash
docker build -t laya-systemone .
docker run -p 8000:8000 -v laya-cache:/root/.cache/huggingface laya-systemone
```

The first start downloads the checkpoints from Hugging Face (about 1.5 GB for all three).
A GPU brings a question down to ~35 ms; on CPU expect 200–500 ms.

## Configure in erupt

| Field      | Value                                                            |
|------------|------------------------------------------------------------------|
| Provider   | Laya                                                             |
| API Domain | `http://127.0.0.1:8000` (or wherever the sidecar listens)        |
| Model      | `auto` — or pin `english`, `multilingual`, `typed-decisions`     |
| API Key    | blank, unless the sidecar was started with `LAYA_API_KEY`        |

`auto` lets Laya's router pick the checkpoint from the language of the state, which is what
the project recommends.

## Limits worth knowing

- Choice options share a fixed token budget inside the model. Past roughly 20 options
  accuracy drops sharply; the sidecar returns HTTP 422 when they no longer fit at all.
  Jev takes up to 255 options; use it, or split the decision, for large label sets.
- Context is 512 tokens on the English checkpoint and 1,024 on the other two. Long states
  are truncated.
- Score questions are Laya's weakest primitive; calibrate the thresholds you branch on.
