# 任务列表：Git 提交周报工具

## 开发阶段

### 基础设施构建

#### Task 1: 创建 CommitLog 数据模型
- **描述**：实现单条提交信息的数据模型类
- **验证**：能成功创建 CommitLog 对象，正确存储提交信息
- **文件**：`src/main/java/com/myth/earth/develop/service/git/CommitLog.java`
- **优先级**：高

#### Task 2: 创建 CommitReport 报告数据模型
- **描述**：实现报告聚合模型，包括 CommitReport、RepositoryCommits 和 ReportStatistics
- **验证**：能正确组织和存储报告数据结构
- **文件**：
  - `src/main/java/com/myth/earth/develop/service/git/CommitReport.java`
  - `src/main/java/com/myth/earth/develop/service/git/RepositoryCommits.java`
  - `src/main/java/com/myth/earth/develop/service/git/ReportStatistics.java`
- **依赖**：Task 1
- **优先级**：高

#### Task 3: 扩展 GitCommandExecutor 支持提交日志获取
- **描述**：
  1. 添加 `getCommitLogs()` 方法，使用 git log 获取提交信息
  2. 支持按作者、时间范围过滤
  3. 正确解析 git 输出，构建 CommitLog 对象列表
- **验证**：
  - 能正确获取指定作者的提交
  - 能正确处理日期过滤
  - 能正确解析提交消息（包含特殊字符）
- **文件**：修改 `src/main/java/com/myth/earth/develop/service/git/GitCommandExecutor.java`
- **优先级**：高

#### Task 4: 创建 GitCommitReporter 报告生成服务
- **描述**：
  1. 实现报告生成逻辑，遍历多个仓库收集提交
  2. 实现 Markdown 格式导出
  3. 实现纯文本格式导出
  4. 计算统计信息
- **验证**：
  - 能正确聚合多个仓库的提交
  - 生成的报告格式正确
  - 统计数据准确
- **文件**：`src/main/java/com/myth/earth/develop/service/git/GitCommitReporter.java`
- **依赖**：Task 2, 3
- **优先级**：高

### UI 层实现

#### Task 5: 创建 GitCommitReportToolViewImpl 工具类框架
- **描述**：
  1. 创建工具类并使用 @Tool 注解注册
  2. 初始化所有 UI 组件（ComboBox、按钮、文本区域等）
  3. 布置 UI 组件，形成合理的布局
  4. 添加基础的事件监听器框架
- **验证**：
  - UI 组件正确显示
  - 布局美观、参数清晰
  - 工具能在 IDE 中加载和显示
- **文件**：`src/main/java/com/myth/earth/develop/ui/toolkit/views/GitCommitReportToolViewImpl.java`
- **优先级**：高

#### Task 6: 实现作者列表加载与初始化
- **描述**：
  1. 在工具初始化时扫描所有仓库并加载作者列表
  2. 后台线程执行，提供加载状态反馈
  3. 填充作者下拉框
  4. 提供刷新功能
- **验证**：
  - 作者列表完整且无重复
  - UI 不卡顿
  - 加载状态正确显示
- **文件**：修改 `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitCommitReportToolViewImpl.java`
- **依赖**：Task 5, 使用 GitRepositoryFinder
- **优先级**：高

#### Task 7: 实现时间范围和作者选择逻辑
- **描述**：
  1. 实现时间范围选择（快捷选项 + 自定义日期）
  2. 自定义日期支持：
     - 手动输入文本框（格式：YYYY-MM-DD）
     - 日期选择器弹窗（点击按钮打开）
     - 日期格式和有效性验证
  3. 实现作者单选
  4. 实现格式选择
  5. 添加参数验证（日期范围、格式等）
- **验证**：
  - 时间范围计算正确
  - 快捷选项和自定义日期的切换逻辑正确
  - 日期格式验证有效（拒绝无效格式）
  - 日期范围验证有效（起始 <= 终止）
  - 参数选择符合用户操作
  - 参数验证完整
- **文件**：修改 `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitCommitReportToolViewImpl.java`
- **依赖**：Task 6
- **优先级**：中

#### Task 8: 实现报告生成和显示功能
- **描述**：
  1. 实现 "生成报告" 按钮的点击处理
  2. 后台线程执行报告生成，提供进度反馈
  3. 将生成的报告内容显示在 UI 中
  4. 错误处理和提示
- **验证**：
  - 报告内容正确显示
  - 进度反馈清晰
  - UI 不卡顿
  - 错误能被捕获和显示
- **文件**：修改 `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitCommitReportToolViewImpl.java`
- **依赖**：Task 4, 7
- **优先级**：高

#### Task 9: 实现复制和导出功能
- **描述**：
  1. 实现 "复制" 按钮，将报告内容复制到剪贴板
  2. 实现 "导出文件" 按钮（可选），支持保存为文件
  3. 实现 "清空" 按钮，清空报告内容
  4. 添加用户提示
- **验证**：
  - 复制功能有效
  - 导出文件成功保存
  - 清空功能正常
  - 用户提示显示
- **文件**：修改 `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitCommitReportToolViewImpl.java`
- **依赖**：Task 8
- **优先级**：中

### 测试实现

#### Task 10: 编写数据模型的单元测试
- **描述**：
  1. 测试 CommitLog、CommitReport 等数据模型的正确性
  2. 测试对象创建、字段赋值、获取等基本操作
- **验证**：所有测试通过，覆盖率 > 80%
- **文件**：
  - `src/test/java/com/myth/earth/develop/service/git/CommitLogTest.java`
  - `src/test/java/com/myth/earth/develop/service/git/CommitReportTest.java`
- **依赖**：Task 1, 2
- **优先级**：中

#### Task 11: 编写 GitCommitReporter 的单元测试
- **描述**：
  1. 测试报告生成逻辑
  2. 测试 Markdown 和纯文本格式导出
  3. 测试统计信息计算
  4. 测试边界情况（无提交、特殊字符等）
- **验证**：所有测试通过
- **文件**：`src/test/java/com/myth/earth/develop/service/git/GitCommitReporterTest.java`
- **依赖**：Task 4
- **优先级**：高

#### Task 12: 编写 GitCommandExecutor 新方法的单元测试
- **描述**：
  1. 测试 `getCommitLogs()` 方法的正确性
  2. 测试日期过滤
  3. 测试作者过滤
  4. 测试解析逻辑
- **验证**：所有测试通过
- **文件**：修改或创建 `src/test/java/com/myth/earth/develop/service/git/GitCommandExecutorTest.java`
- **依赖**：Task 3
- **优先级**：高

#### Task 13: 编写集成测试
- **描述**：
  1. 测试完整的报告生成流程（从参数选择到报告显示）
  2. 测试多仓库场景
  3. 测试错误处理
  4. 测试UI交互
- **验证**：所有测试通过
- **文件**：`src/test/java/com/myth/earth/develop/ui/toolkit/views/GitCommitReportToolViewImplTest.java`
- **依赖**：Task 5-9
- **优先级**：中

### 构建和验证

#### Task 14: 构建项目并运行所有测试
- **描述**：
  1. 执行 `./gradlew clean build`
  2. 运行全部单元测试
  3. 检查代码覆盖率和警告
  4. 修复任何编译或测试错误
- **验证**：
  - 编译无错误
  - 所有测试通过
  - 代码覆盖率 > 70%
- **优先级**：高

#### Task 15: 手动测试和 IDE 沙箱验证
- **描述**：
  1. 在 IDE 沙箱环境中运行插件
  2. 创建测试项目（包含多个 Git 仓库）
  3. 测试所有功能：参数选择、报告生成、复制、导出等
  4. 测试边界情况和错误场景
- **验证**：
  - 所有功能正常工作
  - UI 美观，交互流畅
  - 报告内容正确
  - 错误处理妥当
- **优先级**：高

#### Task 16: 性能测试与优化
- **描述**：
  1. 测试在不同规模仓库下的性能（3、10、20+ 仓库）
  2. 测试进度反馈的实时性
  3. 根据结果进行优化（如并行处理、缓存等）
- **验证**：
  - 小型项目 < 2s
  - 中型项目 < 5s
  - UI 保持响应
- **优先级**：中

#### Task 17: 文档更新（可选）
- **描述**：
  1. 更新项目 README 或使用说明
  2. 添加新功能的使用示例
- **文件**：README.md、CLAUDE.md 等
- **优先级**：低

## 依赖关系图

```
Task 1 (CommitLog)
    ↓
Task 2 (CommitReport)
    ↓
Task 3 (getCommitLogs)
    ↓ + Task 2
Task 4 (GitCommitReporter)
    ├─ Task 10 (数据模型测试)
    ├─ Task 11 (Reporter 测试)
    └─ Task 12 (Executor 测试)
    ↓
Task 5 (UI 框架)
    ↓
Task 6 (作者列表加载)
    ↓
Task 7 (参数选择)
    ↓
Task 8 (报告生成与显示)
    ↓
Task 9 (复制导出)
    ├─ Task 13 (集成测试)
    └─ Task 14 (构建测试)
    ↓
Task 15 (手动测试)
    ↓
Task 16 (性能测试)
    ↓
Task 17 (文档更新)
```

## 工作量估计（相对点数）

| Task | 点数 | 说明 |
|------|------|------|
| 1    | 2    | 简单数据模型 |
| 2    | 3    | 三个相关模型 |
| 3    | 4    | Git 命令执行和解析 |
| 4    | 5    | 报告生成、多格式导出 |
| 5    | 3    | UI 框架构建 |
| 6    | 4    | 作者列表加载，后台线程 |
| 7    | 3    | 参数选择逻辑 |
| 8    | 6    | 核心报告生成流程，进度反馈 |
| 9    | 3    | 复制、导出、清空功能 |
| 10   | 3    | 数据模型测试 |
| 11   | 4    | Reporter 功能测试 |
| 12   | 3    | Executor 方法测试 |
| 13   | 4    | 集成测试 |
| 14   | 2    | 构建和测试运行 |
| 15   | 4    | 手动测试和调试 |
| 16   | 3    | 性能测试和优化 |
| 17   | 2    | 文档更新 |
| **总计** | **58** | |

