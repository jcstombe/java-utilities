package jcstombe.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

/**
 * @author Josh Stomberg <jcstombe@mtu.edu>
 * Last Modified: 05 07, 2018
 */
public abstract class ResultFile<Ident> {

    public class Result {
        private Ident resultId;
        private Map<String, Number> statsMap;

        public Result(Ident resultId) {
            Objects.requireNonNull(resultId, "ResultId cannot be null");
            this.resultId = resultId;
            statsMap = new HashMap<>();
            ResultFile.this.results.add(this);
        }

        public void addStatistic(String name, Number value) {
            if (!formatMap.containsKey(name)) return;
            statsMap.put(name, value);
        }

        public Number getStatistic(String name) {
            return statsMap.get(name);
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder(printResultId(resultId));
            for (String key : formatMap.keySet()) {
                try {
                    String statString = String.format('\t' + formatMap.get(key).formatString(), statsMap.get(key));
                    out.append(statString);
                } catch (IllegalFormatException e) {
                    Log.error("Illegal foramt '%s' for statistic '%s' in a result", formatMap.get(key), key);
                    out.append("\t").append(statsMap.get(key));
                }
            }
            return out.toString();
        }

        @Override
        public boolean equals(Object obj) {
            try {
                Result other = (Result) obj;
                return resultId.equals(other.resultId);
            } catch (ClassCastException ignored) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return resultId.hashCode();
        }
    }

    private File file;
    private Map<String, ResultStatisticFormat> formatMap;
    private Set<Result> results;

    public ResultFile(File file) {
        this.file = file;
        results = new HashSet<>();
        formatMap = new HashMap<>();
    }

    public void addStatistic(String name, ResultStatisticFormat format) {
        formatMap.put(name, format);
    }

    private Set<String> readHeaderLine(String headerLine) {
        Set<String> stats = new LinkedHashSet<>();
        Scanner lineIn = new Scanner(headerLine);
        lineIn.useDelimiter("\t");
        if (validResultIdHeader(lineIn)) {
            while (lineIn.hasNext()) {
                stats.add(lineIn.next());
            }
        } else {
            throw new RuntimeException("Result File Format Exception: Invalid Result Id Header");
        }
        return stats;
    }

    public boolean contains(Result r) {
        return results.contains(r);
    }

    public void readResults() {
        try (Scanner fileIn = new Scanner(file)) {
            if (!fileIn.hasNextLine()) return;
            Set<String> fileStats = readHeaderLine(fileIn.nextLine());
            while (fileIn.hasNextLine()) {
                Scanner lineIn = new Scanner(fileIn.nextLine());
                Ident resultId = readResultId(lineIn);
                lineIn.useDelimiter("\t");
                Result r = new Result(resultId);
                for (String key : fileStats) {
                    ResultStatisticFormat format = formatMap.get(key);
                    if (format == null) {
                        lineIn.next();
                    } else if (format.isFloatingPoint()) {
                        r.addStatistic(key, lineIn.nextDouble());
                    } else {
                        r.addStatistic(key, lineIn.nextInt());
                    }
                }
                results.add(r);
            }
        } catch (FileNotFoundException e) {
            Log.info("Missing Results File: %s. No results loaded", file.getName());
        } catch (NoSuchElementException e) {
            Log.warn("Result File Format Error: Missing Element");
        } catch (RuntimeException e) {
            Log.warn(e.getMessage());
        }
    }

    private void printHeaderLine(PrintStream out) {
        out.print(printResultIdHeader());
        formatMap.keySet().forEach((statName) -> out.print('\t' + statName));
    }

    public void writeResults() {
        try (PrintStream fileOut = new PrintStream(file)) {
            printHeaderLine(fileOut);
            results.forEach(fileOut::println);
        } catch (FileNotFoundException ignored) {
        }
    }

    public abstract String printResultId(Ident resultId);

    public abstract String printResultIdHeader();

    public abstract Ident readResultId(Scanner in);

    public abstract boolean validResultIdHeader(Scanner in);
}
