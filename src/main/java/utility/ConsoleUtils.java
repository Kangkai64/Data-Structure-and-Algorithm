package utility;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ConsoleUtils {
    public static String getStringInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty");
            } else {
                return input;
            }

        }
    }
    

    public static String getInputMatching(Scanner scanner, String prompt, Pattern pattern, String invalidMessage) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty");
            } else if (!pattern.matcher(input).matches()) {
                System.out.println(invalidMessage);
            } else {
                return input;
            }
        }
    }


    public static String getEmailInput(Scanner scanner, String prompt) {
        return getInputMatching(scanner, prompt, PatternChecker.EMAIL_PATTERN, "Please enter a valid email address");
    }


    public static String getInputMatchingWithDefault(Scanner scanner, String prompt, Pattern pattern, String invalidMessage, String defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            } else if (!pattern.matcher(input).matches()) {
                System.out.println(invalidMessage);
            } else {
                return input;
            }
        }
    }

    public static String getICInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("IC number cannot be empty");
            }
            else if (!PatternChecker.IC_PATTERN.matcher(input).matches()) {
                System.out.println("Invalid IC number format");
            }             
            else {
                return input;
            }
        }
    }

    public static String getPhoneInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Phone number cannot be empty");
            }
            else if (!PatternChecker.PHONE_PATTERN.matcher(input).matches()) {
                System.out.println("Invalid phone number format");
            }       
            else {
                return input;
            }
        }
    }

    public static String getTimeInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Time cannot be empty");
            }
            else if (!PatternChecker.TIME_PATTERN.matcher(input).matches()) {
                System.out.println("Invalid time format");
            }
            else {
                return input;
            }
        }
    }


    public static String getICInput(Scanner scanner, String prompt, String defaultValue) {
        return getInputMatchingWithDefault(scanner, prompt, PatternChecker.IC_PATTERN, "IC must be in format: XXXXXX-XX-XXXX", defaultValue);
    }

    public static String getEmailInput(Scanner scanner, String prompt, String defaultValue) {
        return getInputMatchingWithDefault(scanner, prompt, PatternChecker.EMAIL_PATTERN, "Please enter a valid email address", defaultValue);
    }

    public static String getPhoneInput(Scanner scanner, String prompt, String defaultValue) {
        return getInputMatchingWithDefault(scanner, prompt, PatternChecker.PHONE_PATTERN, "Phone must be in format: 0XX-XXXXXXX or 0XX-XXXXXXXX", defaultValue);
    }

    public static String getPostalCodeInput(Scanner scanner, String prompt) {
        Pattern postalPattern = Pattern.compile("^\\d{5}$");
        return getInputMatching(scanner, prompt, postalPattern, "Postal code must be 5 digits").trim();
    }

    public static String getPostalCodeInput(Scanner scanner, String prompt, String defaultValue) {
        Pattern postalPattern = Pattern.compile("^\\d{5}$");
        return getInputMatchingWithDefault(scanner, prompt, postalPattern, "Postal code must be 5 digits", defaultValue).trim();
    }

    public static String getWardNumberInput(Scanner scanner, String prompt) {
        Pattern wardPattern = Pattern.compile("^W\\d{1,19}$");
        return getInputMatching(
            scanner, 
            prompt, 
            wardPattern, 
            "Must start with 'W' followed by up to 19 digits (e.g., W1, W123)"
        );
    }
    
    public static String getWardNumberInput(Scanner scanner, String prompt, String defaultValue) {
        Pattern wardPattern = Pattern.compile("^W\\d{1,19}$");
        return getInputMatchingWithDefault(
            scanner, 
            prompt, 
            wardPattern, 
            "Must start with 'W' followed by up to 19 digits (e.g., W1, W123)", 
            defaultValue
        );
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

    public static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        
        int padding = (width - text.length()) / 2;
        StringBuilder centered = new StringBuilder();
        
        // Add left padding
        for (int i = 0; i < padding; i++) {
            centered.append(" ");
        }
        
        centered.append(text);
        
        // Add right padding to reach exact width
        while (centered.length() < width) {
            centered.append(" ");
        }
        
        return centered.toString();
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