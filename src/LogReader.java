import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader {
    private final static String LOGS_DIRECTORY_PATH = "D:/log";
    private final static String MIN_LOCAL_DATE_TIME = "0001-01-01 00:00:00,000";
    private final static String MAX_LOCAL_DATE_TIME = "9999-12-31 23:59:59,999";

    public static void main(String[] args) {
        File directory = new File(LOGS_DIRECTORY_PATH);
        if (!directory.exists() || !directory.isDirectory()) return;

        List<File> sortedFileListByLastModifiedDescending = Arrays.stream(Objects.requireNonNull(directory.listFiles())).sorted(Comparator.comparing(File::lastModified).reversed()).toList();
        interprateFiles(sortedFileListByLastModifiedDescending);
    }

    public static void interprateFiles(List<File> sortedFileListByLastModifiedDescending) {
        // regex checks whether string cointains date, time, severity and library
        String pattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3} .*? \\[.*?\\]";
        Pattern datePattern = Pattern.compile(pattern);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
        HashMap<String, Integer> severitiesEmptyCountersHashMap = new HashMap<>();
        severitiesEmptyCountersHashMap.put("OFF", 0);
        severitiesEmptyCountersHashMap.put("FATAL", 0);
        severitiesEmptyCountersHashMap.put("ERROR", 0);
        severitiesEmptyCountersHashMap.put("WARN", 0);
        severitiesEmptyCountersHashMap.put("INFO", 0);
        severitiesEmptyCountersHashMap.put("DEBUG", 0);
        severitiesEmptyCountersHashMap.put("TRACE", 0);

        // loading files by last modified descending
        for (File file : sortedFileListByLastModifiedDescending) {
            boolean isThereAtLeastOneLog = false;
            long startTime = System.currentTimeMillis();
            System.out.println("File name:\t\t\t\t\t\t\t" + file.getName());
            int uniqueLibrariesCounter = 0;
            LocalDateTime maxDateTime = LocalDateTime.parse(MIN_LOCAL_DATE_TIME, formatter);
            LocalDateTime minDateTime = LocalDateTime.parse(MAX_LOCAL_DATE_TIME, formatter);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                HashSet<String> librariesSet = new HashSet<>();
                HashMap<String, Integer> severitiesHashMap = new HashMap<>(severitiesEmptyCountersHashMap);

                // reading lines from file
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = datePattern.matcher(line);
                    if (matcher.find()) {
                        // strings array [0]=date [1]=time [2]=severityLevel [3]=library
                        String[] logArray = line.split("]")[0].replaceAll("\\s+\\[", " ").split(" ");
                        String date = logArray[0];
                        String time = logArray[1];
                        String severityLevel = logArray[2];
                        String libraryName = logArray[3];

                        // number of logs grouped by severity level
                        if(!severitiesHashMap.containsKey(severityLevel)) continue;
                        else severitiesHashMap.put(severityLevel, severitiesHashMap.get(severityLevel)+1);

                        // updating earliest and latest date
                        LocalDateTime parsedDateTime = LocalDateTime.parse(date + " " + time, formatter);
                        if (parsedDateTime.compareTo(maxDateTime) > 0) maxDateTime = parsedDateTime;
                        if (parsedDateTime.compareTo(minDateTime) < 0) minDateTime = parsedDateTime;

                        // unique libraries
                        librariesSet.add(libraryName);
                        uniqueLibrariesCounter = librariesSet.size();

                        isThereAtLeastOneLog = true;
                    }
                }
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;

                // print result of one file
                printResult(minDateTime, maxDateTime, elapsedTime, uniqueLibrariesCounter, severitiesHashMap, isThereAtLeastOneLog);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printResult(LocalDateTime minDateTime, LocalDateTime maxDateTime, long elapsedTime, int uniqueLibrariesCounter, HashMap<String, Integer> severitiesHashMap, boolean isThereAtLeastOneLog) {
        if(!isThereAtLeastOneLog) {
            System.out.println("No logs found in this file");
            return;
        }
//        System.out.println("najwiekszy: " + maxDateTime);
//        System.out.println("najmniejszy: " + minDateTime);
        System.out.println("interpreting time:\t\t\t\t\t" + elapsedTime + "ms");

        Duration timeBetween = Duration.between(minDateTime, maxDateTime);
        long days = timeBetween.toDays();
        long hours = timeBetween.toHours() - days * 24;
        long minutes = timeBetween.toMinutes() - (days * 24 * 60 + hours * 60);
        long seconds = timeBetween.getSeconds() - (days * 24 * 60 * 60 + hours * 60 * 60 + minutes * 60);
        long miliseconds = timeBetween.getNano() / 1000000;
        System.out.println("time between first and last log:\t" + days + "d " + hours + "h " + minutes + "m " + seconds + "s " + miliseconds + "ms");
        System.out.println("unique libraries:\t\t\t\t\t" + uniqueLibrariesCounter);

        int sum = 0, errors = 0, ratio = 0;

        // number of severities
        for (Integer value : severitiesHashMap.values())
            sum += value;

        // summaries all severites on the ERROR level or higher
        for (String key : severitiesHashMap.keySet())
            if(key.equals("ERROR") || key.equals("FATAL") || key.equals("OFF"))
                errors +=  severitiesHashMap.get(key);

        if(sum!=0) ratio = (int)(100 * ((double)errors/(double)sum));
        System.out.println("Errors and higher ratio:\t\t\t" + ratio + "%");

        severitiesHashMap.forEach((key, value) -> System.out.println(key + ":\t" + value));
        System.out.println();
    }
}