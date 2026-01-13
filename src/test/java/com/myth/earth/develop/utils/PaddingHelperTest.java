package com.myth.earth.develop.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaddingHelperTest {

    @Test
    void testPadBinaryByte() {
        String result = PaddingHelper.padBinary("11", PaddingHelper.DataType.BYTE);
        assertEquals("00001011", result);
    }

    @Test
    void testPadBinaryInt() {
        String result = PaddingHelper.padBinary("11", PaddingHelper.DataType.INT);
        assertEquals("00000000000000000000000000001011", result);
    }

    @Test
    void testPadBinaryLong() {
        String result = PaddingHelper.padBinary("11", PaddingHelper.DataType.LONG);
        assertEquals("0000000000000000000000000000000000000000000000000000000000001011", result);
    }

    @Test
    void testPadBinaryWithLeadingZeros() {
        String result = PaddingHelper.padBinary("0011", PaddingHelper.DataType.BYTE);
        assertEquals("00001011", result);
    }

    @Test
    void testPadBinarySingleBit() {
        String result = PaddingHelper.padBinary("1", PaddingHelper.DataType.BYTE);
        assertEquals("00000001", result);
    }

    @Test
    void testPadBinaryAllZeros() {
        String result = PaddingHelper.padBinary("0", PaddingHelper.DataType.BYTE);
        assertEquals("00000000", result);
    }

    @Test
    void testPadBinaryAllOnes() {
        String result = PaddingHelper.padBinary("11111111", PaddingHelper.DataType.BYTE);
        assertEquals("11111111", result);
    }

    @Test
    void testPadBinaryExceedsWidth() {
        assertThrows(IllegalArgumentException.class, () -> {
            PaddingHelper.padBinary("100000000", PaddingHelper.DataType.BYTE);
        });
    }

    @Test
    void testPadBinaryEmptyString() {
        String result = PaddingHelper.padBinary("", PaddingHelper.DataType.BYTE);
        assertEquals("", result);
    }

    @Test
    void testPadBinaryNull() {
        String result = PaddingHelper.padBinary(null, PaddingHelper.DataType.BYTE);
        assertNull(result);
    }

    @Test
    void testGetBitWidth() {
        assertEquals(8, PaddingHelper.getBitWidth(PaddingHelper.DataType.BYTE));
        assertEquals(32, PaddingHelper.getBitWidth(PaddingHelper.DataType.INT));
        assertEquals(64, PaddingHelper.getBitWidth(PaddingHelper.DataType.LONG));
    }

    @Test
    void testDataTypeFromBitWidth() {
        assertEquals(PaddingHelper.DataType.BYTE, PaddingHelper.DataType.fromBitWidth(8));
        assertEquals(PaddingHelper.DataType.INT, PaddingHelper.DataType.fromBitWidth(32));
        assertEquals(PaddingHelper.DataType.LONG, PaddingHelper.DataType.fromBitWidth(64));
    }

    @Test
    void testDataTypeFromBitWidthDefault() {
        assertEquals(PaddingHelper.DataType.BYTE, PaddingHelper.DataType.fromBitWidth(128));
    }
}
