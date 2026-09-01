package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.StationBlockPos;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.TextureAlterR;
import team.chisel.ctm.client.texture.context.TextureContextAlterR;
import team.chisel.ctm.client.texture.context.TextureContextPosition;

public class TextureTypeAlterR implements TextureType {
    @Override
    public CTMTexture<? extends TextureTypeAlterR> makeTexture(TextureInfo info) {
        return new TextureAlterR(this, info);
    }

    @Override
    public TextureContext getTextureContext(BlockState state, BlockStateView world, BlockPos pos, CTMTexture<?> texture) {
        return new TextureContextAlterR(pos);
    }

    @Override
    public TextureContext deserializeContext(long data) {
        return new TextureContextPosition(StationBlockPos.fromLong(data));
    }

    @Override
    public int requiredTextures() {
        return 1;
    }
}
