package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

public class App 
{

    private void executeQuery(Connection connection) throws SQLException {
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT pg_sleep(0.1);");
            System.out.println(Thread.currentThread().getName() + " executed successfully");
        } finally {
            try {
                connection.close(); // Properly close connection
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private Connection makeConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/harsha", "postgres", "harsha");
        } catch (Exception e) {
            System.out.println("Err creating connection: " + e.getMessage());
            return null;
        }
    }

    private void benchmarkNonPool() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(100);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Runnable task = () -> {
                try {
                    Connection connection = makeConnection();
                    if (connection != null)
                        executeQuery(connection);
                } catch (Exception e) {
                    System.out.println("Error getting connection: " + e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            };
            new Thread(task).start();
        }
        countDownLatch.await();
        long endTime = System.currentTimeMillis();
        System.out.println("=======================");
        System.out.println("Time taken is: " + (endTime - startTime) + " msec");
        System.out.println("=======================");
    }

    private void benchmarkPool() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(100);
        ConnectionPool connectionPool = new ConnectionPool(10);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Runnable task = () -> {
                Connection connection = null;
                try {
                    connection = connectionPool.getConnection();
                    if (connection != null)
                        executeQuery(connection);
                } catch (Exception e) {
                    System.out.println("Error getting connection: " + e.getMessage());
                } finally {
                    countDownLatch.countDown();
                    if (connection != null) {
                        connectionPool.putConnection(connection);
                    }
                }
            };

            new Thread(task).start();
        }
        countDownLatch.await();
        long endTime = System.currentTimeMillis();
        System.out.println("=======================");
        System.out.println("Time taken is: " + (endTime - startTime) + " msec");
        System.out.println("=======================");
    }

    public static void main( String[] args ) throws InterruptedException, SQLException {

//        new App().benchmarkNonPool();
            new App().benchmarkPool();
    }
}
