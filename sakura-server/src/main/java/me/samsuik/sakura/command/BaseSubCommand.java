package me.samsuik.sakura.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@NullMarked
public abstract class BaseSubCommand extends Command {
    public BaseSubCommand(final String name) {
        super(name);
        this.description = "Sakura Command " + name;
        this.setPermission("bukkit.command." + name);
    }

    public abstract void execute(final CommandSender sender, final String[] args);

    public void tabComplete(final List<String> completions, final String[] args) throws IllegalArgumentException {}

    @Override
    @Deprecated
    public final boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (this.testPermission(sender)) {
            this.execute(sender, args);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) throws IllegalArgumentException {
        final List<String> completions = new ArrayList<>(0);

        if (this.testPermissionSilent(sender)) {
            this.tabComplete(completions, args);
        }

        return completions;
    }

    protected final Optional<Integer> parseInt(final String[] args, final int index) {
        return this.parse(args, index, Integer::parseInt);
    }

    protected final Optional<Long> parseLong(final String[] args, final int index) {
        return this.parse(args, index, Long::parseLong);
    }

    protected final Optional<Float> parseFloat(final String[] args, final int index) {
        return this.parse(args, index, Float::parseFloat);
    }

    protected final Optional<Double> parseDouble(final String[] args, final int index) {
        return this.parse(args, index, Double::parseDouble);
    }

    protected final <T> Optional<T> parse(final String[] args, final int index, final Function<String, T> parseFunction) {
        try {
            final String toParse = args[index];
            return Optional.of(parseFunction.apply(toParse));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            return Optional.empty();
        }
    }
}
