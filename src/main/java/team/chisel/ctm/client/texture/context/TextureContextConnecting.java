package team.chisel.ctm.client.texture.context;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.util.connection.ConnectionLogic;

import java.util.EnumMap;

public class TextureContextConnecting implements TextureContext {
    public static final int CONNECTION_DATA_LENGTH = 10;

    protected final AbstractConnectingTexture<?> texture;
    private EnumMap<Direction, ConnectionLogic> logicMap = new EnumMap<>(Direction.class);
    private long serialized;

    public TextureContextConnecting(@NotNull BlockState state, BlockStateView world, BlockPos pos, AbstractConnectingTexture<?> texture) {
        this.texture = texture;

        for (Direction face : Direction.values()) {
            ConnectionLogic logic = createLogic(world, pos, face);
            logicMap.put(face, logic);
            serialized |= (logic.serialize() & ((1 << CONNECTION_DATA_LENGTH) - 1)) << (face.ordinal() * CONNECTION_DATA_LENGTH);
        }
    }

    protected ConnectionLogic createLogic(BlockStateView world, BlockPos pos, Direction face) {
        ConnectionLogic logic = new ConnectionLogic();
        texture.configureLogic(logic);
        logic.buildConnectionMap(world, pos, face);
        return logic;
    }

    public ConnectionLogic getLogic(Direction face) {
        return logicMap.get(face);
    }

    @Override
    public long serialize() {
        return serialized;
    }
}
