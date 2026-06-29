package team.chisel.ctm.client.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;
import team.chisel.ctm.client.texture.type.TextureTypeSCTM;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogic;
import team.chisel.ctm.config.Config;

import java.util.List;
import java.util.Optional;

public class TextureSCTM extends AbstractConnectingTexture<TextureTypeSCTM> {
    public TextureSCTM(TextureTypeSCTM type, TextureInfo info) {
        super(type, info);

        connectInside = Optional.of(true);
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
        UnbakedQuad quad = unbake(bakedQuad, cullFace);

        int submapId = 0;
        if (!Config.CONFIG.disableCtm && context instanceof TextureContextConnecting) {
            submapId = getSubmapId(((TextureContextConnecting) context).getLogic(quad.lightFace));
        }

        quad.setUVBounds(sprites[0]);
        quad.applySubmap(TextureCTM.getX2Submap(submapId, quad.areUVsRotatedOnce()));
        return List.of(quad.bake());
    }

    private int getSubmapId(ConnectionLogic logic) {
        if (logic == null) {
            return 0;
        }
        boolean top = logic.connected(ConnectionDirection.TOP);
        boolean bottom = logic.connected(ConnectionDirection.BOTTOM);
        boolean left = logic.connected(ConnectionDirection.LEFT);
        boolean right = logic.connected(ConnectionDirection.RIGHT);
        if (top || bottom || left || right) {
            if (!top || !bottom) {
                return (left && right) ? 1 : 0;
            }
            if (!left || !right) {
                return 2;
            }
            if (logic.connected(ConnectionDirection.TOP_LEFT) && logic.connected(ConnectionDirection.TOP_RIGHT)) {
                if (logic.connected(ConnectionDirection.BOTTOM_LEFT) && logic.connected(ConnectionDirection.BOTTOM_RIGHT)) {
                    return 3;
                }
            }
        }
        return 0;
    }
}
