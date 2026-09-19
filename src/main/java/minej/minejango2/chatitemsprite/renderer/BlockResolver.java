package minej.minejango2.chatitemsprite.renderer;

import io.papermc.paper.datacomponent.item.BlockItemDataProperties;
import minej.minejango2.chatitemsprite.util.ServerVersion;
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
            "MANGROVE", "CHERRY", "PALE_OAK", "POPLAR",
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

    private static final Set<Material> FORCE_ITEM = EnumSet.noneOf(Material.class);
    private static final Set<Material> SIDE_TEXTURES = EnumSet.noneOf(Material.class);
    private static final Set<Material> TOP_TEXTURES = EnumSet.noneOf(Material.class);
    private static final Set<Material> FRONT_TEXTURES = EnumSet.noneOf(Material.class);

    private static final Map<Material, String> CUSTOM_TEXTURES = new EnumMap<>(Material.class);

    static {
        add(FORCE_ITEM, "BAMBOO");
        add(FORCE_ITEM, "BARRIER");
        add(FORCE_ITEM, "BELL");
        add(FORCE_ITEM, "BREWING_STAND");
        add(FORCE_ITEM, "CAKE");
        add(FORCE_ITEM, "CAMPFIRE");
        add(FORCE_ITEM, "CANDLE"); // Dyed variation got added in below (addCustom)
        add(FORCE_ITEM, "CAULDRON");
        add(FORCE_ITEM, "COMPARATOR");
        add(FORCE_ITEM, "FLOWER_POT");
        add(FORCE_ITEM, "HOPPER");
        add(FORCE_ITEM, "IRON_CHAIN");
        add(FORCE_ITEM, "IRON_DOOR");
        add(FORCE_ITEM, "KELP");
        add(FORCE_ITEM, "LANTERN");
        add(FORCE_ITEM, "LEAF_LITTER");
        add(FORCE_ITEM, "NETHER_WART");
        add(FORCE_ITEM, "PINK_PETALS");
        add(FORCE_ITEM, "PITCHER_PLANT");
        add(FORCE_ITEM, "POINTED_DRIPSTONE");
        add(FORCE_ITEM, "REPEATER");
        add(FORCE_ITEM, "RESIN_CLUMP");
        add(FORCE_ITEM, "SEA_PICKLE");
        add(FORCE_ITEM, "SNIFFER_EGG");
        add(FORCE_ITEM, "SOUL_CAMPFIRE");
        add(FORCE_ITEM, "SOUL_LANTERN");
        add(FORCE_ITEM, "STRUCTURE_VOID");
        add(FORCE_ITEM, "SUGAR_CANE");
        add(FORCE_ITEM, "SULFUR_SPIKE");
        add(FORCE_ITEM, "TURTLE_EGG");
        add(FORCE_ITEM, "WHEAT");
        add(FORCE_ITEM, "WILDFLOWERS");

        add(SIDE_TEXTURES, "ANCIENT_DEBRIS");
        add(SIDE_TEXTURES, "BASALT");
        add(SIDE_TEXTURES, "CACTUS");
        add(SIDE_TEXTURES, "COMPOSTER");
        add(SIDE_TEXTURES, "DIRT_PATH");
        add(SIDE_TEXTURES, "ENCHANTING_TABLE");
        add(SIDE_TEXTURES, "END_PORTAL_FRAME");
        add(SIDE_TEXTURES, "FLOWERING_AZALEA");
        add(SIDE_TEXTURES, "GRASS_BLOCK");
        add(SIDE_TEXTURES, "HAY_BLOCK");
        add(SIDE_TEXTURES, "LODESTONE");
        add(SIDE_TEXTURES, "MANGROVE_ROOTS");
        add(SIDE_TEXTURES, "MUDDY_MANGROVE_ROOTS");
        add(SIDE_TEXTURES, "MYCELIUM");
        add(SIDE_TEXTURES, "PISTON");
        add(SIDE_TEXTURES, "PODZOL");
        add(SIDE_TEXTURES, "POLISHED_BASALT");
        add(SIDE_TEXTURES, "PURPUR_PILLAR");
        add(SIDE_TEXTURES, "QUARTZ_PILLAR");
        add(SIDE_TEXTURES, "REINFORCED_DEEPSLATE");
        add(SIDE_TEXTURES, "SCULK_CATALYST");
        add(SIDE_TEXTURES, "SCULK_SENSOR");
        add(SIDE_TEXTURES, "SCULK_SHRIEKER");
        add(SIDE_TEXTURES, "STONECUTTER");
        add(SIDE_TEXTURES, "TARGET");
        add(SIDE_TEXTURES, "TNT");

        add(TOP_TEXTURES, "ANVIL");
        add(TOP_TEXTURES, "BARREL");
        add(TOP_TEXTURES, "BIG_DRIPLEAF");
        add(TOP_TEXTURES, "BONE_BLOCK");
        add(TOP_TEXTURES, "CARTOGRAPHY_TABLE");
        add(TOP_TEXTURES, "CHIPPED_ANVIL");
        add(TOP_TEXTURES, "DAMAGED_ANVIL");
        add(TOP_TEXTURES, "DAYLIGHT_DETECTOR");
        add(TOP_TEXTURES, "GLASS");
        add(TOP_TEXTURES, "HONEY_BLOCK");
        add(TOP_TEXTURES, "JIGSAW");
        add(TOP_TEXTURES, "JUKEBOX");
        add(TOP_TEXTURES, "LILAC");
        add(TOP_TEXTURES, "OCHRE_FROGLIGHT");
        add(TOP_TEXTURES, "PEARLESCENT_FROGLIGHT");
        add(TOP_TEXTURES, "PEONY");
        add(TOP_TEXTURES, "ROSE_BUSH");
        add(TOP_TEXTURES, "SCAFFOLDING");
        add(TOP_TEXTURES, "SMALL_DRIPLEAF");
        add(TOP_TEXTURES, "VERDANT_FROGLIGHT");

        add(FRONT_TEXTURES, "BEEHIVE");
        add(FRONT_TEXTURES, "BEE_NEST");
        add(FRONT_TEXTURES, "BLAST_FURNACE");
        add(FRONT_TEXTURES, "CRAFTING_TABLE");
        add(FRONT_TEXTURES, "DISPENSER");
        add(FRONT_TEXTURES, "DROPPER");
        add(FRONT_TEXTURES, "FLETCHING_TABLE");
        add(FRONT_TEXTURES, "FURNACE");
        add(FRONT_TEXTURES, "LOOM");
        add(FRONT_TEXTURES, "OBSERVER");
        add(FRONT_TEXTURES, "SMITHING_TABLE");
        add(FRONT_TEXTURES, "SMOKER");
        add(FRONT_TEXTURES, "SUNFLOWER");

        // _plant
        addCustom("WEEPING_VINES", "<sprite:blocks:block/weeping_vines_plant>");
        addCustom("TWISTING_VINES", "<sprite:blocks:block/twisting_vines_plant>");
        addCustom("AZALEA", "<sprite:blocks:block/azalea_plant>");

        // _back
        addCustom("COMMAND_BLOCK", "<sprite:blocks:block/command_block_back>");
        addCustom("REPEATING_COMMAND_BLOCK", "<sprite:blocks:block/repeating_command_block_back>");
        addCustom("CHAIN_COMMAND_BLOCK", "<sprite:blocks:block/chain_command_block_back>");

        // other suffixes
        addCustom("CALIBRATED_SCULK_SENSOR", "<sprite:blocks:block/calibrated_sculk_sensor_input_side>");
        addCustom("CHISELED_BOOKSHELF", "<sprite:blocks:block/chiseled_bookshelf_empty>");
        addCustom("CRAFTER", "<sprite:blocks:block/crafter_north>");
        addCustom("DRIED_GHAST", "<sprite:blocks:block/dried_ghast_hydration_0_north>");
        addCustom("GRINDSTONE", "<sprite:blocks:block/grindstone_round>");
        addCustom("LECTERN", "<sprite:blocks:block/lectern_base>");
        addCustom("RESPAWN_ANCHOR", "<sprite:blocks:block/respawn_anchor_side0>");
        addCustom("SUSPICIOUS_GRAVEL", "<sprite:blocks:block/suspicious_gravel_0>");
        addCustom("SUSPICIOUS_SAND", "<sprite:blocks:block/suspicious_sand_0>");
        addCustom("TRIAL_SPAWNER", "<sprite:blocks:block/trial_spawner_side_inactive>");
        addCustom("VAULT", "<sprite:blocks:block/vault_front_off>");

        // using different item's sprite
        addCustom("GLASS_PANE", "<sprite:blocks:block/glass>");
        addCustom("MOSS_CARPET", "<sprite:blocks:block/moss_block>");
        addCustom("SNOW_BLOCK", "<sprite:blocks:block/snow>");

        // other things
        addCustom("DECORATED_POT", "<sprite:decorated_pot:entity/decorated_pot/decorated_pot_side>");
        addCustom("CONDUIT", "<sprite:blocks:entity/conduit/break_particle>");
        addCustom("ZOMBIE_HEAD", "<head:entity/zombie/zombie>");

        // weird namings
        addCustom("DRIED_KELP_BLOCK", "<sprite:blocks:block/dried_kelp_side>");
        addCustom("STICKY_PISTON", "<sprite:blocks:block/piston_top_sticky>");

        // plants, adding color
        addCustom("SHORT_GRASS", "<color:#7cbd6b><sprite:blocks:block/short_grass>");
        addCustom("TALL_GRASS", "<color:#7cbd6b><sprite:blocks:block/tall_grass_top>");

        addCustom("FERN", "<color:#7cbd6b><sprite:blocks:block/fern>");
        addCustom("LARGE_FERN", "<color:#7cbd6b><sprite:blocks:block/large_fern_top>");

        addCustom("BUSH", "<color:#7cbd6b><sprite:blocks:block/bush>");

        addCustom("LILY_PAD", "<color:#71c35c><sprite:blocks:block/lily_pad>");

        addCustom("VINE", "<color:#48b518><sprite:blocks:block/vine>");

        addCustom("OAK_LEAVES", "<color:#48b518><sprite:blocks:block/oak_leaves>");
        addCustom("JUNGLE_LEAVES", "<color:#48b518><sprite:blocks:block/jungle_leaves>");
        addCustom("ACACIA_LEAVES", "<color:#48b518><sprite:blocks:block/acacia_leaves>");
        addCustom("DARK_OAK_LEAVES", "<color:#48b518><sprite:blocks:block/dark_oak_leaves>");

        addCustom("MANGROVE_LEAVES", "<color:#92c648><sprite:blocks:block/mangrove_leaves>");
        addCustom("SPRUCE_LEAVES", "<color:#619961><sprite:blocks:block/spruce_leaves>");
        addCustom("BIRCH_LEAVES", "<color:#80a755><sprite:blocks:block/birch_leaves>");
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
                    addCustom(name + "_WOOD", "<sprite:blocks:block/" + name.toLowerCase() + "_log>");
                    addCustom("STRIPPED_" + name + "_WOOD", "<sprite:blocks:block/stripped_" + name.toLowerCase() + "_log>");
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
                addCustom(name + "_HYPHAE", "<sprite:blocks:block/" + name.toLowerCase() + "_stem>");
                addCustom("STRIPPED_" + name + "_HYPHAE", "<sprite:blocks:block/stripped_" + name.toLowerCase() + "_stem>");
                // SIGN
                add(FORCE_ITEM, name + "_SIGN");
                add(FORCE_ITEM, name + "_HANGING_SIGN");
                // NYLIUM
                add(SIDE_TEXTURES, name + "_NYLIUM");
            } catch (IllegalArgumentException ignored) {}
        }

        for (String name : COLOR_NAMES) {
            add(FORCE_ITEM, name + "_CANDLE");
            add(FORCE_ITEM, name + "_CUSHION");
            addCustom(name + "_CARPET", "<sprite:blocks:block/" + name.toLowerCase() + "_wool>");
            addCustom(name + "_STAINED_GLASS_PANE", "<sprite:blocks:block/" + name.toLowerCase() + "_glass>");

            String lower = name.toLowerCase();
            String bedSprite = ServerVersion.bedsAreRegularBlocks() ? "<sprite:blocks:block/" + lower + "_bed_head_up>" : "<head:entity/bed/" + lower + ">";
            addCustom(name + "_BED", bedSprite);
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
