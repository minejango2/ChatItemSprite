package minej.minejango2.chatitemsprite.renderer;

import org.bukkit.Material;

import java.util.*;

public final class BlockResolver {

    private static final List<String> WOOD_NAMES = List.of(
            "OAK",
            "SPRUCE",
            "BIRCH",
            "JUNGLE",
            "ACACIA",
            "DARK_OAK",
            "MANGROVE",
            "CHERRY",
            "PALE_OAK"
    );

    private static final List<String> NETHER_HYPHAE_NAMES = List.of(
            "CRIMSON",
            "WARPED"
    );

    private static final List<String> COLOR_NAMES = List.of(
            "RED",
            "ORANGE",
            "YELLOW",
            "LIME",
            "GREEN",
            "CYAN",
            "LIGHT_BLUE",
            "BLUE",
            "PURPLE",
            "MAGENTA",
            "PINK",
            "BROWN",
            "WHITE",
            "LIGHT_GRAY",
            "GRAY",
            "BLACK"
    );

    private static final List<String> COOPER_PREFIXES = List.of(
            "EXPOSED_COPPER",
            "WEATHERED_COPPER",
            "OXIDIZED_COPPER",
            "COPPER"
    );

    private static final Set<Material> FORCE_ITEM = EnumSet.of(
            Material.IRON_DOOR,
            Material.WHEAT,
            Material.NETHER_WART,
            Material.BAMBOO,
            Material.SUGAR_CANE,
            Material.KELP,
            Material.CANDLE, // Dyed variation got added in below (addCustom)
            Material.BREWING_STAND,
            Material.PITCHER_PLANT,
            Material.PINK_PETALS,
            Material.CAKE,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.LEAF_LITTER,
            Material.POINTED_DRIPSTONE,
            Material.SULFUR_SPIKE,
            Material.CAULDRON,
            Material.HOPPER,
            Material.SEA_PICKLE,
            Material.IRON_CHAIN,
            Material.FLOWER_POT,
            Material.BELL
    );

    private static final Set<Material> SIDE_TEXTURES = EnumSet.of(
            Material.GRASS_BLOCK,
            Material.MYCELIUM,
            Material.PODZOL,
            Material.CRIMSON_NYLIUM,
            Material.WARPED_NYLIUM,
            Material.PISTON,
            Material.STONECUTTER,
            Material.ENCHANTING_TABLE,
            Material.COMPOSTER,
            Material.END_PORTAL_FRAME,
            Material.SCULK_SENSOR,
            Material.SCULK_SHRIEKER
    );

    private static final Set<Material> TOP_TEXTURES = EnumSet.of(
            Material.STICKY_PISTON,
            Material.GLASS, // Dyed variation got added in below (addCustom)
            Material.SCAFFOLDING,
            Material.CARTOGRAPHY_TABLE,
            Material.BIG_DRIPLEAF,
            Material.SMALL_DRIPLEAF,
            Material.TALL_GRASS,
            Material.LARGE_FERN,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,
            Material.JUKEBOX,
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.BARREL,
            Material.HONEY_BLOCK
    );

    private static final Set<Material> FRONT_TEXTURES = EnumSet.of(
            Material.CRAFTING_TABLE,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.OBSERVER,
            Material.FLETCHING_TABLE,
            Material.SMITHING_TABLE,
            Material.LOOM,
            Material.SUNFLOWER,
            Material.BEE_NEST,
            Material.BEEHIVE
    );

    private static final Map<Material, String> CUSTOM_TEXTURES = new EnumMap<>(Material.class);

    static {
        CUSTOM_TEXTURES.put(Material.GRINDSTONE, "blocks:block/grindstone_round");
        CUSTOM_TEXTURES.put(Material.WEEPING_VINES, "blocks:block/weeping_vines_plant");
        CUSTOM_TEXTURES.put(Material.TWISTING_VINES, "blocks:block/twisting_vines_plant");
        CUSTOM_TEXTURES.put(Material.CONDUIT, "blocks:entity/conduit/break_particle");
        CUSTOM_TEXTURES.put(Material.CHISELED_BOOKSHELF, "blocks:block/chiseled_bookshelf_empty");
        CUSTOM_TEXTURES.put(Material.LECTERN, "blocks:block/lectern_base");
        CUSTOM_TEXTURES.put(Material.VAULT, "blocks:block/vault_front_off");
        CUSTOM_TEXTURES.put(Material.TRIAL_SPAWNER, "blocks:block/trial_spawner_side_inactive");
        CUSTOM_TEXTURES.put(Material.RESPAWN_ANCHOR, "blocks:block/respawn_anchor_side0");
        CUSTOM_TEXTURES.put(Material.CRAFTER, "blocks:block/crafter_north");
        CUSTOM_TEXTURES.put(Material.DECORATED_POT, "decorated_pot:entity/decorated_pot/decorated_pot_side");
        CUSTOM_TEXTURES.put(Material.CALIBRATED_SCULK_SENSOR, "blocks:calibrated_sculk_sensor_input_side");
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
                // LOG
                add(TOP_TEXTURES, name + "_LOG");
                add(TOP_TEXTURES, "STRIPPED_" + name + "_LOG");
                // DOOR
                add(FORCE_ITEM, name + "_DOOR");
                // WOOD
                addCustom(name + "_WOOD", "blocks:block/" + name.toLowerCase() + "_log");
                addCustom("STRIPPED_" + name + "_WOOD", "blocks:block/stripped_" + name.toLowerCase() + "_log");
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
            } catch (IllegalArgumentException ignored) {}
        }

        for (String name : COLOR_NAMES) {
            add(FORCE_ITEM, name + "_CANDLE");
            addCustom(name + "_CANDLE", "blocks:block/" + name.toLowerCase() + "_wool");
            addCustom(name + "_GLASS_PANE", "blocks:block/" + name.toLowerCase() + "_glass");
            addCustom(name + "_BED", "blocks:block/" + name.toLowerCase() + "_head_up");
        }

        for (String name : COOPER_PREFIXES) {
            add(FORCE_ITEM, name + "_CHAIN");
            add(FORCE_ITEM, name + "_DOOR");
            add(FORCE_ITEM, name + "_LANTERN");
        }
    }

    public static String resolveBlockSprite(Material material) {
        if (FORCE_ITEM.contains(material)) {
            return "items:item/" + material.name().toLowerCase();
        }

        String custom = CUSTOM_TEXTURES.get(material);
        if (custom != null) {
            return custom;
        }

        String name = material.name().toLowerCase();

        if (SIDE_TEXTURES.contains(material)) {
            return "blocks:block/" + name + "_side";
        }

        if (TOP_TEXTURES.contains(material)) {
            return "blocks:block/" + name + "_top";
        }

        if (FRONT_TEXTURES.contains(material)) {
            return "blocks:block/" + name + "_front";
        }

        return "blocks:block/" + name;
    }
}
