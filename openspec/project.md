# Project Context

## Purpose

Earth Develop Tool 是一个 IntelliJ IDEA 插件，为开发者提供一套便捷的开发工具集合，包括编码解码、数据转换、文本处理、代码统计等功能，提升开发效率。

## Tech Stack

- **语言**：Java (JDK 1.8.0)
- **构建工具**：Gradle 7.6.4
- **IDE 框架**：IntelliJ Platform (2022.1+)
- **UI 框架**：Swing（IntelliJ 组件库）
- **主要依赖**：
  - fastjson 1.2.83 (JSON 处理)
  - hutool-core 5.8.35 (核心工具库)
  - hutool-crypto 5.8.35 (加密工具)
  - snakeyaml 2.0 (YAML 处理)
  - commons-text 1.14.0 (文本处理)
  - zxing 3.0.0 (二维码)

## Project Conventions

### Code Style

- **编码格式**：UTF-8
- **Java 版本**：JDK 1.8.0（所有代码需兼容 Java 8）
- **命名规范**：
  - 类名：PascalCase（例：GitStatisticsToolViewImpl）
  - 方法名：camelCase（例：getStatistics）
  - 常量：UPPER_SNAKE_CASE（例：TIMEOUT_SECONDS）
  - 包名：小写点分形式（例：com.myth.earth.develop.service.git）
- **注释语言**：中文（使用中文注释便于团队理解）
- **License**：Apache License 2.0（所有文件需包含 License 头）
- **代码格式化**：
  - 缩进：4 个空格
  - 行长度：建议不超过 120 字符
  - 大括号风格：Java 风格（开括号与语句同行）

### Architecture Patterns

- **三层结构**：
  - **Core Framework**：`ui/toolkit/core/` 定义工具基础接口和注解
  - **Loader System**：`ToolkitLoader` 使用反射动态发现和加载 `@Tool` 注解的工具类
  - **UI Components**：`ui/toolkit/views/` 存放具体的工具实现

- **工具开发模式**：
  1. 创建类继承 `AbstractToolView`
  2. 使用 `@Tool` 注解标记工具元数据（分类、名称、描述等）
  3. 在构造函数或 `initializeUI()` 方法中初始化 UI
  4. `ToolkitLoader` 自动发现并加载该工具

- **后端服务模式**：
  - 命令执行器（Executor）：封装外部命令调用（如 Git 命令）
  - 数据解析器（Parser）：解析命令输出并提取数据
  - 数据模型（Model）：定义统计或处理结果的对象结构

- **通知和消息提示**：
  - 使用 `PluginNotifyKit` 进行用户提示
  - 可用方法：`info(project, message)` 信息提示、`warn(project, message)` 警告、`error(project, message)` 错误提示
  - 严禁使用不存在的方法（如 `infoNotify`、`warnNotify`、`errorNotify` 等）

### Testing Strategy

- **测试框架**：JUnit 5 (Jupiter)
- **运行命令**：`gradle test`
- **测试位置**：`src/test/java/`，包结构与源代码对应
- **测试覆盖**：
  - 单元测试：测试解析器、工具类的核心逻辑
  - 集成测试（可选）：在 IDE 沙箱环境中验证 UI 交互

### Git Workflow

- **主分支**：`main`
- **提交消息**：
  - 格式：`<type>: <description>`
  - 类型：feat (新功能), fix (修复), perf (性能), refactor (重构), test (测试), docs (文档)
  - 示例：`feat: 新增 Git 代码统计工具`
- **Co-Authored-By**：使用 AI 代码生成时，在提交消息中添加 `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`

## Domain Context

- **开发者目标用户**：IntelliJ IDEA 用户，需要快速进行数据编码/解码、格式转换、代码分析等操作
- **工具分类**：
  - 编码解码（Base64、AES 等）
  - 数据转换（JSON/YAML/Properties 互转）
  - 时间工具（时间戳转换）
  - 代码分析（SQL 转换、Git 统计）
  - 图像工具（二维码生成/解析）
  - 比较工具（JSON/XML/YAML diff）
- **插件加载机制**：通过 IntelliJ 的扩展点（extension point）在 IDE 启动时加载插件，工具通过 `ToolkitLoader` 动态发现

## Important Constraints

1. **Java 版本兼容性**：必须使用 JDK 1.8.0 兼容的语法和 API，禁用 Java 9+ 特性（如模块系统、新的流 API 等）
2. **IDE 版本**：支持 IntelliJ 2022.1 及以上版本（基于 IJ Platform 版本号 > 212.4746.92）
3. **UTF-8 编码**：所有源文件必须使用 UTF-8 编码，在 `build.gradle` 中已配置：
   ```gradle
   tasks.withType(JavaCompile) {
       options.encoding = "UTF-8"
   }
   ```
4. **无新增主要依赖**：尽量复用现有依赖，新增依赖需评估影响
5. **插件大小**：最终的 JAR 包应保持在合理大小，避免无谓的依赖膨胀
6. **性能**：长时间的操作（如 Git 命令）应设置超时保护，防止 IDE 卡顿

## External Dependencies

- **IntelliJ Platform SDK**：2022.1 版本，提供 IDE 集成接口和 UI 组件
- **System Git**：某些工具（如 Git 统计）依赖系统的 Git 命令行工具，通过 `ProcessBuilder` 调用
- **系统文件系统**：二维码保存、文件选择等操作需要访问用户本地文件系统
