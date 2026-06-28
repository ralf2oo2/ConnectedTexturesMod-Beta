package team.chisel.ctm.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.client.render.model.BakedModel;
import net.modificationstation.stationapi.api.world.BlockStateView;
import net.modificationstation.stationapi.impl.client.arsenic.renderer.render.BakedModelRendererImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.chisel.ctm.client.state.CTMRenderContext;

import java.util.Random;

@Mixin(BakedModelRendererImpl.class)
public class BakedModelRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/modificationstation/stationapi/api/client/render/model/BakedModel;getQuads(Lnet/modificationstation/stationapi/api/block/BlockState;Lnet/modificationstation/stationapi/api/util/math/Direction;Ljava/util/Random;)Lcom/google/common/collect/ImmutableList;"), remap = false)
    void setRenderState(BlockView world, BakedModel model, BlockState state, BlockPos pos, boolean cull, Random random, long seed, CallbackInfoReturnable<Boolean> cir){
        CTMRenderContext.set((BlockStateView)world, pos);
    }
}
