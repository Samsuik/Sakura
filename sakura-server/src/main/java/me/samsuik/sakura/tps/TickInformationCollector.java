package me.samsuik.sakura.tps;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;

@NullMarked
public final class TickInformationCollector {
    private static final int TEN_MINUTES = 10 * 60;
    private final ObjectArrayList<ServerTickInformation> collectedInformation = new ObjectArrayList<>();
    private final LongArrayList tickSamples = new LongArrayList();
    private long identifier = 0;

    public ServerTickInformation latestTickInformation() {
        return this.collectedInformation.getLast();
    }

    public void levelData(final Collection<ServerLevel> levels, final double tps) {
        int chunks = 0;
        int entities = 0;
        for (final ServerLevel level : levels) {
            chunks += level.chunkSource.getFullChunksCount();
            entities += level.entityTickList.entities.size();
        }

        final double averageTick = this.tickSamples.longStream()
            .average()
            .orElse(0.0);
        final long longestTick = this.tickSamples.longStream()
            .max()
            .orElse(0);
        final float targetTickRate = MinecraftServer.getServer().tickRateManager().tickrate();
        final ServerTickInformation tickInformation = new ServerTickInformation(
            this.identifier++, tps, averageTick, longestTick, targetTickRate, chunks, entities
        );

        this.collectedInformation.add(tickInformation);
        this.tickSamples.clear();

        // Keep only the last 10 minutes of tick information
        if (this.collectedInformation.size() > TEN_MINUTES) {
            this.collectedInformation.subList(0, 60).clear();
        }
    }

    public void tickDuration(final long timeTaken) {
        this.tickSamples.add(timeTaken);
    }

    public ImmutableList<ServerTickInformation> collect(final long from, final long to) {
        final List<ServerTickInformation> collected = new ObjectArrayList<>();
        for (final ServerTickInformation tickInformation : this.collectedInformation.reversed()) {
            if (tickInformation.identifier() >= from && tickInformation.identifier() < to) {
                collected.add(tickInformation);
            }
        }

        final long ahead = to - this.identifier;
        final long missing = to - from - collected.size();
        for (int remaining = 0; remaining < missing; ++remaining) {
            final int index = (remaining < ahead) ? 0 : collected.size();
            collected.add(index, ServerTickInformation.UNKNOWN);
        }

        return ImmutableList.copyOf(collected);
    }
}
