package minej.minejango2.chatitemsprite.renderer;

import io.papermc.paper.datacomponent.item.BlockItemDataProperties;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Light;
import org.bukkit.block.data.type.TestBlock;
import org.bukkit.inventory.ItemStack;
import io.papermc.paper.datacomponent.DataComponentTypes;

import java.util.*;

public final class BlockResolver {

    private static final List<String> WOOD_NAMES = List.of(
            "OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK",
            "MANGROVE", "CHERRY", "PALE_OAK",// 26.3 "POPLAR",
            "BAMBOO"
    );

    private static final List<String> NETHER_HYPHAE_NAMES = List.of(
            "CRIMSON",
            "WARPED"
    );

    private static final List<String> COLOR_NAMES = List.of(
            "RED", "ORANGE", "YELLOW", "LIME", "GREEN", "CYAN",
            "LIGHT_BLUE", "BLUE", "PURPLE", "MAGENTA", "PINK",
            "BROWN", "WHITE", "LIGHT_GRAY", "GRAY", "BLACK"
    );

    private static final List<String> COOPER_PREFIXES = List.of(
            "EXPOSED_COPPER",
            "WEATHERED_COPPER",
            "OXIDIZED_COPPER",
            "COPPER"
    );

    private static final Set<Material> FORCE_ITEM = EnumSet.of(
            Material.BAMBOO,
            Material.BARRIER,
            Material.BELL,
            Material.BREWING_STAND,
            Material.CAKE,
            Material.CAMPFIRE,
            Material.CANDLE, // Dyed variation got added in below (addCustom)
            Material.CAULDRON,
            Material.COMPARATOR,
            Material.FLOWER_POT,
            Material.HOPPER,
            Material.IRON_CHAIN,
            Material.IRON_DOOR,
            Material.KELP,
            Material.LANTERN,
            Material.LEAF_LITTER,
            Material.NETHER_WART,
            Material.PINK_PETALS,
            Material.PITCHER_PLANT,
            Material.POINTED_DRIPSTONE,
            Material.REPEATER,
            Material.RESIN_CLUMP,
            Material.SEA_PICKLE,
            Material.SNIFFER_EGG,
            Material.SOUL_CAMPFIRE,
            Material.SOUL_LANTERN,
            Material.STRUCTURE_VOID,
            Material.SUGAR_CANE,
            Material.SULFUR_SPIKE,
            Material.TURTLE_EGG,
            Material.WHEAT,
            Material.WILDFLOWERS
    );

    private static final Set<Material> SIDE_TEXTURES = EnumSet.of(
            Material.ANCIENT_DEBRIS,
            Material.BASALT,
            Material.CACTUS,
            Material.COMPOSTER,
            Material.DIRT_PATH,
            Material.ENCHANTING_TABLE,
            Material.END_PORTAL_FRAME,
            Material.FLOWERING_AZALEA,
            Material.GRASS_BLOCK,
            Material.HAY_BLOCK,
            Material.LODESTONE,
            Material.MANGROVE_ROOTS,
            Material.MUDDY_MANGROVE_ROOTS,
            Material.MYCELIUM,
            Material.PISTON,
            Material.PODZOL,
            Material.POLISHED_BASALT,
            Material.PURPUR_PILLAR,
            Material.QUARTZ_PILLAR,
            Material.REINFORCED_DEEPSLATE,
            Material.SCULK_CATALYST,
            Material.SCULK_SENSOR,
            Material.SCULK_SHRIEKER,
            Material.STONECUTTER,
            Material.TARGET,
            Material.TNT
    );

    private static final Set<Material> TOP_TEXTURES = EnumSet.of(
            Material.ANVIL,
            Material.BARREL,
            Material.BIG_DRIPLEAF,
            Material.BONE_BLOCK,
            Material.CARTOGRAPHY_TABLE,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.DAYLIGHT_DETECTOR,
            Material.GLASS,
            Material.HONEY_BLOCK,
            Material.JIGSAW,
            Material.JUKEBOX,
            Material.LILAC,
            Material.OCHRE_FROGLIGHT,
            Material.PEARLESCENT_FROGLIGHT,
            Material.PEONY,
            Material.ROSE_BUSH,
            Material.SCAFFOLDING,
            Material.SMALL_DRIPLEAF,
            Material.VERDANT_FROGLIGHT
    );

    private static final Set<Material> FRONT_TEXTURES = EnumSet.of(
            Material.BEEHIVE,
            Material.BEE_NEST,
            Material.BLAST_FURNACE,
            Material.CRAFTING_TABLE,
            Material.DISPENSER,
            Material.DROPPER,
            Material.FLETCHING_TABLE,
            Material.FURNACE,
            Material.LOOM,
            Material.OBSERVER,
            Material.SMITHING_TABLE,
            Material.SMOKER,
            Material.SUNFLOWER
    );

    private static final Map<Material, String> CUSTOM_TEXTURES = new EnumMap<>(Material.class);

    static {
        // _plant
        CUSTOM_TEXTURES.put(Material.WEEPING_VINES, "<sprite:blocks:block/weeping_vines_plant>");
        CUSTOM_TEXTURES.put(Material.TWISTING_VINES, "<sprite:blocks:block/twisting_vines_plant>");
        CUSTOM_TEXTURES.put(Material.AZALEA, "<sprite:blocks:block/azalea_plant>");

        // _back
        CUSTOM_TEXTURES.put(Material.COMMAND_BLOCK, "<sprite:blocks:block/command_block_back>");
        CUSTOM_TEXTURES.put(Material.REPEATING_COMMAND_BLOCK, "<sprite:blocks:block/repeating_command_block_back>");
        CUSTOM_TEXTURES.put(Material.CHAIN_COMMAND_BLOCK, "<sprite:blocks:block/chain_command_block_back>");

        // other suffixes
        CUSTOM_TEXTURES.put(Material.CALIBRATED_SCULK_SENSOR, "<sprite:blocks:block/calibrated_sculk_sensor_input_side>");
        CUSTOM_TEXTURES.put(Material.CHISELED_BOOKSHELF, "<sprite:blocks:block/chiseled_bookshelf_empty>");
        CUSTOM_TEXTURES.put(Material.CRAFTER, "<sprite:blocks:block/crafter_north>");
        CUSTOM_TEXTURES.put(Material.DRIED_GHAST, "<sprite:blocks:block/dried_ghast_hydration_0_north>");
        CUSTOM_TEXTURES.put(Material.GRINDSTONE, "<sprite:blocks:block/grindstone_round>");
        CUSTOM_TEXTURES.put(Material.LECTERN, "<sprite:blocks:block/lectern_base>");
        CUSTOM_TEXTURES.put(Material.RESPAWN_ANCHOR, "<sprite:blocks:block/respawn_anchor_side0>");
        CUSTOM_TEXTURES.put(Material.SUSPICIOUS_GRAVEL, "<sprite:blocks:block/suspicious_gravel_0>");
        CUSTOM_TEXTURES.put(Material.SUSPICIOUS_SAND, "<sprite:blocks:block/suspicious_sand_0>");
        CUSTOM_TEXTURES.put(Material.TRIAL_SPAWNER, "<sprite:blocks:block/trial_spawner_side_inactive>");
        CUSTOM_TEXTURES.put(Material.VAULT, "<sprite:blocks:block/vault_front_off>");

        // using different item's sprite
        CUSTOM_TEXTURES.put(Material.GLASS_PANE, "<sprite:blocks:block/glass>");
        CUSTOM_TEXTURES.put(Material.MOSS_CARPET, "<sprite:blocks:block/moss_block>");
        CUSTOM_TEXTURES.put(Material.SNOW_BLOCK, "<sprite:blocks:block/snow>");

        // other things
        CUSTOM_TEXTURES.put(Material.DECORATED_POT, "<sprite:decorated_pot:entity/decorated_pot/decorated_pot_side>");
        CUSTOM_TEXTURES.put(Material.CONDUIT, "<sprite:blocks:entity/conduit/break_particle>");
        CUSTOM_TEXTURES.put(Material.ZOMBIE_HEAD, "<head:entity/zombie/zombie>");

        // weird namings
        CUSTOM_TEXTURES.put(Material.DRIED_KELP_BLOCK, "<sprite:blocks:block/dried_kelp_side>");
        CUSTOM_TEXTURES.put(Material.STICKY_PISTON, "<sprite:blocks:block/piston_top_sticky>");

        // plants, adding color
        CUSTOM_TEXTURES.put(Material.SHORT_GRASS, "<color:#7cbd6b><sprite:blocks:block/short_grass>");
        CUSTOM_TEXTURES.put(Material.TALL_GRASS, "<color:#7cbd6b><sprite:blocks:block/tall_grass_top>");

        CUSTOM_TEXTURES.put(Material.FERN, "<color:#7cbd6b><sprite:blocks:block/fern>");
        CUSTOM_TEXTURES.put(Material.LARGE_FERN, "<color:#7cbd6b><sprite:blocks:block/large_fern_top>");

        CUSTOM_TEXTURES.put(Material.BUSH, "<color:#7cbd6b><sprite:blocks:block/bush>");

        CUSTOM_TEXTURES.put(Material.LILY_PAD, "<color:#71c35c><sprite:blocks:block/lily_pad>");

        CUSTOM_TEXTURES.put(Material.VINE, "<color:#48b518><sprite:blocks:block/vine>");

        CUSTOM_TEXTURES.put(Material.OAK_LEAVES, "<color:#48b518><sprite:blocks:block/oak_leaves>");
        CUSTOM_TEXTURES.put(Material.JUNGLE_LEAVES, "<color:#48b518><sprite:blocks:block/jungle_leaves>");
        CUSTOM_TEXTURES.put(Material.ACACIA_LEAVES, "<color:#48b518><sprite:blocks:block/acacia_leaves>");
        CUSTOM_TEXTURES.put(Material.DARK_OAK_LEAVES, "<color:#48b518><sprite:blocks:block/dark_oak_leaves>");

        CUSTOM_TEXTURES.put(Material.MANGROVE_LEAVES, "<color:#92c648><sprite:blocks:block/mangrove_leaves>");
        CUSTOM_TEXTURES.put(Material.SPRUCE_LEAVES, "<color:#619961><sprite:blocks:block/spruce_leaves>");
        CUSTOM_TEXTURES.put(Material.BIRCH_LEAVES, "<color:#80a755><sprite:blocks:block/birch_leaves>");
    }

    private static void add(Set<Material> set, String name) {
        try {
            set.add(Material.valueOf(name));
        } catch (IllegalArgumentException ignored) {}
    }

    private static void addCustom(String materialName, String textureName) {
        if (materialName == null || textureName == null || textureName.isEmpty()) {
            return;
        }

        try {
            CUSTOM_TEXTURES.put(Material.valueOf(materialName), textureName.toLowerCase());
        } catch (IllegalArgumentException ignored) {}
    }

    // for woods
    static {
        for (String name : WOOD_NAMES) {
            try {
                if (!name.equals("BAMBOO")) {
                    // LOG
                    add(TOP_TEXTURES, name + "_LOG");
                    add(TOP_TEXTURES, "STRIPPED_" + name + "_LOG");
                    // WOOD
                    addCustom(name + "_WOOD", "blocks:block/" + name.toLowerCase() + "_log");
                    addCustom("STRIPPED_" + name + "_WOOD", "blocks:block/stripped_" + name.toLowerCase() + "_log");
                }
                // DOOR
                add(FORCE_ITEM, name + "_DOOR");
                // SIGN
                add(FORCE_ITEM, name + "_SIGN");
                add(FORCE_ITEM, name + "_HANGING_SIGN");
            } catch (IllegalArgumentException ignored) {}
        }

        for (String name : NETHER_HYPHAE_NAMES) {
            try {
                // HYPHAE
                add(TOP_TEXTURES, name + "_STEM");
                add(TOP_TEXTURES, "STRIPPED_" + name + "_STEM");
                // DOOR
                add(FORCE_ITEM, name + "_DOOR");
                // WOOD
                addCustom(name + "_HYPHAE", "blocks:block/" + name.toLowerCase() + "_stem");
                addCustom("STRIPPED_" + name + "_HYPHAE", "blocks:block/stripped_" + name.toLowerCase() + "_stem");
                // SIGN
                add(FORCE_ITEM, name + "_SIGN");
                add(FORCE_ITEM, name + "_HANGING_SIGN");
                // NYLIUM
                add(SIDE_TEXTURES, name + "_NYLIUM");
            } catch (IllegalArgumentException ignored) {}
        }

        for (String name : COLOR_NAMES) {
            add(FORCE_ITEM, name + "_CANDLE");
            // 26.3 add(FORCE_ITEM, name + "_CUSHION");
            addCustom(name + "_CARPET", "blocks:block/" + name.toLowerCase() + "_wool");
            addCustom(name + "_STAINED_GLASS_PANE", "blocks:block/" + name.toLowerCase() + "_glass");
            addCustom(name + "_BED", "blocks:block/" + name.toLowerCase() + "_bed_head_up");
        }

        for (String name : COOPER_PREFIXES) {
            add(FORCE_ITEM, name + "_CHAIN");
            add(FORCE_ITEM, name + "_DOOR");
            add(FORCE_ITEM, name + "_LANTERN");
        }
    }

    public static String resolveBlockSprite(ItemStack item) {
        Material originalMaterial = item.getType();

        if (originalMaterial == Material.TEST_BLOCK) {
            BlockItemDataProperties properties = item.getData(DataComponentTypes.BLOCK_DATA);

            if (properties != null) {
                BlockData data = properties.applyTo(originalMaterial.createBlockData());

                if (data instanceof TestBlock testBlock) {
                    return "<sprite:blocks:block/test_block_" + testBlock.getMode().name().toLowerCase() + ">";
                }
            }
            return "<sprite:blocks:block/test_block_start>";
        }

        if (originalMaterial == Material.LIGHT) {
            BlockItemDataProperties properties = item.getData(DataComponentTypes.BLOCK_DATA);

            if (properties != null) {
                BlockData data = properties.applyTo(originalMaterial.createBlockData());

                if (data instanceof Light light) {
                    return "<sprite:items:item/light_%02d>".formatted(light.getLevel());
                }
            }

            return "<sprite:items:item/light_15>";
        }

        Material material = normalizeVanillaName(originalMaterial);

        if (FORCE_ITEM.contains(material)) {
            return "<sprite:items:item/" + material.name().toLowerCase() + ">";
        }

        String custom = CUSTOM_TEXTURES.get(material);
        if (custom != null) {
            return custom;
        }

        String name = material.name().toLowerCase();

        if (SIDE_TEXTURES.contains(material)) {
            return "<sprite:blocks:block/" + name + "_side>";
        }

        if (TOP_TEXTURES.contains(material)) {
            return "<sprite:blocks:block/" + name + "_top>";
        }

        if (FRONT_TEXTURES.contains(material)) {
            return "<sprite:blocks:block/" + name + "_front>";
        }

        return "<sprite:blocks:block/" + name + ">";
    }

    private static Material normalizeVanillaName(Material material) {
        String name = material.name();


        if (name.startsWith("WAXED_")) {
            name = name.substring(6);
        }

        if (name.startsWith("INFESTED_")) {
            name = name.substring(9);
        }

        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            return material;
        }
    }
}
