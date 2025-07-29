package sho.structs;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Time {
    private static final long SECOND = 1000;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 24;
    private static final long WEEK = DAY * 7;
    private static final long MONTH = DAY * 30;

    private static final Map<String, Long> UNITS = new LinkedHashMap<>();

    static {
        UNITS.put("mo", MONTH);
        UNITS.put("w", WEEK);
        UNITS.put("d", DAY);
        UNITS.put("h", HOUR);
        UNITS.put("m", MINUTE);
        UNITS.put("s", SECOND);
        UNITS.put("ms", 1L);
    }

    public static long parse(String time) {
        long total = 0;
        time = time.toLowerCase().replaceAll(",", " ").replaceAll("\\s+", " ").trim();
        Pattern pattern = Pattern.compile("(\\d+)\\s*(mo|w|d|h|m(?!s)|s|ms)");
        Matcher matcher = pattern.matcher(time);
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            Long factor = UNITS.get(unit);
            if (factor != null) {
                total += value * factor;
            }
        }
        if (total == 0) throw new IllegalArgumentException("Invalid time format: " + time);
        return total;
    }

    public static String format(long time) {
        return format(time, false);
    }

    public static String format(long time, boolean shortFormat) {
        StringBuilder sb = new StringBuilder();
        long remaining = time;

        for (Map.Entry<String, Long> entry : UNITS.entrySet()) {
            String unit = entry.getKey();
            long unitValue = entry.getValue();
            if (unit.equals("ms")) continue;

            long count = remaining / unitValue;
            if (count > 0) {
                remaining %= unitValue;
                sb.append(formatUnit(count, unit, shortFormat)).append(" ");
            }
        }

        if (sb.length() == 0 && time > 0) {
            sb.append(shortFormat ? time + "ms" : time + " millisecond" + (time != 1 ? "s" : ""));
        }

        return sb.toString().trim();
    }

    private static String formatUnit(long value, String unit, boolean shortFormat) {
        if (shortFormat) {
            return value + unit;
        }
        String longName =
                switch (unit) {
                    case "mo" -> "month";
                    case "w" -> "week";
                    case "d" -> "day";
                    case "h" -> "hour";
                    case "m" -> "minute";
                    case "s" -> "second";
                    default -> "millisecond";
                };
        return value + " " + pluralize(value, longName);
    }

    public static String pluralize(long value, String unit) {
        return value == 1 ? unit : unit + "s";
    }
}
