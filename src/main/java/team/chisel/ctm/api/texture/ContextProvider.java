package team.chisel.ctm.api.texture;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;


public interface ContextProvider {
    /**
     * Creates the context for an upcoming mesh build. This context will then be passed to
     * {@link CTMTexture#transformQuad(BakedQuad, Direction, TextureContext)}.
     *
     * @param state The state of the block being rendered.
     * @param world The world.
     * @param pos The position of the block being rendered.
     * @param texture The {@link CTMTexture} being rendered.
     * @return The context which can be used to manipulate quads later in the pipeline.
     */
    TextureContext getTextureContext(BlockState state, BlockStateView world, BlockPos pos, CTMTexture<?> texture);

    /**
     * Recreates a TextureContext from compressed data.
     *
     * <p>As of yet, this method is unused.
     *
     * @param data The compressed data, which will match what is produced by {@link TextureContext#serialize()}.
     */
    @Deprecated
    default TextureContext deserializeContext(long data) {
        throw new UnsupportedOperationException();
    }
}
