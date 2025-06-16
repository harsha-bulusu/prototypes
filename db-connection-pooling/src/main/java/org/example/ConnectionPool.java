package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPool {
    private int maxConnections;
    private Queue<Connection> connections;
    private ReentrantLock lock;

    private Semaphore semaphore;

    public ConnectionPool(int maxConnections) {
        this.maxConnections = maxConnections;
        this.connections = new LinkedList<>();
        for (int i = 0; i < maxConnections; i++) {
            try {
                Class.forName("org.postgresql.Driver");
                Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/harsha", "postgres", "harsha");
                connections.add(con);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.printf("Created %d connections\n", connections.size());

        lock = new ReentrantLock();
        semaphore = new Semaphore(maxConnections);
    }

    public Connection getConnection() throws InterruptedException {
        semaphore.acquire();
        lock.lock();
        try {
            if (!connections.isEmpty()) {
                Connection con = connections.poll();
                System.out.println("Remaining connections: " + connections.size());
                return con;
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void putConnection(Connection connection) {
        lock.lock();
        connections.add(connection);
        lock.unlock();
        semaphore.release();
    }
}
