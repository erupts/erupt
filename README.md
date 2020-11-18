# Erupt Framework 通用数据管理框架

[![star](https://gitee.com/erupt/erupt-site/badge/star.svg?theme=dark)](https://gitee.com/erupt/erupt)
[![GitHub stars](https://img.shields.io/github/stars/erupts/erupt?style=social)](https://github.com/erupts/erupt)
[![GitHub forks](https://img.shields.io/github/forks/erupts/erupt?style=social)](https://github.com/erupts/erupt)

官方网站：https://www.erupt.xyz

国内仓库：https://gitee.com/erupt/erupt

国外仓库：https://github.com/erupts/erupt

## 项目介绍
通过注解快速构建Admin管理后台  
零前端代码  
后端controller、service、dao、mapper都可以不写  
仅需创建model与对应注解即可

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
![avatar](https://cdn.nlark.com/yuque/0/2020/gif/117735/1599711030466-c546dd4e-a167-4c12-b00e-00bfbf962c73.gif)

## 技术体系
后端  
Java 8、Spring Boot、JPA、Hibernate、quartz、Gson、lombok ...

前端  
Angular、NG-ZORRO、NG-ALAIN、G2Plot、RxJS、TypeScript ...

## 演示Demo 
体验地址：https://www.erupt.xyz/demo  
账号密码：`guest / guest`

## 使用文档
https://www.yuque.com/yuepeng/erupt

## 软件著作权
本框架已获取软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！

## 作者 
YuePeng / erupts@126.com

## 开源推荐
`zeta-api` : 仅需配置XML快速创建api接口与文档，安全且高效，快速开发必备！ https://github.com/erupts/zeta-api

