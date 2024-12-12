# 默认使用阿里云镜像
FROM openjdk:11-jre-slim

# 安装Redis
RUN apt-get update && apt-get install -y redis

# 暴露Redis的默认端口6379
EXPOSE 6379

# 设置工作目录
WORKDIR /app

# 将本地的 jar 文件拷贝到容器中
COPY target/erupt-cloud-server-docker-*.jar /app/EruptCloudServerApplication.jar

# 启动脚本
COPY start.sh /start.sh
RUN chmod +x /start.sh

# 暴露应用运行的端口
EXPOSE 8080

# 使用启动脚本启动 Java 应用和 Redis
CMD ["/start.sh"]