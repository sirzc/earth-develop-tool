# 实现任务列表

## 1. 框架和核心准备

- [x] 1.1 在 `ToolCategory` 枚举中新增 `GIT("Git工具", null)` 项
- [x] 1.2 创建 `GitStatisticsToolViewImpl.java`，继承 `AbstractToolView`，使用 `@Tool` 注解标记
- [x] 1.3 创建 `GitCommandExecutor.java` 工具类，封装 Git 命令执行逻辑
- [x] 1.4 创建 `GitStatisticsParser.java` 工具类，解析 Git 命令输出

## 2. 后端服务实现

- [x] 2.1 实现 `GitCommandExecutor` 的分支列表获取方法（`getBranches()`）
- [x] 2.2 实现分支作者列表获取方法（`getAuthors(String branch, Date startDate, Date endDate)`）
- [x] 2.3 实现代码行数统计方法（`getStatistics(String branch, Date startDate, Date endDate, List<String> authors)`）
  - [x] 2.3.1 使用 `git log --numstat` 获取每次提交的行数变动
  - [x] 2.3.2 使用 `git rev-list --count` 获取提交次数
  - [x] 2.3.3 使用 `git diff-tree --no-commit-id --name-only` 获取修改文件列表
- [x] 2.4 实现 `GitStatisticsParser` 的数据解析逻辑，输出统计结果对象（包含增加行数、删除行数、提交次数、修改文件数）

## 3. UI 组件实现

- [x] 3.1 创建日期范围选择组件
  - [x] 3.1.1 实现快捷时间选项（最近7天、30天、1年）
  - [x] 3.1.2 实现自定义日期选择器
- [x] 3.2 创建分支选择下拉框，支持加载和刷新分支列表
- [x] 3.3 创建作者多选框，支持加载和刷新作者列表
- [x] 3.4 创建结果表格组件，显示统计数据
  - [x] 3.4.1 实现表格列头（作者、提交次数、增加行数、删除行数、修改文件数）
  - [x] 3.4.2 实现表格数据排序功能
  - [x] 3.4.3 实现表格数据复制功能（纯文本、Markdown、CSV）

## 4. GitStatisticsToolViewImpl 核心逻辑

- [x] 4.1 在 `initializeUI()` 中初始化所有 UI 组件
  - [x] 4.1.1 布局日期范围选择区域
  - [x] 4.1.2 布局分支选择区域
  - [x] 4.1.3 布局作者多选区域
  - [x] 4.1.4 布局统计按钮
  - [x] 4.1.5 布局结果表格区域
- [x] 4.2 实现 `refreshToolData()` 方法，工具显示时自动加载分支和作者列表
- [x] 4.3 实现统计按钮的点击事件处理
  - [x] 4.3.1 参数校验（时间范围、分支、作者有效性）
  - [x] 4.3.2 调用后端服务获取统计数据
  - [x] 4.3.3 异常处理（Git 命令失败、超时、仓库不存在等）
  - [x] 4.3.4 更新结果表格显示
- [x] 4.4 实现分支变更时的作者列表更新

## 5. 测试和验证

- [x] 5.1 编写单元测试，测试 `GitCommandExecutor` 的各个方法
- [x] 5.2 编写单元测试，测试 `GitStatisticsParser` 的数据解析逻辑
- [ ] 5.3 集成测试，验证工具的完整流程
  - [ ] 5.3.1 测试时间范围过滤效果
  - [ ] 5.3.2 测试分支过滤效果
  - [ ] 5.3.3 测试作者多选过滤效果
  - [ ] 5.3.4 测试无结果、错误处理场景
- [ ] 5.4 功能测试，在 IDE 沙箱环境中验证 UI 交互和数据显示

## 6. 构建和部署

- [ ] 6.1 构建插件，确保无编译错误
- [ ] 6.2 手动测试插件在 IntelliJ IDEA 中的运行情况
- [ ] 6.3 更新版本号（build.gradle 中的插件版本）
- [ ] 6.4 生成最终的插件包并上传

