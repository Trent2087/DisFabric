package br.com.brforgers.mods.disfabric.markdown;// Created 2022-04-03T04:52:12

import com.ibm.icu.text.RelativeDateTimeFormatter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ValueRange;
import java.util.concurrent.TimeUnit;

/**
 * @author KJP12
 * @since ${version}
 **/
public enum TimeStyle {
    SHORT_TIME('t', DateTimeFormatter.ofPattern("HH:mmX")),
    LONG_TIME('T', DateTimeFormatter.ofPattern("HH:mm:ssX")),
    SHORT_DATE('d', DateTimeFormatter.ofPattern("yyyy-MM-ddX")),
    LONG_DATE('D', DateTimeFormatter.ofPattern("dd LLL yyyyX")),
    SHORT_DATETIME('f', DateTimeFormatter.ofPattern("dd LLL yyyy HH:mmX")),
    LONG_DATETIME('F', DateTimeFormatter.ofPattern("EEE dd LLL yyyyX HH:mmX")),
    RELATIVE_TIME('R', null);

    private static final ValueRange EPOCH_DAY_RANGE = ChronoField.EPOCH_DAY.range();

    public final char t;
    private final DateTimeFormatter formatter;

    TimeStyle(char t, DateTimeFormatter formatter) {
        this.t = t;
        this.formatter = formatter;
    }

    static TimeStyle of(String in) {
        if (in == null || in.isBlank()) return SHORT_DATETIME;
        return switch (in.charAt(0)) {
            case 't' -> SHORT_TIME;
            case 'T' -> LONG_TIME;
            case 'd' -> SHORT_DATE;
            case 'D' -> LONG_DATE;
            case 'F' -> LONG_DATETIME;
            case 'R' -> RELATIVE_TIME;
            default -> SHORT_DATETIME;
        };
    }

    public String style(long in) {
        if (formatter != null) {
            if (!EPOCH_DAY_RANGE.isValidValue(TimeUnit.SECONDS.toDays(in))) {
                return "Invalid date";
            }
            return formatter.format(OffsetDateTime.ofInstant(Instant.ofEpochSecond(in), ZoneOffset.UTC));
        }
        if (this == RELATIVE_TIME) {
            double relative = in - (System.currentTimeMillis() / 1000D);
            var unit = smallestUnit(Math.abs(relative));
            return RelativeDateTimeFormatter.getInstance().format(mux(relative, unit), unit);
        }
        return "Invalid date style";
    }

    private static RelativeDateTimeFormatter.RelativeDateTimeUnit smallestUnit(double abs) {
        if (abs < TimeUnit.MINUTES.toSeconds(1)) {
            return RelativeDateTimeFormatter.RelativeDateTimeUnit.SECOND;
        }
        if (abs < TimeUnit.HOURS.toSeconds(1)) {
            return RelativeDateTimeFormatter.RelativeDateTimeUnit.MINUTE;
        }
        if (abs < TimeUnit.DAYS.toSeconds(1)) {
            return RelativeDateTimeFormatter.RelativeDateTimeUnit.HOUR;
        }
        if (abs < TimeUnit.DAYS.toSeconds(30)) {
            return RelativeDateTimeFormatter.RelativeDateTimeUnit.DAY;
        }
        if (abs < TimeUnit.DAYS.toSeconds(365)) {
            return RelativeDateTimeFormatter.RelativeDateTimeUnit.MONTH;
        }
        return RelativeDateTimeFormatter.RelativeDateTimeUnit.YEAR;
    }

    private static double mux(double relative, RelativeDateTimeFormatter.RelativeDateTimeUnit unit) {
        return switch (unit) {
            case SECOND -> relative;
            case MINUTE -> relative / 60D;
            case HOUR -> relative / 3600D;
            case DAY -> relative / 86400D;
            case MONTH -> relative / 2592000D;
            case YEAR -> relative / 31536000D;
            default -> throw new AssertionError();
        };
    }
}
