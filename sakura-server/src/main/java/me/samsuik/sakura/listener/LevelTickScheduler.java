package me.samsuik.sakura.listener;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@NullMarked
public final class LevelTickScheduler {
    private final Int2ObjectMap<List<TickTask>> tickTasks = new Int2ObjectLinkedOpenHashMap<>();
    private final Object2IntMap<TickTask> taskIntervals = new Object2IntOpenHashMap<>();
    private final Deque<TickTask> removeLater = new ArrayDeque<>();

    public void delayedTask(Runnable runnable, int delay) {
        this.registerNewTask(new TickTask() {
            private int cycles = 0;

            @Override
            public void run(long tick) {
                if (this.cycles++ >= delay) {
                    runnable.run();
                    LevelTickScheduler.this.removeLater.add(this);
                }
            }
        }, 0);
    }

    public void registerNewTask(Runnable runnable, int interval) {
        this.registerNewTask(tick -> runnable.run(), interval);
    }

    public void registerNewTask(TickTask task, int interval) {
        int safeInterval = Math.max(interval + 1, 1);
        this.tickTasks.computeIfAbsent(safeInterval, i -> new ArrayList<>())
            .add(task);
        this.taskIntervals.put(task, safeInterval);
    }

    private void removeTasks() {
        TickTask tickTask;
        while ((tickTask = this.removeLater.poll()) != null) {
            this.removeTask(tickTask);
        }
    }

    private void removeTask(TickTask task) {
        int interval = this.taskIntervals.removeInt(task);
        if (interval > 0) {
            this.tickTasks.computeIfPresent(interval, (i, tasks) -> {
                tasks.remove(task);
                return tasks.isEmpty() ? null : tasks;
            });
        }
    }

    private void runTasks(List<TickTask> tasks, long gameTime) {
        for (TickTask tickTask : tasks) {
            tickTask.run(gameTime);
        }
    }

    public void levelTick(Level level) {
        long gameTime = level.getGameTime();
        for (int interval : this.tickTasks.keySet()) {
            if (gameTime % interval == 0) {
                this.runTasks(this.tickTasks.get(interval), gameTime);
            }
        }
        this.removeTasks();
    }

    public interface TickTask {
        void run(long tick);
    }
}
