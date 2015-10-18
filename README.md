# Minecraft Tools
Some random tools written for
[Minecraft](https://minecraft.net).

These are just dumped here for reference, no support or proper documentation 
will be given.


## Components
### Clock Generator
Compiles a bunch of files containing commands into a list of setblock commands
that can insert the redstone into Minecraft.

For reference on syntax, you're on your own.

### MC Shell
A small wrapper for running a Minecraft server, that manipulates the std streams
to work around some lag issues as well as executing a file containing command,
bypassing any line buffering of the OS.

To start a server with the wrapper, simply include both the wrapper as well as
the Minecraft server jar and run com.tobi29.minecraft.shell.Shell.

Running "#execfile /Path/To/File" in the console will send each line of the file
to the server as a command.


## Build
The project uses Gradle to build all modules.
