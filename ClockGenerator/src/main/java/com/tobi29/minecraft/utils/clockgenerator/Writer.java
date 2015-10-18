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

import org.tobi29.scapes.engine.utils.math.vector.Vector3;

import java.io.BufferedWriter;
import java.io.IOException;

public class Writer {
    private final Vector3 start;

    public Writer(Vector3 start) {
        this.start = start;
    }

    public void write(BufferedWriter writer, Block[][][] blocks)
            throws IOException {
        int sx = start.intX();
        int sy = start.intY();
        int sz = start.intZ();
        for (int x = 0; x < blocks.length; x++) {
            writer.write("/fill " + (sx + x) + ' ' + sy + ' ' + sz + ' ' +
                    (sx + x) + ' ' + (sy + blocks[0].length) + ' ' +
                    (sz + blocks[0][0].length) + " minecraft:air\n");
        }
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[0].length; y++) {
                for (int z = 0; z < blocks[0][0].length; z++) {
                    Block block = blocks[x][y][z];
                    if (block != null) {
                        String[] command = block.eval(sx + x, sy + y, sz + z);
                        for (String str : command) {
                            writer.write(str + '\n');
                        }
                    }
                }
            }
        }
    }
}
