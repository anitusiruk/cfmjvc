package com.echoesofthepast.block.alchemy;

import com.echoesofthepast.alchemy.AlchemyRecipe;
import com.echoesofthepast.alchemy.PillQuality;
import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.cultivation.CoreThesis;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.imprint.ImprintAction;
import com.echoesofthepast.imprint.ImprintTarget;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.util.Tell;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Where pill quality actually comes from.
 *
 * <p>The cauldron scores itself continuously while it cooks. Every second it looks at how close the
 * heat is to what the batch wants, how well the Qi inside matches the phase relationship the recipe
 * needs, and how dirty the vessel is. The average of those scores over the whole cook is the grade,
 * so nothing here is rolled - a player who controls their conditions gets perfect pills every time,
 * and that is meant to be an engineering achievement.
 */
public class DingCauldronBlockEntity extends QiDeviceBlockEntity implements ImprintTarget {
    /** Working temperature range. Heat drifts towards what the fire below can sustain. */
    private static final int MAX_HEAT = 1200;
    /** Above this the batch scorches. */
    private static final int SCORCH_HEAT = 1050;
    /** Ticks a completed batch waits before it finishes, so the last ingredient can settle. */
    private static final int SETTLE_TICKS = 60;

    private final List<Item> added = new ArrayList<>();
    private int heat;
    /** Residue from previous batches. Cleaning matters, and nobody has to be told twice. */
    private float residue;
    private float scoreSum;
    private int scoreCount;
    private int settleTimer = -1;
    private boolean scorched;
    /** One Spirit Spring bucket blesses the current/next batch and makes Perfect quality reachable. */
    private boolean springInfused;

    public DingCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.DING_CAULDRON.get(), pos, state, 200.0F);
    }

    public int heat() {
        return this.heat;
    }

    public float residue() {
        return this.residue;
    }

    public List<Item> contents() {
        return List.copyOf(this.added);
    }

    /** The recipe the cauldron currently believes it is making, if the contents allow only one. */
    public @Nullable AlchemyRecipe intent() {
        List<AlchemyRecipe> candidates = AlchemyRecipe.candidates(this.added);
        return candidates.size() == 1 ? candidates.get(0) : null;
    }

    /** What the batch wants next. The alchemist's robe reads this off the smoke. */
    public @Nullable Item nextWanted() {
        AlchemyRecipe recipe = this.intent();
        return recipe == null ? null : recipe.nextIngredient(this.added);
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        this.updateHeat(level);
        this.swallowDroppedItems(level);

        if (this.age % 20 == 0) {
            this.scoreConditions();
        }
        if (this.age % 5 == 0) {
            this.showState(level);
        }

        if (this.settleTimer > 0) {
            this.settleTimer--;
        } else if (this.settleTimer == 0) {
            this.settleTimer = -1;
            this.finish(level);
        }
    }

    /**
     * Heat is driven by what burns underneath and nudged upwards by fire Qi in the vessel, so a
     * cauldron can be regulated either with fuel or with plumbing.
     */
    private void updateHeat(ServerLevel level) {
        BlockState below = level.getBlockState(this.worldPosition.below());
        int target = 0;
        if (below.is(Blocks.LAVA)) {
            target = 900;
        } else if (below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE)) {
            target = 700;
        } else if (below.getBlock() instanceof CampfireBlock && below.getValue(CampfireBlock.LIT)) {
            target = below.is(Blocks.SOUL_CAMPFIRE) ? 520 : 620;
        } else if (below.is(Blocks.MAGMA_BLOCK)) {
            target = 420;
        }

        float fireQi = this.storage.blend().get(Phase.FIRE) * this.storage.fillRatio();
        target += Math.round(fireQi * 420.0F);
        target = Math.min(MAX_HEAT, target);

        // Drift, so that a batch can be brought up and down deliberately.
        if (this.heat < target) {
            this.heat = Math.min(target, this.heat + 8);
        } else if (this.heat > target) {
            this.heat = Math.max(target, this.heat - 5);
        }

        if (this.heat >= SCORCH_HEAT && !this.added.isEmpty()) {
            this.scorched = true;
        }
    }

    /** Anything dropped into the pot is an ingredient, which is what makes automation physical. */
    private void swallowDroppedItems(ServerLevel level) {
        if (this.age % 4 != 0) return;
        AABB mouth = new AABB(this.worldPosition.above()).inflate(-0.15, 0.0, -0.15);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, mouth)) {
            ItemStack stack = entity.getItem();
            if (stack.isEmpty()) continue;
            if (this.addIngredient(level, stack.getItem())) {
                stack.shrink(1);
                if (stack.isEmpty()) entity.discard();
            }
        }
    }

    /**
     * @return true if the ingredient was taken.
     */
    public boolean addIngredient(ServerLevel level, Item item) {
        if (this.settleTimer >= 0) return false;

        List<Item> attempt = new ArrayList<>(this.added);
        attempt.add(item);
        if (AlchemyRecipe.candidates(attempt).isEmpty()) {
            // Nothing in the book starts like this. The batch is spoiled, but the ingredient is gone
            // either way - alchemy is not free.
            this.spoil(level);
            return true;
        }

        this.added.add(item);
        this.scoreConditions();
        level.playSound(null, this.worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6F, 1.0F);

        for (AlchemyRecipe recipe : AlchemyRecipe.candidates(this.added)) {
            if (recipe.isComplete(this.added)) {
                this.settleTimer = SETTLE_TICKS;
                break;
            }
        }
        this.setChanged();
        return true;
    }

    /** Records how good conditions are right now. Called every second while cooking. */
    private void scoreConditions() {
        if (this.added.isEmpty()) return;
        AlchemyRecipe recipe = this.intent();
        if (recipe == null) return;

        float heatScore = recipe.heatScore(this.heat);
        float phaseScore = this.storage.isEmpty()
            ? 0.25F
            : this.storage.blend().similarity(recipe.wantedBlend());
        float cleanliness = 1.0F - Math.min(1.0F, this.residue);
        float calm = 1.0F - this.storage.turbulence() * 0.5F;

        // Heat and phase matter most; a filthy pot caps the grade no matter how well you cook.
        // Ordinary water can make a Refined pill. Spirit Spring supplies the last 15% needed for
        // Perfect quality, making the rare fluid a process ingredient rather than a reskinned bucket.
        float spring = this.springInfused ? 0.15F : 0.0F;
        float score = (heatScore * 0.4F + phaseScore * 0.35F + calm * 0.1F + spring)
            * (0.55F + cleanliness * 0.45F);
        this.scoreSum += Math.max(0.0F, score);
        this.scoreCount++;
    }

    private void finish(ServerLevel level) {
        AlchemyRecipe recipe = this.intent();
        if (recipe == null || !recipe.isComplete(this.added)) {
            this.spoil(level);
            return;
        }

        float average = this.scoreCount == 0 ? 0.0F : this.scoreSum / this.scoreCount;
        if (this.scorched) {
            average *= 0.45F;
        }
        PillQuality quality = PillQuality.fromScore(average);
        ItemStack result = recipe.result(quality);

        this.eject(level, result);
        // Every batch leaves a film behind, and a scorched one leaves much more.
        this.residue = Math.min(2.0F, this.residue + (this.scorched ? 0.35F : 0.12F));
        this.reset();

        QiVisuals.bloom(level, Vec3.atCenterOf(this.worldPosition).add(0.0, 0.6, 0.0), recipe.wantedBlend());
        level.playSound(null, this.worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.4F);
        EchoLog.record(level, this.worldPosition, EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.pill_finished", Component.translatable(quality.translationKey())));

        if (quality == PillQuality.PERFECT) {
            for (Player nearby : level.getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(6.0))) {
                Cultivation.teach(nearby, Discovery.PILL_PERFECTION);
            }
        }
    }

    private void spoil(ServerLevel level) {
        if (!this.added.isEmpty()) {
            // Failed work is salvageable rather than simply gone. A Vermilion Furnace turns the
            // wasted heat of a botched batch into something living instead.
            int salvage = Math.max(1, this.added.size() / 2);
            boolean furnace = level.getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(8.0)).stream()
                .map(Cultivation::of)
                .anyMatch(cultivator -> cultivator != null
                    && cultivator.path().thesis() == CoreThesis.VERMILION_FURNACE);
            if (furnace) {
                this.eject(level, new ItemStack(EOTPItems.WOOD_ESSENCE.get(), 1));
            }
            this.eject(level, new ItemStack(EOTPItems.PILL_RESIDUE.get(), salvage));
        }
        this.residue = Math.min(2.0F, this.residue + 0.25F);
        this.reset();
        if (this.level instanceof ServerLevel serverLevel) {
            QiVisuals.backlash(serverLevel, Vec3.atCenterOf(this.worldPosition).add(0.0, 0.6, 0.0), this.storage.blend());
            serverLevel.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.7F);
        }
    }

    private void reset() {
        this.added.clear();
        this.scoreSum = 0.0F;
        this.scoreCount = 0;
        this.settleTimer = -1;
        this.scorched = false;
        this.springInfused = false;
        this.setChanged();
    }

    private void eject(ServerLevel level, ItemStack stack) {
        Vec3 above = Vec3.atCenterOf(this.worldPosition).add(0.0, 0.9, 0.0);
        ItemEntity entity = new ItemEntity(level, above.x, above.y, above.z, stack);
        entity.setDeltaMovement(0.0, 0.18, 0.0);
        level.addFreshEntity(entity);
    }

    /** Smoke colour and bubbling are the readout. */
    private void showState(ServerLevel level) {
        Vec3 mouth = Vec3.atCenterOf(this.worldPosition).add(0.0, 0.55, 0.0);
        if (this.heat > 60) {
            int count = 1 + this.heat / 400;
            level.sendParticles(ParticleTypes.SMOKE, mouth.x, mouth.y, mouth.z, count, 0.16, 0.02, 0.16, 0.01);
        }
        if (!this.added.isEmpty() && this.heat > 200) {
            // Colour comes from the Qi in the vessel, so a badly balanced batch looks wrong.
            level.sendParticles(
                new net.minecraft.core.particles.DustParticleOptions(this.storage.blend().color(), 1.0F),
                mouth.x, mouth.y, mouth.z, 2, 0.14, 0.05, 0.14, 0.02);
        }
        if (this.scorched && this.age % 20 == 0) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, mouth.x, mouth.y, mouth.z, 4, 0.2, 0.05, 0.2, 0.02);
        }
        if (this.residue > 0.6F && this.age % 40 == 0) {
            level.sendParticles(ParticleTypes.ASH, mouth.x, mouth.y, mouth.z, 3, 0.2, 0.05, 0.2, 0.0);
        }
    }

    /** Hand interaction: water cleans, anything edible-ish goes in, everything else is refused. */
    public boolean interactWith(Player player, ItemStack stack) {
        if (!(this.level instanceof ServerLevel level)) return false;

        if (stack.is(Items.WATER_BUCKET) || stack.is(EOTPItems.SPIRIT_SPRING_BUCKET.get())) {
            boolean spiritSpring = stack.is(EOTPItems.SPIRIT_SPRING_BUCKET.get());
            this.residue = 0.0F;
            this.springInfused |= spiritSpring;
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                player.getInventory().placeItemBackInInventory(new ItemStack(Items.BUCKET));
            }
            this.setChanged();
            Tell.overlay(player, spiritSpring ? "eotp.message.cauldron_spring_infused" : "eotp.message.cauldron_cleaned");
            level.playSound(null, this.worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 1.0F);
            return true;
        }

        if (stack.isEmpty()) return false;
        if (this.addIngredient(level, stack.getItem())) {
            stack.shrink(1);
            return true;
        }
        return false;
    }

    /** Empty-handed: the cauldron tells you what it is doing, in words, once. */
    public void report(Player player) {
        Item wanted = this.nextWanted();
        Component next = wanted == null
            ? Component.translatable("eotp.message.nothing_further")
            : Component.translatable(wanted.getDescriptionId());
        Tell.chat(player, Component.translatable("eotp.message.cauldron_state",
            this.heat,
            this.added.size(),
            Math.round(this.residue * 100.0F),
            this.storage.describe(),
            next));
    }

    // ------------------------------------------------------------------------------- imprinting

    @Override
    public boolean acceptImprint(ServerLevel level, ImprintAction action, ItemStack offered) {
        return switch (action) {
            case FEED -> !offered.isEmpty() && this.addIngredient(level, offered.getItem());
            case STIR -> {
                this.scoreConditions();
                yield true;
            }
            default -> false;
        };
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("heat", this.heat);
        output.putFloat("residue", this.residue);
        output.putFloat("score_sum", this.scoreSum);
        output.putInt("score_count", this.scoreCount);
        output.putInt("settle", this.settleTimer);
        output.putBoolean("scorched", this.scorched);
        output.putBoolean("spring_infused", this.springInfused);
        var list = output.list("added", Identifier.CODEC);
        for (Item item : this.added) {
            list.add(BuiltInRegistries.ITEM.getKey(item));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.heat = input.getIntOr("heat", 0);
        this.residue = input.getFloatOr("residue", 0.0F);
        this.scoreSum = input.getFloatOr("score_sum", 0.0F);
        this.scoreCount = input.getIntOr("score_count", 0);
        this.settleTimer = input.getIntOr("settle", -1);
        this.scorched = input.getBooleanOr("scorched", false);
        this.springInfused = input.getBooleanOr("spring_infused", false);
        this.added.clear();
        for (Identifier id : input.listOrEmpty("added", Identifier.CODEC)) {
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(this.added::add);
        }
    }
}
