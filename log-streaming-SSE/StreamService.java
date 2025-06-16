import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class StreamService {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/logs", new LogsHandler());
        server.start();
        System.out.println("Server started running on http://localhost:8080");
    }    
}

class LogsHandler implements HttpHandler {

    private void readFile(String filename, OutputStream output) throws Exception {
        File file = new File(filename);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            try {
                String event = "data: " + line + "\n\n";
                output.write(event.getBytes());
                output.flush();
                Thread.sleep(500);
                
            } catch (InterruptedException e) {
                break;
            }
        }
        bufferedReader.close();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String id = null;

        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals("id")) {
                    id = pair[1];
                    break;
                }
            }
        }

        if (id == null) {
            String response = "Missing 'id' query parameter";
            exchange.sendResponseHeaders(400, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            return;
        }
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String fileName = "logs/" + id + ".txt";

        exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
        exchange.sendResponseHeaders(200, 0);
        var output = exchange.getResponseBody();
        try {
            readFile(fileName, output);
        } catch (Exception e) {
            e.printStackTrace();
            output.close();
        }
    }
}
