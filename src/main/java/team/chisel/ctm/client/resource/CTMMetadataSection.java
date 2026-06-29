package team.chisel.ctm.client.resource;

import com.google.gson.JsonObject;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.client.util.BlockRenderLayer;

import javax.annotation.Nullable;

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
