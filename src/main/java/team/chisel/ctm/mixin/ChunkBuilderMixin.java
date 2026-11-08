package team.chisel.ctm.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.chisel.ctm.client.state.CTMRenderContext;
import team.chisel.ctm.init.UnbakedModelLoadingFinishedListener;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {
    @Shadow
    public boolean[] renderLayerEmpty;

    @WrapOperation(method = "rebuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRenderLayer()I"))
    int test(Block instance, Operation<Integer> original, @Local(ordinal = 7)int var11, @Local(name = "var18") int blockId){
        if(UnbakedModelLoadingFinishedListener.shouldRenderInLayer(blockId, var11)){
            renderLayerEmpty[var11] = false;
            CTMRenderContext.setRenderLayer(var11);
            return var11;
        }
        return original.call(instance);
    }
}
