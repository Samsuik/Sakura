package me.samsuik.sakura.command.subcommand;

import me.samsuik.sakura.command.BaseSubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ConfigCommand extends BaseSubCommand {
    public ConfigCommand(final String name) {
        super(name);
        this.description = "Command for reloading the sakura configuration file";
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        sender.sendMessage(Component.text("Please note that this command is not supported and may cause issues.", NamedTextColor.RED));
        sender.sendMessage(Component.text("If you encounter any issues please use the /stop command to restart your server.", NamedTextColor.RED));

        final MinecraftServer server = ((CraftServer) sender.getServer()).getServer();
        server.sakuraConfigurations.reloadConfigs(server);
        server.server.reloadCount++;

        for (final Level level : server.getAllLevels()) {
            level.localConfig().clearCache();
        }

        sender.sendMessage(Component.text("Sakura config reload complete.", NamedTextColor.GREEN));
    }
}
