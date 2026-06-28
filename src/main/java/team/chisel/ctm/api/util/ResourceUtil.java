package team.chisel.ctm.api.util;

import com.google.gson.JsonParseException;
import net.modificationstation.stationapi.api.client.resource.ReloadableAssetsManager;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.resource.Resource;
import net.modificationstation.stationapi.api.resource.ResourceHelper;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.CTM;
import team.chisel.ctm.client.resource.CTMMetadataReader;
import team.chisel.ctm.client.resource.CTMMetadataSection;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResourceUtil {
    private static final Map<Identifier, CTMMetadataSection> METADATA_CACHE = new HashMap<>();

    public static Optional<Resource> getResource(Identifier identifier) throws IOException {
        return ReloadableAssetsManager.INSTANCE.getResource(identifier);
    }

    public static Optional<Resource> getResource(Sprite sprite) throws IOException {
        return getResource(toTextureIdentifier(sprite.getContents().getId()));
    }

    public static Optional<Resource> getResourceUnsafe(Identifier identifier) {
        try {
            return getResource(identifier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static CTMMetadataSection getMetadata(Identifier identifier) throws IOException {
        if (METADATA_CACHE.containsKey(identifier)) {
            return METADATA_CACHE.get(identifier);
        }
        CTMMetadataSection metadata = null;
        Optional<Resource> resource = getResource(identifier);
        if(resource.isPresent()) {
            Optional<CTMMetadataSection> metadataOpt = resource.get().getMetadata().decode(CTMMetadataReader.INSTANCE);
            if(metadataOpt.isPresent()) {
                metadata = metadataOpt.get();
            }
        }

        METADATA_CACHE.put(identifier, metadata);
        return metadata;
    }

    @Nullable
    public static CTMMetadataSection getMetadata(Sprite sprite) throws IOException {
        return getMetadata(toTextureIdentifier(sprite.getContents().getId()));
    }

    @Nullable
    public static CTMMetadataSection getMetadataUnsafe(Identifier identifier) {
        try {
            return getMetadata(identifier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static CTMMetadataSection getMetadataUnsafe(Sprite sprite) {
        try {
            return getMetadata(sprite);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static CTMMetadataSection getMetadataSafe(Identifier identifier) {
        try {
            return getMetadata(identifier);
        } catch (FileNotFoundException e) {
            //
        } catch (Exception e) {
            CTM.LOGGER.error("Error loading metadata for resource " + identifier + ".", e);
        }
        return null;
    }

    @Nullable
    public static CTMMetadataSection getMetadataSafe(Sprite sprite) {
        try {
            return getMetadata(sprite);
        } catch (FileNotFoundException e) {
            // For virtual sprites, such as missingno
        } catch (Exception e) {
            CTM.LOGGER.error("Error loading metadata for sprite " + sprite.getContents().getId() + ".", e);
        }
        return null;
    }

    // TODO: check if minecraft textures can still be retrieved with the stationapi prefix
    public static Identifier toTextureIdentifier(Identifier identifier) {
        String path = identifier.getPath();
        if (!path.startsWith("stationapi/textures/")) {
            path = "stationapi/textures/" + path;
        }
        if (!path.endsWith(".png")) {
            path = path + ".png";
        }
        return path.equals(identifier.getPath()) ? identifier : Identifier.of(identifier.getNamespace(), path);
    }

    public static void invalidateCaches() {
        METADATA_CACHE.clear();
    }
}
