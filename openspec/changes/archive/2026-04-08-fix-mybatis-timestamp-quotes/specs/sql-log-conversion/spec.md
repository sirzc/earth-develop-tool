# SQL 日志转换规范

## ADDED Requirements

### Requirement: MyBatis 日志转换为可执行 SQL

系统 **SHALL** 能够解析 MyBatis 调试日志（包含 "==> Preparing:" 和 "==> Parameters:" 信息）并生成可直接执行的 SQL 语句。

#### Scenario: 解析基本 SQL 和参数

- **WHEN** 输入包含 MyBatis Preparing 和 Parameters 日志
- **THEN** 系统生成包含参数替换的完整 SQL 语句
- **EXAMPLE**:
  ```
  输入：
  ==> Preparing: SELECT * FROM user WHERE id = ?
  ==> Parameters: 123(Integer)

  输出：
  -- Generated SQL:
  SELECT * FROM user WHERE id = 123;
  ```

#### Scenario: 处理字符串参数的引号

- **WHEN** 参数类型为 String（标记为 (String)）
- **THEN** 参数值用单引号包围，内部单引号需转义
- **EXAMPLE**:
  ```
  输入：Parameters: 'John'(String), 'O''Brien'(String)
  输出: 'John', 'O''Brien'
  ```

### Requirement: 时间戳参数引号处理

系统 **MUST** 为 Timestamp 类型的参数添加单引号包围，以符合 SQL 标准语法。

#### Scenario: Timestamp 参数添加引号

- **WHEN** 参数类型为 Timestamp（标记为 (Timestamp)）
- **THEN** 参数值用单引号包围
- **EXAMPLE**:
  ```
  输入：Parameters: 2026-04-08 17:14:16.282(Timestamp)
  输出: '2026-04-08 17:14:16.282'
  ```

#### Scenario: 混合 String 和 Timestamp 参数

- **WHEN** 同一条 SQL 包含 String 和 Timestamp 类型的混合参数
- **THEN** 两类参数都应被正确引号包围
- **EXAMPLE**:
  ```
  输入：
  ==> Preparing: SELECT COUNT(*) FROM table
    WHERE text_grp IN (?, ?, ?, ?) AND update_timestamp >= ?
  ==> Parameters: t(String), e(String), s(String), t(String),
    2026-04-08 17:14:16.282(Timestamp)

  输出：
  -- Generated SQL:
  SELECT COUNT(*) FROM table
  WHERE text_grp IN ('t', 'e', 's', 't') AND update_timestamp >= '2026-04-08 17:14:16.282';
  ```

#### Scenario: 多个 Timestamp 参数

- **WHEN** SQL 中包含多个 Timestamp 类型的参数
- **THEN** 每个 Timestamp 参数都被独立引号包围
- **EXAMPLE**:
  ```
  输入：Parameters: 2026-01-01 00:00:00(Timestamp), 2026-12-31 23:59:59(Timestamp)
  输出: '2026-01-01 00:00:00', '2026-12-31 23:59:59'
  ```

### Requirement: 非字符类型参数保持原样

系统 **SHALL** 保持 Integer、Long、Double 等非字符类型参数原样，不添加引号。

#### Scenario: 整数参数不添加引号

- **WHEN** 参数类型为 Integer、Long 等数值类型
- **THEN** 参数值不被引号包围
- **EXAMPLE**:
  ```
  输入：Parameters: 123(Integer), 456(Long)
  输出: 123, 456
  ```

## MODIFIED Requirements

（无现有需求修改）

## REMOVED Requirements

（无需删除的需求）
