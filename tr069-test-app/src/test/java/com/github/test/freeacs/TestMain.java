package com.github.test.freeacs;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.Sleep;
import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.github.freeacs" })
public class TestMain {

    public static void main(String[] args) {
        // Create Options object
        Options options = new Options();

        // Add testcontainers option
        Option testcontainersOption = new Option(null, "testcontainers", true, "Enable or disable testcontainers");
        testcontainersOption.setRequired(false);
        options.addOption(testcontainersOption);

        // Add server.port option
        Option serverPortOption = new Option(null, "port", true, "Set server port");
        serverPortOption.setRequired(false);
        options.addOption(serverPortOption);

        // Create a parser
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            // Parse the command line arguments
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line options: " + e.getMessage());
            System.exit(1);
        }

        if (cmd.hasOption("testcontainers")) {
            DatabasePropertiesListener.TESTCONTAINERS_ENABLED = Boolean.parseBoolean(cmd.getOptionValue("testcontainers"));
        }

        if (cmd.hasOption("port")) {
            DatabasePropertiesListener.SERVER_PORT = Integer.parseInt(cmd.getOptionValue("port"));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(Sleep::terminateApplication));
        SpringApplication.run(Main.class, args);
    }
}
