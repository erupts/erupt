"""
System One HTTP sidecar for Laya.

Laya (https://github.com/NandhaKishorM/laya) ships as a python package with no HTTP layer.
This file puts its Router behind the one endpoint erupt-ai-decision speaks, POST /v1/systemone,
in TypeSafe Jev's request and response shape, so the "Laya" provider in erupt is a plain URL.

    pip install -r requirements.txt
    uvicorn server:app --host 0.0.0.0 --port 8000

Environment:
    LAYA_API_KEY   optional; when set, callers must send "Authorization: Bearer <key>"
    LAYA_PRELOAD   "1" (default) keeps every checkpoint resident, "0" loads on demand
    LAYA_DEVICE    "cuda", "cpu" or "mps"; default lets torch decide
"""

import os
from typing import Any, Dict, Optional

from fastapi import FastAPI, Header, HTTPException
from laya import Router
from pydantic import BaseModel

# Checkpoint names Laya's Router accepts as an override; anything else means "route for me"
CHECKPOINTS = {"english", "multilingual", "typed-decisions"}

API_KEY = os.environ.get("LAYA_API_KEY") or None

router = Router(
    preload=os.environ.get("LAYA_PRELOAD", "1") == "1",
    device=os.environ.get("LAYA_DEVICE") or None,
)

app = FastAPI(title="Laya System One", docs_url=None, redoc_url=None)


class SystemOneRequest(BaseModel):
    state: Any
    questions: Dict[str, Dict[str, Any]]
    model: Optional[str] = None


@app.post("/v1/systemone")
def system_one(body: SystemOneRequest, authorization: Optional[str] = Header(default=None)):
    if API_KEY and authorization != f"Bearer {API_KEY}":
        raise HTTPException(status_code=401, detail="invalid or missing API key")
    if not body.questions:
        raise HTTPException(status_code=422, detail="questions must not be empty")
    checkpoint = body.model if body.model in CHECKPOINTS else None
    try:
        return router.predict(body.state, body.questions, model=checkpoint)
    except (ValueError, KeyError) as e:
        # Laya raises on malformed questions and on option sets that overflow its head budget
        raise HTTPException(status_code=422, detail=str(e))


@app.get("/health")
def health():
    return {"status": "ok", "checkpoints": sorted(CHECKPOINTS)}
