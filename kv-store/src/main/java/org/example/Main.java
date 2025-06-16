package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private File file;
    private Map<String, Long> index;

    public Main() {
        file = new File("data.log");
        index = new HashMap<>();
        buildIndex();
    }

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
                        if (index.containsKey(key)) index.remove(key);
                    } else {
                        byte[] valueBytes = new byte[valueLength];
                        dis.readFully(valueBytes);
                        index.put(key, position);
                    }
                } catch (EOFException exception) {
                    break;
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        int port = 8080;
        Main main = new Main();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started running on port: " + port);
            while(true) {
                Socket socket = serverSocket.accept();
                main.processCommand(socket);
            }
        }
    }

    private void processCommand(Socket socket) {
        try (InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        ) {
            String command = reader.readLine();
            String[] parsedCommand = parseCommand(command);

            String action = parsedCommand[0];
            String key = parsedCommand[1];

            String result = "";
            switch(action) {
                case "GET":
                     result = processGetCommand(key);
                     break;
                case "SET":
                    String value = parsedCommand[2];
                    result = processSetCommand(key, value);
                    break;
                case "DEL":
                    result = String.valueOf(processDelCommand(key)) + "\n";
                    break;
                default:
                    System.out.println("Unknown command " + action);
                    result = "Unknown command, please try GET, SET, OR DEL\n";
            }

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(result.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {}
    }

    private String[] parseCommand(String command) {
        return command.split(" ");
    }

    private String processSetCommand(String key, String value) {
        int keyLength = key.getBytes().length;
        int valueLength = value.getBytes().length;

        try (FileOutputStream fos = new FileOutputStream(file, true);
             DataOutputStream dos = new DataOutputStream(fos)
        ) {
            FileChannel channel = fos.getChannel();
            long position = channel.position();

            dos.writeInt(keyLength);
            dos.write(key.getBytes(StandardCharsets.UTF_8));
            dos.writeInt(valueLength);
            dos.write(value.getBytes(StandardCharsets.UTF_8));

            index.put(key, position);
            System.out.println(index);
            return key +  "=" + value + "\n";
        } catch(IOException exception) {}
        return null;
    }

    private String processGetCommand(String targetKey) {
        if (index.containsKey(targetKey)) {
            Long position = index.get(targetKey);
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
        if (index.containsKey(targetKey)) {
            int keyLength = targetKey.getBytes().length;

            try (FileOutputStream fos = new FileOutputStream(file, true);
                 DataOutputStream dos = new DataOutputStream(fos)
            ) {
                dos.writeInt(keyLength);
                dos.write(targetKey.getBytes(StandardCharsets.UTF_8));
                dos.writeInt(-1);

                index.remove(targetKey);
                System.out.println(index);
                return true;
            } catch (IOException exception) {}
        }

        return false;
    }
}
