package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.StationBlockPos;
import net.modificationstation.stationapi.api.world.BlockStateView;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.TextureMap;
import team.chisel.ctm.client.texture.TextureMap.MapType;
import team.chisel.ctm.client.texture.context.TextureContextPosition;

public class TextureTypeMap implements TextureType {
    public static final TextureTypeMap RANDOM = new TextureTypeMap(TextureMap.MapTypeImpl.RANDOM);
    public static final TextureTypeMap PATTERN = new TextureTypeMap(TextureMap.MapTypeImpl.PATTERNED);

    private final MapType type;

    public TextureTypeMap(final MapType type) {
        this.type = type;
    }

    @Override
    public TextureMap makeTexture(TextureInfo info) {
        return new TextureMap(this, info);
    }

    @Override
    public TextureContext getTextureContext(BlockState state, BlockStateView world, @NotNull BlockPos pos, CTMTexture<?> texture) {
        if (!(texture instanceof TextureMap)) {
            return new TextureContextPosition(pos);
        }

        return type.getContext(pos, (TextureMap) texture);
    }

    @Override
    public TextureContext deserializeContext(long data) {
        return new TextureContextPosition(StationBlockPos.fromLong(data));
    }

    public MapType getType() {
        return type;
    }
}
