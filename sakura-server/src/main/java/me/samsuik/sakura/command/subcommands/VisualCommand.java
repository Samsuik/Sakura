package me.samsuik.sakura.command.subcommands;

import me.samsuik.sakura.command.PlayerOnlySubCommand;
import me.samsuik.sakura.configuration.GlobalConfiguration;
import me.samsuik.sakura.player.visibility.VisibilitySettings;
import me.samsuik.sakura.player.visibility.VisibilityState;
import me.samsuik.sakura.player.visibility.VisibilityType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;

@NullMarked
public final class VisualCommand extends PlayerOnlySubCommand {
    private final VisibilityType type;

    public VisualCommand(final VisibilityType type, final String... aliases) {
        super(type.key() + "visibility");
        this.setAliases(Arrays.asList(aliases));
        this.type = type;
    }

    @Override
    public void execute(final Player player, final String[] args) {
        final VisibilitySettings settings = player.getVisibility();
        final VisibilityState state = settings.toggle(type);

        final String stateName = (state == VisibilityState.ON) ? "Enabled" : "Disabled";
        player.sendMessage(GlobalConfiguration.get().messages.fpsSettingChangeComponent(this.type.key(), stateName));
    }
}
