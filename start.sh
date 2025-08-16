#!/bin/bash

# 标书审查系统启动脚本
# 支持多环境启动：dev、test、prod
# 作者：biaoshu
# 版本：1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目信息
PROJECT_NAME="标书审查系统"
VERSION="1.0.0"
BACKEND_DIR="backend"
FRONTEND_DIR="frontend"

# 默认配置
DEFAULT_ENV="dev"
DEFAULT_BACKEND_PORT="8080"
DEFAULT_FRONTEND_PORT="5173"

# 显示帮助信息
show_help() {
    echo -e "${CYAN}${PROJECT_NAME} v${VERSION} 启动脚本${NC}"
    echo ""
    echo -e "${YELLOW}使用方法:${NC}"
    echo "  ./start.sh [环境] [选项]"
    echo ""
    echo -e "${YELLOW}环境选项:${NC}"
    echo "  dev     - 开发环境 (默认)"
    echo "  test    - 测试环境"
    echo "  prod    - 生产环境"
    echo ""
    echo -e "${YELLOW}其他选项:${NC}"
    echo "  --backend-only    - 只启动后端服务"
    echo "  --frontend-only   - 只启动前端服务"
    echo "  --build          - 构建项目"
    echo "  --clean          - 清理构建文件"
    echo "  --stop           - 停止所有服务"
    echo "  --status         - 查看服务状态"
    echo "  --logs           - 查看日志"
    echo "  --help           - 显示此帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  ./start.sh                    # 启动开发环境"
    echo "  ./start.sh dev                # 启动开发环境"
    echo "  ./start.sh prod --build       # 构建并启动生产环境"
    echo "  ./start.sh --backend-only     # 只启动后端服务"
    echo "  ./start.sh --stop             # 停止所有服务"
}

# 打印带颜色的消息
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}[$(date '+%Y-%m-%d %H:%M:%S')] ${message}${NC}"
}

# 检查依赖
check_dependencies() {
    print_message $BLUE "检查系统依赖..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        print_message $RED "错误: 未找到Java，请安装Java 8或更高版本"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        print_message $RED "错误: 未找到Maven，请安装Maven 3.6或更高版本"
        exit 1
    fi
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        print_message $RED "错误: 未找到Node.js，请安装Node.js 16或更高版本"
        exit 1
    fi
    
    # 检查npm
    if ! command -v npm &> /dev/null; then
        print_message $RED "错误: 未找到npm，请安装npm"
        exit 1
    fi
    
    print_message $GREEN "依赖检查通过"
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        return 0  # 端口被占用
    else
        return 1  # 端口未被占用
    fi
}

# 停止服务
stop_services() {
    print_message $YELLOW "停止所有服务..."
    
    # 停止后端服务
    if check_port $DEFAULT_BACKEND_PORT; then
        print_message $YELLOW "停止后端服务 (端口 $DEFAULT_BACKEND_PORT)..."
        pkill -f "spring-boot:run" || true
        pkill -f "java.*document-review" || true
    fi
    
    # 停止前端服务
    if check_port $DEFAULT_FRONTEND_PORT; then
        print_message $YELLOW "停止前端服务 (端口 $DEFAULT_FRONTEND_PORT)..."
        pkill -f "vite" || true
        pkill -f "npm.*dev" || true
    fi
    
    # 等待服务完全停止
    sleep 3
    print_message $GREEN "所有服务已停止"
}

# 清理构建文件
clean_build() {
    print_message $YELLOW "清理构建文件..."
    
    # 清理后端
    if [ -d "$BACKEND_DIR" ]; then
        cd $BACKEND_DIR
        mvn clean > /dev/null 2>&1 || true
        cd ..
        print_message $GREEN "后端构建文件已清理"
    fi
    
    # 清理前端
    if [ -d "$FRONTEND_DIR" ]; then
        cd $FRONTEND_DIR
        rm -rf dist node_modules/.vite > /dev/null 2>&1 || true
        cd ..
        print_message $GREEN "前端构建文件已清理"
    fi
}

# 安装依赖
install_dependencies() {
    local env=$1
    
    print_message $BLUE "安装项目依赖..."
    
    # 安装前端依赖
    if [ -d "$FRONTEND_DIR" ]; then
        print_message $BLUE "安装前端依赖..."
        cd $FRONTEND_DIR
        npm install
        cd ..
        print_message $GREEN "前端依赖安装完成"
    fi
}

# 构建项目
build_project() {
    local env=$1
    
    print_message $BLUE "构建项目 (环境: $env)..."
    
    # 构建前端
    if [ -d "$FRONTEND_DIR" ]; then
        print_message $BLUE "构建前端项目..."
        cd $FRONTEND_DIR
        
        # 根据环境设置构建命令
        if [ "$env" = "prod" ]; then
            npm run build:prod
        elif [ "$env" = "test" ]; then
            npm run build:test
        else
            npm run build
        fi
        
        cd ..
        print_message $GREEN "前端构建完成"
    fi
    
    # 构建后端
    if [ -d "$BACKEND_DIR" ]; then
        print_message $BLUE "构建后端项目..."
        cd $BACKEND_DIR
        mvn clean package -DskipTests -P$env
        cd ..
        print_message $GREEN "后端构建完成"
    fi
}

# 启动后端服务
start_backend() {
    local env=$1
    
    print_message $BLUE "启动后端服务 (环境: $env)..."
    
    if [ ! -d "$BACKEND_DIR" ]; then
        print_message $RED "错误: 后端目录不存在"
        exit 1
    fi
    
    cd $BACKEND_DIR
    
    # 根据环境启动
    if [ "$env" = "prod" ]; then
        # 生产环境使用jar包启动
        if [ -f "target/document-review-*.jar" ]; then
            nohup java -jar target/document-review-*.jar --spring.profiles.active=prod > ../logs/backend.log 2>&1 &
        else
            print_message $RED "错误: 未找到jar包，请先构建项目"
            exit 1
        fi
    else
        # 开发/测试环境使用Maven启动
        nohup mvn spring-boot:run -Dspring-boot.run.profiles=$env > ../logs/backend.log 2>&1 &
    fi
    
    cd ..
    
    # 等待服务启动
    print_message $YELLOW "等待后端服务启动..."
    for i in {1..30}; do
        if check_port $DEFAULT_BACKEND_PORT; then
            print_message $GREEN "后端服务启动成功 (端口: $DEFAULT_BACKEND_PORT)"
            return 0
        fi
        sleep 2
    done
    
    print_message $RED "后端服务启动失败，请检查日志"
    exit 1
}

# 启动前端服务
start_frontend() {
    local env=$1
    
    print_message $BLUE "启动前端服务 (环境: $env)..."
    
    if [ ! -d "$FRONTEND_DIR" ]; then
        print_message $RED "错误: 前端目录不存在"
        exit 1
    fi
    
    cd $FRONTEND_DIR
    
    # 根据环境启动
    if [ "$env" = "prod" ]; then
        # 生产环境使用静态文件服务
        if [ -d "dist" ]; then
            nohup npx serve -s dist -l $DEFAULT_FRONTEND_PORT > ../logs/frontend.log 2>&1 &
        else
            print_message $RED "错误: 未找到构建文件，请先构建项目"
            exit 1
        fi
    else
        # 开发/测试环境使用开发服务器
        nohup npm run dev > ../logs/frontend.log 2>&1 &
    fi
    
    cd ..
    
    # 等待服务启动
    print_message $YELLOW "等待前端服务启动..."
    for i in {1..20}; do
        if check_port $DEFAULT_FRONTEND_PORT; then
            print_message $GREEN "前端服务启动成功 (端口: $DEFAULT_FRONTEND_PORT)"
            return 0
        fi
        sleep 2
    done
    
    print_message $RED "前端服务启动失败，请检查日志"
    exit 1
}

# 查看服务状态
show_status() {
    print_message $BLUE "服务状态检查..."
    
    echo ""
    echo -e "${CYAN}=== 服务状态 ===${NC}"
    
    # 检查后端服务
    if check_port $DEFAULT_BACKEND_PORT; then
        echo -e "后端服务: ${GREEN}运行中${NC} (端口: $DEFAULT_BACKEND_PORT)"
        echo -e "后端地址: ${BLUE}http://localhost:$DEFAULT_BACKEND_PORT${NC}"
        echo -e "API文档: ${BLUE}http://localhost:$DEFAULT_BACKEND_PORT/swagger-ui.html${NC}"
        echo -e "H2控制台: ${BLUE}http://localhost:$DEFAULT_BACKEND_PORT/h2-console${NC}"
    else
        echo -e "后端服务: ${RED}未运行${NC}"
    fi
    
    echo ""
    
    # 检查前端服务
    if check_port $DEFAULT_FRONTEND_PORT; then
        echo -e "前端服务: ${GREEN}运行中${NC} (端口: $DEFAULT_FRONTEND_PORT)"
        echo -e "前端地址: ${BLUE}http://localhost:$DEFAULT_FRONTEND_PORT${NC}"
    else
        echo -e "前端服务: ${RED}未运行${NC}"
    fi
    
    echo ""
}

# 查看日志
show_logs() {
    if [ ! -d "logs" ]; then
        print_message $YELLOW "日志目录不存在"
        return
    fi
    
    echo -e "${CYAN}=== 最近的日志 ===${NC}"
    echo ""
    
    if [ -f "logs/backend.log" ]; then
        echo -e "${YELLOW}后端日志 (最后20行):${NC}"
        tail -20 logs/backend.log
        echo ""
    fi
    
    if [ -f "logs/frontend.log" ]; then
        echo -e "${YELLOW}前端日志 (最后20行):${NC}"
        tail -20 logs/frontend.log
        echo ""
    fi
}

# 主函数
main() {
    # 创建日志目录
    mkdir -p logs
    
    # 解析参数
    ENV=$DEFAULT_ENV
    BACKEND_ONLY=false
    FRONTEND_ONLY=false
    BUILD_ONLY=false
    CLEAN_ONLY=false
    STOP_ONLY=false
    STATUS_ONLY=false
    LOGS_ONLY=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            dev|test|prod)
                ENV=$1
                shift
                ;;
            --backend-only)
                BACKEND_ONLY=true
                shift
                ;;
            --frontend-only)
                FRONTEND_ONLY=true
                shift
                ;;
            --build)
                BUILD_ONLY=true
                shift
                ;;
            --clean)
                CLEAN_ONLY=true
                shift
                ;;
            --stop)
                STOP_ONLY=true
                shift
                ;;
            --status)
                STATUS_ONLY=true
                shift
                ;;
            --logs)
                LOGS_ONLY=true
                shift
                ;;
            --help|-h)
                show_help
                exit 0
                ;;
            *)
                print_message $RED "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 显示启动信息
    echo -e "${PURPLE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                    ${PROJECT_NAME} v${VERSION}                    ║"
    echo "║                        启动脚本                              ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    
    # 执行相应操作
    if [ "$STOP_ONLY" = true ]; then
        stop_services
        exit 0
    fi
    
    if [ "$STATUS_ONLY" = true ]; then
        show_status
        exit 0
    fi
    
    if [ "$LOGS_ONLY" = true ]; then
        show_logs
        exit 0
    fi
    
    if [ "$CLEAN_ONLY" = true ]; then
        clean_build
        exit 0
    fi
    
    # 检查依赖
    check_dependencies
    
    # 停止现有服务
    stop_services
    
    # 安装依赖
    install_dependencies $ENV
    
    # 构建项目
    if [ "$BUILD_ONLY" = true ] || [ "$ENV" = "prod" ]; then
        build_project $ENV
        if [ "$BUILD_ONLY" = true ]; then
            print_message $GREEN "构建完成"
            exit 0
        fi
    fi
    
    print_message $BLUE "启动环境: $ENV"
    
    # 启动服务
    if [ "$FRONTEND_ONLY" = true ]; then
        start_frontend $ENV
    elif [ "$BACKEND_ONLY" = true ]; then
        start_backend $ENV
    else
        start_backend $ENV
        start_frontend $ENV
    fi
    
    # 显示启动完成信息
    echo ""
    print_message $GREEN "🎉 系统启动完成！"
    echo ""
    show_status
    
    print_message $CYAN "使用 './start.sh --stop' 停止所有服务"
    print_message $CYAN "使用 './start.sh --logs' 查看日志"
    print_message $CYAN "使用 './start.sh --status' 查看服务状态"
}

# 执行主函数
main "$@"
