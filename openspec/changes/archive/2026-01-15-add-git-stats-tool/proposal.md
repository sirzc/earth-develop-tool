# Change: 新增 Git 代码统计工具

## Why

开发者在代码审查、绩效评估和项目分析时，需要快速获取代码提交的统计数据。现有的 Git 命令行工具虽然强大，但使用不够便捷。在 IDE 中集成 Git 统计工具，让开发者能够：
- 快速查看特定时间范围内的代码变动统计
- 按多个维度（日期、分支、作者）分析提交情况
- 直观比较团队成员的贡献度

## What Changes

- **新增工具类别**：在 `ToolCategory` 中添加 `GIT` 工具类别
- **新增 Git 统计工具**：实现 `GitStatisticsToolViewImpl` 工具类，提供：
  - 时间范围选择（按日期范围、快捷相对时间）
  - 分支选择过滤
  - 作者选择过滤
  - 多维度代码统计（增加行数、删除行数、提交次数、修改文件数）
  - 表格形式结果展示
- **Git 解析工具类**：创建 `GitStatisticsParser` 用于解析 Git 命令输出并计算统计数据
- **后端服务支持**：实现 Git 数据检索和计算逻辑

## Impact

- **Affected specs**: 新增 `git-statistics` 能力
- **Affected code**:
  - `src/main/java/com/myth/earth/develop/ui/toolkit/core/ToolCategory.java`
  - `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitStatisticsToolViewImpl.java`（新建）
  - `src/main/java/com/myth/earth/develop/service/git/GitStatisticsParser.java`（新建）
  - `src/main/java/com/myth/earth/develop/service/git/GitCommandExecutor.java`（新建）
- **UI Components**: 新增日期选择器、分支选择器、作者多选、结果表格显示
- **Dependencies**: 无新增外部依赖，使用现有的 Git 命令行接口
