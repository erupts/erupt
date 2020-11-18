## Erupt Framework

官方网站：https://www.erupt.xyz

国内镜像：https://gitee.com/erupt/erupt

国外镜像：https://github.com/erupts/erupt


### 作者 
YuePeng / erupts@126.com

### 项目介绍
基于注解理念的java后台快速开发框架，前端页面结构完全由注解指定，将前端工作量减少至零
面向注解开发的通用数据管理框架

### 设计理念
对象视图模型 OVM(Object View Model)

### 代码示例
``` java
@Erupt(name = "简单的例子")
@Table(name = "demo_simple")
@Entity
public class Simple extends BaseModel {

    //文本输入
    @EruptField(
            views = @View(title = "文本"),
            edit = @Edit(title = "文本")
    )
    private String input;
    
    //数值输入
    @EruptField(
            views = @View(title = "数值"),
            edit = @Edit(title = "数值")
    )
    private Integer number;

    //布尔选择
    @EruptField(
            views = @View(title = "布尔"),
            edit = @Edit(title = "布尔")
    )
    private Boolean bool;

    //时间选择
    @EruptField(
            views = @View(title = "时间"),
            edit = @Edit(title = "时间")
    )
    private Date date;

}
```
### 运行效果
![avatar](https://cdn.nlark.com/yuque/0/2020/gif/117735/1599711030466-c546dd4e-a167-4c12-b00e-00bfbf962c73.gif)

### 软件架构
服务端  
Java 8、Spring Boot、JPA、Hibernate、quartz、Gson ...

前端  
Angular、NG-ALAIN、NG-ALAIN、RxJS、TypeScript ...

### 演示Demo 
https://www.erupt.xyz/demo  
用户名：guest  
密码：guest

### 使用教程
https://www.yuque.com/yuepeng/erupt
