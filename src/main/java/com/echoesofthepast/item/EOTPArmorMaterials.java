package com.echoesofthepast.item;

import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Materials for the mod's two sets. Both are light: a cultivator's protection is supposed to come
 * from technique, not from plate.
 *
 * <p>The equipment asset is pointed at a vanilla one for now so that the armour renders while the
 * models are being made. Swap {@link #JADE_SILK_ASSET} to a key of your own once there is an
 * equipment definition and texture to go with it.
 */
public final class EOTPArmorMaterials {
    private EOTPArmorMaterials() {}

    /** Replace with {@code createAssetKey("jade_silk")} once the equipment model exists. */
    public static final ResourceKey<EquipmentAsset> JADE_SILK_ASSET = EquipmentAssets.IRON;

    /** Jade plaques over silk: almost no armour value, all technique support. */
    public static final ArmorMaterial JADE_SILK = new ArmorMaterial(
        16,
        Map.of(
            ArmorType.BOOTS, 1,
            ArmorType.LEGGINGS, 2,
            ArmorType.CHESTPLATE, 3,
            ArmorType.HELMET, 1
        ),
        18,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        ItemTags.PLANKS,
        JADE_SILK_ASSET
    );

    /** Scholar's robes: no protection to speak of, but resistant to what alchemy throws at you. */
    public static final ArmorMaterial ROBE = new ArmorMaterial(
        12,
        Map.of(
            ArmorType.BOOTS, 1,
            ArmorType.LEGGINGS, 1,
            ArmorType.CHESTPLATE, 2,
            ArmorType.HELMET, 1
        ),
        22,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        ItemTags.WOOL,
        JADE_SILK_ASSET
    );

    @SuppressWarnings("unused")
    private static ResourceKey<EquipmentAsset> createAssetKey(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("eotp", name));
    }
}
