@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 标书审查系统启动脚本 (Windows版本)
:: 支持多环境启动：dev、test、prod
:: 作者：biaoshu
:: 版本：1.0.0

set PROJECT_NAME=标书审查系统
set VERSION=1.0.0
set BACKEND_DIR=backend
set FRONTEND_DIR=frontend
set DEFAULT_ENV=dev
set DEFAULT_BACKEND_PORT=8080
set DEFAULT_FRONTEND_PORT=5173

:: 解析参数
set ENV=%DEFAULT_ENV%
set BACKEND_ONLY=false
set FRONTEND_ONLY=false
set BUILD_ONLY=false
set CLEAN_ONLY=false
set STOP_ONLY=false
set STATUS_ONLY=false
set LOGS_ONLY=false

:parse_args
if "%~1"=="" goto :args_done
if /i "%~1"=="dev" (
    set ENV=dev
    shift & goto :parse_args
)
if /i "%~1"=="test" (
    set ENV=test
    shift & goto :parse_args
)
if /i "%~1"=="prod" (
    set ENV=prod
    shift & goto :parse_args
)
if /i "%~1"=="--backend-only" (
    set BACKEND_ONLY=true
    shift & goto :parse_args
)
if /i "%~1"=="--frontend-only" (
    set FRONTEND_ONLY=true
    shift & goto :parse_args
)
if /i "%~1"=="--build" (
    set BUILD_ONLY=true
    shift & goto :parse_args
)
if /i "%~1"=="--clean" (
    set CLEAN_ONLY=true
    shift & goto :parse_args
)
if /i "%~1"=="--stop" (
    set STOP_ONLY=true
    shift & goto :parse_args
)
if /i "%~1"=="--status" (
    set STATUS_ONLY=true
    shift & goto :parse_args
)
if /i "%~1"=="--logs" (
    set LOGS_ONLY=true
    shift & goto :parse_args
)
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-h" goto :show_help
echo 未知参数: %~1
goto :show_help

:args_done

:: 显示帮助信息
:show_help
echo.
echo %PROJECT_NAME% v%VERSION% 启动脚本
echo.
echo 使用方法:
echo   start.bat [环境] [选项]
echo.
echo 环境选项:
echo   dev     - 开发环境 (默认)
echo   test    - 测试环境
echo   prod    - 生产环境
echo.
echo 其他选项:
echo   --backend-only    - 只启动后端服务
echo   --frontend-only   - 只启动前端服务
echo   --build          - 构建项目
echo   --clean          - 清理构建文件
echo   --stop           - 停止所有服务
echo   --status         - 查看服务状态
echo   --logs           - 查看日志
echo   --help           - 显示此帮助信息
echo.
echo 示例:
echo   start.bat                    # 启动开发环境
echo   start.bat dev                # 启动开发环境
echo   start.bat prod --build       # 构建并启动生产环境
echo   start.bat --backend-only     # 只启动后端服务
echo   start.bat --stop             # 停止所有服务
echo.
if /i "%~1"=="--help" exit /b 0
if /i "%~1"=="-h" exit /b 0
exit /b 1

:: 打印消息
:print_message
echo [%date% %time%] %~1
goto :eof

:: 检查端口是否被占用
:check_port
netstat -an | find ":%~1 " | find "LISTENING" >nul
if %errorlevel%==0 (
    exit /b 0
) else (
    exit /b 1
)

:: 检查依赖
:check_dependencies
call :print_message "检查系统依赖..."

java -version >nul 2>&1
if %errorlevel% neq 0 (
    call :print_message "错误: 未找到Java，请安装Java 8或更高版本"
    exit /b 1
)

mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    call :print_message "错误: 未找到Maven，请安装Maven 3.6或更高版本"
    exit /b 1
)

node -v >nul 2>&1
if %errorlevel% neq 0 (
    call :print_message "错误: 未找到Node.js，请安装Node.js 16或更高版本"
    exit /b 1
)

npm -v >nul 2>&1
if %errorlevel% neq 0 (
    call :print_message "错误: 未找到npm，请安装npm"
    exit /b 1
)

call :print_message "依赖检查通过"
goto :eof

:: 停止服务
:stop_services
call :print_message "停止所有服务..."

:: 停止Java进程
taskkill /f /im java.exe >nul 2>&1
:: 停止Node进程
taskkill /f /im node.exe >nul 2>&1

timeout /t 3 >nul
call :print_message "所有服务已停止"
goto :eof

:: 清理构建文件
:clean_build
call :print_message "清理构建文件..."

if exist "%BACKEND_DIR%" (
    cd %BACKEND_DIR%
    mvn clean >nul 2>&1
    cd ..
    call :print_message "后端构建文件已清理"
)

if exist "%FRONTEND_DIR%" (
    cd %FRONTEND_DIR%
    if exist "dist" rmdir /s /q dist >nul 2>&1
    if exist "node_modules\.vite" rmdir /s /q node_modules\.vite >nul 2>&1
    cd ..
    call :print_message "前端构建文件已清理"
)
goto :eof

:: 安装依赖
:install_dependencies
call :print_message "安装项目依赖..."

if exist "%FRONTEND_DIR%" (
    call :print_message "安装前端依赖..."
    cd %FRONTEND_DIR%
    npm install
    cd ..
    call :print_message "前端依赖安装完成"
)
goto :eof

:: 构建项目
:build_project
call :print_message "构建项目 (环境: %ENV%)..."

if exist "%FRONTEND_DIR%" (
    call :print_message "构建前端项目..."
    cd %FRONTEND_DIR%
    
    if "%ENV%"=="prod" (
        npm run build:prod
    ) else if "%ENV%"=="test" (
        npm run build:test
    ) else (
        npm run build
    )
    
    cd ..
    call :print_message "前端构建完成"
)

if exist "%BACKEND_DIR%" (
    call :print_message "构建后端项目..."
    cd %BACKEND_DIR%
    mvn clean package -DskipTests -P%ENV%
    cd ..
    call :print_message "后端构建完成"
)
goto :eof

:: 启动后端服务
:start_backend
call :print_message "启动后端服务 (环境: %ENV%)..."

if not exist "%BACKEND_DIR%" (
    call :print_message "错误: 后端目录不存在"
    exit /b 1
)

cd %BACKEND_DIR%

if "%ENV%"=="prod" (
    if exist "target\document-review-*.jar" (
        start /b java -jar target\document-review-*.jar --spring.profiles.active=prod > ..\logs\backend.log 2>&1
    ) else (
        call :print_message "错误: 未找到jar包，请先构建项目"
        exit /b 1
    )
) else (
    start /b mvn spring-boot:run -Dspring-boot.run.profiles=%ENV% > ..\logs\backend.log 2>&1
)

cd ..

call :print_message "等待后端服务启动..."
set /a count=0
:wait_backend
set /a count+=1
if %count% gtr 30 (
    call :print_message "后端服务启动失败，请检查日志"
    exit /b 1
)
call :check_port %DEFAULT_BACKEND_PORT%
if %errorlevel%==0 (
    call :print_message "后端服务启动成功 (端口: %DEFAULT_BACKEND_PORT%)"
    goto :eof
)
timeout /t 2 >nul
goto :wait_backend

:: 启动前端服务
:start_frontend
call :print_message "启动前端服务 (环境: %ENV%)..."

if not exist "%FRONTEND_DIR%" (
    call :print_message "错误: 前端目录不存在"
    exit /b 1
)

cd %FRONTEND_DIR%

if "%ENV%"=="prod" (
    if exist "dist" (
        start /b npx serve -s dist -l %DEFAULT_FRONTEND_PORT% > ..\logs\frontend.log 2>&1
    ) else (
        call :print_message "错误: 未找到构建文件，请先构建项目"
        exit /b 1
    )
) else (
    start /b npm run dev > ..\logs\frontend.log 2>&1
)

cd ..

call :print_message "等待前端服务启动..."
set /a count=0
:wait_frontend
set /a count+=1
if %count% gtr 20 (
    call :print_message "前端服务启动失败，请检查日志"
    exit /b 1
)
call :check_port %DEFAULT_FRONTEND_PORT%
if %errorlevel%==0 (
    call :print_message "前端服务启动成功 (端口: %DEFAULT_FRONTEND_PORT%)"
    goto :eof
)
timeout /t 2 >nul
goto :wait_frontend

:: 查看服务状态
:show_status
call :print_message "服务状态检查..."
echo.
echo === 服务状态 ===

call :check_port %DEFAULT_BACKEND_PORT%
if %errorlevel%==0 (
    echo 后端服务: 运行中 (端口: %DEFAULT_BACKEND_PORT%)
    echo 后端地址: http://localhost:%DEFAULT_BACKEND_PORT%
    echo API文档: http://localhost:%DEFAULT_BACKEND_PORT%/swagger-ui.html
    echo H2控制台: http://localhost:%DEFAULT_BACKEND_PORT%/h2-console
) else (
    echo 后端服务: 未运行
)

echo.

call :check_port %DEFAULT_FRONTEND_PORT%
if %errorlevel%==0 (
    echo 前端服务: 运行中 (端口: %DEFAULT_FRONTEND_PORT%)
    echo 前端地址: http://localhost:%DEFAULT_FRONTEND_PORT%
) else (
    echo 前端服务: 未运行
)

echo.
goto :eof

:: 查看日志
:show_logs
if not exist "logs" (
    call :print_message "日志目录不存在"
    goto :eof
)

echo === 最近的日志 ===
echo.

if exist "logs\backend.log" (
    echo 后端日志 (最后20行):
    powershell "Get-Content logs\backend.log -Tail 20"
    echo.
)

if exist "logs\frontend.log" (
    echo 前端日志 (最后20行):
    powershell "Get-Content logs\frontend.log -Tail 20"
    echo.
)
goto :eof

:: 主函数
:main
:: 创建日志目录
if not exist "logs" mkdir logs

:: 显示启动信息
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    %PROJECT_NAME% v%VERSION%                    ║
echo ║                        启动脚本                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: 执行相应操作
if "%STOP_ONLY%"=="true" (
    call :stop_services
    exit /b 0
)

if "%STATUS_ONLY%"=="true" (
    call :show_status
    exit /b 0
)

if "%LOGS_ONLY%"=="true" (
    call :show_logs
    exit /b 0
)

if "%CLEAN_ONLY%"=="true" (
    call :clean_build
    exit /b 0
)

:: 检查依赖
call :check_dependencies
if %errorlevel% neq 0 exit /b 1

:: 停止现有服务
call :stop_services

:: 安装依赖
call :install_dependencies

:: 构建项目
if "%BUILD_ONLY%"=="true" (
    call :build_project
    call :print_message "构建完成"
    exit /b 0
)

if "%ENV%"=="prod" (
    call :build_project
)

call :print_message "启动环境: %ENV%"

:: 启动服务
if "%FRONTEND_ONLY%"=="true" (
    call :start_frontend
) else if "%BACKEND_ONLY%"=="true" (
    call :start_backend
) else (
    call :start_backend
    if %errorlevel% neq 0 exit /b 1
    call :start_frontend
)

:: 显示启动完成信息
echo.
call :print_message "🎉 系统启动完成！"
echo.
call :show_status

call :print_message "使用 'start.bat --stop' 停止所有服务"
call :print_message "使用 'start.bat --logs' 查看日志"
call :print_message "使用 'start.bat --status' 查看服务状态"

goto :eof

:: 执行主函数
call :main
