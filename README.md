# 标书审查系统

基于AI的智能标书审查与管理系统，支持文档上传、在线审查、协作编辑等功能。

## 🚀 快速开始

### 环境要求

- **Java**: 8或更高版本
- **Node.js**: 16或更高版本
- **Maven**: 3.6或更高版本
- **npm**: 8或更高版本

### 一键启动

```bash
# 克隆项目
git clone <repository-url>
cd biaoshu

# 给启动脚本执行权限 (Linux/macOS)
chmod +x start.sh

# 启动开发环境
./start.sh

# 或者在Windows上
start.bat
```

### 手动启动

#### 后端启动
```bash
cd backend
mvn spring-boot:run
```

#### 前端启动
```bash
cd frontend
npm install
npm run dev
```

## 📋 启动脚本使用说明

### Linux/macOS (start.sh)

```bash
# 基本用法
./start.sh [环境] [选项]

# 环境选项
./start.sh dev      # 开发环境 (默认)
./start.sh test     # 测试环境
./start.sh prod     # 生产环境

# 其他选项
./start.sh --backend-only     # 只启动后端
./start.sh --frontend-only    # 只启动前端
./start.sh --build           # 构建项目
./start.sh --clean           # 清理构建文件
./start.sh --stop            # 停止所有服务
./start.sh --status          # 查看服务状态
./start.sh --logs            # 查看日志
./start.sh --help            # 显示帮助

# 组合使用
./start.sh prod --build      # 构建并启动生产环境
./start.sh test --backend-only  # 只启动测试环境后端
```

### Windows (start.bat)

```cmd
# 基本用法与Linux版本相同
start.bat dev
start.bat prod --build
start.bat --stop
start.bat --status
```

## 🌍 环境配置

### 开发环境 (dev)
- 数据库: H2内存数据库
- 端口: 后端8080, 前端5173
- 日志级别: DEBUG
- 热重载: 启用

### 测试环境 (test)
- 数据库: H2内存数据库
- 端口: 后端8080, 前端5173
- 日志级别: INFO
- 功能: 完整测试

### 生产环境 (prod)
- 数据库: 可配置MySQL/PostgreSQL
- 端口: 可配置
- 日志级别: WARN
- 优化: 启用压缩、缓存等

## 🔧 环境变量配置

### 后端环境变量

```bash
# 数据库配置
DATABASE_URL=jdbc:mysql://localhost:3306/document_review
DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
DATABASE_USERNAME=root
DATABASE_PASSWORD=password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT配置
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# 文件存储
UPLOAD_DIR=/app/uploads

# 跨域配置
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### 前端环境变量

```bash
# API配置
VITE_API_BASE_URL=https://api.yourdomain.com
VITE_API_TIMEOUT=30000

# 功能开关
VITE_ENABLE_DEBUG=false
VITE_ENABLE_MOCK=false
```

## 🐳 Docker部署

### 使用Docker Compose

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 单独构建镜像

```bash
# 构建镜像
docker build -t document-review:latest .

# 运行容器
docker run -d \
  --name document-review \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  document-review:latest
```

## 📁 项目结构

```
biaoshu/
├── backend/                 # 后端Spring Boot项目
│   ├── src/main/java/      # Java源码
│   ├── src/main/resources/ # 配置文件
│   └── pom.xml             # Maven配置
├── frontend/               # 前端Vue3项目
│   ├── src/                # 前端源码
│   ├── public/             # 静态资源
│   └── package.json        # npm配置
├── logs/                   # 日志目录
├── uploads/                # 文件上传目录
├── start.sh               # Linux/macOS启动脚本
├── start.bat              # Windows启动脚本
├── Dockerfile             # Docker镜像构建文件
├── docker-compose.yml     # Docker编排文件
└── README.md              # 项目说明
```

## 🔗 访问地址

启动成功后，可以通过以下地址访问：

- **前端应用**: http://localhost:5173
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html
- **H2控制台**: http://localhost:8080/h2-console (仅开发/测试环境)
- **健康检查**: http://localhost:8080/actuator/health

## 🛠️ 开发指南

### 添加新功能

1. 后端开发
   - 在`backend/src/main/java`中添加Controller、Service、Entity
   - 更新数据库schema
   - 编写单元测试

2. 前端开发
   - 在`frontend/src`中添加页面组件
   - 更新路由配置
   - 添加API调用

### 数据库迁移

开发环境使用H2内存数据库，生产环境建议使用MySQL或PostgreSQL。

### 日志查看

```bash
# 查看实时日志
./start.sh --logs

# 或直接查看日志文件
tail -f logs/backend.log
tail -f logs/frontend.log
```

## 🔍 故障排除

### 常见问题

1. **端口被占用**
   ```bash
   ./start.sh --stop  # 停止所有服务
   ```

2. **依赖安装失败**
   ```bash
   ./start.sh --clean  # 清理构建文件
   ```

3. **服务启动失败**
   ```bash
   ./start.sh --logs   # 查看详细日志
   ```

### 检查服务状态

```bash
./start.sh --status
```

## 📝 更新日志

### v1.0.0
- ✅ 基础项目架构
- ✅ 用户认证与授权
- ✅ 文档管理功能
- ✅ 多环境启动脚本
- ✅ Docker部署支持

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- 项目维护者: biaoshu
- 邮箱: your-email@example.com
- 项目地址: https://github.com/your-username/biaoshu
