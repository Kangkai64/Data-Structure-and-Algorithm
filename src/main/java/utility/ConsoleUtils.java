package utility;

import java.io.IOException;
import java.util.Scanner;
import java.io.Console;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class ConsoleUtils {
    // ANSI code for color code
    private static final String ESC_CODE = "\u001B";
    private static final String BLUE_COLOR = ESC_CODE + "[34m";
    private static final String RESET_COLOR = ESC_CODE + "[0m";

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

    public static LocalDateTime getDateTimeInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDateTime.parse(input.replace(" ", "T"));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd HH:mm");
            }
        }
    }

    public static double getDoubleInput(Scanner scanner, String prompt) {
        return getDoubleInput(scanner, prompt, 0.0, Double.MAX_VALUE);
    }

    /**
     * Gets password input from user with masking using asterisks.
     *
     * @param prompt The prompt message to display to the user
     * @return The password entered by the user
     */
    public static String getPasswordInput(Scanner scanner, String prompt) {
        Console console = System.console();
        if (console != null) {
            // If console is available, use its readPassword method for secure input
            char[] passwordChars = console.readPassword("%s", prompt);
            return new String(passwordChars);
        } else {
            // If console is not available (e.g., in IDE), use manual masking
            return getPasswordWithMasking(scanner, prompt);
        }
    }

    /**
     * Manually implements password masking for environments where console is not available.
     *
     * @param prompt The prompt message to display to the user
     * @return The password entered by the user
     */
    private static String getPasswordWithMasking(Scanner scanner, String prompt) {
        StringBuilder password = new StringBuilder();

        System.out.print(prompt);

        try {
            // Disable echo in terminal if possible
            // Note: This may not work in all environments (particularly IDEs)
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                char ch;
                while ((ch = (char) System.in.read()) != '\n' && ch != '\r') {
                    if (ch == '\b') {  // Handle backspace
                        if (password.length() > 0) {
                            System.out.print("\b \b");
                            password.deleteCharAt(password.length() - 1);
                        }
                    } else {
                        password.append(ch);
                        System.out.print("*");
                    }
                }
            } else {
                // For Unix-like systems, relying on manual input with masking
                // This is a simplified implementation
                String input = scanner.nextLine();
                password.append(input);

                // Print asterisks for each character entered
                for (int i = 0; i < input.length(); i++) {
                    System.out.print("\b*");
                }
            }
            System.out.println();  // Add a new line after input

        } catch (Exception e) {
            System.err.println("Error reading password: " + e.getMessage());
            // Fallback to regular scanner input
            password = new StringBuilder(scanner.nextLine());
        }

        return password.toString();
    }

    public static void displayLogo() {
        // The PCHub ASCII art logo
        String logo = "\n" +
                "    ____  ________  __      __  \n" +
                "   / __ \\/ ____/ / / /_  __/ /_ \n" +
                "  / /_/ / /   / /_/ / / / / __ \\\n" +
                " / ____/ /___/ __  / /_/ / /_/ /\n" +
                "/_/    \\____/_/ /_/\\__,_/_.___/ \n" +
                "                                \n";

        // Set text color to blue, print logo, then reset color
        System.out.print(BLUE_COLOR + logo + RESET_COLOR);
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

    /**
     * Prints a divider line with the specified character and length
     * @param character The character to use for the divider
     * @param length The length of the divider
     */
    public static void printDivider(char character, int length) {
        StringBuilder divider = new StringBuilder();
        for (int i = 0; i < length; i++) {
            divider.append(character);
        }
        System.out.println(divider.toString());
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
    public static void clearScreen() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback if clearing the screen fails
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Simulates a loading animation with dots
     * @param message The message to display while loading
     * @param duration The duration in milliseconds
     */
    public static void simulateLoading(String message, int duration) {
        System.out.print(message);
        long startTime = System.currentTimeMillis();
        int dotCount = 0;
        
        while (System.currentTimeMillis() - startTime < duration) {
            try {
                Thread.sleep(500);
                System.out.print(".");
                dotCount++;
                if (dotCount > 3) {
                    System.out.print("\r" + message);
                    dotCount = 0;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println();
    }
}