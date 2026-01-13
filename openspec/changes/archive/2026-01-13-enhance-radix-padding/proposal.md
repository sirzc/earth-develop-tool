# Change: 增强进制转换工具的补零功能

## Why
当前的进制转换工具只支持二进制转换时的简单转换，但缺少按照标准数据类型（byte、int、long）补零的功能。用户需要根据不同的数据类型规范（8位、32位、64位）对高位补零，这在处理二进制数据和网络字节序时尤为重要。这个功能可以帮助开发者更准确地理解和转换不同进制的数值。

## What Changes
- 添加数据类型选择器：支持选择 byte（8位）、int（32位）、long（64位）三种标准类型
- 实现自动补零：根据选定的类型自动对高位进行补零
- 保留补零结果：补零后的所有位都参与后续的进制转换计算
- 每行单独应用：输入多行数值时，每行都根据所选类型进行独立的补零和转换

## Impact
- **Affected specs**: 新增 `number-conversion` capability
- **Affected code**: `ui/toolkit/views/RadixConversionToolViewImpl.java`
- **User impact**: 提升用户体验，支持更复杂的数值转换场景
- **Breaking changes**: 无（纯功能增强）
