package me.samsuik.sakura.command;

import me.samsuik.sakura.SakuraFeatureHooks;
import me.samsuik.sakura.command.subcommand.*;
import me.samsuik.sakura.command.subcommand.debug.DebugCommand;
import me.samsuik.sakura.command.subcommand.debug.DebugLocalConfiguration;
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
        // Register primary commands (these can be called through / or the "sakura" command)
        COMMANDS.put("config", new ConfigCommand("config"));
        COMMANDS.put("tps", new TPSCommand("tps"));
        COMMANDS.put("mechanic", new MechanicCommand("mechanic"));
        SakuraFeatureHooks.setupCommands(COMMANDS);

        // Register sub commands (commands callable through the main "sakura" command)
        SUB_COMMANDS.addAll(COMMANDS.values());
        SUB_COMMANDS.add(new DebugCommand("debug"));

        // Register debug commands (commands intended for debugging features/api)
        DEBUG_COMMANDS.add(new DebugLocalConfiguration("local-regions"));
        COMMANDS.put("sakura", new SakuraCommand("sakura"));
    }

    public static void registerCommands(final MinecraftServer server) {
        COMMANDS.forEach((name, command) -> server.server.getCommandMap().register(name, "sakura", command));
    }
}
