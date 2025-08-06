package com.harsha.app;

import java.nio.ByteBuffer;

public class BinaryUtils {
    public static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int bytesToInt(byte[] bytes) {
        if (bytes.length != 4) throw new IllegalArgumentException("Expected 4 bytes for int");
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte[] floatToBytes(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static float bytesToFloat(byte[] bytes) {
        if (bytes.length != 4) throw new IllegalArgumentException("Expected 4 bytes for float");
        return ByteBuffer.wrap(bytes).getFloat();
    }

}
