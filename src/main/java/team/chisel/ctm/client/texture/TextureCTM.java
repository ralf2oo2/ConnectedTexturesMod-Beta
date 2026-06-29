package team.chisel.ctm.client.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;
import team.chisel.ctm.client.texture.type.TextureTypeCTM;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogic;
import team.chisel.ctm.config.Config;

import java.util.*;

import static team.chisel.ctm.client.util.connection.ConnectionDirection.*;

public class TextureCTM extends AbstractConnectingTexture<TextureTypeCTM> {
    protected static final ConnectionDirection[][] DIRECTION_MAP = new ConnectionDirection[][] {
            {TOP, LEFT, TOP_LEFT},
            {BOTTOM, LEFT, BOTTOM_LEFT},
            {BOTTOM, RIGHT, BOTTOM_RIGHT},
            {TOP, RIGHT, TOP_RIGHT}
    };

    public TextureCTM(TextureTypeCTM type, TextureInfo info) {
        super(type, info);
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
        UnbakedQuad quad = unbake(bakedQuad, cullFace);

        if (Config.CONFIG.disableCtm || !(context instanceof TextureContextConnecting)) {
            quad.setUVBounds(sprites[0]);
            return List.of(quad.bake());
        }

        ConnectionLogic logic = ((TextureContextConnecting) context).getLogic(quad.lightFace);
        UnbakedQuad[] quads = quad.toQuadrants();
        for (int i = 0; i < quads.length; i++) {
            if (quads[i] != null) {
                int id = getSubmapId(logic, i);
                if (id != -1) {
                    quads[i].setUVBounds(sprites[1]);
                    quads[i].applySubmap(getX2Submap(id, quad.areUVsRotatedOnce()));
                } else {
                    quads[i].setUVBounds(sprites[0]);
                }
            }
        }

        return Arrays.stream(quads).map(UnbakedQuad::bake).toList();
    }

    protected int getSubmapId(ConnectionLogic logic, int quadrant) {
        ConnectionDirection[] directions = DIRECTION_MAP[quadrant];
        boolean connected1 = logic.connected(directions[0]);
        boolean connected2 = logic.connected(directions[1]);
        if (connected1 && connected2) {
            if (logic.connected(directions[2])) {
                return 0;
            }
            return 3;
        }
        if (connected1) {
            return 1;
        }
        if (connected2) {
            return 2;
        }
        return -1;
    }

    public static Submap getX2Submap(int id, boolean rotate) {
        if (rotate) {
            if (id == 1) {
                id = 2;
            } else if (id == 2) {
                id = 1;
            }
        }
        return SubmapImpl.getX2Submap(id);
    }
}