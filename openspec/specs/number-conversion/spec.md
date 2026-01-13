# number-conversion Specification

## Purpose
TBD - created by archiving change enhance-radix-padding. Update Purpose after archive.
## Requirements
### Requirement: 按数据类型自动补零
系统 SHALL 根据用户选定的数据类型（byte、int、long）自动对输入的数值进行高位补零。

#### Scenario: Byte 类型补零
- **WHEN** 用户选择 byte 类型并输入数值 `11` (二进制)
- **THEN** 系统应补零至 8 位，结果为 `00001011`

#### Scenario: Int 类型补零
- **WHEN** 用户选择 int 类型并输入数值 `11` (二进制)
- **THEN** 系统应补零至 32 位，结果为 `00000000000000000000000000001011`

#### Scenario: Long 类型补零
- **WHEN** 用户选择 long 类型并输入数值 `11` (二进制)
- **THEN** 系统应补零至 64 位，结果为 `0000000000000000000000000000000000000000000000000000000000001011`

### Requirement: 补零后进制转换
系统 SHALL 将补零后的所有位数都参与进制转换计算。

#### Scenario: 二进制补零后转十六进制
- **WHEN** 用户选择 byte 类型，输入数值 `11` (二进制)，目标进制为十六进制
- **THEN** 系统应先补零至 `00001011`，再转换为 `0b` (十六进制)，显示结果为 `b` 或 `0b`

#### Scenario: 二进制补零后转十进制
- **WHEN** 用户选择 int 类型，输入数值 `10000000000000000000000000000000` (二进制)，目标进制为十进制
- **THEN** 系统应先补零至 32 位 `00000000000000000000000000000000`，再转换为十进制 `0`

### Requirement: 数据类型选择器
系统 SHALL 在 UI 中提供数据类型选择器，支持三种标准类型。

#### Scenario: 显示类型选项
- **WHEN** 用户打开进制转换工具
- **THEN** 系统应在转换参数区域显示数据类型下拉菜单，包含 byte、int、long 三个选项

#### Scenario: 保持类型选择状态
- **WHEN** 用户选择 long 类型并进行转换
- **THEN** 下拉菜单应保持 long 类型的选择状态，后续转换继续使用该类型

