package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.StationBlockPos;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextPosition;
import team.chisel.ctm.client.texture.render.TextureEldritch;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TextureTypeEldritch implements ITextureType {

    public static class Context extends TextureContextPosition {

        private final BlockPos wrappedpos;

        public Context(BlockPos pos) {
            super(pos);
            wrappedpos = new BlockPos(pos.getX() & 7, pos.getY() & 7, pos.getZ() & 7);
        }

        @Override
        public BlockPos getPosition() {
            return wrappedpos;
        }

        @Override
        public long getCompressedData() {
            return getPosition().asLong();
        }
    }

    @Override
    public ITextureContext getBlockRenderContext(BlockState state, BlockStateView world, BlockPos pos, ICTMTexture<?> tex) {
        return new Context(pos);
    }

    @Override
    public ITextureContext getContextFromData(long data) {
        return new Context(StationBlockPos.fromLong(data));
    }

    @Override
    public ICTMTexture<TextureTypeEldritch> makeTexture(TextureInfo info) {
        return new TextureEldritch(this, info);
    }
}
