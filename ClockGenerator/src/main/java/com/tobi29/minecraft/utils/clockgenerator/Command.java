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

import java.util.ArrayList;
import java.util.List;

public class Command {
    protected final String command;
    protected final List<String> process = new ArrayList<>();

    public Command(String command) {
        this.command = command;
    }

    public void addProcess(String str) {
        process.add(str);
    }

    public Pair<String, String[]> eval(Generator generator)
            throws GeneratorException {
        String[] array = new String[process.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = generator.preprocess(process.get(i));
        }
        return new Pair<>(generator.preprocess(command), array);
    }
}
