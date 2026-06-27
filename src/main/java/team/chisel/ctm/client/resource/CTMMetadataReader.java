package team.chisel.ctm.client.resource;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.modificationstation.stationapi.api.resource.metadata.ResourceMetadataReader;
import net.modificationstation.stationapi.api.util.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

public class CTMMetadataReader implements ResourceMetadataReader<CTMMetadataSection> {
    public static final String SECTION_KEY = "ctm";

    public static final CTMMetadataReader INSTANCE = new CTMMetadataReader();

    private static final Map<Integer, CTMMetadataFactory> FACTORIES = new ImmutableMap.Builder<Integer, CTMMetadataFactory>()
                                                                              .put(1, CTMMetadataSectionV1::fromJson)
                                                                              .build();

    @Override
    @Nullable
    public CTMMetadataSection fromJson(@Nullable JsonObject jsonObject) throws JsonParseException {
        if (jsonObject != null) {
            if (jsonObject.has("ctm_version")) {
                CTMMetadataFactory factory = FACTORIES.get(jsonObject.get("ctm_version").getAsInt());
                if (factory == null) {
                    throw new JsonParseException("Invalid \"ctm_version\"");
                } else {
                    return factory.getCTMMetadata(jsonObject, this::makeIdentifier);
                }
            } else {
                throw new JsonParseException("Found ctm section without \"ctm_version\"");
            }
        }
        return null;
    }

    @Override
    @NotNull
    public String getKey() {
        return SECTION_KEY;
    }

    public Identifier makeIdentifier(String string) {
        return Identifier.of(string);
    }

    public interface CTMMetadataFactory {
        CTMMetadataSection getCTMMetadata(JsonObject jsonObject, Function<String, Identifier> identifierProvider);
    }
}
