# 提案：Git 提交周报工具

## 摘要

在现有 Git 代码统计工具的基础上，新增一个"Git 提交周报"功能。该功能允许用户：
1. 选择统计时间范围（最近7天、最近30天、最近1年、自定义）
2. 从所有提交作者中选择一个特定用户
3. 自动遍历项目目录下所有 Git 仓库
4. 提取该用户在各仓库中的提交信息（提交哈希、日期、提交消息）
5. 生成结构化的周报文档（Markdown 格式）
6. 在 UI 中实时展示报告内容，支持复制和导出

## 问题陈述

### 当前场景

在 Monorepo 或多仓库项目中，开发者需要快速获取自己在过去一段时间内的工作总结。现有的 Git 统计工具提供了代码行数统计，但缺少**跨仓库的提交消息汇总**功能，导致用户需要手动遍历各仓库查看提交历史。

### 解决方案目标

- **自动化**：自动扫描所有仓库，无需手动指定
- **结构化**：按仓库组织输出，便于生成周报或月报
- **易于分享**：支持导出为 Markdown、纯文本或其他格式
- **用户友好**：在 IDE 中直接展示，支持复制、下载等操作

## 核心特性

### 1. 提交消息聚合（核心功能）
- 时间范围过滤：最近 7 天、30 天、1 年或自定义日期范围
- 作者单选：在所有作者列表中选择一个用户
- 多仓库遍历：自动识别并扫描项目目录下所有 Git 仓库
- 提交信息提取：`git log --author=<user> --pretty=format:"<template>"`

### 2. 报告生成与展示
- 结构化输出：按仓库分组，展示提交信息
- 多种格式：
  - **Markdown**（默认）：包含标题、仓库章节、提交列表、统计总结
  - **纯文本**：简化格式，便于邮件
  - **JSON**（可选）：程序化使用
- 统计汇总：总仓库数、总提交数、总提交消息字数等

### 3. UI 集成
- **新建工具类**：`GitCommitReportToolViewImpl`（或集成到现有 Git 统计工具）
- **参数面板**：时间范围、作者选择、导出格式选择
- **结果展示**：富文本或表格展示报告内容
- **操作按钮**：生成报告、复制内容、导出文件、清空结果

## 设计考量

### 架构层面
1. **复用现有组件**
   - 复用 `GitRepositoryFinder` 扫描仓库
   - 复用 `GitCommandExecutor` 执行 Git 命令
   - 复用 `GitRepository` 数据模型

2. **新增服务层组件**
   - `GitCommitReporter`：提交信息收集和报告生成
   - `CommitLog`：单条提交信息的数据模型
   - `CommitReport`：报告数据的聚合模型

3. **UI 层**
   - 新增工具 `GitCommitReportToolViewImpl`
   - 或在现有 Git 统计工具中添加新的选项卡

### 性能考虑
- 大型 Monorepo 中可能包含数十个仓库，执行 `git log` 的耗时需要控制
- 使用后台线程执行，提供进度反馈（如 "已处理 3/10 仓库"）
- 考虑添加超时控制和并行处理（如 ExecutorService 并发查询）

### 错误处理
- 某个仓库无法访问时，记录并继续处理其他仓库
- 提交消息包含特殊字符（如换行、特殊转义符）的处理

## 影响范围

### 新增文件
1. `src/main/java/com/myth/earth/develop/service/git/GitCommitReporter.java` - 报告生成服务
2. `src/main/java/com/myth/earth/develop/service/git/CommitLog.java` - 提交信息数据模型
3. `src/main/java/com/myth/earth/develop/service/git/CommitReport.java` - 报告数据模型
4. `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitCommitReportToolViewImpl.java` - UI 工具类
5. 对应的单元测试和集成测试

### 修改文件
1. `src/main/java/com/myth/earth/develop/service/git/GitCommandExecutor.java` - 可能需要添加新方法用于获取提交信息
2. `src/main/resources/META-INF/plugin.xml` - 注册新工具（如为独立工具）

### 不需要修改
- 现有的 Git 统计工具类和方法保持不变
- 项目配置文件无需改动

## 验收标准

1. **功能完整性**
   - ✓ 能正确扫描并识别所有 Git 仓库
   - ✓ 能按时间范围和作者过滤提交
   - ✓ 能生成 Markdown 格式报告
   - ✓ 报告包含仓库名、提交哈希、日期、消息等关键信息

2. **用户体验**
   - ✓ UI 布局清晰，参数选择方便
   - ✓ 实时进度反馈（如 loading 动画）
   - ✓ 支持复制报告内容到剪贴板
   - ✓ 支持导出为文件（可选功能）

3. **性能**
   - ✓ 小型项目（< 5 仓库）报告生成在 2 秒内完成
   - ✓ 中型项目（5-20 仓库）在 5 秒内完成
   - ✓ 不阻塞 UI 线程

4. **错误处理**
   - ✓ 无法访问仓库时显示警告
   - ✓ 特殊字符处理正确
   - ✓ 边界情况处理妥当（如无提交、无仓库等）

## 参考实现

用户提供的 Bash 脚本展示了基本功能流程：
```bash
# 1. 扫描项目目录
for repo in "$basepath"/*; do
    if git -C "$repo" rev-parse --git-dir > /dev/null 2>&1; then
        # 2. 获取特定作者的提交
        commits=$(git -C "$repo" log --since="1 weeks ago" --author="$author_name" ...)
        # 3. 生成输出文件
        echo "..." >> "$output_file"
    fi
done
```

本提案将这个流程封装为 IDE 工具，提供更好的用户体验和集成度。

## 后续可扩展功能

- 支持多格式导出（HTML、PDF、JSON）
- 支持自定义报告模板
- 集成日历视图，展示提交分布
- 支持团队成员对比
- 定时生成和邮件推送（需插件特性）

