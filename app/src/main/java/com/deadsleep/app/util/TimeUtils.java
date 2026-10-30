package com.deadsleep.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility functions for time math, formatting, countdowns, and midnight-crossing schedules.
 */
public final class TimeUtils {

    private TimeUtils() {}

    public static final int DAY_MON = 1;
    public static final int DAY_TUE = 2;
    public static final int DAY_WED = 4;
    public static final int DAY_THU = 8;
    public static final int DAY_FRI = 16;
    public static final int DAY_SAT = 32;
    public static final int DAY_SUN = 64;
    public static final int ALL_DAYS = 127;
    public static final int WEEKDAYS = DAY_MON | DAY_TUE | DAY_WED | DAY_THU | DAY_FRI;
    public static final int WEEKENDS = DAY_SAT | DAY_SUN;

    /**
     * Converts hour and minute to standard AM/PM formatted time string (e.g. 11:30 PM).
     * Uses Locale.US to ensure standard numerals across all device locales.
     */
    public static String formatTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        return sdf.format(cal.getTime());
    }

    /**
     * Converts hour and minute based on device 24-hour setting.
     */
    public static String formatTime(android.content.Context context, int hour, int minute) {
        if (context != null && android.text.format.DateFormat.is24HourFormat(context)) {
            return formatTime24(hour, minute);
        }
        return formatTime(hour, minute);
    }

    /**
     * Converts hour and minute to 24-hour format string (e.g. 23:30).
     */
    public static String formatTime24(int hour, int minute) {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    /**
     * Calculates duration in minutes between sleep time and wake time, correctly handling midnight crossing.
     */
    public static int calculateDurationMinutes(int sleepHour, int sleepMinute, int wakeHour, int wakeMinute) {
        int sleepTotal = sleepHour * 60 + sleepMinute;
        int wakeTotal = wakeHour * 60 + wakeMinute;

        if (wakeTotal >= sleepTotal) {
            return wakeTotal - sleepTotal;
        } else {
            // Crosses midnight: minutes remaining today + minutes tomorrow
            return (1440 - sleepTotal) + wakeTotal;
        }
    }

    /**
     * Formats duration in minutes to readable "Xh Ym" string.
     */
    public static String formatDuration(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (minutes == 0) {
            return hours + "h";
        }
        return hours + "h " + minutes + "m";
    }

    /**
     * Formats milliseconds countdown to "HH:MM:SS" or "Xh Ym".
     */
    public static String formatCountdown(long millisRemaining) {
        if (millisRemaining <= 0) return "00:00";
        long totalSeconds = millisRemaining / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Formats countdown into explicit units so it cannot be mistaken for a clock time:
     * e.g. "05h 22m 15s", "45m 10s", or "15s".
     */
    public static String formatCountdownVerbose(long millisRemaining) {
        if (millisRemaining <= 0) return "00m 00s";
        long totalSeconds = millisRemaining / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.US, "%02dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format(Locale.US, "%02dm %02ds", minutes, seconds);
        } else {
            return String.format(Locale.US, "%02ds", seconds);
        }
    }

    /**
     * Computes the exact timestamp in millis for a wake-up time given a sleep start timestamp,
     * correctly handling midnight crossing without depending on weekly repeat days.
     */
    public static long getTargetWakeTimeMillis(long sleepTimeMillis, int wakeHour, int wakeMinute) {
        Calendar wakeCal = Calendar.getInstance();
        wakeCal.setTimeInMillis(sleepTimeMillis);
        wakeCal.set(Calendar.HOUR_OF_DAY, wakeHour);
        wakeCal.set(Calendar.MINUTE, wakeMinute);
        wakeCal.set(Calendar.SECOND, 0);
        wakeCal.set(Calendar.MILLISECOND, 0);

        if (wakeCal.getTimeInMillis() <= sleepTimeMillis) {
            wakeCal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return wakeCal.getTimeInMillis();
    }

    /**
     * Formats days bitmask into a short human-readable string (e.g. "Mon, Tue, Wed", "Everyday", "Weekdays").
     */
    public static String formatDays(int daysBitmask) {
        if ((daysBitmask & ALL_DAYS) == ALL_DAYS) return "Everyday";
        if (daysBitmask == WEEKDAYS) return "Weekdays";
        if (daysBitmask == WEEKENDS) return "Weekends";
        if (daysBitmask == 0) return "Never";

        StringBuilder sb = new StringBuilder();
        if ((daysBitmask & DAY_MON) != 0) sb.append("Mon, ");
        if ((daysBitmask & DAY_TUE) != 0) sb.append("Tue, ");
        if ((daysBitmask & DAY_WED) != 0) sb.append("Wed, ");
        if ((daysBitmask & DAY_THU) != 0) sb.append("Thu, ");
        if ((daysBitmask & DAY_FRI) != 0) sb.append("Fri, ");
        if ((daysBitmask & DAY_SAT) != 0) sb.append("Sat, ");
        if ((daysBitmask & DAY_SUN) != 0) sb.append("Sun, ");

        if (sb.length() >= 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    /**
     * Checks if a bitmask has a particular Calendar day (Calendar.SUNDAY, Calendar.MONDAY, etc.) active.
     */
    public static boolean isDayActive(int daysBitmask, int calendarDay) {
        int mask = getBitmaskForCalendarDay(calendarDay);
        return (daysBitmask & mask) != 0;
    }

    public static int getBitmaskForCalendarDay(int calendarDay) {
        switch (calendarDay) {
            case Calendar.MONDAY: return DAY_MON;
            case Calendar.TUESDAY: return DAY_TUE;
            case Calendar.WEDNESDAY: return DAY_WED;
            case Calendar.THURSDAY: return DAY_THU;
            case Calendar.FRIDAY: return DAY_FRI;
            case Calendar.SATURDAY: return DAY_SAT;
            case Calendar.SUNDAY: return DAY_SUN;
            default: return 0;
        }
    }

    /**
     * Computes the next occurrence timestamp in millis for a given hour and minute considering repeat days.
     */
    public static long getNextOccurrenceMillis(int hour, int minute, int daysBitmask) {
        Calendar now = Calendar.getInstance();
        Calendar candidate = Calendar.getInstance();
        candidate.set(Calendar.HOUR_OF_DAY, hour);
        candidate.set(Calendar.MINUTE, minute);
        candidate.set(Calendar.SECOND, 0);
        candidate.set(Calendar.MILLISECOND, 0);

        if (daysBitmask == 0) {
            // One-shot: if time has passed today, schedule for tomorrow
            if (candidate.getTimeInMillis() <= now.getTimeInMillis()) {
                candidate.add(Calendar.DAY_OF_YEAR, 1);
            }
            return candidate.getTimeInMillis();
        }

        // Check the next 7 days for an active day
        for (int i = 0; i <= 7; i++) {
            int dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK);
            if (isDayActive(daysBitmask, dayOfWeek)) {
                if (candidate.getTimeInMillis() > now.getTimeInMillis()) {
                    return candidate.getTimeInMillis();
                }
            }
            candidate.add(Calendar.DAY_OF_YEAR, 1);
        }

        return candidate.getTimeInMillis();
    }

    /**
     * Formats timestamp to simple date string (e.g. 2026-10-29).
     */
    public static String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date(millis));
    }

    /**
     * Formats timestamp to readable time with seconds (e.g. 11:45:30 PM).
     */
    public static String formatDateTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
