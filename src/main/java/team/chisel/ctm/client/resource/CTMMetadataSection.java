package team.chisel.ctm.client.resource;

import com.google.gson.JsonObject;
import net.modificationstation.stationapi.api.util.Identifier;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.client.util.BlockRenderLayer;

public interface CTMMetadataSection {
    int getVersion();

    TextureType getType();

    BlockRenderLayer getLayer();

    Identifier[] getAdditionalTextures();

    @Nullable
    Identifier getProxy();

    @Nullable
    JsonObject getExtraData();
}
