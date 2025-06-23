package me.samsuik.sakura.command;

import me.samsuik.sakura.command.subcommands.*;
import me.samsuik.sakura.command.subcommands.debug.DebugLocalRegions;
import me.samsuik.sakura.command.subcommands.debug.DebugRedstoneCache;
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
    public static final Map<String, Command> COMMANDS = new HashMap<>();
    public static final Set<Command> SUB_COMMANDS = new HashSet<>();
    public static final Set<Command> DEBUG_COMMANDS = new HashSet<>();

    static {
        COMMANDS.put("config", new ConfigCommand("config"));
        COMMANDS.put("tps", new TPSCommand("tps"));
        COMMANDS.put("fps", new FPSCommand("fps"));
        COMMANDS.put("tntvisibility", new VisualCommand(VisibilityTypes.TNT, "tnttoggle"));
        COMMANDS.put("sandvisibility", new VisualCommand(VisibilityTypes.SAND, "sandtoggle"));
        SUB_COMMANDS.addAll(COMMANDS.values());
        SUB_COMMANDS.add(new DebugCommand("debug"));
        // "sakura" isn't a subcommand
        COMMANDS.put("sakura", new SakuraCommand("sakura"));
        DEBUG_COMMANDS.add(new DebugRedstoneCache("redstone-cache"));
        DEBUG_COMMANDS.add(new DebugLocalRegions("local-regions"));
    }

    public static void registerCommands(MinecraftServer server) {
        COMMANDS.forEach((name, command) -> {
            server.server.getCommandMap().register(name, "sakura", command);
        });
    }
}
