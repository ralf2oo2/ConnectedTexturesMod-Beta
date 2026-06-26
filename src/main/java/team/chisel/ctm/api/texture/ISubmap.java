package team.chisel.ctm.api.texture;

import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface ISubmap {

    float getYOffset();

    float getXOffset();

    float getWidth();

    float getHeight();

    float getInterpolatedU(Sprite sprite, float u);

    float getInterpolatedV(Sprite sprite, float v);

    float[] toArray();

    ISubmap normalize();

    ISubmap relativize();
}
