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

public class CommandRowTrigger extends Command {
    private final String name;
    private final boolean second;

    public CommandRowTrigger(String command, String name, boolean second) {
        super(command);
        this.name = name;
        this.second = second;
    }

    @Override
    public Pair<String, String[]> eval(Generator generator)
            throws GeneratorException {
        String suffix =
                second ? " minecraft:stone 0 replace minecraft:redstone_block" :
                        " minecraft:redstone_block 0 replace minecraft:stone";
        Pair<Vector3, Vector3> row = generator.getRow(name);
        String coords =
                row.a.intX() + " " + row.a.intY() + ' ' + row.a.intZ() + ' ' +
                        row.b.intX() + ' ' + row.b.intY() + ' ' + row.b.intZ();
        Pair<String, String[]> command = super.eval(generator);
        return new Pair<>(command.a + " /fill " + coords + suffix, command.b);
    }
}
