package team.chisel.ctm.client.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextPillar;
import team.chisel.ctm.client.texture.type.TextureTypePillar;
import team.chisel.ctm.client.util.connection.SpacialConnectionLogic;
import team.chisel.ctm.config.Config;

import java.util.List;

import static net.modificationstation.stationapi.api.util.math.Direction.*;
import static net.modificationstation.stationapi.api.util.math.Direction.Axis.*;

public class TexturePillar extends AbstractTexture<TextureTypePillar> {
    public TexturePillar(TextureTypePillar type, TextureInfo info) {
        super(type, info);
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
        UnbakedQuad quad = unbake(bakedQuad, cullFace);
        Direction lightFace = quad.lightFace;

        if (Config.CONFIG.disableCtm || !(context instanceof TextureContextPillar) || !((TextureContextPillar) context).getLogic().hasConnections()) {
            if (lightFace.getAxis().isVertical()) {
                quad.setUVBounds(sprites[0]);
            } else {
                quad.setUVBounds(sprites[1]);
                quad.applySubmap(SubmapImpl.X2[0][0]);
            }
            return List.of(quad.bake());
        }

        SpacialConnectionLogic logic = ((TextureContextPillar) context).getLogic();
        Direction.Axis connectionAxis = getConnectionAxis(logic);

        quad.rotateUVs(getRotation(connectionAxis, lightFace));

        if (lightFace.getAxis() == connectionAxis) {
            quad.setUVBounds(sprites[0]);
        } else {
            Submap submap = SubmapImpl.X2[0][0];
            if (connectionAxis == Y) {
                submap = getSubmap(logic, UP, DOWN);
            } else if (connectionAxis == X) {
                submap = getSubmap(logic, EAST, WEST);
            } else if (connectionAxis == Z) {
                submap = getSubmap(logic, NORTH, SOUTH); // Flipped on purpose
            }

            quad.setUVBounds(sprites[1]);
            quad.applySubmap(submap);
        }
        return List.of(quad.bake());
    }

    private Axis getConnectionAxis(SpacialConnectionLogic logic) {
        if (logic.connectedOr(UP, DOWN)) {
            return Y;
        }
        if (logic.connectedOr(EAST, WEST)) {
            return X;
        }
        if (logic.connectedOr(SOUTH, NORTH)) {
            return Z;
        }
        return null;
    }

    private int getRotation(Axis connectionAxis, Direction lightFace) {
        int rotation = 0;
        if (connectionAxis != Y) {
            if (connectionAxis == X) {
                rotation = 3;
            } else if (connectionAxis == Z && lightFace == DOWN) {
                rotation = 2;
            }
            if (lightFace == WEST) {
                rotation += 1;
            } else if (lightFace == NORTH) {
                rotation += 2;
            } else if (lightFace == EAST) {
                rotation += 3;
            }
        }
        return rotation;
    }

    private Submap getSubmap(SpacialConnectionLogic logic, Direction direction1, Direction direction2) {
        boolean connected1 = logic.connected(direction1);
        boolean connected2 = logic.connected(direction2);
        if (connected1 && connected2) {
            return SubmapImpl.X2[1][0];
        }
        if (connected1) {
            return SubmapImpl.X2[1][1];
        }
        if (connected2) {
            return SubmapImpl.X2[0][1];
        }
        return SubmapImpl.X2[0][0];
    }
}
