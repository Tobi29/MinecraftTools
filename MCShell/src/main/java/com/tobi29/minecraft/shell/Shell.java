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
package com.tobi29.minecraft.shell;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Shell {
    private static final int PIPE_BUFFER = 1 << 10 << 5;
    private static final int STREAM_BUFFER = 1 << 10 << 2;

    public static void main(String[] args) throws IOException {
        // Intercept IO
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        InputStream stdin = System.in;
        BufferedReader stdinReader =
                new BufferedReader(new InputStreamReader(stdin));
        PipedInputStream mcout = new PipedInputStream(PIPE_BUFFER);
        PipedInputStream mcerr = new PipedInputStream(PIPE_BUFFER);
        PipedOutputStream mcin = new PipedOutputStream();
        System.setOut(new PrintStream(new PipedOutputStream(mcout)));
        System.setErr(new PrintStream(new PipedOutputStream(mcerr)));
        System.setIn(new PipedInputStream(mcin, PIPE_BUFFER));

        // Load Main Class
        Method mainMethod;
        try {
            Class<?> minecraftServer = ClassLoader.getSystemClassLoader()
                    .loadClass("net.minecraft.server.MinecraftServer");
            mainMethod = minecraftServer.getMethod("main", String[].class);
        } catch (ClassNotFoundException e) {
            stderr.println(
                    "Failed to load MinecraftServer class, please check if a " +
                            "valid Minecraft Server jar was passed as one of " +
                            "the arguments.");
            System.exit(11);
            return;
        } catch (NoSuchMethodException e) {
            stderr.println(
                    "Failed to get main method, please check if a valid Minec" +
                            "raft Server jar was passed as one of the argumen" +
                            "ts.");
            System.exit(12);
            return;
        } catch (Throwable e) {
            stderr.println("Unknown error on startup: " + e.toString());
            System.exit(10);
            return;
        }

        // Route streams in daemon thread
        Thread thread = new Thread(() -> {
            byte[] buffer = new byte[STREAM_BUFFER];
            while (true) {
                try {
                    boolean active = false;
                    if (stdin.available() > 0) {
                        String str = stdinReader.readLine();
                        if (str != null) {
                            if (str.startsWith("#execfile ")) {
                                String[] split = str.split(" ", 2);
                                if (split.length == 2) {
                                    Path file = Paths.get(split[1]);
                                    try (InputStream streamIn = Files
                                            .newInputStream(file)) {
                                        int length = streamIn.read(buffer);
                                        while (length >= 0) {
                                            mcin.write(buffer, 0, length);
                                            length = streamIn.read(buffer);
                                        }
                                        mcin.flush();
                                    } catch (IOException e) {
                                        stderr.println(e.toString());
                                    }
                                } else {
                                    stderr.println("Failed to read filename");
                                }
                            } else {
                                mcin.write(str.getBytes());
                                mcin.write('\n');
                                mcin.flush();
                            }
                        }
                        active = true;
                    }
                    int available = mcout.available();
                    if (available > 0) {
                        while (mcout.available() > STREAM_BUFFER) {
                            mcout.skip(STREAM_BUFFER);
                            stdout.println("Skipping data...");
                        }
                        int length = mcout.read(buffer);
                        if (length > 0) {
                            stdout.write(buffer, 0, length);
                        }
                        active = true;
                    }
                    available = mcerr.available();
                    if (available > 0) {
                        while (mcerr.available() > STREAM_BUFFER) {
                            mcerr.skip(STREAM_BUFFER);
                            stderr.println("Skipping data...");
                        }
                        int length = mcerr.read(buffer);
                        if (length > 0) {
                            stderr.write(buffer, 0, length);
                        }
                        active = true;
                    }
                    if (!active) {
                        Thread.sleep(100);
                    }
                } catch (IOException e) {
                    stderr.println("IO-Exception: " + e.getMessage());
                } catch (InterruptedException e) {
                }
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setName("Stream-Handler");
        thread.setDaemon(true);
        thread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            byte[] buffer = new byte[STREAM_BUFFER];
            try {
                while (mcout.available() > 0) {
                    int length = mcout.read(buffer);
                    if (length > 0) {
                        stdout.write(buffer, 0, length);
                    }
                }
                while (mcerr.available() > 0) {
                    int length = mcerr.read(buffer);
                    if (length > 0) {
                        stderr.write(buffer, 0, length);
                    }
                }
            } catch (IOException e) {
            }
        }));

        // Run Server
        try {
            mainMethod.invoke(null, new Object[]{args});
        } catch (IllegalAccessException | InvocationTargetException e) {
            stderr.println("Failed to run Server: " + e.toString());
            System.exit(21);
            return;
        } catch (Throwable e) {
            stderr.println("Unknown error on run: " + e.toString());
            System.exit(20);
            return;
        }
    }
}
