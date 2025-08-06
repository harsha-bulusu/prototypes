package com.harsha.snippets;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class InMemoryStore {

    private static Map<String, Object> keyDir = new HashMap<>();

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("""
                        \n\nSelect operation
                        1. GET
                        2. SET
                        3. DEL
                        """);
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch (choice) {
                    case 1:
                        System.out.print("Enter Key: ");
                        String key = scanner.nextLine();
                        System.out.println(">>>>>>" + keyDir.getOrDefault(key, "NIL"));
                        break;
                    case 2:
                        System.out.print("Enter Key: ");
                        key = scanner.nextLine();
                        System.out.print("Enter Value: ");
                        Object value = scanner.nextLine();
                        keyDir.put(key, value);
                        System.out.println(">>>>> OK");
                        break;
                    case 3:
                        System.out.print("Enter Key: ");
                        key = scanner.nextLine();
                        System.out.println(keyDir.remove(key) == null ? "NIL" : ">>>>>> OK");
                        break;
                    default:
                        System.out.println("Enter valid option");
                        break;
                }
            }
        }
        


    }
}  