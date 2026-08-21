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

## Texture and model checklist

Paths below are the project’s **canonical art contract**. Models and renderers do not exist yet, so Minecraft does not hardcode these filenames today; make the future blockstate, model, and renderer files reference these exact paths. All paths are relative to `src/main/resources/`.

**Dimension convention:** one Blockbench pixel is `1/16` of a Minecraft block. “3D: yes” means custom geometry is needed for the intended silhouette; “no custom geometry” means a normal Minecraft JSON cube/stair/slab/wall model is enough. Suggested bounds describe the model’s overall occupied box, not the texture-canvas size. Block textures default to **16×16 px**; a detailed custom model may use a **32×32 px** atlas at the same path. Block items should inherit their block model and therefore need no second item PNG.

Export block models to `assets/eotp/models/block/<id>.json`, connect them through `assets/eotp/blockstates/<id>.json`, and put optional inventory overrides in `assets/eotp/models/item/<id>.json`. These dimensions are visual guidance, not collision boxes: decorative objects currently registered as ordinary full blocks—such as jars, basins, lamps, and screens—will also need matching `getShape`/`getCollisionShape` code when their non-cube models are installed.

Use `.png.mcmeta` beside a PNG only if it is later animated. No animation is currently required.

### Blocks

- **Ancestral Tablet** — observes repeated device gestures, learns the sequence, then replays it after awakening with Echo Essence; 3D: **yes**; about 10×14×4 px, a stone ancestor tablet on a footed base with a rounded or peaked crown; texture(s): `assets/eotp/textures/block/ancestral_tablet_dormant.png`, `assets/eotp/textures/block/ancestral_tablet_awakened.png`.
- **Bagua Distributor** — routes continuous Qi to eight surrounding positions using six selectable distribution rules; 3D: **yes**; about 16×5×16 px, a low octagonal bronze disc with eight readable trigram sectors and a central inlet; texture(s): `assets/eotp/textures/block/bagua_distributor.png`.
- **Bamboo Qi Flue** — moves visible Qi while losing more at bends, climbs, and long unsupported runs; 3D: **yes**; 16×6×6 px along its axis, a hollow bamboo tube with visible cut ends and tied joints; texture(s): `assets/eotp/textures/block/bamboo_qi_flue.png`.
- **Black Lacquered Planks** — builds seal-ready workshop furniture and red, black, or dark lacquered surfaces; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/black_lacquered_planks.png`.
- **Bronze Spirit Bell** — converts pulse strength into pitch, broadcasts resonance, then passes the pulse onward; 3D: **yes**; about 10×14×10 px, a hanging flared bronze bell with a top loop and carved bands; texture(s): `assets/eotp/textures/block/bronze_spirit_bell.png`.
- **Carved Jade Border** — builds carved jade rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/carved_jade_border.png`.
- **Celadon Basin** — decorative Water-aligned vessel whose material insulates adjacent Qi devices; 3D: **yes**; about 14×6×14 px, a broad shallow glazed bowl with a visible inner cavity; texture(s): `assets/eotp/textures/block/celadon_basin.png`.
- **Celadon Bricks** — builds Water-aligned rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/celadon_bricks.png`.
- **Celadon Jar** — decorative Water-aligned storage vessel whose material insulates adjacent Qi devices; 3D: **yes**; about 10×14×10 px, a rounded celadon jar with a narrow neck and small lid; texture(s): `assets/eotp/textures/block/celadon_jar.png`.
- **Celadon Lamp** — decorative glowing ceramic whose celadon body insulates adjacent Qi devices; 3D: **yes**; about 10×15×10 px, a pierced celadon lantern around a glowing centre; texture(s): `assets/eotp/textures/block/celadon_lamp.png`.
- **Celadon Pillar** — builds Water-aligned rooms while reducing Qi leakage from adjacent devices; 3D: no Blockbench model; use a full 16×16×16 px pillar cube with side/top texture mapping; texture(s): `assets/eotp/textures/block/celadon_pillar.png`.
- **Celadon Slab** — builds Water-aligned rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use the vanilla 16×8×16 px slab template; texture(s): `assets/eotp/textures/block/celadon_bricks.png`.
- **Celadon Stairs** — builds Water-aligned rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use the vanilla 16×16×16 px stair template; texture(s): `assets/eotp/textures/block/celadon_bricks.png`.
- **Celadon Tiles** — builds Water-aligned rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/celadon_tiles.png`.
- **Celadon Wall** — builds Water-aligned rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use the vanilla wall post/arm templates; texture(s): `assets/eotp/textures/block/celadon_bricks.png`.
- **Dark Jade Block** — builds carved jade rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/dark_jade_block.png`.
- **Dark Lacquered Planks** — builds seal-ready workshop furniture and red, black, or dark lacquered surfaces; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/dark_lacquered_planks.png`.
- **Deepslate Spirit Stone Ore** — deep ore source for Raw Spirit Stone, requiring stronger mining tools; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/deepslate_spirit_stone_ore.png`.
- **Ding Cauldron** — accepts ordered ingredients and grades pills from heat, phase balance, turbulence, and cleanliness; 3D: **yes**; within 14×14×14 px, a round bronze Ding with two handles, a deep mouth, and three legs; texture(s): `assets/eotp/textures/block/ding_cauldron.png`.
- **Earthroot Ginseng** — matures slowly according to the variety of natural blocks surrounding its garden; 3D: **yes, recommended**; within 10×6×10 px, low leaves above a partly exposed forked root that thickens by age; texture(s): `assets/eotp/textures/block/earthroot_ginseng_age_{0..7}.png`.
- **Five Phase Conversion Wheel** — converts Qi along the generating cycle, or inefficiently reverses it into turbulence; 3D: **yes**; about 16×16×5 px, five nested bronze/jade rings with distinct phase marks; texture(s): `assets/eotp/textures/block/five_phase_conversion_wheel.png`.
- **Footwork Seal** — emits different signals for sneaking, walking, running, jumping, and cloudstepping; 3D: no Blockbench model; use a 16×0.25×16 px floor decal JSON model; texture(s): `assets/eotp/textures/block/footwork_seal.png`.
- **Formation Arc Tile** — provides an arc mark used to close and define formation circuits; 3D: no Blockbench model; use a 16×1×16 px carved floor-tile JSON model; texture(s): `assets/eotp/textures/block/formation_arc_tile.png`.
- **Formation Banner** — extends a floor formation vertically and strengthens its area effect using a selected phase; 3D: **yes**; two-block concept about 6×32×3 px, a tall pole with phase-coloured hanging silk and a pointed tail; texture(s): `assets/eotp/textures/block/formation_banner_{wood,fire,earth,metal,water}.png`.
- **Formation Core** — surveys connected marks, identifies complete circuits, stores Qi, and runs the formation effect; 3D: **yes**; 12×3×12 px, a low carved jade altar plate with a central Qi socket; texture(s): `assets/eotp/textures/block/formation_core.png`.
- **Formation Ink** — forms low-capacity phase-coloured circuit lines in five stroke shapes; 3D: no Blockbench model; use 16×0.25×16 px floor decals for every phase/stroke state; texture(s): `assets/eotp/textures/block/formation_ink_{wood,fire,earth,metal,water}_{straight,corner,cross,curve,glyph}.png`.
- **Formation Line Tile** — provides a line mark used to close and define formation circuits; 3D: no Blockbench model; use a 16×1×16 px carved floor-tile JSON model; texture(s): `assets/eotp/textures/block/formation_line_tile.png`.
- **Formation Node Tile** — provides a node mark used to close and define formation circuits; 3D: no Blockbench model; use a 16×1×16 px carved floor-tile JSON model; texture(s): `assets/eotp/textures/block/formation_node_tile.png`.
- **Formation Trigram Tile** — provides a trigram mark used to close and define formation circuits; 3D: no Blockbench model; use a 16×1×16 px carved floor-tile JSON model; texture(s): `assets/eotp/textures/block/formation_trigram_tile.png`.
- **Hanging Scroll Flame** — displays the flame calligraphy concept for themed cultivation rooms; 3D: **yes**; about 10×16×1 px, a hanging paper scroll with top/bottom rollers and the named glyph; texture(s): `assets/eotp/textures/block/hanging_scroll_flame.png`.
- **Hanging Scroll Gate** — displays the gate calligraphy concept for themed cultivation rooms; 3D: **yes**; about 10×16×1 px, a hanging paper scroll with top/bottom rollers and the named glyph; texture(s): `assets/eotp/textures/block/hanging_scroll_gate.png`.
- **Hanging Scroll Mountain** — displays the mountain calligraphy concept for themed cultivation rooms; 3D: **yes**; about 10×16×1 px, a hanging paper scroll with top/bottom rollers and the named glyph; texture(s): `assets/eotp/textures/block/hanging_scroll_mountain.png`.
- **Hanging Scroll Return** — displays the return calligraphy concept for themed cultivation rooms; 3D: **yes**; about 10×16×1 px, a hanging paper scroll with top/bottom rollers and the named glyph; texture(s): `assets/eotp/textures/block/hanging_scroll_return.png`.
- **Hanging Scroll River** — displays the river calligraphy concept for themed cultivation rooms; 3D: **yes**; about 10×16×1 px, a hanging paper scroll with top/bottom rollers and the named glyph; texture(s): `assets/eotp/textures/block/hanging_scroll_river.png`.
- **Hanging Scroll Silence** — displays the silence calligraphy concept for themed cultivation rooms; 3D: **yes**; about 10×16×1 px, a hanging paper scroll with top/bottom rollers and the named glyph; texture(s): `assets/eotp/textures/block/hanging_scroll_silence.png`.
- **Hanging Scroll Stillness** — displays the stillness calligraphy concept for themed cultivation rooms; 3D: **yes**; about 10×16×1 px, a hanging paper scroll with top/bottom rollers and the named glyph; texture(s): `assets/eotp/textures/block/hanging_scroll_stillness.png`.
- **Hanging Scroll Sword** — displays the sword calligraphy concept for themed cultivation rooms; 3D: **yes**; about 10×16×1 px, a hanging paper scroll with top/bottom rollers and the named glyph; texture(s): `assets/eotp/textures/block/hanging_scroll_sword.png`.
- **Herb Drying Rack** — finishes herbs differently under sunlight, moonlight, or nearby incense smoke; 3D: **yes**; 16×10×4 px, a bamboo wall frame with four visible hanging herb positions; texture(s): `assets/eotp/textures/block/herb_drying_rack.png`.
- **High Spirit Stone Block** — stores high-grade Qi and visibly dims through five charge states as it drains; 3D: no custom geometry; use a full 16×16×16 px cube with five charge-model variants; texture(s): `assets/eotp/textures/block/high_spirit_stone_block_charge_{0..4}.png`.
- **Hollow Bamboo Qi Flue** — moves Qi cheaply but wastes more at every section, bend, climb, and long run; 3D: **yes**; 16×6×6 px along its axis, a thinner cracked bamboo tube with a visibly empty centre; texture(s): `assets/eotp/textures/block/hollow_bamboo_qi_flue.png`.
- **Hollow Spirit Bamboo** — brittle bamboo created by excess Qi, harvested mainly for cheap flues; 3D: **yes, recommended**; within 6×16×6 px, a pale split stalk with a visibly empty centre; texture(s): `assets/eotp/textures/block/hollow_spirit_bamboo_age_{0..3}.png`.
- **Incense Censer** — burns one incense type into a slow room-wide aura and can amplify it with stored Qi; 3D: **yes**; within 10×11×10 px, a lidded bronze censer with feet, side handles, and smoke holes; texture(s): `assets/eotp/textures/block/incense_censer_unlit.png`, `assets/eotp/textures/block/incense_censer_lit.png`.
- **Inkstone** — holds one pigment and rewards complete hand-grinding with a fully loaded brush; 3D: **yes**; 14×3×12 px, a low dark stone slab with a grinding slope and recessed ink well; texture(s): `assets/eotp/textures/block/inkstone.png`.
- **Jade Abacus** — tests fullness, emptiness, pulse counts, tone counts, or intervals and emits a Qi pulse; 3D: **yes**; about 14×10×3 px, a jade frame with nine visibly movable bead positions; texture(s): `assets/eotp/textures/block/jade_abacus.png`.
- **Jade Bi Reservoir** — stores Qi and gains effective capacity by resonating with nearby discs; 3D: **yes**; about 14×3×14 px, a thick jade Bi disc with a true central hole and concentric carving; texture(s): `assets/eotp/textures/block/jade_bi_reservoir.png`.
- **Jade Flue Joint** — resets flue run length and carries a bend with almost no Qi loss; 3D: **yes**; about 8×8×8 px, a carved jade elbow/socket gripping adjacent bamboo tubes; texture(s): `assets/eotp/textures/block/jade_flue_joint.png`.
- **Jade Inlay** — builds carved jade rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/jade_inlay.png`.
- **Jade Meridian Thread** — moves identified signal pulses one block per tick with a fixed distance toll; 3D: no Blockbench model; use a 16×1×16 px silk-and-jade line that lies against its supporting face; texture(s): `assets/eotp/textures/block/jade_meridian_thread.png`.
- **Jade Pillar** — builds carved jade rooms while reducing Qi leakage from adjacent devices; 3D: no Blockbench model; use a full 16×16×16 px pillar cube with side/top texture mapping; texture(s): `assets/eotp/textures/block/jade_pillar.png`.
- **Jade Slab** — builds carved jade rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use the vanilla 16×8×16 px slab template; texture(s): `assets/eotp/textures/block/pale_jade_block.png`.
- **Jade Stairs** — builds carved jade rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use the vanilla 16×16×16 px stair template; texture(s): `assets/eotp/textures/block/pale_jade_block.png`.
- **Lacquered Low Table** — builds seal-ready workshop furniture and red, black, or dark lacquered surfaces; 3D: **yes**; 16×8×16 px, a low rectangular table with four short curved legs; texture(s): `assets/eotp/textures/block/lacquered_low_table.png`.
- **Lacquered Panel** — builds seal-ready workshop furniture and red, black, or dark lacquered surfaces; 3D: no Blockbench model; use a 16×16×2 px inset wooden wall panel; texture(s): `assets/eotp/textures/block/lacquered_panel.png`.
- **Lacquered Pillar** — builds seal-ready workshop furniture and red, black, or dark lacquered surfaces; 3D: no Blockbench model; use a full 16×16×16 px pillar cube with side/top texture mapping; texture(s): `assets/eotp/textures/block/lacquered_pillar.png`.
- **Lacquered Screen** — builds seal-ready workshop furniture and red, black, or dark lacquered surfaces; 3D: **yes**; 16×16×2 px, a lacquer frame around thin paper or silk with visible lattice bars; texture(s): `assets/eotp/textures/block/lacquered_screen.png`.
- **Lacquered Slab** — builds seal-ready workshop furniture and red, black, or dark lacquered surfaces; 3D: no custom geometry; use the vanilla 16×8×16 px slab template; texture(s): `assets/eotp/textures/block/red_lacquered_planks.png`.
- **Lacquered Stairs** — builds seal-ready workshop furniture and red, black, or dark lacquered surfaces; 3D: no custom geometry; use the vanilla 16×16×16 px stair template; texture(s): `assets/eotp/textures/block/red_lacquered_planks.png`.
- **Lingzhi Spirit Fungus** — draws turbulence out of nearby devices and turns the absorbed pollution into harvestable caps; 3D: **yes**; within 10×7×10 px, a layered shelf-like Lingzhi cap swelling as saturation rises; texture(s): `assets/eotp/textures/block/lingzhi_spirit_fungus_saturation_{0..4}.png`.
- **Low Spirit Stone Block** — stores low-grade Qi and visibly dims through five charge states as it drains; 3D: no custom geometry; use a full 16×16×16 px cube with five charge-model variants; texture(s): `assets/eotp/textures/block/low_spirit_stone_block_charge_{0..4}.png`.
- **Middle Spirit Stone Block** — stores refined Qi and visibly dims through five charge states as it drains; 3D: no custom geometry; use a full 16×16×16 px cube with five charge-model variants; texture(s): `assets/eotp/textures/block/middle_spirit_stone_block_charge_{0..4}.png`.
- **Moon Lotus** — opens only beneath a dark sky, gathers Water Qi, and slowly grows harvestable petals; 3D: **yes**; within 12×3×12 px, broad floating leaves and a lotus blossom opening into petal states; texture(s): `assets/eotp/textures/block/moon_lotus_closed.png`, `assets/eotp/textures/block/moon_lotus_open.png`, `assets/eotp/textures/block/moon_lotus_petals_{1..3}.png`.
- **Painted Cloud Screen** — displays a cloud landscape on a thin decorative screen; 3D: **yes**; 16×16×2 px, a framed translucent screen with the named painted scene; texture(s): `assets/eotp/textures/block/painted_cloud_screen.png`.
- **Painted Mountain Screen** — displays a mountain landscape on a thin decorative screen; 3D: **yes**; 16×16×2 px, a framed translucent screen with the named painted scene; texture(s): `assets/eotp/textures/block/painted_mountain_screen.png`.
- **Painted River Screen** — displays a river landscape on a thin decorative screen; 3D: **yes**; 16×16×2 px, a framed translucent screen with the named painted scene; texture(s): `assets/eotp/textures/block/painted_river_screen.png`.
- **Pale Jade Block** — builds carved jade rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/pale_jade_block.png`.
- **Placed Talisman** — stores a stamped rule, waits for Qi or resonance, fires, and wears out after repeated uses; 3D: no Blockbench model; use a 10×10×1 px paper slip attached flush to the selected face; texture(s): `assets/eotp/textures/block/placed_talisman.png`.
- **Qi Prism** — refracts incoming Qi pulses through relay, split, focus, bend, filter, or scatter facets; 3D: **yes**; within 8×8×8 px, a suspended faceted jade crystal or octahedron with clearly cut edges; texture(s): `assets/eotp/textures/block/qi_prism.png`.
- **Red Lacquered Planks** — builds seal-ready workshop furniture and red, black, or dark lacquered surfaces; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/red_lacquered_planks.png`.
- **Resonance Stone** — stores Qi and releases it only after hearing its currently tuned tone; 3D: **yes, recommended**; about 10×8×10 px, a small rounded standing stone with a carved tone groove; texture(s): `assets/eotp/textures/block/resonance_stone.png`.
- **Rice Paper Screen** — provides a thin translucent divider for painted or calligraphic interiors; 3D: **yes**; 16×16×2 px, a dark wooden lattice frame filled with translucent rice paper; texture(s): `assets/eotp/textures/block/rice_paper_screen.png`.
- **Seal Carving Table** — carves rules into reusable seals and stamps inked paper into functional talismans; 3D: **yes**; 16×9×16 px, a low lacquered bench with chisel slots, a paper bed, and a seal recess; texture(s): `assets/eotp/textures/block/seal_carving_table.png`.
- **Silk Screen** — provides a thin translucent divider for painted or calligraphic interiors; 3D: **yes**; 16×16×2 px, a fine wooden frame holding taut translucent silk; texture(s): `assets/eotp/textures/block/silk_screen.png`.
- **Spirit Bamboo** — requires ambient Qi to grow and becomes hollow when deliberately overfed; 3D: **yes, recommended**; within 6×16×6 px, a segmented stalk with small leaves and age-dependent thickness; texture(s): `assets/eotp/textures/block/spirit_bamboo_age_{0..3}.png`.
- **Spirit Jade Block** — decorative luminous jade that strongly insulates adjacent Qi devices; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/spirit_jade_block.png`.
- **Spirit Spring Water** — real flowing fluid that stabilises alchemy, nourishes spiritual plants, and assists cultivation; 3D: no custom model; use Minecraft fluid geometry; texture(s): see Fluid textures below.
- **Spirit Stone Ore** — overworld ore source for Raw Spirit Stone and the Qi-storage progression; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/spirit_stone_ore.png`.
- **White Jade Block** — builds carved jade rooms while reducing Qi leakage from adjacent devices; 3D: no custom geometry; use a standard full 16×16×16 px cube; texture(s): `assets/eotp/textures/block/white_jade_block.png`.

### Standalone items

All listed item paths are inventory/hand textures. A flat item uses a **16×16 px** PNG; a custom 3D item may use a **32×32 px** atlas while keeping the exact filename below.

- **Alchemist Hood** — completes the alchemist set while helping resist workshop hazards; 3D: no custom item geometry; use a flat 16×16 px inventory icon plus the wearable texture layer below; texture: `assets/eotp/textures/item/alchemist_hood.png`.
- **Alchemist Robe** — forms the alchemist set core and helps resist fire, poison, and magical mishaps; 3D: no custom item geometry; use a flat 16×16 px inventory icon plus the wearable texture layer below; texture: `assets/eotp/textures/item/alchemist_robe.png`.
- **Alchemist Sandals** — complete the alchemist set while keeping its light-robed silhouette; 3D: no custom item geometry; use a flat 16×16 px inventory icon plus the wearable texture layer below; texture: `assets/eotp/textures/item/alchemist_sandals.png`.
- **Alchemist Trousers** — complete the alchemist set and supply its wearable leggings layer; 3D: no custom item geometry; use a flat 16×16 px inventory icon plus the wearable texture layer below; texture: `assets/eotp/textures/item/alchemist_trousers.png`.
- **Attuning Incense** — gradually shifts nearby stored Qi toward the phase carried by the stick; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/attuning_incense.png`.
- **Binding Talisman** — spends Qi to stop nearby non-player targets and cancel their movement; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/binding_talisman.png`.
- **Body Tempering Pill** — temporarily grants strong resistance and absorption with pill tolerance; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/body_tempering_pill.png`.
- **Breakthrough Stabilising Pill** — forgives one failure and settles an unstable Golden Core; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/breakthrough_stabilising_pill.png`.
- **Calming Incense** — clears aggression and maintains Clear Heart on nearby cultivators; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/calming_incense.png`.
- **Carved Seal** — stores one reusable rule and a finite number of crisp stampings; 3D: **yes, recommended**; about 6×9×6 px, a small stamp with a square carved face and short grip; texture: `assets/eotp/textures/item/carved_seal.png`.
- **Cinnabar Pigment** — grinds into Fire-aligned ink for Ember and other forceful instructions; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/cinnabar_pigment.png`.
- **Clear Heart Pill** — removes Qi Deviation and grants a long Clear Heart state; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/clear_heart_pill.png`.
- **Clear Heart Talisman** — creates a local Clear Heart field useful against heart demons; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/clear_heart_talisman.png`.
- **Cloudstep Pill** — temporarily grants an extra midair step even without specialised shoes; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/cloudstep_pill.png`.
- **Cloudstep Shoes** — provides two midair steps, with an open foot meridian adding another; 3D: no custom item geometry; use a flat 16×16 px inventory icon plus the wearable texture layer below; texture: `assets/eotp/textures/item/cloudstep_shoes.png`.
- **Dragon Vein Compass** — paints nearby veins and reports device storage, formations, seals, and cultivation state; 3D: **yes, recommended**; about 12×3×12 px, a hand-held luopan with concentric marked rings and a central needle; texture: `assets/eotp/textures/item/dragon_vein_compass.png`.
- **Earth Essence** — supplies Earth character for roots, formations, conversion, and alchemy; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/earth_essence.png`.
- **Echo Essence** — awakens memory systems and crafts echo ink, scrolls, and advanced pills; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/echo_essence.png`.
- **Echo Mirror** — spends crown-channel Qi to show and describe recent events at their original positions; 3D: **yes, recommended**; about 12×16×2 px, a round bronze mirror in a jade-edged hand frame; texture: `assets/eotp/textures/item/echo_mirror.png`.
- **Echo Scroll** — stores one discovery that another cultivator can permanently learn; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/echo_scroll.png`.
- **Ember Talisman** — spends Fire-aligned Qi to ignite a block and burn nearby creatures; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/ember_talisman.png`.
- **Fire Essence** — supplies Fire character for roots, formations, conversion, and alchemy; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/fire_essence.png`.
- **Five Phase Harmony Pill** — adds balanced Qi and Clear Heart to stabilise conflicted roots; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/five_phase_harmony_pill.png`.
- **Flying Sword** — binds to one owner, develops intent, orbits, attacks, chains cuts, and returns; 3D: **yes, recommended**; about 28×3×4 px, a straight Chinese jian with a slim double-edged blade, small guard, tassel-ready pommel, and jade fittings; texture: `assets/eotp/textures/item/flying_sword.png`.
- **Gather Talisman** — pulls dropped items toward its stamped position; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/gather_talisman.png`.
- **Ginseng Root** — valuable slow-grown reagent for meridian, body, and breakthrough pills; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/ginseng_root.png`.
- **High Spirit Stone** — portable high-grade Qi material and ingredient for late progression; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/high_spirit_stone.png`.
- **Hollow Bamboo** — cheap brittle material produced from overfed Spirit Bamboo; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/hollow_bamboo.png`.
- **Jade Dust** — insulating powder used in thread, machinery, seals, and higher stone grades; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/jade_dust.png`.
- **Jade Imperial Seal** — cycles named household faces and stamps containers for crane routing; 3D: **yes, recommended**; about 8×10×8 px, a square seal base with a carved mythical-beast handle; texture: `assets/eotp/textures/item/jade_imperial_seal.png`.
- **Kindling Incense** — rewards still cultivators with faster insight gain inside its aura; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/kindling_incense.png`.
- **Lingzhi Cap** — polluted harvest refined into a purification and Clear Heart reagent; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/lingzhi_cap.png`.
- **Low Spirit Stone** — entry-grade refined Qi material used in basic devices and recipes; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/low_spirit_stone.png`.
- **Meridian Opening Pill** — pushes the least-developed closed meridian toward opening; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/meridian_opening_pill.png`.
- **Meridian Thread Spool** — crafting component for compact pulse-conducting meridian thread; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/meridian_thread_spool.png`.
- **Metal Essence** — supplies Metal character for roots, formations, conversion, and alchemy; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/metal_essence.png`.
- **Middle Spirit Stone** — mid-grade Qi material used in stronger devices and pills; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/middle_spirit_stone.png`.
- **Moon Lotus Petal** — night-grown Water reagent for pills, incense, and ink; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/moon_lotus_petal.png`.
- **Paper Crane** — takes one addressed container stack and physically flies it to a matching seal; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/paper_crane.png`.
- **Pill Residue** — salvageable remains from a spoiled or cracked alchemy batch; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/pill_residue.png`.
- **Preservation Talisman** — refreshes nearby dropped-item lifetimes so they do not despawn; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/preservation_talisman.png`.
- **Purified Lingzhi** — clean reagent for Clear Heart medicine, incense, and alchemist equipment; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/purified_lingzhi.png`.
- **Qi Recovery Pill** — restores a large fraction of personal Qi with diminishing repeats; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/qi_recovery_pill.png`.
- **Raw Jade** — unworked jade refined into insulating blocks, dust, seals, and devices; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/raw_jade.png`.
- **Raw Spirit Stone** — ore drop refined through Low, Middle, and High Spirit Stone grades; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/raw_spirit_stone.png`.
- **Repulsion Talisman** — throws nearby living entities away from its stamped position; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/repulsion_talisman.png`.
- **Return Talisman** — reverses nearby projectile velocity and sends attacks back outward; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/return_talisman.png`.
- **Revealing Incense** — grants Spirit Sight and makes recent world echoes appear in its room; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/revealing_incense.png`.
- **Silence Talisman** — clears nearby mob targets and suppresses aggression; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/silence_talisman.png`.
- **Spirit Bamboo Shoot** — plants Spirit Bamboo or supplies brushes, flues, paper, and incense; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/spirit_bamboo_shoot.png`.
- **Spirit Brush** — holds a limited number of ink strokes and draws phase-specific formation lines; 3D: **yes, recommended**; about 3×24×3 px, a long bamboo handle with a dark tapered bristle tip and jade collar; texture: `assets/eotp/textures/item/spirit_brush.png`.
- **Spirit Silk** — weaving component for robes, banners, thread, and Cloudstep equipment; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/spirit_silk.png`.
- **Spirit Spring Bucket** — places and transports precious Spirit Spring Water; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/spirit_spring_bucket.png`.
- **Spirit Stone Powder** — consumable catalyst charge for the Five-Phase Conversion Wheel; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/spirit_stone_powder.png`.
- **Suppressing Incense** — slowly damages undead creatures inside its aura; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/suppressing_incense.png`.
- **Sword Cultivator Boots** — reduces technique costs and contributes to projectile interception; 3D: no custom item geometry; use a flat 16×16 px inventory icon plus the wearable texture layer below; texture: `assets/eotp/textures/item/sword_cultivator_boots.png`.
- **Sword Cultivator Crown** — reduces technique costs and contributes to projectile interception; 3D: no custom item geometry; use a flat 16×16 px inventory icon plus the wearable texture layer below; texture: `assets/eotp/textures/item/sword_cultivator_crown.png`.
- **Sword Cultivator Robe** — reduces technique costs and contributes to projectile interception; 3D: no custom item geometry; use a flat 16×16 px inventory icon plus the wearable texture layer below; texture: `assets/eotp/textures/item/sword_cultivator_robe.png`.
- **Sword Cultivator Skirt** — reduces technique costs and contributes to projectile interception; 3D: no custom item geometry; use a flat 16×16 px inventory icon plus the wearable texture layer below; texture: `assets/eotp/textures/item/sword_cultivator_skirt.png`.
- **Talisman Paper** — blank paper placed on the carving table before inking and stamping; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/talisman_paper.png`.
- **Water Essence** — supplies Water character for roots, formations, conversion, and alchemy; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/water_essence.png`.
- **Wood Essence** — supplies Wood character for roots, formations, conversion, and alchemy; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/wood_essence.png`.

### Entities

All four currently use vanilla `NoopRenderer`, so their mechanics work but they are invisible except for particles. Each needs a Java model layer and renderer in addition to the texture. Entity bounds below match or stay inside the registered hitbox.

- **Flying Sword** — orbits its owner, flies through targets, chains cuts from intent, and returns; 3D: **required**, about 28×3×4 px inside the 0.5×0.5-block hitbox, a slim Chinese jian with jade fittings; texture: `assets/eotp/textures/entity/flying_sword.png` at 32×32 px.
- **Heart Demon** — mirrors a cultivator during breakthroughs and grows stronger when struck; 3D: **required**, standard humanoid proportions about 24×32×8 px inside the 0.6×1.95-block hitbox, a translucent shadow double with spectral equipment shapes; texture: `assets/eotp/textures/entity/heart_demon.png` at 64×64 px.
- **Meditating Body** — remains seated as the vulnerable anchor of a Nascent Spirit projection; 3D: **required**, seated humanoid about 24×20×12 px inside the 0.6×1.2-block hitbox, crossed legs and resting hands; texture: `assets/eotp/textures/entity/meditating_body.png` at 64×64 px.
- **Paper Crane** — visibly carries one sealed delivery and collapses when wet or struck; 3D: **required**, about 12×5×16 px including folded wings inside the 0.4×0.3-block hitbox, a small origami crane with a cargo fold; texture: `assets/eotp/textures/entity/paper_crane.png` at 32×32 px.

### Mob-effect icons

Effect icons need no model. Minecraft’s vanilla effect icons are **18×18 px**.

- **Clear Heart** — marks settled composure and resistance to heart-demon pressure; 3D: no; texture: `assets/eotp/textures/mob_effect/clear_heart.png` at 18×18 px.
- **Cloudstep** — marks temporary access to an extra midair step; 3D: no; texture: `assets/eotp/textures/mob_effect/cloudstep.png` at 18×18 px.
- **Meridian Sealed** — marks one deliberately closed channel and rerouted Qi; 3D: no; texture: `assets/eotp/textures/mob_effect/meridian_sealed.png` at 18×18 px.
- **Qi Deviation** — marks unstable circulation after backlash or a cracked pill; 3D: no; texture: `assets/eotp/textures/mob_effect/qi_deviation.png` at 18×18 px.
- **Spirit Sight** — marks vision of veins, formation lines, and echoes; 3D: no; texture: `assets/eotp/textures/mob_effect/spirit_sight.png` at 18×18 px.

### Fluid textures

Fluid geometry is supplied by Minecraft, so no 3D model is needed. Because no animation was requested, a single frame is sufficient; add `.png.mcmeta` later only if movement frames are painted.

- **Spirit Spring Water (still)** — calm source surface used by the precious cultivation fluid; 3D: no; texture: `assets/eotp/textures/block/spirit_spring_water_still.png` at 16×16 px.
- **Spirit Spring Water (flowing)** — directional texture for moving spring streams; 3D: no; texture: `assets/eotp/textures/block/spirit_spring_water_flow.png` at 32×32 px.
- **Spirit Spring Water (overlay)** — subtle blue-jade screen overlay while submerged; 3D: no; texture: `assets/eotp/textures/block/spirit_spring_water_overlay.png` at 16×16 px.

### Worn armour textures

Each armour item also needs its 16×16 icon listed above. The current code uses vanilla iron as a temporary wearable asset; after changing `EOTPArmorMaterials` to custom equipment keys, use the files below. Standard humanoid layer geometry is enough, so no Blockbench armour model is required unless protruding jade plates or sleeves are added later.

- **Sword Cultivator body layer** — renders crown, robe, and boots as light jade plates over silk; 3D: no custom geometry; texture: `assets/eotp/textures/entity/equipment/humanoid/jade_silk.png` at 64×32 px.
- **Sword Cultivator leggings layer** — renders the jade-and-silk skirt/leggings; 3D: no custom geometry; texture: `assets/eotp/textures/entity/equipment/humanoid_leggings/jade_silk.png` at 64×32 px.
- **Alchemist body layer** — renders hood, motif-covered robe, and sandals; 3D: no custom geometry; texture: `assets/eotp/textures/entity/equipment/humanoid/alchemist_robes.png` at 64×32 px.
- **Alchemist leggings layer** — renders the matching loose trousers; 3D: no custom geometry; texture: `assets/eotp/textures/entity/equipment/humanoid_leggings/alchemist_robes.png` at 64×32 px.

### Visuals that need no custom texture or model

- **Qi wisps, streams, beams, phase blooms, turbulent sparks, formation rings, tribulation traces, Echo scenes, and tablet hands** — already use phase-tinted vanilla particles.
- **Dragon veins** — already appear as world-space particle lines drawn by the compass.
- **Sounds** — already use vanilla sound events; no custom audio file is required.


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
