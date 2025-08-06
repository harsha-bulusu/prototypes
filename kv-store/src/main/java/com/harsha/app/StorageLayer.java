package com.harsha.app;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;


public class StorageLayer {

    private File file = new File("data.db");;
    private static Map<String, Long> keyDir = new HashMap<>();

    public StorageLayer() {
        buildIndex();
    }

    private long computeCRC(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    private boolean isValidCRC(int keyLength, String key, int valueLength, byte[] valueBytes, long crc) throws IOException {
        // Check CRC
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteArrayOutputStream);
        dos.writeInt(keyLength);
        dos.write(key.getBytes());
        dos.writeInt(valueLength);
        dos.write(valueBytes);
        long currentCRC = computeCRC(byteArrayOutputStream.toByteArray());

        if (crc == currentCRC) {
            return true;
        } 
        return false;
    }


    private void buildIndex() {
        try (FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis)
        ) {
            FileChannel channel = fis.getChannel();

            while (true) {
                long position = channel.position();
                try {
                    long crc = dis.readLong();
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

                        if (isValidCRC(keyLength, key, valueLength, valueBytes, crc)) {
                            keyDir.put(key, position);
                        } else {
                            break;
                        }
                    }
                } catch (EOFException exception) {
                    break;
                }
            }

        } catch(FileNotFoundException fileNotFoundException) {
            System.out.println("Data File not found");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public boolean set(String key, byte[] value) {
        int keyLength = key.getBytes().length;
        int valueLength = value.length;

        try (FileOutputStream fos = new FileOutputStream(file, true);
             DataOutputStream dos = new DataOutputStream(fos)
        ) {
            FileChannel channel = fos.getChannel();
            long position = channel.position();
            ByteArrayOutputStream recordBuffer = new ByteArrayOutputStream();
            DataOutputStream tempOutputStream = new DataOutputStream(recordBuffer);

            // Load the record to a temporary buffer
            tempOutputStream.writeInt(keyLength);
            tempOutputStream.write(key.getBytes(StandardCharsets.UTF_8));
            tempOutputStream.writeInt(valueLength);
            tempOutputStream.write(value);
            
            // Compute CRC
            byte[] recordBytes = recordBuffer.toByteArray();
            long crc = computeCRC(recordBytes);

            // Write data to disk
            dos.writeLong(crc);
            dos.write(recordBytes);

            // Flush changes
            fos.getFD().sync();

            // Update keyDir
            keyDir.put(key, position);
            System.out.println(keyDir);
            return true;
        } catch(IOException exception) {}
        return false;
    }

    public byte[] get(String targetKey) {
        if (keyDir.containsKey(targetKey)) {
            Long position = keyDir.get(targetKey);
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                // move to the position
                raf.seek(position);

                // read CRC
                long crc = raf.readLong();

                // read key length
                int keyLength = raf.readInt();

                // read key
                byte[] keyBytes = new byte[keyLength];
                raf.readFully(keyBytes);
                String key = new String(keyBytes);

                // read value length
                int valueLength = raf.readInt();

                // read value
                byte[] valueBytes = new byte[valueLength];
                raf.readFully(valueBytes);

                if (isValidCRC(keyLength, key, valueLength, valueBytes, crc)) {
                    return valueBytes;
                }

            } catch (IOException exception) {}
        }
        return null;
    }

    public boolean del(String targetKey) {
        if (keyDir.containsKey(targetKey)) {
            int keyLength = targetKey.getBytes().length;

            try (FileOutputStream fos = new FileOutputStream(file, true);
                 DataOutputStream dos = new DataOutputStream(fos)
            ) {
                ByteArrayOutputStream recordBuffer = new ByteArrayOutputStream();
                DataOutputStream tempOutputStream = new DataOutputStream(recordBuffer);

                // Load record to temporary buffer
                tempOutputStream.writeInt(keyLength);
                tempOutputStream.write(targetKey.getBytes(StandardCharsets.UTF_8));
                tempOutputStream.writeInt(-1);
                
                // Compute CRC
                byte[] recordBytes = recordBuffer.toByteArray();
                long crc = computeCRC(recordBytes);

                // Write to Disk
                dos.writeLong(crc);
                dos.write(recordBytes);

                // Flush
                fos.getFD().sync();
                keyDir.remove(targetKey);
                System.out.println(keyDir);
                return true;
            } catch (IOException exception) {}
        }

        return false;
    }

}
