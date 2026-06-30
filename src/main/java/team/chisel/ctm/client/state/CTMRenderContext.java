package team.chisel.ctm.client.state;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.MutableBlockPos;
import net.modificationstation.stationapi.api.world.BlockStateView;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.client.model.CTMModelInfo;
import team.chisel.ctm.client.model.TextureContextMap;

public class CTMRenderContext {
    private static final ThreadLocal<CTMRenderContext> CURRENT = ThreadLocal.withInitial(CTMRenderContext::new);
    private BlockStateView world;
    private final MutableBlockPos mutablePos = new MutableBlockPos();
    public final TextureContextMap contextMap = new TextureContextMap();
    private boolean isFilled = false;
    private boolean isActive = false;

    private CTMRenderContext() {}

    public static void set(BlockStateView world, BlockPos pos) {
        CTMRenderContext ctx = CURRENT.get();
        ctx.world = world;
        ctx.mutablePos.set(pos.getX(), pos.getY(), pos.getZ());
        ctx.contextMap.reset();
        ctx.isFilled = false;
        ctx.isActive = true;
    }

    public static void remove() {
        CTMRenderContext ctx = CURRENT.get();
        ctx.isActive = false;
        ctx.world = null;
        ctx.contextMap.reset();
    }

    @Nullable
    public static TextureContextMap getTextureContextMap(BlockState state, CTMModelInfo info) {
        CTMRenderContext ctx = CURRENT.get();
        if (!ctx.isActive) {
            return null;
        }

        if (!ctx.isFilled) {
            ctx.contextMap.fill(info.getTextures(), state, ctx.world, ctx.mutablePos);
            ctx.isFilled = true;
        }

        return ctx.contextMap;
    }
}
