package team.chisel.ctm.client.texture.context;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.registry.OffsetProviderRegistry;

public class TextureContextPosition implements TextureContext {
    @NotNull
    protected BlockPos pos;

    public TextureContextPosition(@NotNull BlockPos pos) {
        this.pos = pos;
    }

    public TextureContextPosition(int x, int y, int z) {
        this(new BlockPos(x, y, z));
    }

    @SuppressWarnings("resource")
    public TextureContextPosition applyOffset() {
        BlockPos bp = OffsetProviderRegistry.INSTANCE.getOffset(((Minecraft) FabricLoader.getInstance().getGameInstance()).world, pos);
        pos = pos.add(bp.x, bp.y, bp.z);
        return this;
    }

    @NotNull
    public BlockPos getPosition() {
        return pos;
    }

    @Override
    public long serialize() {
        return 0L; // Position data is not useful for serialization (and in fact breaks caching as each location is a new key)
    }
}
