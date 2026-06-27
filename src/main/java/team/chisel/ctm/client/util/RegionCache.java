package team.chisel.ctm.client.util;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.source.BiomeSource;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.BlockStateView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;

/**
 * Used by render state creation to avoid unnecessary block lookups through the world.
 */
@ParametersAreNonnullByDefault
public class RegionCache implements BlockView, BlockStateView {

    /*
     * XXX
     *
     * These are required for future use, in case there is ever a need to have this region cache only store a certain area of the world.
     *
     * Currently, this class is only used by CTM, which is limited to a very small subsection of the world,
     * and thus the overhead of distance checking is unnecessary.
     */
    @SuppressWarnings("unused")
    private final BlockPos center;
    @SuppressWarnings("unused")
    private final int radius;

    private WeakReference<BlockView> passthrough;
    private final Long2ObjectMap<BlockState> stateCache = new Long2ObjectOpenHashMap<>();

    public RegionCache(BlockPos center, int radius, @Nullable BlockView passthrough) {
        this.center = center;
        this.radius = radius;
        this.passthrough = new WeakReference<>(passthrough);
    }

    private BlockView getPassthrough() {
        BlockView ret = passthrough.get();
        Preconditions.checkNotNull(ret);
        return ret;
    }

    private BlockStateView getPassthroughStateView() {
        return (BlockStateView) getPassthrough();
    }

    public @Nonnull RegionCache updateWorld(BlockView passthrough) {
        // We do NOT use getPassthrough() here so as to skip the null-validation - it's obviously valid to be null here
        if (this.passthrough.get() != passthrough) {
            stateCache.clear();
        }
        this.passthrough = new WeakReference<>(passthrough);
        return this;
    }

    @Override
    public int getBlockId(int x, int y, int z) {
        return getPassthrough().getBlockId(x, y, z);
    }

    @Override
    public BlockEntity getBlockEntity(int x, int y, int z) {
        return getPassthrough().getBlockEntity(x, y, z);
    }

    @Override
    public float getNaturalBrightness(int x, int y, int z, int blockLight) {
        return getPassthrough().getNaturalBrightness(x, y, z, blockLight);
    }

    @Override
    public float method_1782(int x, int y, int z) {
        return getPassthrough().method_1782(x, y, z);
    }

    @Override
    public int getBlockMeta(int x, int y, int z) {
        return getPassthrough().getBlockMeta(x, y, z);
    }

    @Override
    public Material getMaterial(int x, int y, int z) {
        return getPassthrough().getMaterial(x, y, z);
    }

    @Override
    public boolean method_1783(int x, int y, int z) {
        return getPassthrough().method_1783(x, y, z);
    }

    @Override
    public boolean shouldSuffocate(int x, int y, int z) {
        return getPassthrough().shouldSuffocate(x, y, z);
    }

    @Override
    public BiomeSource method_1781() {
        return getPassthrough().method_1781();
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        long address = pos.asLong();
        BlockState ret = stateCache.get(address);
        if(ret == null) {
            stateCache.put(address, ret = getPassthroughStateView().getBlockState(pos));
        }
        return ret;
    }
}
