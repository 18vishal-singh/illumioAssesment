package org.example;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class Main {

    public static void main(String[] args) throws IOException {

        // Input files
        String flowLogFilePath = "flow_logs.txt";
        String lookupTableFilePath = "lookup_table.csv";
        String protocols = "protocols.cvs";

        // Output file
        String outputFilePath = "output.txt";

        // Data structures to store mappings and counts
        Map<String, String> tagMap = new HashMap<>();
        Map<String, Integer> tagCounts = new HashMap<>();
        Map<String, Integer> portProtocolCounts = new HashMap<>();
        Map<Integer,String> protocolMapping = new HashMap<>();

        // Load lookup table into memory
        loadLookupTable(lookupTableFilePath, tagMap);

        //load all protocols.cvs
        loadProtocols(protocolMapping, protocols);

        // Process flow logs
        processFlowLogs(flowLogFilePath, tagMap, tagCounts, portProtocolCounts, protocolMapping);

        // Write output
        writeOutput(outputFilePath, tagCounts, portProtocolCounts);
    }

    private static void loadProtocols(Map<Integer, String> protocolMapping, String protocols) throws IOException {
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(protocols)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[0].trim().toLowerCase();
                int number = Integer.parseInt(parts[1].trim());
                protocolMapping.put(number,name);
            }
        }
    }

    private static void loadLookupTable(String lookupTableFilePath, Map<String, String> tagMappings) throws IOException {
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(lookupTableFilePath)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
            // Skip header line
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String dstPort = parts[0].trim().toLowerCase();
                    String protocol = parts[1].trim().toLowerCase();
                    String tag = parts[2].trim();
                    tagMappings.put(dstPort + ":" + protocol, tag);
                }
                else{
                    System.out.println("Invalid lookup entry");
                }
            }
        }
    }

    private static void processFlowLogs(String flowLogFilePath, Map<String, String> tagMappings,
                                        Map<String, Integer> tagCounts, Map<String, Integer> portProtocolCounts, Map<Integer, String> protocolMapping) throws IOException {

        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(flowLogFilePath)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(" ");
                if (parts.length >= 7 && StringUtils.isNotEmpty(parts[6]) && StringUtils.isNotEmpty(parts[7])) {
                    String dstPort = parts[6].trim().toLowerCase();
                    String protocol = protocolMapping.get(Integer.parseInt(parts[7].trim()));
                    String key = dstPort + ":" + protocol;

                    String tag = tagMappings.getOrDefault(key, "Untagged");
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                    portProtocolCounts.put(dstPort + "," + protocol, portProtocolCounts.getOrDefault(dstPort + "," + protocol, 0) + 1);
                }
            }
        }
    }

    private static void writeOutput(String outputFilePath, Map<String, Integer> tagCounts,
                                    Map<String, Integer> portProtocolCounts) throws IOException {

        try (FileWriter writer = new FileWriter(outputFilePath)) {
            // Write tag counts
            writer.write("Tag Counts:\n");
            writer.write("Tag,Count\n");
            for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }

            writer.write("\n"); // Add an empty line between sections

            // Write port/protocol combination counts
            writer.write("Port/Protocol Combination Counts:\n");
            writer.write("Port,Protocol,Count\n");
            for (Map.Entry<String, Integer> entry : portProtocolCounts.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        }
    }
}