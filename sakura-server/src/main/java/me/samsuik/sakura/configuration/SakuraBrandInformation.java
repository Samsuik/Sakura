package me.samsuik.sakura.configuration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public final class SakuraBrandInformation {
    private static final String VERSION_MESSAGE = """
                <dark_purple>.
                <dark_purple>| <white>This server is running <gradient:red:light_purple>Sakura</gradient>
                <dark_purple>| <white>Commit<dark_gray>: \\<<commit>> <gray>targeting </gray>(<yellow>MC</yellow>: <gray><version></gray>)
                <dark_purple>| <white>Github<dark_gray>: \\<<yellow><click:open_url:'https://github.com/Samsuik/Sakura'>link</click></yellow>>
                <dark_purple>'""";

    public static void sendBrandToPlayer(final CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(VERSION_MESSAGE,
            Placeholder.component("commit", gitCommit()),
            Placeholder.unparsed("version", Bukkit.getMinecraftVersion())
        ));
    }

    private static Component gitCommit() {
        return Component.text("hover", NamedTextColor.YELLOW)
            .hoverEvent(HoverEvent.showText(Component.text(Bukkit.getGitInformation())));
    }
}
