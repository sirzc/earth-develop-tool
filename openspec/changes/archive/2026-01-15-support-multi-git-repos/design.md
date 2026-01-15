# 设计文档：支持项目内多 Git 仓库

## 架构设计

### 1. 仓库扫描模块（新增）

**类名**：`GitRepositoryFinder`

**职责**：
- 递归扫描项目目录结构
- 识别所有 `.git` 目录（子模块、嵌套仓库等）
- 返回相对路径和绝对路径的仓库列表

**关键方法**：
```java
/**
 * 扫描项目内所有 Git 仓库
 * @param projectRoot 项目根目录
 * @param maxDepth 最大扫描深度（默认 3）
 * @return Git 仓库列表
 */
List<GitRepository> findRepositories(File projectRoot, int maxDepth);

/**
 * 获取当前目录的 HEAD 分支名称
 * @param repoPath 仓库路径
 * @return 分支名称（如 "main"、"master"）或空
 */
String getCurrentBranch(File repoPath);
```

**数据模型**：
```java
public class GitRepository {
    private String name;                  // 仓库名（相对路径或目录名）
    private File path;                    // 绝对路径
    private String currentBranch;         // 当前 HEAD 分支
    private boolean isMainRepository;     // 是否为项目主仓库

    // getters/setters...
}
```

### 2. 命令执行器改造（修改）

**类名**：`GitCommandExecutor`

**现状分析**：
- 当前通过构造函数传入 `projectRoot`，并在所有命令中使用 `ProcessBuilder.directory(projectRoot)`
- 所有 Git 命令都在此目录下执行

**改造方案**：
```java
public class GitCommandExecutor {
    private File currentWorkingDir;  // 可变的工作目录

    public GitCommandExecutor(File projectRoot) {
        this.currentWorkingDir = projectRoot;
    }

    /**
     * 切换工作目录（Git 仓库）
     */
    public void setWorkingDirectory(File dir) throws GitException {
        if (!isValidGitRepository(dir)) {
            throw new GitException("不是有效的 Git 仓库: " + dir.getAbsolutePath());
        }
        this.currentWorkingDir = dir;
    }

    /**
     * 获取当前工作目录
     */
    public File getWorkingDirectory() {
        return currentWorkingDir;
    }

    /**
     * 验证目录是否为有效的 Git 仓库
     */
    private boolean isValidGitRepository(File dir) throws GitException {
        // 执行 git rev-parse --git-dir 命令验证
        try {
            executeGitCommand("git", "rev-parse", "--git-dir");
            return true;
        } catch (GitException e) {
            return false;
        }
    }
}
```

### 3. UI 层设计（修改）

**在 `GitStatisticsToolViewImpl` 中的变更**：

#### 新增组件
```java
private ComboBox<GitRepository> repositoryBox;  // 仓库选择下拉框
```

#### 初始化流程
```
工具初始化
  ↓
后台扫描项目内所有 Git 仓库
  ↓
填充 repositoryBox
  ↓
自动选择主仓库或第一个仓库
  ↓
加载该仓库的分支列表（默认展示 HEAD）
  ↓
加载该仓库的作者列表
  ↓
UI 就绪
```

#### 仓库切换处理
```
用户选择不同仓库
  ↓
调用 executor.setWorkingDirectory(newRepo)
  ↓
刷新分支列表（带默认 HEAD）
  ↓
刷新作者列表
  ↓
清空统计结果表格
  ↓
提示用户仓库已切换
```

#### 分支默认选择
- 获取 `getCurrentBranch(repoPath)` 返回的当前分支
- 在分支下拉框中将该分支设为默认选中项
- 如果无法获取当前分支，则选择列表中的第一个分支

### 4. 扫描策略

#### 排除列表
默认排除以下常见目录（避免不必要的扫描）：
- `node_modules`, `bower_components` - JavaScript 依赖
- `target`, `build`, `dist` - 构建输出
- `.gradle`, `.maven` - 构建缓存
- `venv`, `.venv`, `env` - Python 虚拟环境
- `.idea`, `.vscode` - IDE 配置
- `.git` 本身（只识别它的存在，不递归进入）

#### 扫描深度控制
- 默认最大深度：3 层
- 可通过 `GitRepositoryFinder.findRepositories(root, depth)` 参数调整

#### 性能考虑
```java
// 并行搜索：使用 ExecutorService 加速扫描
ExecutorService executor = Executors.newFixedThreadPool(4);
// 使用 ForkJoinPool 进行递归搜索
```

### 5. 错误处理

#### 异常场景
1. **无有效 Git 仓库**
   - 扫描结果为空时，展示提示信息
   - 禁用分支、作者、统计按钮

2. **权限不足**
   - 捕获 `IOException`，跳过不可访问的目录
   - 记录日志但不中断扫描

3. **工作目录切换失败**
   - 验证新目录是有效 Git 仓库后再切换
   - 失败时显示错误提示，保留原工作目录

### 6. 后台线程管理

```java
// 扫描在后台线程执行，完成后更新 UI
SwingUtilities.invokeLater(() -> {
    // 更新 repositoryBox
    // 更新 branchBox
    // 更新 authorList
});
```

## 类关系图

```
GitRepositoryFinder
    ↓
    └── GitRepository (数据模型)

GitCommandExecutor (改造)
    ↓ 使用 setWorkingDirectory()

GitStatisticsToolViewImpl (改造)
    ├── repositoryBox (新增)
    ├── branchBox (增强)
    ├── authorList (不变)
    └── executor.setWorkingDirectory() 联动
```

## 兼容性考虑

1. **向后兼容**：
   - 项目根目录仍然是默认选择的仓库
   - 现有的单仓库项目无需改动，自动识别主仓库

2. **API 稳定性**：
   - `GitCommandExecutor` 的现有公共方法签名不变
   - 新增方法不影响已有功能

## 测试策略

### 单元测试
- `GitRepositoryFinder` 的扫描逻辑：正确识别嵌套仓库、排除不必要目录
- `GitRepository` 数据模型：正确存储和访问仓库信息
- `GitCommandExecutor.setWorkingDirectory()` 的验证逻辑

### 集成测试
- UI 初始化时仓库列表正确加载
- 仓库切换后分支列表正确更新
- 分支默认选择（HEAD）正确展示
- 统计功能在多仓库中正常工作

### 测试项目
需要创建包含多个 Git 仓库的测试项目结构：
```
test-project/
├── .git/              (主仓库)
├── submodule1/.git/   (子模块)
├── libs/lib1/.git/    (嵌套仓库)
└── ...
```

## 性能指标

- 扫描耗时（3 层深度、5 个仓库）：< 500ms
- UI 响应延迟：< 100ms
- 内存占用：< 5MB
