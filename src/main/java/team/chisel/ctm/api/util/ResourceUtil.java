package team.chisel.ctm.api.util;

import com.google.gson.JsonParseException;
import net.modificationstation.stationapi.api.client.resource.ReloadableAssetsManager;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.resource.Resource;
import net.modificationstation.stationapi.api.resource.ResourceHelper;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.client.texture.IMetadataSectionCTM;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResourceUtil {

    public static Resource getResource(Sprite sprite) throws IOException {
        // TODO: figure out how to get this working
        // TODO: in this class I replaced sprite.getName() with sprite.getAtlasId(), this might be wrong
        return getResource(spriteToAbsolute(sprite.getAtlasId()));
    }

    public static Identifier spriteToAbsolute(Identifier sprite) {
        if (!sprite.getPath().startsWith("textures/")) {
            sprite = Identifier.of(sprite.getNamespace(), "textures/" + sprite.getPath());
        }
        if (!sprite.getPath().endsWith(".png")) {
            sprite = Identifier.of(sprite.getNamespace(), sprite.getPath() + ".png");
        }
        return sprite;
    }

    public static Resource getResource(Identifier res) throws IOException {
        return ReloadableAssetsManager.INSTANCE.getResource(res);
    }

    public static Resource getResourceUnsafe(Identifier res) {
        try {
            return getResource(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<Identifier, IMetadataSectionCTM> metadataCache = new HashMap<>();
    private static final IMetadataSectionCTM.Serializer SERIALIZER = new IMetadataSectionCTM.Serializer();

    public static @Nullable IMetadataSectionCTM getMetadata(Identifier res) throws IOException {
        // Note, semantically different from computeIfAbsent, as we DO care about keys mapped to null values
        if (metadataCache.containsKey(res)) {
            return metadataCache.get(res);
        }
        IMetadataSectionCTM ret;
        try (Resource resource = getResource(res)) {
            ret = resource.getMetadata().decode(SERIALIZER).orElse(null);
        } catch (FileNotFoundException e) {
            ret = null;
        } catch (JsonParseException e) {
            throw new IOException("Error loading metadata for location " + res, e);
        }
        metadataCache.put(res, ret);
        return ret;
    }

    public static @Nullable IMetadataSectionCTM getMetadata(Sprite sprite) throws IOException {
        return getMetadata(spriteToAbsolute(sprite.getAtlasId()));
    }

    public static @Nullable IMetadataSectionCTM getMetadataUnsafe(Sprite sprite) {
        try {
            return getMetadata(sprite);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void invalidateCaches() {
        metadataCache.clear();
    }
}
