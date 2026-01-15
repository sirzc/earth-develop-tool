# 设计文档：Git 代码统计工具

## Context

### 背景

项目是一个 IntelliJ IDEA 插件，提供多种开发工具。现需添加 Git 代码统计功能，让开发者能在 IDE 中快速获取代码提交统计信息，用于代码审查、绩效评估等场景。

### 约束条件

1. **平台依赖**：基于 IntelliJ Platform，使用 JetBrains plugin dev kit 组件
2. **Git 依赖**：调用系统 Git 命令行工具，不新增第三方 Git 库
3. **性能**：Git 命令执行应在合理时间内完成（<30秒）
4. **兼容性**：需支持 IntelliJ 2022.1+ 版本
5. **编码**：Java 源代码文件使用 UTF-8 编码

## Goals

### 功能目标

1. 在 IDE 中提供 Git 统计工具，支持多维度分析
2. 提供直观的表格展示统计结果
3. 支持数据导出（复制）为多种格式
4. 提供友好的错误提示

### 非目标

- 不提供 Git 可视化图形（如提交拓扑图、热力图等）
- 不修改现有 Git 仓库或提交历史
- 不实现详细的提交日志查看功能

## Decisions

### Decision 1: Git 命令执行策略

**选择**：调用系统 Git 命令行工具，使用 `ProcessBuilder` 执行

**理由**：
- 避免添加第三方 Git 库的依赖
- 项目已有类似的命令执行模式（如 MyBatis 日志解析）
- Git 命令输出格式相对稳定，易于解析

**替代方案考虑**：
- JGit 库：功能完整但增加依赖，不符合"简单第一"的原则
- Git 原生调用：选定的方案

### Decision 2: 数据统计粒度

**选择**：支持按作者分组展示统计数据

**理由**：
- 满足用户需求（按作者过滤且显示）
- 表格形式便于查看各作者的贡献
- 可轻松扩展为其他维度（分支、日期等）

**实现细节**：
- 内部统计时，记录每个作者的详细数据（而非汇总）
- UI 显示时，允许按作者分组或显示总计

### Decision 3: 时间范围处理

**选择**：在 UI 层提供快捷选项，后端使用 `--after` 和 `--before` Git 参数

**理由**：
- 快捷选项提升用户体验
- Git 命令原生支持日期过滤，无需额外逻辑

**实现细节**：
- 快捷选项：最近7天、30天、1年（由 UI 层计算实际日期）
- 自定义日期：用户输入，验证后传给后端

### Decision 4: 错误处理和超时

**选择**：
- 检测 Git 仓库有效性（在工具初始化时）
- 为 Git 命令设置 30 秒超时
- 提供清晰的错误提示信息

**理由**：
- 防止 IDE 卡顿
- 用户需要了解失败原因以便调试

## Risks / Trade-offs

| 风险 | 缓解策略 |
|------|----------|
| Git 命令执行慢 | 设置超时、使用异步执行、显示进度提示 |
| 大仓库数据量大 | 限制统计时间范围为最多一年；如需更长时间，分批查询 |
| 作者名称编码问题 | 使用 UTF-8 处理 Git 输出；在 build.gradle 中已配置 UTF-8 编码 |
| 分支/作者列表动态变化 | 提供刷新按钮，用户手动更新列表 |

## Migration Plan

该功能为新增功能，无迁移需求。用户首次使用时，系统自动加载分支和作者列表。

## Architecture & Implementation Notes

### 模块划分

```
service/git/
├── GitCommandExecutor.java      # Git 命令执行
├── GitStatisticsParser.java     # 数据解析和统计
└── model/
    └── GitStatistics.java       # 统计结果模型

ui/toolkit/views/
└── GitStatisticsToolViewImpl.java  # UI 主组件

ui/component/
└── GitStatsResultTable.java     # 结果表格组件（可选）
```

### 核心类设计

#### GitCommandExecutor

```java
public class GitCommandExecutor {
    private File projectRoot;

    // 获取分支列表（本地+远程）
    public List<String> getBranches() throws GitException

    // 获取指定时间范围、分支的作者列表
    public List<String> getAuthors(String branch, Date startDate, Date endDate)
        throws GitException

    // 获取提交统计数据
    public Map<String, RawStatistics> getStatistics(
        String branch, Date startDate, Date endDate, List<String> authors)
        throws GitException
}
```

#### GitStatisticsParser

```java
public class GitStatisticsParser {
    // 解析 git log 输出，计算行数统计
    public static Map<String, LineStatistics> parseLineStats(String gitLogOutput)

    // 解析文件修改列表
    public static Set<String> parseModifiedFiles(String gitDiffOutput)
}
```

#### GitStatistics 数据模型

```java
public class GitStatistics {
    private String author;
    private int commitCount;
    private int linesAdded;
    private int linesRemoved;
    private int filesModified;

    // ... getters, setters
}
```

### UI 交互流程

1. 工具初始化 → 自动加载分支列表和作者列表
2. 用户选择时间范围、分支、作者 → 更新相关列表（如分支变更则刷新作者列表）
3. 用户点击"统计" → 后端执行、更新结果表格
4. 用户点击"复制" → 将表格数据导出为指定格式

### 错误处理流程

```
用户点击统计
  → 校验参数（时间范围、分支有效性）
    → 校验失败：显示错误提示
    → 校验成功：执行 Git 命令
      → 命令成功：解析数据、显示结果
      → 命令失败：显示错误信息（包含错误原因）
      → 命令超时：显示超时提示
```

## Open Questions

1. **大仓库性能优化**：如果仓库非常大且时间范围很长，是否需要实现分页或分批查询？
   - 暂定：提示用户缩小时间范围

2. **合并提交处理**：`git log` 默认处理合并提交，是否需要特殊处理？
   - 暂定：使用 `git log` 默认行为

3. **表格自动刷新**：是否需要周期性自动刷新数据（如检测到新提交）？
   - 暂定：不实现自动刷新，用户手动点击统计

