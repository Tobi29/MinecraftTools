/*
 * Copyright 2012-2015 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tobi29.minecraft.utils.clockgenerator;

import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClockGenerator {
    private static final String DOT = "\u001B[34;1m::\u001B[0m ";

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        options.addOption("h", "help", false, "Print this text and exit");
        options.addOption("v", "version", false, "Print version and exit");
        options.addOption("o", "output", true, "Output file");
        DefaultParser parser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(255);
            return;
        }
        if (commandLine.hasOption('h')) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("clock-generator", options);
            System.exit(0);
        }
        if (commandLine.hasOption('v')) {
            System.out.println("0.Something.Something_Whatever");
            System.exit(0);
        }
        Path path = Paths.get(".");
        Path output =
                path.resolve(commandLine.getOptionValue('o', "output.txt"));
        try {
            build(path, output);
        } catch (ProjectException e) {
            System.err
                    .println("Failed to configure project: " + e.getMessage());
        } catch (GeneratorException e) {
            System.err.println("Failed to generate project: " + e.getMessage());
        }
    }

    private static void build(Path path, Path output)
            throws ProjectException, GeneratorException, IOException {
        System.out.println(DOT + "Parsing project");
        Project project = Parser.parse(path);
        Generator generator = project.createGenerator();
        System.out.println(DOT + "Layout rows and events");
        project.generate(generator);
        System.out.println(DOT + "Generate blocks");
        Block[][][] blocks = generator.generate();
        Block[][][] eventsBlocks = generator.generateEvents();
        Writer writer = project.createWriter();
        Writer eventsWriter = project.createEventsWriter();
        try (BufferedWriter fileWriter = Files.newBufferedWriter(output)) {
            System.out.println(DOT + "Writing rows into output");
            writer.write(fileWriter, blocks);
            System.out.println(DOT + "Writing events into output");
            eventsWriter.write(fileWriter, eventsBlocks);
        }
        System.out.println(DOT + "Done");
    }
}
