# Change: 修复 Git 代码统计工具无法显示结果

## Why

GitStatisticsToolViewImpl 工具在执行统计后无法显示任何结果，用户点击"统计"按钮后无反应或结果为空。经过诊断，发现存在以下问题：

1. **Git 命令执行问题**：
   - Git 命令执行逻辑中，`git log` 输出格式与解析器期望格式不匹配
   - 解析器期望格式：`作者名\n行数变更行\n...`，但实际可能是其他格式

2. **数据流问题**：
   - Git 命令可能失败但错误提示不清晰
   - 统计完成后表格更新逻辑有缺陷（文本转数字不当）

3. **UI 问题**：
   - 表格模型初始化后未正确刷新
   - 结果显示前缺少数据校验

## What Changes

- **修改 GitCommandExecutor**：
  - 修正 `getStatistics()` 中 Git 命令的参数拼接（避免 bash -c 中的单引号问题）
  - 使用更可靠的 Git 命令格式：`git log --numstat --pretty=format:%an`
  - 改进错误处理和日志记录

- **修改 GitStatisticsParser**：
  - 调整解析逻辑，正确处理 Git 输出格式
  - 增加边界情况处理（空输出、无提交等）

- **修改 GitStatisticsToolViewImpl**：
  - 修复表格数据更新逻辑（确保数据为整数类型，而非字符串）
  - 增加表格结果的验证和刷新
  - 改进错误提示的可见性
  - **移除不必要的数据类型选择器**，保持 UI 简洁

## Impact

- **Affected specs**: `git-statistics` - Git 代码统计能力
- **Affected code**:
  - `src/main/java/com/myth/earth/develop/service/git/GitCommandExecutor.java`
  - `src/main/java/com/myth/earth/develop/service/git/GitStatisticsParser.java`
  - `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitStatisticsToolViewImpl.java`
- **Breaking changes**: 无。仅修复功能，API 保持不变
- **Benefits**:
  - Git 统计工具正常显示结果
  - 表格支持列头点击排序
  - 更清晰的错误提示
  - 用户体验改进
