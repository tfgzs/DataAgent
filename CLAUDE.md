# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在本仓库工作时提供指导。

## 项目概述

**Spring AI Alibaba DataAgent** 是一个基于 Spring AI Alibaba Graph 构建的企业级智能数据分析 Agent，支持 Text-to-SQL、Python 深度分析、智能报告生成和 MCP 服务器集成。

## 技术栈

- **后端**: Java 17, Spring Boot 3.4.8, Spring AI Alibaba 1.1.0.0, Spring WebFlux, MyBatis, Lombok
- **前端**: Vue 3, TypeScript, Vite, Element Plus, ECharts
- **构建**: Maven（优先使用 mvnd）、Makefile（封装 CI/make/\*.mk）
- **测试**: JUnit 5, Mockito, Testcontainers, Reactor Test, Awaitility, JaCoCo
- **数据库**: MySQL 5.7+（主库），支持 PostgreSQL、Oracle、SQL Server、Hive、达梦

## 仓库结构

```
├── data-agent-management/    # 后端 Spring Boot 应用
│   └── src/main/java/.../dataagent/
│       ├── controller/       # REST API 控制器
│       ├── service/          # 业务服务层
│       ├── workflow/         # StateGraph 工作流节点
│       ├── mapper/           # MyBatis Mapper
│       ├── entity/           # 数据库实体
│       ├── dto/              # 数据传输对象
│       ├── config/           # Spring 配置
│       ├── prompt/           # LLM 提示词模板
│       └── strategy/         # 检索与融合策略
├── data-agent-frontend/      # Vue 3 Web 前端
│   └── src/
│       ├── components/
│       ├── views/
│       ├── services/         # API 客户端服务
│       └── router/
├── docs/                     # 架构、开发者和使用文档
├── CI/                       # Makefile、linter 配置、CI 脚本
└── Makefile                  # 顶层 Make 入口
```

## 核心架构

系统使用 **StateGraph 工作流引擎**（来自 Spring AI Alibaba Graph）编排数据分析流程：

1. **IntentRecognitionNode** - 理解用户查询意图
2. **EvidenceRecallNode** - 基于 RAG 的知识召回与查询重写
3. **SchemaRecallNode** - 召回相关数据库 Schema
4. **PlannerNode** - 生成带有序步骤的分析计划
5. **SqlGenerateNode** - 将计划步骤转换为 SQL，含语义一致性检查
6. **PythonGenerateNode** - 生成 Python 代码进行深度分析（统计、机器学习）
7. **ReportGeneratorNode** - 生成包含 ECharts 图表的 HTML/Markdown 报告

关键子系统：
- **AiModelRegistry**: Chat/Embedding 模型动态切换
- **AgentVectorStoreService**: 统一向量检索（支持 ES、Milvus、PGVector 等）
- **CodePoolExecutorService**: 通过 Docker 或本地进程执行 Python
- **MultiTurnContextManager**: 多轮对话状态管理
- **McpServerService**: MCP 协议服务器，对外暴露 NL2SQL 工具

## 常用命令

### 后端（Java）

```bash
# 构建（跳过测试）
make build
# 或直接用 Maven：
cd data-agent-management && mvnd -DskipTests=true package

# 运行测试
make test
# 或：cd data-agent-management && mvnd test

# 运行单个测试类
cd data-agent-management && mvnd test -Dtest=你的测试类名

# 运行单个测试方法
cd data-agent-management && mvnd test -Dtest=你的测试类名#你的测试方法名

# 启动后端服务
cd data-agent-management && ./mvnw spring-boot:run

# 代码格式化
make format-fix        # 应用 Spring Java Format
make spotless-apply    # 应用 Spotless 格式化

# 代码风格检查
make format-check      # 验证格式
make checkstyle-check  # 运行 Checkstyle
```

### 前端（Vue/TypeScript）

```bash
cd data-agent-frontend

# 安装依赖
npm install

# 启动开发服务器（默认端口 9521）
npm run dev

# 生产构建
npm run build

# 代码检查
npm run lint            # 修复 Lint 问题
npm run lint:check      # 仅检查
npm run format          # Prettier 格式化
npm run type-check      # TypeScript 类型检查
```

### Linter（顶层）

```bash
make lint               # 运行 yaml-lint、codespell、换行检查
make secrets-check      # gitleaks 敏感信息扫描
make licenses-check     # 检查许可证头
make licenses-fix       # 修复许可证头
```

### 数据库

```bash
# 初始化数据库
mysql -u root -p < data-agent-management/src/main/resources/sql/schema.sql
```

## 测试

- 使用 **JUnit 5** + **Mockito** 进行单元测试
- 使用 **Testcontainers**（MySQL 容器）进行集成测试
- 使用 **Reactor Test** 测试 WebFlux 流
- JaCoCo 强制要求每个包 **80% 以上行覆盖率**
- 测试源码：`data-agent-management/src/test/java/`

## 配置

所有应用配置在 `application.yml` 的 `spring.ai.alibaba.data-agent` 前缀下。常用配置：

### 基础配置
- `llm-service-type`: STREAM 或 BLOCK 模式
- `code-executor.code-pool-executor`: DOCKER 或 LOCAL Python 执行
- `vector-store.enable-hybrid-search`: 启用关键词 + 向量混合检索
- `max-sql-retry-count`: SQL 重试次数（默认 10）

### 安全认证（Basic Auth）
- `security.username`: 登录用户名（默认 `admin`，可通过环境变量 `DATA_AGENT_AUTH_USERNAME` 覆盖）
- `security.password`: 登录密码（可通过环境变量 `DATA_AGENT_AUTH_PASSWORD` 覆盖）
- `security.role`: 用户角色（默认 `ADMIN`）

### 端口配置
- 后端服务端口：`8065`（`server.port`）
- 前端开发端口：`9521`（`vite.config.js`）

### CORS 白名单
后端 SecurityConfig 仅允许以下来源访问：${DATA_AGENT_ALLOWED_ORIGINS}

## 数据库表名规范

所有系统业务表统一使用 `saad_` 前缀，例如：
- `saad_agent`（智能体表）
- `saad_datasource`（数据源表）
- `saad_model_config`（模型配置表）
- `saad_chat_session`（会话表）
- `saad_chat_message`（消息表）

MyBatis Mapper 中的 SQL 语句、schema.sql、h2 schema 均已统一使用此前缀。示例业务表（`product_schema.sql` 中的 users、products 等）不加此前缀。

## 代码风格

- **Java**: Google Java Style，通过 spring-javaformat 插件校验（4 空格缩进，行宽 120 字符）
- **TypeScript**: Prettier + ESLint（2 空格缩进，禁止 `any` 类型）
- 使用 Lombok 减少样板代码
- 公共类和方法应有 JavaDoc 注释
- 所有文件需包含 Apache 2.0 许可证头（spotless 自动校验）

## 重要规则

- **本文档及所有项目文档必须使用中文**
- 代码注释优先使用中文
- 提交信息使用中文
