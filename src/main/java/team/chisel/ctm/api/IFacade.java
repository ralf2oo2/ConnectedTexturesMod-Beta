package team.chisel.ctm.api;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * To be implemented on blocks that "hide" another block inside, so connected textures can still be accomplished.
 */
public interface IFacade {

    /**
     * @deprecated Use {@link #getFacade(BlockStateView, BlockPos, Direction, BlockPos)}
     */
    @Nonnull
    @Deprecated
    BlockState getFacade(@Nonnull BlockStateView world, @Nonnull BlockPos pos, @Nullable Direction side);

    /**
     * Gets the blockstate this facade appears as.
     *
     * @param world
     *            {@link net.minecraft.world.World}
     * @param pos
     *            The Blocks position
     * @param side
     *            The side being rendered, NOT the side being connected from.
     *            <p/>
     *            This value can be null if no side is specified. Please handle this appropriately.
     * @param connection
     *            The position of the block being connected to.
     * @return The blockstate which your block appears as.
     */
    @Nonnull
    default BlockState getFacade(@Nonnull BlockStateView world, @Nonnull BlockPos pos, @Nullable Direction side, @Nonnull BlockPos connection) {
        return getFacade(world, pos, side);
    }

}
