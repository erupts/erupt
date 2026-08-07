# erupt-ai-staff

**Digital AI employees for Erupt — autonomous staff with duties, schedules and work reports.**

Built on top of [`erupt-ai`](../erupt-ai) and [`erupt-ai-claw`](../erupt-ai-claw), this module turns the admin agent into a workforce: define AI staff members with a persona and a system account, assign them tasks, and let them work on a cron schedule — no human in the loop.

## Concepts

| Model | Role |
|---|---|
| **AI Staff** | A digital employee: name, position, duty (persona prompt), an optional dedicated LLM, and a bound UPMS account |
| **Staff Task** | A work order assigned to a staff member — a Markdown instruction, run on a cron schedule or on demand ("Execute Now") |
| **Staff Channel** | An IM endpoint (DingTalk / WeCom / Feishu / Slack): pushes work reports outbound and routes inbound bot messages to an answering staff |
| **Staff Work Log** | One record per run: status, duration, and the Markdown work report written by the staff |

## How it works

1. Each staff member is bound to an **Erupt user account**. Every task run executes under that account, so the account's roles (via `erupt-ai`'s AI Role config) decide exactly **which AI tools the staff may call** — permissions, not prompts, are the guardrail.
2. On each run the staff's **duty** is appended to the system prompt, the task **instruction** is sent as the user message, and tools run in a ReAct loop (all `erupt-ai-claw` tools, `@AiToolbox` beans, and connected MCP servers are available).
3. The final answer is filed as a **work report** in the Staff Work Log.

## Quick start

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-ai-staff</artifactId>
</dependency>
```

1. Create an account for the staff (e.g. `ai-analyst`) and give it a role with the allowed AI tools.
2. Menu **AI Staff → AI Staff**: create the employee, bind the account, write its duty.
3. Menu **AI Staff → Staff Task**: assign a task with a cron expression (e.g. `0 0 9 * * ?` — daily 9 AM report), or leave cron blank and use **Execute Now**.
4. Read the results in **Staff Work Log**.

## Channels

`StaffChannel` is the channel abstraction (registry idiom, same as `LlmCore`): implement `code()` / `configTemplate()` / `push()` / `onCallback()` / `reply()` in a Spring bean and it self-registers — the four built-ins are `DingTalk`, `WeCom`, `Feishu`, `Slack`.

Two directions per channel:

- **Outbound** — a Staff Task can select a channel as **Report To**; the work report is pushed after each run (group robot webhooks). **Test Push** row operation verifies the config.
- **Inbound** — point the platform's bot callback at `{domain}/erupt-api/ai-staff/channel/{callback-code}`. The endpoint verifies the platform signature (DingTalk HMAC, WeCom AES+SHA1, Feishu AES, Slack signing secret), handles URL-verification handshakes automatically, acks immediately, and answers asynchronously as the channel's **Answering Staff** — with the staff's duty prompt and its account's tool permissions. Conversations are stateless (one turn per message).

Per-platform setup:

| Channel | Push (webhook) | Inbound |
|---|---|---|
| DingTalk | Group robot webhook + optional signing secret | Enterprise robot HTTP callback; replies via `sessionWebhook` |
| WeCom | Group robot webhook | Self-built app message callback (`token` + `encodingAesKey`); replies via app message-send API (`corpId`/`corpSecret`/`agentId`) |
| Feishu | Custom bot webhook + optional signing secret | Event subscription `im.message.receive_v1` (`encryptKey` if encryption on); replies via IM API (`appId`/`appSecret`) |
| Slack | Incoming webhook | Events API `app_mention` + `message.im` (`signingSecret`); replies via `chat.postMessage` (`botToken`) |

## Relationship to sibling modules

| Module | Role |
|---|---|
| [`erupt-ai`](../erupt-ai) | LLM / MCP / A2A plumbing, tool registry, role-based tool permissions |
| [`erupt-ai-claw`](../erupt-ai-claw) | Interactive admin agent + the tool set (models, shell, skills, files) |
| **`erupt-ai-staff`** *(this)* | Autonomous, scheduled workers built from the two above |
