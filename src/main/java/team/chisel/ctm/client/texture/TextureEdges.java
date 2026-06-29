package team.chisel.ctm.client.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextEdges;
import team.chisel.ctm.client.texture.type.TextureTypeEdges;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogic;
import team.chisel.ctm.client.util.connection.ConnectionLogicEdges;
import team.chisel.ctm.config.Config;

import java.util.List;

public class TextureEdges extends TextureCTM {
    public TextureEdges(TextureTypeEdges type, TextureInfo info) {
        super(type, info);
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
        if (Config.CONFIG.disableCtm || !(context instanceof TextureContextEdges)) {
            UnbakedQuad quad = unbake(bakedQuad, cullFace);
            quad.setUVBounds(sprites[0]);
            return List.of(quad.bake());
        }

        ConnectionLogicEdges logic = ((TextureContextEdges) context).getLogic(bakedQuad.getFace());
        if (logic.isObscured()) {
            UnbakedQuad quad = unbake(bakedQuad, cullFace);
            quad.setUVBounds(sprites[2]);
            return List.of(quad.bake());
        }

        return super.transformQuad(bakedQuad, cullFace, context);
    }

    @Override
    protected int getSubmapId(ConnectionLogic logic, int quadrant) {
        ConnectionDirection[] directions = DIRECTION_MAP[quadrant];
        boolean connected1 = logic.connected(directions[0]);
        boolean connected2 = logic.connected(directions[1]);
        if (connected1 && connected2) {
            return 3;
        }
        if (connected1) {
            return 1;
        }
        if (connected2) {
            return 2;
        }
        if (logic.connected(directions[2])) {
            return 0;
        }
        return -1;
    }
}
