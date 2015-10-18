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

import java.util.regex.Pattern;

public class BlockCommand implements Block {
    private static final Pattern REPLACE_X = Pattern.compile("%x");
    private static final Pattern REPLACE_Y = Pattern.compile("%y");
    private static final Pattern REPLACE_Z = Pattern.compile("%z");
    private final String command;
    private final String[] process;

    public BlockCommand(Pair<String, String[]> pair) {
        this(pair.a, pair.b);
    }

    public BlockCommand(String command, String... process) {
        this.command = command;
        this.process = process;
    }

    @Override
    public String[] eval(int x, int y, int z) {
        String[] output = new String[process.length + 1];
        String coords = x + " " + y + ' ' + z;
        output[0] = "/setblock " + coords +
                " minecraft:command_block 0 replace {Command:\"" +
                CommandUtil.escape(command) + "\"}";
        for (int i = 0; i < process.length; i++) {
            output[i + 1] = REPLACE_Z.matcher(REPLACE_Y.matcher(
                    REPLACE_X.matcher(process[0]).replaceAll(String.valueOf(x)))
                    .replaceAll(String.valueOf(y)))
                    .replaceAll(String.valueOf(z));
        }
        return output;
    }
}
