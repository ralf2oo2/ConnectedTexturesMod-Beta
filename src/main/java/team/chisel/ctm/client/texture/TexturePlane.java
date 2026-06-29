package team.chisel.ctm.client.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;
import team.chisel.ctm.client.texture.type.TextureTypePlane;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogic;
import team.chisel.ctm.config.Config;

import java.util.List;

public class TexturePlane extends AbstractConnectingTexture<TextureTypePlane> {
    private final Direction.Type plane;

    public TexturePlane(TextureTypePlane type, TextureInfo info) {
        super(type, info);
        plane = type.getPlane();
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
        UnbakedQuad quad = unbake(bakedQuad, cullFace);

        int submapId = 0;
        if (!Config.CONFIG.disableCtm && context instanceof TextureContextConnecting) {
            submapId = getSubmapId(((TextureContextConnecting) context).getLogic(quad.lightFace));
        }

        quad.setUVBounds(sprites[0]);
        quad.applySubmap(SubmapImpl.getX2Submap(submapId));
        return List.of(quad.bake());
    }

    private int getSubmapId(ConnectionLogic logic) {
        if (logic == null) {
            return 0;
        }
        int u;
        int v;
        if (plane == Direction.Type.VERTICAL) {
            boolean top = logic.connected(ConnectionDirection.TOP);
            u = (top == logic.connected(ConnectionDirection.BOTTOM)) ? 0 : 1;
            v = top ? 1 : 0;
        } else {
            boolean left = logic.connected(ConnectionDirection.LEFT);
            u = left ? 1 : 0;
            v = (left == logic.connected(ConnectionDirection.RIGHT)) ? 0 : 1;
        }
        return v * 2 + u;
    }
}
