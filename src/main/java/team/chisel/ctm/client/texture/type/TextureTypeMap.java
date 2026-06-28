package team.chisel.ctm.client.texture.type;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.StationBlockPos;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextPosition;
import team.chisel.ctm.client.texture.render.TextureMap;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class TextureTypeMap implements ITextureType {
    public static final TextureTypeMap RANDOM = new TextureTypeMap(TextureMap.MapType.RANDOM);
    public static final TextureTypeMap PATTERN = new TextureTypeMap(TextureMap.MapType.PATTERNED);

    private final TextureMap.MapType type;

    @Override
    public TextureMap makeTexture(TextureInfo info) {
        return new TextureMap(this, info, type);
    }

    @Override
    public ITextureContext getBlockRenderContext(BlockState state, BlockStateView world, @Nonnull BlockPos pos, ICTMTexture<?> tex) {
        return type.getContext(pos, (TextureMap) tex);
    }

    @Override
    public ITextureContext getContextFromData(long data) {
        return new TextureContextPosition(StationBlockPos.fromLong(data));
    }
}
