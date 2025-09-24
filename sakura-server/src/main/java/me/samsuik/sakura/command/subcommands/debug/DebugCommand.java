package me.samsuik.sakura.command.subcommands.debug;

import me.samsuik.sakura.command.BaseMenuCommand;
import me.samsuik.sakura.command.SakuraCommands;
import org.bukkit.command.Command;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DebugCommand extends BaseMenuCommand {
    public DebugCommand(final String name) {
        super(name);
    }

    @Override
    public String header() {
        return "Command for debugging Sakura features and api";
    }

    @Override
    public Iterable<Command> subCommands() {
        return SakuraCommands.DEBUG_COMMANDS;
    }
}
