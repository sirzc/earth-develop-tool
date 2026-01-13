# Change: 增强进制转换工具的结果显示格式化

## Why
用户在进行数值进制转换时，特别是转换为二进制的较长数字，需要按照标准的分组方式（如 4 位一组）显示结果，这样便于阅读和理解。例如，将 123 从十进制转换为二进制时，按 int 类型（32 位）补零后的结果为 `0000 0000 0000 0000 0000 0000 0111 1011`，这种格式化显示提升了用户体验。

## What Changes
- 实现格式化逻辑：转换结果根据目标进制进行分组显示：
  - 二进制：每 4 位用空格分隔，并按数据类型位宽补零
  - 八进制：每 3 位用空格分隔，并按数据类型位宽补零
  - 十进制：每 3 位用空格分隔（千位符），不进行位宽补零
  - 十六进制：每 2 位用空格分隔，并按数据类型位宽补零
- 补零处理：对于正整数转换到非二进制进制时，自动补零至数据类型对应的位宽
- 支持多行格式化：每行独立进行格式化处理
- 所有结果自动格式化显示（不需要用户手动启用）

## Impact
- **Affected specs**: `number-conversion` capability
- **Affected code**: `ui/toolkit/views/RadixConversionToolViewImpl.java`、`utils/ResultFormatterHelper.java`、`utils/PaddingHelper.java`
- **User impact**: 提升可读性，使转换结果更易于理解和阅读
- **Breaking changes**: 无（纯功能增强，所有结果自动格式化）
