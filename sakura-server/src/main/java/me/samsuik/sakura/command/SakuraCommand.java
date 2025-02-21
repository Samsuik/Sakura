package me.samsuik.sakura.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
            List<Command> commands = new ArrayList<>(SakuraCommands.SUB_COMMANDS);

            // This part is copied from the VersionCommand SubCommand in paper
            Command internalVersion = MinecraftServer.getServer().server.getCommandMap().getCommand("version");
            if (internalVersion != null) {
                commands.add(internalVersion);
            }

            for (Command base : commands) {
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

        Stream<Command> uniqueCommands = SakuraCommands.SUB_COMMANDS
            .stream()
            .filter(command -> command != this);

        uniqueCommands.forEach((command) -> {
            sender.sendRichMessage(COMMAND_MSG, Placeholder.unparsed("command", command.getName()));
        });

        sender.sendMessage(Component.text("'", NamedTextColor.DARK_PURPLE));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (!this.testPermissionSilent(sender)) {
            return Collections.emptyList();
        }

        return SakuraCommands.SUB_COMMANDS.stream()
            .filter(command -> command != this)
            .map(Command::getName)
            .filter(name -> args.length <= 1 || name.startsWith(args[args.length - 1]))
            .toList();
    }
}
