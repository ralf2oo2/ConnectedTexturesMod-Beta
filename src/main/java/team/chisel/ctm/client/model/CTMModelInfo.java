package team.chisel.ctm.client.model;

import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.api.texture.ICTMTexture;

import javax.annotation.Nullable;
import java.util.Collection;

public interface CTMModelInfo {
    /**
     * <b>The returned collection's iterator must always return the same amount of elements in the same order.</b>
     *
     * @return A collection of all textures this model info contains.
     */
    Collection<ICTMTexture<?>> getTextures();

    ICTMTexture<?> getTexture(Identifier identifier);

    @Nullable
    default Sprite getOverrideSprite(int tintIndex) {
        return null;
    }

    @Nullable
    default ICTMTexture<?> getOverrideTexture(int tintIndex, Identifier identifier) {
        return null;
    }
}
