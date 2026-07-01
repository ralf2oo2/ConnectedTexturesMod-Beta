package team.chisel.ctm.client.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;
import team.chisel.ctm.client.texture.type.TextureTypeOptifineFull;
import team.chisel.ctm.client.util.connection.ConnectionDirection;
import team.chisel.ctm.client.util.connection.ConnectionLogic;
import team.chisel.ctm.config.Config;

import java.util.Arrays;
import java.util.List;

public class TextureOptifineFull extends AbstractConnectingTexture<TextureTypeOptifineFull>{
    private static final int[] CTM_LOOKUP = new int[256];

    static {
        Arrays.fill(CTM_LOOKUP, 0);

        setupLookupTable();
    }

    public TextureOptifineFull(TextureTypeOptifineFull type, TextureInfo info) {
        super(type, info);
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, @Nullable TextureContext context) {
        UnbakedQuad quad = unbake(bakedQuad, cullFace);

        if (Config.CONFIG.disableCtm || !(context instanceof TextureContextConnecting)) {
            quad.setUVBounds(sprites[0]);
            return List.of(quad.bake());
        }

        ConnectionLogic logic = ((TextureContextConnecting) context).getLogic(quad.lightFace);

        boolean top = logic.connected(ConnectionDirection.TOP);
        boolean bottom = logic.connected(ConnectionDirection.BOTTOM);
        boolean left = logic.connected(ConnectionDirection.LEFT);
        boolean right = logic.connected(ConnectionDirection.RIGHT);
        boolean topLeft = logic.connected(ConnectionDirection.TOP_LEFT);
        boolean topRight = logic.connected(ConnectionDirection.TOP_RIGHT);
        boolean bottomLeft = logic.connected(ConnectionDirection.BOTTOM_LEFT);
        boolean bottomRight = logic.connected(ConnectionDirection.BOTTOM_RIGHT);

        int tileId = getTileIndex(top, bottom, left, right, topLeft, topRight, bottomLeft, bottomRight);

        quad.setUVBounds(sprites[1]);
        quad.applySubmap(getFullCtmSubmap(tileId));

        return List.of(quad.bake());
    }

    private Submap getFullCtmSubmap(int index) {
        float intervalX = 16.0F / 12;
        float intervalY = 16.0F / 4;
        float minX = intervalX * (index % 12);
        float minY = intervalY * (float)(index / 12);
        return new SubmapImpl(intervalX, intervalY, minX, minY);
    }

    private int getTileIndex(boolean top, boolean bottom, boolean left, boolean right, boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight) {
        int mask = 0;
        if (top)         mask |= 1;
        if (topRight)    mask |= 2;
        if (right)       mask |= 4;
        if (bottomRight) mask |= 8;
        if (bottom)      mask |= 16;
        if (bottomLeft)  mask |= 32;
        if (left)        mask |= 64;
        if (topLeft)     mask |= 128;

        return CTM_LOOKUP[mask];
    }

    private static void map(int x, int y) {
        int tileId = y * 12 + x;

        for (int mask = 0; mask < 256; mask++) {
            boolean t  = (mask & 1) != 0;
            boolean tr = (mask & 2) != 0;
            boolean r  = (mask & 4) != 0;
            boolean br = (mask & 8) != 0;
            boolean b  = (mask & 16) != 0;
            boolean bl = (mask & 32) != 0;
            boolean l  = (mask & 64) != 0;
            boolean tl = (mask & 128) != 0;

            if (checkRule(x, y, t, tr, r, br, b, bl, l, tl)) {
                CTM_LOOKUP[mask] = tileId;
            }
        }
    }

    private static void setupLookupTable() {
        for (int y = 0; y <= 3; y++) {
            for (int x = 0; x <= 11; x++) {
                if (y == 3 && x == 11) {
                    break;
                }
                map(x, y);
            }
        }
    }

    private static boolean checkRule(int x, int y, boolean t, boolean tr, boolean r, boolean br, boolean b, boolean bl, boolean l, boolean tl) {
        tr = tr && t && r;
        br = br && b && r;
        bl = bl && b && l;
        tl = tl && t && l;

        switch (y) {
            case 0:
                switch (x) {
                    case 0: return !l && !b && !r && !t;
                    case 1: return r && !l && !b && !t;
                    case 2: return l && r && !b && !t;
                    case 3: return l && !b && !r && !t;
                    case 4: return b && r && !l && !br && !t;
                    case 5: return l && b && !bl && !r && !t;
                    case 6: return b && r && t && !l && !br && !tr;
                    case 7: return l && b && r && !bl && !br && !t;
                    case 8: return l && b && r && tr && t && !tl && !bl && !br;
                    case 9: return l && b && br && r && t && !tl && !bl && !tr;
                    case 10: return tl && l && bl && b && r && t && !br && !tr;
                    case 11: return tl && l && b && r && tr && t && !bl && !br;
                }
                break;
            case 1:
                switch (x) {
                    case 0: return b && !l && !r && !t;
                    case 1: return b && br && r && !l && !t;
                    case 2: return l && bl && b && br && r && !t;
                    case 3: return l && bl && b && !r && !t;
                    case 4: return r && t && !l && !b && !tr;
                    case 5: return l && t && !tl && !b && !r;
                    case 6: return l && r && t && !tl && !b && !tr;
                    case 7: return l && b && t && !tl && !bl && !r;
                    case 8: return tl && l && b && r && t && !bl && !br && !tr;
                    case 9: return l && bl && b && r && t && !tl && !br && !tr;
                    case 10: return l && bl && b && br && r && t && !tl && !tr;
                    case 11: return l && b && br && r && tr && t && !tl && !bl;
                }
                break;
            case 2:
                switch (x) {
                    case 0: return b && t && !l && !r;
                    case 1: return b && br && r && tr && t && !l;
                    case 2: return tl && l && bl && b && br && r && tr && t;
                    case 3: return tl && l && bl && b && t && !r;
                    case 4: return b && br && r && t && !l && !tr;
                    case 5: return l && bl && b && r && !br && !t;
                    case 6: return b && r && tr && t && !l && !br;
                    case 7: return l && b && br && r && !bl && !t;
                    case 8: return tl && l && bl && b && r && tr && t && !br;
                    case 9: return tl && l && b && br && r && tr && t && !bl;
                    case 10: return l && bl && b && r && tr && t && !tl && !br;
                    case 11: return tl && l && b && br && r && t && !bl && !tr;
                }
                break;
            case 3:
                switch (x) {
                    case 0: return t && !l && !b && !r;
                    case 1: return r && tr && t && !l && !b;
                    case 2: return tl && l && r && tr && t && !b;
                    case 3: return tl && l && t && !b && !r;
                    case 4: return l && r && tr && t && !tl && !b;
                    case 5: return tl && l && b && t && !bl && !r;
                    case 6: return tl && l && r && t && !b && !tr;
                    case 7: return l && bl && b && t && !tl && !r;
                    case 8: return tl && l && bl && b && br && r && t && !tr;
                    case 9: return l && bl && b && br && r && tr && t && !tl;
                    case 10: return l && b && r && t && !tl && !bl && !br && !tr;
                }
                break;
        }
        return false;
    }
}
