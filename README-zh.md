中文 &nbsp; | &nbsp; [English](README.md)

---

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="readme/logo2.svg">
    <img src="readme/logo.svg" alt="Erupt" height="120">
  </picture>
</p>

<h1 align="center">Erupt · 注解驱动的 Java 管理后台框架</h1>

<h3 align="center">AI Harness · 50+ 大模型 · MCP 原生 · A2A 协议</h3>

<p align="center">
  Java 注解 · Spring Boot 3 · 零前端 · 2~5s 启动
</p>

---

<p align="center">
  <a href="https://mvnrepository.com/search?q=erupt" target="_blank"><img src="https://img.shields.io/maven-central/v/xyz.erupt/erupt" alt="maven-central"></a>
  <a href="./LICENSE"><img src="https://img.shields.io/badge/license-Apache%202-blue" alt="license Apache 2.0"></a>
  <a href="https://github.com/erupts/erupt"><img src="https://img.shields.io/github/stars/erupts/erupt?style=social" alt="GitHub stars"></a>
  <a href="https://github.com/erupts/erupt"><img src="https://img.shields.io/github/forks/erupts/erupt?style=social" alt="GitHub forks"></a>
  <a href="https://gitcode.com/erupts/erupt" target="_blank"><img src="https://gitcode.com/erupts/erupt/star/badge.svg" alt="gitcode"></a>
  <a href="https://gitee.com/erupt/erupt" target="_blank"><img src="https://gitee.com/erupt/erupt/badge/star.svg?theme=dark" alt="Gitee star"></a>
  <a href="https://gitee.com/erupt/erupt" target="_blank"><img src="https://gitee.com/erupt/erupt/badge/fork.svg?theme=dark" alt="Gitee fork"></a>
  <a href="https://github.com/erupts/erupt/commits"><img src="https://img.shields.io/github/last-commit/erupts/erupt?color=FF5722&label=last%20commit" alt="last-commit"></a>
  <a href="https://github.com/erupts/erupt/releases"><img src="https://img.shields.io/github/v/release/erupts/erupt?color=FF5722&label=release" alt="latest-release"></a>
  <a href="https://github.com/erupts/erupt/discussions"><img src="https://img.shields.io/github/discussions/erupts/erupt?color=FF5722" alt="discussions"></a>
</p>

---

<p align="center">
  <a href="https://demo.erupt.xyz" target="_blank"><img src="https://img.shields.io/badge/%F0%9F%8E%AF_%E5%9C%A8%E7%BA%BF%E4%BD%93%E9%AA%8C-FF5722?style=for-the-badge&logoColor=white" alt="在线体验"></a>
  &nbsp;
  <a href="https://start.erupt.xyz" target="_blank"><img src="https://img.shields.io/badge/%E2%9A%A1_%E5%88%9B%E5%BB%BA%E9%A1%B9%E7%9B%AE-28a745?style=for-the-badge&logoColor=white" alt="创建项目"></a>
  &nbsp;
  <a href="https://docs.erupt.xyz" target="_blank"><img src="https://img.shields.io/badge/%F0%9F%93%96_%E9%98%85%E8%AF%BB%E6%96%87%E6%A1%A3-444?style=for-the-badge&logoColor=white" alt="阅读文档"></a>
  &nbsp;
  <a href="https://www.erupt.xyz" target="_blank"><img src="https://img.shields.io/badge/%F0%9F%8C%90_%E5%AE%98%E7%BD%91-2196F3?style=for-the-badge&logoColor=white" alt="官网"></a>
  &nbsp;
  <a href="https://linq.erupt.xyz" target="_blank"><img src="https://img.shields.io/badge/%E2%88%91_Linq.J-9C27B0?style=for-the-badge&logoColor=white" alt="Linq.J"></a>
</p>

---

<p align="center"><img src="readme/hero.svg" alt="Erupt · 注解驱动的 Java 管理后台框架 · AI Harness" width="100%"/></p>

[//]: # (<p align="center"><img alt="Erupt Engine" src="erupt.svg"></p>)

---

## 💡 为什么选 Erupt？

- **一个类文件 = 一套后台页面。** 零 Controller、零前端打包、零脚手架。
- **极速启动。** Java 17 + Spring Boot 3.x，**2~5 秒**起一个完整管理后台。
- **20+ 表单组件**开箱即用：日期、滑块、树、代码编辑器、参照表格、自动补全、地图、签名、Markdown……
- **内置 RBAC、审计日志、Excel 导入导出、OpenAPI** —— 每个 `@Erupt` 实体自动是一个带权限的 REST 接口。
- **AI Native。** 50+ 大模型、MCP 原生工具、70 万+ Skills，全部通过管理界面配置。
- **多数据库支持。** 默认 JPA（MySQL · PostgreSQL · Oracle · SQL Server · 达梦），MongoDB 走 `erupt-mongodb`。
- **完整的生产级生态。** `erupt-cloud` 分布式配置；商业模块 Chart / Flow / Tenant / Cube 覆盖报表、工作流、多租户、语义层 BI。

---

## ⚡ 60 秒上手

根据你的场景选择最适合的方式：

---

### 方式一 · 源码启动（探索 / 参与贡献）

克隆仓库并运行内置示例 —— 内嵌 H2 数据库，无需额外配置。

```bash
git clone https://github.com/erupts/erupt.git
cd erupt
mvn spring-boot:run -pl erupt-sample -am
# → http://localhost:8080   登录：erupt / erupt
```

---

### 方式二 · Docker（零安装体验）

从 Docker Hub 拉取全家桶镜像 —— [hub.docker.com/r/erupts/erupt](https://hub.docker.com/r/erupts/erupt)。镜像基于 `erupt-spring-boot-starter-all` 构建，内置全部特性模块（designer、job、monitor、magic-api、notice、AI、cloud-server 等），默认使用内嵌 H2 数据库，零配置即可启动。

```bash
docker run -p 8080:8080 erupts/erupt
# → http://localhost:8080   登录：erupt / erupt
```

生产环境可通过 `-e` 环境变量切换 MySQL / Redis —— 详见 [镜像使用说明 →](./deploy/erupt-docker/README.md)

---

### 方式三 · Maven 依赖（集成到自有项目）

#### 1. 加一个依赖

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-spring-boot-starter</artifactId>
    <version>${erupt.version}</version>
</dependency>
```

这一个 starter 就已包含跑起来一个 admin 所需的全部依赖（`erupt-admin` + `erupt-web`）。想要开箱即全家桶？改用 `erupt-spring-boot-starter-all`，它额外打包了所有可选特性模块（designer、job、generator、monitor、magic-api、websocket、notice、print、terminal、AI、cloud-server）。

#### 2. 在 JPA 实体上写一个注解 —— 这就是整个 UI

```java
@Erupt(name = "用户")
@Entity
public class User extends BaseModel {

    @EruptField(
        views = @View(title = "姓名"),
        edit  = @Edit(title = "姓名", search = @Search)
    )
    private String name;
}
```

#### 3. 启动，登录

```bash
mvn spring-boot:run
# → http://localhost:8080   登录：erupt / erupt
```

你已经拿到一个**带分页、搜索、导出、行列权限**的后台页面，数据来自 `User` 表。加一个字段、刷新、立刻出现。

> 不想安装任何东西？直接试 **[demo.erupt.xyz](https://demo.erupt.xyz)**（`guest / guest`）。
> 想生成一个起手项目？**[start.erupt.xyz](https://start.erupt.xyz)** 在浏览器里就能生成。
> 需要完整说明？**[详细使用步骤 →](https://docs.erupt.xyz/guide/quick-start)**

<details>
<summary><b>📦 看一个更完整的注解示例 —— 滑块、Choice、自定义按钮、LambdaQuery</b></summary>

```java
@Erupt(
    name = "Simple",
    power = @Power(importable = true, export = true),
    rowOperation = @RowOperation(
        title = "自定义操作",
        mode = RowOperation.Mode.SINGLE,
        operationHandler = OperationHandlerImpl.class
    )
)
@Table(name = "t_simple")
@Entity
public class Simple extends BaseModel {

    @EruptField(
        views = @View(title = "文本"),
        edit  = @Edit(title = "文本", notNull = true, search = @Search)
    )
    private String input;

    @EruptField(
        views = @View(title = "日期"),
        edit  = @Edit(title = "日期", search = @Search)
    )
    private Date date;

    @EruptField(
        views = @View(title = "滑块"),
        edit  = @Edit(title = "滑块", type = EditType.SLIDER, search = @Search,
            sliderType = @SliderType(max = 90, markPoints = {0, 30, 60, 90}, dots = true))
    )
    private Integer slide;

    @EruptField(
        views = @View(title = "选择项"),
        edit  = @Edit(title = "选择项", type = EditType.CHOICE, search = @Search,
            choiceType = @ChoiceType(
                fetchHandler = SqlChoiceFetchHandler.class,
                fetchHandlerParams = "select id, name from e_upms_menu"
            ))
    )
    private Long choice;
}
```

**类型安全的 LambdaQuery 链式查询：**

```java
List<EruptUser> list = eruptDao.lambdaQuery(EruptUser.class)
        .like(EruptUser::getName, "e")
        .isNull(EruptUser::getWhiteIp)
        .in(EruptUser::getId, 1, 2, 3, 4)
        .ge(EruptUser::getCreateTime, "2023-01-01")
        .list();
```

更多场景：**[erupt.xyz/#!/contrast](https://www.erupt.xyz/#!/contrast)**

</details>

---

## 📦 开箱即用的能力

<table>
  <tr>
    <td width="50%" valign="top">

**🧱 UI 自动生成**
表格、表单、搜索、分页、树视图、甘特图、卡片视图、20+ 表单组件 —— 全部由字段上的 `@View`、`@Edit`、`@Search` 驱动。

**🔐 权限管控（UPMS）**
用户、角色、菜单、行级筛选、列级可见性。`@Filter` 注解里写 SpEL 即可实现动态规则。支持 OAuth2 / LDAP / SSO。

**🌐 OpenAPI 自动暴露**
每个 `@Erupt` 实体自动成为 REST 接口，权限规则与界面完全一致。

**🗄️ 多数据库**
默认 JPA（MySQL · PostgreSQL · Oracle · SQL Server · 达梦……）；MongoDB 通过 `erupt-mongodb` 支持。

  </td>
  <td width="50%" valign="top">

**📊 Excel 导入导出**
通过 `erupt-excel` 开箱即用。需要自定义就覆盖 `DataProxy` 的 `excelImport` / `excelExport` 方法。

**🐴 AI Harness（`erupt-ai`）**
50+ 大模型、MCP 原生工具，全部通过管理界面配置，内置 RBAC。[→ 详情见下方](#-erupt-ai-harness--spring-boot-上的生产级-ai-agent-框架)

**🦞 AI Claw（`erupt-ai-claw`）**
用自然语言驱动 Erupt 实体、Shell、文件、浏览器。[→ 详情见下方](#-erupt-ai-claw)

> **该引入哪个？** 要原生 LLM / MCP 能力、自己写 agent，选 **`erupt-ai`**；想直接给 admin 装个开箱即用的 AI 助手，选 **`erupt-ai-claw`**（已传递依赖 `erupt-ai`，只需引这一个）。

**☁️ 集群与多租户**
`erupt-cloud` 提供分布式配置；商业模块 `erupt-tenant` 提供完整 SaaS 能力。

  </td>
  </tr>
</table>

完整模块列表：**[erupt.xyz/#!/module](https://www.erupt.xyz/#!/module)** &nbsp;·&nbsp; API 文档：**[javadoc.erupt.xyz](https://javadoc.erupt.xyz)**

---

## 🐴 Erupt AI Harness · Spring Boot 上的生产级 AI Agent 框架

> `erupt-ai` 模块 —— Spring Boot 的 **AI Harness**：50+ 大模型 · MCP 原生工具 · 内置 **RBAC** 角色权限 · 角色级 system prompt · 会话历史 —— 全部通过管理界面配置，零样板代码。

**为什么叫「Harness」？** 把 AI 推到生产环境，需要的不只是 SDK，而是 **治理（RBAC）+ 互操作（MCP）+ 可观测（会话/Token 追踪）+ 运维友好（管理界面配置）** 四件事。Erupt AI Harness 一次提供齐全。

### 支持 50+ 大模型提供商

OpenAI · Claude · Gemini · DeepSeek · 通义千问 · 智谱 GLM · 豆包 · Moonshot · MiniMax · Mistral · Grok · Fireworks · Together · OpenRouter · Requesty · Ollama（本地）—— **管理界面里随时热切换，共 50+ 个。**

### 核心能力

- **多模型切换** —— 通过管理界面配置多个 LLM，无需修改代码即可切换
- **流式输出（SSE）** —— 实时逐 token 响应，可配置超时时间
- **思考模型支持** —— 原生支持推理模型（DeepSeek、Kimi-K2）
- **MCP 协议** —— 接入任意 MCP 工具服务器（SSE & STDIO），自动健康检查与重连
- **A2A 协议（Agent-to-Agent）** —— Agent 之间通过标准 A2A 协议互相调用，原生支持多 Agent 协作
- **AI Toolbox** —— 通过 `@AiToolbox` + `@Tool` 注解将任意 Spring Bean 暴露为 AI 工具
- **Tool 安全 · Role 动态管控** —— 每个 AI 工具都受 `LLMRole` 管控，可按角色白名单或回收单个工具，运行时生效，无需重启
- **Agentic AI 框架** —— 自定义 AI Agent 的系统提示词、提示词列表、动态 Prompt 处理器，原生集成 MCP 工具
- **会话历史** —— 按用户隔离的对话会话，记录 Token 用量，支持软删除
- **长期记忆** —— 跨会话记忆持久化，AI 自动将重要决策与上下文写入记忆文件，下次会话自动加载

### AI Toolbox 示例

```java
@AiToolbox
@Component
public class MyTools {

    @Tool("根据订单ID查询订单状态")
    public String getOrderStatus(String orderId) {
        return orderService.getStatus(orderId);
    }
}
```

LLM 提供商、MCP 服务器、Agent 均可通过内置管理界面配置，无需重启服务。

---

## 🦞 Erupt AI Claw

> 通过自然语言直接驱动服务器 —— 像与同事对话一样简单。

Erupt AI Claw 让你通过对话操作注解驱动的数据与业务、执行 Shell 命令、读写文件、扩展自定义 Skills，全程无需编写代码。

### Claw 能力

- **Erupt 模型操作** —— 通过对话对任意 `@Erupt` 实体进行增删改查
- **Shell 执行** —— 用自然语言直接运行系统命令
- **文件读写** —— 读取和写入服务器文件
- **浏览器控制** —— 在 MCP 菜单中添加配置即可与浏览器交互
- **Skills（70 万+）** —— 兼容 [skills.sh](https://skills.sh) 70 万+ Skills；AI 根据提示词自动匹配执行；支持通过对话动态创建 Skill

Claw 与 AI Harness 共享同一套基于 Role 的 Tool 安全机制 —— 非管理员账号只能调用被白名单的工具。Skills 存放于 `~/.erupt/skills/`，也可在对话中动态创建。

---

> 📌 **核心模块永久免费、永久开源** —— `erupt-core` / `erupt-annotation` / `erupt-web` / `erupt-jpa` / `erupt-upms` / `erupt-ai` 等核心模块永久遵循 Apache 2.0 协议，**无 License 限制 · 无项目数限制 · 无商用限制**（详见 [官方治理承诺 →](./.github/GOVERNANCE.md)）。下方是核心之外可选的企业级商业模块，与开源核心独立演进、互不影响。

## 🔌 商业扩展模块

在开源核心之外，提供 4 个企业级商业模块（源码交付 · 一次买断 · 无 License / 项目数 / 商用限制）：

| 模块 | 用途 | 文档 |
| --- | --- | --- |
| **Erupt Chart** | 报表图表 / 数据可视化 | [📖 文档](https://docs.erupt.xyz/modules/pro/erupt-chart) |
| **Erupt Flow** | 流程引擎 / 审批工作流 | [📖 文档](https://docs.erupt.xyz/modules/pro/erupt-flow) |
| **Erupt SaaS** | 多租户基建 | [📖 文档](https://docs.erupt.xyz/modules/pro/erupt-tenant) |
| **Erupt Cube** | BI 平台（语义层 + 拖拽分析）| [📖 文档](https://docs.erupt.xyz/modules/pro/erupt-cube) |

*源码交付 · 一次买断 · 永久使用 · 与开源核心独立演进。*

👉 **[查看定价与购买流程 →](https://www.erupt.xyz/?utm_source=gitee&utm_medium=readme&utm_campaign=pro#!/pro)**

---

## 🤝 参与贡献

Erupt 是一个免费且开源的项目。我们欢迎任何人为 Erupt 做出贡献，包括但不限于：提交代码、反馈缺陷、交流想法，或分享你基于 Erupt 的使用案例。同时也欢迎用户在个人博客或社交媒体上分享 Erupt。

如果你想要贡献代码，请先阅读我们的[贡献指南](./.github/CONTRIBUTING.md)，然后在 GitHub 提交 [Issue](https://github.com/erupts/erupt/issues) 或 [Pull Request](https://github.com/erupts/erupt/pulls)。

**感谢以下贡献者对 Erupt 做出的贡献：**

[![Contributors](https://contrib.rocks/image?repo=erupts/erupt)](https://github.com/erupts/erupt/graphs/contributors)

> ⭐ **觉得 Erupt 帮到了你？请到 GitHub 给个 Star —— 开源不易，对项目成长帮助很大！**

---

## ⭐ Star History

<a href="https://www.star-history.com/?repos=erupts%2Ferupt&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/image?repos=erupts/erupt&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/image?repos=erupts/erupt&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/image?repos=erupts/erupt&type=date&legend=top-left" />
 </picture>
</a>

---

## 📄 License

Erupt 遵循 [Apache 2.0](./LICENSE) 许可证 —— 源代码开源免费、可商用、可二次开发。

<p align="right">
  作者：<a href="https://github.com/erupts" target="_blank">YuePeng</a> &nbsp;·&nbsp; <a href="mailto:erupts@126.com">erupts@126.com</a>
</p>
