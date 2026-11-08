package team.chisel.ctm.init;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.Block;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.client.event.render.model.UnbakedModelLoadingFinishedEvent;
import net.modificationstation.stationapi.api.client.render.block.BlockModels;
import net.modificationstation.stationapi.api.client.render.model.ModelIdentifier;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.registry.BlockRegistry;
import team.chisel.ctm.api.util.ResourceUtil;
import team.chisel.ctm.client.model.JsonCTMUnbakedModel;
import team.chisel.ctm.client.resource.CTMMetadataSection;

import java.util.*;

public class UnbakedModelLoadingFinishedListener {

    private static final Int2IntOpenHashMap BLOCK_LAYER_MASKS = new Int2IntOpenHashMap();

    static {
        BLOCK_LAYER_MASKS.defaultReturnValue(0);
    }

    @EventListener
    public void onUnbakedModelLoadingFinished(UnbakedModelLoadingFinishedEvent event) {
        BLOCK_LAYER_MASKS.clear();

        for(Block block : BlockRegistry.INSTANCE) {
            int blockId = block.id;
            int layerMask = 0;

            for(BlockState state : block.getStateManager().getStates()) {
                ModelIdentifier modelId = BlockModels.getModelId(state);

                UnbakedModel unbakedModel = event.unbakedModels.get(modelId.asIdentifier());
                Set<Pair<String, String>> unresolved = new HashSet<>();
                Collection<SpriteIdentifier> textures = unbakedModel.getTextures(event.modelLoader::getOrLoadModel, unresolved);

                if(textures != null) {
                    for (SpriteIdentifier spriteId : textures) {
                        CTMMetadataSection metadata = ResourceUtil.getMetadataSafe(ResourceUtil.toTextureIdentifier(spriteId.texture));
                        if (metadata != null && metadata.getLayer() != null) {
                            int layerIndex = metadata.getLayer().layerIndex;
                            layerMask |= (1 << layerIndex);
                        }
                    }
                }
            }

            if (layerMask != 0) {
                BLOCK_LAYER_MASKS.put(blockId, layerMask);
            }
        }
    }

    public static boolean shouldRenderInLayer(int blockId, int layerIndex) {
        int mask = BLOCK_LAYER_MASKS.get(blockId);
        return (mask & (1 << layerIndex)) != 0;
    }
}
