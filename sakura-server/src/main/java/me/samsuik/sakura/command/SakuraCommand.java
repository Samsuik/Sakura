package me.samsuik.sakura.command;

import com.google.common.collect.Iterables;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.jspecify.annotations.NullMarked;

import java.util.*;

@NullMarked
public final class SakuraCommand extends BaseMenuCommand {
    public SakuraCommand(String name) {
        super(name);
        this.description = "";
    }

    @Override
    public String header() {
        return "This is the main command for <gradient:red:light_purple:0.5>Sakura</gradient>.\n" +
               "All exclusive commands are listed below.";
    }

    @Override
    public Iterable<Command> helpCommands() {
        return SakuraCommands.COMMANDS.values();
    }

    @Override
    public Iterable<Command> subCommands() {
        final Command versionCommand = MinecraftServer.getServer().server.getCommandMap().getCommand("version");
        return Iterables.concat(SakuraCommands.SUB_COMMANDS, List.of(versionCommand));
    }
}
