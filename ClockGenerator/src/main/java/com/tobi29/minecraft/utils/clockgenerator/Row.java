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

import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Row implements Section {
    private static final Pattern SPLIT = Pattern.compile(" -> ");
    private final String name;
    private final Optional<Vector3> location;
    private final List<Command> commands = new ArrayList<>();
    private Optional<Command> previous = Optional.empty();

    public Row(String name, Optional<Vector3> location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public int length() {
        return commands.size();
    }

    public Optional<Vector3> getLocation() {
        return location;
    }

    public Stream<Pair<String, String[]>> eval(Generator generator)
            throws GeneratorException {
        List<Pair<String, String[]>> strings = new ArrayList<>(commands.size());
        for (Command command : commands) {
            strings.add(command.eval(generator));
        }
        return strings.stream();
    }

    @Override
    public void append(String line) throws ParserException {
        char initial = line.charAt(0);
        switch (initial) {
            case ':':
                String[] split = SPLIT.split(line.substring(1));
                if (split.length != 2) {
                    throw new ParserException("Row-trigger missing \" -> \"");
                }
                commands.add(new CommandRowTrigger(split[0], split[1], false));
                commands.add(new CommandRowTrigger(split[0], split[1], true));
                previous = Optional.empty();
                break;
            case '!':
                split = SPLIT.split(line.substring(1));
                if (split.length != 2) {
                    throw new ParserException("Event-trigger missing \" -> \"");
                }
                commands.add(new CommandEventTrigger(split[0], split[1]));
                previous = Optional.empty();
                break;
            case '&':
                if (previous.isPresent()) {
                    previous.get().addProcess(line.substring(1));
                } else {
                    throw new ParserException("No command available in row");
                }
                break;
            default:
                Command command = new Command(line);
                commands.add(command);
                previous = Optional.of(command);
                break;
        }
    }

    @Override
    public void end() {
    }
}
