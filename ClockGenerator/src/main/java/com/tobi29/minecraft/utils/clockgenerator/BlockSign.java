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

import java.util.Arrays;

public class BlockSign implements Block {
    private final String text;
    private final Face face;

    public BlockSign(String text, Face face) {
        this.text = text;
        this.face = face;
    }

    @Override
    public String[] eval(int x, int y, int z) {
        String[] split = text.split("\n");
        String[] text = new String[4];
        Arrays.fill(text, "");
        if (split.length >= 1) {
            text[0] = split[0];
        }
        if (split.length >= 2) {
            text[1] = split[1];
        }
        if (split.length >= 3) {
            text[2] = split[2];
        }
        if (split.length >= 4) {
            text[3] = split[3];
        }
        String type;
        int data;
        String coordsBase;
        switch (face) {
            case NORTH:
                type = "wall";
                data = 3;
                coordsBase = x + " " + y + ' ' + (z - 1);
                break;
            case EAST:
                type = "wall";
                data = 4;
                coordsBase = (x + 1) + " " + y + ' ' + z;
                break;
            case SOUTH:
                type = "wall";
                data = 2;
                coordsBase = x + " " + y + ' ' + (z + 1);
                break;
            case WEST:
                type = "wall";
                data = 5;
                coordsBase = (x - 1) + " " + y + ' ' + z;
                break;
            default:
                type = "standing";
                data = 4;
                coordsBase = x + " " + (y - 1) + ' ' + z;
                break;
        }
        String coords = x + " " + y + ' ' + z;
        return new String[]{
                "/setblock " + coords + " minecraft:" + type + "_sign " + data +
                        " replace {Text1:\"" +
                        CommandUtil.escape(text[0]) + "\",Text2:\"" +
                        CommandUtil.escape(text[1]) + "\",Text3:\"" +
                        CommandUtil.escape(text[2]) + "\",Text4:\"" +
                        CommandUtil.escape(text[3]) + "\"}",
                "/setblock " + coordsBase + " minecraft:fence 0 keep"};
    }
}
