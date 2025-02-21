package me.samsuik.sakura.command;

import me.samsuik.sakura.command.subcommands.*;
import me.samsuik.sakura.player.visibility.VisibilityTypes;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NullMarked
public final class SakuraCommands {
    static final Map<String, Command> COMMANDS = new HashMap<>();
    static final Set<Command> SUB_COMMANDS = new HashSet<>();

    static {
        COMMANDS.put("sakura", new SakuraCommand("sakura"));
        COMMANDS.put("config", new ConfigCommand("config"));
        COMMANDS.put("tps", new TPSCommand("tps"));
        COMMANDS.put("fps", new FPSCommand("fps"));
        COMMANDS.put("tntvisibility", new VisualCommand(VisibilityTypes.TNT, "tnttoggle"));
        COMMANDS.put("sandvisibility", new VisualCommand(VisibilityTypes.SAND, "sandtoggle"));
        SUB_COMMANDS.addAll(COMMANDS.values());
        SUB_COMMANDS.add(new DebugCommand("debug"));
    }

    public static void registerCommands(MinecraftServer server) {
        COMMANDS.forEach((name, command) -> {
            server.server.getCommandMap().register(name, "sakura", command);
        });
    }
}
