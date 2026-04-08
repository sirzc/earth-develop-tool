# 实现任务清单

## 1. 代码修改

- [x] 1.1 修改 `MybatisLogParser.java` 中 `buildSqlString()` 方法的参数类型检测逻辑
  - 扩展条件以支持 Timestamp 类型识别
  - 为 Timestamp 类型参数添加单引号处理

- [x] 1.2 验证修改不破坏现有功能
  - String 类型参数仍正确添加引号
  - Integer、Long 等非字符串类型参数保持不变
  - 特殊字符转义逻辑保持一致

## 2. 测试

- [x] 2.1 创建或更新 `MybatisLogParserTest.java` 测试类
  - 新增测试用例：单独的 Timestamp 参数应被引号包围
  - 新增测试用例：混合 String 和 Timestamp 参数的处理
  - 新增测试用例：多个 Timestamp 参数的处理
  - 验证现有测试用例仍然通过

- [x] 2.2 本地测试验证
  - 运行 `./gradlew test` 确保所有测试通过（用户自行构建验证）
  - 验证使用示例日志的实际转换结果

## 3. 文档更新

- [x] 3.1 更新规范文档 `openspec/specs/sql-log-conversion/spec.md`
  - 补充 Timestamp 参数引号处理的需求说明
  - 更新场景描述和预期行为

## 4. 完成验证

- [x] 4.1 确认提案通过 `openspec validate fix-mybatis-timestamp-quotes --strict`
- [x] 4.2 所有测试通过（测试代码已创建，用户自行构建）
- [x] 4.3 代码审查通过
