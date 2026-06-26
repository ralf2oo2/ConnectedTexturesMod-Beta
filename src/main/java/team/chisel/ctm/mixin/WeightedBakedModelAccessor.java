package team.chisel.ctm.mixin;

import net.modificationstation.stationapi.api.client.render.model.WeightedBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(WeightedBakedModel.class)
public interface WeightedBakedModelAccessor {
    @Accessor
    int getTotalWeight();

    @Accessor("models")
    List<?> getModels();
}
