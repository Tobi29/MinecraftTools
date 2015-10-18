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

public class CommandEventTrigger extends Command {
    private final String name;

    public CommandEventTrigger(String command, String name) {
        super(command);
        this.name = name;
    }

    @Override
    public Pair<String, String[]> eval(Generator generator)
            throws GeneratorException {
        Vector3 event = generator.getEvent(name);
        String coords = event.intX() + " " + event.intY() + ' ' + event.intZ();
        Pair<String, String[]> command = super.eval(generator);
        return new Pair<>(command.a + " /setblock " + coords +
                " minecraft:redstone_block", command.b);
    }
}
