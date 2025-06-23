package me.samsuik.sakura.command.subcommands;

import me.samsuik.sakura.command.PlayerOnlySubCommand;
import me.samsuik.sakura.player.visibility.VisibilityGui;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FPSCommand extends PlayerOnlySubCommand {
    private final VisibilityGui visibilityGui = new VisibilityGui();

    public FPSCommand(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, String[] args) {
        this.visibilityGui.showTo(player);
    }
}
