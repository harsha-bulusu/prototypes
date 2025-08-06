package com.harsha.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class QueryParser {

    private ExecutionEngine executionEngine = new ExecutionEngine();

    public void parseCommand(Socket socket) throws IOException {
        try (InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        ) {
            String command = reader.readLine();
            String[] parsedCommand = command.split(" ");

            String action = parsedCommand[0];
            String key = parsedCommand[1];

            String result = "";
            switch(action) {
                case "GET":
                     result = executionEngine.processGetCommand(key);
                     break;
                case "SET":
                    if (parsedCommand.length < 3 || !parsedCommand[2].contains(":")) {
                        result = "SET command format is: SET <key> <type>:<value>\n";
                        break;
                    }
                    String value = parsedCommand[2];
                    String[] typeAndValue = value.split(":", 2);
                    byte type = Byte.parseByte(typeAndValue[0]);
                    boolean setResult = executionEngine.processSetCommand(key, type, typeAndValue[1]);
                    if (!setResult) {
                        result = "Failed to persist value\n";
                    } else {
                        result = "OK\n";
                    }
                    break;
                case "DEL":
                    executionEngine.processDelCommand(key);
                    break;
                default:
                    System.out.println("Unknown command " + action);
                    result = "Unknown command, please try GET, SET, OR DEL\n";
            }

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(result.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {

        }
        catch(Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

}
