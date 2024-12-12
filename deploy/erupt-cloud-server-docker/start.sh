#!/bin/bash
# 启动 Redis 服务器
redis-server &

# 启动 Java 应用
java -jar -Xms256m -Xmx512m -Xss256k /app/EruptCloudServerApplication.jar