package team.chisel.ctm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.block.Block;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import team.chisel.ctm.client.state.CTMRenderContext;
import team.chisel.ctm.init.UnbakedModelLoadingFinishedListener;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    @WrapOperation(method = "rebuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRenderLayer()I"))
    int renderCTM(Block instance, Operation<Integer> original, @Local(ordinal = 7)int var11, @Local(name = "var18") int blockId, @Local(ordinal = 9) LocalIntRef var13){
        if(UnbakedModelLoadingFinishedListener.shouldRenderInLayer(blockId, var11)){
            CTMRenderContext.setRenderLayer(var11);
            var13.set(var13.get() | 1);
            return var11;
        }
        return original.call(instance);
    }
}
