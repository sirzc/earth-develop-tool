# 提案：支持项目内多 Git 仓库的管理

## 摘要

当前 Git 统计工具仅支持单一 Git 仓库（项目根目录），无法处理包含多个 Git 子仓库或独立 Git 模块的项目。本提案旨在增强 Git 统计工具的功能，支持用户在多 Git 仓库项目中：
1. 自动扫描并列出项目内所有 Git 仓库
2. 允许用户选择要统计的 Git 仓库
3. 在仓库选择后，为该仓库加载并显示分支列表，默认展示当前 HEAD 分支

## 问题陈述

### 当前限制

1. **单仓库假设**：`GitCommandExecutor` 在初始化时接收一个固定的 `projectRoot` 路径，仅支持该路径下的 Git 操作
2. **无多仓库支持**：项目中包含子模块或多个独立 Git 仓库时，工具无法识别和切换
3. **用户体验差**：用户需要手动指定或无法统计除项目根目录外的其他仓库

### 适用场景

- **单体仓库包含子模块**：如 `project-root/.git` 和 `project-root/submodule/.git`
- **Monorepo 结构**：多个独立的 npm/Java 包各有自己的 Git 仓库
- **依赖组织**：第三方库或本地库有各自的 Git 历史

## 设计概述

### 核心变更

#### 1. 仓库发现机制（新增）
- 添加 `GitRepositoryFinder` 工具类
- 递归扫描项目目录，找出所有 `.git` 目录（文件或文件夹）
- 返回相对于项目根目录的仓库列表

#### 2. UI 层增强（修改）
- 在 `GitStatisticsToolViewImpl` 中添加"仓库选择"下拉框
- 工具初始化时自动扫描并加载仓库列表
- 用户选择仓库后，刷新分支列表
- 分支选择框默认展示当前 HEAD 分支

#### 3. 执行层改造（修改）
- 修改 `GitCommandExecutor` 的初始化，支持动态切换工作目录
- 添加方法 `setWorkingDirectory(File dir)` 以更换 Git 仓库

### 技术考量

**扫描范围限制**：为避免扫描整个系统或不必要的深层目录（如 `node_modules`, `target`, `.gradle` 等），应设置：
- 最大扫描深度（建议 3-5 层）
- 排除列表（常见的依赖/构建目录）

**性能优化**：
- 扫描在后台线程执行，避免 IDE 卡顿
- 缓存扫描结果，用户点击刷新时重新扫描

**错误处理**：
- 处理不可访问的目录
- 跳过无效的 `.git` 目录

## 影响范围

### 修改的文件
1. `src/main/java/com/myth/earth/develop/service/git/GitCommandExecutor.java` - 添加动态工作目录支持
2. `src/main/java/com/myth/earth/develop/ui/toolkit/views/GitStatisticsToolViewImpl.java` - 添加仓库和分支选择逻辑

### 新增文件
1. `src/main/java/com/myth/earth/develop/service/git/GitRepositoryFinder.java` - 仓库扫描工具
2. `src/main/java/com/myth/earth/develop/service/git/GitRepository.java` - 仓库数据模型
3. `src/test/java/com/myth/earth/develop/service/git/GitRepositoryFinderTest.java` - 单元测试

### 无需修改
- 现有的 `GitStatistics`, `GitStatisticsParser`, `GitException` 等类保持不变

## 验收标准

1. **仓库发现**
   - 能正确识别项目内所有 `.git` 目录
   - 正确处理嵌套结构和排除不必要的目录

2. **UI 交互**
   - 仓库选择下拉框正常显示所有仓库
   - 选择仓库后，分支列表能正确加载该仓库的分支
   - 分支选择框默认显示当前 HEAD 分支

3. **功能完整性**
   - 统计功能在切换仓库后继续正常工作
   - 错误处理妥当（无有效 Git 仓库、权限不足等）

4. **性能**
   - 扫描不阻塞 UI 线程
   - 扫描耗时在可接受范围内（< 1 秒）

## 后续计划

- 支持配置扫描深度和排除列表
- 记住用户最后选择的仓库
- 显示仓库的相对路径或绝对路径
