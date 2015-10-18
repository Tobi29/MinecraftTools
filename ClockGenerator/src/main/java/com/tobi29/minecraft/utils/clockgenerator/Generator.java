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
import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.Face;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3i;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Generator {
    private final boolean[][] used;
    private final Row[][] rows;
    private final int rowLength, shelveHeight, eventsRows, eventsShelves,
            eventsLength;
    private final boolean flatShelves;
    private final Vector3 start, eventsStart;
    private final List<Function<String, String>> replaces;
    private final Map<String, Pair<Vector3, Vector3>> rowLocations =
            new ConcurrentHashMap<>();
    private final Map<String, Vector3> eventsLocations =
            new ConcurrentHashMap<>();
    private final Face eventsFace;
    private final Event[][] events;

    public Generator(int rowCount, int shelveCount, int rowLength,
            int shelveHeight, boolean flatShelves, Vector3 start,
            Vector3 eventsStart, Face eventsFace, int eventsRows,
            int eventsShelves, int eventsLength,
            List<Function<String, String>> replaces) {
        this.eventsRows = eventsRows;
        this.eventsShelves = eventsShelves;
        this.eventsLength = eventsLength;
        used = new boolean[shelveCount][rowCount];
        rows = new Row[shelveCount][rowCount];
        this.rowLength = rowLength;
        this.shelveHeight = shelveHeight;
        this.flatShelves = flatShelves;
        this.start = start;
        this.eventsStart = eventsStart;
        this.eventsFace = eventsFace;
        events = new Event[eventsShelves][eventsRows];
        this.replaces = replaces;
    }

    public void addRow(Row row) throws GeneratorException {
        int required;
        int xx = -1, yy = -1;
        Optional<Vector3> location = row.getLocation();
        if (location.isPresent()) {
            xx = location.get().intX();
            yy = location.get().intY();
            required = location.get().intZ();
        } else {
            required = (row.length() - 1) / rowLength >> 1;
            for (int y = 0; y < used.length && xx < 0; y++) {
                for (int x = 0; x < used[0].length && xx < 0; x++) {
                    if (x + required < used[0].length) {
                        boolean flag = true;
                        for (int i = 0; i <= required; i++) {
                            if (used[y][x + i]) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            xx = x;
                            yy = y;
                        }
                    }
                }
            }
        }
        if (xx < 0) {
            throw new GeneratorException("Unable to fit row: " + row.getName());
        }
        for (int i = 0; i <= required; i++) {
            if (used[yy][xx + i]) {
                throw new GeneratorException(
                        "Overlapping row: " + row.getName());
            }
            used[yy][xx + i] = true;
        }
        rows[yy][xx] = row;
        int xxx, yyy;
        if (flatShelves) {
            xxx = start.intX() + yy * shelveHeight;
            yyy = start.intY();
        } else {
            xxx = start.intX();
            yyy = start.intY() + yy * shelveHeight;
        }
        int zzz = start.intZ() + xx;
        rowLocations.put(row.getName(),
                new Pair<>(new Vector3i(xxx, yyy + 1, zzz),
                        new Vector3i(xxx + rowLength, yyy + 1,
                                zzz + required)));
    }

    public void addEvent(Event event) throws GeneratorException {
        int xx = -1, yy = -1;
        Optional<Vector2> location = event.getLocation();
        if (location.isPresent()) {
            xx = location.get().intX();
            yy = location.get().intY();
        } else {
            for (int y = 0; y < events.length && xx < 0; y++) {
                for (int x = 0; x < events[0].length && xx < 0; x++) {
                    if (events[y][x] == null) {
                        xx = x;
                        yy = y;
                    }
                }
            }
        }
        if (xx < 0) {
            throw new GeneratorException(
                    "Unable to fit event: " + event.getName());
        }
        if (events[yy][xx] != null) {
            throw new GeneratorException(
                    "Overlapping event: " + event.getName());
        }
        events[yy][xx] = event;
        int xxx = eventsStart.intX();
        int yyy = eventsStart.intY() + yy * 3 + 1;
        int zzz = eventsStart.intZ();
        if (eventsFace == Face.NORTH || eventsFace == Face.SOUTH) {
            xxx += xx << 1;
        } else if (eventsFace == Face.EAST || eventsFace == Face.WEST) {
            zzz += xx << 1;
        }
        eventsLocations.put(event.getName(), new Vector3i(xxx, yyy, zzz));
    }

    public Pair<Vector3, Vector3> getRow(String name)
            throws GeneratorException {
        Pair<Vector3, Vector3> row = rowLocations.get(name);
        if (row == null) {
            throw new GeneratorException("Unknown row: " + name);
        }
        return row;
    }

    public Vector3 getEvent(String name) throws GeneratorException {
        Vector3 event = eventsLocations.get(name);
        if (event == null) {
            throw new GeneratorException("Unknown event: " + name);
        }
        return event;
    }

    public Block[][][] generate() throws GeneratorException {
        Block[][][] blocks;
        if (flatShelves) {
            blocks = new Block[used.length * shelveHeight][4][used[0].length];
        } else {
            blocks = new Block[rowLength][(used.length - 1) * shelveHeight +
                    4][used[0].length];
        }
        for (int y = 0; y < used.length; y++) {
            for (int z = 0; z < used[0].length; z++) {
                Row row = rows[y][z];
                if (row != null) {
                    System.out.println("Generating row: " + row.getName());
                    if (flatShelves) {
                        blocks[y * shelveHeight][3][z] =
                                new BlockSign("Row:\n" + row.getName(),
                                        Face.NONE);
                    } else {
                        blocks[0][y * shelveHeight + 3][z] =
                                new BlockSign("Row:\n" + row.getName(),
                                        Face.NONE);
                    }
                    Iterator<Pair<String, String[]>> iterator =
                            row.eval(this).iterator();
                    int xx = 0, yy = 0, zz = 0;
                    while (iterator.hasNext()) {
                        Pair<String, String[]> command = iterator.next();
                        int xxx, yyy;
                        if (flatShelves) {
                            xxx = y * shelveHeight + xx;
                            yyy = yy;
                        } else {
                            xxx = xx;
                            yyy = y * shelveHeight + yy;
                        }
                        int zzz = z + zz;
                        blocks[xxx][yyy][zzz] = new BlockCommand(command);
                        if (yy == 0) {
                            blocks[xxx][yyy + 1][zzz] =
                                    new BlockFiller("minecraft:stone", 0);
                            yy = 2;
                        } else {
                            yy = 0;
                            xx++;
                            if (xx >= rowLength) {
                                xx = 0;
                                zz++;
                            }
                        }
                    }
                }
            }
        }
        return blocks;
    }

    public Block[][][] generateEvents() throws GeneratorException {
        Block[][][] blocks;
        if (eventsFace == Face.NORTH || eventsFace == Face.SOUTH) {
            blocks =
                    new Block[eventsRows << 1][eventsShelves * 3][eventsLength];
        } else {
            blocks =
                    new Block[eventsLength][eventsShelves * 3][eventsRows << 1];
        }
        for (int y = 0; y < events.length; y++) {
            for (int x = 0; x < events[0].length; x++) {
                Event event = events[y][x];
                if (event != null) {
                    System.out.println("Generating event: " + event.getName());
                    int xx = x << 1;
                    int yy = y * 3;
                    Iterator<Triple<Block, Block, Block>> iterator =
                            event.eval(this, eventsFace).iterator();
                    int z = 0;
                    while (iterator.hasNext()) {
                        Triple<Block, Block, Block> entry = iterator.next();
                        if (FastMath.abs(z) >= eventsLength) {
                            throw new GeneratorException(
                                    "Event too long to fit: " +
                                            event.getName());
                        }
                        int xxx, zzz;
                        if (eventsFace == Face.NORTH ||
                                eventsFace == Face.SOUTH) {
                            xxx = xx;
                            zzz = z;
                        } else {
                            xxx = z;
                            zzz = xx;
                        }
                        blocks[xxx][yy + 2][zzz] = entry.a;
                        blocks[xxx][yy + 1][zzz] = entry.b;
                        blocks[xxx][yy][zzz] = entry.c;
                        if (eventsFace == Face.EAST ||
                                eventsFace == Face.SOUTH) {
                            z++;
                        } else {
                            z--;
                        }
                    }
                }
            }
        }
        return blocks;
    }

    public String preprocess(String str) {
        for (Function<String, String> replace : replaces) {
            str = replace.apply(str);
        }
        return str;
    }
}
