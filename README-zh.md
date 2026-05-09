中文 &nbsp; | &nbsp; [English](README.md)

<p align="center"><img src="./erupt-web/src/main/resources/public/assets/logo-raw2.png" height="120px" alt="logo"/></p>

<h1 align="center">低代码 + AI & 🦞</h1>

<h3 align="center">注解驱动开发，零前端代码，零 CURD，自动建表，实现多维数据管理</h3>
<h3 align="center"><a href="https://erupt.xyz" target="_blank">https://www.erupt.xyz</a></h3>

---

<p align="center">
    <a href="https://mvnrepository.com/search?q=erupt"><img src="https://img.shields.io/maven-central/v/xyz.erupt/erupt" alt="maven-central"></a>
    <a href="./LICENSE"><img src="https://img.shields.io/badge/license-Apache%202-blue" alt="license Apache 2.0"></a>
    <a href="https://github.com/erupts/erupt"><img src="https://img.shields.io/github/stars/erupts/erupt?style=social" alt="GitHub stars"></a>
    <a href="https://github.com/erupts/erupt"><img src="https://img.shields.io/github/forks/erupts/erupt?style=social" alt="GitHub forks"></a>
    <a href="https://gitcode.com/erupts/erupt"><img src="https://gitcode.com/erupts/erupt/star/badge.svg" alt="gitcode"></a>
    <a href="https://gitee.com/erupt/erupt"><img src="https://gitee.com/erupt/erupt/badge/star.svg?theme=dark" alt="Gitee star"></a>
    <a href="https://gitee.com/erupt/erupt"><img src="https://gitee.com/erupt/erupt/badge/fork.svg?theme=dark" alt="Gitee fork"></a>
</p>

<p align="center">
    <a href="https://github.com/erupts/erupt">GitHub</a> &nbsp; | &nbsp;
    <a href="https://gitcode.com/erupts/erupt">GitCode</a> &nbsp; | &nbsp; 
    <a href="https://gitee.com/erupt/erupt">Gitee</a> &nbsp; | &nbsp; 
    <a href="https://www.erupt.xyz" target="_blank"><b>官方网站</b></a> &nbsp; | &nbsp; 
    <a href="https://www.erupt.xyz/#!/module" target="_blank">框架生态</a> &nbsp; | &nbsp;
    <a href="https://www.erupt.xyz/demo" target="_blank">在线体验</a> &nbsp; | &nbsp; 
    <a href="https://docs.erupt.xyz" target="_blank">📕 使用文档</a>
</p>

<img alt="erupt" src="erupt.svg">

---

## 🚀 低代码开发
> @Erupt 注解可实现全场景开箱即用

- 可视化：表 / 树 / 甘特图 / 卡片 + 20+表单组件
- 数据：全库兼容、API 数据源、自动建表/索引、LambdaQuery 多表查询、动态CURD
- API：OpenAPI 一键暴露、行列权限控制
- 权限：UPMS / 类 / 行列 / 角色多维管控
- 扩展：i18n、Cloud 集群、审计 & 事件日志、BI、SaaS、AI 大模型
- Form 复用：Flow 流程、自定义节点、自定义按钮

## 🤖 AI 大模型集成

> `erupt-ai` 模块 —— 零样板代码，开箱即用的企业级大模型接入

### 支持 50+ 大模型提供商

| 提供商 | 模型 |
|--------|------|
| OpenAI / ChatGPT | GPT-4o 系列 |
| Claude（Anthropic）| claude-3-7-sonnet 系列 |
| Gemini（Google）| gemini-2.0-flash 系列 |
| DeepSeek | deepseek-chat（支持思考模型）|
| 通义千问 / 智谱 GLM / 豆包 | 阿里 · 智谱 · 字节 |
| Moonshot / MiniMax | Kimi-K2.5 · MiniMax-M2.5 |
| Mistral / Grok | Mistral Large · Grok-2 |
| Fireworks / Together / OpenRouter | 开源模型托管 |
| Ollama | 本地私有化部署 |

### 核心能力

- **多模型切换** —— 通过管理界面配置多个 LLM，无需修改代码即可切换
- **流式输出（SSE）** —— 实时逐 token 响应，可配置超时时间
- **思考模型支持** —— 原生支持推理模型（DeepSeek、Kimi-K2）
- **MCP 协议** —— 接入任意 MCP 工具服务器（SSE & STDIO），自动健康检查与重连
- **AI Toolbox** —— 通过 `@AiToolbox` + `@Tool` 注解将任意 Spring Bean 暴露为 AI 工具
- **Agent 框架** —— 自定义系统提示词、提示词列表与动态 Prompt 处理器
- **会话历史** —— 按用户隔离的对话会话，记录 Token 用量，支持软删除

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

## 🦞 Erupt AI Claw

> 通过自然语言直接驱动服务器——像与同事对话一样简单

Erupt AI Claw 让你通过对话操作注解驱动的数据与业务、执行 Shell 命令、读写文件、扩展自定义 Skills，全程无需编写代码。

### Claw 能力

- **Erupt 模型操作** —— 通过对话对任意 `@Erupt` 实体进行增删改查
- **Shell 执行** —— 用自然语言直接运行系统命令
- **文件读写** —— 读取和写入服务器文件
- **浏览器控制** —— 在 MCP 菜单中添加配置即可与浏览器交互
- **Skills（70万+）** —— 兼容 [skills.sh](https://skills.sh) 70万+ Skills；AI 根据提示词自动匹配执行；支持通过对话动态创建 Skill
- **长期记忆** —— 跨会话记忆持久化；AI 自动将重要决策与上下文写入记忆文件，下次会话自动加载

Skills 存放于 `~/.erupt/skills/`，也可在对话中动态创建。

---

## 快速开始

> 💡 **本地运行**：克隆仓库后，直接运行 `erupt-sample` 模块中的 `EruptSampleApplication.main()` 方法即可启动示例项目，内置 H2 内存数据库，无需额外配置。
> 默认账号密码：`erupt / erupt`

#### Annotation Example

```java

@Erupt(
        name = "Simple",
        power = @Power(importable = true, export = true),
        @RowOperation(
                title = "Custom Action",
                mode = RowOperation.Mode.SINGLE,
                operationHandler = OperationHandlerImpl.class
        )
)
@Table(name = "t_simple")   //DB Table name
@Entity
public class Simple extends BaseModel {

    @EruptField(
            views = @View(title = "Text"),
            edit = @Edit(title = "Text", notNull = true, search = @Search)
    )
    private String input;

    @EruptField(
            views = @View(title = "Date"),
            edit = @Edit(title = "Date", search = @Search(vague = true))
    )
    private Date date;

    @EruptField(
            views = @View(title = "Slider"),
            edit = @Edit(title = "Slider", type = EditType.SLIDER, search = @Search,
                    sliderType = @SliderType(max = 90, markPoints = {0, 30, 60, 90}, dots = true))
    )
    private Integer slide;

    @EruptField(
            views = @View(title = "Choice Select"),
            edit = @Edit(
                    search = @Search,
                    title = "Choice Select", type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,name from e_upms_menu"
                    )
            )
    )
    private Long choice;

}
```

#### JPA LambdaQuery

```java
public void select() {
    List<EruptUser> list = eruptDao.lambdaQuery(EruptUser.class)
            .like(EruptUser::getName, "e")
            .isNull(EruptUser::getWhiteIp)
            .in(EruptUser::getId, 1, 2, 3, 4)
            .ge(EruptUser::getCreateTime, "2023-01-01")
            .list();
}
```

[功能体验](https://www.erupt.xyz/#!/contrast)

## ⛰ 演示截图

<table>
    <tr>
        <td><img src="readme/goods.png"/></td>
        <td><img src="readme/chart.png"/></td>
        <td><img src="readme/tpl.png"/></td>
    </tr>
    <tr>
        <td><img src="readme/complex.png"/></td>
        <td><img src="readme/complex-edit.png"/></td>
        <td><img src="readme/log.png"/></td>
    </tr>
    <tr>
        <td><img src="readme/component.png"/></td>
        <td><img src="readme/component-edit.png"/></td>
        <td><img src="readme/view.png"/></td>
    </tr>
</table>

## 🔗 下载使用

无需编译源代码，Spring Boot项目添加如下依赖即可

```xml
<!--核心依赖-->
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-admin</artifactId>
    <version>${LATEST-VERSION}</version>
</dependency>
<!--后台WEB界面-->
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-web</artifactId>
    <version>${LATEST-VERSION}</version>
</dependency>
```

> 最新版本
>
> <a href="https://mvnrepository.com/search?q=erupt"><img src="https://img.shields.io/maven-central/v/xyz.erupt/erupt" alt="maven-central"></a>

[详细使用步骤](https://docs.erupt.xyz/guide/quick-start)

## 🌕 在线体验

演示地址：[https://www.erupt.xyz/demo](https://www.erupt.xyz/demo)

账号密码：`guest / guest`

**支持主流现代浏览器，可直接运行在 Electron 等基于 Web 标准的环境上**

## 🔭 开源推荐

[`Linq.J`：基于JVM的对象查询语言](https://github.com/erupts/Linq.J)

[`magic-api`：接口快速开发框架](https://github.com/ssssssss-team/magic-api)

[`Jpom`：简而轻的低侵入式在线构建、自动部署、日常运维、项目监控软件](https://gitee.com/dromara/Jpom)

## 🤝 参与贡献

Erupt 是一个免费且开源的项目。我们欢迎任何人为 Erupt 做出贡献，以帮助改善 Erupt。包括但不限于：提交代码、反馈缺陷、交流想法，或分享你基于
Erupt 的使用案例。同时，我们也欢迎用户在个人博客或社交媒体上分享 Erupt。

如果你想要贡献代码，请先阅读我们的[贡献指南](./CONTRIBUTING.md)。

请在 https://github.com/erupts/erupt 提交 [Issues](https://github.com/erupts/erupt/issues)
和 [Pull Requests](https://github.com/erupts/erupt/pulls)。

#### 感谢以下贡献者对 Erupt 做出的贡献：

[![Contributors](https://contrib.rocks/image?repo=erupts/erupt)](https://github.com/erupts/erupt/graphs/contributors)

## Star History

<a href="https://www.star-history.com/?repos=erupts%2Ferupt&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/image?repos=erupts/erupt&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/image?repos=erupts/erupt&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/image?repos=erupts/erupt&type=date&legend=top-left" />
 </picture>
</a>

## License
⭐️ Erupt 遵循 Apache 2.0 许可证。详情请参阅 [LICENSE](./LICENSE) 文件。


<h3 align="center">⭐️ 使用 Apache License 2.0 协议，源代码开源免费。开源不易，喜欢请给作者 Star 鼓励</h3>

---

<p align="right">
作者 ：<a href="https://github.com/erupts">YuePeng</a> &nbsp; / &nbsp; <a href="mailto:erupts@126.com">erupts@126.com</a>
</p>
<br>
