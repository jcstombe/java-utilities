package jcstombe.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Josh Stomberg <jcstombe@mtu.edu>
 * Last Modified: May 7, 2018
 */
public class Log {

    private static boolean startsWith(String s, String... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (s.startsWith(patterns[i])) return true;
        }
        return false;
    }

    public static final Predicate<String> QQ_FILTER = s -> startsWith(s, "[CRITICAL]");
    public static final Predicate<String> Q_FILTER = s -> QQ_FILTER.test(s) || startsWith(s, "[ERROR]", "[RESULT");
    public static final Predicate<String> S_FILTER = s -> Q_FILTER.test(s) || startsWith(s, "[WARN]", "[TIME]");
    public static final Predicate<String> V_FILTER = s -> S_FILTER.test(s) || startsWith(s, "[INFO]", "[SYSTEM]");
    public static final Predicate<String> VV_FILTER = s -> V_FILTER.test(s) || startsWith(s, "[DETAIL]");

    public enum Verbosity {
        NOTHING, // Nothing
        VERY_MINIMAL, // Log.critical (QQ_FILTER)
        MINIMAL, // Log.error, Log.stackTrace, Log.result, and above (Q_FILTER)
        STANDARD, // Log.warn, Log.time, and above (S_FILTER)
        VERBOSE, // Log.info, Log.system, and above (V_FILTER)
        VERY_VERBOSE, // Log.note and above (VV_FILTER)
        EVERYTHING; // Log.note and above (

        public boolean lessThan(Verbosity l) {
            return ordinal() < l.ordinal();
        }

        public boolean greaterThan(Verbosity l) {
            return ordinal() > l.ordinal();
        }
    }

    private static boolean systemLoggingEnabled;
    private static boolean multiThreadEnabled;
    private static DateFormat timeF = new SimpleDateFormat("YYYYMMdd HH:mm:ss.SSS");
    private static Verbosity lvl;
    private static List<PrintStream> out;

    // --------- Initialize Log configuration --------------- //

    static {
        // Initialize logging (default to printing to System.out)
        out = new ArrayList<>();
        addLoggingOutput(System.out);

        // Configure default logging lvl to STANDARD
        setLoggingLevel(Verbosity.STANDARD);
        // Enable system logging by default
        setSystemLoggingEnabled(true);
        // Disable multithread logging by default
        setMultiThreadEnabled(false);
    }

    // -------- Configuration Methods ----------------------- //
    public synchronized static void addLoggingOutput(PrintStream o) {
        if (o == null) return;
        out.add(o);
    }

    public synchronized static void addLoggingOutputFile(File f) {
        if (f == null) return;
        try {
            out.add(new PrintStream(new FileOutputStream(f, true)));
        } catch (FileNotFoundException e) {
            Log.warn("Logs::addLoggingOutputFile, Unable to add output file %s", f.getName());
        }
    }

    public synchronized static void setSystemLoggingEnabled(boolean enable) {
        systemLoggingEnabled = enable;
    }

    public synchronized static void setMultiThreadEnabled(boolean enable) {
        multiThreadEnabled = enable;
    }

    public static Verbosity getLoggingLevel() {
        return lvl;
    }

    public synchronized static void setLoggingLevel(Verbosity l) {
        if (l == null) return;
        lvl = l;
    }

    public synchronized static void setTimeFormat(DateFormat df) {
        if (df == null) return;
        timeF = df;
    }

    public synchronized static boolean removeLoggingOutput(PrintStream o) {
        return out.remove(o);
    }

    public synchronized static void flushAll() {
        for (PrintStream o : out) {
            o.flush();
        }
    }

    // ---------- Public logging methods ------------------------- //
    public static void critical(String format, Object... objects) {
        if (lvl.greaterThan(Verbosity.NOTHING)) {
            Log.output("CRITICAL", String.format(format, objects));
        }
    }

    public static void error(String format, Object... objects) {
        if (lvl.greaterThan(Verbosity.VERY_MINIMAL)) {
            Log.output("ERROR", String.format(format, objects));
        }
    }

    public static void warn(String format, Object... objects) {
        if (lvl.greaterThan(Verbosity.MINIMAL)) {
            Log.output("WARN", String.format(format, objects));
        }
    }

    public static void info(String format, Object... objects) {
        if (lvl.greaterThan(Verbosity.STANDARD)) {
            Log.output("INFO", String.format(format, objects));
        }
    }

    public static void detail(String format, Object... objects) {
        if (lvl.greaterThan(Verbosity.VERBOSE)) {
            Log.output("DETAIL", String.format(format, objects));
        }
    }

    public static void note(String format, Object... objects) {
        if (lvl.greaterThan(Verbosity.VERY_VERBOSE)) {
            Log.output("NOTE", String.format(format, objects));
        }
    }

    public static void logStackTrace(Exception e) {
        Objects.requireNonNull(e, "Attempted to log a null exception");
        if (lvl.greaterThan(Verbosity.VERY_MINIMAL)) {
            final String time = timeF.format(new Date());
            final String msg = String.format("[%s] Stack Trace", time);
            for (final PrintStream o : out) {
                o.println(msg);
                e.printStackTrace(o);
            }
        }
    }

    public static void logResult() {

    }

    public static void logTime(LogTimer timer) {
        Objects.requireNonNull(timer, "Attempted to log a null timer");
        if (LogTimer.isTiming() && lvl.greaterThan(Verbosity.MINIMAL)) {
            Log.output("TIME", timer.toString());
        }
    }

    public static void logSystemStatus() {
        if (systemLoggingEnabled && lvl.greaterThan(Verbosity.STANDARD)) {
            final Runtime myRuntime = Runtime.getRuntime();
            final long maxMem = myRuntime.maxMemory();
            final long totalMem = myRuntime.totalMemory();
            final long freeMem = myRuntime.freeMemory();
            final String msg = String.format("Memory Usage: %d Free | %d Total | %d Max", freeMem, totalMem, maxMem);
            output("SYSTEM", msg);
        }
    }

    // ---------- Private helper methods -------------------------- //
    private static void output(String prefix, String content) {
        final String time = timeF.format(new Date());
        if (multiThreadEnabled) {
            final String msg = String.format("[%s][%s][%s] %s", prefix, time, Thread.currentThread().getName(), content);
            for (final PrintStream o : out) {
                o.println(msg);
            }
        } else {
            final String msg = String.format("[%s][%s] %s", prefix, time, content);
            for (final PrintStream o : out) {
                o.println(msg);
            }
        }
    }
}
