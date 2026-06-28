package team.chisel.ctm.client.texture.ctx;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.util.math.Vec3i;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.registry.OffsetProviderRegistry;

import javax.annotation.Nonnull;

public class TextureContextPosition implements ITextureContext {

    protected @Nonnull BlockPos position;

    public TextureContextPosition(@Nonnull BlockPos pos) {
        this.position = pos;
    }

    public TextureContextPosition(int x, int y, int z) {
        this(new BlockPos(x, y, z));
    }

    public TextureContextPosition applyOffset() {
        BlockPos bp = OffsetProviderRegistry.INSTANCE.getOffset(((Minecraft)FabricLoader.getInstance().getGameInstance()).world, position);
        this.position = position.add(new Vec3i(bp.x, bp.y, bp.z));
        return this;
    }

    public @Nonnull BlockPos getPosition() {
        return position;
    }

    @Override
    public long getCompressedData() {
        return 0L; // Position data is not useful for serialization (and in fact breaks caching as each location is a new key)
    }
}
