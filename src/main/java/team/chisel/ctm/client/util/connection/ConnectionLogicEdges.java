package team.chisel.ctm.client.util.connection;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.client.util.BitUtil;

public class ConnectionLogicEdges extends ConnectionLogic {
    protected boolean obscured;

    public boolean isObscured() {
        return obscured;
    }

    @Override
    protected void buildConnectionMapInner(@NotNull BlockStateView world, @NotNull BlockPos pos, @NotNull Direction side) {
        BlockPos obscuringPos = pos.offset(side);
        BlockState state = getConnectionState(world, pos, obscuringPos, side);
        BlockState obscuringState = getConnectionState(world, obscuringPos, pos, side);
        if (compare(state, obscuringState, side)) {
            obscured = true;
            return;
        }

        super.buildConnectionMapInner(world, pos, side);
    }

    @Override
    protected boolean isConnected(BlockStateView world, BlockPos pos, BlockPos connection, Direction side, BlockState state) {
        BlockState connectionState = getConnectionState(world, connection, pos, side);
        BlockState obscuringConnectionState = getConnectionState(world, connection.offset(side), pos, side);
        if (compare(state, connectionState, side) || compare(state, obscuringConnectionState, side)) {
            return true;
        }
        return false;
    }

    @Override
    public long serialize() {
        long data = super.serialize();
        if (obscured) {
            data = BitUtil.setBit(data, 8);
        }
        return data;
    }

    @Override
    public void deserialize(long data) {
        super.deserialize(data);
        obscured = BitUtil.getBit(data, 8);
    }
}
