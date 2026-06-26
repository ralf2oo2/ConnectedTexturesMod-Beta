package team.chisel.ctm.api.model;

import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.client.util.BlockRenderLayer;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IUnbakedModelCTM extends UnbakedModel {
    UnbakedModel getVanillaParent();

    void load();

    Collection<ICTMTexture<?>> getCTMTextures();

    ICTMTexture<?> getTexture(String iconName);

    boolean canRenderInLayer(BlockState state, BlockRenderLayer layer);

    @Nullable
    Sprite getOverrideSprite(int tintIndex);

    @Nullable
    ICTMTexture<?> getOverrideTexture(int tintIndex, String sprite);
}
