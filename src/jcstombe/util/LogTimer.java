package jcstombe.util;

/**
 * A performance timing implementation (stopwatch, not alarm).
 *
 * @author Josh Stomberg <jcstombe@mtu.edu>
 * Last Modified: May 7, 2018
 */
public class LogTimer {

    private static boolean timingEnabled = true;

    public static void enableTiming() {
        timingEnabled = true;
    }

    public static void disableTiming() {
        timingEnabled = false;
    }

    public static boolean isTiming() {
        return timingEnabled;
    }

    private String timerName;
    private long startTime;

    public LogTimer(String name) {
        timerName = name;
        start();
    }

    public LogTimer(String nameFormat, Object... args) {
        this(String.format(nameFormat, args));
    }

    public void start() {
        if (!timingEnabled) {
            return;
        }
        startTime = System.nanoTime();
    }

    public void restart() {
        start();
    }

    public void restart(String newName) {
        timerName = newName;
        start();
    }

    private long getNanoseconds(long curTime) {
        if (!timingEnabled) {
            return 0;
        }
        return curTime - startTime;
    }

    public long getNanoseconds() {
        return getNanoseconds(System.nanoTime());
    }

    public long getMicroseconds() {
        return (getNanoseconds() / 1000);
    }

    public double getMicros() {
        return (getNanoseconds() / 1e3);
    }

    public long getMilliseconds() {
        return (getNanoseconds() / 1000000);
    }

    public double getMillis() {
        return (getNanoseconds() / 1e6);
    }

    public long getSeconds() {
        return (getNanoseconds() / 1000000000);
    }

    public double getSecs() {
        return (getNanoseconds() / 1e9);
    }

    public String humanTime() {
        double millis = getMillis();
        if (millis < 1000) {
            return String.format("%.3f milliseconds", millis);
        }
        double seconds = millis / 1000;
        if (seconds < 60) {
            return String.format("%.6f seconds", seconds);
        }
        int minutes = (int) Math.floor(seconds / 60);
        seconds -= (minutes * 60);
        if (minutes < 60) {
            return String.format("%d minutes %.6f seconds", minutes, seconds);
        }
        int hours = minutes / 60;
        minutes %= 60;
        if (hours < 24) {
            return String.format("%d hours %d minutes %.6f seconds", hours, minutes, seconds);
        }
        int days = hours / 24;
        hours %= 24;
        return String.format("%d days %d hours %d minutes %.6f seconds", days, hours, minutes, seconds);
    }

    @Override
    public String toString() {
        return String.format("%s {%s}", timerName, humanTime());
    }
}
