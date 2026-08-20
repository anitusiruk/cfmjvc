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
