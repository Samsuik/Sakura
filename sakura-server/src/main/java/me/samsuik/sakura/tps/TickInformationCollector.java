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

    public void levelData(Collection<ServerLevel> levels, double tps) {
        int chunks = 0;
        int entities = 0;
        for (ServerLevel level : levels) {
            chunks += level.chunkSource.getFullChunksCount();
            entities += level.entityTickList.entities.size();
        }

        double averageTick = this.tickSamples.longStream()
            .average()
            .orElse(0.0);
        long longestTick = this.tickSamples.longStream()
            .max()
            .orElse(0);
        float targetTickRate = MinecraftServer.getServer().tickRateManager().tickrate();

        ServerTickInformation tickInformation = new ServerTickInformation(
            this.identifier++, tps, averageTick, longestTick, targetTickRate, chunks, entities
        );

        this.collectedInformation.add(tickInformation);
        this.tickSamples.clear();

        if (this.collectedInformation.size() > TEN_MINUTES) {
            this.collectedInformation.subList(0, 60).clear();
        }
    }

    public void tickDuration(long timeTaken) {
        this.tickSamples.add(timeTaken);
    }

    public ImmutableList<ServerTickInformation> collect(long from, long to) {
        List<ServerTickInformation> collected = new ObjectArrayList<>();
        for (ServerTickInformation tickInformation : this.collectedInformation.reversed()) {
            if (tickInformation.identifier() >= from && tickInformation.identifier() < to) {
                collected.add(tickInformation);
            }
        }
        long ahead = to - this.identifier;
        long missing = to - from - collected.size();
        for (int i = 0; i < missing; ++i) {
            int ind = (i < ahead) ? 0 : collected.size();
            collected.add(ind, ServerTickInformation.FILLER);
        }
        return ImmutableList.copyOf(collected);
    }
}
