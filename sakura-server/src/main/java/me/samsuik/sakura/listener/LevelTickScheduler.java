package me.samsuik.sakura.listener;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@NullMarked
public final class LevelTickScheduler {
    private final Long2ObjectMap<List<TickTask>> scheduledTasks = new Long2ObjectOpenHashMap<>();
    private final Int2ObjectMap<List<TickTask>> repeatingTasks = new Int2ObjectOpenHashMap<>();
    private final Reference2IntMap<TickTask> taskIntervals = new Reference2IntOpenHashMap<>();
    private final Deque<TickTask> removeLater = new ArrayDeque<>();
    private final Level level;

    public LevelTickScheduler(final Level level) {
        this.level = level;
    }

    public void runTaskLater(final Runnable task, final int delay) {
        this.runTaskLater(tick -> task.run(), delay);
    }

    public void runTaskLater(final TickTask task, final int delay) {
        final long runAt = this.level.getGameTime() + delay;
        this.scheduledTasks.computeIfAbsent(runAt, i -> new ObjectArrayList<>())
            .add(task);
    }

    public void repeatingTask(final Runnable task, final int interval) {
        this.repeatingTask(tick -> task.run(), interval);
    }

    public void repeatingTask(final TickTask task, final int interval) {
        final int taskInterval = Math.max(interval, 1);
        this.repeatingTasks.computeIfAbsent(taskInterval, i -> new ObjectArrayList<>())
            .add(task);
        this.taskIntervals.put(task, taskInterval);
    }

    public void removeTask(final TickTask task) {
        final int taskInterval = this.taskIntervals.removeInt(task);
        this.repeatingTasks.computeIfPresent(taskInterval, (i, tasks) -> {
            tasks.remove(task);
            return tasks.isEmpty() ? null : tasks;
        });
    }

    private void runTasks(final List<TickTask> tasks, final long gameTime) {
        for (final TickTask tickTask : tasks) {
            tickTask.run(gameTime);
        }
    }

    public void tick() {
        final long gameTime = this.level.getGameTime();
        for (final long tick : this.scheduledTasks.keySet()) {
            if (tick > gameTime) {
                continue;
            }

            final List<TickTask> tasks = this.scheduledTasks.remove(tick);
            this.runTasks(tasks, gameTime);
            this.removeLater.addAll(tasks);
        }

        for (final int interval : this.repeatingTasks.keySet()) {
            if (gameTime % interval == 0) {
                this.runTasks(this.repeatingTasks.get(interval), gameTime);
            }
        }

        TickTask tickTask;
        while ((tickTask = this.removeLater.poll()) != null) {
            this.removeTask(tickTask);
        }
    }

    public interface TickTask {
        void run(final long tick);
    }
}
