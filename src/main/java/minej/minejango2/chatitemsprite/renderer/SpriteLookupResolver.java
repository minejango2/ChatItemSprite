package minej.minejango2.chatitemsprite.renderer;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.minimessage.MiniMessageProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SpriteLookupResolver {

    public record Result(@Nullable Component sprite, boolean isCustomItem) {}

    private static final Set<Material> UNSUPPORTED_SPRITES = EnumSet.of(
            Material.SHIELD,
            Material.POTION,
            Material.SPLASH_POTION,
            Material.LINGERING_POTION,
            Material.TIPPED_ARROW,
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ENDER_CHEST,
            Material.RED_BANNER,
            Material.ORANGE_BANNER,
            Material.YELLOW_BANNER,
            Material.LIME_BANNER,
            Material.GREEN_BANNER,
            Material.CYAN_BANNER,
            Material.LIGHT_BLUE_BANNER,
            Material.BLUE_BANNER,
            Material.PURPLE_BANNER,
            Material.MAGENTA_BANNER,
            Material.PINK_BANNER,
            Material.BROWN_BANNER,
            Material.WHITE_BANNER,
            Material.LIGHT_GRAY_BANNER,
            Material.GRAY_BANNER,
            Material.BLACK_BANNER,
            // I couldn't find a safe solutions for these heads (I won't use base64 for internal solutions)
            Material.CREEPER_HEAD,
            Material.SKELETON_SKULL,
            Material.WITHER_SKELETON_SKULL,
            Material.DRAGON_HEAD,
            Material.PIGLIN_HEAD
    );

    private static final List<String> UNSUPPORTED_CATEGORIES = List.of(
            "STAIRS", "SLAB", "WALL", "BUTTON", "FENCE", "PRESSURE_PLATE", "COPPER_CHEST", "GOLEM_STATUE"
    );

    private static final Set<Material> NON_UNSUPPORTED = EnumSet.of(Material.COPPER_CHESTPLATE);

    private static final Map<Material, String> BUILTIN_SPRITE = new EnumMap<>(Material.class);
    static {
        BUILTIN_SPRITE.put(Material.DEBUG_STICK, "<sprite:items:item/stick>");
        BUILTIN_SPRITE.put(Material.COMPASS, "<sprite:items:item/compass_16>");
        BUILTIN_SPRITE.put(Material.CLOCK, "<sprite:items:item/clock_00>");
        BUILTIN_SPRITE.put(Material.LEATHER_HELMET, "<color:#A06540><sprite:items:item/leather_helmet>");
        BUILTIN_SPRITE.put(Material.LEATHER_CHESTPLATE, "<color:#A06540><sprite:items:item/leather_chestplate>");
        BUILTIN_SPRITE.put(Material.LEATHER_LEGGINGS, "<color:#A06540><sprite:items:item/leather_leggings>");
        BUILTIN_SPRITE.put(Material.LEATHER_BOOTS, "<color:#A06540><sprite:items:item/leather_boots>");
        BUILTIN_SPRITE.put(Material.LEATHER_HORSE_ARMOR, "<color:#A06540><sprite:items:item/leather_horse_armor>");
    }

    private final ChatItemSpritePlugin plugin;
    private final CustomItemIdentifier customItemIdentifier;

    public SpriteLookupResolver(ChatItemSpritePlugin plugin, CustomItemIdentifier customItemIdentifier) {
        this.plugin = plugin;
        this.customItemIdentifier = customItemIdentifier;
    }

    public Result resolve(ItemStack item) {
        Material material = item.getType();
        String materialName = material.toString().toLowerCase();

        CustomItemIdentifier.Identification id = customItemIdentifier.identify(item);
        boolean isCustom = id != null;

        if (isCustom) {
            String definedSprite = plugin.getSpriteManager().getCustomPath(id.plugin(), id.rawId());
            if (definedSprite != null) {
                return new Result(toComponent(definedSprite), true);
            }

            if (plugin.isPluginEnabledCustom(ChatItemSpritePlugin.CustomItemPlugin.ITEMSADDER)) {
                String autoTexture = customItemIdentifier.resolveItemsAdderRawTexture(item);
                if (autoTexture != null) {
                    return new Result(toComponent("<sprite:" + autoTexture + ">"), true);
                }
            }

            String customFallback = plugin.getConfig().getString("item.unsupported-custom-item-fallback", "text").toLowerCase();

            if (!customFallback.equals("raw")) {
                return new Result(null, true);
            }
        }

        // Vanilla convert
        String definedVanillaSprite = plugin.getSpriteManager().getVanillaPath(materialName);
        if (definedVanillaSprite != null) {
            return new Result(toComponent(definedVanillaSprite), isCustom);
        }

        // Player head auto texture
        if (!isCustom && material == Material.PLAYER_HEAD
                && plugin.getConfig().getBoolean("item.render-player-head-texture", true)) {
            Component headComponent = HeadResolver.resolveHeadComponent(item);
            if (headComponent != null) {
                return new Result(headComponent, false);
            }
        }

        boolean unsupportedCategories = UNSUPPORTED_CATEGORIES.stream().anyMatch(material.name()::contains);
        if ((UNSUPPORTED_SPRITES.contains(material) || unsupportedCategories) && !NON_UNSUPPORTED.contains(material)) {
            return new Result(null, isCustom);
        }

        String customDefined = BUILTIN_SPRITE.get(material);
        if (customDefined != null) {
            return new Result(toComponent(customDefined), isCustom);
        }

        if (material.isBlock()) {
            String blockSprite = BlockResolver.resolveBlockSprite(item);
            return new Result(toComponent(blockSprite), isCustom);
        }

        return new Result(toComponent("<sprite:items:item/" + materialName + ">"), isCustom);
    }

    private Component toComponent(String tag) {
        return MiniMessageProvider.miniMessage.deserialize(tag);
    }
}
