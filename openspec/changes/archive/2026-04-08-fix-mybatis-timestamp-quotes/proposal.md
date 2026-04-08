# 变更：修复 MyBatis 日志解析中时间戳参数引号问题

## Why

当前 MybatisLogParser 在解析 MyBatis 日志并生成 SQL 时，对于 Timestamp 类型的参数未能正确添加单引号。这导致生成的 SQL 语句在时间戳参数处缺少引号，不符合 SQL 语法规范，该 SQL 无法直接执行。

**现有问题示例**：
```sql
-- 当前输出（错误）
SELECT COUNT(*) FROM table WHERE update_timestamp >= 2026-04-08 17:14:16.282;

-- 期望输出（正确）
SELECT COUNT(*) FROM table WHERE update_timestamp >= '2026-04-08 17:14:16.282';
```

## What Changes

- **修改 MybatisLogParser#buildSqlString()** 方法的参数处理逻辑，扩展类型检测以支持 Timestamp 类型
- 当参数类型为 `Timestamp` 时，自动为其值添加单引号
- 保持对 String 类型和其他含引号参数的现有处理逻辑不变

## Impact

- **受影响的规范**：sql-log-conversion（新增）
- **受影响的代码**：
  - `src/main/java/com/myth/earth/develop/service/logtosql/MybatisLogParser.java`（第 47-66 行）
- **测试覆盖**：
  - 新增测试用例验证 Timestamp 类型参数的引号处理
  - 新增测试用例验证混合参数类型（String + Timestamp）的处理
- **向后兼容性**：完全兼容，仅修复缺陷，不改变现有正确行为
