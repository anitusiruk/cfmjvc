package com.echoesofthepast.block.craft;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.imprint.ImprintAction;
import com.echoesofthepast.imprint.ImprintTarget;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.util.Tell;
import java.util.ArrayList;
import java.util.List;
import com.echoesofthepast.qi.QiStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Drying is environmental. A bundle keeps a running tally of what it has been hung in, and whichever
 * condition dominates decides what it becomes, so the same herb is three different ingredients
 * depending on where the rack stands.
 */
public class HerbDryingRackBlockEntity extends QiDeviceBlockEntity implements ImprintTarget {
    private static final int MAX_BUNDLES = 4;
    /** Exposure needed before a bundle is finished. */
    private static final int DONE = 1200;

    /** One hanging bundle. Sun, moon and smoke are counted separately. */
    private static final class Bundle {
        private Item item;
        private int sun;
        private int moon;
        private int smoke;

        private Bundle(Item item) {
            this.item = item;
        }

        private int total() {
            return this.sun + this.moon + this.smoke;
        }

        private Condition dominant() {
            if (this.smoke >= this.sun && this.smoke >= this.moon) return Condition.SMOKE;
            return this.sun >= this.moon ? Condition.SUN : Condition.MOON;
        }
    }

    private enum Condition { SUN, MOON, SMOKE }

    private final List<Bundle> bundles = new ArrayList<>();

    public HerbDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.HERB_DRYING_RACK.get(), pos, state, 0.0F);
    }

    @Override
    public @Nullable QiStorage qiStorage(@Nullable Direction side) {
        return null;
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.bundles.isEmpty()) return;
        if (this.age % 20 != 0) return;

        boolean openSky = level.canSeeSky(this.worldPosition);
        boolean bright = level.isBrightOutside();
        boolean smoky = this.smokeNearby(level);

        for (Bundle bundle : this.bundles) {
            if (smoky) {
                bundle.smoke += 20;
            } else if (openSky && bright) {
                bundle.sun += 20;
            } else if (openSky) {
                bundle.moon += 20;
            }
        }

        if (this.age % 100 == 0) {
            level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM,
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                1, 0.3, 0.1, 0.3, 0.0);
        }
        this.setChanged();
    }

    /** A censer burning within a couple of blocks counts as smoke. */
    private boolean smokeNearby(ServerLevel level) {
        for (BlockPos pos : BlockPos.betweenClosed(this.worldPosition.offset(-2, -2, -2), this.worldPosition.offset(2, 2, 2))) {
            if (level.getBlockEntity(pos) instanceof com.echoesofthepast.block.craft.IncenseCenserBlockEntity censer
                && censer.isBurning()) {
                return true;
            }
        }
        return false;
    }

    public boolean hang(Player player, ItemStack stack) {
        if (this.bundles.size() >= MAX_BUNDLES) {
            Tell.overlay(player, "eotp.message.rack_full");
            return true;
        }
        if (!isDryable(stack)) return false;

        this.bundles.add(new Bundle(stack.getItem()));
        stack.shrink(1);
        this.updateVisibleCount();
        this.setChanged();
        return true;
    }

    /** Taking down finished bundles; unfinished ones come back as they went up. */
    public void takeDown(Player player) {
        if (this.bundles.isEmpty()) {
            Tell.overlay(player, "eotp.message.rack_empty");
            return;
        }
        for (Bundle bundle : this.bundles) {
            ItemStack result = bundle.total() >= DONE
                ? dried(bundle.item, bundle.dominant())
                : new ItemStack(bundle.item);
            player.getInventory().placeItemBackInInventory(result);
        }
        int taken = this.bundles.size();
        this.bundles.clear();
        this.updateVisibleCount();
        this.setChanged();
        Tell.overlay(player, Component.translatable("eotp.message.rack_taken", taken));
    }

    private void updateVisibleCount() {
        if (this.level == null) return;
        BlockState state = this.getBlockState();
        if (state.hasProperty(HerbDryingRackBlock.HANGING)) {
            this.level.setBlockAndUpdate(this.worldPosition, state.setValue(HerbDryingRackBlock.HANGING, this.bundles.size()));
        }
    }

    private static boolean isDryable(ItemStack stack) {
        return stack.is(EOTPItems.MOON_LOTUS_PETAL.get())
            || stack.is(EOTPItems.GINSENG_ROOT.get())
            || stack.is(EOTPItems.LINGZHI_CAP.get())
            || stack.is(EOTPItems.SPIRIT_BAMBOO_SHOOT.get())
            || stack.is(Items.WHEAT)
            || stack.is(Items.FERN)
            || stack.is(Items.LARGE_FERN);
    }

    /**
     * What a properly finished bundle becomes. The pairings are the useful part of the mechanic: sun
     * concentrates, moonlight refines, smoke infuses.
     */
    private static ItemStack dried(Item item, Condition condition) {
        if (item == EOTPItems.MOON_LOTUS_PETAL.get()) {
            return switch (condition) {
                case MOON -> new ItemStack(EOTPItems.WATER_ESSENCE.get());
                case SMOKE -> new ItemStack(EOTPItems.ECHO_ESSENCE.get());
                case SUN -> new ItemStack(EOTPItems.FIRE_ESSENCE.get());
            };
        }
        if (item == EOTPItems.GINSENG_ROOT.get()) {
            return switch (condition) {
                case SUN -> new ItemStack(EOTPItems.EARTH_ESSENCE.get());
                case SMOKE -> new ItemStack(EOTPItems.METAL_ESSENCE.get());
                case MOON -> new ItemStack(EOTPItems.WOOD_ESSENCE.get());
            };
        }
        if (item == EOTPItems.LINGZHI_CAP.get()) {
            return new ItemStack(EOTPItems.PURIFIED_LINGZHI.get());
        }
        if (item == EOTPItems.SPIRIT_BAMBOO_SHOOT.get()) {
            return new ItemStack(condition == Condition.SMOKE ? EOTPItems.HOLLOW_BAMBOO.get() : EOTPItems.WOOD_ESSENCE.get());
        }
        return new ItemStack(item);
    }

    @Override
    public boolean acceptImprint(ServerLevel level, ImprintAction action, ItemStack offered) {
        return switch (action) {
            case FEED -> !offered.isEmpty() && isDryable(offered) && this.bundles.size() < MAX_BUNDLES
                && this.bundles.add(new Bundle(offered.getItem()));
            case HARVEST -> {
                // A tablet only takes down what is actually finished.
                boolean took = this.bundles.removeIf(bundle -> {
                    if (bundle.total() < DONE) return false;
                    net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                        level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.2, this.worldPosition.getZ() + 0.5,
                        dried(bundle.item, bundle.dominant()));
                    level.addFreshEntity(drop);
                    return true;
                });
                if (took) this.updateVisibleCount();
                yield took;
            }
            default -> false;
        };
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        var list = output.childrenList("bundles");
        for (Bundle bundle : this.bundles) {
            ValueOutput child = list.addChild();
            child.store("item", Identifier.CODEC, BuiltInRegistries.ITEM.getKey(bundle.item));
            child.putInt("sun", bundle.sun);
            child.putInt("moon", bundle.moon);
            child.putInt("smoke", bundle.smoke);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.bundles.clear();
        input.childrenList("bundles").ifPresent(list -> {
            for (ValueInput child : list) {
                child.read("item", Identifier.CODEC)
                    .flatMap(BuiltInRegistries.ITEM::getOptional)
                    .ifPresent(item -> {
                        Bundle bundle = new Bundle(item);
                        bundle.sun = child.getIntOr("sun", 0);
                        bundle.moon = child.getIntOr("moon", 0);
                        bundle.smoke = child.getIntOr("smoke", 0);
                        this.bundles.add(bundle);
                    });
            }
        });
    }
}
