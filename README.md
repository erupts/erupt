# Erupt Framework 通用数据管理框架

<img src="./erupt-web/src/main/resources/public/erupt.svg" width="160" alt="logo"/><br/>

[![star](https://gitee.com/erupt/erupt-site/badge/star.svg?theme=dark)](https://gitee.com/erupt/erupt)
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


## 使用场景


## 为什么要做Erupt 
+ 相信大家都知道，开发管理后台是项目中必不可少的工作，
开发过的都知道功能性与美观程度很难做到优秀  
+ 本来要需要一个月的工作任务，能否在几小时之内完成呢？
+ 需求的不断更迭是常态，代码生成器面对字段的修改与增加工作量会越来越大，但是erupt可以很好的解决这个问题，


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

