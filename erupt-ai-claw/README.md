# erupt-ai-claw

**Drop-in admin agent for Erupt — turns natural language into Erupt admin operations.**

Built on top of [`erupt-ai`](../erupt-ai), this module ships a preconfigured agent ("Claw") with a ready-made system prompt and a curated set of tools so your users can manage an Erupt admin in plain language.

## What's inside

- **`EruptModelTools`** — discover Erupt models, inspect schemas, read and mutate data
- **`EruptRunTimeTools`** — inspect runtime state (scheduled tasks, environment, Spring beans)
- **`EruptSkillTools`** — discover and execute installed Erupt skills
- **`EruptSystemTools`** — execute shell commands (permission-gated)
- **`ClawSystemPrompt`** — opinionated system prompt that wires the tools above into a coherent agent
- **UPMS-aware execution** — every action respects the current user's permissions via `erupt-upms`

## Quick start

`erupt-ai-claw` is enabled by default. To toggle it explicitly:

```yaml
erupt:
  ai:
    claw:
      enabled: true             # default: true — turns the agent on
      enable-exec-shell: true   # default: true — gates the shell tool
```

Open the AI panel in your Erupt admin and start talking.

## Relationship to `erupt-ai`

| Module | Role | Choose when… |
|---|---|---|
| [`erupt-ai`](../erupt-ai) | LLM / MCP / A2A plumbing | You want raw access to providers and protocols, or you're building your own agent |
| **`erupt-ai-claw`** *(this)* | Ready-made admin agent | You want an out-of-the-box assistant for your Erupt UI |

`erupt-ai-claw` depends on `erupt-ai` + `erupt-upms`; you don't need to declare `erupt-ai` separately if you already include this module.
