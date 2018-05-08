package jcstombe.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

/**
 * @author Josh Stomberg <jcstombe@mtu.edu>
 * Last Modified: 05 07, 2018
 */
public class ResultFile {

    public class Result {
        private String modelName, dataId;
        private Map<String, Number> statsMap;

        public Result(String modelName, String dataId) {
            Objects.requireNonNull(modelName, "Model Name cannot be null");
            Objects.requireNonNull(dataId, "Data ID cannot be null");
            this.modelName = modelName;
            this.dataId = dataId;
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
            StringBuilder out = new StringBuilder(String.format("%s\t%s", modelName, dataId));
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
            if (obj instanceof Result) {
                Result other = (Result) obj;
                return modelName.equals(other.modelName) && dataId.equals(other.dataId);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return modelName.hashCode() ^ dataId.hashCode();
        }
    }

    private File file;
    private Map<String, Format> formatMap;
    private Set<Result> results;

    public ResultFile(File file) {
        this.file = file;
        results = new HashSet<>();
        formatMap = new HashMap<>();
    }

    public void addStatistic(String name, Format format) {
        formatMap.put(name, format);
    }

    private Set<String> readHeaderLine(String headerLine) {
        Set<String> stats = new LinkedHashSet<>();
        Scanner lineIn = new Scanner(headerLine);
        lineIn.useDelimiter("\t");
        String token = lineIn.next();
        if (!"Model Name".equals(token)) throw new RuntimeException("Result File Format Exception");
        token = lineIn.next();
        if (!"Fold".equals(token)) throw new RuntimeException("Result File Format Exception");
        while (lineIn.hasNext()) {
            stats.add(lineIn.next());
        }
        return stats;
    }

    public void readResults() {
        try (Scanner fileIn = new Scanner(file)) {
            if (!fileIn.hasNextLine()) return;
            Set<String> fileStats = readHeaderLine(fileIn.nextLine());
            while (fileIn.hasNextLine()) {
                Scanner lineIn = new Scanner(fileIn.nextLine());
                lineIn.useDelimiter("\t");
                String modelName = lineIn.next();
                String foldId = lineIn.next();
                Result r = new Result(modelName, foldId);
                for (String key : fileStats) {
                    Format format = formatMap.get(key);
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
        } catch (FileNotFoundException | NoSuchElementException ignored) {
        } catch (RuntimeException e) {
            Log.warn(e.getMessage());
        }
    }

    public boolean contains(Result r) {
        return results.contains(r);
    }

    private void printHeaderLine(PrintStream out) {
        out.print("Model Name\tFold");
        formatMap.keySet().forEach((statName) -> out.print('\t' + statName));
    }

    public void writeResults() {
        try (PrintStream fileOut = new PrintStream(file)) {
            printHeaderLine(fileOut);
            results.forEach(fileOut::println);
        } catch (FileNotFoundException ignored) {
        }
    }
}
