<p align="center"><img src="./erupt-web/src/main/resources/public/erupt.svg" height="150" alt="logo"/></p>
<h1 align="center"> Erupt Framework 🚀 通用后台管理框架 </h1>
<h3 align="center">零前端代码，几行Java注解，搞定后台管理系统</h3>

---

[![Erupt Framework](https://img.shields.io/badge/Erupt-Framework-brightgreen)](https://www.erupt.xyz) 
[![license Apache 2.0](https://img.shields.io/badge/license-Apache%202-blue)](./LICENSE) 
[![Gitee star](https://gitee.com/erupt/erupt/badge/star.svg?theme=dark)](https://gitee.com/erupt/erupt) 
[![Gitee fork](https://gitee.com/erupt/erupt/badge/fork.svg?theme=dark)](https://gitee.com/erupt/erupt) 
[![GitHub stars](https://img.shields.io/github/stars/erupts/erupt?style=social)](https://github.com/erupts/erupt) 
[![GitHub forks](https://img.shields.io/github/forks/erupts/erupt?style=social)](https://github.com/erupts/erupt) 
![size](https://img.shields.io/github/repo-size/erupts/erupt)


[码云仓库](https://gitee.com/erupt/erupt) &nbsp; | &nbsp; [github仓库](https://github.com/erupts/erupt) &nbsp; | &nbsp; 
[项目官网](https://www.erupt.xyz) &nbsp; | &nbsp; [使用文档](https://www.yuque.com/yuepeng/erupt) &nbsp; | &nbsp; 
[JavaDoc](https://apidoc.gitee.com/erupt/erupt/) &nbsp; | &nbsp; [环境搭建](https://www.yuque.com/yuepeng/erupt/tpq1l9) &nbsp; | &nbsp; 
[代码演示](https://www.erupt.xyz/#!/contrast) &nbsp; | &nbsp; [在线体验](https://www.erupt.xyz/demo)

> QQ交流群：821389129 🔥   
> _加群可提前获取需要 1000 star 才能开放的 erupt-tpl 模块与 erupt-generator 模块 jar 包_

## 什么是 erupt
erupt 是一个低代码 **全栈类** 框架，它使用 **Java 注解** 来生成页面以及增、删、改、查、权限控制等后台功能，不需要懂前端，也不需要写繁琐的CURD，自动创建表结构，controller / service / sao / mapper 文件都不用创建，极大减少开发工作量，将后台开发速度提升到极致。

> 取代代码生成器，开发后台管理系统更优解！

## 为什么要做 erupt ?
无论开发怎样的系统，都需要配套的管理后台做数据支撑，是软件开发中必不可少的一环，但实际开发中存这无法规避的痛点，如：开发效率低下、UI界面不尽人意、交互凑合、代码重复、存在安全漏洞，导致开发成本极高。

虽然近些年来 **代码生成器** 成了后台开发的新宠，但它真的是后台开发的最优解吗？   
代码生成器的本质还是生成繁琐的前端与后台代码，一旦修改后期生成的代码很难合并，想想 Mybatis-Generator，基本上就是一次性的东西，虽然减轻了部分工作，可解决方式并非最佳。

开发后台管理系统大部分情况下只想做个普通的增删改查界面，用于数据管理，类似下面这种：
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
            edit = @Edit(title = "布尔")
    )
    private Boolean bool;

    @EruptField(
            views = @View(title = "时间"),
            edit = @Edit(title = "时间", search = @Search(vague = true))
    )
    private Date date;

}
```
[功能体验](https://www.erupt.xyz/#!/contrast)

这个界面虽然用 Vue + Ant Design + SSM 也能做出个大概，但仔细观察会发现它有大量细节功能如：

+ 可以对数据做筛选
+ 有按钮可以刷新、新增、数据
+ 表格有分页与汇总
+ 预览单行数据
+ 批量删除
+ 隐藏某列
+ 多种组件
+ 按某列排序
+ 有非空校验
+ 支持导入导出 Excel

全部实现这些仅前端就需要大量的代码，后端的接口与业务逻辑更不在少数。

但可以看到，用 erupt 只需要 30 几行 注解 配置，你不需要了解 Angular / React / Vue / Jquery 也不需要了解 JavaScript / HTML / CSS，甚至不需要了解 Spring MVC / JPA / Mybatis / SQL，即便没学过 erupt 也能猜到大部分配置的作用，只需要简单配置就能完成所有后台页面开发。

这正是建立 erupt 的初衷，对于大部分常用页面，应该使用最简单的方法来实现，甚至不需要学习各种框架和工具，专注核心业务，省下的时间做自己喜欢做的事，从此不再因为繁琐的后台开发而焦头烂额。

## 首页 | Home 
https://www.erupt.xyz

##  简介 | Intro
Erupt Framework 快速的构建管理页面，不需要懂前端、零CURD、自动建表，仅需单个类文件 + 简洁的注解配置，即可快速开发企业级 `Admin` 管理后台！

Erupt 提供企业级中后台管理系统的全栈解决方案，提供超多业务组件，简洁美观的后台页面，支持所有主流数据库，支持多数据源，严密的安全策略，极高的扩展性，大幅压缩研发周期，专注核心业务。

## 特性 | Features
+ **易于上手**：仅需了解 @Erupt 与 @EruptField 两个注解即可上手开发。
+ **代码简洁**：前端零代码，后端 template、controller、service、dao 都不需要，仅需一个实体类即可。
+ **敏捷开发**：仅单个`.java`文件即可实现后台管理功能，专注业务与核心功能的研发。
+ **快速迭代**：需求变更仅需修改或添加注解配置即可，迭代速度比需求讨论速度还快。
+ **功能强大**：动态条件处理，支持增删改查等功能代理接口，Session存储机制选择，行为日志记录等。
+ **自动建表**：依托于JPA可自动帮你完成数据库建表相关工作。
+ **低侵入性**：几乎所有功能都围绕注解而展开，不影响Spring Boot其他功能或三方库库的使用。
+ **多数据源**：MySQL、Oracle、SQL Server、PostgreSQL、H2，甚至支持MongoDB。
+ **多种组件**：支持滑动输入、时间选择、开关、图片上传、代码编辑器、自动完成、树、多选框、地图等23类组件
+ **丰富展示**：普通文本、二维码、链接、图片、HTML、代码段、iframe、swf等
+ **代码生成**：erupt代码已经足够简洁，代码生成器可进一步提升开发效率。
+ **扩展性强**：支持自定义数据源实现、自定义页面、自定义模板、自定义附件上传机制等。
+ **界面美观**：每个交互都精心设计，产品思维打磨，只为了更好的操作体验。
+ **权限管理**：用户管理、角色管理、组织管理、菜单管理、登录日志、操作日志等。
+ **高安全性**：可靠的安全机制，登录白名单，权限验证，注解项检查，细颗粒度权限控制，为你的数据保驾护航。
+ **前后端分离**：后端与前端可分开部署
+ **响应式布局**：支持PC端手机端等各种规格的设备中使用。
+ **无需二次开发**：仅需引用 jar 包即可。

## 演示截图 | Screenshot
<table>
    <tr>
        <td><img src="readme/login.png"/></td>
        <td><img src="readme/home.png"/></td>
    </tr>
    <tr>
        <td><img src="readme/role.png"/></td>
        <td><img src="readme/log.png"/></td>
    </tr>
    <tr>
        <td><img src="readme/code.png"/></td>
        <td><img src="readme/job.png"/></td>
    </tr>
    <tr>
        <td><img src="readme/tpl.png"/></td>
        <td><img src="readme/complex.png"/></td>
    </tr>
    <tr>
        <td><img src="readme/goods.png"/></td>
        <td><img src="readme/chart.png"/></td>
    </tr>
    <tr>
        <td><img src="readme/component.png"/></td>
        <td><img src="readme/component-edit.png"/></td>
    </tr>
    <tr>
        <td><img src="readme/bi.png"/></td>
        <td><img src="readme/bi2.png"/></td>
    </tr>
</table>


## 模块说明 | Module
```lua
erupt
├── erupt-annotation -- 核心注解声明
├── erupt-core -- 核心功能实现
├── erupt-data -- 数据实现包
     ├── erupt-jpa -- 关系型数据库erupt实现
     └── erupt-mongodb -- mongodb数据库erupt实现
├── erupt-job -- 定时任务功能
├── erupt-security -- 接口数据安全模块
├── erupt-upms -- 用户权限管理
└── erupt-web -- 前端页面

erupt-site -- erupt官方网站 https://github.com/erupts/erupt-site

erupt-pro -- 暂未开放 star 超过 1K 开源 erupt-tpl 模块与 erupt-generator 模块
├── erupt-bi -- 通过 sql 加 js 混编实现动态报表，支持多数据源，支持十几种图表
├── erupt-tpl -- 支持在 erupt 中自定义页面，自定义图表，自定义模板等功能，模板引擎支持 freemarker / thymeleaf / 原生H5
└── erupt-generator -- 代码生成器，通过简单配置，生成 erupt 代码段

erupt-web-angular -- 暂未开放 erupt 前端源码

```

## 技术体系 | Technology system
####  后端：
Java 8、 ScriptEngine、 Annotation、 JDBC、 Reflect、 Spring Boot、 JPA、 Hibernate、 Quartz、 Gson、 Lombok、 POI ...

#### 前端：
JavaScript、 H5、 MVVM、 Router、 Angular CLI、 Angular、 NG-ZORRO、 NG-ALAIN、 G2Plot、 RxJS、 TypeScript、 Less ...

### 未来更新计划 | Future vision
+ 支持markdown等更多组件
+ 增加按钮级权限控制
+ 大数据分库分表支持
+ 多种不同的登录形式
+ 主题色更改
+ 国际化支持
+ 流程引擎支持（会完全自己做，不会用市面上已有的）

## 在线体验 | Demo
演示地址：https://www.erupt.xyz/demo  
账号密码：`guest / guest`

**支持主流 4 款现代浏览器，以及 Internet Explorer 11+，可直接运行在 Electron 等基于 Web 标准的环境上**

<table width="100%">
    <tr>
        <th width="20%" align="center"><img src="https://cdn.jsdelivr.net/gh/alrra/browser-logos/src/edge/edge_48x48.png" alt="IE / Edge" width="24px" height="24px" /> <br> Edge / IE </th>
        <th width="15%" align="center"><img src="https://cdn.jsdelivr.net/gh/alrra/browser-logos/src/firefox/firefox_48x48.png" alt="Firefox" width="24px" height="24px" /><br> Firefox </th>
        <th width="15%" align="center"><img src="https://cdn.jsdelivr.net/gh/alrra/browser-logos/src/chrome/chrome_48x48.png" alt="Chrome" width="24px" height="24px" /> <br> Chrome </th>
        <th width="15%" align="center"><img src="https://cdn.jsdelivr.net/gh/alrra/browser-logos/src/safari/safari_48x48.png" alt="Safari" width="24px" height="24px" /> <br> Safari </th>
        <th width="15%" align="center"><img src="https://cdn.jsdelivr.net/gh/alrra/browser-logos/src/opera/opera_48x48.png" alt="Opera" width="24px" height="24px" /> <br> Opera </th>
        <th width="20%" align="center"><img src="https://cdn.jsdelivr.net/gh/alrra/browser-logos/src/electron/electron_48x48.png" alt="Electron" width="24px" height="24px" /> <br> Electron </th>
    </tr>
    <tr>
        <td align="center">Edge 16 / IE 11+</td>
        <td align="center">522</td>
        <td align="center">57</td>
        <td align="center">11</td>
        <td align="center">44</td>
        <td align="center">Chromium 57</td>
    </tr>
</table>

## 使用文档 | Documentation
https://www.yuque.com/yuepeng/erupt

## 开源推荐 | Recommend
[`zeta-api`：通过XML配置快速创建api接口与文档，安全且高效，快速开发必备！](https://github.com/erupts/zeta-api)

## 捐赠 | Donate
感谢所有支持者!🙏

**作者**：YuePeng / erupts@126.com
