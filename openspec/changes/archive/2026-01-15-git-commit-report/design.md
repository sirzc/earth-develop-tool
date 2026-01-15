# 设计文档：Git 提交周报工具

## 架构设计

### 1. 数据模型层

#### 1.1 CommitLog（单条提交信息）
```java
public class CommitLog {
    private String hash;              // 提交哈希（短或完整）
    private String author;            // 提交作者
    private LocalDate date;           // 提交日期
    private String message;           // 提交消息（第一行或完整内容）
    private int filesChanged;         // 修改的文件数（可选）
    private int additions;            // 增加的行数（可选）
    private int deletions;            // 删除的行数（可选）
}
```

#### 1.2 CommitReport（报告聚合模型）
```java
public class CommitReport {
    private String author;                           // 报告对应的作者
    private LocalDate startDate, endDate;            // 时间范围
    private List<RepositoryCommits> repositories;    // 仓库列表及其提交
    private ReportStatistics statistics;             // 统计信息
}

public class RepositoryCommits {
    private GitRepository repository;                // 仓库信息
    private List<CommitLog> commits;                 // 该仓库的提交列表
}

public class ReportStatistics {
    private int totalRepositories;    // 总仓库数
    private int repositoriesWithCommits; // 有提交的仓库数
    private int totalCommits;         // 总提交数
    private int totalFilesChanged;    // 总修改文件数
    private int totalAdditions;       // 总增加行数
    private int totalDeletions;       // 总删除行数
}
```

### 2. 服务层

#### 2.1 GitCommitReporter（报告生成器）
**职责**：聚合多个仓库的提交信息，生成结构化报告

**关键方法**：
```java
/**
 * 生成提交周报
 * @param repositories 仓库列表
 * @param author 作者名
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @return 报告对象
 */
public CommitReport generateReport(List<GitRepository> repositories,
                                   String author,
                                   LocalDate startDate,
                                   LocalDate endDate)
        throws GitException;

/**
 * 将报告导出为 Markdown
 */
public String exportAsMarkdown(CommitReport report);

/**
 * 将报告导出为纯文本
 */
public String exportAsPlainText(CommitReport report);

/**
 * 将报告导出为 JSON（可选）
 */
public String exportAsJson(CommitReport report);
```

#### 2.2 GitCommandExecutor 增强
在现有的 `GitCommandExecutor` 中添加新方法，用于获取更详细的提交信息：

```java
/**
 * 获取指定作者在指定时间范围内的所有提交
 * @param branch 分支名
 * @param author 作者名
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @return 提交日志列表
 */
public List<CommitLog> getCommitLogs(String branch, String author,
                                     LocalDate startDate, LocalDate endDate)
        throws GitException;
```

**实现细节**：
使用 `git log` 命令，格式化输出：
```
git log --author=<author> --since=<date> --before=<date> \
  --pretty=format:"%H|%an|%ai|%s" --no-merges
```
然后解析输出，构建 `CommitLog` 对象列表。

### 3. UI 层设计

#### 3.1 组件选择：新增独立工具 vs 集成到现有工具

**方案A：新增独立工具** `GitCommitReportToolViewImpl`（推荐）
- 优点：功能独立，职责清晰，便于扩展
- 缺点：工具列表可能增多

**方案B：集成到现有工具**
- 优点：集中在一个工具中
- 缺点：UI 可能过于复杂

**选择**：**方案A** - 新增独立工具，与 Git 统计工具并行存在

#### 3.2 UI 布局

**快捷时间范围选择时的布局：**
```
┌─────────────────────────────────────────────┐
│ 🔧 Git 提交周报                              │
├─────────────────────────────────────────────┤
│ 时间范围: [最近7天▼]                         │
│ 作者: [选择作者▼]                           │
│ 格式: [Markdown▼]                           │
│ [生成报告] [复制] [导出文件] [清空]           │
│ ────────────────────────────────────────────│
│ 报告内容（富文本或可滚动面板）:                │
│                                             │
│ # Git 提交周报 (2025-01-15)                 │
│ **作者:** ingerchao                         │
│ **时间范围:** 2025-01-08 ~ 2025-01-15       │
│                                             │
│ ## 仓库: project-a                           │
│ - abc1234 - 2025-01-15 : 新增功能 X         │
│ - def5678 - 2025-01-14 : 修复 bug           │
│                                             │
│ ## 仓库: project-b                           │
│ - ghi9012 - 2025-01-13 : 重构模块 Y         │
│                                             │
│ **统计总结:**                               │
│ - 涉及仓库: 2                                │
│ - 总提交数: 3                                │
│                                             │
└─────────────────────────────────────────────┘
```

**自定义日期范围选择时的布局：**
```
┌─────────────────────────────────────────────┐
│ 🔧 Git 提交周报                              │
├─────────────────────────────────────────────┤
│ 时间范围: [自定义日期▼]                      │
│ 起始日期: [2025-01-01] 📅                   │
│ 终止日期: [2025-01-15] 📅                   │
│ 作者: [选择作者▼]                           │
│ 格式: [Markdown▼]                           │
│ [生成报告] [复制] [导出文件] [清空]           │
│ ────────────────────────────────────────────│
│ 报告内容（富文本或可滚动面板）:                │
│                                             │
│ ...                                         │
│                                             │
└─────────────────────────────────────────────┘
```

**UI 设计要点：**
1. **时间范围选择器（ComboBox）**：下拉框，选项包括 "最近7天"、"最近30天"、"最近1年"、"全部"、"自定义日期"
2. **日期输入框（可编辑文本框）**：仅在选择 "自定义日期" 时显示
   - 文本框格式：`YYYY-MM-DD`
   - 支持手动输入或点击日期选择器按钮（📅）打开日期选择弹窗
3. **日期选择器按钮**：点击后打开系统日期选择弹窗
4. **作者选择**：ComboBox，动态加载所有作者
5. **格式选择**：ComboBox，支持 Markdown、纯文本等
6. **操作按钮**：生成、复制、导出、清空


#### 3.3 工作流

**时间范围选择逻辑：**
```
用户选择时间范围 ComboBox
    ↓
选择快捷选项（最近7天、最近30天等）
    ↓
系统自动计算日期范围
隐藏日期输入框
    ↓
或者：选择 "自定义日期"
    ↓
系统显示起始和终止日期输入框
用户手动输入日期（格式：YYYY-MM-DD）
或点击日期选择器按钮打开日期弹窗
    ↓
系统验证日期格式和有效性
```

**报告生成工作流：**
```
用户界面
    ↓
[选择时间范围（快捷或自定义）、作者、格式]
    ↓
[点击"生成报告"]
    ↓
系统验证参数
  ├─ 检查日期格式是否有效
  ├─ 检查起始日期 <= 终止日期
  └─ 检查作者是否选定
    ↓
后台线程执行：
  ├─ 扫描仓库列表
  ├─ 对每个仓库执行 git log（可并行）
  ├─ 解析结果，构建 CommitLog 对象
  └─ 使用 GitCommitReporter 生成报告
    ↓
更新 UI 显示结果
    ↓
用户可选择：
  ├─ 复制报告内容到剪贴板
  ├─ 导出为文件
  └─ 清空结果
```

#### 3.4 日期处理实现细节

**日期解析和计算：**
```java
// 快捷选项的日期计算
LocalDate endDate = LocalDate.now();
LocalDate startDate;
switch (timeRangeOption) {
    case "最近7天":
        startDate = endDate.minusDays(7);
        break;
    case "最近30天":
        startDate = endDate.minusDays(30);
        break;
    case "最近1年":
        startDate = endDate.minusYears(1);
        break;
    case "全部":
        startDate = null;  // 无时间限制
        break;
    case "自定义日期":
        // 从输入框解析日期
        startDate = LocalDate.parse(startDateInput, DateTimeFormatter.ISO_LOCAL_DATE);
        endDate = LocalDate.parse(endDateInput, DateTimeFormatter.ISO_LOCAL_DATE);
        break;
}
```

**日期验证：**
```java
// 验证日期格式
try {
    LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
    // 格式有效
} catch (DateTimeParseException e) {
    // 显示错误提示
    PluginNotifyKit.error(project, "请输入有效的日期格式 (YYYY-MM-DD)");
}

// 检查日期范围
if (startDate.isAfter(endDate)) {
    PluginNotifyKit.warn(project, "起始日期不能晚于终止日期");
}
```

### 4. 处理流程

#### 4.1 获取所有作者列表
在工具初始化时，从所有仓库中收集所有唯一的作者名：
```java
Set<String> allAuthors = new HashSet<>();
for (GitRepository repo : repositories) {
    executor.setWorkingDirectory(repo.getPath());
    List<String> authors = executor.getAuthors("HEAD", null, null);
    allAuthors.addAll(authors);
}
```

#### 4.2 生成报告的核心流程
```
1. 用户选择 [作者:Alice] [时间:最近7天] [格式:Markdown]
2. 获取时间范围：startDate, endDate
3. 初始化 CommitReport 对象
4. 遍历每个仓库：
   a. 调用 executor.getCommitLogs(branch, author, startDate, endDate)
   b. 将结果封装为 RepositoryCommits
   c. 添加到 CommitReport
5. 计算统计信息
6. 使用 GitCommitReporter.exportAsMarkdown(report) 生成文本
7. 在 UI 中展示结果
```

### 5. 性能优化

#### 5.1 并行处理
使用 `ExecutorService` 并发查询多个仓库的提交信息：
```java
ExecutorService executor = Executors.newFixedThreadPool(4);
List<Future<RepositoryCommits>> futures = new ArrayList<>();
for (GitRepository repo : repositories) {
    futures.add(executor.submit(() -> fetchRepositoryCommits(repo, ...)));
}
```

#### 5.2 缓存
缓存作者列表，避免重复扫描：
```java
private Map<GitRepository, List<String>> authorCache = new ConcurrentHashMap<>();
```

#### 5.3 进度反馈
使用 `SwingWorker` 或后台线程，定期更新状态标签：
```
"正在处理... (3/10 仓库)"
```

### 6. 错误处理和恢复

#### 6.1 异常处理
- **仓库无法访问**：记录警告，继续处理其他仓库
- **Git 命令失败**：显示错误消息，但不中断报告生成
- **特殊字符处理**：使用 JSON 转义或正则替换

#### 6.2 边界情况
- **无提交**：显示"该作者在选定时间范围内无提交"
- **作者不存在**：提示用户"作者不存在，请重新选择"
- **无仓库**：提示"未发现 Git 仓库"

### 7. 导出功能

#### 7.1 Markdown 格式（默认）
```markdown
# Git 提交周报 (2025-01-15)

**作者:** ingerchao
**时间范围:** 2025-01-08 ~ 2025-01-15

## 仓库: project-a

- `abc1234` - 2025-01-15 : 新增功能 X
- `def5678` - 2025-01-14 : 修复 bug

## 仓库: project-b

- `ghi9012` - 2025-01-13 : 重构模块 Y

---

**统计总结:**
- 涉及仓库: 2
- 有提交的仓库: 2
- 总提交数: 3
```

#### 7.2 纯文本格式
```
Git 提交周报 (2025-01-15)

作者: ingerchao
时间范围: 2025-01-08 ~ 2025-01-15

仓库: project-a
- abc1234 - 2025-01-15 : 新增功能 X
- def5678 - 2025-01-14 : 修复 bug

仓库: project-b
- ghi9012 - 2025-01-13 : 重构模块 Y

---
统计总结:
涉及仓库: 2
有提交的仓库: 2
总提交数: 3
```

### 8. 测试策略

#### 8.1 单元测试
- `GitCommitReporterTest`：测试报告生成、导出功能
- `CommitLogTest`：测试数据模型的正确性
- `GitCommandExecutor` 扩展方法的测试

#### 8.2 集成测试
- UI 测试：参数选择、报告生成、内容显示
- 多仓库测试：确保多个仓库的提交被正确聚合

### 9. 兼容性与依赖

**复用现有类**：
- `GitRepositoryFinder`：扫描仓库
- `GitCommandExecutor`：执行 Git 命令
- `GitRepository`：仓库信息模型
- `PluginNotifyKit`：用户提示

**新增依赖**：无（仅使用现有依赖库）

**Java 版本**：兼容 JDK 1.8.0
