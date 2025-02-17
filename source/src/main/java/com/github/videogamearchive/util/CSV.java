package com.github.videogamearchive.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public final class CSV {

    private CSV() {
        // Private constructor to make clear that is a non-instantiable utility class
    }

    public static List<CSVRecord> read(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in);
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();
        CSVParser parse = format.parse(reader);
        List<CSVRecord> records = parse.getRecords();
        parse.close();
        return records;
    }

    public static void write(Path file, String[] headers, List<String[]> rows) throws IOException {
        final CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(headers).build();
        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file.toFile(), StandardCharsets.UTF_8), csvFormat)) {
            csvPrinter.printRecords(rows);
        }
    }

    public static String toString(String[] records) {
        if (records == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object record:records) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(record.toString());
        }
        return builder.toString();
    }

    public static String toString(List<?> strings) {
        if (strings == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object string:strings) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(string.toString().trim());
        }
        return builder.toString();
    }

    public static String toString(Object object) {
        if (object == null) {
            return "";
        } else if (object instanceof Boolean) {
            return Boolean.toString((Boolean) object);
        } else if (object instanceof Integer) {
            return Integer.toString((Integer) object);
        } else if (object instanceof Long) {
            return Long.toString((Long) object);
        } else if (object instanceof Enum<?>) {
            return object.toString();
        } else if (object instanceof String) {
            return object.toString();
        } else {
            throw new RuntimeException("object type not supported: " + object.getClass().getSimpleName());
        }
    }
}
