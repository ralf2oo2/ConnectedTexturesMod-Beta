package team.chisel.ctm.api;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * To be implemented on blocks that "hide" another block inside, so that connected textures can still be accomplished.
 */
public interface Facade {
    /**
     * Gets the BlockState this facade appears as.
     *
     * @param world The world.
     * @param pos The block's position.
     * @param connection The position of the block being connected to.
     * @param side The side being rendered, <b>not</b> the side being connected from. This value can be null if no side is specified. Make sure this is handled appropriately.
     * @return The BlockState which the block appears as.
     */
    @NotNull
    BlockState getFacadeState(@NotNull BlockStateView world, @NotNull BlockPos pos, @NotNull BlockPos connection, @Nullable Direction side);
}
