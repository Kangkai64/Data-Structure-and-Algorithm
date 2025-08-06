package utility;

import java.util.regex.Pattern;

public class PatternChecker {
    // Email validation pattern
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    // Phone number validation pattern (basic)
    public static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{2}-[0-9]{7,8}$");
    // IC number validation pattern
    public static final Pattern IC_PATTERN = Pattern.compile("^\\d{6}-\\d{2}-\\d{4}$");
    // Date validation pattern
    public static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$");
}
