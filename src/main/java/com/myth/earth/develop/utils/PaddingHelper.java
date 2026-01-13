package com.myth.earth.develop.utils;

import java.math.BigInteger;

/**
 * 二进制补零工具类
 * 根据数据类型规范对二进制数值进行高位补零
 *
 * @author Claude
 * @since 2025/01/13
 */
public class PaddingHelper {

    /**
     * 数据类型枚举
     */
    public enum DataType {
        BYTE(8, "byte"),
        INT(32, "int"),
        LONG(64, "long");

        public final int bitWidth;
        public final String displayName;

        DataType(int bitWidth, String displayName) {
            this.bitWidth = bitWidth;
            this.displayName = displayName;
        }

        public static DataType fromBitWidth(int bitWidth) {
            for (DataType type : DataType.values()) {
                if (type.bitWidth == bitWidth) {
                    return type;
                }
            }
            return BYTE; // 默认返回 byte
        }
    }

    /**
     * 对二进制字符串进行补零
     *
     * @param binaryValue 二进制字符串（不包含前缀）
     * @param dataType 目标数据类型
     * @return 补零后的二进制字符串
     * @throws IllegalArgumentException 当二进制值位数超过目标宽度时
     */
    public static String padBinary(String binaryValue, DataType dataType) {
        if (binaryValue == null || binaryValue.isEmpty()) {
            return binaryValue;
        }

        int targetBits = dataType.bitWidth;

        // 直接使用字符串长度作为实际位数（包括前导零）
        if (binaryValue.length() > targetBits) {
            throw new IllegalArgumentException(
                String.format("二进制值位数 (%d) 超过 %s 类型的宽度 (%d)",
                    binaryValue.length(), dataType.displayName, targetBits)
            );
        }

        // 补零至目标宽度
        return String.format("%0" + targetBits + "d", new BigInteger(binaryValue, 2));
    }

    /**
     * 获取数据类型的位宽
     *
     * @param dataType 数据类型
     * @return 位宽
     */
    public static int getBitWidth(DataType dataType) {
        return dataType.bitWidth;
    }
}
