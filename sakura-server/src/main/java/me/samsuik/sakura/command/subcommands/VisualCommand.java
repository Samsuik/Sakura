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

    public VisualCommand(VisibilityType type, String... aliases) {
        super(type.key() + "visibility");
        this.setAliases(Arrays.asList(aliases));
        this.type = type;
    }

    @Override
    public void execute(Player player, String[] args) {
        VisibilitySettings settings = player.getVisibility();
        VisibilityState state = settings.toggle(type);

        String stateName = (state == VisibilityState.ON) ? "Enabled" : "Disabled";
        player.sendRichMessage(GlobalConfiguration.get().messages.fpsSettingChange,
            Placeholder.unparsed("name", this.type.key()),
            Placeholder.unparsed("state", stateName)
        );
    }
}
