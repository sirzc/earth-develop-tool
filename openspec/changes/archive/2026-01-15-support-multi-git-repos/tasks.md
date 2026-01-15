# 任务列表：支持项目内多 Git 仓库

## 开发阶段

### 基础设施构建

#### Task 1: 创建 GitRepository 数据模型
- **描述**：实现仓库数据模型类
- **验证**：能成功创建 GitRepository 对象，正确存储仓库信息
- **文件**：`src/main/java/com/myth/earth/develop/service/git/GitRepository.java`
- **优先级**：高

#### Task 2: 创建 GitRepositoryFinder 仓库扫描工具
- **描述**：实现自动扫描项目内所有 Git 仓库的功能
- **验证**：
  - 能正确识别嵌套仓库
  - 正确排除不必要的目录
  - 遵守最大扫描深度限制
  - 扫描性能达到标准（< 500ms）
- **文件**：`src/main/java/com/myth/earth/develop/service/git/GitRepositoryFinder.java`
- **依赖**：Task 1
- **优先级**：高

#### Task 3: 增强 GitCommandExecutor 支持多仓库
- **描述**：
  1. 添加 `setWorkingDirectory()` 方法，支持动态切换工作目录
  2. 添加 `getCurrentBranch()` 方法，获取当前 HEAD 分支
  3. 添加 `isValidGitRepository()` 验证方法
- **验证**：
  - 能成功切换工作目录
  - 切换到无效仓库时抛出异常
  - 能正确获取当前分支名称
- **文件**：`src/main/java/com/myth/earth/develop/service/git/GitCommandExecutor.java`
- **优先级**：高
- **注意**：需保持现有 API 兼容性

### UI 层实现

#### Task 4: 在 GitStatisticsToolViewImpl 中添加仓库选择组件
- **描述**：
  1. 添加 `repositoryBox` ComboBox 组件
  2. 在 UI 布局中正确放置（分支选择框上方）
  3. 添加仓库选择变更监听器
- **验证**：
  - 组件能正确显示和交互
  - 选择变更事件能被正确处理
- **文件**：修改 `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitStatisticsToolViewImpl.java`
- **依赖**：Task 2, 3
- **优先级**：高

#### Task 5: 实现工具初始化时的仓库扫描
- **描述**：
  1. 在 `initializeUI()` 或构造函数中调用 `GitRepositoryFinder`
  2. 后台线程执行扫描，避免 UI 冻结
  3. 扫描完成后填充 `repositoryBox`
  4. 自动选择主仓库或第一个仓库
- **验证**：
  - 仓库列表正确加载
  - UI 不冻结
  - 默认选择正确
- **文件**：修改 `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitStatisticsToolViewImpl.java`
- **依赖**：Task 4
- **优先级**：高

#### Task 6: 实现仓库切换时的分支和作者刷新
- **描述**：
  1. 监听 `repositoryBox` 选择变更事件
  2. 调用 `executor.setWorkingDirectory()` 切换仓库
  3. 加载新仓库的分支列表
  4. 获取新仓库的当前 HEAD 分支并设为默认选项
  5. 加载新仓库的作者列表
  6. 清空统计结果表格
  7. 更新状态标签
- **验证**：
  - 仓库切换时所有关联数据正确更新
  - 分支默认选择为 HEAD
  - 无数据混淆
- **文件**：修改 `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitStatisticsToolViewImpl.java`
- **依赖**：Task 3, 5
- **优先级**：高

#### Task 7: 优化分支选择框的默认分支显示
- **描述**：
  1. 调用 `executor.getCurrentBranch()` 获取 HEAD 分支
  2. 在加载分支列表后，自动将 HEAD 分支设为选中项
  3. 在分支名称旁添加 "(当前分支)" 标签（可选）
- **验证**：
  - 分支默认选择正确
  - 标签显示清晰（如果实现）
- **文件**：修改 `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitStatisticsToolViewImpl.java`
- **依赖**：Task 6
- **优先级**：中

#### Task 8: 实现错误处理和用户提示
- **描述**：
  1. 无有效 Git 仓库时，显示提示并禁用相关控件
  2. 仓库切换失败时，显示错误提示并保持当前仓库
  3. 后台操作异常时，捕获并向用户报告
- **验证**：
  - 各种错误场景都有适当的提示
  - UI 状态在错误后正确恢复
- **文件**：修改 `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitStatisticsToolViewImpl.java`
- **依赖**：Task 5, 6, 7
- **优先级**：中

### 测试实现

#### Task 9: 编写 GitRepositoryFinder 的单元测试
- **描述**：
  1. 测试仓库扫描的准确性
  2. 测试目录排除列表的有效性
  3. 测试扫描深度限制
  4. 测试性能指标
- **验证**：所有测试通过，覆盖率 > 80%
- **文件**：`src/test/java/com/myth/earth/develop/service/git/GitRepositoryFinderTest.java`
- **依赖**：Task 2
- **优先级**：高

#### Task 10: 编写 GitCommandExecutor 新方法的单元测试
- **描述**：
  1. 测试 `setWorkingDirectory()` 的正确性和错误处理
  2. 测试 `getCurrentBranch()` 的返回值
  3. 测试 `isValidGitRepository()` 的验证逻辑
- **验证**：所有测试通过
- **文件**：修改 `src/test/java/com/myth/earth/develop/service/git/...` (现有或新增)
- **依赖**：Task 3
- **优先级**：高

#### Task 11: 编写 GitStatisticsToolViewImpl 的集成测试
- **描述**：
  1. 测试仓库扫描和列表加载
  2. 测试仓库选择和切换流程
  3. 测试分支默认选择
  4. 测试多仓库统计功能
- **验证**：所有测试通过
- **文件**：`src/test/java/com/myth/earth/develop/ui/toolkit/views/GitStatisticsToolViewImplTest.java` (新增)
- **依赖**：Task 4-8
- **优先级**：中

### 构建和验证

#### Task 12: 构建项目并运行所有测试
- **描述**：
  1. 执行 `./gradlew clean build`
  2. 运行全部单元测试
  3. 检查代码覆盖率
- **验证**：
  - 编译无错误
  - 所有测试通过
  - 无新的 Lint 警告
- **优先级**：高

#### Task 13: 手动测试和 IDE 沙箱验证
- **描述**：
  1. 在 IDE 沙箱环境中运行插件
  2. 创建测试项目（含多个 Git 仓库）
  3. 验证所有功能正常工作
- **验证**：
  - 仓库列表正确显示
  - 仓库切换流畅
  - 统计功能正确
  - 无 UI 卡顿
- **优先级**：高

#### Task 14: 文档更新（可选）
- **描述**：
  1. 更新 README 或使用说明，介绍新的多仓库功能
  2. 记录已知限制和性能特性
- **文件**：`README.md`, CLAUDE.md 等
- **优先级**：低

## 依赖关系图

```
Task 1 (GitRepository 数据模型)
    ↓
Task 2 (GitRepositoryFinder)
    ↓
Task 3 (GitCommandExecutor 增强)
    ↓
Task 4 (仓库选择组件)
    ↓ + Task 3
Task 5 (初始化时扫描) → Task 9 (单元测试)
    ↓
Task 6 (仓库切换刷新) → Task 10 (单元测试)
    ↓
Task 7 (分支默认选择)
    ↓
Task 8 (错误处理)
    ↓ + Task 11 (集成测试)
Task 12 (构建和测试)
    ↓
Task 13 (手动测试)
    ↓
Task 14 (文档更新)
```

## 工作量估计（相对点数）

| Task | 点数 | 说明 |
|------|------|------|
| 1    | 2    | 简单数据模型 |
| 2    | 5    | 递归扫描、排除列表、性能优化 |
| 3    | 3    | 方法添加、验证逻辑 |
| 4    | 2    | UI 组件添加 |
| 5    | 4    | 后台线程、事件处理 |
| 6    | 5    | 切换逻辑、数据刷新、同步 |
| 7    | 2    | 调用现有方法、UI 更新 |
| 8    | 3    | 错误处理、提示信息 |
| 9    | 4    | 多个测试场景 |
| 10   | 3    | 验证逻辑测试 |
| 11   | 5    | 集成测试复杂度高 |
| 12   | 2    | 构建和测试运行 |
| 13   | 4    | 手动测试和调试 |
| 14   | 2    | 文档更新 |
| **总计** | **46** | |

