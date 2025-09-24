package me.samsuik.sakura.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class PlayerOnlySubCommand extends BaseSubCommand {
    public PlayerOnlySubCommand(final String name) {
        super(name);
    }

    public abstract void execute(final Player player, final String[] args);

    public final void execute(final CommandSender sender, final String[] args) {
        if (sender instanceof Player player) {
            this.execute(player, args);
        } else {
            sender.sendRichMessage("<red>This command can only be used by players");
        }
    }
}
