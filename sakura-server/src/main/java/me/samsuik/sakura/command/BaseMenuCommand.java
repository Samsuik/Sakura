package me.samsuik.sakura.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@NullMarked
public abstract class BaseMenuCommand extends BaseSubCommand {
    private static final String HEADER_MESSAGE = "<dark_purple>| <white><message>";
    private static final String COMMAND_MSG = "<dark_purple>| <dark_gray>*</dark_gray> /<light_purple><command>";

    public BaseMenuCommand(String name) {
        super(name);
    }

    public abstract String header();

    public Iterable<Command> helpCommands() {
        return this.subCommands();
    }

    public abstract Iterable<Command> subCommands();

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            for (final Command base : this.subCommands()) {
                if (base.getName().equalsIgnoreCase(args[0])) {
                    base.execute(sender, "", Arrays.copyOfRange(args, 1, args.length));
                    return;
                }
            }
        }

        this.sendHelpMessage(sender);
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text(".", NamedTextColor.DARK_PURPLE));
        for (final String header : this.header().split("\n")) {
            if (!header.isEmpty()) {
                sender.sendRichMessage(HEADER_MESSAGE, Placeholder.unparsed("message", header));
            }
        }

        for (final Command command : this.helpCommands()) {
            if (command != this) {
                sender.sendRichMessage(COMMAND_MSG, Placeholder.unparsed("command", command.getName()));
            }
        }

        sender.sendMessage(Component.text("'", NamedTextColor.DARK_PURPLE));
    }

    @Override
    public final List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (!this.testPermissionSilent(sender) || args.length == 0) {
            return Collections.emptyList();
        }

        final Command command = SakuraCommands.getCommand(args[0]);
        final List<String> completions = new ArrayList<>();
        if (command != null && args.length > 1) {
            final String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            completions.addAll(command.tabComplete(sender, alias, newArgs));
        } else {
            for (final Command subCommand : SakuraCommands.SUB_COMMANDS) {
                final String commandName = subCommand.getName();
                if (commandName.startsWith(args[0])) {
                    completions.add(commandName);
                }
            }
        }

        final String lastArg = args[args.length - 1];
        return completions.stream()
            .filter(result -> result.startsWith(lastArg))
            .toList();
    }
}
