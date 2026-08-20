package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiNode;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.registry.EOTPBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Divides Qi among the eight positions around it, including the corners, which is what makes a bagua
 * feel like a bagua rather than a four-way splitter.
 */
public class BaguaDistributorBlockEntity extends QiDeviceBlockEntity {
    /** The eight trigram positions, in clockwise order starting from north. */
    private static final BlockPos[] RING = new BlockPos[] {
        new BlockPos(0, 0, -1),
        new BlockPos(1, 0, -1),
        new BlockPos(1, 0, 0),
        new BlockPos(1, 0, 1),
        new BlockPos(0, 0, 1),
        new BlockPos(-1, 0, 1),
        new BlockPos(-1, 0, 0),
        new BlockPos(-1, 0, -1)
    };

    private BaguaMode mode = BaguaMode.ALTERNATE;
    private int cursor;

    public BaguaDistributorBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.BAGUA_DISTRIBUTOR.get(), pos, state, 60.0F);
    }

    public BaguaMode mode() {
        return this.mode;
    }

    public BaguaMode cycleMode() {
        this.mode = this.mode.next();
        this.cursor = 0;
        this.setChanged();
        return this.mode;
    }

    @Override
    protected int idleParticleInterval() {
        return 40;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.storage.fillRatio() < 0.05F) return;

        float budget = Math.min(this.storage.amount(), 12.0F);
        int front = this.frontIndex();

        switch (this.mode) {
            case ALTERNATE -> {
                if (this.sendTo(level, this.cursor, budget) > 0.0F) {
                    this.cursor = (this.cursor + 1) % RING.length;
                }
            }
            case CLOCKWISE -> {
                this.sendTo(level, this.cursor, budget);
                this.cursor = (this.cursor + 1) % RING.length;
            }
            case OPPOSED -> {
                this.sendTo(level, front, budget * 0.5F);
                this.sendTo(level, (front + 4) % RING.length, budget * 0.5F);
            }
            case PRIORITY -> {
                float sent = this.sendTo(level, front, budget);
                if (sent < budget * 0.5F) {
                    for (int offset = 1; offset < RING.length; offset++) {
                        this.sendTo(level, (front + offset) % RING.length, budget * 0.25F);
                    }
                }
            }
            case SPREAD -> {
                float share = budget / RING.length;
                for (int index = 0; index < RING.length; index++) {
                    this.sendTo(level, index, share);
                }
            }
            case GENERATIVE -> this.sendGeneratively(level, budget);
        }
    }

    /** Which ring slot the disc's markings point at. */
    private int frontIndex() {
        Direction facing = this.getBlockState().getValue(BaguaDistributorBlock.FACING);
        return switch (facing) {
            case NORTH -> 0;
            case EAST -> 2;
            case SOUTH -> 4;
            case WEST -> 6;
            default -> 0;
        };
    }

    private float sendTo(ServerLevel level, int index, float amount) {
        if (amount <= 0.01F) return 0.0F;
        BlockPos target = this.worldPosition.offset(RING[index % RING.length]);
        // Corners cost a little more to reach than the straight sides.
        float efficiency = isCorner(index) ? 0.88F : 0.97F;
        return QiNet.pushToPos(level, this.worldPosition, this.storage, target, amount, efficiency);
    }

    private static boolean isCorner(int index) {
        return index % 2 == 1;
    }

    /**
     * Looks at what each neighbour already holds and prefers the ones this Qi would feed rather than
     * fight. Costs throughput, protects reservoirs.
     */
    private void sendGeneratively(ServerLevel level, float budget) {
        Phase dominant = this.storage.blend().dominant();
        if (dominant == null) {
            float share = budget / RING.length;
            for (int index = 0; index < RING.length; index++) {
                this.sendTo(level, index, share);
            }
            return;
        }

        List<Integer> welcoming = new ArrayList<>();
        List<Integer> neutral = new ArrayList<>();
        for (int index = 0; index < RING.length; index++) {
            QiStorage storage = neighbourStorage(level, this.worldPosition.offset(RING[index]));
            if (storage == null) continue;
            Phase theirs = storage.blend().dominant();
            if (theirs == null || theirs == dominant || dominant.generates() == theirs) {
                welcoming.add(index);
            } else if (dominant.affinity(theirs) >= 0.0F) {
                neutral.add(index);
            }
        }

        List<Integer> chosen = welcoming.isEmpty() ? neutral : welcoming;
        if (chosen.isEmpty()) return;
        float share = budget / chosen.size();
        for (int index : chosen) {
            this.sendTo(level, index, share);
        }
    }

    private static @Nullable QiStorage neighbourStorage(ServerLevel level, BlockPos pos) {
        QiNode node = QiNet.nodeAt(level, pos);
        return node == null ? null : node.qiStorage(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("mode", BaguaMode.CODEC, this.mode);
        output.putInt("cursor", this.cursor);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.mode = input.read("mode", BaguaMode.CODEC).orElse(BaguaMode.ALTERNATE);
        this.cursor = input.getIntOr("cursor", 0);
    }
}
