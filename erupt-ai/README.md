# erupt-ai

**LLM and MCP unified access layer — the foundational AI building block for the Erupt ecosystem.**

Powered by [LangChain4j](https://github.com/langchain4j/langchain4j).

## What this module provides

- **Multi-provider LLM access** — 17 built-in adapters: OpenAI / ChatGPT, Anthropic Claude, Google Gemini, Ollama (local), DeepSeek, Qwen, GLM, Doubao, Grok, Mimo, MiniMax, Mistral, Moonshot, OpenRouter, Together, Fireworks.
- **MCP (Model Context Protocol)** client + server wiring for tool-calling workflows.
- **A2A (agent-to-agent)** protocol support.
- **Chat / Agent / MCP REST endpoints**, exposed as Erupt-managed admin entries — no extra controller code needed.
- **LLM role management & memory tools** for conversational state.

## When to use this module

Add `erupt-ai` when you need:

- A unified API to call any major LLM provider from your Erupt app
- MCP / A2A wiring without writing protocol code yourself
- A foundation to build your **own** agent on top of

## Looking for a turn-key admin agent?

See **[`erupt-ai-claw`](../erupt-ai-claw)** — it builds on `erupt-ai` and ships a preconfigured agent ("Claw") with tools for natural-language CRUD against your Erupt models, runtime inspection, shell execution and skill invocation. If you want an out-of-the-box assistant rather than raw plumbing, start there.
