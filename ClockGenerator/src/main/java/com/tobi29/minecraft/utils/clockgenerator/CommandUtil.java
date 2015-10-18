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

import java.util.regex.Pattern;

public class CommandUtil {
    private static final Pattern REPLACE_QUOTE = Pattern.compile("\"");
    private static final Pattern REPLACE_BACKSLASH = Pattern.compile("\\\\");

    public static String escape(String str) {
        return REPLACE_QUOTE
                .matcher(REPLACE_BACKSLASH.matcher(str).replaceAll("\\\\\\\\"))
                .replaceAll("\\\\\"");
    }
}
