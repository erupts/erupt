中文 &nbsp; | &nbsp; [English](./README-EN.md)

<p align="center"><img src="./erupt-web/src/main/resources/public/erupt.svg" height="120" alt="logo"/></p>

<h1 align="center"> ERUPT &nbsp; 🚀 &nbsp; 为开发者打造的高效低代码引擎 </h1>

<h3 align="center">低代码 + AI，让开发更简单</h3>
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
    <a href="https://github.com/erupts/erupt">GitHub 仓库</a> &nbsp; | &nbsp;
    <a href="https://gitcode.com/erupts/erupt">GitCode 仓库</a> &nbsp; | &nbsp; 
    <a href="https://gitee.com/erupt/erupt">码云仓库</a> &nbsp; | &nbsp; 
    <a href="https://www.erupt.xyz" target="_blank"><b>官方网站</b></a> &nbsp; | &nbsp; 
    <a href="https://www.erupt.xyz/demo" target="_blank">在线体验</a> &nbsp; | &nbsp; 
    <a href="https://www.erupt.xyz/#!/module" target="_blank">框架生态</a> &nbsp; | &nbsp; 
    <a href="https://www.yuque.com/erupts" target="_blank">📕 使用文档</a>
</p>

---

> 一款革命性的低代码开发框架，专为提升企业级中后台应用的开发效率而设计，显著降低开发成本与复杂度。 
> 
> 它不仅仅是一个工具，更是一种能够优化研发流程、提升团队生产力、加速数字化转型的战略选择。
> 
> Erupt 提供效率、灵活性、可控性，大幅压缩研发周期，专注核心业务。
---

## 🚀 简介 | Intro
Erupt 一个通用低代码框架，使用 Java 注解驱动，动态渲染构建页面与API。

零前端代码、零 CURD、自动建表，仅需 一个类文件 + 注解配置，快速开发企业级数据管理后台。

高扩展性，支持CURD自由扩展 @DataProxy 、自定义数据源、逻辑删除、LDAP、OSS。

## 🎯 低代码引擎 | Engine

#### 透明的底层机制与高度可配置性：
  * 注解驱动，配置灵活：通过 `@Erupt`、`@EruptField` 及其丰富的属性，开发者可以精细控制模型的行为、UI 的展现、数据的校验与处理方式。
  * 开放的架构：Erupt 的核心模块设计清晰，鼓励开发者理解其工作原理，并在需要时进行针对性调整。
  * 详细的日志与调试支持：方便开发者追踪问题，理解框架内部执行流程。

####  超越传统代码生成器的开发体验：
  * 动态解释而非静态生成：Erupt 通过运行时解析注解来动态构建应用，避免了传统代码生成器因代码冗余、难以维护、合并困难等问题带来的困扰。修改注解即可实时生效，无需重新生成大量代码。
  * 关注模型而非实现：开发者只需关注业务模型的定义与注解配置，Erupt 会自动处理大部分底层的 UI 渲染、数据绑定、API 调用，数据库交互等工作。

#### 深度定制与扩展能力：
  * 生命周期钩子：在 Erupt 核心流程的多个关键节点提供了钩子函数，方便开发者进行功能增强或行为调整。 
  * `@DataProxy` 接口**：允许开发者在数据操作的各个阶段（如新增前、编辑后、查询时）注入自定义逻辑，实现复杂的业务校验、数据转换、权限控制等。
  * 自定义组件与视图：除了丰富的内置组件，Erupt 支持开发者创建和集成自定义的前端组件和视图模板，满足高度个性化的界面需求。
  * 自定义数据源与方言：轻松扩展对特定数据库或数据存储的支持，甚至可以实现自定义的 SQL 方言适配。

#### 拥抱 Spring Boot 生态，无缝集成：
  * 非侵入式设计：Erupt 构建于 Spring Boot 之上，与 Spring Data JPA 深度集成，但不干涉开发者使用 Spring Boot 的其他功能或引入第三方库。
  * 标准 JPA 实体：Erupt 实体类本身就是标准的 JPA 实体，可以被项目中的其他服务或模块复用。
  * 易于整合现有项目：可以将 Erupt Cloud 渐进式地引入到已有的 Spring Boot 项目中，逐步替换或增强后台管理模块。

> 从 2020 年开始不断优化升级，目前已有上百家公司使用，上万名开发者开发相关应用。上百名开发者参与提交了功能建议与提交代码。


## 🥏 超越传统代码生成器

代码生成器在一定程度上提升了开发效率，但其本质仍是生成大量模板代码。这些生成的代码一旦修改，后续的维护和升级往往面临巨大挑战，容易形成技术债。

Erupt 提供了更为优雅的解决方案：

*   **配置即代码的升华**：Erupt 不是生成代码，而是通过注解动态解释和执行，这意味着业务逻辑与框架核心高度解耦。修改配置（注解）即可实时改变系统行为，无需重新生成和合并代码。
*   **更高的抽象层次**：Erupt 将常见的后台管理功能抽象为一系列标准化的注解和组件，开发者只需关注业务模型的定义，而非底层的实现细节。
*   **持续进化与维护**：作为一款成熟的开源框架，Erupt 核心团队和社区持续对其进行功能增强和缺陷修复。使用 Erupt 意味着您可以享受到框架升级带来的红利，而无需自行维护大量生成的代码。

![result](readme/view.png)
![result](readme/edit.png)

``` java
@Erupt(
       name = "简单的例子",
       power = @Power(importable = true, export = true)
)
@Table(name = "t_simple")   //数据库表名
@Entity
public class Simple extends BaseModel {

    @EruptField(
            views = @View(title = "文本"),
            edit = @Edit(title = "文本", notNull = true, search = @Search)
    )
    private String input;

    @EruptField(
            views = @View(title = "数值", sortable = true),
            edit = @Edit(title = "数值", search = @Search)
    )
    private Float number;

    @EruptField(
            views = @View(title = "布尔"),
            edit = @Edit(title = "布尔", search = @Search)
    )
    private Boolean bool;

    @EruptField(
            views = @View(title = "时间"),
            edit = @Edit(title = "时间", search = @Search(vague = true))
    )
    private Date date;

    @EruptField(
            views = @View(title = "滑动条"),
            edit = @Edit(title = "滑动条", type = EditType.SLIDER, search = @Search,
                    sliderType = @SliderType(max = 90, markPoints = {0, 30, 60, 90}, dots = true))
    )
    private Integer slide;

    @EruptField(
            views = @View(title = "下拉选择"),
            edit = @Edit(
                    search = @Search,
                    title = "下拉选择", type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,name from e_upms_menu"
                    )
            )
    )
    private Long choice;

}
```

[功能体验](https://www.erupt.xyz/#!/contrast)

## ⛰ 演示截图 | Screenshot

<a href="https://www.erupt.xyz/demo" target="_blank"><img src="./readme/seer.png" width="100%"/></a>

<table>
    <tr>
        <td colspan="2"><img src="readme/goods.png"/></td>
        <td colspan="2"><img src="readme/chart.png"/></td>
    </tr>
    <tr>
        <td colspan="2"><img src="readme/code.png"/></td>
        <td colspan="2"><img src="readme/job.png"/></td>
    </tr>
    <tr>
        <td colspan="2"><img src="readme/complex.png"/></td>
        <td colspan="2"><img src="readme/complex-edit.png"/></td>
    </tr>
    <tr>
        <td colspan="2"><img src="readme/tpl.png"/></td>
        <td colspan="2"><img src="readme/role.png"/></td>
    </tr>
    <tr>
        <td colspan="2"><img src="readme/component.png"/></td>
        <td colspan="2"><img src="readme/component-edit.png"/></td>
    </tr>
    <tr>
        <td colspan="4" align="center">移动端展示效果</td>
    </tr>
    <tr>
        <td><img src="readme/m1.png"/></td>
        <td><img src="readme/m2.png"/></td>
        <td><img src="readme/m3.png"/></td>
        <td><img src="readme/m4.png"/></td>
    </tr>
</table>

## 🔗 下载使用 | Download

无需编译源代码，Spring Boot项目添加如下依赖即可
```xml
<!--核心依赖-->
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-admin</artifactId>
    <version>LATEST-VERSION</version>
</dependency>
<!--后台WEB界面-->
<dependency>
  <groupId>xyz.erupt</groupId>
  <artifactId>erupt-web</artifactId>
  <version>LATEST-VERSION</version>
</dependency>
```
> 最新版本
> 
> <a href="https://mvnrepository.com/search?q=erupt"><img src="https://img.shields.io/maven-central/v/xyz.erupt/erupt" alt="maven-central"></a>

[详细使用步骤](https://www.yuque.com/erupts/erupt/tpq1l9)

## 🌕 在线体验 | Demo

演示地址：[https://www.erupt.xyz/demo](https://www.erupt.xyz/demo)

账号密码：`guest / guest`

**支持主流现代浏览器，可直接运行在 Electron 等基于 Web 标准的环境上**

## 🔭 开源推荐 | Recommend
[`Linq.J`：基于JVM的对象查询语言](https://github.com/erupts/Linq.J)

[`magic-api`：接口快速开发框架](https://github.com/ssssssss-team/magic-api)

[`Jpom`：简而轻的低侵入式在线构建、自动部署、日常运维、项目监控软件](https://gitee.com/dromara/Jpom)

## 🧩 加入讨论

QQ交流群：<a href="http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=DhReMX7b17i5e_xaImsIoYJ_JaskDA1H&authKey=%2Bkldm0OLuB9HRv56c5s21YJyvJj%2BqdKul1X7eyUVnF2yzWkks6QTFN%2Bxd4AE2DVX&noverify=0&group_code=836044286">
836044286 🔥</a>

微信交流群：由于微信群二维码有效时间仅7日，关注 Erupt 官方公众号（EruptGuide），可获取最新群二维码

ERUPT - 赋予 Java 开发者更高效、更灵活的后台构建能力。加入我们，一同探索低代码在专业开发领域的无限可能！

## ⛽️ 捐赠 | Donate

此框架服务器，域名，空间，人工等费用一直由作者本人自掏腰包并持续维护，开源不易，一杯咖啡也是动力 🙏

[前往捐赠](https://www.yuque.com/erupts/erupt/mwf15h)

---

### ⭐️ 使用 Apache License 2.0 协议，核心源代码开源免费。开源不易，喜欢请给作者 Star 鼓励

---

<p align="center">
    <a href="https://www.erupt.xyz" target="_blank">官方网站</a> &nbsp; | &nbsp; 
    <a href="https://github.com/erupts/erupt">GITHUB</a> &nbsp; | &nbsp; 
    <a href="https://www.yuque.com/erupts" target="_blank">使用文档</a>
</p>

---

<p align="right">
作者 ：<a href="https://github.com/erupts">YuePeng</a> &nbsp; / &nbsp; <a href="mailto:erupts@126.com">erupts@126.com</a>
</p>
<br>
