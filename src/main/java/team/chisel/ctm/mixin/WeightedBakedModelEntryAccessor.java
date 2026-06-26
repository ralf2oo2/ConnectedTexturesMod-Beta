package team.chisel.ctm.mixin;

import net.modificationstation.stationapi.api.client.render.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.modificationstation.stationapi.api.client.render.model.WeightedBakedModel$Entry")
public interface WeightedBakedModelEntryAccessor {
    @Accessor
    BakedModel getModel();
}
