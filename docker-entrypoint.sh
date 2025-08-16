#!/bin/bash
set -e

# 标书审查系统 Docker 启动脚本

echo "🚀 启动标书审查系统..."

# 设置默认环境变量
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
export SERVER_PORT=${SERVER_PORT:-8080}
export LOG_FILE=${LOG_FILE:-logs/application.log}

# 创建日志目录
mkdir -p logs

# 打印启动信息
echo "环境: $SPRING_PROFILES_ACTIVE"
echo "端口: $SERVER_PORT"
echo "日志文件: $LOG_FILE"

# 启动应用
exec java \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -Dserver.port=$SERVER_PORT \
    -Dlogging.file.name=$LOG_FILE \
    -jar app.jar \
    "$@"
