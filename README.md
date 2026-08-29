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
- **Kindling Incense** — deepens the practice of a still cultivator's most neglected meridian; 3D: no 3D model; use a flat generated-item model with a 16×16 px icon; texture: `assets/eotp/textures/item/kindling_incense.png`.
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

1. Mine spirit stone ore and cut Raw Spirit Stone with Jade Dust into a Low Spirit Stone.
2. Grind charcoal on an inkstone, dip a brush, and draw a ring of formation ink with a node in it.
3. Stamp a seal onto the ring, put a formation core in the middle, and right-click it: it names the
   rule you wrote, or the exact clause that fails.
4. Feed it from a Jade Bi Reservoir through a bamboo flue and watch how much Qi the corners waste.
5. Hold a reservoir steady on a dragon vein, let a moon lotus finish a whole night, and put real
   practice into one meridian: those are the Three Witnesses.
6. Kneel on a strong vein and breathe the Ninefold Returning Breath to enter Breath Gathering — and
   put some lightning rods up long before you try for Golden Core.

## Complete survival progression and interaction map

Progression is not *meditate, fill bar, press breakthrough*. It is **perceive, demonstrate, inscribe,
synthesize, embody, maintain, survive contradiction, leave an Echo**. There is no Insight currency
anywhere in the mod.

### Survival status

A fresh survival world can enter and complete this route without commands or creative items. Ore,
the four cultivation plants and rare Spirit Springs generate naturally; Bamboo Shoots and Ginseng
Roots replant directly; formation discoveries are learned by building and reading a circuit; Echo
Essence recovers Reverse Cycle and advanced Prism facets.

### Stage 0 — finding the hidden world

1. **Mine naturally generated Spirit Stone Ore.** Veins occur between Y -32 and 80 and pick the
   normal or deepslate variant from the surrounding stone. Both drop Raw Spirit Stone.
2. **Craft the Dragon-Vein Compass** from a vanilla compass and Raw Jade. In the air it paints vein
   segments and reports strength, phase, flow direction and crossings, plus your cultivation state.
   On a device it reports stored Qi; on a core, the reading; on a stamped container, its seal face.
3. **Choose a site.** Vein strength raises device efficiency and ambient Qi; an intersection is the
   best long-term cultivation room. Veins are a function of the world seed, not a structure.
4. **Collect the natural starters.** Spirit Bamboo, Moon Lotus, Earthroot Ginseng and Lingzhi grow in
   rare Overworld patches. Underground Spirit Springs are rare and cannot be duplicated, so every
   bucket matters.

### Stage 1 — first materials and preparing spiritual matter

1. **Temper Spirit Stones rather than compressing grades.** Raw Spirit Stones are unstable pieces of
   crystallised ambient Qi. **Raw Spirit Stone + Jade Dust cuts into a Low Spirit Stone**, the normal
   early battery. Higher grades are never crafted from stacks of lower ones.

   A Low stone becomes **Middle** only by participating in cultivation. Leave it in the hole of a
   Jade Bi Reservoir: it records each distinct phase the disc carries, and once it has held **three
   different phases** without the disc going heavily turbulent, taking it back out cuts it into a
   Middle Spirit Stone. Letting turbulence pass 50% wipes what it had learned.

   A Middle stone becomes **High** only by surviving Heaven. Leave it tempering in a disc that takes
   a routed Tribulation bolt; it becomes Tribulation-Charged for 30 seconds, and using a Spirit
   Spring bucket on the disc within that window quenches it into a High Spirit Stone.

   One Low stone can still be crushed into four Spirit Stone Powder catalyst charges.

2. **Grow Spirit Bamboo near ambient Qi.** It will not progress in a dead site. Normal feeding makes
   useful bamboo; excessive nearby device Qi makes the next section Hollow Spirit Bamboo. Two shoots
   make Hollow Bamboo as a crafting shortcut.
3. **Make Spirit Silk** from three strings and Jade Dust. Bamboo, silk, jade, paper and stone are the
   shared inputs for almost every early magical tool.
4. **Build an Inkstone** from blackstone and a **Spirit Brush** from bamboo and Spirit Silk. Put one
   pigment on the Inkstone, then empty-hand click six times to grind it fully. Dipping early gives
   proportionally fewer strokes.
5. **Build simple decorated rooms.** Celadon and carved jade adjacent to a `QiNode` reduce its
   passive leakage. Lacquered blocks are the seal-workshop surfaces. Stairs, slabs and walls share
   the family texture and mechanical tag.

**Ink inputs:** charcoal/coal → Plain; Cinnabar Pigment → Fire; Jade Dust → Metal; Wood Essence →
Wood; clay or Earth Essence → Earth; Moon Lotus Petal → Water; Echo Essence → Echo ink.

**Stone chain:** raw crystal → Low practical storage → lived phase history → Middle stone →
controlled Tribulation + quenching → High stone.

### Stage 2 — producing and moving Qi

The first renewable machine Qi comes from Moon Lotus; later, Gathering Formations, Dragon Veins and
controlled Tribulation become the large sources.

1. **Plant Moon Lotus over water or mud with an open sky.** It closes by day. At night it inserts
   Water Qi into its storage, grows up to three petals, and pushes Qi into adjacent `QiNode`s.
2. **Put a Jade Bi Reservoir beside it.** 400 base Qi. Nearby discs raise one another's capacity with
   diminishing returns, so a spaced ring beats an opaque cube. The hole also tempers stones.
3. **Run Bamboo Qi Flues from storage.** Bends, climbs, long runs, Hollow Bamboo and turbulence all
   increase loss. Jade Flue Joints forgive bends and reset run length. Flow only moves downhill by
   fill ratio.
4. **Use a Bagua Distributor at branches.** Click cycles Alternate, Clockwise, Opposed, Priority,
   Spread and Generative. Sneak-click rotates its front. Generative inspects neighbour phase and
   avoids combinations that fight.
5. **Use the Five-Phase Conversion Wheel.** Spirit Stone Powder is the catalyst; click chooses the
   source phase. Forward runs Wood → Fire → Earth → Metal → Water at 70%. Reverse requires its
   discovery, returns 40% and adds turbulence.
6. **Use Jade Meridian Thread for logic, not bulk power.** One block per tick, fixed toll per block.
   Branches duplicate a signal; they do not divide continuous power.
7. **Use the Qi Prism for open-space pulses.** Relay, Split, Focus, Bend, Filter and Scatter.
   Advanced facets require their discovery.
8. **Use Bells, Resonance Stones and the Abacus for control.** Bell pitch encodes pulse strength and
   broadcasts a tone; a Resonance Stone holds Qi until its tone is heard; the Abacus counts
   fullness, emptiness, pulses, tones or intervals, and its bead value is the threshold.
9. **Use Footwork Seals for movement input.** Sneaking, walking, running, leaping and Cloudstepping
   give progressively stronger pulses and different tones.

**Power chain:** Moon Lotus / Gathering Formation / Dragon Vein / Tribulation → Reservoir or Spirit
Stone → Flue → Bagua → conversion or storage → machine.

**Logic chain:** Abacus / Footwork Seal / Bell → Meridian Thread or Prism → Resonance Stone /
Talisman / Formation Core.

### Stage 3 — herbs, essences, and the Five Phases

1. **Grow Earthroot Ginseng in a varied garden.** It counts distinct nearby dirt, logs, leaves,
   flowers, stone, fluids and tagged natural blocks. Rows of one block grow nothing worth digging.
2. **Place Lingzhi near turbulent machinery.** Each second it removes turbulence from nearby storage;
   absorbed pollution fills four saturation stages and harvests into that many Lingzhi Caps.
3. **Use the Herb Drying Rack to choose what an herb becomes.** A bundle records sun, moon and
   censer-smoke exposure, and the dominant condition at completion decides the result.
4. **Create the phase essences.** Moon Lotus in sun → Fire, moonlight → Water, smoke → Echo. Ginseng
   in sun → Earth, moonlight → Wood, smoke → Metal. Lingzhi under any finished route → Purified
   Lingzhi. Bamboo under smoke → Hollow Bamboo, otherwise → Wood Essence.
5. **Burn incense beside a rack to force its smoke route.** Calming needs Bamboo, gunpowder and Moon
   Lotus and is the intended first smoke source. Revealing uses Echo Essence, Suppressing uses
   Purified Lingzhi, Kindling uses Fire Essence, Attuning uses Earth Essence. Kindling now deepens
   the practice of whichever meridian you have neglected rather than granting experience.

**Garden chain:** ambient Qi → plants → rack condition → phase essences and Echo Essence →
conversion, alchemy, seals, Inner Landscapes, advanced machines and discoveries.

### Stage 4 — ink, seals, and talismans

1. **Make Talisman Paper** from paper and a Spirit Bamboo Shoot, then lay one sheet on the
   Seal-Carving Table.
2. **Select a rule.** Empty-hand click cycles Bind, Repel, Gather, Silence, Preserve, Return, Divide.
3. **Carve the seal** with wood, stone, bronze or jade: 8, 20, 48 or 128 stampings.
4. **Brush the paper with loaded ink, then stamp it.** The rule says what happens; the ink says in
   what manner. Bind with Fire ink makes Ember; ordinary Bind makes Binding.
5. **Use or place the result.** On a creature it needs the Hand Meridian and costs twice the normal
   Qi. On a solid face it becomes a Placed Talisman with eight charges, fired by pulses or by slowly
   accumulated Qi.

**Implemented talismans:** Repulsion, Ember, Binding, Preservation, Clear Heart, Gather, Silence and
Return.

### Stage 5 — formation grammar

Formation marks are explicitly **not another Qi cable system**. Flues carry continuous Qi and
Meridian Thread carries logic pulses; marks describe **what separately supplied Qi is ordered to do**.

1. **Make Line Tiles** from smooth stone. Line + Jade Dust → Arc; Line + Low Spirit Stone → Node;
   Node + Echo Essence + Raw Jade → Trigram. Formation Ink continues writing across awkward surfaces.
2. **Lay a readable statement around a Formation Core.** The Core begins at the stamped governing
   seal and reads outward through Lines and Arcs. Nodes separate clauses. Trigrams alter direction,
   phase interpretation and symmetry. Ink joins the writing but never carries machine power.
3. **A formation must make a complete statement.** Its path must return to its governing rule.
   Unresolved branches, or two seals giving two equally valid readings, make it inert.
4. **Right-click the Core to inspect its interpretation.** Instead of a blanket refusal it names the
   failing clause: no governing seal, ambiguous reading, statement too short, no return, no clause,
   marks not turned inward or outward, too few trigrams, too few phases, or missing both clauses.
5. **Feed the Core Qi separately** through its actual `QiNode`. Upkeep drains once a second; matching
   the preferred phase raises strength and mismatched Qi weakens it. Banners add vertical reach and a
   capped strength bonus.

**Grammar outcomes:**

- **Gathering** — Gather seal, at least one clause-ending Node, marks reading inward, closed, 8+
  marks. 8 Qi/s, any phase. Draws ambient vein Qi to the Core and pushes it into adjacent devices; it
  does not receive Qi through its own lines.
- **Repulsion** — Repel seal, marks reading outward, closed, 12+ marks. 12 Qi/s, prefers Metal.
  Banners turn the floor field into a taller wall.
- **Preservation** — Preserve seal, uninterrupted closed outer clause, 10+ marks. 10 Qi/s, prefers
  Earth. Dropped items inside stop ageing toward despawn.
- **Cultivation** — closed statement, 4+ Trigrams, 3+ ink phases, at least one inward and one
  returning clause, 16+ marks. 20 Qi/s, prefers balance. It generates nothing on its own: it
  stabilises the Witness, Self-Script, Core and Nascent-Spirit rituals.
- **Attunement** — closed five-phase statement, all five inks, 2+ Trigrams, 20+ marks. 40 Qi/s,
  prefers balance. Used to read and imprint an Inner Landscape.

**Formation chain:** governing seal + written topology + orientation + phase language + separately
supplied Qi → interpreted magical rule.

### Stage 6 — entering cultivation: the Three Witnesses

**Mortal → Breath Gathering.** Give three demonstrations, in any order:

- **Witness of Earth** — draw from a live Dragon Vein without roughening it: keep a connected
  reservoir holding Qi at under 35% turbulence, standing on a vein of at least 25% strength, for 60
  continuous seconds.
- **Witness of Heaven** — let a complete natural cycle finish: a Moon Lotus that opens, produces
  through the night while plumbed into something, and closes at dawn unbroken and not force-fed.
- **Witness of Self** — put real practice into any one Meridian. Mortals accumulate practice even
  though nothing can open yet, so no early action is wasted.

Then perform the **First Breath Ritual**: crouch, still, on a vein of at least 35% strength and
breathe the Ninefold Returning Breath, alternating downstream and upstream every two seconds. The
three Witnesses are consumed as proofs. Breath Gathering gives 60 Qi capacity and slow regeneration.

**Open Meridians by doing their work.** Hand 100 from bound-sword and talisman use; Foot 100 from
leaps, Footwork Seals and Cloudsteps; Heart 140 from surviving danger and holding Clear Heart; Crown
140 from reading Echoes and inspecting formations; **Dantian 200 and practice in at least three other
channels**, raising capacity by 50%.

**Breath Gathering → Foundation: inscribe a Self-Script.** The world has been filing your significant
Echoes under tendencies: protecting, returning, transforming, wandering, enduring, creating, cutting,
tending, commanding, observing, stillness. Inside a stable Cultivation Formation the ritual shows
your three strongest as glyphs, and you must **perform an action of each again** before the circle
loses patience. Those three collapse into a Self-Script — Hearth, River-Blade, Mountain, Distant-Sky,
Scripture, or an Unnamed script if they make no known shape. The tendencies are kept, so two Hearth
cultivators are still not the same person, and a contradictory script is harder to hold but stranger
later.

**Foundation progression: Verses instead of experience.** A Verse is 2–4 compatible principles —
Flow + Divide + Return, Heat + Transformation + Preservation, Motion + Edge + Return, Sound + Command
+ Repetition, Growth + Water + Stillness. Hold the principles true in the world at the same time and
use a blank Echo Scroll to draft them; when they are all true at once the scroll awakens into a
mastered Verse.

**Foundation → Golden Core: form a Core Thesis.** Lay three mastered Verse scrolls around a
Cultivation Formation, dropped on it or in a container on it. If their principles cover a recorded
signature they condense into a Thesis:

- Flow + Edge + Return → **Revolving Edge Core**
- Stillness + Earth + Preservation → **Immovable Mountain Core**
- Growth + Transformation + Fire → **Vermilion Furnace Core**
- Echo + Command + Repetition → **Ten-Thousand Words Core**
- anything else coherent → **Unwritten Core**

Golden Core still gives roughly 700 Qi, persistent storage and passive artifact support, but the
Thesis is the actual reward.

**Golden Core → Nascent Spirit.** Requires a Self-Script, a Core Thesis, three open Meridians, an
imprinted Inner Landscape and at least one survived Tribulation. Roughly 1,800 Qi and Spirit
Projection; sneak-use the Echo Mirror to leave the body or return.

**Ritual failure.** Rituals start at 60% stability, read from phase harmony, Formation strength,
Clear Heart, Script coherence, Landscape stability and lessons from earlier failures. Nothing is
deleted on collapse. Instead the ritual leaves **Core Discord** on whichever principle was failing,
which weakens every technique until you **demonstrate that principle again in ordinary play**, or
suppress one with a Breakthrough-Stabilising Pill.

### Stage 7 — Heart Demons and Heaven's Contradiction

**Heart Demon.** Rises during dangerous rituals and binds to you. Weapon damage is cancelled and
restores its resolve; Clear Heart erodes it; four landed hits fail the ritual. Dispelling the first
teaches Heart Demon lore.

**Heaven's Contradiction.** Tribulation is no longer seven bolts because you pressed breakthrough.
The formation reads your Self-Script, Thesis and strongest systems and selects contradictions
weighted toward what you actually rely on:

- **Reversal** — Qi runs backward and roughens.
- **Excess** — one phase floods and must be spent or converted.
- **Silence** — bells and resonance stones stop transmitting.
- **Fracture** — a formation clause becomes unreadable.
- **False Echo** — ghost actions replay old inputs.
- **Tribulation Lightning** — conductor-seeking bolts strike the workshop.
- **Withering** — Landscape plants stop producing their phase.
- **Return Denied** — swords and Return effects cannot complete on their own.

Lightning remains the signature trial rather than the whole system. Bolts still choose the highest
tagged conductor within ten blocks, then another `QiNode`, then you; a machine takes up to 400 Metal
Qi and heavy turbulence. Catching three teaches Tribulation Routing, and **a Middle Spirit Stone
tempering in a struck disc becomes Tribulation-Charged**, ready to be quenched into a High stone.

**Recovery.** Death empties personal Qi and adds 6,000 ticks of instability instead of deleting
progression. Script, Verses, Thesis and Landscape imprint all survive death.

### Stage 8 — the Inner Landscape

This replaces Spiritual Root attunement. There is no random root and no menu: you raise a place, and
eventually you carry the memory of that place inside you.

1. **Establish it.** After Golden Core, build an Attunement Formation and place a **Landscape Stele**
   at its Core. This claims nothing — no chunk, no structure, no protection.
2. **Build a working ecology.** The Stele watches living spiritual plants, water, mineral and block
   diversity, fire and light processes, machinery, Dragon-Vein input, phase production, phase
   consumption, turbulence removal and the creatures living there. Five coloured blocks do nothing.
3. **Make it self-sustaining.** It matures after surviving **three complete day/night cycles** while
   producing at least three phases, having a real consumer for every phase it produces, and never
   letting one phase exceed 70% of the whole. Automation is encouraged: a Moon Lotus feeding a
   reservoir whose Water Qi is converted to maintain Ginseng, with Lingzhi cleaning the turbulence,
   is a legitimate ecology.
4. **Imprint it.** Use Echo Essence at a mature Stele inside a running Attunement Formation. Instead
   of "Water + Wood" you get something like a **Water-Wood Return Garden**: dominant phase, secondary
   phase, strongest relationship, and cyclical, stability and diversity ratings.
5. **Effects.** Dominant and secondary phases set Qi absorption efficiency. A cyclical Landscape
   strengthens returning techniques through the Hand channel; a stable one strengthens the Heart
   channel, barriers and alchemy; a transformative one strengthens Crown and Dantian work; a diverse
   one strengthens movement and growth.
6. **Heaven Scars.** Surviving a Tribulation with a live Landscape leaves a permanent mark on its
   history — Lightning, Drought, Reversal or Ash — and it keeps only the three most recent.
7. **Changing path.** Cultivate and imprint a different Landscape. Replacing an imprint destabilises
   the Golden Core for 2,400 ticks.

**Landscape chain:** build an ecosystem → make its relationships genuinely function → let it acquire
history → imprint that history → affinity and Core interactions emerge from the place you cultivated.

### Stage 9 — the complete alchemy route

Ingredients go into the Ding in order, the requested Qi blend comes through the network, and heat
comes from below: lava 900, fire 700, campfire 620, soul campfire 520, magma 420, plus up to 420 more
from Fire Qi. A water bucket cleans residue; a **Spirit Spring bucket cleans and blesses the batch**,
supplying the final 15% that makes Perfect quality reachable.

- **Qi-Recovery:** Low Spirit Stone → Moon Lotus Petal → Spirit Stone Powder; 300–620; balanced; 2.
- **Meridian-Opening:** Ginseng Root → Meridian Thread Spool → Middle Spirit Stone; 480–760;
  Wood/Water; 1.
- **Clear-Heart:** Moon Lotus Petal → Purified Lingzhi → Water Essence; 220–480; Water; 2.
- **Body-Tempering:** Ginseng Root → Earth Essence → Metal Essence → Jade Dust; 620–900; Earth/Metal;
  1.
- **Cloudstep:** Spirit Bamboo Shoot → Moon Lotus Petal → Spirit Silk; 260–520; Water/Wood; 2.
- **Five-Phase Harmony:** Wood → Fire → Earth → Metal → Water Essence; 400–700; balanced; 1.
- **Breakthrough-Stabilising:** High Spirit Stone → Ginseng Root → Echo Essence → Jade Dust; 540–820;
  Earth; 1.

Every second scores heat, phase similarity, calmness and cleanliness. The average gives Cracked below
45%, Ordinary from 45%, Refined from 75%, Perfect from 93%. Scorching above 1,050 sharply lowers the
score, failures return Pill Residue, and repeats raise tolerance until it decays.
Breakthrough-Stabilising Pills no longer improve breakthrough odds: they suppress Core Discord and
shorten post-failure and post-death instability.

**Alchemy chain:** garden and ore → order + heat + phase-engineered Qi + clean Ding → deterministic
quality → cultivation, combat, movement, Landscape maintenance and spiritual recovery.

### Stage 10 — sword cultivation and Cloudstep

1. **Craft the Flying Sword** from diamonds and Raw Jade; a non-Mortal sneak-right-clicks to bind it.
2. **Open the Hand Meridian.** Right-click at a creature spends 12 Qi to send the orbiting sword for
   one strike and a return. Golden Core allows Core-Thesis interactions; Flowing River Intent adds a
   chained cut.
3. **The sword learns from real fights:** undead → Purifying, aerial descents → Falling Star, crowds
   → Flowing River, guarded use → Mountain, precise use → Still Water.
4. **Right-click with no target to release Sword Qi** for 8 Qi: damage, cleared vegetation, stripped
   buffs and pulses into formation devices it crosses.
5. **Cloudstep Shoes or a Cloudstep Pill.** At the jump apex, or falling fast while sprinting, spend
   6 Qi to kick forward and up. Shoes give two steps, the pill one, an open Foot Meridian one more,
   capped at three.
6. **Sets.** Each Sword Cultivator piece cuts technique cost by 5%, the full set by 30% and spends 5
   Qi to cancel one indirect hit while a bound sword is held. The full Alchemist set reduces Fire and
   Magic damage to 60%.

Sword Intent is part of the blade's own remembered history, so a Mountain-Intent sword in the hands
of a Mountain-Script cultivator fits better than one crafted five minutes ago.

**Combat chain:** fighting style → Sword Intent → flying-sword behaviour; Self-Script + Thesis +
Meridian + Qi + equipment decide which techniques emerge.

### Stage 11 — Echoes, discoveries, Verses, and automation

1. The world records block breaking and placement, deaths, rituals, finished pills, sword techniques,
   awakened tablets, Landscape imprinting, Core formation and Tribulation. Ordinary entries expire;
   major progression Echoes persist.
2. **Echo Mirror** with an open Crown Meridian, 4 Qi: draws each recent event where it happened and
   says how long ago.
3. **Echo Scrolls.** A filled scroll teaches its discovery. A blank one copies a discovery you hold.
4. **Verse Scrolls after Foundation.** A blank scroll drafts a Verse from the principles you are
   currently proving, and awakens when they are all true together.
5. **Verses can be shared but not blindly consumed.** Another player's mastered Verse shows you the
   relationship, but you must reproduce it once before it counts toward your own Thesis.
6. **Ancestral Tablet** within five blocks of a device: repeat a job and it records Strike, Feed,
   Turn, Stir or Harvest at relative positions. Three repetitions make it a lesson.
7. **Feed it Echo Essence to awaken it.** Every 40 ticks it spends 3 Qi and replays the next gesture
   with visible Echo hands, pulling items from an adjacent container.
8. **Imprint-compatible targets:** Ding, Censer, Drying Rack, Bell, Bagua, Conversion Wheel, Moon
   Lotus and Lingzhi.

**Knowledge chain:** world action → Echo → discovery → several demonstrated together → Verse →
Self/Core progression.

### Stage 12 — sealed logistics

1. **Jade Imperial Seal**, sneak-used, cycles Hall, Kitchen, Store, Kiln, Garden, Study, Gate and
   Workshop.
2. **Use it on a container** to apply that household mark; the same mark again removes it.
3. **Hold a Paper Crane with the seal in the other hand** to address it.
4. **Use the crane on a source container.** It spends 5 Qi, takes the first non-empty stack and flies
   to the nearest loaded container with the same mark within 64 blocks.
5. **Rain, water or one hit destroys it** and drops the cargo; a full or missing destination makes it
   set the cargo down rather than delete it.

**Logistics chain:** named seal face → stamped household → addressed crane + Qi → visible transport,
deliberately slower and more fragile than pipes.

### Stage 13 — late-game workshop and cultivation loop

- Dragon Veins and Moon Lotus give baseline Qi; Gathering Formations scale it; Tribulation is the
  dangerous burst source.
- Low stones are ordinary storage, Middle stones have cultivated phase histories, High stones have
  survived Heaven.
- Flues move power, Bagua routes it, Conversion Wheels make the needed phase, Lingzhi eats the
  turbulence both create.
- Formation marks never duplicate that transport network: they are a grammar telling separately
  supplied Qi which rule to enact.
- Abacuses, Bells, Resonance Stones, Footwork Seals, Threads and Prisms are the control layer.
- Gardens and Drying Racks make the essences alchemy, incense, ink and Landscapes need.
- The Three Witnesses make interaction with the world the entrance requirement instead of meditation.
- Meridians open by doing the work they exist to support.
- The Self-Script makes Foundation a record of who you turned out to be.
- Discoveries combine into Verses; three demonstrated Verses become your Core Thesis.
- The Inner Landscape makes affinity a consequence of a place you actually cultivated.
- Heart Demons attack contradictions in your own behaviour; Heaven attacks the systems you claim to
  understand.
- Survived Tribulations leave history in High Spirit Stones, Core development and Heaven Scars.
- Pills support meridians, combat, movement, Landscapes and Discord rather than being XP bottles.
- Echo Mirrors reveal history, Scrolls preserve understanding, Verses preserve relationships, Tablets
  repeat demonstrated work, Seals group inventories and Cranes move the results.

**observe the world → demonstrate that you understand it → make its Qi physically useful → cultivate
plants and machinery into stable relationships → let your own actions form a Self-Script →
reconstruct lost principles as Verses → synthesize those principles into a Golden Core → cultivate a
living Landscape whose history becomes your affinity → survive Heaven attacking the exact weaknesses
of what you built → teach the workshop to remember and repeat your work.**

## Proposed reduced-scope progression and art plan (design only)

**Status:** This is a recommendation only. It does not replace or delete the complete progression above, and it makes **no gameplay-code change**. It defines a version one artist can finish without losing the mod’s original identity.

### Recommendation: alchemy, written formations, and Inner Landscapes

Keep almost all play grounded: stone, bamboo, ceramic, paper, smoke, water and carefully handled Qi. The **only overt high-xianxia event** should be the Inner Landscape—the player carries a garden’s memory, not a generic elemental class or combat build.

#### Cut from a reduced release

- Classic realm ladder, Meridians, personal Qi combat pool, Cloudstep, Flying Sword, Sword Intent, Sword Qi, armour sets and Footwork progression.
- Heart Demon, projection, combat-personalised Tribulation, Core Thesis and Verse player upgrades.
- High Spirit Stone as a mandatory gate; retain it only as optional prestige workshop history.

#### Keep because this is the identity

- Visible Qi, turbulence, flues, reservoirs, Bagua routing, conversion, bells, resonance and Abacus logic.
- Ding process alchemy, Drying Rack conditions, incense and deterministic quality.
- Formation grammar: seals, written marks, Core interpretation and named failure clauses.
- Plants, Spirit Spring, Dragon Veins, Landscape Stele, Echoes, Tablets, Imperial Seals and Paper Cranes.

### Revised reduced-scope progression

**1. Find a place worth tending.** Find Spirit Stone Ore, a Dragon Vein, a rare Spirit Spring and spiritual plants. Cut Raw Spirit Stone with Jade Dust into Low Spirit Stone. The goal is a living workshop, not player combat advancement.

**2. Build the quiet workshop.** Use Low Spirit Stone/Moon Lotus, a Bi Reservoir and Bamboo Flues. Make Inkstone, Brush and Seal-Carving Table. Jade/celadon reduce leakage; lacquer gives the seal workspace its identity.

**3. Write instructions rather than build machines.** Grind ink, carve seals and make Talismans. A formation is a closed statement: Lines/Arcs continue clauses, Nodes separate them, Trigrams add emphasis and phase Ink gives context. The Core names broken clauses. Gathering brings vein Qi to storage, Preservation protects ingredients/drops, Repulsion keeps gardens safe. Flues power the Core; marks never become cables.

**4. Grow ecology.** Moon Lotus makes night Water Qi; Ginseng rewards variety; Lingzhi clears turbulence; the Rack turns sun/moon/smoke into essences. The Ding combines ingredient order, heat and phase-conditioned Qi into workshop medicines—recovery, purification and stabilisation, not combat buffs. Low → Middle → High stone tempering stays optional workshop history.

**5. Make it self-sustaining.** Build an Attunement Formation and Landscape Stele around plants, water, machinery and consumers. It matures after three day/night cycles with three produced phases, a consumer for every phase and no phase monopoly. Imprint it with Echo Essence: the Inner Landscape is the single high-fantasy transition, improving workshop circulation, stability, conversion or plant recovery.

**6. Let the workshop remember.** Mirror recent work, preserve discoveries on Scrolls, and teach Tablets repeated jobs. Imperial Seals name rooms; Paper Cranes move real ingredients. The endgame is a workshop that remembers who cultivated it.

**Reduced loop:** discover a vein → stabilise Qi → write formations → cultivate plants → refine medicines/essences → build balanced ecology → imprint its Inner Landscape → teach the workshop to repeat its work.

### Block-model production priority

**P0 — make first** is a coherent vertical slice. **P1 — complete loop** supports it. **P2 — optional support** can wait. **P3 — decorative/defer** should reuse templates or remain placeholders. One Blockbench pixel is 1/16 block.
- **P1 — complete loop — Ancestral Tablet:** Echo automation teacher; 10×14×4 tablet on a footed base; two state textures.
- **P1 — complete loop — Bagua Distributor:** Qi branch router; 16×5×16 octagonal bronze disc, eight trigram wedges and central socket.
- **P0 — make first — Bamboo Qi Flue:** continuous Qi transport; 16×6×6 hollow bamboo tube along its axis; rotate one model.
- **P3 — decorative/defer — Black Lacquered Planks:** lacquered workshop decoration; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P1 — complete loop — Bronze Spirit Bell:** pulse/resonance reader; 10×14×10 flared hanging bell with loop and clapper.
- **P3 — decorative/defer — Carved Jade Border:** jade decorative insulation; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P3 — decorative/defer — Celadon Basin:** celadon decorative insulation; 14×6×14 shallow glazed bowl.
- **P3 — decorative/defer — Celadon Bricks:** celadon decorative insulation; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P3 — decorative/defer — Celadon Jar:** celadon decorative insulation; 10×14×10 round jar with neck/lid.
- **P3 — decorative/defer — Celadon Lamp:** celadon decorative insulation; 10×15×10 pierced ceramic lantern.
- **P3 — decorative/defer — Celadon Pillar:** celadon decorative insulation; 16×16×16 pillar cube with side/end textures; no Blockbench.
- **P3 — decorative/defer — Celadon Slab:** celadon decorative insulation; vanilla slab template; shared family texture; no Blockbench.
- **P3 — decorative/defer — Celadon Stairs:** celadon decorative insulation; vanilla stair template; shared family texture; no Blockbench.
- **P3 — decorative/defer — Celadon Tiles:** celadon decorative insulation; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P3 — decorative/defer — Celadon Wall:** celadon decorative insulation; vanilla wall template; reuse Celadon Bricks texture; no Blockbench.
- **P3 — decorative/defer — Dark Jade Block:** jade decorative insulation; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P3 — decorative/defer — Dark Lacquered Planks:** lacquered workshop decoration; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P1 — complete loop — Deepslate Spirit Stone Ore:** deep ore source; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P0 — make first — Ding Cauldron:** process alchemy vessel; 14×14×14 three-legged bronze Ding with deep mouth and handles.
- **P1 — complete loop — Earthroot Ginseng:** varied-garden herb; 10×6×10 low leaves with exposed forked root; age textures.
- **P1 — complete loop — Five Phase Conversion Wheel:** phase converter; 16×16×5 five nested bronze/jade rings.
- **P2 — optional support — Footwork Seal:** footwork seal; 16×0.25×16 floor decal; no Blockbench.
- **P0 — make first — Formation Arc Tile:** formation statement mark; 16×1×16 floor tile; shared JSON template, unique top texture; no Blockbench.
- **P1 — complete loop — Formation Banner:** vertical formation amplifier; 6×32×3 pole with phase-coloured silk; one recolourable model.
- **P0 — make first — Formation Core:** formation grammar reader; 12×3×12 jade altar-disc with central socket.
- **P0 — make first — Formation Ink:** phase-coloured written connection; 16×0.25×16 decal; five strokes × phase recolours; no Blockbench.
- **P0 — make first — Formation Line Tile:** formation statement mark; 16×1×16 floor tile; shared JSON template, unique top texture; no Blockbench.
- **P0 — make first — Formation Node Tile:** formation statement mark; 16×1×16 floor tile; shared JSON template, unique top texture; no Blockbench.
- **P0 — make first — Formation Trigram Tile:** formation statement mark; 16×1×16 floor tile; shared JSON template, unique top texture; no Blockbench.
- **P3 — decorative/defer — Hanging Scroll Flame:** decorative calligraphy scroll; 10×16×1 hanging scroll; one shared paper/roller model, glyph texture changes.
- **P3 — decorative/defer — Hanging Scroll Gate:** decorative calligraphy scroll; 10×16×1 hanging scroll; one shared paper/roller model, glyph texture changes.
- **P3 — decorative/defer — Hanging Scroll Mountain:** decorative calligraphy scroll; 10×16×1 hanging scroll; one shared paper/roller model, glyph texture changes.
- **P3 — decorative/defer — Hanging Scroll Return:** decorative calligraphy scroll; 10×16×1 hanging scroll; one shared paper/roller model, glyph texture changes.
- **P3 — decorative/defer — Hanging Scroll River:** decorative calligraphy scroll; 10×16×1 hanging scroll; one shared paper/roller model, glyph texture changes.
- **P3 — decorative/defer — Hanging Scroll Silence:** decorative calligraphy scroll; 10×16×1 hanging scroll; one shared paper/roller model, glyph texture changes.
- **P3 — decorative/defer — Hanging Scroll Stillness:** decorative calligraphy scroll; 10×16×1 hanging scroll; one shared paper/roller model, glyph texture changes.
- **P3 — decorative/defer — Hanging Scroll Sword:** decorative calligraphy scroll; 10×16×1 hanging scroll; one shared paper/roller model, glyph texture changes.
- **P0 — make first — Herb Drying Rack:** environmental herb processor; 16×10×4 bamboo rack with four hanging hooks.
- **P1 — complete loop — High Spirit Stone Block:** tribulation-history storage; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P1 — complete loop — Hollow Bamboo Qi Flue:** cheap lossy tube; 16×6×6 cracked hollow bamboo tube.
- **P1 — complete loop — Hollow Spirit Bamboo:** hollow spirit bamboo; 6×16×6 split stalk; reuse bamboo geometry.
- **P1 — complete loop — Incense Censer:** slow workshop aura; 10×11×10 lidded bronze censer, feet, handles and smoke holes.
- **P0 — make first — Inkstone:** magical ink grinder; 14×3×12 dark slab with grinding slope and ink well.
- **P1 — complete loop — Jade Abacus:** automation condition controller; 14×10×3 jade frame, rails and bead positions.
- **P0 — make first — Jade Bi Reservoir:** Qi buffer and Spirit Stone tempering; 14×3×14 jade Bi disc with a real centre hole.
- **P1 — complete loop — Jade Flue Joint:** low-loss tube turn; 8×8×8 carved jade elbow/socket.
- **P3 — decorative/defer — Jade Inlay:** jade decorative insulation; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P1 — complete loop — Jade Meridian Thread:** logic pulse line; 16×1×16 face-attached line; no Blockbench.
- **P3 — decorative/defer — Jade Pillar:** jade decorative insulation; 16×16×16 pillar cube with side/end textures; no Blockbench.
- **P3 — decorative/defer — Jade Slab:** jade decorative insulation; vanilla slab template; shared family texture; no Blockbench.
- **P3 — decorative/defer — Jade Stairs:** jade decorative insulation; vanilla stair template; shared family texture; no Blockbench.
- **P3 — decorative/defer — Lacquered Low Table:** lacquered workshop decoration; 16×8×16 low table with curved legs.
- **P3 — decorative/defer — Lacquered Panel:** lacquered workshop decoration; 16×16×2 inset panel; simple JSON geometry; no Blockbench.
- **P3 — decorative/defer — Lacquered Pillar:** lacquered workshop decoration; 16×16×16 pillar cube with side/end textures; no Blockbench.
- **P3 — decorative/defer — Lacquered Screen:** lacquered workshop decoration; 16×16×2 lacquer frame and lattice.
- **P3 — decorative/defer — Lacquered Slab:** lacquered workshop decoration; vanilla slab template; shared family texture; no Blockbench.
- **P3 — decorative/defer — Lacquered Stairs:** lacquered workshop decoration; vanilla stair template; shared family texture; no Blockbench.
- **P0 — make first — Landscape Stele:** Inner Landscape ecology reader; 8×15×8 jade stele with inset landscape panel; mature state.
- **P1 — complete loop — Lingzhi Spirit Fungus:** turbulence-cleaning fungus; 10×7×10 layered shelf fungus; saturation textures.
- **P1 — complete loop — Low Spirit Stone Block:** daily Qi storage; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P1 — complete loop — Middle Spirit Stone Block:** phase-history storage; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P0 — make first — Moon Lotus:** night Water-Qi plant; 12×3×12 floating leaves and opening blossom; state textures.
- **P3 — decorative/defer — Painted Cloud Screen:** decorative painted screen; 16×16×2 framed screen; reuse Rice Paper Screen geometry, change panel texture.
- **P3 — decorative/defer — Painted Mountain Screen:** decorative painted screen; 16×16×2 framed screen; reuse Rice Paper Screen geometry, change panel texture.
- **P3 — decorative/defer — Painted River Screen:** decorative painted screen; 16×16×2 framed screen; reuse Rice Paper Screen geometry, change panel texture.
- **P3 — decorative/defer — Pale Jade Block:** jade decorative insulation; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P0 — make first — Placed Talisman:** face-mounted instruction; 10×10×1 face-attached paper decal; no Blockbench.
- **P2 — optional support — Qi Prism:** open-air pulse router; 8×8×8 suspended faceted jade crystal.
- **P3 — decorative/defer — Red Lacquered Planks:** lacquered workshop decoration; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P1 — complete loop — Resonance Stone:** tone-gated Qi release; 10×8×10 rounded standing stone with tone groove.
- **P3 — decorative/defer — Rice Paper Screen:** rice paper screen; 16×16×2 wood lattice and paper; shared screen geometry.
- **P0 — make first — Seal Carving Table:** seal/talisman workstation; 16×9×16 lacquered bench with chisel, paper and seal recess.
- **P3 — decorative/defer — Silk Screen:** silk screen; 16×16×2 wood frame and taut silk; shared screen geometry.
- **P1 — complete loop — Spirit Bamboo:** Qi-grown workshop plant; 6×16×6 segmented stalk/leaves; age textures.
- **P2 — optional support — Spirit Jade Block:** jade decorative insulation; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P3 — decorative/defer — Spirit Spring Water:** spirit spring water; vanilla fluid geometry; still/flow/overlay textures only; no model.
- **P1 — complete loop — Spirit Stone Ore:** Raw Spirit Stone source; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.
- **P3 — decorative/defer — White Jade Block:** jade decorative insulation; 16×16×16 cube; standard JSON model; one static texture; no Blockbench.

### Minimum art set for a first playable build

Finish: Ding Cauldron, Inkstone, Seal-Carving Table, Jade Bi Reservoir, Bamboo Flue, Formation Core, one shared Formation Tile template with four top textures, Formation Ink decals, Placed Talisman, Moon Lotus, Spirit Bamboo, Ginseng, Lingzhi, Drying Rack, Censer, Landscape Stele, Low Spirit Stone Block, Spirit Stone Ore, and one each of Celadon/Jade/Lacquer cubes. That is about **18 unique block models plus flat decals and cube templates**, not nearly eighty projects.

Every deferred decorative entry reuses the cube, stair, slab, wall, pillar, screen or scroll template stated in its line. The reduced progression needs no entity renderer, armour layer, flying-sword model, Heart Demon, projection body or combat visual.
