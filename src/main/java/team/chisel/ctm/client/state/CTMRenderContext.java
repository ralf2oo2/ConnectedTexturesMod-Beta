package team.chisel.ctm.client.state;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.modificationstation.stationapi.api.block.BlockState;
import team.chisel.ctm.api.util.RenderContextList;
import team.chisel.ctm.client.model.UnbakedModelCTM;

import javax.annotation.Nullable;

public class CTMRenderContext {
    private static final ThreadLocal<CTMRenderContext> CURRENT = ThreadLocal.withInitial(() -> null);
    private final BlockView world;
    private final BlockPos pos;
    private @Nullable RenderContextList ctxCache;

    private CTMRenderContext(BlockView world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public static void set(BlockView world, BlockPos pos) {
        CURRENT.set(new CTMRenderContext(world, pos));
    }

    public static void remove() {
        CURRENT.remove();
    }

    @Nullable
    public static RenderContextList getContextList(BlockState state, UnbakedModelCTM model) {
        CTMRenderContext ctx = CURRENT.get();
        if(ctx == null) {
            return null;
        }

        if(ctx.ctxCache == null) {
            ctx.ctxCache = new RenderContextList(state, model.getCTMTextures(), ctx.world, ctx.pos);
        }
        return ctx.ctxCache;
    }
}
