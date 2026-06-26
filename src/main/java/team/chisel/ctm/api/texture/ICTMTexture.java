package team.chisel.ctm.api.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.util.Identifier;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.client.util.BlockRenderLayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;

/**
 * Represents a CTM Texture/resource
 */
@ParametersAreNonnullByDefault
public interface ICTMTexture<T extends ITextureType> {

    /**
     * Transforms a quad to conform with this texture
     *
     * @param quad
     *            The Quad
     * @param context
     *            The Context NULL CONTEXT MEANS INVENTORY
     * @param quadGoal
     *            Amount of quads that should be made
     * @return A List of Quads
     */
    List<BakedQuad> transformQuad(BakedQuad quad, @Nullable ITextureContext context, int quadGoal);

    Collection<Identifier> getTextures();

    /**
     * Gets the block render type of this texture
     *
     * @return The Rendertype of this texture
     */
    T getType();

    /**
     * Gets the texture for a particle
     *
     * @return The Texture for a particle
     */
    Sprite getParticle();

    /**
     * The layer this texture requires. The layers will be prioritized for a face in the order:
     * <ul>
     * <li>{@link BlockRenderLayer#TRANSLUCENT}</li>
     * <li>{@link BlockRenderLayer#CUTOUT}</li>
     * <li>{@link BlockRenderLayer#SOLID}</li>
     * </ul>
     *
     * @return The layer of this texture.
     */
    BlockRenderLayer getLayer();
}
