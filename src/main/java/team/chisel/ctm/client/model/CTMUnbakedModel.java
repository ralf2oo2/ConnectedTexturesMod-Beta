package team.chisel.ctm.client.model;

import net.modificationstation.stationapi.api.client.render.model.BakedModel;
import net.modificationstation.stationapi.api.client.render.model.Baker;
import net.modificationstation.stationapi.api.client.render.model.ModelBakeSettings;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CTMUnbakedModel implements UnbakedModel {
    private final UnbakedModel parent;

    private Set<SpriteIdentifier> textureDependencies;

    public CTMUnbakedModel(UnbakedModel parent) {
        this.parent = parent;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return parent.getModelDependencies();
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> parents) {

    }

    @Override
    public @Nullable BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        return null;
    }
}
