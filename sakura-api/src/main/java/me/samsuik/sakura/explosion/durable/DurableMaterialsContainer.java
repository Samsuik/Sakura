package me.samsuik.sakura.explosion.durable;

import com.google.common.base.Preconditions;
import me.samsuik.sakura.configuration.local.Container;
import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A container for durable materials.
 */
@NullMarked
public sealed class DurableMaterialsContainer implements Container<BlockType, DurableMaterial> {
    private final Map<BlockType, DurableMaterial> materials = new IdentityHashMap<>();

    public static SealedDurableMaterialsContainer sealedContainer(final Object... contents) {
        if (contents.length % 2 != 0) {
            throw new IllegalArgumentException("Expected an even number of contents, got " + contents.length);
        }
        final Map<BlockType, DurableMaterial> materials = new IdentityHashMap<>();
        for (int index = 0; index < contents.length; index += 2) {
            Object key = contents[index];
            Object value = contents[index + 1];
            if (key instanceof Material bukkitMaterial) {
                key = blockTypeFromBukkitMaterial(bukkitMaterial);
            }
            if (!(key instanceof BlockType blockType)) {
                throw new IllegalArgumentException("Key at index " + index + " must be of type BlockType or Material");
            }
            if (!(value instanceof DurableMaterial material)) {
                throw new IllegalArgumentException("Value at index " + (index + 1) + " must be of type DurableMaterial");
            }
            materials.put(blockType, material);
        }
        return new SealedDurableMaterialsContainer(materials);
    }

    private DurableMaterialsContainer(final Map<BlockType, DurableMaterial> materials) {
        this.materials.putAll(materials);
    }

    public DurableMaterialsContainer() {}

    private static BlockType blockTypeFromBukkitMaterial(final Material bukkitMaterial) {
        final BlockType blockType = bukkitMaterial.asBlockType();
        Preconditions.checkNotNull(blockType, "Material " + bukkitMaterial + " is not a block");
        return blockType;
    }

    @Deprecated
    public final @Nullable DurableMaterial set(final Material bukkitMaterial, final DurableMaterial material) {
        return this.set(blockTypeFromBukkitMaterial(bukkitMaterial), material);
    }

    @Deprecated
    public final @Nullable DurableMaterial remove(final Material bukkitMaterial) {
        return this.remove(blockTypeFromBukkitMaterial(bukkitMaterial));
    }

    @Deprecated
    public final @Nullable DurableMaterial get(final Material bukkitMaterial) {
        return this.get(blockTypeFromBukkitMaterial(bukkitMaterial));
    }

    public @Nullable DurableMaterial set(final BlockType blockType, final DurableMaterial material) {
        Preconditions.checkNotNull(material, "Material cannot be null");
        return this.materials.put(blockType, material);
    }

    public @Nullable DurableMaterial remove(final BlockType blockType) {
        return this.materials.remove(blockType);
    }

    public final @Nullable DurableMaterial get(final BlockType blockType) {
        return this.materials.get(blockType);
    }

    public void clear() {
        this.materials.clear();
    }

    @Override
    public final Map<BlockType, DurableMaterial> contents() {
        return Map.copyOf(this.materials);
    }

    public final DurableMaterialsContainer open() {
        return this instanceof SealedDurableMaterialsContainer
            ? new DurableMaterialsContainer(this.materials)
            : this;
    }

    public final SealedDurableMaterialsContainer seal() {
        return this instanceof SealedDurableMaterialsContainer sealed
            ? sealed
            : new SealedDurableMaterialsContainer(this.materials);
    }

    public static final class SealedDurableMaterialsContainer extends DurableMaterialsContainer {
        private SealedDurableMaterialsContainer(final Map<BlockType, DurableMaterial> materials) {
            super(materials);
        }

        @Override
        public DurableMaterial set(final BlockType key, final DurableMaterial value) {
            throw new UnsupportedOperationException("Container is sealed");
        }

        @Override
        public DurableMaterial remove(final BlockType key) {
            throw new UnsupportedOperationException("Container is sealed");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Container is sealed");
        }
    }
}
