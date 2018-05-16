package com.github.freeacs.logparser;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final Pattern methodNamePattern = Pattern.compile(":Body.*>\\s*<(cwmp|soapenv|SOAP-ENV):(\\w+)(>|/>)", Pattern.DOTALL);

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java -jar logparser.jar /path/to/conversation.log /path/to/modified.log");
            return;
        }
        String fileName = args[0];
        BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
        String outputFile = args[1];
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
        StringBuilder currentPayload = new StringBuilder();
        String currentKey = null;
        String format = "%-40s%s";
        System.out.print("converting, please wait...");
        while(br.ready()) {
            String line = br.readLine();
            String payload = currentPayload.toString();
            if (line.contains("Conversation - ==============")) {
                if (currentKey != null) {
                    String methodName = extractMethodName(payload);
                    String newKey = currentKey.replaceAll("\\[.*?\\]", "").replace("INFO  Conversation", String.format(format, methodName, ""));
                    String newPayload = payload.replaceAll("\n", "").replaceAll("\r", "");
                    bw.append(newKey);
                    bw.newLine();
                    bw.append(newPayload);
                    bw.newLine();
                }
                currentPayload = new StringBuilder();
                currentKey = line;
            } else {
                currentPayload.append(line);
            }
        }
        System.out.print("Done");
        System.out.println();
        br.close();
        bw.close();
    }

    private static String extractMethodName(String s) {
        Matcher matcher = methodNamePattern.matcher(s);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "Empty";
    }
}
