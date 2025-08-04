package utility;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ConsoleUtils {
    public static String getStringInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int getIntInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }
    }

    public static double getDoubleInput(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }
    }

    public static Date getDateInput(Scanner scanner, String prompt) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return dateFormat.parse(input);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use dd-MM-yyyy");
            }
        }
    }

    public static String dateTimeFormatter(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }

    public static String reportDateTimeFormatter(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
        return dateFormat.format(date);
    }

    // Get user input with default value
    public static String getStringInput(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    public static int getIntInput(Scanner scanner, String prompt, int defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : Integer.parseInt(input);
    }

    public static double getDoubleInput(Scanner scanner, String prompt, double defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : Double.parseDouble(input);
    }

    public static Date getDateInput(Scanner scanner, String prompt, Date defaultValue) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                return dateFormat.parse(input);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use dd-MM-yyyy");
            }
        }
    }

    public static void printHeader(String title) {
        int headerWidth = title.length();

        // Create a repeated string of "=" characters
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < headerWidth; i++) {
            separator.append("=");
        }

        System.out.println(separator.toString());
        System.out.println(title);
        System.out.println(separator.toString());
    }

    public static void waitMessage() {
        System.out.println("\n\n\nPress Enter to continue...");
        try {
            // Read until we get a newline character
            while (System.in.read() != '\n') {
                // Keep reading until we get a newline
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}