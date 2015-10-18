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

import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.Face;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Event implements Section {
    private static final Pattern SPLIT = Pattern.compile(" -> ");
    private final String name;
    private final Optional<Vector2> location;
    private final List<Entry> entries = new ArrayList<>();
    private Optional<EntryTrigger> trigger = Optional.empty();
    private Optional<Command> stash = Optional.empty();
    private Optional<Command> previous = Optional.empty();

    public Event(String name, Optional<Vector2> location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Optional<Vector2> getLocation() {
        return location;
    }

    @Override
    public void append(String line) throws ParserException {
        char initial = line.charAt(0);
        switch (initial) {
            case '!':
                String[] split = SPLIT.split(line.substring(1));
                if (split.length != 2) {
                    throw new ParserException("Event-trigger missing \" -> \"");
                }
                add(new CommandEventTrigger(split[0], split[1]));
                previous = Optional.empty();
                break;
            case '&':
                if (previous.isPresent()) {
                    previous.get().addProcess(line.substring(1));
                } else {
                    throw new ParserException("No command available in event");
                }
                break;
            default:
                if (line.startsWith("delay ")) {
                    String amount = line.substring(6);
                    try {
                        if (!trigger.isPresent()) {
                            entries.add(new EntryCommand(new Command("/setblock ~ ~1 ~ minecraft:stone"),
                                    Optional.empty()));
                        }
                        if (stash.isPresent()) {
                            entries.add(new EntryCommand(stash.get(),
                                    Optional.empty()));
                            stash = Optional.empty();
                        }
                        entries.add(
                                new EntryRepeater(Integer.parseInt(amount)));
                        trigger = Optional.empty();
                    } catch (NumberFormatException e) {
                        throw new ParserException("Invalid number: " + amount);
                    }
                } else {
                    Command command = new Command(line);
                    add(command);
                    previous = Optional.of(command);
                }
                break;
        }
    }

    private void add(Command command) {
        Optional<Command> optional = Optional.of(command);
        if (stash.isPresent()) {
            entries.add(new EntryCommand(stash.get(), optional));
            stash = Optional.empty();
        } else {
            stash = optional;
            if (!trigger.isPresent()) {
                EntryTrigger trigger = new EntryTrigger();
                entries.add(trigger);
                this.trigger = Optional.of(trigger);
            }
            trigger.get().length++;
        }
    }

    @Override
    public void end() {
        if (stash.isPresent()) {
            entries.add(new EntryCommand(stash.get(), Optional.empty()));
            stash = Optional.empty();
        }
    }

    public Stream<Triple<Block, Block, Block>> eval(Generator generator,
            Face face) throws GeneratorException {
        List<Triple<Block, Block, Block>> triples =
                new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            triples.add(entry.eval(generator, face));
        }
        return triples.stream();
    }

    private interface Entry {
        Triple<Block, Block, Block> eval(Generator generator, Face face)
                throws GeneratorException;
    }

    private static class EntryCommand implements Entry {
        private final Command command1;
        private final Optional<Command> command2;

        private EntryCommand(Command command1, Optional<Command> command2) {
            this.command1 = command1;
            this.command2 = command2;
        }

        @Override
        public Triple<Block, Block, Block> eval(Generator generator, Face face)
                throws GeneratorException {
            Block cb1 = new BlockCommand(command1.eval(generator));
            Block redstone = new BlockFiller("minecraft:stone", 0);
            Block cb2;
            if (command2.isPresent()) {
                cb2 = new BlockCommand(command2.get().eval(generator));
            } else {
                cb2 = null;
            }
            return new Triple<>(cb2, redstone, cb1);
        }
    }

    private static class EntryRepeater implements Entry {
        private final int delay;

        private EntryRepeater(int delay) {
            this.delay = delay;
        }

        @Override
        public Triple<Block, Block, Block> eval(Generator generator, Face face)
                throws GeneratorException {
            Block repeater = new BlockFiller("minecraft:unpowered_repeater",
                    face.getData() - 2 + (delay - 1 << 2));
            Block block = new BlockFiller("minecraft:quartz_block", 0);
            return new Triple<>(null, repeater, block);
        }
    }

    private static class EntryTrigger implements Entry {
        private int length;

        @Override
        public Triple<Block, Block, Block> eval(Generator generator, Face face)
                throws GeneratorException {
            int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
            switch (face) {
                case NORTH:
                    y1 = -1;
                    y2 = -length;
                    break;
                case EAST:
                    x1 = 1;
                    x2 = length;
                    break;
                case SOUTH:
                    y1 = 1;
                    y2 = length;
                    break;
                case WEST:
                    x1 = -1;
                    x2 = -length;
                    break;
            }
            Block cb1 = new BlockCommand(
                    "/fill ~" + x1 + " ~1 ~" + y1 + " ~" + x2 + " ~1 ~" + y2 +
                            " minecraft:redstone_block");
            Block redstone = new BlockFiller("minecraft:stone", 0);
            Block cb2 =
                    new BlockCommand("/fill ~ ~-1 ~ ~" + x2 + " ~-1 ~" + y2 +
                            " minecraft:stone");
            return new Triple<>(cb2, redstone, cb1);
        }
    }
}
