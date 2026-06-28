package team.chisel.ctm.client.state;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.client.model.CTMModelInfo;
import team.chisel.ctm.client.model.TextureContextMap;

import javax.annotation.Nullable;

public class CTMRenderContext {
    private static final ThreadLocal<CTMRenderContext> CURRENT = ThreadLocal.withInitial(() -> null);
    private final BlockStateView world;
    private final BlockPos pos;
    public TextureContextMap contextMap = new TextureContextMap();

    private CTMRenderContext(BlockStateView world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public static void set(BlockStateView world, BlockPos pos) {
        CURRENT.set(new CTMRenderContext(world, pos));
    }

    public static void remove() {
        CURRENT.remove();
    }

    @Nullable
    public static TextureContextMap getTextureContextMap(BlockState state, CTMModelInfo info) {
        CTMRenderContext ctx = CURRENT.get();
        if(ctx == null) {
            return null;
        }

        if(ctx.contextMap == null) {
            ctx.contextMap = new TextureContextMap();
        }
        ctx.contextMap.fill(info.getTextures(), state, ctx.world, ctx.pos);
        return ctx.contextMap;
    }
}
