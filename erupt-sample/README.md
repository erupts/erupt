## 源码编译方式演示

    首先：建议使用jar方式依赖部署，可降低使用成本

运行：执行`src/main/java/EruptSampleApplication.java` 文件中的main方法即可

默认的数据源为H2如果需要调整，则在`pom.xml`增加数据库驱动，且修改 `src/main/recorces/application.yml` 中的配置即可，支持所有主流关系型数据库。
