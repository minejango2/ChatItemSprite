package minej.minejango2.chatitemsprite.renderer;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.renderer.custom.ItemsAdderRenderer;
import minej.minejango2.chatitemsprite.renderer.custom.OraxenRenderer;
import minej.minejango2.chatitemsprite.renderer.custom.NexoRenderer;
import minej.minejango2.chatitemsprite.renderer.custom.CraftEngineRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ItemRenderer {

    private static final Set<Material> MOB_HEADS = EnumSet.of(
            Material.ZOMBIE_HEAD/*,
            Material.CREEPER_HEAD,
            Material.SKELETON_SKULL,
            Material.WITHER_SKELETON_SKULL,
            Material.DRAGON_HEAD,
            Material.PIGLIN_HEAD*/
    );

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
            // not yet - I couldn't find a safe solutions for these heads
            Material.CREEPER_HEAD,
            Material.SKELETON_SKULL,
            Material.WITHER_SKELETON_SKULL,
            Material.DRAGON_HEAD,
            Material.PIGLIN_HEAD
    );

    private static final List<String> UNSUPPORTED_CATEGORIES = List.of(
            "STAIRS",
            "SLAB",
            "WALL",
            "BUTTON",
            "FENCE",
            "PRESSURE_PLATE",
            "COPPER_CHEST"
    );

    private static final Set<Material> NON_UNSUPPORTED = EnumSet.of(
            Material.COPPER_CHESTPLATE
    );

    private final ChatItemSpritePlugin plugin;
    private final MiniMessage miniMessage;
    private final ItemsAdderRenderer itemsAdderRenderer;
    private final OraxenRenderer oraxenRenderer;
    private final NexoRenderer nexoRenderer;
    private final CraftEngineRenderer craftEngineRenderer;

    public ItemRenderer(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.itemsAdderRenderer = new ItemsAdderRenderer();
        this.oraxenRenderer = new OraxenRenderer();
        this.nexoRenderer = new NexoRenderer();
        this.craftEngineRenderer = new CraftEngineRenderer();
    }

    public Component render(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR || item.getAmount() <= 0) {
            return renderEmptyHand();
        }

        return renderItem(item);
    }

    private static final String VANILLA_SENTINEL = "chatitemsprite-this-item-is-vanilla";
    private static final String FALLBACK_DUDE_SENTINEL = "chatitemsprite-custom-item-fallback-dude";

    private static final Set<String> VALID_DISPLAY_MODES = Set.of("both", "sprite-only", "text-only");
    private static final Set<String> VALID_VANILLA_FALLBACKS = Set.of("text", "none");
    private static final Set<String> VALID_CUSTOM_FALLBACKS = Set.of("text", "raw", "none");

    private record SpriteLookupResult(@Nullable String key, boolean isCustomItem, @Nullable Component prebuiltComponent) {
        SpriteLookupResult(@Nullable String key, boolean isCustomItem) {
            this(key, isCustomItem, null);
        }
    }

    private Component renderItem(ItemStack item) {
        String displayMode = plugin.getConfig().getString("item.display-mode", "both").toLowerCase();

        if (!VALID_DISPLAY_MODES.contains(displayMode)) {
            plugin.getLogger().warning("Unknown config item.display-mode '" + displayMode + "', falling back to 'both'.");
            displayMode = "both";
        }

        SpriteLookupResult lookup = resolveSpriteLookup(item);
        Component sprite = lookup.prebuiltComponent() != null ? lookup.prebuiltComponent() : toSpriteComponent(lookup.key());
        String formatMode = resolveFormatMode(displayMode, sprite == null, lookup.isCustomItem());

        String format = plugin.getConfig().getString("text-format." + formatMode, defaultFormatFor(formatMode)
        );

        Component component = miniMessage.deserialize(
                format,
                Placeholder.component("cis_sprite", sprite == null ? Component.empty() : sprite),
                Placeholder.component("cis_name", getNameComponent(item)),
                Placeholder.unparsed("cis_amount", String.valueOf(item.getAmount())), // DEPRECATED
                Placeholder.component("cis_amount_tag", renderAmountTag(item.getAmount(), format))
        );

        return decorateItem(component, item);
    }

    private Component renderAmountTag(int amount, String format) {
        if (!format.contains("<cis_amount_tag>")) {
            return Component.empty();
        }

        boolean hideWhenOne = plugin.getConfig().getBoolean("item.hide-amount-when-one", false);
        if (hideWhenOne && amount == 1) {
            return Component.empty();
        }

        String template = plugin.getConfig().getString("amount-tag-format", "<cis_amount_number>x ");
        String amountStr = String.valueOf(amount);

        return miniMessage.deserialize(
                template,
                Placeholder.unparsed("cis_amount_number", amountStr),
                Placeholder.unparsed("cis_amount", amountStr)
        );
    }

    private String resolveFormatMode(String displayMode, boolean spriteMissing, boolean isCustomItem) {
        if (!spriteMissing || displayMode.equals("text-only")) {
            return displayMode;
        }

        String configKey = isCustomItem ? "item.unsupported-custom-item-fallback" : "item.unsupported-vanilla-item-fallback";
        Set<String> validValues = isCustomItem ? VALID_CUSTOM_FALLBACKS : VALID_VANILLA_FALLBACKS;

        String fallback = plugin.getConfig().getString(configKey, "text").toLowerCase();

        if (!validValues.contains(fallback)) {
            plugin.getLogger().warning("Unknown " + configKey + " '" + fallback + "', defaulting to 'text'. Valid values: " + validValues);
            fallback = "text";
        }

        return switch (fallback) {
            case "text" -> "text-only";
            case "none" -> "fallback-none";
            default -> displayMode;
        };
    }

    private Component decorateItem(Component component, ItemStack item) {
        if (plugin.getConfig().getBoolean("item.show-hover-info", true)) {
            component = component.hoverEvent(item.asHoverEvent());
        }

        return component;
    }

    private String defaultFormatFor(String formatMode) {
        return switch (formatMode) {
            case "sprite-only" -> "<gray>[<reset><cis_sprite><gray>]";
            case "text-only" -> "<gray>[<reset><cis_name><cis_amount_tag><gray>]";
            case "fallback-none" -> "<gray>[<reset>?<gray>]";
            default -> "<gray>[<reset><cis_sprite> <cis_name><cis_amount_tag><gray>]";
        };
    }

    private Component toSpriteComponent(@Nullable String spriteKey) {
        if (spriteKey == null || spriteKey.equals(FALLBACK_DUDE_SENTINEL)) {
            return null;
        }
        return miniMessage.deserialize("<sprite:" + spriteKey + ">");
    }

    private SpriteLookupResult resolveSpriteLookup(ItemStack item) {
        Material material = item.getType();
        String materialName = material.toString().toLowerCase();
        String customFallback = plugin.getConfig().getString("item.unsupported-custom-item-fallback", "text").toLowerCase();

        String customKey = getDefinedSprite(item);
        boolean isCustom = customKey != null && !customKey.equals(VANILLA_SENTINEL);

        // Custom convert
        if (isCustom) {
            if (!customKey.equals(FALLBACK_DUDE_SENTINEL)) {
                return new SpriteLookupResult(customKey, true);
            }

            if (customFallback.equals("raw")) {
                // IA custom fallback
                if (plugin.isPluginEnabledCustom(ChatItemSpritePlugin.CustomItemPlugin.ITEMSADDER)) {
                    String fallbackKey = itemsAdderRenderer.getFallbackKey(item);
                    if (fallbackKey != null && !fallbackKey.equals(VANILLA_SENTINEL)) {
                        return new SpriteLookupResult(fallbackKey, true);
                    }
                }
            } else if (customFallback.equals("none") || customFallback.equals("text")) {
                return new SpriteLookupResult(null, true);
            } else {
                plugin.getLogger().warning("Unknown config item.unsupported-custom-item-fallback '" + customFallback + "', defaulting to 'text'.");
                return new SpriteLookupResult(null, true);
            }
        }

        // V

        // Vanilla convert
        String definedVanillaSprite = plugin.getSpriteManager().getVanillaPath(materialName);
        if (definedVanillaSprite != null) {
            return new SpriteLookupResult(definedVanillaSprite, isCustom);
        }

        // Vanilla fallback
        // Player Head Fallback
        if (!isCustom && material == Material.PLAYER_HEAD) {
            Component headComponent = HeadResolver.resolveHeadComponent(item);
            if (headComponent != null) {
                return new SpriteLookupResult(null, false, headComponent);
            }
        }

        // Mob Head Fallback
        if (!isCustom && MOB_HEADS.contains(material)) {
            Component mobHeadComponent = HeadResolver.resolveMobHeadComponent(item);
            if (mobHeadComponent != null) {
                return new SpriteLookupResult(null, false, mobHeadComponent);
            }
        }


        // remove unnecessary prefix
        Material normalized = normalizeVanillaName(material);

        boolean unsupportedCategories = UNSUPPORTED_CATEGORIES.stream().anyMatch(material.name()::contains);
        if ((UNSUPPORTED_SPRITES.contains(material) || unsupportedCategories) && !NON_UNSUPPORTED.contains(material)) {
            return new SpriteLookupResult(null, isCustom);
        }

        // Block Fallback
        if (normalized.isBlock()) {
            return new SpriteLookupResult(BlockResolver.resolveBlockSprite(normalized), isCustom);
        }

        return new SpriteLookupResult("items:item/" + materialName, isCustom);
    }

    // Custom Item Detector
    @Nullable
    private String getDefinedSprite(ItemStack item) {
        if (plugin.isPluginEnabledCustom(ChatItemSpritePlugin.CustomItemPlugin.ITEMSADDER)) {
            String result = itemsAdderRenderer.getItemsAdderResult(item);

            if (result == null) {
                return "chatitemsprite-this-item-is-vanilla";
            } else {
                String sprite = plugin.getSpriteManager().getCustomPath(ChatItemSpritePlugin.CustomItemPlugin.ITEMSADDER, result);

                if (sprite == null) {
                    return "chatitemsprite-custom-item-fallback-dude";
                } else {
                    return sprite;
                }
            }
        }


        if (plugin.isPluginEnabledCustom(ChatItemSpritePlugin.CustomItemPlugin.ORAXEN)) {
            String result = oraxenRenderer.getOraxenResult(item);

            if (result == null) {
                return "chatitemsprite-this-item-is-vanilla";
            } else {
                String sprite = plugin.getSpriteManager().getCustomPath(ChatItemSpritePlugin.CustomItemPlugin.ORAXEN, result);

                if (sprite == null) {
                    return "chatitemsprite-custom-item-fallback-dude";
                } else {
                    return sprite;
                }
            }
        }

        if (plugin.isPluginEnabledCustom(ChatItemSpritePlugin.CustomItemPlugin.NEXO)) {
            String result = nexoRenderer.getNexoResult(item);

            if (result == null) {
                return "chatitemsprite-this-item-is-vanilla";
            } else {
                String sprite = plugin.getSpriteManager().getCustomPath(ChatItemSpritePlugin.CustomItemPlugin.NEXO, result);

                if (sprite == null) {
                    return "chatitemsprite-custom-item-fallback-dude";
                } else {
                    return sprite;
                }
            }
        }


        if (plugin.isPluginEnabledCustom(ChatItemSpritePlugin.CustomItemPlugin.CRAFTENGINE)) {
            String result = craftEngineRenderer.getCraftEngineResult(item);

            if (result == null) {
                return "chatitemsprite-this-item-is-vanilla";
            } else {
                //plugin.getLogger().info("[CraftEngineRenderer] " + result);
                String sprite = plugin.getSpriteManager().getCustomPath(ChatItemSpritePlugin.CustomItemPlugin.CRAFTENGINE, result);
                if (sprite == null) {
                    return "chatitemsprite-custom-item-fallback-dude";
                } else {
                    return sprite;
                }
            }
        }

        return null;
    }

    // Remove unnecessary prefix like 'WAXED_' etc.
    private Material normalizeVanillaName(Material material) {
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

    // Localization support
    private Component getNameComponent(ItemStack item) {
        Material material = item.getType();

        if (item.hasItemMeta()) {
            // Check item_name component
            if (item.getItemMeta().hasItemName()) {
                return item.getItemMeta().itemName();
            }

            // Check item's custom display name
            if (item.getItemMeta().hasDisplayName()) {
                return item.getItemMeta().displayName();
            }
        }

        // localization support
        if (material.isBlock()) {
            String translationKey = "block.minecraft." + item.getType().toString().toLowerCase();
            return Component.translatable(translationKey);
        } else {
            String translationKey = "item.minecraft." + item.getType().toString().toLowerCase();
            return Component.translatable(translationKey);
        }
    }

    private Component renderEmptyHand() {
        String format = plugin.getConfig().getString("item.empty-hand-format", "[Empty Hand]");
        return miniMessage.deserialize(format);
    }
}