package me.samsuik.sakura.command.subcommands;

import me.samsuik.sakura.command.BaseSubCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public final class DebugCommand extends BaseSubCommand {
    public DebugCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player) || args.length == 0) {
            return;
        }

        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        if (args[0].equalsIgnoreCase("redstone-cache")) {
            this.showCachedWires(player, nmsPlayer.level());
        }
    }

    @Override
    public void tabComplete(List<String> list, String[] args) throws IllegalArgumentException {
        list.add("redstone-cache");
    }

    private void showCachedWires(Player player, Level level) {
        player.sendMessage("Redstone cache debug not available");
    }
}
