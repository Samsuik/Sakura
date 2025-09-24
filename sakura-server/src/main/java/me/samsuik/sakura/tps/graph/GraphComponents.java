package me.samsuik.sakura.tps.graph;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public final class GraphComponents {
    private static final Style STRIKE_THROUGH_STYLE = Style.style(TextDecoration.STRIKETHROUGH);
    private static final Style REMOVE_STRIKE_THROUGH_STYLE = Style.style(TextDecoration.STRIKETHROUGH.withState(false));

    public static final Component BACKGROUND = Component.text("::");
    public static final Component HORIZONTAL_LINE = Component.text(" ", STRIKE_THROUGH_STYLE);
    public static final Component VERTICAL_LINE = Component.text("||");
    public static final Component TOP_DOTTED_LINE = Component.text("''");
    public static final Component BOTTOM_DOTTED_LINE = Component.text("..");
    public static final Component BL_TO_TR = Component.text(".", STRIKE_THROUGH_STYLE).append(Component.text("'", REMOVE_STRIKE_THROUGH_STYLE));
    public static final Component TL_TO_BR = Component.text("'").append(Component.text(".", STRIKE_THROUGH_STYLE));
    public static final Component CONE_TOP_LEFT = Component.text(".!");
    public static final Component CONE_TOP_RIGHT = Component.text("!.");
    public static final Component CONE_BOTTOM_LEFT = Component.text("'!");
    public static final Component CONE_BOTTOM_RIGHT = Component.text("!'");

    private static final List<TextColor> COLOURS = List.of(
        NamedTextColor.GREEN, NamedTextColor.YELLOW, NamedTextColor.GOLD,
        NamedTextColor.RED, NamedTextColor.DARK_GRAY, TextColor.color(40, 40, 40)
    );

    public static TextColor colour(final float num) {
        final float segment = 1.0f / COLOURS.size();
        final float a = (1.0f - num) / segment;
        final float t = a % 1.0f;
        final int startIndex = Math.clamp((int) a, 0, COLOURS.size() - 2);
        final int endIndex = startIndex + 1;
        final TextColor startColour = COLOURS.get(startIndex);
        final TextColor endColour = COLOURS.get(endIndex);
        return TextColor.lerp(t, startColour, endColour);
    }
}
