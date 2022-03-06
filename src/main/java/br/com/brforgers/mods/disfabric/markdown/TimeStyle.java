package br.com.brforgers.mods.disfabric.markdown;// Created 2022-04-03T04:52:12

/**
 * @author KJP12
 * @since ${version}
 **/
public enum TimeStyle {
    SHORT_TIME('t'),
    LONG_TIME('T'),
    SHORT_DATE('d'),
    LONG_DATE('D'),
    SHORT_DATETIME('f'),
    LONG_DATETIME('F'),
    RELATIVE_TIME('R');

    final char t;

    TimeStyle(char t) {
        this.t = t;
    }

    static TimeStyle of(String in) {
        if (in.isBlank()) return SHORT_DATETIME;
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
}
