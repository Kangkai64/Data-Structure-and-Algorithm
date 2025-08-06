package utility;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.time.DateTimeException;

public class ConsoleUtils {
    public static String getStringInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty");
            } else if (!PatternChecker.CONTAIN_ALPHABETS_PATTERN.matcher(input).matches()) {
                System.out.println("Input must contain at least one alphabet");
            } else {
                return input;
            }
        }
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

    public static LocalDate getDateInput(Scanner scanner, String prompt, DateType dateType) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                LocalDate date = LocalDate.parse(input, dateFormat);
                if (date.isBefore(LocalDate.of(1900, 1, 1))) {
                    System.out.println("Year should be greater than 1900");
                } else if (dateType == DateType.PAST_DATE_ONLY && date.isAfter(LocalDate.now())) {
                    System.out.println("Date should be in the past");
                } else if (dateType == DateType.FUTURE_DATE_ONLY && date.isBefore(LocalDate.now())) {
                    System.out.println("Date should be in the future");
                } else {
                    return date;
                }
            } catch (DateTimeException e) {
                System.out.println("Invalid date format. Please use DD-MM-YYYY");
            }
        }
    }

    public static boolean getBooleanInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty");
            } else if (input.equalsIgnoreCase("Y") || input.equalsIgnoreCase("N")) {
                return input.equalsIgnoreCase("Y");
            } else {
                System.out.println("Invalid input. Please enter Y or N.");
            }
        }
    }

    // Get user input with default value
    public static String getStringInput(Scanner scanner, String prompt, String defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            } else if (!PatternChecker.CONTAIN_ALPHABETS_PATTERN.matcher(input).matches()) {
                System.out.println("Input must contain at least one alphabet");
            } else {
                return input;
            }
        }
    }

    public static int getIntInput(Scanner scanner, String prompt, int defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            } else {
                try {
                    return Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number");
                }
            }
        }
    }

    public static double getDoubleInput(Scanner scanner, String prompt, double defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            } else {
                try {
                    return Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number");
                }
            }
        }
    }

    public static LocalDate getDateInput(Scanner scanner, String prompt, DateType dateType, LocalDate defaultValue) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-uuuu");
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                LocalDate date = LocalDate.parse(input, dateFormat);
                if (date.isBefore(LocalDate.of(1900, 1, 1))) {
                    System.out.println("Year should be greater than 1900");
                } else if (dateType == DateType.PAST_DATE_ONLY && date.isAfter(LocalDate.now())) {
                    System.out.println("Date should be in the past");
                } else if (dateType == DateType.FUTURE_DATE_ONLY && date.isBefore(LocalDate.now())) {
                    System.out.println("Date should be in the future");
                } else {
                    return date;
                }
            } catch (DateTimeException e) {
                System.out.println("Invalid date format. Please use DD-MM-YYYY");
            }
        }
    }

    public static boolean getBooleanInput(Scanner scanner, String prompt, boolean defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            } else if (input.equalsIgnoreCase("Y") || input.equalsIgnoreCase("N")) {
                return input.equalsIgnoreCase("Y");
            } else {
                System.out.println("Invalid input. Please enter Y or N.");
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