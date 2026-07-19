package minej.minejango2.chatitemsprite.renderer;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.renderer.custom.ItemsAdderRenderer;
import minej.minejango2.chatitemsprite.renderer.custom.OraxenRenderer;
import minej.minejango2.chatitemsprite.renderer.custom.NexoRenderer;
import minej.minejango2.chatitemsprite.renderer.custom.CraftEngineRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ItemRenderer {

    private static final Set<Material> UNSUPPORTED_SPRITES = EnumSet.of(
            Material.PLAYER_HEAD,
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
            Material.BLACK_BANNER
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

    private Component renderItem(ItemStack item) {
        Component itemComponent;

        String displayMode = plugin.getConfig()
                .getString("item.display-mode", "both")
                .toLowerCase();

        itemComponent = switch (displayMode) {
            case "sprite-only" -> renderSprite(item);
            case "text-only" -> renderText(item);
            default -> renderBoth(item);
        };

        return decorateItem(itemComponent, item);
    }

    private Component decorateItem(Component component, ItemStack item) {
        // Add hover info if enabled
        if (plugin.getConfig().getBoolean("item.show-hover-info", true)) {
            component = component.hoverEvent(item.asHoverEvent());
        }

        // Wrap in brackets if enabled
        if (plugin.getConfig().getBoolean("item.wrap-in-brackets", true)) {
            component = Component.text("[")
                    .append(component)
                    .append(Component.text("]"));
        }

        return component;
    }

    private enum SpriteMode {
        SPRITE,
        TEXT,
        EMPTY
    }

    private record SpriteResult(Component component, SpriteMode mode) {}

    private SpriteResult resolveSprite(ItemStack item) {
        String spriteKey = getSpriteKey(item);

        if (Objects.equals(spriteKey, "chatitemsprite-custom-item-fallback-dude")) {
            String fallback = plugin.getConfig()
                    .getString("item.unsupported-custom-item-fallback", "text")
                    .toLowerCase();

            return switch (fallback) {
                case "none" -> new SpriteResult(Component.empty(), SpriteMode.EMPTY);
                // case "text" -> new SpriteResult(renderText(item), SpriteMode.TEXT);
                default -> new SpriteResult(renderText(item), SpriteMode.TEXT);
            };
        }

        if (spriteKey == null) {
            String fallback = plugin.getConfig()
                    .getString("item.unsupported-vanilla-item-fallback", "text")
                    .toLowerCase();

            return switch (fallback) {
                case "none" -> new SpriteResult(Component.empty(), SpriteMode.EMPTY);
                // case "text" -> new SpriteResult(renderText(item), SpriteMode.TEXT);
                default -> new SpriteResult(renderText(item), SpriteMode.TEXT);
            };
        }

        Component sprite = miniMessage.deserialize("<sprite:" + spriteKey + ">");
        return new SpriteResult(sprite, SpriteMode.SPRITE);
    }

    private Component renderSprite(ItemStack item) {
        return resolveSprite(item).component();
    }

    private Component renderBoth(ItemStack item) {
        SpriteResult result = resolveSprite(item);

        if (result.mode() != SpriteMode.SPRITE) {
            return result.component();
        }

        return result.component()
                .append(Component.text(" "))
                .append(renderText(item));
    }

    private Component renderText(ItemStack item) {
        String textFormat = plugin.getConfig().getString("item.text-format", "{name} x{amount}");
        String formatted = textFormat.replace("{amount}", String.valueOf(item.getAmount()));
        Component result = Component.empty();
        String[] nameParts = formatted.split("\\{name\\}", -1);

        for (int i = 0; i < nameParts.length; i++) {
            if (!nameParts[i].isEmpty()) {
                result = result.append(Component.text(nameParts[i]));
            }
            if (i < nameParts.length - 1) {
                result = result.append(getTranslatableItemName(item));
            }
        }

        return result;
    }

    private Material normalizeVanillaName(Material material) {
        String name = material.name();

        // Remove 'WAXED_', so waxed blocks made of COPPER could get a sprite from their unwaxed form
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

    private String getSpriteKey(ItemStack item) {
        Material material = item.getType();
        String materialName = material.toString().toLowerCase();
        String customFallback = plugin.getConfig().getString("item.unsupported-custom-item-fallback", "text");

        String customKey = getDefinedSprite(item);

        // Custom item convert + fallback
        if (customKey != null && !customKey.equals("chatitemsprite-this-item-is-vanilla") && !customFallback.equals("raw")) {
            // plugin.getLogger().info("get back " + customKey);
            return customKey;
        }

        // ItemsAdder try fallback
        if (plugin.isPluginEnabledCustom(ChatItemSpritePlugin.CustomItemPlugin.ITEMSADDER)) {
            String fallbackKey = itemsAdderRenderer.getFallbackKey(item);
            if (fallbackKey != null && !Objects.equals(fallbackKey, "chatitemsprite-this-item-is-vanilla")) {
                return fallbackKey;
            } else if (customFallback.equals("text")){
                return null;
            }
        }

        // Vanilla convert
        String definedVanillaSprite = plugin.getSpriteManager().getVanillaPath(materialName);
        if (definedVanillaSprite != null) {
            return definedVanillaSprite;
        }

        // Vanilla fallback
        material = normalizeVanillaName(material);

        boolean unsupportedCategories = UNSUPPORTED_CATEGORIES.stream().anyMatch(item.getType().name()::contains);
        if ((UNSUPPORTED_SPRITES.contains(item.getType()) || unsupportedCategories) && !NON_UNSUPPORTED.contains(item.getType())) {
            return null;
        }

        if (material.isBlock()) {
            return BlockResolver.resolveBlockSprite(material);
        }

        return "items:item/" + materialName;
    }

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

    private Component getTranslatableItemName(ItemStack item) {
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
        return Component.text(format);
    }
}
