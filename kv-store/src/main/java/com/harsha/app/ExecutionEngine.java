package com.harsha.app;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ExecutionEngine {

    private StorageLayer storageLayer = new StorageLayer();

    /**
     * Process Commands
     */

    public boolean processSetCommand(String key, byte type, String value) {
        return storageLayer.set(key, serializeValue(type, value));
    }

    public boolean processDelCommand(String key) {
        return storageLayer.del(key);
    }

    public String processGetCommand(String key) {
        byte[] rawValue = storageLayer.get(key);
        if (rawValue == null) {
            return "NIL\n";
        }

        byte type = rawValue[0];
        byte[] data = Arrays.copyOfRange(rawValue, 1, rawValue.length);
        
        Object deserialized = deserializeValue(type, data);
        return String.format("%s:%s\n", type, deserialized.toString());
    }

    /**
     * Serializers and Deserializers
     */
    
    public byte[] serializeValue(byte type, String data) {
        byte[] encodedData;

        if (type == 0) {
            int value = Integer.parseInt(data);
            encodedData = BinaryUtils.intToBytes(value);
        } else if (type == 1) {
            float value = Float.parseFloat(data);
            encodedData = BinaryUtils.floatToBytes(value);
        } else {
            encodedData = data.getBytes(StandardCharsets.UTF_8);
        }

        // Prepend the type byte
        byte[] result = new byte[1 + encodedData.length];
        result[0] = type;
        System.arraycopy(encodedData, 0, result, 1, encodedData.length);

        return result;
    }

    public Object deserializeValue(byte type, byte[] data) {
        if (type == 0) {
            return BinaryUtils.bytesToInt(data);
        } else if (type == 1) {
            return BinaryUtils.bytesToFloat(data);
        } else {
            return new String(data);
        }
    }
    
}
