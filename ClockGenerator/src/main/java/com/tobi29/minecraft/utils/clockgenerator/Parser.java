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

import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2i;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3i;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

public class Parser {
    private static final Pattern REPLACE_SPLIT = Pattern.compile(" -> ");
    private final Project project;
    private Optional<Section> section = Optional.empty();

    private Parser(Properties properties) throws ProjectException {
        project = new Project(properties);
    }

    public static Project parse(Path path)
            throws ProjectException, IOException {
        Properties properties = new Properties();
        properties
                .load(Files.newInputStream(path.resolve("project.properties")));
        Parser parser = new Parser(properties);
        parser.parseStream(path.resolve("main.txt"));
        parser.section.ifPresent(Section::end);
        return parser.project;
    }

    private String parseStream(Path path) throws IOException {
        System.out.println("Parsing file: " + path);
        BufferedReader reader = Files.newBufferedReader(path);
        String line = reader.readLine();
        StringBuilder lineBuilder = new StringBuilder(4096);
        int i = 1;
        while (line != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                char initial = line.charAt(0);
                switch (initial) {
                    case '#':
                        break;
                    case '§':
                        String name = line.substring(1);
                        lineBuilder
                                .append(parseStream(path.resolveSibling(name)));
                        break;
                    default:
                        int semicolon = line.indexOf(';');
                        int lastSemicolon = 0;
                        while (semicolon >= 0) {
                            if (semicolon >= 0) {
                                lineBuilder.append(line
                                        .substring(lastSemicolon, semicolon));
                                if (lineBuilder.length() > 0) {
                                    parseLine(lineBuilder.toString(), i);
                                    lineBuilder.setLength(0);
                                }
                                lastSemicolon = semicolon + 1;
                            }
                            semicolon = line.indexOf(';', semicolon + 1);
                        }
                        if (lastSemicolon < line.length()) {
                            lineBuilder.append(line
                                    .substring(lastSemicolon, line.length()));
                        }
                        break;
                }
            }
            i++;
            line = reader.readLine();
        }
        return lineBuilder.toString();
    }

    private void parseLine(String line, int i) {
        try {
            char initial = line.charAt(0);
            switch (initial) {
                case '-':
                    section.ifPresent(Section::end);
                    String[] split = line.substring(1).split(" ", 4);
                    Optional<Vector3> location;
                    if (split.length == 4) {
                        try {
                            location = Optional.of(
                                    new Vector3i(Integer.parseInt(split[1]),
                                            Integer.parseInt(split[2]),
                                            Integer.parseInt(split[3])));
                        } catch (NumberFormatException e) {
                            throw new ParserException(
                                    "Invalid row start location");
                        }
                    } else if (split.length == 1) {
                        location = Optional.empty();
                    } else {
                        throw new ParserException("Invalid row start");
                    }
                    section = Optional.of(project.addRow(split[0], location));
                    break;
                case '*':
                    section.ifPresent(Section::end);
                    split = line.substring(1).split(" ", 3);
                    Optional<Vector2> location2;
                    if (split.length == 3) {
                        try {
                            location2 = Optional.of(
                                    new Vector2i(Integer.parseInt(split[1]),
                                            Integer.parseInt(split[2])));
                        } catch (NumberFormatException e) {
                            throw new ParserException(
                                    "Invalid event start location");
                        }
                    } else if (split.length == 1) {
                        location2 = Optional.empty();
                    } else {
                        throw new ParserException("Invalid event start");
                    }
                    section =
                            Optional.of(project.addEvent(split[0], location2));
                    break;
                case '~':
                    split = REPLACE_SPLIT.split(line.substring(1), 2);
                    if (split.length == 2) {
                        project.addReplace(split[0], split[1]);
                    } else {
                        throw new ParserException("Invalid pattern replace");
                    }
                    break;
                default:
                    if (!section.isPresent()) {
                        throw new ParserException("No row or event active");
                    }
                    Section section = this.section.get();
                    section.append(line);
                    break;
            }
        } catch (ParserException e) {
            System.err.println(i + ": " + e.getMessage());
        }
    }
}
