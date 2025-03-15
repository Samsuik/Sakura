package me.samsuik.sakura.command;

import com.google.common.collect.Iterables;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

import java.util.*;

@NullMarked
public final class SakuraCommand extends Command {
    private static final Component HEADER_MESSAGE = MiniMessage.miniMessage().deserialize("""
        <dark_purple>.</dark_purple>
        <dark_purple>| <white>This is the main command for <gradient:red:light_purple:0.5>Sakura</gradient>.
        <dark_purple>| <white>All exclusive commands are listed below."""
    );

    private static final String COMMAND_MSG = "<dark_purple>| <dark_gray>*</dark_gray> /<light_purple><command>";

    public SakuraCommand(String name) {
        super(name);
        this.description = "";
        this.usageMessage = "/sakura";
        this.setPermission("bukkit.command.sakura");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length > 0) {
            final Command versionCommand = MinecraftServer.getServer().server.getCommandMap().getCommand("version");
            for (final Command base : Iterables.concat(SakuraCommands.SUB_COMMANDS, List.of(versionCommand))) {
                if (base.getName().equalsIgnoreCase(args[0])) {
                    return base.execute(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));
                }
            }
        }

        this.sendHelpMessage(sender);
        return false;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(HEADER_MESSAGE);

        for (final Command command : SakuraCommands.COMMANDS.values()) {
            if (command != this) {
                sender.sendRichMessage(COMMAND_MSG, Placeholder.unparsed("command", command.getName()));
            }
        }

        sender.sendMessage(Component.text("'", NamedTextColor.DARK_PURPLE));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
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
