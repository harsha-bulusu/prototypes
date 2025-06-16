package org.example;

import java.util.*;

public class App {

    private Set<Integer> processedIds = new HashSet<>();
    int offset = 0;

    public App() {
        processedIds.add(-1440014730);
    }

    static class Message {
        String message;
        int deduplicationId;

        public Message(String message, int deduplicationId) {
            this.message = message;
            this.deduplicationId = deduplicationId;
        }
    }

    private void produceEvents(Message[] queue) {
        for (int  i = 0; i < 100; i++) {
            queue[i] = new Message("message " + i, Objects.hash("message " + i));
        }
    }


    private void consumeEvents(Message[] queue) {
        while (true) {
            Message message = poll(queue);
            if (processedIds.contains(message.deduplicationId)) {
                // ignore processing
                System.out.println("Ignoring processing for message: " + message.message);
            } else {
                //process
                System.out.println("Processing Message " + message.message);
            }
        }
    }

    private Message poll(Message[] queue) {
        if (offset == 100) {
            while(true){}
        }
        return queue[offset++];
    }


    public static void main(String[] args) {
        Message[] queue = new Message[100];
        App app = new App();
        app.produceEvents(queue);
        app.consumeEvents(queue);
    }
}
