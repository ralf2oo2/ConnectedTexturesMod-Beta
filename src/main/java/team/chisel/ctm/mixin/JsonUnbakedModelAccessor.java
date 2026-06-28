package team.chisel.ctm.mixin;

import com.mojang.datafixers.util.Either;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(JsonUnbakedModel.class)
public interface JsonUnbakedModelAccessor {
    @Accessor("parent")
    JsonUnbakedModel getParent();

    @Accessor("parentId")
    Identifier getParentId();

    @Accessor("textureMap")
    Map<String, Either<SpriteIdentifier, String>> getTextureMap();
}
