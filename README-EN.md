[中文](./README.md) &nbsp; | &nbsp; English

<p align="center"><img src="./erupt-web/src/main/resources/public/erupt.svg" height="120" alt="logo"/></p>

<h1 align="center"> ERUPT &nbsp; 🚀 &nbsp; An Efficient Low-Code Engine for Developers </h1>

<h3 align="center">Low-Code + AI, Making Development Easier</h3>
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
    <a href="https://github.com/erupts/erupt">GitHub Repo</a> &nbsp; | &nbsp;
    <a href="https://gitcode.com/erupts/erupt">GitCode Repo</a> &nbsp; | &nbsp;
    <a href="https://gitee.com/erupt/erupt">Gitee Repo</a> &nbsp; | &nbsp;
    <a href="https://www.erupt.xyz" target="_blank"><b>Official Website</b></a> &nbsp; | &nbsp;
    <a href="https://www.erupt.xyz/demo" target="_blank">Live Demo</a> &nbsp; | &nbsp;
    <a href="https://www.erupt.xyz/#!/module" target="_blank">Module Ecosystem</a> &nbsp; | &nbsp;
    <a href="https://www.yuque.com/erupts" target="_blank">📕 Documentation</a>
</p>

---

> A revolutionary low-code development framework designed to boost enterprise-level backend development efficiency, significantly reducing development costs and complexity.
>
> More than just a tool, it is a strategic choice for optimizing development processes, enhancing team productivity, and accelerating digital transformation.
>
> Erupt delivers efficiency, flexibility, and control—dramatically shortening development cycles and allowing you to focus on core business.

---

## 🚀 Introduction | Intro

**Erupt** is a universal low-code framework driven by Java annotations that dynamically renders and constructs pages and APIs.

Zero front-end code, zero CURD, and automatic table creation — only one class file with annotation configuration is required to quickly develop enterprise-grade data management backends.

Highly extensible, supporting CURD customization via `@DataProxy`, custom data sources, soft delete, LDAP, OSS, and more.

## 🎯 Low-Code Engine | Engine

#### Transparent Mechanics & High Configurability:

* **Annotation-driven, flexible configuration:** Control model behavior, UI rendering, data validation, and processing through annotations like `@Erupt`, `@EruptField`, and their rich properties.
* **Open architecture:** Erupt’s modular core encourages developers to understand its internal mechanisms and adjust them as needed.
* **Detailed logs and debugging support:** Helps developers track issues and understand internal execution flows.

#### Beyond Traditional Code Generators:

* **Runtime interpretation over static generation:** Erupt dynamically constructs applications through annotation parsing, avoiding bloated, hard-to-maintain generated code.
* **Model-focused development:** Developers only need to define business models and annotations — Erupt handles UI rendering, data binding, API calls, and database interactions automatically.

#### Deep Customization and Extensibility:

* **Lifecycle hooks:** Key lifecycle points expose hooks for extending and adjusting behavior.
* **`@DataProxy` interface:** Inject custom logic during data operations (e.g., before add, after edit, on query) for validations, transformations, permission control, etc.
* **Custom components and views:** Easily integrate personalized front-end components or templates beyond built-in options.
* **Custom data sources and dialects:** Extend support for specific databases or create your own SQL dialect adapters.

#### Embracing the Spring Boot Ecosystem:

* **Non-intrusive design:** Built on Spring Boot with deep Spring Data JPA integration without interfering with other Spring or third-party libraries.
* **Standard JPA entities:** Erupt entity classes are standard JPA entities, reusable across other services or modules.
* **Easy to integrate into existing projects:** Erupt Cloud can be progressively integrated to enhance or replace backend modules in existing Spring Boot projects.

> Since 2020, Erupt has been continuously optimized and upgraded. It's already used by hundreds of companies and thousands of developers. Hundreds have contributed feature suggestions and code.

## 🥏 Beyond Code Generators

While code generators improve efficiency, they produce large volumes of template code that are difficult to maintain and prone to technical debt once modified.

Erupt offers a more elegant solution:

* **Elevating configuration to code:** Erupt doesn’t generate code; it dynamically interprets annotations. Business logic is decoupled from core framework behavior — changing an annotation instantly updates system behavior.
* **Higher abstraction:** Erupt abstracts common backend features into standardized annotations and components. Developers focus solely on business model definitions.
* **Ongoing evolution and maintenance:** As a mature open-source framework, the Erupt team and community continuously enhance features and fix issues. You benefit from framework updates without maintaining generated code.

![result](readme/view.png)
![result](readme/edit.png)

```java
@Erupt(
       name = "Simple Example",
       power = @Power(importable = true, export = true)
)
@Table(name = "t_simple")   // Database table name
@Entity
public class Simple extends BaseModel {

    @EruptField(
            views = @View(title = "Text"),
            edit = @Edit(title = "Text", notNull = true, search = @Search)
    )
    private String input;

    @EruptField(
            views = @View(title = "Number", sortable = true),
            edit = @Edit(title = "Number", search = @Search)
    )
    private Float number;

    @EruptField(
            views = @View(title = "Boolean"),
            edit = @Edit(title = "Boolean", search = @Search)
    )
    private Boolean bool;

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
            views = @View(title = "Dropdown"),
            edit = @Edit(
                    search = @Search,
                    title = "Dropdown", type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,name from e_upms_menu"
                    )
            )
    )
    private Long choice;
}
```

[Feature Demo](https://www.erupt.xyz/#!/contrast)

## ⛰ Screenshots

<a href="https://www.erupt.xyz/demo" target="_blank"><img src="./readme/seer.png" width="100%"/></a>

<!-- screenshot table as-is -->

## 🔗 Download and Usage

No need to compile source code. Just add the following dependencies to your Spring Boot project:

```xml
<!-- Core Dependency -->
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-admin</artifactId>
    <version>LATEST-VERSION</version>
</dependency>
<!-- Admin Web UI -->
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-web</artifactId>
    <version>LATEST-VERSION</version>
</dependency>
```

> Latest version: <a href="https://mvnrepository.com/search?q=erupt"><img src="https://img.shields.io/maven-central/v/xyz.erupt/erupt" alt="maven-central"></a>

[Detailed Installation Guide](https://www.yuque.com/erupts/erupt/tpq1l9)

## 🌕 Live Demo

Demo: [https://www.erupt.xyz/demo](https://www.erupt.xyz/demo)
Account/Password: `guest / guest`

**Supports modern browsers and runs on Electron or other Web standard-based environments**

## 🔭 Recommended Projects

* [`Linq.J` – Object Query Language for JVM](https://github.com/erupts/Linq.J)
* [`magic-api` – Rapid API Development Framework](https://github.com/ssssssss-team/magic-api)
* [`Jpom` – Lightweight Online Build/Deploy/Monitoring Tool](https://gitee.com/dromara/Jpom)

## 🧩 Join the Community

QQ Group: <a href="http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=DhReMX7b17i5e_xaImsIoYJ_JaskDA1H&authKey=%2Bkldm0OLuB9HRv56c5s21YJyvJj%2BqdKul1X7eyUVnF2yzWkks6QTFN%2Bxd4AE2DVX&noverify=0&group_code=836044286">836044286 🔥</a>

WeChat Group: Follow the **EruptGuide** public account to get the latest group QR code (updated weekly)

**ERUPT — Empower Java Developers to Build Backends More Efficiently and Flexibly. Join Us to Explore the Future of Low-Code in Professional Development!**

## ⛽️ Donate

The author personally covers the costs of servers, domains, hosting, and maintenance.
Open source is not easy — a cup of coffee is appreciated 🙏

[Donate Here](https://www.yuque.com/erupts/erupt/mwf15h)

---

### ⭐️ Licensed under Apache License 2.0 — Open Source and Free to Use. If You Like It, Please ⭐ Star the Project to Show Support!

---

<p align="center">
    <a href="https://www.erupt.xyz" target="_blank">Official Website</a> &nbsp; | &nbsp;
    <a href="https://github.com/erupts/erupt">GITHUB</a> &nbsp; | &nbsp;
    <a href="https://www.yuque.com/erupts" target="_blank">Documentation</a>
</p>

---

<p align="right">
Author: <a href="https://github.com/erupts">YuePeng</a> &nbsp; / &nbsp; <a href="mailto:erupts@126.com">erupts@126.com</a>
</p>  
<br>
