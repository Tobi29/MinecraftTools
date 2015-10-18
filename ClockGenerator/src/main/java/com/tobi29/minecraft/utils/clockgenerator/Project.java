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

import org.tobi29.scapes.engine.utils.math.Face;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Project {
    private final List<Function<String, String>> replaces = new ArrayList<>();
    private final Map<String, Row> rows = new ConcurrentHashMap<>();
    private final Map<String, Event> events = new ConcurrentHashMap<>();
    private final Vector3 start, eventsStart;
    private final int rowCount, rowLength, shelveCount, shelveHeight,
            eventsRows, eventsShelves, eventsLength;
    private final boolean flatShelves;
    private final Face eventsFace;

    public Project(Properties properties) throws ProjectException {
        String startStr = properties.getProperty("start", "");
        String[] startSplit = startStr.split(" ");
        if (startSplit.length != 3) {
            throw new ProjectException("Invalid start");
        }
        try {
            start = new Vector3i(Integer.parseInt(startSplit[0]),
                    Integer.parseInt(startSplit[1]),
                    Integer.parseInt(startSplit[2]));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid start");
        }
        try {
            rowCount = Integer.parseInt(properties.getProperty("rows", ""));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid rows");
        }
        try {
            rowLength =
                    Integer.parseInt(properties.getProperty("row-length", ""));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid row-length");
        }
        try {
            shelveCount =
                    Integer.parseInt(properties.getProperty("shelves", ""));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid shelves");
        }
        try {
            shelveHeight = Integer.parseInt(
                    properties.getProperty("shelve-height", ""));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid shelve-height");
        }
        try {
            flatShelves = Boolean.parseBoolean(
                    properties.getProperty("flat-shelves", "false"));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid shelve-height");
        }
        String eventsStartStr = properties.getProperty("events-start", "");
        String[] eventsStartSplit = eventsStartStr.split(" ");
        if (eventsStartSplit.length != 3) {
            throw new ProjectException("Invalid events-start");
        }
        try {
            eventsStart = new Vector3i(Integer.parseInt(eventsStartSplit[0]),
                    Integer.parseInt(eventsStartSplit[1]),
                    Integer.parseInt(eventsStartSplit[2]));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid events-start");
        }
        eventsFace =
                Face.valueOf(properties.getProperty("events-face", "NONE"));
        if (eventsFace == Face.NONE) {
            throw new ProjectException("Invalid events-face");
        }
        try {
            eventsRows =
                    Integer.parseInt(properties.getProperty("events-rows", ""));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid events-rows");
        }
        try {
            eventsShelves = Integer.parseInt(
                    properties.getProperty("events-shelves", ""));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid events-shelves");
        }
        try {
            eventsLength = Integer.parseInt(
                    properties.getProperty("events-length", ""));
        } catch (NumberFormatException e) {
            throw new ProjectException("Invalid events-length");
        }
    }

    public Row addRow(String name, Optional<Vector3> location)
            throws ParserException {
        if (rows.containsKey(name)) {
            throw new ParserException("Duplicate row: " + name);
        }
        Row row = new Row(name, location);
        rows.put(name, row);
        return row;
    }

    public Event addEvent(String name, Optional<Vector2> location)
            throws ParserException {
        if (events.containsKey(name)) {
            throw new ParserException("Duplicate event: " + name);
        }
        Event event = new Event(name, location);
        events.put(name, event);
        return event;
    }

    public void addReplace(String pattern, String str) {
        Pattern compiled = Pattern.compile(pattern);
        replaces.add(0, command -> compiled.matcher(command).replaceAll(str));
    }

    public Generator createGenerator() {
        return new Generator(rowCount, shelveCount, rowLength, shelveHeight,
                flatShelves, start, eventsStart, eventsFace, eventsRows,
                eventsShelves, eventsLength, replaces);
    }

    public void generate(Generator generator) throws GeneratorException {
        Iterator<Row> rowIterator =
                rows.values().stream().sorted((row1, row2) -> {
                    Optional<Vector3> location1 = row1.getLocation();
                    Optional<Vector3> location2 = row2.getLocation();
                    if (location1.isPresent() && !location2.isPresent()) {
                        return -1;
                    }
                    if (location2.isPresent() && !location1.isPresent()) {
                        return 1;
                    }
                    if (location1.isPresent()) {
                        return 0;
                    }
                    int length1 = row1.length();
                    int length2 = row2.length();
                    if (length1 > length2) {
                        return -1;
                    }
                    if (length2 > length1) {
                        return 1;
                    }
                    return 0;
                }).iterator();
        while (rowIterator.hasNext()) {
            generator.addRow(rowIterator.next());
        }
        Iterator<Event> eventIterator =
                events.values().stream().sorted((event1, event2) -> {
                    Optional<Vector2> location1 = event1.getLocation();
                    Optional<Vector2> location2 = event2.getLocation();
                    if (location1.isPresent() && !location2.isPresent()) {
                        return -1;
                    }
                    if (location2.isPresent() && !location1.isPresent()) {
                        return 1;
                    }
                    return 0;
                }).iterator();
        while (eventIterator.hasNext()) {
            generator.addEvent(eventIterator.next());
        }
    }

    public Writer createWriter() {
        return new Writer(start);
    }

    public Writer createEventsWriter() {
        return new Writer(eventsStart);
    }
}
