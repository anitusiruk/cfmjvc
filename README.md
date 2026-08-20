# Echoes of the Past

A xianxia / wuxia cultivation mod for **Minecraft 26.1** on **Forge 62** (unobfuscated Minecraft,
Java 25, ForgeGradle 7).

Qi is not a number in a machine. It exists in the world as visible breath with a character of its
own, formations are drawn by hand with a loaded brush, alchemy is judged by watching a cauldron, and
the past can be looked at, taught to a stone tablet, and made to repeat itself.

## No art assets, by design

There are **no textures, models, blockstate files, animations or sounds** anywhere in this
repository. Everything is registered and mechanically complete so that the art can be made in
Blockbench and dropped in afterwards.

Concretely, that means:

- blocks and items are registered and fully functional; they will render as missing-model /
  missing-texture placeholders until models exist;
- every entity is registered with vanilla's `NoopRenderer`, so entities exist, tick and can be
  interacted with while being invisible except for the particles they trail. Replace the renderer in
  `client/EOTPClient.java` with your own once a model exists;
- the armour materials point at a vanilla equipment asset (`EquipmentAssets.IRON`) so armour renders
  while its own asset is being made. `EOTPArmorMaterials.createAssetKey` is there for when you swap
  it;
- **all visuals use vanilla particle types tinted by phase colour**, and all sounds are vanilla
  sounds. Nothing in the mod needs an asset in order to be legible in game;
- loot tables, block/item tags and crafting recipes *are* included, since they are data rather than
  art. Every block drops correctly and every device is craftable.

## Texture checklist

Paths below are the project’s **canonical art contract**. Model and renderer files do not exist yet, so Minecraft does not hardcode these filenames today; make the models reference these exact paths. All paths are relative to `src/main/resources/`. Block items should inherit their block model and therefore need no second item PNG unless you intentionally make a separate inventory model.

Use `.png.mcmeta` beside any PNG that you later choose to animate. No animation is currently required.

### Blocks

- **Ancestral Tablet** — watches and repeats gestures; textures: `assets/eotp/textures/block/ancestral_tablet_dormant.png`, `assets/eotp/textures/block/ancestral_tablet_awakened.png`.
- **Bagua Distributor** — routes Qi eight ways; texture: `assets/eotp/textures/block/bagua_distributor.png`.
- **Bamboo Qi Flue** — carries Qi with losses; texture: `assets/eotp/textures/block/bamboo_qi_flue.png`.
- **Black Lacquered Planks** — decorative seal-ready wood; texture: `assets/eotp/textures/block/black_lacquered_planks.png`.
- **Bronze Spirit Bell** — rings from Qi pulses; texture: `assets/eotp/textures/block/bronze_spirit_bell.png`.
- **Carved Jade Border** — insulates nearby Qi; texture: `assets/eotp/textures/block/carved_jade_border.png`.
- **Celadon Basin** — decorative water-aligned ceramic; texture: `assets/eotp/textures/block/celadon_basin.png`.
- **Celadon Bricks** — decorative water-aligned ceramic; texture: `assets/eotp/textures/block/celadon_bricks.png`.
- **Celadon Jar** — decorative water-aligned ceramic; texture: `assets/eotp/textures/block/celadon_jar.png`.
- **Celadon Lamp** — decorative water-aligned ceramic; texture: `assets/eotp/textures/block/celadon_lamp.png`.
- **Celadon Pillar** — decorative water-aligned ceramic; texture: `assets/eotp/textures/block/celadon_pillar.png`.
- **Celadon Slab** — decorative water-aligned ceramic; texture: `assets/eotp/textures/block/celadon_bricks.png`.
- **Celadon Stairs** — decorative water-aligned ceramic; texture: `assets/eotp/textures/block/celadon_bricks.png`.
- **Celadon Tiles** — decorative water-aligned ceramic; texture: `assets/eotp/textures/block/celadon_tiles.png`.
- **Celadon Wall** — decorative water-aligned ceramic; texture: `assets/eotp/textures/block/celadon_bricks.png`.
- **Dark Jade Block** — insulates nearby Qi; texture: `assets/eotp/textures/block/dark_jade_block.png`.
- **Dark Lacquered Planks** — decorative seal-ready wood; texture: `assets/eotp/textures/block/dark_lacquered_planks.png`.
- **Deepslate Spirit Stone Ore** — deep raw spirit stone ore; texture: `assets/eotp/textures/block/deepslate_spirit_stone_ore.png`.
- **Ding Cauldron** — refines ordered pill batches; texture: `assets/eotp/textures/block/ding_cauldron.png`.
- **Earthroot Ginseng** — grows in varied gardens; texture: `assets/eotp/textures/block/earthroot_ginseng_age_{0..7}.png`.
- **Five Phase Conversion Wheel** — converts Qi by phase cycle; texture: `assets/eotp/textures/block/five_phase_conversion_wheel.png`.
- **Footwork Seal** — reads crossing movement; texture: `assets/eotp/textures/block/footwork_seal.png`.
- **Formation Arc Tile** — builds formation circuits; texture: `assets/eotp/textures/block/formation_arc_tile.png`.
- **Formation Banner** — raises formation effects; texture: `assets/eotp/textures/block/formation_banner_{wood,fire,earth,metal,water}.png`.
- **Formation Core** — reads and runs circuits; texture: `assets/eotp/textures/block/formation_core.png`.
- **Formation Ink** — draws phase-aligned circuits; texture: `assets/eotp/textures/block/formation_ink_{wood,fire,earth,metal,water}_{straight,corner,cross,curve,glyph}.png`.
- **Formation Line Tile** — builds formation circuits; texture: `assets/eotp/textures/block/formation_line_tile.png`.
- **Formation Node Tile** — builds formation circuits; texture: `assets/eotp/textures/block/formation_node_tile.png`.
- **Formation Trigram Tile** — builds formation circuits; texture: `assets/eotp/textures/block/formation_trigram_tile.png`.
- **Hanging Scroll Flame** — displays a calligraphy concept; texture: `assets/eotp/textures/block/hanging_scroll_flame.png`.
- **Hanging Scroll Gate** — displays a calligraphy concept; texture: `assets/eotp/textures/block/hanging_scroll_gate.png`.
- **Hanging Scroll Mountain** — displays a calligraphy concept; texture: `assets/eotp/textures/block/hanging_scroll_mountain.png`.
- **Hanging Scroll Return** — displays a calligraphy concept; texture: `assets/eotp/textures/block/hanging_scroll_return.png`.
- **Hanging Scroll River** — displays a calligraphy concept; texture: `assets/eotp/textures/block/hanging_scroll_river.png`.
- **Hanging Scroll Silence** — displays a calligraphy concept; texture: `assets/eotp/textures/block/hanging_scroll_silence.png`.
- **Hanging Scroll Stillness** — displays a calligraphy concept; texture: `assets/eotp/textures/block/hanging_scroll_stillness.png`.
- **Hanging Scroll Sword** — displays a calligraphy concept; texture: `assets/eotp/textures/block/hanging_scroll_sword.png`.
- **Herb Drying Rack** — dries herbs by environment; texture: `assets/eotp/textures/block/herb_drying_rack.png`.
- **High Spirit Stone Block** — stores Qi and dims; texture: `assets/eotp/textures/block/high_spirit_stone_block_charge_{0..4}.png`.
- **Hollow Bamboo Qi Flue** — carries Qi through bamboo; texture: `assets/eotp/textures/block/hollow_bamboo_qi_flue.png`.
- **Hollow Spirit Bamboo** — brittle overfed bamboo; textures: `assets/eotp/textures/block/hollow_spirit_bamboo_age_{0..3}.png`.
- **Incense Censer** — burns room-wide incense; textures: `assets/eotp/textures/block/incense_censer_unlit.png`, `assets/eotp/textures/block/incense_censer_lit.png`.
- **Inkstone** — grinds magical ink; texture: `assets/eotp/textures/block/inkstone.png`.
- **Jade Abacus** — counts magical conditions; texture: `assets/eotp/textures/block/jade_abacus.png`.
- **Jade Bi Reservoir** — stores resonating Qi; texture: `assets/eotp/textures/block/jade_bi_reservoir.png`.
- **Jade Flue Joint** — turns Qi with low loss; texture: `assets/eotp/textures/block/jade_flue_joint.png`.
- **Jade Inlay** — insulates nearby Qi; texture: `assets/eotp/textures/block/jade_inlay.png`.
- **Jade Meridian Thread** — carries timed Qi pulses; texture: `assets/eotp/textures/block/jade_meridian_thread.png`.
- **Jade Pillar** — insulates nearby Qi; texture: `assets/eotp/textures/block/jade_pillar.png`.
- **Jade Slab** — insulates nearby Qi; texture: `assets/eotp/textures/block/pale_jade_block.png`.
- **Jade Stairs** — insulates nearby Qi; texture: `assets/eotp/textures/block/pale_jade_block.png`.
- **Lacquered Low Table** — decorative seal-ready wood; texture: `assets/eotp/textures/block/lacquered_low_table.png`.
- **Lacquered Panel** — decorative seal-ready wood; texture: `assets/eotp/textures/block/lacquered_panel.png`.
- **Lacquered Pillar** — decorative seal-ready wood; texture: `assets/eotp/textures/block/lacquered_pillar.png`.
- **Lacquered Screen** — decorative seal-ready wood; texture: `assets/eotp/textures/block/lacquered_screen.png`.
- **Lacquered Slab** — decorative seal-ready wood; texture: `assets/eotp/textures/block/red_lacquered_planks.png`.
- **Lacquered Stairs** — decorative seal-ready wood; texture: `assets/eotp/textures/block/red_lacquered_planks.png`.
- **Lingzhi Spirit Fungus** — eats turbulent Qi; texture: `assets/eotp/textures/block/lingzhi_spirit_fungus_saturation_{0..4}.png`.
- **Low Spirit Stone Block** — stores Qi and dims; texture: `assets/eotp/textures/block/low_spirit_stone_block_charge_{0..4}.png`.
- **Middle Spirit Stone Block** — stores Qi and dims; texture: `assets/eotp/textures/block/middle_spirit_stone_block_charge_{0..4}.png`.
- **Moon Lotus** — gathers moonlit Qi; textures: `assets/eotp/textures/block/moon_lotus_closed.png`, `assets/eotp/textures/block/moon_lotus_open.png`, `assets/eotp/textures/block/moon_lotus_petals_{1..3}.png`.
- **Painted Cloud Screen** — shows a painted landscape; texture: `assets/eotp/textures/block/painted_cloud_screen.png`.
- **Painted Mountain Screen** — shows a painted landscape; texture: `assets/eotp/textures/block/painted_mountain_screen.png`.
- **Painted River Screen** — shows a painted landscape; texture: `assets/eotp/textures/block/painted_river_screen.png`.
- **Pale Jade Block** — insulates nearby Qi; texture: `assets/eotp/textures/block/pale_jade_block.png`.
- **Placed Talisman** — holds a pasted instruction; texture: `assets/eotp/textures/block/placed_talisman.png`.
- **Qi Prism** — refracts Qi beams; texture: `assets/eotp/textures/block/qi_prism.png`.
- **Red Lacquered Planks** — decorative seal-ready wood; texture: `assets/eotp/textures/block/red_lacquered_planks.png`.
- **Resonance Stone** — answers a tuned sound; texture: `assets/eotp/textures/block/resonance_stone.png`.
- **Rice Paper Screen** — thin decorative screen; texture: `assets/eotp/textures/block/rice_paper_screen.png`.
- **Seal Carving Table** — carves and stamps rules; texture: `assets/eotp/textures/block/seal_carving_table.png`.
- **Silk Screen** — thin decorative screen; texture: `assets/eotp/textures/block/silk_screen.png`.
- **Spirit Bamboo** — grows from ambient Qi; texture: `assets/eotp/textures/block/spirit_bamboo_age_{0..3}.png`.
- **Spirit Jade Block** — insulates nearby Qi; texture: `assets/eotp/textures/block/spirit_jade_block.png`.
- **Spirit Spring Water** — cultivation-enhancing water; texture: see Fluid section below.
- **Spirit Stone Ore** — contains raw spirit stone; texture: `assets/eotp/textures/block/spirit_stone_ore.png`.
- **White Jade Block** — insulates nearby Qi; texture: `assets/eotp/textures/block/white_jade_block.png`.

### Standalone items

- **Alchemist Hood** — alchemy-resistant headwear; texture: `assets/eotp/textures/item/alchemist_hood.png`.
- **Alchemist Robe** — alchemy-resistant robes; texture: `assets/eotp/textures/item/alchemist_robe.png`.
- **Alchemist Sandals** — alchemy-resistant footwear; texture: `assets/eotp/textures/item/alchemist_sandals.png`.
- **Alchemist Trousers** — alchemy-resistant leggings; texture: `assets/eotp/textures/item/alchemist_trousers.png`.
- **Attuning Incense** — shifts nearby Qi phase; texture: `assets/eotp/textures/item/attuning_incense.png`.
- **Binding Talisman** — roots nearby targets; texture: `assets/eotp/textures/item/binding_talisman.png`.
- **Body Tempering Pill** — briefly hardens the body; texture: `assets/eotp/textures/item/body_tempering_pill.png`.
- **Breakthrough Stabilising Pill** — steadies breakthroughs and cores; texture: `assets/eotp/textures/item/breakthrough_stabilising_pill.png`.
- **Calming Incense** — settles minds and mobs; texture: `assets/eotp/textures/item/calming_incense.png`.
- **Carved Seal** — stores a reusable rule; texture: `assets/eotp/textures/item/carved_seal.png`.
- **Cinnabar Pigment** — makes fire-aligned ink; texture: `assets/eotp/textures/item/cinnabar_pigment.png`.
- **Clear Heart Pill** — clears deviation and fear; texture: `assets/eotp/textures/item/clear_heart_pill.png`.
- **Clear Heart Talisman** — creates a calming field; texture: `assets/eotp/textures/item/clear_heart_talisman.png`.
- **Cloudstep Pill** — grants temporary air steps; texture: `assets/eotp/textures/item/cloudstep_pill.png`.
- **Cloudstep Shoes** — enables chained air steps; texture: `assets/eotp/textures/item/cloudstep_shoes.png`.
- **Dragon Vein Compass** — reveals veins and devices; texture: `assets/eotp/textures/item/dragon_vein_compass.png`.
- **Earth Essence** — carries Earth phase; texture: `assets/eotp/textures/item/earth_essence.png`.
- **Echo Essence** — tangible recovered memory; texture: `assets/eotp/textures/item/echo_essence.png`.
- **Echo Mirror** — replays nearby past events; texture: `assets/eotp/textures/item/echo_mirror.png`.
- **Echo Scroll** — teaches a discovery; texture: `assets/eotp/textures/item/echo_scroll.png`.
- **Ember Talisman** — ignites its target; texture: `assets/eotp/textures/item/ember_talisman.png`.
- **Fire Essence** — carries Fire phase; texture: `assets/eotp/textures/item/fire_essence.png`.
- **Five Phase Harmony Pill** — balances conflicted Qi; texture: `assets/eotp/textures/item/five_phase_harmony_pill.png`.
- **Flying Sword** — orbits, strikes, and returns; texture: `assets/eotp/textures/item/flying_sword.png`.
- **Gather Talisman** — pulls nearby items inward; texture: `assets/eotp/textures/item/gather_talisman.png`.
- **Ginseng Root** — slow-grown alchemy herb; texture: `assets/eotp/textures/item/ginseng_root.png`.
- **High Spirit Stone** — stores clean high-grade Qi; texture: `assets/eotp/textures/item/high_spirit_stone.png`.
- **Hollow Bamboo** — brittle flue material; texture: `assets/eotp/textures/item/hollow_bamboo.png`.
- **Jade Dust** — powdered magical jade; texture: `assets/eotp/textures/item/jade_dust.png`.
- **Jade Imperial Seal** — marks shared destinations; texture: `assets/eotp/textures/item/jade_imperial_seal.png`.
- **Kindling Incense** — speeds resting cultivation; texture: `assets/eotp/textures/item/kindling_incense.png`.
- **Lingzhi Cap** — stores absorbed turbulence; texture: `assets/eotp/textures/item/lingzhi_cap.png`.
- **Low Spirit Stone** — stores low-grade Qi; texture: `assets/eotp/textures/item/low_spirit_stone.png`.
- **Meridian Opening Pill** — pushes a meridian open; texture: `assets/eotp/textures/item/meridian_opening_pill.png`.
- **Meridian Thread Spool** — crafts pulse-conducting thread; texture: `assets/eotp/textures/item/meridian_thread_spool.png`.
- **Metal Essence** — carries Metal phase; texture: `assets/eotp/textures/item/metal_essence.png`.
- **Middle Spirit Stone** — stores refined middle-grade Qi; texture: `assets/eotp/textures/item/middle_spirit_stone.png`.
- **Moon Lotus Petal** — moonlit alchemy reagent; texture: `assets/eotp/textures/item/moon_lotus_petal.png`.
- **Paper Crane** — flies one sealed delivery; texture: `assets/eotp/textures/item/paper_crane.png`.
- **Pill Residue** — salvage from failed pills; texture: `assets/eotp/textures/item/pill_residue.png`.
- **Preservation Talisman** — prevents item despawning; texture: `assets/eotp/textures/item/preservation_talisman.png`.
- **Purified Lingzhi** — purification reagent; texture: `assets/eotp/textures/item/purified_lingzhi.png`.
- **Qi Recovery Pill** — restores personal Qi; texture: `assets/eotp/textures/item/qi_recovery_pill.png`.
- **Raw Jade** — unworked magical jade; texture: `assets/eotp/textures/item/raw_jade.png`.
- **Raw Spirit Stone** — unrefined Qi mineral; texture: `assets/eotp/textures/item/raw_spirit_stone.png`.
- **Repulsion Talisman** — throws entities outward; texture: `assets/eotp/textures/item/repulsion_talisman.png`.
- **Return Talisman** — reflects projectiles; texture: `assets/eotp/textures/item/return_talisman.png`.
- **Revealing Incense** — makes echoes visible; texture: `assets/eotp/textures/item/revealing_incense.png`.
- **Silence Talisman** — calms nearby aggression; texture: `assets/eotp/textures/item/silence_talisman.png`.
- **Spirit Bamboo Shoot** — plants or crafts bamboo; texture: `assets/eotp/textures/item/spirit_bamboo_shoot.png`.
- **Spirit Brush** — draws formation ink; texture: `assets/eotp/textures/item/spirit_brush.png`.
- **Spirit Silk** — weaves magical equipment; texture: `assets/eotp/textures/item/spirit_silk.png`.
- **Spirit Spring Bucket** — carries spirit spring water; texture: `assets/eotp/textures/item/spirit_spring_bucket.png`.
- **Spirit Stone Powder** — fuels phase conversion; texture: `assets/eotp/textures/item/spirit_stone_powder.png`.
- **Suppressing Incense** — harms nearby undead; texture: `assets/eotp/textures/item/suppressing_incense.png`.
- **Sword Cultivator Boots** — supports sword techniques; texture: `assets/eotp/textures/item/sword_cultivator_boots.png`.
- **Sword Cultivator Crown** — supports sword techniques; texture: `assets/eotp/textures/item/sword_cultivator_crown.png`.
- **Sword Cultivator Robe** — supports sword techniques; texture: `assets/eotp/textures/item/sword_cultivator_robe.png`.
- **Sword Cultivator Skirt** — supports sword techniques; texture: `assets/eotp/textures/item/sword_cultivator_skirt.png`.
- **Talisman Paper** — blank magical instruction; texture: `assets/eotp/textures/item/talisman_paper.png`.
- **Water Essence** — carries Water phase; texture: `assets/eotp/textures/item/water_essence.png`.
- **Wood Essence** — carries Wood phase; texture: `assets/eotp/textures/item/wood_essence.png`.

### Entity textures

These become required when each `NoopRenderer` is replaced by its Blockbench renderer.

- **Flying Sword** — bound sword in flight; texture: `assets/eotp/textures/entity/flying_sword.png`.
- **Heart Demon** — shadow copy of a cultivator; texture: `assets/eotp/textures/entity/heart_demon.png`.
- **Meditating Body** — body left during projection; texture: `assets/eotp/textures/entity/meditating_body.png`.
- **Paper Crane** — flying delivery familiar; texture: `assets/eotp/textures/entity/paper_crane.png`.

### Mob-effect icons

- **Clear Heart** — settled-mind buff; texture: `assets/eotp/textures/mob_effect/clear_heart.png`.
- **Cloudstep** — temporary air-step buff; texture: `assets/eotp/textures/mob_effect/cloudstep.png`.
- **Meridian Sealed** — closed-channel status; texture: `assets/eotp/textures/mob_effect/meridian_sealed.png`.
- **Qi Deviation** — unstable-Qi debuff; texture: `assets/eotp/textures/mob_effect/qi_deviation.png`.
- **Spirit Sight** — hidden-energy vision; texture: `assets/eotp/textures/mob_effect/spirit_sight.png`.

### Fluid textures

- **Spirit Spring Water (still)** — calm source surface; texture: `assets/eotp/textures/block/spirit_spring_water_still.png`.
- **Spirit Spring Water (flowing)** — moving spring stream; texture: `assets/eotp/textures/block/spirit_spring_water_flow.png`.
- **Spirit Spring Water (overlay)** — underwater/screen overlay; texture: `assets/eotp/textures/block/spirit_spring_water_overlay.png`.

### Worn armour textures

Each armour item also keeps its item-icon PNG listed above. The current code deliberately uses vanilla iron as a temporary wearable asset; after changing `EOTPArmorMaterials` to the matching custom equipment keys, use these files:

- **Sword Cultivator set, body layer** — crown, robe, and boots; texture: `assets/eotp/textures/entity/equipment/humanoid/jade_silk.png`.
- **Sword Cultivator set, leggings layer** — skirt/leggings; texture: `assets/eotp/textures/entity/equipment/humanoid_leggings/jade_silk.png`.
- **Alchemist set, body layer** — hood, robe, and sandals; texture: `assets/eotp/textures/entity/equipment/humanoid/alchemist_robes.png`.
- **Alchemist set, leggings layer** — trousers; texture: `assets/eotp/textures/entity/equipment/humanoid_leggings/alchemist_robes.png`.

### Textures not required

- **Qi wisps, streams, beams, echoes, formation glows, tribulation, and tablet hands** — use tinted vanilla particles; no custom PNG required.
- **Dragon veins** — particle-only world visualization; no custom PNG required.
- **Sounds** — use vanilla sound events; no custom audio required.


## Design rules the whole mod obeys

**No GUIs, no HUD.** Nothing in this mod opens a screen. Devices are configured with hand gestures —
click to cycle a rule, sneak-click to rotate or reverse — and they answer in particles first and in a
single line above the hotbar second. The bagua's routing rule, the abacus's threshold, a spirit
stone's charge and a censer's state are all readable off the block itself.

**Qi is judged by eye.** A device shows what it holds as drifting motes coloured by phase, turbulent
Qi sparks and crackles, and Qi in transit is drawn as a trail from one block to the next. You can ask
for exact figures with the compass, but you should rarely need to.

**Nothing important is rolled.** Pill grade comes from the conditions the cauldron actually held.
Formation completeness is a property of the shape you drew. Sword intent comes from how you have
really been fighting. Where the mod is random, it is random about weather and world generation, not
about outcomes you worked for.

**Server-authoritative.** There are no custom network packets and no capabilities. Cultivation state
lives in world save data keyed by player UUID, which means it survives death with no special
handling and can be read by a block the player is nowhere near.

## Building

Requires JDK 25 (Minecraft 26.1 needs it).

```bash
./gradlew build          # produces build/libs/eotp-0.1.0.jar
./gradlew runClient      # or runServer
```

## Where things live

| Package | What is in it |
| --- | --- |
| `qi` | phases, blends, storage, transport, pulses, beams, the particle vocabulary |
| `cultivation` | realms, meridians, roots, breakthroughs, tribulation, projection |
| `formation` | circuit surveying and formation identification |
| `block/qi` | reservoirs, flues, threads, bagua, conversion wheel, prisms, bells, abacus |
| `block/formation` | tiles, ink, banners, the core, footwork seals |
| `block/alchemy` | the Ding cauldron |
| `block/craft` | inkstone, seal carving table, censer, drying rack |
| `block/echo` | ancestral tablets |
| `block/plant` | spirit bamboo, moon lotus, ginseng, lingzhi |
| `alchemy`, `ink`, `seal`, `talisman` | the crafting chains |
| `echo`, `imprint` | the world's memory and the imprinting vocabulary |
| `entity` | heart demon, flying sword, paper crane, meditating body |

See [FEATURES.md](FEATURES.md) for the fifty selected features and what each one actually does.

## First hour, roughly

1. Mine spirit stone ore, refine raw stone into Low Spirit Stone.
2. Grind charcoal on an inkstone, dip a brush, and draw a ring of formation ink with a node in it.
3. Put a formation core in the middle and right-click it: it will tell you what you have drawn.
4. Feed it from a Jade Bi Reservoir through a bamboo flue and watch how much Qi the corners waste.
5. Sit still on a dragon vein until you have the insight to attempt Breath Gathering.
6. Once you have Foundation, build a cultivation circuit and try a real breakthrough — and put some
   lightning rods up before you try for Golden Core.
