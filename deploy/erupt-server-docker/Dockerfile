# 默认使用阿里云镜像
FROM registry.cn-zhangjiakou.aliyuncs.com/barcke-myblog/openjdk

# 设置工作目录
WORKDIR /app

# 将本地的 jar 文件拷贝到容器中
COPY target/sample-server-*.jar /app/EruptServerApplication.jar

# 暴露应用运行的端口
EXPOSE 8080

# 设置 JVM 堆内存和线程栈大小
ENV JAVA_OPTS="-Xms256m -Xmx512m -Xss256k"

# 定义容器启动时执行的命令
ENTRYPOINT ["java", "-jar", "/app/EruptServerApplication.jar"]
CMD ["${JAVA_OPTS}"]