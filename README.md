English &nbsp; | &nbsp; [中文](README-zh.md)

<p align="center"><img src="./erupt-web/src/main/resources/public/assets/logo-raw2.png" height="120px" alt="logo"/></p>

<h1 align="center">ERUPT &nbsp; 🚀 &nbsp; Low-code</h1>

<h3 align="center">Annotation-driven development, zero front-end code, zero CRUD, multi-dimensional data management</h3>
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
    <a href="https://www.erupt.xyz" target="_blank"><b>Official Website</b></a> &nbsp; | &nbsp; 
    <a href="https://www.erupt.xyz/#!/module" target="_blank">Ecosystem</a> &nbsp; | &nbsp;
    <a href="https://www.erupt.xyz/demo" target="_blank">Online Demo</a> &nbsp; | &nbsp; 
    <a href="https://www.yuque.com/erupts" target="_blank">Documentation</a>
</p>

<img alt="erupt" src="erupt.svg">

---

## 🚀 Low-code Development
> @Erupt annotation enables out-of-the-box functionality in all scenarios

- Visualization: Tables / Trees / Gantt Charts / Cards + 20+ form components
- Data: Full database compatibility, API data sources, automatic table/index creation, LambdaQuery multi-table queries, dynamic CRUD
- API: One-click OpenAPI exposure, row/column permission control
- Permissions: UPMS / Class / Row/Column / Role multi-dimensional management
- Extensions: i18n, Cloud clustering, audit & event logs, BI, SaaS, AI large models
- Form Reusability: Flow processes, custom nodes, custom buttons

## Quick Start

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

[Feature Demo](https://www.erupt.xyz/#!/contrast)

## ⛰ Demo Screenshots

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

## 🔗 Download and Use

No need to compile source code. Simply add the following dependencies to your Spring Boot project:

```xml
<!--Core dependency-->
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-admin</artifactId>
    <version>${LATEST-VERSION}</version>
</dependency>
<!--Backend WEB interface-->
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-web</artifactId>
    <version>${LATEST-VERSION}</version>
</dependency>
```

> Latest Version
>
> <a href="https://mvnrepository.com/search?q=erupt"><img src="https://img.shields.io/maven-central/v/xyz.erupt/erupt" alt="maven-central"></a>

[Detailed Usage Steps](https://www.yuque.com/erupts/erupt/tpq1l9)

## 🌕 Online Demo

Demo URL: [https://www.erupt.xyz/demo](https://www.erupt.xyz/demo)

Username/Password: `guest / guest`

**Supports mainstream modern browsers and can run directly on Electron and other web-standard-based environments**

## 🔭 Open Source Recommendations

[`Linq.J`: Object query language based on JVM](https://github.com/erupts/Linq.J)

[`magic-api`: Rapid interface development framework](https://github.com/ssssssss-team/magic-api)

[`Jpom`: Lightweight low-invasive online build, automatic deployment, daily operations and maintenance, project monitoring software](https://gitee.com/dromara/Jpom)

## 🤝 Contributing

Erupt is a free and open-source project. We welcome anyone to contribute to Erupt to help improve it. This includes but is not limited to: submitting code, reporting bugs, sharing ideas, or sharing your use cases based on Erupt. We also welcome users to share Erupt on personal blogs or social media.

If you want to contribute code, please read our [Contribution Guidelines](./CONTRIBUTING.md) first.

Please submit [Issues](https://github.com/erupts/erupt/issues) and [Pull Requests](https://github.com/erupts/erupt/pulls) at https://github.com/erupts/erupt.

#### Thanks to the following contributors for their contributions to Erupt:

[![Contributors](https://contrib.rocks/image?repo=erupts/erupt)](https://github.com/erupts/erupt/graphs/contributors)

<h3 align="center">⭐️ Licensed under Apache License 2.0, source code is free and open source. Open source is not easy, please give the author a Star if you like it</h3>

---

<p align="right">
Author: <a href="https://github.com/erupts">YuePeng</a> &nbsp; / &nbsp; <a href="mailto:erupts@126.com">erupts@126.com</a>
</p>
<br>
