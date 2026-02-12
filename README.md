#🤖通用型AI助手
在污水监控系统基础上，集成大语言模型，添加了通用型AI助手，支持通过对话分析实时水质数据、获取污染分析报告、诊断设备异常。

# 🌊 SewageWatch - 污水监控系统

一个基于实时视频流的智能污水监控系统，支持无人机RTMP推流、实时视频分析和任务管理。

## 📋 项目概述

SewageWatch是一个综合性的污水监控解决方案，集成了多种技术栈来实现实时视频监控、数据管理和任务调度。系统主要用于环境监测，特别是河道污水排放的实时监控和管理。

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   移动端推流     │───▶│   RTMP服务器     │───▶│   视频处理服务   │
│  (DJI无人机)    │    │ (Node.js)       │    │   (Python)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web播放器     │    │   后端API服务    │    │   数据库服务     │
│   (HTML5)       │◀───│  (Spring Boot)  │───▶│   (MySQL)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   前端管理界面   │
                       │    (Vue.js)     │
                       └─────────────────┘
```

## 🚀 核心功能

### 📹 实时视频监控
- **RTMP流接收**: 支持移动端(DJI无人机)实时推流
- **H.264优化**: 专门优化的H.264解码处理，解决常见的解码错误
- **WebSocket传输**: 实时视频帧传输到Web端
- **多格式支持**: 支持RTMP/HLS多种流媒体格式

### 🎯 任务管理系统
- **任务创建**: 支持创建巡逻、确认等不同类型任务
- **优先级管理**: 五级优先级系统(LOWEST/LOW/MEDIUM/HIGH/HIGHEST)
- **状态跟踪**: TODO/DOING/DONE状态管理
- **截止时间**: 任务截止时间提醒

### 🔧 系统管理
- **用户权限**: 基于角色的用户权限管理
- **实时监控**: 系统性能和连接状态监控
- **日志记录**: 详细的操作和错误日志

## 🛠️ 技术栈

### 后端服务
- **Python服务**: FastAPI + OpenCV + FFmpeg + WebSocket
- **Java服务**: Spring Boot 3.4.6 + MySQL
- **RTMP服务**: Node.js + node-media-server

### 前端界面
- **管理界面**: Vue.js 3 + Element Plus + Vite
- **视频播放**: HTML5 + HLS.js

### 数据存储
- **数据库**: MySQL 8.0+
- **媒体文件**: 本地文件系统

### 开发工具
- **容器化**: Docker + Docker Compose
- **构建工具**: Maven + npm

## 📦 项目结构

```
sewageWatch/
├── README.md                           # 项目说明文档
├── sewage-watch-Python/                # Python视频处理服务
│   ├── main.py                         # 主服务程序
│   └── README.md                       # Python服务说明
├── sewage-watch-JAVA/                  # Java后端API服务
│   ├── src/main/java/com/zhuchen/      # Java源码
│   ├── src/main/resources/             # 配置文件
│   ├── pom.xml                         # Maven配置
│   ├── Dockerfile                      # Docker构建文件
├── Vue/                                # Vue前端管理界面
│   ├── src/                            # Vue源码
│   ├── package.json                    # npm配置
├── createDatabase.sql                  # 数据库初始化脚本
```

## 🚀 快速开始

### 环境要求
- **Node.js**: 16.0+
- **Python**: 3.8+
- **Java**: 17+
- **MySQL**: 8.0+
- **FFmpeg**: 4.0+ (用于视频处理)

### 1. 数据库初始化

```sql
-- 创建数据库
mysql -u root -p < createDatabase.sql
```

### 2. 启动服务
**执行start.bat文件**

开放端口如下:
* 5173 ->  Vue服务
* 1935 ->  RTMP服务器(接收端口)
* 8080 ->  Java服务
* 8081 ->  Python服务
### 3. 终止服务
**执行stop.bat文件**

## 📱 使用说明

### RTMP推流配置

**推流地址格式**:
```
rtmp://服务器IP:1935/live/流名称
```

**示例**:
```
rtmp://192.168.1.100:1935/live/drone_stream
```


### 管理界面功能

1. **任务管理**: 创建、编辑、删除监控任务
2. **实时监控**: 查看当前视频流状态
3. **历史记录**: 查看任务执行历史
4. **用户管理**: 管理系统用户和权限

## 🔧 配置说明

### RTMP服务器配置 (server.js)

```js
const NodeMediaServer = require('node-media-server');
const config = {
  rtmp: { port: 1935, chunk_size: 4096 }, // 接收 RTMP
  http: { port: 8000, allow_origin: '*' }  // 输出 WebSocket
};
const nms = new NodeMediaServer(config);
nms.run();
```

### 数据库配置 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sewagewatch
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 🚢 ~~Docker部署(待完成)~~

~~### Java服务Docker化~~

```bash
cd sewage-watch-JAVA

# 构建镜像
docker build -t sewage-watch-java .

# 运行容器
docker run -p 8080:8080 sewage-watch-java
```


## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request


## 🙏 致谢
感谢以下开源项目的支持：
- [OpenCV](https://opencv.org/) - 计算机视觉库
- [FastAPI](https://fastapi.tiangolo.com/) - 现代Python Web框架
- [Spring Boot](https://spring.io/projects/spring-boot) - Java应用框架
- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [Node Media Server](https://github.com/illuspas/Node-Media-Server) - RTMP服务器
- [Element Plus](https://element-plus.org/) - Vue 3 UI组件库

---

**注意**: 本系统主要用于环境监测和教育目的，请确保在使用过程中遵守相关法律法规。
