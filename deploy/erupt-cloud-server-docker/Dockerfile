# 默认使用阿里云镜像
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 将本地的 jar 文件拷贝到容器中
COPY target/erupt-cloud-server-docker-*.jar /app/EruptCloudServerApplication.jar

# 暴露应用运行的端口
EXPOSE 8080

# 设置 JVM 堆内存和线程栈大小
ENV JAVA_OPTS="-Xms256m -Xmx512m -Xss256k"

# 定义容器启动时执行的命令
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar /app/EruptCloudServerApplication.jar ${JAVA_OPTS}"]