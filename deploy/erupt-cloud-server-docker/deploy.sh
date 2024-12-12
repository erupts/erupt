#!/bin/bash

# 退出时显示错误
set -e

# 获取 Maven 项目的版本号
DOCKER_TAG=$(sed -n 's|.*<version>\([^<]*\)</version>.*|\1|p' pom.xml | head -n 1)

IMG_NAME="erupts/erupt-cloud-server"

# 打印版本号
echo "Using Docker tag: $DOCKER_TAG"

echo "Running Maven clean package..."
mvn clean package -DskipTests

# Docker 镜像构建
echo "Building Docker image..."
docker build -t $IMG_NAME:$DOCKER_TAG .

# Docker 登录
docker login

# Docker 推送
echo "Pushing Docker image to Docker Hub..."
docker push $IMG_NAME:$DOCKER_TAG

echo "Docker hub push complete!"

# 询问用户是否需要推送到阿里云
#read -p "是否需要推送到阿里云? (y/n): " PUSH_TO_ALIYUN
#
#if [[ "$PUSH_TO_ALIYUN" == "y" || "$PUSH_TO_ALIYUN" == "Y" ]]; then
#    # 提示用户输入阿里云登录信息
#    read -p "Enter your Aliyun username: " ALIYUN_USERNAME
#    read -p "Enter your Aliyun password: " ALIYUN_PASSWORD
#    echo # 为了换行
#
#    # 登录到阿里云 Docker Registry
#    echo "Logging into Aliyun Docker Registry..."
#    echo $ALIYUN_PASSWORD | docker login --username=$ALIYUN_USERNAME registry.cn-hangzhou.aliyuncs.com --password-stdin
#
#    # 推送到阿里云 Docker Registry。  TODO 调整阿里云地址改这里 改前缀即可
#    ALIYUN_REPO="registry.cn-hangzhou.aliyuncs.com/barcke/$IMG_NAME"
#    echo "Pushing Docker image to Aliyun Registry..."
#    docker tag $IMG_NAME:$DOCKER_TAG $ALIYUN_REPO:$DOCKER_TAG
#    docker push $ALIYUN_REPO:$DOCKER_TAG
#
#    echo "Docker image pushed to Aliyun successfully!"
#else
#    echo "Skipping push to Aliyun."
#fi