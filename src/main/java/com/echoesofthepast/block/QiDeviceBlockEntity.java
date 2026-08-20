package com.echoesofthepast.block;

import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiNode;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.qi.QiVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Shared body for every block that holds Qi. Handles storage, leakage, the ambient particle
 * language and the once-a-second housekeeping that all devices share.
 */
public abstract class QiDeviceBlockEntity extends BlockEntity implements QiNode {
    protected final QiStorage storage;
    protected @Nullable Direction lastInflow;
    protected int age;

    protected QiDeviceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, float capacity) {
        super(type, pos, state);
        this.storage = new QiStorage(capacity);
    }

    public QiStorage storage() {
        return this.storage;
    }

    @Override
    public @Nullable QiStorage qiStorage(@Nullable Direction side) {
        return this.storage;
    }

    @Override
    public void onQiArrived(Direction side, float amount) {
        this.lastInflow = side;
    }

    /** Device specific behaviour, run every tick on the server. */
    protected abstract void deviceTick(ServerLevel level);

    /** How often the device shows what it is holding. Quiet devices should be quiet. */
    protected int idleParticleInterval() {
        return 20;
    }

    public void serverTick(ServerLevel level) {
        this.age++;
        float lost = this.storage.leak(QiNet.insulation(level, this.worldPosition));
        if (lost > 0.02F) {
            QiVisuals.leak(level, this.worldPosition, this.storage.blend(), lost);
        }
        this.deviceTick(level);
        int interval = this.idleParticleInterval();
        if (interval > 0 && this.age % interval == 0) {
            QiVisuals.resting(level, this.worldPosition, this.storage);
        }
    }

    /** Convenience for a device that wants neighbours and clients to notice a state change. */
    protected void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.storage.save(output, "qi");
        if (this.lastInflow != null) {
            output.putString("inflow", this.lastInflow.getSerializedName());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storage.load(input, "qi");
        this.lastInflow = input.getString("inflow").map(Direction::byName).orElse(null);
    }

    /** Ticker every device block can hand back from {@code getTicker}. */
    public static <T extends BlockEntity> BlockEntityTicker<T> ticker() {
        return (level, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel && blockEntity instanceof QiDeviceBlockEntity device) {
                device.serverTick(serverLevel);
            }
        };
    }

    public static boolean isServer(Level level) {
        return level instanceof ServerLevel;
    }
}
