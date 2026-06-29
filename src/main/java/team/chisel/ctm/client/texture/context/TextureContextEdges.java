package team.chisel.ctm.client.texture.context;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.util.connection.ConnectionLogicEdges;

public class TextureContextEdges extends TextureContextConnecting {
    public TextureContextEdges(@NotNull BlockState state, BlockStateView world, BlockPos pos, AbstractConnectingTexture<?> texture) {
        super(state, world, pos, texture);
    }

    @Override
    protected ConnectionLogicEdges createLogic(BlockStateView world, BlockPos pos, Direction face) {
        ConnectionLogicEdges logic = new ConnectionLogicEdges();
        texture.configureLogic(logic);
        logic.buildConnectionMap(world, pos, face);
        return logic;
    }

    @Override
    public ConnectionLogicEdges getLogic(Direction face) {
        return (ConnectionLogicEdges) super.getLogic(face);
    }
}
