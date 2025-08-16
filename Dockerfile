# 多阶段构建 Dockerfile
# 阶段1: 构建前端
FROM node:18-alpine AS frontend-builder

WORKDIR /app/frontend

# 复制前端依赖文件
COPY frontend/package*.json ./
RUN npm ci --only=production

# 复制前端源码并构建
COPY frontend/ ./
RUN npm run build:prod

# 阶段2: 构建后端
FROM maven:3.9-openjdk-8-slim AS backend-builder

WORKDIR /app/backend

# 复制Maven配置文件
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B

# 复制后端源码并构建
COPY backend/src ./src
RUN mvn clean package -DskipTests -Pprod

# 阶段3: 运行时镜像
FROM openjdk:8-jre-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 创建应用目录
WORKDIR /app

# 创建必要的目录
RUN mkdir -p logs uploads data && \
    chown -R appuser:appuser /app

# 复制构建产物
COPY --from=backend-builder /app/backend/target/document-review-*.jar app.jar
COPY --from=frontend-builder /app/frontend/dist ./static

# 复制启动脚本
COPY docker-entrypoint.sh ./
RUN chmod +x docker-entrypoint.sh && \
    chown appuser:appuser docker-entrypoint.sh

# 切换到应用用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["./docker-entrypoint.sh"]
