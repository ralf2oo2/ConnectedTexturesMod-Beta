package team.chisel.ctm.api.texture;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.BlockStateView;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface IContextProvider {

    /**
     * Called to create a context for an upcoming render. This context will then be passed to
     * {@link ICTMTexture#transformQuad(net.modificationstation.stationapi.api.client.render.model.BakedQuad, ITextureContext, int)}.
     *
     * @param state
     *            The state of the block being rendered. Will <i>not</i> be an {@link IExtendedBlockState}.
     * @param world
     *            The current rendering world.
     * @param pos
     *            The position of the block being rendered.
     * @param tex
     *            The current {@link ICTMTexture} being rendered.
     * @return A context which can be used to manipulate quads later in the pipeline.
     */
    ITextureContext getBlockRenderContext(BlockState state, BlockStateView world, BlockPos pos, ICTMTexture<?> tex);

    /**
     * Recreates a render context compressed data.
     * <br><br>
     * As of yet, this method is unused.
     *
     * @param data
     *            The compressed data, will match what is produced by {@link ITextureContext#getCompressedData()}.
     */
    @Deprecated
    ITextureContext getContextFromData(long data);
}
