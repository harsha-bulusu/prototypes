package com.harsha.snippets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PersistentStore {

    private File file = new File("data.db");;
    private static Map<String, Long> keyDir = new HashMap<>();

    private void buildIndex() {
        try (FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis)
        ) {
            FileChannel channel = fis.getChannel();

            while (true) {
                long position = channel.position();
                try {
                    int keyLength = dis.readInt();
                    byte[] keyBytes = new byte[keyLength];
                    dis.readFully(keyBytes);
                    String key = new String(keyBytes, StandardCharsets.UTF_8);
                    int valueLength = dis.readInt();
                    if (valueLength == - 1) {
                        if (keyDir.containsKey(key)) keyDir.remove(key);
                    } else {
                        byte[] valueBytes = new byte[valueLength];
                        dis.readFully(valueBytes);
                        keyDir.put(key, position);
                    }
                } catch (EOFException exception) {
                    break;
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String processSetCommand(String key, byte[] value) {
        int keyLength = key.getBytes().length;
        int valueLength = value.length;

        try (FileOutputStream fos = new FileOutputStream(file, true);
             DataOutputStream dos = new DataOutputStream(fos)
        ) {
            FileChannel channel = fos.getChannel();
            long position = channel.position();

            dos.writeInt(keyLength);
            dos.write(key.getBytes(StandardCharsets.UTF_8));
            dos.writeInt(valueLength);
            dos.write(value);

            keyDir.put(key, position);
            System.out.println(keyDir);
            return key +  "=" + value + "\n";
        } catch(IOException exception) {}
        return null;
    }

    private String processGetCommand(String targetKey) {
        if (keyDir.containsKey(targetKey)) {
            Long position = keyDir.get(targetKey);
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                // move to the position
                raf.seek(position);

                // read key length
                int keyLength = raf.readInt();
                System.out.println(keyLength);
                // read key
                byte[] keyBytes = new byte[keyLength];
                raf.readFully(keyBytes);
                String key = new String(keyBytes);
                // read value length
                int valueLength = raf.readInt();
                // read value
                byte[] valueBytes = new byte[valueLength];
                raf.readFully(valueBytes);
                String value = new String(valueBytes);
                return key + "=" + value+  "\n";
            } catch (IOException exception) {}
        }
        return "No Matching Keys found\n";
    }

    private boolean processDelCommand(String targetKey) {
        if (keyDir.containsKey(targetKey)) {
            int keyLength = targetKey.getBytes().length;

            try (FileOutputStream fos = new FileOutputStream(file, true);
                 DataOutputStream dos = new DataOutputStream(fos)
            ) {
                dos.writeInt(keyLength);
                dos.write(targetKey.getBytes(StandardCharsets.UTF_8));
                dos.writeInt(-1);

                keyDir.remove(targetKey);
                System.out.println(keyDir);
                return true;
            } catch (IOException exception) {}
        }

        return false;
    }

    public static void main(String[] args) throws IOException {
        PersistentStore persistentStore = new PersistentStore();
        persistentStore.buildIndex();
    }
}
