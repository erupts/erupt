# Erupt Framework 通用数据管理框架
快速搭建企业级管理后台

<img src="./erupt-web/src/main/resources/public/erupt.svg" width="160" alt="logo"/><br/>

[![Gitee star](https://gitee.com/erupt/erupt/badge/star.svg?theme=dark)](https://gitee.com/erupt/erupt)
[![GitHub stars](https://img.shields.io/github/stars/erupts/erupt?style=social)](https://github.com/erupts/erupt)
[![GitHub forks](https://img.shields.io/github/forks/erupts/erupt?style=social)](https://github.com/erupts/erupt)

国内仓库：https://gitee.com/erupt/erupt

国外仓库：https://github.com/erupts/erupt

## 网站首页🚀
https://www.erupt.xyz

## 项目简介
快速构建Admin管理后台  
零前端代码、无需编写后端controller、service、dao、mapper ！    
仅创建实体类与对应注解即可 ！



## 为什么要做Erupt 
+ 开发管理后台是项目中必不可少的工作，但往往管理后台的开发周期要占到实际工作量的50%
+ 本来要需要一个月甚至几个月的工作任务，能否在几小时之内完成呢？
+ 代码生成器的本质还是生成繁琐的后台代码，一旦修改后期生成的代码很难合并。


## 代码示例
``` java
@Erupt(name = "简单的例子")
@Table(name = "simple_table")
@Entity
public class Simple extends BaseModel {

    @EruptField(
            views = @View(title = "文本"),
            edit = @Edit(title = "文本输入")
    )
    private String input;
    
    @EruptField(
            views = @View(title = "数值"),
            edit = @Edit(title = "数值输入")
    )
    private Integer number;

    @EruptField(
            views = @View(title = "布尔"),
            edit = @Edit(title = "布尔选择")
    )
    private Boolean bool;

    @EruptField(
            views = @View(title = "时间"),
            edit = @Edit(title = "时间选择")
    )
    private Date date;

}
```
## 运行效果
![result](./img/simple.gif)

##演示截图
<table>
    <tr>
        <td><img src="./img/login.png"/></td>
        <td><img src="./img/home.png"/></td>
    </tr>
    <tr>
        <td><img src="./img/role.png"/></td>
        <td><img src="./img/log.png"/></td>
    </tr>
    <tr>
        <td><img src="./img/code.png"/></td>
        <td><img src="./img/job.png"/></td>
    </tr>
    <tr>
        <td><img src="./img/tpl.png"/></td>
        <td><img src="./img/complex.png"/></td>
    </tr>
    <tr>
        <td><img src="./img/goods.png"/></td>
        <td><img src="./img/chart.png"/></td>
    </tr>
    <tr>
        <td><img src="./img/bi.png"/></td>
        <td><img src="./img/bi2.png"/></td>
    </tr>
</table>

## 技术体系
后端  
Java 8、Spring Boot、JPA、Hibernate、quartz、Gson、lombok ...

前端  
Angular、NG-ZORRO、NG-ALAIN、G2Plot、RxJS、TypeScript ...

## 演示Demo 
演示地址：https://www.erupt.xyz/demo  
账号密码：`guest / guest`

## 使用文档
https://www.yuque.com/yuepeng/erupt

## 软件著作权
本框架已获得软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！

## 作者 
YuePeng / erupts@126.com

## 开源推荐
`zeta-api` : 仅需配置XML快速创建api接口与文档，安全且高效，快速开发必备！ https://github.com/erupts/zeta-api

