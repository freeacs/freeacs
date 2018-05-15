package com.github.freeacs.logparser;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
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
        String currentPayload = null;
        String currentKey = null;
        String format = "%-40s%s";
        while(br.ready()) {
            String line = br.readLine();
            String payload = currentPayload;
            if (line.contains("Conversation - ==============")) {
                if (currentKey != null) {
                    String methodName = extractMethodName(currentPayload);
                    String newKey = currentKey.replaceAll("\\[.*?\\]", "").replace("INFO  Conversation", String.format(format, methodName, ""));
                    System.out.println(newKey);
                    payload = payload.replaceAll("\n", "").replaceAll("\r", "");
                    bw.append(newKey);
                    bw.newLine();
                    bw.append(payload);
                    bw.newLine();
                }
                currentPayload = "";
                currentKey = line;
            } else {
                currentPayload += line;
            }
        }
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
