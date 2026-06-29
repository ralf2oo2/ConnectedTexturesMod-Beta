package team.chisel.ctm.api.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.client.util.BlockRenderLayer;

import java.util.Collection;
import java.util.List;

/**
 * Represents a CTM Texture/resource
 */
public interface CTMTexture<T extends TextureType> {

    /**
     * Transforms a BakedQuad.
     *
     * @param bakedQuad The BakedQuad.
     * @param cullFace The cull face. This is not the same as the BakedQuad's face.
     * @param context The context. <b>If this is null, the mesh that is currently being built is for an item model.</b>
     * @return A Renderable.
     */
    List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, @Nullable TextureContext context);

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
